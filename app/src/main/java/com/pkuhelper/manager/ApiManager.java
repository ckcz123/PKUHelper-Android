package com.pkuhelper.manager;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.pkuhelper.AppContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by LuoLiangchen on 15/11/28.
 */
public class ApiManager {
    private static final String TAG = "ApiManager";

    public static final int CODE_SUCCESS = 0;

    public static String PROTOCOL = "http";
    public static String DOMAIN = "www.xiongdianpku.com";
    public static String SERVICES = "services";
    public static String APP_NAME = "pkuhelper";

    private static RequestQueue mReqQueue;
    private static ApiManager apiManager;
    private AppContext mContext;

    private ApiManager(Context context) {
        mContext = (AppContext) context.getApplicationContext();
        mReqQueue = Volley.newRequestQueue(mContext);
    }

    public synchronized static void newInstance(Context context) {
        if (apiManager == null) {
            apiManager = new ApiManager(context);
        }
    }

    public static ApiManager getInstance() {
        return apiManager;
    }

    /**
     * 构造接口地址
     * @param method 方法名
     * @param controller 接口逻辑
     * @param isPrivateApi 是否为PKU Helper私有API
     * @param isService 是否为Service
     * @return 接口地址
     */
    private String buildUrl(String method, String controller, boolean isPrivateApi, boolean isService) {
        String url = PROTOCOL + "://" + DOMAIN + "/";
        if (isService) url += SERVICES + "/";
        if (isPrivateApi) url += APP_NAME + "/";
        if (controller != null && !"".equals(controller)) url += controller + "/";
        Log.v(TAG, "request url= " + url + method + ".php");
        return url + method + ".php";
    }

    /**
     * GET访问接口（无参数，无接口逻辑控制，PKU Helper私有API，Service）
     * @param method 接口方法名
     * @param listener 成功时回调
     * @param errorListener 错误时回调
     */
    public void get(String method, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        get(method, null, true, true, listener, errorListener);
    }

    /**
     * GET访问接口（无参数）
     * @param method 接口方法名
     * @param controller 接口逻辑
     * @param isPrivateApi 是否为PKU Helper私有API
     * @param isService 是否为Service
     * @param listener 成功时回调
     * @param errorListener 失败时回调
     */
    public void get(String method, String controller, boolean isPrivateApi, boolean isService,
                    Response.Listener<String> listener, Response.ErrorListener errorListener) {
        get(null, method, controller, isPrivateApi, isService, listener, errorListener);
    }

    /**
     * GET访问接口（无接口逻辑控制，PKU Helper私有API，Service）
     * @param params GET的参数
     * @param method 接口方法名
     * @param listener 成功时回调
     * @param errorListener 错误时回调
     */
    public void get(final ArrayList<Parameter> params, String method, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        get(params, method, null, true, true, listener, errorListener);
    }

    /**
     * GET访问接口
     * @param params GET的参数
     * @param method 接口方法名
     * @param controller 接口逻辑
     * @param isPrivateApi 是否为PKU Helper私有API
     * @param isService 是否为Service
     * @param listener 成功时回调
     * @param errorListener 失败时回调
     */
    public void get(final ArrayList<Parameter> params, String method, String controller, boolean isPrivateApi, boolean isService,
                    Response.Listener<String> listener, Response.ErrorListener errorListener) {
        String url = buildUrl(method, controller, isPrivateApi, isService);
        StringRequest stringRequest = new StringRequest(url, listener, errorListener) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                if (params == null || params.isEmpty()) return null;

                Map<String, String> map = new HashMap<>();
                for (int i = 0; i < params.size(); ++i) {
                    map.put(params.get(i).name, params.get(i).value);
                }
                return map;
            }
        };

        mReqQueue.add(stringRequest);
    }

    /**
     * POST访问接口（无接口逻辑控制，PKU Helper私有API，Service）
     * @param params POST的参数
     * @param method 接口方法名
     * @param listener 成功时回调
     * @param errorListener 失败时回调
     */
    public void post(final ArrayList<Parameter> params, String method, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        post(params, method, null, true, true, listener, errorListener);
    }

    /**
     * POST访问接口
     * @param params POST的参数
     * @param method 接口方法名
     * @param controller 接口逻辑
     * @param isPrivateApi 是否为PKU Helper私有API
     * @param isService 是否为Service
     * @param listener 成功时回调
     * @param errorListener 失败时回调
     */
    public void post(final ArrayList<Parameter> params, String method, String controller, boolean isPrivateApi, boolean isService,
                     Response.Listener<String> listener, Response.ErrorListener errorListener) {
        String url = buildUrl(method, controller, isPrivateApi, isService);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, listener, errorListener) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                for (int i = 0; i < params.size(); ++i) {
                    map.put(params.get(i).name, params.get(i).value);
                }
                return map;
            }
        };

        mReqQueue.add(stringRequest);
    }

    public Parameter makeParam(String name, String value) {
        return new Parameter(name, value);
    }

    public class Parameter {
        public String name;
        public String value;

        public Parameter(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }
}
