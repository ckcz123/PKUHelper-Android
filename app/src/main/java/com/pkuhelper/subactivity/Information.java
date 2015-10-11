package com.pkuhelper.subactivity;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.View;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;

public class Information {
	SubActivity subActivity;
	
	public Information(SubActivity _subActivity) {
		subActivity=_subActivity;
	}
	
	public Information init() {
		subActivity.getActionBar().setTitle("常用信息");
		subActivity.setContentView(R.layout.subactivity_information_view);
		
		ViewSetting.setOnClickListener(subActivity, R.id.settings_map, new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent=new Intent(subActivity, SubActivity.class);
				intent.putExtra("type", Constants.SUBACTIVITY_TYPE_PICTURE_RESOURCE);
				intent.putExtra("resid", R.drawable.pkumap);
				intent.putExtra("title", "北大地图");
				subActivity.startActivity(intent);
			}
		});
		ViewSetting.setOnClickListener(subActivity, R.id.settings_subway, new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent=new Intent(subActivity, SubActivity.class);
				intent.putExtra("type", Constants.SUBACTIVITY_TYPE_PICTURE_RESOURCE);
				intent.putExtra("resid", R.drawable.subwaymap);
				intent.putExtra("title", "北京地铁线路图");
				subActivity.startActivity(intent);
			}
		});
		ViewSetting.setOnClickListener(subActivity, R.id.settings_phone, new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent=new Intent(subActivity, SubActivity.class);
				intent.putExtra("type", Constants.SUBACTIVITY_TYPE_PHONE);
				subActivity.startActivity(intent);
			}
		});
		
		ViewSetting.setOnClickListener(subActivity, R.id.settings_calendar, new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent=new Intent(subActivity, SubActivity.class);
				intent.putExtra("type", Constants.SUBACTIVITY_TYPE_WEBVIEW_CALENDAR);
				subActivity.startActivity(intent);
			}
		});
		
		ViewSetting.setOnClickListener(subActivity, R.id.settings_bill, new View.OnClickListener() {
			@SuppressWarnings("unchecked")
			public void onClick(View v) {
				if (!Constants.isValidLogin()) {
					CustomToast.showErrorToast(subActivity, "请先进行有效登录！");
					return;
				}
				new RequestingTask(subActivity, "正在获取校园卡余额...", 
						Constants.domain+"/services/card_amount.php"
								+ "?token="+Constants.token, Constants.REQUEST_SUBACTIVITY_CARD_AMOUNT)
				.execute(new ArrayList<Parameters>());
			}
		});
		return this;
	}
	
	public void showAmount(String string) {
		try {
			JSONObject jsonObject=new JSONObject(string);
			int code=jsonObject.getInt("code");
			if (code!=0) {
				CustomToast.showErrorToast(subActivity, jsonObject.optString("msg", "获取失败"));
				return;
			}
			
			String amount=jsonObject.getString("data");
			new AlertDialog.Builder(subActivity).setTitle("余额信息")
			.setMessage("你的校园卡余额是："+amount).setPositiveButton("确定", null).show();
		}
		catch (Exception e) {
			CustomToast.showErrorToast(subActivity, "获取失败");
		}
		
		
	}
	
}