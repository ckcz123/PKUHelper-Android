package com.pkuhelper.pkuhole;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;

import com.pkuhelper.R;
import com.pkuhelper.lib.BaseActivity;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.MyCalendar;
import com.pkuhelper.lib.MyFile;
import com.pkuhelper.lib.RequestingTask;
import com.pkuhelper.lib.Util;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.subactivity.SubActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class HoleSearch extends BaseActivity {

	ArrayList<HoleInfo> infos = new ArrayList<HoleInfo>();
	private static final int AUDIO_TYPE_START = 0;
	private static final int AUDIO_TYPE_UPDATE = 1;
	private static final int AUDIO_TYPE_STOP = 2;

	private static final int PAGE_SIZE = 100;

	Handler handler;
	MediaPlayer mediaPlayer;
	int playingpid;
	long startplayingtime;

	SearchView searchView;
	AutoCompleteTextView autoComplete;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hole_search);
		getActionBar().setTitle("搜索树洞");


		searchView = (SearchView) findViewById(R.id.hole_search);
		LinearLayout linearLayout1 = (LinearLayout) searchView.getChildAt(0);
		LinearLayout linearLayout2 = (LinearLayout) linearLayout1.getChildAt(2);
		LinearLayout linearLayout3 = (LinearLayout) linearLayout2.getChildAt(1);
		autoComplete = (AutoCompleteTextView) linearLayout3.getChildAt(0);
		autoComplete.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		int padding = Util.sp2px(this, 5);
		autoComplete.setPadding(autoComplete.getPaddingLeft(), padding, autoComplete.getPaddingRight(), padding);
		autoComplete.clearFocus();

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				search();
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				// TODO Auto-generated method stub
				return false;
			}
		});

		ViewSetting.setOnClickListener(this, R.id.hole_search_confirm, new View.OnClickListener() {
			public void onClick(View v) {
				search();
			}
		});

		Spinner spinner = (Spinner) findViewById(R.id.hole_search_spinner);
		spinner.setAdapter(new ArrayAdapter<String>(this, R.layout.hole_search_spinner,
				new String[]{"全部", "图片", "语音"}));
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
									   int position, long id) {
				autoComplete.clearFocus();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

		findViewById(R.id.hole_search_layout).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				View view = findViewById(R.id.hole_search_layout);
				int width = view.getWidth(), height = view.getHeight();
				if (width != 0 && height != 0) {
					ViewSetting.setBackground(HoleSearch.this, view,
							R.drawable.chat_bg);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					} else {
						view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					}
				}
			}
		});

		handler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				if (msg.what == Constants.MESSAGE_HOLE_FILE_DOWNLOAD_FINISHED) {
					update();
					return true;
				}
				if (msg.what == Constants.MESSAGE_HOLE_AUDIO_TIME_UPDATE) {
					setPlayerStatus(AUDIO_TYPE_UPDATE);
					return true;
				}
				return false;
			}
		});

		mediaPlayer = new MediaPlayer();
		playingpid = -1;
		startplayingtime = 0;

	}

	@SuppressWarnings("unchecked")
	void search() {
		setPlayerStatus(AUDIO_TYPE_STOP);
		String type = "";
		Spinner spinner = (Spinner) findViewById(R.id.hole_search_spinner);
		int pos = spinner.getSelectedItemPosition();
		if (pos == 1) type = "image";
		if (pos == 2) type = "audio";

		ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
		arrayList.add(new Parameters("action", "search"));
		arrayList.add(new Parameters("keywords", new String(searchView.getQuery().toString()).trim()));
		arrayList.add(new Parameters("pagesize", PAGE_SIZE + ""));
		arrayList.add(new Parameters("type", type));

		new RequestingTask(this, "正在搜索", Constants.domain + "/services/pkuhole/api.php",
				Constants.REQUEST_HOLE_SEARCH).execute(arrayList);


	}

	protected void finishRequest(int type, String string) {
		if (type == Constants.REQUEST_HOLE_SEARCH) {
			try {
				JSONObject jsonObject = new JSONObject(string);
				int code = jsonObject.getInt("code");
				if (code != 0) {
					CustomToast.showErrorToast(this, jsonObject.optString("msg", "获取失败"));
					return;
				}

				JSONArray array = jsonObject.getJSONArray("data");
				ArrayList<HoleInfo> arrayList = new ArrayList<HoleInfo>();
				int len = array.length();
				for (int i = 0; i < len; i++) {
					JSONObject object = array.getJSONObject(i);
					HoleInfo holeInfo = new HoleInfo(this, handler, object.getInt("pid"), object.optString("text"),
							object.optLong("timestamp"), object.optString("type"), object.optInt("reply"),
							object.optInt("likenum"), object.optInt("extra"), object.optString("url"));
					arrayList.add(holeInfo);
				}
				if (len == 0)
					CustomToast.showInfoToast(this, "没有结果！");
				if (len >= PAGE_SIZE)
					CustomToast.showInfoToast(this, "只显示前100条！");

				infos = new ArrayList<HoleInfo>(arrayList);
				show();

			} catch (Exception e) {
				e.printStackTrace();
				CustomToast.showErrorToast(this, "树洞获取失败，请重试。");
			}


		}
	}

	void show() {
		setPlayerStatus(AUDIO_TYPE_STOP);
		ListView listView = (ListView) findViewById(R.id.hole_listview);

		listView.setAdapter(new BaseAdapter() {

			@SuppressLint("ViewHolder")
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				HoleInfo holeInfo = infos.get(position);
				convertView = getLayoutInflater().inflate(R.layout.hole_list_item, parent, false);
				ViewSetting.setTextView(convertView, R.id.hole_listitem_pid, "#" + holeInfo.pid);

				if (holeInfo.type != HoleInfo.TYPE_TEXT && "".equals(holeInfo.text))
					convertView.findViewById(R.id.hole_listitem_text).setVisibility(View.GONE);
				else
					ViewSetting.setTextView(convertView, R.id.hole_listitem_text, holeInfo.text);

				if (holeInfo.type == HoleInfo.TYPE_IMAGE) {
					convertView.findViewById(R.id.hole_listitem_image_layout).setVisibility(View.VISIBLE);
					Bitmap bitmap = holeInfo.getBitmap();
					if (bitmap != null) {
						convertView.findViewById(R.id.hole_listitem_image).setVisibility(View.VISIBLE);
						ViewSetting.setImageBitmap(convertView, R.id.hole_listitem_image, bitmap);
						if (bitmap.getHeight() > 1.5 * bitmap.getWidth())
							convertView.findViewById(R.id.hole_listitem_image_too_long).setVisibility(View.VISIBLE);

						final String file = MyFile.getCache(HoleSearch.this, Util.getHash(holeInfo.url)) + "";

						ViewSetting.setOnClickListener(convertView, R.id.hole_listitem_image, new View.OnClickListener() {
							public void onClick(View v) {
								Intent intent = new Intent(HoleSearch.this, SubActivity.class);
								intent.putExtra("type", Constants.SUBACTIVITY_TYPE_PICTURE_FILE);
								intent.putExtra("file", file);
								setPlayerStatus(AUDIO_TYPE_STOP);
								startActivity(intent);
							}
						});
					} else {
						convertView.findViewById(R.id.hole_listitem_image_hint).setVisibility(View.VISIBLE);
					}
				}
				if (holeInfo.type == HoleInfo.TYPE_AUDIO) {
					convertView.findViewById(R.id.hole_listitem_audio_layout).setVisibility(View.VISIBLE);
					File file = holeInfo.getAudio();
					if (file != null) {
						convertView.findViewById(R.id.hole_listitem_audio).setVisibility(View.VISIBLE);
						convertView.findViewById(R.id.hole_listitem_audio_length).setVisibility(View.VISIBLE);

						// isplaying
						if (playingpid == holeInfo.pid) {
							ViewSetting.setImageResource(convertView, R.id.hole_listitem_audio, R.drawable.audio_stop);
							long time = System.currentTimeMillis();
							long deltatime = (time - startplayingtime) / 1000;
							int lefttime = (holeInfo.extra - (int) deltatime);
							if (lefttime < 0) lefttime = 0;
							ViewSetting.setTextView(convertView, R.id.hole_listitem_audio_length, lefttime + "'");
						} else {
							ViewSetting.setImageResource(convertView, R.id.hole_listitem_audio, R.drawable.audio_start);
							ViewSetting.setTextView(convertView, R.id.hole_listitem_audio_length, holeInfo.extra + "\"");
						}

						final int pid = holeInfo.pid;
						ViewSetting.setOnClickListener(convertView, R.id.hole_listitem_audio, new View.OnClickListener() {
							public void onClick(View v) {
								togglePlay(pid);
							}
						});

					} else
						convertView.findViewById(R.id.hole_listitem_audio_hint).setVisibility(View.VISIBLE);

				}

				ViewSetting.setTextView(convertView, R.id.hole_listitem_time, MyCalendar.format(holeInfo.timestamp));

				ViewSetting.setTextView(convertView, R.id.hole_listitem_like, "(" + holeInfo.like + ")");
				ViewSetting.setTextView(convertView, R.id.hole_listitem_reply, "(" + holeInfo.reply + ")");

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
				return infos.size();
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				try {
					String spid = ViewSetting.getTextView(view, R.id.hole_listitem_pid);
					if (spid.startsWith("#")) spid = spid.substring(1);
					int pid = Integer.parseInt(spid);

					Intent intent = new Intent(HoleSearch.this, HoleComment.class);
					intent.putExtra("pid", pid);
					setPlayerStatus(AUDIO_TYPE_STOP);
					startActivityForResult(intent, 1);

				} catch (Exception e) {
				}
			}
		});
		autoComplete.clearFocus();
	}

	void update() {
		try {
			ListView listView = (ListView) findViewById(R.id.hole_listview);
			if (listView == null) return;
			int cnt = listView.getCount();
			for (int i = 0; i < cnt; i++) {
				try {
					View view = listView.getChildAt(i);
					if (view == null) continue;
					String spid = ViewSetting.getTextView(view, R.id.hole_listitem_pid);
					if (spid.startsWith("#")) spid = spid.substring(1);
					final int pid = Integer.parseInt(spid);
					HoleInfo holeInfo = HoleInfo.getHoleInfo(pid);
					if (holeInfo == null) continue;
					if (holeInfo.type == HoleInfo.TYPE_IMAGE) {
						if (view.findViewById(R.id.hole_listitem_image).getVisibility() != View.GONE)
							continue;
						Bitmap bitmap = holeInfo.getBitmap();
						if (bitmap == null) continue;
						view.findViewById(R.id.hole_listitem_image_hint).setVisibility(View.GONE);
						view.findViewById(R.id.hole_listitem_image).setVisibility(View.VISIBLE);
						ViewSetting.setImageBitmap(view, R.id.hole_listitem_image, bitmap);
						if (bitmap.getHeight() > 1.5 * bitmap.getWidth())
							view.findViewById(R.id.hole_listitem_image_too_long).setVisibility(View.VISIBLE);
						final String file = MyFile.getCache(HoleSearch.this, Util.getHash(holeInfo.url)) + "";

						ViewSetting.setOnClickListener(view, R.id.hole_listitem_image, new View.OnClickListener() {
							public void onClick(View v) {
								Intent intent = new Intent(HoleSearch.this, SubActivity.class);
								intent.putExtra("type", Constants.SUBACTIVITY_TYPE_PICTURE_FILE);
								intent.putExtra("file", file);
								setPlayerStatus(AUDIO_TYPE_STOP);
								startActivity(intent);
							}
						});

					}
					if (holeInfo.type == HoleInfo.TYPE_AUDIO) {
						if (view.findViewById(R.id.hole_listitem_audio).getVisibility() != View.GONE)
							continue;
						File file = holeInfo.getAudio();
						if (file == null) continue;
						view.findViewById(R.id.hole_listitem_audio_hint).setVisibility(View.GONE);
						view.findViewById(R.id.hole_listitem_audio).setVisibility(View.VISIBLE);
						view.findViewById(R.id.hole_listitem_audio_length).setVisibility(View.VISIBLE);

						ViewSetting.setImageResource(view, R.id.hole_listitem_audio, R.drawable.audio_start);
						ViewSetting.setTextView(view, R.id.hole_listitem_audio_length, holeInfo.extra + "\"");

						ViewSetting.setOnClickListener(view, R.id.hole_listitem_audio, new View.OnClickListener() {
							public void onClick(View v) {
								togglePlay(pid);
							}
						});

					}
				} catch (Exception ee) {
				}
			}

		} catch (Exception e) {
		}
	}

	public void togglePlay(int pid) {
		HoleInfo holeInfo = HoleInfo.getHoleInfo(pid);
		if (holeInfo == null) return;
		File audioFile = holeInfo.getAudio();
		if (audioFile == null) return;
		boolean another = pid != playingpid;
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
			playingpid = pid;
			startplayingtime = System.currentTimeMillis();
			setPlayerStatus(AUDIO_TYPE_START);
			new Thread(new Runnable() {

				@Override
				public void run() {
					while (playingpid != -1) {
						try {
							handler.sendEmptyMessage(Constants.MESSAGE_HOLE_AUDIO_TIME_UPDATE);
							Thread.sleep(500);
						} catch (Exception e) {
						}
					}
				}
			}).start();

		} catch (Exception e) {
			e.printStackTrace();
			CustomToast.showErrorToast(this, "无法播放，不支持的格式");
		}
	}

	public void setPlayerStatus(int statusType) {
		if (playingpid == -1) return;
		ListView listView = (ListView) findViewById(R.id.hole_listview);
		if (listView == null) return;
		int cnt = listView.getCount();
		for (int i = 0; i < cnt; i++) {
			try {
				View view = listView.getChildAt(i);
				if (view == null) continue;
				String spid = ViewSetting.getTextView(view, R.id.hole_listitem_pid);
				if (spid.startsWith("#")) spid = spid.substring(1);
				final int pid = Integer.parseInt(spid);
				if (pid == playingpid) {
					HoleInfo holeInfo = HoleInfo.getHoleInfo(pid);
					if (holeInfo == null) return;
					if (statusType == AUDIO_TYPE_START
							|| statusType == AUDIO_TYPE_UPDATE) {
						if (statusType == AUDIO_TYPE_START)
							ViewSetting.setImageResource(view, R.id.hole_listitem_audio, R.drawable.audio_stop);
						long time = System.currentTimeMillis();
						int lefttime = holeInfo.extra - (int) (time - startplayingtime) / 1000;
						if (lefttime <= 0) lefttime = 0;
						ViewSetting.setTextView(view, R.id.hole_listitem_audio_length, lefttime + "\"");
					} else if (statusType == AUDIO_TYPE_STOP) {
						ViewSetting.setImageResource(view, R.id.hole_listitem_audio, R.drawable.audio_start);
						ViewSetting.setTextView(view, R.id.hole_listitem_audio_length, holeInfo.extra + "\"");
					}
					break;
				}
			} catch (Exception e) {
			}
		}
		if (statusType == AUDIO_TYPE_STOP) {
			try {
				mediaPlayer.stop();
				mediaPlayer.reset();
			} catch (Exception e) {
			}
			playingpid = -1;
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
