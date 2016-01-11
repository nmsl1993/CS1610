#!/usr/bin/env python3
#CS1610 Gwendolyn Brooks Poetry Generator
#author: Noah Levy
import numpy as np
import scipy.stats as stats
import nltk, pickle
from stat_parser import Parser
import random, string
import hw1_util
from hw1_util import spunctuation
ptypes = 0
vocab  = 0
rhymes = 0
with open('ptypes.pkl','rb') as f:
    ptypes = pickle.load(f)
with open('vocab.pkl','rb')  as f:
    vocab = pickle.load(f)
with open('rhymes.pkl','rb') as f:
    rhymes = pickle.load(f)

def normalize_pmf_dict(pmf_dict):
    sum = 0.0
    for x in pmf_dict.keys():
        sum += pmf_dict[x]
    for x in pmf_dict.keys():
        pmf_dict[x] = (float(pmf_dict[x])/sum)
    return pmf_dict
def choose_outcome(pmf_dict):
    #Choose the outcome of a random event given all the possible outcomes as keys
    #And the probability of an outcome being the value
    assert(np.abs(np.sum(list(pmf_dict.values())) - 1) < 1e-8) #The pmfs must sum to  1
    r = random.random() #generate a random number between {0,1}

    for key in pmf_dict: #Iterate through Outcome-PMF pairs
        r -= pmf_dict[key] #Subtract PMF value from random number
        if r <= 0: #if less/equal 0 than currently tested is outcome
            return key

def genTextFromTree(ltree):
        if len(ltree) > 1:
            #print ltree.label()
            return  ''.join([genTextFromTree(lt) for lt in ltree])
        if ltree.label() not in vocab:
            #print ltree
            return genTextFromTree(ltree[0])
        else:
            vocab_for_pos =  vocab[ltree.label()] 
            out =  random.choice(list(vocab_for_pos)).lower()
            if out not in spunctuation:
                out = ' ' + out
            return out
#### RHYMES OVERIDE #####
#Using markov model for generating rhyme schemes
#Measuring rhymee scheme from existing poetry was failing
len_of_rhyme_scheme = random.randrange(8,15)
pmf_prev_rhyme_idx = {
    -3 : .1,
    -2 : .5,
    -1 : .2,
    0 : .2
}
rhyme_offsets = [choose_outcome(pmf_prev_rhyme_idx) for x in range(0,len_of_rhyme_scheme)] 

cur_letter = 'A' 

rhyme_scheme = []
for index, rhyme_offset in enumerate(rhyme_offsets):
    if rhyme_offset == 0 or index + rhyme_offset < 0:
        cur_letter = chr(ord(cur_letter) + 1)
        rhyme_scheme.append(cur_letter)
    else:
        rhyme_scheme.append(rhyme_scheme[index+rhyme_offset])
###########################
#rhyme_scheme = rhymes[random.choice(rhymes.keys())]
#rhyme_scheme = [tup[0] for tup in rhyme_scheme] 
number_of_lines = len(rhyme_scheme)
current_rhyming = dict()
initial_tree_type = random.choice(ptypes.keys())
poem_lines = []
cur_tree_type = initial_tree_type
rhyme_idx = 0

print rhyme_scheme
while rhyme_idx < number_of_lines:
    if not cur_tree_type.isspace():
        transition_column = normalize_pmf_dict(ptypes[cur_tree_type][1])
        cur_tree = random.choice(ptypes[cur_tree_type][0])
        
        line = genTextFromTree(cur_tree)
        strip_line = ''.join(c for c in line if c not in spunctuation)
        rword = strip_line.split(' ')[-1::]
        while type(rword) is list:
            rword = rword[0]

        rhyming_letter = rhyme_scheme[rhyme_idx] #postprocess in rhyming words, ignore semantic tree for this part
        if rhyming_letter not in current_rhyming:
            current_rhyming[rhyming_letter] = rword 
        else:
            rhyming_words = hw1_util.listRhymesByPOS(current_rhyming[rhyming_letter])
            if len(rhyming_words) > 0:
                repl_word = random.choice(rhyming_words)
                #print '\n', 'trying to rhyme with', current_rhyming[rhyming_letter] 
                #print "REPLACING", rword, repl_word 
                line = line.replace(rword,repl_word)
                
        rhyme_idx = rhyme_idx + 1
        poem_lines.append(line)
        cur_tree_type = choose_outcome(transition_column) 
    else:
        poem_lines.append(' ')
        cur_tree_type = random.choice(ptypes.keys())
print '\n'.join(poem_lines)
