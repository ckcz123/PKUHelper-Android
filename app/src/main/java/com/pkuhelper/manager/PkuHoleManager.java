package com.pkuhelper.manager;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.pkuhelper.AppContext;
import com.pkuhelper.model.HoleListItemMod;

import java.util.ArrayList;

/**
 * Created by LuoLiangchen on 15/11/30.
 */
public class PkuHoleManager {
    private static final String TAG = "PkuHoleManager";

    private AppContext mContext;
    private ApiManager mApiManager;
    private Gson gson = new Gson();

    public PkuHoleManager(Context context) {
        mContext = (AppContext) context.getApplicationContext();
        mApiManager = ApiManager.getInstance();
    }

    /**
     * 封装对ApiManager的请求调用
     * @param params POST参数
     * @param listener 成功时回调
     * @param errorListener 失败时回调
     */
    private void sendRequest(ArrayList<ApiManager.Parameter> params, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        String method = "api";
        String controller = "pkuhole";
        mApiManager.post(params, method, controller, false, true, listener, errorListener);
    }

    /**
     * 获取第page页的树洞列表
     * @param page 页
     * @param callback 回调
     */
    public void getHoleList(int page, final Callback<ArrayList<HoleListItemMod>> callback) {
        ArrayList<ApiManager.Parameter> params = new ArrayList<>();
        params.add(mApiManager.makeParam("action", "getlist"));
        params.add(mApiManager.makeParam("p", "" + page));

        sendRequest(params, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String jsonStr) {
                        try {
                            JsonObject response = gson.fromJson(jsonStr, JsonObject.class);
                            int code = response.get("code").getAsInt();
                            switch (code) {
                                case 0:
                                    ArrayList<HoleListItemMod> mods = new ArrayList<>();
                                    JsonArray array = response.getAsJsonArray("data");
                                    Log.v(TAG, "HoleList: " + array.toString());
                                    for (int i = 0; i < array.size(); ++i) {
                                        JsonElement data = array.get(i);
                                        mods.add(gson.fromJson(data, HoleListItemMod.class));
                                    }
                                    callback.onSuccess(code, mods);
                                    break;
                                default:
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
}
