package com.pkuhelper.grade;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class GradeActivity extends BaseActivity {

	public static GradeActivity gradeActivity;

	static String phpsessid = "";
	ArrayList<HashMap<String, String>> listItems = new ArrayList<HashMap<String, String>>();
	ArrayList<Semester> semesters = new ArrayList<Semester>();
	ArrayList<Semester> dualSemesters = new ArrayList<Semester>();

	String totalWeight, avggpa;
	String dualTotalWeight, dualavggpa;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		gradeActivity = this;
		setContentView(R.layout.grade_listview_layout);
		getActionBar().setTitle("成绩查询");
		Intent intent = getIntent();
		String string = intent.getExtras().getString("phpsessid");
		if (!(string == null || "".equals(string))) {
			phpsessid = string;
		}
		if (phpsessid == null || "".equals(phpsessid)) {
			CustomToast.showInfoToast(this, "请重新输入验证码查看成绩。");
			wantToExit();
		}
		getGrades();
	}

	@SuppressWarnings("unchecked")
	void getGrades() {
		new RequestingTask(this, "正在获取成绩...",
				Constants.domain + "/services/pkuhelper/allGrade.php?phpsessid=" + phpsessid,
				Constants.REQUEST_DEAN_GETTING_GRADE).execute(new ArrayList<Parameters>());
	}

	public void finishRequest(int type, String string) {
		try {
			JSONObject jsonObject = new JSONObject(string);
			Log.w("grade", string);
			int code = jsonObject.getInt("code");
			if (code != 0) {
				CustomToast.showErrorToast(this, jsonObject.optString("msg", "成绩解析失败"));
				return;
			}

			totalWeight = jsonObject.optString("total", "unknown");
			avggpa = jsonObject.optString("avggpa", "unknown");
			semesters = setGrade(jsonObject.getJSONArray("gpas"), jsonObject.getJSONArray("courses"));

			dualTotalWeight = jsonObject.optString("dualtotal", "unknown");
			dualavggpa = jsonObject.optString("dualavggpa", "unknown");
			dualSemesters = setGrade(jsonObject.getJSONArray("dualgpas"),
					jsonObject.getJSONArray("dualcourses"));

			setStrings();
		} catch (JSONException e) {
			CustomToast.showErrorToast(this, "成绩解析失败，请重试");
			e.printStackTrace();
		}
	}

	private ArrayList<Semester> setGrade(JSONArray gpas, JSONArray courses) {
		try {
			ArrayList<Semester> arrayList = new ArrayList<Semester>();
			int len = gpas.length();
			for (int i = 0; i < len; i++) {
				JSONObject termObject = gpas.optJSONObject(i);
				String year = termObject.getString("year");
				String term = termObject.getString("term");
				String gpa = termObject.getString("gpa");
				Semester semester = new Semester(year, term, gpa, false);
				arrayList.add(semester);
			}

			len = courses.length();
			for (int i = 0; i < len; i++) {
				JSONObject courseObject = courses.getJSONObject(i);
				String name = courseObject.getString("name");
				String fullName = courseObject.getString("fullName");
				String year = courseObject.getString("year");
				String term = courseObject.getString("term");
				String weight = courseObject.getString("weight");
				String grade = courseObject.getString("grade");
				String delta = courseObject.getString("delta");
				String gpa = courseObject.getString("gpa");
				String accurate = courseObject.getString("accurate");
				String type = courseObject.getString("type");

				Iterator<Semester> iterator = arrayList.iterator();
				while (iterator.hasNext()) {
					Semester semester = iterator.next();
					if (semester.isThisSemester(year, term))
						semester.addCourse(name, fullName,
								type, weight, grade, delta, accurate, gpa);
				}
			}

			Collections.sort(arrayList, new Comparator<Semester>() {
				@Override
				public int compare(Semester s1, Semester s2) {
					int x = s1.year.compareTo(s2.year);
					int y = s1.term.compareTo(s2.term);
					int ret = 1;
					if (x < 0) ret = -1;
					else if (x == 0 && y < 0) ret = -1;
					return ret;
				}
			});

			return arrayList;
		} catch (Exception e) {
			return new ArrayList<Semester>();
		}
	}

	private void setStrings() {
		listItems = new ArrayList<HashMap<String, String>>();
		Iterator<Semester> iterator = semesters.iterator();
		while (iterator.hasNext()) {
			Semester semester = iterator.next();
			semester.calWeight();
			String year = semester.year, term = semester.term;
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("type", "text");
			map.put("text", year + "年度，第" + term + "学期");
			listItems.add(map);
			Iterator<Course> iterator2 = semester.courses.iterator();
			while (iterator2.hasNext()) {
				Course course = iterator2.next();
				HashMap<String, String> hashMap = new HashMap<String, String>();
				hashMap.put("type", "course");
				hashMap.put("name", course.name);
				hashMap.put("fullname", course.fullname);
				hashMap.put("isDual", "0");
				hashMap.put("courseType", course.type);
				hashMap.put("year", year);
				hashMap.put("term", term);
				hashMap.put("weight", course.weight);
				hashMap.put("grade", course.grade);
				hashMap.put("gpa", course.gpa);
				hashMap.put("accurate", course.accurate);
				hashMap.put("delta", course.delta);
				listItems.add(hashMap);
			}
			HashMap<String, String> avgTextMap = new HashMap<String, String>();
			avgTextMap.put("type", "text");
			avgTextMap.put("text", "当前学期学分数： " + semester.weight + "，平均绩点： " + semester.gpa);
			listItems.add(avgTextMap);
			HashMap<String, String> emptyTextMap = new HashMap<String, String>();
			emptyTextMap.put("text", "");
			listItems.add(emptyTextMap);
		}

		if (listItems.size() != 0) {
			listItems.remove(listItems.size() - 1);
			HashMap<String, String> total = new HashMap<String, String>();
			total.put("type", "text");
			total.put("text", "你已修完学分数： " + totalWeight + "，平均绩点： " + avggpa);
			listItems.add(total);
		}

		if (dualSemesters.size() != 0) {
			HashMap<String, String> emptyTextMap = new HashMap<String, String>();
			emptyTextMap.put("text", "");
			listItems.add(emptyTextMap);

			HashMap<String, String> dualHintmap = new HashMap<String, String>();
			dualHintmap.put("text", "------------以下为辅/双成绩------------");
			listItems.add(dualHintmap);

			HashMap<String, String> memptyTextMap = new HashMap<String, String>();
			memptyTextMap.put("text", "");
			listItems.add(memptyTextMap);


			iterator = dualSemesters.iterator();
			while (iterator.hasNext()) {
				Semester semester = iterator.next();
				semester.calWeight();
				String year = semester.year, term = semester.term;
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("type", "text");
				map.put("text", year + "年度，第" + term + "学期");
				listItems.add(map);
				Iterator<Course> iterator2 = semester.courses.iterator();
				while (iterator2.hasNext()) {
					Course course = iterator2.next();
					HashMap<String, String> hashMap = new HashMap<String, String>();
					hashMap.put("type", "course");
					hashMap.put("name", course.name);
					hashMap.put("isDual", "1");
					hashMap.put("fullname", course.fullname);
					hashMap.put("courseType", course.type);
					hashMap.put("year", year);
					hashMap.put("term", term);
					hashMap.put("weight", course.weight);
					hashMap.put("grade", course.grade);
					hashMap.put("gpa", course.gpa);
					hashMap.put("accurate", course.accurate);
					hashMap.put("delta", course.delta);

					listItems.add(hashMap);
				}
				HashMap<String, String> avgTextMap = new HashMap<String, String>();
				avgTextMap.put("type", "text");
				avgTextMap.put("text", "当前学期辅/双学分数： " + semester.weight + "，平均绩点： " + semester.gpa);
				listItems.add(avgTextMap);
				HashMap<String, String> emptyTextMap3 = new HashMap<String, String>();
				emptyTextMap3.put("type", "text");
				emptyTextMap3.put("text", "");
				listItems.add(emptyTextMap3);
			}
			listItems.remove(listItems.size() - 1);
			HashMap<String, String> total2 = new HashMap<String, String>();
			total2.put("type", "text");
			total2.put("text", "你已修完辅/双学分数： " + dualTotalWeight + "，平均绩点： " + dualavggpa);
			listItems.add(total2);
		}

		showGrade();
	}


	public void showGrade() {
		ListView listView = (ListView) findViewById(R.id.grade_listview_layout);
		if (listView == null) return;
		//listView.setAdapter(new GradeAdapter(listItems));
		listView.setAdapter(new BaseAdapter() {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				HashMap<String, String> hashMap = listItems.get(position);
				LayoutInflater mInflater = GradeActivity.this.getLayoutInflater();
				if ("course".equals(hashMap.get("type"))) {
					convertView = mInflater.inflate(R.layout.grade_item_view, parent, false);
					String name = hashMap.get("name");
					if (name.length() >= 15)
						name = name.substring(0, 13) + "...";
					ViewSetting.setTextView(convertView, R.id.grade_course_name, name);
					ViewSetting.setTextView(convertView, R.id.grade_course_score, hashMap.get("grade"));
				} else {
					convertView = mInflater.inflate(R.layout.grade_text_view, parent, false);
					ViewSetting.setTextView(convertView, R.id.grade_text, hashMap.get("text"));
				}
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
				// TODO Auto-generated method stub
				return listItems.size();
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				HashMap<String, String> hashMap = listItems.get(position);
				String type = hashMap.get("type");
				if (!"course".equals(type)) return;
				List<HashMap<String, String>> maps = getList(hashMap);

				Dialog dialog = new Dialog(GradeActivity.this);
				dialog.setTitle("课程详细信息");
				dialog.setContentView(R.layout.grade_detail_view);
				ListView listView = (ListView) dialog.findViewById(R.id.grade_detail);
				if (listView == null) return;
				listView.setAdapter(new SimpleAdapter(GradeActivity.this, maps, R.layout.grade_detail_item,
						new String[]{"name", "value"}, new int[]{R.id.grade_detail_name, R.id.grade_detail_value}));
				dialog.setCancelable(true);
				dialog.setCanceledOnTouchOutside(true);
				dialog.show();
			}
		});
	}

	private List<HashMap<String, String>> getList(HashMap<String, String> hashMap) {
		List<HashMap<String, String>> maps = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> mp = new HashMap<String, String>();
		mp.put("name", "课程名称");
		String name = hashMap.get("name");
		if (name.length() >= 15) name = name.substring(0, 13) + "...";
		mp.put("value", name);
		maps.add(mp);
		mp = new HashMap<String, String>();
		mp.put("name", "课程类别");
		mp.put("value", hashMap.get("courseType"));
		maps.add(mp);

		mp = new HashMap<String, String>();
		mp.put("name", "成绩");
		mp.put("value", hashMap.get("grade"));
		maps.add(mp);

		mp = new HashMap<String, String>();
		mp.put("name", "学分");
		mp.put("value", hashMap.get("weight"));
		maps.add(mp);

		mp = new HashMap<String, String>();
		mp.put("name", "绩点");
		mp.put("value", hashMap.get("gpa"));
		maps.add(mp);

		if ("0".equals(hashMap.get("accurate"))) {
			mp = new HashMap<String, String>();
			mp.put("name", "备注");
			mp.put("value", "请及时参加评估");
		}

		return maps;
	}
}
