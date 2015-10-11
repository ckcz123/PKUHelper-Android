package com.pkuhelper.lostfound;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;

import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.MyBitmapFactory;
import com.pkuhelper.lib.MyFile;
import com.pkuhelper.lib.Util;

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
	Bitmap bitmap;
	
	Context context;
	
	public LostFoundInfo(
			Context context,
			Handler handler, int _id,String _name, String _lost_or_found, String _type,
			String _detail, long _posttime, long _actiontime, String _image,
			String _posterUid, String _posterPhone, String _posterName, String _posterCollege) {
		this.context=context;
		id=_id;
		name=_name;
		if ("lost".equals(_lost_or_found)) lost_or_found=LOST;
		else lost_or_found=FOUND;
		if ("card".equals(_type)) type=TYPE_CARD;
		else if ("book".equals(_type)) type=TYPE_BOOK;
		else if ("device".equals(_type)) type=TYPE_DEVICE;
		else type=TYPE_OTHERS;
		detail=_detail;
		//posttime=_posttime+28800;  // 8 hours
		//actiontime=_actiontime+28800;
		posttime=_posttime;
		actiontime=_actiontime;
		if (_image!=null && !"".equals(_image)) {
			imgName=_image;
			imgURL=Constants.domain+"/services/image/"+_image+".jpg";
			thumbImgUrl=Constants.domain+"/services/image/"+_image+"_thumb.jpg";
		}
		else {
			imgURL="";thumbImgUrl="";imgName="";
		}
		posterUid=_posterUid;
		posterPhone=_posterPhone;
		posterName=_posterName;
		posterCollege=_posterCollege;
		
		bitmap=null;
		
		if (!"".equals(thumbImgUrl)) {
			String hash=Util.getHash(thumbImgUrl);
			File file=MyFile.getCache(context, hash);
			if (!file.exists())
				download(handler, thumbImgUrl, file);
		}
	}
	
	public Bitmap getBitmap() {
		if ("".equals(thumbImgUrl)) return null;
		if (bitmap!=null) return bitmap;
		String hash=Util.getHash(thumbImgUrl);
		File file=MyFile.getCache(context, hash);
		if (!file.exists()) return null;
		try {
			//bitmap=BitmapFactory.decodeFile(file.getAbsolutePath());
			bitmap=MyBitmapFactory.getCompressedBitmap(file.getAbsolutePath(), 1);
			return bitmap;
		}
		catch (Exception e) {
			bitmap=null;
			return null;
		}
	}
	
	public static void download(final Handler handler, final String url, final File file) {
		new Thread(new Runnable() {
			public void run() {
				if (MyFile.urlToFile(url, file)) {
					if (handler!=null)
						handler.sendEmptyMessage(Constants.MESSAGE_LOSTFOUND_IMAGE_REQUEST_FINISHED);
				}
				else {
					if (handler!=null)
						handler.sendEmptyMessage(Constants.MESSAGE_LOSTFOUND_IMAGE_REQUEST_FAILED);
				}
			}
		}).start();
	}
	
}
