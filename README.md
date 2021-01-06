# Argument Search Engine
##### uh-t1-deadpool

This repository provides a search engine for arguments. For a given query (e.g. "Is Climate Change real?") it will retreive the most relevant pro- and con-arguments regarding the query. The two main functionalities this repo. provides are:
  - Retreival (my-software.sh)
  - Evaluation (eval.sh)

### Retreival
The shell-script "my-software.sh" will index a given argument dataset, retreive the most relevant arguments for a given set of queries and store the results in trec-format. To run the script, open your console in the root directory of the repo. and type:
```sh
$ ./my-software.sh -i INPUT_PATH -o OUTPUT_PATH
```
Where INPUT_PATH is the path to a directory containing .json files of the to-be-indexed arguments and a .xml file containing the queries. OUTPUT_PATH denotes the directory in which the results will be stored as "run.txt".

### Evaluation
The shell-script "eval.sh" will compare the results of the search engine with given relevance jugdments and return the NDCG-score. To run the script, open your console in the root directory of the repo. and type:
```sh
$ ./eval.sh JUDGEMENT_FILE RESULTS_FILE
```
Where JUDGEMENT_FILE is a .qrel-file containing the relevance judgements for the queries and RESULTS_FILE denotes the file containing the results of retreival, i.e. "run.txt".
