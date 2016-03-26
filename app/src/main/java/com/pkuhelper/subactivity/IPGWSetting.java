package com.pkuhelper.subactivity;

import android.widget.CompoundButton;

import com.pkuhelper.R;
import com.pkuhelper.lib.Editor;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.widget.IPGWNotification;

public class IPGWSetting {
	SubActivity subActivity;

	public IPGWSetting(SubActivity _subactivity) {
		subActivity = _subactivity;
	}

	public void show() {
		subActivity.setContentView(R.layout.settings_ipgw);
		subActivity.setTitle("网关控制");

		ViewSetting.setSwitchChecked(subActivity, R.id.settings_shake_switch, Editor.getBoolean(subActivity, "use_shake"));
		ViewSetting.setSwitchOnCheckChangeListener(subActivity, R.id.settings_shake_switch, new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor.putBoolean(subActivity, "use_shake", isChecked);
				//CustomToast.showInfoToast(subActivity, "退出并重进后方可生效", 1200);
			}
		});

		if (android.os.Build.VERSION.SDK_INT < 16) {
			subActivity.findViewById(R.id.settings_its_noti).setEnabled(false);
			subActivity.findViewById(R.id.settings_its_noti_switch).setEnabled(false);
			subActivity.findViewById(R.id.settings_its_noti_icon).setEnabled(false);
			subActivity.findViewById(R.id.settings_its_noti_icon_switch).setEnabled(false);
			Editor.putBoolean(subActivity, "ipgwnoti", false);
			Editor.putBoolean(subActivity, "ipgwnotishow", false);
			return;
		}

		ViewSetting.setSwitchChecked(subActivity, R.id.settings_its_noti_switch, Editor.getBoolean(subActivity, "ipgwnoti", true));
		ViewSetting.setSwitchOnCheckChangeListener(subActivity, R.id.settings_its_noti_switch, new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					subActivity.findViewById(R.id.settings_its_noti_icon).setEnabled(false);
					subActivity.findViewById(R.id.settings_its_noti_icon_switch).setEnabled(false);
				} else {
					subActivity.findViewById(R.id.settings_its_noti_icon).setEnabled(true);
					subActivity.findViewById(R.id.settings_its_noti_icon_switch).setEnabled(true);
				}
				Editor.putBoolean(subActivity, "ipgwnoti", isChecked);
				IPGWNotification.update(subActivity);
			}
		});
		if (!Editor.getBoolean(subActivity, "ipgwnoti", true)) {
			subActivity.findViewById(R.id.settings_its_noti_icon).setEnabled(false);
			subActivity.findViewById(R.id.settings_its_noti_icon_switch).setEnabled(false);
		}
		ViewSetting.setSwitchChecked(subActivity, R.id.settings_its_noti_icon_switch, Editor.getBoolean(subActivity, "ipgwnotishow", true));
		ViewSetting.setSwitchOnCheckChangeListener(subActivity, R.id.settings_its_noti_icon_switch, new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor.putBoolean(subActivity, "ipgwnotishow", isChecked);
				IPGWNotification.update(subActivity);
			}
		});


	}


}
