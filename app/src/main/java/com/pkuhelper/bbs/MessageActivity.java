package com.pkuhelper.bbs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.MyCalendar;
import com.pkuhelper.lib.Util;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class MessageActivity extends Activity {
	static ArrayList<MessageInfo> messageInfos=new ArrayList<MessageInfo>();
	private static int PAGE_LIST=1;
	private static int PAGE_DETAIL=2;
	static MessageInfo tmpInfo=null;
	static String content="";
	static String originalText="";
	int showingPage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.getOverflowMenu(this);
		if ("".equals(Userinfo.token)) {
			CustomToast.showInfoToast(this, "请先登录！");
			finish();
			return;
		}
		messageInfos.clear();
		requestList();
	}
	
	@SuppressWarnings("unchecked")
	void requestList() {
		showList();
		new RequestingTask(this, "正在获取信件列表", 
				"http://www.bdwm.net/client/bbsclient.php?type=getmaillist&token="+Userinfo.token, 
				Constants.REQUEST_BBS_GET_MAIL_LIST).execute(new ArrayList<Parameters>());
	}
	
	@SuppressWarnings("unchecked")
	void requestDetail() {
		new RequestingTask(this, "正在获取内容...", 
				"http://www.bdwm.net/client/bbsclient.php?type=getmail&token="+Userinfo.token
				+"&timestamp="+tmpInfo.timestamp, Constants.REQUEST_BBS_GET_MAIL).execute(new ArrayList<Parameters>());
	}
	
	void finishDetail(String string) {
		try {
			JSONObject jsonObject=new JSONObject(string);
			int code=jsonObject.getInt("code");
			if (code!=0) {
				CustomToast.showErrorToast(this, jsonObject.optString("msg", "获取失败"), 1500);
				return;
			}
			originalText=jsonObject.optString("content");
			content=translate(originalText);
			showDetail();
		}
		catch (Exception e) {CustomToast.showErrorToast(this, "获取失败", 1500);}
	}
	
	String translate(String content) {
		String string=new String(content);
		string=string.replace("<pre>\n", "");
		string=string.replace("</pre>\n", "");
		string=string.replace("\n", "<br />");
		string=string.replace("<span class=col30>", "<font color='#000000'>");
		string=string.replace("<span class=col31>", "<font color='#800000'>");
		string=string.replace("<span class=col32>", "<font color='#008000'>");
		string=string.replace("<span class=col33>", "<font color='#808000'>");
		string=string.replace("<span class=col34>", "<font color='#000080'>");
		string=string.replace("<span class=col35>", "<font color='#800080'>");
		string=string.replace("<span class=col36>", "<font color='#008080'>");
		string=string.replace("<span class=col37>", "<font color='#808080'>");
		string=string.replace("</span>", "</font>");
		//string=string.replace("使用WWW方式可以查看附件", "<b><u><font color='#800000'>长按此楼以查看附件</font></u></b>");
		return string;
	}
	
	void showDetail() {
		setContentView(R.layout.bbs_message_detail);
		getActionBar().setTitle(tmpInfo.title);
		showingPage=PAGE_DETAIL;
		invalidateOptionsMenu();
		ViewSetting.setTextView(this, R.id.bbs_message_detail, Html.fromHtml(content));
	}
	
	void showList() {
		setContentView(R.layout.bbs_message_listview);
		showingPage=PAGE_LIST;
		getActionBar().setTitle("站内信");
		invalidateOptionsMenu();
		ListView listView=(ListView)findViewById(R.id.bbs_message_listview);
		listView.setAdapter(new BaseAdapter() {
			
			@SuppressLint("ViewHolder")
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				convertView=getLayoutInflater().inflate(R.layout.bbs_message_item, 
						parent, false);
				MessageInfo messageInfo=messageInfos.get(position);
				ViewSetting.setTextView(convertView, R.id.bbs_message_item_title, messageInfo.title);
				ViewSetting.setTextView(convertView, R.id.bbs_message_item_author, messageInfo.author);
				ViewSetting.setTextView(convertView, R.id.bbs_message_item_time, MyCalendar.format(1000*messageInfo.timestamp));
				
				if (messageInfo.isnew)
					convertView.setBackgroundColor(Color.parseColor("#FFEC8B"));
				else convertView.setBackgroundColor(Color.WHITE);				
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
				return messageInfos.size();
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				messageInfos.get(position).isnew=false;
				tmpInfo=messageInfos.get(position);
				requestDetail();
			}
		});
	}
	
	void finishRequest(int type, String string) {
		if (type==Constants.REQUEST_BBS_GET_MAIL_LIST)
			fillList(string);
		if (type==Constants.REQUEST_BBS_GET_MAIL)
			finishDetail(string);
	}
	
	void fillList(String string) {
		try {
			messageInfos.clear();
			JSONObject jsonObject=new JSONObject(string);
			int code=jsonObject.getInt("code");
			if (code!=0) {
				CustomToast.showErrorToast(this, jsonObject.optString("msg", "获取失败"), 1500);
				return;
			}
			JSONArray jsonArray=jsonObject.getJSONArray("datas");
			int len=jsonArray.length();
			for (int i=0;i<len;i++) {
				JSONObject object=jsonArray.getJSONObject(i);
				messageInfos.add(new MessageInfo(object.getInt("number"),
						object.optString("title"), object.optString("author"),
						object.getLong("timestamp"), object.optInt("new")));
			}
			
			Collections.sort(messageInfos, new Comparator<MessageInfo>() {
				@Override
				public int compare(MessageInfo lhs, MessageInfo rhs) {
					return (int)(rhs.timestamp-lhs.timestamp);
				}
			});
			
			ListView listView=(ListView)findViewById(R.id.bbs_message_listview);
			if (listView==null) return;
			BaseAdapter baseAdapter=(BaseAdapter)listView.getAdapter();
			baseAdapter.notifyDataSetChanged();
			
		}
		catch (Exception e) {
			CustomToast.showErrorToast(this, "获取失败", 1500);
			messageInfos.clear();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Util.setIconEnable(menu, true);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (showingPage==PAGE_LIST) {
			menu.add(Menu.NONE, Constants.MENU_BBS_MESSAGE_POST, Constants.MENU_BBS_MESSAGE_POST, "")
			.setIcon(R.drawable.add).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		else if (showingPage==PAGE_DETAIL) {
			menu.add(Menu.NONE, Constants.MENU_BBS_MESSAGE_POST, Constants.MENU_BBS_MESSAGE_POST, "")
			.setIcon(R.drawable.reply).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id==Constants.MENU_BBS_MESSAGE_POST) {
			if (showingPage==PAGE_LIST) {
				Intent intent=new Intent(this, MessagePostActivity.class);
				intent.putExtra("author", "");
				startActivityForResult(intent, 1);
			}
			else if (showingPage==PAGE_DETAIL) {
				Intent intent=new Intent(this, MessagePostActivity.class);
				intent.putExtra("author", tmpInfo.author);
				intent.putExtra("title", "Re: "+tmpInfo.title);
				intent.putExtra("content", originalText);
				intent.putExtra("number", tmpInfo.number+"");
				startActivityForResult(intent, 2);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode==KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN) {
			if (showingPage==PAGE_DETAIL) {
				showList();
			}
			else finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode!=RESULT_OK) return;
		requestList();
	}
	
}
