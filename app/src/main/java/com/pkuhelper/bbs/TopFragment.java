package com.pkuhelper.bbs;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.MyCalendar;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class TopFragment extends Fragment {	
	public static TopFragment topFragment;
	public static ArrayList<ThreadInfo> tops=new ArrayList<ThreadInfo>();
	static View topView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView=inflater.inflate(R.layout.bbs_top_listview, container, false);
		topFragment=this;
		topView=rootView;
		realShowView(true);
		return rootView;
	}
	
	@SuppressWarnings("unchecked")
	public static void showView() {
		new RequestingTask(BBSActivity.bbsActivity, "正在获取热门帖子...", "http://www.bdwm.net/client/bbsclient.php?type=gettop&number=30", 
				Constants.REQUEST_BBS_GET_TOP).execute(new ArrayList<Parameters>());
	}
	
	public static void finishRequest(String string) {
		tops=new ArrayList<ThreadInfo>();
		try {
			JSONArray jsonArray=new JSONArray(string);
			int len=jsonArray.length();
			for (int i=0;i<len;i++) {
				JSONObject jsonObject=jsonArray.getJSONObject(i);
				try {
					tops.add(new ThreadInfo(jsonObject.getInt("rank"), 
						jsonObject.getString("boardName"), jsonObject.getString("boardDescription"), 
						jsonObject.optString("author"), jsonObject.getString("date"), 
						jsonObject.optString("title"), jsonObject.getInt("threadid")));
				}
				catch (Exception e) {}
			}
		}
		catch (Exception e) {
			tops=new ArrayList<ThreadInfo>();
		}
		finally {
			if (tops.size()==0) {
				CustomToast.showInfoToast(BBSActivity.bbsActivity, "暂时没有热门帖子，请刷新重试", 1500);
			}
			realShowView(false);
		}
	}
	
	public static void realShowView(boolean refresh) {
		if (tops.size()==0 && refresh) {
			showView();
			return;
		}
		
		ListView listView=(ListView)topView.findViewById(R.id.bbs_top_listview);
		listView.setAdapter(new BaseAdapter() {
			@Override
			@SuppressLint("ViewHolder")
			public View getView(int position, View convertView, ViewGroup parent) {
				convertView=BBSActivity.bbsActivity.getLayoutInflater().inflate(R.layout.bbs_top_listitem, 
						parent, false);
				ThreadInfo threadInfo=tops.get(position);
				ViewSetting.setTextView(convertView, R.id.bbs_top_item_from, threadInfo.board+" / "+threadInfo.boardName);
				ViewSetting.setTextView(convertView, R.id.bbs_top_item_rank, "#"+threadInfo.rank);
				ViewSetting.setTextView(convertView, R.id.bbs_top_item_title, threadInfo.title);
				ViewSetting.setTextView(convertView, R.id.bbs_top_item_author, threadInfo.author);
				ViewSetting.setTextView(convertView, R.id.bbs_top_item_time, 
						MyCalendar.format(threadInfo.time));
				return convertView;
			}
			
			@Override
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public Object getItem(int position) {
				return null;
			}
			
			@Override
			public int getCount() {
				return tops.size();
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ThreadInfo threadInfo=tops.get(position);
				Intent intent=new Intent(BBSActivity.bbsActivity, ViewActivity.class);
				intent.putExtra("board", threadInfo.board);
				intent.putExtra("threadid", threadInfo.threadid+"");
				intent.putExtra("type", "thread");
				BBSActivity.bbsActivity.startActivity(intent);
			}
		});
		
	}
	
}
