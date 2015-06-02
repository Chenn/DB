package logicplan;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import ra.Operator;
import ra.OperatorHashJoin;
import ra.OperatorHashJoinExternal;
import ra.OperatorHashJoinMem;
import sqlparse.Config;

import common.TimeCalc;

import dao.EqualJoin;

/**
 * Decide which join to use,
 * what is the order to join
 */
public class JoinManager {
	private Set<String> joinedHistory;
	private List<EqualJoin> joinList;
	private Map<String, Operator> operMap;
	private File swapDir;
	
	public JoinManager(List<EqualJoin> joins, Map<String, Operator> operMapIn, File swapDirIn){
		joinedHistory = new HashSet<String>();
		joinList = joins;
		operMap = operMapIn;
		swapDir = swapDirIn;
		
		//find and set size
		for(EqualJoin ej : joins){
			ej.setLeftSize(operMap.get(ej.getLeftTableName()).getLength());
			ej.setRightSize(operMap.get(ej.getRightTableName()).getLength());
		}
		
		//move the biggest last, and
		//make each of them has an overlap, 
		//so that easy to pipeline
		reorder(joinList);
	}
	
	
	/**
	 * Pipeline joins
	 * @return
	 */
	public Operator pipeline(){
		EqualJoin firstJoin = joinList.get(0);
		TimeCalc.begin(firstJoin.toString());
		OperatorHashJoin hashJoin = decideWhichToCreate(firstJoin, operMap);
		TimeCalc.end(firstJoin.toString()+" Finish create the first hash join.");
		joinedHistory.add(firstJoin.getLeftTableName());
		joinedHistory.add(firstJoin.getRightTableName());
		
		for(int i=1; i<joinList.size(); i++){
			EqualJoin ej = joinList.get(i);
			Operator leftOper = operMap.get(ej.getLeftTableName());
			Operator rightOper = operMap.get(ej.getRightTableName());
			
			Operator neverJoinedOper;
			if(joinedHistory.contains(ej.getLeftTableName()))
				neverJoinedOper = rightOper;
			else
				neverJoinedOper = leftOper;
			
			TimeCalc.begin(ej.toString());
			hashJoin = decideWhichToCreate(ej, hashJoin, neverJoinedOper);
			TimeCalc.end(ej.toString()+" Finish pipeline one hash join " + ej);
			
			joinedHistory.add(ej.getLeftTableName());
			joinedHistory.add(ej.getRightTableName());
		}
		
		
		return hashJoin;
	}
	
	/**
	 * Decide which join to create the first join
	 * @param ej
	 * @param operMap
	 * @return
	 */
	private OperatorHashJoin decideWhichToCreate(EqualJoin ej,  Map<String, Operator> operMap){
		Operator left = operMap.get(ej.getLeftTableName());
		Operator right = operMap.get(ej.getRightTableName());

		if(left.getLength()>Config.FileThreshold_MB&&right.getLength()>Config.FileThreshold_MB){
			return new OperatorHashJoinExternal(ej, left, right); 
		}else{
			return new OperatorHashJoinMem(ej, left, right);
		}
	}
	
	/**
	 * Decide which join to create the rest joins
	 * @param ej
	 * @param joined
	 * @param neverJoined
	 * @return
	 */
	private OperatorHashJoin decideWhichToCreate(EqualJoin ej, Operator joined, Operator neverJoined){
//		if(joined.getLength()>Config.FileThreshold_MB && neverJoined.getLength()>Config.FileThreshold_MB){
//			return new OperatorHashJoinExternal(ej, joined, neverJoined); 
//		}else{
//			return new OperatorHashJoinMem(ej, joined, neverJoined);
//		}
		return new OperatorHashJoinMem(ej, joined, neverJoined);
	}

	
	
	

	
	/**
	 * put the length of the largest difference of join to the first
	 * based on the max difference, the first join in the list, 
	 * reorder it by the finding the one which overlap the previous join,
	 * each join should have one table overlapped in the previous join
	 *  in order to pipeline them
	 * @param joins
	 */
	private void reorder(List<EqualJoin> joins){
		//
		int n  = joins.size();
		int maxIndex = 0;
		long min = Integer.MIN_VALUE;
		//find the index of the max difference between left and right 
		for(int i=0; i<n; i++){
			EqualJoin ej = joins.get(i);
			long diff = ej.getLeftSize()-ej.getRightSize();
			if(diff<0)
				diff = diff*(-1);
			
			if(diff>min){
				maxIndex = i;
				min = diff;
			}
		}
		//swap the max diff value to the first
		swap(joins, 0, maxIndex);
		
		
		//reorder based one the first
		Set<String> previous = new HashSet<String>(n+1);
		previous.add(joins.get(0).getLeftTableName());
		previous.add(joins.get(0).getRightTableName());
		for(int i=1; i<n; i++){
			EqualJoin current = joins.get(i);
			boolean left = previous.contains(current.getLeftTableName());
			boolean right = previous.contains(current.getRightTableName());
			if(!left&&!right){
				//both not contains, then switch
				for(int j=i+1; j<n; j++){
					EqualJoin after = joins.get(j);
					boolean leftAfter = previous.contains(after.getLeftTableName());
					boolean rightAfter = previous.contains(after.getRightTableName());
					if(leftAfter||rightAfter){
						swap(joins, i, j);
						previous.add(after.getLeftTableName());
						previous.add(after.getRightTableName());
						break;
					}
				}//end for j
			}else{
				previous.add(current.getLeftTableName());
				previous.add(current.getRightTableName());
			}
		}//end for i
	}
	
	
	private static void swap(List<EqualJoin> list, int i, int j){
		EqualJoin temp = list.get(i);
		list.set(i, list.get(j));
		list.set(j, temp);
	}
	
	public static void main(String[] args){
		List<EqualJoin> ejs = new ArrayList<EqualJoin>();
		Column col1t1 = new Column(new Table(null,"t1"), "c1");
		Column col1t2 = new Column(new Table(null,"t2"), "c1");
		EqualJoin ej = new EqualJoin(col1t1,col1t2);
		ej.setLeftSize(100);
		ej.setRightSize(200);
		ejs.add(ej);
		
		Column col2t2 = new Column(new Table(null,"t2"), "c2");
		Column col2t3 = new Column(new Table(null,"t3"), "c2");
		ej = new EqualJoin(col2t2,col2t3);
		ej.setLeftSize(200);
		ej.setRightSize(300);
		ejs.add(ej);
		
		Column col3t3 = new Column(new Table(null,"t3"), "c3");
		Column col3t4 = new Column(new Table(null,"t4"), "c3");
		ej = new EqualJoin(col3t3,col3t4);
		ej.setLeftSize(30);
		ej.setRightSize(40);
		ejs.add(ej);
		
		Column col4t4 = new Column(new Table(null,"t4"), "c4");
		Column col4t5 = new Column(new Table(null,"t5"), "c4");
		ej = new EqualJoin(col4t4,col4t5);
		ej.setLeftSize(40);
		ej.setRightSize(50);
		ejs.add(ej);
		
		Column col5t5 = new Column(new Table(null,"t5"), "c5");
		Column col5t6 = new Column(new Table(null,"t6"), "c5");
		ej = new EqualJoin(col5t5,col5t6);
		ej.setLeftSize(50);
		ej.setRightSize(60);
		ejs.add(ej);

		//reorder(ejs);
		for(EqualJoin j : ejs)
			System.out.println(j);
	}
	
	
}
