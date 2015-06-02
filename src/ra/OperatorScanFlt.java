/**
 * 
 */
package ra;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sqlparse.TestEnvironment;
import dao.Datum;
import dao.DatumFactory;
import dao.DatumType;
import dao.Schema;
import dao.Tuple;

public class OperatorScanFlt implements Operator {

	private BufferedReader inputReader;
	private File file;
	private Schema schema;
	private Map<String, List<Tuple>> filter;
	private int index;
	
	public OperatorScanFlt(File f, Schema schemaIn, Map<String, List<Tuple>> flt, int indexIn){
		file = f;
		schema = schemaIn;
		filter = flt;
		index = indexIn;
		
		reset();
	}

	@Override
	public long getLength(){
		return file.length();
	}

	@Override
	public Tuple readOneTuple() {
		checkInput();  //could be annotated?
		Tuple tuple = null;
		String line = new String();
		try {
			while((line = inputReader.readLine()) != null){
				if(filter == null){
					tuple = new Tuple(line, schema);
					return tuple;
				}

				String cellVal = stringAtIndex(line, index);
				if(filter.containsKey(cellVal)){
					tuple = new Tuple(line, schema);
					return tuple;
				}

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return tuple;
	}


	@Override
	public void reset() {
		try {
			if(inputReader!=null)
				inputReader.close();
			inputReader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	/**
	 * Check inputReader whether is null
	 * @return
	 */
	private boolean checkInput(){
		if(inputReader==null){
			try {
				throw new Exception("inputReader is null!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}else
			return true;
	}

	public File getFile(){
		return file;
	}

	@Override
	public Schema getSchema() {
		return schema;
	}
	

	@Override
	public void close() {
		try {
			inputReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/***/
	public Datum valueAtIndex(String row, int index, String type){
		String ret = new String();
//		Tuple tuple = new Tuple(input,schema);
		int splitter = 0;
		int start = 0; int end = 0;
//		System.out.println(input);
//		System.out.println(input.length()); 
		for(int i = 0 ; i < row.length(); i++){
			if(row.charAt(i) == '|') {splitter++;}
			if(splitter == index) {start = i; break;}
		}
		for(int i = start+1 ; i < row.length(); i++){
			if(row.charAt(i) == '|') {splitter++;}
			if(splitter == index+1) {end = i; break;}
		}
		ret = row.substring(start+1, end);
		
		Datum datum = DatumFactory.create(ret, DatumType.valueOf(type));
		return datum;
	}
	
	

	/**
	 * Get the String value of column the of specific index of a row
	 * @param row
	 * @param index
	 * @return
	 */
	public String stringAtIndex(String row, int index){
		String ret = new String();
//		Tuple tuple = new Tuple(input,schema);
		int splitter = 0;
		int start = 0; int end = 0;
//		System.out.println(input);
//		System.out.println(input.length()); 
		for(int i = 0 ; i < row.length(); i++){
			if(row.charAt(i) == '|') {splitter++;}
			if(splitter == index) {start = i; break;}
		}
		for(int i = start+1 ; i < row.length(); i++){
			if(row.charAt(i) == '|') {splitter++;}
			if(splitter == index+1) {end = i; break;}
		}
		ret = row.substring(start+1, end);
		
		return ret;
	}
	
	
	public static void main(String arg[]){
//		File file = new File("/Users/Shawna/Desktop/lineitem.tbl");
		try {
			File dataDir = new File("test/cp2_grade");
			
			TestEnvironment envir = new TestEnvironment();
			Schema schema = envir.generateSchema("lineitem");
			//Schema schema = Schema.schemaFactory(colsMapper, ct, tableName);
			Map<String, List<Tuple>> flt = new HashMap<String, List<Tuple>>();
			flt.put(new String("R"),null);
			OperatorScanFlt scanFilter = new OperatorScanFlt(new File(dataDir+"/lineitem.dat"),schema,flt,8);
			
			Tuple tuple = null;
			while((tuple = scanFilter.readOneTuple()) !=null){
				System.out.println(tuple);
			}
			
			Datum dat;
			String input = new String("aaa|bbb|ccc|true|FOB");
			String type = new String("Bool");
			dat = scanFilter.valueAtIndex(input, 3, type);
			System.out.println(dat);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
