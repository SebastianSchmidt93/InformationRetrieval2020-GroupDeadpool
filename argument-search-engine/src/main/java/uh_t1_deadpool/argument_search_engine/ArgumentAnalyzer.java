package uh_t1_deadpool.argument_search_engine;

import java.util.Arrays;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
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
	/** Default minimum and maximum size of n-grams to be used */
	public static final int DEFAULT_MIN_N_GRAM = 2;
	public static final int DEFAULT_MAX_N_GRAM = 2;

	private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;
	private int minNGramSize = DEFAULT_MIN_N_GRAM;
	private int maxNGramSize = DEFAULT_MAX_N_GRAM;

	
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
	
	public void setMinNGramSize(int size)
	{
		this.minNGramSize = size;
	}
	
	/** Returns the current minimum n-gram size
	 * 
	 *  @see #setMinNGramSize */
	public int getMinNGramSize()
	{
		return this.minNGramSize;
	}
	
	public void setMaxNGramSize(int size)
	{
		this.maxNGramSize = size;
	}
	
	/** Returns the current minimum n-gram size
	 * 
	 *  @see #setMaxNGramSize */
	public int getMaxNGramSize()
	{
		return this.maxNGramSize;
	}

	@Override
	protected TokenStreamComponents createComponents(final String fieldName) 
	{
		final StandardTokenizer src = new StandardTokenizer();
	    src.setMaxTokenLength(maxTokenLength);
	    
	    TokenStream tok = new LowerCaseFilter(src);
	    tok = new StopFilter(tok, stopwords);
	    //tok = new PorterStemFilter(tok);
	    //tok = new ShingleFilter(tok, this.minNGramSize, this.maxNGramSize);
	    
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
