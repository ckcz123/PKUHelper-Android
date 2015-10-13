package com.pkuhelper.service;

import com.pkuhelper.lib.Lib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Lib.updateAndCheck(context);
	}
	
}
