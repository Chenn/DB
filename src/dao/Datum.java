package dao;

import java.io.Serializable;


/**
 * Abstraction of a cell, the cell in a row in a table
 */
public abstract class Datum implements Comparable<Datum>, Serializable {

    // UID? D
	private static final long serialVersionUID = 1517024754320323043L;
	private DatumType type;
	
	public Datum(DatumType typeIn){
		type = typeIn;
	}
	
	public DatumType getType(){
		return type;
	}
	
	@Override
	public abstract String toString();
	
	public abstract int getHashValue();
	
	/**
	 * they are equal if they have the same type and value.
	 */
	@Override
	public boolean equals(Object obj){
		if(obj==null)
			return false;
		
		if(obj instanceof Datum){
			Datum compData = (Datum)obj;
			int compResult = this.compareTo(compData);
			return (compResult==0);
		}else{
			return false;	
		}
	}
	
	
	public abstract double getNumericValue();
	public abstract void setNumericValue(double valueIn);
	public abstract Datum clone();
	
	/**
	 * compare whether two Datum objects are equal (have the same value).
	 * @param data1
	 * @param data2
	 * @return
	 * @throws Exception
	 */

    // space to improve...D
	public static boolean equals(Datum data1, Datum data2) throws Exception{
		if(data1 instanceof DatumBool && data2 instanceof DatumBool){
			DatumBool bool1 = (DatumBool)data1;
			DatumBool bool2 = (DatumBool)data2;
			if(bool1.getValue()==bool2.getValue())
				return true;
			else
				return false;
		}else if(data1 instanceof DatumString && data2 instanceof DatumString){
			DatumString str1 = (DatumString)data1;
			DatumString str2 = (DatumString)data2;
			if(str1.getValue().equalsIgnoreCase(str2.getValue()))
				return true;
			else
				return false;
		}else if(data1 instanceof DatumDate && data2 instanceof DatumDate){
			DatumDate date1 = (DatumDate)data1;
			DatumDate date2 = (DatumDate)data2;
			if(compare(date1,date2)==0)	return true;
			else return false;	
		}else if(data1 instanceof DatumLong||data1 instanceof DatumDouble){
			if(data1 instanceof DatumLong||data1 instanceof DatumDouble){
				if(compare(data1,data2)==0)	return true;
				else return false;	
			}else
				return false;
		}else
			return false;
	}
	
	/**
	 * Compare two Datum objects, larger return 1, equal return 0, less return -1
	 * Cannot compare DatumString, DatumBool
	 * @param data1
	 * @param data2
	 * @return
	 * @throws Exception
	 */
	public static int compare(Datum data1, Datum data2) throws Exception{
		if(data1 instanceof DatumString||data2 instanceof DatumString)
			throw new Exception("can not compare string, it might be variables.");
		if(data1 instanceof DatumBool||data2 instanceof DatumBool)
			throw new Exception("can not compare boolean value.");
			
		if(data1 instanceof DatumDate){
			if(data2 instanceof DatumDate){
				DatumDate val1 = (DatumDate)data1;
				DatumDate val2 = (DatumDate)data2;
				
				if(val1.getValue().compareTo(val2.getValue())>0)
					return 1;	//Date1 is after the Date2 
				else if(val1.getValue().compareTo(val2.getValue())<0)
					return -1;
				else
					return 0;
			}else
				throw new Exception("DatumDate can not compare two different type.");
		}else{
			double val1 = data1.getNumericValue();
			double val2 = data2.getNumericValue();
			if(val1>val2) return 1;
			else if(val1<val2) return -1;
			else return 0;
		}
	}

	public abstract long getBytes();
}
