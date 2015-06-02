package ra;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import common.Tools;

import dao.EqualJoin;
import dao.Tuple;

/**
 * Hash join operator implemented by a internal hash join,
 * that all the tables resides in memory
 */
public class OperatorHashJoinMem extends OperatorHashJoin{

	private EqualJoin ejInfo;
	//store the full name of the large input join column
	private String largeInputColName;
	private String smallInputColName;
	private Queue<Tuple> joinResults;
	private Map<String, List<Tuple>> hashMap;
	private Operator largeInput;
	
	public OperatorHashJoinMem(EqualJoin equalJoin,  Operator left, Operator right){
		Tools.debug("[Mem Join] " +equalJoin+ " Created!");
		
		ejInfo = equalJoin;
		joinResults = new ArrayDeque<Tuple>();
		hashMap = new HashMap<String, List<Tuple>>();
		
		Operator smallInput = null;
		if(left.getLength()>right.getLength()){
			largeInput = left;
			smallInput = right;
			
			largeInputColName = left.getSchema().getColNameByName(ejInfo.getColName()).toString();
			smallInputColName = right.getSchema().getColNameByName(ejInfo.getColName()).toString();
		}else{
			largeInput = right;
			smallInput = left;
			
			largeInputColName = right.getSchema().getColNameByName(ejInfo.getColName()).toString();
			smallInputColName = left.getSchema().getColNameByName(ejInfo.getColName()).toString();
		}
		
		super.setSchema(joinSchema(equalJoin.getColName(), smallInput.getSchema(), largeInput.getSchema()));
		
		fillHashMap(smallInput, smallInputColName, hashMap);
	}
	


	@Override
	public Tuple readOneTuple() {
		boolean isMatched;
		do{
			Tuple data = largeInput.readOneTuple();
			if(data==null)
				return joinResults.poll();
			//Join tuple and Enqueue the join result
			isMatched = joinAndBuffer(smallInputColName, largeInputColName, data, hashMap, joinResults);
		}while(!isMatched);;
		
		return joinResults.poll();
	}


	@Override
	public void reset() {
		joinResults = new ArrayDeque<Tuple>();
		largeInput.reset();
	}

	@Override
	public long getLength() {
		return largeInput.getLength();
	}

	@Override
	public void close() {
		largeInput.close();
	}
	
}
