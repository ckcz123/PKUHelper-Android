package com.pkuhelper.noticecenter;

import java.io.File;
import java.util.HashMap;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.Editor;
import com.pkuhelper.lib.MyBitmapFactory;
import com.pkuhelper.lib.MyFile;
import com.pkuhelper.lib.Util;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class Notice {

	public int sid;
	public String name;
	public String iconURL;
	public Drawable icon;
	public String desc;
	public boolean isDefault;
	public boolean isSelected;
	public String wantsToSelect="0";
	public static boolean hasSelected=false;
	public static Notice courseNotice=null;
	public static HashMap<String, Drawable> drawableMap=null;
	
	/**
	 * Should only used for construct course notice
	 */
	public Notice() {
		sid=0;
		name="教学网";
		iconURL="";
		desc="收集来自教学网的最新通知";
		isDefault=true;
		isSelected=Editor.getBoolean(NCActivity.ncActivity, Constants.username+"_nc_course");
		if (isSelected) wantsToSelect="1";
		else wantsToSelect="0";
		setIcon(NCActivity.ncActivity.getResources().getDrawable(R.drawable.icon_course));
	}
	
	public Notice(String _sid, String _name, String _iconURL, String _desc,
			String _isDefault, String _isSelected) {
		sid=Integer.parseInt(_sid);
		name=_name;
		iconURL=_iconURL;
		icon=null;
		desc=_desc;
		isDefault=false;
		if ("1".equals(_isDefault)) isDefault=true;
		isSelected=false;
		if ("1".equals(_isSelected)) isSelected=true;
		if (isSelected)
			wantsToSelect="1";
		if (drawableMap.containsKey(sid+"")) {
			icon=drawableMap.get(sid+"");
			return;
		}
		
		icon=null;
		final File file=MyFile.getCache(NCActivity.ncActivity, Util.getHash(iconURL));
		if (file.exists()) {
			new Thread(new Runnable() {
				public void run() {
					try {
						Bitmap bitmap=MyBitmapFactory.getCompressedBitmap(file.getAbsolutePath(), 1);
						icon=new BitmapDrawable(NCActivity.ncActivity.getResources(), bitmap);
						drawableMap.put(sid+"", icon);
					}
					catch (Exception e) {file.delete();icon=null;}
				}
			}).start();	
		}
		else {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						File file=MyFile.getCache(NCActivity.ncActivity, Util.getHash(iconURL));
						if (MyFile.urlToFile(iconURL, file)) {
							Bitmap bitmap=MyBitmapFactory.getCompressedBitmap(file.getAbsolutePath(), 1);
							icon=new BitmapDrawable(NCActivity.ncActivity.getResources(), bitmap);
							drawableMap.put(sid+"", icon);
						}
					}	
					catch (Exception e) {
						icon=null;
						drawableMap.put(sid+"", icon);
					}
				}
			}).start();
		}
	}
	
	public void setIcon(Drawable _drawable) {
		icon=_drawable;		
		drawableMap.put(sid+"", _drawable);
	}
	
	public void setSelectedFromWanted() {
		isSelected="1".equals(wantsToSelect);
		Editor.putBoolean(NCActivity.ncActivity, Constants.username+"_nc_course", isSelected);
		
	}
}
