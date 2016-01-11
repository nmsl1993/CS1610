#CS1610 Gwendolyn Brooks Poetry Generator
#author: Noah Levy
#!/usr/bin/env python3
import numpy as np
import scipy.stats as stats
import nltk, pickle
import random, string
spunctuation = set(string.punctuation)
ptypes = 0
with open('ptypes.pkl','rb') as f:
    ptypes = pickle.load(f)
with open('vocab.pkl','rb') as f:
    vocab = pickle.load(f)

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


number_of_lines = random.randint(10,18)


initial_tree_type = random.choice(ptypes.keys())
initial_tree_type = 'VP'
poem_lines = []
cur_tree_type = initial_tree_type
for x in range(0,number_of_lines):
    if not cur_tree_type.isspace():
        #print cur_tree_type
        transition_column = normalize_pmf_dict(ptypes[cur_tree_type][1])
        cur_tree = random.choice(ptypes[cur_tree_type][0])
        #print '\n\n'
        #print cur_tree
        poem_lines.append(genTextFromTree(cur_tree))
        cur_tree_type = choose_outcome(transition_column) 
    else:
        poem_lines.append(' ')
        cur_tree_type = random.choice(ptypes.keys())
print '\n'.join(poem_lines)
