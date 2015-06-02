package dao;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Date data cell
 */
public class DatumDate extends Datum {

	private static final long serialVersionUID = 4697762788088326757L;

	private static DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	
	private Date value;
	
	public DatumDate(String dataIn){
        // unlike the date below, determine if it's really a date
		super(DatumType.Date);
		try {
			value = format.parse(dataIn);
		} catch (ParseException e) {
			value = null;
			e.printStackTrace();
		}
	}
	
	public DatumDate(Date dataIn){
		super(DatumType.Date);
		value = dataIn;
	}
	
	public Date getValue(){
		return value;
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
	public String toString(){
		return format.format(value);
	}

	@Override
	public int compareTo(Datum o) {
		if(o instanceof DatumDate){
			DatumDate obj = (DatumDate)o;
			Date obj1 = this.getValue();
			Date obj2 = obj.getValue();
			return obj1.compareTo(obj2);
		} else
			throw new IllegalArgumentException("Wrong type (" + o.getClass().getCanonicalName() + ") of this Object.");

	}
	
	@Override
	public Datum clone() {
		Datum copy = new DatumDate((Date)value.clone());
		return copy;
	}
	

	@Override
	public long getBytes() {
		return 32;
	}

}
