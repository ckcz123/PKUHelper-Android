package com.pkuhelper.widget;

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
import com.pkuhelper.lib.MyCalendar;
import com.pkuhelper.lib.MyFile;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Calendar;

public class WidgetCourse2Provider extends AppWidgetProvider {

	private Context context;

	public static int page = 0;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		this.context = context;
		for (int i = 0; i < appWidgetIds.length; i++) {
			int appId = appWidgetIds[i];
			appWidgetManager.updateAppWidget(appWidgetIds[i],
					getRemoteViews(context, null, appId, page));
		}
	}

	protected RemoteViews getRemoteViews(Context context, String text, int appId, int page) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_course2_layout);
		if (page <= 0) page = 0;
		if (page >= 6) page = 6;

		int week = Editor.getInt(context, "week");
		String dayofweek = MyCalendar.getWeekDayName(Calendar.getInstance(), page);

		if (week <= 0 || week >= 21) week = 0;

		String string;
		if (week == 0)
			string = "当前为放假期间";
		else {
			week += MyCalendar.getWeekPassed(Calendar.getInstance(), page);
			string = "第" + week + "周     " + dayofweek;
		}
		remoteViews.setTextViewText(R.id.widget_course2_textview, string);

		Intent intent = new Intent(context, WidgetCourse2Service.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appId);
		intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
		remoteViews.setRemoteAdapter(R.id.widget_course2_listview, intent);

		if (page != 0) {
			remoteViews.setInt(R.id.widget_course2_previous, "setVisibility", View.VISIBLE);
			remoteViews.setOnClickPendingIntent(R.id.widget_course2_previous,
					getPendingSelfIntent(context, Constants.ACTION_PAGE_PREVIOUS, appId));
		} else
			remoteViews.setInt(R.id.widget_course2_previous, "setVisibility", View.GONE);

		if (page != 6) {
			remoteViews.setInt(R.id.widget_course2_next, "setVisibility", View.VISIBLE);
			remoteViews.setOnClickPendingIntent(R.id.widget_course2_next,
					getPendingSelfIntent(context, Constants.ACTION_PAGE_NEXT, appId));
		} else
			remoteViews.setInt(R.id.widget_course2_next, "setVisibility", View.GONE);

		remoteViews.setOnClickPendingIntent(R.id.widget_course2_textview,
				getPendingSelfIntent(context, Constants.ACTION_VIEW_COURSE, appId));


		boolean hasCourse = hasCourseToday(page);
		if (!hasCourse)
			remoteViews.setInt(R.id.widget_course2_hint, "setVisibility", View.VISIBLE);
		else
			remoteViews.setInt(R.id.widget_course2_hint, "setVisibility", View.GONE);

		return remoteViews;
	}

	protected PendingIntent getPendingSelfIntent(Context context, String action, int id) {
		Intent intent = new Intent(context, getClass());
		intent.setAction(action);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
		return PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	private boolean hasCourseToday(int page) {
		try {
			String username = Editor.getString(context, "username");
			if ("".equals(username)) throw new Exception();
			String string = MyFile.getString(context, username, "course", null);
			if (string == null || "".equals(string)) throw new Exception();
			Document document = Jsoup.parse(string);
			Element table = document.getElementById("classAssignment");
			Elements trs = table.getElementsByTag("tr");
			int week = Editor.getInt(context, "week");
			if (week <= 0 || week >= 21) week = 0;
			if (week == 0) return false;

			week += MyCalendar.getWeekPassed(Calendar.getInstance(), page);
			int today = MyCalendar.getWeekDayInNumber(Calendar.getInstance(), page);

			for (int i = 1; i <= 12; i++) {
				Element td = trs.get(i).getElementsByTag("td").get(today);
				if (td.hasAttr("style")
						&& !(week % 2 == 0 && td.text().contains("单周"))
						&& !(week % 2 != 0 && td.text().contains("双周"))) {
					return true;
				}
			}
			return false;

		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		String action = intent.getAction();
		if (Constants.ACTION_VIEW_COURSE.equals(action)) {
			Intent intent2 = new Intent(context, PKUHelper.class);
			intent2.putExtra("type", "course");
			intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			context.startActivity(intent2);
		}
		if (Constants.ACTION_REFRESH_COURSE.equals(action)) {
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			ComponentName thisAppWidget = new ComponentName(context, WidgetCourse2Provider.class.getName());
			int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
			page = 0;
			onUpdate(context, appWidgetManager, appWidgetIds);
			appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_course2_listview);
		}
		if (Constants.ACTION_PAGE_NEXT.equals(action)) {
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			ComponentName thisAppWidget = new ComponentName(context, WidgetCourse2Provider.class.getName());
			int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
			page++;
			if (page > 6) page = 6;
			onUpdate(context, appWidgetManager, appWidgetIds);
			appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_course2_listview);
		}
		if (Constants.ACTION_PAGE_PREVIOUS.equals(action)) {
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
			ComponentName thisAppWidget = new ComponentName(context, WidgetCourse2Provider.class.getName());
			int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
			page--;
			if (page < 0) page = 0;
			onUpdate(context, appWidgetManager, appWidgetIds);
			appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_course2_listview);
		}
	}

}
