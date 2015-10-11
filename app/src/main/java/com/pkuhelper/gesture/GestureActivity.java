package com.pkuhelper.gesture;

import java.io.File;
import java.io.IOException;

import com.pkuhelper.IPGW;
import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.MyFile;
import com.pkuhelper.lib.Util;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomViewPager;

import android.app.Activity;
import android.app.Fragment;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.*;

public class GestureActivity extends Activity {
	
	CustomViewPager mViewPager;
	public static GestureActivity gestureActivity;
	GestureLibrary gestureLibrary;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gesture_layout);
		gestureActivity=this;
		getActionBar().setTitle("手势设置");
		Util.getOverflowMenu(this);
		if (!Constants.isLogin()) {
			finish();
			return;
		}
		gestureLibrary=IPGW.gestureLibrary;
		if (gestureLibrary==null) {
			File file=MyFile.getFile(this, Constants.username, "gesture");
			try {
				file.createNewFile();
			} catch (IOException e) {e.printStackTrace();} 
		 
			gestureLibrary=GestureLibraries.fromFile(file);
			IPGW.gestureLibrary=gestureLibrary;
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
				String[] strings={"connect","connectnofree","disconnect","disconnectall"};
				Bundle bundle=new Bundle();
				bundle.putString("type", strings[position]);
				return Fragment.instantiate(gestureActivity, "com.pkuhelper.gesture.GestureFragment", bundle);
			}
		});
			
		mViewPager
		.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
				@Override
				public void onPageSelected(int position) {
					switch (position) {
					case 0:selectConnect(null);break;
					case 1:selectConnectNoFree(null);break;
					case 2:selectDisconnect(null);break;
					case 3:selectDisconnectall(null);break;
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
	public boolean onCreateOptionsMenu(Menu menu) {
		Util.setIconEnable(menu, true);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(Menu.NONE, Constants.MENU_SUBACTIVITY_CLOSE, Constants.MENU_SUBACTIVITY_CLOSE, "")
		.setIcon(R.drawable.close).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == Constants.MENU_SUBACTIVITY_CLOSE) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode==KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
}
