package dao;

/**
 * Datum factory, 
 * simple factory that creates Datum by type
 */
public class DatumFactory {
	public static Datum create(String dataIn, DatumType type){
		switch(type){
		case Long:
			return new DatumLong(dataIn);
		case Double:
			return new DatumDouble(dataIn);
		case Bool:
			return new DatumBool(dataIn);
		case String:
			return new DatumString(dataIn);
		case Date:
			return new DatumDate(dataIn);
		default:
			try {
				throw new Exception("unknown datum type : " + type);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}
}
