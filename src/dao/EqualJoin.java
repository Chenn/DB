package dao;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;


public class EqualJoin {
	private Column leftCol;
	private Column rightCol;
	private String colName;
	private long leftSize;
	private long rightSize;
	
	public EqualJoin(Column left, Column right){
		if(left.getColumnName().equalsIgnoreCase(right.getColumnName())&&!left.getTable().getName().equalsIgnoreCase(right.getTable().getName())){
			leftCol = left;
			rightCol = right;
			colName = left.getColumnName();
			leftSize = 0;
			rightSize = 0;
		}else{
			throw new IllegalArgumentException("Illegal argument: "+left.toString()+", "+right.toString());
		}
	}
	
	public Table getLeftTable(){
		return leftCol.getTable();
	}
	
	public Table getRightTable(){
		return rightCol.getTable();
	}
	
	public String getColName(){
		return colName;
	}
	
	public Column getLeftColumn(){
		return leftCol;
	}
	
	public Column getRightColumn(){
		return rightCol;
	}
	
	public String getLeftTableName(){
		return leftCol.getTable().getName();
	}
	
	public String getRightTableName(){
		return rightCol.getTable().getName();
	}

	public long getTotalSize() {
		return leftSize+rightSize;
	}

	public long getLeftSize() {
		return leftSize;
	}

	public void setLeftSize(long leftSize) {
		this.leftSize = leftSize;
	}

	public long getRightSize() {
		return rightSize;
	}

	public void setRightSize(long rightSize) {
		this.rightSize = rightSize;
	}
	
	public String toString(){
		return getLeftTableName()+"^"+getRightTableName()+" ("+getColName()+")";
	}
}
