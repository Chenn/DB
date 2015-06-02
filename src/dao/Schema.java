package dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import ra.Aggregator;
import sqlparse.TestEnvironment;

/**
 * Schema of a table
 */
public class Schema implements Serializable {

	private static final long serialVersionUID = -3104984314917863119L;
	private Map<String, Integer> indexMap;
	private Map<String, Integer> fullNameIndexMap;
	private int length;
	private Table tableName;
	private Column[] columnNames;
	private Expression[] columnSources;
	private DatumType[] colTypes;
	private Map<Function, Aggregator> aggregatorMap;
	private int[] rawPosition;
	
	
	public static Schema schemaFactory(Map<String,Column> colsMapper, CreateTable ct, Table tableName) throws Exception{
		@SuppressWarnings("unchecked")
		List<ColumnDefinition> allColDefs = ct.getColumnDefinitions();
		
		List<Column> colsInUse = new ArrayList<Column>();
		List<ColumnDefinition> colDefsInUse = new ArrayList<ColumnDefinition>();
		List<Integer> rawPosition = new ArrayList<Integer>();
		for(int i = 0; i < allColDefs.size(); i++){
			ColumnDefinition colDef = allColDefs.get(i);
			Column col = new Column(tableName, colDef.getColumnName());
			
			//if colsMapper == null, it will map all the attributes
			if(colsMapper!=null){
				//Add columns that only appeared(in use) in SQL
				String colFullName = null;
				if(col.getTable().getAlias()!=null)
					colFullName = col.getTable().getAlias()+"."+col.getColumnName();
				else
					colFullName = col.toString();
				if(colsMapper.containsKey(colFullName)){
					//Tools.debug("add column: "+col.toString()+",  position: "+i);
					rawPosition.add(i);
					colsInUse.add(col);
					colDefsInUse.add(colDef);
				}
			}else{
				rawPosition.add(i);
				colsInUse.add(col);
				colDefsInUse.add(colDef);
			}
		}
		
		//to array
		int size = colsInUse.size();
		Column[] cols = new Column[size];
		ColumnDefinition[] colDefs = new ColumnDefinition[size];
		int[] rawPos = new int[size];
		colsInUse.toArray(cols);
		colDefsInUse.toArray(colDefs);
		for(int i=0; i<size; i++)
			rawPos[i] = rawPosition.get(i);
		
		return  new Schema(cols, colDefs, rawPos);
	}


	public Schema(Column[] colsIn, ColumnDefinition[] colDefsIn, int[] rawPosIn) throws Exception{
		if(colsIn.length == 0 || colDefsIn.length == 0)
			throw new IllegalArgumentException("the number of columns/column definitions is 0.");
		if(colsIn.length != colDefsIn.length)
			throw new IllegalArgumentException("Column[] size and DatumType[] size doesn't match : " + colsIn.length + "," + colDefsIn.length);
		
		length = colsIn.length;
		columnNames = colsIn;
		colTypes = new DatumType[length];
		columnSources = new Expression[length];
		rawPosition = rawPosIn;
				
		for(int i = 0; i < length; i++){
			colTypes[i] = convertColType(i, colDefsIn[i]);
			columnSources[i] = colsIn[i];	//assign it as a column
		}
		
		initialIndexMap(length, columnNames);
		initialTableName();
	}
	
	public Schema(Column[] colsIn, DatumType[] colTypesIn, Expression[] columnSourcesIn, Map<Function, Aggregator> aggreMapIn){
		if(colsIn.length == 0 || colTypesIn.length == 0)
			throw new IllegalArgumentException("the number of columns/column definitions is 0.");
		if(colsIn.length != colTypesIn.length)
			throw new IllegalArgumentException("Column[] size and DatumType[] size doesn't match : " + colsIn.length + "," + colTypesIn.length);
		
		length = colsIn.length;
		columnNames = colsIn;
		colTypes = colTypesIn;
		columnSources = columnSourcesIn;
		rawPosition = new int[length];
		for(int i = 0; i < length; i++)
			rawPosition[i] = i;
		
		if(aggreMapIn!=null)
			aggregatorMap = aggreMapIn;
		
		initialIndexMap(length, columnNames);
		initialTableName();
	}
	
	
	private void initialIndexMap(int size, Column[] columnNamesIn){
		indexMap = new HashMap<String, Integer>(size);
		fullNameIndexMap = new HashMap<String, Integer>(size);
		
		for(int i=0; i<size; i++){
			String colName = columnNamesIn[i].getColumnName().toUpperCase();
			indexMap.put(colName, i);
			
			Table colTab = columnNamesIn[i].getTable();
			if(colTab!=null){
				if(colTab.getAlias()!=null)	//alias + colName
					fullNameIndexMap.put(colTab.getAlias().toUpperCase()+"."+colName, i);
				else						//table name + colName
					fullNameIndexMap.put(columnNamesIn[i].toString().toUpperCase(), i);
			}
				
		}
	}
	
	private void initialTableName(){
		Table tab = columnNames[0].getTable();
		StringBuilder tname = new StringBuilder(tab.getName());		
		StringBuilder talias = new StringBuilder("");;
		if(tab.getAlias()!=null)
			talias.append(tab.getAlias());
		
		for(int i=1; i<columnNames.length; i++){
			Table currColTab = columnNames[i].getTable();
			Table lastColTab = columnNames[i-1].getTable();
			if(currColTab!=null&&lastColTab!=null){
				if(currColTab.getAlias()!=null&&lastColTab.getAlias()!=null){
					if(!lastColTab.getAlias().equals(currColTab.getAlias())){
						talias.append('^');
						talias.append(currColTab.getAlias());
					}	
				}
				
				if(currColTab.getName()!=null&&lastColTab.getName()!=null){
					if(!lastColTab.getName().equals(currColTab.getName())){
						tname.append('^');
						tname.append(currColTab.getName());		
					}	
				}			
			}		
		}
		tableName = new Table();
		tableName.setName(tname.toString());
		if(talias.length()!=0)
			tableName.setAlias(talias.toString());
	}
	
	
	
	/**
	 * Convert column type from String to DatumType
	 * @param index
	 * @return
	 * @throws Exception
	 */
	private DatumType convertColType(int index, ColumnDefinition colDef) throws Exception{
//		ColumnDefinition colDef = getColDefinition(index);
		String typeStr = colDef.getColDataType().getDataType();
		DatumType type;
		typeStr = DataType.recognizeType(typeStr);
		switch(typeStr){

		case DataType.LONG:
			type = DatumType.Long;
			return type;
			
		case DataType.DOUBLE:
			type = DatumType.Double;
			return type;
			
		case DataType.BOOL:
			type = DatumType.Bool;
			return type;
		
		case DataType.STRING:
			type = DatumType.String;
			return type;
		
		case DataType.DATE:
			type = DatumType.Date;
			return type;
			
		default :
			throw new Exception("Wrong input type : " + typeStr);
		}
	}
	
	public String getTableName(){
		return tableName.getName();
	}
	
	public String getTableAlias(){
		return tableName.getAlias();
	}

	
	/**
	 * Get column type
	 * @param index
	 * @return
	 */
	public DatumType getColType(int index){
		if(index >= columnNames.length)
			throw new IndexOutOfBoundsException();		
		return colTypes[index];
	}
	
	/**
	 * Get column source
	 * @param index
	 * @return
	 */
	public Expression getColSource(int index){
		if(index >= columnNames.length)
			throw new IndexOutOfBoundsException();		
		return columnSources[index];
	}

	
	public int getLength(){
		return length;
	}
	
	
	/**
	 * Get Column name by index
	 * @param index
	 * @return
	 */
	public Column getColNameByIndex(int index){
		if(index >= columnNames.length)
			throw new IndexOutOfBoundsException();		
		return columnNames[index];
	}
	
	/**
	 * Get Column name by String name
	 * @param name
	 * @return
	 */
	public Column getColNameByName(String name){
		int index = getColIndex(name);
		if(index < 0){
			if(name.contains(".")){
				name = name.split("\\.")[1];
				index = getColIndex(name);
				if(index>=0)
					return columnNames[index];
				else
					return null;
			}else
				return null;	
		}else
			return columnNames[index];
	}
	
	public Aggregator getAggregator(Function func){
		return aggregatorMap.get(func);
	}
	
	public Column[] getColumnNames(){		
		return columnNames;
	}
	
	public Expression[] getColumnSources(){
		return columnSources;
	}
	
	public DatumType[] getColTypes(){
		return colTypes;
	}
	
	/*
	 * First search full name index,if not found,
	 * then search attribute name index
	 */
	public int getColIndex(String colName){
		colName = colName.toUpperCase();
		
		Integer index = fullNameIndexMap.get(colName);
		if(index!=null)
			return index;
		else
			index = indexMap.get(colName);
		
		if(index==null)
			return -1;
		else
			return index.intValue();
	}
	
	public Map<String, Integer> getIndexMap(){
		return indexMap;
	}
	
	
	public int getRawPosition(int i){
		return rawPosition[i];
	}
	
	public void setRawPosition(int i, int position){
		rawPosition[i] = position;
	}
	
	public void setRawPosition(String colName, int position){
		int index= getColIndex(colName);
		if(index>=0)
			setRawPosition(index, position);
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(Column col : columnNames)
			sb.append(col.getWholeColumnName()+"|");
		sb.deleteCharAt(sb.length()-1);
		sb.append("\n");
		
		for(Expression source : columnSources)
			sb.append(source.toString()+"|");
		sb.deleteCharAt(sb.length()-1);
		sb.append("\n");
		
		
		for(DatumType type : colTypes)
			sb.append(type.toString()+" | ");
		sb.deleteCharAt(sb.length()-2);
		sb.append("\n");
		
		return sb.toString();
	}
	
	public static void main(String[] args) {

		TestEnvironment envir = new TestEnvironment();
		Schema schema = envir.generateSchema("lineitem");
		System.out.println(schema.toString());
	
	}
}
