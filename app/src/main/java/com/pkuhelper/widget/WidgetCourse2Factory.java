package com.pkuhelper.widget;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.pkuhelper.R;
import com.pkuhelper.lib.Editor;
import com.pkuhelper.lib.MyCalendar;
import com.pkuhelper.lib.MyFile;

public class WidgetCourse2Factory implements RemoteViewsService.RemoteViewsFactory {
	private Context context;
	ArrayList<Course2> arrayList=new ArrayList<Course2>();
	int nearest;
	int page;
	
	public WidgetCourse2Factory(Context _context, Intent intent) {
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
		RemoteViews rv=new RemoteViews(context.getPackageName(), R.layout.widget_course2_item);
		Course2 course2=arrayList.get(position);
		rv.setTextViewText(R.id.widget_course2_item_name, course2.name);
		rv.setTextViewText(R.id.widget_course2_item_location, course2.location);
		
		String time=course2.starttime+"";
		if (course2.starttime!=course2.endtime) {
			time+="-"+course2.endtime;
		}
		rv.setTextViewText(R.id.widget_course2_item_time, time);
		
		if (course2.hasFinished
				|| page!=0) {
			rv.setInt(R.id.widget_course2_item_time_layout, "setBackgroundColor", 
					Color.parseColor("#60cdc9c9"));
		}
		else {
			rv.setInt(R.id.widget_course2_item_time_layout, "setBackgroundColor", 
					Color.parseColor("#60ee2c2c"));
		}
		
		return rv;
	}
	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return true;
	}
	
	private void refresh(){
		page=WidgetCourse2Provider.page;
		nearest=getNearestIndex();
		arrayList=new ArrayList<Course2>();
		try {
			String username=Editor.getString(context, "username");
			if ("".equals(username)) throw new Exception();
			String string=MyFile.getString(context, username, "course", null);
			if (string==null || "".equals(string)) throw new Exception();
			Document document=Jsoup.parse(string);
			Element table=document.getElementById("classAssignment");
			Elements trs=table.getElementsByTag("tr");
			int week=Editor.getInt(context, "week");
			if (week<=0 || week>=21) week=0;
			if (week==0) throw new Exception();

			int today=MyCalendar.getWeekDayInNumber(Calendar.getInstance(), page);
			week+=MyCalendar.getWeekPassed(Calendar.getInstance(), page);					
			
			String lastCss="",name="",location="";
			boolean has=false;
			int index=1;
			int starttime=0;
			while (index<=12) {
				Element td=trs.get(index).getElementsByTag("td").get(today);
				
				if (td.hasAttr("style")
						&& !(week%2==0 && td.text().contains("单周"))
						&& !(week%2!=0 && td.text().contains("双周"))) {
					String style=td.attr("style").trim();
					// 和上一堂课相同：忽略
					if (style.equals(lastCss))  {
						index++;continue;
					}
					
					// 否则为一节新课
					
					// 当前存在一节课：记录
					if (has) {
						arrayList.add(new Course2(name, location, starttime, index-1, nearest));
						has=false;
					}
					
					lastCss=style;
					String[] strings=td.child(0).html().split("<br>");
					name=strings[0];
					if (strings.length!=1) {
						Scanner scanner=new Scanner(strings[1]);
						String tt=scanner.next();
						scanner.close();
						tt=tt.trim();
						// 检查是不是有地点
						if (tt.startsWith("(") && tt.endsWith(")")) {
							tt=tt.substring(1);
							tt=tt.substring(0, tt.length()-1);
							location=tt;
						}	
						else location="地点未知";
					}
					else location="地点未知";
					
					starttime=index;
					has=true;
					
				}
				else {
					if (has) {
						arrayList.add(new Course2(name, location, starttime, index-1, nearest));
						has=false;
					}
					has=false;
					lastCss="";
					name="";
					location="";
					starttime=0;					
				}
				index++;
			}
			if (has) {
				arrayList.add(new Course2(name, location, starttime, 12, nearest));
				has=false;
			}
			
		}
		catch (Exception e) {
			arrayList=new ArrayList<Course2>();
		}
		
		if (page==0) {
			Collections.sort(arrayList, new Comparator<Course2>() {
				@Override
				public int compare(Course2 lhs, Course2 rhs) {
					if (lhs.hasFinished && !rhs.hasFinished) return 1;
					if (!lhs.hasFinished && rhs.hasFinished) return -1;
					return lhs.endtime-rhs.endtime;
				}
			});
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
	
	private int getNearestIndex() {
		Calendar calendar=Calendar.getInstance();
		int hour=calendar.get(Calendar.HOUR_OF_DAY);
		if (hour==24) hour=0;
		int minute=calendar.get(Calendar.MINUTE);
		
		if (hour<8 || (hour==8 && minute<50)) return 1;
		if (hour<9 || (hour==9 && minute<50)) return 2;
		if (hour<11) return 3;
		if (hour<12) return 4;
		if (hour<13 || (hour==13 && minute<50)) return 5;
		if (hour<14 || (hour==14 && minute<50)) return 6;
		if (hour<16) return 7;
		if (hour<17) return 8;
		if (hour<18) return 9;
		if (hour<19 || (hour==19 && minute<30)) return 10;
		if (hour<20 || (hour==20 && minute<30)) return 11;
		if (hour<21 || (hour==21 && minute<30)) return 12;
		return 13;
	}
	
}

class Course2 {
	String name;
	String location;
	int starttime,endtime;
	boolean hasFinished;
	public Course2(String _name, String _location, int _starttime, int _endtime, int nearest) {
		name=_name;location=_location;starttime=_starttime;endtime=_endtime;
		hasFinished=endtime<nearest;
	}

}
