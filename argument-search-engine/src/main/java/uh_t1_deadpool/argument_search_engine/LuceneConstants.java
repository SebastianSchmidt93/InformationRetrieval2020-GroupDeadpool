package uh_t1_deadpool.argument_search_engine;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.Similarity;

public class LuceneConstants 
{
	//TODO
	public static final String ID_FIELD = "id";
	public static final String CONCLUSION_FIELD = "conclusion";
	public static final String PREMISE_FIELD = "premise";
	public static final String STANCE_FIELD = "stance";
	
	public static final String[] FIELDS = {LuceneConstants.CONCLUSION_FIELD, LuceneConstants.PREMISE_FIELD};
	public static Map<String, Float> FIELD_BOOSTS;
	static
	{
		FIELD_BOOSTS = new HashMap<String, Float>();
		FIELD_BOOSTS.put(CONCLUSION_FIELD, 0.1f);
		FIELD_BOOSTS.put(PREMISE_FIELD, 0.9f);
	}
	
	public static final String TOPICS_XML_FILE_NAME = "topics.xml";
	public static final String RESULT_FILE_NAME = "run.txt";
	public static final int MAX_SEARCH = 50;
	
	public static final String GROUP_NAME = "uh-t1-deadpool";
	
	public static final Similarity SIMILARITY = new LMDirichletSimilarity(4000);
}
