package com.pkuhelper.pkuhole.old;

import java.util.*;

import org.json.*;

import android.os.Message;
import android.view.View;
import android.widget.*;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.*;

public class ShowLists {
	
	static int requestPage=1;
	static boolean requesting=false;
	
	public static void showPage(boolean refreshed) {
		PKUHoleActivity pkuHoleActivity=PKUHoleActivity.pkuHoleActivity;
		pkuHoleActivity.firstTimeToGetBottom=true;
		pkuHoleActivity.listView=new ListView(pkuHoleActivity);
		ListView listView=pkuHoleActivity.listView;
		pkuHoleActivity.setContentView(listView);
		listView.setAdapter(new SimpleAdapter(pkuHoleActivity, 
				pkuHoleActivity.maps, R.layout.pkuhole_show_list, new String[] {"id","time","text"}, 
				new int[] {R.id.pkuhole_list_id,R.id.pkuhole_list_time,R.id.pkuhole_list_text}));
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				PKUHoleActivity pkuHoleActivity=PKUHoleActivity.pkuHoleActivity;
				pkuHoleActivity.lastVisiableItem=pkuHoleActivity.listView.getFirstVisiblePosition();
				ShowComments.showComments(position);
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
		PKUHoleActivity.pkuHoleActivity.getActionBar().setTitle("P大树洞");
		pkuHoleActivity.isShowing=PKUHoleActivity.PAGE_LIST;
		pkuHoleActivity.invalidateOptionsMenu();
		requestPage(refreshed);
	}
	
	@SuppressWarnings("unchecked")
	public static void requestPage(boolean refreshed) {
		if (!refreshed) return;
		PKUHoleActivity.pkuHoleActivity.currPage=0;
		PKUHoleActivity.pkuHoleActivity.maps.clear();
		PKUHoleActivity.pkuHoleActivity.messageList.clear();
		int page=PKUHoleActivity.pkuHoleActivity.currPage+1;
		
		String string="正在获取树洞内容...";
		new RequestingTask(string, 
				Constants.domain+"/services/pkuhelper/pkuhole.php?page="+page, 
				Constants.REQUEST_PKUHOLE_GET_PAGE).execute(new ArrayList<Parameters>());
		//new RequestingTask(string, 
		//				"http://pkuhole.sinaapp.com/PKUhelper/get1.php?n="+page, 
		//				Constants.REQUEST_PKUHOLE_GET_PAGE).execute(new ArrayList<Parameters>());
		requestPage=page;
	}
	
	public static void requestMore() {
		if (requesting) return;
		requesting=true;
		int page=PKUHoleActivity.pkuHoleActivity.currPage+1;
		if (page==31) {
			if (PKUHoleActivity.pkuHoleActivity.firstTimeToGetBottom) {
				CustomToast.showInfoToast(PKUHoleActivity.pkuHoleActivity, "没有更多了", 1000);
				PKUHoleActivity.pkuHoleActivity.firstTimeToGetBottom=false;
			}
			return;
		}
		requestPage=page;
		new Thread(new Runnable() {
			@Override
			public void run() {
				String url=Constants.domain+"/services/pkuhelper/pkuhole.php?page="+requestPage;
				Parameters parameters=WebConnection.connect(url, null);
				EventHandler eventHandler=PKUHoleActivity.pkuHoleActivity.eventHandler;
				if (!"200".equals(parameters.name))
					eventHandler.sendMessage(Message.obtain(eventHandler, 
							Constants.MESSAGE_PKUHOLE_LIST_MORE_FAILED, parameters.name));
				else {
					eventHandler.sendMessage(Message.obtain(eventHandler, 
							Constants.MESSAGE_PKUHOLE_LIST_MORE_FINISHED, parameters.value));
				}
			}
		}).start();
	}
	
	public static void finishRequest(String string) {
		int page=requestPage;
		ArrayList<HoleMessage> arrayList=new ArrayList<HoleMessage>();
		try {
			JSONArray jsonArray=new JSONArray(string);
			int len=jsonArray.length();
			for (int i=0;i<len;i++) {
				JSONObject jsonObject=jsonArray.getJSONObject(i);
				arrayList.add(new HoleMessage(jsonObject.getString("id"),
						jsonObject.getString("StatusId"), jsonObject.getString("time"), 
						jsonObject.getString("text")));
			}
			addMore(page, arrayList);
		}
		catch (Exception e) {
			String msg="树洞获取失败";
			try {
				JSONObject jsonObject=new JSONObject(string);
				msg=jsonObject.optString("msg","树洞获取失败");
			}
			catch (Exception ee) {}
			CustomToast.showErrorToast(PKUHoleActivity.pkuHoleActivity, msg);
			return;
		}
		
	}
	
	public static void addMore(int page, ArrayList<HoleMessage> arrayList) {
		PKUHoleActivity pkuHoleActivity=PKUHoleActivity.pkuHoleActivity;
		pkuHoleActivity.messageList.addAll(arrayList);
		ArrayList<HashMap<String, String>> maps=pkuHoleActivity.maps;
		for (Iterator<HoleMessage> iterator=arrayList.iterator();iterator.hasNext();) {
			HoleMessage holeMessage=iterator.next();
			HashMap<String, String> holeMap=new HashMap<String, String>();
			holeMap.put("id", "#"+holeMessage.id);
			holeMap.put("time", holeMessage.time);
			holeMap.put("text", holeMessage.hint);
			maps.add(holeMap);
		}
		ListView listView=pkuHoleActivity.listView;
		SimpleAdapter simpleAdapter=(SimpleAdapter)listView.getAdapter();
		simpleAdapter.notifyDataSetChanged();
		PKUHoleActivity.pkuHoleActivity.currPage=page;
		requesting=false;
	}
}
