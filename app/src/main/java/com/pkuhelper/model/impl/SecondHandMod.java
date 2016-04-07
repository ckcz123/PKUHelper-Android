package com.pkuhelper.model.impl;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.pkuhelper.AppContext;
import com.pkuhelper.entity.SecondHandCategoryEntity;
import com.pkuhelper.entity.SecondHandItemEntity;
import com.pkuhelper.manager.ApiManager;
import com.pkuhelper.model.Callback;
import com.pkuhelper.model.ISecondHandMod;
import com.pkuhelper.ui.main.impl.PkuHelperActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zyxu on 4/4/16.
 */
public class SecondHandMod implements ISecondHandMod {

    private static final String TAG = "SecondHandMod";
    private AppContext mContext;
    private ApiManager mApiManager;
    private UserMod mUserMod;
    private Gson gson = new Gson();

    public SecondHandMod(Context context) {

        // TODO: 4/8/16 DEV
        context = PkuHelperActivity.pkuHelperActivity;
        mContext = (AppContext) context.getApplicationContext();
        mApiManager = ApiManager.getInstance();
        mUserMod = new UserMod(mContext);
    }

    /**
     * 封装对ApiManager的请求调用
     * @param method 接口方法名
     * @param params POST参数
     * @param listener 成功时回调
     * @param errorListener 失败时回调
     */
    private void sendRequest(String method, ArrayList<ApiManager.Parameter> params, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        String controller = "secondhand";
        mApiManager.post(params, method, controller, false, true, listener, errorListener);
    }


    @Override
    public void getItemList(String type, int page, String category1, String category2, String keyword, final Callback<ArrayList<SecondHandItemEntity>> callback) {
        ArrayList<ApiManager.Parameter> params = new ArrayList<>();
        params.add(mApiManager.makeParam("type",type));
        params.add(mApiManager.makeParam("page",page+""));
        if (!category1.isEmpty())
            params.add(mApiManager.makeParam("category1",category1));
        if (!category2.isEmpty())
            params.add(mApiManager.makeParam("category2",category2));
        if (!keyword.isEmpty())
            params.add(mApiManager.makeParam("keyword",keyword));
        sendRequest("getlist", params, new Response.Listener<String>() {
            @Override
            public void onResponse(String jsonStr) {
                try {
                    JsonObject response = gson.fromJson(jsonStr, JsonObject.class);
                    int code = response.get("code").getAsInt();
                    if (code == 0) {
                        ArrayList<SecondHandItemEntity> mods;
                        mods = gson.fromJson(response.get("result"), new TypeToken<ArrayList<SecondHandItemEntity>>() {
                        }.getType());
                        callback.onFinished(code, mods);
                    } else {
                        String msg = response.get("msg").getAsString();
                        Log.v(TAG, ("error, code=" + code) + " " + msg);
                        callback.onError(msg);
                    }
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    callback.onError(e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                callback.onError(volleyError.getMessage());
            }
        });
    }

    @Override
    public void getItemList(int page, Callback<ArrayList<SecondHandItemEntity>> callback) {

    }

    @Override
    public void getItemList(Callback<ArrayList<SecondHandItemEntity>> callback) {

    }

    @Override
    public void getItem(int itemID, Callback<SecondHandItemEntity> callback) {

    }

    @Override
    public void postItem(SecondHandItemEntity entity, Callback<Integer> callback) {

    }

    @Override
    public void changeItem(SecondHandItemEntity entity) {

    }

    @Override
    public void changeItemStatus(int itemID, String status) {

    }

    @Override
    public void getCategoryList(final Callback<ArrayList<SecondHandCategoryEntity>> callback) {
        ArrayList<ApiManager.Parameter> params = new ArrayList<>();
        sendRequest("getCategoryList", params, new Response.Listener<String>() {
            @Override
            public void onResponse(String jsonStr) {
                try {
                    JsonObject response = gson.fromJson(jsonStr, JsonObject.class);
                    int code = response.get("code").getAsInt();
                    if (code == 0) {
                        ArrayList<SecondHandCategoryEntity> mods;
                        JsonObject result = response.get("result").getAsJsonObject();
                        String version = result.get("version").getAsString();
                        Log.d(TAG,version);
                        Log.d(TAG,result.get("categories")+"");
                        // TODO: 4/8/16
                        mods = gson.fromJson(result.get("categories"), new TypeToken<ArrayList<SecondHandCategoryEntity>>(){}.getType());
                        Log.d(TAG,"category num:"+mods.size());
                        callback.onFinished(code, mods);
                    } else {
                        String msg = response.get("msg").getAsString();
                        Log.v(TAG, ("error, code=" + code) + " " + msg);
                        callback.onError(msg);
                    }
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    callback.onError(e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                callback.onError(volleyError.getMessage());
            }
        });


    }

    @Override
    public void createSession(int itemID, Callback<Integer> callback) {

    }
}
