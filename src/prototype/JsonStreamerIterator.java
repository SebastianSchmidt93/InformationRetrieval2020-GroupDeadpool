package prototype;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class JsonStreamerIterator implements Iterator<Document>
{
	// Field names of the Json File
    public static final String ARGUMENT_ID = "id";
    public static final String ARGUMENT_CONCLUSION = "conclusion";
    public static final String ARGUMENT_PREMISE = "text";
    public static final String ARGUMENT_STANCE = "stance";
	
	private File jsonFile;
	private JsonParser jsonParser;
	private int recordsParsed = 0;
	
	public JsonStreamerIterator(File jsonFile)
	{
		this.jsonFile = jsonFile;
		JsonFactory factory = new JsonFactory();
		
		try
		{
			this.jsonParser = factory.createParser(this.jsonFile);
			// Skip the "arguments" fieldName and startArray
			this.jsonParser.nextToken();
			this.jsonParser.nextToken();
		}
		catch (IOException e) 
		{
            e.printStackTrace();
        }
	}

	@Override
	public boolean hasNext() 
	{
		Boolean res = false;
		
		try
		{
			res = !JsonToken.END_ARRAY.equals(this.jsonParser.nextToken());
		}
		catch (IOException e) 
		{
            e.printStackTrace();
        }
		
		return res;
	}

	@Override
	public Document next() 
	{
        Document document = new Document();
        StoredField argIdField = null;
        TextField argConclusionField = null;
        TextField argPremiseField = null;
        StoredField argStanceField = null;
        
        Boolean reading  = true;
        
        try
        {
	        JsonToken jsonToken = this.jsonParser.nextToken();
	        
	        while (reading)
	        {
	        	
	        	if(JsonToken.FIELD_NAME.equals(jsonToken))
	        	{
		            String fieldname = jsonParser.getCurrentName(); //get current name of token
		            
		            switch(fieldname)
		            {
		            case JsonStreamerIterator.ARGUMENT_ID:
		            	jsonToken = jsonParser.nextToken();
		            	
		            	argIdField = new StoredField(LuceneConstants.ID_FIELD, jsonParser.getText());
		            	break;
		            	
		            case JsonStreamerIterator.ARGUMENT_CONCLUSION:
		            	jsonToken = jsonParser.nextToken();
		            	
		            	argConclusionField = new TextField(LuceneConstants.CONCLUSION_FIELD, jsonParser.getText(), Field.Store.YES);
		            	break;
		            	
		            case JsonStreamerIterator.ARGUMENT_PREMISE:
		            	jsonToken = jsonParser.nextToken();
		            	
		            	argPremiseField = new TextField(LuceneConstants.PREMISE_FIELD, jsonParser.getText(), Field.Store.YES);
		            	break;
		            	
		            case JsonStreamerIterator.ARGUMENT_STANCE:
		            	jsonToken = jsonParser.nextToken();
		            	
		            	argStanceField = new StoredField(LuceneConstants.STANCE_FIELD, jsonParser.getText());
		            	break;
		            	
		            default:
		            	jsonToken = jsonParser.nextToken();
		            }
	        	}
	        	
	        	if (JsonToken.END_OBJECT.equals(jsonToken)) reading = false;
	        	
	        	jsonToken = this.jsonParser.nextToken();
	        	
	        	if (!JsonToken.END_OBJECT.equals(jsonToken)) reading = true;
	        }
        }
        catch(IOException e)
        {
        	e.printStackTrace();
        }
		
        document.add(argIdField);
        document.add(argConclusionField);
        document.add(argPremiseField);
        document.add(argStanceField);
        
        this.recordsParsed++;
		
		return document;
	}
	
	public int getNumDocumentsParsed()
	{
		return this.recordsParsed;
	}

}
