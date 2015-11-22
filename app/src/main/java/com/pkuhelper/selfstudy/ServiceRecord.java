package com.pkuhelper.selfstudy;

import java.util.ArrayList;
import java.util.List;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ServiceRecord {
	DBHelper dbHelper;
	Context context;
	
	public ServiceRecord(Context context) {
		this.context = context;
		dbHelper = new DBHelper(context);
	}
	
	public void insertARecord(Record record) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("insert into records (timeStart, timeEnd) values(" + record.getTimeStart() + ", " + record.getTimeEnd() + ")");
		db.close();
	}
	
	public void deleteAllData() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.execSQL("delete from records");
		db.close();
	}
	
	public List<Record> getRecords(String sql) {
		List<Record> list = new ArrayList<>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(sql, null);
		Record record;
		while (cursor.moveToNext()) {
			record = new Record();
			record.setId(cursor.getInt(cursor.getColumnIndex("id")));
			record.setTimeStart(cursor.getLong(cursor.getColumnIndex("timeStart")));
			record.setTimeEnd(cursor.getLong(cursor.getColumnIndex("timeEnd")));
			long timeStart = record.getTimeStart();
			long timeEnd = record.getTimeEnd();
			System.out.println("servicerecord : " + timeStart + " " + timeEnd + " " + (timeEnd-timeStart));
			list.add(record);
		}
		cursor.close();
		db.close();
		return list;
	}
}
