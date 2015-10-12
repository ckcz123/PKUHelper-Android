package com.pkuhelper.noticecenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.RequestingTask;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.subactivity.SubActivity;

public class NCList {
	
	static boolean hasModified=false;
	
	@SuppressWarnings("unchecked")
	public static void getAllSources() {
		new RequestingTask(NCActivity.ncActivity, "正在获取源信息...",
			Constants.domain+"/pkuhelper/nc/source.php?token="+Constants.token,
				Constants.REQUEST_NOTICECENTER_GETSOURCE)
		.execute(new ArrayList<Parameters>());
	}
	
	public static void finishGetSource(String string) {
		NCActivity ncActivity=NCActivity.ncActivity;
		Log.w("nclist", string);
		try {
			JSONArray jsonArray=new JSONArray(string);
			int len=jsonArray.length();
			HashMap<String, Notice> map=new HashMap<String, Notice>();
			ArrayList<String> arrayList=new ArrayList<String>();
			boolean hasSelected=false;
			for (int i=0;i<len;i++) {
				JSONObject jsonObject=jsonArray.getJSONObject(i);
				String sid=jsonObject.getString("sid");
				map.put(sid, new Notice(sid, jsonObject.getString("name"), 
						jsonObject.getString("icon"), jsonObject.getString("desc"), 
						jsonObject.getString("default"), jsonObject.getString("selected")));
				if ("1".equals(jsonObject.getString("selected")))
					hasSelected=true;
				arrayList.add(sid);
			}
			map.put("0", Notice.courseNotice);
			arrayList.add("0");
			Notice.hasSelected=hasSelected;
			if (!hasSelected) {
				for (Map.Entry<String, Notice> entry: map.entrySet()) {
					Notice notice=entry.getValue();
					if (notice.isDefault)
						notice.wantsToSelect="1";
					else 
						notice.wantsToSelect="0";
				}
			}
			ncActivity.sourceListMap=map;
			Collections.sort(arrayList, new Comparator<String>() {
				@Override
				public int compare(String s1, String s2) {
					Notice notice1=NCActivity.ncActivity.sourceListMap.get(s1);
					Notice notice2=NCActivity.ncActivity.sourceListMap.get(s2);
					boolean x,y;
					x=notice1.isSelected;y=notice2.isSelected;
					if (!Notice.hasSelected) {
						x=notice1.isDefault;y=notice2.isDefault;
					}
					if (x && y)
						return notice1.sid-notice2.sid;
					if (x && !y)
						return -1;
					if (!x && y)
						return 1;
					return notice1.sid-notice2.sid;
				}
			});
			ncActivity.sourceListArray.clear();
			ncActivity.sourceListArray.addAll(arrayList);
			//showList();
			
			NCContent.getNotice();
			
			
			
		}
		catch (Exception e) {
			CustomToast.showErrorToast(ncActivity, "获取源信息失败");
		}		
	}
	
	@SuppressLint("InflateParams")
	public static void showList() {
		NCActivity ncActivity=NCActivity.ncActivity;
		ncActivity.setContentView(R.layout.nc_setsource_listview);
		ncActivity.nowShowing=NCActivity.PAGE_SOURCE;
		ncActivity.invalidateOptionsMenu();
		ncActivity.getActionBar().setTitle("设置订阅的源");
		ncActivity.sourceListView=(ListView)ncActivity.findViewById(R.id.nc_setsource_listview);
		ListView listView=ncActivity.sourceListView;
		View headerView=ncActivity.getLayoutInflater().inflate(R.layout.nc_setsource_headerview, null);
		View footerView=ncActivity.getLayoutInflater().inflate(R.layout.nc_setsource_footerview, null);
		listView.addHeaderView(headerView);
		listView.addFooterView(footerView);
		ViewSetting.setTextView(footerView, R.id.nc_setsource_hint_2, Html.fromHtml("<u>"+Constants.domain+"/pkuhelper/nc/post/</u>"));
		listView.setHeaderDividersEnabled(false);
		listView.setFooterDividersEnabled(false);
		listView.setAdapter(new BaseAdapter() {
			
			@SuppressLint("ViewHolder")
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				NCActivity ncActivity=NCActivity.ncActivity;
				Notice notice=ncActivity.sourceListMap.get(ncActivity.sourceListArray.get(position));
				LayoutInflater inflater=ncActivity.getLayoutInflater();
				convertView=inflater.inflate(R.layout.nc_setsource_item, parent, false);
				ViewSetting.setTextView(convertView, R.id.nc_setsource_name, notice.name);
				ViewSetting.setTextView(convertView, R.id.nc_setsource_desc, notice.desc);
				CheckBox checkBox=(CheckBox)convertView.findViewById(R.id.nc_setsource_chechbox);
				if ("1".equals(notice.wantsToSelect))
					checkBox.setChecked(true);
				else
					checkBox.setChecked(false);
				if (notice.icon!=null)
					ViewSetting.setImageDrawable(convertView, R.id.nc_setsource_image, notice.icon);
				convertView.setTag(notice.sid+"");
				return convertView;
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
				return NCActivity.ncActivity.sourceListArray.size();
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position==parent.getCount()-1) {
					Intent intent=new Intent(NCActivity.ncActivity, SubActivity.class);
					intent.putExtra("url", Constants.domain+"/pkuhelper/nc/post/");
					intent.putExtra("type", Constants.SUBACTIVITY_TYPE_WEBVIEW);
					NCActivity.ncActivity.startActivity(intent);
					return;
				}
				try {
				Notice notice=NCActivity.ncActivity.sourceListMap.get(
						NCActivity.ncActivity.sourceListArray.get(position-1));
				CheckBox checkBox=(CheckBox)view.findViewById(R.id.nc_setsource_chechbox);
				if (checkBox==null) return;
				if (checkBox.isChecked())
					checkBox.setChecked(false);
				else
					checkBox.setChecked(true);
				
				if (checkBox.isChecked()) {
					if (notice.sid==0)
						Notice.courseNotice.wantsToSelect="1";
					else {
						NCActivity.ncActivity.sourceListMap.get(
								NCActivity.ncActivity.sourceListArray.get(position-1)).wantsToSelect="1";
					}
				}
				else  {
					if (notice.sid==0)
						Notice.courseNotice.wantsToSelect="0";
					else {
						NCActivity.ncActivity.sourceListMap.get(
								NCActivity.ncActivity.sourceListArray.get(position-1)).wantsToSelect="0";
					}
				}
				
				hasModified=true;
				}
				catch (Exception e) {}
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public static void saveList() {
		
		Notice.courseNotice.setSelectedFromWanted();
		
		try {
			JSONObject detail=new JSONObject();
			for (Map.Entry<String, Notice> entry:NCActivity.ncActivity.sourceListMap.entrySet()) {
				Notice notice=entry.getValue();
				if (notice.sid!=0)
					detail.put(notice.sid+"", notice.wantsToSelect);
			}
			
			ArrayList<Parameters> arrayList=new ArrayList<Parameters>();
			arrayList.add(new Parameters("uid",Constants.username));
			arrayList.add(new Parameters("source", detail.toString()));
			new RequestingTask(NCActivity.ncActivity, "正在保存...",
					Constants.domain+"/pkuhelper/nc/setSource.php", Constants.REQUEST_NOTICECENTER_SAVESOURCE)
				.execute(arrayList);
		}
		catch (Exception e) {
			CustomToast.showErrorToast(NCActivity.ncActivity, "保存失败，请重试");
		}
	}
	
	public static void realSaveList(String string) {
		hasModified=false;
		try {
			JSONObject jsonObject=new JSONObject(string);
			String code=jsonObject.getString("code");
			if (!"0".equals(code)) {
				new AlertDialog.Builder(NCActivity.ncActivity).setTitle("提示")
				.setMessage(jsonObject.optString("msg", "保存失败，请重试"))
				.setCancelable(true).setPositiveButton("确定", null).show();
				return;
			}
			
			CustomToast.showSuccessToast(NCActivity.ncActivity, "设置成功");
			getAllSources();
			
		}
		catch (Exception e) {
			CustomToast.showErrorToast(NCActivity.ncActivity, "保存失败");
		}
	}
	
	public static void selectSourceToView() {
		HashMap<String, Notice> sourceListMap=NCActivity.ncActivity.sourceListMap;
		final ArrayList<Notice> arrayList=new ArrayList<Notice>();
		ArrayList<String> nameList=new ArrayList<String>();
		for (Map.Entry<String, Notice> entry : sourceListMap.entrySet()) {
			Notice notice=entry.getValue();
			if (notice.sid==0)
				continue;
			if (!notice.isSelected)
				continue;
			arrayList.add(notice);
			nameList.add(notice.name);
		}
		String[] names=new String[nameList.size()];
		for (int i=0;i<names.length;i++)
			names[i]=nameList.get(i);
		new AlertDialog.Builder(NCActivity.ncActivity).setTitle("选择你想要查看的源")
			.setItems(names, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Notice notice=arrayList.get(which);
					String sid=notice.sid+"";
					NCActivity.ncActivity.contentPosition=NCActivity.ncActivity.contentListView.getFirstVisiblePosition();
					NCContent.getNotice(sid);
				}
			}).show();		
	}
	
}
