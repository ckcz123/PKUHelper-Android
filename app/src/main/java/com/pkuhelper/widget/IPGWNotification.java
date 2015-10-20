package com.pkuhelper.widget;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.Editor;
import com.pkuhelper.lib.Lib;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.lib.webconnection.WebConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IPGWNotification extends BroadcastReceiver {

	Handler handler=new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			Context context;
			String string;
			int appId=msg.arg2;
			try {
				MessageObject messageObject=(MessageObject)msg.obj;
				context=messageObject.context;
				string=messageObject.string;
				if (context==null || string==null) throw new Exception();
			}
			catch (Exception e) {return true;}
			switch (msg.what) {
				case Constants.MESSAGE_WIDGET_FAILED:
					int code=Integer.parseInt(string);
					String hint="";
					if (code==-1)
						hint="无法连接网络(-1,-1)";
					else
						hint="无法连接到服务器 (HTTP "+code+")";
					update(context, hint);
					break;
				case Constants.MESSAGE_WIDGET_FINISHED:
					finishRequest(msg.arg1, string, context);
				default:
					break;
			}

			return true;
		}
	});

	private static RemoteViews getRemoteViews(Context context, String text) {
		//RemoteViews remoteViews=new RemoteViews(context.getPackageName(), R.layout.ipgw_notification);
		RemoteViews remoteViews=new RemoteViews(context.getPackageName(), R.layout.its_noti_layout);

		remoteViews.setOnClickPendingIntent(R.id.widget_connect, getPendingIntent(context, Constants.ACTION_CONNECT));
		remoteViews.setOnClickPendingIntent(R.id.widget_connect_nofree, getPendingIntent(context, Constants.ACTION_CONNECT_NO_FREE));
		remoteViews.setOnClickPendingIntent(R.id.widget_disconnect, getPendingIntent(context, Constants.ACTION_DISCONNECT));
		remoteViews.setOnClickPendingIntent(R.id.widget_disconnectall, getPendingIntent(context, Constants.ACTION_DISCONNECT_ALL));
		if ("".equals(text)) text=context.getResources().getString(R.string.widget_text);
		remoteViews.setTextViewText(R.id.widget_text, text);

		return remoteViews;
	}

	public static void update(Context context) {
		update(context, context.getResources().getString(R.string.widget_text));
	}

	public static void update(Context context, boolean connected, boolean inschool, boolean nofree) {
		String string="";
		if (!connected) {
			string="连接状态：未连接";
		}
		else if (!inschool) {
			string="你当前不在校内";
		}
		else if (nofree) {
			string="连接状态：已连接到收费地址";
		}
		else string="连接状态：已连接到免费地址";
		update(context, string);
	}

	@SuppressLint("NewApi")
	private static void update(Context context, String hint) {
		int ID_IPGW_NOTIFICATION = 0x100;
		if (android.os.Build.VERSION.SDK_INT<16) return;

		boolean use=Editor.getBoolean(context, "ipgwnoti", true);
		NotificationManager notificationManager=(NotificationManager)
				context.getSystemService(Context.NOTIFICATION_SERVICE);
		if (!use) {
			notificationManager.cancel(ID_IPGW_NOTIFICATION);
			return;
		}

		Notification.Builder builder=new Notification.Builder(context).setAutoCancel(false)
				.setTicker("PKU Helper IPGW 网关控制").setSmallIcon(R.drawable.p_white)
				.setContent(getRemoteViews(context, hint)).setContentIntent(null)
				.setPriority(Notification.PRIORITY_MIN);

		Notification notification=builder.build();
		notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
		notificationManager.notify(ID_IPGW_NOTIFICATION, notification);
	}

	private static PendingIntent getPendingIntent(Context context, String action) {
		Intent intent=new Intent(context, IPGWNotification.class);
		intent.setAction(action);
		return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action=intent.getAction();
		if (Constants.ACTION_CONNECT.equals(action)) {
			doConnection(Constants.REQUEST_ITS_CONNECT, "正在连接免费地址...", context);
			return;
		}
		else if (Constants.ACTION_CONNECT_NO_FREE.equals(action)) {
			doConnection(Constants.REQUEST_ITS_CONNECT_NO_FREE, "正在连接收费地址...", context);
			return;
		}
		else if (Constants.ACTION_DISCONNECT.equals(action)) {
			doConnection(Constants.REQUEST_ITS_DISCONNECT,"正在断开连接", context);
			return;
		}
		else if (Constants.ACTION_DISCONNECT_ALL.equals(action)) {
			doConnection(Constants.REQUEST_ITS_DISCONNECT_ALL,"正在断开全部连接", context);
			return;
		}
	}

	private void doConnection(final int constantType, String hintString, final Context context) {
		String type="connect";
		String free="2";
		if (constantType==Constants.REQUEST_ITS_CONNECT) {
			free="2";
		}
		else if (constantType==Constants.REQUEST_ITS_CONNECT_NO_FREE) {
			free="1";
		}
		else if (constantType==Constants.REQUEST_ITS_DISCONNECT) {
			type="disconnect";
		}
		else if (constantType==Constants.REQUEST_ITS_DISCONNECT_ALL) {
			type="disconnectall";
		}
		else return;

		String username=Editor.getString(context, "username"),
				password=Editor.getString(context, "password");
		if ("".equals(username)) {
			update(context, "你还没有登录！");
			return;
		}
		final ArrayList<Parameters> arrayList=new ArrayList<>();
		arrayList.add(new Parameters("uid", username));
		arrayList.add(new Parameters("password", password));
		arrayList.add(new Parameters("operation", type));
		arrayList.add(new Parameters("range", free));
		arrayList.add(new Parameters("timeout", "-1"));
		new Thread(new Runnable() {

			@Override
			public void run() {
				Parameters parameters= WebConnection.connect("https://its.pku.edu.cn:5428/ipgatewayofpku", arrayList);
				if (!"200".equals(parameters.name)) {
					handler.sendMessage(Message.obtain(handler,
							Constants.MESSAGE_WIDGET_FAILED, constantType,
							0, new MessageObject(context, parameters.name)));
				}
				else {
					handler.sendMessage(Message.obtain(handler,
							Constants.MESSAGE_WIDGET_FINISHED, constantType,
							0, new MessageObject(context, parameters.value)));
				}
			}
		}).start();
		update(context, hintString);
	}

	private void finishRequest(int type, String msg, Context context) {
		Map<String, String> map=getReturnMsg(msg);
		if (!map.containsKey("SUCCESS")) {
			update(context, "网络连接失败，请重试");
			return;
		}
		String successmsg=map.get("SUCCESS");
		boolean success="YES".equals(successmsg);

		if (type==Constants.REQUEST_ITS_CONNECT
				|| type==Constants.REQUEST_ITS_CONNECT_NO_FREE) {
			if (success) {
				String string="连接成功！\n当前连接数："+map.get("CONNECTIONS")+"\n已用总时长："+map.get("FR_TIME");
				String hint="连接状态：已连接到";
				if (type==Constants.REQUEST_ITS_CONNECT)
					hint+="免费地址";
				else hint+="收费地址";
				update(context, hint);
			}
			else {
				update(context, map.get("REASON"));
			}
			Lib.checkConnectedStatus(context);
			return;
		}
		if (type==Constants.REQUEST_ITS_DISCONNECT) {
			if (success) {
				update(context, "连接断开成功");
			}
			else {
				update(context, map.get("REASON"));
			}
			Lib.checkConnectedStatus(context);
			return;
		}
		if (type==Constants.REQUEST_ITS_DISCONNECT_ALL) {
			if (success) {
				update(context, "全部连接断开成功");
			}
			else {
				update(context, map.get("REASON"));
			}
			Lib.checkConnectedStatus(context);
			return;
		}
	}

	private static Map<String, String> getReturnMsg(String string) {
		Map<String, String> map=new HashMap<>();
		int pos1=string.indexOf("SUCCESS=");
		int pos2=string.indexOf("IPGWCLIENT_END-->");

		String msg=string.substring(pos1, pos2-1);
		Log.i("IPGWReturnMsg", msg);

		String[] strings=msg.split(" ");
		for (int i=0;i<strings.length;i++) {
			String str=strings[i];
			str.trim();
			if (!str.contains("=")) continue;
			String[] strings2=str.split("=");
			if (strings2.length!=1)
				map.put(strings2[0], strings2[1]);
			else map.put(strings2[0], "");
		}

		return map;
	}

	private class MessageObject {
		Context context;
		String string;
		public MessageObject(Context _context, String _string) {
			context=_context;string=_string;
		}
	}
}
