package com.pkuhelper.lostfound.old;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.view.CustomToast;

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
			case Constants.MESSAGE_LOSTFOUND_IMAGE_REQUEST_FINISHED:
			case Constants.MESSAGE_LOSTFOUND_IMAGE_REQUEST_FAILED:
				Image.setImage((Image) message.obj);
				break;
			case Constants.MESSAGE_LOSTFOUND_LOST_MORE_FINISHED:
				Lost.finishMoreRequest((String) message.obj);
				break;
			case Constants.MESSAGE_LOSTFOUND_FOUND_MORE_FINISHED:
				Found.finishMoreRequest((String) message.obj);
				break;
			case Constants.MESSAGE_LOSTFOUND_LOST_MORE_FAILED:
			case Constants.MESSAGE_LOSTFOUND_FOUND_MORE_FAILED:
			case Constants.MESSAGE_LOSTFOUND_MY_MORE_FAILED:
				string = (String) message.obj;
				if ("-1".equals(string)) {
					CustomToast.showInfoToast(LostFoundActivity.lostFoundActivity, "无法连接网络(-1,-1)", 1000);
				} else
					CustomToast.showInfoToast(LostFoundActivity.lostFoundActivity, "无法连接到服务器 (HTTP " + string + ")", 1000);
				break;
		}
	}
}