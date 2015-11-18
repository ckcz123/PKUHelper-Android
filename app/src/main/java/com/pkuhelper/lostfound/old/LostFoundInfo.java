package com.pkuhelper.lostfound.old;

import com.pkuhelper.lib.Constants;

public class LostFoundInfo {
	public static final int LOST = 1;
	public static final int FOUND = 2;
	public static final int TYPE_CARD = 3;
	public static final int TYPE_BOOK = 4;
	public static final int TYPE_DEVICE = 5;
	public static final int TYPE_OTHERS = 6;

	int id;
	String name;
	int lost_or_found;
	int type;
	String detail;
	long posttime;
	long actiontime;
	String imgURL;
	String thumbImgUrl;
	String imgName;
	String posterUid;
	String posterPhone;
	String posterName;
	String posterCollege;

	public LostFoundInfo(int _id, String _name, String _lost_or_found, String _type,
						 String _detail, long _posttime, long _actiontime, String _image,
						 String _posterUid, String _posterPhone, String _posterName, String _posterCollege) {
		id = _id;
		name = _name;
		if ("lost".equals(_lost_or_found)) lost_or_found = LOST;
		else lost_or_found = FOUND;
		if ("card".equals(_type)) type = TYPE_CARD;
		else if ("book".equals(_type)) type = TYPE_BOOK;
		else if ("device".equals(_type)) type = TYPE_DEVICE;
		else type = TYPE_OTHERS;
		detail = _detail;
		//posttime=_posttime+28800;  // 8 hours
		//actiontime=_actiontime+28800;
		posttime = _posttime;
		actiontime = _actiontime;
		if (_image != null && !"".equals(_image)) {
			imgName = _image;
			imgURL = Constants.domain + "/services/image/" + _image + ".jpg";
			thumbImgUrl = Constants.domain + "/services/image/" + _image + "_thumb.jpg";
		} else {
			imgURL = "";
			thumbImgUrl = "";
			imgName = "";
		}
		posterUid = _posterUid;
		posterPhone = _posterPhone;
		posterName = _posterName;
		posterCollege = _posterCollege;
		if (!"".equals(thumbImgUrl))
			Image.requestImage(id, thumbImgUrl);
	}

}
