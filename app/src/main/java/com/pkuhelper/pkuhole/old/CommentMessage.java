package com.pkuhelper.pkuhole.old;

import android.app.Dialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.RequestingTask;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentMessage {
	static Dialog dialog;
	static String savedString = "";

	public static void commentMessage() {
		showDialog();
	}

	public static void commentMessage(String username) {
		savedString = "回复" + username + ": ";
		showDialog();
	}

	private static void showDialog() {
		if (!Constants.isValidLogin()) {
			CustomToast.showInfoToast(PKUHoleActivity.pkuHoleActivity, "请有效登录，才能匿名回复树洞！");
			return;
		}
		dialog = new Dialog(PKUHoleActivity.pkuHoleActivity);
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setTitle("回复树洞");
		dialog.setContentView(R.layout.pkuhole_dialog_postmsg);
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				savedString = ViewSetting.getEditTextValue(dialog, R.id.pkuhole_newmsg_text);
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
		String string = ViewSetting.getEditTextValue(dialog, R.id.pkuhole_newmsg_text);
		string += " //from PKU Helper";
		int id = PKUHoleActivity.pkuHoleActivity.currId;
		String mid = PKUHoleActivity.pkuHoleActivity.messageList.get(id).id;
		String url = "";
		if (string.length() > 1000) {
			CustomToast.showInfoToast(PKUHoleActivity.pkuHoleActivity, "不要超过1000字！",
					1500);
			return;
		}
		Pattern pattern = Pattern.compile("(.+)\\1{7}");
		Matcher matcher = pattern.matcher(string);
		if (matcher.find()) {
			CustomToast.showInfoToast(PKUHoleActivity.pkuHoleActivity, "请不要刷屏！",
					1500);
			return;
		}
		ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
		arrayList.add(new Parameters("message", string));
		arrayList.add(new Parameters("mid", mid));
		url = "http://pkuhole.sinaapp.com/PKUhelper/addComment.php";
		new RequestingTask(PKUHoleActivity.pkuHoleActivity, "正在发布中...", url, Constants.REQUEST_PKUHOLE_POST_COMMENT)
				.execute(arrayList);
	}

	public static void finishRequest(String string) {
		string = string.trim();
		Log.i("commentMsg", "1" + string);
		String msg = string.trim();

		if ("回复成功".equals(msg)) {
			ShowComments.showComments(PKUHoleActivity.pkuHoleActivity.currId);
			savedString = "";
			CustomToast.showSuccessToast(PKUHoleActivity.pkuHoleActivity, "回复成功");
			return;
		}

		showDialog();
		CustomToast.showErrorToast(PKUHoleActivity.pkuHoleActivity, msg);
	}

}
