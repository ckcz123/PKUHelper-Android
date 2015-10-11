package com.pkuhelper.course;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CourseInfo {
	String name;
	String where;
	ArrayList<TimeInfo> when;
	
	public CourseInfo(String _name, String _where) {
		name=new String(_name).trim();where=new String(_where).trim();
		when=new ArrayList<TimeInfo>();
	}

	public CourseInfo() {
		name="";
		where="";
		when=new ArrayList<TimeInfo>();
	}
	
	public void addTime(int _week, int _starttime, int _endtime, int _type) {
		when.add(new TimeInfo(_week, _starttime, _endtime, _type));
	}

	public void addTime(String _week, String _times, int type) throws Exception {
		_week=_week.trim();
		int week=Integer.parseInt(_week);
		_times=_times.trim();
		String[] strings=_times.split("-");
		if (strings.length==1) {
			when.add(new TimeInfo(week, Integer.parseInt(strings[0]), Integer.parseInt(strings[0]), type));
			return;
		}
		else if (strings.length!=2) throw new Exception("解析出错");
		int begin=Integer.parseInt(strings[0]),end=Integer.parseInt(strings[1]);
		if (begin>end) {
			int tmp=begin;begin=end;end=tmp;
		}
		when.add(new TimeInfo(week, begin, end, type));
	}
	public int size() {
		return when.size();
	}
	
	public void sortAndMerge() {
		Collections.sort(when, new Comparator<TimeInfo>() {
			@Override
			public int compare(TimeInfo t1, TimeInfo t2) {
				if (t1.type<t2.type) return -1;
				if (t1.type>t2.type) return 1;
				if (t1.week<t2.week) return -1;
				if (t1.week>t2.week) return 1;
				if (t1.starttime<t2.starttime) return -1;
				return 1;
			}
		});
		
		ArrayList<TimeInfo> arrayList=new ArrayList<TimeInfo>();
		int len=when.size();
		int index=0;
		TimeInfo lastTimeInfo=null;
		while (index<len) {
			TimeInfo thisTimeInfo=when.get(index);
			
			if (lastTimeInfo!=null
					&& thisTimeInfo.type==lastTimeInfo.type
					&& thisTimeInfo.week==lastTimeInfo.week
					&& thisTimeInfo.starttime==lastTimeInfo.endtime+1) {
				thisTimeInfo.starttime=lastTimeInfo.starttime;				
			}
			else {
				if (lastTimeInfo!=null)
					arrayList.add(lastTimeInfo);
			}
			
			lastTimeInfo=thisTimeInfo;
			
			index++;
		}
		if (lastTimeInfo!=null)
			arrayList.add(lastTimeInfo);
		
		when=new ArrayList<TimeInfo>(arrayList);
	}
	
}