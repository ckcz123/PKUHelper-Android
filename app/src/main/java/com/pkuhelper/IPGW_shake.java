package com.pkuhelper;

import android.app.AlertDialog;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.Lib;
import com.pkuhelper.lib.RequestingTask;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IPGW_shake extends Fragment {
	static View ipgwView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.ipgw_shake_view,
				container, false);
		ipgwView = rootView;
		ipgwView.findViewById(R.id.ipgw_image_shake)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						connect();
					}
				});
		ipgwView.findViewById(R.id.ipgw_disconnect)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						disconnect();
					}
				});
		ipgwView.findViewById(R.id.ipgw_disconnectall)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						disconnectall();
					}
				});
		return rootView;
	}

	public static void disconnect() {
		doConnection("disconnect", Constants.REQUEST_ITS_DISCONNECT, "正在断开连接");
	}

	public static void disconnectall() {
		doConnection("disconnectall", Constants.REQUEST_ITS_DISCONNECT_ALL, "正在断开全部连接");
	}

	public static void connect() {
		doConnection("connect", Constants.REQUEST_ITS_CONNECT, "正在连接，请稍候");
	}

	@SuppressWarnings("unchecked")
	private static void doConnection(String type, int constantType, String hintString) {
		if (!Constants.isLogin()) {
			IAAA.showLoginView();
			return;
		}

		int free = ViewSetting.getSwitchChecked(ipgwView, R.id.ipgw_switch) ? 1 : 2;
		ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
		arrayList.add(new Parameters("uid", Constants.username));
		arrayList.add(new Parameters("password", Constants.password));
		arrayList.add(new Parameters("operation", type));
		arrayList.add(new Parameters("range", free + ""));
		arrayList.add(new Parameters("timeout", "-1"));

		new RequestingTask(PKUHelper.pkuhelper, hintString,
				"https://its.pku.edu.cn:5428/ipgatewayofpku", constantType)
				.execute(arrayList);
	}

	public static void finishConnection(int type, String msg) {
		Map<String, String> map = getReturnMsg(msg);
		if (!map.containsKey("SUCCESS")) {
			CustomToast.showErrorToast(PKUHelper.pkuhelper, "网络连接失败，请重试");
			return;
		}
		String successmsg = map.get("SUCCESS");
		boolean success = "YES".equals(successmsg);

		if (type == Constants.REQUEST_ITS_CONNECT) {
			if (success) {
				//Hint.show("连接成功......");

				String string = "";
				string += "姓名：\t\t" + Constants.name + "\n";
				string += "学号：\t\t" + Constants.username + "\n";
				string += "IP：\t\t" + map.get("IP") + "\n";
				String scope = "";
				if ("domestic".equals(map.get("SCOPE")))
					scope = "免费地址";
				else if ("international".equals(map.get("SCOPE")))
					scope = "收费地址";
				string += "连接状态：\t" + scope + "\n";
				string += "连接数目：\t" + map.get("CONNECTIONS") + "\n";
				string += "包月状态：\t" + map.get("FR_DESC_CN") + "\n";
				string += "已用时长：\t" + map.get("FR_TIME") + "\n";
				string += "账户余额：\t" + map.get("BALANCE") + "\n";
				new AlertDialog.Builder(PKUHelper.pkuhelper).setTitle("连接成功！")
						.setMessage(string).setCancelable(true).setPositiveButton("关闭", null).show();

				Lib.sendStatistics(PKUHelper.pkuhelper);
			} else {
				new AlertDialog.Builder(PKUHelper.pkuhelper).setTitle("连接失败！")
						.setMessage(map.get("REASON")).setCancelable(true).
						setPositiveButton("关闭", null).show();
			}
			return;
		}
		if (type == Constants.REQUEST_ITS_DISCONNECT) {
			if (success) {
				CustomToast.showSuccessToast(PKUHelper.pkuhelper, "连接断开成功！");
			} else {
				new AlertDialog.Builder(PKUHelper.pkuhelper).setTitle("连接断开失败！")
						.setMessage(map.get("REASON")).setCancelable(true).
						setPositiveButton("关闭", null).show();
			}
			return;
		}
		if (type == Constants.REQUEST_ITS_DISCONNECT_ALL) {
			if (success) {
				CustomToast.showSuccessToast(PKUHelper.pkuhelper, "全部连接断开成功！");
			} else {
				new AlertDialog.Builder(PKUHelper.pkuhelper).setTitle("全部连接断开失败！")
						.setMessage(map.get("REASON")).setCancelable(true).
						setPositiveButton("关闭", null).show();
			}
			return;
		}
	}

	private static Map<String, String> getReturnMsg(String string) {
		Map<String, String> map = new HashMap<String, String>();
		int pos1 = string.indexOf("SUCCESS=");
		int pos2 = string.indexOf("IPGWCLIENT_END-->");

		String msg = string.substring(pos1, pos2 - 1);
		Log.i("IPGWReturnMsg", msg);

		String[] strings = msg.split(" ");
		for (int i = 0; i < strings.length; i++) {
			String str = strings[i];
			str.trim();
			if (!str.contains("=")) continue;
			String[] strings2 = str.split("=");
			if (strings2.length != 1)
				map.put(strings2[0], strings2[1]);
			else map.put(strings2[0], "");
		}

		return map;
	}

}
