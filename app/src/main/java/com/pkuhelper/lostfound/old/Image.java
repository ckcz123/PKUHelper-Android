package com.pkuhelper.lostfound.old;

import android.graphics.drawable.Drawable;
import android.os.Message;
import android.view.View;
import android.widget.ListView;

import com.pkuhelper.R;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.webconnection.WebConnection;

import java.io.InputStream;
import java.util.HashMap;

public class Image {
	int id;
	String url;
	Drawable drawable;

	public Image(int _id, String _url, Drawable _drawable) {
		id = _id;
		url = _url;
		drawable = _drawable;
	}

	public static HashMap<String, Drawable> imageMap = new HashMap<String, Drawable>();

	public static void requestImage(int id, String url) {
		if (imageMap.containsKey(url)) return;
		final String _url = new String(url);
		final int _id = id;
		new Thread(new Runnable() {
			@Override
			public void run() {
				EventHandler eventHandler = LostFoundActivity.lostFoundActivity.eventHandler;
				try {
					InputStream inputStream = WebConnection.connect(_url);
					Drawable drawable = Drawable.createFromStream(inputStream, _url);
					eventHandler.sendMessage(Message.obtain(eventHandler,
							Constants.MESSAGE_LOSTFOUND_IMAGE_REQUEST_FINISHED,
							new Image(_id, _url, drawable)));
				} catch (Exception e) {
					eventHandler.sendMessage(Message.obtain(eventHandler,
							Constants.MESSAGE_LOSTFOUND_IMAGE_REQUEST_FAILED,
							new Image(_id, _url, null)));
				}
			}
		}).start();

	}

	public static void setImage(Image image) {
		if (image.drawable == null) return;
		imageMap.put(image.url, image.drawable);
		int index = LostFoundActivity.lostFoundActivity.lostArray.indexOf(image.id);
		if (index != -1) {
			try {
				ListView lostView = LostFoundActivity.lostFoundActivity.lostListView;
				View subView = lostView.getChildAt(index - lostView.getFirstVisiblePosition());
				ViewSetting.setImageDrawable(subView, R.id.lostfound_item_image, image.drawable);
			} catch (Exception e) {
			}
		}
		index = LostFoundActivity.lostFoundActivity.foundArray.indexOf(image.id);
		if (index != -1) {
			try {
				ListView listView = LostFoundActivity.lostFoundActivity.foundListView;
				View subView = listView.getChildAt(index - listView.getFirstVisiblePosition());
				ViewSetting.setImageDrawable(subView, R.id.lostfound_item_image, image.drawable);
			} catch (Exception e) {
			}
		}
		index = LostFoundActivity.lostFoundActivity.myArray.indexOf(image.id);
		if (index != -1) {
			try {
				ListView listView = LostFoundActivity.lostFoundActivity.myListView;
				View subView = listView.getChildAt(index - listView.getFirstVisiblePosition());
				ViewSetting.setImageDrawable(subView, R.id.lostfound_item_image, image.drawable);
			} catch (Exception e) {
			}
		}
	}

	public static Drawable getImage(String url) {
		return imageMap.get(url);
	}

}
