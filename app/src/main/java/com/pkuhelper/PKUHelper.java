package com.pkuhelper;


import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.pkuhelper.chat.ChatActivity;
import com.pkuhelper.course.CustomCourseActivity;
import com.pkuhelper.course.DeanCourseActivity;
import com.pkuhelper.course.ExamActivity;
import com.pkuhelper.lib.BaseActivity;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.Editor;
import com.pkuhelper.lib.MyBitmapFactory;
import com.pkuhelper.lib.MyFile;
import com.pkuhelper.lib.Util;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomViewPager;
import com.pkuhelper.noticecenter.NCActivity;
import com.pkuhelper.pkuhole.HoleActivity;
import com.pkuhelper.qrcode.QRCodeActivity;
import com.pkuhelper.subactivity.SubActivity;

public class PKUHelper extends BaseActivity {

	public static PKUHelper pkuhelper;
	public CustomViewPager mViewPager;
	ActionBar actionBar;
	private SensorManager sensorManager;
	private Vibrator vibrator;
	private SensorEventListener sensorEventListener;
	private long lastShakeTime = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		MyFile.setUseSDCard(true, this);

		pkuhelper = this;
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		sensorEventListener = new SensorEventListener() {

			@Override
			public void onSensorChanged(SensorEvent event) {
				if (mViewPager.getCurrentItem() != 0) return;
				long currTime = System.currentTimeMillis();
				if (currTime - lastShakeTime <= 2500) return;
				float[] values = event.values;
				float x = values[0]; // x轴方向的重力加速度
				float y = values[1]; // y轴方向的重力加速度
				float z = values[2]; // z轴方向的重力加速度
				if (Math.abs(x) > 17 || Math.abs(y) > 17 || Math.abs(z) > 24) {
					lastShakeTime = currTime;
					vibrator.vibrate(200);
					IPGW_shake.connect();
				}
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
		};

		// Set up the action bar.
		actionBar = getActionBar();

		mViewPager = (CustomViewPager) findViewById(R.id.tabpager);
		mViewPager.setAdapter(new FragmentPagerAdapter(getFragmentManager()) {

			@Override
			public int getCount() {
				return 4;
			}

			@Override
			public Fragment getItem(int position) {
				if (position == 0) {
					if (Editor.getBoolean(PKUHelper.this, "use_shake"))
						return Fragment.instantiate(PKUHelper.this, "com.pkuhelper.IPGW_shake", null);
					return Fragment.instantiate(PKUHelper.this, "com.pkuhelper.IPGW", null);
				} else if (position == 1)
					return Fragment.instantiate(PKUHelper.this, "com.pkuhelper.Course", null);
				else if (position == 2)
					return Fragment.instantiate(PKUHelper.this, "com.pkuhelper.MYPKU", null);
				else return Fragment.instantiate(PKUHelper.this, "com.pkuhelper.Settings", null);
			}
		});

		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				if (position == 0)
					clickIPGW(null);
				else if (position == 1) clickCourse(null);
				else if (position == 2) clickMYPKU(null);
				else if (position == 3) clickSettings(null);
			}
		});
		init();
	}

	private void resetAllTab() {
		((TextView) findViewById(R.id.title_ipgw)).setTextColor(Color.BLACK);
		((TextView) findViewById(R.id.title_course)).setTextColor(Color.BLACK);
		((TextView) findViewById(R.id.title_mypku)).setTextColor(Color.BLACK);
		((TextView) findViewById(R.id.title_settings)).setTextColor(Color.BLACK);
		ViewSetting.setImageResource(this, R.id.img_ipgw, R.drawable.tab_ipgw);
		ViewSetting.setImageResource(this, R.id.img_course, R.drawable.tab_course);
		ViewSetting.setImageResource(this, R.id.img_mypku, R.drawable.tab_mypku);
		ViewSetting.setImageResource(this, R.id.img_settings, R.drawable.tab_settings);
	}

	public void clickIPGW(View view) {
		mViewPager.setCurrentItem(0);
		invalidateOptionsMenu();
		resetAllTab();
		ViewSetting.setImageResource(this, R.id.img_ipgw, R.drawable.tab_ipgw_selected);
		((TextView) findViewById(R.id.title_ipgw)).setTextColor(Color.parseColor("#319de1"));
		actionBar.setTitle("欢迎进入PKU Helper");
	}

	public void clickCourse(View view) {
		mViewPager.setCurrentItem(1);
		invalidateOptionsMenu();
		resetAllTab();
		ViewSetting.setImageResource(this, R.id.img_course, R.drawable.tab_course_selected);
		((TextView) findViewById(R.id.title_course)).setTextColor(Color.parseColor("#319de1"));
		int week = Editor.getInt(this, "week");
		if (week < 0 || week >= 20) week = 0;
		if (week == 0)
			actionBar.setTitle("放假期间");
		else
			actionBar.setTitle("第" + week + "周课表");
	}

	public void clickMYPKU(View view) {
		mViewPager.setCurrentItem(2);
		invalidateOptionsMenu();
		resetAllTab();
		ViewSetting.setImageResource(this, R.id.img_mypku, R.drawable.tab_mypku_selected);
		((TextView) findViewById(R.id.title_mypku)).setTextColor(Color.parseColor("#319de1"));
		actionBar.setTitle("我的PKU");
	}

	public void clickSettings(View view) {
		mViewPager.setCurrentItem(3);
		invalidateOptionsMenu();
		resetAllTab();
		ViewSetting.setImageResource(this, R.id.img_settings, R.drawable.tab_settings_selected);
		((TextView) findViewById(R.id.title_settings)).setTextColor(Color.parseColor("#319de1"));
		actionBar.setTitle("设置");
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (mViewPager.getCurrentItem() == 0) {
			if (!Editor.getBoolean(this, "use_shake"))
				menu.add(Menu.NONE, Constants.MENU_IPGW_SET_BACKGROUND, Constants.MENU_IPGW_SET_BACKGROUND, "")
						.setIcon(R.drawable.set).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add(Menu.NONE, Constants.MENU_QRCODE, Constants.MENU_QRCODE, "")
					.setIcon(R.drawable.qrcode).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		if (mViewPager.getCurrentItem() == 1) {
			menu.add(Menu.NONE, Constants.MENU_COURSE_ADD, Constants.MENU_COURSE_ADD, "").setIcon(R.drawable.add)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add(Menu.NONE, Constants.MENU_COURSE_REFRESH, Constants.MENU_COURSE_REFRESH, "").setIcon(R.drawable.reload)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add(Menu.NONE, Constants.MENU_COURSE_SHARE, Constants.MENU_COURSE_SHARE, "")
					.setIcon(R.drawable.open).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			return true;
		}
		if (mViewPager.getCurrentItem() == 2) {
			menu.add(Menu.NONE, Constants.MENU_MYPKU_SET, Constants.MENU_MYPKU_SET, "")
					.setIcon(R.drawable.set).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == Constants.MENU_IPGW_SET_BACKGROUND
				&& !Editor.getBoolean(this, "use_shake")) {
			String[] strings = {"修改背景图", "设置文字颜色", "重置为默认"};
			new AlertDialog.Builder(this).setTitle("选择项目")
					.setItems(strings, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (which == 0) IPGW.setBackground();
							else if (which == 1) IPGW.setTextColor();
							else if (which == 2) IPGW.reset();
						}
					}).show();

			return true;
		}
		if (id == Constants.MENU_QRCODE) {
			startActivity(new Intent(this, QRCodeActivity.class));
		}
		if (id == Constants.MENU_COURSE_REFRESH) {

			String[] strings = {"更换颜色或背景", "重新导入教务课程", "只导入自定义课程"};
			new AlertDialog.Builder(this).setTitle("选择项目")
					.setItems(strings, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (which == 0) {
								String[] stringsSec = {"更换背景图", "更换课程颜色", "重置为默认"};
								new AlertDialog.Builder(pkuhelper).setTitle("选择项目")
										.setItems(stringsSec, new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialogInterface, int i) {
												if (i == 0) Course.setBackground();
												else if (i == 1) Course.changeColor();
												else if (i == 2) Course.resetBackground();
											}
										}).show();
							} else if (which == 1) Course.gettingCourse();
							else if (which == 2) Course.getCustom();
						}
					}).show();

			return true;
		}
		if (id == Constants.MENU_COURSE_ADD) {
			String[] strings = {"编辑教务课程地点", "编辑自定义课程", "编辑考试倒计时"};
			new AlertDialog.Builder(this).setTitle("选择项目")
					.setItems(strings, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (which == 0)
								startActivity(new Intent(pkuhelper, DeanCourseActivity.class));
							else if (which == 1)
								startActivity(new Intent(pkuhelper, CustomCourseActivity.class));
							else if (which == 2)
								startActivity(new Intent(pkuhelper, ExamActivity.class));
						}
					}).show();

			return true;
		}
		if (id == Constants.MENU_COURSE_SHARE) {
			MyBitmapFactory.showBitmap(this, Util.captureWebView(Course.courseView));
		}
		if (id == Constants.MENU_MYPKU_SET) {
			Intent intent = new Intent(this, SubActivity.class);
			intent.putExtra("type", Constants.SUBACTIVITY_TYPE_MYPKU_SET);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	void init() {
		clickIPGW(null);
		Constants.init(this);
		if (!Constants.isLogin()) {
			IAAA.showLoginView();
//			if (!Editor.getBoolean(this, "privacy", false))
//				showPrivacy();
		} else {
			//doWhenFirstLaunch();
			if (!dealWithActionType(getIntent().getStringExtra("type"))) {
//				if (!Editor.getBoolean(this, "privacy", false))
//					showPrivacy();
			}
		}
	}
/*
	void doWhenFirstLaunch() {
		try {
			if (Editor.getBoolean(this, "launch_" + Constants.version, true)) {
				//MyFile.deleteFile(MyFile.getFile(this, Constants.username, "deancourse"));
				//MyFile.deleteFile(MyFile.getFile(this, Constants.username, "course"));

			}
			Editor.putBoolean(this, "launch_" + Constants.version, false);
		} catch (Exception e) {
		}
	}
*/
	boolean dealWithActionType(String type) {
		if (type == null) return false;
		if ("course".equals(type)) {
			clickCourse(null);
		} else if ("edit_course".equals(type)) {
			clickCourse(null);
			startActivity(new Intent(this, CustomCourseActivity.class));
		} else if ("exam".equals(type)) {
			clickCourse(null);
			startActivity(new Intent(this, ExamActivity.class));
		} else if ("notification".equals(type)) {
			clickMYPKU(null);
			startActivity(new Intent(this, NCActivity.class));
		} else if ("message".equals(type)) {
			clickSettings(null);
			startActivity(new Intent(this, ChatActivity.class));
		} else if ("pkuhole".equals(type)) {
			clickMYPKU(null);
			Intent intent = new Intent(this, HoleActivity.class);
			intent.putExtra("page", HoleActivity.PAGE_MINE);
			startActivity(intent);
		}
		else return false;
		return true;
	}

	public void showPrivacy() {
		new AlertDialog.Builder(this).setTitle("用户隐私条例")
				.setMessage(Constants.privacy).setCancelable(false)
				.setPositiveButton("我接受", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Editor.putBoolean(pkuhelper, "privacy", true);
					}
				})
				.setNegativeButton("我拒绝", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Editor.putBoolean(pkuhelper, "privacy", false);
						finish();
					}
				}).show();
	}

	protected void finishRequest(int type, String string) {

		if (type == Constants.REQUEST_IAAA)
			IAAA.finishLogin(string);
		if (type == Constants.REQUEST_ITS_CONNECT
				|| type == Constants.REQUEST_ITS_CONNECT_NO_FREE
				|| type == Constants.REQUEST_ITS_DISCONNECT
				|| type == Constants.REQUEST_ITS_DISCONNECT_ALL)
			IPGW.finishConnection(type, string);
		if (type == Constants.REQUEST_ELECTIVE_COURSES || type == Constants.REQUEST_ELECTIVE_TOKEN
				|| type == Constants.REQUEST_ELECTIVE_COOKIE)
			Course.finishConnection(type, string);
		if (type == Constants.REQUEST_DEAN_COURSES)
			Course.finishGetCourses(string);
		if (type == Constants.REQUEST_CUSTOM_COURSES)
			Course.finishGetCustom(string);
		if (type == Constants.REQUEST_DEAN_LOGIN)
			Dean.finishLogin(string);
		if (type == Constants.REQUEST_PE_TEST)
			PE.finishPeTestRequest(string);
		if (type == Constants.REQUEST_PE_CARD)
			PE.finishPeCardRequest(string);
		if (type == Constants.REQUEST_UPDATE)
			Settings.finishCheckUpdate(string);
		if (type == Constants.REQUEST_REPORT)
			Settings.finishReport(string);
		if (type == Constants.REQUEST_FOUND_USERNAME)
			Settings.finishFound(string);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (sensorManager != null) {// 注册监听器
			sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (sensorManager != null) {// 取消监听器
			sensorManager.unregisterListener(sensorEventListener);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) return;
		if (requestCode == 0) IPGW.realSetBackground(data);
		if (requestCode == 1) Course.realSetBackground(data);
	}
}
