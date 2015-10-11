package com.pkuhelper.pkuhole;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.MyCalendar;
import com.pkuhelper.lib.MyFile;
import com.pkuhelper.lib.Util;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.lib.webconnection.WebConnection;
import com.pkuhelper.subactivity.SubActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;

public class HoleActivity extends Activity {
	public static final int PAGE_ALL = 0;
	public static final int PAGE_MINE = 1;
	
	private static final int AUDIO_TYPE_START=0;
	private static final int AUDIO_TYPE_UPDATE=1;
	private static final int AUDIO_TYPE_STOP=2;
	
	ArrayList<HoleInfo> allInfos=null;
	ArrayList<HoleInfo> myInfos=null;
	
	//static HoleActivity holeActivity;
	Handler handler;
	
	int page;
	int requestpage=1;
	int currpage=1;
	boolean requesting=false;
	
	MediaPlayer mediaPlayer;
	int playingpid;
	long startplayingtime;
	
	long timestamp;
	
	View headerView;
	View returnTopView;
	
	SwipeRefreshLayout swipeRefreshLayout;
	
	@SuppressLint("InflateParams")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//holeActivity=this;
		getActionBar().setTitle("P大树洞");
		
		MyFile.getCache(this, "").mkdirs();
		mediaPlayer=new MediaPlayer();
		
		Util.getOverflowMenu(this);
		
		handler=new Handler(new Handler.Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				if (msg.what==Constants.MESSAGE_HOLE_FILE_DOWNLOAD_FINISHED) {
					update();
					return true;
				}
				if (msg.what==Constants.MESSAGE_HOLE_AUDIO_TIME_UPDATE) {
					setPlayerStatus(AUDIO_TYPE_UPDATE);
					return true;
				}
				if (msg.what==Constants.MESSAGE_PKUHOLE_LIST_MORE_FAILED) {
					String string=(String)msg.obj;
					if ("-1".equals(string))
						CustomToast.showErrorToast(HoleActivity.this, "无法连接网络(-1,-1)");
					else
						CustomToast.showErrorToast(HoleActivity.this, "无法连接到服务器 (HTTP "+string+")");
					setRefreshing();
					return true;
				}
				if (msg.what==Constants.MESSAGE_PKUHOLE_LIST_MORE_FINISHED) {
					String string=(String)msg.obj;
					finishMoreRequest(string);
					return true;
				}
				if (msg.what==Constants.MESSAGE_PKUHOLE_REFRESH_FAILED) {
					setRefreshing();
					return true;
				}
				if (msg.what==Constants.MESSAGE_PKUHOLE_REFRESH_FINISHED) {
					String string=(String)msg.obj;
					finishRefresh(string);
					return true;
				}
				if (msg.what==Constants.MESSAGE_SLEEP_FINISHED) {
					swipeRefreshLayout.setRefreshing(false);
					return true;
				}
				return false;
			}
		});
		
		int p=getIntent().getIntExtra("page", 0);
		if (p!=PAGE_MINE) p=PAGE_ALL;
		page=p;
		playingpid=-1;
		startplayingtime=0;
		
		setContentView(R.layout.hole_listview);
		
		swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.hole_swipeRefreshLayout);
		swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_purple, 
	            android.R.color.holo_green_light, 
	            android.R.color.holo_blue_bright, 
	            android.R.color.holo_orange_light);
		
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			public void onRefresh() {
				pullToRefresh();
			}
		});
		
		returnTopView=findViewById(R.id.hole_returntop);
		returnTopView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ListView listView=(ListView)findViewById(R.id.hole_listview);
				listView.smoothScrollToPosition(0);
			}
		});
		
		swipeRefreshLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			
			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				int width=swipeRefreshLayout.getWidth(), height=swipeRefreshLayout.getHeight();
				if (width!=0 && height!=0) {
					ViewSetting.setBackground(HoleActivity.this, swipeRefreshLayout,
							R.drawable.chat_bg);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						swipeRefreshLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					}
					else {
						swipeRefreshLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					}
				}
			}
		});
		
		ListView listView=(ListView)findViewById(R.id.hole_listview);
		headerView=getLayoutInflater().inflate(R.layout.hole_list_headerview, null, false);
		listView.addHeaderView(headerView);
		
		show(page);		
	}

	void pullToRefresh() {
		if (page!=PAGE_ALL || timestamp==0) {
			setRefreshing();
			return;
		}
		ListView listView=(ListView)findViewById(R.id.hole_listview);
		if (listView==null) return;
		new Thread(new Runnable() {
			public void run() {
				Parameters parameters=WebConnection.connect(
						Constants.domain+"/services/pkuhole/api.php?action=refreshlist&timestamp="+timestamp, null);
				if (!"200".equals(parameters.name)) 
					handler.sendMessage(Message.obtain(handler, Constants.MESSAGE_PKUHOLE_REFRESH_FAILED, parameters.name));
				else
					handler.sendMessage(Message.obtain(handler, Constants.MESSAGE_PKUHOLE_REFRESH_FINISHED, parameters.value));
			}
		}).start();		
	}
	
	void finishRefresh(String string) {		
		if (allInfos==null) return;
		ArrayList<HoleInfo> arrayList=new ArrayList<HoleInfo>();
		try {
			JSONObject jsonObject=new JSONObject(string);
			int code=jsonObject.getInt("code");
			if (code!=0) return;
			timestamp=jsonObject.getLong("timestamp");
			
			ListView listView=(ListView)findViewById(R.id.hole_listview);
			if (listView==null) return;
			
			JSONArray jsonArray=jsonObject.getJSONArray("data");
			int len=jsonArray.length();
			for (int i=0;i<len;i++) {
				JSONObject object=jsonArray.getJSONObject(i);
				HoleInfo holeInfo=new HoleInfo(this, handler, object.getInt("pid"), object.optString("text"), 
						object.optLong("timestamp"), object.optString("type"), object.optInt("reply"), 
						object.optInt("likenum"), object.optInt("extra"), object.optString("url"));
				arrayList.add(holeInfo);
			}
			if (len==0) return;
			
			int size=allInfos.size();
			
			allInfos.addAll(0, arrayList);
			while (allInfos.size()>size) {
				allInfos.remove(size);
			}
			
			if (page==PAGE_ALL)
				((BaseAdapter)((HeaderViewListAdapter)listView.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			setRefreshing();
		}
	}
	
	public void setRefreshing() {
		new Thread(new Runnable() {
			public void run() {
				try {Thread.sleep(500);
				}catch (Exception e) {}
				handler.sendEmptyMessage(Constants.MESSAGE_SLEEP_FINISHED);
			}
		}).start();
	}
	
	void show(int pg) {
		if (pg!=PAGE_MINE) pg=PAGE_ALL;
		if (pg==PAGE_MINE) show(pg, myInfos);
		else show(pg, allInfos);
	}
	@SuppressLint("InflateParams")
	void show(int pg, final ArrayList<HoleInfo> list) {
		setPlayerStatus(AUDIO_TYPE_STOP);
		if (pg!=PAGE_MINE) pg=PAGE_ALL;
		page=pg;
		ListView listView=(ListView)findViewById(R.id.hole_listview);
		if (listView==null) return;
		setHeaderView();
		if (list==null) {
			getlist(pg);
			listView.setAdapter(new BaseAdapter() {
				public View getView(int position, View convertView, ViewGroup parent) {
					return null;
				}
				public long getItemId(int position) {
					return 0;
				}
				@Override
				public Object getItem(int position) {
					return null;
				}
				public int getCount() {
					return 0;
				}
			});
			return;
		}
		if (page==PAGE_ALL) {
			currpage=requestpage;
		}
		listView.setAdapter(new BaseAdapter() {
			
			@SuppressLint("ViewHolder")
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				HoleInfo holeInfo=list.get(position);
				convertView=getLayoutInflater().inflate(R.layout.hole_list_item, parent, false);
				ViewSetting.setTextView(convertView, R.id.hole_listitem_pid, "#"+holeInfo.pid);
				
				if (holeInfo.type!=HoleInfo.TYPE_TEXT && "".equals(holeInfo.text))
					convertView.findViewById(R.id.hole_listitem_text).setVisibility(View.GONE);
				else
					ViewSetting.setTextView(convertView, R.id.hole_listitem_text, holeInfo.text);
				
				if (holeInfo.type==HoleInfo.TYPE_IMAGE) {
					convertView.findViewById(R.id.hole_listitem_image_layout).setVisibility(View.VISIBLE);
					Bitmap bitmap=holeInfo.getBitmap();
					if (bitmap!=null) {
						convertView.findViewById(R.id.hole_listitem_image).setVisibility(View.VISIBLE);
						ViewSetting.setImageBitmap(convertView, R.id.hole_listitem_image, bitmap);
						if (bitmap.getHeight()>1.5*bitmap.getWidth())
							convertView.findViewById(R.id.hole_listitem_image_too_long).setVisibility(View.VISIBLE);
						
						final String file=MyFile.getCache(HoleActivity.this, Util.getHash(holeInfo.url))+"";
					
						ViewSetting.setOnClickListener(convertView, R.id.hole_listitem_image, new View.OnClickListener() {
							public void onClick(View v) {
								Intent intent=new Intent(HoleActivity.this, SubActivity.class);
								intent.putExtra("type", Constants.SUBACTIVITY_TYPE_PICTURE_FILE);
								intent.putExtra("file", file);
								setPlayerStatus(AUDIO_TYPE_STOP);
								startActivity(intent);
							}
						});	
					}
					else {
						convertView.findViewById(R.id.hole_listitem_image_hint).setVisibility(View.VISIBLE);
					}
				}
				if (holeInfo.type==HoleInfo.TYPE_AUDIO) {
					convertView.findViewById(R.id.hole_listitem_audio_layout).setVisibility(View.VISIBLE);
					File file=holeInfo.getAudio();
					if (file!=null) {
						convertView.findViewById(R.id.hole_listitem_audio).setVisibility(View.VISIBLE);
						convertView.findViewById(R.id.hole_listitem_audio_length).setVisibility(View.VISIBLE);
						
						// isplaying
						if (playingpid==holeInfo.pid) {
							ViewSetting.setImageResource(convertView, R.id.hole_listitem_audio, R.drawable.audio_stop);
							long time=System.currentTimeMillis();
							long deltatime=(time-startplayingtime)/1000;
							int lefttime=(holeInfo.extra-(int)deltatime);
							if (lefttime<0) lefttime=0;
							ViewSetting.setTextView(convertView, R.id.hole_listitem_audio_length, lefttime+"'");
						}
						else {
							ViewSetting.setImageResource(convertView, R.id.hole_listitem_audio, R.drawable.audio_start);
							ViewSetting.setTextView(convertView, R.id.hole_listitem_audio_length, holeInfo.extra+"\"");
						}
						
						final int pid=holeInfo.pid;
						ViewSetting.setOnClickListener(convertView, R.id.hole_listitem_audio, new View.OnClickListener() {
							public void onClick(View v) {
								togglePlay(pid);
							}
						});
						
					}
					else
						convertView.findViewById(R.id.hole_listitem_audio_hint).setVisibility(View.VISIBLE);
					
				}
				
				ViewSetting.setTextView(convertView, R.id.hole_listitem_time, MyCalendar.format(holeInfo.timestamp));				
				
				ViewSetting.setTextView(convertView, R.id.hole_listitem_like, "("+holeInfo.like+")");
				ViewSetting.setTextView(convertView, R.id.hole_listitem_reply, "("+holeInfo.reply+")");
				
				return convertView;
			}
			
			@Override
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public Object getItem(int position) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int getCount() {
				return list.size();
			}
		});
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
			
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem>=3) {
					if (returnTopView.getVisibility()==View.GONE)
						returnTopView.setVisibility(View.VISIBLE);
				}
				else {
					if (returnTopView.getVisibility()==View.VISIBLE)
						returnTopView.setVisibility(View.GONE);
				}
				if (totalItemCount!=0) {
					int lastItem=firstVisibleItem+visibleItemCount;
					int itemLeft=3;
					if (lastItem>=totalItemCount-itemLeft)
						requestMore();
				}
				
				try {
					swipeRefreshLayout.setEnabled(firstVisibleItem==0 && view.getChildAt(0).getTop()>=0);
				}
				catch (Exception e) {}
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				try {
					String spid=ViewSetting.getTextView(view, R.id.hole_listitem_pid);
					if (spid.startsWith("#")) spid=spid.substring(1);
					int pid=Integer.parseInt(spid);
					
					Intent intent=new Intent(HoleActivity.this, HoleComment.class);
					intent.putExtra("pid", pid);
					setPlayerStatus(AUDIO_TYPE_STOP);
					startActivityForResult(intent, 1);
					
				}
				catch (Exception e) {}
			}
		});
		
		
		
	}
	
	void setHeaderView() {
		
		if (page==PAGE_ALL) {
			headerView.findViewById(R.id.hole_viewall).setBackgroundResource(R.drawable.hole_button_left_selected);
			headerView.findViewById(R.id.hole_viewmine).setBackgroundResource(R.drawable.hole_button_right);
			ViewSetting.setTextViewColor(headerView, R.id.hole_viewall, Color.parseColor("#e8e8e7"));
			ViewSetting.setTextViewColor(headerView, R.id.hole_viewmine, Color.parseColor("#333333"));
		}
		else {
			headerView.findViewById(R.id.hole_viewall).setBackgroundResource(R.drawable.hole_button_left);
			headerView.findViewById(R.id.hole_viewmine).setBackgroundResource(R.drawable.hole_button_right_selected);
			ViewSetting.setTextViewColor(headerView, R.id.hole_viewmine, Color.parseColor("#e8e8e7"));
			ViewSetting.setTextViewColor(headerView, R.id.hole_viewall, Color.parseColor("#333333"));
		}
		
		ViewSetting.setOnClickListener(headerView, R.id.hole_viewall, new View.OnClickListener() {
			public void onClick(View v) {
				if (page==PAGE_ALL) return;
				else show(PAGE_ALL);
			}
		});
		ViewSetting.setOnClickListener(headerView, R.id.hole_viewmine, new View.OnClickListener() {
			public void onClick(View v) {
				if (page==PAGE_MINE) return;
				else show(PAGE_MINE);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	void getlist(int pg) {
		if (pg!=PAGE_MINE) pg=PAGE_ALL;
		String url=Constants.domain+"/services/pkuhole/api.php";
		ArrayList<Parameters> arrayList=new ArrayList<Parameters>();
		int requestType;
		if (pg==PAGE_ALL) {
			arrayList.add(new Parameters("action", "getlist"));
			arrayList.add(new Parameters("p", "1"));
			requestType=Constants.REQUEST_HOLE_GETLIST_ALL;
			requestpage=1;
			requesting=false;
		}
		else {
			arrayList.add(new Parameters("action", "getattention"));
			arrayList.add(new Parameters("token", Constants.token));
			requestType=Constants.REQUEST_HOLE_GETLIST_MINE;
		}
		new RequestingTask(this, "正在获取...", url, requestType).execute(arrayList);
		
	}
	void finishRequest(int type, String string) { 
		if (type==Constants.REQUEST_HOLE_GETLIST_ALL 
				|| type==Constants.REQUEST_HOLE_GETLIST_MINE) {
			
			try {
				JSONObject jsonObject=new JSONObject(string);
				int code=jsonObject.getInt("code");
				if (code!=0) {
					CustomToast.showErrorToast(this, jsonObject.optString("msg", "获取失败"));
					return;
				}
				
				JSONArray array=jsonObject.getJSONArray("data");
				ArrayList<HoleInfo> arrayList=new ArrayList<HoleInfo>();
				int len=array.length();
				for (int i=0;i<len;i++) {
					JSONObject object=array.getJSONObject(i);
					HoleInfo holeInfo=new HoleInfo(this, handler, object.getInt("pid"), object.optString("text"), 
							object.optLong("timestamp"), object.optString("type"), object.optInt("reply"), 
							object.optInt("likenum"), object.optInt("extra"), object.optString("url"));
					arrayList.add(holeInfo);
				}
				if (len==0)
					CustomToast.showInfoToast(this, "还没有消息！");
				
				if (type==Constants.REQUEST_HOLE_GETLIST_ALL) {
					allInfos=new ArrayList<HoleInfo>(arrayList);
					currpage=requestpage;
					requesting=false;
					timestamp=jsonObject.getLong("timestamp");
					show(PAGE_ALL);
				}
				else {
					myInfos=new ArrayList<HoleInfo>(arrayList);
					show(PAGE_MINE);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				CustomToast.showErrorToast(this, "树洞获取失败，请重试。");
			}
		}
		if (type==Constants.REQUEST_HOLE_GET_SETTINGS)
			showSettingsDialog(string);
		if (type==Constants.REQUEST_HOLE_SET_SETTINGS) {
			try {
				JSONObject jsonObject=new JSONObject(string);
				int code=jsonObject.getInt("code");
				if (code!=0)
					CustomToast.showErrorToast(this, jsonObject.optString("msg", "设置失败"));
				else
					CustomToast.showSuccessToast(this, "设置成功！");
			}
			catch (Exception e) {
				CustomToast.showErrorToast(this, "设置失败");
			}
		}
	}
	
	void update() {
		try {
			ListView listView=(ListView)findViewById(R.id.hole_listview);
			if (listView==null) return;
			int cnt=listView.getCount();
			for (int i=0;i<cnt;i++) {
				try {
					View view=listView.getChildAt(i);
					if (view==null) continue;
					String spid=ViewSetting.getTextView(view, R.id.hole_listitem_pid);
					if (spid.startsWith("#")) spid=spid.substring(1);
					final int pid=Integer.parseInt(spid);
					HoleInfo holeInfo=HoleInfo.getHoleInfo(pid);
					if (holeInfo==null) continue;
					if (holeInfo.type==HoleInfo.TYPE_IMAGE) {
						if (view.findViewById(R.id.hole_listitem_image).getVisibility()!=View.GONE)
							continue;
						Bitmap bitmap=holeInfo.getBitmap();
						if (bitmap==null) continue;
						view.findViewById(R.id.hole_listitem_image_hint).setVisibility(View.GONE);
						view.findViewById(R.id.hole_listitem_image).setVisibility(View.VISIBLE);
						ViewSetting.setImageBitmap(view, R.id.hole_listitem_image, bitmap);
						if (bitmap.getHeight()>1.5*bitmap.getWidth())
							view.findViewById(R.id.hole_listitem_image_too_long).setVisibility(View.VISIBLE);
						final String file=MyFile.getCache(HoleActivity.this, Util.getHash(holeInfo.url))+"";
						
						ViewSetting.setOnClickListener(view, R.id.hole_listitem_image, new View.OnClickListener() {
							public void onClick(View v) {
								Intent intent=new Intent(HoleActivity.this, SubActivity.class);
								intent.putExtra("type", Constants.SUBACTIVITY_TYPE_PICTURE_FILE);
								intent.putExtra("file", file);
								setPlayerStatus(AUDIO_TYPE_STOP);
								startActivity(intent);
							}
						});
					
					}
					if (holeInfo.type==HoleInfo.TYPE_AUDIO) {
						if (view.findViewById(R.id.hole_listitem_audio).getVisibility()!=View.GONE)
							continue;
						File file=holeInfo.getAudio();
						if (file==null) continue;
						view.findViewById(R.id.hole_listitem_audio_hint).setVisibility(View.GONE);
						view.findViewById(R.id.hole_listitem_audio).setVisibility(View.VISIBLE);
						view.findViewById(R.id.hole_listitem_audio_length).setVisibility(View.VISIBLE);
							
						ViewSetting.setImageResource(view, R.id.hole_listitem_audio, R.drawable.audio_start);
						ViewSetting.setTextView(view, R.id.hole_listitem_audio_length, holeInfo.extra+"\"");
						
						ViewSetting.setOnClickListener(view, R.id.hole_listitem_audio, new View.OnClickListener() {
							public void onClick(View v) {
								togglePlay(pid);
							}
						});
						
					}
				}
				catch (Exception ee) {}
			}
			
		}
		catch (Exception e) {}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Util.setIconEnable(menu, true);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(Menu.NONE, Constants.MENU_PKUHOLE_ADD, Constants.MENU_PKUHOLE_ADD, "")
		.setIcon(R.drawable.add).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		/*
		menu.add(Menu.NONE, Constants.MENU_PKUHOLE_SETTINGS, Constants.MENU_PKUHOLE_SETTINGS, "")
		.setIcon(R.drawable.set).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		*/
		menu.add(Menu.NONE, Constants.MENU_PKUHOLE_SEARCH, Constants.MENU_PKUHOLE_SEARCH, "")
		.setIcon(R.drawable.icon_search).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(Menu.NONE, Constants.MENU_PKUHOLE_CLOSE, Constants.MENU_PKUHOLE_CLOSE, "")
		.setIcon(R.drawable.close).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == Constants.MENU_PKUHOLE_ADD) {
			if (!Constants.isValidLogin()) {
				CustomToast.showErrorToast(this, "有效登录后才能匿名发布树洞！");
				return true;
			}
			setPlayerStatus(AUDIO_TYPE_STOP);
			startActivityForResult(new Intent(this, HolePost.class), 0);
			return true;
		}
		if (id == Constants.MENU_PKUHOLE_SETTINGS) {
			if (!Constants.isValidLogin()) {
				CustomToast.showErrorToast(this, "请先有效登录。");
				return true;
			}
			setPlayerStatus(AUDIO_TYPE_STOP);
			new RequestingTask(this, "正在获取推送设置..", 
					Constants.domain+"/services/pkuhole/api.php?action=pushsettings_get&token="+Constants.token, 
					Constants.REQUEST_HOLE_GET_SETTINGS).execute(new ArrayList<Parameters>());
			return true;
		}
		if (id==Constants.MENU_PKUHOLE_SEARCH) {
			setPlayerStatus(AUDIO_TYPE_STOP);
			startActivity(new Intent(this, HoleSearch.class));
			return true;
		}
		if (id == Constants.MENU_PKUHOLE_CLOSE) {
			setPlayerStatus(AUDIO_TYPE_STOP);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void refresh() {
		myInfos=null;
		allInfos=null;
		requesting=false;
		requestpage=1;
		show(page);
	}
	
	public void togglePlay(int pid) {
		HoleInfo holeInfo=HoleInfo.getHoleInfo(pid);
		if (holeInfo==null) return;
		File audioFile=holeInfo.getAudio();
		if (audioFile==null) return;
		boolean another=pid!=playingpid;
		try {
			setPlayerStatus(AUDIO_TYPE_STOP);
			if (!another) return;
			mediaPlayer.reset();
			mediaPlayer.setDataSource(audioFile.getAbsolutePath());
			mediaPlayer.setLooping(false);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.prepare();
			mediaPlayer.setVolume(1f, 1f);
			mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				public void onCompletion(MediaPlayer mp) {
					setPlayerStatus(AUDIO_TYPE_STOP);
				}
			});
			mediaPlayer.start();
			playingpid=pid;
			startplayingtime=System.currentTimeMillis();
			setPlayerStatus(AUDIO_TYPE_START);
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					while (playingpid!=-1) {
						try {
							handler.sendEmptyMessage(Constants.MESSAGE_HOLE_AUDIO_TIME_UPDATE);
							Thread.sleep(500);
						}
						catch (Exception e) {}
					}
				}
			}).start();
			
		}
		catch (Exception e) {
			e.printStackTrace();
			CustomToast.showErrorToast(this, "无法播放，不支持的格式");
		}
	}
	
	public void setPlayerStatus(int statusType) {
		if (playingpid==-1) return;
		ListView listView=(ListView)findViewById(R.id.hole_listview);
		if (listView==null) return;
		int cnt=listView.getCount();
		for (int i=0;i<cnt;i++) {
			try {
				View view=listView.getChildAt(i);
				if (view==null) continue;
				String spid=ViewSetting.getTextView(view, R.id.hole_listitem_pid);
				if (spid.startsWith("#")) spid=spid.substring(1);
				final int pid=Integer.parseInt(spid);
				if (pid==playingpid) {
					HoleInfo holeInfo=HoleInfo.getHoleInfo(pid);
					if (holeInfo==null) return;
					if (statusType==AUDIO_TYPE_START
							|| statusType==AUDIO_TYPE_UPDATE) {
						if (statusType==AUDIO_TYPE_START)
							ViewSetting.setImageResource(view, R.id.hole_listitem_audio, R.drawable.audio_stop);
						long time=System.currentTimeMillis();
						int lefttime=holeInfo.extra-(int)(time-startplayingtime)/1000;
						if (lefttime<=0) lefttime=0;
						ViewSetting.setTextView(view, R.id.hole_listitem_audio_length, lefttime+"\"");
					}
					else if (statusType==AUDIO_TYPE_STOP) {
						ViewSetting.setImageResource(view, R.id.hole_listitem_audio, R.drawable.audio_start);
						ViewSetting.setTextView(view, R.id.hole_listitem_audio_length, holeInfo.extra+"\"");
					}
					break;
				}
			}
			catch (Exception e) {}
		}
		if (statusType==AUDIO_TYPE_STOP) {
			try {
				mediaPlayer.stop();
				mediaPlayer.reset();
			}
			catch (Exception e) {}
			playingpid=-1;
		}
		
	}
	
	void requestMore() {
		if (page!=PAGE_ALL) return;
		if (requesting) return;
		requesting=true;
		
		requestpage=currpage+1;
		Log.w("request-page", requestpage+"");
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String url=Constants.domain+"/services/pkuhole/api.php?action=getlist&p="+requestpage;
				Parameters parameters=WebConnection.connect(url, null);
				if (!"200".equals(parameters.name))
					handler.sendMessage(Message.obtain(handler, Constants.MESSAGE_PKUHOLE_LIST_MORE_FAILED, parameters.name));
				else
					handler.sendMessage(Message.obtain(handler, Constants.MESSAGE_PKUHOLE_LIST_MORE_FINISHED, parameters.value));				
			}
		}).start();
		
	}
	
	void finishMoreRequest(String string) {
		if (page!=PAGE_ALL || allInfos==null) return;
		ListView listView=(ListView)findViewById(R.id.hole_listview);
		if (listView==null) return;
		if (requestpage!=currpage+1) return;
		
		ArrayList<HoleInfo> arrayList=new ArrayList<HoleInfo>();
		try {
			JSONObject jsonObject=new JSONObject(string);
			int code=jsonObject.getInt("code");
			if (code!=0){
				CustomToast.showErrorToast(this, jsonObject.optString("msg", "更多内容加载失败"));
				return;
			}
			JSONArray jsonArray=jsonObject.getJSONArray("data");
			int len=jsonArray.length();
			for (int i=0;i<len;i++) {
				JSONObject object=jsonArray.getJSONObject(i);
				HoleInfo holeInfo=new HoleInfo(this, handler, object.getInt("pid"), object.optString("text"), 
						object.optLong("timestamp"), object.optString("type"), object.optInt("reply"), 
						object.optInt("likenum"), object.optInt("extra"), object.optString("url"));
				arrayList.add(holeInfo);
			}
			if (len==0) return;
			
			allInfos.addAll(arrayList);
			
			((BaseAdapter)((HeaderViewListAdapter)listView.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
			
			currpage=requestpage;
			requesting=false;
		}
		catch (Exception e) {
			CustomToast.showErrorToast(this, "更多内容获取失败。");
			e.printStackTrace();
		}
		
	}
	
	@SuppressLint("InflateParams")
	void showSettingsDialog(String string) {
		try {
			JSONObject jsonObject=new JSONObject(string);
			int code=jsonObject.getInt("code");
			if (code!=0) {
				CustomToast.showErrorToast(this, jsonObject.optString("msg", "推送设置失败"));
				return;
			}
			JSONObject data=jsonObject.getJSONObject("data");
			boolean push=data.optInt("pkuhole_push")!=0;
			boolean hide=data.optInt("pkuhole_hide_content")!=0;
			AlertDialog.Builder builder=new AlertDialog.Builder(this);
			final View settingView=getLayoutInflater().inflate(R.layout.hole_push_settings, null, false);
			ViewSetting.setSwitchChecked(settingView, R.id.hole_setting_push_switch, push);
			ViewSetting.setSwitchChecked(settingView, R.id.hole_setting_hide_switch, hide);
			if (!push)
				settingView.findViewById(R.id.hole_setting_hide_switch).setEnabled(false);
			ViewSetting.setSwitchOnCheckChangeListener(settingView, R.id.hole_setting_push_switch, 
					new CompoundButton.OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					settingView.findViewById(R.id.hole_setting_hide_switch).setEnabled(isChecked);			
				}
			});
			
			builder.setView(settingView).setTitle("推送设置")
			.setPositiveButton("确认", new DialogInterface.OnClickListener() {
				@SuppressWarnings("unchecked")
				public void onClick(DialogInterface dialog, int which) {
					boolean push=ViewSetting.getSwitchChecked(settingView, R.id.hole_setting_push_switch);
					boolean hide=ViewSetting.getSwitchChecked(settingView, R.id.hole_setting_hide_switch);
					
					try {
						JSONObject jsonObject=new JSONObject();
						jsonObject.put("pkuhole_push", push?1:0);
						jsonObject.put("pkuhole_hide_content", hide?1:0);
						
						ArrayList<Parameters> arrayList=new ArrayList<Parameters>();
						arrayList.add(new Parameters("action", "pushsettings_set"));
						arrayList.add(new Parameters("token", Constants.token));
						arrayList.add(new Parameters("data", jsonObject.toString()));						
						
						new RequestingTask(HoleActivity.this, "正在保存设置...", 
								Constants.domain+"/services/pkuhole/api.php", Constants.REQUEST_HOLE_SET_SETTINGS)
						.execute(arrayList);
					}
					catch (Exception e) {
						CustomToast.showErrorToast(HoleActivity.this, "设置失败");
					}
					
				}
			}).setNegativeButton("取消", null).show();
			
			
		}
		catch (Exception e) {
			e.printStackTrace();
			CustomToast.showErrorToast(this, "推送设置获取失败。");
		}
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode!=RESULT_OK) return;
		if (requestCode==0) {
			myInfos=null;
			if (page==PAGE_MINE) show(page);
			else if (page==PAGE_ALL) pullToRefresh();
			return;
		}
		if (requestCode==1) {
			myInfos=null;
			if (page==PAGE_MINE) show(page);
		}
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode==KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN) {
			setPlayerStatus(AUDIO_TYPE_STOP);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onPause() {
		setPlayerStatus(AUDIO_TYPE_STOP);
		super.onPause();
	}
	
}
