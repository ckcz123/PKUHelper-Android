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
    public void doConnect(final boolean isFree, final Callback<Map<String,String>> callback) {
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
                    Map<String, String> map = parseFeedback(str);

                    callback.onFinished(0,map);

                } catch (Exception e) {
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
    public void disconnect(final Callback<Map<String,String>> callback, boolean isDisconnectAll) {
        ArrayList<ApiManager.Parameter> params = new ArrayList<>();

        params.add(mApiManager.makeParam("uid", Constants.username));
        params.add(mApiManager.makeParam("password", Constants.password));

        if (isDisconnectAll)
            params.add(mApiManager.makeParam("operation", "disconnectall"));
        else
            params.add(mApiManager.makeParam("operation", "disconnect"));
        params.add(mApiManager.makeParam("range", 2 + ""));
        params.add(mApiManager.makeParam("timeout", "-1"));

        mApiManager.ipgwPost(params,new Response.Listener<String>() {
            @Override
            public void onResponse(String str) {
                try {
                    Map<String, String> map = parseFeedback(str);
                    callback.onFinished(0, map);
                } catch (Exception e) {
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

    private Map<String, String> parseFeedback(String string){
//        SUCCESS 成功与否
//        STATE 连接状态
//        USERNAME 学号
//        FIXRATE 欠费断网是否开启
//        FR_DESC_CN 包月状态，中文
//        FR_DESC_EN 包月状态，英文
//        FR_TIME 包月使用时长
//        SCOPE 连接范围
//        DEFICIT 是否欠费
//        CONNECTIONS 当前连接数
//        BALANCE 账户余额
//        IP 当前IP地址
//        MESSAGE 备注信息

        Map<String, String> map = new HashMap<String, String>();
        int pos1 = string.indexOf("<!--IPGWCLIENT_START");
        int pos2 = string.indexOf("IPGWCLIENT_END-->");

        if (pos1>0 && pos2 >0 && pos2>pos1){
            Log.d("ipgw string",string);
            Log.d("ipgw stat",pos1+" "+pos2);
            String msg = string.substring(pos1, pos2 - 1);

            String[] strings = msg.split(" ");
            for (int i = 0; i < strings.length; i++) {
                String str = strings[i];
                str.trim();
                if (!str.contains("=")) continue;
                String[] strings2 = str.split("=");
                if (strings2.length != 1)
                    map.put(strings2[0], strings2[1]);
                else map.put(strings2[0], "");
            }
            return map;
        }
        else
            return null;


    }

}
