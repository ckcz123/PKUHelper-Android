package com.pkuhelper.bbs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Html;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.MyCalendar;
import com.pkuhelper.lib.RequestingTask;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ViewThread {
	static ArrayList<ThreadInfo> threadInfos = new ArrayList<ThreadInfo>();
	static int page;
	static int tmpPage;
	static int totalPage;
	static int selection;

	@SuppressWarnings("unchecked")
	public static void getThreads(int page) {
		ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
		arrayList.add(new Parameters("type", "getthreads"));
		arrayList.add(new Parameters("page", page + ""));
		arrayList.add(new Parameters("board", ViewActivity.board));
		arrayList.add(new Parameters("token", Userinfo.token));
		new RequestingTask(ViewActivity.viewActivity, "正在获取内容...",
				"http://www.bdwm.net/client/bbsclient.php", Constants.REQUEST_BBS_GET_LIST)
				.execute(arrayList);
		tmpPage = page;
	}

	static void finishRequest(String string) {
		try {
			JSONObject jsonObject = new JSONObject(string);
			int code = jsonObject.getInt("code");
			if (code != 0) {
				CustomToast.showErrorToast(ViewActivity.viewActivity,
						jsonObject.optString("msg", "获取失败"));
				ViewActivity.viewActivity.setContentView(R.layout.bbs_thread_listview);
				ViewActivity.viewActivity.showingPage = ViewActivity.PAGE_THREAD;
				return;
			}
			totalPage = (jsonObject.optInt("number") - 1) / 20 + 1;
			JSONArray datas = jsonObject.getJSONArray("datas");
			threadInfos.clear();
			int len = datas.length();
			for (int i = 0; i < len; i++) {
				JSONObject thread = datas.getJSONObject(i);
				threadInfos.add(new ThreadInfo(ViewActivity.board, "",
						thread.optString("author"), thread.optLong("timestamp"),
						thread.optString("title"), thread.getInt("threadid"), thread.optInt("top")));
			}

			page = tmpPage;
			selection = 0;
			viewThreads();
		} catch (Exception e) {
			threadInfos.clear();
			CustomToast.showErrorToast(ViewActivity.viewActivity, "获取失败");
		}
	}

	public static void viewThreads() {
		final ViewActivity viewActivity = ViewActivity.viewActivity;
		viewActivity.setContentView(R.layout.bbs_thread_listview);
		viewActivity.showingPage = ViewActivity.PAGE_THREAD;
		viewActivity.invalidateOptionsMenu();
		viewActivity.setTitle("(" + page + "/" + totalPage + ") " + ViewActivity.boardName);
		final ListView listView = (ListView) viewActivity.findViewById(R.id.bbs_thread_listview);
		listView.setAdapter(new BaseAdapter() {

			@SuppressLint("ViewHolder")
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				convertView = viewActivity.getLayoutInflater().inflate(R.layout.bbs_thread_item, parent, false);
				ThreadInfo threadInfo = threadInfos.get(position);
				String title = "<font color='#006060'>" + threadInfo.title + "</font>";
				if (threadInfo.isTop) title = "<font color='red'>[置顶]</font> " + title;
				ViewSetting.setTextView(convertView, R.id.bbs_thread_item_title,
						Html.fromHtml(title));
				ViewSetting.setTextView(convertView, R.id.bbs_thread_item_author,
						threadInfo.author);
				ViewSetting.setTextView(convertView, R.id.bbs_thread_item_time,
						MyCalendar.format(threadInfo.time));
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
				return threadInfos.size();
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				ThreadInfo threadInfo = threadInfos.get(position);
				String threadid = threadInfo.threadid + "";
				selection = listView.getFirstVisiblePosition();
				ViewPost.getPosts(threadid, 1);
			}
		});
		listView.setSelection(selection);
	}

	public static void jump() {
		AlertDialog.Builder builder = new AlertDialog.Builder(ViewActivity.viewActivity);
		builder.setTitle("跳页");
		builder.setNegativeButton("取消", null);
		if (totalPage >= 300) {
			final EditText editText = new EditText(builder.getContext());
			editText.setHint("1-" + totalPage);
			editText.setInputType(InputType.TYPE_CLASS_NUMBER);
			builder.setView(editText);
			builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					int pg = Integer.parseInt(editText.getEditableText().toString());
					if (pg < 1 || pg > totalPage) {
						CustomToast.showErrorToast(ViewActivity.viewActivity, "无效的页码", 1200);
						return;
					}
					ViewThread.getThreads(pg);
				}
			});
		} else {
			final Spinner spinner = new Spinner(builder.getContext());
			ArrayList<String> arrayList = new ArrayList<String>();
			for (int i = 1; i <= totalPage; i++)
				arrayList.add(i + "");
			spinner.setAdapter(new ArrayAdapter<String>(builder.getContext(),
					android.R.layout.simple_spinner_item, arrayList));
			builder.setView(spinner);
			spinner.setSelection(page - 1);
			builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ViewThread.getThreads(spinner.getSelectedItemPosition() + 1);
				}
			});
		}
		builder.show();
	}

}
