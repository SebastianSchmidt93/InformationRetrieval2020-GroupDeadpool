package uh_t1_deadpool.argument_search_engine;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Searcher {
	public static final String[] FIELDS = {LuceneConstants.CONCLUSION_FIELD, LuceneConstants.PREMISE_FIELD};
	
	ArgumentQueryParser queryParser;
	IndexSearcher indexSearcher;
	Query query;
   
	public Searcher(String indexDirectoryPath) throws IOException
	{
	  Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
	  
	  DirectoryReader indexReader = DirectoryReader.open(indexDirectory);
	  this.indexSearcher = new IndexSearcher(indexReader);
	  this.indexSearcher.setSimilarity(new LMDirichletSimilarity(4000));
	  
	  // TODO Custom Queryparser
	  this.queryParser = new ArgumentQueryParser(FIELDS, new ArgumentAnalyzer(), indexSearcher, indexReader);
   }
   
	public TopDocs search(String searchQuery) throws IOException, ParseException 
	{
		query = this.queryParser.simpleParse(searchQuery); //TODO
		//TODO test
		System.out.println("Query :" + query.toString());
		
		return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
	}
	
	public Document getDocument(ScoreDoc scoreDoc) throws CorruptIndexException, IOException
	{
		return indexSearcher.doc(scoreDoc.doc);
	}
	
	public static List<String> tokenizeString(Analyzer analyzer, String string) 
	{
		List<String> result = new ArrayList<String>();
	    try 
	    {
	    	TokenStream stream  = analyzer.tokenStream(null, new StringReader(string));
	    	stream.reset();
	    	while (stream.incrementToken()) 
	    	{
	    		result.add(stream.getAttribute(CharTermAttribute.class).toString());
	    	}
	    	
	    	stream.end();
	    	stream.close();
	    } 
	    catch (IOException e) 
	    {
	    	// not thrown b/c we're using a string reader...
	    	throw new RuntimeException(e);
	    }
	    return result;
	}
}
