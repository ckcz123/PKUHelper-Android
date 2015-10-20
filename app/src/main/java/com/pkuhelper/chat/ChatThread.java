package com.pkuhelper.chat;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.lib.webconnection.WebConnection;

import java.util.ArrayList;

public class ChatThread extends Thread implements Runnable {
	private String uid = "";
	private boolean stopped = false;
	private boolean realstopped = false;

	public ChatThread(String _uid) {
		uid = new String(_uid);
		realstopped = false;
	}

	public void setStop() {
		stopped = true;
	}

	public boolean getrealstopped() {
		return realstopped;
	}

	@Override
	public void start() {
		//stopped=false;
		//super.start();
		stopped = false;
		if (isAlive()) {
			return;
		}
		try {
			super.start();
		} catch (Exception e) {
		}
	}

	@Override
	public void run() {
		while (!stopped) {
			ChatActivity chatActivity = ChatActivity.chatActivity;
			String requestTo = new String(ChatActivity.toUid);
			try {
				Parameters parameters = WebConnection.connect(
						Constants.domain + "/services/hasnew.php?uid=" + uid + "&to=" + requestTo, null);
				boolean listnew = false, detailnew = false;
				if ("200".equals(parameters.name)) {
					String string = parameters.value.trim();
					Log.w("chat-return", string);

					listnew = string.charAt(0) == '1';
					detailnew = string.length() != 1 && string.charAt(1) == '1';
				}
				if (listnew) {
					ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
					//arrayList.add(new Parameters("uid", Constants.username));
					//String timestamp=System.currentTimeMillis()/1000+"";
					//String hash=Util.getHash(Constants.username+timestamp+"I2V587");
					//arrayList.add(new Parameters("timestamp", timestamp));
					//arrayList.add(new Parameters("hash", hash));
					arrayList.add(new Parameters("type", "getlist"));
					arrayList.add(new Parameters("token", Constants.token));
					Parameters ans = WebConnection.connect(Constants.domain + "/services/msg.php", arrayList);
					if ("200".equals(ans.name)) {
						Handler handler = chatActivity.handler;
						handler.sendMessage(Message.obtain(handler,
								Constants.MESSAGE_CHAT_REFRESH_LIST, ans.value));
					}
				}
				if (detailnew) {
					// refresh directly
					if (chatActivity.pageShowing == ChatActivity.PAGE_CHAT) {
						ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
						//arrayList.add(new Parameters("uid", uid));
						arrayList.add(new Parameters("to", requestTo));
						//String timestamp=System.currentTimeMillis()/1000+"";
						//String hash=Util.getHash(Constants.username+timestamp+"I2V587");
						//arrayList.add(new Parameters("timestamp", timestamp));
						//arrayList.add(new Parameters("hash", hash));
						arrayList.add(new Parameters("token", Constants.token));
						arrayList.add(new Parameters("type", "getdetail"));
						arrayList.add(new Parameters("newonly", "1"));
						Parameters ans = WebConnection.connect(Constants.domain + "/services/msg.php", arrayList);
						if ("200".equals(ans.name)
								&& chatActivity.pageShowing == ChatActivity.PAGE_CHAT
								&& requestTo.equals(ChatActivity.toUid)) {
							Handler handler = chatActivity.handler;
							handler.sendMessage(Message.obtain(handler,
									Constants.MESSAGE_CHAT_REFRESH_DETAIL, ans.value));
						}
					}
				}
			} catch (Exception e) {
			} finally {
				try {
					sleep(4000);
				} catch (Exception ee) {
				}
			}
		}
		realstopped = true;
	}
}
