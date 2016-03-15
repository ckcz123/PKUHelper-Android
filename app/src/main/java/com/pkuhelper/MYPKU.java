package com.pkuhelper;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.pkuhelper.ui.hole.impl.HoleActivity;
import com.pkuhelper.bbs.BBSActivity;
import com.pkuhelper.chat.ChatActivity;
import com.pkuhelper.classroom.ClassActivity;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.Editor;
import com.pkuhelper.lib.Features;
import com.pkuhelper.lib.Lib;
import com.pkuhelper.lib.MyDrawable;
import com.pkuhelper.lib.Util;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lostfound.old.LostFoundActivity;
import com.pkuhelper.media.MediaActivity;
import com.pkuhelper.noticecenter.NCActivity;
import com.pkuhelper.pkuhole.old.PKUHoleActivity;
import com.pkuhelper.subactivity.SubActivity;

import java.util.ArrayList;

@SuppressLint("InflateParams")
public class MYPKU extends Fragment {

	public final static int IMAGE_SP = 90;
	public static int COLS = 3;
	public static int PADDING_SP = 15;
	public static int PADDING_PX = 15;
	public static int IMAGE_WIDTH = 0;

	public static String[][] publics = {
			{"tzzx", "通知中心", R.drawable.tzzx + ""}
			, {"jzyg", "讲座预告", R.drawable.jzyg + ""}
			, {"bjyc", "百讲演出", R.drawable.bjyc + ""}
			, {"jscx", "教室查询", R.drawable.jscx + ""}
			, {"swzl", "失物招领", R.drawable.lostfound + ""}
			, {"cyxx", "常用信息", R.drawable.cyxx + ""}
			, {"xmtlm", "新媒体联盟", R.drawable.xmtlm + ""}
	};

	public static String[][] selfs = {
			{"message", "我的消息", R.drawable.message + ""}
			, {"wdpz", "我的凭证", R.drawable.wdpz + ""}
			, {"cjcx", "成绩查询", R.drawable.cjcx + ""}
			, {"tccj", "体测成绩", R.drawable.tccj + ""}
			, {"tydk", "体育打卡", R.drawable.tydk + ""}
			, {"pkumail", "PKU邮箱", R.drawable.pkumail + ""}
	};

	public static String[][] communities = {
			{"pkuhole", "P大树洞", R.drawable.pkuhole + ""}
			//	,{"pdsd","P大树洞",R.drawable.pdsd+""}
			, {"wmbbs", "未名BBS", R.drawable.wmbbs + ""}

	};

	public static View mypkuView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		Point point = new Point();
		getActivity().getWindowManager().getDefaultDisplay().getSize(point);
		// sp value
		int width = Util.px2sp(getActivity(), point.x);
		Log.w("px:sp", point.x + ":" + width);
		COLS = width / (IMAGE_SP + 20);
		PADDING_SP = (width - IMAGE_SP * COLS) / (2 * COLS);
		PADDING_PX = Util.sp2px(getActivity(), PADDING_SP);
		IMAGE_WIDTH = Util.sp2px(getActivity(), IMAGE_SP);

		View rootView = inflater.inflate(R.layout.mypku_view,
				container, false);
		mypkuView = rootView;
		String string = Editor.getString(PKUHelper.pkuhelper, "mypku_notwants");
		try {
			setView(getList(publics, string), R.id.mypku_public, "公共信息");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			setView(getList(selfs, string), R.id.mypku_self, "个人信息");
		} catch (Exception e) {
			e.printStackTrace();
		}


        try {
			setView(getList(communities, string), R.id.mypku_community, "P大社区");
		} catch (Exception e) {
			e.printStackTrace();
		}

		setOthers(Constants.features);
		Lib.setBadgeView();
		return rootView;
	}

	public static String[][] getList(String[][] strings, String notwants) {
		ArrayList<String[]> arrayList = new ArrayList<String[]>();
		for (int i = 0; i < strings.length; i++) {
			String[] string = strings[i];
			if (notwants.contains(string[0])) continue;
			arrayList.add(string);
		}

		String[][] wants = new String[arrayList.size()][];
		for (int i = 0; i < arrayList.size(); i++)
			wants[i] = arrayList.get(i);

		return wants;
	}

	public static void setOthers(final ArrayList<Features> features) {
		if (mypkuView == null) return;
		try {
			int len = features.size();
			if (len == 0) {
				mypkuView.findViewById(R.id.mypku_more).setVisibility(View.GONE);
				return;
			}
			LinearLayout rootView = (LinearLayout) mypkuView.findViewById(R.id.mypku_more);
			rootView.setVisibility(View.VISIBLE);
			rootView.removeAllViews();

			View titleView = PKUHelper.pkuhelper.getLayoutInflater()
					.inflate(R.layout.mypku_title, null, false);
			rootView.addView(titleView);
			ViewSetting.setTextView(rootView, R.id.mypku_title, "更多功能");
			ViewSetting.setTextViewBold(rootView, R.id.mypku_title);
			int totalline = (len - 1) / COLS + 1;
			for (int i = 0; i < totalline; i++) {
				LinearLayout lineView = (LinearLayout) PKUHelper.pkuhelper.getLayoutInflater()
						.inflate(R.layout.mypku_line, null, false);
				LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f);

				for (int j = 0; j < COLS; j++) {
					final int position = i * COLS + j;
					String text = "";
					Drawable drawable = null;
					if (position < len) {
						Features feature = features.get(position);
						text = feature.title;
						drawable = MyDrawable.getDrawable(feature.drawable, feature.darkcolor, IMAGE_WIDTH);
					}
					View itemView = PKUHelper.pkuhelper.getLayoutInflater()
							.inflate(R.layout.mypku_item, null, false);
					itemView.setLayoutParams(layoutParams);
					itemView.setPadding(PADDING_PX, 0, PADDING_PX, 0);
					ViewSetting.setTextView(itemView, R.id.mypku_text, text);
					ViewSetting.setImageDrawable(itemView, R.id.mypku_image, drawable);
					itemView.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							click(features, position);
						}
					});

					lineView.addView(itemView);

				}
				rootView.addView(lineView);
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void setView(final String[][] s,
							   int resid, String title) throws Exception {
		if (s.length == 0) {
			mypkuView.findViewById(resid).setVisibility(View.GONE);
			return;
		}
		LinearLayout rootView = (LinearLayout) mypkuView.findViewById(resid);
		rootView.setVisibility(View.VISIBLE);
		ViewSetting.setTextView(rootView, R.id.mypku_title, title);
		ViewSetting.setTextViewBold(rootView, R.id.mypku_title);
		int len = s.length;
		int totalline = (len - 1) / COLS + 1;
		for (int i = 0; i < totalline; i++) {
			LinearLayout lineView = (LinearLayout) PKUHelper.pkuhelper.getLayoutInflater()
					.inflate(R.layout.mypku_line, null, false);
			LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f);

			for (int j = 0; j < COLS; j++) {
				int position = i * COLS + j;
				final String name = position < len ? s[position][0] : "";
				String text = position < len ? s[position][1] : "";
				int imagesource = position < len ? Integer.parseInt(s[position][2]) : 0;
				View itemView = PKUHelper.pkuhelper.getLayoutInflater()
						.inflate(R.layout.mypku_item, null, false);
				itemView.setLayoutParams(layoutParams);
				itemView.setPadding(PADDING_PX, 0, PADDING_PX, 0);
				ViewSetting.setTextView(itemView, R.id.mypku_text, text);
				ViewSetting.setImageResource(itemView, R.id.mypku_image, imagesource);
				itemView.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						click(name);
					}
				});
				itemView.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						longClick();
						return true;
					}
				});
				if (!"".equals(name))
					itemView.setTag("mypkuitem_" + name);
				lineView.addView(itemView);

			}
			rootView.addView(lineView);
		}
	}

	private static void click(String string) {
		if ("tzzx".equals(string))
			PKUHelper.pkuhelper.startActivity(
					new Intent(PKUHelper.pkuhelper, NCActivity.class));
		else if ("xmtlm".equals(string))
			PKUHelper.pkuhelper.startActivity(
					new Intent(PKUHelper.pkuhelper, MediaActivity.class));
		else if ("cjcx".equals(string))
			Dean.getSessionId(Dean.FLAG_GETTING_GRADE);
		else if ("jscx".equals(string))
			PKUHelper.pkuhelper.startActivity(
					new Intent(PKUHelper.pkuhelper, ClassActivity.class));
		else if ("jzyg".equals(string)) {
			Intent intent = new Intent();
			intent.setClass(PKUHelper.pkuhelper, SubActivity.class);
			intent.putExtra("type", Constants.SUBACTIVITY_TYPE_LECTURE);
			PKUHelper.pkuhelper.startActivity(intent);
		} else if ("bjyc".equals(string)) {
			Intent intent = new Intent();
			intent.setClass(PKUHelper.pkuhelper, SubActivity.class);
			intent.putExtra("type", Constants.SUBACTIVITY_TYPE_SHOWS);
			PKUHelper.pkuhelper.startActivity(intent);
		} else if (("tccj").equals(string))
			PE.getPeTestScore();
		else if (("tydk").equals(string))
			PE.peCard();
		else if ("wmbbs".equals(string))
			PKUHelper.pkuhelper.startActivity(
					new Intent(PKUHelper.pkuhelper, BBSActivity.class));
		else if ("pkuhole".equals(string))
			PKUHelper.pkuhelper.startActivity(

					//TEST
					new Intent(PKUHelper.pkuhelper, HoleActivity.class));
					//new Intent(PKUHelper.pkuhelper, HoleActivity.class));
		else if (("pdsd").equals(string))
			PKUHelper.pkuhelper.startActivity(
					new Intent(PKUHelper.pkuhelper, PKUHoleActivity.class));
		else if ("pkumail".equals(string)) {
			Intent intent = new Intent();
			intent.setClass(PKUHelper.pkuhelper, SubActivity.class);
			intent.putExtra("type", Constants.SUBACTIVITY_TYPE_WEBVIEW_PKUMAIL);
			PKUHelper.pkuhelper.startActivity(intent);
		} else if ("swzl".equals(string)) {
			if (!Constants.isValidLogin()) {
				CustomToast.showInfoToast(PKUHelper.pkuhelper, "请先进行有效登录！");
				return;
			}
			PKUHelper.pkuhelper.startActivity(
					new Intent(PKUHelper.pkuhelper, com.pkuhelper.lostfound.LostFoundActivity.class));
		} else if ("lostfound".equals(string)) {
			if (!Constants.isValidLogin()) {
				CustomToast.showInfoToast(PKUHelper.pkuhelper, "请先进行有效登录！");
				return;
			}
			PKUHelper.pkuhelper.startActivity(
					new Intent(PKUHelper.pkuhelper, LostFoundActivity.class));
		} else if ("cyxx".equals(string)) {
			Intent intent = new Intent(PKUHelper.pkuhelper, SubActivity.class);
			intent.putExtra("type", Constants.SUBACTIVITY_TYPE_INFORMATION);
			PKUHelper.pkuhelper.startActivity(intent);
		} else if ("wdpz".equals(string)) {
			if (!Constants.isValidLogin()) {
				CustomToast.showInfoToast(PKUHelper.pkuhelper, "请先进行有效登录！");
				return;
			}
			Intent intent = new Intent(PKUHelper.pkuhelper, SubActivity.class);
			intent.putExtra("type", Constants.SUBACTIVITY_TYPE_CERTIFICATION);
			PKUHelper.pkuhelper.startActivity(intent);
			Constants.newPass = 0;
			Lib.setBadgeView();
		} else if ("message".equals(string)) {
			PKUHelper.pkuhelper.startActivity(new Intent(PKUHelper.pkuhelper, ChatActivity.class));
			Constants.newMsg = 0;
			Lib.setBadgeView();
		}
	}

	private static void click(ArrayList<Features> arrayList, int offset) {
		if (offset >= arrayList.size()) return;
		String url = arrayList.get(offset).url;
		Intent intent = new Intent(PKUHelper.pkuhelper, SubActivity.class);
		intent.putExtra("type", Constants.SUBACTIVITY_TYPE_WEBVIEW);
		intent.putExtra("url", url);
		intent.putExtra("title", arrayList.get(offset).title);
		intent.putExtra("post", "user_token=" + Constants.user_token);
		PKUHelper.pkuhelper.startActivity(intent);
		return;
	}

	private static void longClick() {
		String[] strings = new String[]{"编辑项目"};
		new AlertDialog.Builder(PKUHelper.pkuhelper).setItems(strings, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(PKUHelper.pkuhelper, SubActivity.class);
				intent.putExtra("type", Constants.SUBACTIVITY_TYPE_MYPKU_SET);
				PKUHelper.pkuhelper.startActivity(intent);
			}
		}).show();
	}

}
