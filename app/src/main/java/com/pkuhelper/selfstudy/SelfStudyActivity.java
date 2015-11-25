package com.pkuhelper.selfstudy;

import java.util.List;
import java.util.Random;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.pkuhelper.R;
import com.pkuhelper.lib.BaseActivity;

public class SelfStudyActivity extends BaseActivity {

	private boolean isStudying = false;
	
	private TextView tvTime;
	private TextView tvHint;
	private ImageView ivEgg;
	private ImageButton imbtStudy;
	
	private int duration = 0;
	private long timeStart = 0;
	private long timeEnd = 0;

	private Thread threadClock = null;
	
	private ServiceRecord serviceRecord;
	
	private boolean isInBackground = false;
	
	private ListView lv = null;
	private List<Record> list;
	private RecordAdapter adapter;
	/*
	@Override
	public void onBackPressed() {
		System.out.println("onBackPressed");
		if(isStudying) {
			isStudying = false;
			insertARecord();
		}
		super.onBackPressed();
	}
	*/
	public void wantToExit() {
		if(isStudying) {
			isStudying = false;
			insertARecord();
		}
		else super.wantToExit();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		System.out.println("onWindowFocusChanged : " + hasFocus);
		if(hasFocus) {
			if(isInBackground) {
				isInBackground = false;
				System.out.println("onResume");
				myHandler.sendEmptyMessage(Constants.Message.GIVE_UP);
			}
		} else {
			if(isStudying) {
				isStudying = false;
				isInBackground = true;
				System.out.println("onPause");
			}
		}
		super.onWindowFocusChanged(hasFocus);
	}
 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
		setContentView(R.layout.selfstudy_main);

		setStatusBarColor(Color.parseColor("#ffe1c1"));

		tvTime = (TextView) findViewById(R.id.tvTime);
		tvHint = (TextView) findViewById(R.id.tvHint);
		ivEgg = (ImageView) findViewById(R.id.ivEgg);
		imbtStudy = (ImageButton) findViewById(R.id.imbtStudy);
		
		tvHint.setText(HintUtil.getString2());
		
		lv = (ListView)findViewById(R.id.lv);
		adapter = new RecordAdapter();
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				go2ActivityRecords(null);
			}
		});
		getRecent3Record();
//		long time = System.currentTimeMillis();
//		serviceRecord = new ServiceRecord(this);
//		serviceRecord.deleteAllData();
//		serviceRecord = new ServiceRecord(this);
//		serviceRecord.insertARecord(new Record(time - 500, time));
//		serviceRecord = new ServiceRecord(this);
//		serviceRecord.insertARecord(new Record(time - 60*1000 - 3600*1000, time - 3600*1000));
//		serviceRecord = new ServiceRecord(this);
//		serviceRecord.insertARecord(new Record(time - 1800*1000 - 2*3600*1000, time - 2*3600*1000));
//		serviceRecord = new ServiceRecord(this);
//		serviceRecord.insertARecord(new Record(time - 3600*1000 - 3*3600*1000, time - 3*3600*1000));
//		serviceRecord = new ServiceRecord(this);
//		serviceRecord.insertARecord(new Record(time - 3660*1000 - 5*3600*1000, time - 5*3600*1000));
//		serviceRecord = new ServiceRecord(this);
//		serviceRecord.insertARecord(new Record(time - 2*3600*1000 - 7*3600*1000, time - 7*3600*1000));
//		serviceRecord = new ServiceRecord(this);
//		serviceRecord.insertARecord(new Record(time - 2*3660*1000 - 10*3600*1000, time - 10*3600*1000));
//		serviceRecord = new ServiceRecord(this);
//		serviceRecord.insertARecord(new Record(time - 3*3660*1000 - 14*3600*1000, time - 14*3600*1000));
	}

	public void getRecent3Record() {
		serviceRecord = new ServiceRecord(this);
		String sql = "select * from records order by timeStart desc limit 0, 3";
		list = serviceRecord.getRecords(sql);
		adapter.notifyDataSetChanged();
	}
	
	public void go2ActivityRecords(View view) {
		if(!isStudying) {
			Intent intent = new Intent(this, ActivityRecords.class);
			this.startActivity(intent);
		}
	}

	public void finishRequest(int type, String string) {}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(isStudying) {
			isStudying = false;
			System.out.println("dispatchGenericMotionEvent");
			while(threadClock != null && threadClock.isAlive()) {
				threadClock.interrupt();
			}
			myHandler.sendEmptyMessage(Constants.Message.GIVE_UP);
			return true;
		}
		return super.dispatchTouchEvent(ev);
	}
	
	Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.Message.REFRESH_DURATION_STATE:
				refreshTime(duration);
				break;
			case Constants.Message.GIVE_UP:
				giveUp();
				break;
			case Constants.Message.CHANGE_HINT:
				changeHint();
				break;
			}
			super.handleMessage(msg);
		}
	};

	public void onClickedImbtStudy(View view) {
		isStudying = !isStudying;
		if (isStudying) {
			tvHint.setText(HintUtil.getString2());
			tvTime.setText("00:00");
			imbtStudy.setImageResource(R.drawable.im1_8);
			ivEgg.setImageResource(R.drawable.im1_2);
			timeStart = System.currentTimeMillis();
			
			threadClock = new Thread(new TicTok());
			threadClock.start();
		} else {
			myHandler.sendEmptyMessage(Constants.Message.GIVE_UP);
		}
	}

	protected void changeHint() {
		tvHint.setText(HintUtil.getString1(duration));
	}

	protected void giveUp() {
		isStudying = false;
		duration = 0;
//		tvTime.setText("00:00");
		tvHint.setText(HintUtil.getString0());
		imbtStudy.setImageResource(R.drawable.im1_7);
		ivEgg.setImageResource(R.drawable.im1_3);
		insertARecord();
	}
	
	
	public void insertARecord() {
		timeEnd = System.currentTimeMillis();
		Record record = new Record();
		record.setTimeStart(timeStart);
		record.setTimeEnd(timeEnd);
		serviceRecord = new ServiceRecord(this);
		serviceRecord.insertARecord(record);
		getRecent3Record();
	}
	
	private String formatTime(long time) {
		int high = (int) (time / 60.0);
		int low = (int) (time % 60);
		return (high < 10 ? "0" + high : high) + ":"
				+ (low < 10 ? "0" + low : low);
	}

	protected void refreshTime(long duration) {
		tvTime.setText(formatTime(duration));
		int high = (int) (duration / 60.0);
//		if (high < Constants.duration1) {
//			ivEgg.setImageResource(R.drawable.im1_2);
//		} else 
		if (Constants.duration1 <= high && high < Constants.duration2) {
			ivEgg.setImageResource(R.drawable.im1_4);
		} else if (Constants.duration2 <= high) {
			isStudying = false;
			while(threadClock != null && threadClock.isAlive()) {
				threadClock.interrupt();
			}
			tvHint.setText(HintUtil.getString3());
			ivEgg.setImageResource(R.drawable.im1_5);
			tvTime.setText("00:00");
			this.duration = 0;
			imbtStudy.setImageResource(R.drawable.im1_7);
			insertARecord();
		}
	}
	
	class TicTok implements Runnable {

		private int time2ChangeHint = 0;
		
		public TicTok() {
			this.time2ChangeHint = new Random().nextInt(10);
		}
		@Override
		public void run() {
			while (isStudying) {
				try {
					Thread.sleep(Constants.span);
					duration++;
					if(duration >= time2ChangeHint) {
						time2ChangeHint += new Random().nextInt(10);
						myHandler.sendEmptyMessage(Constants.Message.CHANGE_HINT);
					}
					myHandler.sendEmptyMessage(Constants.Message.REFRESH_DURATION_STATE);
					int high = (int) (duration / 60.0);
					if (Constants.duration2 <= high) {
						isStudying = false;
						break;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private class RecordAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder;
			if(convertView == null) {
				convertView = LayoutInflater.from(SelfStudyActivity.this).inflate(R.layout.selfstudy_item_recent, parent, false);
	            holder = new Holder();
	            holder.rlItem = (RelativeLayout)convertView.findViewById(R.id.rlItem);
	            holder.tvHigh = (TextView)convertView.findViewById(R.id.tvHigh);
	            holder.tvSemicolon = (TextView)convertView.findViewById(R.id.tvSemicolon);
	            holder.tvLow = (TextView)convertView.findViewById(R.id.tvLow);
	            convertView.setTag(holder);
			} else {
				holder = (Holder)convertView.getTag();
			}
			if(position < list.size()){
				Record record = FormatRecord.getFormattedData(list.get(position));
				holder.rlItem.setBackgroundResource(0);
				String duration[] = list.get(position).getDurationInHHmm().split(":");
				holder.tvHigh.setText(duration[0]);
				holder.tvSemicolon.setText(":");
				holder.tvLow.setText(duration[1]);
			} else {
				holder.rlItem.setBackgroundResource(R.drawable.shape_round_rect_recent);
				holder.tvHigh.setText("");
				holder.tvSemicolon.setText("");
				holder.tvLow.setText("");
			}
			return convertView;
		}
	}
	
	private static class Holder {
		RelativeLayout rlItem;
		TextView tvHigh;
		TextView tvSemicolon;
		TextView tvLow;
	}
}
