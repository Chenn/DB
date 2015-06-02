package sqlparse;

import io.FileAccessor;

import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.List;

import dao.Datum;
import dao.Schema;
import dao.Tuple;


public class SerializeTest {

	public static void main(String[] args) throws Exception {
		TestEnvironment envir = new TestEnvironment();
		Schema sche = envir.generateSchema("orders");
		String addr = "test/cp2_grade/orders.dat";
		
		List<Tuple> tups = FileAccessor.getInstance().readSpecificBlock(addr, sche, 0, 10);
		//List<Tuple> tups = FileAccessor.getInstance().readBlockTuple(addr, sche);
		System.out.println(tups.size());
		File serializeFile = new File("test/serializedData.swap");
		writeNStringObject(tups,10);
//		TimeCalc.begin(1);
//		ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(serializeFile));
//		for(Tuple tup : tups){
//			//System.out.println("Write: " + tup.toString());
//			output.writeObject(tup.getDataArr());
//		}
//		output.close();
//		TimeCalc.end(1,"Write tuples "+tups.size());
//		System.out.println();
//		//write end
//		
//		TimeCalc.begin(1);
//		List<Tuple> readTups = traditionRead(sche, serializeFile);
//		TimeCalc.end(1, "Read " + readTups.size() + " tuples");
	}
	
	public static void writeNStringObject(List<Tuple> tups, int n){
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(new File("test/WriteStringObject.test")))){
			for(int i=0; i<n; i++)
				writer.write(tups.get(i).toString()+"\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void writeNSerialObject(List<Tuple> tups, int n){
		try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(new File("test/WriteOneObjTest.test")))){
			for(int i=0; i<n; i++)
				output.writeObject(tups.get(i).getDataArr());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static List<Tuple> traditionRead(Schema schema, File f){
		List<Tuple> tups = new ArrayList<Tuple>();
		//read
		try(ObjectInputStream input = new ObjectInputStream(new FileInputStream(f))){
           Object obj = null;
            while ((obj = input.readObject()) != null) {
                if (obj instanceof Datum[]) {
                	Datum[] dataArr = (Datum[])obj;
                	tups.add(new Tuple(dataArr, schema));
                }else
                	throw new UnexpectedException("Object casting error, this is not a Datum[] object, Class type: "+obj.getClass().toString());
            }
		} catch (EOFException ex) {  //This exception will be caught when EOF is reached
            System.out.println("End of file reached.");
        } catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return tups;
	}

}
