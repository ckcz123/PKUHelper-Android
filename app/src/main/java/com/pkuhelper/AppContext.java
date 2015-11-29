package com.pkuhelper;

import android.app.Application;

import com.pkuhelper.manager.ApiManager;

/**
 * Created by LuoLiangchen on 15/11/30.
 */
public class AppContext extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化ApiManager
        ApiManager.newInstance(this);
    }
}
