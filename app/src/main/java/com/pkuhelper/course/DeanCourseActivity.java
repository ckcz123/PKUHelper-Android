package com.pkuhelper.course;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.pkuhelper.R;
import com.pkuhelper.lib.BaseActivity;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.Lib;
import com.pkuhelper.lib.MyFile;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.widget.WidgetCourse2Provider;
import com.pkuhelper.widget.WidgetCourseProvider;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class DeanCourseActivity extends BaseActivity {
	ArrayList<String> courseInfos=new ArrayList<String>();
	HashMap<String, CourseInfo> courseMap=new HashMap<String, CourseInfo>();
	boolean hasModified;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.customcourse_listview);
		getActionBar().setTitle("修改教务课程地点");
		getList();
	}
	
	private void getList() {
		//String html=Editor.getString(this, Constants.username+"_course");
		String html;
		try {
			html=MyFile.getString(this, Constants.username, "course", "");
		}
		catch (Exception e) {html="";}
		if ("".equals(html)) {
			CustomToast.showErrorToast(this, "请重新进行教务课程的同步！");
			return;
		}
		String dehtml=new String(html);
		try {
			Document document=Jsoup.parse(dehtml);
			Element table=document.getElementById("classAssignment");
			Elements trs=table.getElementsByTag("tr");
			for (int i=1;i<=12;i++) {
				Element tr=trs.get(i);
				Elements tds=tr.getElementsByTag("td");
				for (int j=1;j<=7;j++) {
					Element td=tds.get(j);
					if (td.hasAttr("style")) {
						Element span=td.child(0);
						String[] strings=span.html().split("<br>");
						String name=strings[0].trim();
						String secondLine=strings[1].trim();
						
						String location="？";
						int pos=secondLine.indexOf(")");
						if (secondLine.startsWith("(") && pos!=-1) {
							location=secondLine.substring(1, pos);
						}
						
						int type=Constants.COURSE_TYPE_EVERY;
						if (td.text().contains("单周")) type=Constants.COURSE_TYPE_ODD;
						else if (td.text().contains("双周")) type=Constants.COURSE_TYPE_EVEN;
						
						if (courseMap.containsKey(name)) {
							courseMap.get(name).addTime(j+"", i+"", type);
						}
						else {
							CourseInfo courseInfo=new CourseInfo(name, location);
							courseInfo.addTime(j+"", i+"", type);
							courseInfos.add(name);
							courseMap.put(name, courseInfo);
						}
					}
				}
			}
			for (Map.Entry<String, CourseInfo> entry: courseMap.entrySet()) {
				entry.getValue().sortAndMerge();
			}
			hasModified=false;
			showList();
		}
		catch (Exception e) {
			CustomToast.showErrorToast(this, "未知错误");
		}
	}

	protected void finishRequest(int type, String string) {}

	public void showList() {
		setContentView(R.layout.customcourse_listview);
		invalidateOptionsMenu();
		ListView listView=(ListView)findViewById(R.id.customcourse_listview);
		listView.setAdapter(new BaseAdapter() {
			
			@SuppressLint("ViewHolder")
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				CourseInfo courseInfo=courseMap.get(courseInfos.get(position));
				String name=courseInfo.name;
				String location=courseInfo.where;
				LayoutInflater layoutInflater=getLayoutInflater();
				convertView=layoutInflater.inflate(R.layout.customcourse_listitem, parent, false);
				ViewSetting.setTextView(convertView, R.id.customcourse_list_name, name);
				ViewSetting.setTextView(convertView, R.id.customcourse_list_location, location);
				String when="";
				ArrayList<TimeInfo> arrayList=courseInfo.when;
				Collections.sort(arrayList, new Comparator<TimeInfo>() {
					@Override
					public int compare(TimeInfo t1, TimeInfo t2) {
						if (t1.type<t2.type) return -1;
						if (t1.type>t2.type) return 1;
						if (t1.week<t2.week) return -1;
						if (t1.week>t2.week) return 1;
						if (t1.starttime<t2.starttime) return -1;
						return 1;
					}
				});
				int l=arrayList.size();
				for (int i=0;i<l;i++) {
					TimeInfo timeInfo=arrayList.get(i);
					if (i!=0) when+="\n";
					when+=getWeekName(timeInfo.week)+"   "
							+getClassTime(timeInfo.starttime, timeInfo.endtime)
							+" "+getWeek(timeInfo.type);
				}
				ViewSetting.setTextView(convertView, R.id.customcourse_list_when, when);				
				return convertView;
			}
			private String getWeekName(int week) {
				switch (week) {
					case 1: return"星期一";
					case 2: return"星期二";
					case 3: return"星期三";
					case 4: return"星期四";
					case 5: return"星期五";
					case 6: return"星期六";
					case 7: return"星期日";
				}
				return "星期日";
			}
			private String getClassTime(int starttime, int endtime) {
				String time="第";
				time+=starttime;
				if (starttime!=endtime)
					time+="-"+endtime;
				time+="节";
				return time;
			}
			private String getWeek(int type) {
				if (type==Constants.COURSE_TYPE_ODD) return "单周";
				else if (type==Constants.COURSE_TYPE_EVEN) return "双周";
				else return "每周";
			}
			
			@Override
			public long getItemId(int position) {
				return 0;
			}
			
			@Override
			public Object getItem(int position) {
				return null;
			}
			
			@Override
			public int getCount() {
				return courseInfos.size();
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final CourseInfo courseInfo=courseMap.get(courseInfos.get(position));
				
				AlertDialog.Builder alertBuilder=new AlertDialog.Builder(DeanCourseActivity.this);
				alertBuilder.setTitle("设置地点");
				final EditText editText=new EditText(alertBuilder.getContext());
				editText.setText(courseInfo.where);
				alertBuilder.setView(editText);
				
				alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						courseInfo.where=editText.getEditableText().toString();
						hasModified=true;
						showList();
					}
				}).setNegativeButton("取消", null).show();				
			}
		});	
	}
	
	void save(final boolean finished) {
		/*
		try {
			Editor.putString(this, Constants.username+"_course", 
					doWithDocument(Jsoup.parse(Editor.getString(this, "course"))).toString());
		}
		catch (Exception e) {}
		*/
		try {
			MyFile.putString(this, Constants.username, "course", 
					doWithDocument(Jsoup.parse(
							MyFile.getString(this, Constants.username, "course", null)))
							.toString());
		}
		catch (Exception e) {}
		
		hasModified=false;
		

		Lib.sendBroadcast(this, WidgetCourseProvider.class, Constants.ACTION_REFRESH_COURSE);
		Lib.sendBroadcast(this, WidgetCourse2Provider.class, Constants.ACTION_REFRESH_COURSE);
		
		new AlertDialog.Builder(this)
		.setTitle("保存成功！").setMessage("退出并重进软件后方可生效。")
		.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (finished) {
					finish();
				}
			}
		}).show();
		
	}
	
	private Document doWithDocument(Document document) throws Exception {
		Element table=document.getElementById("classAssignment");
		Elements trs=table.getElementsByTag("tr");
		for (int i=1;i<=12;i++) {
			Element tr=trs.get(i);
			Elements tds=tr.getElementsByTag("td");
			for (int j=1;j<=7;j++) {
				Element td=tds.get(j);
				if (td.hasAttr("style")) {
					Element span=td.child(0);
					String[] strings=span.html().split("<br>");
					String name=strings[0].trim();
					
					if (courseMap.containsKey(name)) {
						CourseInfo courseInfo=courseMap.get(name);
						String type="";
						if (span.html().contains("单周")) type="单周";
						if (span.html().contains("双周")) type="双周";
						if (span.html().contains("每周")) type="每周";
						span.html(name+"<br>("+courseInfo.where+")<br>"+type);
					}
				}
			}
		}
		return document;
	}

	private void wantToExit() {
		if (hasModified) {
			new AlertDialog.Builder(this).setTitle("是否保存？")
			.setMessage("你经过了修改，是否保存？").setCancelable(true)
			.setPositiveButton("是", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					save(true);
				}
			}).setNegativeButton("否", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					finish();
				}
			}).show();
		}
		else {
			finish();
		}
	}
	
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(Menu.NONE, Constants.MENU_CUSTOM_COURSE_SAVE, Constants.MENU_CUSTOM_COURSE_SAVE, "")
		.setIcon(R.drawable.save).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(Menu.NONE, Constants.MENU_CUSTOM_COURSE_CLOSE, Constants.MENU_CUSTOM_COURSE_CLOSE, "")
		.setIcon(R.drawable.close).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id==Constants.MENU_CUSTOM_COURSE_SAVE) {
			save(false);
			return true;
		}
		if (id==Constants.MENU_CUSTOM_COURSE_CLOSE) {
			wantToExit();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode==KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN) {
			wantToExit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
				
	}

}
