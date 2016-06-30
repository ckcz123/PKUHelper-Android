package com.pkuhelper.subactivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

import com.pkuhelper.R;
import com.pkuhelper.chat.ChatActivity;
import com.pkuhelper.lib.BaseActivity;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.DataObject;
import com.pkuhelper.lib.Share;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;

import java.util.Locale;

public class SubActivity extends BaseActivity {

	WebView webView;
	Picture picture;
	GifView gifView;
	MyWebView myWebView;
	SchoolCalendar schoolCalendar;
	PhoneView phoneView;
	Lecture lecture;
	Shows shows;
	Information information;
	Certification certification;
	NotificationSetting settingNotification;
	MYPKUSetting mypkuSetting;
	SwipeRefreshLayout swipeRefreshLayout;

	int type;
	String url;
	String html;
	String decodeString = "";

	Handler handler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			if (msg.what == Constants.MESSAGE_SUBACTIVITY_DECODE_PICTURE) {
				decodeString = (String) msg.obj;
				if (!"".equals(decodeString)) {
					SubActivity.this.closeContextMenu();
					CustomToast.showInfoToast(SubActivity.this, "长按图片可以识别二维码", 1200);
				}
				return true;
			}
			if (msg.what == Constants.MESSAGE_SLEEP_FINISHED) {
				try {
					swipeRefreshLayout.setRefreshing(false);
				} catch (Exception e) {
				}
				return true;
			}
			if (msg.what == Constants.MESSAGE_SUBACTIVITY_CERTIFICATION) {
				try {
					String string = (String) msg.obj;
					certification.finishRequest(string, false);
				} catch (Exception e) {
				}
				setRefresh();
			}
			if (msg.what == Constants.MESSAGE_SUBACTIVITY_SHOWS_UPDATE_BITMAP
					&& shows != null) {
				shows.updateBitmap();
				return true;
			}
			if (msg.what == Constants.MESSAGE_SUBACTIVITY_SHOWS_PICTURE
					&& shows != null) {
				shows.updateImage(msg.arg1);
				return true;
			}
			return false;
		}
	});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		type = bundle.getInt("type");
		url = bundle.getString("url", "");

		// 判断是不是gif格式图片
		if (type == Constants.SUBACTIVITY_TYPE_PICTURE_FILE
				&& bundle.getString("title", "查看图片")
				.toLowerCase(Locale.getDefault()).endsWith(".gif")) {
			type = Constants.SUBACTIVITY_TYPE_PICTURE_GIF;
		}

		if (type == Constants.SUBACTIVITY_TYPE_ABOUT)
			viewAbout();
		else if (type == Constants.SUBACTIVITY_TYPE_PICTURE_RESOURCE)
			picture = new Picture(this).showPicture(bundle.getInt("resid"),
					bundle.getString("title", "查看图片"));
		else if (type == Constants.SUBACTIVITY_TYPE_PICTURE_FILE) {
			picture = new Picture(this).showPicture(bundle.getString("file"),
					bundle.getString("title", "查看图片"));
		} else if (type == Constants.SUBACTIVITY_TYPE_PICTURE_URL) {
			picture = new Picture(this).showPicture(bundle.getString("url"));
		} else if (type == Constants.SUBACTIVITY_TYPE_PICTURE_GIF)
			gifView = new GifView(this).showGif(bundle.getString("file")
					, bundle.getString("title", "查看图片"));
		else if (type == Constants.SUBACTIVITY_TYPE_WEBVIEW)
			myWebView = new MyWebView(this, bundle.getInt("sid"))
					.showWebView(bundle.getString("title", ""), url);
		else if (type == Constants.SUBACTIVITY_TYPE_WEBVIEW_CALENDAR)
			schoolCalendar = new SchoolCalendar(this).showCalendar();
		else if (type == Constants.SUBACTIVITY_TYPE_WEBVIEW_HTML)
			myWebView = new MyWebView(this).showWebHtml(bundle.getString("title", "查看网页"),
					bundle.getString("html"));
		else if (type == Constants.SUBACTIVITY_TYPE_PHONE)
			phoneView = new PhoneView(this).showPhoneView();
		else if (type == Constants.SUBACTIVITY_TYPE_LECTURE)
			lecture = new Lecture(this).showLecture();
		else if (type == Constants.SUBACTIVITY_TYPE_SHOWS)
			shows = new Shows(this).getShows();
		else if (type == Constants.SUBACTIVITY_TYPE_WEBVIEW_PKUMAIL)
			myWebView = new MyWebView(this).showPKUMail();
			//else if (type==Constants.SUBACTIVITY_TYPE_WEBVIEW_LECTURE)
			//	lecture=new Lecture(this).getHtml(bundle.getString("title", "查看网页")
			//			, url);
		else if (type == Constants.SUBACTIVITY_TYPE_WEBVIEW_SHOWS)
			shows = new Shows(this).getHtml(bundle.getString("title", "查看网页")
					, url);
		else if (type == Constants.SUBACTIVITY_TYPE_CERTIFICATION)
			certification = new Certification(this).getCertification(bundle.getBoolean("refresh"));
		else if (type == Constants.SUBACTIVITY_TYPE_NOTIFICATIONS)
			settingNotification = new NotificationSetting(this).set();
		else if (type == Constants.SUBACTIVITY_TYPE_NOTIFICATIONS_SETTING)
			settingNotification = new NotificationSetting(this).setPushes();
		else if (type == Constants.SUBACTIVITY_TYPE_MYPKU_SET)
			mypkuSetting = new MYPKUSetting(this).set();
		else if (type == Constants.SUBACTIVITY_TYPE_COURSE_SET)
			new CourseSetting(this).show();
		else if (type == Constants.SUBACTIVITY_TYPE_IPGW_SET)
			new IPGWSetting(this).show();
		else if (type == Constants.SUBACTIVITY_TYPE_INFORMATION)
			information = new Information(this).init();
		else {
			wantToExit();
			return;
		}
		try {
			swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.subactivity_swipeRefreshLayout);
			if (swipeRefreshLayout != null) {
				swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_purple,
						android.R.color.holo_green_light,
						android.R.color.holo_blue_bright,
						android.R.color.holo_orange_light);
				swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
					public void onRefresh() {
						//setRefresh();
						if (type == Constants.SUBACTIVITY_TYPE_CERTIFICATION
								&& certification != null) {
							certification.pullToRefresh();
							return;
						}
						setRefresh();
					}
				});
			}
		} catch (Exception e) {
		}
	}

	void setRefresh() {
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

	public void viewAbout() {
		setContentView(R.layout.about);
		setTitle("关于本软件");
		ViewSetting.setTextView(this, R.id.about_version, Constants.version);
		ViewSetting.setTextView(this, R.id.about_time, Constants.update_time);
	}

	public void finishRequest(int type, String string) {
		if (type == Constants.REQUEST_SUBACTIVITY_CALENDAR)
			schoolCalendar.finishRequest(string);
		else if (type == Constants.REQUEST_SUBACTIVITY_LECTURE)
			lecture.finishRequest(string);
		else if (type == Constants.REQUEST_SUBACTIVITY_SHOWS)
			shows.finishRequest(string);
			//else if (type==Constants.REQUEST_SUBACTIVITY_LECTURE_DETAIL)
			//	lecture.viewHtml(string);
		else if (type == Constants.REQUEST_SUBACTIVITY_SHOWS_DETAIL)
			shows.viewHtml(string);
		else if (type == Constants.REQUEST_SUBACTIVITY_CERTIFICATION)
			certification.finishRequest(string, false);
		else if (type == Constants.REQUEST_SUBACTIVITY_PUSHES_GET)
			settingNotification.finishGetPushes(string);
		else if (type == Constants.REQUEST_SUBACTIVITY_PUSHES_SET)
			settingNotification.finishSave(string);
		else if (type == Constants.REQUEST_SUBACTIVITY_CARD_AMOUNT)
			information.showAmount(string);
		else if (type == Constants.REQUEST_HOLE_GET_SETTINGS)
			settingNotification.showHoleSettingDialog(string);
		else if (type == Constants.REQUEST_HOLE_SET_SETTINGS)
			settingNotification.finishHoleSetting(string);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			if (type == Constants.SUBACTIVITY_TYPE_WEBVIEW ||
					type == Constants.SUBACTIVITY_TYPE_WEBVIEW_PKUMAIL)
				if (webView.canGoBack()
						&& !webView.getUrl().equals("http://mail.pku.edu.cn/coremail/xphone/")
						&& !webView.getUrl().startsWith("http://mail.pku.edu.cn/coremail/xphone/index.jsp")
						) {
					webView.goBack();
					return true;
				}

			wantToExit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (type == Constants.SUBACTIVITY_TYPE_NOTIFICATIONS_SETTING
				|| type == Constants.SUBACTIVITY_TYPE_MYPKU_SET) {
			menu.add(Menu.NONE, Constants.MENU_SUBACTIVITY_SAVE, Constants.MENU_SUBACTIVITY_SAVE, "")
					.setIcon(R.drawable.ic_save_white_48dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		if (type == Constants.SUBACTIVITY_TYPE_WEBVIEW
				|| type == Constants.SUBACTIVITY_TYPE_WEBVIEW_CALENDAR
				|| type == Constants.SUBACTIVITY_TYPE_WEBVIEW_SHOWS
				|| type == Constants.SUBACTIVITY_TYPE_WEBVIEW_PKUMAIL) {
			if (myWebView != null && !myWebView.loading) {
				menu.add(Menu.NONE, Constants.MENU_SUBACTIVITY_OPEN_IN_BROWSER, Constants.MENU_SUBACTIVITY_OPEN_IN_BROWSER, "")
						.setIcon(R.drawable.ic_open_in_browser_white_48dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
				if (type != Constants.SUBACTIVITY_TYPE_WEBVIEW_PKUMAIL)
					menu.add(Menu.NONE, Constants.MENU_SUBACTIVITY_SHARE, Constants.MENU_SUBACTIVITY_SHARE, "")
							.setIcon(R.drawable.ic_share_white_48dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
				if (myWebView != null) {
					if (myWebView.sid != 0) {
						menu.add(Menu.NONE, Constants.MENU_SUBACTIVITY_REPLY, Constants.MENU_SUBACTIVITY_REPLY, "")
								.setIcon(R.drawable.ic_reply_white_48dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
					}
				}
			}
		}
		if (type == Constants.SUBACTIVITY_TYPE_PICTURE_FILE) {
			menu.add(Menu.NONE, Constants.MENU_SUBACTIVITY_SHARE, Constants.MENU_SUBACTIVITY_SHARE, "")
					.setIcon(R.drawable.ic_share_white_48dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add(Menu.NONE, Constants.MENU_SUBACTIVITY_SAVE_PICTURE, Constants.MENU_SUBACTIVITY_SAVE_PICTURE, "")
					.setIcon(R.drawable.ic_file_download_white_48dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		}
		if (type == Constants.SUBACTIVITY_TYPE_PICTURE_GIF) {
			menu.add(Menu.NONE, Constants.MENU_SUBACTIVITY_SAVE_PICTURE, Constants.MENU_SUBACTIVITY_SAVE_PICTURE, "")
					.setIcon(R.drawable.ic_file_download_white_48dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		/*
		if (type==Constants.SUBACTIVITY_TYPE_CERTIFICATION) {
			menu.add(Menu.NONE, Constants.MENU_SUBACTIVITY_REFRESH, Constants.MENU_SUBACTIVITY_REFRESH, "")
			.setIcon(R.drawable.reload).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		*/
		menu.add(Menu.NONE, Constants.MENU_SUBACTIVITY_CLOSE, Constants.MENU_SUBACTIVITY_CLOSE, "")
				.setIcon(R.drawable.ic_close_white_48dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == Constants.MENU_SUBACTIVITY_SAVE
				&& type == Constants.SUBACTIVITY_TYPE_NOTIFICATIONS_SETTING) {
			if (settingNotification != null)
				settingNotification.save();
			return true;
		}
		if (id == Constants.MENU_SUBACTIVITY_SAVE
				&& type == Constants.SUBACTIVITY_TYPE_MYPKU_SET) {
			if (mypkuSetting != null)
				mypkuSetting.save(false);
			return true;
		}
		if (id == Constants.MENU_SUBACTIVITY_OPEN_IN_BROWSER) {
			String url = webView.getUrl();
			Uri uri = Uri.parse(url);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
			return true;
		}
		if (id == Constants.MENU_SUBACTIVITY_SHARE) {
			if (webView != null) {
				String url = webView.getUrl();
				if (url == null || "".equals(url)) {
					url = this.url;
					if (url == null || "".equals(url))
						return true;
				}
				String title = getTitle().toString();
				String content = html;
				if (content == null || "".equals(content))
					content = getIntent().getStringExtra("content");
				if (content == null) content = "详情请点击查看";
				Bitmap bitmap = null;
				if (getIntent().getBooleanExtra("hasBitmap", false)) {
					bitmap = (Bitmap) DataObject.getInstance().getObject();
				}
				Share.readyToShareURL(this, "分享网页", url, title, content, bitmap);
			} else if (type == Constants.SUBACTIVITY_TYPE_PICTURE_FILE) {
				picture.sharePicture();
			}
			return true;
		}
		if (id == Constants.MENU_SUBACTIVITY_SAVE_PICTURE
				&& type == Constants.SUBACTIVITY_TYPE_PICTURE_FILE) {
			try {
				picture.savePicture();
			} catch (Exception e) {
				CustomToast.showErrorToast(this, "保存失败", 1500);
			}
			return true;
		}
		if (id == Constants.MENU_SUBACTIVITY_SAVE_PICTURE
				&& type == Constants.SUBACTIVITY_TYPE_PICTURE_GIF) {
			try {
				gifView.savePicture();
			} catch (Exception e) {
				CustomToast.showErrorToast(this, "保存失败", 1500);
			}
			return true;
		}
		if (id == Constants.MENU_SUBACTIVITY_REFRESH
				&& type == Constants.SUBACTIVITY_TYPE_CERTIFICATION) {
			certification.refresh();
			return true;
		}
		if (id == Constants.MENU_SUBACTIVITY_REPLY
				&& type == Constants.SUBACTIVITY_TYPE_WEBVIEW
				&& myWebView != null && myWebView.sid != 0) {
			Intent intent = new Intent(this, ChatActivity.class);
			intent.putExtra("uid", myWebView.sid + "");
			startActivity(intent);
			return true;
		}
		if (id == Constants.MENU_SUBACTIVITY_CLOSE) {
			wantToExit();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (type == Constants.SUBACTIVITY_TYPE_PICTURE_FILE) {
			if (picture == null) return;
			menu.add(Menu.NONE, Constants.CONTEXT_MENU_SUBACTIVITY_SHARE_PICTURE,
					Constants.CONTEXT_MENU_SUBACTIVITY_SHARE_PICTURE, "分享");
			menu.add(Menu.NONE, Constants.CONTEXT_MENU_SUBACTIVITY_SAVE_PICTURE,
					Constants.CONTEXT_MENU_SUBACTIVITY_SAVE_PICTURE, "保存到手机");
			if (!"".equals(decodeString)) {
				menu.add(Menu.NONE, Constants.CONTEXT_MENU_SUBACTIVITY_DECODE_PICTURE,
						Constants.CONTEXT_MENU_SUBACTIVITY_DECODE_PICTURE, "识别二维码");
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == Constants.CONTEXT_MENU_SUBACTIVITY_SAVE_PICTURE &&
				type == Constants.SUBACTIVITY_TYPE_PICTURE_FILE) {
			try {
				picture.savePicture();
			} catch (Exception e) {
				CustomToast.showErrorToast(this, "保存失败", 1500);
			}
			return true;
		}
		if (id == Constants.CONTEXT_MENU_SUBACTIVITY_SHARE_PICTURE &&
				type == Constants.SUBACTIVITY_TYPE_PICTURE_FILE) {
			picture.sharePicture();
			return true;
		}
		if (id == Constants.CONTEXT_MENU_SUBACTIVITY_DECODE_PICTURE) {
			picture.decodePicture(decodeString);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	protected void wantToExit() {
		if (type == Constants.SUBACTIVITY_TYPE_NOTIFICATIONS_SETTING
				&& settingNotification.hasModified) {
			new AlertDialog.Builder(this).setTitle("是否保存？")
					.setMessage("你进行了修改，是否保存？").setPositiveButton("是", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					settingNotification.save();
				}
			})
					.setNegativeButton("否", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							SubActivity.super.wantToExit();
						}
					}).setCancelable(true).show();
			return;
		}
		if (type == Constants.SUBACTIVITY_TYPE_MYPKU_SET
				&& mypkuSetting.hasModified) {
			new AlertDialog.Builder(this).setTitle("是否保存？")
					.setMessage("你进行了修改，是否保存？").setPositiveButton("是", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mypkuSetting.save(true);
				}
			})
					.setNegativeButton("否", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							SubActivity.super.wantToExit();
						}
					}).setCancelable(true).show();
			return;
		}
		if (type == Constants.SUBACTIVITY_TYPE_WEBVIEW && webView != null) {
			try {
				webView.stopLoading();
			} catch (Exception e) {
			}
		}
		if (gifView != null) gifView.stop();
		System.gc();
		super.wantToExit();
	}

}
