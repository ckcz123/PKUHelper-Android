package com.pkuhelper.classroom;

import com.pkuhelper.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class ClassroomFragment extends Fragment{
	final static String defaultString="<html><head><meta charset='utf-8'></html><body><center>"+
			"<p style='margin-top:100px'>请点击菜单栏上的图标<br>选择你想查看的教学楼"+
			"</p></center></body></html>";
	WebView webView;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.classroom_fragement,
				container, false);
		webView=(WebView)rootView.findViewById(R.id.classroom_view);
		webView.setVerticalScrollBarEnabled(false);
		webView.setHorizontalScrollBarEnabled(false);
		showView(getArguments().getInt("index"));
		return rootView;
	}
	public void showView(int index) {
		String html=ClassActivity.classActivity.htmls[index];
		if (html==null || "".equals(html))
			html=defaultString;
		if (webView!=null)
		webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
	}
	
}
