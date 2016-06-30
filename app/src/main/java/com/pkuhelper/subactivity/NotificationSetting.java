package com.pkuhelper.subactivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.Editor;
import com.pkuhelper.lib.RequestingTask;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class NotificationSetting {
	SubActivity subActivity;
	JSONArray jsonArray;
	ListView listView;
	boolean hasModified;

	public NotificationSetting(SubActivity _subActivity) {
		subActivity = _subActivity;
	}

	public NotificationSetting set() {
		boolean exam = Editor.getBoolean(subActivity, "n_exam", true);
		boolean course = Editor.getBoolean(subActivity, "n_course", true);
		boolean notifications = Editor.getBoolean(subActivity, "n_notifications", true);

		subActivity.setContentView(R.layout.settings_notification);
		subActivity.setTitle("消息提醒");

		ViewSetting.setSwitchChecked(subActivity, R.id.settings_notification_course_switch, course);
		ViewSetting.setSwitchOnCheckChangeListener(subActivity, R.id.settings_notification_course_switch, new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor.putBoolean(subActivity, "n_course", isChecked);
			}
		});

		ViewSetting.setSwitchChecked(subActivity, R.id.settings_notification_exam_switch, exam);
		ViewSetting.setSwitchOnCheckChangeListener(subActivity, R.id.settings_notification_exam_switch, new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor.putBoolean(subActivity, "n_exam", isChecked);
			}
		});

		ViewSetting.setSwitchChecked(subActivity, R.id.settings_notification_notification_switch, notifications);
		subActivity.findViewById(R.id.settings_notification_notification_setting).setEnabled(notifications);
		subActivity.findViewById(R.id.settings_notification_pkuhole_setting).setEnabled(notifications);
		ViewSetting.setSwitchOnCheckChangeListener(subActivity, R.id.settings_notification_notification_switch, new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor.putBoolean(subActivity, "n_notifications", isChecked);
				subActivity.findViewById(R.id.settings_notification_notification_setting).setEnabled(isChecked);
				subActivity.findViewById(R.id.settings_notification_pkuhole_setting).setEnabled(isChecked);
			}
		});

		ViewSetting.setOnClickListener(subActivity, R.id.settings_notification_notification_setting, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(subActivity, SubActivity.class);
				intent.putExtra("type", Constants.SUBACTIVITY_TYPE_NOTIFICATIONS_SETTING);
				subActivity.startActivity(intent);
			}
		});
		ViewSetting.setOnClickListener(subActivity, R.id.settings_notification_pkuhole_setting, new View.OnClickListener() {
			@SuppressWarnings("unchecked")
			public void onClick(View v) {
				if (!Constants.isValidLogin()) {
					CustomToast.showErrorToast(subActivity, "请先有效登录。");
					return;
				}
				new RequestingTask(subActivity, "正在获取推送设置..",
						Constants.domain + "/services/pkuhole/api.php?action=pushsettings_get&token=" + Constants.token,
						Constants.REQUEST_HOLE_GET_SETTINGS).execute(new ArrayList<Parameters>());
			}
		});

		return this;
	}

	@SuppressLint("InflateParams")
	public void showHoleSettingDialog(String string) {
		try {
			JSONObject jsonObject = new JSONObject(string);
			int code = jsonObject.getInt("code");
			if (code != 0) {
				CustomToast.showErrorToast(subActivity, jsonObject.optString("msg", "推送设置失败"));
				return;
			}
			JSONObject data = jsonObject.getJSONObject("data");
			boolean push = data.optInt("pkuhole_push") != 0;
			boolean hide = data.optInt("pkuhole_hide_content") != 0;
			AlertDialog.Builder builder = new AlertDialog.Builder(subActivity);
			final View settingView = subActivity.getLayoutInflater().inflate(R.layout.hole_push_settings, null, false);
			ViewSetting.setSwitchChecked(settingView, R.id.hole_setting_push_switch, push);
			ViewSetting.setSwitchChecked(settingView, R.id.hole_setting_hide_switch, hide);
			if (!push)
				settingView.findViewById(R.id.hole_setting_hide_switch).setEnabled(false);
			ViewSetting.setSwitchOnCheckChangeListener(settingView, R.id.hole_setting_push_switch,
					new CompoundButton.OnCheckedChangeListener() {
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							settingView.findViewById(R.id.hole_setting_hide_switch).setEnabled(isChecked);
						}
					});

			builder.setView(settingView).setTitle("推送设置")
					.setPositiveButton("确认", new DialogInterface.OnClickListener() {
						@SuppressWarnings("unchecked")
						public void onClick(DialogInterface dialog, int which) {
							boolean push = ViewSetting.getSwitchChecked(settingView, R.id.hole_setting_push_switch);
							boolean hide = ViewSetting.getSwitchChecked(settingView, R.id.hole_setting_hide_switch);

							try {
								JSONObject jsonObject = new JSONObject();
								jsonObject.put("pkuhole_push", push ? 1 : 0);
								jsonObject.put("pkuhole_hide_content", hide ? 1 : 0);

								ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
								arrayList.add(new Parameters("action", "pushsettings_set"));
								arrayList.add(new Parameters("token", Constants.token));
								arrayList.add(new Parameters("data", jsonObject.toString()));

								new RequestingTask(subActivity, "正在保存设置...",
										Constants.domain + "/services/pkuhole/api.php", Constants.REQUEST_HOLE_SET_SETTINGS)
										.execute(arrayList);
							} catch (Exception e) {
								CustomToast.showErrorToast(subActivity, "设置失败");
							}

						}
					}).setNegativeButton("取消", null).show();


		} catch (Exception e) {
			e.printStackTrace();
			CustomToast.showErrorToast(subActivity, "推送设置获取失败。");
		}
	}

	public void finishHoleSetting(String string) {
		try {
			JSONObject jsonObject = new JSONObject(string);
			int code = jsonObject.getInt("code");
			if (code != 0)
				CustomToast.showErrorToast(subActivity, jsonObject.optString("msg", "设置失败"));
			else
				CustomToast.showSuccessToast(subActivity, "设置成功！");
		} catch (Exception e) {
			CustomToast.showErrorToast(subActivity, "设置失败");
		}
	}

	@SuppressWarnings("unchecked")
	@SuppressLint("InflateParams")
	public NotificationSetting setPushes() {
		subActivity.setContentView(R.layout.settings_pushes_listview);
		subActivity.setTitle("推送设置");
		View headerView = subActivity.getLayoutInflater().inflate(R.layout.settings_pushes_headerview, null);
		listView = (ListView) subActivity.findViewById(R.id.settings_pushes_listview);
		listView.addHeaderView(headerView);
		listView.setHeaderDividersEnabled(false);
		listView.setAdapter(new SimpleAdapter(subActivity, new ArrayList<HashMap<String, ?>>(),
				R.layout.settings_pushes_item, new String[]{}, new int[]{}));

		new RequestingTask(subActivity, "正在获取订阅的源列表",
				Constants.domain + "/pkuhelper/nc/selectedSource.php?token=" + Constants.token,
				Constants.REQUEST_SUBACTIVITY_PUSHES_GET).execute(new ArrayList<Parameters>());

		return this;
	}

	public void finishGetPushes(String string) {
		try {
			jsonArray = new JSONArray(string);
			listView.setAdapter(new BaseAdapter() {

				@SuppressLint("ViewHolder")
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					convertView = subActivity.getLayoutInflater().inflate(R.layout.settings_pushes_item, parent, false);
					String string = "";
					boolean check = false;
					try {
						string = jsonArray.getJSONObject(position).optString("name");
						check = jsonArray.getJSONObject(position).optInt("push") == 1;
						ViewSetting.setTextView(convertView, R.id.settings_pushes_item_name, string);
						CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.settings_pushes_item_checkbox);
						checkBox.setChecked(check);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return convertView;
				}

				@Override
				public long getItemId(int position) {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public Object getItem(int position) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public int getCount() {
					// TODO Auto-generated method stub
					return jsonArray.length();
				}
			});
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
										int position, long id) {
					try {
						CheckBox checkBox = (CheckBox) view.findViewById(R.id.settings_pushes_item_checkbox);
						if (checkBox.isChecked()) checkBox.setChecked(false);
						else checkBox.setChecked(true);
						hasModified = true;
						if (checkBox.isChecked())
							jsonArray.getJSONObject(position - 1).put("push", 1);
						else jsonArray.getJSONObject(position - 1).put("push", 0);
						Log.w("json-array", jsonArray.getJSONObject(position - 1).toString());

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

		} catch (Exception e) {
			CustomToast.showErrorToast(subActivity, "解析出错");
		}

	}

	@SuppressWarnings("unchecked")
	public void save() {
		ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
		arrayList.add(new Parameters("token", Constants.token));
		arrayList.add(new Parameters("push", jsonArray.toString()));
		new RequestingTask(subActivity, "正在保存",
				Constants.domain + "/pkuhelper/nc/setPush.php", Constants.REQUEST_SUBACTIVITY_PUSHES_SET)
				.execute(arrayList);
	}

	public void finishSave(String string) {
		try {
			JSONObject object = new JSONObject(string);
			int code = object.getInt("code");
			if (code == 0) {
				CustomToast.showSuccessToast(subActivity, "保存成功！");
				setPushes();
			} else {
				new AlertDialog.Builder(subActivity).setTitle("保存失败！")
						.setMessage(object.optString("msg")).setCancelable(true).setPositiveButton("取消", null).show();
			}
		} catch (Exception e) {
			CustomToast.showErrorToast(subActivity, "解析失败");
		}
		hasModified = false;
	}

}
