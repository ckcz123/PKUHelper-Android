package com.pkuhelper.subactivity;

import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;

import android.support.v4.widget.SwipeRefreshLayout;
import android.webkit.WebView;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;

public class SchoolCalendar {
	
	SubActivity subActivity;
	public SchoolCalendar(SubActivity subActivity) {
		this.subActivity=subActivity;
	}

	@SuppressWarnings("unchecked")
	public SchoolCalendar showCalendar() {
		subActivity.setContentView(R.layout.subactivity_webview);
		subActivity.getActionBar().setTitle("北京大学校历");
		
		subActivity.webView=
				(WebView)subActivity.findViewById(R.id.subactivity_webview);
		subActivity.webView.setVerticalScrollBarEnabled(false);
		subActivity.webView.setHorizontalScrollBarEnabled(false);
		new RequestingTask(subActivity,"正在获取校历...", "http://www.pku.edu.cn/campuslife/xl/index.htm"
				, Constants.REQUEST_SUBACTIVITY_CALENDAR)
		.execute(new ArrayList<Parameters>());
		return this;
	}
	
	public void finishRequest(String string) {
			Calendar calendar=Calendar.getInstance(Locale.CHINA);
			int year=calendar.get(Calendar.YEAR);
			int month=calendar.get(Calendar.MONTH)+1;
			String nowyear="";
			if (month<=7)
				nowyear=(year-1)+"-"+year;
			else nowyear=year+"-"+(year+1);
			Document document=Jsoup.parse(string, "utf-8");
			Element element=null;
			
			for (int i=0;i<=10;i++) {
				element=document.getElementById("id_swap_"+i+"_data");
				if (element==null) continue;
				String text=element.text();
				if (text.contains("校历") && text.contains(nowyear))
					break;
				else element=null;
			}
			if (element==null) {
				CustomToast.showErrorToast(subActivity, "获取校历失败。");
				return;
			}
			element.getElementsByTag("a").remove();
			String title=element.getElementsByClass("clearfix1").get(0).text();
			subActivity.getActionBar().setTitle(title);
			String html="<html><body><style>"
					+ "html,body,div,span,h2,p,a,table,td,tr,th{border: 0px;"
					+ "padding: 0px;margin: 0px;"
					+ "font-family: 'Microsoft Yahei','Lucida Grande','Tahoma','Arial','Helvetica','sans-serif';}"
					+ "body{padding-bottom:40px}"
					+ "h2{font-size:20px;text-align:center;color:#ff0000;font-weight:bold;"
					+ "margin-top:20px;margin-bottom:20px;}"
					+ ".article_cn{margin-left:10px;}"
					+ "table{display:inline}"
					+ "td{vertical-align:top;font-size:16px;line-height:160%}"
					+ ".yaheired {font-size:18px;font-weight:bold;color:#9b0000;}"
					+ "</style>";
			html+=element.html();
			html+="</body></html>";
			SwipeRefreshLayout swipeRefreshLayout=(SwipeRefreshLayout)subActivity.findViewById(R.id.subactivity_swipeRefreshLayout);
			swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_purple, 
		            android.R.color.holo_green_light, 
		            android.R.color.holo_blue_bright, 
		            android.R.color.holo_orange_light);
			swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
				public void onRefresh() {
					subActivity.setRefresh();
				}
			});
			subActivity.webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
	}
}
