package dao;


/**
 * Double data cell
 */
public class DatumDouble extends Datum{
 
	private static final long serialVersionUID = 6695592229704647336L;
	private double value;
	
	public DatumDouble(String dataIn){
		super(DatumType.Double);
		value = Double.parseDouble(dataIn);
	}
	
	public DatumDouble(double dataIn){
		super(DatumType.Double);
		value = dataIn;
	}
	
	public double getValue(){
		return value;
	}

	@Override
    // why get "hashvalue" and why precision?......
	public int getHashValue(){
		//TODO Assume the precision is enough, too strong assumption
		return (int)(value*100000000);
	}
	
	@Override
	public double getNumericValue() {
		return value;
	}

	@Override
	public void setNumericValue(double valueIn) {
		value = valueIn;
	}
	
	@Override
	public int compareTo(Datum o) {
		if(o instanceof DatumDouble){
			DatumDouble obj = (DatumDouble)o;
			if(this.value>obj.value)
				return 1;
			else if(this.value<obj.value){
				return -1;
			}else
				return 0;
		} else if(o instanceof DatumLong){
			DatumLong obj = (DatumLong)o;
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
		Datum copy = new DatumDouble(value);
		return copy;
	}
	

	@Override
	public long getBytes() {
		return 64;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
