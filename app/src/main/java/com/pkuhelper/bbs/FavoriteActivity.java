package com.pkuhelper.bbs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.pkuhelper.R;
import com.pkuhelper.lib.BaseActivity;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.Editor;
import com.pkuhelper.lib.MyCalendar;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FavoriteActivity extends BaseActivity {

	private static ArrayList<SearchInfo> arrayList=null;
	ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setTitle("收藏的主题帖");
		load(this);
		setContentView(R.layout.bbs_thread_listview);
		listView=(ListView)findViewById(R.id.bbs_thread_listview);
		listView.setAdapter(new BaseAdapter() {
			@Override
			public int getCount() {
				return arrayList.size();
			}

			@Override
			public Object getItem(int position) {
				return null;
			}

			@Override
			public long getItemId(int position) {
				return 0;
			}

			@SuppressLint("ViewHolder")
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				//return null;
				SearchInfo threadInfo=arrayList.get(position);
				convertView = getLayoutInflater().inflate(
						R.layout.bbs_thread_item, parent, false);
				String title = "<font color='#006060'>" + threadInfo.title + "</font>";
				ViewSetting.setTextView(convertView, R.id.bbs_thread_item_title,
						Html.fromHtml(title));
				ViewSetting.setTextView(convertView, R.id.bbs_thread_item_author,
						threadInfo.board + " / " + threadInfo.author);
				ViewSetting.setTextViewColor(convertView, R.id.bbs_thread_item_author,
						Color.parseColor("#333333"));
				ViewSetting.setTextView(convertView, R.id.bbs_thread_item_time,
						"收藏于 " + MyCalendar.format(threadInfo.timestamp, "yyyy-MM-dd"));
				ViewSetting.setTextViewColor(convertView, R.id.bbs_thread_item_time,
						Color.parseColor("#333333"));
				return convertView;
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				SearchInfo threadInfo = arrayList.get(position);
				Intent intent = new Intent(FavoriteActivity.this, ViewActivity.class);
				intent.putExtra("board", threadInfo.board);
				intent.putExtra("threadid", threadInfo.threadid + "");
				intent.putExtra("type", "thread");
				startActivity(intent);
			}
		});
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				final SearchInfo threadInfo = arrayList.get(position);
				String[] strings = new String[]{"删除项目"};
				new AlertDialog.Builder(FavoriteActivity.this)
						.setItems(strings, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								delFavorite(FavoriteActivity.this,
										threadInfo.board, threadInfo.threadid);
								((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
							}
						}).show();
				return true;
			}
		});
		if (arrayList.size()==0) {
			CustomToast.showInfoToast(this, "你还没有收藏的帖子！");
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Constants.MENU_SUBACTIVITY_CLOSE, Constants.MENU_SUBACTIVITY_CLOSE, "")
				.setIcon(R.drawable.close).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId()==Constants.MENU_SUBACTIVITY_CLOSE) {
			wantToExit();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private static void load(Context context) {
		String favorite=Editor.getString(context, "bbs_favorite_posts", "[]");
		arrayList=new ArrayList<>();
		try {
			JSONArray jsonArray=new JSONArray(favorite);
			int len=jsonArray.length();
			for (int i=0;i<len;i++) {
				JSONObject jsonObject=jsonArray.getJSONObject(i);
				SearchInfo searchInfo=new SearchInfo(jsonObject.getString("board"),
						jsonObject.getInt("threadid"), jsonObject.getString("title"),
						jsonObject.optString("author"), 0,
						jsonObject.getLong("time"));
				arrayList.add(searchInfo);
			}
		}
		catch (Exception e) {}
	}

	private static void save(Context context) {
		if (arrayList==null) return;
		try {
			Collections.sort(arrayList, new Comparator<SearchInfo>() {
				@Override
				public int compare(SearchInfo lhs, SearchInfo rhs) {
					return (int)(rhs.timestamp-lhs.timestamp);
				}
			});
			JSONArray jsonArray=new JSONArray();
			int len=arrayList.size();
			for (int i=0;i<len;i++) {
				SearchInfo searchInfo=arrayList.get(i);
				JSONObject jsonObject=new JSONObject();
				jsonObject.put("board", searchInfo.board);
				jsonObject.put("threadid", searchInfo.threadid);
				jsonObject.put("title", searchInfo.title);
				jsonObject.put("author", searchInfo.author);
				jsonObject.put("time", searchInfo.timestamp);
				jsonArray.put(jsonObject);
			}
			Editor.putString(context, "bbs_favorite_posts", jsonArray.toString());
		}
		catch (Exception e) {CustomToast.showErrorToast(context, "保存失败");}
	}

	public static boolean addFavorite(Context context,
								   String board, int threadid, String title,
								   String boardname) {
		if (arrayList==null) load(context);
		for (SearchInfo info: arrayList.toArray(new SearchInfo[arrayList.size()])) {
			if (info.threadid==threadid && info.board.equals(board)) {
				CustomToast.showInfoToast(context, "此主题已收藏！");
				return false;
			}
		}
		//infoHashMap.put(System.currentTimeMillis(), searchInfo);
		arrayList.add(new SearchInfo(board, threadid, title, boardname, 0, System.currentTimeMillis()));
		save(context);
		return true;
	}

	public static void delFavorite(Context context, String board, int threadid) {
		if (arrayList==null) load(context);
		for (SearchInfo info: arrayList.toArray(new SearchInfo[arrayList.size()])) {
			if (info.threadid==threadid && info.board.equals(board)) {
				arrayList.remove(info);
				save(context);
				return;
			}
		}
	}

	@Override
	protected void finishRequest(int type, String string) {

	}

}
