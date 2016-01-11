#! /usr/bin/env python2
import nltk
from stat_parser import Parser
import os
import pickle, string
import hw1_util

spunctuation = set(string.punctuation)
poem_names = []
poem_names.extend(['gb_poems/' + s for s in os.listdir("gb_poems")])
poem_names.extend(['lh_poems/' + s for s in os.listdir("lh_poems")])

#poem_names = ['gb_poems/Sadie_and_Maud.txt']
parser = Parser()
vocab = dict()
parsed_lines = dict()
rhymes = dict()
for poem_name in poem_names:
    with open(poem_name,'rb') as f: 
        parsed_lines[poem_name] = list()
        rhyme_count = ord('A')
        rhymes[poem_name] = list()
        print poem_name 
        for line in f.read().split('\n'):
            line = line.decode('utf-8')
            line = line.replace("'","")
            line = line.replace('"','')
            last_word = ''
            try:
                if line == "" or line.isspace():
                    raise TypeError
                p =  parser.parse(line)
                p.pretty_print() 
                tagged = hw1_util.list_leaf_pos(p) 
                if type(tagged) is tuple:
                    tagged = [tagged]
                for (word,pos) in tagged:
                    if pos not in vocab:
                        vocab[pos] = set([word])
                    else:
                        vocab[pos].add(word)
                    if word not in spunctuation:
                        last_word = word
                if len(rhymes[poem_name]) == 0:
                    rhymes[poem_name] = [(chr(rhyme_count), last_word)]
                    rhyme_count += 1
                else:
                    for (x,wrd) in rhymes[poem_name]:
                        if wrd is not ' ' and hw1_util.rhyme(wrd,last_word):
                            rhymes[poem_name].append((x,last_word))
                            break
                    else:
                        rhymes[poem_name].append((chr(rhyme_count),last_word))
                        rhyme_count += 1
                parsed_lines[poem_name].append(p)
            except TypeError:
                parsed_lines[poem_name].append(" ")
                #rhymes[poem_name].append((ord(' '),' '))
                print "Blank Line!"
            print rhymes[poem_name]
print "DONE!"
fvocab = open('vocab.pkl','wb')
pickle.dump(vocab,fvocab)
fvocab.close()
ftrees = open('trees.pkl', 'wb')
pickle.dump(parsed_lines,ftrees)
ftrees.close()
frhymes = open('rhymes.pkl','wb')
pickle.dump(rhymes,frhymes)
frhymes.close()
