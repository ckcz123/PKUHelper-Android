package com.pkuhelper.bbs;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pkuhelper.R;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.BadgeView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserinfoFragment extends Fragment {
	static View userinfoView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.bbs_userinfo_view, container, false);
		userinfoView = rootView;
		set();
		return rootView;
	}

	public static void set() {
		if (userinfoView == null) return;
		try {
			if ("".equals(Userinfo.token)) {
				ViewSetting.setTextView(userinfoView, R.id.bbs_userinfo_username, "点击登录...");
				ViewSetting.setOnClickListener(userinfoView, R.id.bbs_userinfo_username, new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Userinfo.showLoginView();
					}
				});
				ViewSetting.setTextView(userinfoView, R.id.bbs_userinfo_message, "站内信");
				ViewSetting.setOnClickListener(userinfoView, R.id.bbs_userinfo_message, null);
				ViewSetting.setTextView(userinfoView, R.id.bbs_userinfo_nickname, "");
				ViewSetting.setTextView(userinfoView, R.id.bbs_userinfo_numposts, "");
				ViewSetting.setTextView(userinfoView, R.id.bbs_userinfo_numlogins, "");
				ViewSetting.setTextView(userinfoView, R.id.bbs_userinfo_life, "");
				ViewSetting.setTextView(userinfoView, R.id.bbs_userinfo_staytime, "");
				ViewSetting.setTextView(userinfoView, R.id.bbs_userinfo_createtime, "");
				ViewSetting.setTextView(userinfoView, R.id.bbs_userinfo_lasttime, "");
				ViewSetting.setOnClickListener(userinfoView, R.id.bbs_userinfo_relogin, new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Userinfo.showLoginView();
					}
				});
				userinfoView.findViewById(R.id.bbs_userinfo_logout).setEnabled(false);
			} else {
				ViewSetting.setTextView(userinfoView, R.id.bbs_userinfo_username, Userinfo.username);
				ViewSetting.setOnClickListener(userinfoView, R.id.bbs_userinfo_username, null);
				String text = "";
				String text2 = "站内信 (" + Userinfo.message + ")";
				if (Userinfo.hasNewMsg) {
					text = "new";
					text2 += "        ";
				}
				ViewSetting.setTextView(userinfoView, R.id.bbs_userinfo_message, text2);
				BadgeView.show(BBSActivity.bbsActivity, BBSActivity.bbsActivity.findViewById(R.id.bbs_bottom_img_me), text);
				BadgeView.show(BBSActivity.bbsActivity, userinfoView.findViewById(R.id.bbs_userinfo_message), text);
				ViewSetting.setOnClickListener(userinfoView, R.id.bbs_userinfo_tablerow_message, new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Userinfo.hasNewMsg = false;
						set();
						BBSActivity.bbsActivity.startActivity(new Intent(BBSActivity.bbsActivity, MessageActivity.class));
					}
				});
				ViewSetting.setTextView(userinfoView, R.id.bbs_userinfo_nickname, Userinfo.nickname);
				ViewSetting.setTextView(userinfoView, R.id.bbs_userinfo_numposts, Userinfo.numposts + "");
				ViewSetting.setTextView(userinfoView, R.id.bbs_userinfo_numlogins, Userinfo.numlogins + "");
				ViewSetting.setTextView(userinfoView, R.id.bbs_userinfo_life, Userinfo.life + "");
				ViewSetting.setTextView(userinfoView, R.id.bbs_userinfo_staytime, Userinfo.staytime / 60 + "");
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
				ViewSetting.setTextView(userinfoView, R.id.bbs_userinfo_createtime, simpleDateFormat.format(new Date(Userinfo.createtime)));
				ViewSetting.setTextView(userinfoView, R.id.bbs_userinfo_lasttime, simpleDateFormat.format(new Date(Userinfo.lasttime)));
				ViewSetting.setOnClickListener(userinfoView, R.id.bbs_userinfo_relogin, new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Userinfo.login();
					}
				});
				userinfoView.findViewById(R.id.bbs_userinfo_logout).setEnabled(true);
				ViewSetting.setOnClickListener(userinfoView, R.id.bbs_userinfo_logout, new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Userinfo.logout();
						set();
					}
				});
			}
		} catch (Exception e) {
		}
	}
}
