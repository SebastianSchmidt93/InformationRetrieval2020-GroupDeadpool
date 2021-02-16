package uh_t1_deadpool.argument_search_engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.BytesRef;

public class ArgumentQueryParser
{
	/** How many of the top retrieved documents will be examined */
	public static final int DEFAULT_NUM_DOCS_REFERENCED = 5;
	/** How many of the most occurring terms will be added in all cases */
	public static final int DEFAULT_MIN_TERMS_ADDED = 0;
	public static final int DEFAULT_MAX_TERMS_ADDED = 10;
	public static final float DEFAULT_SCORE_CAP = 0.9f;
	public static final float DEFAULT_EXPANSION_BOOST = 0.2f;
	
	private int numDocsReferenced = DEFAULT_NUM_DOCS_REFERENCED;
	
	private String[] fields;
	private Analyzer analyzer;
	private IndexSearcher indexSearcher;
	private IndexReader indexReader;
	private float numDocs;
	
	public ArgumentQueryParser(String[] fields, Analyzer analyzer, IndexSearcher indexSearcher, IndexReader indexReader)
	{
		this.fields = fields;
		this.analyzer = analyzer;
		this.indexSearcher = indexSearcher;
		this.indexReader = indexReader;
		this.numDocs = this.indexReader.numDocs();
	}
	
	public Query parse(String searchQuery) throws IOException, ParseException 
	{
		// Get the highest scored documents TODO Analyzer
		//QueryParser queryParser = new MultiFieldQueryParser(this.fields, this.analyzer);
		//Query query = queryParser.parse(searchQuery);
		Query query = this.simpleParse(searchQuery);
		List<String> queryTerms = Searcher.tokenizeString(this.analyzer, searchQuery);
		List<TermScore> queryTermsProbs = this.getTermProb(queryTerms);
		
		ScoreDoc[] docs = indexSearcher.search(query, numDocsReferenced).scoreDocs;
		
		// Queue that dynamically sorts the accumulated scores of all occurring terms
		PriorityQueue<TermScore> queue = new PriorityQueue<TermScore>();
		// Map that contains the handles to the terms in the queue
		HashMap<String, TermScore> termScoreMap = new HashMap<String, TermScore>();
		
		// Iterate over relevant documents
		for(int i = 0; i < docs.length && i < numDocsReferenced; i++)
		{
			ScoreDoc doc = docs[i];
			
			// Iterate over all terms occurring in document
			for(String field : this.fields)
			{
				TermsEnum terms = indexReader.getTermVector(doc.doc, field).iterator();
				BytesRef term = terms.next();
				
				while( term != null )
				{
					// Actually compute scoring
					String termString = term.utf8ToString();
					boolean legalTerm = true;
					
					// Skip terms that are already in query
					for(String queryTerm : queryTerms)
						if(termString.equals(queryTerm))
							legalTerm = false;
					// Skip Stopwords
					for(String stopWord : ArgumentAnalyzer.STOP_WORDS_SET)
						if(termString.equals(stopWord))
							legalTerm = false;
					
					if(legalTerm)
					{
						TermScore current = termScoreMap.get(termString);
						if(current == null)
						{
							//current = new TermScore(termString, 0);
							current = new TermScore(termString, getTermSimilarity(queryTermsProbs, termString));
							termScoreMap.put(termString, current);
						}
						// Scale with score of document to emphasize importance
						/*
						current.score += doc.score *
								getScore(indexSearcher, field, current.name, terms);
						*/
					}
					
					term = terms.next();
				}
			}
		}
		
		
		// Sort terms by value
		for(String term : termScoreMap.keySet())
		{
			TermScore termScore = termScoreMap.get(term);

			queue.add(termScore);
		}
		
		return makeQuery((BooleanQuery)query, queue);
	}
	
	
	public TermScore getTermProb(String termString) throws IOException
	{
		/* 
		 * TODO Multifield Queries
		 * Expensive but accurate 
		 */
		//Query termQ = this.getMultiFieldQuery(termString);
		//float docFreq = this.indexSearcher.count(termQ);
		/*
		 * Quick but slightly inaccurate
		 */
		Term term = new Term(LuceneConstants.PREMISE_FIELD, termString);
		float docFreq = this.indexReader.docFreq(term);
		
		float pTerm = docFreq / numDocs;
		
		return new TermScore(termString, pTerm);
	}
	
	
	public List<TermScore> getTermProb(Iterable<String> terms) throws IOException
	{
		List<TermScore> res = new ArrayList<TermScore>();
		
		for(String term : terms)
		{
			res.add(this.getTermProb(term));
		}
		
		return res;
	}
	
	
	public float getTermSimilarity(TermScore queryTermProb, TermScore expTermProb) throws IOException
	{
		// Single Queries
		Query queryTermQ = new TermQuery(new Term(LuceneConstants.PREMISE_FIELD, queryTermProb.name));//this.getMultiFieldQuery(queryTermProb.name);
		Query expTermQ = new TermQuery(new Term(LuceneConstants.PREMISE_FIELD, expTermProb.name));//this.getMultiFieldQuery(expTermProb.name);
		// AND-Query
		BooleanQuery.Builder qBuilder = new BooleanQuery.Builder();
		qBuilder.add(queryTermQ, BooleanClause.Occur.MUST);
		qBuilder.add(expTermQ, BooleanClause.Occur.MUST);
		BooleanQuery intersectQ = qBuilder.build();
		
		float pQueryTerm = queryTermProb.score;
		float pExpTerm = expTermProb.score;
		float pIntersect = this.indexSearcher.count(intersectQ) / numDocs;
		
		float sim = (pIntersect/pQueryTerm) * (float)Math.pow(Math.log(1/pExpTerm), 2);//(float)Math.log(1/pExpTerm);//(float)Math.pow(Math.log(1/pExpTerm), 2);//(pIntersect/pQueryTerm); // * (float) Math.log(1/pExpTerm); //(float) Math.log(pIntersect/(pQueryTerm*pExpTerm) + 1);
		
		return sim;
	}
	
	
	public float getTermSimilarity(Iterable<TermScore> queryTermProbs, String expTermString) throws IOException
	{
		float sim = 0;
		TermScore expTermProb = this.getTermProb(expTermString);
		
		for(TermScore queryTermProb : queryTermProbs)
		{
			sim += this.getTermSimilarity(queryTermProb, expTermProb);
		}
		
		return sim;
	}
	
	
	public Query getMultiFieldQuery(String term, float boost)
	{
		BooleanQuery.Builder fieldQuery = new BooleanQuery.Builder();
		
		for(String field : this.fields)
		{
			Query termQuery = new TermQuery(new Term(field, term));
			termQuery = new BoostQuery(termQuery, LuceneConstants.FIELD_BOOSTS.get(field)); // Boost field
			
			BooleanClause clause = new BooleanClause(termQuery, BooleanClause.Occur.SHOULD);
			fieldQuery.add(clause);
		}
		
		Query multiFieldQuery = fieldQuery.build();
		Query boostedMultiFieldQuery = new BoostQuery(multiFieldQuery, boost); // Boost query
		
		return boostedMultiFieldQuery;
	}
	
	
	public Query getMultiFieldQuery(String term)
	{
		return this.getMultiFieldQuery(term, 1);
	}
	
	
	/**
	 * Creates a query based on the scores of the given terms
	 * @param initQuery
	 * @param queue
	 * @return
	 * @throws IOException 
	 */
	private Query makeQuery(BooleanQuery initQuery, PriorityQueue<TermScore> queue) throws IOException
	{
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		float maxScore = queue.peek().score;
		float minScore = DEFAULT_SCORE_CAP*maxScore;
		
		for(BooleanClause clause : initQuery.clauses())
		{
			queryBuilder.add(clause);
		}
		
		
		// Get all Elements that have an above-average score
		// but add at least DEFAULT_MIN_TERMS_ADDED
		int count = 0;
		TermScore termScore = null;
		
		while( queue.peek() != null )
		{
			count++;
			termScore = queue.poll();
			float boost = (termScore.score - minScore) / (maxScore - minScore);
			
			
			if(count > DEFAULT_MIN_TERMS_ADDED && termScore.score <= minScore
					|| count > DEFAULT_MAX_TERMS_ADDED)
				break;
			
			Query newQuery = this.getMultiFieldQuery(termScore.name, DEFAULT_EXPANSION_BOOST*boost);
			
			queryBuilder.add(newQuery, BooleanClause.Occur.SHOULD);
		}
		
		return queryBuilder.build();
	}
	
	/**
	 * Calculates the similarity score between the document and the term
	 * @param doc
	 * @param term
	 * @return
	 * @throws IOException 
	 */
	public float getScore(IndexSearcher indexSearcher, String field, String termString, TermsEnum termAttr) throws IOException
	{
		// Get the default scoring model 
		IndexReader reader = indexSearcher.getIndexReader();
		Term term = new Term(field, termString);
		
		// get the used model
		Similarity.SimScorer scorer = indexSearcher.getSimilarity().scorer(
				1, 
				indexSearcher.collectionStatistics(field), 
				indexSearcher.termStatistics(term, reader.docFreq(term), reader.totalTermFreq(term)));
		
		PostingsEnum postings = termAttr.postings(null);
		int currentDoc = postings.nextDoc();
		float termFreq = 0;
		
		// Iterate over the postings of the term
		// (may not be necessary as the only expected docId will be 0, but who knows)
		while(currentDoc != DocIdSetIterator.NO_MORE_DOCS)
		{
			// As we are in the termvector of a document, the docId will allways be 0
			if(currentDoc == 0)
			{
				termFreq = postings.freq();
				break;
			}
			
			currentDoc = postings.nextDoc();
		}
		
		return scorer.score(termFreq, 1);
	}
	
	public Query simpleParse(String query) throws IOException, ParseException
	{
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		
		TokenStream tokens = this.analyzer.tokenStream(this.fields[0], query);
		
		CharTermAttribute charTermAttribute = tokens.addAttribute(CharTermAttribute.class);

		tokens.reset();
		
		while (tokens.incrementToken()) 
		{
			String term = charTermAttribute.toString();
		    
		    queryBuilder.add(getMultiFieldQuery(term, 1), BooleanClause.Occur.SHOULD);
		}
		
		tokens.end();
		tokens.close();
		
		return queryBuilder.build();
	}
	
	// TODO Experimental: expand per term
	public Query synonymParse(String query) throws IOException, ParseException
	{
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		List<String> tokens = Searcher.tokenizeString(this.analyzer, query);
		
		for( String term : tokens )
		{
			Query termQuery = this.parse(term);
		    
		    queryBuilder.add(termQuery, BooleanClause.Occur.MUST); //TODO
		}
		
		return queryBuilder.build();
	}
	
	/**
	 * Private class used to represent terms and their attributes
	 *
	 */
	private class TermScore implements Comparable<TermScore>
	{
		public String name;
		public float score;
		
		public TermScore(String name, float score)
		{
			this.name = name;
			this.score = score;
		}
		
		@Override
		public int compareTo(TermScore other) {
			float diff = ((TermScore)other).score - this.score;
			int comp = 0;
			
			if(diff > 0)
			{
				comp = 1;
			}
			else if (diff < 0)
			{
				comp = -1;
			}
			
			return comp;
		}
		
	}
}
