package com.pkuhelper.bbs;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
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

public class SearchFragment extends Fragment {
	static View searchView;
	static String searchBoard;
	static ArrayList<String> boards = new ArrayList<String>();
	static ArrayList<SearchInfo> searchInfos = new ArrayList<SearchInfo>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.bbs_search_view, container, false);
		searchView = rootView;
		set();
		return rootView;
	}

	public static void set() {
		updateSpinner();
		Spinner spinner = (Spinner) searchView.findViewById(R.id.bbs_search_type);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(BBSActivity.bbsActivity,
				android.R.layout.simple_spinner_item, new String[]{"搜索标题", "搜索全文", "搜索作者"});
		spinner.setAdapter(adapter);
		ViewSetting.setOnClickListener(searchView, R.id.bbs_search_confirm, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				search();
			}
		});

		boards = new ArrayList<String>(Board.favorite);
		if (boards.size() == 0) boards.add("(null)");

		Spinner boardspinner = (Spinner) searchView.findViewById(R.id.bbs_search_board);
		ArrayAdapter<String> boardadapter = new ArrayAdapter<String>(BBSActivity.bbsActivity,
				android.R.layout.simple_spinner_item, boards);
		boardspinner.setAdapter(boardadapter);

		ListView listView = (ListView) searchView.findViewById(R.id.bbs_search_listview);
		listView.setAdapter(new BaseAdapter() {

			@SuppressLint("ViewHolder")
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				SearchInfo threadInfo = searchInfos.get(position);
				convertView = BBSActivity.bbsActivity.getLayoutInflater().inflate(
						R.layout.bbs_thread_item, parent, false);
				String title = "<font color='#006060'>" + threadInfo.title + "</font>";
				ViewSetting.setTextView(convertView, R.id.bbs_thread_item_title,
						Html.fromHtml(title));
				ViewSetting.setTextView(convertView, R.id.bbs_thread_item_author,
						threadInfo.author);
				ViewSetting.setTextView(convertView, R.id.bbs_thread_item_time,
						MyCalendar.format(threadInfo.timestamp * 1000));
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
				return searchInfos.size();
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				SearchInfo threadInfo = searchInfos.get(position);
				Intent intent = new Intent(BBSActivity.bbsActivity, ViewActivity.class);
				intent.putExtra("board", threadInfo.board);
				intent.putExtra("threadid", threadInfo.threadid + "");
				intent.putExtra("number", threadInfo.number);
				intent.putExtra("type", "thread");
				BBSActivity.bbsActivity.startActivity(intent);
			}
		});
	}

	public static void updateSpinner() {
		boards = new ArrayList<String>(Board.favorite);
		if (boards.size() == 0) boards.add("(null)");
		try {
			Spinner spinner = (Spinner) searchView.findViewById(R.id.bbs_search_board);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(BBSActivity.bbsActivity,
					android.R.layout.simple_spinner_item, boards);
			spinner.setAdapter(adapter);
		} catch (Exception e) {
		}
	}

	@SuppressWarnings("unchecked")
	public static void search() {
		Spinner boardSpinner = (Spinner) searchView.findViewById(R.id.bbs_search_board);
		String string = (String) boardSpinner.getSelectedItem();
		if ("".equals(string) || "(null)".equals(string)
				|| !Board.favorite.contains(string)) {
			CustomToast.showInfoToast(BBSActivity.bbsActivity, "请选择版面！", 1500);
			return;
		}
		String text = ViewSetting.getEditTextValue(searchView, R.id.bbs_search_text).trim();
		if ("".equals(text)) {
			CustomToast.showInfoToast(BBSActivity.bbsActivity, "没有搜索的内容！", 1500);
			return;
		}
		Spinner searchType = (Spinner) searchView.findViewById(R.id.bbs_search_type);
		String type = "title";
		int index = searchType.getSelectedItemPosition();
		if (index == 1) type = "text";
		if (index == 2) type = "author";

		ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
		arrayList.add(new Parameters("type", "search"));
		arrayList.add(new Parameters("token", Userinfo.token));
		arrayList.add(new Parameters("board", string));
		arrayList.add(new Parameters("search", type));
		arrayList.add(new Parameters("text", text));

		new RequestingTask(BBSActivity.bbsActivity, "正在搜索..",
				"http://www.bdwm.net/client/bbsclient.php", Constants.REQUEST_BBS_SEARCH)
				.execute(arrayList);
		searchBoard = new String(string);
	}

	public static void finishSearch(String string) {
		try {
			JSONObject jsonObject = new JSONObject(string);
			int code = jsonObject.getInt("code");
			if (code != 0) {
				CustomToast.showErrorToast(BBSActivity.bbsActivity,
						jsonObject.optString("msg", "搜索失败"), 2000);
				return;
			}

			JSONArray jsonArray = jsonObject.getJSONArray("datas");
			searchInfos.clear();
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				JSONObject object = jsonArray.getJSONObject(i);
				searchInfos.add(new SearchInfo(searchBoard, object.getInt("threadid"),
						object.getString("title"), object.optString("author"),
						object.optInt("number"), object.optLong("timestamp")));
			}

			ListView listView = (ListView) searchView.findViewById(R.id.bbs_search_listview);
			BaseAdapter baseAdapter = (BaseAdapter) listView.getAdapter();
			baseAdapter.notifyDataSetChanged();

			if (jsonObject.optInt("hasmore") == 1)
				CustomToast.showInfoToast(BBSActivity.bbsActivity, "只显示前100条结果！",
						1500);
			if (len == 0)
				CustomToast.showInfoToast(BBSActivity.bbsActivity, "没有搜索到结果！",
						1500);

		} catch (Exception e) {
			CustomToast.showErrorToast(BBSActivity.bbsActivity, "搜索失败", 1500);
		}
	}

}
