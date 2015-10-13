package com.pkuhelper.pkuhole;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.pkuhelper.R;
import com.pkuhelper.lib.BaseActivity;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.MyBitmapFactory;
import com.pkuhelper.lib.MyFile;
import com.pkuhelper.lib.RequestingTask;
import com.pkuhelper.lib.Util;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.subactivity.SubActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

public class HolePost extends BaseActivity{
	
	Uri imageUri=null;
	File audioFile=null;
	byte[] bts=null;
	
	AlertDialog audioDialog=null;
	long starttime=-1;
	int length=0;
	
	MediaRecorder mediaRecorder;
	MediaPlayer mediaPlayer;
	
	private static final int AUDIO_TYPE_START=0;
	private static final int AUDIO_TYPE_UPDATE=1;
	private static final int AUDIO_TYPE_STOP=2;
	
	Handler handler=new Handler(new Handler.Callback() {
		public boolean handleMessage(Message msg) {
			if (msg.what==0) {
				updateRecordAudioTime();
				return true;
			}
			if (msg.what==1) {
				setPlayerStatus(AUDIO_TYPE_UPDATE);
				return true;
			}
			return false;
		}
	});
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.hole_post_view);
		getActionBar().setTitle("发布匿名树洞");
		mediaRecorder = new MediaRecorder();
		mediaPlayer=new MediaPlayer();
		ViewSetting.setOnClickListener(this, R.id.hole_post_image_button, new View.OnClickListener() {
			public void onClick(View v) {
				selectImage();
			}
		});
		ViewSetting.setOnClickListener(this, R.id.hole_post_audio_button, new View.OnClickListener() {
			public void onClick(View v) {
				recordAudio();
			}
		});
		ViewSetting.setOnClickListener(this, R.id.hole_post_commit, new View.OnClickListener() {
			public void onClick(View v) {
				submit();
			}
		});
		
	}
	
	void selectImage() {
		String[] strings={"拍照","从相册选取"};
		new AlertDialog.Builder(this).setTitle("插入图片").setItems(strings, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which==0) {
					Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					imageUri=Uri.fromFile(
							MyFile.getFile(HolePost.this, "camera", Util.getHash("pkuhole")+".jpg"));
					cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
					startActivityForResult(cameraIntent, 0);
				}
				else if (which==1) {
					Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
					intent.setType("image/*");
					startActivityForResult(intent, 1);
				}
			}
		}).show();
	}
	@SuppressLint("InflateParams")
	void recordAudio() {
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		View view=getLayoutInflater().inflate(R.layout.hole_post_audio_dialog, null, false);
		ViewSetting.setTextView(view, R.id.hole_post_audio_record_time, "已录制时长： 0\"");
		
		View audioView=view.findViewById(R.id.hole_post_audio_record_button);
		length=0;
		audioView.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				
				if (event.getAction()==MotionEvent.ACTION_DOWN) {
					startRecord();
					v.setBackgroundResource(R.drawable.hole_audio_record_pressed);
					return true;
				}
				if (event.getAction()==MotionEvent.ACTION_UP) {
					endRecord();
					v.setBackgroundResource(R.drawable.hole_audio_record_normal);
					v.performClick();
					return true;
				}
				return false;
			}
		});
		
		builder.setTitle("录制音频").setView(view).setCancelable(true).setPositiveButton("确认", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (audioFile!=null)
					setAudio();
			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				audioFile=null;
			}
		}).setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				audioFile=null;
			}
		});
		
		audioDialog=builder.create();
		audioDialog.setCancelable(true);
		audioDialog.setCanceledOnTouchOutside(false);
		audioDialog.show();
	}
	
	void startRecord() {
		
		try {
			audioFile=MyFile.getCache(this, Util.getHash("temp_audio"));
			audioFile.delete();
			
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mediaRecorder.setOutputFile(audioFile.getAbsolutePath());
			mediaRecorder.prepare();
			mediaRecorder.start();
		}
		catch (Exception e) {
			e.printStackTrace();
			CustomToast.showErrorToast(this, "无法录制语音；请检查是否开启了PKU Helper的语音录制权限");
			return;
		}
		
		starttime=System.currentTimeMillis();
		length=-1;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (starttime>0) {
					try {
						handler.sendEmptyMessage(0);
						Thread.sleep(500);
					}
					catch (Exception e) {}
				}
			}
		}).start();
	}
	
	void endRecord() {
		try {
			mediaRecorder.stop();
		}
		catch (Exception e) {}
		if (audioDialog==null
				|| !audioDialog.isShowing()
				|| starttime<0) {
			starttime=-1;
			return;
		}
		length=(int)(System.currentTimeMillis()-starttime)/1000;
		ViewSetting.setTextView(audioDialog, R.id.hole_post_audio_record_time, "已录制时长： "+length+"\"");
		starttime=-1;
	}
	
	void updateRecordAudioTime() {
		if (audioDialog==null
				|| !audioDialog.isShowing()
				|| starttime<0) {
			starttime=-1;
			return;
		}
		long time=(System.currentTimeMillis()-starttime)/1000;
		ViewSetting.setTextView(audioDialog, R.id.hole_post_audio_record_time, "已录制时长： "+time+"\"");
		if (time>120 && length==-1) {
			CustomToast.showErrorToast(this, "超时，录制结束。");
			endRecord();
		}
	}
	
	public void reset() {
		imageUri=null;
		bts=null;
		audioFile=null;
		String content=ViewSetting.getEditTextValue(this, R.id.hole_post_text);
		setContentView(R.layout.hole_post_view);
		ViewSetting.setEditTextValue(this, R.id.hole_post_text, content);
		ViewSetting.setOnClickListener(this, R.id.hole_post_image_button, new View.OnClickListener() {
			public void onClick(View v) {
				selectImage();
			}
		});
		ViewSetting.setOnClickListener(this, R.id.hole_post_audio_button, new View.OnClickListener() {
			public void onClick(View v) {
				recordAudio();
			}
		});
		ViewSetting.setOnClickListener(this, R.id.hole_post_commit, new View.OnClickListener() {
			public void onClick(View v) {
				submit();
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public void submit() {
		setPlayerStatus(AUDIO_TYPE_STOP);
		String content=ViewSetting.getEditTextValue(this, R.id.hole_post_text);
		if (content.length()>1000) {
			CustomToast.showErrorToast(this, "请不要大于1000字！");
			return;
		}
		Pattern pattern=Pattern.compile("(.+)\\1{7}");
		Matcher matcher=pattern.matcher(content);
		if (matcher.find()) {
			CustomToast.showErrorToast(this, "爱惜树洞环境，请勿刷屏灌水！");
			return;
		}
		
		String type="text";
		if (imageUri!=null) {
			if (bts==null)
				bts=MyBitmapFactory.getCompressedBitmapBytes(imageUri.getPath(), 5);
			type="image";
		}
		else if (audioFile!=null) {
			try {
				FileInputStream fileInputStream=new FileInputStream(audioFile);
				bts=new byte[(int)audioFile.length()];
				fileInputStream.read(bts);
				fileInputStream.close();
				type="audio";
			}
			catch (Exception e) {bts=null;}
		}
		
		if ("".equals(content) && bts==null) {
			CustomToast.showErrorToast(this, "没有要发布的内容！");
			return;
		}
		
		ArrayList<Parameters> arrayList=new ArrayList<Parameters>();
		arrayList.add(new Parameters("action", "dopost"));
		arrayList.add(new Parameters("token", Constants.token));
		arrayList.add(new Parameters("type", type));
		arrayList.add(new Parameters("text", content));
		
		if (bts!=null) {
			String string=Base64.encodeToString(bts, Base64.DEFAULT);
			arrayList.add(new Parameters("data", string));
		}
		if ("audio".equals(type))
			arrayList.add(new Parameters("length", length+""));
		
		String url=Constants.domain+"/services/pkuhole/api.php";
		new RequestingTask(this, "正在发布...", url, Constants.REQUEST_HOLE_POST)
		.execute(arrayList);
		
	}
	
	public void setBitmap(final Bitmap bitmap, final String filepath) {
		if (bitmap==null) {
			CustomToast.showErrorToast(this, "添加图片失败，请重试");
			reset();
			return;
		}
		findViewById(R.id.hole_post_extra_layout).setVisibility(View.GONE);
		findViewById(R.id.hole_post_extra_hasitem_layout).setVisibility(View.VISIBLE);
		ViewSetting.setOnClickListener(this, R.id.hole_post_extra_hasitem_delete, new View.OnClickListener() {
			public void onClick(View v) {
				reset();
			}
		});
		ViewSetting.setTextView(this, R.id.hole_post_extra_hasitem_name, "已添加图片");
		
		findViewById(R.id.hole_post_extra_item).setVisibility(View.VISIBLE);
		findViewById(R.id.hole_post_image).setVisibility(View.VISIBLE);
		ViewSetting.setImageBitmap(this, R.id.hole_post_image, bitmap);
		
		ViewSetting.setOnClickListener(this, R.id.hole_post_image, new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(HolePost.this, SubActivity.class);
				intent.putExtra("type", Constants.SUBACTIVITY_TYPE_PICTURE_FILE);
				intent.putExtra("file", filepath);
				startActivity(intent);
			}
		});
		
	}
	
	public void finishRequest(int type, String string) {
		if (type==Constants.REQUEST_HOLE_POST) {
			try {
				JSONObject jsonObject=new JSONObject(string);
				int code=jsonObject.getInt("code");
				if (code!=0) {
					CustomToast.showErrorToast(this, jsonObject.optString("msg", "发布失败"));
					return;
				}
				CustomToast.showSuccessToast(this, "发布成功！");
				setResult(RESULT_OK);
				setPlayerStatus(AUDIO_TYPE_STOP);
				finish();
			}
			catch (Exception e) {
				CustomToast.showErrorToast(this, "发布失败");
			}
		}
	}
	
	public void setAudio() {
		if (audioFile==null) {
			reset();
			return;
		}
		findViewById(R.id.hole_post_extra_layout).setVisibility(View.GONE);
		findViewById(R.id.hole_post_extra_hasitem_layout).setVisibility(View.VISIBLE);
		ViewSetting.setOnClickListener(this, R.id.hole_post_extra_hasitem_delete, new View.OnClickListener() {
			public void onClick(View v) {
				reset();
			}
		});
		ViewSetting.setTextView(this, R.id.hole_post_extra_hasitem_name, "已录制音频");
		
		findViewById(R.id.hole_post_extra_item).setVisibility(View.VISIBLE);
		findViewById(R.id.hole_post_audio_layout).setVisibility(View.VISIBLE);
		
		ViewSetting.setImageResource(this, R.id.hole_post_audio, R.drawable.audio_start);
		ViewSetting.setTextView(this, R.id.hole_post_audio_length, length+"\"");
		
		starttime=-1;
		ViewSetting.setOnClickListener(this, R.id.hole_post_audio, new View.OnClickListener() {
			public void onClick(View v) {
				if (starttime==-1)
					playAudio();
				else
					setPlayerStatus(AUDIO_TYPE_STOP);
			}
		});
		
	}
	
	void playAudio() {
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
							handler.sendEmptyMessage(1);
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
	
	void setPlayerStatus(int statusType) {
		if (statusType==AUDIO_TYPE_START
				|| statusType==AUDIO_TYPE_UPDATE) {
			if (statusType==AUDIO_TYPE_START)
				ViewSetting.setImageResource(this, R.id.hole_post_audio, R.drawable.audio_stop);
			int lefttime=length-(int)(System.currentTimeMillis()-starttime)/1000;
			if (lefttime<=0) lefttime=0;
			ViewSetting.setTextView(this, R.id.hole_post_audio_length, lefttime+"\"");
		}
		else if (statusType==AUDIO_TYPE_STOP) {
			ViewSetting.setImageResource(this, R.id.hole_post_audio, R.drawable.audio_start);
			ViewSetting.setTextView(this, R.id.hole_post_audio_length, length+"\"");
			try {
				mediaPlayer.stop();
				mediaPlayer.reset();
			}
			catch (Exception e) {}
			starttime=-1;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode!=RESULT_OK) return;
		if (requestCode==0) {
			Bitmap bitmap=MyBitmapFactory.getCompressedBitmap(imageUri.getPath(), 5);
			bts=MyBitmapFactory.bitmapToArray(bitmap);
			setBitmap(bitmap, imageUri.getPath());
		}
		else if (requestCode==1) {
			Uri uri=data.getData();
			if (uri==null) return;
			try {
				String[] proj={MediaColumns.DATA};
				CursorLoader loader=new CursorLoader(this, uri, proj, null, null, null);
				Cursor cursor = loader.loadInBackground();
				int index=cursor.getColumnIndexOrThrow(MediaColumns.DATA);
				cursor.moveToFirst();
				String filePath=cursor.getString(index);
				imageUri=Uri.fromFile(new File(filePath));
				
				Bitmap bitmap=MyBitmapFactory.getCompressedBitmap(imageUri.getPath(), 5);
				bts=MyBitmapFactory.bitmapToArray(bitmap);
				setBitmap(bitmap, filePath);	
			}
			catch (Exception e) {
				e.printStackTrace();
				imageUri=null;
				CustomToast.showErrorToast(this, "添加图片失败，请重试");
			}
		}
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(Menu.NONE, Constants.MENU_PKUHOLE_CLOSE, Constants.MENU_PKUHOLE_CLOSE, "")
		.setIcon(R.drawable.close).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == Constants.MENU_PKUHOLE_CLOSE) {
			wantToExit();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void wantToExit() {
		setPlayerStatus(AUDIO_TYPE_STOP);
		super.wantToExit();
	}
	
	@Override
	protected void onPause() {
		setPlayerStatus(AUDIO_TYPE_STOP);
		super.onPause();
	}
	
}
