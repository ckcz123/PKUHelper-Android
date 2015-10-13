package com.pkuhelper.subactivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;

import com.pkuhelper.R;

public class PhoneView {
	SubActivity subActivity;
	ArrayList<HashMap<String,String>> lists=new ArrayList<HashMap<String,String>>();
	ArrayList<HashMap<String, String>> phones=new ArrayList<HashMap<String,String>>();
	
	public PhoneView(SubActivity _s) {
		subActivity=_s;
		init();
	}
	
	private void init() {
		phones=new ArrayList<HashMap<String,String>>();
		Scanner scanner=new Scanner(TelephoneList.phone);
		while (scanner.hasNext()) {
			String name=scanner.next();
			String phone=scanner.next();
			HashMap<String, String> map=new HashMap<String, String>();
			map.put("name", name);
			map.put("value", phone);
			phones.add(map);
		}
		scanner.close();
	}
	
	public PhoneView showPhoneView() {
		subActivity.setContentView(R.layout.subactivity_phone_view);
		subActivity.getActionBar().setTitle("常用电话");
		lists=new ArrayList<HashMap<String,String>>(phones);
		final ListView listView=(ListView)subActivity.findViewById(R.id.subactivity_phone_list);
		listView.setAdapter(new SimpleAdapter(subActivity,lists, 
				R.layout.subactivity_phone_listitem, new String[] {"name","value"}, 
				new int[] {R.id.subactivity_phone_name,R.id.subactivity_phone_number}));
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final String phonenum=lists.get(position).get("value");
				new AlertDialog.Builder(subActivity).setTitle("拨打此号码？")
				.setCancelable(true).setMessage("你确定要拨打号码 "+phonenum+" 吗？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent=new Intent(Intent.ACTION_DIAL, 
								Uri.parse("tel:"+phonenum));
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						subActivity.startActivity(intent);
					}
				}).setNegativeButton("返回", null).show();
			}
		});
		SearchView searchView=(SearchView)subActivity.findViewById(R.id.subactivity_phone_search);
		AutoCompleteTextView search_text = (AutoCompleteTextView) searchView.findViewById(searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null));
		search_text.setTextSize(14);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String query) {
				return true;
			}
			
			@Override
			public boolean onQueryTextChange(String newText) {
				getQuery(newText);
				((SimpleAdapter)listView.getAdapter()).notifyDataSetChanged();
				return true;
			}
		});
		subActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		return this;
	}
	
	private void getQuery(String text) {
		lists.clear();
		Iterator<HashMap<String, String>> iterator=phones.iterator();
		while (iterator.hasNext()) {
			HashMap<String, String> hashMap=iterator.next();
			if (hashMap.get("name").contains(text)
					|| hashMap.get("value").contains(text))
				lists.add(new HashMap<String, String>(hashMap));
		}
	}
}
