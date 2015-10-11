package com.pkuhelper;

import java.util.*;

import com.pkuhelper.grade.GradeActivity;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.DeanDecode;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.webconnection.*;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.*;
/**
 * 获取验证码；登录教务获取phpsessid，并执行相应的操作
 * @author oc
 */
public class Dean {
	public static final int FLAG_NONE=0;
	public static final int FLAG_GETTING_GRADE=1;
	public static final int FLAG_GETTING_COURSE=2;
	
	public static int flag=FLAG_NONE;
	
	public static Handler handler=null;	
	
	public static Dialog dialog;
	public static void getSessionId(int _flag) {
		handler=new Handler(PKUHelper.pkuhelper.getMainLooper(), new Handler.Callback() {
			public boolean handleMessage(Message msg) {
				if (msg.what==Constants.MESSAGE_DEAN_PICTURE_FINISHED) {
					setPicture((Drawable)msg.obj);
					return true;
				}
				if (msg.what==Constants.MESSAGE_DEAN_PICTURE_FAILED) {
					setPicture(PKUHelper.pkuhelper.getResources().getDrawable(R.drawable.failure));
					return true;
				}
				if (msg.what==Constants.MESSAGE_DEAN_DECODE_FINISHED) {
					setInput((String)msg.obj);
					return true;
				}
				return false;
			}
		});
		
		flag=_flag;
		if (!Constants.isLogin()) {
			IAAA.showLoginView();
			return;
		}
		dialog=new Dialog(PKUHelper.pkuhelper);
		dialog.setContentView(R.layout.dean_captcha_view);
		dialog.setTitle("请输入验证码");
		dialog.setCancelable(true);
		refreshPicture();
		ViewSetting.setOnClickListener(dialog, R.id.dean_captcha_connect, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				connect();
			}
		});
		ViewSetting.setOnClickListener(dialog, R.id.dean_captcha_refresh, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshPicture();
			}
		});
		ViewSetting.setOnClickListener(dialog, R.id.dean_captcha_cancel, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	private static void refreshPicture() {
		EditText editText=(EditText)dialog.findViewById(R.id.dean_captcha_input);
		if (editText==null) return;
		editText.setText("");
		/*
		new ImageRequest(PKUHelper.pkuhelper, 
				(ImageView)dialog.findViewById(R.id.dean_captcha_image), 
				"http://dean.pku.edu.cn/student/yanzheng.php?act=init")
			.requestAndSetImages();
		*/
		new Thread(new Runnable() {
			public void run() {
				try {
					Drawable drawable=Drawable.createFromStream(
							WebConnection.connect("http://dean.pku.edu.cn/student/yanzheng.php?act=init"), 
							"dean");
					handler.sendMessage(Message.obtain(handler, Constants.MESSAGE_DEAN_PICTURE_FINISHED, drawable));
				}
				catch (Exception e) {
					handler.sendEmptyMessage(Constants.MESSAGE_DEAN_PICTURE_FAILED);
				}
			}
		}).start();
	}

	private static void setPicture(final Drawable drawable) {
		ViewSetting.setImageDrawable(dialog, R.id.dean_captcha_image, drawable);
		new Thread(new Runnable() {
			public void run() {
				String input=DeanDecode.decode(drawable);
				if (!"".equals(input))
					handler.sendMessage(Message.obtain(handler, Constants.MESSAGE_DEAN_DECODE_FINISHED, input));
			}
		}).start();
	}
	
	private static void setInput(String string) {
		ViewSetting.setEditTextValue(dialog, R.id.dean_captcha_input, 
				string);
	}
	
	@SuppressWarnings("unchecked")
	public static void connect() {
		String captcha=ViewSetting.getEditTextValue(dialog, R.id.dean_captcha_input).trim();
		
		ArrayList<Parameters> arrayList=new ArrayList<Parameters>();
		arrayList.add(new Parameters("sno", Constants.username));
		arrayList.add(new Parameters("password", Constants.password));
		arrayList.add(new Parameters("captcha", captcha));
		new RequestingTask("正在登录教务...", "http://dean.pku.edu.cn/student/authenticate.php"
				, Constants.REQUEST_DEAN_LOGIN).execute(arrayList);
		
	}
	
	public static void finishLogin(String string) {
		if (string.contains("alert")) {
			refreshPicture();
			int pos=string.indexOf("alert(");
			string=string.substring(pos);
			pos=string.indexOf("\"");
			string=string.substring(pos+1);
			int pos2=string.indexOf("\"");
			string=string.substring(0,pos2);
			new AlertDialog.Builder(PKUHelper.pkuhelper).setTitle("提示")
			.setMessage(string).setPositiveButton("确定", null).show();
			return;
		}
		dialog.dismiss();
		String sessionId="";
		int pos=string.indexOf("PHPSESSID");
		string=string.substring(pos);
		pos=string.indexOf("=");
		string=string.substring(pos+1);
		int pos2=string.indexOf("\"");
		string=string.substring(0, pos2);
		sessionId=string;
		Constants.phpsessid=sessionId;
		
		int tmpflag=flag;
		flag=FLAG_NONE;
		
		if (tmpflag==FLAG_GETTING_GRADE)
			getGrade();
		else if (tmpflag==FLAG_GETTING_COURSE)
			getTimetable();
	}
	
	private static void getGrade() {
		Intent intent=new Intent();
		intent.setClass(PKUHelper.pkuhelper, GradeActivity.class);
		intent.putExtra("phpsessid", Constants.phpsessid);
		PKUHelper.pkuhelper.startActivity(intent);
	}
	
	private static void getTimetable(){
		Course.getCourses();
	}
	
}
