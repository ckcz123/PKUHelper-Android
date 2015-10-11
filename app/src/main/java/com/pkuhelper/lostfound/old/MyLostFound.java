package com.pkuhelper.lostfound.old;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;

public class MyLostFound {
	@SuppressWarnings("unchecked")
	public static void getMyInfo() {
		if (!Constants.isValidLogin()) {
			CustomToast.showInfoToast(LostFoundActivity.lostFoundActivity, 
					"请先于主界面登录后，再发布失物/招领信息");
			return;
		}
		
		new RequestingTask("正在获取我发布的失物招领信息...", 
				Constants.domain+"/services/LFList.php?token="+Constants.token,
				Constants.REQUEST_LOSTFOUND_GETMINE).execute(new ArrayList<Parameters>());
	}
	
	@SuppressLint("UseSparseArrays")
	public static void finishRequest(String string) {
		try {
			LostFoundActivity lostFoundActivity=LostFoundActivity.lostFoundActivity;
			JSONObject jsonObject=new JSONObject(string);
			JSONArray array=jsonObject.getJSONArray("data");
			int len=array.length();
			ArrayList<Integer> arrayList=new ArrayList<Integer>();
			HashMap<Integer, LostFoundInfo> hashMap=new HashMap<Integer, LostFoundInfo>();
			for (int i=0;i<len;i++) {
				JSONObject item=array.getJSONObject(i);
				int id=item.getInt("id");
				LostFoundInfo lostFoundInfo=new LostFoundInfo(id,item.getString("name"),
						item.getString("lost_or_found"),
						item.getString("type"), item.getString("detail"),
						item.getLong("post_time"), item.getLong("action_time"),
						item.getString("image"), item.getString("poster_uid"),
						item.getString("poster_phone"), item.getString("poster_name"),
						item.getString("poster_college"));
				hashMap.put(id, lostFoundInfo);
				arrayList.add(id);
			}
			lostFoundActivity.myArray=arrayList;
			lostFoundActivity.myMap=hashMap;
			showMyList();
		}
		catch (Exception e) {
			CustomToast.showErrorToast(LostFoundActivity.lostFoundActivity, "信息获取失败");
		}
	}
	
	public static void showMyList() {
		LostFoundActivity lostFoundActivity=LostFoundActivity.lostFoundActivity;
		lostFoundActivity.setContentView(R.layout.lostfound_listview);
		lostFoundActivity.getActionBar().setTitle("我的失物招领");
		lostFoundActivity.nowShowing=LostFoundActivity.PAGE_MINE;
		lostFoundActivity.invalidateOptionsMenu();
		lostFoundActivity.myListView=(ListView)lostFoundActivity.findViewById(R.id.lostfound_listview);
		ListView listView=lostFoundActivity.myListView;
		listView.setAdapter(new BaseAdapter() {
			
			@SuppressLint("ViewHolder")
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				LostFoundActivity lostFoundActivity=LostFoundActivity.lostFoundActivity;
				int id=lostFoundActivity.myArray.get(position);
				LostFoundInfo lostFoundInfo=lostFoundActivity.myMap.get(id);
				LayoutInflater layoutInflater=LostFoundActivity.lostFoundActivity.getLayoutInflater();
				convertView=layoutInflater.inflate(R.layout.lostfound_item, parent, false);
				ViewSetting.setTextView(convertView, R.id.lostfound_item_name, lostFoundInfo.name);
				String detail=new String(lostFoundInfo.detail);
				if (detail.length()>=35) detail=detail.substring(0, 33)+"..."; 
				ViewSetting.setTextView(convertView, R.id.lostfound_item_detail, detail);
				SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
				String lost_or_found="丢失于 ";
				if (lostFoundInfo.lost_or_found==LostFoundInfo.FOUND) lost_or_found="拾到于 ";
				String atimeString=lost_or_found+simpleDateFormat.format(new Date(lostFoundInfo.actiontime*1000));
				String ptimeString="发布于 "+simpleDateFormat.format(new Date(lostFoundInfo.posttime*1000));
				ViewSetting.setTextView(convertView, R.id.lostfound_item_posttime, ptimeString);
				ViewSetting.setTextView(convertView, R.id.lostfound_item_actiontime, atimeString);
				Drawable drawable=Image.getImage(lostFoundInfo.thumbImgUrl);
				if (drawable!=null)
					ViewSetting.setImageDrawable(convertView, R.id.lostfound_item_image, drawable);
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
				return LostFoundActivity.lostFoundActivity.myArray.size();
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				LostFoundActivity lostFoundActivity=LostFoundActivity.lostFoundActivity;
				int iid=lostFoundActivity.myArray.get(position);
				LostFoundInfo lostFoundInfo=lostFoundActivity.myMap.get(iid);
				Detail.showDetail(lostFoundInfo);
			}
		});
		if (lostFoundActivity.myArray.size()==0)
			CustomToast.showInfoToast(lostFoundActivity, "你没有发布的失物/招领信息");
	}
	
}
