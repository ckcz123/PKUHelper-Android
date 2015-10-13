package com.pkuhelper.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.Editor;
import com.pkuhelper.lib.Lib;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.lib.webconnection.WebConnection;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetItsProvider extends AppWidgetProvider{
	
	private Handler handler=new Handler(new Handler.Callback() {
		
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
				CustomToast.showErrorToast(context, hint);
				setText(context, hint, appId);
				break;
			case Constants.MESSAGE_WIDGET_FINISHED:
				finishRequest(msg.arg1, string, context, appId);
			default:
				break;
			}
			
			return true;
		}
	});
	
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		for (int i=0;i<appWidgetIds.length;i++) {
			int appId=appWidgetIds[i];
			appWidgetManager.updateAppWidget(appWidgetIds[i],
					getRemoteViews(context, null, appId));			
		}
	}
	protected RemoteViews getRemoteViews(Context context, String text, int appId) {
		RemoteViews remoteViews=new RemoteViews(context.getPackageName(), R.layout.widget_its_layout);

		// 设置各个按钮
		remoteViews.setOnClickPendingIntent(R.id.widget_connect, 
				getPendingSelfIntent(context, Constants.ACTION_CONNECT, appId));
		remoteViews.setOnClickPendingIntent(R.id.widget_connect_nofree, 
				getPendingSelfIntent(context, Constants.ACTION_CONNECT_NO_FREE, appId));
		remoteViews.setOnClickPendingIntent(R.id.widget_disconnect, 
				getPendingSelfIntent(context, Constants.ACTION_DISCONNECT, appId));
		remoteViews.setOnClickPendingIntent(R.id.widget_disconnectall, 
				getPendingSelfIntent(context, Constants.ACTION_DISCONNECT_ALL, appId));
		
		// 设置文本
		if (text!=null && !"".equals(text)) {
			remoteViews.setTextViewText(R.id.widget_text, text);
		}
		else remoteViews.setTextViewText(R.id.widget_text, "点击按钮立刻连接网关！");
		
		return remoteViews;
		
	}
	protected PendingIntent getPendingSelfIntent(Context context, String action, int id) {
	    Intent intent = new Intent(context, getClass());
	    intent.setAction(action);
	    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
	    return PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		String action=intent.getAction();
		int appId=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 
				AppWidgetManager.INVALID_APPWIDGET_ID);
		if (Constants.ACTION_CONNECT.equals(action)) {
			doConnection(Constants.REQUEST_ITS_CONNECT, "正在连接免费地址...", context, appId);
			return;
		}
		else if (Constants.ACTION_CONNECT_NO_FREE.equals(action)) {
			doConnection(Constants.REQUEST_ITS_CONNECT_NO_FREE, "正在连接收费地址...", context, appId);
			return;
		}
		else if (Constants.ACTION_DISCONNECT.equals(action)) {
			doConnection(Constants.REQUEST_ITS_DISCONNECT,"正在断开连接", context, appId);
			return;
		}
		else if (Constants.ACTION_DISCONNECT_ALL.equals(action)) {
			doConnection(Constants.REQUEST_ITS_DISCONNECT_ALL,"正在断开全部连接", context, appId);
			return;
		}
		else if (Constants.ACTION_CONNECT_STATUS_SET.equals(action)) {
			boolean connected=intent.getBooleanExtra("connect", false);
			boolean inschool=intent.getBooleanExtra("inschool", false);
			boolean nofree=intent.getBooleanExtra("nofree", false);
			
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
			
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		    ComponentName thisAppWidget = new ComponentName(context, WidgetItsProvider.class.getName());
		    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
		    for (int id:appWidgetIds)
		    	setText(context, string, id);
		    
		}
		
	}
	
	private void setText(Context context, String text, int appId) {
		if (appId==AppWidgetManager.INVALID_APPWIDGET_ID) return;
		if (text==null || "".equals(text))
			text="点击按钮立刻连接网关！";
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
	    appWidgetManager.updateAppWidget(appId, getRemoteViews(context, text, appId));
	}
	
	private void doConnection(final int constantType, String hintString, final Context context
			, final int appId) {
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
			CustomToast.showErrorToast(context, "你还没有登录！");
			return;
		}
		final ArrayList<Parameters> arrayList=new ArrayList<Parameters>();
		arrayList.add(new Parameters("uid", username));
		arrayList.add(new Parameters("password", password));
		arrayList.add(new Parameters("operation", type));
		arrayList.add(new Parameters("range", free));
		arrayList.add(new Parameters("timeout", "-1"));
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Parameters parameters=WebConnection.connect("https://its.pku.edu.cn:5428/ipgatewayofpku", arrayList);
				if (!"200".equals(parameters.name)) {
					handler.sendMessage(Message.obtain(handler, 
							Constants.MESSAGE_WIDGET_FAILED, constantType, 
							appId, new MessageObject(context, parameters.name)));
				}
				else {
					handler.sendMessage(Message.obtain(handler, 
							Constants.MESSAGE_WIDGET_FINISHED, constantType, 
							appId, new MessageObject(context, parameters.value)));
				}
			}
		}).start();
		setText(context, hintString, appId);
	}
	
	private void finishRequest(int type, String msg, Context context, int appId) {
		Map<String, String> map=getReturnMsg(msg);
		if (!map.containsKey("SUCCESS")) {
			CustomToast.showErrorToast(context, "网络连接失败，请重试");
			setText(context, "网络连接失败，请重试", appId);
			return;
		}
		String successmsg=map.get("SUCCESS");
		boolean success="YES".equals(successmsg);
		
		if (type==Constants.REQUEST_ITS_CONNECT
				|| type==Constants.REQUEST_ITS_CONNECT_NO_FREE) {
			if (success) {
				String string="连接成功！\n当前连接数："+map.get("CONNECTIONS")+"\n已用总时长："+map.get("FR_TIME");
				CustomToast.showSuccessToast(context, string);
				String hint="连接状态：已连接到";
				if (type==Constants.REQUEST_ITS_CONNECT)
					hint+="免费地址";
				else hint+="收费地址";
				setText(context, hint, appId);
			}
			else {
				CustomToast.showErrorToast(context, map.get("REASON"));
				setText(context, "连接失败", appId);
			}
			Lib.checkConnectedStatus(context);
			return;
		}
		if (type==Constants.REQUEST_ITS_DISCONNECT) {
			if (success) {
				CustomToast.showSuccessToast(context, "连接断开成功");
				setText(context, "连接断开成功", appId);
			}
			else {
				CustomToast.showErrorToast(context, map.get("REASON"));
				setText(context, "连接断开失败", appId);
			}
			Lib.checkConnectedStatus(context);
			return;
		}
		if (type==Constants.REQUEST_ITS_DISCONNECT_ALL) {
			if (success) {
				CustomToast.showSuccessToast(context, "全部连接断开成功");
				setText(context, "全部连接断开成功", appId);
			}
			else {
				CustomToast.showErrorToast(context, map.get("REASON"));
				setText(context, "全部连接断开失败", appId);
			}
			Lib.checkConnectedStatus(context);
			return;
		}
	}
	
	private static Map<String, String> getReturnMsg(String string) {
		Map<String, String> map=new HashMap<String, String>();
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



