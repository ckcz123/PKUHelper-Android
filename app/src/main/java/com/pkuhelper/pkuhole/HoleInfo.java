package com.pkuhelper.pkuhole;

import java.io.File;
import java.util.HashMap;

import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.MyBitmapFactory;
import com.pkuhelper.lib.MyFile;
import com.pkuhelper.lib.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;

public class HoleInfo {
	public static final int TYPE_UNKNOWN=-1;
	public static final int TYPE_TEXT=0;
	public static final int TYPE_IMAGE=1;
	public static final int TYPE_AUDIO=2;
	
	int pid;
	String text;
	long timestamp;
	int type;
	int reply;
	int like;
	int extra;
	String url;
	Bitmap bitmap;
	File audio;
	
	Context context;
	
	@SuppressLint("UseSparseArrays")
	static HashMap<Integer, HoleInfo> holeInfos=new HashMap<Integer, HoleInfo>();
	
	public HoleInfo(Context context, Handler handler, 
			int _pid, String _text, long _timestamp, String _type,
			int _reply, int _like, int _extra, String _url) {
		this.context=context;
		pid=_pid;
		text=new String(_text);
		timestamp=_timestamp*1000;
		if ("image".equals(_type)) type=TYPE_IMAGE;
		else if ("audio".equals(_type)) type=TYPE_AUDIO;
		else if ("text".equals(_type)) type=TYPE_TEXT;
		else {
			type=TYPE_TEXT;
			text="（这是一条不支持的消息，请更新到最新版PKU Helper进行查看。）";
		}
		
		reply=_reply;
		like=_like;
		extra=_extra;
		url=new String(_url);
		bitmap=null;
		
		if ((type==TYPE_IMAGE || type==TYPE_AUDIO) && !"".equals(url)) {
			String hash=Util.getHash(url);
			File file=MyFile.getCache(context, hash);
			if (!file.exists()) {
				String realurl="";
				if (type==TYPE_IMAGE) 
					realurl=Constants.domain+"/services/pkuhole/images/"+url;
				else
					realurl=Constants.domain+"/services/pkuhole/audios/"+url;
				if (!"".equals(realurl))
					download(handler, realurl, file);
			}			
		}
		if (!holeInfos.containsKey(pid))
			holeInfos.put(pid, this);
	}
	
	public Bitmap getBitmap() {
		if (type!=TYPE_IMAGE) return null;
		if (bitmap!=null) return bitmap;
		try {
			String hash=Util.getHash(url);
			bitmap=MyBitmapFactory.getCompressedBitmap(MyFile.getCache(context, hash).getAbsolutePath(), 2);
			return bitmap;
		}
		catch (Exception e) {}
		return null;
	}
	
	public File getAudio() {
		if (type!=TYPE_AUDIO) return null;
		if (audio!=null) return audio;
		String hash=Util.getHash(url);
		audio=MyFile.getCache(context, hash);
		if (!audio.exists()) {
			audio=null;
			return null;
		}
		return audio;
	}
	
	public static void download(final Handler handler, final String url, 
			final File file) {
		new Thread(new Runnable() {
			public void run() {
				/*
				File tmpFile=new File(file+"temp");
				try {
					InputStream inputStream=WebConnection.connect(url);
					FileOutputStream fileOutputStream=new FileOutputStream(tmpFile);
					byte[] bts=new byte[40960];
					
					int nbytes=0;
					while (true) {
						nbytes=inputStream.read(bts);
						if (nbytes==-1) break;
						fileOutputStream.write(bts, 0, nbytes);
					}
					fileOutputStream.close();
					file.delete();
					tmpFile.renameTo(file);
					if (handler!=null)
						handler.sendEmptyMessage(Constants.MESSAGE_HOLE_FILE_DOWNLOAD_FINISHED);
				}
				catch (Exception e) {
					e.printStackTrace();
					tmpFile.delete();
					file.delete();
					if (handler!=null)
						handler.sendEmptyMessage(Constants.MESSAGE_HOLE_FILE_DOWNLOAD_FAILED);
				}
				*/
				if (MyFile.urlToFile(url, file)) {
					if (handler!=null)
						handler.sendEmptyMessage(Constants.MESSAGE_HOLE_FILE_DOWNLOAD_FINISHED);
				}
				else {
					if (handler!=null)
						handler.sendEmptyMessage(Constants.MESSAGE_HOLE_FILE_DOWNLOAD_FAILED);
				}
			}
		}).start();
	}
	
	public static HoleInfo getHoleInfo(int pid) {
		return holeInfos.get(pid);
	}
	
}
