package com.pkuhelper.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.Editor;
import com.pkuhelper.lib.MyFile;
import com.pkuhelper.lib.Util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Scanner;

public class WidgetCourseFactory implements RemoteViewsService.RemoteViewsFactory {
	private Context context;
	HashMap<String, Course> hashMap = new HashMap<String, Course>();
	String[] lists = new String[84];
	int[] types = new int[84];

	public WidgetCourseFactory(Context _context, Intent intent) {
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
		return 104;
	}

	@Override
	public RemoteViews getViewAt(int position) {
		RemoteViews rv;
		if (position == 0) {
			rv = new RemoteViews(context.getPackageName(), R.layout.widget_course_item_image);
			Intent intent = new Intent();
			intent.putExtra("refresh", true);
			rv.setOnClickFillInIntent(R.id.widget_gridview_item, intent);
		} else {
			rv = new RemoteViews(context.getPackageName(), R.layout.widget_course_item_textview);
			int x = position / 8;
			int y = position % 8;
			if (x == 0) {
				if (y != 0) {
					rv.setTextViewText(R.id.widget_gridview_item, getWeekName(y));
				}
			} else if (y == 0) {
				rv.setTextViewText(R.id.widget_gridview_item, x + "");
			} else {
				int realPosition = (x - 1) * 7 + (y - 1);
				String courseName = lists[realPosition];
				if (courseName != null && !"".equals(courseName)) {
					Course course = hashMap.get(lists[realPosition]);
					if (course != null) {
						rv.setInt(R.id.widget_gridview_item, "setBackgroundColor", course.color);
						Intent intent = new Intent();
						intent.putExtra("name", course.name);
						intent.putExtra("location", course.location);
						String type = "每周";
						if (types[realPosition] == Constants.COURSE_TYPE_EVEN) type = "双周";
						else if (types[realPosition] == Constants.COURSE_TYPE_ODD) type = "单周";
						intent.putExtra("type", type);
						rv.setOnClickFillInIntent(R.id.widget_gridview_item, intent);
					} else
						rv.setInt(R.id.widget_gridview_item, "setBackgroundColor",
								Color.parseColor("#80f3f3f3"));
				} else
					rv.setInt(R.id.widget_gridview_item, "setBackgroundColor",
							Color.parseColor("#80f3f3f3"));
				rv.setTextViewText(R.id.widget_gridview_item, "");
			}
		}

		return rv;
	}

	private String getWeekName(int week) {
		switch (week) {
			case 1:
				return "一";
			case 2:
				return "二";
			case 3:
				return "三";
			case 4:
				return "四";
			case 5:
				return "五";
			case 6:
				return "六";
			case 7:
				return "日";
		}
		return "日";
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return true;
	}

	private void refresh() {
		try {
			String username = Editor.getString(context, "username");
			if ("".equals(username)) throw new Exception();
			hashMap = new HashMap<String, Course>();
			lists = new String[12 * 7];
			String string = MyFile.getString(context, username, "course", null);
			if (string == null || "".equals(string)) throw new Exception();
			Document document = Jsoup.parse(string);
			Element table = document.getElementById("classAssignment");
			Elements trs = table.getElementsByTag("tr");
			int week = Editor.getInt(context, "week");
			if (week <= 0 || week >= 21) week = 0;
			if (week == 0) throw new Exception();
			for (int i = 1; i <= 12; i++) {
				Element tr = trs.get(i);
				Elements tds = tr.getElementsByTag("td");
				for (int j = 1; j <= 7; j++) {
					Element td = tds.get(j);
					if (td.hasAttr("style")) {

						if (td.text().contains("单周") && week % 2 == 0) continue;
						if (td.text().contains("双周") && week % 2 != 0) continue;

						Element span = td.child(0);
						String[] strings = span.html().split("<br>");
						String name = strings[0].trim();
						String secondLine = "";
						if (strings.length != 1) secondLine = strings[1].trim();
						lists[(i - 1) * 7 + (j - 1)] = name;
						int type = Constants.COURSE_TYPE_EVERY;
						if (td.text().contains("单周")) type = Constants.COURSE_TYPE_ODD;
						else if (td.text().contains("双周")) type = Constants.COURSE_TYPE_EVEN;

						types[(i - 1) * 7 + (j - 1)] = type;
						if (!hashMap.containsKey(name))
							hashMap.put(name, new Course(name, secondLine));
					}
				}
			}
		} catch (Exception e) {
			hashMap = new HashMap<String, Course>();
			lists = new String[12 * 7];
		}
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

class Course {
	String name;
	String location;
	int color;

	public Course(String firstLine, String secondLine) {
		name = firstLine;
		if (secondLine == null || "".equals(secondLine)) location = "";
		else {
			Scanner scanner = new Scanner(secondLine);
			String text = scanner.next();
			scanner.close();
			text = text.trim();
			if (text.startsWith("(") && text.endsWith("")) {
				location = text.substring(1, text.length() - 1);
			}
		}
		color = Util.generateColorInt();
	}
}

