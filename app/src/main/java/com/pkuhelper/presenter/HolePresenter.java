package com.pkuhelper.presenter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.pkuhelper.AppContext;
import com.pkuhelper.model.Callback;
import com.pkuhelper.model.impl.PkuHoleMod;
import com.pkuhelper.entity.HoleListItemEntity;
import com.pkuhelper.ui.hole.IHoleUI;

import java.util.ArrayList;

/**
 * Created by zyxu on 16/1/11.
 */
public class HolePresenter {
    private IHoleUI mHoleView = null;
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
        mHoleView = (IHoleUI) context;
        pkuHoleMod = new PkuHoleMod(context);
        callback = new Callback() {
            @Override
            public void onFinished(int code, Object data) {

                if (code == 0) {
                    mods = (ArrayList<HoleListItemEntity>) data;
                    requestPage++;
                    //TO-DO load data
                    if (requestPage == 1)
                        mHoleView.firstLoad(mods);
                    else
                        mHoleView.moreLoad(mods);
                } else {
                    mHoleView.error();
                }
                isLoading = false;
            }

            @Override
            public void onError(String msg) {
                isLoading = false;
                mHoleView.error();
            }
        };
    }

    public void firstLoad() {

        isLoading = true;
        requestPage = 0;

        mHoleView.loading();
        //request from manager
        pkuHoleMod.getHoleList(requestPage+1, callback);
    }

    public void moreLoad() {
        Log.d("Presenter Status:",String.valueOf(isLoading));
        if (isLoading)
            return;
        isLoading = true;
        mHoleView.loadingMore();
        pkuHoleMod.getHoleList(requestPage+1, callback);
    }

    public void refreshLoad() {
        //TO-DO refresh list
    }
}