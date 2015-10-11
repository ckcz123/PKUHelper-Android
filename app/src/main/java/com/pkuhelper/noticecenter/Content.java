package com.pkuhelper.noticecenter;

import android.util.Log;

public class Content {
	public int nid;
	public String title;
	public String sid;
	public String url;
	public String subscribe;
	public String time;
	
	public Content(String _nid,String _title, String _sid, 
			String _url, String _subscribe, String _time) {
		nid=Integer.parseInt(_nid);
		title=_title;
		sid=_sid;
		url=_url;
		subscribe=_subscribe;
		if (subscribe.length()>=30)
			subscribe=subscribe.substring(0, 28)+"...";
		time=_time;
	}
	
	public void print() {
		Log.i("nid_"+nid, nid+"");
		Log.i("title_"+nid, title);
		Log.i("sid_"+nid, sid);
		Log.i("url_"+nid, url);
		Log.i("subscribe_"+nid, subscribe);
		Log.i("time_"+nid, time);
	}
	
}
