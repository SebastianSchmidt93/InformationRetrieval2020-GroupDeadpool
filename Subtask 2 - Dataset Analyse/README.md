##Problems and solutions:
* JSONs are humongous - how do we process them?
    * Use IJSON python package. It processes in chunks.
* Which parts of the argument do we use?
    * Concatenate conclusion and premise for every argument.
* Are URLs words?
    * Remove URLs beforehand using regex but keep them for possible later purposes.
* How to we extract words?
    * Use NLTK python package with its word_tokenizer.
* How do we make sure the words are unique?
    * Use python sets.
* Which stopwords are to be removed?
    * Use NLTKs built-in stop words.
* Special characters are interpreted as words (e.g. ",", "...", "?").
    * Remove all "words" that do not contain a digit or letter.
* Some sentences are badly formatted resulting in words like "there.I".
    * Split words, that are of form "^\w+\.\w+$".
* Abbreviations (e.g. "n't") are interpreted as unique words.
    * See them as regular form of the word (e.g. "shop" and "shops") and thus keep them.
    
##Open problems
* Many words make no sense and/or contain mostly special characters.
