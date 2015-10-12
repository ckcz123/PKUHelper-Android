package com.pkuhelper.lostfound.old;

import java.util.ArrayList;
import java.util.HashMap;

import com.pkuhelper.R;
import com.pkuhelper.lib.BaseActivity;
import com.pkuhelper.lib.Constants;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

@SuppressLint("UseSparseArrays")
public class LostFoundActivity extends BaseActivity {
	public HashMap<Integer, LostFoundInfo> lostMap=new HashMap<Integer, LostFoundInfo>();
	public HashMap<Integer, LostFoundInfo> foundMap=new HashMap<Integer, LostFoundInfo>();
	public HashMap<Integer, LostFoundInfo> myMap=new HashMap<Integer, LostFoundInfo>();
	public ArrayList<Integer> lostArray=new ArrayList<Integer>();
	public ArrayList<Integer> foundArray=new ArrayList<Integer>();
	public ArrayList<Integer> myArray=new ArrayList<Integer>();
	public ListView lostListView,foundListView,myListView;
	public static LostFoundActivity lostFoundActivity;
	public int lostPage, foundPage;
	boolean lostFirstTimeToBottom, foundFirstTimeToBottom;
	public EventHandler eventHandler;
	public static final int PAGE_NONE = 0;
	public static final int PAGE_LOST = 1;
	public static final int PAGE_FOUND = 2;
	public static final int PAGE_MINE = 3;
	public static final int PAGE_ADD = 4;
	int nowShowing;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		lostFoundActivity=this;
		eventHandler=new EventHandler(getMainLooper());
		setContentView(R.layout.lostfound_listview);
		nowShowing=0;
		Lost.getLostInfo();		
	}
	
	public void finishRequest(int type, String string) {
		if (type==Constants.REQUEST_LOSTFOUND_GETLOST)
			Lost.finishRequest(string);
		if (type==Constants.REQUEST_LOSTFOUND_GETFOUND)
			Found.finishRequest(string);
		if (type==Constants.REQUEST_LOSTFOUND_GETMINE)
			MyLostFound.finishRequest(string);
		else if (type==Constants.REQUEST_LOSTFOUND_ADD)
			Add.finishRequest(string);
		else if (type==Constants.REQUEST_LOSTFOUND_DELETE)
			Detail.finishDelete(string);
	}
	

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (nowShowing!=PAGE_NONE && nowShowing!=PAGE_ADD) {
			if (nowShowing==PAGE_MINE) {
				menu.add(Menu.NONE, Constants.MENU_LOSTFOUND_ADD, 
						Constants.MENU_LOSTFOUND_ADD, "")
						.setIcon(R.drawable.add).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			}
			menu.add(Menu.NONE, Constants.MENU_LOSTFOUND_CHOOSE, 
					Constants.MENU_LOSTFOUND_CHOOSE, "")
					.setIcon(R.drawable.some).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			return true;
		}
		if (nowShowing==PAGE_ADD) {
			menu.add(Menu.NONE, Constants.MENU_LOSTFOUND_SAVE, 
					Constants.MENU_LOSTFOUND_SAVE, "")
					.setIcon(R.drawable.save).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			return true;
		}
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id=item.getItemId();
		if (id==Constants.MENU_LOSTFOUND_CHOOSE) {
			String[] strings={"失物信息","招领信息","我发布的失物招领"};
			new AlertDialog.Builder(this).setCancelable(true).setTitle("请选择查看的内容")
			.setItems(strings, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0:
						Lost.getLostInfo();
						break;
					case 1:
						Found.getFoundInfo();
						break;
					case 2:
						MyLostFound.getMyInfo();
						break;
					}
				}
			}).show();
			return true;
		}
		if (id==Constants.MENU_LOSTFOUND_ADD) {
			Add.showAddView();
			return true;
		}
		if (id==Constants.MENU_LOSTFOUND_SAVE) {
			Add.confirm();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode==KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN) {
			if (nowShowing==PAGE_ADD) {
				MyLostFound.getMyInfo();
				return true;
			}
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode!=RESULT_OK) return;
		Add.finishSelectImage(requestCode, data);
	}
	
}
