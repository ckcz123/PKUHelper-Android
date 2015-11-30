package com.pkuhelper.selfstudy;

public class Constants {
	public static final String DBName = "Record";
	
	public static final int span = 60*1000;// 以分作为最小时间跨度
	public static final int duration1 = 1;
	public static final int duration2 = 2;
	
	public static class Message {
		public static final int REFRESH_DURATION_STATE = 0;
		public static final int GIVE_UP = 1;
		public static final int CHANGE_HINT = 2;
	}
}
