package com.pkuhelper.media;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.lib.webconnection.WebConnection;
import com.pkuhelper.subactivity.SubActivity;

public class MediaList {
	
	static int requestPage=1;
	static boolean requesting=false;
	static int currPage=1;
	static boolean firstTimeToBottom=true;
	static int nowSid=0;
	
	@SuppressWarnings("unchecked")
	public static void getContent() {
		new RequestingTask("正在获取消息列表...", 
				Constants.domain+"/pkuhelper/media/fetch.php?p=1",
				Constants.REQUEST_MEDIA_FETCH)
				.execute(new ArrayList<Parameters>());
		nowSid=0;
		requesting=false;
	}
	
	@SuppressWarnings("unchecked")
	public static void getContent(int sid) {
		new RequestingTask("正在获取消息列表...", 
				Constants.domain+"/pkuhelper/media/fetch.php?p=1&sid="+sid,
				Constants.REQUEST_MEDIA_FETCH)
				.execute(new ArrayList<Parameters>());
		nowSid=sid;
		requesting=false;
	}
	
	public static void finishRequest(String string) {
		try {
			JSONObject jsonObject=new JSONObject(string);
			int code=jsonObject.getInt("code");
			if (code!=0) throw new Exception();
			JSONArray sources=jsonObject.optJSONArray("sources");
			try {
				int slen=sources.length();
				ArrayList<Parameters> arrayList=new ArrayList<Parameters>();
				for (int i=0;i<slen;i++) {
					JSONObject object=sources.getJSONObject(i);
					arrayList.add(new Parameters(object.getInt("sid")+"", object.getString("name")));
				}
				if (arrayList.size()!=0)
					Source.setSources(arrayList);
			}
			catch (Exception e) {}
			JSONArray jsonArray=jsonObject.getJSONArray("data");
			int len=jsonArray.length();
			MediaActivity.mediaActivity.arrayList.clear();
			SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
			for (int i=0;i<len;i++) {
				JSONObject content=jsonArray.getJSONObject(i);
				String timestamp=content.getString("timestamp");
				String time=simpleDateFormat.format(new Date(Long.parseLong(timestamp)*1000));
				MediaActivity.mediaActivity.arrayList.add(
						new Content(content.getInt("nid"), content.getString("title"), 
								content.getInt("sid"), content.optString("url", ""),
								content.optString("text", "这条通知没有摘要"), time));
			}
			if (len==0) {
				CustomToast.showInfoToast(MediaActivity.mediaActivity, "暂时还没有任何通知", 700);
			}			
			currPage=requestPage=1;
			firstTimeToBottom=true;
			showContent();
		}
		catch (Exception e) {
			MediaActivity.mediaActivity.arrayList.clear();
			CustomToast.showErrorToast(MediaActivity.mediaActivity, "消息获取失败");
		}
	}
	
	public static void showContent() {
		final MediaActivity mediaActivity=MediaActivity.mediaActivity;
		mediaActivity.invalidateOptionsMenu();
		mediaActivity.getActionBar().setTitle("新媒体联盟");
		mediaActivity.setContentView(R.layout.nc_viewcontent_listview);
		
		ListView listView=(ListView)mediaActivity.findViewById(R.id.nc_viewcontent_listview);
		listView.setAdapter(new BaseAdapter() {
			
			@SuppressLint("ViewHolder")
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				Content content=mediaActivity.arrayList.get(position);
				int sid=content.sid;
				Source source=Source.sources.get(sid);
				
				LayoutInflater inflater=mediaActivity.getLayoutInflater();
				convertView=inflater.inflate(R.layout.nc_viewcontent_item, parent, false);
				ViewSetting.setTextView(convertView, R.id.nc_viewcontent_author, source.name);
				ViewSetting.setTextView(convertView, R.id.nc_viewcontent_time, content.time);
				ViewSetting.setTextView(convertView, R.id.nc_viewcontent_title, content.title);
				ViewSetting.setTextView(convertView, R.id.nc_viewcontent_text, content.subscribe);
				//ViewSetting.setImageDrawable(convertView, R.id.nc_viewcontent_image, source.icon);
				convertView.findViewById(R.id.nc_viewcontent_image).setVisibility(View.GONE);
				
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
				return mediaActivity.arrayList.size();
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Content content=mediaActivity.arrayList.get(position);
				String url=content.url;
				Intent intent=new Intent(mediaActivity, SubActivity.class);
				intent.putExtra("type", Constants.SUBACTIVITY_TYPE_WEBVIEW);
				intent.putExtra("url", url);
				intent.putExtra("title", content.title);
				intent.putExtra("content", content.subscribe);
				mediaActivity.startActivity(intent);
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
				int itemLeft=3;
				if (lastItem>=totalItemCount-itemLeft)
					requestMore();
			}
		});
	}
	
	static void requestMore() {
		if (requesting) return;
		requesting=true;
		requestPage=currPage+1;
		Log.i("Media-request-more", requestPage+"");
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String url=Constants.domain+"/pkuhelper/nc/fetch.php?p="+requestPage
						+"&sid="+nowSid;
				EventHandler eventHandler=MediaActivity.mediaActivity.eventHandler;
				Parameters parameters=WebConnection.connect(url, null);
				if (!"200".equals(parameters.name))
					eventHandler.sendMessage(Message.obtain(eventHandler, 
							Constants.MESSAGE_MEDIA_LIST_MORE_FAILED, parameters.name));
				else {
					eventHandler.sendMessage(Message.obtain(eventHandler, 
							Constants.MESSAGE_MEDIA_LIST_MORE_FINISHED, parameters.value));
				}
			}
		}).start();
		
	}
	
	static void finishMoreRequest(String string) {
		ArrayList<Content> arrayList=new ArrayList<Content>();
		try {
			JSONObject jsonObject=new JSONObject(string);
			if (jsonObject.getInt("code")!=0) return;
			JSONArray jsonArray=jsonObject.getJSONArray("data");
			int len=jsonArray.length();
			if (len==0) {
				if (firstTimeToBottom) {
					if (MediaActivity.mediaActivity.arrayList.size()>=10)
						CustomToast.showInfoToast(MediaActivity.mediaActivity, "没有更多了", 700);
					firstTimeToBottom=false;
				}
				return;
			}
			SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
			for (int i=0;i<len;i++) {
				JSONObject content=jsonArray.getJSONObject(i);
				String timestamp=content.getString("timestamp");
				String time=simpleDateFormat.format(new Date(Long.parseLong(timestamp)*1000));
				arrayList.add(
						new Content(content.getInt("nid"), content.getString("title"), 
								content.getInt("sid"), content.optString("url", ""),
								content.optString("text", "这条通知没有摘要"), time));
			}
			MediaActivity.mediaActivity.arrayList.addAll(arrayList);
			BaseAdapter baseAdapter=(BaseAdapter)((ListView)MediaActivity.mediaActivity.findViewById(R.id.nc_viewcontent_listview)).getAdapter();
			baseAdapter.notifyDataSetChanged();
			currPage=requestPage;
			requesting=false;
		}
		catch (Exception e) {
		}
	}
	
}
