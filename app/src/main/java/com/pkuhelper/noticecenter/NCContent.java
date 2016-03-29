package com.pkuhelper.noticecenter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.pkuhelper.lib.RequestingTask;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.lib.webconnection.WebConnection;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NCContent {
	static int requestPage = 1;
	static int oneRequestPage = 1;
	static boolean requesting = false;
	static boolean oneRequesting = false;

	@SuppressWarnings("unchecked")
	public static void getNotice() {
		new RequestingTask(NCActivity.ncActivity, "正在获取通知列表...",
				Constants.domain + "/pkuhelper/nc/fetch.php?token=" + Constants.token
						+ "&p=1&platform=Android", Constants.REQUEST_NOTICECENTER_GETCONTENT_ALL)
				.execute(new ArrayList<Parameters>());

	}

	@SuppressWarnings("unchecked")
	public static void getNotice(String sid) {
		new RequestingTask(NCActivity.ncActivity, "正在获取通知列表...",
				Constants.domain + "/pkuhelper/nc/fetch.php?token=" + Constants.token
						+ "&p=1&platform=Android&sid=" + sid, Constants.REQUEST_NOTICECENTER_GETCONTENT_ONE)
				.execute(new ArrayList<Parameters>());
		NCActivity.ncActivity.lastRequestSid = sid;
	}

    //已经获得一部分json格式的通知，在string中
	public static void finishRequest(String string) {
		try {
			JSONObject jsonObject = new JSONObject(string);
			String code = jsonObject.getString("code");
			if ("1".equals(code)) {
				new AlertDialog.Builder(NCActivity.ncActivity)
						.setTitle("提示").setMessage("你还没有设置订阅的源！")
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								NCList.showList();
								return;
							}
						}).setCancelable(false).setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						NCList.showList();
					}
				}).show();
				return;
			}

			JSONArray jsonArray = jsonObject.getJSONArray("data");
			int len = jsonArray.length();
			NCActivity.ncActivity.contentListArray.clear();
			for (int i = 0; i < len; i++) {
				JSONObject content = jsonArray.getJSONObject(i);
				String timestamp = content.getString("time");
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
				String time = simpleDateFormat.format(new Date(Long.parseLong(timestamp) * 1000));
				NCActivity.ncActivity.contentListArray.add(
						new Content(content.getString("nid"), content.getString("title"),
								content.getString("sid"), "",
								content.getString("subscribe"), time));
			}
			NCActivity.ncActivity.currpage = requestPage = 1;
			NCActivity.ncActivity.firstTimeToBottom = true;
			NCActivity.ncActivity.contentPosition = 0;
			if (Notice.courseNotice.isSelected) {
                showContent();
				CourseNotice.loginToCourse();
				return;
			} else {
				showContent();
			}
		} catch (Exception e) {
			NCActivity.ncActivity.oneListArray.clear();
			CustomToast.showErrorToast(NCActivity.ncActivity, "通知获取失败");
		}

	}

	public static void finishOneRequest(String string) {
		try {
			JSONObject jsonObject = new JSONObject(string);
			JSONArray jsonArray = jsonObject.getJSONArray("data");
			int len = jsonArray.length();
			NCActivity.ncActivity.oneListArray.clear();
			for (int i = 0; i < len; i++) {
				JSONObject content = jsonArray.getJSONObject(i);
				String timestamp = content.getString("time");
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
				String time = simpleDateFormat.format(new Date(Long.parseLong(timestamp) * 1000));
				NCActivity.ncActivity.oneListArray.add(
						new Content(content.getString("nid"), content.getString("title"),
								content.getString("sid"), "",
								content.getString("subscribe"), time));
			}
			NCActivity.ncActivity.onepage = requestPage = 1;
			NCActivity.ncActivity.oneFirstTimeToBottom = true;
			NCActivity.ncActivity.onePosition = 0;
			showContent(NCActivity.ncActivity.lastRequestSid);
		} catch (Exception e) {
			NCActivity.ncActivity.oneListArray.clear();
			CustomToast.showErrorToast(NCActivity.ncActivity, "通知获取失败");
		}
	}

	public static void showContent() {
		NCActivity ncActivity = NCActivity.ncActivity;
		ncActivity.nowShowing = NCActivity.PAGE_VIEW;
		ncActivity.invalidateOptionsMenu();
		ncActivity.setContentView(R.layout.nc_viewcontent_listview);
		ncActivity.swipeRefreshLayout = (SwipeRefreshLayout) ncActivity.findViewById(R.id.nc_swipeRefreshLayout);
		ncActivity.swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_purple,
				android.R.color.holo_green_light,
				android.R.color.holo_blue_bright,
				android.R.color.holo_orange_light);
		ncActivity.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			public void onRefresh() {
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(500);
						} catch (Exception e) {
						}
						NCActivity.ncActivity.handler.sendEmptyMessage(Constants.MESSAGE_SLEEP_FINISHED);
					}
				}).start();
			}
		});
		ViewSetting.setBackground(ncActivity, ncActivity.swipeRefreshLayout, Color.WHITE,
				ncActivity.screenWidth, ncActivity.screenHeight);
		ncActivity.contentListView = (ListView) ncActivity.findViewById(R.id.nc_viewcontent_listview);
		ncActivity.setTitle("查看通知");

		ListView listView = ncActivity.contentListView;
		/*
		View view=ncActivity.getLayoutInflater().inflate(R.layout.mypku_list_odd_view, null, false);
		view.setBackgroundColor(Color.parseColor("#f19ec2"));
		ViewSetting.setTextView(view, R.id.image_odd_name, "新媒体联盟");
		ViewSetting.setImageResource(view, R.id.image_odd_view, R.drawable.xmtlm);
		listView.addHeaderView(view);
		*/
		listView.setAdapter(new BaseAdapter() {

			@SuppressLint("ViewHolder")
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				NCActivity ncActivity = NCActivity.ncActivity;
				Content content = ncActivity.contentListArray.get(position);
				String sid = content.sid;
				Notice notice = ncActivity.sourceListMap.get(sid);
				LayoutInflater inflater = ncActivity.getLayoutInflater();
				convertView = inflater.inflate(R.layout.nc_viewcontent_item, parent, false);
				ViewSetting.setTextView(convertView, R.id.nc_viewcontent_author, notice.name);
				ViewSetting.setTextView(convertView, R.id.nc_viewcontent_time, content.time);
				ViewSetting.setTextView(convertView, R.id.nc_viewcontent_title, content.title);
				ViewSetting.setTextView(convertView, R.id.nc_viewcontent_text, content.subscribe);
				ViewSetting.setImageDrawable(convertView, R.id.nc_viewcontent_image, notice.icon);
				convertView.setTag(sid);
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
				return NCActivity.ncActivity.contentListArray.size();
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				NCActivity ncActivity = NCActivity.ncActivity;
				Content content = ncActivity.contentListArray.get(position);
				ncActivity.contentPosition = ncActivity.contentListView.getFirstVisiblePosition();
				String sid = content.sid;
				if ("0".equals(sid)) {
					NCDetail.getCourse(content.title, content.url);
					return;
				}
				Drawable drawable = ncActivity.sourceListMap.get(content.sid).icon;
				Bitmap bitmap;
				if (drawable == null) bitmap = null;
				else bitmap = ((BitmapDrawable) drawable).getBitmap();
				NCDetail.showDirectly(content.title, Integer.parseInt(sid),
						content.nid, content.subscribe, bitmap);
			}
		});
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
								 int visibleItemCount, int totalItemCount) {
				if (totalItemCount == 0) return;
				int lastItem = firstVisibleItem + visibleItemCount;
				int itemLeft = 3;
				if (lastItem >= totalItemCount - itemLeft)
					requestMore();
			}
		});
		listView.setSelection(NCActivity.ncActivity.contentPosition);
	}

	public static void showContent(String sid) {
		NCActivity ncActivity = NCActivity.ncActivity;
		ncActivity.nowShowing = NCActivity.PAGE_ONE_VIEW;
		ncActivity.invalidateOptionsMenu();
		ncActivity.setContentView(R.layout.nc_viewcontent_listview);
		ncActivity.swipeRefreshLayout = (SwipeRefreshLayout) ncActivity.findViewById(R.id.nc_swipeRefreshLayout);
		ncActivity.swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_purple,
				android.R.color.holo_green_light,
				android.R.color.holo_blue_bright,
				android.R.color.holo_orange_light);
		ncActivity.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			public void onRefresh() {
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(500);
						} catch (Exception e) {
						}
						NCActivity.ncActivity.handler.sendEmptyMessage(Constants.MESSAGE_SLEEP_FINISHED);
					}
				}).start();
			}
		});
		ViewSetting.setBackground(ncActivity, ncActivity.swipeRefreshLayout, Color.WHITE,
				ncActivity.screenWidth, ncActivity.screenHeight);
		ncActivity.oneListView = (ListView) ncActivity.findViewById(R.id.nc_viewcontent_listview);
		Notice notice = ncActivity.sourceListMap.get(sid);
		ncActivity.setTitle("查看" + notice.name + "的通知");

		ListView listView = ncActivity.oneListView;
		listView.setAdapter(new BaseAdapter() {

			@SuppressLint("ViewHolder")
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				NCActivity ncActivity = NCActivity.ncActivity;
				Content content = ncActivity.oneListArray.get(position);
				String sid = content.sid;
				Notice notice = ncActivity.sourceListMap.get(sid);
				LayoutInflater inflater = ncActivity.getLayoutInflater();
				convertView = inflater.inflate(R.layout.nc_viewcontent_item, parent, false);

				ViewSetting.setTextView(convertView, R.id.nc_viewcontent_author, notice.name);
				ViewSetting.setTextView(convertView, R.id.nc_viewcontent_time, content.time);
				ViewSetting.setTextView(convertView, R.id.nc_viewcontent_title, content.title);
				ViewSetting.setTextView(convertView, R.id.nc_viewcontent_text, content.subscribe);
				ViewSetting.setImageDrawable(convertView, R.id.nc_viewcontent_image, notice.icon);
				convertView.setTag(sid);
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
				return NCActivity.ncActivity.oneListArray.size();
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				NCActivity ncActivity = NCActivity.ncActivity;
				Content content = ncActivity.oneListArray.get(position);
				ncActivity.onePosition = ncActivity.oneListView.getFirstVisiblePosition();
				String sid = content.sid;
				if ("0".equals(sid)) {
					NCDetail.getCourse(content.title, content.url);
					return;
				}
				Drawable drawable = ncActivity.sourceListMap.get(content.sid).icon;
				Bitmap bitmap;
				if (drawable == null) bitmap = null;
				else bitmap = ((BitmapDrawable) drawable).getBitmap();
				NCDetail.showDirectly(content.title, Integer.parseInt(sid),
						content.nid, content.subscribe, bitmap);
			}
		});
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
								 int visibleItemCount, int totalItemCount) {
				if (totalItemCount == 0) return;
				int lastItem = firstVisibleItem + visibleItemCount;
				int itemLeft = 3;
				if (lastItem >= totalItemCount - itemLeft)
					requestMore(NCActivity.ncActivity.lastRequestSid);
			}
		});
		listView.setSelection(NCActivity.ncActivity.onePosition);
	}

	public static void requestMore() {
		if (requesting) return;
		requesting = true;
		int page = NCActivity.ncActivity.currpage + 1;
		Log.w("wants more", page + "");
		requestPage = page;
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String url = Constants.domain + "/pkuhelper/nc/fetch.php?token="
						+ Constants.token + "&p=" + requestPage + "&platform=Android";
				EventHandler eventHandler = NCActivity.ncActivity.eventHandler;
				Parameters parameters = WebConnection.connect(url, new ArrayList<Parameters>());
				if (!"200".equals(parameters.name))
					eventHandler.sendMessage(Message.obtain(eventHandler,
							Constants.MESSAGE_NOTICECENTER_LIST_MORE_FAILED, parameters.name));
				else {
					eventHandler.sendMessage(Message.obtain(eventHandler,
							Constants.MESSAGE_NOTICECENTER_LIST_MORE_FINISHED, parameters.value));
				}
			}
		}).start();
	}

	public static void requestMore(String sid) {
		if (oneRequesting) return;
		oneRequesting = true;
		int page = NCActivity.ncActivity.onepage + 1;
		Log.w("wants more", page + "");
		oneRequestPage = page;
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String url = Constants.domain + "/pkuhelper/nc/fetch.php?token="
						+ Constants.token + "&p=" + oneRequestPage + "&sid=" + NCActivity.ncActivity.lastRequestSid
						+ "&platform=Android";
				EventHandler eventHandler = NCActivity.ncActivity.eventHandler;
				Parameters parameters = WebConnection.connect(url, new ArrayList<Parameters>());
				if (!"200".equals(parameters.name))
					eventHandler.sendMessage(Message.obtain(eventHandler,
							Constants.MESSAGE_NOTICECENTER_ONE_MORE_FAILED, parameters.name));
				else {
					eventHandler.sendMessage(Message.obtain(eventHandler,
							Constants.MESSAGE_NOTICECENTER_ONE_MORE_FINISHED, parameters.value));
				}
			}
		}).start();
	}

	public static void finishMoreRequest(String string) {
		ArrayList<Content> arrayList = new ArrayList<Content>();
		try {
			JSONObject jsonObject = new JSONObject(string);
			JSONArray jsonArray = jsonObject.getJSONArray("data");
			int len = jsonArray.length();

			if (len == 0) {
				if (NCActivity.ncActivity.firstTimeToBottom) {
					CustomToast.showInfoToast(NCActivity.ncActivity, "没有更多了", 1000);
					NCActivity.ncActivity.firstTimeToBottom = false;
				}
				return;
			}

			//NCActivity.ncActivity.contentListArray.clear();
			for (int i = 0; i < len; i++) {
				JSONObject content = jsonArray.getJSONObject(i);
				String timestamp = content.getString("time");
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
				String time = simpleDateFormat.format(new Date(Long.parseLong(timestamp) * 1000));
				arrayList.add(
						new Content(content.getString("nid"), content.getString("title"),
								content.getString("sid"), "",
								content.getString("subscribe"), time));
			}

			//requesting=false;
			NCActivity.ncActivity.contentListArray.addAll(arrayList);
			Log.w("more", NCActivity.ncActivity.contentListArray.size() + "");
			BaseAdapter baseAdapter = (BaseAdapter) NCActivity.ncActivity.contentListView.getAdapter();
			baseAdapter.notifyDataSetChanged();
			NCActivity.ncActivity.currpage = requestPage;
			requesting = false;
		} catch (Exception e) {
		}
	}

	public static void finishMoreRequest(String sid, String string) {
		ArrayList<Content> arrayList = new ArrayList<Content>();
		try {
			JSONObject jsonObject = new JSONObject(string);
			JSONArray jsonArray = jsonObject.getJSONArray("data");
			int len = jsonArray.length();

			if (len == 0) {
				if (NCActivity.ncActivity.oneFirstTimeToBottom) {
					CustomToast.showInfoToast(NCActivity.ncActivity, "没有更多了", 1000);
					NCActivity.ncActivity.oneFirstTimeToBottom = false;
				}
				return;
			}

			//NCActivity.ncActivity.contentListArray.clear();
			for (int i = 0; i < len; i++) {
				JSONObject content = jsonArray.getJSONObject(i);
				String timestamp = content.getString("time");
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
				String time = simpleDateFormat.format(new Date(Long.parseLong(timestamp) * 1000));
				arrayList.add(
						new Content(content.getString("nid"), content.getString("title"),
								content.getString("sid"), "",
								content.getString("subscribe"), time));
			}

			//requesting=false;
			NCActivity.ncActivity.oneListArray.addAll(arrayList);
			Log.w("more", NCActivity.ncActivity.oneListArray.size() + "");
			BaseAdapter baseAdapter = (BaseAdapter) NCActivity.ncActivity.oneListView.getAdapter();
			baseAdapter.notifyDataSetChanged();
			NCActivity.ncActivity.onepage = oneRequestPage;
			oneRequesting = false;
		} catch (Exception e) {
		}
	}

}
