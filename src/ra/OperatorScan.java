/**
 * 
 */
package ra;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import dao.Schema;
import dao.Tuple;

public class OperatorScan implements Operator {

	private BufferedReader inputReader;
	private File file;
	private Schema schema;

	public OperatorScan(File f, Schema schemaIn){
		file = f;
		schema = schemaIn;
		reset();
	}
	
	public File getFile(){
		return file;
	}
	
	@Override
	public long getLength(){
		return file.length();
	}
	
	@Override
	public Tuple readOneTuple() {
		checkInput();
		Tuple tuple = null;
		try {
			String line = inputReader.readLine();
			if(line==null)
				tuple = null;			
			else
				tuple = new Tuple(line, schema);
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


	@Override
	public Schema getSchema() {
		return schema;
	}
	
	
	@Override
	public void close(){
		try {
			inputReader.close();
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
}
