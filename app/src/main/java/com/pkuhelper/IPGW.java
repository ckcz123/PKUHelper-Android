package com.pkuhelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import com.pkuhelper.R;
import com.pkuhelper.gesture.GestureActivity;
import com.pkuhelper.lib.*;
import com.pkuhelper.lib.view.CustomToast;
import com.pkuhelper.lib.view.colorpicker.*;
import com.pkuhelper.lib.webconnection.*;
import android.annotation.SuppressLint;
import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.widget.RelativeLayout;

public class IPGW extends Fragment{
	public static View ipgwView=null;
	static String hintString="连接状态：未连接";
	static GestureOverlayView gestureOverlayView;
	public static GestureLibrary gestureLibrary;
	static Drawable drawable;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Lib.checkConnectedStatus(PKUHelper.pkuhelper);
		View rootView = inflater.inflate(R.layout.ipgw_view,
				container, false);
		ipgwView=rootView;
		if (drawable==null) {
			File bgFile=MyFile.getFile(getActivity(), null, "bg.jpg");
			if (!bgFile.exists()) {
				drawable=PKUHelper.pkuhelper.getResources().getDrawable(R.drawable.bg);
				try {
					Bitmap bitmap=((BitmapDrawable)drawable).getBitmap();
					FileOutputStream fileOutputStream=new FileOutputStream(bgFile);
					bitmap.compress(CompressFormat.JPEG, 100, fileOutputStream);
					fileOutputStream.flush();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				try {
					drawable=Drawable.createFromPath(bgFile.getAbsolutePath());
				}
				catch (Exception e) {
					drawable=PKUHelper.pkuhelper.getResources().getDrawable(R.drawable.bg);
					try {
						Bitmap bitmap=((BitmapDrawable)drawable).getBitmap();
						FileOutputStream fileOutputStream=new FileOutputStream(bgFile);
						bitmap.compress(CompressFormat.JPEG, 100, fileOutputStream);
						fileOutputStream.flush();
					}
					catch (Exception ee) {
						e.printStackTrace();
					}
				}
			}
			PKUHelper.pkuhelper.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(bgFile)));
		}
		
		ViewSetting.setTextView(ipgwView, R.id.ipgw_state, hintString);
		int color=Editor.getInt(PKUHelper.pkuhelper, "ipgw_text_color",Color.BLACK);
		ViewSetting.setTextViewColor(ipgwView, R.id.ipgw_hint, color);
		ViewSetting.setTextViewColor(ipgwView, R.id.ipgw_state, color);
		
		if (Constants.isLogin())
			setOthers();
		
		return rootView;
	}
	
	@Override
	public void onViewCreated(final View view, Bundle savedInstanceState) {
		final ViewTreeObserver observer=view.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			
			@SuppressLint("NewApi")
			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				int width=view.getWidth(), height=view.getHeight();
				if (width!=0 && height!=0) {
					ViewSetting.setBackground(getActivity(), view, drawable);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
						observer.removeOnGlobalLayoutListener(this);
					}
					else {
						observer.removeGlobalOnLayoutListener(this);
					}
				}
			}
		});
		super.onViewCreated(view, savedInstanceState);
	}
	
	public static void setOthers() {
		if (ipgwView==null) return;
		gestureOverlayView=(GestureOverlayView)ipgwView.findViewById(R.id.ipgw_gesture);
		if (gestureOverlayView==null) return;
		gestureOverlayView.setGestureColor(Color.YELLOW);
		gestureOverlayView.setGestureStrokeType(GestureOverlayView.GESTURE_STROKE_TYPE_SINGLE);
		gestureOverlayView.setGestureStrokeWidth(5);
		
		File file=MyFile.getFile(PKUHelper.pkuhelper, Constants.username, "gesture");
		try {
			if (!file.exists() && Constants.isValidLogin()) {
				file.createNewFile();
				new AlertDialog.Builder(PKUHelper.pkuhelper)
				.setTitle("请设置手势！").setMessage("初次使用，请设置网关连接的手势。\n你也可以在设置中修改手势。")
				.setCancelable(false).setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						PKUHelper.pkuhelper.startActivity(new Intent(PKUHelper.pkuhelper, GestureActivity.class));
					}
				}).setOnCancelListener(new DialogInterface.OnCancelListener() {
					
					@Override
					public void onCancel(DialogInterface dialog) {
						// TODO Auto-generated method stub
						PKUHelper.pkuhelper.startActivity(new Intent(PKUHelper.pkuhelper, GestureActivity.class));
					}
				}).show();
			}
		} catch (IOException e) {e.printStackTrace();} 
		
		gestureLibrary=GestureLibraries.fromFile(file);
		gestureLibrary.load();
		gestureOverlayView.addOnGestureListener(new GestureOverlayView.OnGestureListener() {
			@Override
			public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
				PKUHelper.pkuhelper.mViewPager.setPagingEnabled(false);
			}
			@Override
			public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
				PKUHelper.pkuhelper.mViewPager.setPagingEnabled(true);
				Gesture gesture=overlay.getGesture();
				if (gesture.getLength()<=100) {
					hintString="长度不足，请重新绘制";
					ViewSetting.setTextView(ipgwView, R.id.ipgw_state, hintString);
					return;
				}
				ArrayList<Prediction> arrayList=gestureLibrary.recognize(gesture);
				if (arrayList==null || arrayList.size()==0) {
					hintString="你没有定义手势，请先于设置中定义";
					ViewSetting.setTextView(ipgwView, R.id.ipgw_state, hintString);
					return;
				}
				Collections.sort(arrayList, new Comparator<Prediction>() {
					@Override
					public int compare(Prediction p, Prediction q) {
						if (p.score>q.score) return -1;
						if (p.score<q.score) return 1;
						return 0;
					}
				});
				
				Prediction prediction=arrayList.get(0);
				Log.w("prediction", prediction.score+"");
				if (prediction.score<3)  {
					hintString="手势无法识别，请重试。";
					ViewSetting.setTextView(ipgwView, R.id.ipgw_state, hintString);
					return;
				}
				String name=prediction.name;
				if ("connect".equals(name)) {
					doConnection(Constants.REQUEST_ITS_CONNECT, "正在连接免费地址...");
					return;
				}
				if ("connectnofree".equals(name)) {
					doConnection(Constants.REQUEST_ITS_CONNECT_NO_FREE, "正在连接收费地址...");
					return;
				}
				if ("disconnect".equals(name)) {
					doConnection(Constants.REQUEST_ITS_DISCONNECT,"正在断开连接");
					return;
				}
				if ("disconnectall".equals(name)) {
					doConnection(Constants.REQUEST_ITS_DISCONNECT_ALL,"正在断开全部连接");
					return;
				}
				
				hintString="手势无法识别，请重试。";
				ViewSetting.setTextView(ipgwView, R.id.ipgw_state, hintString);
			}
			@Override
			public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {
				// TODO Auto-generated method stub
				PKUHelper.pkuhelper.mViewPager.setPagingEnabled(true);
			}
			
			@Override
			public void onGesture(GestureOverlayView overlay, MotionEvent event) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	private static void doConnection(int constantType, String hintString) {
		String type="connect";
		String free="2";
		if (constantType==Constants.REQUEST_ITS_CONNECT) {
		}
		else if (constantType==Constants.REQUEST_ITS_CONNECT_NO_FREE) {
			free="1";
		}
		else if (constantType==Constants.REQUEST_ITS_DISCONNECT) {
			type="disconnect";
		}
		else if (constantType==Constants.REQUEST_ITS_DISCONNECT_ALL) {
			type="disconnectall";
		}
		else return;
		if (!Constants.isLogin()) {
			IAAA.showLoginView();
			return;
		}
		ArrayList<Parameters> arrayList=new ArrayList<Parameters>();
		arrayList.add(new Parameters("uid", Constants.username));
		arrayList.add(new Parameters("password", Constants.password));
		arrayList.add(new Parameters("operation", type));
		arrayList.add(new Parameters("range", free));
		arrayList.add(new Parameters("timeout", "-1"));
		
		new RequestingTask(hintString, 
				"https://its.pku.edu.cn:5428/ipgatewayofpku", constantType)
				.execute(arrayList);
	}
	
	public static void finishConnection(int type, String msg) {
		Map<String, String> map=getReturnMsg(msg);
		if (!map.containsKey("SUCCESS")) {
			CustomToast.showErrorToast(PKUHelper.pkuhelper, "网络连接失败，请重试");
			return;
		}
		String successmsg=map.get("SUCCESS");
		boolean success="YES".equals(successmsg);
		
		if (type==Constants.REQUEST_ITS_CONNECT
				|| type==Constants.REQUEST_ITS_CONNECT_NO_FREE) {
			if (success) {
				CustomToast.showSuccessToast(PKUHelper.pkuhelper, "连接成功！", 1500);
				String scope="";
				if ("domestic".equals(map.get("SCOPE").trim()))
					scope="免费地址";
				else if ("international".equals(map.get("SCOPE").trim()))
					scope="收费地址";
				hintString="连接状态：已连接"+scope+"\n"
						+ "IP: "+map.get("IP")+"\n当前连接数："+map.get("CONNECTIONS")+"\n"
						+ "已用时长： "+map.get("FR_TIME")+"\n"+"账户余额："+map.get("BALANCE");
				
				ViewSetting.setTextView(ipgwView, R.id.ipgw_state, hintString);
				Lib.sendStatistics(PKUHelper.pkuhelper);
				
				Lib.checkConnectedStatus(PKUHelper.pkuhelper, false);
			}
			else {
				new AlertDialog.Builder(PKUHelper.pkuhelper).setTitle("连接失败！")
				.setMessage(map.get("REASON")).setCancelable(true).
				setPositiveButton("关闭", null).show();
				hintString="连接状态：未连接";
				ViewSetting.setTextView(ipgwView, R.id.ipgw_state, hintString);
				Lib.checkConnectedStatus(PKUHelper.pkuhelper);
			}			
			return;
		}
		if (type==Constants.REQUEST_ITS_DISCONNECT) {
			if (success) {
				CustomToast.showSuccessToast(PKUHelper.pkuhelper, "连接断开成功！");
			}
			else {
				new AlertDialog.Builder(PKUHelper.pkuhelper).setTitle("连接断开失败！")
				.setMessage(map.get("REASON")).setCancelable(true).
				setPositiveButton("关闭", null).show();
			}
			hintString="连接状态：未连接";
			ViewSetting.setTextView(ipgwView, R.id.ipgw_state, hintString);
			Lib.checkConnectedStatus(PKUHelper.pkuhelper);
			return;
		}
		if (type==Constants.REQUEST_ITS_DISCONNECT_ALL) {
			if (success) {
				CustomToast.showSuccessToast(PKUHelper.pkuhelper, "全部连接断开成功！");
			}
			else {
				new AlertDialog.Builder(PKUHelper.pkuhelper).setTitle("全部连接断开失败！")
				.setMessage(map.get("REASON")).setCancelable(true).
				setPositiveButton("关闭", null).show();
			}
			hintString="连接状态：未连接";
			ViewSetting.setTextView(ipgwView, R.id.ipgw_state, hintString);
			Lib.checkConnectedStatus(PKUHelper.pkuhelper);
			return;
		}
	}
	
	private static Map<String, String> getReturnMsg(String string) {
		Map<String, String> map=new HashMap<String, String>();
		int pos1=string.indexOf("SUCCESS=");
		int pos2=string.indexOf("IPGWCLIENT_END-->");
		
		String msg=string.substring(pos1, pos2-1);
		
		String[] strings=msg.split(" ");
		for (int i=0;i<strings.length;i++) {
			String str=strings[i];
			str.trim();
			if (!str.contains("=")) continue;
			String[] strings2=str.split("=");
			if (strings2.length!=1)
				map.put(strings2[0], strings2[1]);
			else map.put(strings2[0], "");
		}
		
		return map;
	}
	
	public static void setConnectStatus() {
		if (!Constants.connected) {
			hintString="连接状态：未连接";
		}
		else if (!Constants.inSchool) {
			hintString="你当前不在校内";
		}
		else if (Constants.connectedToNoFree) {
			hintString="已连接到收费地址";
		}
		else hintString="已连接到免费地址";
		
		String nowString=ViewSetting.getTextView(ipgwView, R.id.ipgw_state);
		if (nowString.contains("IP") && hintString.contains("已连接到"))
			hintString=nowString;
		
		ViewSetting.setTextView(ipgwView, R.id.ipgw_state, hintString);	
	}
	/**
	 * 设置背景图，将得到的图片存在 /pkuhelper/temp.jpg
	 */
	public static void setBackground() {
		int width=PKUHelper.pkuhelper.findViewById(R.id.ipgw_view).getWidth();
		int height=PKUHelper.pkuhelper.findViewById(R.id.ipgw_view).getHeight();
		Intent intent=new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", width);
		intent.putExtra("aspectY", height);
		intent.putExtra("return-data", false);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(
			MyFile.getFile(PKUHelper.pkuhelper, null, "temp.jpg")));
		PKUHelper.pkuhelper.startActivityForResult(intent, 0);
	}
	
	@SuppressWarnings("deprecation")
	public static void realSetBackground(Intent data) {
		try {
			File file=MyFile.getFile(PKUHelper.pkuhelper, null, "temp.jpg");
			if (file.exists()) {
				drawable=Drawable.createFromPath(file.getAbsolutePath());
				RelativeLayout relativeLayout=(RelativeLayout)PKUHelper.pkuhelper.findViewById(R.id.ipgw_view);
				relativeLayout.setBackgroundDrawable(drawable);
				
				File file2=MyFile.getFile(PKUHelper.pkuhelper, null, "bg.jpg");
				file2.delete();
				file.renameTo(file2);
				
				PKUHelper.pkuhelper.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
				
			}
		}
		catch (Exception e) {
			CustomToast.showErrorToast(PKUHelper.pkuhelper, "设置失败");
		}
	}
	
	public static void setTextColor() {
		ColorPickerDialogBuilder.with(PKUHelper.pkuhelper, android.R.style.Theme_Holo_Dialog)
		.setTitle("修改文字颜色").initialColor(Editor.getInt(PKUHelper.pkuhelper, "ipgw_text_color", Color.BLACK))
		.wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
		.density(12)
		.setPositiveButton("确定", new ColorPickerClickListener() {
			public void onClick(DialogInterface d, int lastSelectedColor,
					Integer[] allColors) {
				int color=lastSelectedColor;
				ViewSetting.setTextViewColor(ipgwView, R.id.ipgw_hint, color);
				ViewSetting.setTextViewColor(ipgwView, R.id.ipgw_state, color);
				Editor.putInt(PKUHelper.pkuhelper, "ipgw_text_color", color);
			}
		}).setNegativeButton("取消", null)
		.build().show();
	}
	
	public static void reset() {
		try {	
			drawable=PKUHelper.pkuhelper.getResources().getDrawable(R.drawable.bg);
			File bgFile=MyFile.getFile(PKUHelper.pkuhelper, null, "bg.jpg");
			try {
				Bitmap bitmap=((BitmapDrawable)drawable).getBitmap();
				FileOutputStream fileOutputStream=new FileOutputStream(bgFile);
				bitmap.compress(CompressFormat.JPEG, 100, fileOutputStream);
				fileOutputStream.flush();
				fileOutputStream.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			ViewSetting.setBackground(PKUHelper.pkuhelper, 
					PKUHelper.pkuhelper.findViewById(R.id.ipgw_view), drawable);
			PKUHelper.pkuhelper.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(bgFile)));
			int color=Color.BLACK;
			Editor.putInt(PKUHelper.pkuhelper, "ipgw_text_color",color);
			ViewSetting.setTextViewColor(ipgwView, R.id.ipgw_hint, color);
			ViewSetting.setTextViewColor(ipgwView, R.id.ipgw_state, color);	
		}
		catch (Exception e) {
			e.printStackTrace();
			CustomToast.showErrorToast(PKUHelper.pkuhelper, "无法重置为默认....");
		}
		
	}
	
}
