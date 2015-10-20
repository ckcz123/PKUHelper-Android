package com.pkuhelper.media;

public class Content {
	public int nid;
	public String title;
	public int sid;
	public String url;
	public String subscribe;
	public String time;

	public Content(int _nid, String _title, int _sid,
				   String _url, String _subscribe, String _time) {
		nid = _nid;
		title = _title;
		sid = _sid;
		url = _url;
		subscribe = _subscribe;
		if (subscribe.length() >= 30)
			subscribe = subscribe.substring(0, 29) + "...";
		time = _time;
	}

}
