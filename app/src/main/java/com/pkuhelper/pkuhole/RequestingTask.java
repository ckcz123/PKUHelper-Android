package com.pkuhelper.pkuhole;

import java.util.*;

import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.*;

import android.app.*;
import android.os.AsyncTask;

public class RequestingTask extends AsyncTask<ArrayList<Parameters>, String, Parameters>{
	ProgressDialog progressDialog;
	String requestString;
	int requestType;
	Activity activity;
	
	public RequestingTask(Activity activity, String msg,String url,int type) {
		this.activity=activity;
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
			if ("-1".equals(parameters.name)) {
				CustomToast.showInfoToast(activity, 
						"无法连接网络(-1,-1)");
			}
			else
				CustomToast.showInfoToast(activity, 
					"无法连接到服务器 (HTTP "+parameters.name+")");
		}
		else {
			if (activity instanceof HoleActivity) {
				((HoleActivity)activity).finishRequest(requestType, parameters.value);
			}
			else if (activity instanceof HolePost) {
				((HolePost)activity).finishRequest(requestType, parameters.value);
			}
			else if (activity instanceof HoleComment) {
				((HoleComment)activity).finishRequest(requestType, parameters.value);
			}
			else if (activity instanceof HoleSearch) {
				((HoleSearch)activity).finishRequest(requestType, parameters.value);
			}
		}
	}
	
}
