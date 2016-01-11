#! /usr/bin/env python2
import nltk
from stat_parser import Parser
import os
import pickle
poem_names = []
poem_names.extend(['gb_poems/' + s for s in os.listdir("gb_poems")])
poem_names.extend(['lh_poems/' + s for s in os.listdir("lh_poems")])
parser = Parser()
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
                parsed_lines[poem_name].append(p)
            except TypeError:
                parsed_lines[poem_name].append(" ")
                print "Blank Line!"
 
print "DONE!"
ftrees = open('trees.pkl', 'wb')
pickle.dump(parsed_lines,ftrees)
ftrees.close()
