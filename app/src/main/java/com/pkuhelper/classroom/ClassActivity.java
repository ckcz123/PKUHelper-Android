package com.pkuhelper.classroom;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.pkuhelper.R;
import com.pkuhelper.lib.BaseActivity;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.RequestingTask;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ClassActivity extends BaseActivity {

	ViewPager mViewPager;
	static ClassActivity classActivity;
	ArrayList<String> buildings = new ArrayList<String>();
	ArrayList<String> links = new ArrayList<String>();
	public String[] htmls = new String[3];
	String html_header = "<html><head><style type='text/css'>table{width: 100%%;"
			+ "border-collapse: collapse;border: solid 2px #555555;}"
			+ "body{background-color:#EEEEEE;}td,th{border: solid 2px #888888;}"
			+ "td.yes{background-color:#AAAAAA}</style>"
			+ "</head><body><p align='center'>";
	String html_header2 = "教室占用情况（";
	String html_header3 = "天）<br><font size='2'>深色已占浅色未占</font>"
			+ "</p><table><tr><th>教室号</th>";
	String requestBuilding = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.classroom_layout);
		classActivity = this;
		htmls = new String[3];
		htmls[0] = htmls[1] = htmls[2] = null;
		getActionBar().setTitle("教室查询");

		mViewPager = (ViewPager) findViewById(R.id.classroom_pager);
		mViewPager.setAdapter(new FragmentPagerAdapter(getFragmentManager()) {

			@Override
			public int getCount() {
				return 3;
			}

			@Override
			public Fragment getItem(int position) {
				Bundle bundle = new Bundle();
				bundle.putInt("index", position);
				return Fragment.instantiate(ClassActivity.this, "com.pkuhelper.classroom.ClassroomFragment", bundle);
			}

			@Override
			public int getItemPosition(Object object) {
				return POSITION_NONE;
			}
		});

		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						if (position == 0) selectToday(null);
						else if (position == 1) selectTomorrow(null);
						else if (position == 2) selectAfterTomorrow(null);
					}
				});

		selectToday(null);

	}

	public void resetAllTab() {
		ViewSetting.setTextViewColor(this, R.id.classroom_today, Color.BLACK);
		ViewSetting.setTextViewColor(this, R.id.classroom_tomorrow, Color.BLACK);
		ViewSetting.setTextViewColor(this, R.id.classroom_aftertomorrow, Color.BLACK);
	}

	public void selectToday(View view) {
		mViewPager.setCurrentItem(0);
		resetAllTab();
		ViewSetting.setTextViewColor(this, R.id.classroom_today, Color.parseColor("#2d90dc"));
	}

	public void selectTomorrow(View view) {
		mViewPager.setCurrentItem(1);
		resetAllTab();
		ViewSetting.setTextViewColor(this, R.id.classroom_tomorrow, Color.parseColor("#2d90dc"));
	}

	public void selectAfterTomorrow(View view) {
		mViewPager.setCurrentItem(2);
		resetAllTab();
		ViewSetting.setTextViewColor(this, R.id.classroom_aftertomorrow, Color.parseColor("#2d90dc"));
	}

	@SuppressWarnings("unchecked")
	public void showSelectDialog() {
		requestBuilding = "";
		if (buildings.size() == 0) {
			new RequestingTask(this, "正在获取教学楼列表...",
					Constants.domain + "/services/pkuhelper/classroom.php",
					Constants.REQUEST_CLASSROOM_LIST).execute(new ArrayList<Parameters>());
			return;
		}
		final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		String[] strings = new String[buildings.size()];
		for (int i = 0; i < buildings.size(); i++) strings[i] = buildings.get(i);
		dialog.setItems(strings, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				getClassroowState(links.get(which), buildings.get(which));
				dialog.dismiss();
			}
		}).setCancelable(true).setTitle("请选择教学楼").show();
	}

	@SuppressWarnings("unchecked")
	public void getClassroowState(String url, String building) {
		new RequestingTask(this, "正在获取教室状态", url, Constants.REQUEST_CLASSROOM)
				.execute(new ArrayList<Parameters>());
		requestBuilding = building;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(Menu.NONE, Constants.MENU_CLASSROOM_SELECT, Constants.MENU_CLASSROOM_SELECT, "")
				.setIcon(R.drawable.item).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	public void setClassroomList(String string) {
		try {
			buildings.clear();
			links.clear();
			JSONArray jsonArray = new JSONArray(string);
			int size = jsonArray.length();
			for (int i = 0; i < size; i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String name = jsonObject.getString("name");
				String link = jsonObject.getString("link");
				buildings.add(name);
				links.add(link);
			}
			showSelectDialog();
		} catch (JSONException e) {
			buildings.clear();
			links.clear();
			CustomToast.showErrorToast(this, "教室列表获取失败，请重试");
		}
	}

	public void setClassroomState(String string) {
		try {
			JSONObject jsonObject = new JSONObject(string);
			String state = jsonObject.optString("success");
			if (!"1".equals(state)) throw new JSONException("stateError");
			JSONObject data = jsonObject.optJSONObject("data");
			if (data == null) {
				setHtml(null, 0);
				setHtml(null, 1);
				setHtml(null, 2);
			} else {
				setHtml(data.optJSONArray("today"), 0);
				setHtml(data.optJSONArray("tomorrow"), 1);
				setHtml(data.optJSONArray("aftertomorrow"), 2);
			}
			FragmentPagerAdapter fragmentPagerAdapter = (FragmentPagerAdapter) mViewPager.getAdapter();
			fragmentPagerAdapter.notifyDataSetChanged();

		} catch (Exception e) {
			CustomToast.showErrorToast(this, "教室状态获取失败，请重试");
		}
	}

	public void setHtml(JSONArray jsonArray, int index) throws Exception {
		String _html0 = html_header + requestBuilding + html_header2;
		if (index == 0) _html0 = _html0 + "今";
		else if (index == 1) _html0 = _html0 + "明";
		else if (index == 2) _html0 = _html0 + "后";
		_html0 = _html0 + html_header3;
		for (int i = 1; i <= 12; i++) {
			String j = i + "";
			if (i < 10) j = "0" + i;
			_html0 += "<th>" + j + "</th>";
		}
		_html0 += "</tr>";

		if (jsonArray == null || jsonArray.length() == 0) {
			_html0 += "</table><center><p style='margin-top:40px'>"
					+ "暂时没有数据</p></center></body></html>";
			htmls[index] = _html0;
			return;
		}

		int len = jsonArray.length();

		for (int i = 0; i < len; i++) {
			JSONArray array = jsonArray.getJSONArray(i);
			String name = array.getString(0);
			_html0 += "<tr><td>" + name + "</td>";

			for (int j = 0; j < 12; j++) {
				String string2 = array.getString(j + 1);
				if ("1".equals(string2))
					_html0 += "<td class='yes'></td>";
				else _html0 += "<td class='no'></td>";
			}

			_html0 += "</tr>";
		}
		_html0 += "</table></body></html>";

		htmls[index] = _html0;
	}

	public void finishRequest(int type, String string) {
		if (type == Constants.REQUEST_CLASSROOM_LIST)
			setClassroomList(string);
		else if (type == Constants.REQUEST_CLASSROOM)
			setClassroomState(string);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == Constants.MENU_CLASSROOM_SELECT) {
			showSelectDialog();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
