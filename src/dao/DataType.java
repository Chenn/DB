package dao;

/**
 * Unify all the types in String form
 */
public class DataType {

	public final static String LONG = "long";
	public final static String DOUBLE = "double";
	public final static String DATE = "date";
	public final static String BOOL = "bool";
	public final static String STRING = "string";
	
	/**
	 * Recognize the type given a string type 
	 * @param typeIn
	 * @return
	 */
	public static String recognizeType(String typeIn){
		if(typeIn.toUpperCase().contains("CHAR")){
			return DataType.STRING;
		}else if(typeIn.toUpperCase().contains("INT")){
			return DataType.LONG;
		}else if(typeIn.toUpperCase().contains("LONG")){
			return DataType.LONG;
		}else if(typeIn.toUpperCase().contains("DATE")){
			return DataType.DATE;
		}else if(typeIn.toUpperCase().contains("DECIMAL")){
			return DataType.DOUBLE;
		}else if(typeIn.toUpperCase().contains("FLOAT")){
			return DataType.DOUBLE;
		}else if(typeIn.toUpperCase().contains("DOUBLE")){
			return DataType.DOUBLE;
		}else if(typeIn.toUpperCase().contains("STRING")){
			return DataType.STRING;
		}else if(typeIn.toUpperCase().contains("BOOL")){
			return DataType.BOOL;
		}
		else{
			throw new UnsupportedOperationException("Not supported yet. " + typeIn.toUpperCase()); 
		}
	}
}
