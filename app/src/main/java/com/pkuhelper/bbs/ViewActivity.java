package com.pkuhelper.bbs;

import java.io.File;
import java.util.Locale;
import java.util.regex.Matcher;

import com.pkuhelper.R;
import com.pkuhelper.lib.BaseActivity;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.MyBitmapFactory;
import com.pkuhelper.lib.MyFile;
import com.pkuhelper.lib.Util;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.subactivity.SubActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Patterns;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;

public class ViewActivity extends BaseActivity {
	static ViewActivity viewActivity;
	
	public static final int PAGESIZE = 20;
	
	static final int PAGE_NONE = 0;
	static final int PAGE_THREAD = 1;
	static final int PAGE_POST = 2;
	int showingPage;
	int urlNum;
	int imageNum;
	static String board;
	static String boardName;
	static String threadid;
	static boolean startFromParent=false;
	
	Handler handler=new Handler(new Handler.Callback() {
		public boolean handleMessage(Message msg) {
			if (msg.what==Constants.MESSAGE_IMAGE_REQUEST_FINISHED) {
				updateImage(msg.arg1);
			}
			return false;
		}
	});
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewActivity=this;
		
		init();
		String type=getIntent().getStringExtra("type");
		showingPage=PAGE_NONE;
		if ("board".equals(type)) {
			startFromParent=false;
			ViewThread.getThreads(1);
		}
		if ("thread".equals(type)) {
			startFromParent=true;
			String threadid=getIntent().getStringExtra("threadid");
			if (threadid==null) {
				CustomToast.showErrorToast(this, "没有threadid！");
				super.wantToExit();
				return;
			}
			ViewPost.selectNum=getIntent().getIntExtra("number", 0);
			ViewPost.getPosts(threadid, 1);
		}
	}
	
	void init() {
		board=getIntent().getStringExtra("board");
		if (board==null || "".equals(board)) {
			CustomToast.showErrorToast(this, "没有这个版面！");
			super.wantToExit();
			return;
		}
		if ("AcademicInfo".equals(board)) {
			boardName=board;
			return;
		}
		
		Board bd=Board.boards.get(board);
		if (bd==null) {
			CustomToast.showErrorToast(this, "没有这个版面！");
			super.wantToExit();
			return;
		}
		boardName=bd.name;
	}

	public void finishRequest(int type, String string) {
		if (type==Constants.REQUEST_BBS_GET_LIST)
			ViewThread.finishRequest(string);
		if (type==Constants.REQUEST_BBS_GET_POST)
			ViewPost.finishRequest(string);
		if (type==Constants.REQUEST_BBS_REPRINT)
			ViewPost.finishReprint(string);
	}
	
	void updateImage(int postid) {
		if (showingPage==PAGE_POST) {
			ListView listView=(ListView)findViewById(R.id.bbs_post_listview);
			int size=ViewPost.postInfos.size();
			for (int i=0;i<size;i++) {
				PostInfo postInfo=ViewPost.postInfos.get(i);
				if (postInfo.postid==postid) {
					int cnt=listView.getChildCount();
					for (int j=0;j<cnt;j++) {
						try {
							View view=listView.getChildAt(j);
							if (view==null) continue;
							if ((Integer)view.getTag()!=i) continue;
							ViewSetting.setTextView(view, R.id.bbs_post_item_text, 
									Html.fromHtml(postInfo.content, new Html.ImageGetter() {
										public Drawable getDrawable(String source) {
											if (source.startsWith("/"))
												source="http://www.bdwm.net"+source;
											final File file=MyFile.getCache(ViewActivity.viewActivity, Util.getHash(source));
											if (file.exists()) {
												Bitmap bitmap=MyBitmapFactory.getCompressedBitmap(file.getAbsolutePath(), 2.5);
												if (bitmap!=null) {
													Drawable drawable=new BitmapDrawable(ViewActivity.viewActivity.getResources(), bitmap);
													drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
													return drawable;
												}
											}
											return null;
										}
									}, null));
						}
						catch (Exception e) {}
					}
				}
			}
		}
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (showingPage==PAGE_THREAD) {
			if (ViewThread.page!=1)
				menu.add(Menu.NONE, Constants.MENU_BBS_VIEW_PREVIOUS, Constants.MENU_BBS_VIEW_PREVIOUS, "")
				.setIcon(R.drawable.bbs_pre).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			if (ViewThread.page!=ViewThread.totalPage)
				menu.add(Menu.NONE, Constants.MENU_BBS_VIEW_NEXT, Constants.MENU_BBS_VIEW_NEXT, "")
				.setIcon(R.drawable.bbs_nxt).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add(Menu.NONE, Constants.MENU_BBS_FAVORITE, Constants.MENU_BBS_FAVORITE, 
					Board.favorite.contains(board)?"移出收藏夹":"加入收藏夹");
			menu.add(Menu.NONE, Constants.MENU_BBS_VIEW_POST, Constants.MENU_BBS_VIEW_POST, "发表主题");
		}
		if (showingPage==PAGE_POST) {
			if (ViewPost.page>1)
				menu.add(Menu.NONE, Constants.MENU_BBS_VIEW_PREVIOUS, Constants.MENU_BBS_VIEW_PREVIOUS, "")
				.setIcon(R.drawable.bbs_pre).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			if (ViewPost.page<(ViewPost.totalNum-1)/PAGESIZE+1)
				menu.add(Menu.NONE, Constants.MENU_BBS_VIEW_NEXT, Constants.MENU_BBS_VIEW_NEXT, "")
				.setIcon(R.drawable.bbs_nxt).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add(Menu.NONE, Constants.MENU_BBS_VIEW_POST, Constants.MENU_BBS_VIEW_POST, "发表回复");
			menu.add(Menu.NONE, Constants.MENU_BBS_VIEW_SHARE, Constants.MENU_BBS_VIEW_SHARE, "分享");
		}		
		menu.add(Menu.NONE, Constants.MENU_BBS_VIEW_JUMP, Constants.MENU_BBS_VIEW_JUMP, "跳页");
		menu.add(Menu.NONE, Constants.MENU_BBS_VIEW_EXIT, Constants.MENU_BBS_VIEW_EXIT, "返回首页");		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id==Constants.MENU_BBS_VIEW_PREVIOUS) {
			if (showingPage==PAGE_THREAD)
				ViewThread.getThreads(ViewThread.page-1);
			else if (showingPage==PAGE_POST)
				ViewPost.getPosts(threadid, ViewPost.page-1);
			return true;
		}
		if (id==Constants.MENU_BBS_VIEW_NEXT) {
			if (showingPage==PAGE_THREAD)
				ViewThread.getThreads(ViewThread.page+1);
			else if (showingPage==PAGE_POST)
				ViewPost.getPosts(threadid, ViewPost.page+1);
			return true;
		}
		if (id==Constants.MENU_BBS_FAVORITE) {
			Board.toggleFavorite(board);
			CustomToast.showSuccessToast(this, "操作成功", 1000);
			invalidateOptionsMenu();
			AllBoardsFragment.resetList();
			return true;
		}
		if (id==Constants.MENU_BBS_VIEW_SHARE && showingPage==PAGE_POST) {
			ViewPost.share();
			return true;
		}
		if (id==Constants.MENU_BBS_VIEW_POST) {
			if (showingPage==PAGE_THREAD) {
				Intent intent=new Intent(this, PostActivity.class);
				intent.putExtra("type", "post");
				intent.putExtra("board", board);
				startActivityForResult(intent, PAGE_THREAD);
			}
			else if (showingPage==PAGE_POST) {
				Intent intent=new Intent(this, PostActivity.class);
				intent.putExtra("type", "reply");
				intent.putExtra("board", board);
				intent.putExtra("threadid", threadid);
				intent.putExtra("title", ViewPost.title);
				if (ViewPost.firstFloor!=null) {
					intent.putExtra("postid", ViewPost.firstFloor.postid+"");
					intent.putExtra("number", ViewPost.firstFloor.number+"");
					intent.putExtra("author", ViewPost.firstFloor.author+"");
					intent.putExtra("timestamp", ViewPost.firstFloor.timestamp+"");
				}
				startActivityForResult(intent, PAGE_POST);
			}
			return true;
		}
		if (id==Constants.MENU_BBS_VIEW_JUMP) {
			if (showingPage==PAGE_THREAD) {
				ViewThread.jump();
			}
			else if (showingPage==PAGE_POST) {
				ViewPost.jump();
			}
			return true;
		}
		if (id==Constants.MENU_BBS_VIEW_EXIT) {
			super.wantToExit();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu,View v,ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (showingPage==PAGE_POST) {
			PostInfo postInfo=ViewPost.postInfos.get(ViewPost.index);
			/*
			if (postInfo.attaches.size()!=0)
				menu.add(Menu.NONE, Constants.CONTEXT_MENU_BBS_ATTACHES, 
						Constants.CONTEXT_MENU_BBS_ATTACHES, "查看附件");
			*/
			String content=postInfo.content;
			
			menu.add(Menu.NONE, Constants.CONTEXT_MENU_BBS_POST, 
					Constants.CONTEXT_MENU_BBS_POST, "回复");
			menu.add(Menu.NONE, Constants.CONTEXT_MENU_BBS_MESSAGE, 
					Constants.CONTEXT_MENU_BBS_MESSAGE, "发站内信");
			menu.add(Menu.NONE, Constants.CONTEXT_MENU_BBS_REPRINT, 
					Constants.CONTEXT_MENU_BBS_REPRINT, "转载");
			menu.add(Menu.NONE, Constants.CONTEXT_MENU_BBS_EDIT, 
						Constants.CONTEXT_MENU_BBS_EDIT, "编辑");
			
			urlNum=0;
			Matcher matcher=Patterns.WEB_URL.matcher(content);
			while (matcher.find()) {
				int start=matcher.start();
				int end=matcher.end();
				String url=content.substring(start, end);
				String tmp=url.toLowerCase(Locale.getDefault());
				if (tmp.startsWith("http://") || tmp.startsWith("https://")) {
					if (url.endsWith("'") || url.endsWith("\"") 
							|| url.endsWith(")") || url.endsWith("]")
							|| url.endsWith("}")) 
						url=url.substring(0, url.length()-1);
					menu.add(Menu.NONE, Constants.CONTEXT_MENU_BBS_URL+urlNum, Constants.CONTEXT_MENU_BBS_URL+urlNum, 
						url);
					urlNum++;
				}
			}
			
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int id=item.getItemId();
		/*
		if (id==Constants.CONTEXT_MENU_BBS_ATTACHES) {
			PostInfo postInfo=ViewPost.postInfos.get(ViewPost.index);
			ArrayList<Parameters> attaches=postInfo.attaches;
			if (attaches.size()==0) CustomToast.showInfoToast(this, "没有附件");
			else ViewPost.showAttaches(attaches);
			return true;
		}
		*/
		if (id==Constants.CONTEXT_MENU_BBS_POST
				&& showingPage==PAGE_POST) {
			PostInfo postInfo=ViewPost.postInfos.get(ViewPost.index);
			Intent intent=new Intent(this, PostActivity.class);
			intent.putExtra("type", "reply");
			intent.putExtra("board", board);
			intent.putExtra("threadid", threadid);
			intent.putExtra("title", ViewPost.title);
			intent.putExtra("postid", postInfo.postid+"");
			intent.putExtra("number", postInfo.number+"");
			intent.putExtra("author", postInfo.author+"");
			intent.putExtra("timestamp", postInfo.timestamp+"");
			startActivityForResult(intent, PAGE_POST);
			return true;
		}
		if (id==Constants.CONTEXT_MENU_BBS_MESSAGE) {
			PostInfo postInfo=ViewPost.postInfos.get(ViewPost.index);
			Intent intent=new Intent(this, MessagePostActivity.class);
			intent.putExtra("author", postInfo.author);
			startActivity(intent);
			return true;
		}
		if (id==Constants.CONTEXT_MENU_BBS_REPRINT) {
			ViewPost.reprint(ViewPost.index);
			return true;
		}
		if (id==Constants.CONTEXT_MENU_BBS_EDIT) {
			PostInfo postInfo=ViewPost.postInfos.get(ViewPost.index);
			Intent intent=new Intent(this, PostActivity.class);
			intent.putExtra("type", "edit");
			intent.putExtra("board", board);
			intent.putExtra("number", postInfo.number+"");
			intent.putExtra("timestamp", postInfo.timestamp+"");
			startActivityForResult(intent, 3);
			return true;
		}
		if (id>=Constants.CONTEXT_MENU_BBS_URL && id<Constants.CONTEXT_MENU_BBS_URL+urlNum) {
			String url=item.getTitle().toString();			
			Intent intent=new Intent(this, SubActivity.class);
			intent.putExtra("type", Constants.SUBACTIVITY_TYPE_WEBVIEW);
			intent.putExtra("url", url);
			startActivity(intent);
			
		}
		return super.onContextItemSelected(item);
	}

	protected void wantToExit() {
		if (showingPage==PAGE_POST && !startFromParent) {
			ViewThread.viewThreads();
			return;
		}
		super.wantToExit();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode!=RESULT_OK) return;
		if (requestCode==PAGE_THREAD) {
			ViewThread.getThreads(1);
		}
		else if (requestCode==PAGE_POST) {
			int tp=ViewPost.totalNum/PAGESIZE+1;
			ViewPost.getPosts(threadid, tp);
		}
		else if (requestCode==3) {
			ViewPost.getPosts(threadid, ViewPost.page);
		}
	}
	
}
