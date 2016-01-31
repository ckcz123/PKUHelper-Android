package com.pkuhelper.model.impl;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.pkuhelper.AppContext;
import com.pkuhelper.manager.ApiManager;
import com.pkuhelper.model.IUserMod;

/**
 * Created by LuoLiangchen on 16/1/9.
 */
public class UserMod implements IUserMod{
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
//        Log.v(TAG, mContext.getUserEntity().getToken());
        return mContext.getUserEntity().getToken();
    }
}
