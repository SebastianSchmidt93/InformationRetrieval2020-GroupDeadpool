package core;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class XMLTopicReaderIterator implements Iterator<Topic>
{
	
	private Iterator<Element> elementIterator;
	
	public XMLTopicReaderIterator(File xmlFile)
	{
		SAXBuilder builder = new SAXBuilder();
        Document doc;
		try 
		{
			doc = builder.build(xmlFile);
			
			Element element = doc.getRootElement();

	        // List that contains all topics
	        List<Element> children = element.getChildren();
	        this.elementIterator = children.iterator();
		} 
		catch (JDOMException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean hasNext() {
		return this.elementIterator.hasNext();
	}

	@Override
	public Topic next() {
		Element elem = this.elementIterator.next();
		
		Topic topic = new Topic();
		
		topic.number = Integer.parseInt( elem.getChild("number").getValue() );
		topic.title = elem.getChild("title").getValue();
		topic.description = elem.getChild("description").getValue();
		topic.narrative = elem.getChild("narrative").getValue();
		
		return topic;
	}

}
