package com.pkuhelper.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pkuhelper.lib.Lib;

public class MyReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Lib.updateAndCheck(context);
	}

}
