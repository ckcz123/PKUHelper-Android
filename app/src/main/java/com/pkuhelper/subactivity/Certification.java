package com.pkuhelper.subactivity;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Message;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.MyBitmapFactory;
import com.pkuhelper.lib.MyFile;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.lib.webconnection.WebConnection;

public class Certification {
	SubActivity subActivity;
	ListView listView;
	ArrayList<CertificationInfo> lists=new ArrayList<CertificationInfo>();
	
	class CertificationInfo {
		int id;
		String event;
		String time;
		String location;
		String price;
		String seat;
		int newPass;
		String serial;
		String description;
		
		public CertificationInfo(int _id, String _event, String _time,
				String _location, String _price, String _seat, int _newPass, String _serial,
				String _description) {
			id=_id;event=_event;time=_time;location=_location;
			price=_price;seat=_seat;newPass=_newPass;serial=_serial;
			description=_description;
		}
	}
	public Certification(SubActivity _subActivity) {
		subActivity=_subActivity;
	}
	@SuppressLint("InflateParams")
	public Certification getCertification(boolean refresh) {
		subActivity.setContentView(R.layout.subactivity_listview);
		subActivity.getActionBar().setTitle("我的凭证");
		subActivity.findViewById(R.id.subactivity_swipeRefreshLayout).setBackgroundColor(Color.parseColor("#fda58c"));
		listView=(ListView)subActivity.findViewById(R.id.subactivity_listview);
		LayoutInflater layoutInflater=subActivity.getLayoutInflater();
		View headerView=layoutInflater.inflate(R.layout.subactivity_listview_headerview, null);
		ViewSetting.setImageResource(headerView, R.id.subactivity_listview_image, R.drawable.wdpz);
		listView.addHeaderView(headerView);
		listView.addFooterView(layoutInflater.inflate(R.layout.subactivity_listview_footerview, null));
		lists=new ArrayList<CertificationInfo>();
		ArrayList<HashMap<String, String>> arrayList=new ArrayList<HashMap<String,String>>();
		listView.setHeaderDividersEnabled(false);
		listView.setFooterDividersEnabled(false);
		listView.setAdapter(new SimpleAdapter(subActivity, arrayList, 
				R.layout.subactivity_listview_item, new String[] {}, new int[] {}));
		if (!refresh) {
			File file=MyFile.getFile(subActivity, Constants.username, "passbook");
			if (!file.exists()) {
				try {
					file.createNewFile();
				}catch (Exception e) {}
				refresh();
			}
			else {
				try {
					FileInputStream fileInputStream=new FileInputStream(file);
					byte[] bts=new byte[(int)file.length()];
					fileInputStream.read(bts);
					fileInputStream.close();
					String str=new String(Base64.decode(bts, Base64.DEFAULT),"utf-8");
					finishRequest(str, true);
				}
				catch (Exception e) {refresh();}
			}
		}
		else refresh();
		return this;
	}
	@SuppressWarnings("unchecked")
	public void refresh() {
		lists=new ArrayList<CertificationInfo>();
		ArrayList<Parameters> arrayList=new ArrayList<Parameters>();
		arrayList.add(new Parameters("token", Constants.token));
		
		new RequestingTask(subActivity, "正在获取我的凭证...", 
				Constants.domain+"/services/pass.php", Constants.REQUEST_SUBACTIVITY_CERTIFICATION)
				.execute(arrayList);
	}
	
	public void pullToRefresh() {
		new Thread(new Runnable() {
			public void run() {
				Parameters parameters=WebConnection.connect(Constants.domain+"/services/pass.php?token="+Constants.token,
						null);
				if ("200".equals(parameters.name))
					subActivity.handler.sendMessage(Message.obtain(
							subActivity.handler, Constants.MESSAGE_SUBACTIVITY_CERTIFICATION, parameters.value));
				else
					subActivity.setRefresh();
			}
		}).start();
	}
	
	private void sort() {
		Collections.sort(lists, new Comparator<CertificationInfo>() {
			@Override
			public int compare(CertificationInfo c1, CertificationInfo c2) {
				if (c1.newPass>c2.newPass) return -1;
				if (c1.newPass<c2.newPass) return 1;
				if (c1.id>c2.id) return -1;
				return 1;
			}
		});
	}

	public void finishRequest(String string, boolean isFromFile) {
		lists=new ArrayList<CertificationInfo>();
		try {
			JSONObject jsonObject=new JSONObject(string);
			int code=jsonObject.getInt("error");
			if (code!=0) {
				CustomToast.showErrorToast(subActivity, jsonObject.optString("msg", "凭证获取失败"));
				return;
			}
			JSONArray jsonArray=jsonObject.optJSONArray("passes");
			if (jsonArray==null) {
				return;
			}
			int len=jsonArray.length();
			for (int i=0;i<len;i++) {
				JSONObject object=jsonArray.getJSONObject(i);
				String description=object.optString("info");
				if ("".equals(description)) description="该凭证没有简介";
				lists.add(new CertificationInfo(object.getInt("id"), 
						object.optString("event"), object.optString("time"),
						object.optString("loc"), object.optString("price"),
						object.optString("seat"), object.optInt("newpass"), 
						object.getString("serial"), description));
				object.put("newpass", 0);
				jsonArray.put(i, object);
			}
			sort();
			if (!isFromFile) {
				try {
					MyFile.putString(subActivity, Constants.username, "passbook", string);
				}
				catch (Exception e) {}
			}
			showList();
		}
		catch (Exception e) {
			if (isFromFile)
				refresh();
			else
				CustomToast.showErrorToast(subActivity, "凭证获取失败");
			return;
		}
	}
	
	public void showList() {
		if (listView==null) return;
		listView.setAdapter(new BaseAdapter() {
			
			@SuppressLint("ViewHolder")
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				CertificationInfo certificationInfo=lists.get(position);
				convertView=subActivity.getLayoutInflater()
						.inflate(R.layout.subactivity_certification_listitem, parent, false);
				ViewSetting.setTextView(convertView, R.id.subactivity_listitem_title, certificationInfo.event);
				ViewSetting.setTextViewBold(convertView, R.id.subactivity_listitem_title);
				ViewSetting.setTextView(convertView, R.id.subactivity_listitem_description, certificationInfo.description);
				ViewSetting.setTextView(convertView, R.id.subactivity_listitem_location_text, certificationInfo.location);
				ViewSetting.setTextView(convertView, R.id.subactivity_listitem_time_text, certificationInfo.time);
				ViewSetting.setTextView(convertView, R.id.subactivity_listitem_seat_text, certificationInfo.seat);
				return convertView;
			}
						
			@Override
			public long getItemId(int position) {
				return 0;
			}
			
			@Override
			public Object getItem(int position) {
				return null;
			}
			
			@Override
			public int getCount() {
				return lists.size();
			}
		});
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (position==0 || position==parent.getCount()-1) return;
				CertificationInfo certificationInfo=lists.get(position-1);
				String serial=certificationInfo.serial;
				Bitmap bitmap=MyBitmapFactory.generateQRCode(serial);
				if (bitmap==null) {
					CustomToast.showInfoToast(subActivity, "无法生成二维码");
					String hint="请在电脑上搜索\"二维码在线生成\"，并输入以下字符：\n"
							+serial+"\n将得到的二维码保存在你的手机中作为凭证。";
					new AlertDialog.Builder(subActivity).setTitle("无法生成凭证。")
					.setPositiveButton("确认", null).setMessage(hint).show();
					return;
				}
				AlertDialog.Builder builder=new AlertDialog.Builder(subActivity)
				.setTitle(certificationInfo.event).setPositiveButton("确认", null).setCancelable(true);
				ImageView imageView=new ImageView(builder.getContext());				
				imageView.setLayoutParams(new ViewGroup.LayoutParams(bitmap.getWidth(), bitmap.getHeight()));
				imageView.setImageBitmap(bitmap);
				builder.setView(imageView).show();
			}
		});
	}
	
}
