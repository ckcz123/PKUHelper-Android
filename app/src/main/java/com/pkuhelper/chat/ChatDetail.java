package com.pkuhelper.chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.RequestingTask;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.lib.webconnection.WebConnection;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class ChatDetail {
	static int requestingPage = 1;
	static boolean requesting = false;
	static int position = -1;
	static String tmpContent = "";

	@SuppressWarnings("unchecked")
	static void showDetail(String uid, String name) {
		ChatActivity.toUid = uid;
		ChatActivity.toName = name;
		position = -1;
		requestingPage = 1;
		requesting = false;
		ChatActivity.chatActivity.currPage = 1;
		ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
		//arrayList.add(new Parameters("uid", Constants.username));
		arrayList.add(new Parameters("to", uid));
		//String timestamp=System.currentTimeMillis()/1000+"";
		//String hash=Util.getHash(Constants.username+timestamp+"I2V587");
		//arrayList.add(new Parameters("timestamp", timestamp));
		//arrayList.add(new Parameters("hash", hash));
		arrayList.add(new Parameters("type", "getdetail"));
		arrayList.add(new Parameters("page", requestingPage + ""));
		arrayList.add(new Parameters("token", Constants.token));
		new RequestingTask(ChatActivity.chatActivity, "正在获取聊天记录",
				Constants.domain + "/services/msg.php", Constants.REQUEST_CHAT_GET_CONTENT)
				.execute(arrayList);

	}

	static void finishGetDetail(String string) {
		try {
			JSONObject jsonObject = new JSONObject(string);
			int code = jsonObject.getInt("code");
			if (code != 0) {
				CustomToast.showErrorToast(ChatActivity.chatActivity, jsonObject.optString("msg", "获取失败"));
				return;
			}
			String name = jsonObject.optString("name");
			if (!"".equals(name) && !ChatActivity.toUid.equals("name"))
				ChatActivity.toName = name;
			JSONArray jsonArray = jsonObject.optJSONArray("data");
			int len = jsonArray.length();
			ChatActivity.chatActivity.chatDetailInfos = new ArrayList<ChatDetailInfo>();

			if (ChatActivity.chatActivity.chatThread.getrealstopped()) {
				ChatActivity.chatActivity.chatThread = new ChatThread(Constants.username);
			}
			ChatActivity.chatActivity.chatThread.start();

			for (int i = 0; i < len; i++) {
				JSONObject object = jsonArray.optJSONObject(i);
				ChatActivity.chatActivity.chatDetailInfos.add(
						new ChatDetailInfo(object.getInt("id"), object.optString("content"),
								object.optString("mime"), object.optLong("timestamp"),
								object.getString("type")));
			}
			//if (len==0) {
			//	CustomToast.showInfoToast(ChatActivity.chatActivity, "暂时没有聊天记录");
			//}

		} catch (Exception e) {
			CustomToast.showErrorToast(ChatActivity.chatActivity, "获取失败");
		} finally {
			realShowDetail();
		}
	}

	static void realShowDetail() {
		final ChatActivity chatActivity = ChatActivity.chatActivity;
		chatActivity.pageShowing = ChatActivity.PAGE_CHAT;
		chatActivity.invalidateOptionsMenu();
		chatActivity.setContentView(R.layout.chatdetail_view);
		chatActivity.getActionBar().setTitle(ChatActivity.toName);

		final ListView listView = (ListView) chatActivity.findViewById(R.id.chatdetail_list);
		listView.setDividerHeight(0);
		Collections.sort(chatActivity.chatDetailInfos, new Comparator<ChatDetailInfo>() {
			@Override
			public int compare(ChatDetailInfo lhs, ChatDetailInfo rhs) {
				return (int) (lhs.timestamp - rhs.timestamp);
			}
		});

		listView.setAdapter(new BaseAdapter() {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ChatDetailInfo chatDetailInfo = chatActivity.chatDetailInfos.get(position);
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

				String hint = "";
				String content = chatDetailInfo.content;
				if (ChatActivity.TYPE_FROM.equals(chatDetailInfo.type) &&
						(content.contains("手机") || content.contains("微信")
								|| content.contains("QQ") || content.contains("qq")))
					hint = "请慎重给对方你自己的联系方式。";
				if (ChatActivity.TYPE_FROM.equals(chatDetailInfo.type) &&
						content.contains("钱"))
					hint = "如果涉及到财产问题请慎重处理。";
				if (position == chatActivity.chatDetailInfos.size() - 1
						&& ChatActivity.toName.equals(ChatActivity.toUid))
					hint = "只有对方回复你后你才能看到对方的姓名。";
				if (position == chatActivity.chatDetailInfos.size() - 1 &&
						BlackList.isInBlackList()) hint = "对方在你的黑名单中。";

				if (ChatActivity.TYPE_FROM.equals(chatDetailInfo.type)) {
					convertView = chatActivity.getLayoutInflater().inflate(R.layout.chatdetail_item_from, parent, false);

					if ("".equals(chatDetailInfo.mime) || ChatActivity.MIME_PLAIN.equals(chatDetailInfo.mime)) {
						ViewSetting.setTextView(convertView, R.id.chatfrom_text, chatDetailInfo.content);
					} else if (ChatActivity.MIME_HTML.equals(chatDetailInfo.mime)) {
						ViewSetting.setTextView(convertView, R.id.chatfrom_text, Html.fromHtml(chatDetailInfo.content));
					} else {
						ViewSetting.setTextView(convertView, R.id.chatfrom_text,
								"（此版本不支持的消息格式，请先升级版本后再查看）");
					}

					ViewSetting.setTextView(convertView, R.id.chatfrom_time,
							simpleDateFormat.format(new Date(chatDetailInfo.timestamp * 1000)));

					if (!"".equals(hint)) {
						convertView.findViewById(R.id.chatfrom_hint).setVisibility(View.VISIBLE);
						ViewSetting.setTextView(convertView, R.id.chatfrom_hint_text, hint);
					}

				} else {
					convertView = chatActivity.getLayoutInflater().inflate(R.layout.chatdetail_item_to, parent, false);

					if ("".equals(chatDetailInfo.mime) || ChatActivity.MIME_PLAIN.equals(chatDetailInfo.mime)) {
						ViewSetting.setTextView(convertView, R.id.chatto_text, chatDetailInfo.content);
					} else if (ChatActivity.MIME_HTML.equals(chatDetailInfo.mime)) {
						ViewSetting.setTextView(convertView, R.id.chatto_text, Html.fromHtml(chatDetailInfo.content));
					} else {
						ViewSetting.setTextView(convertView, R.id.chatto_text,
								"（此版本不支持的消息格式，请先升级版本后再查看）");
					}

					ViewSetting.setTextView(convertView, R.id.chatto_time,
							simpleDateFormat.format(new Date(chatDetailInfo.timestamp * 1000)));

					if (!"".equals(hint)) {
						convertView.findViewById(R.id.chatto_hint).setVisibility(View.VISIBLE);
						ViewSetting.setTextView(convertView, R.id.chatto_hint_text, hint);
					}


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
				return chatActivity.chatDetailInfos.size();
			}
		});
		listView.setSelection(chatActivity.chatDetailInfos.size());

		listView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
								 int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem <= 5) requestMore();
			}
		});

		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
										   int position, long id) {
				ChatDetail.position = position;
				return false;
			}
		});
		chatActivity.registerForContextMenu(listView);
		position = -1;

		EditText editText = (EditText) chatActivity.findViewById(R.id.chatdetail_send_text);
		editText.setHint("回复消息给" + ChatActivity.toName);
		editText.requestFocus();

		// 设置当输入法弹出时，自动滚到最下面
		// 略蛋疼：先当点击EditText时强制弹出输出框；然后sleep 200ms（输出框弹出完毕）
		// 后将listView设置到最下
		editText.setOnTouchListener(new View.OnTouchListener() {

			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					InputMethodManager inputMethodManager = (InputMethodManager) chatActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
					final Handler mHandler = new Handler(new Handler.Callback() {
						@Override
						public boolean handleMessage(Message msg) {
							if (msg.what == 1234) {
								listView.setSelection(listView.getCount());
							}
							return false;
						}
					});
					ResultReceiver receiver = new ResultReceiver(mHandler) {
						@Override
						protected void onReceiveResult(int resultCode, Bundle resultData) {
							if (resultCode == InputMethodManager.RESULT_SHOWN) {
								new Thread(new Runnable() {
									@Override
									public void run() {
										try {
											Thread.sleep(200);
										} catch (Exception e) {
										}
										mHandler.sendEmptyMessage(1234);
									}
								}).start();
							}
						}
					};
					inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_FORCED, receiver);
				}
				return false;
			}
		});

		ViewSetting.setOnClickListener(chatActivity, R.id.chatdetail_send_button, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String content = ViewSetting.getEditTextValue(chatActivity, R.id.chatdetail_send_text);
				if ("".equals(content)) {
					CustomToast.showInfoToast(chatActivity, "请输入内容！");
					EditText editText = (EditText) chatActivity.findViewById(R.id.chatdetail_send_text);
					if (editText.requestFocus()) {
						((InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
								.showSoftInput(editText, 0);
					}
					return;
				}

				//chatActivity.sendMessage(ChatActivity.toUid, content);
				sendMsg(ChatActivity.toUid, content);

			}
		});
	}

	static void requestMore() {
		if (requesting) return;
		requesting = true;
		requestingPage = ChatActivity.chatActivity.currPage + 1;
		new Thread(new Runnable() {
			@Override
			public void run() {
				Handler handler = ChatActivity.chatActivity.handler;
				ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
				//arrayList.add(new Parameters("uid", Constants.username));
				arrayList.add(new Parameters("to", ChatActivity.toUid));
				//String timestamp=System.currentTimeMillis()/1000+"";
				//String hash=Util.getHash(Constants.username+timestamp+"I2V587");
				//arrayList.add(new Parameters("timestamp", timestamp));
				//arrayList.add(new Parameters("hash", hash));
				arrayList.add(new Parameters("token", Constants.token));
				arrayList.add(new Parameters("type", "getdetail"));
				arrayList.add(new Parameters("page", requestingPage + ""));
				Parameters result = WebConnection.connect(Constants.domain + "/services/msg.php", arrayList);
				String string = "";
				if (!"200".equals(result.name)) {
					if ("-1".equals(result.name)) string = "无法连接网络(-1,-1)";
					else string = "无法连接到服务器 (HTTP " + result.name + ")";
					handler.sendMessage(Message.obtain(handler, Constants.MESSAGE_CHAT_GET_MORE_FAILED, string));
				}
				handler.sendMessage(Message.obtain(handler, Constants.MESSAGE_CHAT_GET_MORE_FINISHED, result.value));
			}
		}).start();
	}

	public static void addMore(String string) {
		try {
			JSONObject jsonObject = new JSONObject(string);
			int code = jsonObject.getInt("code");
			if (code != 0) {
				CustomToast.showErrorToast(ChatActivity.chatActivity, jsonObject.optString("msg", "获取失败"));
				return;
			}
			JSONArray jsonArray = jsonObject.optJSONArray("data");
			int len = jsonArray.length();
			if (len == 0) return;

			ArrayList<ChatDetailInfo> arrayList = new ArrayList<ChatDetailInfo>();

			for (int i = 0; i < len; i++) {
				JSONObject object = jsonArray.optJSONObject(i);
				arrayList.add(
						new ChatDetailInfo(object.getInt("id"), object.optString("content"),
								object.optString("mime"), object.optLong("timestamp"),
								object.getString("type")));
			}

			ChatActivity chatActivity = ChatActivity.chatActivity;
			chatActivity.chatDetailInfos.addAll(arrayList);
			Collections.sort(chatActivity.chatDetailInfos, new Comparator<ChatDetailInfo>() {

				@Override
				public int compare(ChatDetailInfo lhs, ChatDetailInfo rhs) {
					return (int) (lhs.timestamp - rhs.timestamp);
				}
			});

			ListView listView = (ListView) chatActivity.findViewById(R.id.chatdetail_list);
			int index = listView.getFirstVisiblePosition();
			View v = listView.getChildAt(0);
			int top = (v == null) ? 0 : (v.getTop() - listView.getPaddingTop());

			index += len;
			BaseAdapter baseAdapter = (BaseAdapter) listView.getAdapter();
			baseAdapter.notifyDataSetChanged();

			listView.setSelectionFromTop(index, top);

			chatActivity.currPage = requestingPage;
			requesting = false;
		} catch (Exception e) {
			CustomToast.showErrorToast(ChatActivity.chatActivity, "获取失败");
		}
	}

	static void refresh(String string) {
		ChatActivity chatActivity = ChatActivity.chatActivity;
		try {
			JSONObject jsonObject = new JSONObject(string);
			int code = jsonObject.getInt("code");
			if (code != 0) {
				return;
			}
			JSONArray jsonArray = jsonObject.optJSONArray("data");
			int len = jsonArray.length();
			ArrayList<ChatDetailInfo> arrayList = new ArrayList<ChatDetailInfo>();

			for (int i = 0; i < len; i++) {
				JSONObject object = jsonArray.optJSONObject(i);
				arrayList.add(
						new ChatDetailInfo(object.getInt("id"), object.optString("content"),
								object.optString("mime"), object.optLong("timestamp"),
								object.getString("type")));
			}

			if (len != 0) {
				chatActivity.chatDetailInfos.addAll(arrayList);
				Collections.sort(chatActivity.chatDetailInfos, new Comparator<ChatDetailInfo>() {
					@Override
					public int compare(ChatDetailInfo lhs, ChatDetailInfo rhs) {
						return (int) (lhs.timestamp - rhs.timestamp);
					}
				});
				ListView listView = (ListView) chatActivity.findViewById(R.id.chatdetail_list);
				BaseAdapter baseAdapter = (BaseAdapter) listView.getAdapter();

				baseAdapter.notifyDataSetChanged();
				listView.setSelection(listView.getCount() - 1);
			}

		} catch (Exception e) {
		}
	}

	public static void sendMsg(final String to, final String content) {
		ChatActivity.chatActivity.findViewById(R.id.chatdetail_send_text).setEnabled(false);
		ChatActivity.chatActivity.findViewById(R.id.chatdetail_send_button).setEnabled(false);
		tmpContent = content;
		ViewSetting.setEditTextValue(ChatActivity.chatActivity, R.id.chatdetail_send_text, "正在发送中...");
		new Thread(new Runnable() {
			@Override
			public void run() {
				ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
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

				Parameters parameters = WebConnection.connect(Constants.domain + "/services/msg.php",
						arrayList);

				Handler handler = ChatActivity.chatActivity.handler;
				if (!"200".equals(parameters.name)) {
					handler.sendMessage(Message.obtain(handler, Constants.MESSAGE_CHAT_SEND_FAILED, parameters.name));
				} else {
					handler.sendMessage(Message.obtain(handler, Constants.MESSAGE_CHAT_SEND_FINISHED, parameters.value));
				}

				return;
			}
		}).start();

	}

	public static void finishSend(boolean succeed, String string) {
		ChatActivity chatActivity = ChatActivity.chatActivity;
		try {

			if (succeed) {
				try {
					JSONObject jsonObject = new JSONObject(string);
					int code = jsonObject.getInt("code");
					if (code != 0) {
						CustomToast.showErrorToast(chatActivity, jsonObject.optString("msg", "发送失败"));
						ViewSetting.setEditTextValue(chatActivity, R.id.chatdetail_send_text, tmpContent);
					} else {
						ViewSetting.setEditTextValue(chatActivity, R.id.chatdetail_send_text, "");
						chatActivity.chatDetailInfos.add(new ChatDetailInfo(-1, tmpContent,
								ChatActivity.MIME_PLAIN, System.currentTimeMillis() / 1000, "to"));
						chatActivity.hasModified = true;
						ListView listView = (ListView) chatActivity.findViewById(R.id.chatdetail_list);
						BaseAdapter baseAdapter = (BaseAdapter) listView.getAdapter();
						baseAdapter.notifyDataSetChanged();
						listView.setSelection(listView.getCount() - 1);
					}
				} catch (Exception e) {
				}
			} else {
				ViewSetting.setEditTextValue(chatActivity, R.id.chatdetail_send_text, tmpContent);
				if ("-1".equals(string)) {
					CustomToast.showErrorToast(chatActivity, "无法连接到网络(-1, -1)", 1500);
				} else {
					CustomToast.showErrorToast(chatActivity, "无法连接到服务器(HTTP " + string + ")", 1500);
				}
			}
		} catch (Exception e) {
		} finally {
			try {
				chatActivity.findViewById(R.id.chatdetail_send_text).setEnabled(true);
				chatActivity.findViewById(R.id.chatdetail_send_button).setEnabled(true);
				tmpContent = "";
			} catch (Exception ee) {
			}
		}
	}
}
