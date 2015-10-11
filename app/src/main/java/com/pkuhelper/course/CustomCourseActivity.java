package com.pkuhelper.course;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.MyFile;
import com.pkuhelper.lib.Util;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

public class CustomCourseActivity extends Activity {
	ArrayList<CourseInfo> courseInfos=new ArrayList<CourseInfo>();
	int currCourseIndex=0;
	int[][] hasCourse=new int[12][7];
	
	boolean[][] defaultCourse=new boolean[12][7];
	public static CustomCourseActivity customCourseActivity;
	public static final int PAGE_LIST = 0;
	public static final int PAGE_MODIFY = 1;
	int page;
	boolean hasModified;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.customcourse_listview);
		Util.getOverflowMenu(this);
		customCourseActivity=this;
		getActionBar().setTitle("自选课程");
		setDefaultCourse();
		getList();
	}
	
	public void setDefaultCourse() {
		String html="";
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
					if (td.hasAttr("style"))
						defaultCourse[i-1][j-1]=true;
					else defaultCourse[i-1][j-1]=false;
				}
			}
			html=document.toString();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public void getList() {
		ArrayList<Parameters> arrayList=new ArrayList<Parameters>();
		//arrayList.add(new Parameters("uid", Constants.username));
		//String timestamp=System.currentTimeMillis()/1000+"";
		//String hash=Util.getHash(Constants.username+timestamp+"CHAW68ERFR23G");
		//arrayList.add(new Parameters("timestamp", timestamp));
		//arrayList.add(new Parameters("hash", hash));
		arrayList.add(new Parameters("operation", "get"));
		arrayList.add(new Parameters("token", Constants.token));
		new RequestingTask("正在获取自选课表..", 
				Constants.domain+"/services/course.php", Constants.REQUEST_CUSTOM_COURSE_GET)
				.execute(arrayList);
		hasModified=false;
	}
	
	public void realGetList(String string) {
		try {
			JSONObject object=new JSONObject(string);
			int hasCustom=object.getInt("hasCustom");
			if (hasCustom!=1) {
				CustomToast.showInfoToast(this, "你暂时没有自选课程");
				showList();
				return;
			}
			JSONArray jsonArray=object.optJSONArray("courses");
			int len=jsonArray.length();
			for (int i=0;i<len;i++) {
				JSONObject jsonObject=jsonArray.optJSONObject(i);
				String name=jsonObject.optString("courseName");
				String location=jsonObject.optString("location");
				CourseInfo courseInfo=new CourseInfo(name, location);
				JSONArray times=jsonObject.optJSONArray("times");
				int l=times.length();
				for (int j=0;j<l;j++) {
					JSONObject time=times.optJSONObject(j);
					int type;
					String typeString=time.optString("week", "all");
					typeString=typeString.trim();
					if ("odd".equals(typeString)) type=Constants.COURSE_TYPE_ODD;
					else if ("even".equals(typeString)) type=Constants.COURSE_TYPE_EVEN;
					else type=Constants.COURSE_TYPE_EVERY;
					courseInfo.addTime(time.optString("day"), time.optString("num"), type);
				}
				courseInfos.add(courseInfo);
			}
			if (courseInfos.size()==0) {
				CustomToast.showInfoToast(this, "你暂时没有自选课程");
			}
		}
		catch (Exception e) {
		}
		finally {
			showList();
		}
	}
	
	public void showList() {
		setContentView(R.layout.customcourse_listview);
		page=PAGE_LIST;
		invalidateOptionsMenu();
		for (int i=0;i<12;i++)
			for (int j=0;j<7;j++)
				hasCourse[i][j]=0;
		ListView listView=(ListView)findViewById(R.id.customcourse_listview);
		listView.setAdapter(new BaseAdapter() {
			
			@SuppressLint("ViewHolder")
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				CourseInfo courseInfo=courseInfos.get(position);
				String name=courseInfo.name;
				String location=courseInfo.where;
				LayoutInflater layoutInflater=customCourseActivity.getLayoutInflater();
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
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public Object getItem(int position) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return courseInfos.size();
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final int index=Integer.valueOf(position);
				String[] items={"修改","删除"};
				new AlertDialog.Builder(customCourseActivity)
				.setTitle("请选择你要的操作")
				.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which==0) {
							currCourseIndex=index;
							modifyCustomCourse(courseInfos.get(index));
						}
						else if (which==1) {
							new AlertDialog.Builder(customCourseActivity).setMessage("你确定要删除此项吗？").
							setTitle("确定删除？").setCancelable(true)
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									courseInfos.remove(index);
									hasModified=true;
									showList();
								}
							}).setNegativeButton("取消", null).show();
						}
					}
				}).setCancelable(true).show();
			}
		});	
		
		
	}
	
	public void modifyCustomCourse(CourseInfo currentCourse) {
		setContentView(R.layout.customcourse_modifyview);
		((TextView)findViewById(R.id.custom_course_when_hint))
			.setText(Html.fromHtml(getString(R.string.custom_course_when_hint)));
		for (int i=0;i<12;i++)
			for (int j=0;j<7;j++)
				hasCourse[i][j]=Constants.COURSE_TYPE_NONE;
		if (currentCourse==null) {
			currentCourse=new CourseInfo();
			currCourseIndex=-1;
		}
		ArrayList<TimeInfo> arrayList=currentCourse.when;
		for (int i=0;i<arrayList.size();i++) {
			TimeInfo timeInfo=arrayList.get(i);
			int week=timeInfo.week,st=timeInfo.starttime,ed=timeInfo.endtime;
			for (int j=st;j<=ed;j++)
				hasCourse[j-1][week-1]=timeInfo.type;
		}
		page=PAGE_MODIFY;
		invalidateOptionsMenu();
		ViewSetting.setTextView(this, R.id.custom_course_name, currentCourse.name);
		ViewSetting.setTextView(this, R.id.custom_course_location, currentCourse.where);
		
		GridView gridView=(GridView)findViewById(R.id.custom_course_time);
		gridView.setAdapter(new BaseAdapter() {
			
			@SuppressLint("ViewHolder")
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				LayoutInflater layoutInflater=customCourseActivity.getLayoutInflater();
				convertView=layoutInflater.inflate(R.layout.customcourse_modify_item, parent, false);
				int x=position/8;
				int y=position%8;
				if (x==0) {
					if (y!=0)
						ViewSetting.setTextView(convertView, R.id.custom_course_grid_item, getWeekName(y));
					return convertView;
				}
				if (y==0) {
					ViewSetting.setTextView(convertView, R.id.custom_course_grid_item, x+"");
				}
				else {
					convertView.setBackgroundColor(getColor(hasCourse[x-1][y-1], defaultCourse[x-1][y-1]));
				}
				return convertView;
			}
			private String getWeekName(int week) {
				switch (week) {
					case 1: return"一";
					case 2: return"二";
					case 3: return"三";
					case 4: return"四";
					case 5: return"五";
					case 6: return"六";
					case 7: return"日";
				}
				return "日";
			}
			
			@Override
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public Object getItem(int position) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return 104;
			}
		});
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				int x=position/8;
				int y=position%8;
				if (x==0 || y==0) return;
				int type=hasCourse[x-1][y-1];
				switch (type) {
				case Constants.COURSE_TYPE_NONE:type=Constants.COURSE_TYPE_EVERY;break;
				case Constants.COURSE_TYPE_EVERY:type=Constants.COURSE_TYPE_ODD;break;
				case Constants.COURSE_TYPE_ODD:type=Constants.COURSE_TYPE_EVEN;break;
				case Constants.COURSE_TYPE_EVEN:type=Constants.COURSE_TYPE_NONE;break;
				}
				hasCourse[x-1][y-1]=type;
				view.setBackgroundColor(getColor(type, defaultCourse[x-1][y-1]));
				
			}
		});
		
	}
	
	public CourseInfo setCourse() {
		if (page!=PAGE_MODIFY) return null;
		String name=ViewSetting.getEditTextValue(this, R.id.custom_course_name);
		String location=ViewSetting.getEditTextValue(this, R.id.custom_course_location);
		// 先扫描双周，再扫描单周，最后扫描每周
		return getCourseInfo(getCourseInfo(getCourseInfo(
				new CourseInfo(name, location), Constants.COURSE_TYPE_EVEN), 
				Constants.COURSE_TYPE_ODD), Constants.COURSE_TYPE_EVERY);
	}
	
	private CourseInfo getCourseInfo(CourseInfo currentCourse, int type) {
		for (int week=1;week<=7;week++) {
			int index=1;
			int starttime=0,endtime=0;
			boolean has=false;
			while (index<=12) {
				if (hasCourse[index-1][week-1]==type) {
					if (!has) {
						has=true;
						starttime=index;
					}					
				}
				else {
					if (has) {
						has=false;
						endtime=index-1;
						currentCourse.addTime(week, starttime, endtime, type);
						starttime=endtime=0;
					}
				}
				index++;
			}
			if (has) {
				currentCourse.addTime(week, starttime, 12, type);
			}
		}
		return currentCourse;
	}
	
	public void finishRequest(int type,String string) {
		if (type==Constants.REQUEST_CUSTOM_COURSE_GET)
			realGetList(string);
		if (type==Constants.REQUEST_CUSTOM_COURSE_SAVE)
			finishSave(string);
	}
	public void finishSave(String string) {
		Log.w("msg", string);
		try {
			JSONObject jsonObject=new JSONObject(string);
			int code=jsonObject.getInt("code");
			if (code!=0) {
				new AlertDialog.Builder(this).setTitle("保存失败！").setMessage(jsonObject.optString("msg"))
				.setPositiveButton("确定", null).setCancelable(true).show();
			}
			else {
				new AlertDialog.Builder(this).setTitle("保存成功！")
				.setMessage("请于主界面刷新（仅导入自定义课程）以进行同步。")
				.setPositiveButton("确定", null).setCancelable(true).show();
				hasModified=false;
			}
		}
		catch (Exception e) {
			CustomToast.showErrorToast(this, "保存失败");
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Util.setIconEnable(menu, true);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (page==PAGE_LIST)
			menu.add(Menu.NONE, Constants.MENU_CUSTOM_COURSE_ADD, Constants.MENU_CUSTOM_COURSE_ADD, "")
				.setIcon(R.drawable.add).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(Menu.NONE, Constants.MENU_CUSTOM_COURSE_SAVE, Constants.MENU_CUSTOM_COURSE_SAVE, "")
		.setIcon(R.drawable.save).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(Menu.NONE, Constants.MENU_CUSTOM_COURSE_CLOSE, Constants.MENU_CUSTOM_COURSE_CLOSE, "")
		.setIcon(R.drawable.close).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}
	@SuppressWarnings("unchecked")
	public void save() {
		try {
			JSONArray jsonArray=new JSONArray();
			for (int i=0;i<courseInfos.size();i++) {
				CourseInfo courseInfo=courseInfos.get(i);
				JSONObject object=new JSONObject();
				object.put("courseName", courseInfo.name);
				object.put("location", courseInfo.where);
				ArrayList<TimeInfo> arrayList=courseInfo.when;
				JSONArray array=new JSONArray();
				for (int j=0;j<arrayList.size();j++) {
					TimeInfo timeInfo=arrayList.get(j);
					JSONObject time=new JSONObject();
					time.put("day", timeInfo.week+"");
					String str=""+timeInfo.starttime;
					if (timeInfo.starttime!=timeInfo.endtime) {
						str+="-"+timeInfo.endtime;
					}
					time.put("num", str);
					String type="all";
					if (timeInfo.type==Constants.COURSE_TYPE_EVEN) type="even";
					else if (timeInfo.type==Constants.COURSE_TYPE_ODD) type="odd";
					time.put("week", type);
					array.put(time);
				}
				object.put("times", array);
				jsonArray.put(object);
			}
			ArrayList<Parameters> arrayList=new ArrayList<Parameters>();
			//arrayList.add(new Parameters("uid", Constants.username));
			//String timestamp=System.currentTimeMillis()/1000+"";
			//String hash=Util.getHash(Constants.username+timestamp+"CHAW68ERFR23G");
			//arrayList.add(new Parameters("timestamp", timestamp));
			//arrayList.add(new Parameters("hash", hash));
			arrayList.add(new Parameters("token", Constants.token));
			arrayList.add(new Parameters("operation", "set"));
			arrayList.add(new Parameters("content", jsonArray.toString()));
			new RequestingTask("正在保存..", 
					Constants.domain+"/services/course.php", Constants.REQUEST_CUSTOM_COURSE_SAVE)
					.execute(arrayList);
		}
		catch (Exception e) {
		}
	}
	public void wantToExit() {
		if (!hasModified) 
			finish();
		else {
			new AlertDialog.Builder(this).setTitle("是否保存？")
			.setMessage("你进行了修改，是否保存？")
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					save();
				}
			}).setCancelable(true).setNegativeButton("取消", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					finish();
				}
			}).show();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id==Constants.MENU_CUSTOM_COURSE_ADD) {
			modifyCustomCourse(null);
			return true;
		}
		if (id==Constants.MENU_CUSTOM_COURSE_SAVE) {
			if (page==PAGE_LIST) {
				save();
				return true;
			}
			else if (page==PAGE_MODIFY) {
				CourseInfo currentCourse=setCourse();
				if (currentCourse==null) return true;
				if (currCourseIndex!=-1)
					courseInfos.set(currCourseIndex, currentCourse);
				else 
					courseInfos.add(currentCourse);
				new AlertDialog.Builder(this).setTitle("保存成功！")
				.setMessage("你还需要在自选课程列表页点击右上角的保存来和服务器同步。")
				.setPositiveButton("确认", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						showList();
					}
				}).setCancelable(true).setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						showList();
					}
				}).show();
				hasModified=true;
				return true;
			}
		}
		if (id==Constants.MENU_CUSTOM_COURSE_CLOSE) {
			if (page==PAGE_LIST)
				wantToExit();
			else if (page==PAGE_MODIFY)
				showList();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode==KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN) {
			if (page==PAGE_LIST)
				wantToExit();
			else if (page==PAGE_MODIFY)
				showList();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private int getColor(int type, boolean hasDefault) {
		switch (type) {
		case Constants.COURSE_TYPE_NONE:
			if (hasDefault) return Color.CYAN;
			return Color.WHITE;
		case Constants.COURSE_TYPE_EVERY:
			return Color.YELLOW;
		case Constants.COURSE_TYPE_EVEN:
			return Color.MAGENTA;
		case Constants.COURSE_TYPE_ODD:
			return Color.GREEN;
		default:
			break;
		}
		return Color.WHITE;
	}
	
}
