package io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class IOTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			RandomAccessFile raf = new RandomAccessFile(new File("test/cache.dat"),"rw");
			
			int keyIndex=0;
			Map<Integer, Long> index = new HashMap<Integer, Long>();
			raf.seek(0);
			String line="";
			do{
				//System.out.println("pointer:"+ raf.getFilePointer());
				index.put(++keyIndex, raf.getFilePointer());
				line = raf.readLine();
				//System.out.println(line);
			}while(line!=null);
			
	
			
			System.out.println("Finish indexing");
			Random ran = new Random();
			for(int i=0; i<5; i++){
				int key = ran.nextInt(keyIndex);
				raf.seek(index.get(key));
				System.out.println("seek key:"+key);
				System.out.println(raf.readLine());
			}
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
