package com.pkuhelper.Mpkuhole;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.pkuhelper.model.Callback;
import com.pkuhelper.model.impl.PkuHoleMod;
import com.pkuhelper.entity.HoleListItemEntity;
import com.pkuhelper.view.IHoleView;
import com.pkuhelper.view.HoleView;

import java.util.ArrayList;

/**
 * Created by zyxu on 16/1/11.
 */
public class HolePresenter {
    private IHoleView mHoleView = null;
    private PkuHoleMod pkuHoleMod;
    private Activity activity;
    private View view;
    private Context context;
    private int requestPage;
    private ArrayList<HoleListItemEntity> mods;

    private Callback callback = null;

    public HolePresenter(Context context) {
        this.context = context;
        mHoleView = new HoleView(context);
        pkuHoleMod = new PkuHoleMod(context);
        callback = new Callback() {
            @Override
            public void onFinished(int code, Object data) {
                if (code == 0) {
                    mods = (ArrayList<HoleListItemEntity>) data;
                    //TO-DO load data
                    mHoleView.firstLoad(mods);
                } else {
                    //TO-DO error solver
                    mHoleView.error();
                }
            }

            @Override
            public void onError(String msg) {
                mHoleView.error();
            }
        };
    }

    public void firstLoad() {
        requestPage = 1;

        mHoleView.loading();
        //request from manager
        pkuHoleMod.getHoleList(requestPage, callback);
    }

    public void moreLoad() {
        requestPage++;

        pkuHoleMod.getHoleList(requestPage, callback);
    }

    public void refreshLoad() {
        //TO-DO refresh list
    }
}