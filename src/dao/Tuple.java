package dao;

import java.io.Serializable;
import java.rmi.UnexpectedException;
import java.util.Comparator;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import ra.Aggregator;
import ra.EvaluatorArithmeticExpres;
import ra.OperatorGroupBy;

/**
 * A row of a table
 */
public class Tuple implements Serializable{
	
	private static final long serialVersionUID = -6349689782189417493L;
	
	private Datum[] dataArr;
	private Schema schema;
	
	/**
	 * Constructor
	 */	
	public Tuple(String rawLine, Schema schemaIn){
		String[] splitedData = rawLine.split("\\|");
		
		int n = schemaIn.getLength();
		dataArr = new Datum[n];
		schema = schemaIn;
		
		for(int i=0; i<n; i++){
			DatumType type = schemaIn.getColType(i);
			dataArr[i] = DatumFactory.create(splitedData[schema.getRawPosition(i)], type);
		}
	}
	
	public Tuple(Datum[] dataIn, Schema schemaIn){
		dataArr = dataIn;
		schema = schemaIn;
	}
	
	public void changeTuple(Schema newSchema){
		int length = newSchema.getLength();
		Datum[] newDataArr = new Datum[length];

		Expression[] newColSources = newSchema.getColumnSources();
		//Get new data from newSchema.source
		for(int i=0; i<length; i++){
			//Get data from old tuple
			Datum oldData = null;
			Expression newSource = newColSources[i];

			if(newSource instanceof Column){
				//is a column
				Column ns = (Column)newSource;
				oldData = getDataByName(ns);
			}else if(newSource instanceof Function){ 
				//is an aggregate function
				Function func = (Function)newSource;

				//find the key
				Aggregator aggre = newSchema.getAggregator(func);
				StringBuilder groupbyKey = new StringBuilder();
				for(String colName : aggre.getGroupByColumns()){
					if(colName.equals(OperatorGroupBy.NOGROUPBYKEY)){
						//no group by
						groupbyKey.append(OperatorGroupBy.NOGROUPBYKEY);
						break;
					}
						
					Datum groupbyColumn = getDataByName(colName); //key
					groupbyKey.append(groupbyColumn.toString());
				}
				//map to the aggregated Datum value
				oldData = aggre.getValue(groupbyKey.toString());
				
				if(oldData==null)
					try {
						throw new UnexpectedException("Can not find aggregate value, key:"+groupbyKey.toString());
					} catch (UnexpectedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}else{
				//should be a constant or expression
				EvaluatorArithmeticExpres eval = new EvaluatorArithmeticExpres(this);
				newSource.accept(eval);
				oldData = eval.getData();
			}
			newDataArr[i] = oldData;
		}
		dataArr = newDataArr;
		schema = newSchema;
	}
	
	
	public int compareTo(Tuple compTup, CompareAttribute[] attrs){
		for(CompareAttribute attr : attrs){
			String colName = attr.getColName();
			Datum selfVal = this.getDataByName(colName);
			Datum compVal = compTup.getDataByName(colName);
			int res = selfVal.compareTo(compVal);
			if(res==0)
				continue;
			
			//res!=0, two attributes are not equal
			if(attr.isAsc())
				return res;
			else
				return -res;		
		}
		
		return 0;
	}
	
	
	public static Comparator<Tuple> getComparator(final CompareAttribute[] attrs){
		Comparator<Tuple> comptr = new Comparator<Tuple>(){
			@Override
			public int compare(Tuple arg0, Tuple arg1) {
				return arg0.compareTo(arg1, attrs);
			}
		};
		return comptr;
	}
	
	
	public Datum[] getDataArr(){
		return dataArr;
	}

	
	/**
	 * Get specific data block by column index
	 * @param index
	 * @return
	 * @throws Exception 
	 */
	public Datum getData(int index) throws Exception{
		if(index>=dataArr.length){
			throw new Exception("Index out of range! index: " + index + ", Length: " + dataArr.length);
		}else{
			return dataArr[index];
		}		
	}
	
	/**
	 * Get data by column name
	 * @param colName
	 * @return
	 * @throws Exception
	 */
	public Datum getDataByName(String colName){
		int index = schema.getColIndex(colName);
		if(index<0){
			if(colName.contains(".")){
				colName = colName.split("\\.")[1];
				index = schema.getColIndex(colName);
				if(index>=0)
					return dataArr[index];
				else
					return null;
			}else
				return null;	
		}else
			return dataArr[index];
	}
	
	
	public Datum getDataByName(Column col){
		if(col.getTable()==null)
			return getDataByName(col.getColumnName());
		else{
			//return full name
			Datum data = getDataByName(col.toString());
			if(data==null)
				data = getDataByName(col.getColumnName());
			return data;
		}
			
	}
	
	/**
	 * Get Datum type by column name
	 * @param colName
	 * @return
	 */
	public DatumType getDataTypeByName(Column colName){
		String name = colName.getTable()==null?colName.getColumnName():colName.toString();
		int index = schema.getColIndex(name);
		if(index<0)
			return null;
		else
			return schema.getColType(index);
	}
	
	
	
	public String getTableName(){
		return schema.getTableName();
	}
	
	public String getTableAlias(){
		return schema.getTableAlias();
	}
	
	
	/**
	 * Print tuple on screen
	 */
	public void printTuple(){
		int i=0;
		for(Datum data : dataArr){
			i++;
			if(i==dataArr.length)
				System.out.println(data.toString());
			else
				System.out.print(data.toString()+"|");
		}
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		for(Datum data : dataArr)
			sb.append(data.toString()+'|');
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	
	public Schema getSchema(){
		return schema;
	}
	
	public long getBytes(){
		long size = 0;
		for(Datum data : dataArr){
			size += data.getBytes();
		}
		return size;
	}
	
//	public static void main(String[] args){
//		 Date d1 = new Date(1994,01,01);
//		 Date d2 = new Date(1993,12,12);
//		 System.out.println(d1.compareTo(d2));
//	}

}
