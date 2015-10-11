package com.pkuhelper.pkuhole;

public class CommentInfo {
	int cid;
	String text;
	boolean islz;
	long timestamp;
	public CommentInfo(int _cid, String _text, boolean _islz, long _timestamp) {
		cid=_cid;
		text=new String(_text);
		islz=_islz;
		timestamp=_timestamp*1000;
	}
}
