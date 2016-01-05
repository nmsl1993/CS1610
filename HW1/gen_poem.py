import numpy as np
import scipy.stats as stats
import random

def choose_outcome(cdf_dict):
    print(cdf_dict)
    #Choose the outcome of a random event given all the possible outcomes as keys
    #And the probability of an outcome being the value
    assert(np.abs(np.sum(list(cdf_dict.values())) - 1) < 1e-8) #The CDF must sum to  1
    r = random.random()

    for key in cdf_dict:
        r -= cdf_dict[key]
        if r <= 0:
            return key
part_of_speech_tmatrix_pithyline = {
        'noun' : {'verb' : .7, 'adverb' : .3},
        'verb' : {'noun' : .5, 'adj' : .5}, 
        'adj'  : {'noun' : .7, 'adj' : .3},
        'adverb' :{'verb' : .9, 'adverb' : .1}
    }
prob_of_type_given_part_of_speech = {
        'noun' : {'nature' : .2, 'domestic' : .5, 'other' : .3},
        'adj'  : {'nature' : .4, 'domestic' : .3, 'other' : .3},
        'verb' : {'domestic' : .3, 'other' : .7}
    }


vocab = {
        'noun'   : dict(),
        'adj'    : dict(),
        'verb'   : dict(),
        'adverb' : list()
        }
with open('vocab/noun_nature.txt','r') as f: vocab['noun']['nature'] = list(filter(None,f.read().split('\n')))
with open('vocab/noun_domestic.txt','r') as f: vocab['noun']['domestic'] = list(filter(None,f.read().split('\n')))
with open('vocab/noun_other.txt','r') as f: vocab['noun']['other'] = list(filter(None,f.read().split('\n')))


with open('vocab/adj_nature.txt','r') as f: vocab['adj']['nature'] = list(filter(None,f.read().split('\n')))
with open('vocab/adj_domestic.txt','r') as f: vocab['adj']['domestic'] = list(filter(None,f.read().split('\n')))
with open('vocab/adj_other.txt','r') as f: vocab['adj']['other'] = list(filter(None,f.read().split('\n')))


with open('vocab/verb_domestic.txt','r') as f: vocab['verb']['domestic'] = list(filter(None,f.read().split('\n')))
with open('vocab/verb_other.txt','r') as f: vocab['verb']['other'] = list(filter(None,f.read().split('\n')))

with open('vocab/adverbs.txt','r') as f: vocab['adverb'] = list(filter(None,f.read().split('\n')))


#Enter line creator as adjective


def create_line():
    part_of_speech = 'adj'
    line = ""
    idx = 0
    prob_of_terminate = 0 
    while part_of_speech not in ['noun', 'verb'] or  random.random() > prob_of_terminate:
        part_of_speech = choose_outcome(part_of_speech_tmatrix_pithyline[part_of_speech])
        word = ''
        if type(vocab[part_of_speech]) == dict:
            word = random.choice(vocab[part_of_speech][choose_outcome(prob_of_type_given_part_of_speech[part_of_speech])])
        else:
            word = random.choice(vocab[part_of_speech])
        line = line + ' ' + word
        prob_of_terminate = (-.5 + stats.norm.cdf(idx/7) )*2
        idx = idx + 1
    return line
    

number_of_lines = random.randint(10,18)
stanza_every_n_lines = random.randint(3,5)

print(number_of_lines)

poem_lines = []
for x in range(0,number_of_lines):
    poem_lines.append(create_line() + '\n')
    if x % stanza_every_n_lines == stanza_every_n_lines - 1: 
        poem_lines.append("\n");
print(''.join(poem_lines))
