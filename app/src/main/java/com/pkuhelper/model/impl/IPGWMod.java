package com.pkuhelper.model.impl;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.pkuhelper.AppContext;
import com.pkuhelper.R;
import com.pkuhelper.entity.AQIEntity;
import com.pkuhelper.entity.HoleListItemEntity;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.lib.webconnection.Parameters;
import com.pkuhelper.manager.ApiManager;
import com.pkuhelper.model.Callback;
import com.pkuhelper.model.IIPGWMod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zyxu on 3/1/16.
 */
public class IPGWMod implements IIPGWMod {

    private AppContext mContext;
    private ApiManager mApiManager;
    private UserMod mUserMod;
    private String url = "https://its.pku.edu.cn:5428/ipgatewayofpku";

    public IPGWMod(Context context) {
        mContext = (AppContext) context.getApplicationContext();
        mApiManager = ApiManager.getInstance();
        mUserMod = new UserMod(mContext);
    }

    @Override
    public void doConnect(boolean isFree, final Callback<String> callback) {
        ArrayList<ApiManager.Parameter> params = new ArrayList<>();

        int free = isFree ? 2 : 1;

        params.add(mApiManager.makeParam("uid", Constants.username));
        params.add(mApiManager.makeParam("password", Constants.password));
        params.add(mApiManager.makeParam("operation", "connect"));
        params.add(mApiManager.makeParam("range", free + ""));
        params.add(mApiManager.makeParam("timeout", "-1"));

        mApiManager.ipgwPost(params,new Response.Listener<String>() {
            @Override
            public void onResponse(String str) {
                try {
                    Log.d("IPGW RETURN STR", str);

                    callback.onFinished(0,str);

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
    public void disconnect(final Callback<String> callback) {
        ArrayList<ApiManager.Parameter> params = new ArrayList<>();

        params.add(mApiManager.makeParam("uid", Constants.username));
        params.add(mApiManager.makeParam("password", Constants.password));
        params.add(mApiManager.makeParam("operation", "disconnect"));
        params.add(mApiManager.makeParam("range", 2 + ""));
        params.add(mApiManager.makeParam("timeout", "-1"));

        mApiManager.ipgwPost(params,new Response.Listener<String>() {
            @Override
            public void onResponse(String str) {
                try {
                    Log.d("IPGW RETURN STR", str);

                    callback.onFinished(0,str);

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
    public void disconnectAll() {

    }


    public void getAQI(final Callback<AQIEntity> callback){
        ArrayList<ApiManager.Parameter> params = new ArrayList<>();

        mApiManager.get(params, "aqi",null,false,true, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {

                Log.d("AQI String",s);
                if (s.isEmpty() || s.equals("")){
                    callback.onFinished(-1,null);
                }
                else {
                    AQIEntity aqiEntity = new AQIEntity(s);
                    callback.onFinished(0, aqiEntity);
                }
            }
        },new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                callback.onError(volleyError.getMessage());
            }
        });


    }
}
