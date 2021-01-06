package uh_t1_deadpool.argument_search_engine;

import java.io.File;
import java.util.Iterator;

public class XMLTopicReader implements Iterable<Topic>
{
	
	private File xmlFile;
	
	public XMLTopicReader(String filePath)
	{
		this.xmlFile = new File(filePath);
	}

	@Override
	public Iterator<Topic> iterator() 
	{
		return new XMLTopicReaderIterator(this.xmlFile);
	}
	
}
