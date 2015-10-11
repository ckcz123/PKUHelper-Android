package com.pkuhelper.service;

import com.pkuhelper.lib.Constants;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class PKUHelperService extends Service {
	@Override
	public void onCreate () {
		super.onCreate();
	}
	
	@Override
	public int onStartCommand (Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		if (android.os.Build.VERSION.SDK_INT<16) {
			stopSelf();
			return START_NOT_STICKY;
		}
		
		Handler handler=new Handler(getMainLooper(), new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				if (msg.what==Constants.MESSAGE_SERVICE_FINISHED)
					stopSelf();
				return false;
			}
		});
		try {
			new NotifyThread(this, handler).start();
		}
		catch (Exception e) {}
		
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onDestroy() {
		Log.w("service", "destroy");
	}
	
}
