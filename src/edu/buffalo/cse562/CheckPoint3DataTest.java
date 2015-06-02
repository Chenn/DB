package edu.buffalo.cse562;

import io.FileAccessor;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import dao.Schema;
import dao.Tuple;

public class CheckPoint3DataTest {

	private final static String TestFileDir = "test/Checkpoint3DataTest";
	
	
	
	@Test
	public void testTestSpecificSQL_tpch05() {
		String sqlPath1 = TestFileDir + "/" + "tpch_schemas.sql";
		String sqlPath2 = TestFileDir + "/" + "tpch5.sql";
		String resultPath = TestFileDir + "/" + "tpch5.expected.dat";
		
		List<Tuple> results = Main.testSpecificSQL(TestFileDir, sqlPath1, sqlPath2);
		if(results.size()>0){
			Schema schema = results.get(0).getSchema();	
			List<String> correctResults = FileAccessor.getInstance().readAllLines(resultPath);
			
			for(int i=0; i<results.size(); i++){
				System.out.println(correctResults.get(i)+"\n"+results.get(i)+"\n");	
				//Assert.assertEquals(results.get(i).toString(), correctResults.get(i));
			}

		}else
			Assert.fail("0 result.");
	}
	
	@Test
	public void testTestSpecificSQL_tpch07a() {
		String sqlPath1 = TestFileDir + "/" + "tpch_schemas.sql";
		String sqlPath2 = TestFileDir + "/" + "tpch07a.sql";
		String resultPath = TestFileDir + "/" + "tpch07a.expected.dat";
		
		List<Tuple> results = Main.testSpecificSQL(TestFileDir, sqlPath1, sqlPath2);
		if(results.size()>0){
			Schema schema = results.get(0).getSchema();	
			List<String> correctResults = FileAccessor.getInstance().readAllLines(resultPath);
			
			for(int i=0; i<results.size(); i++){
				System.out.println(correctResults.get(i)+"\n"+results.get(i)+"\n");	
				//Assert.assertEquals(results.get(i).toString(), correctResults.get(i));
			}

		}else
			Assert.fail("0 result.");
	}
	
	@Test
	public void testTestSpecificSQL_tpch07b() {
		String sqlPath1 = TestFileDir + "/" + "tpch_schemas.sql";
		String sqlPath2 = TestFileDir + "/" + "tpch07b.sql";
		String resultPath = TestFileDir + "/" + "tpch07b.expected.dat";
		
		List<Tuple> results = Main.testSpecificSQL(TestFileDir, sqlPath1, sqlPath2);
		if(results.size()>0){
			Schema schema = results.get(0).getSchema();	
			List<String> correctResults = FileAccessor.getInstance().readAllLines(resultPath);
			
			for(int i=0; i<results.size(); i++){
				System.out.println(correctResults.get(i)+"\n"+results.get(i)+"\n");	
				//Assert.assertEquals(results.get(i).toString(), correctResults.get(i));
			}

		}else
			Assert.fail("0 result.");
	}

	@Test
	public void testTestSpecificSQL_tpch07c() {
		String sqlPath1 = TestFileDir + "/" + "tpch_schemas.sql";
		String sqlPath2 = TestFileDir + "/" + "tpch07c.sql";
		String resultPath = TestFileDir + "/" + "tpch07c.expected.dat";
		
		List<Tuple> results = Main.testSpecificSQL(TestFileDir, sqlPath1, sqlPath2);
		if(results.size()>0){
			Schema schema = results.get(0).getSchema();	
			List<String> correctResults = FileAccessor.getInstance().readAllLines(resultPath);
			
			for(int i=0; i<results.size(); i++){
				System.out.println(correctResults.get(i)+"\n"+results.get(i)+"\n");	
				//Assert.assertEquals(results.get(i).toString(), correctResults.get(i));
			}

		}else
			Assert.fail("0 result.");
	}

	
	@Test
	public void testTestSpecificSQL_tpch07d() {
		String sqlPath1 = TestFileDir + "/" + "tpch_schemas.sql";
		String sqlPath2 = TestFileDir + "/" + "tpch07d.sql";
		String resultPath = TestFileDir + "/" + "tpch07d.expected.dat";
		
		List<Tuple> results = Main.testSpecificSQL(TestFileDir, sqlPath1, sqlPath2);
		if(results.size()>0){
			Schema schema = results.get(0).getSchema();	
			List<String> correctResults = FileAccessor.getInstance().readAllLines(resultPath);
			
			for(int i=0; i<results.size(); i++){
				System.out.println(correctResults.get(i)+"\n"+results.get(i)+"\n");	
				//Assert.assertEquals(results.get(i).toString(), correctResults.get(i));
			}

		}else
			Assert.fail("0 result.");
	}

	@Test
	public void testTestSpecificSQL_tpch07e() {
		String sqlPath1 = TestFileDir + "/" + "tpch_schemas.sql";
		String sqlPath2 = TestFileDir + "/" + "tpch07e.sql";
		String resultPath = TestFileDir + "/" + "tpch07e.expected.dat";
		
		List<Tuple> results = Main.testSpecificSQL(TestFileDir, sqlPath1, sqlPath2);
		if(results.size()>0){
			Schema schema = results.get(0).getSchema();	
			List<String> correctResults = FileAccessor.getInstance().readAllLines(resultPath);
			
			for(int i=0; i<results.size(); i++){
				System.out.println(correctResults.get(i)+"\n"+results.get(i)+"\n");	
				//Assert.assertEquals(results.get(i).toString(), correctResults.get(i));
			}

		}else
			Assert.fail("0 result.");
	}

	@Test
	public void testTestSpecificSQL_tpch07f() {
		String sqlPath1 = TestFileDir + "/" + "tpch_schemas.sql";
		String sqlPath2 = TestFileDir + "/" + "tpch07f.sql";
		String resultPath = TestFileDir + "/" + "tpch07f.expected.dat";
		
		List<Tuple> results = Main.testSpecificSQL(TestFileDir, sqlPath1, sqlPath2);
		if(results.size()>0){
			Schema schema = results.get(0).getSchema();	
			List<String> correctResults = FileAccessor.getInstance().readAllLines(resultPath);
			
			for(int i=0; i<results.size(); i++){
				System.out.println(correctResults.get(i)+"\n"+results.get(i)+"\n");	
				//Assert.assertEquals(results.get(i).toString(), correctResults.get(i));
			}

		}else
			Assert.fail("0 result.");
	}

	
	@Test
	public void testTestSpecificSQL_tpch07g() {
		String sqlPath1 = TestFileDir + "/" + "tpch_schemas.sql";
		String sqlPath2 = TestFileDir + "/" + "tpch07g.sql";
		String resultPath = TestFileDir + "/" + "tpch07g.expected.dat";
		
		List<Tuple> results = Main.testSpecificSQL(TestFileDir, sqlPath1, sqlPath2);
		if(results.size()>0){
			Schema schema = results.get(0).getSchema();	
			List<String> correctResults = FileAccessor.getInstance().readAllLines(resultPath);
			
			for(int i=0; i<results.size(); i++){
				System.out.println(correctResults.get(i)+"\n"+results.get(i)+"\n");	
				//Assert.assertEquals(results.get(i).toString(), correctResults.get(i));
			}

		}else
			Assert.fail("0 result.");
	}

	@Test
	public void testTestSpecificSQL_tpch10a() {
		String sqlPath1 = TestFileDir + "/" + "tpch_schemas.sql";
		String sqlPath2 = TestFileDir + "/" + "tpch10a.sql";
		String resultPath = TestFileDir + "/" + "tpch10a.expected.dat";
		
		List<Tuple> results = Main.testSpecificSQL(TestFileDir, sqlPath1, sqlPath2);
		if(results.size()>0){
			Schema schema = results.get(0).getSchema();	
			List<String> correctResults = FileAccessor.getInstance().readAllLines(resultPath);
			
			for(int i=0; i<results.size(); i++){
				System.out.println(correctResults.get(i)+"\n"+results.get(i)+"\n");	
				//Assert.assertEquals(results.get(i).toString(), correctResults.get(i));
			}

		}else
			Assert.fail("0 result.");
	}

	

//	@Test
//	public void testTestSpecificSQL_tpch10b() {
//		String sqlPath = TestFileDir + "/" + "tpch10b.sql";
//		String resultPath = TestFileDir + "/" + "tpch10b.expected.dat";
//		
//		List<Tuple> results = Main.testSpecificSQL(TestFileDir, sqlPath);
//		if(results.size()>0){
//
//			List<String> correctResults = FileAccessor.getInstance().readAllLines(resultPath);
//			
//			for(int i=0; i<results.size(); i++){
//				System.out.println(results.get(i));
//				if(i<correctResults.size())
//					System.out.println(correctResults.get(i));
//				
//				//Assert.assertEquals(results.get(i), correctResults.get(i).toString());
//			}
//
//		}else
//			Assert.fail("0 result.");
//	}
//	
//	@Test
//	public void testTestSpecificSQL_tpch10c() {
//		String sqlPath = TestFileDir + "/" + "tpch10c.sql";
//		String resultPath = TestFileDir + "/" + "tpch10c.expected.dat";
//		
//		List<Tuple> results = Main.testSpecificSQL(TestFileDir, sqlPath);
//		if(results.size()>0){
//
//			List<String> correctResults = FileAccessor.getInstance().readAllLines(resultPath);
//			
//			for(int i=0; i<results.size(); i++){
//				System.out.println(results.get(i));
//				if(i<correctResults.size())
//					System.out.println(correctResults.get(i));
//				
//				//Assert.assertEquals(results.get(i), correctResults.get(i).toString());
//			}
//
//		}else
//			Assert.fail("0 result.");
//	}
//	
//	@Test
//	public void testTestSpecificSQL_tpch10d() {
//		String sqlPath = TestFileDir + "/" + "tpch10d.sql";
//		String resultPath = TestFileDir + "/" + "tpch10d.expected.dat";
//
//		List<Tuple> results = Main.testSpecificSQL(TestFileDir, sqlPath);
//		if(results.size()>0){
//
//			List<String> correctResults = FileAccessor.getInstance().readAllLines(resultPath);
//			
//			for(int i=0; i<results.size(); i++){
//				System.out.println(results.get(i));
//				if(i<correctResults.size())
//					System.out.println(correctResults.get(i));
//				
//				//Assert.assertEquals(results.get(i), correctResults.get(i).toString());
//			}
//
//		}else
//			Assert.fail("0 result.");
//	}
	

	@Test
	public void testTestSpecificSQL_tpch12a() {
		String sqlPath1 = TestFileDir + "/" + "tpch_schemas.sql";
		String sqlPath2 = TestFileDir + "/" + "tpch12a.sql";
		String resultPath = TestFileDir + "/" + "tpch12a.expected.dat";
		
		List<Tuple> results = Main.testSpecificSQL(TestFileDir, sqlPath1, sqlPath2);
		if(results.size()>0){
			Schema schema = results.get(0).getSchema();	
			List<String> correctResults = FileAccessor.getInstance().readAllLines(resultPath);
			
			for(int i=0; i<results.size(); i++){
				System.out.println(correctResults.get(i)+"\n"+results.get(i)+"\n");	
				//Assert.assertEquals(results.get(i).toString(), correctResults.get(i));
			}

		}else
			Assert.fail("0 result.");
	}

	
	@Test
	public void testTestSpecificSQL_tpch16a() {
		String sqlPath1 = TestFileDir + "/" + "tpch_schemas.sql";
		String sqlPath2 = TestFileDir + "/" + "tpch16a.sql";
		String resultPath = TestFileDir + "/" + "tpch16a.expected.dat";
		
		List<Tuple> results = Main.testSpecificSQL(TestFileDir, sqlPath1, sqlPath2);
		if(results.size()>0){
			Schema schema = results.get(0).getSchema();	
			List<String> correctResults = FileAccessor.getInstance().readAllLines(resultPath);
			
			for(int i=0; i<results.size(); i++){
				System.out.println(correctResults.get(i)+"\n"+results.get(i)+"\n");	
				//Assert.assertEquals(results.get(i).toString(), correctResults.get(i));
			}

		}else
			Assert.fail("0 result.");
	}

	
//	@Test
//	public void testTestSpecificSQL_tpch16b() {
//		String sqlPath = TestFileDir + "/" + "tpch16b.sql";
//		String resultPath = TestFileDir + "/" + "tpch16b.expected.dat";
//		
//		List<Tuple> results = Main.testSpecificSQL(TestFileDir, sqlPath);
//		if(results.size()>0){
//
//			List<String> correctResults = FileAccessor.getInstance().readAllLines(resultPath);
//			
//			for(int i=0; i<results.size(); i++){
//				//System.out.println(results.get(i));
//				//if(i<correctResults.size())
//					//System.out.println(correctResults.get(i));
//				
//				Assert.assertEquals(results.get(i).toString(), correctResults.get(i));
//			}
//		}else
//			Assert.fail("0 result.");
//	}
//	
//	
//	@Test
//	public void testTestSpecificSQL_tpch16c() {
//		String sqlPath = TestFileDir + "/" + "tpch16c.sql";
//		String resultPath = TestFileDir + "/" + "tpch16c.expected.dat";
//		
//		List<Tuple> results = Main.testSpecificSQL(TestFileDir, sqlPath);
//		if(results.size()>0){
//
//			List<String> correctResults = FileAccessor.getInstance().readAllLines(resultPath);
//			
//			for(int i=0; i<results.size(); i++){
//				//System.out.println(results.get(i));
//				//if(i<correctResults.size())
//					//System.out.println(correctResults.get(i));
//				
//				Assert.assertEquals(results.get(i).toString(), correctResults.get(i));
//			}
//		}else
//			Assert.fail("0 result.");
//	}
//	
//	
//	
//	@Test
//	public void testTestSpecificSQL_tpch16d() {
//		String sqlPath = TestFileDir + "/" + "tpch16d.sql";
//		String resultPath = TestFileDir + "/" + "tpch16d.expected.dat";
//		
//		List<Tuple> results = Main.testSpecificSQL(TestFileDir, sqlPath);
//		if(results.size()>0){
//
//			List<String> correctResults = FileAccessor.getInstance().readAllLines(resultPath);
//			
//			for(int i=0; i<results.size(); i++){
//				//System.out.println(results.get(i));
//				//if(i<correctResults.size())
//					//System.out.println(correctResults.get(i));
//				
//				Assert.assertEquals(results.get(i).toString(), correctResults.get(i));
//			}
//		}else
//			Assert.fail("0 result.");
//	}
}

