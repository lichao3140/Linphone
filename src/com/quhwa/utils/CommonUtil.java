package com.quhwa.utils;


public class CommonUtil {
	private static String Tag = "CommonUtil";

	public static String getJpushToken(){
//		String token = getToken();
//		if(token != null || !token.equals("")){
//			return token;
//		}else{
//			token = JPushManager.getRegisterId(MyApplication.instance);
//		}
//		String token = JPushManager.getRegisterId(MyApplication.instance);
//		token = "123";
		return "123";
	}
	public static String saveToken(){
//		String token = JPushManager.getRegisterId(MyApplication.instance);
//		MyLog.print(Tag, "保存的token:"+token, MyLog.PRINT_RED);
//		MySharedPreferenceManager.saveString(MyApplication.instance, Table.TAB_USER, Table.TAB_USER_JPUSH_TOKEN_KEY, token);
		return "123";
	}
	
	public static String getToken(){
		return "123";
//		return MySharedPreferenceManager.queryString(MyApplication.instance, Table.TAB_USER, Table.TAB_USER_JPUSH_TOKEN_KEY);
	}
	
}
