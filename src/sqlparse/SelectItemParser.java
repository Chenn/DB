package sqlparse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import ra.Aggregator;
import ra.AggregatorAvg;
import ra.AggregatorCount;
import ra.AggregatorSum;
import ra.OperatorGroupBy;
import dao.DatumType;
import dao.Schema;

/**
 * Parse selected item and generate new schema
 * Constant and function will be stored as a DatumString in Column[]
 * If it is a function, then parse it and store it in colSources as a Function,
 * and generate related aggregator
 */
public class SelectItemParser implements SelectItemVisitor{
	
	private List<SelectItem> items;
	private Schema newSchema;
	private boolean ifSelectAll;
	private List<Aggregator> aggregators;
	
	private List<Column> colNames;
	private List<DatumType> colTypes;
	private List<Expression> colSources;
	private Table table;
	private Map<Function, Aggregator> aggreMap;
	private String[] groupbyNames;
	
	private static final String TABLE_NAME = "";
	
	
	@SuppressWarnings("unchecked")
	public SelectItemParser(PlainSelect select){
		items = select.getSelectItems();
		ifSelectAll = false;
		
		//initialization
		colNames = new LinkedList<Column>();
		colTypes = new LinkedList<DatumType>();
		colSources = new LinkedList<Expression>();
		table = new Table(null, TABLE_NAME);
		aggreMap = new HashMap<Function, Aggregator>();
		aggregators = new ArrayList<Aggregator>();
		
		//get group by column names
		@SuppressWarnings("rawtypes")
		List groupbys = select.getGroupByColumnReferences();
		if(groupbys!=null && groupbys.size()!=0){
			groupbyNames = new String[groupbys.size()];
			for(int i=0; i<groupbys.size(); i++){
				Column col = (Column)groupbys.get(i);
				if(col.getTable()==null)
					groupbyNames[i] = col.getColumnName();
				else
					groupbyNames[i] = col.toString();
			}
		}else{
			groupbyNames = new String[1];
			groupbyNames[0] = OperatorGroupBy.NOGROUPBYKEY;
		}
				
		for(SelectItem item : items){
			item.accept(this);
		}
			
		if(!ifSelectAll){		
			//create new Schema
			int length = colNames.size();
			Column[] colArr = new Column[length];
			DatumType[] typeArr = new DatumType[length];
			Expression[] exprArr = new Expression[length];
			colNames.toArray(colArr);
			colTypes.toArray(typeArr);
			colSources.toArray(exprArr);
			newSchema = new Schema(colArr, typeArr,exprArr, aggreMap);
		}
	}
	
	public Aggregator[] getAggregators(){
		if(aggregators==null)
			return new Aggregator[]{};
		else{
			Aggregator[] aggrArr = new Aggregator[aggregators.size()];
			return aggregators.toArray(aggrArr);
		}
	}
	
	public String[] getGroupByNames(){
		return groupbyNames;
	}
	
	public Schema getSelectedColumns(){
		return newSchema;
	}
	
	public boolean getIfSelectAll(){
		return ifSelectAll;
	}

	@Override
	public void visit(AllColumns arg0) {
		ifSelectAll = true;
	}

	@Override																																										
	public void visit(AllTableColumns arg0) {
		ifSelectAll = true;
	}

	@Override
	public void visit(SelectExpressionItem arg0) {
		String alias = arg0.getAlias();
		Expression exp = arg0.getExpression();

		colTypes.add(DatumType.String);
		
		if(exp instanceof Column){
			/***** original column *****/
			Column source =(Column)exp;
			Column name;
			if(alias!=null){
				name = new Column(table,alias);
			}else{
				name = source;
			}

			colNames.add(name);
			colSources.add(source);
		}else if(exp instanceof Function){
			/***** aggregate function *****/
			Column col = new Column();
			col.setTable(table);
			if(alias!=null)
				col.setColumnName(alias);
			else
				col.setColumnName(exp.toString());
			
			Function func = (Function)exp;
			colNames.add(col);
			colSources.add(func);

			//sum, count, avg, max, min
			String funcName = func.getName().toUpperCase();
			switch(funcName){
				case "AVG":
				Aggregator aggrAvg = new AggregatorAvg(func, groupbyNames);
				aggregators.add(aggrAvg);
				aggreMap.put(func, aggrAvg);
				return;
			case "COUNT":
				Aggregator aggrCount = new AggregatorCount(func, groupbyNames);
				aggregators.add(aggrCount);
				aggreMap.put(func, aggrCount);
				return;
			case "SUM":
				Aggregator aggrSum = new AggregatorSum(func, groupbyNames);
				aggregators.add(aggrSum);
				aggreMap.put(func, aggrSum);
				return;
			case "MAX":
				return;
			case "MIN":
				return;
			default:
				return;
				
			}
		}else{
			/***** could be a arithmetic expression *****/
			Column col = new Column();
			col.setTable(table);
			if(alias!=null)
				col.setColumnName(alias);
			else
				col.setColumnName(exp.toString());
			
			colSources.add(exp);
			colNames.add(col);
		}
	}
}
