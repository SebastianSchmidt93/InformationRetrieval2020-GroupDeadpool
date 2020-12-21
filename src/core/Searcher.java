package core;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Searcher {
	QueryParser queryParser;
	IndexSearcher indexSearcher;
	Query query;
   
	public Searcher(String indexDirectoryPath) throws IOException
	{
	  Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
	  
	  DirectoryReader indexReader = DirectoryReader.open(indexDirectory);
	  this.indexSearcher = new IndexSearcher(indexReader);
	  
	  String[] contentFields = {LuceneConstants.CONCLUSION_FIELD, LuceneConstants.PREMISE_FIELD};
	  
	  this.queryParser = new MultiFieldQueryParser(contentFields, new StandardAnalyzer()); //TODO
   }
   
	public TopDocs search( String searchQuery) throws IOException, ParseException 
	{
		query = this.queryParser.parse(searchQuery);
		
		return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
	}
	
	public Document getDocument(ScoreDoc scoreDoc) throws CorruptIndexException, IOException
	{
		return indexSearcher.doc(scoreDoc.doc);
	}
}
