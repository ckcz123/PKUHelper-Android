package com.pkuhelper.chat;

public class ChatDetailInfo {
	int id;
	String content;
	String mime;
	long timestamp;
	String type;
	String name;
	
	public ChatDetailInfo(int _id,String _content, String _mime, long _timestamp,
			String _type) {
		id=_id;
		content=new String(_content);
		mime=new String(_mime).trim();
		timestamp=_timestamp;
		type=new String(_type).trim();
	}
}
