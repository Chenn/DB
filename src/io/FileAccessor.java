package io;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import sqlparse.TestEnvironment;

import common.TimeCalc;

import dao.Datum;
import dao.DatumBool;
import dao.DatumDouble;
import dao.DatumLong;
import dao.Schema;
import dao.Tuple;

public class FileAccessor {

	private static FileAccessor instance = new FileAccessor();
	private final int BUFFERSIZE = 200000000;	//200MB
	
	private FileAccessor(){} 
	
	public static FileAccessor getInstance(){
		return instance;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String add0 = "data/NBA/";
		String add1 = "data/NBA/nba11.sql";
		String add2 = "data/NBA/nba16.expected.dat";
		String add3 = "test/cp1/partsupp.dat";
		System.out.println("start");
		
		TimeCalc.begin();
		//String content = FileAccessor.getInstance().readLine(add2);
		//StringBuilder content = FileAccessor.getInstance().readBlock(add2);
		//List<File> test =FileAccessor.getInstance().getDataFiles(add0, "dat");
		//List<String> sqls = FileAccessor.getInstance().readAllSqls(add1);
		
		//FileAccessor.StringBuilder sb = FileAccessor.getInstance().readPartOfFile(new File(add3), 2);
		TestEnvironment envir = new TestEnvironment();
		Schema schema = envir.generateSchema("partsupp");
		List<Tuple> tups = FileAccessor.getInstance().readBlockTuple(add3, schema);
		System.out.println(tups.get(1).getBytes());
		TimeCalc.end("Finish reading");
		System.out.println("end");
	}
	

	
	public PlainSelect parsePSelect(File f) throws JSQLParserException, FileNotFoundException{
		CCJSqlParserManager parser = new CCJSqlParserManager();
		FileReader reader = new FileReader(f);
		Statement stmt = parser.parse(reader);
		
		if(stmt instanceof Select){
			Select sel = (Select)stmt;
			SelectBody sb = sel.getSelectBody();
			if(sb instanceof PlainSelect)
				return (PlainSelect)sb;
		}
		
		return null;
	}
	
	/**
	 * Get a file list with a specified type of file
	 * @param addr : file path
	 * @param type : file type
	 * @return
	 */
	public List<File> getDataFiles(String addr, String type){
		File file = new File(addr);
		File[] files = file.listFiles();
		
		List<File> filelist = new ArrayList<File>();
		for(File f : files){
			if(f.getName().indexOf(type)>0){
				filelist.add(f);
			}
		}
		return filelist;
	}
	
	
	/**
	 * Read all content from a file
	 * @param addr : file path
	 * @return : String content
	 */
	public List<Tuple> readBlockString(String addr, Schema schema){
		List<Tuple> tuples = new LinkedList<Tuple>();
		try(BufferedReader reader = getBR(addr)){
			String line = null;
			while((line = reader.readLine())!=null){
				tuples.add(new Tuple(line, schema));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tuples;
	}
	
	public StringBuilder readBlock(File f){
		StringBuilder sb = new StringBuilder();
		char[] content = new char[BUFFERSIZE];
		try(BufferedReader reader = getBR(f)){
			
			reader.read(content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sb.append(content);
		return sb;
	}
	
	
	/**
	 * Read all content from a file
	 * @param addr : file path
	 * @return : String content
	 */
	public List<Tuple> readSpecificBlock(String addr, Schema schema, int beginLine, int endLine){
		List<Tuple> tuples = new LinkedList<Tuple>();
		try(BufferedReader reader = getBR(addr)){
			int current = 0;	//current line number
			
			String line = null;
			while((line = reader.readLine())!=null){
				current++;
				if(current<beginLine)
					continue;
				if(current>endLine)
					break;

				tuples.add(new Tuple(line, schema));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tuples;
	}
	
	
	public List<Tuple> readBlockTuple(String addr, Schema schema){
		List<Tuple> tuples = new LinkedList<Tuple>();
		try(BufferedReader reader = getBR(addr)){			
			String line = null;
			while((line = reader.readLine())!=null){
				tuples.add(new Tuple(line, schema));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tuples;
	}
	
	
	public StringBuilder readPartOfFile(File f, int parts){
		StringBuilder sb = new StringBuilder();
		try(BufferedReader reader = getBR(f)){
			String line = "";
			int i=0;
			while((line = reader.readLine())!=null){
				if(i==parts){
					sb.append(line+"\n");
					i=0;
				}
					
				i++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return sb;
	}
	
	
	/**
	 * Read all the Lines from a file
	 * @param addr : file path
	 * @return
	 */
	public List<String> readAllLines(String addr){
		List<String> lines = new ArrayList<String>();
		try(BufferedReader br = getBR(addr)){
			String line = "";
			while((line = br.readLine())!=null){
				lines.add(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return lines;
	}
	
	public void writeFile(StringBuilder sb, String fileName){
		try(FileWriter writer = new FileWriter(new File("data/tpch/" + fileName))){
			writer.write(sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
	/**
	 * Write tuple into a file
	 * @param tups
	 * @param fileDir
	 * @throws IOException 
	 */
	public void writeTuples(List<Tuple> tups, File fileDir) throws IOException{
		RandomAccessFile writer = getRaf(fileDir,"rw");
		//RandomAccessFile writer = new RandomAccessFile(fileDir, "rw");
		int count = 0;
		writer.seek(writer.length());
		for(Tuple tup : tups){
			//finish writing one tuple
			System.out.println("pointer:"+writer.getFilePointer());
			writer.writeUTF(tup.toString()+"\n");
			System.out.println(tup.toString());
			count++;
			if(count==10)
				break;
			//get offset
			long offset = writer.getFilePointer();
			System.out.println(offset);
		}
		
		try{
			writer.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	private void writeDatum(BufferedRandomReader writer, Datum data) throws IOException{
		switch(data.getType()){
		case Long:
			DatumLong dl = (DatumLong)data;
			writer.writeLong(dl.getValue());
			return;
		case Double:
			DatumDouble doub = (DatumDouble)data;
			writer.writeDouble(doub.getValue());
			return;
		case Bool:
			DatumBool bool = (DatumBool)data;
			writer.writeBoolean(bool.getValue());
			return;
		default:
			//String and Date
			writer.writeUTF(data.toString());
			return;
		}
	}
	
	
	private DataOutputStream getDOS(File file){
		try {
			return new DataOutputStream(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	
	private RandomAccessFile getRaf(File file, String mode){
		try {
			return new RandomAccessFile(file, mode);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	
	/**
	 * Get BufferedReader
	 * @param addr : file path
	 * @return
	 * @throws FileNotFoundException
	 */
	private BufferedReader getBR(String addr){
		try {
			return new BufferedReader(new InputStreamReader(new FileInputStream(new File(addr))));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private BufferedReader getBR(File f){
		try {
			return new BufferedReader(new InputStreamReader(new FileInputStream(f)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
