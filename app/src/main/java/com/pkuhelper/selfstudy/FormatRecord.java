package com.pkuhelper.selfstudy;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.pkuhelper.R;

public class FormatRecord {
	public static Record getFormattedData(Record record) {
		long timeStart = record.getTimeStart();
		long timeEnd = record.getTimeEnd();
//		System.out.println(timeStart + " " + timeEnd + " " + (timeEnd-timeStart));
		SimpleDateFormat df;
		
		df =new SimpleDateFormat("yyyy年MM月dd日  HH:mm", Locale.getDefault());
//		System.out.println(df.format(new Date(timeStart)));
		
		df =new SimpleDateFormat("HH:mm", Locale.getDefault());
		record.setFormattedTime(df.format(new Date(timeStart)) + " - " +df.format(new Date(timeEnd)));
		long span = timeEnd - timeStart;
		record.setDurationInHHmm(df.format(new Date(span - 8*3600*1000)));
		System.out.println("df.format(new Date(span))");
		int high = (int) ((span+2000)/(float)(3600*1000));//加2秒延迟，令很接近2小时的时间显示为2小时
		int low = (int) (span/(float)(60*1000))%(60*1000);
		String duration;
		if(high > 0) {
			duration = high + "小时+";
//			if(low > 0) {
//				duration += "+";
//			}
			if (high < Constants.duration1) {
				record.setColorResourceId(R.drawable.shape_round_rect_1);
			} else if (Constants.duration1 <= high && high < Constants.duration2) {
				record.setColorResourceId(R.drawable.shape_round_rect_2);
			} else if (Constants.duration2 <= high) {
				record.setColorResourceId(R.drawable.shape_round_rect_3);
				record.setDurationInHHmm("02:00");//
			}
		} else {
			record.setColorResourceId(R.drawable.shape_round_rect_1);
			duration = low + "分钟";
		}
		record.setFormattedDuration(duration);
		df =new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
		record.setFormattedDate(df.format(new Date(timeStart)));
//		System.out.println(df.format(new Date(timeStart)));
		df =new SimpleDateFormat("E", Locale.getDefault());
		record.setFormattedWeek(df.format(new Date(timeStart)));
//		System.out.println(df.format(new Date(timeStart)));
		return record;
	}
}
