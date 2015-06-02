package common;

import java.util.Stack;

import sqlparse.Config;

public class TimeCalc {
	
	private static Stack<Long> startTimes = new Stack<Long>();
	private static boolean ifPrint = Config.DebugMode;
	
	private static String getStackTraceInfo(){
		StackTraceElement ele = Thread.currentThread().getStackTrace()[3];
		return "Line number: "+ele.getLineNumber()+" - "+ele.getMethodName()+" - "+ele.getFileName();
	}
	
	public static void begin(){
		begin("");
	}
	
	public static void begin(String msg){
		if(ifPrint){
			System.out.println(startTimes.size() + ".Begin calculating time ------------------ "+ msg);
			System.out.println(getStackTraceInfo());
		}
		startTimes.push(System.currentTimeMillis());
	}
	
	public static int end(){
		return end("");
	}
	
	public static int end(String userMsg){
		long span_ms = System.currentTimeMillis() - startTimes.pop();
		String content;
		if(span_ms>1000){
			float span_s = (float)span_ms/1000;
			content = startTimes.size() + " running time : " + span_s + "s.\t" + userMsg ;
		}else
			content = startTimes.size() + " running time : " + span_ms + "ms.\t" + userMsg ;

		if(ifPrint)
			System.out.println(content);

		return (int)span_ms;
		
	}
}
