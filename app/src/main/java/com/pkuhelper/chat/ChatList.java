package com.pkuhelper.chat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Dialog;
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

public class ChatList {
	static Dialog dialog;
	static int index=0;
	
	@SuppressWarnings("unchecked")
	static void showChatList() {
		ChatActivity.chatActivity.getActionBar().setTitle("消息中心");
		ArrayList<Parameters> arrayList=new ArrayList<Parameters>();
		//arrayList.add(new Parameters("uid", Constants.username));
		//String timestamp=System.currentTimeMillis()/1000+"";
		//String hash=Util.getHash(Constants.username+timestamp+"I2V587");
		//arrayList.add(new Parameters("timestamp", timestamp));
		//arrayList.add(new Parameters("hash", hash));
		arrayList.add(new Parameters("token", Constants.token));
		arrayList.add(new Parameters("type", "getlist"));
		new RequestingTask("正在获取聊天列表...", Constants.domain+"/services/msg.php", Constants.REQUEST_CHAT_GET_LIST)
			.execute(arrayList);
	}
	
	static void finishGetList(String string) {
		final ChatActivity chatActivity=ChatActivity.chatActivity;
		try {
			JSONObject jsonObject=new JSONObject(string);
			int code=jsonObject.getInt("code");
			if (code!=0) {
				CustomToast.showErrorToast(chatActivity, jsonObject.optString("msg", "列表解析失败"));
				return;
			}
			JSONArray datas=jsonObject.getJSONArray("data");
			chatActivity.chatListInfos=new ArrayList<ChatListInfo>();
			int len=datas.length();
			for (int i=0;i<len;i++) {
				JSONObject object=datas.getJSONObject(i);
				String username=object.optString("username");
				long time=object.optLong("timestamp");
				String content=object.optString("content");
				int num=object.optInt("number");
				int hasNew=object.optInt("hasNew");
				String name=object.optString("name", username);
				chatActivity.chatListInfos.add(new ChatListInfo(username, name, time, content, num, hasNew>0));
			}
			chatActivity.blackList=new ArrayList<String>();
			JSONArray blacks=jsonObject.getJSONArray("blacks");
			int blen=blacks.length();
			for (int i=0;i<blen;i++) {
				String blid=blacks.optString(i, "");
				if (!"".equals(blid)) chatActivity.blackList.add(blid);
			}
			if (len==0) {
				CustomToast.showInfoToast(chatActivity, "暂无消息");
			}
			
			
		}
		catch (Exception e) {
			chatActivity.chatListInfos=new ArrayList<ChatListInfo>();
			CustomToast.showErrorToast(chatActivity, "列表解析失败", 1000);
		}
		finally {
			index=0;
			chatActivity.hasModified=false;
			realShowList();
		}
		
	}
	
	static void realShowList() {
		final ChatActivity chatActivity=ChatActivity.chatActivity;
		chatActivity.setContentView(R.layout.chatlist_listview);
		
		chatActivity.pageShowing=ChatActivity.PAGE_LIST;
		chatActivity.invalidateOptionsMenu();
		if (chatActivity.hasModified) {
			showChatList();
			return;
		}
		ChatActivity.toUid="";
		
		if (chatActivity.chatThread.getrealstopped()) {
			chatActivity.chatThread=new ChatThread(Constants.username);
		}
		chatActivity.chatThread.start();
		
		chatActivity.getActionBar().setTitle("消息中心");
		Collections.sort(chatActivity.chatListInfos, new Comparator<ChatListInfo>() {
			@Override
			public int compare(ChatListInfo lhs, ChatListInfo rhs) {
				return (int)(rhs.timestamp-lhs.timestamp);
			}
		});
		ListView listView=(ListView)chatActivity.findViewById(R.id.chatlist_listview);
		listView.setAdapter(new BaseAdapter() {
			
			@SuppressLint("ViewHolder")
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				convertView=chatActivity.getLayoutInflater().inflate(R.layout.chatlist_listitem, parent, false);
				ChatListInfo info=chatActivity.chatListInfos.get(position);
				String time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", 
						Locale.getDefault()).format(new Date(info.timestamp*1000));
				ViewSetting.setTextView(convertView, R.id.chatlist_author, info.name);
				ViewSetting.setTextView(convertView, R.id.chatlist_number, "("+info.number+")");
				ViewSetting.setTextView(convertView, R.id.chatlist_time, time);
				ViewSetting.setTextView(convertView, R.id.chatlist_content, info.content);
				
				if (info.hasNew) {
					ViewSetting.setTextView(convertView, R.id.chatlist_hasnew, "new");
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
				return chatActivity.chatListInfos.size();
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				chatActivity.chatListInfos.get(position).hasNew=false;
				ViewSetting.setTextView(view, R.id.chatlist_hasnew, "");
				ChatListInfo info=chatActivity.chatListInfos.get(position);
				index=parent.getFirstVisiblePosition();
				ChatDetail.showDetail(info.username, info.name);
			}
		});
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				chatActivity.tmpUid=chatActivity.chatListInfos.get(position).username;				
				return false;
			}
		});
		chatActivity.registerForContextMenu(listView);
		listView.setSelection(index);
	}
	
	public static void addChat() {
		dialog=new Dialog(ChatActivity.chatActivity);
		dialog.setTitle("发送新消息");
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setContentView(R.layout.chat_add_dialog);
		
		ViewSetting.setOnClickListener(dialog, R.id.chat_send, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String uid=ViewSetting.getEditTextValue(dialog, R.id.chat_send_uid);
				if ("".equals(uid)) {
					CustomToast.showInfoToast(ChatActivity.chatActivity, "ID不能为空！");
					return;
				}
				String content=ViewSetting.getEditTextValue(dialog, R.id.chat_send_content);
				if ("".equals(content)) {
					CustomToast.showInfoToast(ChatActivity.chatActivity, "内容不能为空！");
					return;
				}
				ChatActivity.chatActivity.sendMessage(uid, content);
			}
		});
		
		ViewSetting.setOnClickListener(dialog, R.id.chat_cancel, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		
		dialog.show();
		
	}
	
	static void refresh(String string) {
		ChatActivity chatActivity=ChatActivity.chatActivity;
		try {
			JSONObject jsonObject=new JSONObject(string);
			int code=jsonObject.getInt("code");
			if (code!=0) {
				return;
			}
			JSONArray datas=jsonObject.getJSONArray("data");
			//chatActivity.chatListInfos=new ArrayList<ChatListInfo>();
			ArrayList<ChatListInfo> arrayList=new ArrayList<ChatListInfo>();
			int len=datas.length();
			for (int i=0;i<len;i++) {
				JSONObject object=datas.getJSONObject(i);
				String username=object.optString("username");
				long time=object.optLong("timestamp");
				String content=object.optString("content");
				int num=object.optInt("number");
				int hasNew=object.optInt("hasNew");
				String name=object.optString("name", username);
				//chatActivity.chatListInfos.add(new ChatListInfo(username, name, time, content, num, hasNew>0));
				arrayList.add(new ChatListInfo(username, name, time, content, num, hasNew>0));
			}
			
			chatActivity.chatListInfos.clear();
			chatActivity.chatListInfos.addAll(arrayList);
			Collections.sort(chatActivity.chatListInfos, new Comparator<ChatListInfo>() {
				@Override
				public int compare(ChatListInfo lhs, ChatListInfo rhs) {
					return (int)(rhs.timestamp-lhs.timestamp);
				}
			});
			chatActivity.hasModified=false;
			
			ListView listView=(ListView)chatActivity.findViewById(R.id.chatlist_listview);
			BaseAdapter baseAdapter=(BaseAdapter)listView.getAdapter();
			baseAdapter.notifyDataSetChanged();
			listView.setSelection(0);
			
		}
		catch (Exception e) {}
	}
	
}
