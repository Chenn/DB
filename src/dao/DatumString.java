package dao;


/**
 * String data cell
 */
public class DatumString extends Datum {
 
	private static final long serialVersionUID = 6843611218396643951L;
	private String value;
	
	public DatumString(String dataIn){
		super(DatumType.String);
		value = dataIn;
	}
	
	public String getValue(){
		return value;
	}
	
	public void setValue(String str){
		value = str;
	}
	
	@Override
	public int getHashValue(){
		return value.hashCode();
	}
	
	@Override
	public double getNumericValue() {
		System.out.println("Wrong get value.");
		return 0;
	}

	@Override
	public void setNumericValue(double valueIn) {
		System.out.println("Wrong set value.");
	}
	
	@Override
	public int compareTo(Datum o) {
		if(o instanceof DatumString){
			DatumString obj = (DatumString)o;
			String obj1 = this.getValue();
			String obj2 = obj.getValue();
			return obj1.compareTo(obj2);
		} else{
				throw new IllegalArgumentException("Wrong type (" + o.getClass().getCanonicalName() + ") of this Object.");
		}
	}
	
	@Override
	public Datum clone() {
		Datum copy = new DatumString(new String(value));
		return copy;
	}
	

	@Override
	public long getBytes() {
		return value.getBytes().length;
	}

	@Override
	public String toString() {
		return value;
	}
}
