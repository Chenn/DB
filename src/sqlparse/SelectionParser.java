package sqlparse;

import io.FileAccessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SubSelect;
import ra.EvaluatorEqualJoin;
import dao.DAOTools;
import dao.EqualJoin;


/**
 * Recognize each condition in where sentence,
 * distribute each one(Selection) by table name
 */
public class SelectionParser implements ExpressionVisitor{

	public final static String MultiTabName = "MultiTable";
	private final static Table MultiTable = new Table(null, MultiTabName);
	
	private List<EqualJoin> joins;	
	private Map<String, List<Expression>> exprsMap;
	private Stack<Table> allTables;	//all pushed down Tables
	private SelectionParserAsist orParser;
	
	public SelectionParser(Collection<String> tableNames){
		allTables = new Stack<Table>();
		exprsMap = new HashMap<String, List<Expression>>();
		joins = new ArrayList<EqualJoin>();
		orParser = new SelectionParserAsist();
		
		for(String tName : tableNames)
			exprsMap.put(tName, new LinkedList<Expression>());
		
		exprsMap.put(MultiTabName, new LinkedList<Expression>());
	}
	
	public void parse(Expression where){
		where.accept(this);
		extractEqualJoin();
		printResults();
	}

	public List<EqualJoin> getJoinList(){
		return joins;
	}
	
	public Expression getExprByTName(String tName){
		List<Expression> exprs = exprsMap.get(tName);
		if(exprs==null||exprs.size()==0)
			return null;
			
		Expression exp = exprs.get(0);
		
		for(int i=1; i<exprs.size(); i++){
			exp = new AndExpression(exp, exprs.get(i));
		}
		
		return exp;
	}

	@Override	public void visit(NullValue arg0) {}

	@Override	public void visit(Function arg0) {}

	@Override	public void visit(InverseExpression arg0) {}

	@Override	public void visit(JdbcParameter arg0) {}

	@Override	public void visit(DoubleValue arg0) {}

	@Override	public void visit(LongValue arg0) {}

	@Override 	public void visit(DateValue arg0) {}

	@Override	public void visit(TimeValue arg0) {}

	@Override	public void visit(TimestampValue arg0) {}

	@Override
	public void visit(Parenthesis arg0) {
		arg0.getExpression().accept(this);
	}

	@Override	public void visit(StringValue arg0) {}
	
	@Override
	public void visit(Addition arg0) {		
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);
	}

	@Override
	public void visit(Division arg0) {
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);
	}

	@Override
	public void visit(Multiplication arg0) {
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);
	}

	@Override
	public void visit(Subtraction arg0) {
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);
	}

	@Override
	public void visit(AndExpression arg0) {
		arg0.getLeftExpression().accept(this);
		arg0.getRightExpression().accept(this);
	}

	@Override
	public void visit(OrExpression arg0) {
		/**********  Parse them in SelectionParserAsist  ***********/
		//push all the Tables appear in OrExpression into orParser
		arg0.getLeftExpression().accept(orParser);
		arg0.getRightExpression().accept(orParser);
		
		List<Table> tabs = orParser.getORTables();
		Expression expr = new Parenthesis(arg0);
		if(tabs.size()==0)	//all constants
			distributeExpression(MultiTable, expr);
		
		if(DAOTools.isSameTable(tabs)){
			distributeExpression(tabs.get(0), expr);
		}else
			distributeExpression(MultiTable, expr);
	}

	@Override
	public void visit(Between arg0) {
		//Assume the right hand side is not a Subselect
		//only check left expression
		distributeExpression(arg0.getLeftExpression(), arg0);	
	}

	@Override
	public void visit(EqualsTo arg0) {
		distributeBinaryExpression(arg0);
	}

	@Override
	public void visit(GreaterThan arg0) {
		distributeBinaryExpression(arg0);
	}

	@Override
	public void visit(GreaterThanEquals arg0) {
		distributeBinaryExpression(arg0);
	}

	@Override
	public void visit(InExpression arg0) {
		//Assume the right hand side is not a Subselect
		//only check left expression
		distributeExpression(arg0.getLeftExpression(), arg0);	
	}

	@Override
	public void visit(IsNullExpression arg0) {
		distributeExpression(arg0.getLeftExpression(), arg0);	
	}

	@Override
	public void visit(LikeExpression arg0) {
		distributeExpression(arg0.getLeftExpression(), arg0);	
	}

	@Override
	public void visit(MinorThan arg0) {
		distributeBinaryExpression(arg0);
	}

	@Override
	public void visit(MinorThanEquals arg0) {
		distributeBinaryExpression(arg0);
	}

	@Override
	public void visit(NotEqualsTo arg0) {
		distributeBinaryExpression(arg0);
	}

	@Override
	public void visit(Column arg0) {
		allTables.push(arg0.getTable());
	}

	@Override	public void visit(SubSelect arg0) {
		//For simplicity, assume all SubSelect are from MultiTable
		allTables.push(MultiTable);
	}

	@Override
	public void visit(CaseExpression arg0) {
		throw new UnsupportedOperationException("Unexpected......"); 
	}

	@Override
	public void visit(WhenClause arg0) {}

	@Override
	public void visit(ExistsExpression arg0) {
		distributeExpression(arg0.getRightExpression(), arg0);
	}

	@Override	public void visit(AllComparisonExpression arg0) {}

	@Override	public void visit(AnyComparisonExpression arg0) {}

	@Override
	public void visit(Concat arg0) {
		distributeBinaryExpression(arg0);
	}

	@Override
	public void visit(Matches arg0) {
		distributeBinaryExpression(arg0);
	}

	@Override
	public void visit(BitwiseAnd arg0) {
		distributeBinaryExpression(arg0);
	}

	@Override
	public void visit(BitwiseOr arg0) {
		distributeBinaryExpression(arg0);
	}

	@Override
	public void visit(BitwiseXor arg0) {
		distributeBinaryExpression(arg0);
	}

	
	public void printResults(){
		if(Config.DebugMode){
			int i=1;
			for(Entry<String, List<Expression>> entry : exprsMap.entrySet()){
				String tName = entry.getKey();
				System.out.print("\t" + i + ")." + tName + ": ");
				for(Expression exp:entry.getValue()){
					System.out.print(exp.toString()+", ");
				}
				i++;
				System.out.println();
			}
			
			System.out.println("Equal Joins:");
			
			for(EqualJoin ej : joins){
				System.out.println("\t"+ej.toString());
			}
			
			System.out.println();
		}	
	}
	
	
	/**
	 * Distribute Expression to corresponding Table
	 * @param t
	 * @param expr
	 */
	private void distributeExpression(Table t, Expression expr){
		String alias = t.getAlias();
		if(alias!=null){
			if(exprsMap.containsKey(alias)){
				exprsMap.get(alias).add(expr);
			}else
				throw new UnsupportedOperationException("Unexpected......"); 
		}else if(t.getName()!=null){
			if(exprsMap.containsKey(t.getName())){
				exprsMap.get(t.getName()).add(expr);
			}else
				throw new UnsupportedOperationException("Unexpected......"); 
		}else{
			//both alias and table name are null
			//then it is a non-null table with all null value
			//it means there is only one table
			//there should not have such case
			throw new UnsupportedOperationException("Unexpected......"); 
		}
	}

	
	private void distributeExpression(Expression check, Expression expr){
		int saveLength = allTables.size();
		check.accept(this);		
		
		checkSameAndPushResult(saveLength);
		
		//pop the result table
		Table resTable = allTables.pop();
		if(resTable==null)
			distributeExpression(MultiTable, expr);
		else
			distributeExpression(resTable, expr);
	}
	
	private void distributeBinaryExpression(BinaryExpression expr){
		int saveLength = allTables.size();
		expr.getLeftExpression().accept(this);		
		checkSameAndPushResult(saveLength);
		Table leftTab = allTables.pop();
		if(leftTab!=null&&leftTab.getName().equals(MultiTabName)){
			distributeExpression(leftTab, expr);
			return;
		}
			
		expr.getRightExpression().accept(this);		
		checkSameAndPushResult(saveLength);
		Table rightTab = allTables.pop();
		if(rightTab!=null&&rightTab.getName().equals(MultiTabName)){
			distributeExpression(rightTab, expr);
			return;
		}
		
		
		if(leftTab==null&&rightTab!=null)
			distributeExpression(rightTab, expr);
		else if(leftTab!=null&&rightTab==null)
			distributeExpression(leftTab, expr);
		else if(leftTab==null&&rightTab==null)
			distributeExpression(MultiTable, expr);
		else if(DAOTools.isSameTable(leftTab, rightTab)){
			distributeExpression(leftTab, expr);
		}else
			distributeExpression(MultiTable, expr);
				
	}

	private void checkSameAndPushResult(int pastLength){
		int currLength = allTables.size();
		//no columns pushed down, no variable in this Expression.
		//the Expression contains only constants, return true
		if(currLength==pastLength){
			allTables.push(null);
			return;
		}
					
		
		int pushedTimes = currLength - pastLength;		
		List<Table> tabs = new ArrayList<Table>(pushedTimes);
		for(int i=0; i<pushedTimes; i++)
			tabs.add(allTables.pop());
		
		boolean result = DAOTools.isSameTable(tabs);
		if(result){	
			//they are all the same, get anyone of them as the result table 
			allTables.push(tabs.get(0));
		}else{
			allTables.push(MultiTable);
		}
	}

	
	/**
	 * Get EqualJoin form MultiTable of exprsMap
	 */
	private void extractEqualJoin(){
		List<Expression> multiTabExpr = exprsMap.get(MultiTabName);
		EvaluatorEqualJoin evalEJ = new EvaluatorEqualJoin();
		List<Expression> removeList = new ArrayList<Expression>();
		
		for(Expression expr : multiTabExpr){
			expr.accept(evalEJ);
			if(evalEJ.getSize()==1){
				joins.add(evalEJ.popJoin());
				removeList.add(expr);
			}else if(evalEJ.getSize()>1){
				try {
					throw new UnexpectedException("There shouldn't be more than one Equal Join!");
				} catch (UnexpectedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		//remove EqualJoin Expression in MultiTable Expression list
		for(Expression expr : removeList)
			multiTabExpr.remove(expr);

	}
	
	
	public static void main(String[] args) {
		File files = new File("D:/testDecompose/");
		File schemaFile = new File("test/cp2_littleBig/tpch_schemas.sql");
		File dataFile = new File("test/data");		
		File swapPath = new File("test/");
		for(File f : files.listFiles()){
			try {
				Config.setSwapDir(swapPath);
				SQLEngine engine = new SQLEngine(dataFile);

				//create schema
				FileReader schemaReader = new FileReader(schemaFile);
				Statement create;
				CCJSqlParser createParser = new CCJSqlParser(schemaReader);
				while((create =createParser.Statement()) !=null){		
					if(create instanceof CreateTable)	
						SQLEngine.create(create);
				}
				
				//read sql
				PlainSelect psel = FileAccessor.getInstance().parsePSelect(f);
				Map<String, Table>  tableMap = new HashMap<String, Table>();
				engine.extractLocalTable(psel, tableMap);	
				
				System.out.println(f.toString());
				System.out.println(psel.toString());
				
				Collection<String> tableNames = tableMap.keySet();
				if(tableNames.size()>1){	
					System.out.println("****parsing where: \n****"+psel.getWhere().toString());
					System.out.println("global table:"+tableNames.toString());
					Expression where = psel.getWhere();
					SelectionParser selParser = new SelectionParser(tableNames);
					selParser.parse(where);
				}else{
					System.out.println("There is only one table, do not use EvaluatorDecomposeWhere.");
				}
				System.out.println();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSQLParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
