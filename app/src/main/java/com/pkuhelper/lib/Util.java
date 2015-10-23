package com.pkuhelper.lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.webkit.WebView;

import java.security.MessageDigest;
import java.util.Random;

public class Util {

	@SuppressWarnings("deprecation")
	public static Bitmap captureWebView(WebView webView) {
		if (webView == null) return null;
		Picture snapShot = webView.capturePicture();
		Bitmap bmp = Bitmap.createBitmap(snapShot.getWidth(), snapShot.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		snapShot.draw(canvas);
		return bmp;
	}

	public static String getHash(String string) {
		return getHash(string, "");
	}

	public static String getHash(String string, String defaultString) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			return bytes2Hex(md.digest(string.getBytes()));
		} catch (Exception e) {
			return defaultString;
		}
	}

	private static String bytes2Hex(byte[] bts) {
		String des = "";
		for (int i = 0; i < bts.length; i++) {
			String tmp = (Integer.toHexString(bts[i] & 0xFF));
			if (tmp.length() == 1)
				des += "0";
			des += tmp;
		}
		return des;
	}

	public static int sp2px(Context context, int sp) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (sp * fontScale + 0.5f);
	}

	public static int px2sp(Context context, int px) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (px / fontScale + 0.5f);
	}

	public static int generateColorInt() {
		return Color.parseColor(generateColorString());
	}

	public static String generateColorString() {
		Random random = new Random();
		int[] x = new int[6];
		while (true) {
			for (int i=0;i<6;i++)
				x[i]=random.nextInt(16);
			int val=x[0]+x[2]+x[4];
			if (val>=27 && val<=40) break;
		}
		String string = "#";
		for (int i = 0; i < 6; i++)
			string += intToHex(x[i]);
		return string;
	}

	private static String intToHex(int i) {
		if (i <= 9) return i + "";
		if (i == 10) return "a";
		if (i == 11) return "b";
		if (i == 12) return "c";
		if (i == 13) return "d";
		if (i == 14) return "e";
		return "f";
	}

}
