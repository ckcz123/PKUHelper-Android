package com.pkuhelper.bbs;

import java.util.*;

import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.*;

import android.app.*;
import android.os.AsyncTask;

public class RequestingTask extends AsyncTask<ArrayList<Parameters>, String, Parameters>{
	Activity activity;
	ProgressDialog progressDialog;
	String requestString;
	int requestType;
	public RequestingTask(Activity _activity, String msg,String url,int type) {
		activity=_activity;
		progressDialog=new ProgressDialog(activity);
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
			if ("-1".equals(parameters.name))
				CustomToast.showInfoToast(activity, "无法连接网络(-1,-1)");
			else  {
				CustomToast.showInfoToast(activity, "无法连接到服务器 (HTTP "+parameters.name+")");
			}
		}
		else {
			if (activity instanceof BBSActivity)
				((BBSActivity)activity).finishRequest(requestType,parameters.value);
			else if (activity instanceof ViewActivity)
				((ViewActivity)activity).finishRequest(requestType,parameters.value);
			else if (activity instanceof PostActivity)
				((PostActivity)activity).finishRequest(requestType,parameters.value);
			else if (activity instanceof MessageActivity)
				((MessageActivity)activity).finishRequest(requestType,parameters.value);
			else if (activity instanceof MessagePostActivity)
				((MessagePostActivity)activity).finishRequest(requestType,parameters.value);
		}
	}
	
}
