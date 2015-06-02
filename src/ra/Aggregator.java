package ra;

import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import dao.Datum;
import dao.Tuple;

/**
 * Abstraction of aggregate function
 */
public abstract class Aggregator{
	Function func;
	String[] groupbyNames;
	
	public Aggregator(Function funcIn, String[] groupByNamesIn){
		func = funcIn;
		if(groupByNamesIn==null||groupByNamesIn.length==0)
			groupbyNames = new String[]{""};
		else
			groupbyNames = groupByNamesIn;
	}
	
	public ExpressionList getArgs(){
		return func.getParameters();
	}
	
	public Function getFunc(){
		return func;
	}
	
	public String[] getGroupByColumns(){
		return groupbyNames;
	}
	
	public abstract void aggregate(Tuple tuple, String key);
	public abstract Datum getValue(String key);
}
