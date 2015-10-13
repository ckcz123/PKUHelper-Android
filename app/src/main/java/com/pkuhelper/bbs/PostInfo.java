package com.pkuhelper.bbs;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.pkuhelper.lib.webconnection.Parameters;

public class PostInfo {
	String author;
	int postid;
	int number;
	long timestamp; // 单位为s
	String content;
	ArrayList<Parameters> attaches=new ArrayList<Parameters>();
	
	public PostInfo(String _author, int _postid, int _number,
			long _timestamp, String _content, String _attaches) {
		author=new String(_author);
		postid=_postid;
		number=_number;
		timestamp=_timestamp;
		content=translate(_content);
		attaches=getAttaches(_attaches);
	}
	public PostInfo(PostInfo another) {
		author=new String(another.author);
		postid=another.postid;
		number=another.number;
		timestamp=another.timestamp;
		content=new String(another.content);
		attaches=new ArrayList<Parameters>(another.attaches);
	}
	private String translate(String content) {
		String string=new String(content);
		string=string.replace("<pre>\n", "");
		string=string.replace("</pre>\n", "");
		string=string.replace("\n", "<br />");
		string=string.replace("<span class=col30>", "<font color='#000000'>");
		string=string.replace("<span class=col31>", "<font color='#800000'>");
		string=string.replace("<span class=col32>", "<font color='#008000'>");
		string=string.replace("<span class=col33>", "<font color='#808000'>");
		string=string.replace("<span class=col34>", "<font color='#000080'>");
		string=string.replace("<span class=col35>", "<font color='#800080'>");
		string=string.replace("<span class=col36>", "<font color='#008080'>");
		string=string.replace("<span class=col37>", "<font color='#808080'>");
		string=string.replace("<img class='signimg' src='/", "<img src='http://www.bdwm.net/");
		string=string.replaceAll("alt=\"mlatex:([^\"]+)\"", 
				"src=\"http://www.bdwm.net/m.php?p$1.png\"");
		string=string.replace("</span>", "</font>");
		string=string.replace("使用WWW方式可以查看附件", "<b><u><font color='#800000'>单击此楼可查看附件</font></u></b>");
		string=string.replace("兆字节", "MB");
		string=string.replace("千字节", "KB");
		return string;
	}
	private ArrayList<Parameters> getAttaches(String attaches) {
		attaches=attaches.trim();
		Document document=Jsoup.parse(attaches);
		Elements elements=document.getElementsByTag("a");
		int len=elements.size();
		ArrayList<Parameters> arrayList=new ArrayList<Parameters>();
		ArrayList<String> urls=new ArrayList<String>();
		for (int i=0;i<len;i++) {
			Element element=elements.get(i);
			String href=element.attr("href").trim();
			String text=element.text().trim();
			if (!urls.contains(href)
					&& !"".equals(text)) {
				urls.add(href);
				arrayList.add(new Parameters(text, href));
			}
		}
		return arrayList;
	}
}
