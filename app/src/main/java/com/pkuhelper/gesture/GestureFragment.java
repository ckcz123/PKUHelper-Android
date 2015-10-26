package com.pkuhelper.gesture;

import android.app.Fragment;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pkuhelper.R;
import com.pkuhelper.lib.MyBitmapFactory;

import java.util.ArrayList;

public class GestureFragment extends Fragment {
	public TextView titleTextView, hintTextView;
	public GestureOverlayView gestureOverlayView;
	public ImageView imageView;
	private String type;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.gesture_view,
				container, false);
		titleTextView = (TextView) rootView.findViewById(R.id.gesture_text);
		hintTextView = (TextView) rootView.findViewById(R.id.gesture_hint);
		imageView = (ImageView) rootView.findViewById(R.id.gesture_image);
		gestureOverlayView = (GestureOverlayView) rootView.findViewById(R.id.gesture_overlay_view);
		type = getArguments().getString("type");
		init();
		return rootView;
	}

	public void init() {
		String text = "";
		if ("connect".equals(type)) text = "请绘制你喜欢的手势来连接免费地址";
		else if ("connectnofree".equals(type)) text = "请绘制你喜欢的手势来连接收费地址";
		else if ("disconnect".equals(type)) text = "请绘制你喜欢的手势来断开连接";
		else if ("disconnectall".equals(type)) text = "请绘制你喜欢的手势来断开全部连接";
		titleTextView.setText(text);
		gestureOverlayView.setGestureColor(Color.YELLOW);
		gestureOverlayView.setGestureStrokeType(GestureOverlayView.GESTURE_STROKE_TYPE_SINGLE);
		gestureOverlayView.setGestureStrokeWidth(8);
		setImage();
		gestureOverlayView.addOnGestureListener(new GestureOverlayView.OnGestureListener() {
			@Override
			public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
				GestureActivity.gestureActivity.mViewPager.setPagingEnabled(false);
				hintTextView.setText("放手以结束绘制");
			}

			@Override
			public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
				GestureActivity.gestureActivity.mViewPager.setPagingEnabled(true);
				Gesture gesture = overlay.getGesture();
				if (gesture.getLength() <= 100) {
					hintTextView.setText("长度不足，请重新绘制");
					return;
				}
				GestureActivity.gestureActivity.gestureLibrary.removeEntry(type);
				GestureActivity.gestureActivity.gestureLibrary.addGesture(type, gesture);
				GestureActivity.gestureActivity.gestureLibrary.save();
				setImage();
				hintTextView.setText("保存成功！");
			}

			@Override
			public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {
				// TODO Auto-generated method stub
				GestureActivity.gestureActivity.mViewPager.setPagingEnabled(true);
			}

			@Override
			public void onGesture(GestureOverlayView overlay, MotionEvent event) {
				// TODO Auto-generated method stub

			}
		});
	}

	private void setImage() {
		if (GestureActivity.gestureActivity.gestureLibrary.load()) {
			ArrayList<Gesture> arrayList =
					GestureActivity.gestureActivity.gestureLibrary.getGestures(type);
			if (arrayList == null || arrayList.size() == 0) {
				hintTextView.setText("你暂时没有手势");
				return;
			}
			Gesture g = arrayList.get(arrayList.size() - 1);
			imageView.setImageBitmap(MyBitmapFactory.gestureToBitmap(g, Color.LTGRAY, 3));
		} else
			hintTextView.setText("你暂时没有手势");
	}
}
