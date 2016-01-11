#! /usr/bin/env python2
import nltk
from stat_parser import Parser
import os
import pickle
poem_names = []
poem_names.extend(['gb_poems/' + s for s in os.listdir("gb_poems")])
poem_names.extend(['lh_poems/' + s for s in os.listdir("lh_poems")])

def list_leaf_pos(ltree):
    if len(ltree) > 1:
        l = list()
        for lt in ltree:
            r = list_leaf_pos(lt)
            if type(r) is tuple:
                l.append(r)
            elif type(r) is list:
                l.extend(r)
            else:
                raise Exception('leaf','issue')
        return l
    elif type(ltree[0]) is nltk.tree.Tree:
            return list_leaf_pos(ltree[0])
    else:
        assert type(ltree[0]) is unicode
        return (ltree[0],ltree.label())
parser = Parser()
vocab = dict()
parsed_lines = dict()
def getLabel(t):
    if type(t) is nltk.tree.Tree:
        return t.label()
    else:
        return str(t)
for poem_name in poem_names:
    with open(poem_name,'rb') as f: 
        parsed_lines[poem_name] = list()
        print poem_name 
        for line in f.read().split('\n'):
            line = line.decode('utf-8')
            line = line.replace("'","")
            line = line.replace('"','')
            try:
                if line == "" or line.isspace():
                    raise TypeError
                p =  parser.parse(line)
                p.pretty_print() 
                tagged = list_leaf_pos(p) 
                for (word,pos) in tagged:
                    if pos not in vocab:
                        vocab[pos] = set([word])
                    else:
                        vocab[pos].add(word)
                print tagged
                parsed_lines[poem_name].append(p)
            except TypeError:
                parsed_lines[poem_name].append(" ")
                print "Blank Line!"
print "DONE!"
fvocab = open('vocab.pkl','wb')
pickle.dump(vocab,fvocab)
fvocab.close()
ftrees = open('trees.pkl', 'wb')
pickle.dump(parsed_lines,ftrees)
ftrees.close()
