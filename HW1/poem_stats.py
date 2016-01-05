import sys
from nltk import *
from nltk.corpus import cmudict
d = cmudict.dict()
def nsyl(word):
      return [len(list(y for y in x if y[-1].isdigit())) for x in d[word.lower()]] 
def nsyl_line(line):
    tokens = word_tokenize(line)
    syl_sum = sum([nsyl(x) for x in tokens]) 
    return syl_sum

poem_path = sys.argv[1]
with open(poem_path) as f:
    lines = f.readlines()
    num_syl_text = [nsyl_line(x) for x in lines]
    print(num_syl_text)

