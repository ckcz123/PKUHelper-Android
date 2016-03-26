package com.pkuhelper.bbs;

import android.os.Bundle;
import android.view.View;

import com.pkuhelper.R;
import com.pkuhelper.lib.BaseActivity;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.RequestingTask;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;

public class MessagePostActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getIntent().getExtras();
		String author = bundle.getString("author", "");
		String title = bundle.getString("title", "");
		String content = bundle.getString("content", "");

		setContentView(R.layout.bbs_post_mail);
		setTitle("发站内信");
		ViewSetting.setEditTextValue(this, R.id.bbs_post_mail_author, author);
		ViewSetting.setEditTextValue(this, R.id.bbs_post_mail_title, title);
		String text = "";
		if (!"".equals(content)) {
			String string = content;
			string = string.replaceAll("<[^>]+>", "");

			BufferedReader bufferedReader = new BufferedReader(new StringReader(string));
			text = "\n\n【在 " + author + " 的来信中提到: 】\n";
			String line;
			int cnt = 0;
			try {
				boolean lastBlankline = false;
				while ((line = bufferedReader.readLine()) != null) {
					String str = new String(line);
					str = str.trim();
					if ("".equals(str) && lastBlankline) continue;
					if (str.startsWith("寄信人:")) continue;
					if (str.startsWith("标  题:")) continue;
					if (str.startsWith("发信站:")) continue;
					if (str.startsWith("来  源:")) continue;
					text += ": " + line + "\n";
					lastBlankline = "".equals(str);
					cnt++;
					if (cnt >= 6)
						break;
				}
				if (line != null) {
					text += ": ...........................";
				}
			} catch (Exception e) {
			}
		}
		ViewSetting.setEditTextValue(this, R.id.bbs_post_mail_text, text);

		ViewSetting.setOnClickListener(this, R.id.bbs_post_mail_button, new View.OnClickListener() {
			@Override
			@SuppressWarnings("unchecked")
			public void onClick(View v) {
				String author = ViewSetting.getEditTextValue(MessagePostActivity.this, R.id.bbs_post_mail_author);
				String title = ViewSetting.getEditTextValue(MessagePostActivity.this, R.id.bbs_post_mail_title);
				String text = ViewSetting.getEditTextValue(MessagePostActivity.this, R.id.bbs_post_mail_text);

				if ("".equals(author) || "".equals(title) || "".equals(text)) {
					CustomToast.showInfoToast(MessagePostActivity.this, "信息不能为空！", 1200);
					return;
				}

				text += "\n\n--\n发自 PKU Helper (Android " + Constants.version + ")\n\n";

				ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
				arrayList.add(new Parameters("type", "postmail"));
				arrayList.add(new Parameters("token", Userinfo.token));
				arrayList.add(new Parameters("to", author));
				arrayList.add(new Parameters("title", title));
				arrayList.add(new Parameters("text", text));
				arrayList.add(new Parameters("number", getIntent().getExtras().getString("number", "")));
				new RequestingTask(MessagePostActivity.this, "正在发送...",
						"http://www.bdwm.net/client/bbsclient.php", Constants.REQUEST_BBS_POST_MAIL)
						.execute(arrayList);
			}
		});
	}

	protected void finishRequest(int type, String string) {
		if (type == Constants.REQUEST_BBS_POST_MAIL) {
			try {
				JSONObject jsonObject = new JSONObject(string);
				int code = jsonObject.getInt("code");
				if (code != 0) {
					CustomToast.showErrorToast(this, jsonObject.optString("msg", "发送失败"), 1500);
					return;
				}
				setResult(RESULT_OK);
				CustomToast.showSuccessToast(this, "发送成功", 1500);
				finish();
			} catch (Exception e) {
				CustomToast.showErrorToast(this, "发送失败");
			}
		}
	}

}
