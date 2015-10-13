package com.pkuhelper.lib;

import java.io.File;
import java.net.URLEncoder;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.util.Log;

import com.pkuhelper.IPGW;
import com.pkuhelper.MYPKU;
import com.pkuhelper.PKUHelper;
import com.pkuhelper.R;
import com.pkuhelper.Settings;
import com.pkuhelper.lib.view.BadgeView;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.lib.webconnection.WebConnection;
import com.pkuhelper.service.PKUHelperService;
import com.pkuhelper.widget.WidgetCourse2Provider;
import com.pkuhelper.widget.WidgetCourseProvider;
import com.pkuhelper.widget.WidgetItsProvider;

public class Lib {
	
	private static EventHandler eventHandler;
	
	public static void checkConnectedStatus(final Context context) {
		checkConnectedStatus(context, true);
	}
	public static void checkConnectedStatus(final Context context, final boolean override) {
		eventHandler=new EventHandler(context);
		new Thread(new Runnable() {
			@Override
			public void run() {
				int status=WebConnection.checkIfInSchool();
				
				if (status==-1) {
					Constants.connected=false;
					Constants.inSchool=false;
					Constants.connectedToNoFree=false;
					eventHandler.sendMessage(Message.obtain(eventHandler, Constants.MESSAGE_CHECK_CONNECTED_FINISHED, 
							override?1:0, 0, context));
					return;
				}
				Constants.connected=true;
				if (status==1) {
					Constants.inSchool=true;
					Constants.connectedToNoFree=WebConnection.checkIfConnectedToNoFree();
				}
				else {
					Constants.inSchool=false;
					Constants.connectedToNoFree=true;
				}				
				eventHandler.sendMessage(Message.obtain(eventHandler, Constants.MESSAGE_CHECK_CONNECTED_FINISHED, 
						override?1:0, 0, context));
			}
		}).start();
	}
 	
	public static void getDrawable(final Context context, int type, String url, int id) {
		eventHandler=new EventHandler(context);
		final int _id=id;
		final int _type=type;
		final String _url=new String(url);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					/*
					InputStream inputStream=WebConnection.connect(_url);
					Drawable drawable=Drawable.createFromStream(inputStream, _id+".png");
					*/
					File file=MyFile.getCache(context, Util.getHash(_url));
					MyFile.urlToFile(_url, file, true);
					Drawable drawable=Drawable.createFromPath(file.getAbsolutePath());
					eventHandler.sendMessage(Message.obtain(
							eventHandler, Constants.MESSAGE_IMAGE_REQUEST_FINISHED, _type, _id, drawable));
				}
				catch (Exception e) {
					eventHandler.sendMessage(Message.obtain(
							eventHandler, Constants.MESSAGE_IMAGE_REQUEST_FAILED, _type, _id));
				}
			}
		}).start();
		
	}
	
	public static void sendStatistics(Context context) {
		eventHandler=new EventHandler(context);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String deviceString= "35" + //we make this look like a valid IMEI 
						Build.BOARD.length()%10+ Build.BRAND.length()%10 + 
						Build.CPU_ABI.length()%10 + Build.DEVICE.length()%10 + 
						Build.DISPLAY.length()%10 + Build.HOST.length()%10 + 
						Build.ID.length()%10 + Build.MANUFACTURER.length()%10 + 
						Build.MODEL.length()%10 + Build.PRODUCT.length()%10 + 
						Build.TAGS.length()%10 + Build.TYPE.length()%10 + 
						Build.USER.length()%10 ; 
				
				String ss=new String(Constants.username);
				if ("".equals(ss) || "guest".equals(ss)) ss="anonymous";
				String device=android.os.Build.MODEL;
//				String name=Constants.name;
//				String major=Constants.major;
//				String sex=Constants.sex;
				try {
					device=URLEncoder.encode(device, "utf-8");
//					name=URLEncoder.encode(name,"utf-8");
//					major=URLEncoder.encode(major, "utf-8");
//					sex=URLEncoder.encode(sex,"utf-8");
				} catch (Exception e) {}
				String url=Constants.domain+"/services/info.php"
						+ "?uid="+ss+"&platform=Android"
//						+ "&name="+name+"&major="+major+"&sex="+sex
						+ "&version="+Constants.version+"&sysver="
						+ android.os.Build.VERSION.RELEASE+"&device="
						+ device+"&device_id="+deviceString;
				Parameters parameters=WebConnection.connect(url, null);
				if (!"200".equals(parameters.name)) return;
				eventHandler.sendMessage(
						Message.obtain(eventHandler, Constants.MESSAGE_STATISTICS,parameters.value));
			}
		}).start();
	}
	
	public static void updateAndCheck(Context context) {
		// 检查时间：有没有过一周？
		checkWeek(context);
		
		// 启动service，检查有没有新课程提醒/通知/...
		Intent service=new Intent(context, PKUHelperService.class);
		context.startService(service);
		
		// 更新桌面小部件course2
		Lib.sendBroadcast(context, WidgetCourse2Provider.class, Constants.ACTION_REFRESH_COURSE);

	}
	
	// 检查周数有没有变化
	private static void checkWeek(Context context) {
		int week=Editor.getInt(context, "week");
		long time=Editor.getLong(context, "time");
		
		// 不存在记录，将其记录进去
		if (time==0 || week<=0 || week>=21) {
			if (week<=0 || week>=21) week=0;
			Editor.putLong(context, "time", System.currentTimeMillis());
			Editor.putInt(context, "week", week);
			return;
		}
		
		Calendar calendar1=Calendar.getInstance();
		calendar1.setTimeInMillis(time);
		int weekDelta=MyCalendar.getDeltaWeeks(calendar1, Calendar.getInstance());
		
		// 变了周数
		if (weekDelta>0) {
			week+=weekDelta;
			if (week>20) week=20;
			Editor.putLong(context, "time", System.currentTimeMillis());
			Editor.putInt(context, "week", week);
			
			Lib.sendBroadcast(context, WidgetCourseProvider.class, Constants.ACTION_REFRESH_COURSE);
			Lib.sendBroadcast(context, WidgetCourse2Provider.class, Constants.ACTION_REFRESH_COURSE);

		}
		
	}
	
	public static void updateConnectStatus(Context context, boolean override) {
		if (override)
			IPGW.setConnectStatus();
		
		Intent intent=new Intent(context, WidgetItsProvider.class);
		intent.setAction(Constants.ACTION_CONNECT_STATUS_SET);
		intent.putExtra("connect", Constants.connected);
		intent.putExtra("inschool", Constants.inSchool);
		intent.putExtra("nofree", Constants.connectedToNoFree);
		context.sendBroadcast(intent);
		
	}
	
	public static void sendBroadcast(Context context, Class<?> cls,
			String action) {
		Intent intent=new Intent(context, cls);
		intent.setAction(action);
		context.sendBroadcast(intent);
	}
	
	public static void setBadgeView() {
		if (Constants.hasUpdate) {
			try {
				BadgeView.show(PKUHelper.pkuhelper, PKUHelper.pkuhelper.findViewById(R.id.img_settings), "new");
				if (Settings.settingView!=null) {
					ViewSetting.setTextView(Settings.settingView, R.id.settings_update, "检查更新        ");
					BadgeView.show(PKUHelper.pkuhelper, Settings.settingView.findViewById(R.id.settings_update), "new");
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			if (Constants.newMsg+Constants.newPass!=0) {
				BadgeView.show(PKUHelper.pkuhelper, PKUHelper.pkuhelper.findViewById(R.id.img_mypku),
						(Constants.newMsg+Constants.newPass)+"");
			}
			else {
				BadgeView.show(PKUHelper.pkuhelper, PKUHelper.pkuhelper.findViewById(R.id.img_mypku), "");
			}
		}
		catch (Exception e) {}
		if (MYPKU.mypkuView!=null) {
			try {
				BadgeView.show(PKUHelper.pkuhelper, MYPKU.mypkuView.findViewWithTag("mypkuitem_message"),
							Constants.newMsg==0?"":Constants.newMsg+"",
							MYPKU.PADDING_PX, 1.4f);
			}
			catch (Exception e) {}
			try {
				BadgeView.show(PKUHelper.pkuhelper, MYPKU.mypkuView.findViewWithTag("mypkuitem_wdpz"),
							Constants.newPass==0?"":Constants.newPass+"", MYPKU.PADDING_PX, 1.4f);
			}
			catch (Exception e) {}
		}
	}
	
	static class EventHandler extends Handler {
		Context context;
		public EventHandler(Context c) {
			super();
			context=c;
		}
		@Override
		public void handleMessage(Message message) {
			super.handleMessage(message);
			switch (message.what) {
			case Constants.MESSAGE_CHECK_CONNECTED_FINISHED:
				Context context=(Context)message.obj;
				updateConnectStatus(context, message.arg1==1);
				break;
			case Constants.MESSAGE_STATISTICS:
				try {
					String val=(String)message.obj;
					Log.w("val", val);
					JSONObject jsonObject=new JSONObject(val);
					JSONObject versionJsonObject=jsonObject.getJSONObject("versions");
					String version=versionJsonObject.getString("Android");
					if (Constants.version.compareTo(version)<0) {
							Constants.hasUpdate=true;
							Constants.updateVersion=version;
							JSONObject versionMsg=jsonObject.optJSONObject("versionmsg");
							if (version!=null) {
								Constants.updateMessage=versionMsg.optString("Android");
							}
					}
					Constants.newMsg=jsonObject.optInt("msg");
					Constants.newPass=jsonObject.optInt("passBadge");
					Constants.week=jsonObject.optInt("week");
					if (Editor.getBoolean(this.context, "autoweek", true)) {
						Editor.putLong(this.context, "time", System.currentTimeMillis());
						Editor.putInt(this.context, "week", Constants.week);
					}
					JSONArray jsonArray=jsonObject.optJSONArray("features");
					if (jsonArray!=null) {
						Constants.features.clear();
						int len=jsonArray.length();
						for (int i=0;i<len;i++) {
							JSONObject feature=jsonArray.optJSONObject(i);
							Constants.features.add(
								new Features(this.context, i, feature.optString("title"), 
									feature.optString("imageurl"), feature.optString("color"),
									feature.optString("darkColor"), feature.optString("url")));
						}
					}
					MYPKU.setOthers(Constants.features);
				}
				catch (Exception e) {
				}
				finally {
					setBadgeView();
				}
				break;
			case Constants.MESSAGE_IMAGE_REQUEST_FINISHED:
				if (message.arg1==Constants.REQUEST_FEATURES_IMAGE)
					Constants.setDrawable(message.arg2, (Drawable)message.obj);
				break;
			case Constants.MESSAGE_IMAGE_REQUEST_FAILED:
				if (message.arg1==Constants.REQUEST_FEATURES_IMAGE) {
					Constants.setDrawable(message.arg2, null);
				}
				break;
			default:
				break;
			}
		}
	}
	
}
