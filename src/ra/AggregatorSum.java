package ra;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import dao.Datum;
import dao.Tuple;

/**
 * Aggregate sum function
 */
public class AggregatorSum extends Aggregator {

	private Map<String, Datum> sumMap;
	private Expression paraExpr;
	
	public AggregatorSum(Function funcIn, String[] groupByNamesIn) {
		super(funcIn, groupByNamesIn);
		
		sumMap = new HashMap<String, Datum>();
		
		@SuppressWarnings("unchecked")
		List<Expression> paraList = funcIn.getParameters().getExpressions();
		if(paraList.size()>1)
			throw new UnsupportedOperationException("Not supported yet."); 
		else
			paraExpr = paraList.get(0);
	}

	@Override
	public void aggregate(Tuple tuple, String key) {
		EvaluatorArithmeticExpres eval = new EvaluatorArithmeticExpres(tuple);
		paraExpr.accept(eval);
		Datum newVal = eval.getData();
		
		if(!sumMap.containsKey(key)){
			//insert new
			sumMap.put(key, newVal);
		}else{
			//update old, sum
			Datum oldVal = sumMap.get(key);
			//can not sum Bool, String, Date
			double sum = oldVal.getNumericValue()+newVal.getNumericValue();
			oldVal.setNumericValue(sum);
		}
	}

	@Override
	public Datum getValue(String key) {
		return sumMap.get(key);
	}

//	public static void main(String[] args) throws Exception{
//		
//		try {
//			Column col1 = new Column();
//			Column col2 = new Column();
//			col1.setColumnName("A");
//			col2.setColumnName("Num");
//			ColumnDefinition colDef1 = new ColumnDefinition();
//			ColumnDefinition colDef2 = new ColumnDefinition();
//			colDef1.setColumnName(col1.getColumnName());
//			colDef2.setColumnName(col2.getColumnName());
//			ColDataType type1 = new ColDataType();
//			ColDataType type2 = new ColDataType();
//			type1.setDataType("string");
//			type2.setDataType("float");
//			colDef1.setColDataType(type1);
//			colDef2.setColDataType(type2);
//			List<ColumnDefinition> defs =  new ArrayList<ColumnDefinition>();
//			defs.add(colDef1);
//			defs.add(colDef2);
//			Schema schema = new Schema(new Column[]{col1,col2},defs);
//			Tuple t1 = new Tuple(new String[]{"E","1.1"}, schema);
//			Tuple t2 = new Tuple(new String[]{"F","2.2"}, schema);
//			Tuple t3 = new Tuple(new String[]{"A","3.3"}, schema);
//			Tuple t4 = new Tuple(new String[]{"C","4.4"}, schema);
//			Tuple t5 = new Tuple(new String[]{"B","5.5"}, schema);
//			Tuple t6 = new Tuple(new String[]{"B","6.6"}, schema);
//			List<Tuple> tuples = new ArrayList<Tuple>();
//			tuples.add(t1);
//			tuples.add(t2);
//			tuples.add(t3);
//			tuples.add(t4);
//			tuples.add(t5);
//			tuples.add(t6);
//			
//			Function aggrFunc = new Function();
//			aggrFunc.setName("sum");
//			List<Expression> colList = new ArrayList<Expression>();
//			colList.add(col2);
//			ExpressionList paraList = new ExpressionList(colList);
//			aggrFunc.setParameters(paraList);
//			AggregatorSum sum = new AggregatorSum(aggrFunc, null);
//			for(Tuple t : tuples){
//				t.printTuple();
//				sum.aggregate(t, "");
//			}
//			System.out.println(sum.getValue("").getNumericValue());
//				
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
