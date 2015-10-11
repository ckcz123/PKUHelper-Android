package com.pkuhelper.bbs;

public class SearchInfo {
	String board;
	int threadid;
	String author;
	long timestamp;
	int number;
	String title;
	
	public SearchInfo(String _board, int _threadid, 
			String _title, String _author, int _number, long _timestamp) {
		board=new String(_board);
		threadid=_threadid;
		title=new String(_title);
		author=new String(_author);
		number=_number;
		timestamp=_timestamp;
	}
}
