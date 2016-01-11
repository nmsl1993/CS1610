import nltk
import string
spunctuation = set(string.punctuation)
d = nltk.corpus.cmudict.dict()
hack_length = 2
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
        #print l
        return l
    elif type(ltree[0]) is nltk.tree.Tree:
            return list_leaf_pos(ltree[0])
    else:
        assert type(ltree[0]) is unicode
        return (ltree[0],ltree.label())
def getLabel(t):
    if type(t) is nltk.tree.Tree:
        return t.label()
    else:
        return str(t)
#Influenced by (more than 50% my original code) from https://github.com/hyperreality/Poetry-Tools/blob/master/poetics.py
def rhyme(w1, w2, level=1): # finds if two words rhyme
    syllables1 = 0
    syllables2 = 0
    try:
        if type(w2) is list:
            print 'DFA',w2
        syllables1 = d[w1][0]
        syllables2 = d[w2][0]

    except KeyError:
        #print 'KeyError!'
        if min([len(w1),len(w2)]) < hack_length:
            return False
        return w1[-hack_length::] == w2[-hack_length::]
    #print syllables1, syllables2
    #print syllables1[-level::]
    return syllables1[-level::] == syllables2[-level::]
def listRhymesByPOS(word,part_of_speech=0,vocab=0):
    out = []
    #for v in vocab[part_of_speech]:
    for v in d:
        v = ''.join(c for c in v if c not in spunctuation)
        if not v.isspace() and v not in spunctuation  and  rhyme(v,word,2):
            out.append(v)
    return out
    
