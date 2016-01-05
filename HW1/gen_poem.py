import numpy as np
import random

def choose_outcome(cdf_dict):
    #Choose the outcome of a random event given all the possible outcomes as keys
    #And the probability of an outcome being the value
    assert(np.abs(np.sum(list(cdf_dict.values())) - 1) < 1e-8) #The CDF must sum to  1
    r = random.random()

    for key in cdf_dict:
        r -= cdf_dict[key]
        if r <= 0:
            return key

prob_of_type_given_noun = {'nature' : .2, 'domestic' : .5, 'other' : .3}
prob_of_type_given_adj = {'nature' : .4, 'domestic' : .3, 'other' : .3}
prob_of_type_given_verb = {'domestic' : .3, 'other' : .7}

part_of_speech_tmatrix_pithyline = {
        'noun' : {'verb' : .7, 'adverb' : .3},
        'verb' : {'noun' : .5, 'adj' : .5}, 
        'adj'  : {'noun' : .7, 'adj' : .3},
        'adverb' :{'verb' : .9, 'adverb' : .1}.


adj = dict()
noun = dict()
verb = dict()
adverb = []

with open('vocab/noun_nature.txt','r') as f: noun['nature'] = list(filter(None,f.read().split('\n')))
with open('vocab/noun_domestic.txt','r') as f: noun['domestic'] = list(filter(None,f.read().split('\n')))
with open('vocab/noun_other.txt','r') as f: noun['other'] = list(filter(None,f.read().split('\n')))


with open('vocab/adj_nature.txt','r') as f: adj['nature'] = list(filter(None,f.read().split('\n')))
with open('vocab/adj_domestic.txt','r') as f: adj['domestic'] = list(filter(None,f.read().split('\n')))
with open('vocab/adj_other.txt','r') as f: adj['other'] = list(filter(None,f.read().split('\n')))


with open('vocab/verb_domestic.txt','r') as f: verb['domestic'] = list(filter(None,f.read().split('\n')))
with open('vocab/verb_other.txt','r') as f: verb['other'] = list(filter(None,f.read().split('\n')))

with open('vocab/adverb.txt','r') as f: adverb = list(filter(None,f.read().split('\n')))


print(adj)
print(verb)
print(noun)

#Enter line creator as adjective
def create_line():
    last_pos = 'adj'
    pos_tmatrix_column = part_of_speech_tmatrix_pithyline[last_pos]
    

print(create_line)
    
