package com.pkuhelper.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.pkuhelper.R;
import com.pkuhelper.lib.Editor;
import com.pkuhelper.lib.MyCalendar;
import com.pkuhelper.lib.MyFile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class WidgetExamFactory implements RemoteViewsService.RemoteViewsFactory {
	private Context context;
	ArrayList<ExamInfo> arrayList = new ArrayList<ExamInfo>();

	public WidgetExamFactory(Context _context, Intent intent) {
		context = _context;
		refresh();
	}

	@Override
	public void onCreate() {
	}

	@Override
	public void onDataSetChanged() {
		refresh();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return arrayList.size();
	}

	@Override
	public RemoteViews getViewAt(int position) {
		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_exam_item);

		ExamInfo examInfo = arrayList.get(position);
		rv.setTextViewText(R.id.widget_exam_item_name, examInfo.name);
		String location = examInfo.date.substring(5) + " " + examInfo.time;
		if (!"".equals(examInfo.location)) location += " @" + examInfo.location;
		rv.setTextViewText(R.id.widget_exam_item_location, location);

		rv.setTextViewText(R.id.widget_exam_item_time, examInfo.daysLeft);

		if (examInfo.finished) {
			rv.setInt(R.id.widget_exam_item_time_layout, "setBackgroundColor",
					Color.parseColor("#60cdc9c9"));
		} else {
			rv.setInt(R.id.widget_exam_item_time_layout, "setBackgroundColor",
					Color.parseColor("#60ee2c2c"));
		}

		return rv;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return true;
	}

	private void refresh() {
		arrayList = new ArrayList<ExamInfo>();
		try {
			String username = Editor.getString(context, "username");
			if ("".equals(username)) throw new Exception();
			String string = MyFile.getString(context, username, "exam", null);
			if (string == null || "".equals(string)) throw new Exception();

			JSONArray jsonArray = new JSONArray(string);
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String name = jsonObject.optString("name");
				String location = jsonObject.optString("location");
				String time = jsonObject.optString("time");
				String date = jsonObject.optString("date");
				arrayList.add(new ExamInfo(name, location, date, time));
			}
		} catch (Exception e) {
			arrayList = new ArrayList<ExamInfo>();
		}
		Collections.sort(arrayList, new Comparator<ExamInfo>() {
			@Override
			public int compare(ExamInfo lhs, ExamInfo rhs) {
				if (lhs.finished && !rhs.finished) return 1;
				if (!lhs.finished && rhs.finished) return -1;
				int dateCmp = lhs.date.compareTo(rhs.date);
				if (dateCmp != 0) return dateCmp;
				return lhs.time.compareTo(rhs.time);
			}
		});
	}

	@Override
	public RemoteViews getLoadingView() {
		// TODO Auto-generated method stub
		return new RemoteViews(context.getPackageName(), R.layout.widget_course_item_textview);
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
}

class ExamInfo {
	String name;
	String location;
	String date;
	String time;
	boolean finished;
	String daysLeft;

	public ExamInfo(String _name, String _location, String _date, String _time) {
		setInfo(_name, _location, _date, _time);
	}

	public void setInfo(String _name, String _location, String _date, String _time) {
		name = _name;
		location = _location;
		date = _date;
		time = _time;
		finished = checkfinished();
		daysLeft = getDeltaDays();
	}

	private boolean checkfinished() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
		String nowDate = dateFormat.format(calendar.getTime());
		String nowTime = timeFormat.format(calendar.getTime());

		int dateCompare = date.compareTo(nowDate);
		int timeCompare = time.compareTo(nowTime);

		if (dateCompare > 0) return false;
		if (dateCompare == 0 && timeCompare > 0) return false;
		return true;
	}

	private String getDeltaDays() {
		if (finished) return "已结束";
		return MyCalendar.getDaysLeft(date) + "";
	}
}
