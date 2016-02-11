package com.pkuhelper.model.impl;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.pkuhelper.AppContext;
import com.pkuhelper.manager.ApiManager;
import com.pkuhelper.model.IUserMod;

/**
 * Created by Liangchen Luo on 16/1/9.
 * @author Liangchen Luo
 */
public class UserMod implements IUserMod {
    private static final String TAG = "UserMod";

    private AppContext mContext;
    private ApiManager mApiManager;
    private Gson gson = new Gson();

    public UserMod(Context context) {
        mContext = (AppContext) context.getApplicationContext();
        mApiManager = ApiManager.getInstance();
    }

    @Override
    public String getToken() {
        return mContext.getUserEntity().getToken();
    }

    @Override
    public String getUserName() {
        return mContext.getUserEntity().getName();
    }

    @Override
    public String getUserDepartment() {
        return mContext.getUserEntity().getDepartment();
    }
}
