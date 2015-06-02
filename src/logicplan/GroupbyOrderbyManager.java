package logicplan;

import java.io.File;
import java.util.Collections;
import java.util.List;

import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import ra.Aggregator;
import ra.ExternalMergeSort;
import ra.Operator;
import ra.OperatorCache;
import ra.OperatorGroupBy;
import ra.OperatorOrderBy;
import ra.OperatorProjection;
import ra.OperatorScan;
import sqlparse.Config;
import sqlparse.SQLEngine;
import sqlparse.SelectItemParser;
import dao.Schema;
import dao.Tuple;

/**
 * Decide whether use group by/order by or not,
 * and which group by/order by to use,
 * external or internal version
 */
public class GroupbyOrderbyManager {

	private Schema selectSechma;
	private Aggregator[] aggrs;
	private final PlainSelect pselect;
	private Operator data;
	private boolean ifSelectAll;
	
	private boolean doGroupBy;
	private boolean doOrderBy;
	
	public GroupbyOrderbyManager(PlainSelect pselectIn, Operator oper){
		/*********************    Parsing selected items    ********************/
		SelectItemParser selectItemScan = new SelectItemParser(pselectIn);
		selectSechma = selectItemScan.getSelectedColumns();
		aggrs = selectItemScan.getAggregators();
		pselect = pselectIn;
		data = oper;
		ifSelectAll = selectItemScan.getIfSelectAll();
		
		//if aggregate function exists or group by column exists
		doGroupBy = (aggrs.length>0 || pselect.getGroupByColumnReferences()!=null);
		doOrderBy = (pselect.getOrderByElements()!=null);
	}
	
	
	public void initialGroupBy(){	
		/*********************    Group By + Order By   ********************/
		OperatorGroupBy groupby = null;
		if(doGroupBy){
			@SuppressWarnings("rawtypes")
			List groupbyCols = pselect.getGroupByColumnReferences();
			groupby = new OperatorGroupBy(data, Config.getSwapDir(), groupbyCols, aggrs);
			
		}
		
		OperatorOrderBy orderby = null;
		if(pselect.getOrderByElements()!=null){
			@SuppressWarnings("unchecked")
			List<OrderByElement> eles = pselect.getOrderByElements();
			orderby = new OperatorOrderBy(eles);
		}
			
		if(groupby.isSwap()){
			List<File> groupFiles = null;
			if(orderby==null)
				groupFiles = groupby.dumpToDisk(null);
			else
				groupFiles = groupby.dumpToDisk(orderby.getTupleComparator());
			
			ExternalMergeSort emsort = new ExternalMergeSort(
					groupFiles,
					groupby.getSchema(),
					orderby.getCompAttrs());
			
			File mergedF = emsort.sort();
			data = new OperatorScan(mergedF, data.getSchema());			
		}else{
			List<Tuple> tuples = groupby.dump();
			data = new OperatorCache(tuples);
			/*********************    Projection    ********************/
			if(!ifSelectAll){
				data = new OperatorProjection(data, selectSechma);
			}
			tuples = SQLEngine.dump(data);
			if(orderby!=null)
				Collections.sort(tuples, orderby.getTupleComparator());
			data = new OperatorCache(tuples);
		}
	
		/*********************  Only Projection No Group By    ********************/
		if(!ifSelectAll){
			data = new OperatorProjection(data, selectSechma);
		}
		
	}
}
