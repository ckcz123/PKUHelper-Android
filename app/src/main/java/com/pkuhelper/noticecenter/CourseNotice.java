package com.pkuhelper.noticecenter;

import java.util.ArrayList;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.RequestingTask;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;

public class CourseNotice {
	@SuppressWarnings("unchecked")
	public static void loginToCourse() {
		ArrayList<Parameters> arrayList=new ArrayList<Parameters>();
		arrayList.add(new Parameters("user_id", Constants.username));
		arrayList.add(new Parameters("pwd", CourseJSTranslation.strEncode(Constants.password)));
		
		new RequestingTask(NCActivity.ncActivity, "正在登录教学网...", "http://course.pku.edu.cn/webapps/login/",
				Constants.REQUEST_NOTICECENTER_COURSE_LOGIN).execute(arrayList);
		
	}
	
	@SuppressWarnings("unchecked")
	public static void finishLogin(String string) {
		Log.i("courseString", string);
		if (!string.contains("Please Wait")) {
			CustomToast.showErrorToast(NCActivity.ncActivity, "教学网登录失败，请退出并重试");
			NCContent.showContent();
			return;
		}
		ArrayList<Parameters> arrayList=new ArrayList<Parameters>();
		arrayList.add(new Parameters("action","refreshAjaxModule"));
		arrayList.add(new Parameters("modId", "_1_1"));
		arrayList.add(new Parameters("tabId", "_1_1"));
		arrayList.add(new Parameters("tab_tab_group_id", "_3_1"));
		new RequestingTask(NCActivity.ncActivity, "正在获取教学网的通知...",
			"http://course.pku.edu.cn/webapps/portal/execute/tabs/tabAction", 
			Constants.REQUEST_NOTICECENTER_COURSE_GETDETAIL).execute(arrayList);
	}
	
	public static void finishGetContent(String string) {
		String html=string;
		Log.i("stringFromCourse", html);
		html=html.replace("<!--", "");
		html=html.replace("-->", "");
		html=html.replace("<![CDATA[", "");
		html=html.replace("]]>", "");
		try {
			Document document=Jsoup.parse(html);
			Elements elements=document.getElementsByTag("h3");
			Iterator<Element> iterator=elements.iterator();
			ArrayList<Content> arrayList=new ArrayList<Content>();
			while (iterator.hasNext()) {
				Element element=iterator.next();
				String courseName=element.text();
				Element hh2=element.nextElementSibling();
				Elements lis=hh2.getElementsByTag("li");
				Iterator<Element> iterator2=lis.iterator();
				while (iterator2.hasNext()) {
					Element content=iterator2.next();
					String url="http://course.pku.edu.cn"+content.child(0).attr("href");
					String title=content.text();
					Content courseContent=new Content("0", title, "0", url, "来自于课程："+courseName, "一周以内");
					arrayList.add(courseContent);
				}
			}
			NCActivity.ncActivity.contentListArray.addAll(0, arrayList);
		}
		catch (Exception e) {
			CustomToast.showErrorToast(NCActivity.ncActivity, "教学网通知获取失败");
		}
		finally {
			NCContent.showContent();
		}
	}
}
