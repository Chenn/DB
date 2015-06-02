package io;

import java.io.File;
import java.io.FileFilter;

public class FileTypeFilter implements FileFilter {
	private String type;
	
	public FileTypeFilter(String typeIn){
		type = typeIn;
	}
	
	@Override
	public boolean accept(File arg0) {
		String name = arg0.getName();
		if(name.length()<3)
			return false;
		else{
			String suffix = name.substring(name.length()-3);
			if(suffix.equalsIgnoreCase(type))
				return true;
			else
				return false;
		}
	}

}
