package uh_t1_deadpool.argument_search_engine;

import java.util.Arrays;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.shingle.FixedShingleFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public final class ArgumentAnalyzer extends StopwordAnalyzerBase
{
	/** The stopwords used for filtering */
	public static final String[] STOP_WORDS_SET = {"but", "be", "with", "such", "then", "for", "no",
			"will", "not", "are", "and", "their", "if", "this", "on", "into", "a", "or", "there", 
			"in", "that", "they", "was", "is", "it", "an", "the", "as", "at", "these", "by", "to", 
			"of", "you", "your", "i"}; //, "should", "is", "would", "could"};
	/** Default maximum allowed token length */
	public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

	private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;

	
	/** Builds an analyzer */
	public ArgumentAnalyzer() 
	{
		super( new CharArraySet(Arrays.asList(ArgumentAnalyzer.STOP_WORDS_SET), true) );
	}

	/**
	 * Set the max allowed token length.  Tokens larger than this will be chopped
	 * up at this token length and emitted as multiple tokens.  If you need to
	 * skip such large tokens, you could increase this max length, and then
	 * use {@code LengthFilter} to remove long tokens.  The default is
	 * {@link ArgumentAnalyzer#DEFAULT_MAX_TOKEN_LENGTH}.
	 */
	public void setMaxTokenLength(int length) 
	{
		maxTokenLength = length;
	}
	 
	/** Returns the current maximum token length
	 * 
	 *  @see #setMaxTokenLength */
	public int getMaxTokenLength() 
	{
		return maxTokenLength;
	}
	
	@Override
	protected TokenStreamComponents createComponents(final String fieldName) 
	{
		final StandardTokenizer src = new StandardTokenizer();
	    src.setMaxTokenLength(maxTokenLength);
	    
	    TokenStream tok = new LowerCaseFilter(src);
	    tok = new StopFilter(tok, stopwords);
	    // The following filters have lead to a decrease in performance
	    //tok = new KStemFilter(tok);
	    
	    return new TokenStreamComponents(
	    		r -> {
				    src.setMaxTokenLength(this.maxTokenLength);
				    src.setReader(r);}, 
	    		tok);
	}

	@Override
	protected TokenStream normalize(String fieldName, TokenStream in) 
	{
		return new LowerCaseFilter(in);
	}
}
