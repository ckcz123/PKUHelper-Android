package com.pkuhelper.bbs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.ImageRequest;
import com.pkuhelper.lib.MyBitmapFactory;
import com.pkuhelper.lib.MyCalendar;
import com.pkuhelper.lib.MyFile;
import com.pkuhelper.lib.RequestingTask;
import com.pkuhelper.lib.Share;
import com.pkuhelper.lib.Util;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.subactivity.SubActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class ViewPost {
	static ArrayList<PostInfo> postInfos = new ArrayList<PostInfo>();
	static int page;
	static int tmpPage;
	static int totalNum;
	static String tmpThreadid;
	static String title;
	static int index;
	static PostInfo firstFloor = null;
	static String tmpBoard;
	static int selectNum;
	static int selection = 0;

	@SuppressWarnings("unchecked")
	public static void getPosts(String threadid, int page) {
		ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
		arrayList.add(new Parameters("type", "getposts"));
		arrayList.add(new Parameters("page", page + ""));
		arrayList.add(new Parameters("board", ViewActivity.board));
		arrayList.add(new Parameters("threadid", threadid));
		arrayList.add(new Parameters("pagesize", ViewActivity.PAGESIZE + ""));
		arrayList.add(new Parameters("token", Userinfo.token));
		new RequestingTask(ViewActivity.viewActivity, "正在获取内容...",
				Constants.bbsurl, Constants.REQUEST_BBS_GET_POST)
				.execute(arrayList);
		tmpPage = page;
		tmpThreadid = threadid;
	}

	static void finishRequest(String string) {
		try {
			int tmpNum = selectNum;
			selectNum = 0;
			selection = 0;
			JSONObject jsonObject = new JSONObject(string);
			int code = jsonObject.getInt("code");
			if (code != 0) {
				CustomToast.showErrorToast(ViewActivity.viewActivity,
						jsonObject.optString("msg", "获取失败"));
				if (ViewActivity.startFromParent) {
					ViewActivity.viewActivity.setContentView(R.layout.bbs_post_listview);
					ViewActivity.viewActivity.showingPage = ViewActivity.PAGE_POST;
				}
				return;
			}
			totalNum = jsonObject.getInt("totalnum");
			title = jsonObject.optString("title");
			JSONArray datas = jsonObject.getJSONArray("datas");
			postInfos.clear();
			int len = datas.length();
			for (int i = 0; i < len; i++) {
				JSONObject post = datas.getJSONObject(i);
				PostInfo postInfo = new PostInfo(post.optString("author"),
						post.getInt("postid"), post.getInt("number"),
						post.optLong("timestamp"), post.getString("content"), post.optString("attaches"));
				postInfos.add(postInfo);
				int number = post.getInt("number");
				if (tmpNum == number)
					selection = i;
				if (tmpPage == 1 && i == 0)
					firstFloor = new PostInfo(postInfo);
			}

			page = tmpPage;
			ViewActivity.threadid = tmpThreadid;
			viewPosts();
		} catch (Exception e) {
			postInfos.clear();
			CustomToast.showErrorToast(ViewActivity.viewActivity, "获取失败");
		}
	}

	public static void viewPosts() {
		final ViewActivity viewActivity = ViewActivity.viewActivity;
		viewActivity.setContentView(R.layout.bbs_post_listview);
		viewActivity.showingPage = ViewActivity.PAGE_POST;
		viewActivity.invalidateOptionsMenu();
		viewActivity.getActionBar().setTitle("(" + page + "/" + ((totalNum - 1) / ViewActivity.PAGESIZE + 1) + ") " + title);
		final ListView listView = (ListView) viewActivity.findViewById(R.id.bbs_post_listview);
		listView.setAdapter(new BaseAdapter() {

			@SuppressLint("ViewHolder")
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				convertView = viewActivity.getLayoutInflater().inflate(R.layout.bbs_post_item,
						parent, false);
				final PostInfo postInfo = postInfos.get(position);

				ViewSetting.setTextView(convertView, R.id.bbs_post_item_text,
						Html.fromHtml(postInfo.content, new Html.ImageGetter() {
							public Drawable getDrawable(String source) {
								if (source == null) return null;
								if (source.startsWith("/"))
									source = "https://bbs.pku.edu.cn" + source;
								final File file = MyFile.getCache(ViewActivity.viewActivity, Util.getHash(source));
								if (file.exists()) {
									Bitmap bitmap = MyBitmapFactory.getCompressedBitmap(file.getAbsolutePath(), 2.5);
									if (bitmap != null) {
										Drawable drawable = new BitmapDrawable(ViewActivity.viewActivity.getResources(), bitmap);
										drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
										return drawable;
									}
								}
								final String url = source;
								new Thread(new Runnable() {
									public void run() {
										if (MyFile.urlToFile(url, file))
											ViewActivity.viewActivity.handler.sendMessage(
													Message.obtain(ViewActivity.viewActivity.handler, Constants.MESSAGE_IMAGE_REQUEST_FINISHED, postInfo.postid, 0));
									}
								}).start();
								return null;
							}
						}, null));

				ViewSetting.setTextView(convertView, R.id.bbs_post_item_floor, ((page - 1) * ViewActivity.PAGESIZE + position + 1) + "");
				ViewSetting.setTextView(convertView, R.id.bbs_post_item_author,
						postInfo.author);
				ViewSetting.setTextView(convertView, R.id.bbs_post_item_time,
						MyCalendar.format(postInfo.timestamp * 1000));
				convertView.setTag(position);
				return convertView;
			}

			@Override
			public long getItemId(int position) {
				return 0;
			}

			@Override
			public Object getItem(int position) {
				return null;
			}

			@Override
			public int getCount() {
				return postInfos.size();
			}
		});
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
										   int position, long id) {
				index = position;
				return false;
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				index = position;
				PostInfo postInfo = postInfos.get(position);
				if (postInfo.attaches.size() != 0) {
					showAttaches(postInfo.attaches);
					return;
				}
				listView.showContextMenu();
			}
		});
		listView.setSelection(selection);
		selection = 0;
		viewActivity.registerForContextMenu(listView);
	}

	public static void showAttaches(final ArrayList<Parameters> arrayList) {
		int len = arrayList.size();
		if (len == 0) return;
		ArrayList<String> nameArrayList = new ArrayList<String>();
		ArrayList<String> valueArrayList = new ArrayList<String>();
		for (int i = 0; i < len; i++) {
			Parameters parameters = arrayList.get(i);
			nameArrayList.add(parameters.name);
			valueArrayList.add(parameters.value);
		}
		final String[] names = nameArrayList.toArray(new String[nameArrayList.size()]);
		final String[] values = valueArrayList.toArray(new String[valueArrayList.size()]);

		new AlertDialog.Builder(ViewActivity.viewActivity).setTitle("查看附件")
				.setItems(names, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String url = values[which];
						String uurl = url.toLowerCase(Locale.getDefault()).replaceAll("\\?(.+?)*","");
						if (uurl.endsWith(".gif") || uurl.endsWith(".ico") || uurl.endsWith(".jpg")
								|| uurl.endsWith(".jpeg") || uurl.endsWith(".bmp") || uurl.endsWith(".png")) {
							//new ImageRequestingTask(names[which]).execute(url);
							ImageRequest.showImage(ViewActivity.viewActivity, url, names[which]);
						} else {
							Intent intent = new Intent(ViewActivity.viewActivity, SubActivity.class);
							intent.putExtra("type", Constants.SUBACTIVITY_TYPE_WEBVIEW);
							intent.putExtra("url", url);
							intent.putExtra("title", names[which]);
							ViewActivity.viewActivity.startActivity(intent);
						}
					}
				}).show();

	}

	@SuppressLint("InflateParams")
	static void reprint(int index) {
		if ("".equals(Userinfo.token)) {
			CustomToast.showErrorToast(ViewActivity.viewActivity, "请先登录！");
			return;
		}

		final PostInfo postInfo = postInfos.get(index);
		AlertDialog.Builder builder = new AlertDialog.Builder(ViewActivity.viewActivity);
		View view = ViewActivity.viewActivity.getLayoutInflater().inflate(R.layout.bbs_reprint_view,
				null, false);
		builder.setView(view);
		builder.setTitle("转载");

		if (Board.favorite.size() == 0) {
			view.findViewById(R.id.bbs_reprint_layout).setVisibility(View.GONE);
			view.findViewById(R.id.bbs_reprint_error).setVisibility(View.VISIBLE);
			builder.setPositiveButton("确定", null);
			builder.show();
			return;
		}

		final Spinner spinner = (Spinner) view.findViewById(R.id.bbs_reprint);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(builder.getContext(),
				android.R.layout.simple_spinner_item, Board.favorite);
		spinner.setAdapter(adapter);
		spinner.setSelection(0);

		builder.setPositiveButton("转载", new DialogInterface.OnClickListener() {
			@Override
			@SuppressWarnings("unchecked")
			public void onClick(DialogInterface dialog, int which) {
				String text = (String) spinner.getSelectedItem();

				ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
				arrayList.add(new Parameters("type", "reprint"));
				arrayList.add(new Parameters("token", Userinfo.token));
				arrayList.add(new Parameters("board", ViewActivity.board));
				arrayList.add(new Parameters("number", postInfo.number + ""));
				arrayList.add(new Parameters("timestamp", postInfo.timestamp + ""));
				arrayList.add(new Parameters("toboard", text));
				new RequestingTask(ViewActivity.viewActivity, "正在转载...", Constants.bbsurl,
						Constants.REQUEST_BBS_REPRINT).execute(arrayList);
				tmpBoard = text;
			}
		});

		builder.setNegativeButton("取消", null);
		builder.show();

	}

	public static void finishReprint(String string) {
		try {
			JSONObject jsonObject = new JSONObject(string);
			int code = jsonObject.getInt("code");
			if (code != 0) {
				CustomToast.showErrorToast(ViewActivity.viewActivity, jsonObject.optString("msg", "转载失败"),
						1500);
				return;
			}
			CustomToast.showSuccessToast(ViewActivity.viewActivity, "转载成功！", 1500);
			ViewActivity.board = tmpBoard;
			ViewThread.getThreads(1);
		} catch (Exception e) {
			CustomToast.showErrorToast(ViewActivity.viewActivity, "转载失败", 1500);
		} finally {
			tmpBoard = "";
		}
	}

	public static void jump() {
		AlertDialog.Builder builder = new AlertDialog.Builder(ViewActivity.viewActivity);
		builder.setTitle("跳页");
		builder.setNegativeButton("取消", null);
		final Spinner spinner = new Spinner(builder.getContext());
		ArrayList<String> arrayList = new ArrayList<String>();
		int totalPage = (totalNum - 1) / ViewActivity.PAGESIZE + 1;
		for (int i = 1; i <= totalPage; i++)
			arrayList.add(i + "");
		spinner.setAdapter(new ArrayAdapter<String>(builder.getContext(),
				android.R.layout.simple_spinner_item, arrayList));
		builder.setView(spinner);
		spinner.setSelection(page - 1);
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ViewPost.getPosts(ViewActivity.threadid, spinner.getSelectedItemPosition() + 1);
			}
		});
		builder.show();
	}

	public static void share() {
		/*
		Share.readyToShareURL(ViewActivity.viewActivity, "分享帖子",
				"https://bbs.pku.edu.cn/v2/post-read.php?bid=" + ViewActivity.board + "&threadid=" + ViewActivity.threadid,
				title, firstFloor.content, null);
				*/
		CustomToast.showErrorToast(ViewActivity.viewActivity, "暂时不支持分享帖子！");
	}

}
