package com.pkuhelper.qrcode;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.pkuhelper.lib.BaseActivity;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.subactivity.SubActivity;

public class QRCodeActivity extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startActivityForResult(new Intent(this, MipcaActivityCapture.class), 1);
		getActionBar().setTitle("二维码扫描");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK || requestCode != 1 || data == null) {
			finish();
			return;
		}
		final String result = data.getStringExtra("result");
		if (!result.startsWith("http://") && !result.startsWith("https://")) {
			new AlertDialog.Builder(this).setTitle("扫描结果").setMessage(result)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					})
					.setNegativeButton("复制", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
							clipboardManager.setPrimaryClip(ClipData.newPlainText("text", result));
							CustomToast.showSuccessToast(QRCodeActivity.this, "已复制到剪切板！", 1500);
							finish();
						}
					}).setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					finish();
				}
			}).show();
		} else if (result.startsWith("http://weixin.qq.com/")) {
			CustomToast.showErrorToast(this, "微信用户和微信群的二维码无法解析，"
					+ "请用微信扫一扫进行识别。");
			finish();
		} else {
			Intent intent = new Intent(this, SubActivity.class);
			intent.putExtra("type", Constants.SUBACTIVITY_TYPE_WEBVIEW);
			intent.putExtra("url", result);
			intent.putExtra("post", "user_token=" + Constants.user_token);
			startActivity(intent);
			finish();
		}

	}

	protected void finishRequest(int type, String string) {
	}

}
