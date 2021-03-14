#!/bin/bash
MEASURES="-m ndcg_cut.5 -m set_recall -m set_P -m set_F.1.15"
STD_INPUT_PATH="./results/run.txt"
STD_QRELS_PATH="./results/topics_labels.qrels"

if [ $# -ge 2 ]; then
	./lib/trec_eval $MEASURES $1 $2
else
	echo "Showing results for:"
	echo "> qrels: $STD_QRELS_PATH"
	echo "> input: $STD_QRELS_PATH"
	echo "------------------------"
	./lib/trec_eval $MEASURES $STD_QRELS_PATH $STD_INPUT_PATH
fi
