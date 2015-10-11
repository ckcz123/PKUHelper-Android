package com.pkuhelper.bbs;

import java.util.ArrayList;

import org.json.JSONObject;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class PostActivity extends Activity {
	String type="";
	String board="";
	String threadid="";
	String postid="";
	String number="";
	String author="";
	String timestamp="";
	
	@Override
	@SuppressWarnings("unchecked")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if ("".equals(Userinfo.token)) {
			CustomToast.showInfoToast(this, "请先登录！");
			setResult(RESULT_CANCELED);
			finish();
			return;
		}
		
		
		Bundle bundle=getIntent().getExtras();
		
		type=bundle.getString("type", "post");
		
		board=bundle.getString("board", "");
		threadid=bundle.getString("threadid","");
		String title=bundle.getString("title", "").trim();
		postid=bundle.getString("postid", "");
		number=bundle.getString("number", "");
		author=bundle.getString("author", "");
		timestamp=bundle.getString("timestamp","");
		
		setContentView(R.layout.bbs_postpage);
		
		String tt="发表帖子";
		if ("reply".equals(type)) tt="回复帖子";
		if ("edit".equals(type)) tt="编辑帖子";
		getActionBar().setTitle(tt);
		if (!"".equals(title)) {
			ViewSetting.setEditTextValue(this, R.id.bbs_postpage_title, "Re: "+title);
		}
		Board bd=Board.boards.get(board);
		if (bd!=null && bd.anonymous) {
			CheckBox checkBox=(CheckBox)findViewById(R.id.bbs_postpage_anonymous);
			checkBox.setEnabled(true);
			checkBox.setChecked(bd.anonymous);
		}
		Button button=(Button)findViewById(R.id.bbs_postpage_button);
		String hint="发表";
		if ("reply".equals(type)) hint="回复";
		if ("edit".equals(type)) hint="编辑";
		button.setText(hint);
		button.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				post();
			}
		});
		if ("reply".equals(type)) {
			new RequestingTask(this, "正在拉取内容...", 
					"http://www.bdwm.net/client/bbsclient.php?type=quote"
					+"&token="+Userinfo.token+"&board="+board+"&number="+number
					+"&timestamp="+timestamp
					, Constants.REQUEST_BBS_GET_QUOTE)
				.execute(new ArrayList<Parameters>());
		}
		if ("edit".equals(type)) {
			new RequestingTask(this, "正在拉取内容...", 
					"http://www.bdwm.net/client/bbsclient.php?type=getedit"
					+"&token="+Userinfo.token+"&board="+board+"&number="+number
					+"&timestamp="+timestamp
					, Constants.REQUEST_BBS_GET_EDIT).execute(new ArrayList<Parameters>());
		}
		
	}

	void finishRequest(int type, String string) {
		if (type==Constants.REQUEST_BBS_GET_QUOTE)
			setText(string);
		if (type==Constants.REQUEST_BBS_POST)
			finishPost(string);
		if (type==Constants.REQUEST_BBS_GET_EDIT)
			finishGetEdit(string);
	}
	
	void setText(String string) {
		try {
			JSONObject jsonObject=new JSONObject(string);
			int code=jsonObject.getInt("code");
			if (code!=0) {
				CustomToast.showErrorToast(this, jsonObject.optString("msg"), 
						1500);
				return;
			}
			String text=jsonObject.optString("text");
			ViewSetting.setEditTextValue(this, R.id.bbs_postpage_text, "\n\n"+text+"\n");
			findViewById(R.id.bbs_postpage_text).requestFocus();
			((EditText)findViewById(R.id.bbs_postpage_text)).setSelection(0);
		}
		catch (Exception e) {}
		
	}
	
	@SuppressWarnings("unchecked")
	void post() {
		
		if ("edit".equals(type)) {
			edit();
			return;
		}
		
		String title=ViewSetting.getEditTextValue(this, R.id.bbs_postpage_title).trim();
		String text=ViewSetting.getEditTextValue(this, R.id.bbs_postpage_text);		
		if ("".equals(title)) {
			CustomToast.showErrorToast(this, "标题不能为空！", 1500);
			return;
		}
		text+="\n\n--\n发自 PKU Helper (Android "+Constants.version+")\n";
		CheckBox checkBox=(CheckBox)findViewById(R.id.bbs_postpage_anonymous);
		String anonymous=checkBox.isChecked()?"1":"0";
		
		ArrayList<Parameters> arrayList=new ArrayList<Parameters>();
		arrayList.add(new Parameters("type", "post"));
		arrayList.add(new Parameters("token", Userinfo.token));
		arrayList.add(new Parameters("board", board));
		arrayList.add(new Parameters("title", title));
		arrayList.add(new Parameters("text", text));
		arrayList.add(new Parameters("anonymous", anonymous));
		if ("reply".equals(type)) {
			arrayList.add(new Parameters("threadid", threadid));
			arrayList.add(new Parameters("postid", postid));
			arrayList.add(new Parameters("author", author));
		}
		new RequestingTask(this, "正在发表...", "http://www.bdwm.net/client/bbsclient.php", 
				Constants.REQUEST_BBS_POST).execute(arrayList);
		
	}
	
	@SuppressWarnings("unchecked")
	void edit() {
		String title=ViewSetting.getEditTextValue(this, R.id.bbs_postpage_title).trim();
		String text=ViewSetting.getEditTextValue(this, R.id.bbs_postpage_text);		
		if ("".equals(title)) {
			CustomToast.showErrorToast(this, "标题不能为空！", 1500);
			return;
		}
		text+="\n\n--\n发自 PKU Helper (Android "+Constants.version+")\n\n";
		CheckBox checkBox=(CheckBox)findViewById(R.id.bbs_postpage_anonymous);
		String anonymous=checkBox.isChecked()?"1":"0";
		
		ArrayList<Parameters> arrayList=new ArrayList<Parameters>();
		arrayList.add(new Parameters("type", "edit"));
		arrayList.add(new Parameters("token", Userinfo.token));
		arrayList.add(new Parameters("board", board));
		arrayList.add(new Parameters("title", title));
		arrayList.add(new Parameters("text", text));
		arrayList.add(new Parameters("anonymous", anonymous));
		arrayList.add(new Parameters("timestamp", timestamp));
		arrayList.add(new Parameters("number", number));
		new RequestingTask(this, "正在修改...", "http://www.bdwm.net/client/bbsclient.php", 
				Constants.REQUEST_BBS_POST).execute(arrayList);
		
	}
	
	void finishPost(String string) {
		try {
			JSONObject jsonObject=new JSONObject(string);
			int code=jsonObject.getInt("code");
			if (code!=0) {
				CustomToast.showErrorToast(this, jsonObject.optString("msg", "发表失败"), 1500);
				return;
			}
			setResult(RESULT_OK);
			CustomToast.showSuccessToast(this, "发表成功！");
			finish();
		}
		catch (Exception e) {
			CustomToast.showErrorToast(this, "发表失败", 1500);
		}
	}
	
	public void finishGetEdit(String string) {
		try {
			JSONObject jsonObject=new JSONObject(string);
			int code=jsonObject.getInt("code");
			if (code!=0) {
				CustomToast.showErrorToast(this, 
						jsonObject.optString("msg", "内容拉取失败"), 1300);
				setResult(RESULT_CANCELED);
				finish();
				return;
			}
			String title=jsonObject.optString("title");
			String text=jsonObject.optString("text");
			
			ViewSetting.setEditTextValue(this, R.id.bbs_postpage_title, title);
			ViewSetting.setEditTextValue(this, R.id.bbs_postpage_text, "\n\n"+text+"\n");
			findViewById(R.id.bbs_postpage_text).requestFocus();
			((EditText)findViewById(R.id.bbs_postpage_text)).setSelection(0);
			
		}
		catch (Exception e) {
			CustomToast.showErrorToast(this, "内容获取失败", 1300);
			setResult(RESULT_CANCELED);
			finish();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode==KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN) {
			setResult(RESULT_CANCELED);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
}
