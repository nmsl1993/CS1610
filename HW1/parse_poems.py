#! /usr/bin/env python2
import nltk
from stat_parser import Parser
import os
poem_names = []
poem_names.extend(['gb_poems/' + s for s in os.listdir("gb_poems")])
poem_names.extend(['lh_poems/' + s for s in os.listdir("lh_poems")])
parser = Parser()

parsed_lines = dict()
for poem_name in poem_names:
    with open(poem_name,'rb') as f: 
        parsed_lines[poem_name] = list()
        print poem_name 
        for line in list(filter(None,f.read().split('\n'))):
            if len(line) > 1:
                p =  parser.parse(line)
                p.pretty_print() 
                parsed_lines[poem_name].append(p)
            else:
                print "TYPE ERROR!"
 
