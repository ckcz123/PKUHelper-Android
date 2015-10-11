package com.pkuhelper.chat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;

import org.json.JSONArray;
import org.json.JSONObject;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.Util;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.subactivity.SubActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Patterns;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;

public class ChatActivity extends Activity {
	ArrayList<ChatListInfo> chatListInfos=new ArrayList<ChatListInfo>();
	ArrayList<ChatDetailInfo> chatDetailInfos=new ArrayList<ChatDetailInfo>();
	ArrayList<String> blackList=new ArrayList<String>();
	static final int PAGE_LIST = 0;
	static final int PAGE_CHAT = 1;
	static final String TYPE_FROM = "from";
	static final String MIME_PLAIN = "text/plain";
	static final String MIME_HTML = "text/html";
	static ChatActivity chatActivity;
	int pageShowing;
	boolean hasModified=false;
	static String toUid="";
	static String toName="";
	String tmpUid="";
	int urlNum=0;
	int currPage=1;
	Handler handler;
	ChatThread chatThread;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chatlist_listview);
		Util.getOverflowMenu(this);
		chatActivity=this;
		
		handler=new Handler(new Handler.Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				String string;
				switch (msg.what) {
					case Constants.MESSAGE_CHAT_GET_MORE_FAILED:
						string=(String)msg.obj;
						CustomToast.showErrorToast(chatActivity, string);
						return true;
					case Constants.MESSAGE_CHAT_GET_MORE_FINISHED:
						string=(String)msg.obj;
						ChatDetail.addMore(string);
						return true;
					case Constants.MESSAGE_CHAT_REFRESH_LIST:
						string=(String)msg.obj;
						ChatList.refresh(string);
						return true;
					case Constants.MESSAGE_CHAT_REFRESH_DETAIL:
						string=(String)msg.obj;
						ChatDetail.refresh(string);
						return true;
					case Constants.MESSAGE_CHAT_SEND_FAILED:
						string=(String)msg.obj;
						ChatDetail.finishSend(false, string);
						return true;
					case Constants.MESSAGE_CHAT_SEND_FINISHED:
						string=(String)msg.obj;
						ChatDetail.finishSend(true, string);
						return true;
				}
				return false;
			}
		});
		chatThread=new ChatThread(Constants.username);
		
		String tuid=getIntent().getStringExtra("uid");
		if (tuid!=null) {
			hasModified=true;
			ChatDetail.showDetail(tuid, tuid);
			return;
		}
		
		pageShowing=PAGE_LIST;		
		ChatList.showChatList();
	}
	
	@Override
	public void onContentChanged() {
		super.onContentChanged();
		try {
			View view=findViewById(android.R.id.content);
			if (view==null) return;
			int width=view.getWidth(), height=view.getHeight();
			Log.w("content-change", width+":"+height);
			if (width!=0 && height!=0) {
				ViewSetting.setBackground(chatActivity, 
						findViewById(R.id.chat_bg), R.drawable.chat_bg,
						width, height);
			}
		}
		catch (Exception e) {}
	}
	
	
	@SuppressWarnings("unchecked")
	void sendMessage(String to, String content) {
		if ("".equals(to)) {
			CustomToast.showInfoToast(chatActivity, "没有收件人！");
			return;
		}
		if ("".equals(content)) {
			CustomToast.showInfoToast(chatActivity, "内容为空！");
			return;
		}
		
		tmpUid=to;
		ArrayList<Parameters> arrayList=new ArrayList<Parameters>();
		//arrayList.add(new Parameters("uid", Constants.username));
		//arrayList.add(new Parameters("name", Constants.name));
		arrayList.add(new Parameters("to", to));
		arrayList.add(new Parameters("content", content));
		arrayList.add(new Parameters("type", "sendmsg"));
		//String timestamp=System.currentTimeMillis()/1000+"";
		//String hash=Util.getHash(Constants.username+timestamp+"I2V587");
		//arrayList.add(new Parameters("timestamp", timestamp));
		//arrayList.add(new Parameters("hash", hash));
		arrayList.add(new Parameters("token", Constants.token));
		
		new RequestingTask("正在发送...", Constants.domain+"/services/msg.php", 
				Constants.REQUEST_CHAT_SEND_CONTENT).execute(arrayList);
	}
	
	void finishSend(String string) {
		try {
			JSONObject jsonObject=new JSONObject(string);
			int code=jsonObject.getInt("code");
			if (code!=0) {
				CustomToast.showErrorToast(this, jsonObject.optString("msg", "获取失败"));
				return;
			}
			CustomToast.showSuccessToast(this, "发送成功！");
			hasModified=true;
			try {
				ChatList.dialog.dismiss();
			}
			catch (Exception e) {}
			
			Iterator<ChatListInfo> iterator=chatListInfos.iterator();
			String name=tmpUid;
			while (iterator.hasNext()) {
				ChatListInfo chatListInfo=iterator.next();
				if (tmpUid.equals(chatListInfo.username)) {
					name=chatListInfo.name;
					break;
				}
			}
			ChatDetail.showDetail(tmpUid, name);
			
		}
		catch (Exception e) {
			CustomToast.showErrorToast(this, "发送失败");
		}
		
	}
	
	void finishRequest(int type, String string) {
		if (type==Constants.REQUEST_CHAT_GET_LIST)
			ChatList.finishGetList(string);
		else if (type==Constants.REQUEST_CHAT_GET_CONTENT)
			ChatDetail.finishGetDetail(string);
		else if (type==Constants.REQUEST_CHAT_SEND_CONTENT)
			finishSend(string);
		else if (type==Constants.REQUEST_CHAT_DELETE_CONTENT
				|| type==Constants.REQUEST_CHAT_DELETE_LIST) {
			finishDelete(type, string);
		}
		else if (type==Constants.REQUEST_CHAT_BLACKLIST_MOVE_IN
				|| type==Constants.REQUEST_CHAT_BLACKLIST_MOVE_OUT)
			BlackList.finishRequest(type, string);
		else if (type==Constants.REQUEST_CHAT_GET_NC_SERVICES)
			getncservices(string);
	}
	
	void finishDelete(int type, String string) {
		try {
			JSONObject jsonObject=new JSONObject(string);
			int code=jsonObject.getInt("code");
			if (code!=0) {
				CustomToast.showErrorToast(this, jsonObject.optString("msg", "删除失败！"));
				return;
			}
			CustomToast.showSuccessToast(this, "删除成功！", 1500);
			hasModified=true;
			if (type==Constants.REQUEST_CHAT_DELETE_CONTENT) {
				ChatDetail.showDetail(toUid, toName);
			}
			else {
				ChatList.showChatList();
			}
			
		}
		catch (Exception e) {CustomToast.showErrorToast(this, "删除失败", 1500);}
	}
	public void addChat() {
		new AlertDialog.Builder(this).setItems(new String[] {"私信", "服务号"}, 
				new DialogInterface.OnClickListener() {
			@SuppressWarnings("unchecked")
			public void onClick(DialogInterface dialog, int which) {
				if (which==0) {
					ChatList.addChat();
					return;
				}
				else if (which==1) {
					new RequestingTask("正在获取服务号列表...", 
							Constants.domain+"/pkuhelper/nc/getncservices.php", 
							Constants.REQUEST_CHAT_GET_NC_SERVICES)
					.		execute(new ArrayList<Parameters>());
				}
			}
		}).show();
	}
	
	public void getncservices(String string) {
		try {
			JSONObject jsonObject=new JSONObject(string);
			int code=jsonObject.getInt("code");
			if (code!=0) {
				CustomToast.showErrorToast(this, jsonObject.optString("msg", "获取服务号列表失败"));
				return;
			}
			JSONArray data=jsonObject.getJSONArray("data");
			int len=data.length();
			final ArrayList<Integer> sids=new ArrayList<Integer>();
			final ArrayList<String> names=new ArrayList<String>();
			for (int i=0;i<len;i++) {
				JSONObject object=data.getJSONObject(i);
				int sid=object.getInt("sid");
				String name=object.getString("name");
				int callback=object.optInt("callback");
/*
				if (callback!=0) name=name+" [V]";
				sids.add(sid);
				names.add(name);
*/
				if (callback!=0) {
					sids.add(sid);
					names.add(name);
				}
			}
			new AlertDialog.Builder(this).setItems(names.toArray(new String[names.size()]),
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					int sid=sids.get(which);
					ChatDetail.showDetail(sid+"", names.get(which));
				}
			}).setTitle("请选择服务号").show();			
		}
		catch (Exception e) {
			e.printStackTrace();
			CustomToast.showErrorToast(this, "获取服务号列表失败");
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
		if (pageShowing==PAGE_LIST)
			menu.add(Menu.NONE, Constants.MENU_CHAT_ADD, Constants.MENU_CHAT_ADD, "")
				.setIcon(R.drawable.add).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(Menu.NONE, Constants.MENU_CHAT_REFRESH, Constants.MENU_CHAT_REFRESH, "")
		.setIcon(R.drawable.reload).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		String blHint="我的黑名单";
		if (pageShowing==PAGE_CHAT) {
			if (BlackList.isInBlackList()) blHint="移出黑名单";
			else blHint="加入黑名单";
		}
		menu.add(Menu.NONE, Constants.MENU_CHAT_BLACKLIST, Constants.MENU_CHAT_BLACKLIST, blHint);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id==Constants.MENU_CHAT_ADD) {
			//ChatList.addChat();
			addChat();
			return true;
		}
		if (id==Constants.MENU_CHAT_REFRESH) {
			if (pageShowing==PAGE_LIST) {
				ChatList.showChatList();
			}
			else if (pageShowing==PAGE_CHAT) {
				ChatDetail.showDetail(toUid, toName);
			}
			return true;
		}
		if (id==Constants.MENU_CHAT_CLOSE) {
			if (pageShowing==PAGE_CHAT) ChatList.realShowList();
			else if (pageShowing==PAGE_LIST) {
				chatThread.setStop();
				finish();
			}
			return true;
		}
		if (id==Constants.MENU_CHAT_BLACKLIST) {
			if (pageShowing==PAGE_LIST) {
				if (blackList.size()==0) {
					CustomToast.showInfoToast(this, "你没有黑名单！", 1500);
					return true;
				}
				
				int len=blackList.size();
				String[] strings=new String[len];
				for (int i=0;i<len;i++) {
					String username=blackList.get(i);
					String name=getName(username);
					strings[i]=username;
					if (!name.equals(username)) strings[i]+=" "+name;
				}
				
				new AlertDialog.Builder(this).setTitle("我的黑名单")
				.setItems(strings, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						final String username=blackList.get(which);
						new AlertDialog.Builder(chatActivity).setTitle("移出黑名单")
						.setMessage("你确定要将 "+getName(username)+" 移出黑名单吗？")
						.setPositiveButton("是", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								BlackList.moveOut(username);
							}
						}).setNegativeButton("否", null).show();
					}
				}).show();
				
			}
			else if (pageShowing==PAGE_CHAT) {
				new AlertDialog.Builder(this).setTitle("确认操作？")
				.setMessage("确定要将"+getName(toUid)+item.getTitle()+"吗？")
				.setPositiveButton("是", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (BlackList.isInBlackList()) BlackList.moveOut(toUid);
						else BlackList.moveIn(toUid);
					}
				}).setNegativeButton("否", null).show();
			}
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu,View v,ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (pageShowing==PAGE_CHAT) {
			menu.add(Menu.NONE, Constants.CONTEXT_MENU_CHAT_COPY, Constants.CONTEXT_MENU_CHAT_COPY, "复制");
			//menu.add(Menu.NONE, Constants.CONTEXT_MENU_CHAT_GET_URL, Constants.CONTEXT_MENU_CHAT_GET_URL, "复制");
			
			int position=ChatDetail.position;
			if (position!=-1) {
				String content=chatDetailInfos.get(position).content;
				
				Matcher matcher=Patterns.WEB_URL.matcher(content);
				
				urlNum=0;
				while (matcher.find()) {
					int start=matcher.start();
					int end=matcher.end();
					String url=content.substring(start, end);
					String tmp=url.toLowerCase(Locale.getDefault());
					if (tmp.startsWith("http://") || tmp.startsWith("https://")) {
						if (url.endsWith("'") || url.endsWith("\"") 
								|| url.endsWith(")") || url.endsWith("]")
								|| url.endsWith("}")) 
							url=url.substring(0, url.length()-1);
						menu.add(Menu.NONE, Constants.CONTEXT_MENU_CHAT_GET_URL+urlNum, Constants.CONTEXT_MENU_CHAT_GET_URL+urlNum, 
							url);
						urlNum++;
					}
				}
			}
		}
		if (pageShowing==PAGE_LIST) {
			String hint="加入黑名单";
			if (BlackList.isInBlackList(tmpUid)) hint="移出黑名单";
			menu.add(Menu.NONE, Constants.CONTEXT_MENU_CHAT_BLACKLIST, 
					Constants.CONTEXT_MENU_CHAT_BLACKLIST, hint);
		}
		menu.add(Menu.NONE, Constants.CONTEXT_MENU_CHAT_DELETE, Constants.CONTEXT_MENU_CHAT_DELETE, "删除");
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean onContextItemSelected(MenuItem item) {
		int id=item.getItemId();
		if (id==Constants.CONTEXT_MENU_CHAT_COPY) {
			if (ChatDetail.position==-1) return true;
			
			String content=chatDetailInfos.get(ChatDetail.position).content;
			ClipboardManager clipboardManager=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
			clipboardManager.setPrimaryClip(ClipData.newPlainText("text", content));
			CustomToast.showSuccessToast(this, "文本已复制到剪切板！", 1500);
			
			return true;
		}
		if (id==Constants.CONTEXT_MENU_CHAT_BLACKLIST) {
			new AlertDialog.Builder(this).setTitle("确认操作？")
			.setMessage("确认将"+getName(tmpUid)+item.getTitle()+"吗？")
			.setPositiveButton("是", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (BlackList.isInBlackList(tmpUid))
						BlackList.moveOut(tmpUid);
					else BlackList.moveIn(tmpUid);
				}
			}).setNegativeButton("否", null).show();
		}
		if (id==Constants.CONTEXT_MENU_CHAT_DELETE) {
			
			if (pageShowing==PAGE_CHAT) {
				
				int position=ChatDetail.position;
				if (position==-1) return true;
				
				int idd=chatDetailInfos.get(position).id;
				ArrayList<Parameters> arrayList=new ArrayList<Parameters>();
				arrayList.add(new Parameters("type", "deldetail"));
				arrayList.add(new Parameters("id", idd+""));
				arrayList.add(new Parameters("token", Constants.token));
				
				new RequestingTask("正在删除...", Constants.domain+"/services/msg.php", 
						Constants.REQUEST_CHAT_DELETE_CONTENT).execute(arrayList);
				
				return true;
			}
			
			new AlertDialog.Builder(this).setTitle("确认删除？")
			.setMessage("确认删除这条对话？").setPositiveButton("是", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ArrayList<Parameters> arrayList=new ArrayList<Parameters>();
					arrayList.add(new Parameters("type", "dellist"));
					arrayList.add(new Parameters("to", tmpUid));
					arrayList.add(new Parameters("token", Constants.token));
					new RequestingTask("正在删除...", Constants.domain+"/services/msg.php", 
							Constants.REQUEST_CHAT_DELETE_LIST).execute(arrayList);
				}
			}).setNegativeButton("否", null).show();
			
			return true;
		}
		if (id>=Constants.CONTEXT_MENU_CHAT_GET_URL && id<Constants.CONTEXT_MENU_CHAT_GET_URL+urlNum) {
			String url=item.getTitle().toString();
			
			Intent intent=new Intent(this, SubActivity.class);
			intent.putExtra("type", Constants.SUBACTIVITY_TYPE_WEBVIEW);
			intent.putExtra("url", url);
			startActivity(intent);
			
		}
		return super.onContextItemSelected(item);
		
	}
	
	@Override
	public void onPause() {
		chatThread.setStop();
		super.onPause();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (chatThread.getrealstopped()) {
			chatThread=new ChatThread(Constants.username);
		}
		chatThread.start();
	}
	
	public String getName(String username) {
		Iterator<ChatListInfo> iterator=chatListInfos.iterator();
		while (iterator.hasNext()) {
			ChatListInfo chatListInfo=iterator.next();
			if (chatListInfo.username.equals(username)) return chatListInfo.name;
		}
		return username;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode==KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN) {
			if (pageShowing==PAGE_CHAT) {
				ChatList.realShowList();
				return true;
			}
			if (pageShowing==PAGE_LIST) {
				chatThread.setStop();
				finish();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
}
