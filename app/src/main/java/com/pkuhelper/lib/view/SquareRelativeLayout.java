package com.pkuhelper.lib.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class SquareRelativeLayout extends RelativeLayout {
	public SquareRelativeLayout(Context context) {
		super(context);		
	}
	public SquareRelativeLayout(Context context, AttributeSet a) {
		super(context,a);		
	}
	public SquareRelativeLayout(Context context,AttributeSet a,int b) {
		super(context,a,b);		
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		  super.onMeasure(widthMeasureSpec, widthMeasureSpec);
	}
}
