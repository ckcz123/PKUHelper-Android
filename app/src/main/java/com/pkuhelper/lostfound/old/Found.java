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
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.RequestingTask;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.lib.webconnection.WebConnection;

public class Found {
	static boolean requesting;
	static int requestPage;
	
	@SuppressWarnings("unchecked")
	public static void getFoundInfo() {
		new RequestingTask(LostFoundActivity.lostFoundActivity, "正在获取招领信息...",
				Constants.domain+"/services/LFList.php?type=found&page=0",
				Constants.REQUEST_LOSTFOUND_GETFOUND).execute(new ArrayList<Parameters>());
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
				LostFoundInfo lostFoundInfo=new LostFoundInfo(id, item.getString("name"),
						item.getString("lost_or_found"),
						item.getString("type"), item.getString("detail"),
						item.getLong("post_time"), item.getLong("action_time"),
						item.getString("image"), item.getString("poster_uid"),
						item.getString("poster_phone"), item.getString("poster_name"),
						item.getString("poster_college"));
				hashMap.put(id, lostFoundInfo);
				arrayList.add(id);
			}
			lostFoundActivity.foundArray=arrayList;
			lostFoundActivity.foundMap=hashMap;
			requestPage=0;
			requesting=false;
			LostFoundActivity.lostFoundActivity.foundPage=0;
			lostFoundActivity.foundFirstTimeToBottom=true;
			showFoundList();
		}
		catch (Exception e) {
			CustomToast.showErrorToast(LostFoundActivity.lostFoundActivity, "信息获取失败");
		}
	}
	
	public static void showFoundList() {
		LostFoundActivity lostFoundActivity=LostFoundActivity.lostFoundActivity;
		lostFoundActivity.setContentView(R.layout.lostfound_listview);
		lostFoundActivity.getActionBar().setTitle("招领信息");
		lostFoundActivity.nowShowing=LostFoundActivity.PAGE_FOUND;
		lostFoundActivity.invalidateOptionsMenu();
		lostFoundActivity.foundListView=(ListView)lostFoundActivity.findViewById(R.id.lostfound_listview);
		ListView listView=lostFoundActivity.foundListView;
		listView.setAdapter(new BaseAdapter() {
			
			@SuppressLint("ViewHolder")
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				LostFoundActivity lostFoundActivity=LostFoundActivity.lostFoundActivity;
				int id=lostFoundActivity.foundArray.get(position);
				LostFoundInfo lostFoundInfo=lostFoundActivity.foundMap.get(id);
				LayoutInflater layoutInflater=LostFoundActivity.lostFoundActivity.getLayoutInflater();
				convertView=layoutInflater.inflate(R.layout.lostfound_item, parent, false);
				ViewSetting.setTextView(convertView, R.id.lostfound_item_name, lostFoundInfo.name);
				String detail=new String(lostFoundInfo.detail);
				if (detail.length()>=35) detail=detail.substring(0, 33)+"..."; 
				ViewSetting.setTextView(convertView, R.id.lostfound_item_detail, detail);
				SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
				String atimeString="拾到于 "+simpleDateFormat.format(new Date(lostFoundInfo.actiontime*1000));
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
				return LostFoundActivity.lostFoundActivity.foundArray.size();
			}
		});
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (totalItemCount==0) return;
				int lastItem=firstVisibleItem+visibleItemCount;
				int itemLeft=2;
				if (lastItem>=totalItemCount-itemLeft)
					requestMore();
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				LostFoundActivity lostFoundActivity=LostFoundActivity.lostFoundActivity;
				int iid=lostFoundActivity.foundArray.get(position);
				LostFoundInfo lostFoundInfo=lostFoundActivity.foundMap.get(iid);
				Detail.showDetail(lostFoundInfo);
			}
		});
		if (lostFoundActivity.lostArray.size()==0)
			CustomToast.showInfoToast(lostFoundActivity, "暂时没有招领信息");
	}
	
	public static void requestMore() {
		if (requesting) return;
		requesting=true;
		requestPage=LostFoundActivity.lostFoundActivity.foundPage+1;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String url=Constants.domain+"/services/LFList.php?type=found&page="+requestPage;
				EventHandler eventHandler=LostFoundActivity.lostFoundActivity.eventHandler;
				Parameters parameters=WebConnection.connect(url, new ArrayList<Parameters>());
				if (!"200".equals(parameters.name))
					eventHandler.sendMessage(Message.obtain(eventHandler, 
							Constants.MESSAGE_LOSTFOUND_FOUND_MORE_FAILED, parameters.name));
				else {
					eventHandler.sendMessage(Message.obtain(eventHandler, 
							Constants.MESSAGE_LOSTFOUND_FOUND_MORE_FINISHED, parameters.value));
				}
			}
		}).start();
	}
	
	@SuppressLint("UseSparseArrays")
	public static void finishMoreRequest(String string) {
		try {
			LostFoundActivity lostFoundActivity=LostFoundActivity.lostFoundActivity;
			JSONObject jsonObject=new JSONObject(string);
			JSONArray array=jsonObject.getJSONArray("data");
			int len=array.length();
			if (len==0) {
				if (lostFoundActivity.foundFirstTimeToBottom) {
					CustomToast.showInfoToast(lostFoundActivity, "没有更多了");
				}
				lostFoundActivity.foundFirstTimeToBottom=false;
				return;
			}
			
			ArrayList<Integer> arrayList=new ArrayList<Integer>();
			HashMap<Integer, LostFoundInfo> hashMap=new HashMap<Integer, LostFoundInfo>();
			for (int i=0;i<len;i++) {
				JSONObject item=array.getJSONObject(i);
				int id=item.getInt("id");
				LostFoundInfo lostFoundInfo=new LostFoundInfo(id, 
						item.getString("name"),
						item.getString("lost_or_found"),
						item.getString("type"), item.getString("detail"),
						item.getLong("post_time"), item.getLong("action_time"),
						item.getString("image"), item.getString("poster_uid"),
						item.getString("poster_phone"), item.getString("poster_name"),
						item.getString("poster_college"));
				hashMap.put(id, lostFoundInfo);
				arrayList.add(id);
			}
			lostFoundActivity.foundArray.addAll(arrayList);
			lostFoundActivity.foundMap.putAll(hashMap);
			BaseAdapter baseAdapter=(BaseAdapter)lostFoundActivity.foundListView.getAdapter();
			baseAdapter.notifyDataSetChanged();
			LostFoundActivity.lostFoundActivity.foundPage=requestPage;
			requesting=false;
		}
		catch (Exception e) {
		}
	}
	
}
