package ra;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.rmi.UnexpectedException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import dao.DAOTools;
import dao.Datum;
import dao.DatumType;
import dao.Schema;
import dao.Tuple;

/**
 * Abstraction of a hash join
 */
public abstract class OperatorHashJoin implements Operator {
	private Schema joinedSchema;
	private boolean ifCheckedOrder;
	private boolean ifNeedSwitch;
	
	public OperatorHashJoin(){
		ifCheckedOrder = false;
		ifNeedSwitch = false;
	}
	
	@Override
	public Schema getSchema() {
		return joinedSchema;
	}
	
	protected void setSchema(Schema schema){
		joinedSchema = schema;
	}
	
	
	protected void joinAndWrite(String matchAttr, String dataAttr, Tuple data, Map<String,List<Tuple>> map, BufferedWriter writer) throws IOException{
		Datum keyData = data.getDataByName(dataAttr);
		if(keyData==null){
			try {
				throw new UnexpectedException("Can't get data from tuple : " + data.toString() 
						+ "\n" + data.getTableName()+", col: "+dataAttr
						+ data.getSchema().toString());
			} catch (UnexpectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		String key = keyData.toString();
		List<Tuple> matches = map.get(key);
		if(matches!=null){
			for(Tuple match : matches){
				Tuple result = joinTuple(matchAttr, dataAttr, match, data);
				writer.write(result.toString());
				writer.newLine();
			}
		}
	}
	
	protected boolean joinAndBuffer(String matchAttr, String dataAttr, Tuple data, Map<String,List<Tuple>> map,List<Tuple> buffer){
		Datum keyCol = data.getDataByName(dataAttr);
		if(keyCol==null){
			try {
				throw new UnexpectedException("Can't get data from tuple : " + data.toString() 
						+ "\n" + data.getTableName()+", col: "+dataAttr
						+ data.getSchema().toString());
			} catch (UnexpectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		String key = keyCol.toString();
		List<Tuple> matches = map.get(key);
		if(matches!=null){
			for(Tuple match : matches){
				Tuple result = joinTuple(matchAttr, dataAttr, match, data);
				buffer.add(result);
			}
			return true;
		}else
			return false;
	}
	

	protected boolean joinAndBuffer(String matchAttr, String dataAttr, Tuple data, Map<String, List<Tuple>> hashMap, Queue<Tuple> buffer){
		Datum keyCol = data.getDataByName(dataAttr);
		if(keyCol==null){
			try {
				throw new UnexpectedException("Can't get data from tuple : " + data.toString() 
						+ "\n" + data.getTableName()+", col: "+dataAttr
						+ data.getSchema().toString());
			} catch (UnexpectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		List<Tuple> matches = hashMap.get(keyCol.toString());
		if(matches!=null){
			for(Tuple match : matches)
				buffer.add(joinTuple(matchAttr, dataAttr, match, data));
			return true;
		}else
			return false;
	}

	/**
	 * Initialize hashmap
	 * @param data
	 * @param schema
	 * @param keyColName
	 * @return
	 */
	protected void fillHashMap(File data, Schema schema, String keyColName, Map<String, List<Tuple>> map){
		//TODO change the type of key of hash to the type of the key column's type 
		OperatorScan scan = new OperatorScan(data, schema);
		Tuple tup;
		
		while((tup = scan.readOneTuple())!=null){
			addTuple(keyColName,tup,map);
		}
	}
	
	protected void fillHashMap(Operator data, String keyColName, Map<String, List<Tuple>> map){ 
		Tuple tup;
		while((tup = data.readOneTuple())!=null){
			addTuple(keyColName,tup,map);
		}
		data.close();
	}
	
	
	protected void addTuple(String attr, Tuple tuple, Map<String, List<Tuple>> map){
		Datum keyData = tuple.getDataByName(attr);
		if(keyData==null){
			try {
				throw new UnexpectedException("Can't get data from tuple : " + tuple.toString());
			} catch (UnexpectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		String key = keyData.toString();
		List<Tuple> matchList = map.get(key);
		if(matchList==null){
			matchList = new LinkedList<Tuple>();
			matchList.add(tuple);
			map.put(key, matchList);
		}else
			matchList.add(tuple);
	}
	
	public Tuple joinTuple(String t1EqualCol, String t2EqualCol, Tuple t1, Tuple t2){
		String equalColName = t2EqualCol;
		//the order of input argument is not the same 
		//as the order in schema, may need to switch the order
		if(ifNeedSwitch){
			Tuple tmp = t1;
			t1 = t2;
			t2 = tmp;
			equalColName = t1EqualCol;
		}
		
		if(!ifCheckedOrder){ 
			//first time to check order, compare the name of first column table
			Table joinedTab = joinedSchema.getColNameByIndex(0).getTable();
			Table t1Tab = t1.getSchema().getColNameByIndex(0).getTable();
			if(!DAOTools.isSameTable(joinedTab, t1Tab)){
				Tuple tmp = t1;
				t1 = t2;
				t2 = tmp;
				
				equalColName = t1EqualCol;
				ifNeedSwitch = true;
			}
			
			ifCheckedOrder = true;
		}
		
		
		Datum[] newDataArr = new Datum[joinedSchema.getLength()];

		int j = 0;
		for(int i=0; i<t1.getDataArr().length; i++){
			newDataArr[j] = t1.getDataArr()[i];
			j++;
		}
		
		
		int equalColIndex = t2.getSchema().getColIndex(equalColName);
		if(equalColIndex<0){
			try {
				throw new UnexpectedException("Can't find Column index : " + equalColName);
			} catch (UnexpectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for(int i=0; i<t2.getDataArr().length; i++){
			if(i==equalColIndex)
				continue;
			newDataArr[j] = t2.getDataArr()[i];
			j++;
		}
		
		return new Tuple(newDataArr, joinedSchema);
	}
	
	public Schema joinSchema(String equalColNameIn, Schema s1, Schema s2){
		Column[] colNames1 = s1.getColumnNames();
		Expression[] colSources1 = s1.getColumnSources();
		DatumType[] colTypes1 = s1.getColTypes();
		
		Column[] colNames2 = s2.getColumnNames();
		Expression[] colSources2 = s2.getColumnSources();
		DatumType[] colTypes2 = s2.getColTypes();
		
		int length = s1.getLength() + s2.getLength() -1;
		Column[] joinedNames = new Column[length];
		Expression[] joinedSources = new Expression[length];
		DatumType[] joinedTypes = new DatumType[length];
		
		int j=0;
		for(int i=0; i<colNames1.length; i++){
			joinedNames[j] = colNames1[i];
			joinedSources[j] = colSources1[i];
			joinedTypes[j] = colTypes1[i];
			j++;
		}
		
		int equalColIndex = s2.getColIndex(equalColNameIn);
		for(int i=0; i<colNames2.length; i++){
			if(i==equalColIndex)
				continue;
			joinedNames[j] = colNames2[i];
			joinedSources[j] = colSources2[i];
			joinedTypes[j] = colTypes2[i];
			j++;
		}
		
		
		return new Schema(joinedNames, joinedTypes, joinedSources, null);
	}
}
