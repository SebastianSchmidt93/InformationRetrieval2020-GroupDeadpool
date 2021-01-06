package uh_t1_deadpool.argument_search_engine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;

public class Main 
{
	public static void main(String[] args)
	{
		
		try
		{
			String[] paths = parseArgs(args);
			
			String dataDirPath = paths[0];
			String topicsFilePath = paths[1];
			String resultsFilePath = paths[2];
			String indexDirPath = "index";
			
			
			createIndex(dataDirPath, indexDirPath);
	        
			
			// Search and store results of topics
			runToucheChallenge(indexDirPath, topicsFilePath, resultsFilePath);
		}
		catch(Exception e) //TODO handle exceptions more specifically
		{
			e.printStackTrace();
		}
	}
	
	
	public static void createIndex(String dataDirPath, String indexDirPath)
	{
		// Create directory for index
		ensureDirExists(indexDirPath);
		
		try
		{
			if(indexExists(indexDirPath))
			{
				System.out.println("Index already Exists.");
			}
			else
			{
				Indexer indexer = new Indexer(indexDirPath);
				
				JsonFileFilter fileFilter = new JsonFileFilter();
				
				indexer.createIndex(dataDirPath, fileFilter);
				
				indexer.close();
				
				System.out.println("Index created.");
			}
		}
		catch (IOException e) 
		{
            e.printStackTrace();
        }
	}
	
	
	public static String toTRECFormat(Document document, int topicNumber, int rank, float score)
	{
		String resLine = topicNumber + " " + "q0" + " " + document.get("id") + " " + rank + " " + score + " " + LuceneConstants.GROUP_NAME + "\n";
		
		return resLine;
	}
	
	
	public static void writeResults(ScoreDoc[] docs, Searcher searcher, int topicNumber, FileWriter resFileWriter) 
			throws CorruptIndexException, IOException
	{
		for(int i = 0; i < docs.length; i++)
		{
			ScoreDoc doc = docs[i];
			Document document = searcher.getDocument(doc);
			int rank = i+1;
			
			String resLine = toTRECFormat(document, topicNumber, rank, doc.score);
			
			resFileWriter.append(resLine);
		}
	}
	
	
	public static void runToucheChallenge(String indexDirPath, String topicsFilePath, String resultsFilePath) throws IOException, ParseException
	{
		Searcher searcher = new Searcher(indexDirPath);
			
		XMLTopicReader topicReader = new XMLTopicReader(topicsFilePath);
		
		ensureDirExists( Paths.get(resultsFilePath).getParent().toString() );
		
		// create result file
		File resFile = new File(resultsFilePath);
		resFile.createNewFile();
		
		try(FileWriter resFileWriter = new FileWriter(resFile, true);)
		{
			for(Topic topic : topicReader)
			{
				System.out.println("------ " + topic.title + " ------");
				
				ScoreDoc[] docs = searcher.search(topic.title).scoreDocs;
				
				writeResults(docs, searcher, topic.number, resFileWriter);
			}
		}
		
		System.out.println("Touché Challenge successfully run.");
	}
	
	
	/**
	 * Parses the arguments received by the command line. Linux OS is assumed.
	 * @param args Arguments received by the command line
	 * @return Returns a String-Array containing path names that is composed as follows: 
	 * 	[0] = directory of JSON arguments, 
	 * 	[1] = XML file with topics, 
	 * 	[2] = result file
	 */
	public static String[] parseArgs(String[] args) throws RuntimeException
	{
		String[] out = new String[3];
		boolean inDefined = false, outDefined = false;
		
		for(int i = 0; i < args.length; i++)
		{
			switch(args[i])
			{
			case "-i":
				i++;
				out[0] = args[i];
				out[1] = args[i] + "/" + LuceneConstants.TOPICS_XML_FILE_NAME;
				
				inDefined = true;
				break;
			case "-o":
				i++;
				out[2] = args[i] + "/" + LuceneConstants.RESULT_FILE_NAME;
				
				outDefined = true;
				break;
			default:
				System.err.println("Warning: Unknown command-line argument \"" + args[i] + "\". Skipping argument.");
			}
		}
		
		if(!inDefined || !outDefined)
		{
			// TODO custom exception
			throw new RuntimeException("Error: Input and output directories must be defined. Use -i for input and -o for output.");
		}
		
		return out;
	}
	
	
	public static void ensureDirExists(String dirPath)
	{
		try
		{
			File indexDir = new File(dirPath);
			indexDir.mkdir();
			
			System.out.println("Directory \"" + dirPath + "\" created.");
		}
		catch(Exception e)
		{
			System.out.println("Directory \"" + dirPath + "\" already exists.");
		}
	}
	
	
	public static boolean indexExists(String indexDirPath)
	{
		boolean exists = true;
		
		try 
		{
			DirectoryReader.open(FSDirectory.open(Paths.get(indexDirPath)) );
	    }
		catch ( IOException e) 
		{
	    	exists = false;
	    }
		
		return exists;
	}
}
