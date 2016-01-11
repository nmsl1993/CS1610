import nltk
import pickle

parsed_lines = 0
def getLabel(t):
    if type(t) is nltk.tree.Tree:
        return t.label()
    else:
        return str(t)
with open('trees.pkl','rb') as f:
    poem_trees = pickle.load(f)
with open('vocab.pkl','rb') as f:
    vocab = pickle.load(f)



ptypes = dict()
for poem in poem_trees:
    for i,line in enumerate(poem_trees[poem]):
        print type(line)
        if type(line) is  nltk.tree.Tree:
            if line.label() not in ptypes:
                #Packed as examples of tree, next type of line
                next_type = ' '
                if (i+1) < len(poem_trees[poem]):
                    next_type = getLabel(poem_trees[poem][i+1])
                ptypes[line.label()] = ([line], dict())
                ptypes[line.label()][1][next_type] = 1
            else:
                next_type = ' '
                if (i+1) < len(poem_trees[poem]):
                    next_type = getLabel(poem_trees[poem][i+1])
                ptypes[line.label()][0].append(line)
                if next_type not in ptypes[line.label()][1]:
                    ptypes[line.label()][1][next_type] = 1
                else:
                    ptypes[line.label()][1][next_type] += 1
            
fptypes = open('ptypes.pkl','wb')
pickle.dump(ptypes,fptypes)
fptypes.close()
