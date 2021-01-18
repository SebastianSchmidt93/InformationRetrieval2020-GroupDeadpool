package uh_t1_deadpool.argument_search_engine;

import java.io.IOException;
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
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.util.BytesRef;

public class ArgumentQueryParser
{
	/** How many of the top retrieved documents will be examined */
	public static final int DEFAULT_NUM_DOCS_REFERENCED = 20;
	/** How many of the most occurring terms will be added in all cases */
	public static final int DEFAULT_MIN_TERMS_ADDED = 0;
	
	private int numDocsReferenced = DEFAULT_NUM_DOCS_REFERENCED;
	private String[] fields;
	private Analyzer analyzer;
	private IndexSearcher indexSearcher;
	private IndexReader indexReader;
	
	public ArgumentQueryParser(String[] fields, Analyzer analyzer, IndexSearcher indexSearcher, IndexReader indexReader)
	{
		this.fields = fields;
		this.analyzer = analyzer;
		this.indexSearcher = indexSearcher;
		this.indexReader = indexReader;
	}
	
	public Query parse(String searchQuery) throws IOException, ParseException 
	{
		// Get the highest scored documents TODO Analyzer
		//QueryParser queryParser = new MultiFieldQueryParser(this.fields, this.analyzer);
		//Query query = queryParser.parse(searchQuery);
		Query query = this.simpleParse(searchQuery);
		ScoreDoc[] docs = indexSearcher.search(query, LuceneConstants.MAX_SEARCH).scoreDocs;
		
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
				List<String> queryTerms = Searcher.tokenizeString(this.analyzer, searchQuery);
				
				while( terms.next() != null );
				{
					// Actually compute scoring
					BytesRef term = terms.term();
					String termString = term.utf8ToString();
					
					// Skip terms that are already in query
					for(String queryTerm : queryTerms)
						if(termString.equals(queryTerm))
							continue;
					
					TermScore current = termScoreMap.get(termString);
					if(current == null)
					{
						current = new TermScore(term, termString, 0);
						termScoreMap.put(termString, current);
					}
					// Scale with score of document to emphasize importance
					current.score += doc.score *
							getScore(indexSearcher, field, new Term(field, current.term), terms);
				}
			}
		}
		
		int termCount = termScoreMap.size();
		float totalScore = 0;
		
		// Sort terms by value
		for(String term : termScoreMap.keySet())
		{
			TermScore termScore = termScoreMap.get(term);
			
			totalScore += termScore.score;
			queue.add(termScore);
		}
		
		float avgScore = totalScore / termCount;
		
		return makeQuery(avgScore, (BooleanQuery)query, queue);
	}
	
	/**
	 * Creates a query based on the scores of the given terms
	 * @param avgScore
	 * @param initQuery
	 * @param queue
	 * @return
	 */
	private Query makeQuery(float avgScore, BooleanQuery initQuery, PriorityQueue<TermScore> queue)
	{
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		
		for(BooleanClause clause : initQuery.clauses())
		{
			queryBuilder.add(clause);
		}
		
		
		// Get all Elements that have an above-average score
		// but add at least DEFAULT_MIN_TERMS_ADDED
		int count = 0;
		
		for(TermScore termScore : queue)
		{
			count++;
			
			if(count > DEFAULT_MIN_TERMS_ADDED && termScore.score < avgScore)
				break;
			
			BooleanQuery.Builder fieldQuery = new BooleanQuery.Builder();
			
			for(String field : this.fields)
			{
				TermQuery termQuery = new TermQuery(new Term(field, termScore.term));
				BooleanClause clause = new BooleanClause(termQuery, BooleanClause.Occur.SHOULD);
				fieldQuery.add(clause);
			}
			
			queryBuilder.add(fieldQuery.build(), BooleanClause.Occur.SHOULD);
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
	public float getScore(IndexSearcher indexSearcher, String field, Term term, TermsEnum termAttr) throws IOException
	{
		// Get the default scoring model 
		// TODO get the used model
		Similarity.SimScorer scorer = indexSearcher.getSimilarity().scorer(
				1, 
				indexSearcher.collectionStatistics(field), 
				indexSearcher.termStatistics(term, termAttr.docFreq(), termAttr.totalTermFreq()));
		
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
	
	public Query simpleParse(String query) throws IOException
	{
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		
		TokenStream tokens = this.analyzer.tokenStream(this.fields[0], query);
		
		CharTermAttribute charTermAttribute = tokens.addAttribute(CharTermAttribute.class);

		tokens.reset();
		
		while (tokens.incrementToken()) 
		{
			BooleanQuery.Builder fieldQuery = new BooleanQuery.Builder();
		    String term = charTermAttribute.toString();
		    
		    for(String field : this.fields)
			{
				TermQuery termQuery = new TermQuery(new Term(field, term));
				BooleanClause clause = new BooleanClause(termQuery, BooleanClause.Occur.SHOULD);
				fieldQuery.add(clause);
			}
		    
		    queryBuilder.add(fieldQuery.build(), BooleanClause.Occur.SHOULD);
		}
		
		tokens.end();
		tokens.close();
		
		return queryBuilder.build();
	}
	
	/**
	 * Private class used to represent terms and their attributes
	 *
	 */
	private class TermScore implements Comparable<TermScore>
	{
		public String name;
		public BytesRef term;
		public float score;
		
		public TermScore(BytesRef term, String name, float score)
		{
			this.name = name;
			this.term = term;
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
