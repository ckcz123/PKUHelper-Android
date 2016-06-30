package com.pkuhelper.lostfound;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.pkuhelper.R;
import com.pkuhelper.lib.BaseActivity;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.MyBitmapFactory;
import com.pkuhelper.lib.MyFile;
import com.pkuhelper.lib.RequestingTask;
import com.pkuhelper.lib.Util;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.view.MyDatePickerDialog;
import com.pkuhelper.lib.view.MyTimePickerDialog;
import com.pkuhelper.lib.webconnection.Parameters;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AddActivity extends BaseActivity {
	ArrayList<String> arrayList = new ArrayList<String>();
	Calendar calendar = null;
	Uri imageUri = null, tempUri = null;
	static String[] typeList = {"card", "book", "device", "other"};
	String type = "";
	byte[] bts = null;

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setTitle("发布新的失物招领");
		setContentView(R.layout.lostfound_add);
		setResult(RESULT_CANCELED);

		Spinner spinner = (Spinner) findViewById(R.id.lostfound_add_spinner);
		arrayList.add("卡片或钱包");
		arrayList.add("书籍或笔记本");
		arrayList.add("电子设备");
		arrayList.add("其他");
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrayList);
		spinner.setAdapter(arrayAdapter);

		calendar = Calendar.getInstance(Locale.getDefault());
		String date = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(calendar.getTime());
		ViewSetting.setTextView(this, R.id.lostfound_add_date, date);
		String time = new SimpleDateFormat("HH:mm", Locale.CHINA).format(calendar.getTime());
		ViewSetting.setTextView(this, R.id.lostfound_add_time, time);
		ViewSetting.setOnClickListener(this, R.id.lostfound_tablerow_date, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int year = calendar.get(Calendar.YEAR);
				int monthOfYear = calendar.get(Calendar.MONTH);
				int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
				MyDatePickerDialog datePickerDialog = new MyDatePickerDialog(AddActivity.this, new DatePickerDialog.OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear,
										  int dayOfMonth) {
						calendar.set(Calendar.YEAR, year);
						calendar.set(Calendar.MONTH, monthOfYear);
						calendar.set(Calendar.DATE, dayOfMonth);
						ViewSetting.setTextView(this, R.id.lostfound_add_date,
								new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime()));
					}
				}, year, monthOfYear, dayOfMonth);

				datePickerDialog.setPermanentTitle("选择日期");
				datePickerDialog.setCancelable(true);
				datePickerDialog.setCanceledOnTouchOutside(true);
				datePickerDialog.show();

			}
		});
		ViewSetting.setOnClickListener(this, R.id.lostfound_tablerow_time, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				MyTimePickerDialog timePickerDialog = new MyTimePickerDialog(AddActivity.this, new TimePickerDialog.OnTimeSetListener() {

					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
						calendar.set(Calendar.MINUTE, minute);
						ViewSetting.setTextView(this, R.id.lostfound_add_time,
								new SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.getTime()));
					}
				}, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
				timePickerDialog.setPermanentTitle("选择时间");
				timePickerDialog.setCancelable(true);
				timePickerDialog.setCanceledOnTouchOutside(true);
				timePickerDialog.show();
			}
		});
		imageUri = null;
		setImage(null);
		ViewSetting.setOnClickListener(this, R.id.lostfound_add_image_select, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectImage();
			}
		});
		ViewSetting.setOnClickListener(this, R.id.lostfound_add_image_delete, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				imageUri = null;
				setImage(null);
			}
		});

	}

	@SuppressWarnings("unchecked")
	public void confirm() {
		String name = ViewSetting.getEditTextValue(this, R.id.lostfound_add_name);
		if ("".equals(name)) {
			CustomToast.showInfoToast(this, "物品名称必须填写");
			findViewById(R.id.lostfound_add_name).requestFocus();
			return;
		}
		String phone = ViewSetting.getEditTextValue(this, R.id.lostfound_add_phone);
		if ("".equals(phone)) {
			CustomToast.showInfoToast(this, "电话号码必须填写");
			findViewById(R.id.lostfound_add_phone).requestFocus();
			return;
		}
		String detail = ViewSetting.getEditTextValue(this, R.id.lostfound_add_detail);
		Spinner spinner = (Spinner) findViewById(R.id.lostfound_add_spinner);
		String type = typeList[spinner.getSelectedItemPosition()];
		String lost_or_found = ViewSetting.getSwitchChecked(this,
				R.id.lostfound_add_switch) ? "found" : "lost";
		String imageString = null;
		if (bts != null) {
			imageString = Base64.encodeToString(bts, Base64.DEFAULT);
		}
		ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
		arrayList.add(new Parameters("name", name));
		arrayList.add(new Parameters("type", type));
		arrayList.add(new Parameters("detail", detail));
		arrayList.add(new Parameters("action_time", calendar.getTimeInMillis() / 1000 + ""));
		arrayList.add(new Parameters("poster_phone", phone));
		arrayList.add(new Parameters("lost_or_found", lost_or_found));
		arrayList.add(new Parameters("token", Constants.token));
		if (imageString != null)
			arrayList.add(new Parameters("imageData", imageString));

		new RequestingTask(this, "正在发布...", Constants.domain + "/services/LFpost.php",
				Constants.REQUEST_LOSTFOUND_ADD).execute(arrayList);
		this.type = lost_or_found;
	}

	void setImage(Bitmap bitmap) {
		if (bitmap == null) {
			ViewSetting.setImageResource(this, R.id.lostfound_add_image, R.drawable.image);
			bts = null;
		} else
			ViewSetting.setImageBitmap(this, R.id.lostfound_add_image, bitmap);
	}

	void selectImage() {
		String[] strings = {"拍照", "从相册选取"};
		new AlertDialog.Builder(this)
				.setItems(strings, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == 0) {
							Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
							tempUri = Uri.fromFile(
									MyFile.getFile(AddActivity.this, "camera", Util.getHash("lostfound") + ".jpg"));
							cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
							startActivityForResult(cameraIntent, 0);
						} else {
							Intent imageIntent = new Intent(Intent.ACTION_GET_CONTENT);
							imageIntent.setType("image/*");
							startActivityForResult(imageIntent, 1);
						}
					}
				}).setCancelable(true).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) return;
		if (requestCode == 0) {
			imageUri = tempUri;
			Bitmap bitmap = MyBitmapFactory.getCompressedBitmap(imageUri.getPath(), 2);
			bts = MyBitmapFactory.bitmapToArray(bitmap);
			setImage(bitmap);
		}
		if (requestCode == 1) {
			Uri uri = data.getData();
			if (uri == null) return;
			imageUri = uri;
			try {
				String[] proj = {MediaColumns.DATA};
				CursorLoader loader = new CursorLoader(this, uri, proj, null, null, null);
				Cursor cursor = loader.loadInBackground();
				int index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
				cursor.moveToFirst();
				String filePath = cursor.getString(index);
				Log.w("filePath", filePath);
				imageUri = Uri.fromFile(new File(filePath));
				Bitmap bitmap = MyBitmapFactory.getCompressedBitmap(imageUri.getPath(), 2);
				bts = MyBitmapFactory.bitmapToArray(bitmap);
				setImage(bitmap);
			} catch (Exception e) {
			}
		}
	}

	public void finishRequest(int type, String string) {
		if (type == Constants.REQUEST_LOSTFOUND_ADD) {
			try {
				JSONObject jsonObject = new JSONObject(string);
				int success = jsonObject.getInt("success");
				if (success == 0) {
					new AlertDialog.Builder(this)
							.setTitle("发布失败").setMessage(jsonObject.optString("reason"))
							.setCancelable(true).setPositiveButton("确定", null).show();
					return;
				}
				CustomToast.showSuccessToast(this, "发布成功！");
				Intent result = new Intent();
				result.putExtra("type", this.type);
				setResult(RESULT_OK, result);
				finish();
			} catch (Exception e) {
				CustomToast.showErrorToast(this, "发布失败，请重试");
			}
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(Menu.NONE, Constants.MENU_LOSTFOUND_SAVE,
				Constants.MENU_LOSTFOUND_SAVE, "")
				.setIcon(R.drawable.ic_save_white_48dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(Menu.NONE, Constants.MENU_LOSTFOUND_CLOSE,
				Constants.MENU_LOSTFOUND_CLOSE, "")
				.setIcon(R.drawable.ic_close_white_48dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == Constants.MENU_LOSTFOUND_SAVE) {
			confirm();
			return true;
		}
		if (id == Constants.MENU_LOSTFOUND_CLOSE) {
			wantToExit();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
