package com.pkuhelper.pkuhole.old;

import java.util.*;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.Lib;
import com.pkuhelper.lib.Util;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.widget.ListView;

public class PKUHoleActivity extends Activity {

	static PKUHoleActivity pkuHoleActivity;
	EventHandler eventHandler;
	ArrayList<HoleMessage> messageList=new ArrayList<HoleMessage>();
	ArrayList<HashMap<String, String>> maps=new ArrayList<HashMap<String,String>>();
	ListView listView;
	ListView commentListView;
	int currPage;
	int currId;
	int lastVisiableItem=0;
	boolean firstTimeToGetBottom=true;
	public static final int PAGE_LIST=1;
	public static final int PAGE_DETAIL=2;
	int isShowing=PAGE_LIST;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pkuHoleActivity=this;
		Util.getOverflowMenu(this);
		eventHandler=new EventHandler(getMainLooper());
		listView=new ListView(this);
		currPage=0;
		Lib.checkConnectedStatus(this);
		ShowLists.showPage(true);
	}
	
	public void finishRequest(int type,String string) {
		if (type==Constants.REQUEST_PKUHOLE_GET_PAGE)
			ShowLists.finishRequest(string);
		else if (type==Constants.REQUEST_PKUHOLE_GET_DETAIL_FINISHED
				|| type==Constants.REQUEST_PKUHOLE_GET_DETAIL_FAILED)
			ShowComments.finishRequest(string);
		else if (type==Constants.REQUEST_PKUHOLE_POST_MESSAGE)
			PostMessage.finishRequest(string);
		else if (type==Constants.REQUEST_PKUHOLE_POST_COMMENT)
			CommentMessage.finishRequest(string);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Util.setIconEnable(menu, true);
		return super.onCreateOptionsMenu(menu);
	}
	
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (isShowing==PAGE_LIST)
			menu.add(Menu.NONE, Constants.MENU_PKUHOLE_ADD, Constants.MENU_PKUHOLE_ADD, "")
			.setIcon(R.drawable.add).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		else if (isShowing==PAGE_DETAIL)
			menu.add(Menu.NONE, Constants.MENU_PKUHOLE_ADD, Constants.MENU_PKUHOLE_ADD, "")
			.setIcon(R.drawable.reply).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(Menu.NONE, Constants.MENU_PKUHOLE_REFRESH, Constants.MENU_PKUHOLE_REFRESH, "")
		.setIcon(R.drawable.reload).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == Constants.MENU_PKUHOLE_REFRESH) {
			if (isShowing==PAGE_LIST)
				ShowLists.showPage(true);
			else 
				ShowComments.showComments(currId);
			//Course.gettingCourse();
			return true;
		}
		if (id == Constants.MENU_PKUHOLE_ADD) {
			if (isShowing==PAGE_LIST)
				PostMessage.postMessage();
			else 
				CommentMessage.commentMessage();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode==KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN) {
			if (isShowing==PAGE_LIST)
				finish();
			else if (isShowing==PAGE_DETAIL) {
				ShowLists.showPage(false);
				listView.setSelection(lastVisiableItem);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
}
