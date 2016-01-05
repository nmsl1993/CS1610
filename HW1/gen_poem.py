#CS1610 Gwendolyn Brooks Poetry Generator
#author: Noah Levy
#!/usr/bin/env python3
import numpy as np
import scipy.stats as stats
import nltk
import random

def choose_outcome(cdf_dict):
    print(cdf_dict)
    #Choose the outcome of a random event given all the possible outcomes as keys
    #And the probability of an outcome being the value
    assert(np.abs(np.sum(list(cdf_dict.values())) - 1) < 1e-8) #The CDF must sum to  1
    r = random.random() #generate a random number between {0,1}

    for key in cdf_dict: #Iterate through Outcome-PMF pairs
        r -= cdf_dict[key] #Subtract PMF value from random number
        if r <= 0: #if less/equal 0 than currently tested is outcome
            return key

#Transition matrix for the parts of speech used by a pithy line
part_of_speech_tmatrix_pithyline = {
        'noun' : {'verb' : .7, 'adverb' : .3},
        'verb' : {'noun' : .5, 'adj' : .5}, 
        'adj'  : {'noun' : .7, 'adj' : .3},
        'adverb' :{'verb' : .9, 'adverb' : .1}
    }

#Table for selecting the sub-category for a given part of speech (not done for adverbs)`
prob_of_type_given_part_of_speech = {
        'noun' : {'nature' : .2, 'domestic' : .5, 'other' : .3},
        'adj'  : {'nature' : .4, 'domestic' : .3, 'other' : .3},
        'verb' : {'domestic' : .3, 'other' : .7}
    }

#Vector for selecting ending punctuation (This should probably depend more on context.
prob_of_end_line_punctuation = { '.' : .3, ',' : .07, '-' : .08, '' : .55}



vocab = {
        'noun'   : dict(),
        'adj'    : dict(),
        'verb'   : dict(),
        'adverb' : list()
        }
#Populate vocab from textfiles

#nouns
with open('vocab/noun_nature.txt','r') as f: vocab['noun']['nature'] = list(filter(None,f.read().split('\n')))
with open('vocab/noun_domestic.txt','r') as f: vocab['noun']['domestic'] = list(filter(None,f.read().split('\n')))
with open('vocab/noun_other.txt','r') as f: vocab['noun']['other'] = list(filter(None,f.read().split('\n')))
with open('vocab/pronouns.txt','r') as f: vocab['noun']['other'].extend(list(filter(None,f.read().split('\n'))))

#adjectives
with open('vocab/adj_nature.txt','r') as f: vocab['adj']['nature'] = list(filter(None,f.read().split('\n')))
with open('vocab/adj_domestic.txt','r') as f: vocab['adj']['domestic'] = list(filter(None,f.read().split('\n')))
with open('vocab/adj_other.txt','r') as f: vocab['adj']['other'] = list(filter(None,f.read().split('\n')))

#verbs
with open('vocab/verb_domestic.txt','r') as f: vocab['verb']['domestic'] = list(filter(None,f.read().split('\n')))
with open('vocab/verb_other.txt','r') as f: vocab['verb']['other'] = list(filter(None,f.read().split('\n')))

#adverbs
with open('vocab/adverbs.txt','r') as f: vocab['adverb'] = list(filter(None,f.read().split('\n')))



def create_line():
    part_of_speech = 'adj' #Having adj as initial state ensures all lines begin with either a noun or an adjective
    line = ""
    idx = 0
    prob_of_terminate = 0 
    #only terminate on noun or verbs (similar to gb style)
    while part_of_speech not in ['noun', 'verb'] or  random.random() > prob_of_terminate:

        #The part of speech is a Markov output based upon the last POS
        part_of_speech = choose_outcome(part_of_speech_tmatrix_pithyline[part_of_speech]) 
        word = ''

        #If this part of speech has sub categories, traverse that transition matrix as well.
        if type(vocab[part_of_speech]) == dict:
            word = random.choice(vocab[part_of_speech][choose_outcome(prob_of_type_given_part_of_speech[part_of_speech])])
        else:
            word = random.choice(vocab[part_of_speech])
        line = line + ' ' + word
        #Probability of termination increases asymptotically to 1
        prob_of_terminate = (-.5 + stats.norm.cdf(idx/7) )*2
        idx = idx + 1
    return line
    

number_of_lines = random.randint(10,18)
stanza_every_n_lines = random.randint(3,5)

poem_lines = []
for x in range(0,number_of_lines):
    punctuation = choose_outcome(prob_of_end_line_punctuation)
    line = create_line() 
    poem_lines.append(line + punctuation + '\n')
    if x % stanza_every_n_lines == stanza_every_n_lines - 1: 
        poem_lines.append("\n");

print(''.join(poem_lines))

