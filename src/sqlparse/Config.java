package sqlparse;

import java.io.File;


public class Config {
	public static final int Buffer_SIZE = 100000;	//50w row, 5 attributes ~ 20MB
	public static final long OneMB = 1048576;	//1024*1024
	
	/**
	 * If a file's size>Threshold_MB, then write into disk
	 */
	public static final long FileThreshold_MB = 500*OneMB;	//MB
	
	//default value
	private static File swapDir = new File("test/swap/");
	
	public static final boolean DebugMode = false;

	public static final int OneTupeSize_Byte = 100;
	
	public static File getSwapDir(){
		return swapDir;
	}
	
	public static void setSwapDir(File f){
		swapDir = f;
	}
	
	public static boolean canSwap(){
		if(getSwapDir()==null)
			return false;
		else
			return true;
	}

}
