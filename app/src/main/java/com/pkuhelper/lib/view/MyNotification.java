package com.pkuhelper.lib.view;

import java.io.File;
import java.util.Locale;
import java.util.Random;

import com.pkuhelper.PKUHelper;
import com.pkuhelper.R;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

@SuppressLint("NewApi")
public class MyNotification {
	public static void sendNotification(String title,
			String content, String ticker, Context context, String type) {
		if (android.os.Build.VERSION.SDK_INT<16) return;
		Notification.Builder builder=new Notification.Builder(context);
		builder.setAutoCancel(true);
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setContentTitle(title);
		builder.setContentText(content);
		builder.setTicker(ticker);
		Intent resultIntent=new Intent(context, PKUHelper.class);
		if (type!=null && !"".equals(type)) {
			resultIntent.setAction("com.pkuhelper.action."+type);
			resultIntent.putExtra("type", type);
		}
		resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		TaskStackBuilder taskStackBuilder=TaskStackBuilder.create(context);
		taskStackBuilder.addParentStack(PKUHelper.class);
		taskStackBuilder.addNextIntent(resultIntent);
		PendingIntent pendingIntent=taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);
		NotificationManager notificationManager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(new Random().nextInt(), builder.build());
	}
	
	public static void sendNotificationToOpenfile(String title,
			String content, String ticker, Context context, File file) {
		if (android.os.Build.VERSION.SDK_INT<16) return;
		if (file==null || !file.exists()) return;
		Notification.Builder builder=new Notification.Builder(context);
		builder.setAutoCancel(true);
		builder.setSmallIcon(R.drawable.ic_launcher);
		
		builder.setContentTitle(title);
		builder.setContentText(content);
		builder.setTicker(ticker);
		
		Intent intent=new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		String type=getMIMEType(file);
		intent.setDataAndType(Uri.fromFile(file), type);
		PendingIntent pendingIntent=PendingIntent.getActivity(context, 
				new Random().nextInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);
		NotificationManager notificationManager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification=builder.build();
		notificationManager.notify(new Random().nextInt(), notification);
		
	}
	
	private static String getMIMEType(File file) {
		String[][] MIME_TABLE={
			{".3gp","video/3gpp"},{".apk","application/vnd.android.package-archive"},
			{".asf","video/x-ms-asf"},{".avi","video/x-msvideo"},
			{".bmp","image/bmp"},{".txt","text/plain"},{".jpg","image/jpeg"},
			{".jpeg","image/jpeg"},{".jpz","image/jpeg"},
			{".doc","application/msword"},{".docx","application/msword"},
			{".png","image/png"},{".gif","image/gif"},
			{".html","text/html"},{".htm","text/html"},{".ico","image/x-icon"},
			{".js","application/x-javascript"},{".mp3","audio/x-mpeg"},
			{".mov","video/quicktime"},{".mpg","video/x-mpeg"},{".mp4","video/mp4"},
			{".pdf","application/pdf"},{".ppt","application/vnd.ms-powerpoint"},
			{".swf","application/x-shockwave-flash"},{".wav","audio/x-wav"},
			{".zip","application/zip"},{".rar","application/x-rar-compressed"},
			{".rm","audio/x-pn-realaudio"},{".rmvb","audio/x-pn-realaudio"}
		};
		String type="*/*";
		String end=getSuffix(file);
		if (end.equals("")) return type;
		for (int i=0;i<MIME_TABLE.length;i++) {
			if (end.equals(MIME_TABLE[i][0])) type=MIME_TABLE[i][1];
		}
		return type;
	}
	
	private static String getSuffix(File file) {
		String fname=file.getName();
		int doIndex=fname.lastIndexOf(".");
		if (doIndex<0) return "";
		return fname.substring(doIndex, fname.length()).toLowerCase(Locale.getDefault());
	}
	
}
