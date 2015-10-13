package com.pkuhelper.media;

import java.util.ArrayList;
import java.util.HashMap;

import com.pkuhelper.lib.webconnection.Parameters;

import android.annotation.SuppressLint;

@SuppressLint("UseSparseArrays")
public class Source {
	public int sid;
	public String name;
	public static HashMap<Integer, Source> sources=new HashMap<Integer, Source>();
	
	public Source(int _sid, String _name) {
		sid=_sid;
		name=new String(_name);
	}
	
	public static void init() {
		if (sources==null) sources=new HashMap<Integer, Source>();
		sources.clear();
		sources.put(1, new Source(1, "北京大学官方微信"));
		sources.put(2, new Source(2, "北京大学官方微博"));
		sources.put(3, new Source(3, "北大未名BBS官方微信"));
		sources.put(4, new Source(4, "北大未名BBS官方微博"));
		sources.put(5, new Source(5, "北大新青年官方微信"));	
	}
	
	public static void setSources(ArrayList<Parameters> arrayList) {
		if (sources==null) sources=new HashMap<Integer, Source>();
		sources.clear();
		for (Parameters parameter: arrayList)
			sources.put(Integer.parseInt(parameter.name), new Source(Integer.parseInt(parameter.name), parameter.value));
	}
	
}
