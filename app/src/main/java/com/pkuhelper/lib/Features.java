package com.pkuhelper.lib;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.io.File;

public class Features {
	public Drawable drawable;
	int id;
	public String title;
	String imgurl;
	public String color;
	public String darkcolor;
	public String url;

	public Features(Context context, int _id, String _title, String _imgurl, String _color,
					String _darkcolor, String _url) {
		id = _id;
		title = _title;
		imgurl = _imgurl;
		color = _color;
		darkcolor = _darkcolor;
		url = _url;
		drawable = null;
		File file = MyFile.getCache(context, Util.getHash(url));
		if (file.exists()) {
			try {
				drawable = Drawable.createFromPath(file.getAbsolutePath());
			} catch (Exception e) {
				drawable = null;
			}
		}
		if (drawable == null) {
			Lib.getDrawable(context, Constants.REQUEST_FEATURES_IMAGE, imgurl, id);
		}
	}
}