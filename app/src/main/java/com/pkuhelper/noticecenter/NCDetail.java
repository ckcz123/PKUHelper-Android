package com.pkuhelper.noticecenter;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.DataObject;
import com.pkuhelper.lib.RequestingTask;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.subactivity.SubActivity;

import android.content.Intent;
import android.graphics.Bitmap;

public class NCDetail {
	static String requestURL="";
	static String requestTitle="";
	
	@SuppressWarnings("unchecked")
	public static void getCourse(String title, String url) {
		new RequestingTask(NCActivity.ncActivity, "正在获取详细信息...", url,
				Constants.REQUEST_NOTICECENTER_COURSE_GETWEBSITE)
				.execute(new ArrayList<Parameters>());
		requestURL=url;
		requestTitle=title;
	}
	
	public static void showDirectly(String title, int sid, int nid, 
			String description, Bitmap bitmap) {
		Intent intent=new Intent(NCActivity.ncActivity, SubActivity.class);
		intent.putExtra("type", Constants.SUBACTIVITY_TYPE_WEBVIEW);
		intent.putExtra("url", Constants.domain+"/pkuhelper/nc/content.php?nid="+nid);
		intent.putExtra("title", title);
		intent.putExtra("sid", sid);
		intent.putExtra("content", description);
		if (bitmap!=null)
		{
			intent.putExtra("hasBitmap", true);
			DataObject.getInstance().setObject(bitmap);
		}
		NCActivity.ncActivity.startActivity(intent);
	}
	
	public static void finishGetCourse(String string) {
		try {
			String html="<html><head><style>li{margin-top:15px;}</style></head><body>";
			Document document=Jsoup.parse(string);
			Element ul=document.getElementById("announcementList");
			html+=ul.html();
			html+="<br><br>***********************"
					+ "<br>此网页由PKU Helper经过了摘要提取，如需访问原网页请点击<a href='"
					+requestURL+"' target='_blank'>这里</a><br><br></body></html>";
			Intent intent=new Intent(NCActivity.ncActivity, SubActivity.class);
			intent.putExtra("type", Constants.SUBACTIVITY_TYPE_WEBVIEW_HTML);
			intent.putExtra("html", html);
			intent.putExtra("title", requestTitle);
			NCActivity.ncActivity.startActivity(intent);
		}
		catch (Exception e) {
			CustomToast.showErrorToast(NCActivity.ncActivity, "详细信息获取失败");
		}
		
	}
	
}
