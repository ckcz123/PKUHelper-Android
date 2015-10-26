package com.pkuhelper.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.Lib;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Constants.ACTION_ALARM.equals(intent.getAction())) {
			Lib.updateAndCheck(context);
		}
	}

}
