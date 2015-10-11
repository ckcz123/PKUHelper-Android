package com.pkuhelper.lib.view;

import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.TimePicker;

public class MyTimePickerDialog extends TimePickerDialog {
	private String title;
	public MyTimePickerDialog(Context context, OnTimeSetListener callBack, int hourOfDay, int minute, boolean is24HourView) {
		super(context, callBack, hourOfDay, minute, is24HourView);
	}
	public void setPermanentTitle(String title) {
        this.title=title;
        setTitle(title);
    }
	@Override
	public void onTimeChanged(TimePicker timePicker, int hour, int minute) {
		super.onTimeChanged(timePicker, hour, minute);
		setTitle(title);
	}
}