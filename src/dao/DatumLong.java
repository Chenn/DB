package dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Long data cell
 */
public class DatumLong extends Datum{
 
	private static final long serialVersionUID = -4625153744377548371L;
	private long value;
	
	public DatumLong(String dataIn){
		super(DatumType.Long);
		value = Long.parseLong(dataIn);
	}
	
	public DatumLong(long dataIn){
		super(DatumType.Long);
		value = dataIn;
	}

	public long getValue(){
		return value;
	}

	@Override
	public int getHashValue(){
		return (int)value;
	}

	@Override
	public double getNumericValue() {
		return (double)value;
	}

	@Override
	public void setNumericValue(double valueIn) {
		value = (long)valueIn;
	}
	
	
	@Override
	public int compareTo(Datum o) {
		if(o instanceof DatumLong){
			DatumLong obj = (DatumLong)o;
			if(this.value>obj.value)
				return 1;
			else if(this.value<obj.value){
				return -1;
			}else
				return 0;
		} else if(o instanceof DatumDouble){
			DatumDouble obj = (DatumDouble)o;
			if(this.value>obj.getValue())
				return 1;
			else if(this.value<obj.getValue()){
				return -1;
			}else
				return 0;
		}else
			throw new IllegalArgumentException("Wrong type (" + o.getClass().getCanonicalName() + ") of this Object.");

	}
	
	
	@Override
	public Datum clone() {
		Datum copy = new DatumLong(value);
		return copy;
	}
	

	@Override
	public long getBytes() {
		return 64;
	}
	
	@Override
	public String toString(){
		return String.valueOf(value);
	}
	
	public static void main(String[] args){
		try(ObjectOutputStream  output = new ObjectOutputStream(new FileOutputStream(new File("test/AttrSizeTest.test")))){
			Datum data = new DatumLong(1111);
			DatumType type = DatumType.Long;
			
			System.out.println(data.getBytes());
			System.out.println(data.toString().getBytes().length);
			output.writeObject(type);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
