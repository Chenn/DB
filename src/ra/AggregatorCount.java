package ra;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.jsqlparser.expression.Function;
import dao.Datum;
import dao.DatumLong;
import dao.Tuple;

/**
 * Aggregate count function
 */
public class AggregatorCount extends Aggregator{

	private Map<String, Datum> countMap;
	private String colName;
	private boolean distinct;
	private boolean countAll;
	private Map<String, Set<String>> distinctCountMap;
	private static final String SelectAll = "*";
	
	public AggregatorCount(Function funcIn, String[] groupByCols){
		super(funcIn, groupByCols);
		
		countMap = new HashMap<String, Datum>();
		
		if(funcIn.isAllColumns()){
			countAll = true;
			colName = SelectAll;
		}else{
			countAll = false;
			//assume Count() function only takes one variable
			colName = func.getParameters().getExpressions().get(0).toString();
		}
		
		if(func.isDistinct()){
			distinct = true;
			distinctCountMap = new HashMap<String, Set<String>>();
		}
		else
			distinct = false;	
		
	}
	
	@Override
	public void aggregate(Tuple tuple, String key) {

		if(countAll){
			//do not need to compare column,
			//because it is countAll, no specific column 	
			if(!countMap.containsKey(key)){
				//insert new
				countMap.put(key, new DatumLong(1));
			}else{
				//else count ++
				countPlusPlus(key);
			}
			
		}else{
			Datum data = tuple.getDataByName(colName);
			if(data!=null){	//only count non-null value
				if(distinct){
					//Tools.debug("Group:"+key+"  aggre val:"+data.toString());
					if(!distinctCountMap.containsKey(key)){
						//insert new
						Set<String> dataSet = new HashSet<String>();
						dataSet.add(data.toString());
						distinctCountMap.put(key, dataSet);
					}else{
						//update old
						Set<String> dataSet = distinctCountMap.get(key);
						dataSet.add(data.toString());
					}
				}else{	//no distinct
					if(!countMap.containsKey(key))//insert new					
						countMap.put(key, new DatumLong(1));
					else
						countPlusPlus(key);
				}
			}
		}
	}


	@Override
	public Datum getValue(String key) {
		if(distinct){
			Set<String> valueSet = distinctCountMap.get(key);
			return new DatumLong(valueSet.size());	
		}else
			return countMap.get(key);
	}
	
	private void countPlusPlus(String key){
		Datum data = countMap.get(key);
		data.setNumericValue(data.getNumericValue()+1);
	}

}
