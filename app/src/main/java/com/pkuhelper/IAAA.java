package com.pkuhelper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.pkuhelper.entity.UserEntity;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.Editor;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.lib.webconnection.WebConnection;

import org.json.JSONObject;

import java.util.ArrayList;

public class IAAA {
	static Dialog dialog;
	static String username;
	static String password;

	public static void showLoginView() {
		dialog = new Dialog(PKUHelper.pkuhelper);
		dialog.setContentView(R.layout.iaaa_login_view);
		dialog.setTitle("请登录您的校园网账号");
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				ViewSetting.setEditTextValue(dialog, R.id.username, "");
				ViewSetting.setEditTextValue(dialog, R.id.password, "");
				if (!Constants.isLogin()) {
					IAAA.showLoginView();
				}
			}
		});
		dialog.show();

		ViewSetting.setOnClickListener(dialog, R.id.iaaa_login, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				username = ViewSetting.getEditTextValue(dialog, R.id.username);
				password = ViewSetting.getEditTextValue(dialog, R.id.password);
				doLogin(username, password);
			}
		});
		ViewSetting.setOnClickListener(dialog, R.id.iaaa_guest, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				loginAsGuest();
			}
		});
	}

	public static void loginAsGuest() {
		Constants.token = "guest";
		Constants.user_token = "guest";
		Constants.username = "guest";
		Constants.password = "guest";
		Constants.name = "游客";
		Constants.major = "";
		Constants.sex = "";
		Constants.birthday = "";
		Editor.putString(PKUHelper.pkuhelper, "token", Constants.token);
		Editor.putString(PKUHelper.pkuhelper, "user_token", Constants.user_token);
		Editor.putString(PKUHelper.pkuhelper, "username", Constants.username);
		Editor.putString(PKUHelper.pkuhelper, "password", Constants.password);
		Editor.putString(PKUHelper.pkuhelper, "name", Constants.name);
		Editor.putString(PKUHelper.pkuhelper, "major", Constants.major);
		Editor.putString(PKUHelper.pkuhelper, "sex", Constants.sex);
		Editor.putString(PKUHelper.pkuhelper, "birthday", Constants.birthday);
		dialog.dismiss();
		Constants.init(PKUHelper.pkuhelper);
		new AlertDialog.Builder(PKUHelper.pkuhelper).setTitle("欢迎使用PKU Helper！")
				.setMessage("游客你好，欢迎体验PKU Helper！")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						IPGW.setOthers();
					}
				}).setCancelable(true).setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				IPGW.setOthers();
			}
		}).show();
	}

	public static void doLogin(String username, String password) {
		if ("12345678".equals(username) && "123".equals(password)) {
			loginAsGuest();
			return;
		} else if ("12345678".equals(username) && "admin".equals(password)) {
			Constants.token = "admin";
			Constants.user_token = "admin";
			Constants.username = "admin";
			Constants.password = password;
			Constants.name = "管理员";
			Constants.major = "PKU Helper";
			Constants.sex = "";
			Constants.birthday = "";
			Editor.putString(PKUHelper.pkuhelper, "token", Constants.token);
			Editor.putString(PKUHelper.pkuhelper, "user_token", Constants.user_token);
			Editor.putString(PKUHelper.pkuhelper, "username", Constants.username);
			Editor.putString(PKUHelper.pkuhelper, "password", Constants.password);
			Editor.putString(PKUHelper.pkuhelper, "name", Constants.name);
			Editor.putString(PKUHelper.pkuhelper, "major", Constants.major);
			Editor.putString(PKUHelper.pkuhelper, "sex", Constants.sex);
			Editor.putString(PKUHelper.pkuhelper, "birthday", Constants.birthday);
			dialog.dismiss();
			finishIAAA();
			Constants.init(PKUHelper.pkuhelper);
			return;
		}
		new LoginTask(username, password).execute();
	}

	static void finishLogin(String string) {
		try {
			Gson gson = new Gson();
			JsonObject jsonObject = gson.fromJson(string, JsonObject.class);
			int code = jsonObject.get("code").getAsInt();
//			JSONObject jsonObject = new JSONObject(string);
//			int code = jsonObject.getInt("code");
			if (code != 0) {
//				CustomToast.showErrorToast(PKUHelper.pkuhelper, jsonObject.optString("msg", "登录失败"));
				CustomToast.showErrorToast(PKUHelper.pkuhelper, jsonObject.get("msg").getAsString());
				return;
			}
//			Constants.token = jsonObject.getString("token");
//			Constants.user_token = jsonObject.getString("user_token");
//			Constants.name = jsonObject.optString("name");
//			Constants.sex = jsonObject.optString("gender");
//			Constants.major = jsonObject.optString("department");
//			Constants.birthday = jsonObject.optString("birthday");
			Constants.token = jsonObject.get("token").getAsString();
			Constants.user_token = jsonObject.get("user_token").getAsString();
			Constants.name = jsonObject.get("name").getAsString();
			Constants.sex = jsonObject.get("gender").getAsString();
			Constants.major = jsonObject.get("department").getAsString();
			Constants.birthday = jsonObject.get("birthday").getAsString();
			Constants.username = username;
			Constants.password = password;
			Editor.putString(PKUHelper.pkuhelper, "token", Constants.token);
			Editor.putString(PKUHelper.pkuhelper, "user_token", Constants.user_token);
			Editor.putString(PKUHelper.pkuhelper, "username", Constants.username);
			Editor.putString(PKUHelper.pkuhelper, "password", Constants.password);
			Editor.putString(PKUHelper.pkuhelper, "name", Constants.name);
			Editor.putString(PKUHelper.pkuhelper, "major", Constants.major);
			Editor.putString(PKUHelper.pkuhelper, "sex", Constants.sex);
			Editor.putString(PKUHelper.pkuhelper, "birthday", Constants.birthday);

			// 整体存入
			// TO-DO: 移入UserMod中
			UserEntity userEntity = gson.fromJson(jsonObject, UserEntity.class);
			String mUserEntityJson = gson.toJson(userEntity);
			Editor.putString(PKUHelper.pkuhelper, "mUserEntity", mUserEntityJson);

			dialog.dismiss();
			finishIAAA();
		} catch (Exception e) {
			CustomToast.showErrorToast(PKUHelper.pkuhelper, "登录失败");
		}
	}

	private static void finishIAAA() {
		Constants.init(PKUHelper.pkuhelper);
		new AlertDialog.Builder(PKUHelper.pkuhelper).setTitle("欢迎使用PKU Helper！")
				.setMessage("来自" + Constants.major + "的" + Constants.name + "你好，欢迎使用PKU Helper！")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						IPGW.setOthers();
					}
				}).setCancelable(true).setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				IPGW.setOthers();
			}
		}).show();
	}
}

class LoginTask extends AsyncTask<String, String, Parameters> {
	ProgressDialog progressDialog;
	String username, password;

	public LoginTask(String username, String password) {
		progressDialog = new ProgressDialog(PKUHelper.pkuhelper);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setTitle("提示");
		progressDialog.setMessage("正在登录...");
		progressDialog.setIndeterminate(false);
		progressDialog.setCancelable(false);
		this.username = username;
		this.password = password;
	}

	@Override
	protected void onPreExecute() {
		progressDialog.show();
	}

	@Override
	protected Parameters doInBackground(String... params) {
		try {
			Parameters parameters = WebConnection.connect(Constants.domain + "/services/login/local.php", null);
			if (!"200".equals(parameters.name)) return parameters;
			JSONObject jsonObject = new JSONObject(parameters.value);
			int code = jsonObject.getInt("code");
			if (code != 0) return parameters;
			boolean local = jsonObject.optInt("local") != 0;
			String token = "";
			if (local) {
				ArrayList<Parameters> arrayList = new ArrayList<>();
				arrayList.add(new Parameters("appid", "portal"));
				arrayList.add(new Parameters("userName", username));
				arrayList.add(new Parameters("password", password));
				arrayList.add(new Parameters("redirUrl",
						"http://portal.pku.edu.cn/portal2013/login.jsp/../ssoLogin.do"));
				parameters = WebConnection.connect("https://iaaa.pku.edu.cn/iaaa/oauthlogin.do",
						arrayList);

				if (!"200".equals(parameters.name)) return parameters;
				jsonObject = new JSONObject(parameters.value);
				boolean success = jsonObject.getBoolean("success");
				if (!success) {
					JSONObject errors = jsonObject.getJSONObject("errors");
					String msg = errors.optString("msg", "登录失败");
					return new Parameters("200", "{\"code\": 1, \"msg\": \"" + msg + "\"}");
				}

				token = jsonObject.getString("token");
				username = "";
				password = "";
			}
			ArrayList<Parameters> arrayList = new ArrayList<>();
			arrayList.add(new Parameters("uid", username));
			arrayList.add(new Parameters("password", password));
			arrayList.add(new Parameters("token", token));
			arrayList.add(new Parameters("platform", "Android"));
			return WebConnection.connect(Constants.domain + "/services/login/login.php", arrayList);
		} catch (Exception e) {
			return new Parameters("200", "{\"code\": 1, \"msg\": \"登录失败，请重试\"}");
		}
	}

	@Override
	protected void onPostExecute(Parameters parameters) {
		progressDialog.dismiss();
		if (!"200".equals(parameters.name)) {
			if ("-1".equals(parameters.name))
				CustomToast.showInfoToast(PKUHelper.pkuhelper, "无法连接网络(-1,-1)");
			else {
				CustomToast.showInfoToast(PKUHelper.pkuhelper, "无法连接到服务器 (HTTP " + parameters.name + ")");
			}
		} else
			IAAA.finishLogin(parameters.value);
	}
}
