Noah Levy
CS1610

parse_poems.py

This script is run to create a dictionary of semantic trees + the frequency of their immediate
followers, saved in trees.pkl a dictionary of vocab organized by part of speech, saved in vocab.pkl,
and a dictionary of rhyme schemes, saved in rhymes.pkl. 
It makes heavy use of the python NLTK library for parsing semantic trees

hw1_util.py

This script contains utility functions for finding rhyming words, it uses the NLTK cmu dictionary
which I found to be very nonideal. It could easily find rhyming words in the corpus but many of them
seemed to be orderline gibberish.

analyze_trees.py

This script is used for postprocessing the dictionairy of trees. It consolidates trees with
different structures by global label

gen_poem2.py

This script is used for generating a poem from the semantic trees. It uses the choose_outcome()
method and treats the recorded frequences in the ptypes data structure as columns in a single PMF
vector to choose each successive line. After it has inserted the appropriate parts of speech in a syntax tree line
it postprocesses the data by changing words to be rhyming using hw1_util.rhyme
