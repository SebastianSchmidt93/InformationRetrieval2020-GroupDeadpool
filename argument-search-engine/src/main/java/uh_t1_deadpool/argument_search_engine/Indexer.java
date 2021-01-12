package uh_t1_deadpool.argument_search_engine;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexer {

   private IndexWriter writer;

   public Indexer(String indexDirectoryPath) throws IOException
   {
	   //this directory will contain the indexes
	   Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
  
	   //TODO choose analyzer
	   Analyzer analyzer = new ArgumentAnalyzer();
  
	   //configure index
	   IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
	   writerConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

	   //create the indexer
	   this.writer = new IndexWriter(indexDirectory, writerConfig);
   }

   public void close() throws CorruptIndexException, IOException
   {
	   this.writer.close();
   }

   private void indexFile(File file) throws IOException {
	  System.out.println("Indexing "+file.getCanonicalPath());
	  
      JsonStreamer jsonStreamer = new JsonStreamer(file);
      int count = 0;
      
      Set<String> seenArgs = new HashSet<String>();
      
      for(Document d : jsonStreamer)
      {
    	  // If argument has been seen before, skip it
    	  if( !seenArgs.contains(d.get("id")) )
    	  {
    		  writer.addDocument(d);
    		  seenArgs.add(d.get("id"));
    	  }
    	  
    	  count++;
    	  
    	  System.out.println("> " + count + " Records indexed");
      }
   }

   public void createIndex(String dataDirPath, FileFilter filter) throws IOException 
   {
      //get all files in the data directory
      File[] files = new File(dataDirPath).listFiles();

      for (File file : files) {
         if
         (
    		 !file.isDirectory()
    		 && !file.isHidden()
    		 && file.exists()
    		 && file.canRead()
    		 && filter.accept(file)
         )
         {
            indexFile(file);
         }
      }
   }
}
