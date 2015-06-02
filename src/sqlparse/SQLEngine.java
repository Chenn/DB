package sqlparse;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import logicplan.JoinManager;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;
import ra.Aggregator;
import ra.ExternalMergeSort;
import ra.Operator;
import ra.OperatorCache;
import ra.OperatorGroupBy;
import ra.OperatorOrderBy;
import ra.OperatorProjection;
import ra.OperatorScan;
import ra.OperatorSelection;
import dao.Schema;
import dao.Tuple;

public class SQLEngine {
	/*********************	static	filed	*********************/
	private static Map<String, CreateTable> globalCreateTables;
	private static Tuple globalTuple;

	static {
		if(globalCreateTables==null)
			globalCreateTables = new HashMap<String, CreateTable>();
	} 
	
	public static Map<String, CreateTable> getGlobalCreateTables(){
		return globalCreateTables;
	}
	
	public static Tuple getGlobalTuple(){
		return globalTuple;
	}
	
	public static void setGlobalTuple(Tuple tup){
		globalTuple = tup;
	}
	
	/**
	 * Parse Create Table statement
	 * @param stmt
	 */
	public static void create(Statement stmt)
	{
		if(stmt instanceof CreateTable){
			CreateTable ct = (CreateTable)stmt;

			globalCreateTables.put(
				ct.getTable().getName(),
				ct
			);
		}
	}
	
	
	/*********************	non-static	filed	*********************/	
	private Map<String, Table> localFromItemTable;
	private File dataPath;
	
	public SQLEngine(File dataPathIn){
		localFromItemTable = new HashMap<String, Table>();

		if(dataPathIn!=null)
			dataPath = dataPathIn;
	}
	
	public Map<String, Table> getLocalTables(){
		return localFromItemTable;
	}

	
	public List<Tuple> select(SelectBody select){

		Map<String, Operator> operMap = new HashMap<String, Operator>();
		Operator oper = null;
		
		if(select instanceof PlainSelect){
			PlainSelect pselect = (PlainSelect) select;
			
			//extract local table into a map
			extractLocalTable(pselect,localFromItemTable);
			ColumnUsedParser cf = new ColumnUsedParser(pselect);
	
			/*********************    Scan&Selection    ********************/
			//parse scan
			FromItemConvertor fromItemScan = new FromItemConvertor(dataPath, globalCreateTables, cf.getColumnMapper());
			operMap = parseScan(pselect, fromItemScan);
			//push down selection
			if(operMap.size()==1){
				if(pselect.getWhere()!=null){
					//get the latest operator
					oper = operMap.get(fromItemScan.getTableName());
					oper = new OperatorSelection(oper, pselect.getWhere());
					operMap.put(fromItemScan.getTableName(), oper);	
					pselect.setWhere(null);
				}else
					oper = operMap.get(fromItemScan.getTableName());
			}else{
				/*********************   Push down Selection   ****************/
				//resolve and redistribute the big where condition
				SelectionParser distributor = pushDownSelection(pselect, operMap);
				
				/*********************    Equal Join    ********************/
				if(pselect.getJoins()!=null){
					JoinManager jm = new JoinManager(distributor.getJoinList(), operMap, Config.getSwapDir());
					oper = jm.pipeline();
					//oper = jm.executeJoin();
				}
			}
			
			//TODO delete
//			List<Tuple> tups = dump(oper);
//			System.out.println("Total size after join:"+tups.size());
//			oper = new OperatorCache(tups);
			
			/*********************    Add filtered where condition  ****************/
			if(pselect.getWhere()!=null)
				oper = new OperatorSelection(oper, pselect.getWhere());
			
			/*********************    Parsing selected items    ********************/
			SelectItemParser selectItemScan = new SelectItemParser(pselect);
			Schema newSchema = selectItemScan.getSelectedColumns();
			Aggregator[] aggrs = selectItemScan.getAggregators();
			
			/*********************    Group By + Order By   ********************/
			@SuppressWarnings("rawtypes")
			List groupbyCols = pselect.getGroupByColumnReferences();
			if(aggrs.length>0 || groupbyCols!=null){
				//if aggregate function exist or group by column exist
				OperatorGroupBy groupby = new OperatorGroupBy(oper, Config.getSwapDir(), groupbyCols, aggrs);
				OperatorOrderBy ob = null;
				if(pselect.getOrderByElements()!=null){
					@SuppressWarnings("unchecked")
					List<OrderByElement> eles = pselect.getOrderByElements();
					ob = new OperatorOrderBy(eles);
				}
				
				if(groupby.isSwap()){
					List<File> groupFiles = null;
					if(ob==null)
						groupFiles = groupby.dumpToDisk(null);
					else
						groupFiles = groupby.dumpToDisk(ob.getTupleComparator());
					
					ExternalMergeSort emsort = new ExternalMergeSort(
							groupFiles,
							groupby.getSchema(),
							ob.getCompAttrs());
					
					File mergedF = emsort.sort();
					oper = new OperatorScan(mergedF, oper.getSchema());			
				}else{
					List<Tuple> tuples = groupby.dump();
					oper = new OperatorCache(tuples);
					/*********************    Projection    ********************/
					if(!selectItemScan.getIfSelectAll()){
						oper = new OperatorProjection(oper, newSchema);
					}
					tuples = dump(oper);
					if(ob!=null)
						Collections.sort(tuples, ob.getTupleComparator());
					oper = new OperatorCache(tuples);
				}
			}else{
				/*********************  Only Projection No Group By    ********************/
				if(!selectItemScan.getIfSelectAll()){
					oper = new OperatorProjection(oper, newSchema);
				}
			}

			
			if(pselect.getLimit()==null)
				return SQLEngine.dump(oper);
			else{
				int n = (int) pselect.getLimit().getRowCount();
				List<Tuple> limitedRes = new ArrayList<Tuple>(n);

				Tuple tup;
				int limitCount = 0;
				while((tup=oper.readOneTuple())!=null){
					limitedRes.add(tup);
					limitCount++;
					if(limitCount==n)
						break;
				}

				return limitedRes;
			}	
		}
		
		return null;		
	}
	
	/**
	 * Extract local table out, fill into localFromItemTable
	 * ignore subselect, subjoin
	 * @param pselect
	 */
	public void extractLocalTable(final PlainSelect pselect, Map<String, Table> tableMap){
		//Parse From, getLocalItemTable
		TableParser fiParser = new TableParser(tableMap);
		pselect.getFromItem().accept(fiParser);
		@SuppressWarnings("unchecked")
		List<Join> joinList = pselect.getJoins();
		if(joinList!=null){
			for(Join t : joinList){
				Table table = (Table)t.getRightItem();
				table.accept(fiParser);	//generate scan operator
			}
		}
	}
	
	private Map<String, Operator> parseScan(final PlainSelect pselect, FromItemConvertor scanGenerator){
		/*********************  Create Scan    ********************/
		Map<String, Operator> operMap = new HashMap<String, Operator>(); 
		
		FromItem fromitem = pselect.getFromItem();
		fromitem.accept(scanGenerator);
		//the first table, fromItem, always have
		Operator oper = scanGenerator.getSourceOperator();	
		operMap.put(scanGenerator.getTableName(), oper);
		
		//the rest joinItems
		@SuppressWarnings("unchecked")
		List<Join> joinList = pselect.getJoins();
		if(joinList!=null){
			for(Join t : joinList){
				Table table = (Table)t.getRightItem();
				table.accept(scanGenerator);	//generate scan operator
				Operator scan = scanGenerator.getSourceOperator();
				operMap.put(scanGenerator.getTableName(), scan);
			}
		}
		
		return operMap;
	}
	
	
	private SelectionParser pushDownSelection(final PlainSelect pselect, Map<String, Operator> operMap){
		/*********************  Push down Selection    ********************/
		Expression where = pselect.getWhere();
		
		//reorganize where condition, distribute to each one
		SelectionParser selParser = new SelectionParser(operMap.keySet());
		selParser.parse(where);
		
		//for every scan operator, pipeline select
		for(Entry<String, Operator> scan : operMap.entrySet()){
			//subwhere
			String tName = scan.getKey();
			Expression subwhere = selParser.getExprByTName(tName);
			if(subwhere!=null){
				Operator scan_select = scan.getValue();
				scan_select = new OperatorSelection(scan_select, subwhere);
				operMap.put(tName, scan_select);
			}
		}
		
		//reset the original where to a filtered where
		pselect.setWhere(selParser.getExprByTName(SelectionParser.MultiTabName));			
		
		return selParser;
	}

	
	public static List<Tuple> dump(Operator oper){
		List<Tuple> results = new LinkedList<Tuple>();
		Tuple tup;
		while((tup=oper.readOneTuple())!=null){
			results.add(tup);
		}
		
		return results;
	}
}
