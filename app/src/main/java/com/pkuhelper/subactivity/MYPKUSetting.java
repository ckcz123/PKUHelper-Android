package com.pkuhelper.subactivity;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

import com.pkuhelper.MYPKU;
import com.pkuhelper.R;
import com.pkuhelper.lib.Editor;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;

public class MYPKUSetting {
	SubActivity subActivity;
	boolean hasModified;
	ArrayList<HashMap<String, String>> lists=new ArrayList<HashMap<String,String>>();
	public MYPKUSetting(SubActivity _subActivity) {
		subActivity=_subActivity;
	}
	@SuppressLint("InflateParams")
	public MYPKUSetting set() {
		getList();
		hasModified=false;
		subActivity.setContentView(R.layout.settings_mypku_listview);
		subActivity.getActionBar().setTitle("我的PKU设置");
		View headerView=subActivity.getLayoutInflater().inflate(R.layout.settings_mypku_headerview, null);
		ListView listView=(ListView)subActivity.findViewById(R.id.settings_mypku_listview);
		listView.addHeaderView(headerView);
		listView.setHeaderDividersEnabled(false);
		
		listView.setAdapter(new BaseAdapter() {
			
			@SuppressLint("ViewHolder")
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				convertView=subActivity.getLayoutInflater().inflate(R.layout.settings_mypku_item, parent, false);
				HashMap<String , String> hashMap=lists.get(position);
				ViewSetting.setTextView(convertView, R.id.settings_mypku_item_name, hashMap.get("title"));
				CheckBox checkBox=(CheckBox)convertView.findViewById(R.id.settings_mypku_item_checkbox);
				checkBox.setChecked("1".equals(hashMap.get("select")));
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
				return lists.size();
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position==0) return;
				position--;
				HashMap<String, String> hashMap=lists.get(position);
				CheckBox checkBox=(CheckBox)view.findViewById(R.id.settings_mypku_item_checkbox);
				if (checkBox.isChecked()) {
					checkBox.setChecked(false);
					hashMap.put("select", "0");
				}
				else {
					checkBox.setChecked(true);
					hashMap.put("select", "1");
				}
				lists.set(position, hashMap);
				hasModified=true;
			}
			
		});
		return this;
	}
	
	public void getList() {
		lists=new ArrayList<HashMap<String,String>>();
		String string=Editor.getString(subActivity, "mypku_notwants", "");
		getList(MYPKU.publics, string);
		getList(MYPKU.selfs, string);
		getList(MYPKU.communities, string);
	}
	
	private void getList(String[][] strings, String string) {	
		for (int i=0;i<strings.length;i++) {
			String[] one=strings[i];
			String name=one[0];
			String title=one[1];			
			HashMap<String, String> hashMap=new HashMap<String, String>();
			hashMap.put("name", name);
			hashMap.put("title", title);
			hashMap.put("select", string.contains(name)?"0":"1");
			lists.add(hashMap);
		}
	}
	
	public void save(final boolean exit) {
		String string="";
		int size=lists.size();
		int cnt=0;
		for (int i=0;i<size;i++) {
			HashMap<String, String> hashMap=lists.get(i);
			String name=hashMap.get("name");
			if ("0".equals(hashMap.get("select"))) {
				string+=name+",";			
				cnt++;
			}
		}
		if (cnt==size) {
			CustomToast.showInfoToast(subActivity, "至少需要选择一项！");
			return;
		}
		if (string.length()!=0)
			string=string.substring(0, string.length()-1);
		
		Editor.putString(subActivity, "mypku_notwants", string);
		hasModified=false;
		
		new AlertDialog.Builder(subActivity).setTitle("保存成功！")
		.setMessage("退出并重进软件后方可生效").setPositiveButton("确认", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (exit) subActivity.wantToExit();
			}
		}).setCancelable(true).setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				if (exit) subActivity.wantToExit();
			}
		}).show();
		
	}
	
}
