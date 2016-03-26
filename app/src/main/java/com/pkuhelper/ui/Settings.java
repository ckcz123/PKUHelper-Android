package com.pkuhelper.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;

import com.pkuhelper.IAAA;
import com.pkuhelper.PE;
import com.pkuhelper.PKUHelper;
import com.pkuhelper.R;
import com.pkuhelper.chat.ChatActivity;
import com.pkuhelper.gesture.GestureActivity;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.Editor;
import com.pkuhelper.lib.Lib;
import com.pkuhelper.lib.MyFile;
import com.pkuhelper.lib.RequestingTask;
import com.pkuhelper.lib.Share;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.subactivity.SubActivity;
import com.pkuhelper.ui.main.impl.PkuHelperActivity;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class Settings extends Fragment {
	static final Settings settings = new Settings();

	public static ScrollView settingView = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.settings_view,
				container, false);
		settingView = (ScrollView) rootView.findViewById(R.id.settings_view);
		setName();
		setOthers();
		Lib.setBadgeView();



		/*
		* @TODO
		* @DEV
		* */
        settingView.findViewById(R.id.settings_table_name)
                .setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Intent intent = new Intent(getActivity(), PkuHelperActivity.class);
                        startActivity(intent);
                        return true;
                    }
                });
        //END-DEV


		return rootView;
	}

	public static void setName() {
		try {
			if (!Constants.isLogin()) {
				ViewSetting.setTextView(settingView, R.id.settings_name, "点击登录...");
				ViewSetting.setTextView(settingView, R.id.settings_id, "");
			} else {
				ViewSetting.setTextView(settingView, R.id.settings_name, Constants.name);
				ViewSetting.setTextView(settingView, R.id.settings_id, Constants.username);
			}
		} catch (Exception e) {
		}
	}

	public static void setOthers() {


        //TODO MAR 27
		ViewSetting.setSwitchChecked(settingView, R.id.settings_switch_beta, Editor.getBoolean(PKUHelper.pkuhelper, "beta_version", false));
		ViewSetting.setSwitchOnCheckChangeListener(settingView, R.id.settings_switch_beta, new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor.putBoolean(PKUHelper.pkuhelper, "beta_version", isChecked);
			}
		});
        //DEV


		ViewSetting.setOnClickListener(settingView, R.id.settings_table_name, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Constants.isLogin()) {
                    IAAA.showLoginView();
                    return;
                }
                String message = "";
                message += "姓名：    " + Constants.name + "\n";
                message += "学号：    " + Constants.username + "\n";
                message += "性别：    " + Constants.sex + "\n";
                message += "院系：    " + Constants.major + "\n";
                //  message+="User-token: "+Constants.user_token+"\n";
                new AlertDialog.Builder(PKUHelper.pkuhelper).setTitle("详细信息").setCancelable(true)
                        .setMessage(message).setPositiveButton("确定", null)
                        .setNegativeButton("注销", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Constants.reset(PKUHelper.pkuhelper);
                                CustomToast.showSuccessToast(PKUHelper.pkuhelper, "注销成功");
                                setName();
                                IAAA.showLoginView();
                            }
                        }).show();
            }
        });

		ViewSetting.setOnClickListener(settingView, R.id.settings_gesture, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				PKUHelper.pkuhelper.startActivity(new Intent(PKUHelper.pkuhelper, GestureActivity.class));
			}
		});
		ViewSetting.setOnClickListener(settingView, R.id.settings_course, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PKUHelper.pkuhelper, SubActivity.class);
				intent.putExtra("type", Constants.SUBACTIVITY_TYPE_COURSE_SET);
				PKUHelper.pkuhelper.startActivity(intent);
			}
		});
		ViewSetting.setOnClickListener(settingView, R.id.settings_notifications, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PKUHelper.pkuhelper, SubActivity.class);
				intent.putExtra("type", Constants.SUBACTIVITY_TYPE_NOTIFICATIONS);
				PKUHelper.pkuhelper.startActivity(intent);
			}
		});
		ViewSetting.setOnClickListener(settingView, R.id.settings_ipgw, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PKUHelper.pkuhelper, SubActivity.class);
				intent.putExtra("type", Constants.SUBACTIVITY_TYPE_IPGW_SET);
				PKUHelper.pkuhelper.startActivity(intent);
			}
		});
		ViewSetting.setSwitchChecked(settingView, R.id.settings_switch_pkumail, Editor.getBoolean(PKUHelper.pkuhelper, "pkumail_fill", true));
		ViewSetting.setSwitchOnCheckChangeListener(settingView, R.id.settings_switch_pkumail, new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor.putBoolean(PKUHelper.pkuhelper, "pkumail_fill", isChecked);
			}
		});

		ViewSetting.setOnClickListener(settingView, R.id.settings_petest, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PE.setPeTestPassword();
			}
		});
		ViewSetting.setOnClickListener(settingView, R.id.settings_clearcache, new View.OnClickListener() {
			public void onClick(View v) {
				clearCache();
			}
		});
		ViewSetting.setOnClickListener(settingView, R.id.settings_tablerow_update, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Constants.version.equals(Constants.updateVersion)) {
					CustomToast.showInfoToast(PKUHelper.pkuhelper, "已是最新版！");
					return;
				}
				new AlertDialog.Builder(PKUHelper.pkuhelper)
						.setTitle("存在版本" + Constants.updateVersion + "更新！").setMessage(
						Constants.updateMessage)
						.setCancelable(true).setPositiveButton("立即下载", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Uri uri = Uri.parse(Constants.domain + "/applications/pkuhelper/getandroid.php");
						try {
							Request request = new Request(uri);
							request.setTitle("正在下载PKU Helper...");
							File file = MyFile.getFile(PKUHelper.pkuhelper, null, "PKUHelper.apk");
							if (file.exists()) file.delete();
							request.setDestinationUri(Uri.fromFile(file));
							request.setDescription("文件保存在" + file.getAbsolutePath());
							request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
							request.setMimeType("application/vnd.android.package-archive");
							request.allowScanningByMediaScanner();
							DownloadManager downloadManager = (DownloadManager) PKUHelper.pkuhelper.getSystemService(Context.DOWNLOAD_SERVICE);
							downloadManager.enqueue(request);
							CustomToast.showInfoToast(PKUHelper.pkuhelper, "正在下载中，请在通知栏查看下载进度");
						} catch (Exception e) {
							PKUHelper.pkuhelper.startActivity(new Intent(Intent.ACTION_VIEW, uri));
						}
					}
				}).setNegativeButton("关闭", null).show();

			}
		});
		ViewSetting.setOnClickListener(settingView, R.id.settings_about, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PKUHelper.pkuhelper, SubActivity.class);
				intent.putExtra("type", Constants.SUBACTIVITY_TYPE_ABOUT);
				PKUHelper.pkuhelper.startActivity(intent);
			}
		});
		ViewSetting.setOnClickListener(settingView, R.id.settings_faq, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PKUHelper.pkuhelper, SubActivity.class);
				intent.putExtra("type", Constants.SUBACTIVITY_TYPE_WEBVIEW);
				intent.putExtra("url", Constants.domain + "/pkuhelper/faq/?user_token="
						+ Constants.user_token + "#Android");
				intent.putExtra("title", "常见FAQ");
				intent.putExtra("content", "PKU Helper for Android 常见FAQ汇总");
				intent.putExtra("sid", 10);
				PKUHelper.pkuhelper.startActivity(intent);
			}
		});
		ViewSetting.setOnClickListener(settingView, R.id.settings_report, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
//				sendInfo(0);
				Intent intent = new Intent(PKUHelper.pkuhelper, ChatActivity.class);
				intent.putExtra("uid", "10");
				PKUHelper.pkuhelper.startActivity(intent);
			}
		});
		ViewSetting.setOnClickListener(settingView, R.id.settings_recommended, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Share.urlToWx(PKUHelper.pkuhelper, Constants.domain + "/applications/detail.php?id=2",
						"快来使用PKU Helper吧~", "（请在浏览器中打开）\n网关，课表，BBS等等应有尽有~\n快来下载使用吧~",
						null, false);
			}
		});

	}

	public static void finishCheckUpdate(String string) {
		try {
			JSONObject jsonObject = new JSONObject(string);
			JSONObject versionJsonObject = jsonObject.getJSONObject("versions");
			String version = versionJsonObject.getString("Android");
			JSONObject msgJsonObject = jsonObject.getJSONObject("versionmsg");
			String msg = msgJsonObject.getString("Android");
			if (Constants.version.equals(version)) {
				CustomToast.showInfoToast(PKUHelper.pkuhelper, "已是最新版！");
				return;
			} else {
				new AlertDialog.Builder(PKUHelper.pkuhelper)
						.setTitle("存在版本" + version + "更新！").setMessage(msg)
						.setCancelable(true).setPositiveButton("立即下载", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Uri uri = Uri.parse(Constants.domain + "/applications/pkuhelper/getandroid.php");
						try {
							Request request = new Request(uri);
							request.setTitle("正在下载PKU Helper...");
							File file = MyFile.getFile(PKUHelper.pkuhelper, null, "PKUHelper.apk");
							if (file.exists()) file.delete();
							request.setDestinationUri(Uri.fromFile(file));
							request.setDescription("文件保存在" + file.getAbsolutePath());
							request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
							request.setMimeType("application/vnd.android.package-archive");
							request.allowScanningByMediaScanner();
							DownloadManager downloadManager = (DownloadManager) PKUHelper.pkuhelper.getSystemService(Context.DOWNLOAD_SERVICE);
							downloadManager.enqueue(request);
							CustomToast.showInfoToast(PKUHelper.pkuhelper, "正在下载中，请在通知栏查看下载进度");
						} catch (Exception e) {
							PKUHelper.pkuhelper.startActivity(new Intent(Intent.ACTION_VIEW, uri));
						}
					}
				}).setNegativeButton("关闭", null).show();
			}
		} catch (Exception e) {
			CustomToast.showErrorToast(PKUHelper.pkuhelper, "检查更新失败");
		}
	}

	public static void sendFound() {
		final Dialog dialog = new Dialog(PKUHelper.pkuhelper);
		dialog.setContentView(R.layout.settings_found);
		dialog.setTitle("为捡到的校园卡寻找失主");
		Spinner spinner = (Spinner) dialog.findViewById(R.id.settings_found_type);
		final String[] strings = {"校园卡", "学生证", "其他"};
		spinner.setAdapter(new ArrayAdapter<>(PKUHelper.pkuhelper, android.R.layout.simple_spinner_item, strings));
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
									   int position, long id) {
				EditText editText = (EditText) dialog.findViewById(R.id.settings_found_name);
				if (position != 2) {
					editText.setText(strings[position]);
					editText.setEnabled(false);
				} else {
					editText.setEnabled(true);
					editText.setText("");
					editText.requestFocus();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		spinner.setSelection(0);

		ViewSetting.setOnClickListener(dialog, R.id.settings_found_send, new View.OnClickListener() {
			@Override
			@SuppressWarnings("unchecked")
			public void onClick(View v) {

				String to = ViewSetting.getEditTextValue(dialog, R.id.settings_found_username);
				String name = ViewSetting.getEditTextValue(dialog, R.id.settings_found_name);
				String phone = ViewSetting.getEditTextValue(dialog, R.id.settings_found_phone);

				if ("".equals(to) || "".equals(name) || "".equals(phone)) {
					CustomToast.showInfoToast(PKUHelper.pkuhelper, "信息不能为空！", 1300);
					return;
				}

				String content = to + "同学你好！我拾到了你的" + name + "，请尽快找我认领，谢谢。我的联系方式是" + phone + "。";

				ArrayList<Parameters> arrayList = new ArrayList<>();
				arrayList.add(new Parameters("to", to));
				arrayList.add(new Parameters("content", content));
				arrayList.add(new Parameters("type", "sendmsg"));
				arrayList.add(new Parameters("token", Constants.token));

				new RequestingTask(PKUHelper.pkuhelper, "正在发送...", Constants.domain + "/services/msg.php",
						Constants.REQUEST_FOUND_USERNAME).execute(arrayList);

				dialog.dismiss();
			}
		});
		ViewSetting.setOnClickListener(dialog, R.id.settings_found_cancel, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	public static void finishFound(String string) {
		try {
			JSONObject jsonObject = new JSONObject(string);
			int code = jsonObject.getInt("code");
			if (code != 0) {
				CustomToast.showErrorToast(PKUHelper.pkuhelper, jsonObject.optString("msg", "发送失败"));
				return;
			}
			new AlertDialog.Builder(PKUHelper.pkuhelper).setTitle("发送成功！")
					.setMessage("对方将收到一条你发出的信息。\n你也可以在我的消息中进行查看。").setPositiveButton("确认", null).show();
		} catch (Exception e) {
			CustomToast.showErrorToast(PKUHelper.pkuhelper, "发送失败");
		}
	}

	public static void sendInfo(int type) {
		final Dialog dialog = new Dialog(PKUHelper.pkuhelper);
		dialog.setContentView(R.layout.about_report);

		EditText editText = (EditText) dialog.findViewById(R.id.about_report_text);

		if (type == 0) {
			dialog.setTitle("意见反馈");
			editText.setHint(R.string.report_bug);
		} else {
			dialog.setTitle("新功能建议");
			editText.setHint(R.string.report_new);
		}


		ViewSetting.setOnClickListener(dialog, R.id.about_report_cancel, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		ViewSetting.setOnClickListener(dialog, R.id.about_report_reset, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewSetting.setEditTextValue(dialog, R.id.about_report_text, "");
			}
		});
		ViewSetting.setOnClickListener(dialog, R.id.about_report_post, new View.OnClickListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String string = ViewSetting.getEditTextValue(dialog, R.id.about_report_text);
				if ("".equals(string)) {
					CustomToast.showInfoToast(PKUHelper.pkuhelper, "内容不能为空");
					return;
				}

				ArrayList<Parameters> arrayList = new ArrayList<>();
				arrayList.add(new Parameters("to", "10"));
				arrayList.add(new Parameters("content", string));
				arrayList.add(new Parameters("type", "sendmsg"));
				arrayList.add(new Parameters("token", Constants.token));

				new RequestingTask(PKUHelper.pkuhelper, "正在发布...", Constants.domain + "/services/msg.php",
						Constants.REQUEST_REPORT).execute(arrayList);
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	public static void finishReport(String string) {
		try {
			JSONObject jsonObject = new JSONObject(string);
			int code = jsonObject.getInt("code");
			if (code != 0) {
				CustomToast.showErrorToast(PKUHelper.pkuhelper, jsonObject.optString("msg", "发送失败"));
				return;
			}
			new AlertDialog.Builder(PKUHelper.pkuhelper).setTitle("反馈成功！")
					.setMessage("请注意我的消息中是否有回复。").setPositiveButton("确认", null).show();
		} catch (Exception e) {
			CustomToast.showErrorToast(PKUHelper.pkuhelper, "反馈失败");
		}
	}

	public static void clearCache() {
		File file = MyFile.getCache(PKUHelper.pkuhelper, null);

		String msg = "缓存路径：\n" + file.getAbsolutePath() + "/" + "\n\n文件数目：" + MyFile.getFileCount(file)
				+ "\n缓存大小：" + MyFile.getFileSizeString(file);

		new AlertDialog.Builder(PKUHelper.pkuhelper)
				.setTitle("清除缓存").setMessage(msg).setPositiveButton("清除", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				MyFile.clearCache(PKUHelper.pkuhelper);
				CustomToast.showSuccessToast(PKUHelper.pkuhelper, "清除成功！");
			}
		}).setNegativeButton("取消", null).show();
	}

}
