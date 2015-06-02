package ra;

import java.io.FileNotFoundException;
import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.select.OrderByElement;
import dao.CompareAttribute;
import dao.Schema;
import dao.Tuple;

public class OperatorOrderBy{
	
	private CompareAttribute[] compAttrs;
	
	public OperatorOrderBy(List<OrderByElement> eles){
	
		compAttrs = new CompareAttribute[eles.size()];
		
		for(int i=0; i<eles.size(); i++){
			OrderByElement obe = eles.get(i);
			Expression expr = obe.getExpression();
			if(expr instanceof Column){
				Column col = (Column)expr;
				compAttrs[i] = new CompareAttribute(col, obe.isAsc());
			} else
				try {
					throw new UnexpectedException("Unsupported, OrderByElement is an expression! "+expr.toString());
				} catch (UnexpectedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public CompareAttribute[] getCompAttrs(){
		return compAttrs;
	}
	
	
	public Comparator<Tuple> getTupleComparator(){
		return Tuple.getComparator(getCompAttrs());
	}
	
	 
	public static void main(String[] args) throws Exception{
		
		try {
			Table tab = new Table(null,"test");
			Column col1 = new Column(tab, "A");
			Column col2 = new Column(tab, "Num");
			ColumnDefinition colDef1 = new ColumnDefinition();
			ColumnDefinition colDef2 = new ColumnDefinition();
			colDef1.setColumnName(col1.getColumnName());
			colDef2.setColumnName(col2.getColumnName());
			ColDataType type1 = new ColDataType();
			ColDataType type2 = new ColDataType();
			type1.setDataType("string");
			type2.setDataType("int");
			colDef1.setColDataType(type1);
			colDef2.setColDataType(type2);
			List<ColumnDefinition> defs =  new ArrayList<ColumnDefinition>();
			defs.add(colDef1);
			defs.add(colDef2);
			Schema schema = new Schema(new Column[]{col1,col2}, new ColumnDefinition[]{colDef1, colDef2}, new int[]{0,1});
			Tuple t1 = new Tuple(new String("C|22"), schema);
			Tuple t2 = new Tuple(new String("C|5"), schema);
			Tuple t3 = new Tuple(new String("A|11"), schema);
			Tuple t4 = new Tuple(new String("C|8"), schema);
			Tuple t5 = new Tuple(new String("B|1"), schema);
			Tuple t6 = new Tuple(new String("B|4"), schema);
			List<Tuple> tuples = new ArrayList<Tuple>();
			tuples.add(t1);
			tuples.add(t2);
			tuples.add(t3);
			tuples.add(t4);
			tuples.add(t5);
			tuples.add(t6);
			
			//order by
			List<OrderByElement> eles = new ArrayList<OrderByElement>();
			OrderByElement ele1 = new OrderByElement();
			OrderByElement ele2 = new OrderByElement();
			ele1.setExpression(col1);
			ele2.setExpression(col2);
			ele2.setAsc(true);
			eles.add(ele1);
			eles.add(ele2);
			
			OperatorOrderBy ob = new OperatorOrderBy(eles);
			Collections.sort(tuples, Tuple.getComparator(ob.getCompAttrs()));
			for(Tuple t : tuples){
				t.printTuple();
			}
				
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

