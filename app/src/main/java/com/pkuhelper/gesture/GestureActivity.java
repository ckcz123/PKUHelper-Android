package com.pkuhelper.gesture;

import android.app.Fragment;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.pkuhelper.IPGW;
import com.pkuhelper.R;
import com.pkuhelper.lib.BaseActivity;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.MyFile;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomViewPager;

import java.io.File;
import java.io.IOException;

public class GestureActivity extends BaseActivity {

	CustomViewPager mViewPager;
	public static GestureActivity gestureActivity;
	GestureLibrary gestureLibrary;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gesture_layout);
		gestureActivity = this;
		setTitle("手势设置");
		if (!Constants.isLogin()) {
			wantToExit();
			return;
		}
		gestureLibrary = IPGW.gestureLibrary;
		if (gestureLibrary == null) {
			File file = MyFile.getFile(this, Constants.username, "gesture");
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

			gestureLibrary = GestureLibraries.fromFile(file);
			IPGW.gestureLibrary = gestureLibrary;
		}
		gestureLibrary.load();

		mViewPager = (CustomViewPager) findViewById(R.id.gesture_pager);
		mViewPager.setAdapter(new FragmentPagerAdapter(getFragmentManager()) {

			@Override
			public int getCount() {
				return 4;
			}

			@Override
			public Fragment getItem(int position) {
				String[] strings = {"connect", "connectnofree", "disconnect", "disconnectall"};
				Bundle bundle = new Bundle();
				bundle.putString("type", strings[position]);
				return Fragment.instantiate(gestureActivity, "com.pkuhelper.gesture.GestureFragment", bundle);
			}
		});

		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						switch (position) {
							case 0:
								selectConnect(null);
								break;
							case 1:
								selectConnectNoFree(null);
								break;
							case 2:
								selectDisconnect(null);
								break;
							case 3:
								selectDisconnectall(null);
								break;
						}
					}
				});
		selectConnect(null);
	}

	public void resetAllTab() {
		ViewSetting.setTextViewColor(this, R.id.gesture_connect, Color.BLACK);
		ViewSetting.setTextViewColor(this, R.id.gesture_connect_no_free, Color.BLACK);
		ViewSetting.setTextViewColor(this, R.id.gesture_disconnect, Color.BLACK);
		ViewSetting.setTextViewColor(this, R.id.gesture_disconnectall, Color.BLACK);
	}

	public void selectConnect(View view) {
		mViewPager.setCurrentItem(0);
		resetAllTab();
		ViewSetting.setTextViewColor(this, R.id.gesture_connect, Color.parseColor("#2d90dc"));
	}

	public void selectConnectNoFree(View view) {
		mViewPager.setCurrentItem(1);
		resetAllTab();
		ViewSetting.setTextViewColor(this, R.id.gesture_connect_no_free, Color.parseColor("#2d90dc"));
	}

	public void selectDisconnect(View view) {
		mViewPager.setCurrentItem(2);
		resetAllTab();
		ViewSetting.setTextViewColor(this, R.id.gesture_disconnect, Color.parseColor("#2d90dc"));
	}

	public void selectDisconnectall(View view) {
		mViewPager.setCurrentItem(3);
		resetAllTab();
		ViewSetting.setTextViewColor(this, R.id.gesture_disconnectall, Color.parseColor("#2d90dc"));
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(Menu.NONE, Constants.MENU_SUBACTIVITY_CLOSE, Constants.MENU_SUBACTIVITY_CLOSE, "")
				.setIcon(R.drawable.close).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}


	protected void finishRequest(int type, String string) {
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == Constants.MENU_SUBACTIVITY_CLOSE) {
			wantToExit();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
