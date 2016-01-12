package com.pkuhelper;

import android.app.Application;
import android.util.Log;

import com.google.gson.Gson;
import com.pkuhelper.entity.UserEntity;
import com.pkuhelper.lib.Editor;
import com.pkuhelper.manager.ApiManager;

/**
 * Created by LuoLiangchen on 15/11/30.
 */
public class AppContext extends Application {
    private static final String TAG = "AppContext";

    private UserEntity mUserEntity;
    private long holeTimestamp;

    private Gson gson = new Gson();

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化ApiManager
        ApiManager.newInstance(this);

        initUserEntity();
    }

    private void initUserEntity() {
        mUserEntity = gson.fromJson(Editor.getString(this, "mUserEntity"), UserEntity.class);
        if (mUserEntity == null) mUserEntity = new UserEntity();
    }

    public void updateHoleTimestamp(long timestamp) {
        holeTimestamp = timestamp;
    }

    public long getHoleTimestamp() {
        return holeTimestamp;
    }

    public UserEntity getUserEntity() {
        return mUserEntity;
    }
}
