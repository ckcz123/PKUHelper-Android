package com.pkuhelper.lostfound;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.pkuhelper.R;
import com.pkuhelper.chat.ChatActivity;
import com.pkuhelper.lib.BaseActivity;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;

@SuppressLint({"UseSparseArrays", "InflateParams"})
public class LostFoundActivity extends BaseActivity {
	public HashMap<Integer, LostFoundInfo> lostMap = new HashMap<Integer, LostFoundInfo>();
	public HashMap<Integer, LostFoundInfo> foundMap = new HashMap<Integer, LostFoundInfo>();
	public HashMap<Integer, LostFoundInfo> myMap = new HashMap<Integer, LostFoundInfo>();
	public ArrayList<Integer> lostArray = null;
	public ArrayList<Integer> foundArray = null;
	public ArrayList<Integer> myArray = null;
	public static final int PAGE_LOST = 1;
	public static final int PAGE_FOUND = 2;
	public static final int PAGE_MINE = 3;

	int page;
	int lostpage = 0, foundpage = 0;
	boolean lostrequesting = false, foundrequesting = false;
	String toUid;

	ListView listView;
	View headerView;
	View topView;
	SwipeRefreshLayout swipeRefreshLayout;

	String deletingType = "";

	Handler handler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			if (msg.what == Constants.MESSAGE_LOSTFOUND_IMAGE_REQUEST_FINISHED) {
				updateImage();
				return true;
			}
			if (msg.what == Constants.MESSAGE_LOSTFOUND_REFRESH_FAILED) {
				setRefreshing();
				return true;
			}
			if (msg.what == Constants.MESSAGE_LOSTFOUND_REFRESH_FINISHED) {
				finishRefresh(msg.arg1, (String) msg.obj);
				setRefreshing();
				return true;
			}
			if (msg.what == Constants.MESSAGE_LOSTFOUND_LOAD_MORE_FINISHED) {
				finishMore(msg.arg1, msg.arg2, (String) msg.obj);
				return true;
			}
			if (msg.what == Constants.MESSAGE_SLEEP_FINISHED) {
				swipeRefreshLayout.setRefreshing(false);
				return true;
			}
			return false;
		}
	});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getActionBar().setTitle("失物招领");
		setContentView(R.layout.lf_listview);

		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.lostfound_swipeRefreshLayout);
		swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_purple,
				android.R.color.holo_green_light,
				android.R.color.holo_blue_bright,
				android.R.color.holo_orange_light);

		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			public void onRefresh() {
				pullToRefresh(page);
			}
		});
		swipeRefreshLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				int width = swipeRefreshLayout.getWidth(), height = swipeRefreshLayout.getHeight();
				if (width != 0 && height != 0) {
					ViewSetting.setBackground(LostFoundActivity.this, swipeRefreshLayout,
							R.drawable.chat_bg);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						swipeRefreshLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					} else {
						swipeRefreshLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					}
				}
			}
		});

		page = PAGE_LOST;
		listView = (ListView) findViewById(R.id.lostfound_listview);
		headerView = getLayoutInflater().inflate(R.layout.lf_headerview, null, false);
		listView.addHeaderView(headerView);
		setHeaderView();
		topView = findViewById(R.id.lostfound_returntop);
		topView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					listView.smoothScrollToPosition(0);
				} catch (Exception e) {
				}
			}
		});

		show(page);

	}

	public void setListviewNothing() {
		listView.setAdapter(new BaseAdapter() {
			public View getView(int position, View convertView, ViewGroup parent) {
				return null;
			}

			public long getItemId(int position) {
				return 0;
			}

			public Object getItem(int position) {
				return null;
			}

			public int getCount() {
				return 0;
			}
		});
	}

	public void pullToRefresh(final int pg) {
		if (pg == PAGE_MINE) {
			setRefreshing();
			return;
		}
		swipeRefreshLayout.setRefreshing(true);
		new Thread(new Runnable() {
			public void run() {
				if (pg != PAGE_LOST && pg != PAGE_FOUND) {
					handler.sendEmptyMessage(Constants.MESSAGE_LOSTFOUND_REFRESH_FAILED);
					return;
				}
				String type = pg == PAGE_FOUND ? "found" : "lost";
				Parameters parameters = WebConnection.connect(
						Constants.domain + "/services/LFList.php?type=" + type + "&page=0", null);
				if (!"200".equals(parameters.name)) {
					handler.sendEmptyMessage(Constants.MESSAGE_LOSTFOUND_REFRESH_FAILED);
					return;
				} else {
					handler.sendMessage(Message.obtain(handler, Constants.MESSAGE_LOSTFOUND_REFRESH_FINISHED, pg, 0, parameters.value));
				}
			}
		}).start();
	}

	public void setRefreshing() {
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(500);
				} catch (Exception e) {
				}
				handler.sendEmptyMessage(Constants.MESSAGE_SLEEP_FINISHED);
			}
		}).start();
	}

	public void finishRefresh(int pg, String string) {
		try {
			JSONObject jsonObject = new JSONObject(string);
			JSONArray array = jsonObject.getJSONArray("data");
			int len = array.length();
			ArrayList<Integer> arrayList = new ArrayList<Integer>();
			HashMap<Integer, LostFoundInfo> hashMap = new HashMap<Integer, LostFoundInfo>();
			for (int i = 0; i < len; i++) {
				JSONObject item = array.getJSONObject(i);
				int id = item.getInt("id");
				LostFoundInfo lostFoundInfo = new LostFoundInfo(this, handler, id,
						item.getString("name"),
						item.getString("lost_or_found"),
						item.getString("type"), item.getString("detail"),
						item.getLong("post_time"), item.getLong("action_time"),
						item.getString("image"), item.getString("poster_uid"),
						item.getString("poster_phone"), item.getString("poster_name"),
						item.getString("poster_college"));
				hashMap.put(id, lostFoundInfo);
				arrayList.add(id);
			}
			if (pg == PAGE_LOST) {
				int pos = 0;
				int id0 = lostMap.get(lostArray.get(0)).id;
				for (int j = 0; j < len; j++) {
					if (id0 == hashMap.get(arrayList.get(j)).id) {
						pos = j;
					}
				}
				if (pos != 0) {
					lostMap.putAll(hashMap);
					int cnt = lostArray.size();
					lostArray.addAll(0, arrayList.subList(0, pos));
					while (lostArray.size() > cnt)
						lostArray.remove(lostArray.size() - 1);
				}
			} else if (pg == PAGE_FOUND) {
				int pos = 0;
				int id0 = foundMap.get(foundArray.get(0)).id;
				for (int j = 0; j < len; j++) {
					if (id0 == hashMap.get(arrayList.get(j)).id) {
						pos = j;
					}
				}
				if (pos != 0) {
					foundMap.putAll(hashMap);
					int cnt = foundArray.size();
					foundArray.addAll(0, arrayList.subList(0, pos));
					while (foundArray.size() > cnt)
						foundArray.remove(foundArray.size() - 1);
				}
			}

			if (pg == page)
				((BaseAdapter) ((HeaderViewListAdapter) listView.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setHeaderView() {
		View lostView = headerView.findViewById(R.id.lf_lost);
		View foundView = headerView.findViewById(R.id.lf_found);
		View mineView = headerView.findViewById(R.id.lf_mine);

		lostView.setBackgroundResource(R.drawable.lf_btn_left);
		foundView.setBackgroundResource(R.drawable.lf_btn_middle);
		mineView.setBackgroundResource(R.drawable.lf_btn_right);
		ViewSetting.setTextViewColor(headerView, R.id.lf_lost, Color.parseColor("#333333"));
		ViewSetting.setTextViewColor(headerView, R.id.lf_found, Color.parseColor("#333333"));
		ViewSetting.setTextViewColor(headerView, R.id.lf_mine, Color.parseColor("#333333"));

		if (page == PAGE_LOST) {
			lostView.setBackgroundResource(R.drawable.lf_btn_left_selected);
			ViewSetting.setTextViewColor(headerView, R.id.lf_lost, Color.parseColor("#e8e8e7"));
		} else if (page == PAGE_FOUND) {
			foundView.setBackgroundResource(R.drawable.lf_btn_middle_selected);
			ViewSetting.setTextViewColor(headerView, R.id.lf_found, Color.parseColor("#e8e8e7"));
		} else if (page == PAGE_MINE) {
			mineView.setBackgroundResource(R.drawable.lf_btn_right_selected);
			ViewSetting.setTextViewColor(headerView, R.id.lf_mine, Color.parseColor("#e8e8e7"));
		}

		lostView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (page == PAGE_LOST) return;
				show(PAGE_LOST);
			}
		});
		foundView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (page == PAGE_FOUND) return;
				show(PAGE_FOUND);
			}
		});
		mineView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (page == PAGE_MINE) return;
				show(PAGE_MINE);
			}
		});

	}

	public void show(int pg) {
		if (pg != PAGE_FOUND && pg != PAGE_MINE) pg = PAGE_LOST;
		if (pg == PAGE_LOST) show(pg, lostArray, lostMap);
		else if (pg == PAGE_FOUND) show(pg, foundArray, foundMap);
		else if (pg == PAGE_MINE) show(pg, myArray, myMap);
	}

	private void show(final int pg, final ArrayList<Integer> arrayList,
					  final HashMap<Integer, LostFoundInfo> map) {
		page = pg;
		if (arrayList == null) {
			setListviewNothing();
			request(pg);
			return;
		}
		listView.setAdapter(new BaseAdapter() {

			@SuppressLint("ViewHolder")
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				int id = arrayList.get(position);
				LostFoundInfo lostFoundInfo = map.get(id);
				convertView = getLayoutInflater().inflate(R.layout.lostfound_item, parent, false);
				ViewSetting.setTextView(convertView, R.id.lostfound_item_name, lostFoundInfo.name);
				String detail = new String(lostFoundInfo.detail);
				if (detail.length() >= 35) detail = detail.substring(0, 33) + "...";
				ViewSetting.setTextView(convertView, R.id.lostfound_item_detail, detail);
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
				String atimeString =
						(lostFoundInfo.lost_or_found == LostFoundInfo.LOST ? "丢失" : "拾到") +
								"于 " + simpleDateFormat.format(new Date(lostFoundInfo.actiontime * 1000));
				String ptimeString = "发布于 " + simpleDateFormat.format(new Date(lostFoundInfo.posttime * 1000));
				ViewSetting.setTextView(convertView, R.id.lostfound_item_posttime, ptimeString);
				ViewSetting.setTextView(convertView, R.id.lostfound_item_actiontime, atimeString);
				Bitmap bitmap = lostFoundInfo.getBitmap();
				if (bitmap != null) {
					convertView.findViewById(R.id.lostfound_item_image).setVisibility(View.VISIBLE);
					ViewSetting.setImageBitmap(convertView, R.id.lostfound_item_image, bitmap);
				}
				convertView.setTag(id);

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
				return arrayList.size();
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				try {
					int nid = (Integer) view.getTag();
					LostFoundInfo lostFoundInfo = null;
					if (page == PAGE_LOST) lostFoundInfo = lostMap.get(nid);
					else if (page == PAGE_FOUND) lostFoundInfo = foundMap.get(nid);
					else if (page == PAGE_MINE) lostFoundInfo = myMap.get(nid);
					if (lostFoundInfo == null) return;
					Detail.showDetail(LostFoundActivity.this, lostFoundInfo);
				} catch (Exception e) {
				}
			}
		});
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {

			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
								 int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem >= 5) {
					if (topView.getVisibility() == View.GONE)
						topView.setVisibility(View.VISIBLE);
				} else {
					if (topView.getVisibility() == View.VISIBLE)
						topView.setVisibility(View.GONE);
				}

				if (totalItemCount != 0) {
					int lastItem = firstVisibleItem + visibleItemCount;
					int itemLeft = 3;
					if (lastItem >= totalItemCount - itemLeft)
						requestMore(page);
				}
				try {
					swipeRefreshLayout.setEnabled(firstVisibleItem == 0
							&& view.getChildAt(0).getTop() >= 0);
				} catch (Exception e) {
				}
			}
		});
		setHeaderView();
	}

	void updateImage() {
		if (listView == null) return;
		int cnt = listView.getCount();
		for (int i = 0; i < cnt; i++) {
			try {
				View view = listView.getChildAt(i);
				if (view.findViewById(R.id.lostfound_item_image).getVisibility() == View.VISIBLE)
					continue;
				int id = (Integer) view.getTag();
				LostFoundInfo lostFoundInfo = null;
				if (page == PAGE_LOST) lostFoundInfo = lostMap.get(id);
				else if (page == PAGE_FOUND) lostFoundInfo = foundMap.get(id);
				else if (page == PAGE_MINE) lostFoundInfo = myMap.get(id);
				if (lostFoundInfo == null) continue;
				Bitmap bitmap = lostFoundInfo.getBitmap();
				if (bitmap == null) return;
				view.findViewById(R.id.lostfound_item_image).setVisibility(View.VISIBLE);
				ViewSetting.setImageBitmap(view, R.id.lostfound_item_image, bitmap);
			} catch (Exception e) {
			}
		}


	}

	@SuppressWarnings("unchecked")
	public void request(int pg) {
		String hint = "";
		String type = "", token = "";
		if (pg == PAGE_LOST) {
			type = "lost";
			hint = "失物信息";
		} else if (pg == PAGE_FOUND) {
			type = "found";
			hint = "招领信息";
		} else if (pg == PAGE_MINE) {
			token = Constants.token;
			hint = "我发布的失物招领";
		}

		new RequestingTask(this, "正在获取" + hint + "...",
				Constants.domain + "/services/LFList.php?type=" + type + "&page=0&token=" + token,
				Constants.REQUEST_LOSTFOUND_GET).execute(new ArrayList<Parameters>());
	}

	public void requestMore(final int pg) {
		if (pg != PAGE_LOST && pg != PAGE_FOUND) return;
		if (pg == PAGE_LOST) {
			if (lostrequesting) return;
			lostrequesting = true;
		}
		if (pg == PAGE_FOUND) {
			if (foundrequesting) return;
			foundrequesting = true;
		}
		new Thread(new Runnable() {
			public void run() {
				int requestpage = pg == PAGE_LOST ? lostpage + 1 : foundpage + 1;
				String type = pg == PAGE_LOST ? "lost" : "found";
				Parameters parameters = WebConnection.connect(
						Constants.domain + "/services/LFList.php?type=" + type + "&page=" + requestpage
						, null);
				if (!"200".equals(parameters.name)) return;
				handler.sendMessage(Message.obtain(handler, Constants.MESSAGE_LOSTFOUND_LOAD_MORE_FINISHED, pg, requestpage, parameters.value));
			}
		}).start();
	}

	public void finishMore(int pg, int requestpage, String string) {
		try {
			JSONObject jsonObject = new JSONObject(string);
			JSONArray array = jsonObject.getJSONArray("data");
			int len = array.length();
			ArrayList<Integer> arrayList = new ArrayList<Integer>();
			HashMap<Integer, LostFoundInfo> hashMap = new HashMap<Integer, LostFoundInfo>();
			for (int i = 0; i < len; i++) {
				JSONObject item = array.getJSONObject(i);
				int id = item.getInt("id");
				LostFoundInfo lostFoundInfo = new LostFoundInfo(this, handler, id,
						item.getString("name"),
						item.getString("lost_or_found"),
						item.getString("type"), item.getString("detail"),
						item.getLong("post_time"), item.getLong("action_time"),
						item.getString("image"), item.getString("poster_uid"),
						item.getString("poster_phone"), item.getString("poster_name"),
						item.getString("poster_college"));
				hashMap.put(id, lostFoundInfo);
				arrayList.add(id);
			}
			if (pg == PAGE_LOST) {
				lostArray.addAll(arrayList);
				lostMap.putAll(hashMap);
				lostpage = requestpage;
				lostrequesting = false;
			} else if (pg == PAGE_FOUND) {
				foundArray.addAll(arrayList);
				foundMap.putAll(hashMap);
				foundpage = requestpage;
				foundrequesting = false;
			}

			if (pg == page)
				((BaseAdapter) ((HeaderViewListAdapter) listView.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void finishRequest(int type, String string) {
		if (type == Constants.REQUEST_LOSTFOUND_GET) {
			try {
				JSONObject jsonObject = new JSONObject(string);
				JSONArray array = jsonObject.getJSONArray("data");
				int len = array.length();
				ArrayList<Integer> arrayList = new ArrayList<Integer>();
				HashMap<Integer, LostFoundInfo> hashMap = new HashMap<Integer, LostFoundInfo>();
				for (int i = 0; i < len; i++) {
					JSONObject item = array.getJSONObject(i);
					int id = item.getInt("id");
					LostFoundInfo lostFoundInfo = new LostFoundInfo(this, handler, id,
							item.getString("name"),
							item.getString("lost_or_found"),
							item.getString("type"), item.getString("detail"),
							item.getLong("post_time"), item.getLong("action_time"),
							item.getString("image"), item.getString("poster_uid"),
							item.getString("poster_phone"), item.getString("poster_name"),
							item.getString("poster_college"));
					hashMap.put(id, lostFoundInfo);
					arrayList.add(id);
				}
				if (page == PAGE_LOST) {
					lostArray = new ArrayList<Integer>(arrayList);
					lostMap = new HashMap<Integer, LostFoundInfo>(hashMap);
					lostpage = 0;
					lostrequesting = false;
				} else if (page == PAGE_FOUND) {
					foundArray = new ArrayList<Integer>(arrayList);
					foundMap = new HashMap<Integer, LostFoundInfo>(hashMap);
					foundpage = 0;
					foundrequesting = false;
				} else if (page == PAGE_MINE) {
					myArray = new ArrayList<Integer>(arrayList);
					myMap = new HashMap<Integer, LostFoundInfo>(hashMap);
					if (len == 0)
						CustomToast.showInfoToast(this, "你没有发布过失物招领！");
				}
				show(page);
			} catch (Exception e) {
				e.printStackTrace();
				CustomToast.showErrorToast(this, "获取失败");
			}
		} else if (type == Constants.REQUEST_LOSTFOUND_DELETE) {
			if (deletingType.equals("lost")) lostArray = null;
			else if (deletingType.equals("found")) foundArray = null;
			myArray = null;
			show(PAGE_MINE);
		} else if (type == Constants.REQUEST_FOUND_USERNAME) {
			finishSendFound(string);
		}
	}

	void finishDelete(String string) {
		Scanner scanner = new Scanner(string);
		int code = scanner.nextInt();
		if (code == 0) {
			CustomToast.showSuccessToast(this, "删除成功！");
			myMap = null;
			if (page == PAGE_MINE)
				show(page);
		} else
			CustomToast.showErrorToast(this, "删除失败，请重试");
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(Menu.NONE, Constants.MENU_LOSTFOUND_ADD, Constants.MENU_LOSTFOUND_ADD, "")
				.setIcon(R.drawable.add).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(Menu.NONE, Constants.MENU_LOSTFOUND_CLOSE, Constants.MENU_LOSTFOUND_CLOSE, "")
				.setIcon(R.drawable.close).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	public void add() {
		new AlertDialog.Builder(this).setItems(new String[]{"发布失物招领", "为捡到的校园卡寻找失主"},
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == 0) {
							startActivityForResult(new Intent(LostFoundActivity.this, AddActivity.class), 0);
						} else {
							sendFound();
						}
					}
				}).show();

	}

	public void sendFound() {
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.settings_found);
		dialog.setTitle("为捡到的校园卡寻找失主");
		Spinner spinner = (Spinner) dialog.findViewById(R.id.settings_found_type);
		final String[] strings = {"校园卡", "学生证", "其他"};
		spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, strings));
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
									   int position, long id) {
				EditText editText = (EditText) dialog.findViewById(R.id.settings_found_name);
				if (position != 2) {
					editText.setText(strings[position]);
					editText.setEnabled(false);
				} else {
					editText.setEnabled(true);
					editText.setText("");
					editText.requestFocus();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		spinner.setSelection(0);

		ViewSetting.setOnClickListener(dialog, R.id.settings_found_send, new View.OnClickListener() {
			@Override
			@SuppressWarnings("unchecked")
			public void onClick(View v) {

				String to = ViewSetting.getEditTextValue(dialog, R.id.settings_found_username);
				String name = ViewSetting.getEditTextValue(dialog, R.id.settings_found_name);
				String phone = ViewSetting.getEditTextValue(dialog, R.id.settings_found_phone);

				if ("".equals(to) || "".equals(name) || "".equals(phone)) {
					CustomToast.showInfoToast(LostFoundActivity.this, "信息不能为空！", 1300);
					return;
				}

				String content = to + "同学你好！我拾到了你的" + name + "，请尽快找我认领，谢谢。我的联系方式是" + phone + "。";

				ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
				arrayList.add(new Parameters("to", to));
				arrayList.add(new Parameters("content", content));
				arrayList.add(new Parameters("type", "sendmsg"));
				arrayList.add(new Parameters("token", Constants.token));

				new RequestingTask(LostFoundActivity.this, "正在发送...", Constants.domain + "/services/msg.php",
						Constants.REQUEST_FOUND_USERNAME).execute(arrayList);

				toUid = to;

				dialog.dismiss();
			}
		});
		ViewSetting.setOnClickListener(dialog, R.id.settings_found_cancel, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	public void finishSendFound(String string) {
		try {
			JSONObject jsonObject = new JSONObject(string);
			int code = jsonObject.getInt("code");
			if (code != 0) {
				CustomToast.showErrorToast(this, jsonObject.optString("msg", "发送失败"));
				return;
			}
			//ChatDetail.showDetail(toUid, toUid);

			Intent intent = new Intent(this, ChatActivity.class);
			intent.putExtra("uid", toUid);
			startActivity(intent);

		} catch (Exception e) {
			CustomToast.showErrorToast(this, "发送失败");
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == Constants.MENU_LOSTFOUND_ADD) {
			add();
			// startActivityForResult(new Intent(this, AddActivity.class), 0);
			return true;
		}
		if (id == Constants.MENU_LOSTFOUND_CLOSE) {
			wantToExit();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) return;
		if (requestCode == 0) {
			swipeRefreshLayout.setRefreshing(true);
			myArray = null;
			if (page == PAGE_MINE) show(page);
			String type = data.getStringExtra("type");
			if ("lost".equals(type))
				pullToRefresh(PAGE_LOST);
			else if ("found".equals(type))
				pullToRefresh(PAGE_FOUND);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
