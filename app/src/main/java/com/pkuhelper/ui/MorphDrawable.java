package com.pkuhelper.ui;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.util.Property;

/**
 * 形态和颜色可以发生变化的Drawable，形态变化是通过cornerRadius来实现的，颜色变化是通过paint的color来实现的
 * 该类在Drawable的基础上添加了cornerRadius和color两个属性，前者是float类型，后者是int类型
 * <p/>
 * A drawable that can morph size, shape (via it's corner radius) and color.  Specifically this is
 * useful for animating between a FAB and a dialog.
 */
public class MorphDrawable extends Drawable {

    private Paint paint;
    private float cornerRadius;

    public static final Property<MorphDrawable, Float> CORNER_RADIUS = new Property<MorphDrawable, Float>(Float.class, "cornerRadius") {

        @Override
        public void set(MorphDrawable morphDrawable, Float value) {
            morphDrawable.setCornerRadius(value);
        }

        @Override
        public Float get(MorphDrawable morphDrawable) {
            return morphDrawable.getCornerRadius();
        }
    };

    public static final Property<MorphDrawable, Integer> COLOR = new Property<MorphDrawable, Integer>(Integer.class, "color") {

        @Override
        public void set(MorphDrawable morphDrawable, Integer value) {
            morphDrawable.setColor(value);
        }

        @Override
        public Integer get(MorphDrawable morphDrawable) {
            return morphDrawable.getColor();
        }
    };

    public MorphDrawable(@ColorInt int color, float cornerRadius) {
        this.cornerRadius = cornerRadius;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
    }

    public float getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(float cornerRadius) {
        this.cornerRadius = cornerRadius;
        invalidateSelf();
    }

    public int getColor() {
        return paint.getColor();
    }

    public void setColor(int color) {
        paint.setColor(color);
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(getBounds().left, getBounds().top, getBounds().right, getBounds()
                    .bottom, cornerRadius, cornerRadius, paint);//hujiawei
        }
    }

    @Override
    public void getOutline(Outline outline) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            outline.setRoundRect(getBounds(), cornerRadius);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return paint.getAlpha();
    }

}