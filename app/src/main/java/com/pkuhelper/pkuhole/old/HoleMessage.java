package com.pkuhelper.pkuhole.old;

public class HoleMessage {
	String id;
	String statusId;
	String time;
	String text;
	String hint;

	public HoleMessage(String _id, String _statusId, String _time, String _text) {
		id = _id;
		statusId = _statusId;
		time = _time;
		text = _text;
		hint = text;
		if (hint.length() >= 250)
			hint = hint.substring(0, 249) + "...";
	}
}
