package com.pkuhelper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.Editor;
import com.pkuhelper.lib.RequestingTask;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.subactivity.SubActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class PE {
	@SuppressWarnings("unchecked")
	public static void peCard() {
		if (!Constants.isLogin()) {
			IAAA.showLoginView();
			return;
		}
		ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
		String proxypsd = Base64.encodeToString(Constants.password.getBytes(), Base64.DEFAULT);
		String password = Base64.encodeToString(Constants.username.getBytes(), Base64.DEFAULT);
		arrayList.add(new Parameters("uid", Constants.username));
		arrayList.add(new Parameters("proxypsd", proxypsd));
		arrayList.add(new Parameters("password", password));
		new RequestingTask(PKUHelper.pkuhelper, "正在获取打卡信息...",
				Constants.domain + "/services/pkuhelper/pecard.php",
				Constants.REQUEST_PE_CARD).execute(arrayList);
	}

	public static void finishPeCardRequest(String string) {
		try {
			JSONObject jsonObject = new JSONObject(string);
			String success = jsonObject.getString("success");
			if (!"1".equals(success)) {
				new AlertDialog.Builder(PKUHelper.pkuhelper).setTitle("打卡信息获取失败")
						.setMessage(jsonObject.optString("reason", "未知错误。"))
						.setCancelable(true).setNegativeButton("确定", null).show();
				return;
			}
			JSONObject data = jsonObject.getJSONObject("data");
			ArrayList<HashMap<String, String>> list = getList(data);

			if (list == null) {
				new AlertDialog.Builder(PKUHelper.pkuhelper).setTitle("打卡信息")
						.setMessage("你没有注册打卡！")
						.setCancelable(true).setNegativeButton("确定", null).show();
				return;
			}


			Dialog dialog = new Dialog(PKUHelper.pkuhelper);
			dialog.setTitle("打卡信息");
			dialog.setContentView(R.layout.pe_card_dialog_view);
			ListView listView = (ListView) dialog.findViewById(R.id.pecard_view);
			if (listView == null) throw new Exception();
			listView.setAdapter(new SimpleAdapter(PKUHelper.pkuhelper,
					list, R.layout.pe_card_dialog_item,
					new String[]{"name", "value"},
					new int[]{R.id.pecard_name, R.id.pecard_value}));
			dialog.setCancelable(true);
			dialog.setCanceledOnTouchOutside(true);
			dialog.show();
			return;
		} catch (Exception e) {
			CustomToast.showErrorToast(PKUHelper.pkuhelper, "打卡信息解析失败");
		}
	}

	private static ArrayList<HashMap<String, String>> getList(JSONObject jsonObject)
			throws Exception {
		JSONArray jsonArray = jsonObject.names();
		ArrayList<HashMap<String, String>> hashMaps = new ArrayList<HashMap<String, String>>();
		int len = jsonArray.length();
		boolean hasValue = false;
		for (int i = 0; i < len; i++) {
			String name = jsonArray.getString(i);
			String value = jsonObject.getString(name);
			if (!"-1".equals(value.trim())) hasValue = true;
			HashMap<String, String> hashMap = new HashMap<String, String>();
			hashMap.put("name", name);
			hashMap.put("value", value);
			hashMaps.add(hashMap);
		}
		if (hasValue)
			return hashMaps;
		return null;
	}

	@SuppressWarnings("unchecked")
	public static void getPeTestScore() {
		if (!Constants.isLogin()) {
			IAAA.showLoginView();
			return;
		}
		String pepassword = Editor.getString(PKUHelper.pkuhelper, "pepass_" + Constants.username);
		if ("".equals(pepassword)) {
			String s = new String(Constants.birthday);
			s.replace("-", "");
			if (s.matches("\\d{8}")) {
				pepassword = new String(s);
			}
		}

		if ("".equals(pepassword)) {

			new AlertDialog.Builder(PKUHelper.pkuhelper).setTitle("提示")
					.setMessage("初次使用，请设置自己的体测密码。（一般为8位生日）\n你可以在设置中修改自己的体测密码。")
					.setCancelable(true).setPositiveButton("设置", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					setPeTestPassword();
				}
			}).setNegativeButton("取消", null).show();
			return;
		}

		String proxypsd = Base64.encodeToString(Constants.password.getBytes(), Base64.DEFAULT);
		String password = Base64.encodeToString(pepassword.getBytes(), Base64.DEFAULT);

		ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
		arrayList.add(new Parameters("uid", Constants.username));
		arrayList.add(new Parameters("proxypsd", proxypsd));
		arrayList.add(new Parameters("password", password));

		new RequestingTask(PKUHelper.pkuhelper, "正在获取体测成绩...",
				Constants.domain + "/services/pkuhelper/petest.php", Constants.REQUEST_PE_TEST)
				.execute(arrayList);
	}

	public static void setPeTestPassword() {
		if (!Constants.isLogin()) {
			IAAA.showLoginView();
			return;
		}
		final Dialog dialog = new Dialog(PKUHelper.pkuhelper);
		dialog.setContentView(R.layout.pe_password_dialog);
		dialog.setTitle("输入体测查询密码");
		ViewSetting.setOnClickListener(dialog, R.id.pepassword_change, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				EditText userEditText = (EditText) dialog.findViewById(R.id.pepassword);
				String string = userEditText.getEditableText().toString();
				if ("".equals(string)) {
					CustomToast.showErrorToast(PKUHelper.pkuhelper, "体测密码不能为空");
					return;
				}
				Editor.putString(PKUHelper.pkuhelper, "pepass_" + Constants.username, string);
				CustomToast.showSuccessToast(PKUHelper.pkuhelper, "修改成功！");
				dialog.dismiss();
			}
		});
		ViewSetting.setOnClickListener(dialog, R.id.pepassword_cancel, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	public static void finishPeTestRequest(String string) {
		try {
			JSONObject jsonObject = new JSONObject(string);
			String success = jsonObject.getString("success");
			if (!"1".equals(success)) {
				new AlertDialog.Builder(PKUHelper.pkuhelper).setTitle("体测成绩获取失败")
						.setMessage(jsonObject.optString("reason").trim())
						.setCancelable(true).setNegativeButton("确定", null).show();
				return;
			}
			String html = jsonObject.getString("data");
			Intent intent = new Intent(PKUHelper.pkuhelper, SubActivity.class);
			intent.putExtra("type", Constants.SUBACTIVITY_TYPE_WEBVIEW_HTML);
			intent.putExtra("html", html);
			intent.putExtra("title", "体测成绩");
			PKUHelper.pkuhelper.startActivity(intent);
			return;
		} catch (Exception e) {
			CustomToast.showErrorToast(PKUHelper.pkuhelper, "体测成绩解析失败");
		}


	}


}
