package com.pkuhelper.widget;

import org.json.JSONArray;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

import com.pkuhelper.PKUHelper;
import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.Editor;
import com.pkuhelper.lib.MyFile;

public class WidgetExamProvider extends AppWidgetProvider{

	private Context context;
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		this.context=context;
		for (int i=0;i<appWidgetIds.length;i++) {
			int appId=appWidgetIds[i];
			appWidgetManager.updateAppWidget(appWidgetIds[i],
					getRemoteViews(context, null, appId));			
		}
	}
	protected RemoteViews getRemoteViews(Context context, String text, int appId) {
		RemoteViews remoteViews=new RemoteViews(context.getPackageName(), R.layout.widget_exam_layout);
		
		Intent intent=new Intent(context, WidgetExamService.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appId);
		intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
		remoteViews.setRemoteAdapter(R.id.widget_exam_listview, intent);
		
		remoteViews.setOnClickPendingIntent(R.id.widget_exam_header, 
				getPendingSelfIntent(context, Constants.ACTION_SET_EXAM, appId));
		
		boolean hasExam=hasExam();
		if (!hasExam) {
			remoteViews.setInt(R.id.widget_exam_hint, "setVisibility", View.VISIBLE);
			remoteViews.setOnClickPendingIntent(R.id.widget_exam_hint, getPendingSelfIntent(context, Constants.ACTION_SET_EXAM, appId));
		}
		else
			remoteViews.setInt(R.id.widget_exam_hint, "setVisibility", View.GONE);
		
		return remoteViews;
	}
	
	protected PendingIntent getPendingSelfIntent(Context context, String action, int id) {
	    Intent intent = new Intent(context, getClass());
	    intent.setAction(action);
	    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
	    return PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	private boolean hasExam() {
		try {
			String username=Editor.getString(context,"username");
			if ("".equals(username)) return false;
			String examString=MyFile.getString(context, username, "exam", "[]");
			JSONArray jsonArray=new JSONArray(examString);
			return jsonArray.length()!=0;
		}
		catch (Exception e) {return false;}
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		String action=intent.getAction();
		if (Constants.ACTION_SET_EXAM.equals(action)) {
			Intent intent2=new Intent(context, PKUHelper.class);
			intent2.putExtra("type", "exam");
			intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			context.startActivity(intent2);
		}
		if (Constants.ACTION_REFRESH_EXAM.equals(action)) {
			AppWidgetManager appWidgetManager=AppWidgetManager.getInstance(context);
			ComponentName thisAppWidget = new ComponentName(context, WidgetExamProvider.class.getName());
		    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
		    onUpdate(context, appWidgetManager, appWidgetIds);
		    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_exam_listview);
		}
	}
	
}
