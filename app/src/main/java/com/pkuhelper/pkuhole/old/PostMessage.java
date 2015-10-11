package com.pkuhelper.pkuhole.old;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;

import android.app.Dialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;

public class PostMessage {
	static Dialog dialog;
	static String savedString="";
	
	public static void postMessage() {
		if (!Constants.isValidLogin()) {
			CustomToast.showInfoToast(PKUHoleActivity.pkuHoleActivity, "请先有效登录，才能匿名发表树洞！");
			return;
		}
		//IsInSchool.checkIfInSchool();
		dialog=new Dialog(PKUHoleActivity.pkuHoleActivity);
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setTitle("发表新树洞");
		dialog.setContentView(R.layout.pkuhole_dialog_postmsg);
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				savedString=ViewSetting.getEditTextValue(dialog, R.id.pkuhole_newmsg_text);
			}
		});
		ViewSetting.setEditTextValue(dialog, R.id.pkuhole_newmsg_text, savedString);
		ViewSetting.setOnClickListener(dialog, R.id.pkuhole_newmsg_post, new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.cancel();
				realPostMsg();
			}
		});
		ViewSetting.setOnClickListener(dialog, R.id.pkuhole_newmsg_reset, new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ViewSetting.setEditTextValue(dialog, R.id.pkuhole_newmsg_text, "");
			}
		});
		ViewSetting.setOnClickListener(dialog, R.id.pkuhole_newmsg_cancel, new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});
		dialog.show();
	}
	
	@SuppressWarnings("unchecked")
	public static void realPostMsg() {
		String string=ViewSetting.getEditTextValue(dialog, R.id.pkuhole_newmsg_text);
		String url="";
		ArrayList<Parameters> arrayList=new ArrayList<Parameters>();
		if (string.length()>1000) {
			CustomToast.showInfoToast(PKUHoleActivity.pkuHoleActivity, "不要超过1000字！", 
					1500);
			return;
		}
		Pattern pattern=Pattern.compile("(.+)\\1{7}");
		Matcher matcher=pattern.matcher(string);
		if (matcher.find()) {
			CustomToast.showInfoToast(PKUHoleActivity.pkuHoleActivity, "请不要刷屏！", 
					1500);
			return;
		}
		
		arrayList.add(new Parameters("message", string));
		url="http://pkuhole.sinaapp.com/PKUhelper/post.php";
		new RequestingTask("正在发布中...", url, Constants.REQUEST_PKUHOLE_POST_MESSAGE)
		.execute(arrayList);
		
	}

	public static void finishRequest(String string) {
		string=string.trim();
		Log.i("postMsg",string);
		String msg=string;
		if ("".equals(msg)) {
			ShowLists.showPage(true);
			savedString="";
			CustomToast.showSuccessToast(PKUHoleActivity.pkuHoleActivity, "发布成功");
			return;
		}
		postMessage();
		CustomToast.showErrorToast(PKUHoleActivity.pkuHoleActivity, msg);
	}

}
