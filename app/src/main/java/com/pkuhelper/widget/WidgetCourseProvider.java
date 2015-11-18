package com.pkuhelper.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.pkuhelper.PKUHelper;
import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.Editor;

public class WidgetCourseProvider extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		for (int i = 0; i < appWidgetIds.length; i++) {
			int appId = appWidgetIds[i];
			appWidgetManager.updateAppWidget(appWidgetIds[i],
					getRemoteViews(context, null, appId));
		}
	}

	protected RemoteViews getRemoteViews(Context context, String text, int appId) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_course_layout);

		int week = Editor.getInt(context, "week");
		if (week <= 0 || week >= 21) week = 0;
		String string;
		if (week == 0) string = "当前为放假期间";
		else string = "第" + week + "周课表";
		//if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY) week++;

		remoteViews.setTextViewText(R.id.widget_course_textview, string);

		Intent intent = new Intent(context, WidgetCourseService.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appId);
		intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

		remoteViews.setRemoteAdapter(R.id.widget_gridview, intent);

		Intent clickIntent = new Intent(context, WidgetActionService.class);
		PendingIntent clickPI = PendingIntent.getService(context, 0,
				clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setPendingIntentTemplate(R.id.widget_gridview, clickPI);

		remoteViews.setOnClickPendingIntent(R.id.widget_course_textview,
				getPendingSelfIntent(context, Constants.ACTION_VIEW_COURSE, appId));

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
		String action = intent.getAction();
		if (Constants.ACTION_REFRESH_COURSE.equals(action)) {
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			ComponentName thisAppWidget = new ComponentName(context, WidgetCourseProvider.class.getName());
			int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
			onUpdate(context, appWidgetManager, appWidgetIds);
			appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_gridview);
		}
		if (Constants.ACTION_VIEW_COURSE.equals(action)) {
			Intent intent2 = new Intent(context, PKUHelper.class);
			intent2.putExtra("type", "course");
			intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			context.startActivity(intent2);
		}
	}
}
