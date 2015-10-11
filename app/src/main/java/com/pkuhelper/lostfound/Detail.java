package com.pkuhelper.lostfound;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import com.pkuhelper.R;
import com.pkuhelper.chat.ChatActivity;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.MyFile;
import com.pkuhelper.lib.Util;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.subactivity.SubActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class Detail {
	@SuppressLint("InflateParams")
	public static void showDetail(final LostFoundActivity lostFoundActivity, final LostFoundInfo lostFoundInfo) {
		final Dialog detaildialog=new Dialog(lostFoundActivity);
		detaildialog.setCancelable(true);
		detaildialog.setCanceledOnTouchOutside(true);
		detaildialog.setTitle("查看详细信息");
		detaildialog.setContentView(R.layout.lostfound_detail_listview);
		ListView listView=(ListView)detaildialog.findViewById(R.id.lostfound_detail_listview);
		View footerView=lostFoundActivity.getLayoutInflater().inflate(R.layout.lostfound_detail_buttonview, null);
		
		final String detailString=new String(lostFoundInfo.detail);
		ViewSetting.setOnClickListener(footerView, R.id.lostfound_detail_button_detail, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(lostFoundActivity)
				.setTitle("查看描述").setMessage(detailString).setCancelable(true)
				.setPositiveButton("确认", null).show();
			}
		});
		final String imgUrl=new String(lostFoundInfo.imgURL);
		if (imgUrl==null || "".equals(imgUrl)) {
			footerView.findViewById(R.id.lostfound_detail_button_image).setVisibility(View.GONE);
		}
		else {
			ViewSetting.setOnClickListener(footerView, R.id.lostfound_detail_button_image, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					new ImageRequestingTask(lostFoundActivity, lostFoundInfo.name).execute(imgUrl);
				}
			});
		}
		if (lostFoundActivity.page==LostFoundActivity.PAGE_MINE) {
			Button button=(Button)footerView.findViewById(R.id.lostfound_detail_button_phone);
			button.setText("删除");
			final long posttime=lostFoundInfo.posttime;
			final String imgString=lostFoundInfo.imgName;
			button.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					new AlertDialog.Builder(lostFoundActivity)
					.setTitle("确定删除？").setMessage("确定删除这一条失物招领信息？")
					.setCancelable(true).setNegativeButton("取消", null)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						
						@SuppressWarnings("unchecked")
						@Override
						public void onClick(DialogInterface dialog, int which) {
							detaildialog.dismiss();
							ArrayList<Parameters> arrayList=new ArrayList<Parameters>();
							arrayList.add(new Parameters("posttime", posttime+""));
							arrayList.add(new Parameters("token", Constants.token));
							arrayList.add(new Parameters("imageName", imgString));
							new RequestingTask(lostFoundActivity, "正在删除..", Constants.domain+"/services/LFDelete.php",
									Constants.REQUEST_LOSTFOUND_DELETE).execute(arrayList);
						}
					}).show();
				}
			});
			lostFoundActivity.deletingType=lostFoundInfo.lost_or_found==LostFoundInfo.LOST?"lost":"found";
		}		
		else {
			final String phoneNum=new String(lostFoundInfo.posterPhone);
			ViewSetting.setOnClickListener(footerView, R.id.lostfound_detail_button_phone, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent=new Intent(Intent.ACTION_DIAL, 
							Uri.parse("tel:"+phoneNum));
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					lostFoundActivity.startActivity(intent);
				}
			});
		}
		ViewSetting.setOnClickListener(footerView, R.id.lostfound_detail_button_chat, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//detaildialog.dismiss();
				Intent intent=new Intent(lostFoundActivity, ChatActivity.class);
				intent.putExtra("uid", lostFoundInfo.posterUid);
				lostFoundActivity.startActivity(intent);
			}
		});
		listView.addFooterView(footerView);
		listView.setFooterDividersEnabled(false);
		
		final ArrayList<HashMap<String, String>> arrayList=getList(lostFoundInfo);
		listView.setAdapter(new SimpleAdapter(lostFoundActivity, arrayList, 
				R.layout.lostfound_detail_item, new String[] {"name","value"},
				new int[] {R.id.lostfound_detail_item_name, R.id.lostfound_detail_item_value}));
		
		detaildialog.show();
		
	}
	
	private static ArrayList<HashMap<String, String>> getList(LostFoundInfo lostFoundInfo) {
		ArrayList<HashMap<String, String>> arrayList=new ArrayList<HashMap<String,String>>();
		HashMap<String, String> map=new HashMap<String, String>();
		SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
		
		map=new HashMap<String, String>();
		map.put("name", "物品名称");map.put("value", lostFoundInfo.name);
		arrayList.add(map);
		
		map=new HashMap<String, String>();
		String lost_or_found="失物";
		if (lostFoundInfo.lost_or_found==LostFoundInfo.FOUND) lost_or_found="招领";
		map.put("name", "信息类别");map.put("value", lost_or_found);
		arrayList.add(map);
		
		map=new HashMap<String, String>();
		String type="";
		switch (lostFoundInfo.type) {
		case LostFoundInfo.TYPE_CARD:
			type="卡片或钱包";
			break;
		case LostFoundInfo.TYPE_BOOK:
			type="书籍或笔记本";
			break;
		case LostFoundInfo.TYPE_DEVICE:
			type="电子设备";
			break;
		default:
			type="其他";
			break;
		}
		map.put("name", "物品分类");map.put("value", type);
		arrayList.add(map);
		
		map=new HashMap<String, String>();
		String actiontime_name="丢失时间";
		if (lostFoundInfo.lost_or_found==LostFoundInfo.FOUND) actiontime_name="拾到时间";
		map.put("name", actiontime_name);
		map.put("value", simpleDateFormat.format(new Date(lostFoundInfo.actiontime*1000)));
		arrayList.add(map);
		
		if (!"secret".equals(lostFoundInfo.posterName)) {
			map=new HashMap<String, String>();
			map.put("name", "发布人");
			map.put("value", lostFoundInfo.posterName);
			arrayList.add(map);
		}
		
		if (!"secret".equals(lostFoundInfo.posterUid)) {
			map=new HashMap<String, String>();
			map.put("name", "发布人学号");
			map.put("value", lostFoundInfo.posterUid);
			arrayList.add(map);
		}
		
		if (!"secret".equals(lostFoundInfo.posterCollege)) {
			map=new HashMap<String, String>();
			map.put("name", "发布人学院");
			map.put("value", lostFoundInfo.posterCollege);
			arrayList.add(map);
		}
		map=new HashMap<String, String>();
		map.put("name", "联系电话");
		map.put("value", lostFoundInfo.posterPhone);
		arrayList.add(map);
		
		map=new HashMap<String, String>();
		map.put("name", "发布时间");
		map.put("value", simpleDateFormat.format(new Date(lostFoundInfo.posttime*1000)));
		arrayList.add(map);
		
		return arrayList;
	}
	
	private static void showImage(LostFoundActivity lostFoundActivity, File file, String title) {
		Intent intent=new Intent(lostFoundActivity, SubActivity.class);
		intent.putExtra("type", Constants.SUBACTIVITY_TYPE_PICTURE_FILE);
		intent.putExtra("title", title);
		intent.putExtra("file", file.getAbsolutePath());
		//DataObject.getInstance().setObject(drawable);
		lostFoundActivity.startActivity(intent);
	}
	
	private static class ImageRequestingTask extends AsyncTask<String, String, File> {
		ProgressDialog progressDialog;
		String title;
		LostFoundActivity lostFoundActivity;
		public ImageRequestingTask(LostFoundActivity lostFoundActivity, String title) {
			this.title=title;
			progressDialog=new ProgressDialog(lostFoundActivity);
			this.lostFoundActivity=lostFoundActivity;
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setTitle("提示");
			progressDialog.setMessage("正在获取图片...");
			progressDialog.setIndeterminate(false);
			progressDialog.setCancelable(false);
		}
		@Override
		protected void onPreExecute() {
			progressDialog.show();
		}
		@Override
		protected File doInBackground(String... params) {
			try {
				File file=MyFile.getCache(lostFoundActivity, 
						Util.getHash(params[0]));
				if (file.exists()) return file;
				if (MyFile.urlToFile(params[0], file))
					return file;
				else
					return null;
				//InputStream inputStream=WebConnection.connect(params[0]);
				//Drawable drawable=Drawable.createFromStream(inputStream, params[0]);
				
				//return drawable;
			}
			catch (Exception e) {
				return null;
			}
		}
		@Override
		protected void onPostExecute(File file) {
			progressDialog.dismiss();
			if (file==null) {
				CustomToast.showErrorToast(lostFoundActivity, "图片加载失败");
				return;
			}
			showImage(lostFoundActivity, file, title);
		}
	}
	
}
