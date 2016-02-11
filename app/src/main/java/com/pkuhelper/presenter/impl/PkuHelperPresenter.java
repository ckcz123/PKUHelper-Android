package com.pkuhelper.presenter.impl;

import android.content.Context;
import android.content.Intent;

import com.pkuhelper.model.IUserMod;
import com.pkuhelper.model.impl.UserMod;
import com.pkuhelper.presenter.IPkuHelperPresenter;
import com.pkuhelper.ui.hole.impl.HoleActivity;
import com.pkuhelper.ui.main.IPkuHelperUI;

/**
 * Created by Liangchen Luo on 16/1/24, who is missing the girl in his dream.
 * @author Liangchen Luo
 */
public class PkuHelperPresenter implements IPkuHelperPresenter {
    private static final String TAG = "PkuHelperPresenter";

    private IPkuHelperUI mPkuHelperUI;
    private IUserMod mUserMod;
    private Context mContext;

    public PkuHelperPresenter(Context context) {
        mContext = context;
        mUserMod = new UserMod(context);
    }

    @Override
    public void setPkuHelperUI(IPkuHelperUI ui) {
        mPkuHelperUI = ui;
    }

    @Override
    public void setupUserInfoInDrawer() {
        mPkuHelperUI.setUserNameInDrawer(mUserMod.getUserName());
        mPkuHelperUI.setUserDepartmentInDrawer(mUserMod.getUserDepartment());
    }

    @Override
    public void startHoleUI() {
        Intent intent = new Intent(mContext, HoleActivity.class);
        mContext.startActivity(intent);
    }
}
