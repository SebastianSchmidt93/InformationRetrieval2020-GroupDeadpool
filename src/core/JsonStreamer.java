package core;

import java.io.File;
import java.util.Iterator;
import org.apache.lucene.document.Document;

public class JsonStreamer implements Iterable<Document>
{
	private File jsonFile;
	
	public JsonStreamer(File jsonFile)
	{
		this.jsonFile = jsonFile;
	}
	
	@Override
	public Iterator<Document> iterator() 
	{
		return new JsonStreamerIterator(this.jsonFile);
	}

}
