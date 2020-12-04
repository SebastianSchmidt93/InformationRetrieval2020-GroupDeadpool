package prototype;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;

public class Main 
{
	public static void main(String[] args)
	{
		//TODO
		String indexDirPath = "/home/rikso/eclipse-workspace/LuceneTest/src/prototype/index";
		String dataDirPath = "/home/rikso/Dokumente/Studium/WS 20,21/Information Retrieval/Übung/Touche 1: Argument Search/args.me Datensatz";
		String topicsFilePath = "/home/rikso/Dokumente/Studium/WS 20,21/Information Retrieval/Übung/Touche 1: Argument Search/Query Datensatz/query_tasks.xml";
		String resultsFilePath = "/home/rikso/eclipse-workspace/LuceneTest/src/prototype/results.txt";
		
		/*
		// Create an index
		try
		{
			Indexer indexer = new Indexer(indexDirPath);
			
			JsonFileFilter fileFilter = new JsonFileFilter();
			
			indexer.createIndex(dataDirPath, fileFilter);
			
			indexer.close();
		}
		catch (IOException e) 
		{
            e.printStackTrace();
        }
        */
		
		// Search and store results
		// TODO make more readable, more object-oriented
		try
		{
			Searcher searcher = new Searcher(indexDirPath);
			
			XMLTopicReader topicReader = new XMLTopicReader(topicsFilePath);
			
			// create result file
			File resFile = new File(resultsFilePath);
			resFile.createNewFile();
			
			try(FileWriter resFileWriter = new FileWriter(resFile, true);)
			{
				for(Topic topic : topicReader)
				{
					System.out.println("------ " + topic.title + " ------");
					
					ScoreDoc[] docs = searcher.search(topic.title).scoreDocs;
					
					for(int i = 0; i < docs.length; i++)
					{
						ScoreDoc doc = docs[i];
						Document document = searcher.getDocument(doc);
						
						String resLine = topic.number + " " + "q0" + " " + document.get("id") + " " + (i+1) + " " + doc.score + " " + LuceneConstants.GROUP_NAME + "\n";
						
						resFileWriter.append(resLine);
					}
				}
			}
		}
		catch(Exception e) //TODO
		{
			e.printStackTrace();
		}
		
	}
}
