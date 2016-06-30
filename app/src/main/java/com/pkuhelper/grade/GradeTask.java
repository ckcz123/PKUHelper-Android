package com.pkuhelper.grade;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.lib.webconnection.WebConnection;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by oc on 2016/6/30.
 */
public class GradeTask extends AsyncTask<String, String, Parameters> {

	ProgressDialog progressDialog;
	GradeActivity gradeActivity;
	String phpsessid;

	public GradeTask(GradeActivity gradeActivity, String phpsessid) {
		this.gradeActivity=gradeActivity;
		this.phpsessid=new String(phpsessid);
		progressDialog=new ProgressDialog(gradeActivity);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setTitle("提示");
		progressDialog.setMessage("正在查询成绩...");
		progressDialog.setIndeterminate(false);
		progressDialog.setCancelable(false);
	}

	@Override
	protected void onPreExecute() {
		progressDialog.show();
	}

	@Override
	protected Parameters doInBackground(String... params) {
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("code", 0);
			JSONObject grade=dealWithDean("http://dean.pku.edu.cn/student/new_grade.php?PHPSESSID="+phpsessid);
			jsonObject.put("total", grade.optString("total", "0"));
			jsonObject.put("avggpa", grade.optString("avggpa", "0"));
			jsonObject.put("gpas", grade.optJSONArray("gpas"));
			jsonObject.put("courses", grade.optJSONArray("courses"));

			JSONObject grade2=dealWithDean("http://dean.pku.edu.cn/student/fxsxw.php?PHPSESSID="+phpsessid);
			jsonObject.put("dualtotal", grade2.optString("total", "0"));
			jsonObject.put("dualavggpa", grade2.optString("avggpa", "0"));
			jsonObject.put("dualgpas", grade2.optJSONArray("gpas"));
			jsonObject.put("dualcourses", grade2.optJSONArray("courses"));

			return new Parameters("200", jsonObject.toString());

		} catch (Exception e) {return new Parameters("-1", "");}
	}

	private JSONObject dealWithDean(String url)  {
		try {
			Parameters parameters=WebConnection.connect(url, null);
			if (!"200".equals(parameters.name)) return new JSONObject();
			Document document= Jsoup.parse(parameters.value);
			Elements tables=document.getElementsByTag("table");
			if (tables.size()==0) return new JSONObject();
			Element table=tables.get(0);
			Elements trs=table.getElementsByTag("tr");
			int len=trs.size();

			JSONArray gpas=new JSONArray();
			JSONArray courses=new JSONArray();

			for (int i=0;i<len;i++) {
				try {
					Element tr=trs.get(i);
					Elements tds=tr.getElementsByTag("td");
					if (tds.size()==4) {
						JSONObject gpa=new JSONObject();
						gpa.put("year", tds.get(0).text());
						gpa.put("term", tds.get(1).text());
						gpa.put("gpa", tds.get(3).text());
						gpas.put(gpa);
					}
					if (tds.size()<8) continue;
					String courseName = tds.get(5).text();
					String[] cns = courseName.split(" ");
					String shortCourseName = cns[0];
					String grade = tds.get(3).text();
					String accurate = "1";
					String delta = "0";
					String[] reg = grade.split("±");
					if (reg.length > 1) {
						accurate = "0";
						grade = reg[0];
						delta = reg[1];
					}
					String gpa = tds.get(7).text();
					if (gpa.contains("绩点请自行计算")) {
						try {
							gpa = "" + (4 - Math.pow(100.0 - Integer.parseInt(grade), 2) / 1600 * 3);
						} catch (Exception e) {
						}
					}
					JSONObject course = new JSONObject();
					course.put("name", shortCourseName);
					course.put("fullName", courseName);
					course.put("year", tds.get(0).text());
					course.put("term", tds.get(1).text());
					course.put("type", tds.get(4).text());
					course.put("weight", tds.get(6).text());
					course.put("grade", grade);
					course.put("delta", delta);
					course.put("accurate", accurate);
					course.put("gpa", gpa);
					courses.put(course);
				}
				catch (Exception ee) {}
			}
			Matcher matcher=Pattern.compile("总学分:(\\d+)").matcher(document.toString());
			String total="";
			if (matcher.find()) total=matcher.group(1);
			String avggpa="";
			matcher=Pattern.compile("平均绩点(\\(GPA\\)):([\\d.]+)").matcher(document.toString());
			if (matcher.find()) avggpa=matcher.group(2);
			JSONObject jsonObject=new JSONObject();
			jsonObject.put("gpas", gpas);
			jsonObject.put("courses", courses);
			jsonObject.put("total", total);
			jsonObject.put("avggpa", avggpa);
			return  jsonObject;
		}
		catch (Exception e) {return new JSONObject();}
	}

	@Override
	protected void onPostExecute(Parameters parameters) {
		progressDialog.dismiss();
		if (!"200".equals(parameters.name)) {
			if ("-1".equals(parameters.name))
				CustomToast.showInfoToast(gradeActivity, "无法连接网络(-1,-1)");
			else {
				CustomToast.showInfoToast(gradeActivity, "无法连接到服务器 (HTTP " + parameters.name + ")");
			}
		} else
			gradeActivity.finishRequest(Constants.REQUEST_DEAN_GETTING_GRADE, parameters.value);
	}


}
