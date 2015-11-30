package com.pkuhelper.noticecenter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.ListView;

import com.pkuhelper.R;
import com.pkuhelper.lib.BaseActivity;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.ViewSetting;

import java.util.ArrayList;
import java.util.HashMap;

public class NCActivity extends BaseActivity {

	static NCActivity ncActivity;
	HashMap<String, Notice> sourceListMap = new HashMap<String, Notice>();
	ArrayList<String> sourceListArray = new ArrayList<String>();
	ArrayList<Content> contentListArray = new ArrayList<Content>();
	ArrayList<Content> oneListArray = new ArrayList<Content>();
	EventHandler eventHandler;
	ListView sourceListView;
	ListView contentListView;
	ListView oneListView;
	SwipeRefreshLayout swipeRefreshLayout;
	public static final int PAGE_VIEW = 1;
	public static final int PAGE_SOURCE = 2;
	public static final int PAGE_ONE_VIEW = 3;
	int nowShowing;
	int currpage;
	int onepage;
	int contentPosition;
	int onePosition;
	boolean firstTimeToBottom = true;
	boolean oneFirstTimeToBottom = true;
	String lastRequestSid = "";

	int screenWidth = 0, screenHeight = 0;

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
		ncActivity = this;
		eventHandler = new EventHandler(getMainLooper());
		Notice.drawableMap = new HashMap<String, Drawable>();
		Notice.courseNotice = new Notice();
		setContentView(R.layout.nc_viewcontent_listview);

		//设置刷新控件
		swipeRefreshLayout = (SwipeRefreshLayout) ncActivity.findViewById(R.id.nc_swipeRefreshLayout);
		swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_purple,
				android.R.color.holo_green_light,
				android.R.color.holo_blue_bright,
				android.R.color.holo_orange_light);

        //设置0.5秒的刷新动画
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			public void onRefresh() {

                NCActivity.ncActivity.handler.sendEmptyMessageDelayed(Constants.MESSAGE_SLEEP_FINISHED, 500);
            }
        });
		swipeRefreshLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				int width = swipeRefreshLayout.getWidth(), height = swipeRefreshLayout.getHeight();
				if (width != 0 && height != 0) {
					screenHeight = height;
					screenWidth = width;
					ViewSetting.setBackground(NCActivity.this, swipeRefreshLayout,
							R.drawable.chat_bg);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						swipeRefreshLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					} else {
						swipeRefreshLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					}
				}
			}
		});

        //获得NCList
		NCList.getAllSources();
	}

	public void finishRequest(int type, String string) {
		if (type == Constants.REQUEST_NOTICECENTER_GETSOURCE)
			NCList.finishGetSource(string);
		else if (type == Constants.REQUEST_NOTICECENTER_SAVESOURCE)
			NCList.realSaveList(string);
		else if (type == Constants.REQUEST_NOTICECENTER_GETCONTENT_ALL)
			NCContent.finishRequest(string);
		else if (type == Constants.REQUEST_NOTICECENTER_COURSE_LOGIN)
			CourseNotice.finishLogin(string);
		else if (type == Constants.REQUEST_NOTICECENTER_COURSE_GETDETAIL)
			CourseNotice.finishGetContent(string);
		else if (type == Constants.REQUEST_NOTICECENTER_GETCONTENT_ONE)
			NCContent.finishOneRequest(string);
		else if (type == Constants.REQUEST_NOTICECENTER_COURSE_GETWEBSITE)
			NCDetail.finishGetCourse(string);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (nowShowing == PAGE_SOURCE)
			menu.add(Menu.NONE, Constants.MENU_NOTICECENTER_SETSOURCE_SAVE,
					Constants.MENU_NOTICECENTER_SETSOURCE_SAVE, "")
					.setIcon(R.drawable.save).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		if (nowShowing == PAGE_VIEW) {
			menu.add(Menu.NONE, Constants.MENU_NOTICECENTER_SHOWCONTENTS_CHOOSE,
					Constants.MENU_NOTICECENTER_SHOWCONTENTS_CHOOSE, "")
					.setIcon(R.drawable.some).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add(Menu.NONE, Constants.MENU_NOTICECENTER_SHOWCONTENTS_SETTINGS,
					Constants.MENU_NOTICECENTER_SHOWCONTENTS_SETTINGS, "")
					.setIcon(R.drawable.set).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == Constants.MENU_NOTICECENTER_SETSOURCE_SAVE) {
			NCList.saveList();
			return true;
		}
		if (id == Constants.MENU_NOTICECENTER_SHOWCONTENTS_SETTINGS) {
			contentPosition = contentListView.getFirstVisiblePosition();
			NCList.showList();
			return true;
		}
		if (id == Constants.MENU_NOTICECENTER_SHOWCONTENTS_CHOOSE) {
			//contentPosition=contentListView.getFirstVisiblePosition();
			//NCList.showList();
			NCList.selectSourceToView();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			if (nowShowing == PAGE_SOURCE) {
				if (NCList.hasModified) {
					new AlertDialog.Builder(this).setTitle("是否保存？")
							.setMessage("你进行了修改，是否保存？").setCancelable(true)
							.setPositiveButton("是", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									NCList.saveList();
								}
							}).setNegativeButton("否", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							NCContent.showContent();
						}
					}).show();
				} else
					NCContent.showContent();
				return true;
			}
			if (nowShowing == PAGE_VIEW) {
				wantToExit();
				return true;
			}
			if (nowShowing == PAGE_ONE_VIEW) {
				NCContent.showContent();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

}
