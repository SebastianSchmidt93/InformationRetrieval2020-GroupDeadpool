#!/bin/bash
if [ $# -ge 2 ]; then
	./lib/trec_eval -m ndcg_cut.10 $1 $2
else
	./app/lib/trec_eval -m ndcg_cut.10 ./results/topics_labels.qrels ./results/run.txt
fi
