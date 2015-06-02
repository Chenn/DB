package sqlparse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import dao.Schema;

public class TestEnvironment {
	private  Map<String, CreateTable> createTables;
	
	public TestEnvironment(){
		// TODO Auto-generated method stub
		File sql = new File("test/cp2_littleBig/tpch_schemas.sql");

		FileReader stream;
		try {
			stream = new FileReader(sql);
			CCJSqlParser parser = new CCJSqlParser(stream);
			Statement stmt;
			
			while((stmt = parser.Statement()) !=null){		
				if(stmt instanceof CreateTable)	
					SQLEngine.create(stmt);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		createTables = SQLEngine.getGlobalCreateTables();
	}
	
	public TestEnvironment(File schemaFile){
		FileReader stream;
		try {
			stream = new FileReader(schemaFile);
			CCJSqlParser parser = new CCJSqlParser(stream);
			Statement stmt;
			
			while((stmt = parser.Statement()) !=null){		
				if(stmt instanceof CreateTable)	
					SQLEngine.create(stmt);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		createTables = SQLEngine.getGlobalCreateTables();
	}
	
	public Schema generateSchema(String tableName){
		Schema schema = null;
		Table tab = new Table(null,tableName);
		CreateTable ct = createTables.get(tableName.toUpperCase());
		try {
			schema = Schema.schemaFactory(null, ct, tab);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return schema;
	}
}
