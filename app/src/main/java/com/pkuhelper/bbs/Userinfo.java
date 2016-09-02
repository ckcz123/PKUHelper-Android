package com.pkuhelper.bbs;

import android.app.Dialog;
import android.os.Message;
import android.view.View;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.Editor;
import com.pkuhelper.lib.RequestingTask;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.BadgeView;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.lib.webconnection.WebConnection;

import org.json.JSONObject;

import java.util.ArrayList;

public class Userinfo {
	static String username, password, token, nickname;
	static int numposts, numlogins, life, message;
	static long staytime, createtime, lasttime;
	static boolean hasNewMsg = false;
	static Dialog dialog;
	static boolean givehint = false;

	public static void load() {
		username = Editor.getString(BBSActivity.bbsActivity, "bbs_username");
		password = Editor.getString(BBSActivity.bbsActivity, "bbs_password");
		token = nickname = "";
		numposts = numlogins = life = 0;
		staytime = createtime = lasttime = 0;
		autoLogin();
	}

	public static void autoLogin() {
		if ("".equals(username)) return;
		new Thread(new Runnable() {
			public void run() {
				ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
				arrayList.add(new Parameters("type", "login"));
				arrayList.add(new Parameters("username", username));
				arrayList.add(new Parameters("password", password));
				Parameters parameters = WebConnection.connect(Constants.bbsurl, arrayList);
				if ("200".equals(parameters.name)) {
					BBSActivity.bbsActivity.handler.sendMessage(Message.obtain(
							BBSActivity.bbsActivity.handler, Constants.MESSAGE_BBS_LOGIN, parameters.value));
				}
			}
		}).start();
		givehint = false;
	}

	@SuppressWarnings("unchecked")
	public static void login() {
		if ("".equals(username)) {
			showLoginView();
			return;
		}
		ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
		arrayList.add(new Parameters("type", "login"));
		arrayList.add(new Parameters("username", username));
		arrayList.add(new Parameters("password", password));
		new RequestingTask(BBSActivity.bbsActivity, "正在登录 ...", Constants.bbsurl,
				Constants.REQUEST_BBS_LOGIN).execute(arrayList);
		givehint = true;
	}

	public static void showLoginView() {
		username = Editor.getString(BBSActivity.bbsActivity, "bbs_username");
		password = Editor.getString(BBSActivity.bbsActivity, "bbs_password");

		dialog = new Dialog(BBSActivity.bbsActivity);
		dialog.setContentView(R.layout.bbs_login_view);
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(true);
		dialog.setTitle("登录未名bbs");
		ViewSetting.setEditTextValue(dialog, R.id.username, username);
		ViewSetting.setEditTextValue(dialog, R.id.password, password);
		ViewSetting.setOnClickListener(dialog, R.id.bbs_login, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String u = ViewSetting.getEditTextValue(dialog, R.id.username).trim(),
						p = ViewSetting.getEditTextValue(dialog, R.id.password).trim();
				if ("".equals(u) || "".equals(p)) {
					CustomToast.showInfoToast(BBSActivity.bbsActivity, "账号或密码不能为空！", 1500);
					return;
				}
				username = u;
				password = p;
				login();
			}
		});
		ViewSetting.setOnClickListener(dialog, R.id.bbs_cancel, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	public static void finishLogin(String string) {
		try {
			JSONObject jsonObject = new JSONObject(string);
			int code = jsonObject.getInt("code");
			if (code != 0) {
				CustomToast.showErrorToast(BBSActivity.bbsActivity, jsonObject.optString("msg", "登录失败"));
				return;
			}
			Editor.putString(BBSActivity.bbsActivity, "bbs_username", username);
			Editor.putString(BBSActivity.bbsActivity, "bbs_password", password);
			token = jsonObject.optString("token");
			nickname = jsonObject.optString("nickname");
			numposts = jsonObject.optInt("numposts");
			numlogins = jsonObject.optInt("numlogins");
			life = jsonObject.optInt("life");
			staytime = jsonObject.optLong("staytime");
			createtime = jsonObject.optLong("createtime") * 1000;
			lasttime = jsonObject.optLong("lasttime") * 1000;
			message = jsonObject.optInt("mail");
			hasNewMsg = jsonObject.optInt("newmail", 0) == 1;
			try {
				dialog.dismiss();
			} catch (Exception e) {
			}
			if (givehint)
				CustomToast.showSuccessToast(BBSActivity.bbsActivity, "登录成功！", 1000);
			givehint = false;
			if (hasNewMsg)
				BadgeView.show(BBSActivity.bbsActivity, BBSActivity.bbsActivity.findViewById(R.id.bbs_bottom_img_me)
						, "new");
			UserinfoFragment.set();
		} catch (Exception e) {
			CustomToast.showErrorToast(BBSActivity.bbsActivity, "登录失败");
		}

	}

	public static void logout() {
		Editor.putString(BBSActivity.bbsActivity, "bbs_username", "");
		Editor.putString(BBSActivity.bbsActivity, "bbs_password", "");
		username = password = token = nickname = "";
		numposts = numlogins = life = 0;
		staytime = createtime = lasttime = 0;
		message = 0;
		hasNewMsg = false;
		BadgeView.show(BBSActivity.bbsActivity,
				BBSActivity.bbsActivity.findViewById(R.id.bbs_bottom_img_me), "");
	}

}
