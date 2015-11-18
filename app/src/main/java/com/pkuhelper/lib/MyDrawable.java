package com.pkuhelper.lib;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;

import com.pkuhelper.PKUHelper;

public class MyDrawable {
	public static Drawable getDrawable(int picId, String colorname) {
		return getDrawable(picId, colorname, 80);
	}

	public static Drawable getDrawable(int picId, String colorname, int width) {
		return getDrawable(PKUHelper.pkuhelper.getResources().getDrawable(picId), colorname);
	}

	public static Drawable getDrawable(Drawable drawable, String colorname) {
		return getDrawable(drawable, colorname, 80);
	}

	public static Drawable getDrawable(Drawable drawable, String colorname, int width) {
		GradientDrawable gradientDrawable = new GradientDrawable();
		int color;
		try {
			color = Color.parseColor(colorname);
		} catch (Exception e) {
			color = Util.generateColorInt();
		}
		gradientDrawable.setShape(GradientDrawable.OVAL);
		gradientDrawable.setBounds(0, 0, width, width);
		gradientDrawable.setColor(color);
		if (drawable == null) return gradientDrawable;

		Drawable[] array = new Drawable[2];
		drawable.setBounds(0, 0, width, width);
		array[1] = drawable;
		array[0] = gradientDrawable;

		LayerDrawable layerDrawable = new LayerDrawable(array);
		layerDrawable.setLayerInset(0, 0, 0, 0, 0);
		layerDrawable.setLayerInset(1, 0, 0, 0, 0);
		layerDrawable.setBounds(0, 0, width, width);
		return layerDrawable;
	}

}
