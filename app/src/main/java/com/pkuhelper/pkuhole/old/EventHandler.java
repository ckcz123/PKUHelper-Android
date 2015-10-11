package com.pkuhelper.pkuhole.old;

import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.view.CustomToast;

import android.os.*;

public class EventHandler extends Handler{
	public EventHandler() {
		super();
	}
	public EventHandler(Looper looper) {
		super(looper);
	}
	@Override
	public void handleMessage(Message message) {
		super.handleMessage(message);
		String string="";
		switch (message.what) {
			case Constants.MESSAGE_PKUHOLE_LIST_MORE_FAILED:
				string=(String)message.obj;
				if ("-1".equals(string))
					CustomToast.showInfoToast(PKUHoleActivity.pkuHoleActivity, "无法连接网络(-1,-1)");
				else 
					CustomToast.showInfoToast(PKUHoleActivity.pkuHoleActivity, "无法连接到服务器 (HTTP "+string+")");
				break;
			case Constants.MESSAGE_PKUHOLE_LIST_MORE_FINISHED:
				string=(String)message.obj;
				ShowLists.finishRequest(string);
				break;
			case Constants.MESSAGE_PKUHOLE_IMAGE_REQUEST:
				ShowComments.imageRequestFinished(message.arg1, message.obj);
				break;
			case Constants.REQUEST_PKUHOLE_GET_DETAIL_FAILED:
			case Constants.REQUEST_PKUHOLE_GET_DETAIL_FINISHED:
				PKUHoleActivity.pkuHoleActivity.finishRequest(message.what, (String)message.obj);
				break;
		}
	}
}
