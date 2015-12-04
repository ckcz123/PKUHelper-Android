package com.pkuhelper;

import android.app.Application;
import android.util.Log;

import com.google.gson.Gson;
import com.pkuhelper.lib.Editor;
import com.pkuhelper.manager.ApiManager;
import com.pkuhelper.model.UserMod;

/**
 * Created by LuoLiangchen on 15/11/30.
 */
public class AppContext extends Application {
    private static final String TAG = "AppContext";

    private UserMod mUserMod;

    private Gson gson = new Gson();

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化ApiManager
        ApiManager.newInstance(this);

        initUserMod();
    }

    private void initUserMod() {
        mUserMod = gson.fromJson(Editor.getString(this, "mUserMod"), UserMod.class);
        if (mUserMod == null) mUserMod = new UserMod();
    }

    public UserMod getUserMod() {
        return mUserMod;
    }
}
