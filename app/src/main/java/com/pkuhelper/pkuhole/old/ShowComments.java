package com.pkuhelper.pkuhole.old;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.lib.webconnection.WebConnection;

public class ShowComments {
	
	static View headerView;
	static View footerView;
	@SuppressLint("UseSparseArrays")
	static HashMap<Integer, Drawable> drawableMap=new HashMap<Integer, Drawable>();
	
	@SuppressLint("InflateParams")
	public static void showComments(int id) {
		PKUHoleActivity pkuHoleActivity=PKUHoleActivity.pkuHoleActivity;
		pkuHoleActivity.setContentView(R.layout.pkuhole_show_detail_listview);
		pkuHoleActivity.getActionBar().setTitle("查看评论");
		pkuHoleActivity.isShowing=PKUHoleActivity.PAGE_DETAIL;
		pkuHoleActivity.invalidateOptionsMenu();
		ListView listView=(ListView)pkuHoleActivity.findViewById(R.id.pkuhole_detail_listview);
		pkuHoleActivity.commentListView=listView;
		LayoutInflater layoutInflater=pkuHoleActivity.getLayoutInflater();
		headerView=layoutInflater.inflate(R.layout.pkuhole_show_detail_headerview, null);
		footerView=layoutInflater.inflate(R.layout.pkuhole_show_detail_footerview, null);
		listView.addHeaderView(headerView);
		listView.addFooterView(footerView);
		listView.setHeaderDividersEnabled(false);
		listView.setFooterDividersEnabled(false);
		ArrayList<HashMap<String, String>> hashMaps=new ArrayList<HashMap<String,String>>();
		listView.setAdapter(new SimpleAdapter(pkuHoleActivity, hashMaps,
				R.layout.pkuhole_show_detail_item, new String[] {"name"}, new int[] {R.id.pkuhole_comment_id}));		
		HoleMessage holeMessage=pkuHoleActivity.messageList.get(id);
		ViewSetting.setTextView(headerView, R.id.pkuhole_detail_time, holeMessage.time);
		ViewSetting.setTextView(headerView, R.id.pkuhole_detail_id, "#"+holeMessage.id);
		ViewSetting.setTextView(headerView, R.id.pkuhole_detail_text, holeMessage.text);	
		ViewSetting.setTextView(footerView, R.id.pkuhole_detail_total, "获取评论中...");
		
		final String statusId=holeMessage.statusId;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Parameters parameters=WebConnection.connect(
						Constants.domain+"/services/pkuhelper/pkuholeComment.php?statusid="+statusId,null);
				//Parameters parameters=WebConnection.connect(
				//		"http://page.renren.com/ajaxcomment/list?id="+statusId
				//		+"&t=2&pid=601785903&page=1&from=0",null);
				EventHandler eventHandler=PKUHoleActivity.pkuHoleActivity.eventHandler;
				eventHandler.sendMessage(Message.obtain(eventHandler, 
						Constants.REQUEST_PKUHOLE_GET_DETAIL_FINISHED, parameters.value));
			}
		}).start();
		
		pkuHoleActivity.currId=id;
	}
	
	public static void finishRequest(String string) {
		PKUHoleActivity pkuHoleActivity=PKUHoleActivity.pkuHoleActivity;
		try {
			if (string==null || "".equals(string)) throw new Exception();
			JSONObject jsonObject=new JSONObject(string);
			int code=jsonObject.optInt("code");
			if (code!=0) {
				String msg= jsonObject.optString("msg", "树洞解析失败");
				CustomToast.showErrorToast(pkuHoleActivity,msg);
				ViewSetting.setTextView(footerView, 
						R.id.pkuhole_detail_total, msg);
				return;
			}
			
			JSONArray jsonArray=jsonObject.optJSONArray("replyList");
			int len=jsonArray.length();
			final ArrayList<String> imageUrls=new ArrayList<String>();
			final ArrayList<HashMap<String, String>> commentList=new ArrayList<HashMap<String,String>>();
			for (int i=0;i<len;i++) {
				JSONObject comment=jsonArray.optJSONObject(i);
				HashMap<String, String> map=new HashMap<String, String>();
				map.put("name", comment.optString("ubname"));
				map.put("time", comment.optString("replyTime"));
				map.put("text", comment.optString("replyContent"));
				imageUrls.add(comment.optString("replayer_tinyurl"));
				commentList.add(map);
			}
			
			//ListView listView=(ListView)pkuHoleActivity.findViewById(R.id.pkuhole_detail_listview);
			final ListView listView=pkuHoleActivity.commentListView;
			listView.setAdapter(new SimpleAdapter(PKUHoleActivity.pkuHoleActivity, 
					commentList, R.layout.pkuhole_show_detail_item, new String[] {"name","time","text"},
					new int[] {R.id.pkuhole_comment_id,R.id.pkuhole_comment_time,
					R.id.pkuhole_comment_text}));
			listView.setOnScrollListener(new AbsListView.OnScrollListener() {
				
				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
				}
				
				@Override
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
					View fstView=PKUHoleActivity.pkuHoleActivity.commentListView.getChildAt(0);
					if (drawableMap.containsKey(firstVisibleItem)) {
						ViewSetting.setImageDrawable(fstView, R.id.pkuhole_comment_image, drawableMap.get(firstVisibleItem));
					}
					View lstView=PKUHoleActivity.pkuHoleActivity.commentListView.getChildAt(visibleItemCount-1);
					int lstId=firstVisibleItem+visibleItemCount-1;
					if (drawableMap.containsKey(lstId)) {
						ViewSetting.setImageDrawable(lstView, R.id.pkuhole_comment_image, drawableMap.get(lstId));
					}
				}
			});
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					try {
						String name=commentList.get(position-1).get("name");
						CommentMessage.commentMessage(name);
					}
					catch (Exception e) {}
				}
			});
			for (int i=0;i<len;i++) {
				final int j=i;
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						PKUHoleActivity pku=PKUHoleActivity.pkuHoleActivity;
						Drawable drawable=pku.getResources().getDrawable(R.drawable.failure);
						try {
							InputStream inputStream=WebConnection.connect(imageUrls.get(j));
							drawable=Drawable.createFromStream(inputStream, j +".png");
						}
						catch (Exception e) {
						}
						pku.eventHandler.sendMessage(Message.obtain(
								pku.eventHandler, Constants.MESSAGE_PKUHOLE_IMAGE_REQUEST,
								j +1, 0, drawable));
					}
				}).start();
			}
			ViewSetting.setTextView(footerView, 
					R.id.pkuhole_detail_total, "共有"+len+"条评论");
				
		}
		catch (Exception e) {
			
			ViewSetting.setTextView(footerView, 
					R.id.pkuhole_detail_total, "评论获取失败.. 请重试");
		}	
	}
	
	public static void imageRequestFinished(int id, Object object) {
		try {
			Drawable drawable=(Drawable)object;
			ListView listView=PKUHoleActivity.pkuHoleActivity.commentListView;
			drawableMap.put(id, drawable);
			ViewSetting.setImageDrawable(listView.getChildAt(id-listView.getFirstVisiblePosition()
					), R.id.pkuhole_comment_image, drawable);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
