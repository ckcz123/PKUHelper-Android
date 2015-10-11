package com.pkuhelper.course;

public class TimeInfo {
	int week,starttime,endtime;
	int type;
	public TimeInfo(int _week, int _starttime, int _endtime, int _type) {
		week=_week;starttime=_starttime;endtime=_endtime;type=_type;
	}
	public TimeInfo(TimeInfo time) {
		week=Integer.valueOf(time.week);
		starttime=Integer.valueOf(time.starttime);
		endtime=Integer.valueOf(time.endtime);
		type=Integer.valueOf(type);
	}
}