package com.pkuhelper.widget;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.Lib;
import com.pkuhelper.lib.view.CustomToast;

public class WidgetActionService extends Service {

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		run(intent);
		stopSelf();
		return START_NOT_STICKY;
	}

	private void run(Intent intent) {
		if (intent == null) return;
		if (intent.getBooleanExtra("refresh", false)) {
			Lib.sendBroadcast(this, WidgetCourseProvider.class, Constants.ACTION_REFRESH_COURSE);
			return;
		}

		String name = intent.getStringExtra("name");
		String location = intent.getStringExtra("location");
		String type = intent.getStringExtra("type");
		String hint = name;
		if (location != null && !"".equals(location))
			hint += " (" + location + ")";
		hint += " " + type;
		CustomToast.showInfoToast(this, hint, 750);

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}


}
