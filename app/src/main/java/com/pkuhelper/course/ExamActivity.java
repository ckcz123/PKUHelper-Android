package com.pkuhelper.course;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.Lib;
import com.pkuhelper.lib.MyCalendar;
import com.pkuhelper.lib.MyFile;
import com.pkuhelper.lib.Util;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.view.MyDatePickerDialog;
import com.pkuhelper.lib.view.MyTimePickerDialog;
import com.pkuhelper.widget.WidgetExamProvider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TimePicker;

public class ExamActivity extends Activity {
	public static String examString="[]";
	public static ExamActivity examActivity;
	ArrayList<ExamInfo> examInfos;
	ListView listView;
	boolean hasModified;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exam_listview);
		examActivity=this;
		getActionBar().setTitle("我的考试");
		Util.getOverflowMenu(this);
		init();
	}
	
	void init() {
		examInfos=new ArrayList<ExamInfo>();
		try {
			examString=MyFile.getString(this, Constants.username, "exam", "[]");
		}
		catch (Exception e) {examString="[]";}
		if (examString==null || "".equals(examString)) examString="[]";
		Log.w("exams", examString);
		try {
			JSONArray jsonArray=new JSONArray(examString);
			int len=jsonArray.length();
			for (int i=0;i<len;i++) {
				JSONObject jsonObject=jsonArray.getJSONObject(i);
				String name=jsonObject.optString("name");
				String location=jsonObject.optString("location");
				String time=jsonObject.optString("time");
				String date=jsonObject.optString("date");
				examInfos.add(new ExamInfo(name, location, date, time));
			}
			if (examInfos.size()==0) {
				CustomToast.showInfoToast(this, "暂时没有考试信息");
			}
			hasModified=false;
			showList();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void sort() {
		Collections.sort(examInfos, new Comparator<ExamInfo>() {
			@Override
			public int compare(ExamInfo lhs, ExamInfo rhs) {
				if (lhs.finished && !rhs.finished) return 1;
				if (!lhs.finished && rhs.finished) return -1;
				int dateCmp=lhs.date.compareTo(rhs.date);
				if (dateCmp!=0) return dateCmp;
				return lhs.time.compareTo(rhs.time);
			}
		});
	}
	
	void showList() {
		sort();
		setContentView(R.layout.exam_listview);
		listView=(ListView)findViewById(R.id.exam_listview);
		listView.setAdapter(new BaseAdapter() {
			
			@SuppressLint("ViewHolder")
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ExamInfo examInfo=examInfos.get(position);
				convertView=getLayoutInflater().inflate(R.layout.exam_listitem, parent, false);
				ViewSetting.setTextView(convertView, R.id.exam_list_name, examInfo.name);
				ViewSetting.setTextView(convertView, R.id.exam_list_location, examInfo.location);
				ViewSetting.setTextView(convertView, R.id.exam_list_time, examInfo.date+" "+examInfo.time);
				ViewSetting.setTextView(convertView, R.id.exam_list_days_left, examInfo.daysLeft);
				if (!examInfo.finished)
					convertView.setBackgroundColor(Color.parseColor("#FFEC8B"));
				else {
					convertView.setBackgroundColor(Color.WHITE);
					ViewSetting.setTextViewColor(convertView, R.id.exam_list_days_left, Color.parseColor("#aaaaaa"));
				}
				return convertView;
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
				return examInfos.size();
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				String[] strings={"修改","删除"};
				new AlertDialog.Builder(ExamActivity.this).setTitle("编辑考试")
				.setItems(strings, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which==0) modifyExam(position);
						else if (which==1) {
							if (!examInfos.get(position).finished) {
								new AlertDialog.Builder(examActivity).setTitle("确认删除？")
								.setMessage("确认删除此考试条目？")
								.setPositiveButton("是", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										examInfos.remove(position);
										makeChange();
									}
								}).setNegativeButton("否", null).show();
							}
							else {
								examInfos.remove(position);
								makeChange();
							}
						}
					}
				}).show();
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Util.setIconEnable(menu, true);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(Menu.NONE, Constants.MENU_EXAM_ADD, Constants.MENU_EXAM_ADD, "")
		.setIcon(R.drawable.add).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(Menu.NONE, Constants.MENU_EXAM_SAVE, Constants.MENU_EXAM_SAVE, "")
		.setIcon(R.drawable.save).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(Menu.NONE, Constants.MENU_EXAM_CLOSE, Constants.MENU_EXAM_CLOSE, "")
		.setIcon(R.drawable.close).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		
		if (id==Constants.MENU_EXAM_ADD) {
			modifyExam(-1);
			return true;
		}
		if (id==Constants.MENU_EXAM_SAVE) {
			save(false);
			return true;
		}
		if (id==Constants.MENU_EXAM_CLOSE) {
			wantToExit();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	void makeChange() {
		hasModified=true;
		sort();
		try {
		((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
		}catch (Exception e) {}
	}
	
	void wantToExit() {
		if (!hasModified) 
			finish();
		else {
			new AlertDialog.Builder(this).setTitle("是否保存？")
			.setMessage("你进行了修改，是否保存？")
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					save(true);
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
	
	void save(boolean needToFinish) {
		try {
			String string="";
			JSONArray jsonArray=new JSONArray();
			int size=examInfos.size();
			for (int i=0;i<size;i++) {
				ExamInfo examInfo=examInfos.get(i);
				JSONObject jsonObject=new JSONObject();
				jsonObject.put("name", examInfo.name);
				jsonObject.put("location", examInfo.location);
				jsonObject.put("date", examInfo.date);
				jsonObject.put("time", examInfo.time);
				jsonArray.put(jsonObject);
			}
			string=jsonArray.toString();
			MyFile.putString(this, Constants.username, "exam", string);
			hasModified=false;
			CustomToast.showSuccessToast(this, "保存成功");
			

			Lib.sendBroadcast(this, WidgetExamProvider.class, Constants.ACTION_REFRESH_EXAM);
			
		}
		catch (Exception e) {CustomToast.showErrorToast(this, "保存失败");}
		if (needToFinish) finish();
		else init();
	}
	
	void modifyExam(final int id) {
		ExamInfo examInfo;
		final Calendar calendar=Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 8);
		calendar.set(Calendar.MINUTE, 30);
		calendar.set(Calendar.SECOND, 0);
		final SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		final SimpleDateFormat timeFormat=new SimpleDateFormat("HH:mm", Locale.getDefault());
		if (id==-1)
			examInfo=new ExamInfo("", "", dateFormat.format(calendar.getTime()),
					timeFormat.format(calendar.getTime()));
		else {
			examInfo=examInfos.get(id);
			SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
			try {
				Date date=format.parse(examInfo.date+" "+examInfo.time);
				calendar.setTime(date);
				calendar.set(Calendar.SECOND, 0);
			}catch (Exception e) {}
		}
		
		final Dialog dialog=new Dialog(this);
		dialog.setTitle("编辑考试");
		dialog.setContentView(R.layout.exam_modify);
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		
		ViewSetting.setEditTextValue(dialog, R.id.exam_name, examInfo.name);
		ViewSetting.setEditTextValue(dialog, R.id.exam_location, examInfo.location);
		ViewSetting.setTextView(dialog, R.id.exam_date, examInfo.date);
		ViewSetting.setTextView(dialog, R.id.exam_time, examInfo.time);
		
		ViewSetting.setOnClickListener(dialog, R.id.exam_tablerow_date, new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int year=calendar.get(Calendar.YEAR);
				int monthOfYear=calendar.get(Calendar.MONTH);
				int dayOfMonth=calendar.get(Calendar.DAY_OF_MONTH);
				MyDatePickerDialog datePickerDialog=new MyDatePickerDialog(ExamActivity.this, new DatePickerDialog.OnDateSetListener() {
					
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear,
							int dayOfMonth) {
						//String date=year+"-"+monthOfYear+"-"+dayOfMonth;
						calendar.set(Calendar.YEAR, year);
						calendar.set(Calendar.MONTH, monthOfYear);
						calendar.set(Calendar.DATE, dayOfMonth);
						ViewSetting.setTextView(dialog, R.id.exam_date, 
								dateFormat.format(calendar.getTime()));
					}
				}, year, monthOfYear, dayOfMonth);
				datePickerDialog.setPermanentTitle("选择考试日期");
				datePickerDialog.setCancelable(true);
				datePickerDialog.setCanceledOnTouchOutside(true);
				datePickerDialog.show();
			}
		});
		ViewSetting.setOnClickListener(dialog, R.id.exam_tablerow_time, new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MyTimePickerDialog timePickerDialog=new MyTimePickerDialog(ExamActivity.this, new TimePickerDialog.OnTimeSetListener() {
					
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
						calendar.set(Calendar.MINUTE, minute);
						ViewSetting.setTextView(dialog, R.id.exam_time, 
								timeFormat.format(calendar.getTime()));
					}
				}, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
				timePickerDialog.setPermanentTitle("选择考试时间");
				timePickerDialog.setCancelable(true);
				timePickerDialog.setCanceledOnTouchOutside(true);
				timePickerDialog.show();
			}
		});
		ViewSetting.setOnClickListener(dialog, R.id.exam_save, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				realSave(id, ViewSetting.getEditTextValue(dialog, R.id.exam_name), 
						ViewSetting.getEditTextValue(dialog, R.id.exam_location),
						ViewSetting.getTextView(dialog, R.id.exam_date),
						ViewSetting.getTextView(dialog, R.id.exam_time));
				dialog.dismiss();
			}
		});
		
		ViewSetting.setOnClickListener(dialog, R.id.exam_cancel, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		
		dialog.show();
	}
	
	
	
	void realSave(int id, String name, String location, String date, String time) {
		ExamInfo examInfo=new ExamInfo(name, location, date, time);
		if (id!=-1) examInfos.set(id, examInfo);
		else examInfos.add(examInfo);
		makeChange();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode==KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN) {
			wantToExit();
		}
		return super.onKeyDown(keyCode, event);
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
			name=_name;location=_location;date=_date;time=_time;
			finished=checkfinished();
			daysLeft=getDeltaDays();
		}
		private boolean checkfinished() {
			Calendar calendar=Calendar.getInstance();
			SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
			SimpleDateFormat timeFormat=new SimpleDateFormat("HH:mm", Locale.getDefault());
			String nowDate=dateFormat.format(calendar.getTime());
			String nowTime=timeFormat.format(calendar.getTime());
			
			int dateCompare=date.compareTo(nowDate);
			int timeCompare=time.compareTo(nowTime);
			
			if (dateCompare>0) return false;
			if (dateCompare==0 && timeCompare>0) return false;
			return true;
		}
		private String getDeltaDays() {
			if (finished) return "已结束";
			return "还剩"+MyCalendar.getDaysLeft(date)+"天";
		}
	}
	
}
