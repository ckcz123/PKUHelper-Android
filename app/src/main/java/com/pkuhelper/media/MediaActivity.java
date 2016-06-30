package com.pkuhelper.media;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;

import com.pkuhelper.R;
import com.pkuhelper.lib.BaseActivity;
import com.pkuhelper.lib.Constants;

import java.util.ArrayList;
import java.util.Map;

public class MediaActivity extends BaseActivity {
	static MediaActivity mediaActivity;
	ArrayList<Content> arrayList = new ArrayList<Content>();
	EventHandler eventHandler;
	SwipeRefreshLayout swipeRefreshLayout;

	Handler handler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			if (msg.what == Constants.MESSAGE_SLEEP_FINISHED) {
				try {
					swipeRefreshLayout.setRefreshing(false);
				} catch (Exception e) {
				}
				return false;
			}
			return false;
		}
	});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mediaActivity = this;
		eventHandler = new EventHandler(getMainLooper());
		setContentView(R.layout.nc_viewcontent_listview);
		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.nc_swipeRefreshLayout);
		swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_purple,
				android.R.color.holo_green_light,
				android.R.color.holo_blue_bright,
				android.R.color.holo_orange_light);
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			public void onRefresh() {
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
		});
		Source.init();
		arrayList = new ArrayList<Content>();
		MediaList.getContent();
	}

	public void finishRequest(int type, String string) {
		if (type == Constants.REQUEST_MEDIA_FETCH) {
			MediaList.finishRequest(string);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(Menu.NONE, Constants.MENU_MEDIA_CHOOSE,
				Constants.MENU_MEDIA_CHOOSE, "")
				.setIcon(R.drawable.ic_folder_open_white_48dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == Constants.MENU_MEDIA_CHOOSE) {
			ArrayList<String> nameList = new ArrayList<String>();
			final ArrayList<Source> arrayList = new ArrayList<Source>();
			for (Map.Entry<Integer, Source> entry : Source.sources.entrySet()) {
				Source source = entry.getValue();
				arrayList.add(source);
				nameList.add(source.name);
			}
			String[] names = new String[nameList.size() + 1];
			for (int i = 0; i < names.length - 1; i++)
				names[i + 1] = nameList.get(i);
			names[0] = "全部来源";

			new AlertDialog.Builder(this).setTitle("选择你想要查看的源")
					.setItems(names, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (which == 0)
								MediaList.getContent();
							else {
								MediaList.getContent(arrayList.get(which - 1).sid);
							}
						}
					}).show();

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

}
