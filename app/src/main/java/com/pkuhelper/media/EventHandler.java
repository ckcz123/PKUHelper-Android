package com.pkuhelper.media;

import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.view.CustomToast;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class EventHandler extends Handler {
	public EventHandler() {
		super();
	}
	public EventHandler(Looper looper) {
		super(looper);
	}
	@Override
	public void handleMessage(Message message) {
		super.handleMessage(message);
		String string;
		switch (message.what) {
			case Constants.MESSAGE_MEDIA_LIST_MORE_FAILED:
				string=(String)message.obj;
				if ("-1".equals(string))
					CustomToast.showInfoToast(MediaActivity.mediaActivity, "无法连接网络(-1,-1)");
				else 
					CustomToast.showInfoToast(MediaActivity.mediaActivity, "无法连接到服务器 (HTTP "+string+")");
				break;
			case Constants.MESSAGE_MEDIA_LIST_MORE_FINISHED:
				MediaList.finishMoreRequest((String)message.obj);
				break;
		}
	}
}