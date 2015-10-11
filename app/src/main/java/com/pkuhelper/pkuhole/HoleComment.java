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
import com.pkuhelper.subactivity.SubActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;

public class HoleComment extends Activity {
	HoleInfo holeInfo;
	
	private static final int AUDIO_TYPE_START=0;
	private static final int AUDIO_TYPE_UPDATE=1;
	private static final int AUDIO_TYPE_STOP=2;
	
	View headerView;
	
	MediaPlayer mediaPlayer;
	long starttime;
	boolean modified=false;
	boolean attention=false;
	
	ListView listView;
	
	ArrayList<CommentInfo> arrayList=new ArrayList<CommentInfo>();
	
	Handler handler=new Handler(new Handler.Callback() {
		
		@Override
		public boolean handleMessage(Message msg) {
			if (msg.what==0) {
				setPlayerStatus(AUDIO_TYPE_UPDATE);
				return true;
			}
			return false;
		}
	});
	
	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		int pid=getIntent().getIntExtra("pid", -1);
		holeInfo=HoleInfo.getHoleInfo(pid);
		if (holeInfo==null) {
			finish();
			return;
		}
		mediaPlayer=new MediaPlayer();
		getActionBar().setTitle("查看评论");
		Util.getOverflowMenu(this);
		setContentView(R.layout.hole_comment_listview);
		setResult(RESULT_CANCELED);
		
		setHeaderView();
		listView=(ListView)findViewById(R.id.hole_comment_listview);
		listView.addHeaderView(headerView);
		listView.addFooterView(getLayoutInflater().inflate(R.layout.hole_comment_footerview, null, false));
		
		listView.setAdapter(new BaseAdapter() {
			
			@SuppressLint("ViewHolder")
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				CommentInfo commentInfo=arrayList.get(position);
				convertView=getLayoutInflater().inflate(R.layout.hole_comment_listitem, parent, false);
				ViewSetting.setTextView(convertView, R.id.hole_comment_listitem_id, "#"+commentInfo.cid);
				if (commentInfo.islz)
					convertView.findViewById(R.id.hole_comment_listitem_islz).setVisibility(View.VISIBLE);
				ViewSetting.setTextView(convertView, R.id.hole_comment_listitem_text, commentInfo.text);
				ViewSetting.setTextView(convertView, R.id.hole_comment_listitem_time, MyCalendar.format(commentInfo.timestamp));
				return convertView;
			}
			
			@Override
			public long getItemId(int position) {
				return 0;
			}
			
			@Override
			public Object getItem(int position) {
				return null;
			}
			
			@Override
			public int getCount() {
				return arrayList.size();
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				try {
					String scid=ViewSetting.getTextView(view, R.id.hole_comment_listitem_id);
					if (scid.startsWith("#")) scid=scid.substring(1);
					int cid=Integer.parseInt(scid);
					if (cid!=0)
						reply("Re #"+cid+": ");
				}
				catch (Exception e) {}
				
			}
		});
		
		listView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			
			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				int width=listView.getWidth(), height=listView.getHeight();
				if (width!=0 && height!=0) {
					ViewSetting.setBackground(HoleComment.this, listView,
							R.drawable.chat_bg);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						listView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					}
					else {
						listView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					}
				}
			}
		});
		
		show();
		
	}
	
	@SuppressWarnings("unchecked")
	public void setAttention() {
		new RequestingTask(this, "正在"+(attention?"取消":"")+"关注此树洞", 
				Constants.domain+"/services/pkuhole/api.php?token="+Constants.token
				+"&action=attention&pid="+holeInfo.pid+"&switch="+(attention?0:1), 
				Constants.REQUEST_HOLE_SET_ATTENTION).execute(new ArrayList<Parameters>());
	}
	
	@SuppressLint("InflateParams")
	void setHeaderView() {
		starttime=-1;
		headerView=getLayoutInflater().inflate(R.layout.hole_comment_headerview, null, false);
		ViewSetting.setTextView(headerView, R.id.hole_comment_detail_pid, "#"+holeInfo.pid);
		
		if (holeInfo.type!=HoleInfo.TYPE_TEXT && "".equals(holeInfo.text))
			headerView.findViewById(R.id.hole_comment_detail_text).setVisibility(View.GONE);
		else
			ViewSetting.setTextView(headerView, R.id.hole_comment_detail_text, holeInfo.text);
		
		if (holeInfo.type==HoleInfo.TYPE_IMAGE) {
			headerView.findViewById(R.id.hole_comment_detail_image_layout).setVisibility(View.VISIBLE);
			Bitmap bitmap=holeInfo.getBitmap();
			if (bitmap!=null) {
				headerView.findViewById(R.id.hole_comment_detail_image).setVisibility(View.VISIBLE);
				ViewSetting.setImageBitmap(headerView, R.id.hole_comment_detail_image, bitmap);
				if (bitmap.getHeight()>1.5*bitmap.getWidth())
					headerView.findViewById(R.id.hole_comment_detail_image_too_long).setVisibility(View.VISIBLE);
				
				final String file=MyFile.getCache(HoleComment.this, Util.getHash(holeInfo.url))+"";
			
				ViewSetting.setOnClickListener(headerView, R.id.hole_comment_detail_image, new View.OnClickListener() {
					public void onClick(View v) {
						Intent intent=new Intent(HoleComment.this, SubActivity.class);
						intent.putExtra("type", Constants.SUBACTIVITY_TYPE_PICTURE_FILE);
						intent.putExtra("file", file);
						setPlayerStatus(AUDIO_TYPE_STOP);
						startActivity(intent);
					}
				});	
			}
			else {
				headerView.findViewById(R.id.hole_comment_detail_image_hint).setVisibility(View.VISIBLE);
			}
		}
		if (holeInfo.type==HoleInfo.TYPE_AUDIO) {
			headerView.findViewById(R.id.hole_comment_detail_audio_layout).setVisibility(View.VISIBLE);
			File file=holeInfo.getAudio();
			if (file!=null) {
				headerView.findViewById(R.id.hole_comment_detail_audio).setVisibility(View.VISIBLE);
				headerView.findViewById(R.id.hole_comment_detail_audio_length).setVisibility(View.VISIBLE);
				
				ViewSetting.setImageResource(headerView, R.id.hole_comment_detail_audio, R.drawable.audio_start);
				ViewSetting.setTextView(headerView, R.id.hole_comment_detail_audio_length, holeInfo.extra+"\"");
				
				ViewSetting.setOnClickListener(headerView, R.id.hole_comment_detail_audio, new View.OnClickListener() {
					public void onClick(View v) {
						if (starttime==-1)
							playAudio();
						else
							setPlayerStatus(AUDIO_TYPE_STOP);
					}
				});
				
			}
			else
				headerView.findViewById(R.id.hole_comment_detail_audio_hint).setVisibility(View.VISIBLE);
			
		}
		
		ViewSetting.setTextView(headerView, R.id.hole_comment_detail_time, MyCalendar.format(holeInfo.timestamp));				
	}
	
	@SuppressWarnings("unchecked")
	void show() {
		new RequestingTask(this, "正在获取评论...", 
				Constants.domain+"/services/pkuhole/api.php?action=getcomment&pid="+holeInfo.pid+"&token="+Constants.token, 
				Constants.REQUEST_HOLE_GETCOMMENT).execute(new ArrayList<Parameters>());
	}
	
	void show(String string) {
		try {
			JSONObject jsonObject=new JSONObject(string);
			int code=jsonObject.getInt("code");
			if (code!=0)
				CustomToast.showErrorToast(this, jsonObject.optString("msg", "评论获取失败"));
			attention=jsonObject.optInt("attention")!=0?true:false;
			JSONArray data=jsonObject.getJSONArray("data");
			int len=data.length();
			
			ArrayList<CommentInfo> commentInfos=new ArrayList<CommentInfo>();
			for (int i=0;i<len;i++) {
				JSONObject comment=data.getJSONObject(i);
				commentInfos.add(new CommentInfo(comment.getInt("cid"), 
						comment.optString("text"),
						comment.optInt("islz")!=0?true:false,
								comment.optLong("timestamp")));
			}
			arrayList.clear();
			arrayList.addAll(commentInfos);
			
			ListView listView=(ListView)findViewById(R.id.hole_comment_listview);
			((BaseAdapter)((HeaderViewListAdapter)listView.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
			
			ViewSetting.setTextView(headerView, R.id.hole_comment_number, "评论 ("+len+")");
			
			invalidateOptionsMenu();
		}
		catch (Exception e) {
			e.printStackTrace();
			CustomToast.showErrorToast(this, "评论获取失败");
		}
	}
	
	void finishRequest(int type, String string) {
		if (type==Constants.REQUEST_HOLE_GETCOMMENT) {
			show(string);
		}
		if (type==Constants.REQUEST_HOLE_POST_COMMENT) {
			try {
				JSONObject jsonObject=new JSONObject(string);
				int code=jsonObject.getInt("code");
				if (code!=0) {
					CustomToast.showErrorToast(this, jsonObject.optString("msg", "发表评论失败"));
					return;
				}
				CustomToast.showSuccessToast(this, "评论成功！");
				if (!attention) setResult(RESULT_OK);
				show();
			}
			catch (Exception e) {
				CustomToast.showErrorToast(this, "发表失败");
			}
		}
		if (type==Constants.REQUEST_HOLE_SET_ATTENTION) {
			try {
				JSONObject jsonObject=new JSONObject(string);
				int code=jsonObject.getInt("code");
				if (code!=0) {
					CustomToast.showErrorToast(this, jsonObject.optString("msg", (attention?"取消":"")+"关注失败"));
					return;
				}
				CustomToast.showSuccessToast(this, (attention?"取消":"")+"关注成功");
				attention=!attention;
				invalidateOptionsMenu();
				setResult(RESULT_OK);
			}
			catch (Exception e) {
				CustomToast.showErrorToast(this, (attention?"取消":"")+"关注失败");
			}
		}
		if (type==Constants.REQUEST_HOLE_REPORT) {
			try {
				JSONObject jsonObject=new JSONObject(string);
				int code=jsonObject.getInt("code");
				if (code!=0) {
					CustomToast.showErrorToast(this, jsonObject.optString("msg", "举报失败"));
					return;
				}
				new AlertDialog.Builder(this).setMessage("我们将尽快处理你的举报信息，感谢对P大树洞的支持！")
				.setTitle("举报成功").setPositiveButton("关闭", null).show();
			}
			catch (Exception e) {
				CustomToast.showErrorToast(this, "举报失败");
			}
		}
	}
	
	@SuppressLint("InflateParams")
	void reply(String string) {
		if (!Constants.isValidLogin()) {			
			CustomToast.showErrorToast(this, "登录后才能匿名回复树洞！");
			return;
		}
		
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		final View view=getLayoutInflater().inflate(R.layout.hole_edittext, null, false);
		((EditText)view.findViewById(R.id.hole_edittext)).setHint("请输入评论内容");
		ViewSetting.setEditTextValue(view, R.id.hole_edittext, string);
		
		builder.setTitle("回复树洞").setView(view).setPositiveButton("发送", new DialogInterface.OnClickListener() {
			@SuppressWarnings("unchecked")
			public void onClick(DialogInterface dialog, int which) {
				String content=ViewSetting.getEditTextValue(view, R.id.hole_edittext);
				content=content.trim();
				if (content.length()>=240) {
					CustomToast.showErrorToast(HoleComment.this, "评论请不要超过240字！");
					reply(content);
					return;
				}
				if ("".equals(content)) {
					CustomToast.showErrorToast(HoleComment.this, "没有回复内容！");
					reply("");
					return;
				}
				
				ArrayList<Parameters> arrayList=new ArrayList<Parameters>();
				arrayList.add(new Parameters("action", "docomment"));
				arrayList.add(new Parameters("token", Constants.token));
				arrayList.add(new Parameters("pid", holeInfo.pid+""));
				arrayList.add(new Parameters("text", content));
				
				new RequestingTask(HoleComment.this, "正在发表...", 
						Constants.domain+"/services/pkuhole/api.php", Constants.REQUEST_HOLE_POST_COMMENT)
				.execute(arrayList);
			}
		}).setNegativeButton("取消", null).show();
		
	}
	
	@SuppressLint("InflateParams")
	void report() {
		
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		final View view=getLayoutInflater().inflate(R.layout.hole_edittext, null, false);
		((EditText)view.findViewById(R.id.hole_edittext)).setHint("请简述举报理由");
		
		builder.setTitle("举报此树洞").setView(view).setPositiveButton("确认", new DialogInterface.OnClickListener() {
			@SuppressWarnings("unchecked")
			public void onClick(DialogInterface dialog, int which) {
				String content=ViewSetting.getEditTextValue(view, R.id.hole_edittext);
				content=content.trim();
				
				ArrayList<Parameters> arrayList=new ArrayList<Parameters>();
				arrayList.add(new Parameters("action", "report"));
				arrayList.add(new Parameters("token", Constants.token));
				arrayList.add(new Parameters("pid", holeInfo.pid+""));
				arrayList.add(new Parameters("reason", content));
				
				new RequestingTask(HoleComment.this, "正在举报...", 
						Constants.domain+"/services/pkuhole/api.php", Constants.REQUEST_HOLE_REPORT)
				.execute(arrayList);
			}
		}).setNegativeButton("取消", null).show();
		
	}
	
	void playAudio() {
		File audioFile=holeInfo.getAudio();
		if (audioFile==null) return;
		try {
			mediaPlayer.reset();
			mediaPlayer.setDataSource(audioFile.getAbsolutePath());
			mediaPlayer.setLooping(false);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.prepare();
			mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				public void onCompletion(MediaPlayer mp) {
					setPlayerStatus(AUDIO_TYPE_STOP);
				}
			});
			mediaPlayer.start();
			starttime=System.currentTimeMillis();
			setPlayerStatus(AUDIO_TYPE_START);
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					while (starttime!=-1) {
						try {
							handler.sendEmptyMessage(0);
							Thread.sleep(500);
						}
						catch (Exception e) {}
					}
				}
			}).start();
		}
		catch (Exception e) {
			e.printStackTrace();
			CustomToast.showErrorToast(this, "无法播放音频");
			return;
		}
		
	}
	
	public void setPlayerStatus(int statusType) {
		if (statusType==AUDIO_TYPE_START
				|| statusType==AUDIO_TYPE_UPDATE) {
			if (statusType==AUDIO_TYPE_START)
				ViewSetting.setImageResource(this, R.id.hole_comment_detail_audio, R.drawable.audio_stop);
			int lefttime=holeInfo.extra-(int)(System.currentTimeMillis()-starttime)/1000;
			if (lefttime<=0) lefttime=0;
			ViewSetting.setTextView(this, R.id.hole_comment_detail_audio_length, lefttime+"\"");
		}
		else if (statusType==AUDIO_TYPE_STOP) {
			ViewSetting.setImageResource(this, R.id.hole_comment_detail_audio, R.drawable.audio_start);
			ViewSetting.setTextView(this, R.id.hole_comment_detail_audio_length, holeInfo.extra+"\"");
			try {
				mediaPlayer.stop();
				mediaPlayer.reset();
			}
			catch (Exception e) {}
			starttime=-1;
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
	public boolean onCreateOptionsMenu(Menu menu) {
		Util.setIconEnable(menu, true);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(Menu.NONE, Constants.MENU_PKUHOLE_ADD, Constants.MENU_PKUHOLE_ADD, "")
		.setIcon(R.drawable.reply).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(Menu.NONE, Constants.MENU_PKUHOLE_ATTENSION, Constants.MENU_PKUHOLE_ATTENSION, "")
		.setIcon(attention?R.drawable.star_solid:R.drawable.star_hollow).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(Menu.NONE, Constants.MENU_PKUHOLE_REPORT, Constants.MENU_PKUHOLE_REPORT, "")
		.setIcon(R.drawable.open).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(Menu.NONE, Constants.MENU_PKUHOLE_CLOSE, Constants.MENU_PKUHOLE_CLOSE, "")
		.setIcon(R.drawable.close).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id=item.getItemId();
		if (id==Constants.MENU_PKUHOLE_ADD) {
			reply("");
			return true;
		}
		if (id==Constants.MENU_PKUHOLE_ATTENSION) {
			setAttention();
			return true;
		}
		if (id==Constants.MENU_PKUHOLE_REPORT) {
			report();
			return true;
		}
		if (id==Constants.MENU_PKUHOLE_CLOSE) {
			setPlayerStatus(AUDIO_TYPE_STOP);
			finish();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onPause() {
		setPlayerStatus(AUDIO_TYPE_STOP);
		super.onPause();
	}
	
}
