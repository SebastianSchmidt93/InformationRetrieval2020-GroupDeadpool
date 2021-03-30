package uh_t1_deadpool.argument_search_engine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class TrecEval 
{
	public static final String[] DEFAULT_MEASURES = {"ndcg_cut.5", "set_F.1.10"};
	
	public String evalScriptPath;
	public String qrelsFilePath;
	public ArrayList<String> measures = new ArrayList<>();
	
	public TrecEval(String evalScriptPath, String qrelsFilePath)
	{
		this.evalScriptPath = evalScriptPath;
		this.qrelsFilePath = qrelsFilePath;
		
		for(String measure : DEFAULT_MEASURES)
			this.measures.add(measure);
	}
	
	public HashMap<String, Float> eval(String trecFilePath)
	{
		HashMap<String, Float> res = new HashMap<String, Float>();
		
		Process p;
		try 
		{
			ArrayList<String> cmd = new ArrayList<>();
			cmd.add(this.evalScriptPath);
			for(String measure : this.measures)
			{
				cmd.add("-m");
				cmd.add(measure);
			}
			cmd.add(this.qrelsFilePath);
			cmd.add(trecFilePath);
			
			String[] cmdArray = cmd.toArray(new String[cmd.size()]);
			
			ProcessBuilder pb = new ProcessBuilder(cmdArray);
			p = pb.start();
			p.waitFor();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			String line;
			while((line = reader.readLine()) != null) 
			{
				String[] entries = line.split(" ");
				String measure = entries[0];
				int begin = line.lastIndexOf("0.");
				int end = line.length();
				String scoreString = line.substring(begin, end);
				
				float score = Float.parseFloat(scoreString);
				
				res.put(measure, score);
			}
		}
		catch(Exception e) //TODO more accurate
		{
			e.printStackTrace();
		}
		
		return res;
	}
	
	public static String quote(String s)
	{
		return String.format("\"%s\"", s);
	}
}
