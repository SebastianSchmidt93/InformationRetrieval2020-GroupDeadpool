#!/bin/bash
if [ $# -ge 2 ]; then
	./lib/trec_eval -m ndcg_cut.10 $1 $2
else
	./lib/trec_eval -m ndcg_cut.10 topics_labels.qrels run.txt
fi
