package com.pkuhelper.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.pkuhelper.R;

import java.io.OutputStream;

/**
 * Created by zyxu on 3/9/16.
 */
public class DrawView extends View {
    private Paint mPaint = null;
    private Bitmap mBitmap = null;
    private Canvas mBitmapCanvas = null;
    private boolean canDraw = false;

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);


        mBitmap = Bitmap.createBitmap(2000, 2000, Bitmap.Config.ARGB_8888);
        mBitmapCanvas = new Canvas(mBitmap);
        mBitmapCanvas.drawColor(Color.TRANSPARENT);
        mPaint = new Paint();
        mPaint.setColor(Color.GRAY);
        mPaint.setStrokeWidth(6);
    }


    private float startX;
    private float startY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        Log.d("drawing","on canvas");
        if (canDraw) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getX();
                    startY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float stopX = event.getX();
                    float stopY = event.getY();
                    Log.e("DrawView", "onTouchEvent-ACTION_MOVE\nstartX is " + startX +
                            " startY is " + startY + " stopX is " + stopX + " stopY is " + stopY);
                    mBitmapCanvas.drawLine(startX, startY, stopX, stopY, mPaint);
                    startX = event.getX();
                    startY = event.getY();
                    invalidate();//call onDraw()
                    break;
            }
            return true;
        }
        else
            return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        }
    }

    public void saveBitmap(OutputStream stream) {
        if (mBitmap != null) {
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        }
    }

    public void setCanDraw(boolean canDraw) {
        this.canDraw = canDraw;
    }

    public void changeDrawState(){
        canDraw = !canDraw;
    }
}