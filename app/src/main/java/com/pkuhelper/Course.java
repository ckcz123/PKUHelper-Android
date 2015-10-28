package com.pkuhelper;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;

import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.Editor;
import com.pkuhelper.lib.Lib;
import com.pkuhelper.lib.MyCalendar;
import com.pkuhelper.lib.MyFile;
import com.pkuhelper.lib.RequestingTask;
import com.pkuhelper.lib.Util;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.widget.WidgetCourse2Provider;
import com.pkuhelper.widget.WidgetCourseProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class Course extends Fragment {
	static WebView courseView;
	static String html;
	static Drawable drawable;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.course_view,
				container, false);
		courseView = (WebView) rootView.findViewById(R.id.course_view);
		courseView.getSettings().setJavaScriptEnabled(false);
		courseView.setVerticalScrollBarEnabled(false);
		courseView.setBackgroundColor(Color.TRANSPARENT);
		//courseView.setBackgroundDrawable(PKUHelper.pkuhelper.getResources().getDrawable(R.drawable.mypku_bg));
		initBackgroundFromFile();
//		try {
//			courseView.setBackgroundResource(R.drawable.mypku_bg);
//		}
//		catch (Exception | OutOfMemoryError e) {}
		try {
			html = MyFile.getString(PKUHelper.pkuhelper, Constants.username, "course", CourseString.defaultHtml);
			if ("".equals(html)) html = CourseString.defaultHtml;
		} catch (Exception e) {
			html = CourseString.defaultHtml;
		}
		if (!MyFile.getFile(PKUHelper.pkuhelper, Constants.username, "deancourse").exists())
			html=CourseString.defaultHtml;
		showView();
		return rootView;
	}

	@Override
	public void onViewCreated(final View view, Bundle savedInstanceState) {
		final ViewTreeObserver observer = view.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				int width = view.getWidth(), height = view.getHeight();
				if (width != 0 && height != 0) {
					ViewSetting.setBackground(getActivity(), view, drawable);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						observer.removeOnGlobalLayoutListener(this);
					} else {
						observer.removeGlobalOnLayoutListener(this);
					}
				}
			}
		});
		super.onViewCreated(view, savedInstanceState);
	}

	public static void showView() {
		try {
			MyFile.putString(PKUHelper.pkuhelper, Constants.username, "course", html);
		} catch (Exception e) {
			e.printStackTrace();
		}
		courseView.loadDataWithBaseURL(null, showHtml(html), "text/html", "utf-8", null);
	}

	@SuppressWarnings("unchecked")
	public static void gettingCourse() {
		if (!Constants.isLogin()) {
			IAAA.showLoginView();
			return;
		}

		if (!Editor.getBoolean(PKUHelper.pkuhelper, "course_elective", true)) {
			html = decodeHtml(CourseString.defaultCouseHtml);
			showView();
			Dean.getSessionId(Dean.FLAG_GETTING_COURSE);
			return;
		}

		ArrayList<Parameters> parameters = new ArrayList<>();
		parameters.add(new Parameters("appid", "syllabus"));
		parameters.add(new Parameters("userName", Constants.username));
		parameters.add(new Parameters("password", Constants.password));
		parameters.add(new Parameters("randCode", "0"));
		parameters.add(new Parameters("redirUrl",
				"http://elective.pku.edu.cn:80/elective2008/agent4Iaaa.jsp/../ssoLogin.do"));
		RequestingTask requestingTask = new RequestingTask(PKUHelper.pkuhelper, "正在连接...",
				"https://iaaa.pku.edu.cn/iaaa/oauthlogin.do", Constants.REQUEST_ELECTIVE_TOKEN);
		requestingTask.execute(parameters);
	}

	@SuppressWarnings("unchecked")
	private static void gettingCookie(String token) {
		RequestingTask requestingTask = new RequestingTask(PKUHelper.pkuhelper, "正在验证...",
				"http://elective.pku.edu.cn/elective2008/ssoLogin.do?token=" + token,
				Constants.REQUEST_ELECTIVE_COOKIE);
		requestingTask.execute(new ArrayList<Parameters>());
	}

	@SuppressWarnings("unchecked")
	private static void connectCourse() {
		RequestingTask requestingTask = new RequestingTask(PKUHelper.pkuhelper, "正在获取课表...",
				"http://elective.pku.edu.cn/elective2008/edu/pku/stu/elective/controller/electiveWork/showResults.do",
				Constants.REQUEST_ELECTIVE);
		requestingTask.execute(new ArrayList<Parameters>());
	}

	public static void finishConnection(int type, String string) {
		if (type == Constants.REQUEST_ELECTIVE_TOKEN) {
			try {
				JSONObject jsonObject = new JSONObject(string);
				boolean success = jsonObject.optBoolean("success");
				String token = jsonObject.optString("token");
				if (success) {
					gettingCookie(token);
					return;
				} else {
					CustomToast.showErrorToast(PKUHelper.pkuhelper, "您的用户名或密码有误，请重新登录");
					Constants.reset(PKUHelper.pkuhelper);
					IAAA.showLoginView();
					return;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else if (type == Constants.REQUEST_ELECTIVE_COOKIE) {
			connectCourse();
		} else if (type == Constants.REQUEST_ELECTIVE) {
			html = decodeHtml(string);
			showView();
			try {
				MyFile.putString(PKUHelper.pkuhelper, Constants.username, "deancourse", html);
			}
			catch (Exception e) {}
			Dean.getSessionId(Dean.FLAG_GETTING_COURSE);
		}
	}

	@SuppressWarnings("unchecked")
	public static void getCourses() {
		ArrayList<Parameters> arrayList = new ArrayList<>();
		arrayList.add(new Parameters("token", Constants.token));
		arrayList.add(new Parameters("phpsessid", Constants.phpsessid));
		new RequestingTask(PKUHelper.pkuhelper, "正在获取课表..",
				Constants.domain + "/services/pkuhelper/course.php",
				Constants.REQUEST_ELECTIVE_COURSES).execute(arrayList);
	}

	public static void finishGetCourses(String string) {
		try {
			JSONObject jsonObject = new JSONObject(string);
			int code = jsonObject.getInt("code");
			if (code != 0) {
				CustomToast.showErrorToast(PKUHelper.pkuhelper, jsonObject.optString("msg", "课表解析失败"));
				return;
			}
			JSONArray jsonArray = jsonObject.optJSONArray("courses");
			ArrayList<CourseInfo> deanCourseInfos = new ArrayList<>();
			ArrayList<CourseInfo> customCourseInfos = new ArrayList<>();
			ArrayList<CourseInfo> dualCourseInfos = new ArrayList<>();
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
				JSONObject object = jsonArray.optJSONObject(i);
				String name = object.optString("courseName");
				String location = object.optString("location").trim();
				CourseInfo courseInfo = new CourseInfo(name, location);
				JSONArray times = object.optJSONArray("times");
				String course_type = object.getString("type");
				int l = times.length();
				for (int j = 0; j < l; j++) {
					JSONObject time = times.optJSONObject(j);
					int type;
					String typeString = time.optString("week", "all");
					typeString = typeString.trim();
					if ("odd".equals(typeString)) type = Constants.COURSE_TYPE_ODD;
					else if ("even".equals(typeString)) type = Constants.COURSE_TYPE_EVEN;
					else type = Constants.COURSE_TYPE_EVERY;
					String day = time.optString("day").trim(), num = time.optString("num").trim();
					if ("".equals(day) || "".equals(num)) continue;
					courseInfo.addTime(day, num, type);
					if ("".equals(location) || location.contains("备注")) {
						courseInfo.where = "？";
					//	hasQuestion = true;
					}

				}
				if (course_type.equals("main")) {
					deanCourseInfos.add(courseInfo);
				} else if (course_type.equals("dual")) dualCourseInfos.add(courseInfo);
				else if (course_type.equals("custom")) customCourseInfos.add(courseInfo);
			}
			if (Editor.getBoolean(PKUHelper.pkuhelper, "course_dean", true))
				addCourse(deanCourseInfos, true);
			if (Editor.getBoolean(PKUHelper.pkuhelper, "course_dual", true))
				addCourse(dualCourseInfos, false);
			MyFile.putString(PKUHelper.pkuhelper, Constants.username, "deancourse", html);
			if (Editor.getBoolean(PKUHelper.pkuhelper, "course_custom", true))
				addCourse(customCourseInfos, true);
		} catch (Exception e) {
			CustomToast.showErrorToast(PKUHelper.pkuhelper, "课表解析失败");
		} finally {
			// 更新桌面小部件
			try {
				Lib.sendBroadcast(PKUHelper.pkuhelper, WidgetCourseProvider.class, Constants.ACTION_REFRESH_COURSE);
				Lib.sendBroadcast(PKUHelper.pkuhelper, WidgetCourse2Provider.class, Constants.ACTION_REFRESH_COURSE);
			} catch (Exception e) {
			}
			showView();
		}
	}

	/**
	 * 只获取自选课表
	 */
	@SuppressWarnings("unchecked")
	public static void getCustom() {
		try {
			html = MyFile.getString(PKUHelper.pkuhelper, Constants.username, "deancourse", CourseString.defaultCouseHtml);
		} catch (Exception e) {
			html = CourseString.defaultCouseHtml;
		}
		ArrayList<Parameters> arrayList = new ArrayList<>();
		arrayList.add(new Parameters("operation", "get"));
		arrayList.add(new Parameters("token", Constants.token));
		new RequestingTask(PKUHelper.pkuhelper, "正在获取自选课表..",
				Constants.domain + "/services/course.php", Constants.REQUEST_ELECTIVE_CUSTOM)
				.execute(arrayList);
	}

	public static void finishGetCustom(String string) {
		try {
			JSONObject jsonObject = new JSONObject(string);
			int hasDual = jsonObject.getInt("hasCustom");
			if (hasDual != 1)
				return;
			addCourse(jsonObject.optJSONArray("courses"), true);
		} catch (Exception e) {
			CustomToast.showErrorToast(PKUHelper.pkuhelper, "自选课表解析失败");
		} finally {
			try {

				Lib.sendBroadcast(PKUHelper.pkuhelper, WidgetCourseProvider.class, Constants.ACTION_REFRESH_COURSE);
				Lib.sendBroadcast(PKUHelper.pkuhelper, WidgetCourse2Provider.class, Constants.ACTION_REFRESH_COURSE);
			} catch (Exception e) {
			}
			showView();
		}
	}

	/**
	 * 将arrayList里面课程添加到html中
	 *
	 * @param arrayList
	 * @param override  是否覆盖原课表
	 */
	public static void addCourse(ArrayList<CourseInfo> arrayList, boolean override) {
		int len = arrayList.size();
		for (int i = 0; i < len; i++)
			html = arrayList.get(i).addToHtml(html, override);
	}

	/**
	 * 将jsonArray含的信息加到html中
	 *
	 * @param jsonArray
	 * @param override  是否覆盖原课表
	 * @throws Exception
	 */
	public static void addCourse(JSONArray jsonArray, boolean override) throws Exception {
		int len = jsonArray.length();
		for (int i = 0; i < len; i++) {
			JSONObject jsonObject = jsonArray.optJSONObject(i);
			String name = jsonObject.optString("courseName");
			String location = jsonObject.optString("location").trim();
			if ("".equals(location)
					|| location.contains("备注")) {
				location = "？";
				//hasQuestion = true;
			}
			CourseInfo courseInfo = new CourseInfo(name, location);
			JSONArray times = jsonObject.optJSONArray("times");
			int l = times.length();
			for (int j = 0; j < l; j++) {
				JSONObject time = times.optJSONObject(j);
				int type;
				String typeString = time.optString("week", "all");
				typeString = typeString.trim();
				if ("odd".equals(typeString)) type = Constants.COURSE_TYPE_ODD;
				else if ("even".equals(typeString)) type = Constants.COURSE_TYPE_EVEN;
				else type = Constants.COURSE_TYPE_EVERY;
				courseInfo.addTime(time.optString("day"), time.optString("num"), type);
			}
			html = courseInfo.addToHtml(html, override);
		}
	}

	/**
	 * 解析html，取出table；不存在table则放默认的courseHtml
	 *
	 * @param html
	 * @return
	 */
	private static String decodeHtml(String html) {
		Document document = Jsoup.parse(html);
		Element table = document.getElementById("classAssignment");
		String tableString = "";
		if (table != null) tableString = table.toString();
		else tableString = CourseString.defaultCouseHtml;
		html = "<html><head><meta charset='utf-8'><style>" + CourseString.cssString
				+ "</style></head><body>" + tableString
				+ "</body></html>";
		return dealWithHtml(html);
	}

	/**
	 * 对html进行处理：
	 * <ol><li>星期几只留下一个字</li>
	 * <li>节数加上上课时间和结束时间的提示；</li>
	 * <li>每节课改为三行，名称，(地点)，每周/单周/双周；无法解析的地点标为"？"</li></ol>
	 *
	 * @param html
	 * @return
	 */
	private static String dealWithHtml(String html) {
		String dehtml = new String(html);
		try {
			Document document = Jsoup.parse(dehtml);
			Element table = document.getElementById("classAssignment");

			Elements trs = table.getElementsByTag("tr");
			Elements ths = trs.get(0).getElementsByTag("th");
			ths.get(0).text("");
			for (int j = 1; j <= 7; j++) {
				Element th = ths.get(j);
				String string = th.text();
				string = string.substring(2);
				th.text(string);
			}

			for (int i = 1; i <= 12; i++) {
				Element tr = trs.get(i);
				Elements tds = tr.getElementsByTag("td");

				tds.get(0).child(0).html(getHtmlHeaderFromIndex(i));
				for (int j = 1; j <= 7; j++) {
					Element td = tds.get(j);
					if (td.hasAttr("style")) {
						Element span = td.child(0);
						String[] strings = span.html().split("<br>");
						int k = 0;
						while (k < strings.length && strings[k].trim().length() == 0) k++;
						if (k == strings.length) continue;
						String courseName = strings[k];
						k++;
						while (k < strings.length && strings[k].trim().length() == 0) k++;
						if (k == strings.length) continue;
						String where = strings[k].trim();
						int pos = where.indexOf(")");
						String location = "(？)";
						if (pos != -1) {
							location = where.substring(0, pos + 1);
							if (location.contains("备注")) location = "(？)";
						}
						//if ("(？)".equals(location)) hasQuestion = true;
						String type = "";
						if (span.text().contains("单周")) type = "<br>单周";
						if (span.text().contains("双周")) type = "<br>双周";
						if (span.text().contains("每周")) type = "<br>每周";
						span.html(courseName + "<br>" + location + type);
					}
				}
			}
			html = document.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return html;
	}

	private static String getHtmlHeaderFromIndex(int index) {
		if (index == 1) return "一<br>08:00<br>08:50";
		if (index == 2) return "二<br>09:00<br>09:50";
		if (index == 3) return "三<br>10:10<br>11:00";
		if (index == 4) return "四<br>11:10<br>12:00";
		if (index == 5) return "五<br>13:00<br>13:50";
		if (index == 6) return "六<br>14:00<br>14:50";
		if (index == 7) return "七<br>15:10<br>16:00";
		if (index == 8) return "八<br>16:10<br>17:00";
		if (index == 9) return "九<br>17:10<br>18:00";
		if (index == 10) return "十<br>18:40<br>19:30";
		if (index == 11) return "十一<br>19:40<br>20:30";
		if (index == 12) return "十二<br>20:40<br>21:30";
		return "";
	}

	static class CourseInfo {
		String name;
		String where;
		String color;
		ArrayList<Integer> when;

		public CourseInfo(String _name, String _where) {
			name = new String(_name).trim();
			where = new String(_where).trim();
			color = Util.generateColorString();
			when = new ArrayList<>();
		}

		public void addTime(String _week, String _times, int _type) throws Exception {
			_week = _week.trim();
			int week = Integer.parseInt(_week);
			_times = _times.trim();
			String[] strings = _times.split("-");
			if (strings.length == 1) {
				when.add(_type * 1000000 + week * 1000 + Integer.parseInt(strings[0]));
				return;
			} else if (strings.length != 2) throw new Exception("解析出错");
			int begin = Integer.parseInt(strings[0]), end = Integer.parseInt(strings[1]);
			if (begin > end) {
				int tmp = begin;
				begin = end;
				end = tmp;
			}
			for (int j = begin; j <= end; j++) {
				when.add(_type * 1000000 + week * 1000 + j);
			}

		}

		public int size() {
			return when.size();
		}

		public int getWeek(int index) {
			return (when.get(index) / 1000) % 1000;
		}

		public int getIndex(int index) {
			return when.get(index) % 1000;
		}

		public int getType(int index) {
			return when.get(index) / 1000000;
		}

		public String addToHtml(String html, boolean override) {
			Document document = Jsoup.parse(html);
			Element table = document.getElementById("classAssignment");
			Elements trs = table.getElementsByTag("tr");
			for (int i = 0; i < when.size(); i++) {
				int week = getWeek(i);
				int index = getIndex(i);
				Element tr = trs.get(index);
				Elements tds = tr.getElementsByTag("td");
				Element td = tds.get(week);
				if (!override && td.hasAttr("style")) continue;
				String type;
				int t = getType(i);
				if (t == Constants.COURSE_TYPE_ODD) type = "<br>单周";
				else if (t == Constants.COURSE_TYPE_EVEN) type = "<br>双周";
				else type = "";
				td.html("<span>" + name + "<br>(" + where + ")" + type + "</span>");
				td.attr("style", "background-color: " + color);
			}

			return document.toString();
		}

	}

	private static String showHtml(String html) {
		try {
			int week = Editor.getInt(PKUHelper.pkuhelper, "week");
			Document document = Jsoup.parse(html);

			Element table = document.getElementById("classAssignment");
			Elements trs = table.getElementsByTag("tr");
			for (int i = 1; i <= 12; i++) {
				Element tr = trs.get(i);
				Elements tds = tr.getElementsByTag("td");

				for (int j = 1; j <= 7; j++) {
					Element td = tds.get(j);
					td.attr("align","left");
					td.attr("valign","top");
					Element span = td.child(0);
					span.html(span.html()
							.replaceAll("^([^<>]+)<br>", "<span style='font-size:13px; font-weight:bold'>$1</span><br>")
							.replaceAll("<br>\\((.+?)\\)", "<br>@$1")
							.replaceAll("<br>([^@<>]+)$", "<br>[$1]")
							.replace("@？", "@未知地点")
							.replace("<br>[每周]", ""));
					if (week == 0 || (span.html().contains("单周") && week % 2 == 0)
							|| (span.html().contains("双周") && week % 2 != 0)) {
						td.attr("style", td.attr("style") + "; opacity:0.4;");
					}
				}
			}

			for (int i=1;i<=12;i++) {
				Element tr = trs.get(i);
				Elements tds = tr.getElementsByTag("td");
				for (int j = 1; j <= 7; j++) {
					Element td = tds.get(j);
					if (td.hasAttr("style") && !td.attr("style").contains("display")) {
						int cnt=1;
						for (int w=i+1;w<=12;w++) {
							Element trw = trs.get(w);
							Element tdw = trw.getElementsByTag("td").get(j);
							if (td.text().equals(tdw.text())) {
								tdw.attr("style", tdw.attr("style")+"; display: none;");
								cnt++;
							}
							else break;
						}
						td.attr("rowspan", cnt+"");
					}
				}
			}

			// 判断周六和周日是否有课
			boolean hascourse=false;
			for (int i = 1; i <= 12; i++) {
				Element tr = trs.get(i);
				Elements tds = tr.getElementsByTag("td");
				for (int j = 6; j <= 7; j++) {
					if (tds.get(j).hasAttr("style"))
						hascourse=true;
				}
			}

			Elements ths=trs.get(0).getElementsByTag("th");
			ths.get(0).attr("width", "10%");
			if (hascourse) {
				for (int i=1;i<=7;i++) {
					ths.get(i).attr("width", "12.857%");
				}
			}
			else {
				for (int i=1;i<=5;i++) {
					ths.get(i).attr("width", "18%");
				}
				ths.get(6).attr("style", "display:none;");
				ths.get(7).attr("style", "display:none;");
				for (int i = 1; i <= 12; i++) {
					Element tr = trs.get(i);
					Elements tds = tr.getElementsByTag("td");
					for (int j = 6; j <= 7; j++) {
						tds.get(j).attr("style", "display:none;");
					}
				}
			}

			return addLine(document.toString().replace("body{background-color: #ffffff;", "body{"));
		} catch (Exception e) {
			return CourseString.defaultHtml;
		}
	}

	/**
	 * 给html加上红线，表示当时的时间
	 *
	 * @param html
	 * @return
	 */
	private static String addLine(String html) {
		int week = Editor.getInt(PKUHelper.pkuhelper, "week");
		if (week < 0 || week >= 20) week = 0;
		if (week == 0) return html;

		try {
			Calendar calendar = Calendar.getInstance();
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);
			int dayofweek = MyCalendar.getWeekDayInNumber(calendar);

			Parameters parameters = getCourseIndex(hour, minute);
			int index = Integer.parseInt(parameters.name), percentage = Integer.parseInt(parameters.value);

			if (index == -1) return html;
			if (percentage <= 0) percentage = 0;
			if (percentage >= 100) percentage = 100;

			Document document = Jsoup.parse(html);
			Element table = document.getElementById("classAssignment");
			Elements trs = table.getElementsByTag("tr");

			Element td = trs.get(index).child(dayofweek);

			if (td.hasAttr("style")) {
				int u=0;
				while (td.attr("style").contains("display")) {
					index--;
					if (index<=0) return html;
					td = trs.get(index).child(dayofweek);
					u++;
				}
				int cnt=Integer.parseInt(td.attr("rowspan"));
				percentage=(100*u+percentage)/cnt;
				td.attr("style", td.attr("style")+"; position:relative");
				td.html(td.html()+String.format(Locale.getDefault(), CourseString.HR_STRING, percentage));
			}
			else {
				td.attr("style", "position:relative");
				td.html(td.html() + String.format(Locale.getDefault(), CourseString.HR_STRING, percentage));
			}

			return document.toString();
		} catch (Exception e) {
			return html;
		}
	}

	/**
	 * 根据hour和minute计算此时是第几节课以及百分比
	 *
	 * @param hour
	 * @param minute
	 * @return Parameters.name 第几节课  Parameters.value 百分比
	 */
	private static Parameters getCourseIndex(int hour, int minute) {
		if (hour < 8) return new Parameters("-1", "-1");
		if (hour == 8 && minute < 50) return new Parameters("1", minute * 2 + "");
		if (hour == 8) return new Parameters("2", "0");
		if (hour == 9 && minute < 50) return new Parameters("2", minute * 2 + "");
		if (hour == 9) return new Parameters("3", "0");
		if (hour == 10 && minute < 10) return new Parameters("3", "0");
		if (hour == 10) return new Parameters("3", (minute - 10) * 2 + "");
		if (hour == 11 && minute < 10) return new Parameters("4", "0");
		if (hour == 11) return new Parameters("4", (minute - 10) * 2 + "");
		if (hour == 12) return new Parameters("5", "0");
		if (hour == 13 && minute < 50) return new Parameters("5", minute * 2 + "");
		if (hour == 13) return new Parameters("6", "0");
		if (hour == 14 && minute < 50) return new Parameters("6", minute * 2 + "");
		if (hour == 14) return new Parameters("7", "0");
		if (hour == 15 && minute < 10) return new Parameters("7", "0");
		if (hour == 15) return new Parameters("7", (minute - 10) * 2 + "");
		if (hour == 16 && minute < 10) return new Parameters("8", "0");
		if (hour == 16) return new Parameters("8", (minute - 10) * 2 + "");
		if (hour == 17 && minute < 10) return new Parameters("9", "0");
		if (hour == 17) return new Parameters("9", (minute - 10) * 2 + "");
		if (hour == 18 && minute < 40) return new Parameters("10", "0");
		if (hour == 18) return new Parameters("10", (minute - 40) * 2 + "");
		if (hour == 19 && minute < 30) return new Parameters("10", (minute + 20) * 2 + "");
		if (hour == 19 && minute < 40) return new Parameters("11", "0");
		if (hour == 19) return new Parameters("11", (minute - 40) * 2 + "");
		if (hour == 20 && minute < 30) return new Parameters("11", (minute + 20) * 2 + "");
		if (hour == 20 && minute < 40) return new Parameters("12", "0");
		if (hour == 20) return new Parameters("12", (minute - 40) * 2 + "");
		if (hour == 21 && minute < 30) return new Parameters("12", (minute + 20) * 2 + "");
		return new Parameters("-1", "-1");
	}

	public static void changeColor() {
		final HashMap<String, String> map=new HashMap<>();
		try {
			html=changeColor(html, map);
			showView();
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						MyFile.putString(PKUHelper.pkuhelper, Constants.username, "deancourse",
								changeColor(MyFile.getString(PKUHelper.pkuhelper,
										Constants.username, "deancourse", CourseString.defaultCouseHtml), map));
					}
					catch (Exception e) {}
				}
			}).start();
		}
		catch (Exception e) {}
	}

	private static String changeColor(String html, HashMap<String, String> map) {
		try {
			String dehtml=new String(html);
			Document document=Jsoup.parse(html);

			Element table = document.getElementById("classAssignment");
			Elements trs = table.getElementsByTag("tr");
			for (int i = 1; i <= 12; i++) {
				Element tr = trs.get(i);
				Elements tds = tr.getElementsByTag("td");
				for (int j = 1; j <= 7; j++) {
					Element td = tds.get(j);
					if (td.hasAttr("style")) {
						Element span = td.child(0);
						String[] strings = span.html().split("<br>");
						String name = strings[0].trim();

						String color = map.get(name);
						if (color == null || "".equals(color)) color = Util.generateColorString();
						map.put(name, color);

						//String style=td.attr("style");
						td.attr("style", "background-color: " + color);

					}
				}
			}
			return document.toString();
		}
		catch (Exception e) {return html;}
	}

	public static void setBackground() {
		int width = PKUHelper.pkuhelper.findViewById(R.id.course_view).getWidth();
		int height = PKUHelper.pkuhelper.findViewById(R.id.course_view).getHeight();
		Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", width);
		intent.putExtra("aspectY", height);
		intent.putExtra("return-data", false);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(
				MyFile.getFile(PKUHelper.pkuhelper, null, "bg_course_temp.jpg")));
		PKUHelper.pkuhelper.startActivityForResult(intent, 1);
	}

	@SuppressWarnings("deprecation")
	public static void realSetBackground(Intent data) {
		try {
			File file = MyFile.getFile(PKUHelper.pkuhelper, null, "bg_course_temp.jpg");
			if (file.exists()) {
				drawable = Drawable.createFromPath(file.getAbsolutePath());
				courseView.setBackgroundDrawable(drawable);

				File file2 = MyFile.getFile(PKUHelper.pkuhelper, null, "bg_course.jpg");
				file2.delete();
				file.renameTo(file2);

				PKUHelper.pkuhelper.sendBroadcast(
						new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
			}
		} catch (Exception e) {
			CustomToast.showErrorToast(PKUHelper.pkuhelper, "设置失败");
		}
	}

	private void initBackgroundFromFile() {
		if (drawable == null) {
			File bgFile = MyFile.getFile(getActivity(), null, "bg_course.jpg");
			if (!bgFile.exists()) {
				drawable = PKUHelper.pkuhelper.getResources().getDrawable(R.drawable.mypku_bg);
				try {
					Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
					FileOutputStream fileOutputStream = new FileOutputStream(bgFile);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
					fileOutputStream.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					drawable = Drawable.createFromPath(bgFile.getAbsolutePath());
				} catch (Exception e) {
					drawable = PKUHelper.pkuhelper.getResources().getDrawable(R.drawable.mypku_bg);
					try {
						Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
						FileOutputStream fileOutputStream = new FileOutputStream(bgFile);
						bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
						fileOutputStream.flush();
					} catch (Exception ee) {
						e.printStackTrace();
					}
				}
			}

			PKUHelper.pkuhelper.sendBroadcast(
					new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(bgFile)));

			//courseView.setBackground(drawable);
		}
	}
}

class CourseString {
	public final static String defaultHtml =
			"<html><head><meta charset='utf-8'></html><body><center>" +
					"<p style='margin-top:100px'>请点击菜单栏上的刷新按钮<br>以和教务进行课程同步" +
					"</p></center></body></html>";

	public static final String cssString =
			"body{background-color: #ffffff;background-repeat: no-repeat;font-family: Arial, Verdana;"
					+ "font-size: 12px;font-style: normal;font-weight: normal;margin: 0; padding: 0;}"
					+ ".course{border: 1px solid #E2E2E2; border-collapse: collapse;padding: 3px;"
					+ "}.course-all{background-color: #E7F7AA;}"
					+ ".course-header,.course-footer{background-color: #CCDDEE;color: #555588;"
					+ "font-size: 14px;text-align: center;vertical-align: baseline;"
					+ "line-height: 18px;border-color: #999999;border-style: solid;"
					+ "border-width: 1px;padding-left: 10px;}a,div,span,p,font,table,tr,td{"
					+ "font-size: 12px;}";

	public static final String defaultCouseHtml = "<table id='classAssignment' class='course' width='100%'>"
			+ "<tr class='course-header'><th class='course'>节数</th><th class='course'>"
			+ "星期一</th><th class='course'>星期二</th><th class='course'>星期三</th><th class='course'>星期四"
			+ "</th><th class='course'>星期五</th><th class='course'>星期六</th><th class="
			+ "'course'>星期日</th></tr><tr class='course-even'><td class='course' align='"
			+ "center'><span>第一节</span></td><td class='course' align='center'><span></sp"
			+ "an></td><td class='course' align='center'><span></span></td><td class='course"
			+ "' align='center'><span></span></td><td class='course' align='center'><span></"
			+ "span></td><td class='course' align='center'><span></span></td><td class='cour"
			+ "se' align='center'><span></span></td><td class='course' align='center'><span>"
			+ "</span></td></tr><tr class='course-odd'><td class='course' align='center'><sp"
			+ "an>第二节</span></td><td class='course' align='center'><span></span></td><td "
			+ "class='course' align='center'><span></span></td><td class='course' align='cen"
			+ "ter'><span></span></td><td class='course' align='center'><span></span></td><td"
			+ " class='course' align='center'><span></span></td><td class='course' align='ce"
			+ "nter'><span></span></td><td class='course' align='center'><span></span></td><"
			+ "/tr><tr class='course-even'><td class='course' align='center'><span>第三节</s"
			+ "pan></td><td class='course' align='center'><span></span></td><td class='cours"
			+ "e' align='center'><span></span></td><td class='course' align='center'><span>"
			+ "</span></td><td class='course' align='center'><span></span></td><td class='co"
			+ "urse' align='center'><span></span></td><td class='course' align='center'><spa"
			+ "n></span></td><td class='course' align='center'><span></span></td></tr><tr cl"
			+ "ass='course-odd'><td class='course' align='center'><span>第四节</span></td><t"
			+ "d class='course' align='center'><span></span></td><td class='course' align='c"
			+ "enter'><span></span></td><td class='course' align='center'><span></span></td><"
			+ "td class='course' align='center'><span></span></td><td class='course' align='"
			+ "center'><span></span></td><td class='course' align='center'><span></span></td"
			+ "><td class='course' align='center'><span></span></td></tr><tr class='course-e"
			+ "ven'><td class='course' align='center'><span>第五节</span></td><td class='cou"
			+ "rse' align='center'><span></span></td><td class='course' align='center'><span"
			+ "></span></td><td class='course' align='center'><span></span></td><td class='c"
			+ "ourse' align='center'><span></span></td><td class='course' align='center'><sp"
			+ "an></span></td><td class='course' align='center'><span></span></td><td class="
			+ "'course' align='center'><span></span></td></tr><tr class='course-odd'><td cl"
			+ "ass='course' align='center'><span>第六节</span></td><td class='course' align="
			+ "'center'><span></span></td><td class='course' align='center'><span></span></"
			+ "td><td class='course' align='center'><span></span></td><td class='course' al"
			+ "ign='center'><span></span></td><td class='course' align='center'><span></span"
			+ "></td><td class='course' align='center'><span></span></td><td class='course' "
			+ "align='center'><span></span></td></tr><tr class='course-even'><td class='cour"
			+ "se' align='center'><span>第七节</span></td><td class='course' align='center'>"
			+ "<span></span></td><td class='course' align='center'><span></span></td><td cla"
			+ "ss='course' align='center'><span></span></td><td class='course' align='center"
			+ "'><span></span></td><td class='course' align='center'><span></span></td><td c"
			+ "lass='course' align='center'><span></span></td><td class='course' align='cent"
			+ "er'><span></span></td></tr><tr class='course-odd'><td class='course' align='c"
			+ "enter'><span>第八节</span></td><td class='course' align='center'><span></span"
			+ "></td><td class='course' align='center'><span></span></td><td class='course' "
			+ "align='center'><span></span></td><td class='course' align='center'><span></sp"
			+ "an></td><td class='course' align='center'><span></span></td><td class='course"
			+ "' align='center'><span></span></td><td class='course' align='center'><span></"
			+ "span></td></tr><tr class='course-even'><td class='course' align='center'><spa"
			+ "n>第九节</span></td><td class='course' align='center'><span></span></td><td c"
			+ "lass='course' align='center'><span></span></td><td class='course' align='cent"
			+ "er'><span></span></td><td class='course' align='center'><span></span></td><td"
			+ " class='course' align='center'><span></span></td><td class='course' align='ce"
			+ "nter'><span></span></td><td class='course' align='center'><span></span></td><"
			+ "/tr><tr class='course-odd'><td class='course' align='center'><span>第十节</sp"
			+ "an></td><td class='course' align='center'><span></span></td><td class='course"
			+ "' align='center'><span></span></td><td class='course' align='center'><span></"
			+ "span></td><td class='course' align='center'><span></span></td><td class='cour"
			+ "se' align='center'><span></span></td><td class='course' align='center'><span>"
			+ "</span></td><td class='course' align='center'><span></span></td></tr><tr clas"
			+ "s='course-even'><td class='course' align='center'><span>第十一节</span></td><"
			+ "td class='course' align='center'><span></span></td><td class='course' align='"
			+ "center'><span></span></td><td class='course' align='center'><span></span></td"
			+ "><td class='course' align='center'><span></span></td><td class='course' align"
			+ "='center'><span></span></td><td class='course' align='center'><span></span></"
			+ "td><td class='course' align='center'><span></span></td></tr><tr class='course"
			+ "-odd'><td class='course' align='center'><span>第十二节</span></td><td class='"
			+ "course' align='center'><span></span></td><td class='course' align='center'><s"
			+ "pan></span></td><td class='course' align='center'><span></span></td><td class"
			+ "='course' align='center'><span></span></td><td class='course' align='center'>"
			+ "<span></span></td><td class='course' align='center'><span></span></td><td cla"
			+ "ss='course' align='center'><span></span></td></tr></table>";


	public static final String HR_STRING = "<hr color='red' style='position: absolute; z-index:100;"
			+ "width: 95%%;left: 0;top: %d%%;margin: 0;padding: 0;'>";
}

