package com.pkuhelper.presenter.impl;

import android.content.Context;
import android.content.Intent;

import com.pkuhelper.presenter.IPkuHelperPresenter;
import com.pkuhelper.ui.hole.impl.MHoleActivity;
import com.pkuhelper.ui.main.IPkuHelperUI;

/**
 * Created by LuoLiangchen on 16/1/24.
 */
public class PkuHelperPresenter implements IPkuHelperPresenter {

    private Context mContext;
    private IPkuHelperUI mPkuHelperUI;

    public PkuHelperPresenter(Context context) {
        mContext = context;
    }

    @Override
    public void startHoleUI() {
        Intent intent = new Intent(mContext, MHoleActivity.class);
        mContext.startActivity(intent);
    }
}
