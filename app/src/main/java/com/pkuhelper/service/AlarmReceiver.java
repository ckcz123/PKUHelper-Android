package com.pkuhelper.service;

import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.Lib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Constants.ACTION_ALARM.equals(intent.getAction())) {
			Lib.updateAndCheck(context);
		}
	}

}
