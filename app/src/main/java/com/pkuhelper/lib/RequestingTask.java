package com.pkuhelper.lib;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.lib.webconnection.WebConnection;

import java.util.ArrayList;

public class RequestingTask extends AsyncTask<ArrayList<Parameters>, String, Parameters> {
	ProgressDialog progressDialog;
	String requestString;
	int requestType;
	BaseActivity baseActivity;
	int encodingType=-1;

	/**
	 * 发起一个http调用；如果访问失败那么直接Toast提醒，无返回
	 *
	 * @param msg  提示消息
	 * @param url  请求地址
	 * @param type 访问类型
	 */
	public RequestingTask(BaseActivity activity, String msg, String url, int type) {
		baseActivity = activity;
		progressDialog = new ProgressDialog(activity);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setTitle("提示");
		progressDialog.setMessage(msg);
		progressDialog.setIndeterminate(false);
		progressDialog.setCancelable(false);
		requestString = url;
		requestType = type;
		encodingType=-1;
	}

	/**
	 * 发起一个http调用；如果访问失败那么直接Toast提醒，无返回
	 *
	 * @param msg  提示消息
	 * @param url  请求地址
	 * @param type 访问类型
	 * @param encodingType 编码类型
	 */
	public RequestingTask(BaseActivity activity, String msg, String url, int type, int encodingType) {
		baseActivity = activity;
		progressDialog = new ProgressDialog(activity);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setTitle("提示");
		progressDialog.setMessage(msg);
		progressDialog.setIndeterminate(false);
		progressDialog.setCancelable(false);
		requestString = url;
		requestType = type;
		this.encodingType=encodingType;
	}

	@Override
	protected void onPreExecute() {
		progressDialog.show();
	}

	@Override
	protected Parameters doInBackground(ArrayList<Parameters>... params) {
		if (encodingType!=-1)
			return WebConnection.connect(requestString, params[0], encodingType);
		return WebConnection.connect(requestString, params[0]);
	}

	@Override
	protected void onPostExecute(Parameters parameters) {
		progressDialog.dismiss();
		if (!"200".equals(parameters.name)) {
			if ("-1".equals(parameters.name)) {
				CustomToast.showInfoToast(baseActivity, "无法连接网络(-1,-1)");
			} else
				CustomToast.showInfoToast(baseActivity, "无法连接到服务器 (HTTP " + parameters.name + ")");
		} else baseActivity.finishRequest(requestType, parameters.value);
	}

}
