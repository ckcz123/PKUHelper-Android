package com.pkuhelper.M_PKUhole;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.pkuhelper.model.Callback;
import com.pkuhelper.model.impl.PkuHoleMod;
import com.pkuhelper.entity.HoleListItemEntity;

import java.util.ArrayList;

/**
 * Created by zyxu on 16/1/11.
 */
public class MHolePresenter {
    private IMHoleView mHoleView = null;
    private PkuHoleMod pkuHoleManager;
    private Activity activity;
    private View view;
    private Context context;
    private int requestPage;
    private ArrayList<HoleListItemEntity> mods;

    private Callback callback = new Callback() {
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

    public MHolePresenter(Context context) {
        this.context = context;
        mHoleView = new MHoleView(context);
    }

    public void firstLoad() {
        requestPage = 1;

        //request from manager
        pkuHoleManager.getHoleList(requestPage, callback);

        //update view
        //mHoleView.

    }

    public void moreLoad() {
        requestPage++;

        pkuHoleManager.getHoleList(requestPage, callback);
    }

    public void refreshLoad() {
        //TO-DO refresh list
    }
}