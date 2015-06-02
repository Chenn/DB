package dao;

import net.sf.jsqlparser.schema.Column;

/**
 * Store the information of an attribute for comparing 
 * in order by... sentence
 */
public class CompareAttribute {
	private Column col;
	private boolean isAsc;
	
	public CompareAttribute(Column colIn, boolean isAscIn){
		col = colIn;
		isAsc = isAscIn;
	}
	
	public boolean isAsc(){
		return isAsc;
	}
	
	public Column getCol(){
		return col;
	}
	
	public String getColName(){
		return col.toString();
	}
}
