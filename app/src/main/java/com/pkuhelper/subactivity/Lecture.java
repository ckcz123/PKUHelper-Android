package com.pkuhelper.subactivity;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.*;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import com.pkuhelper.R;
import com.pkuhelper.bbs.ViewActivity;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;

public class Lecture {
	SubActivity subActivity;
	ArrayList<HashMap<String,String>> lists=new ArrayList<HashMap<String,String>>();
	ListView listView;
	public Lecture(SubActivity _subActivity) {
		subActivity=_subActivity;
	}
	
	@SuppressLint("InflateParams")
	@SuppressWarnings("unchecked")
	public Lecture showLecture() {
		subActivity.setContentView(R.layout.subactivity_listview);
		subActivity.getActionBar().setTitle("讲座预告");
		subActivity.findViewById(R.id.subactivity_swipeRefreshLayout)
		.setBackgroundColor(Color.parseColor("#34afd3"));
		listView=(ListView)subActivity.findViewById(R.id.subactivity_listview);
		LayoutInflater layoutInflater=subActivity.getLayoutInflater();
		View headerView=layoutInflater.inflate(R.layout.subactivity_listview_headerview, null);
		ViewSetting.setImageResource(headerView, R.id.subactivity_listview_image, R.drawable.jzyg);
		listView.addHeaderView(headerView);
		listView.addFooterView(layoutInflater.inflate(R.layout.subactivity_listview_footerview, null));
		listView.setHeaderDividersEnabled(false);
		listView.setFooterDividersEnabled(false);
		listView.setAdapter(new SimpleAdapter(subActivity, 
				new ArrayList<HashMap<String,String>>(), 
				R.layout.subactivity_listview_item, new String[] {}, new int[] {}));
		new RequestingTask(subActivity,"正在获取讲座信息...", 
				Constants.domain+"/services/pkuhelper/lectures.php", 
				Constants.REQUEST_SUBACTIVITY_LECTURE).execute(new ArrayList<Parameters>());
		return this;
		
	}
	
	
	public void finishRequest(String string) {
		lists=new ArrayList<HashMap<String,String>>();
		try {
			JSONArray jsonArray=new JSONArray(string);
			int len=jsonArray.length();
			for (int i=0;i<len;i++) {
				HashMap<String, String> map=new HashMap<String, String>();
				JSONObject jsonObject=jsonArray.getJSONObject(i);
				String title=jsonObject.getString("title");
				String link=jsonObject.getString("link");
				String description=jsonObject.optString("description");
				String location=getLocation(description);
				String time=getTime(description);
				if (!title.contains("[") && !title.contains("]")
						&& !title.contains("【") && !title.contains("】")
						&& "".equals(time) && "".equals(location))
					continue;
				
				description=description.replaceAll("(\\n){2,100}", "\n");
				description=description.trim();
				if (description.length()>=35)
					description=description.substring(0, 32)+"...";
				map.put("title", title);
				map.put("link", link);
				map.put("time", time);
				map.put("location", location);
				map.put("description", description);
				map.put("id", i+"");
				lists.add(map);
			}
			
			listView.setAdapter(new SimpleAdapter(subActivity, lists, 
					R.layout.subactivity_lecture_listitem, 
					new String[] {"title","description","location","time"}, 
					new int[] {R.id.subactivity_listitem_title,
							R.id.subactivity_listitem_description,
							R.id.subactivity_listitem_location_text,
							R.id.subactivity_listitem_time_text}));
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					try {
						String link=lists.get(position-1).get("link");
						String title=lists.get(position-1).get("title");
						if (link.trim().startsWith("http://www.bdwm.net/bbs/")) {
							int pos=link.lastIndexOf("&threadid");
							if (pos!=-1) {
								int pos2=link.indexOf("=", pos);
								int pos3=link.indexOf("&", pos2);
								if (pos3==-1) pos3=link.length();
								String threadid=link.substring(pos2+1, pos3);
								Intent intent=new Intent(subActivity, ViewActivity.class);
								intent.putExtra("threadid", threadid);
								intent.putExtra("type", "thread");
								intent.putExtra("board", "AcademicInfo");
								subActivity.startActivity(intent);
								return;
							}
						}
						Intent intent=new Intent(subActivity, SubActivity.class);
						intent.putExtra("type", Constants.SUBACTIVITY_TYPE_WEBVIEW);
						intent.putExtra("url", link);
						intent.putExtra("title", title);
						subActivity.startActivity(intent);
					}
					catch (Exception e) {}
				}
			});
		}
		catch (Exception e) {
			Log.i("msg", e.getMessage());
			CustomToast.showErrorToast(subActivity, "讲座加载失败，请重试");
		}
	}
	
	public String getTime(String description) {
		String string=match(description, "时间】");
		if ("".equals(string))
			string=match(description, "时间\\]");
		if ("".equals(string))
			string=match(description, "时间：");
		if ("".equals(string))
			string=match(description, "时间\\:");
		return string;
	}
	
	public String getLocation(String description) {
		String string=match(description, "地点】");
		if ("".equals(string))
			string=match(description, "地点\\]");
		if ("".equals(string))
			string=match(description, "地点：");
		if ("".equals(string))
			string=match(description, "地点\\:");
		if ("".equals(string))
			string=match(description, "地址】");
		if ("".equals(string))
			string=match(description, "地址\\]");
		if ("".equals(string))
			string=match(description, "地址：");
		if ("".equals(string))
			string=match(description, "地址\\:");
		return string;
	}
	
	public String match(String description, String text) {
		Pattern pattern=Pattern.compile(text+"(.*)\\n");
		Matcher matcher=pattern.matcher(description);
		if (matcher.find()) {
			return matcher.group(1).trim();
		}
		return "";
	}

}
