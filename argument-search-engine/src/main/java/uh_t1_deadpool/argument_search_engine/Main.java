package uh_t1_deadpool.argument_search_engine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Random;

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
		boolean test = false;
		
		try
		{
			String[] paths = parseArgs(args);
			
			String dataDirPath = paths[0];
			String topicsFilePath = paths[0] + "/" + LuceneConstants.TOPICS_XML_FILE_NAME;
			String resultsDirPath = paths[1];
			String resultsFilePath = paths[1] + "/" + LuceneConstants.RESULT_FILE_NAME;
			String indexDirPath = "index";
			
			createIndex(dataDirPath, indexDirPath);
	        
			// Search and store results of topics
			if(test)
			{
				String qrelsFilePath = paths[2];
				String evalScriptPath = paths[3];
				
				if(evalScriptPath == null || qrelsFilePath == null)
					throw(new RuntimeException("Error: Testing needs a qrels-file (argument \"-qrel\") and a TREC evaluation script  (argument \"-eval\")!"));
				
				TrecEval eval = new TrecEval(evalScriptPath, qrelsFilePath);
				
				//evalFiles(resultsDirPath, eval)
				
				runTests(indexDirPath, topicsFilePath, resultsDirPath, eval);
			}
			else
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
	
	
	public static void runToucheChallenge(String indexDirPath, String topicsFilePath, String resultsFilePath, Searcher searcher) throws IOException, ParseException
	{
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
	
	
	public static void runToucheChallenge(String indexDirPath, String topicsFilePath, String resultsFilePath) throws IOException, ParseException
	{
		Searcher searcher = new Searcher(indexDirPath);
		
		runToucheChallenge(indexDirPath, topicsFilePath, resultsFilePath, searcher);
	}
	
	
	public static void runTests(String indexDirPath, String topicsFilePath, String resultsPath, TrecEval eval) throws IOException, ParseException
	{
		String csvFileName = "test_results.csv";
		
		Random generator = new Random();
		CSVWriter csvWriter = new CSVWriter();
		
		int testRuns = 10;
		int numDocsRef = 5;
		Searcher searcher = new Searcher(indexDirPath);
		searcher.queryParser.numDocsReferenced = numDocsRef;
		
		// Weighting schemes for query expansion
		HashMap<String, TriFunction<Float, Float, Float, Float>> schemes = new HashMap<>();
		schemes.put("MI", ArgumentQueryParser::MI);
		schemes.put("CP", ArgumentQueryParser::CP);
		schemes.put("SCP1", ArgumentQueryParser::SCP1);
		schemes.put("SCP2", ArgumentQueryParser::SCP2);
		
		// Run Baseline
		searcher.queryExpansion = false;
		testRun(searcher, "Baseline", resultsPath, indexDirPath, topicsFilePath, eval, csvWriter);
		searcher.queryExpansion = true;
		
		// Run Expansion variants
		for(int i = 0; i < testRuns; i++)
		{
			float m = generator.nextFloat();
			float b = generator.nextFloat();
			
			searcher.queryParser.scoreCap = m;
			searcher.queryParser.expansionBoost = b;
			
			System.out.println(String.format("\nm: %f\nb: %f", m, b));
			
			for(String schemeName : schemes.keySet())
			{
				searcher.queryParser.termSim = schemes.get(schemeName);
				testRun(searcher, schemeName, resultsPath, indexDirPath, topicsFilePath, eval, csvWriter);
			}
		}
		
		csvWriter.write(String.format("%s/%s", resultsPath, csvFileName));
		System.out.println("Test complete!");
	}
	
	
	public static void testRun(Searcher searcher, String schemeName, String resultsPath, String indexDirPath, String topicsFilePath, 
			TrecEval eval, CSVWriter csvWriter) throws IOException, ParseException
	{
		System.out.println(String.format("----- %s -----", schemeName));
		
		// Add summary of scores to CSV-file
		HashMap<String, String> entry = new HashMap<>();
		entry.put("scheme", schemeName);
		
		String resultFile;
		
		if(searcher.queryExpansion)
		{
			int n = searcher.queryParser.numDocsReferenced;
			float m = searcher.queryParser.scoreCap;
			float b = searcher.queryParser.expansionBoost;
			
			entry.put("n", String.valueOf(n));
			entry.put("m", String.valueOf(m));
			entry.put("b", String.valueOf(b));
			
			resultFile = getResultFileName(resultsPath, schemeName, n, m, b);
		}
		else
		{
			resultFile = String.format("%s/run_scheme:%s.txt", resultsPath, schemeName);
		}
		
		// Produce TREC style output file
		runToucheChallenge(indexDirPath, topicsFilePath, resultFile, searcher);
		
		HashMap<String, Float> scores = eval.eval(resultFile);
		for(String measure : scores.keySet())
			entry.put(measure, String.valueOf(scores.get(measure)));
		
		csvWriter.add(entry);
	}
	
	
	public static String getResultFileName(String path, String scheme, int n, float m, float b)
	{
		return String.format("%s/run_scheme:%s_n:%d_m:%f_b:%f.txt", path, scheme, n, m, b);
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
		String[] out = new String[4];
		boolean inDefined = false, outDefined = false;
		
		for(int i = 0; i < args.length; i++)
		{
			switch(args[i])
			{
			case "-i":
				i++;
				out[0] = args[i];
				
				inDefined = true;
				break;
			case "-o":
				i++;
				out[1] = args[i];
				
				outDefined = true;
				break;
			case "-qrel":
				i++;
				out[2] = args[i];
				
				break;
			case "-eval":
				i++;
				out[3] = args[i];
				
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
	
	
	public static void evalFiles(String resultsDirPath, TrecEval eval) throws IOException
	{
		//TODO begin
		CSVWriter csvWriter = new CSVWriter();
		
		File dir = new File(resultsDirPath);
		for(String fileName : dir.list())
		{
			int extInd = fileName.lastIndexOf(".");
			
			if(extInd < 0)
				continue;
			
			String ext = fileName.substring(extInd, fileName.length());
			String fileNamePlain = fileName.substring(0, extInd);
			String[] components = fileNamePlain.split("_");
			
			if(ext.equals(".txt") && components[0].equals("run"))
			{
				HashMap<String, String> params = new HashMap<>();
				
				for(int i = 1; i < components.length; i++)
				{
					String[] parts = components[i].split(":");
					
					params.put(parts[0], parts[1].replace(",", "."));
				}
				
				HashMap<String, Float> scores = eval.eval(String.format("%s/%s", resultsDirPath, fileName));
				
				for(String key : scores.keySet())
					params.put(key, scores.get(key).toString());
				
				csvWriter.add(params);
			}
		}
		csvWriter.write(String.format("%s/%s", resultsDirPath, "results.csv"));
	}
}
