package ra;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dao.CompareAttribute;
import dao.Schema;
import dao.Tuple;

/**
 * External Merge Sort
 */
public class ExternalMergeSort {

	private List<File> files;
	private Schema schema;
	private CompareAttribute[] cmprAttrs;
	
	public ExternalMergeSort(List<File> filesIn, Schema schemaIn, CompareAttribute[] cmprAttrIn){
		files = filesIn;
		schema = schemaIn;
		cmprAttrs = cmprAttrIn;
	}
	
	public File sort(){
		if(files.size()==1)
			return files.get(0);
		
		int size = files.size();
		
		do{
			List<File> cachedFiles = new ArrayList<File>();
			for(int i=0; i<files.size(); i++){
				if(i==size-1){
					cachedFiles.add(files.get(i));
					break;
				}
				File mergedF = mergeFile(files.get(i), files.get(i+1));
				cachedFiles.add(mergedF);
				i++; //i+2
			}

			files = cachedFiles;
			
		}while(files.size()!=1);
		
		
		return files.get(0);
	}
	
	private File mergeFile(File f1, File f2){
		File mergeF = new File(f1.getName()+"_merge_"+f2.getName());
		
		try(BufferedReader reader1 = new BufferedReader(new FileReader(f1))){
			try(BufferedReader reader2 = new BufferedReader(new FileReader(f1))){
				try(BufferedWriter writer = new BufferedWriter(new FileWriter(mergeF))){
					String line1;
					String line2;
					while((line1=reader1.readLine())!=null && (line2=reader2.readLine())!=null){
						Tuple tup1 = new Tuple(line1, schema);
						Tuple tup2 = new Tuple(line2, schema);
						
						int compRes = tup1.compareTo(tup2, cmprAttrs);
						if(compRes>0){
							writer.write(tup1.toString());
							writer.newLine();
							writer.write(tup2.toString());
							writer.newLine();
						}else{
							writer.write(tup2.toString());
							writer.newLine();
							writer.write(tup1.toString());
							writer.newLine();
						}
					}//end while
								
					BufferedReader reader;
					if(line1!=null){
						reader = reader1;
						writer.write(line1);
						writer.newLine();
					}else
						reader = reader2;
					
					String line;
					while((line=reader.readLine())!=null){
						writer.write(line);
						writer.newLine();
					}
					writer.flush();
				}//writer close
			}//reader2 close
		//reader1 close
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mergeF;
	}

	
	public static void main(String[] args){
		List<File> files = new ArrayList<File>();
		files.add(new File("1"));
		files.add(new File("2"));
		files.add(new File("3"));
		files.add(new File("4"));
		files.add(new File("5"));
		files.add(new File("6"));
		files.add(new File("7"));
		files.add(new File("8"));
		
//		ExternalMergeSort msort = new ExternalMergeSort(files);
//		File f = msort.sort();
//		System.out.println(f.getPath());
	}
}
