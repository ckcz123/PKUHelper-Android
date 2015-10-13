package com.pkuhelper.chat;

public class ChatListInfo {
	String username;
	long timestamp;
	int number;
	String content;
	boolean hasNew;
	String name;
	public ChatListInfo(String _username, String _name,long _timestamp, String _content, int _number,
			boolean _hasNew) {
		username=new String(_username);timestamp=_timestamp;content=new String(_content);
		number=_number;hasNew=_hasNew;
		if ("".equals(_name)) name=username;
		else name=_name;
	}
}
