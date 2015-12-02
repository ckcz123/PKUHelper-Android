package com.pkuhelper.manager;

import android.content.Context;

import com.google.gson.Gson;
import com.pkuhelper.AppContext;
import com.pkuhelper.model.UserMod;

/**
 * Created by LuoLiangchen on 15/12/2.
 */
public class UserManager {
    private static final String TAG = "UserManager";

    private AppContext mContext;
    private ApiManager mApiManager;
    private Gson gson = new Gson();

    public UserManager(Context context) {
        mContext = (AppContext) context.getApplicationContext();
        mApiManager = ApiManager.getInstance();
    }

    public UserMod getUserMod() {
        return mContext.getUserMod();
    }
}
