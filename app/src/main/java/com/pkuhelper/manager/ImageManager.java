package com.pkuhelper.manager;

import android.content.Context;
import android.widget.ImageView;

import com.pkuhelper.AppContext;
import com.squareup.picasso.Picasso;

/**
 * Created by LuoLiangchen on 16/1/14.
 */
public class ImageManager {
    private static final String TAG = "ImageManager";
    private AppContext mContext;

    public ImageManager(Context context) {
        mContext = (AppContext) context.getApplicationContext();
    }

    public void displayBigImage(String imgUrl, ImageView imageView) {
        Picasso.with(mContext).load(imgUrl).into(imageView);
    }
}
