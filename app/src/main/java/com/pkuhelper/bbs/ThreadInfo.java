package com.pkuhelper.bbs;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ThreadInfo {
	int rank;
	String board;
	String boardName;
	String author;
	long time;
	String title;
	int threadid;
	boolean isTop;

	/**
	 * 十大帖子的初始化
	 *
	 * @param _rank
	 * @param _board
	 * @param _boardName
	 * @param _author
	 * @param _time      为M-d HH:mm:ss形式
	 * @param _title
	 * @param _threadid
	 */
	public ThreadInfo(int _rank, String _board, String _boardName,
					  String _author, String _time, String _title, int _threadid) throws Exception {
		rank = _rank;
		board = new String(_board);
		boardName = new String(_boardName);
		author = new String(_author);
		title = new String(_title);
		threadid = _threadid;

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("M-d HH:mm:ss", Locale.getDefault());
		Date date = simpleDateFormat.parse(_time);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);

		Calendar nowTime = Calendar.getInstance();
		calendar.set(Calendar.YEAR, nowTime.get(Calendar.YEAR));

		// 如果在现在之后，代表是去年发的
		if (nowTime.getTimeInMillis() <= calendar.getTimeInMillis()) {
			calendar.add(Calendar.YEAR, -1);
		}

		time = calendar.getTimeInMillis();
	}

	/**
	 * 一个普通帖子的初始化
	 *
	 * @param _board
	 * @param _boardName
	 * @param _author
	 * @param _time      时间戳（以秒为单位）
	 * @param _title
	 * @param _threadid
	 */
	public ThreadInfo(String _board, String _boardName,
					  String _author, long _time, String _title, int _threadid, int top) {
		rank = 0;
		board = new String(_board);
		boardName = new String(_boardName);
		author = new String(_author);
		title = new String(_title);
		threadid = _threadid;
		time = _time * 1000;
		isTop = top == 1;
	}

}
