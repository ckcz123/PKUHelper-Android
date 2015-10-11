package com.pkuhelper.bbs;

public class MessageInfo {
	int number;
	String title;
	String author;
	long timestamp;
	boolean isnew;
	public MessageInfo(int _number, String _title, String _author,
			long _timestamp, int _isnew) {
		number=_number;
		title=new String(_title);
		author=new String(_author);
		timestamp=_timestamp;
		isnew=_isnew==1;
	}	
}
