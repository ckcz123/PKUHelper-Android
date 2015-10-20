package com.pkuhelper.lib;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.subactivity.SubActivity;

import java.io.File;
import java.util.Locale;

public class ImageRequest extends AsyncTask<String, String, File> {
	ProgressDialog progressDialog;
	String title;
	String url;
	Context context;

	public static void showImage(Context context, String url) {
		showImage(context, url, "查看图片");
	}

	public static void showImage(Context context, String url, String title) {
		File file = MyFile.getCache(context, Util.getHash(url));
		if (!file.exists())
			new ImageRequest(context, url, title).execute("");
		else {
			Intent intent = new Intent(context, SubActivity.class);
			if (title.toLowerCase(Locale.getDefault()).endsWith(".gif")
					|| url.toLowerCase(Locale.getDefault()).endsWith(".gif"))
				intent.putExtra("type", Constants.SUBACTIVITY_TYPE_PICTURE_GIF);
			else
				intent.putExtra("type", Constants.SUBACTIVITY_TYPE_PICTURE_FILE);
			intent.putExtra("title", title.toLowerCase(Locale.getDefault()));
			intent.putExtra("file", file.getAbsolutePath());
			context.startActivity(intent);
		}
	}

	public ImageRequest(Context _context, String _url, String _title) {
		context = _context;
		title = _title;
		url = _url;
		progressDialog = new ProgressDialog(context);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setTitle("提示");
		progressDialog.setMessage("正在获取图片...");
		progressDialog.setIndeterminate(false);
		progressDialog.setCancelable(false);
	}

	@Override
	protected void onPreExecute() {
		progressDialog.show();
	}

	@Override
	protected File doInBackground(String... params) {
		try {
			File file = MyFile.getCache(context, Util.getHash(url));
			if (file.exists()) return file;
			if (MyFile.urlToFile(url, file))
				return file;
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	protected void onPostExecute(File file) {
		progressDialog.dismiss();
		if (file == null) {
			CustomToast.showErrorToast(context, "图片获取失败");
			return;
		}
		showImage(context, url, title);
	}

}
