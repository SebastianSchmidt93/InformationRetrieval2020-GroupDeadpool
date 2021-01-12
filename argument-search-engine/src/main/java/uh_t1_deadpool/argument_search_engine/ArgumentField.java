package uh_t1_deadpool.argument_search_engine;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;

public class ArgumentField extends Field
{
	/* Indexed, tokenized, not stored. */
	public static final FieldType TYPE_NOT_STORED = new FieldType();

	/* Indexed, tokenized, stored. */
	public static final FieldType TYPE_STORED = new FieldType();

	static 
	{
	    TYPE_NOT_STORED.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
	    TYPE_NOT_STORED.setTokenized(true);
	    TYPE_NOT_STORED.setStoreTermVectors(true);
	    TYPE_NOT_STORED.setStoreTermVectorPositions(true);
	    TYPE_NOT_STORED.freeze();

	    TYPE_STORED.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
	    TYPE_STORED.setTokenized(true);
	    TYPE_STORED.setStored(true);
	    TYPE_STORED.setStoreTermVectors(true);
	    TYPE_STORED.setStoreTermVectorPositions(true);
	    TYPE_STORED.freeze();
	}
	
	public ArgumentField(String name, String value, Store store)
	{
		super(name, value, store == Store.YES ? TYPE_STORED : TYPE_NOT_STORED);
	}
}
