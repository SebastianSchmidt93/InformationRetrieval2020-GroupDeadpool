package uh_t1_deadpool.argument_search_engine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CSVWriter
{
	private ArrayList<String> columns;
	private ArrayList<HashMap<String, String>> entries;
	
	public CSVWriter()
	{
		this.columns = new ArrayList<>();
		this.entries = new ArrayList<>();
	}
	
	public void add(HashMap<String, String> entry)
	{
		for(String column : entry.keySet())
			if(!this.columns.contains(column))
				this.columns.add(column);
		
		this.entries.add(entry);
	}
	
	@Override
	public String toString()
	{
		StringBuilder s = new StringBuilder();
		
		for(String column : this.columns)
			s.append(column).append(";");
		s.append("\n");
		
		for(HashMap<String, String> entry : this.entries)
		{
			for(String column : this.columns)
				s.append(entry.get(column)).append(";");
			s.append("\n");
		}
		
		return s.toString();
	}
	
	public void write(String filePath) throws IOException
	{
		File resFile = new File(filePath);
		resFile.createNewFile();
		
		try(FileWriter fileWriter = new FileWriter(resFile, true);)
		{
			fileWriter.write(this.toString());
		}
	}
	
}
