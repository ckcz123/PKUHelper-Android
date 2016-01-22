package com.pkuhelper.presenter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.pkuhelper.AppContext;
import com.pkuhelper.R;
import com.pkuhelper.lib.MyBitmapFactory;
import com.pkuhelper.model.Callback;
import com.pkuhelper.model.impl.PkuHoleMod;
import com.pkuhelper.entity.HoleListItemEntity;
import com.pkuhelper.ui.hole.IHolePostUI;
import com.pkuhelper.ui.hole.IHoleUI;
import com.pkuhelper.ui.hole.impl.HolePostFragment;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by zyxu on 16/1/11.
 */
public class HolePresenter {
    private IHoleUI mHoleUI = null;
    private IHolePostUI mHolePostUI = null;
    private PkuHoleMod pkuHoleMod;
    private Activity activity;
    private View view;
    private AppContext mContext;
    private int requestPage;
    private ArrayList<HoleListItemEntity> mods;
    private boolean isLoading = false;

    private Callback callback = null;

    public HolePresenter(Context context) {
        mContext = (AppContext) context.getApplicationContext();
        mHoleUI = (IHoleUI) context;
        pkuHoleMod = new PkuHoleMod(context);
        callback = new Callback() {
            @Override
            public void onFinished(int code, Object data) {

                if (code == 0) {
                    mods = (ArrayList<HoleListItemEntity>) data;
                    requestPage++;
                    //TO-DO load data
                    if (requestPage == 1)
                        mHoleUI.firstLoad(mods);
                    else
                        mHoleUI.moreLoad(mods);
                } else {
                    mHoleUI.error();
                }
                isLoading = false;
            }

            @Override
            public void onError(String msg) {
                isLoading = false;
                mHoleUI.error();
            }
        };
    }

    public void firstLoad() {

        isLoading = true;
        requestPage = 0;

        mHoleUI.loading();
        //request from manager
        pkuHoleMod.getHoleList(requestPage+1, callback);
    }

    public void moreLoad() {
        Log.d("Presenter Status:",String.valueOf(isLoading));
        if (isLoading)
            return;
        isLoading = true;
        mHoleUI.loadingMore();
        pkuHoleMod.getHoleList(requestPage+1, callback);
    }

    public void refreshLoad() {
        //TO-DO refresh list
    }

    public void post(Bundle bundle) throws IOException {
        String type = bundle.getString("type");
        String text="";
        String uri;
        String data = null;
        byte[] bts;
        if (type==null){
            return;
        }

        Callback simpleCallback = new Callback<Void>() {
            @Override
            public void onFinished(int code, Void data) {
                Log.d("success code:",""+code);
            }

            @Override
            public void onError(String msg) {
                Log.d("error",msg);
            }
        };

        if (type.equals("text")){
            text = bundle.getString("text");
            pkuHoleMod.post(type, text, "", 0, simpleCallback);
        }
        else if(type.equals("image")){

            text = bundle.getString("text");
            data = bundle.getString("data");
            pkuHoleMod.post(type, text, data, 0, simpleCallback);
        }
        else if(type.equals("audio")){
            uri = bundle.getString("uri");
        }


    }
}