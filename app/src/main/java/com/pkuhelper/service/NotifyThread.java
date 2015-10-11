package com.pkuhelper.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.Editor;
import com.pkuhelper.lib.MyCalendar;
import com.pkuhelper.lib.MyFile;
import com.pkuhelper.lib.view.MyNotification;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.lib.webconnection.WebConnection;

public class NotifyThread extends Thread implements Runnable {
	Context context;
	Handler handler;
	int last_notify_course_day;
	int last_notify_exam_day;
	int last_notify_course_index;
	
	public final static String[] wakeUpTime={
		"00:01","07:30","08:30","09:40","10:40","12:30",
				"13:30","14:40","15:40","16:40","18:10","19:10","20:10", "21:50"
	};
	public NotifyThread(Context _context, Handler _handler) {
		super();
		context=_context;
		handler=_handler;
	}
	
	@Override
	public void run() {
		try {
			if (android.os.Build.VERSION.SDK_INT<16)
				return;
			String username=Editor.getString(context, "username");
			String token=Editor.getString(context, "token");
			if ("".equals(username)) throw new Exception();
			boolean exam=Editor.getBoolean(context, "n_exam", true);
			boolean course=Editor.getBoolean(context, "n_course", true);
			boolean notifications=Editor.getBoolean(context, "n_notifications", true);
			
			last_notify_course_day=Editor.getInt(context, "course_day");
			last_notify_exam_day=Editor.getInt(context, "exam_day");
			last_notify_course_index=Editor.getInt(context, "course_index");
			
			if (exam) {
				try {
					checkExam(username);
				}catch (Exception e) {}
			}
			if (course) {
				try {
					checkCourse(username);
				} catch (Exception e) {}
			}
			if (notifications) {
				try {
					checkPushPool(token, "notification", "通知");
				} catch (Exception e) {}
				try {
					checkPushPool(token, "pkuhole", "树洞推送");
				} catch (Exception e) {}
			}
			try {
				checkPushPool(token, "message", "消息");
			} catch (Exception e) {}
			Editor.putInt(context, "course_day", last_notify_course_day);
			Editor.putInt(context, "course_index", last_notify_course_index);
			Editor.putInt(context, "exam_day", last_notify_exam_day);
		}
		catch (Exception e) {}
		finally {
			setAlarm();
			handler.sendEmptyMessage(Constants.MESSAGE_SERVICE_FINISHED);
		}
	}
	
	private long getSleepTime() {
		try {
			Calendar calendar=Calendar.getInstance();
			SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
			long currTime=calendar.getTimeInMillis();
			SimpleDateFormat format2=new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
			String today_date=format2.format(calendar.getTime());
			int len=wakeUpTime.length;
			long[] times=new long[len];
			for (int i=0;i<len;i++) {
				times[i]=simpleDateFormat.parse(today_date+" "+wakeUpTime[i]).getTime();
			}
			int nearest=0;
			while (nearest<len && times[nearest]<currTime) {
				nearest++;
			}
			if (nearest==len) return times[0]+86400*1000-currTime;
			return times[nearest]-currTime;
		}
		catch (Exception e) {return 600*1000;}
	}
	
	private void checkExam(String username) throws Exception {
		String exams=MyFile.getString(context, username, "exam", null);
		if ("".equals(exams)) return;
		JSONArray jsonArray=new JSONArray(exams);
		int len=jsonArray.length();
		Calendar calendar=Calendar.getInstance();
		int t=calendar.get(Calendar.DAY_OF_YEAR);
		int hour=calendar.get(Calendar.HOUR_OF_DAY);
		if (t==last_notify_exam_day) return;
		if (hour<19) return;
		String name="", location="", time="";
		for (int i=0;i<len;i++) {
			try {
				JSONObject jsonObject=jsonArray.getJSONObject(i);
				String _name=jsonObject.optString("name");
				String _location=jsonObject.optString("location");
				String _time=jsonObject.optString("time");
				String _date=jsonObject.optString("date");

				if (MyCalendar.getDaysLeft(_date)==1) {
					name=_name;
					location=_location;
					time=_time;
					break;
				}
			}
			catch (Exception e) {continue;}	
		}
		if (!"".equals(name)) {
			String title="您明天有考试哦！";
			String content=name+"将于"+time;
			if (!"".equals(location)) content+="在"+location;
			content+="开考。";
			MyNotification.sendNotification(title, content, content, context, "exam");
			last_notify_exam_day=t;
		}
	}
	
	/**
	 * 首先检查第二天是否有课；如果有课且时间位于21点30分以后，则进行提醒
	 * 再检查在当天的25分钟内存不存在课程（不连堂的）；存在则提醒
	 * @param username
	 * @throws Exception
	 */
	private void checkCourse(String username) throws Exception {
		if (context==null) return;
		Calendar calendar=Calendar.getInstance();
		//int minute=calendar.get(Calendar.MINUTE);
		String[] courseTime={"00:00","08:00","09:00","10:10","11:10","13:00",
				"14:00","15:10","16:10","17:10","18:40","19:40","20:40"};
		SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
		long currTime=calendar.getTimeInMillis();
		SimpleDateFormat format2=new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
		String today_date=format2.format(calendar.getTime());
		long[] times=new long[13];
		for (int i=0;i<=12;i++) {
			times[i]=simpleDateFormat.parse(today_date+" "+courseTime[i]).getTime();
		}
		

		int t=calendar.get(Calendar.DAY_OF_YEAR);
		
		String html=MyFile.getString(context, username, "course", null);
		if ("".equals(html)) return;

		int hour=calendar.get(Calendar.HOUR_OF_DAY);

		int day=MyCalendar.getWeekDayInNumber(calendar);
		int nxtday=MyCalendar.getWeekDayInNumber(calendar, 1);
		
		// 当前周数：单周/双周
		int week=Editor.getInt(context, "week");		
		// 如果放假期间，不提醒
		if (week==0) return;
		int nxtDayWeek=nxtday==1?week+1:week;
		
		int firstToHaveClass=-1;
		String dehtml=new String(html);
		// 记录第二天不同课程数
		HashSet<String> hashSet=new HashSet<String>();
		String nextTime="", nextCourse="", nextPlace="";
		int nxtIndex=0;
		
		
		try {
			Document document=Jsoup.parse(dehtml);
			Element table=document.getElementById("classAssignment");
			Elements trs=table.getElementsByTag("tr");
			for (int i=1;i<=12;i++) {
				Element tr=trs.get(i);
				Elements tds=tr.getElementsByTag("td");
				
				// 检查第二天有没有课
				Element td=tds.get(nxtday);
				if (td.hasAttr("style")) {
					
					// 如果是单周/双周
					if(td.text().contains("单周") && nxtDayWeek%2!=1) continue;
					if(td.text().contains("双周") && nxtDayWeek%2!=0) continue;
					
					if (firstToHaveClass==-1) firstToHaveClass=i;
					String style=td.attr("style");
					style=style.trim();
					hashSet.add(style);
				}
				
				// 检查即将有没有课
				// 判断是不是在35分钟内
				if (times[i]-currTime>=0 && times[i]-currTime<=2100*1000) {
					Element td2=tds.get(day);
					// 有课
					if (td2.hasAttr("style")) {
						
						if (td2.text().contains("单周") && week%2!=1) continue;
						if (td2.text().contains("双周") && week%2!=0) continue;
						
						String style=td2.attr("style").trim();
						nextCourse="";
						// 检查是不是连堂
						if (i!=1) {
							// 上一堂课
							Element td3=trs.get(i-1).getElementsByTag("td")
									.get(day);
							// 一样的课：忽略
							if (td3.hasAttr("style")
									&& td3.attr("style").trim().equals(style)) {
								nextCourse=null;
							}
						}
						// 不连堂，提醒
						if (nextCourse!=null) {
							String[] strings=td2.child(0).html().split("<br>");
							nextCourse=strings[0];
							if (strings.length!=1) {
								Scanner scanner=new Scanner(strings[1]);
								String tt=scanner.next();
								scanner.close();
								tt=tt.trim();
								// 检查是不是有地点
								if (tt.startsWith("(") && tt.endsWith(")")) {
									tt=tt.substring(1);
									tt=tt.substring(0, tt.length()-1);
									nextPlace=tt;
								}	
							}
							nextTime=courseTime[i];
							nxtIndex=i;
						}
					}
					
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		// 提醒第二天的课程
		if ((hour>=22 || (hour==21 && calendar.get(Calendar.MINUTE)>=30)) 
				&& firstToHaveClass!=-1 && t!=last_notify_course_day) {
			int num=hashSet.size();
			String title="您明天有"+num+"门课程哦！";
			String content="明天的第一门课程是第"+firstToHaveClass+"节。";
			String ticker="您明天有"+num+"门课程，第一门课程是第"+firstToHaveClass+"节。";
			MyNotification.sendNotification(title, content, ticker, context, "course");
			last_notify_course_day=t;
		}
		// 存在下一堂课，提醒
		if (nextCourse!=null && !"".equals(nextCourse)
				&& !"".equals(nextTime) && t*30+nxtIndex!=last_notify_course_index) {
			String title="下一门课是 "+nextCourse;
			String content="上课时间："+nextTime;
			String ticker=nextCourse+"将于"+nextTime;
			if (nextPlace!=null && !"".equals(nextPlace)) {
				content+=" 地点："+nextPlace;
				ticker+="在"+nextPlace;
			}
			ticker+="上课";
			MyNotification.sendNotification(title, content, ticker, context, "course");
			last_notify_course_index=nxtIndex+t*30;
		}
		
	}
	
	private void checkPushPool(String token, String type, String text) throws Exception {
		ArrayList<Parameters> arrayList=new ArrayList<Parameters>();
		arrayList.add(new Parameters("token", token));
		arrayList.add(new Parameters("type", type));
		Parameters parameters=WebConnection.connect(
			Constants.domain+"/pkuhelper/nc/androidPushPool.php", arrayList);
		if (!"200".equals(parameters.name)) return;
		JSONArray jsonArray=new JSONArray(parameters.value);
		int len=jsonArray.length();
		if (len==0) return;
		String title="你有"+len+"条新"+text;
		String content="点击立刻查看";
		if (len==1) {
			content=jsonArray.getJSONObject(0).optString("content");
			if ("".equals(content)) content="点击立刻查看";
		}
		if (content.length()>=50)
			content=content.substring(0, 48)+"...";
		MyNotification.sendNotification(title, content, title, context, type);
	}
	private void setAlarm() {
		Intent intent=new Intent(context, AlarmReceiver.class);
		intent.setAction(Constants.ACTION_ALARM);
		long millseconds=getSleepTime();
		AlarmManager alarmManager=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+millseconds, 
				PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
		
	}
	
}
