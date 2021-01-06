package uh_t1_deadpool.argument_search_engine;

import java.io.File;
import java.io.FileFilter;

public class JsonFileFilter implements FileFilter {

   @Override
   public boolean accept(File pathname) {
      return pathname.getName().toLowerCase().endsWith(".json");
   }
}