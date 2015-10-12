package com.pkuhelper;

import java.util.ArrayList;

import com.pkuhelper.bbs.BBSActivity;
import com.pkuhelper.classroom.ClassActivity;
import com.pkuhelper.lib.*;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lostfound.old.LostFoundActivity;
import com.pkuhelper.media.MediaActivity;
import com.pkuhelper.noticecenter.NCActivity;
import com.pkuhelper.pkuhole.HoleActivity;
import com.pkuhelper.pkuhole.old.PKUHoleActivity;
import com.pkuhelper.subactivity.SubActivity;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;


public class MYPKU_old extends Fragment {
	public final static String[][] pkuLists={
		{"tzzx","通知中心",R.drawable.tzzx+"","#f9949b","#f98585"}
		,{"cjcx","成绩查询",R.drawable.cjcx+"","#b1b1b1","#a3a3a3"}
		,{"xmtlm","新媒体联盟",R.drawable.xmtlm+"","#f19ec2","#ee86b5"}
		,{"wmbbs","未名bbs",R.drawable.wmbbs+"","#4887d9","3c6eb0"}
		,{"jscx","教室查询",R.drawable.jscx+"","#fde278","#f7d742"}
		,{"pkuhole","P大树洞",R.drawable.pkuhole+"","#bdd600","#acc300"}
		,{"pdsd","P大树洞",R.drawable.pdsd+"","#47e6b0","#32d79f"}
		,{"tccj","体测成绩",R.drawable.tccj+"","#d496fd","#cd89fd"}
		,{"tydk","体育打卡",R.drawable.tydk+"","#84b1fb","#76a8fa"}
		,{"jzyg","讲座预告",R.drawable.jzyg+"","#47c3e7","#34afd3"}
		,{"bjyc","百讲演出",R.drawable.bjyc+"","#fcbf83","#feb772"}
		,{"pkumail","PKU邮箱",R.drawable.pkumail+"","#b8e078","#aed66e"}
		,{"lostfound","失物招领",R.drawable.lostfound+"","#e88bb5","#e279a1"}
	};
	
	static final MYPKU_old mypku=new MYPKU_old();
	static ListView listView;
	
	static Drawable[] drawables=null;
	static String[][] wants=null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.mypku_listview,
				container, false);
		listView=(ListView)rootView.findViewById(R.id.mypku_listview);
		setWanted();
		showList();
		return rootView;
	}
	
	private static void setWanted() {
		String string=Editor.getString(PKUHelper.pkuhelper, "MYPKU-notwants", "");
		
		ArrayList<String[]> arrayList=new ArrayList<String[]>();
		int len=pkuLists.length;
		for (int i=0;i<len;i++) {
			String[] one=pkuLists[i];
			if (!string.contains(one[0])) {
				arrayList.add(one);
			}
		}
		
		int size=arrayList.size();
		if (size==0) {
			wants=pkuLists;
			return;
		}
		wants=new String[size][];
		for (int i=0;i<size;i++)
			wants[i]=arrayList.get(i);
		
	}
	
	public static void showList() {
		listView.setAdapter(new BaseAdapter() {
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				LayoutInflater mInflater=PKUHelper.pkuhelper.getLayoutInflater();
				String name;
				int color;
				Drawable drawable;
				// new features
				if (position>=wants.length) {
					int offset=position-wants.length;
					Features feature=Constants.features.get(offset);
					name=feature.title;
					try {
					color=Color.parseColor(feature.color);
					}
					catch (Exception e) {color=Util.generateColorInt();}
					drawable=MyDrawable.getDrawable(feature.drawable, feature.darkcolor);
				}
				
				else {
					String[] strings=wants[position];
					name=strings[1];
					color=Color.parseColor(strings[3]);
					drawable=PKUHelper.pkuhelper.getResources().getDrawable(Integer.parseInt(strings[2]));
				}
				
				if (position%2==0) {
					convertView=mInflater.inflate(R.layout.mypku_list_odd_view, parent,false);
					convertView.setBackgroundColor(color);
					ViewSetting.setImageDrawable(convertView, R.id.image_odd_view, drawable);
					ViewSetting.setTextView(convertView, R.id.image_odd_name, name);
				}
				else {
					convertView=mInflater.inflate(R.layout.mypku_list_even_view, parent,false);
					convertView.setBackgroundColor(color);
					ViewSetting.setImageDrawable(convertView, R.id.image_even_view, drawable);
					ViewSetting.setTextView(convertView, R.id.image_even_name, name);
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
				return wants.length+Constants.features.size();
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position>=wants.length) {
					int offset=position-wants.length;
					ArrayList<Features> arrayList=Constants.features;
					String url=arrayList.get(offset).url;
					Intent intent=new Intent(PKUHelper.pkuhelper, SubActivity.class);
					intent.putExtra("type", Constants.SUBACTIVITY_TYPE_WEBVIEW);
					intent.putExtra("url", url);
					intent.putExtra("title", arrayList.get(offset).title);
					intent.putExtra("post", "user_token="+Constants.user_token);
					PKUHelper.pkuhelper.startActivity(intent);
					return;
				}
				
				String string=wants[position][0];
				if ("tzzx".equals(string))
					PKUHelper.pkuhelper.startActivity(
						new Intent(PKUHelper.pkuhelper, NCActivity.class));
				else if ("xmtlm".equals(string)) 
					PKUHelper.pkuhelper.startActivity(
							new Intent(PKUHelper.pkuhelper, MediaActivity.class));
				else if ("cjcx".equals(string))
					Dean.getSessionId(Dean.FLAG_GETTING_GRADE);
				else if ("jscx".equals(string))
					PKUHelper.pkuhelper.startActivity(
							new Intent(PKUHelper.pkuhelper, ClassActivity.class));
				else if ("jzyg".equals(string)) {
					Intent intent=new Intent();
					intent.setClass(PKUHelper.pkuhelper, SubActivity.class);
					intent.putExtra("type", Constants.SUBACTIVITY_TYPE_LECTURE);
					PKUHelper.pkuhelper.startActivity(intent);
				}
				else if ("bjyc".equals(string)) {
					Intent intent=new Intent();
					intent.setClass(PKUHelper.pkuhelper, SubActivity.class);
					intent.putExtra("type", Constants.SUBACTIVITY_TYPE_SHOWS);
					PKUHelper.pkuhelper.startActivity(intent);
				}
				else if (("tccj").equals(string))
					PE.getPeTestScore();
				else if (("tydk").equals(string))
					PE.peCard();
				else if ("wmbbs".equals(string))
					PKUHelper.pkuhelper.startActivity(
							new Intent(PKUHelper.pkuhelper, BBSActivity.class));
				else if ("pkuhole".equals(string))
					PKUHelper.pkuhelper.startActivity(
							new Intent(PKUHelper.pkuhelper, HoleActivity.class));
				else if (("pdsd").equals(string))
					PKUHelper.pkuhelper.startActivity(
							new Intent(PKUHelper.pkuhelper, PKUHoleActivity.class));
				else if ("pkumail".equals(string)) {
					Intent intent=new Intent();
					intent.setClass(PKUHelper.pkuhelper, SubActivity.class);
					intent.putExtra("type", Constants.SUBACTIVITY_TYPE_WEBVIEW_PKUMAIL);
					PKUHelper.pkuhelper.startActivity(intent);
				}
				else if ("lostfound".equals(string)) {
					if (!Constants.isValidLogin()) {
						CustomToast.showInfoToast(PKUHelper.pkuhelper, "请先进行有效登录！");
						return;
					}
					PKUHelper.pkuhelper.startActivity(
							new Intent(PKUHelper.pkuhelper, LostFoundActivity.class));
				}
		    }
		});
	}
}
