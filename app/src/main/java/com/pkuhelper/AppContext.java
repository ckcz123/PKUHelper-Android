package com.pkuhelper;

import android.app.Application;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pkuhelper.entity.HoleListItemEntity;
import com.pkuhelper.entity.UserEntity;
import com.pkuhelper.lib.Editor;
import com.pkuhelper.manager.ApiManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by LuoLiangchen on 15/11/30.
 */
public class AppContext extends Application {
    private static final String TAG = "AppContext";

    private UserEntity mUserEntity;

    /**
     * 树洞关注PID的哈希表
     * key: 树洞PID
     * 查询：存在为关注，空为未关注
     */
    private Set<Integer> mHoleAttentionSet;

    private long holeTimestamp;

    private Gson gson = new Gson();

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化ApiManager
        ApiManager.newInstance(this);

        initUserEntity();
        initHoleAttentionSet();
    }

    private void initUserEntity() {
        mUserEntity = gson.fromJson(Editor.getString(this, "mUserEntity"), UserEntity.class);
        if (mUserEntity == null) mUserEntity = new UserEntity();

        // dev
        // 登陆网关炸了
        mUserEntity.setToken("70951e84f8a0d48f256a159e1dfd8d56");
    }

    public UserEntity getUserEntity() {
        return mUserEntity;
    }

    public void saveUserEntity() {
        String mUserEntityJson = gson.toJson(mUserEntity);
        Editor.putString(this, "mUserEntity", mUserEntityJson);
    }

    /**
     * 初始化关注PID集合，从preference里读取（如果不在本地存储也可以删掉load部分）
     */
    private void initHoleAttentionSet() {
        mHoleAttentionSet = new HashSet<>();
        ArrayList<Integer> attentionPids;
        attentionPids = gson.fromJson(Editor.getString(this, "attentionPids"), new TypeToken<ArrayList<Integer>>() {}.getType());
        if (attentionPids == null) attentionPids = new ArrayList<>();
        for (Integer pid : attentionPids) mHoleAttentionSet.add(pid);
    }

    public void setHoleAttentionSet(ArrayList<HoleListItemEntity> entities) {
        mHoleAttentionSet.clear();
        for (HoleListItemEntity entity : entities) mHoleAttentionSet.add(entity.getPid());
        saveHoleAttentionSet(); // 待定
    }

    public Set<Integer> getHoleAttentionSet() {
        return mHoleAttentionSet;
    }

    private void saveHoleAttentionSet() {
        String attentionPidsJson;
        ArrayList<Integer> attentionPids = new ArrayList<>();
        for (Integer pid : mHoleAttentionSet) attentionPids.add(pid);
        attentionPidsJson = gson.toJson(attentionPids);
        Editor.putString(this, "attentionPids", attentionPidsJson);
    }

    public void updateHoleTimestamp(long timestamp) {
        holeTimestamp = timestamp;
    }

    public long getHoleTimestamp() {
        return holeTimestamp;
    }
}
