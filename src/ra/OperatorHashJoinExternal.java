package ra;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import sqlparse.Config;
import sqlparse.TestEnvironment;

import common.TimeCalc;
import common.Tools;

import dao.CompareAttribute;
import dao.Datum;
import dao.EqualJoin;
import dao.Schema;
import dao.Tuple;



public class OperatorHashJoinExternal extends OperatorHashJoin{
	private static final String fileName = "EHJ_";	//External Hash Join
	private static final String swap = Config.getSwapDir().getPath()+"/";
	
	private EqualJoin ejInfo;
	private List<File> leftBlocks;
	private List<File> rightBlocks;
	private Schema leftSchema;
	private Schema rightSchema;
	
	//column index of each side
	private int leftColIndex;
	private int rightColIndex;
	
	//count the number of tuples in each block
	private int[] leftBlockTupsCount;
	private int[] rightBlockTupsCount;
	
	private String largeInputColName;
	private String smallInputColName;
	
	private int blockNum;
	private int index;
	private Operator dataScan;
	private HashMap<String, List<Tuple>> hashMap;
	private Queue<Tuple> joinResults;
	
	
	public OperatorHashJoinExternal(EqualJoin equalJoin, Operator left, Operator right){
		Tools.debug("[External Hash Join] " +  left.getLength() + "\t" 
				+ equalJoin + "\t" + right.getLength() + " Created!");
		
		long size;
		if(left.getLength()>right.getLength()){
			size = left.getLength();
			super.setSchema(joinSchema(equalJoin.getColName(), right.getSchema(), left.getSchema()));
		}else{
			size = right.getLength();
			super.setSchema(joinSchema(equalJoin.getColName(), left.getSchema(), right.getSchema()));
		}
		
		
		blockNum = (int)(size/Config.FileThreshold_MB);
		if(blockNum==0){
			Tools.debug("OperatorHashJoinExternal Error! The number of blocks should no be 0!");
			blockNum = 1;
		}

		ejInfo = equalJoin;
		leftBlockTupsCount = new int[blockNum];
		rightBlockTupsCount = new int[blockNum];
		
		String equalColName = ejInfo.getColName();
		//TimeCalc.begin("hash partition");
		leftBlocks = hashPartition(equalColName, left, blockNum, leftBlockTupsCount);
		rightBlocks = hashPartition(equalColName, right, blockNum, rightBlockTupsCount);
		//TimeCalc.end("Finish hash partition!");
		
		leftSchema = left.getSchema();
		rightSchema = right.getSchema();
		leftColIndex= leftSchema.getColIndex(equalColName);
		rightColIndex = rightSchema.getColIndex(equalColName);
		index = -1;
		
		initNextBlock();
		
		joinResults = new ArrayDeque<Tuple>();

	}
	
	@Override
	public Tuple readOneTuple() {
		if(index>=blockNum)
			return joinResults.poll();
		boolean isMatched;
		do{
			Tuple data = dataScan.readOneTuple();
			if(data==null){//reach the end of stream
				if(initNextBlock()){
					data = dataScan.readOneTuple();
				}else  //reach the end of blocks, no block to read
					return joinResults.poll();
			}
				
			//Join tuple and Enqueue the join result
			isMatched = joinAndBuffer(smallInputColName, largeInputColName, data, hashMap, joinResults);
		}while(!isMatched);;
		
		return joinResults.poll();
	}

	
	@Override
	public void reset() {
		dataScan.reset();
	}

	@Override
	public long getLength() {
		long leftLenAll = 0;
		long rightLenAll = 0;
		for(int i=0; i<blockNum; i++){
			long leftLen = leftBlocks.get(i).length();
			long rightLen = rightBlocks.get(i).length();
			if(leftLen!=0 && rightLen!=0){
				leftLenAll += leftLen;
				rightLenAll += rightLen;
			}
		}
		return leftLenAll>rightLenAll?leftLenAll:rightLenAll;
	}
	

	@Override
	public void close() {
		dataScan.close();
	}
	
	
	/**
	 * Initialize next block
	 * @return
	 */
	private boolean initNextBlock(){
		while(true){
			index++;
			if(index>=blockNum)	//reach the end
				return false;
			
			File leftF = leftBlocks.get(index);
			File rightF = rightBlocks.get(index);
			
			if(leftF.length()!=0 && rightF.length()!=0){
				if(dataScan!=null)
					dataScan.close();	//close previous stream
				
				if(leftF.length()>rightF.length()){
					largeInputColName = leftSchema.getColNameByName(ejInfo.getColName()).toString();
					smallInputColName = rightSchema.getColNameByName(ejInfo.getColName()).toString();
					
					hashMap = new HashMap<String, List<Tuple>>(rightBlockTupsCount[index]);
					fillHashMap(rightF, rightSchema, smallInputColName, hashMap);
					dataScan = new OperatorScanFlt(leftF, leftSchema, hashMap, leftColIndex);
					Tools.debug("  Finish initializing next block: "+leftF.getName());
				}else{
					largeInputColName = rightSchema.getColNameByName(ejInfo.getColName()).toString();
					smallInputColName = leftSchema.getColNameByName(ejInfo.getColName()).toString();
					
					hashMap = new HashMap<String, List<Tuple>>(leftBlockTupsCount[index]);
					fillHashMap(leftF, leftSchema, smallInputColName, hashMap);
					dataScan = new OperatorScanFlt(rightF, rightSchema, hashMap, rightColIndex);
					Tools.debug("  Finish initializing next block: "+rightF.getName());
				}
				
				break;
			}
		}
		
		return true;
	}
	
	
	/**
	 * Partition data in blocks by hash the column value
	 * (Blocks are files in disk)
	 * @param equalColIn
	 * @param oper
	 * @param blockSize
	 * @return
	 */
	private List<File> hashPartition(String equalColIn, Operator oper, int blockNumIn, int[] tupsCount){
		List<File> blocks = new ArrayList<File>();
		List<BufferedWriter> writers = new ArrayList<BufferedWriter>();
		String tname = oper.getSchema().getTableName();
		
		//create FileWriters
		for(int i=0; i<blockNumIn; i++){
			try {
				File block = new File(swap+fileName+tname+i);
				blocks.add(block);
				
				BufferedWriter writer = new BufferedWriter(new FileWriter(block));
				writers.add(writer);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//calculate hash and write into blocks
		Tuple tup;
		while((tup=oper.readOneTuple())!=null){
			Datum attrVal = tup.getDataByName(equalColIn);
			int hash = attrVal.getHashValue();
			int num = hash%blockNumIn;	//block number
			BufferedWriter writer = writers.get(num);
			
			try {
				writer.write(tup.toString());
				writer.newLine();
				tupsCount[num]++;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		//close
		for(BufferedWriter writer : writers){
			try {
				writer.flush();
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		oper.close();
		
		
		//change schema
		Schema schema = oper.getSchema();
		for(int i=0; i<schema.getLength();i++)
			schema.setRawPosition(i, i);
		return blocks;
	}


			
	public static void main(String[] a){
		File leftF = new File("test/ExJoinTest/ORDERS.dat");
		File rightF = new File("test/ExJoinTest/CUSTOMER.dat");
		TestEnvironment envir = new TestEnvironment();
		OperatorScan left = new OperatorScan(leftF, envir.generateSchema("orders"));
		OperatorScan right = new OperatorScan(rightF, envir.generateSchema("customer"));
		String col = "custkey";
		
		Column cuskeyA = new Column(new Table(null,"A"), col);
		Column cuskeyB = new Column(new Table(null,"B"), col);
		EqualJoin ej = new EqualJoin(cuskeyA, cuskeyB);
		TimeCalc.begin();
		
		//writeTrueResult(ej, left, right);
		List<String> results = readTrueResult();
		left.reset();
		right.reset();
		OperatorHashJoinExternal join = new OperatorHashJoinExternal(ej, left, right);		
		List<Tuple> calcResults = new ArrayList<Tuple>(results.size());
		Tuple tup;
		while((tup = join.readOneTuple())!=null){
			calcResults.add(tup);
		}
		//sorting
		CompareAttribute[] comps = new CompareAttribute[3];
		comps[0] = new CompareAttribute(calcResults.get(0).getSchema().getColNameByIndex(0), true);
		comps[1] = new CompareAttribute(calcResults.get(0).getSchema().getColNameByIndex(8), true);
		comps[2] = new CompareAttribute(calcResults.get(0).getSchema().getColNameByIndex(11), true);
		Collections.sort(calcResults, Tuple.getComparator(comps));
		
		//compare
		int size = (results.size()==calcResults.size()?results.size():-1);
		if(size==-1){
			System.out.println("Number of results are not equal!");
			return;
		}
		
		for(int i=0; i<size; i++){
			Tuple calc = calcResults.get(i);
			String trueTup = results.get(i);
			
			if(!trueTup.equalsIgnoreCase(calc.toString())){
				System.out.println("Results are not equal!");
				System.out.println("True: "+trueTup);
				System.out.println("Calc: "+calc.toString());
				
			}
		}
		
		TimeCalc.end("Correct!");
		System.out.println("End");
	}
	
	public static List<String> readTrueResult(){
		List<String> trueRes = new ArrayList<String>();
		try(BufferedReader  reader= new BufferedReader(new FileReader(new File(Config.getSwapDir().getPath()+"/ExternalJoinTestTrueReuslts.txt")))){
			String trueTup;
			while((trueTup = reader.readLine())!=null){
				trueRes.add(trueTup);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Finish reading!");
		return trueRes;
	}
	
	public static void writeTrueResult(EqualJoin ej, Operator left, Operator right){
		OperatorHashJoinMem join = new OperatorHashJoinMem(ej, left, right);
		List<Tuple> buf = new ArrayList<Tuple>();
		Tuple tup;
		while((tup = join.readOneTuple())!=null){
			buf.add(tup);
		}
		
		//sorting
		CompareAttribute[] comps = new CompareAttribute[3];
		comps[0] = new CompareAttribute(buf.get(0).getSchema().getColNameByIndex(0), true);
		comps[1] = new CompareAttribute(buf.get(0).getSchema().getColNameByIndex(8), true);
		comps[2] = new CompareAttribute(buf.get(0).getSchema().getColNameByIndex(11), true);
		
		Collections.sort(buf, Tuple.getComparator(comps));
		
		try(BufferedWriter  writer= new BufferedWriter(new FileWriter(new File(Config.getSwapDir().getPath()+"/ExternalJoinTestTrueReuslts.txt")))){
			for(Tuple t : buf){
				writer.write(t.toString());
				writer.newLine();
			}
				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Finish writing!");
		left.reset();
		right.reset();
	}

}