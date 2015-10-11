package com.pkuhelper.noticecenter;

import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.view.CustomToast;

import android.graphics.drawable.Drawable;
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
			case Constants.MESSAGE_NOTICECENTER_IMAGE_REQUEST:
				Notice notice=NCActivity.ncActivity.sourceListMap.get(message.arg1+"");
				if (notice!=null) notice.setIcon((Drawable)message.obj);
				break;
			case Constants.MESSAGE_NOTICECENTER_LIST_MORE_FAILED:
				string=(String)message.obj;
				if ("-1".equals(string))
					CustomToast.showInfoToast(NCActivity.ncActivity, "无法连接网络(-1,-1)");
				else 
					CustomToast.showInfoToast(NCActivity.ncActivity, "无法连接到服务器 (HTTP "+string+")");
				break;
			case Constants.MESSAGE_NOTICECENTER_LIST_MORE_FINISHED:
				NCContent.finishMoreRequest((String)message.obj);
				break;
			case Constants.MESSAGE_NOTICECENTER_ONE_MORE_FAILED:
				string=(String)message.obj;
				if ("-1".equals(string))
					CustomToast.showInfoToast(NCActivity.ncActivity, "无法连接网络(-1,-1)");
				else 
					CustomToast.showInfoToast(NCActivity.ncActivity, "无法连接到服务器 (HTTP "+string+")");
				break;
			case Constants.MESSAGE_NOTICECENTER_ONE_MORE_FINISHED:
				NCContent.finishMoreRequest(NCActivity.ncActivity.lastRequestSid, (String)message.obj);
				break;
		}
	}
}