package sqlparse;

import java.io.File;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import ra.Operator;
import ra.OperatorCache;
import ra.OperatorScan;
import dao.Schema;
import dao.Tuple;


/**
 * Convert a FromItem to an Operator, data source
 * A FromItem could be a Table/Subselect/Subjoin object
 */
public class FromItemConvertor implements FromItemVisitor{
	private final File dataPath;
	private Map<String,CreateTable> tables;
	private String tableName;
	private Map<String, Column> colsInUseMapper;
	private Schema schema = null;
	private Operator source = null;
	
	public FromItemConvertor(File basePath, Map<String,CreateTable> tablesIn, Map<String, Column> allCols)
	{
		colsInUseMapper = allCols;
		dataPath = basePath;
		tables = tablesIn;
	}
	
	public String getTableName(){
		return tableName;
	}
	
	public Schema getSchema(){
		return schema;
	}
	
	public Operator getSourceOperator(){
		return source;
	}
	
	public void visit (SubJoin subjoin)
	{
		throw new UnsupportedOperationException("Unexpected......"); 
	}
	
	public void visit(SubSelect subselect)
	{
		if(subselect.getAlias()==null)
			this.tableName = subselect.toString();
		else
			this.tableName = subselect.getAlias();
		
		SQLEngine parser = new SQLEngine(dataPath);
		List<Tuple> tuples = parser.select(subselect.getSelectBody());
		source = new OperatorCache(tuples); 
	}
	
	public void visit(Table tableName)
	{
		String alias = tableName.getAlias();
		if(alias!=null){
			this.tableName = alias;
		}else{
			this.tableName =tableName.getName();
		}
		
		CreateTable ctable = tables.get(tableName.getName().toUpperCase());

		try {
			if(ctable==null)
				throw new Exception("No such table : " + tableName.getName());
			
			schema = Schema.schemaFactory(colsInUseMapper, ctable, tableName);
			source = new OperatorScan(
					new File(dataPath, tableName.getName() + ".dat"),
					schema
				);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
