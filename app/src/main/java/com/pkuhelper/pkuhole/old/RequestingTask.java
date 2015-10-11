package com.pkuhelper.pkuhole.old;

import java.util.*;

import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.*;

import android.app.*;
import android.os.AsyncTask;

public class RequestingTask extends AsyncTask<ArrayList<Parameters>, String, Parameters>{
	ProgressDialog progressDialog;
	String requestString;
	int requestType;
	/**
	 * 发起一个http调用；如果访问失败那么直接Toast提醒，无返回
	 * @param pkuhelper 调用时需要传this
	 * @param msg 提示消息
	 * @param url 请求地址
	 * @param type 访问类型
	 */
	public RequestingTask(String msg,String url,int type) {
		progressDialog=new ProgressDialog(PKUHoleActivity.pkuHoleActivity);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setTitle("提示");
		progressDialog.setMessage(msg);
		progressDialog.setIndeterminate(false);
		progressDialog.setCancelable(false);
		requestString=url;
		requestType=type;
	}
	@Override
	protected void onPreExecute() {
		progressDialog.show();
	}
	@Override
	protected Parameters doInBackground(ArrayList<Parameters>... params) {
		return WebConnection.connect(requestString, params[0]);
	}
	@Override
	protected void onPostExecute(Parameters parameters) {
		progressDialog.dismiss();
		if (!"200".equals(parameters.name)) {
			if ("-1".equals(parameters.name)) {
				CustomToast.showInfoToast(PKUHoleActivity.pkuHoleActivity, 
						"无法连接网络(-1,-1)");
			}
			else
				CustomToast.showInfoToast(PKUHoleActivity.pkuHoleActivity, 
					"无法连接到服务器 (HTTP "+parameters.name+")");
		}
		else PKUHoleActivity.pkuHoleActivity.finishRequest(requestType,parameters.value);
	}
	
}
