package sqlparse;


import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import ra.OperatorScan;

import common.TimeCalc;

import dao.CompareAttribute;
import dao.Schema;
import dao.Tuple;


public class Test {

	public static void main(String[] args) {
		try{

			System.out.print("create a big change!!!");
			System.out.print("create a big change!!!");
			System.out.print("create a big change!!!");
			System.out.print("create a big change!!!");

			File swap = new File("test/");
			File dataDir = new File("test/cp2_grade");
			File sql = new File("test/cp2_littleBig/tpch_schemas.sql");
			
			Config.setSwapDir(swap);
			FileReader stream = new FileReader(sql);
			CCJSqlParser parser = new CCJSqlParser(stream);
			Statement stmt;
			
			SQLEngine myParser = new SQLEngine(dataDir);
			
			while((stmt = parser.Statement()) !=null){		
				if(stmt instanceof CreateTable)	
					myParser.create(stmt);
			}
			
			Table tab = new Table(null,"lineitem");
			CreateTable ct = SQLEngine.getGlobalCreateTables().get("LINEITEM");

			Column col1 = new Column(tab, "suppkey");
			Column col2 = new Column(tab, "returnflag");
			Column col3 = new Column(tab, "shipmode");
			Column col4 = new Column(tab, "orderkey");
			Column col5 = new Column(tab, "partkey");
			Column col6 = new Column(tab, "shipdate");

			Map<String, Column> colsMapper = new HashMap<String, Column>();
			colsMapper.put(col1.toString(), col1);
			colsMapper.put(col2.toString(), col2);
//			colsMapper.put(col3.toString(), col3);
//			colsMapper.put(col4.toString(), col4);
			
			Schema schema = Schema.schemaFactory(null, ct, tab);
			File dataFile = new File(dataDir+"/test.txt");
//			OperatorScan scan = new OperatorScan(,schema);
			
			FileInputStream fs = new FileInputStream(dataFile);
			FileChannel fc = fs.getChannel();
			BufferedInputStream bin = new BufferedInputStream(fs);
			DataInputStream din = new DataInputStream(fs);
			System.out.print(din.readInt());
			
			CharBuffer buffer = CharBuffer.allocate(4);
			


			OperatorScan scan = new OperatorScan(new File(dataDir+"/lineitem.dat"),schema);
			int length = 100000;
			List<Tuple> tups = new LinkedList<Tuple>();
			int count=0;
			while(count<=length){
				tups.add(scan.readOneTuple());
				count++;
			}
			TimeCalc.begin("Sorting");
			Collections.sort(tups, Tuple.getComparator(new CompareAttribute[]{new CompareAttribute(col1,true)}));
			
//			for(Tuple tup : tups)
//				System.out.println(tup.toString());
			

			TimeCalc.end("Finish Sorting!");
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
