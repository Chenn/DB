package dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.rmi.UnexpectedException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ra.Operator;
import net.sf.jsqlparser.schema.Table;

/**
 * Hash index implemented by random access file
 */
public class HashIndex {
	private Table tab;
	private String attr;
	private Schema schema;
	private Map<String, List<Long>> hashIndexDir;
	private File hashIndexFile;
	private RandomAccessFile raf;
	
	public HashIndex(Table tabIn, String attrIn, Schema schemaIn, File swap){
		tab = tabIn;
		attr = attrIn;
		schema = schemaIn;
		hashIndexDir = new HashMap<String, List<Long>>();
		hashIndexFile = new File(swap.getPath()+"/"+tab.getWholeTableName());
		try {
			raf = new RandomAccessFile(hashIndexFile,"rw");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public HashIndex(Table table, String attribute, Operator dataSource, File swap){
		tab = table;
		attr = attribute;
		hashIndexDir = new HashMap<String, List<Long>>();
		try {
			raf = new RandomAccessFile(hashIndexFile,"rw");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Write data into file, construct a hash index file
		hashIndexFile = new File(swap.getPath()+"/"+tab.getWholeTableName());
		insertBlock(dataSource, false);
	}
	
	
	public void close(){
		try {
			raf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	


	
	public void insertBlock(Operator dataSource, boolean append){
		try{	
			if(append)
				raf.seek(raf.length());
		
			Tuple tup;
			while((tup=dataSource.readOneTuple())!=null){
				if(schema==null)
					schema = tup.getSchema();
				
				//datum of join column
				Datum keyData = tup.getDataByName(attr);
				if(keyData==null){
					try {
						throw new UnexpectedException("Can't get data from tuple : " + tup.toString());
					} catch (UnexpectedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				//construct index
				String key = keyData.toString();
				long pointer = raf.getFilePointer();
				List<Long> list = hashIndexDir.get(key);
				if(list==null){
					list = new LinkedList<Long>();
					list.add(pointer);
					hashIndexDir.put(key, list);
				}else
					list.add(pointer);
				
				//write into file
				raf.write((tup.toString()+"\n").getBytes());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public List<Long> get(String key){
		return hashIndexDir.get(key);
	}


	public boolean containsKey(String key) {
		return hashIndexDir.containsKey(key);
	}
	
	public File getIndexFile(){
		return hashIndexFile;
	}
	
	public Schema getSchema(){
		return schema;
	}
}
