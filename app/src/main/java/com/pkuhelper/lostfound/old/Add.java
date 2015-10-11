package com.pkuhelper.lostfound.old;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import org.json.JSONObject;

import android.app.*;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.MyBitmapFactory;
import com.pkuhelper.lib.MyFile;
import com.pkuhelper.lib.Util;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.view.MyDatePickerDialog;
import com.pkuhelper.lib.view.MyTimePickerDialog;
import com.pkuhelper.lib.webconnection.Parameters;

public class Add {
	static Calendar calendar;
	static Uri imageUri=null;
	static Uri tempUri=null;
	static ArrayList<String> arrayList;
	static String[] typeList={"card","book","device","other"};
	
	public static void showAddView() {
		LostFoundActivity lostFoundActivity=LostFoundActivity.lostFoundActivity;
		lostFoundActivity.setContentView(R.layout.lostfound_add);
		lostFoundActivity.getActionBar().setTitle("发布新的失物招领");
		lostFoundActivity.invalidateOptionsMenu();
		lostFoundActivity.nowShowing=LostFoundActivity.PAGE_ADD;
		
		// 设置下拉框
		Spinner spinner=(Spinner)lostFoundActivity.findViewById(R.id.lostfound_add_spinner);
		arrayList=new ArrayList<String>();
		arrayList.add("卡片或钱包");
		arrayList.add("书籍或笔记本");
		arrayList.add("电子设备");
		arrayList.add("其他");
		ArrayAdapter<String> spinnerAdapter=new ArrayAdapter<String>(lostFoundActivity, android.R.layout.simple_spinner_item, arrayList);
		spinner.setAdapter(spinnerAdapter);
		
		// 设置日期和时间部分
		calendar=Calendar.getInstance(Locale.CHINA);
		String date=new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(calendar.getTime());
		ViewSetting.setTextView(lostFoundActivity, R.id.lostfound_add_date, date);
		String time=new SimpleDateFormat("HH:mm", Locale.CHINA).format(calendar.getTime());
		ViewSetting.setTextView(lostFoundActivity, R.id.lostfound_add_time, time);
		ViewSetting.setOnClickListener(lostFoundActivity, R.id.lostfound_tablerow_date, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int year=calendar.get(Calendar.YEAR);
				int monthOfYear=calendar.get(Calendar.MONTH);
				int dayOfMonth=calendar.get(Calendar.DAY_OF_MONTH);
				MyDatePickerDialog datePickerDialog=new MyDatePickerDialog(LostFoundActivity.lostFoundActivity, new DatePickerDialog.OnDateSetListener() {
					
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear,
							int dayOfMonth) {
						//String date=year+"-"+monthOfYear+"-"+dayOfMonth;
						calendar.set(Calendar.YEAR, year);
						calendar.set(Calendar.MONTH, monthOfYear);
						calendar.set(Calendar.DATE, dayOfMonth);
						ViewSetting.setTextView(LostFoundActivity.lostFoundActivity, R.id.lostfound_add_date, 
								new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime()));
					}
				}, year, monthOfYear, dayOfMonth);
				
				datePickerDialog.setPermanentTitle("选择日期");
				datePickerDialog.setCancelable(true);
				datePickerDialog.setCanceledOnTouchOutside(true);
				datePickerDialog.show();
				
			}
		});
		ViewSetting.setOnClickListener(lostFoundActivity, R.id.lostfound_tablerow_time, new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MyTimePickerDialog timePickerDialog=new MyTimePickerDialog(LostFoundActivity.lostFoundActivity, new TimePickerDialog.OnTimeSetListener() {
					
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
						calendar.set(Calendar.MINUTE, minute);
						ViewSetting.setTextView(LostFoundActivity.lostFoundActivity, R.id.lostfound_add_time, 
								new SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.getTime()));
					}
				}, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
				timePickerDialog.setPermanentTitle("选择时间");
				timePickerDialog.setCancelable(true);
				timePickerDialog.setCanceledOnTouchOutside(true);
				timePickerDialog.show();
			}
		});
		
		// 设置图片和图片按钮
		imageUri=null;
		setImage(null);
		ViewSetting.setOnClickListener(lostFoundActivity, R.id.lostfound_add_image_select, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectImage();
			}
		});
		ViewSetting.setOnClickListener(lostFoundActivity, R.id.lostfound_add_image_delete, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				imageUri=null;
				setImage(null);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public static void confirm() {
		LostFoundActivity lostFoundActivity=LostFoundActivity.lostFoundActivity;
		String name=ViewSetting.getEditTextValue(lostFoundActivity, R.id.lostfound_add_name);
		if ("".equals(name)) {
			CustomToast.showInfoToast(lostFoundActivity, "物品名称必须填写");
			(lostFoundActivity.findViewById(R.id.lostfound_add_name)).requestFocus();
			return;
		}
		String phone=ViewSetting.getEditTextValue(lostFoundActivity, R.id.lostfound_add_phone);
		if ("".equals(phone)) {
			CustomToast.showInfoToast(lostFoundActivity, "电话号码必须填写");
			(lostFoundActivity.findViewById(R.id.lostfound_add_phone)).requestFocus();
			return;
		}
		String detail=ViewSetting.getEditTextValue(lostFoundActivity, R.id.lostfound_add_detail);
		Spinner spinner=(Spinner)lostFoundActivity.findViewById(R.id.lostfound_add_spinner);
		String type=typeList[spinner.getSelectedItemPosition()];
		String lost_or_found=ViewSetting.getSwitchChecked(lostFoundActivity, 
				R.id.lostfound_add_switch)?"found":"lost";
		
		//String posttime=Calendar.getInstance(Locale.CHINA).getTimeInMillis()/1000+"";
		//String hash=Util.getHash(posttime+"19940804");
		String imageString=null;
		if (imageUri!=null) {
			byte[] bts=MyBitmapFactory.getCompressedBitmapBytes(imageUri.getPath(), 3);
			imageString=Base64.encodeToString(bts, Base64.DEFAULT);
		}
		ArrayList<Parameters> arrayList=new ArrayList<Parameters>();
		arrayList.add(new Parameters("name",name));
		arrayList.add(new Parameters("type",type));
		arrayList.add(new Parameters("detail",detail));
		arrayList.add(new Parameters("action_time",calendar.getTimeInMillis()/1000+""));
		//arrayList.add(new Parameters("poster_name",Constants.name));
		//arrayList.add(new Parameters("poster_uid",Constants.username));
		arrayList.add(new Parameters("poster_phone",phone));
		//arrayList.add(new Parameters("poster_college",Constants.major));
		arrayList.add(new Parameters("lost_or_found", lost_or_found));
		//arrayList.add(new Parameters("timestamp", posttime));
		//arrayList.add(new Parameters("hash", hash));
		arrayList.add(new Parameters("token", Constants.token));
		if (imageString!=null)
			arrayList.add(new Parameters("imageData", imageString));
		
		new RequestingTask("正在发布...", Constants.domain+"/services/LFpost.php",
				Constants.REQUEST_LOSTFOUND_ADD).execute(arrayList);
	}
	
	private static void setImage(Bitmap bitmap) {
		LostFoundActivity lostFoundActivity=LostFoundActivity.lostFoundActivity;
		
		Drawable defaultDrawable=lostFoundActivity.getResources().getDrawable(R.drawable.image);
		if (bitmap==null)
			ViewSetting.setImageDrawable(lostFoundActivity, R.id.lostfound_add_image, defaultDrawable);
		else
			ViewSetting.setImageBitmap(lostFoundActivity, R.id.lostfound_add_image, bitmap);
	}
	
	private static void selectImage() {
		String[] strings={"相册","拍照"};
		new AlertDialog.Builder(LostFoundActivity.lostFoundActivity)
		.setTitle("选择照片").setItems(strings, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which==0) {
					Intent imageIntent=new Intent(Intent.ACTION_GET_CONTENT);
					//imageIntent.addCategory(Intent.CATEGORY_OPENABLE);
					imageIntent.setType("image/*");
					LostFoundActivity.lostFoundActivity.startActivityForResult(imageIntent, 0);
				}
				else {
					Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					tempUri=Uri.fromFile(
							MyFile.getFile(LostFoundActivity.lostFoundActivity, "camera",
									Util.getHash("lostfound")+".jpg"));
					cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
					LostFoundActivity.lostFoundActivity.startActivityForResult(cameraIntent, 1);
				}
			}
		}).setCancelable(true).show();
	}
	
	public static void finishSelectImage(int code, Intent data) {
		LostFoundActivity lostFoundActivity=LostFoundActivity.lostFoundActivity;
		// 选取图片，从相册
		if (code==0) {
			Uri uri=data.getData();
			if (uri==null) return;
			imageUri=uri;
			try {
				String[] proj={MediaColumns.DATA};
				CursorLoader loader=new CursorLoader(lostFoundActivity, uri, proj, null, null, null);
				Cursor cursor = loader.loadInBackground();
				int index=cursor.getColumnIndexOrThrow(MediaColumns.DATA);
				cursor.moveToFirst();
				String filePath=cursor.getString(index);
				Log.w("filePath", filePath);
				imageUri=Uri.fromFile(new File(filePath));
				
				// 压缩图片
				Bitmap bitmap=MyBitmapFactory.getCompressedBitmap(imageUri.getPath(), 1);
				setImage(bitmap);
				
			} catch (Exception e) {}
		}
		// 选取图片，从拍照
		else if (code==1) {
			imageUri=tempUri;
			Bitmap bitmap=MyBitmapFactory.getCompressedBitmap(imageUri.getPath(), 1);
			setImage(bitmap);
		}
	}
	
	public static void finishRequest(String string) {
		try {
			JSONObject jsonObject=new JSONObject(string);
			int success=jsonObject.getInt("success");
			if (success==0) {
				new AlertDialog.Builder(LostFoundActivity.lostFoundActivity)
				.setTitle("发布失败").setMessage(jsonObject.optString("reason"))
				.setCancelable(true).setPositiveButton("确定", null).show();
				return;
			}
			CustomToast.showSuccessToast(LostFoundActivity.lostFoundActivity, "发布成功！");
			MyLostFound.getMyInfo();
		}
		catch (Exception e) {
			CustomToast.showErrorToast(LostFoundActivity.lostFoundActivity, "发布失败，请重试");
		}
		
	}
	
}
