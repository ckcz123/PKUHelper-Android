package com.pkuhelper.bbs;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pkuhelper.R;
import com.pkuhelper.lib.BaseActivity;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.Editor;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.view.CustomViewPager;

import java.util.ArrayList;

public class BBSActivity extends BaseActivity {
	public static BBSActivity bbsActivity;
	public CustomViewPager mViewPager;

	Handler handler = new Handler(new Handler.Callback() {
		public boolean handleMessage(Message msg) {
			if (msg.what == Constants.MESSAGE_BBS_LOGIN) {
				Userinfo.finishLogin((String) msg.obj);
			}
			return false;
		}
	});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bbs_main);
		bbsActivity = this;
		mViewPager = (CustomViewPager) findViewById(R.id.bbspager);
		mViewPager.setAdapter(new FragmentPagerAdapter(getFragmentManager()) {
			@Override
			public int getCount() {
				return 4;
			}

			@Override
			public Fragment getItem(int arg0) {
				if (arg0 == 1)
					return Fragment.instantiate(bbsActivity, "com.pkuhelper.bbs.AllBoardsFragment");
				if (arg0 == 2)
					return Fragment.instantiate(bbsActivity, "com.pkuhelper.bbs.SearchFragment");
				if (arg0 == 3)
					return Fragment.instantiate(bbsActivity, "com.pkuhelper.bbs.UserinfoFragment");
				return Fragment.instantiate(bbsActivity, "com.pkuhelper.bbs.TopFragment");
			}
		});
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				if (position == 0) clickTop(null);
				else if (position == 1) clickAllBoards(null);
				else if (position == 2) clickSearch(null);
				else if (position == 3) clickMe(null);
			}
		});
		Board.load();
		Userinfo.load();
		TopFragment.tops = new ArrayList<ThreadInfo>();
		clickTop(null);
	}

	private void resetAllTab() {
		((ImageView) findViewById(R.id.bbs_bottom_img_top)).getDrawable().clearColorFilter();
		((ImageView) findViewById(R.id.bbs_bottom_img_allboards)).getDrawable().clearColorFilter();
		((ImageView) findViewById(R.id.bbs_bottom_img_search)).getDrawable().clearColorFilter();
		((ImageView) findViewById(R.id.bbs_bottom_img_me)).getDrawable().clearColorFilter();
		((TextView) findViewById(R.id.bbs_bottom_top)).setTextColor(Color.BLACK);
		((TextView) findViewById(R.id.bbs_bottom_allboards)).setTextColor(Color.BLACK);
		((TextView) findViewById(R.id.bbs_bottom_search)).setTextColor(Color.BLACK);
		((TextView) findViewById(R.id.bbs_bottom_me)).setTextColor(Color.BLACK);
	}

	public void clickTop(View view) {
		mViewPager.setCurrentItem(0);
		invalidateOptionsMenu();
		resetAllTab();
		((ImageView) findViewById(R.id.bbs_bottom_img_top)).getDrawable().
				setColorFilter(Color.parseColor("#2d90dc"), PorterDuff.Mode.MULTIPLY);
		((TextView) findViewById(R.id.bbs_bottom_top)).setTextColor(Color.parseColor("#2d90dc"));
		setTitle("热门帖子");
	}

	public void clickAllBoards(View view) {
		mViewPager.setCurrentItem(1);
		invalidateOptionsMenu();
		resetAllTab();
		((ImageView) findViewById(R.id.bbs_bottom_img_allboards)).getDrawable().setColorFilter(Color.parseColor("#2d90dc"), PorterDuff.Mode.MULTIPLY);
		((TextView) findViewById(R.id.bbs_bottom_allboards)).setTextColor(Color.parseColor("#2d90dc"));
		setTitle("版面列表");
	}

	public void clickSearch(View view) {
		mViewPager.setCurrentItem(2);
		invalidateOptionsMenu();
		resetAllTab();
		((ImageView) findViewById(R.id.bbs_bottom_img_search)).getDrawable().setColorFilter(Color.parseColor("#2d90dc"), PorterDuff.Mode.MULTIPLY);
		((TextView) findViewById(R.id.bbs_bottom_search)).setTextColor(Color.parseColor("#2d90dc"));
		setTitle("搜索帖子");
	}

	public void clickMe(View view) {
		mViewPager.setCurrentItem(3);
		invalidateOptionsMenu();
		resetAllTab();
		((ImageView) findViewById(R.id.bbs_bottom_img_me)).getDrawable().setColorFilter(Color.parseColor("#2d90dc"), PorterDuff.Mode.MULTIPLY);
		((TextView) findViewById(R.id.bbs_bottom_me)).setTextColor(Color.parseColor("#2d90dc"));
		setTitle("个人信息");
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (mViewPager.getCurrentItem() == 1) {
			String board = AllBoardsFragment.tmpBoard;
			String hint = "加入收藏夹";
			if (Board.favorite.contains(board)) hint = "移出收藏夹";
			menu.add(Menu.NONE, Constants.CONTEXT_MENU_BBS_FAVORITE,
					Constants.CONTEXT_MENU_BBS_FAVORITE, hint);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == Constants.CONTEXT_MENU_BBS_FAVORITE) {
			Board.toggleFavorite(AllBoardsFragment.tmpBoard);
			CustomToast.showSuccessToast(this, "操作成功", 1000);
			if (!AllBoardsFragment.allBoards)
				AllBoardsFragment.resetList();
			return true;
		}
		return super.onContextItemSelected(item);

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (mViewPager.getCurrentItem() < 2)
			menu.add(Menu.NONE, Constants.MENU_BBS_REFRESH, Constants.MENU_BBS_REFRESH, "")
					.setIcon(R.drawable.reload).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		/*
		if (mViewPager.getCurrentItem()==1)
			menu.add(Menu.NONE, Constants.MENU_BBS_FAVORITE, Constants.MENU_BBS_FAVORITE, "")
			.setIcon(R.drawable.some).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			*/
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == Constants.MENU_BBS_REFRESH) {
			int curr = mViewPager.getCurrentItem();
			if (curr == 0)
				TopFragment.showView();
			else if (curr == 1)
				Board.reload();
			return true;
		}
		if (id == Constants.MENU_BBS_FAVORITE) {
			AllBoardsFragment.allBoards = !AllBoardsFragment.allBoards;
			Editor.putBoolean(this, "bbs_viewall", AllBoardsFragment.allBoards);
			AllBoardsFragment.resetList();
			if (AllBoardsFragment.allBoards)
				setTitle("所有版面");
			else
				setTitle("收藏版面");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void finishRequest(int type, String string) {
		if (type == Constants.REQUEST_BBS_GET_TOP)
			TopFragment.finishRequest(string);
		if (type == Constants.REQUEST_BBS_GET_ALL_BOARDS)
			//AllBoardsFragment.setBoards(string, false);
			Board.save(string);
		if (type == Constants.REQUEST_BBS_LOGIN)
			Userinfo.finishLogin(string);
		if (type == Constants.REQUEST_BBS_SEARCH)
			SearchFragment.finishSearch(string);
	}

}
