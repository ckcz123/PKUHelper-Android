package com.pkuhelper.lib;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spanned;
import android.text.TextPaint;
import android.view.View;
import android.widget.*;

public class ViewSetting {
	public static void setTextView(final Object father, final int resourceid, final String text) {
		try {
			if (father==null || resourceid==0 || text==null) return;
			TextView textView=null;
			if (father instanceof Activity)
				textView=(TextView)((Activity)father).findViewById(resourceid);
			else if (father instanceof View)
				textView=(TextView)((View)father).findViewById(resourceid);
			else if (father instanceof Dialog)
				textView=(TextView)((Dialog)father).findViewById(resourceid);
			if (textView==null) return;
			textView.setText(text);
		}
		catch (Exception e) {}
	}
	public static void setTextView(final Object father, final int resourceid, final Spanned text) {
		try {
			if (father==null || resourceid==0 || text==null) return;
			TextView textView=null;
			if (father instanceof Activity)
				textView=(TextView)((Activity)father).findViewById(resourceid);
			else if (father instanceof View)
				textView=(TextView)((View)father).findViewById(resourceid);
			else if (father instanceof Dialog)
				textView=(TextView)((Dialog)father).findViewById(resourceid);
			if (textView==null) return;
			textView.setText(text);
		} catch (Exception e) {}
	}
	public static String getTextView(final Object father, final int resourceid) {
		try {
			if (father==null || resourceid==0) return "";
			TextView textView=null;
			if (father instanceof Activity)
				textView=(TextView)((Activity)father).findViewById(resourceid);
			else if (father instanceof View)
				textView=(TextView)((View)father).findViewById(resourceid);
			else if (father instanceof Dialog)
				textView=(TextView)((Dialog)father).findViewById(resourceid);
			if (textView==null) return "";
			return textView.getText().toString();
		} catch (Exception e) {return "";}
	}
	public static void setTextViewColor(final Object father, final int resourceid, final int color) {
		try {
			if (father==null || resourceid==0) return;
			TextView textView=null;
			if (father instanceof Activity)
				textView=(TextView)((Activity)father).findViewById(resourceid);
			else if (father instanceof View)
				textView=(TextView)((View)father).findViewById(resourceid);
			else if (father instanceof Dialog)
				textView=(TextView)((Dialog)father).findViewById(resourceid);
			if (textView==null) return;
			textView.setTextColor(color);
		} catch (Exception e) {}
 	}
	public static void setTextViewBold(final Object father, final int resourceid) {
		try {
			if (father==null || resourceid==0) return;
			TextView textView=null;
			if (father instanceof Activity)
				textView=(TextView)((Activity)father).findViewById(resourceid);
			else if (father instanceof View)
				textView=(TextView)((View)father).findViewById(resourceid);
			else if (father instanceof Dialog)
				textView=(TextView)((Dialog)father).findViewById(resourceid);
			if (textView==null) return;
			TextPaint tPaint=textView.getPaint();
			tPaint.setFakeBoldText(true);
		} catch (Exception e) {}
	}
	public static String getEditTextValue(final Object father, final int resourceid) {
		try {
			if (father==null || resourceid==0) return "";
			EditText editText=null;
			if (father instanceof Activity)
				editText=(EditText)((Activity)father).findViewById(resourceid);
			else if (father instanceof View)
				editText=(EditText)((View)father).findViewById(resourceid);
			else if (father instanceof Dialog)
				editText=(EditText)((Dialog)father).findViewById(resourceid);
			if (editText==null) return "";
			return editText.getEditableText().toString();
		} catch (Exception e) {return "";}
	}
	public static void setEditTextValue(final Object father, final int resourceid, final String string) {
		try {
			if (father==null || resourceid==0) return;
			EditText editText=null;
			if (father instanceof Activity)
				editText=(EditText)((Activity)father).findViewById(resourceid);
			else if (father instanceof View)
				editText=(EditText)((View)father).findViewById(resourceid);
			else if (father instanceof Dialog)
				editText=(EditText)((Dialog)father).findViewById(resourceid);
			if (editText==null) return;
			String str;
			if (string==null) str="";
			else str=new String(string);
			editText.setText(str);
			editText.setSelection(str.length());
		} catch (Exception e) {}
	}
	public static void setOnClickListener(final Object father, final int resourceid, final View.OnClickListener onClickListener) {
		try {
			if (father==null || resourceid==0) return;
			View view=null;
			if (father instanceof Activity)
				view=((Activity)father).findViewById(resourceid);
			else if (father instanceof View)
				view=((View)father).findViewById(resourceid);
			else if (father instanceof Dialog)
				view=((Dialog)father).findViewById(resourceid);
			if (view==null) return;
			view.setOnClickListener(onClickListener);
		} catch (Exception e) {}
	}
	public static void setImageDrawable(final Object father, final int resourceid, final Drawable drawable) {
		try {
			if (father==null || resourceid==0) return;
			ImageView imageView=null;
			if (father instanceof Activity)
				imageView=(ImageView)((Activity)father).findViewById(resourceid);
			else if (father instanceof View)
				imageView=(ImageView)((View)father).findViewById(resourceid);
			else if (father instanceof Dialog)
				imageView=(ImageView)((Dialog)father).findViewById(resourceid);
			if (imageView==null) return;
			imageView.setImageDrawable(drawable);
		} catch (Exception e) {}
	}
	public static void setImageBitmap(final Object father, final int resourceid, final Bitmap bitmap) {
		try {
			if (father==null || resourceid==0) return;
			ImageView imageView=null;
			if (father instanceof Activity)
				imageView=(ImageView)((Activity)father).findViewById(resourceid);
			else if (father instanceof View)
				imageView=(ImageView)((View)father).findViewById(resourceid);
			else if (father instanceof Dialog)
				imageView=(ImageView)((Dialog)father).findViewById(resourceid);
			if (imageView==null) return;
			imageView.setImageBitmap(bitmap);
		} catch (Exception e) {}
	}
	public static void setImageResource(final Object father, final int resourceid, final int resID) {
		try {
			if (father==null || resourceid==0 || resID==0) return;
		ImageView imageView=null;
		if (father instanceof Activity)
			imageView=(ImageView)((Activity)father).findViewById(resourceid);
		else if (father instanceof View)
			imageView=(ImageView)((View)father).findViewById(resourceid);
		else if (father instanceof Dialog)
			imageView=(ImageView)((Dialog)father).findViewById(resourceid);
		if (imageView==null) return;
		imageView.setImageResource(resID);
		}catch (Exception e) {}
	}
	public static void setSwitchChecked(final Object father, final int resourceid, final boolean checked) {
		try {
			if (father==null || resourceid==0) return;
			Switch switch1=null;
			if (father instanceof Activity)
				switch1=(Switch)((Activity)father).findViewById(resourceid);
			else if (father instanceof View)
				switch1=(Switch)((View)father).findViewById(resourceid);
			else if (father instanceof Dialog)
				switch1=(Switch)((Dialog)father).findViewById(resourceid);
			if (switch1==null) return;
			switch1.setChecked(checked);
		}
		catch (Exception e) {}
	}
	public static void setSwitchOnCheckChangeListener(final Object father, final int resourceid, final CompoundButton.OnCheckedChangeListener listener) {
		try {
			if (father==null || resourceid==0) return;
			Switch switch1=null;
			if (father instanceof Activity)
				switch1=(Switch)((Activity)father).findViewById(resourceid);
			else if (father instanceof View)
				switch1=(Switch)((View)father).findViewById(resourceid);
			else if (father instanceof Dialog)
				switch1=(Switch)((Dialog)father).findViewById(resourceid);
			if (switch1==null) return;
			switch1.setOnCheckedChangeListener(listener);
		}
		catch (Exception e) {}
	}
	public static boolean getSwitchChecked(Object father, int resourceid) {
		return getSwitchChecked(father, resourceid, false);
	}
	
	public static boolean getSwitchChecked(Object father, int resourceid, boolean defaultValue) {
		try {
			if (father==null || resourceid==0) return defaultValue;
			Switch switch1=null;
			if (father instanceof Activity)
				switch1=(Switch)((Activity)father).findViewById(resourceid);
			else if (father instanceof View)
				switch1=(Switch)((View)father).findViewById(resourceid);
			else if (father instanceof Dialog)
				switch1=(Switch)((Dialog)father).findViewById(resourceid);
			if (switch1==null) return defaultValue;
			return switch1.isChecked();
		}
		catch (Exception e) {return defaultValue;}
	}
	
	
	@SuppressWarnings("deprecation")
	public static void setBackground(Context context, View view, Bitmap bitmap,
			int width, int height) {
		if (height==0 || width==0 || view==null || bitmap==null) return;
		try {
			double scaled=(width+0.0)/(height+0.0);
			width=bitmap.getWidth();
			height=bitmap.getHeight();
			int x, y, dstwidth, dstheight;
			double s=(width+0.0)/(height+0.0);
			if (s>scaled) {
				dstheight=height;
				dstwidth=(int)(dstheight*scaled);
				y=0;
				x=(width-dstwidth)/2;
			}
			else {
				dstwidth=width;			
				dstheight=(int)(dstwidth/scaled);
				x=0;
				y=(height-dstheight)/2;
			}
			Bitmap ans=Bitmap.createBitmap(bitmap, x, y, dstwidth, dstheight);
			view.setBackgroundDrawable(
					new BitmapDrawable(context.getResources(), ans));
		}
		catch (Exception e) {}
		catch (OutOfMemoryError e) {System.gc();}
	}
	
	public static void setBackground(Context context, View view, Bitmap bitmap) {
		if (view==null || bitmap==null) return;
		int width=view.getWidth(), height=view.getHeight();
		if (width==0 || height==0) return;
		setBackground(context, view, bitmap, width, height);
	}
	
	public static void setBackground(Context context, View view, Drawable drawable) {
		if (view==null || drawable==null) return;
		int width=view.getWidth(), height=view.getHeight();
		if (width==0 || height==0) return;
		setBackground(context, view, drawable, width, height);
	}
	
	public static void setBackground(Context context, View view, Drawable drawable,
			int width, int height) {
		if (width==0 || height==0 || view==null || drawable==null) return;
		setBackground(context, view, ((BitmapDrawable)drawable).getBitmap(),
				width, height);
	}
	
	public static void setBackground(Context context, View view, int resid) {
		try {
			if (view==null || resid==0) return;
			Drawable drawable=context.getResources().getDrawable(resid);
			if (drawable==null) return;
			setBackground(context, view, drawable);
		}
		catch (Exception e) {}
		catch (OutOfMemoryError e) {System.gc();}
	}
	
	public static void setBackground(Context context, View view, int resid,
			int width, int height) {
		try {
			if (view==null || resid==0) return;
			Drawable drawable=context.getResources().getDrawable(resid);
			if (drawable==null) return;
			setBackground(context, view, drawable, width, height);
		}
		catch (Exception e) {}
		catch (OutOfMemoryError e) {System.gc();}
	}
	
}
