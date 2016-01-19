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
import com.pkuhelper.entity.HoleCommentListItemEntity;
import com.pkuhelper.entity.HoleListItemEntity;
import com.pkuhelper.manager.ApiManager;
import com.pkuhelper.model.Callback;
import com.pkuhelper.model.IPkuHoleMod;

import java.util.ArrayList;

/**
 * Created by LuoLiangchen on 16/1/9.
 */
public class PkuHoleMod implements IPkuHoleMod {
    private static final String TAG = "PkuHoleMod";

    private AppContext mContext;
    private ApiManager mApiManager;
    private UserMod mUserMod;
    private Gson gson = new Gson();

    public PkuHoleMod(Context context) {
        mContext = (AppContext) context.getApplicationContext();
        mApiManager = ApiManager.getInstance();
        mUserMod = new UserMod(mContext);
    }

    public void updateTimestamp(long timestamp) {
        mContext.updateHoleTimestamp(timestamp);
    }

    public long getTimestamp() {
        return mContext.getHoleTimestamp();
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
     * 封装对ApiManager的请求调用（使用默认回调）
     * @param params POST参数
     * @param callback 回调
     */
    private void sendRequest(ArrayList<ApiManager.Parameter> params, final Callback<Void> callback) {
        sendRequest(params, new Response.Listener<String>() {
            @Override
            public void onResponse(String jsonStr) {
                JsonObject response = gson.fromJson(jsonStr, JsonObject.class);
                int code = response.get("code").getAsInt();
                switch (code) {
                    case 0:
                        callback.onFinished(code, null);
                        break;
                    default:
                        String msg = response.get("msg").getAsString();
                        Log.v(TAG, ("error, code=" + code) + " " + msg);
                        callback.onFinished(code, null);
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
    public void getHoleList(int page, final Callback<ArrayList<HoleListItemEntity>> callback) {
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
                            ArrayList<HoleListItemEntity> mods;
                            mods = gson.fromJson(response.get("data"), new TypeToken<ArrayList<HoleListItemEntity>>() {}.getType());
                            callback.onFinished(code, mods);
                            break;
                        default:
                            String msg = response.get("msg").getAsString();
                            Log.v(TAG, ("error, code=" + code) + " " + msg);
                            callback.onFinished(code, null);
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
    public void getCommentList(int pid, final Callback<ArrayList<HoleCommentListItemEntity>> callback) {
        ArrayList<ApiManager.Parameter> params = new ArrayList<>();
        params.add(mApiManager.makeParam("action", "getcomment"));
        params.add(mApiManager.makeParam("pid", "" + pid));

        sendRequest(params, new Response.Listener<String>() {
            @Override
            public void onResponse(String jsonStr) {
                try {
                    JsonObject response = gson.fromJson(jsonStr, JsonObject.class);
                    int code = response.get("code").getAsInt();
                    switch (code) {
                        case 0:
                            ArrayList<HoleCommentListItemEntity> mods;
                            mods = gson.fromJson(response.get("data"), new TypeToken<ArrayList<HoleCommentListItemEntity>>() {}.getType());
                            callback.onFinished(code, mods);
                            break;
                        default:
                            String msg = response.get("msg").getAsString();
                            Log.v(TAG, ("error, code=" + code) + " " + msg);
                            callback.onFinished(code, null);
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
    public void post(String type, String text, String data, int length, final Callback<Void> callback) {
        // TO-DO: 边界检查，参数检查
        ArrayList<ApiManager.Parameter> params = new ArrayList<>();
        params.add(mApiManager.makeParam("action", "dopost"));

        params.add(mApiManager.makeParam("type", type));
        params.add(mApiManager.makeParam("text", text));
        params.add(mApiManager.makeParam("data", data));
        params.add(mApiManager.makeParam("length", "" + length));

        sendRequest(params, callback);
    }

    @Override
    public void reply(int pid, String text, final Callback<Void> callback) {
        ArrayList<ApiManager.Parameter> params = new ArrayList<>();
        params.add(mApiManager.makeParam("action", "docomment"));
        params.add(mApiManager.makeParam("token", mUserMod.getToken()));
        params.add(mApiManager.makeParam("pid", "" + pid));
        params.add(mApiManager.makeParam("text", text));

        sendRequest(params, callback);
    }

    @Override
    public void setAttention(int pid, int what, final Callback<Void> callback) {
        ArrayList<ApiManager.Parameter> params = new ArrayList<>();
        params.add(mApiManager.makeParam("action", "attention"));
        params.add(mApiManager.makeParam("token", mUserMod.getToken()));
        params.add(mApiManager.makeParam("pid", "" + pid));
        params.add(mApiManager.makeParam("switch", "" + what));

        sendRequest(params, callback);
    }

    @Override
    public void isNewHoleExist(final Callback<Void> callback) {
        ArrayList<ApiManager.Parameter> params = new ArrayList<>();
        params.add(mApiManager.makeParam("action", "hasnew"));
        params.add(mApiManager.makeParam("timestamp", "" + getTimestamp()));

        sendRequest(params, new Response.Listener<String>() {
            @Override
            public void onResponse(String jsonStr) {
                try {
                    JsonObject response = gson.fromJson(jsonStr, JsonObject.class);
                    int code = response.get("code").getAsInt();
                    switch (code) {
                        case NEW_HOLE_EXIST:
                        case NEW_HOLE_NOT_EXIST:
                            callback.onFinished(code, null);
                            break;
                        default:
                            String msg = response.get("msg").getAsString();
                            Log.v(TAG, ("error, code=" + code) + " " + msg);
                            callback.onFinished(code, null);
                    }
                    updateTimestamp(response.get("data").getAsLong());
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
    public void getAttentionList(final Callback<ArrayList<HoleListItemEntity>> callback) {
        ArrayList<ApiManager.Parameter> params = new ArrayList<>();
        params.add(mApiManager.makeParam("action", "getattention"));
        params.add(mApiManager.makeParam("token", mUserMod.getToken()));

        sendRequest(params, new Response.Listener<String>() {
            @Override
            public void onResponse(String jsonStr) {
                try {
                    JsonObject response = gson.fromJson(jsonStr, JsonObject.class);
                    int code = response.get("code").getAsInt();
                    switch (code) {
                        case 0:
                            ArrayList<HoleListItemEntity> mods;
                            mods = gson.fromJson(response.get("data"), new TypeToken<ArrayList<HoleListItemEntity>>() {}.getType());
                            callback.onFinished(code, mods);
                            break;
                        default:
                            String msg = response.get("msg").getAsString();
                            Log.v(TAG, ("error, code=" + code) + " " + msg);
                            callback.onFinished(code, null);
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
    public void getPushSettings(final Callback<Integer> callback) {
        ArrayList<ApiManager.Parameter> params = new ArrayList<>();
        params.add(mApiManager.makeParam("action", "pushsettings_get"));
        params.add(mApiManager.makeParam("token", mUserMod.getToken()));

        sendRequest(params, new Response.Listener<String>() {
            @Override
            public void onResponse(String jsonStr) {
                try {
                    JsonObject response = gson.fromJson(jsonStr, JsonObject.class);
                    int code = response.get("code").getAsInt();
                    switch (code) {
                        case 0:
                            int isPushOn;
                            int isShowContent;
                            int result = PUSH_OFF;
                            JsonObject data = gson.fromJson(response, JsonObject.class);
                            isPushOn = data.get("pkuhole_push").getAsInt();
                            isShowContent = data.get("pkuhole_hide_content").getAsInt();
                            if (isPushOn > 0) result |= PUSH_ON;
                            if (isShowContent > 0) result |= PUSH_SHOW_CONTENT;
                            callback.onFinished(code, result);
                            break;
                        default:
                            String msg = response.get("msg").getAsString();
                            Log.v(TAG, ("error, code=" + code) + " " + msg);
                            callback.onFinished(code, null);
                    }
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    callback.onError(e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse (VolleyError volleyError){
                callback.onError(volleyError.getMessage());
            }
        });
    }

    @Override
    public void setPushSettings(int param, final Callback<Void> callback) {
        ArrayList<ApiManager.Parameter> params = new ArrayList<>();
        params.add(mApiManager.makeParam("action", "pushsettings_set"));
        params.add(mApiManager.makeParam("token", mUserMod.getToken()));

        JsonObject data = new JsonObject();
        data.addProperty("pkuhole_push", (param & PUSH_ON) > 0 ? 1 : 0);
        data.addProperty("pkuhole_hide_content", (param & PUSH_SHOW_CONTENT) > 0 ? 1 : 0);
        params.add(mApiManager.makeParam("data", data.toString()));

        sendRequest(params, callback);
    }

    @Override
    public void report(int pid, String reason, final Callback<Void> callback) {
        ArrayList<ApiManager.Parameter> params = new ArrayList<>();
        params.add(mApiManager.makeParam("action", "report"));
        params.add(mApiManager.makeParam("token", mUserMod.getToken()));
        params.add(mApiManager.makeParam("pid", "" + pid));
        params.add(mApiManager.makeParam("reason", reason));

        sendRequest(params, callback);
    }

    @Override
    public void search(String keywords, int page, int pageSize, String type, final Callback<ArrayList<HoleListItemEntity>> callback) {
        ArrayList<ApiManager.Parameter> params = new ArrayList<>();
        params.add(mApiManager.makeParam("action", "search"));
        params.add(mApiManager.makeParam("keywords", keywords.trim()));
        params.add(mApiManager.makeParam("page", "" + page));
        params.add(mApiManager.makeParam("pagesize", "" + pageSize));
        params.add(mApiManager.makeParam("type", type));

        sendRequest(params, new Response.Listener<String>() {
            @Override
            public void onResponse(String jsonStr) {
                try {
                    JsonObject response = gson.fromJson(jsonStr, JsonObject.class);
                    int code = response.get("code").getAsInt();
                    switch (code) {
                        case 0:
                            ArrayList<HoleListItemEntity> mods;
                            mods = gson.fromJson(response.get("data"), new TypeToken<ArrayList<HoleListItemEntity>>() {}.getType());
                            callback.onFinished(code, mods);
                            break;
                        default:
                            String msg = response.get("msg").getAsString();
                            Log.v(TAG, ("error, code=" + code) + " " + msg);
                            callback.onFinished(code, null);
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
    public String getResourceUrl(String type, String url) {
        String baseUrl = ApiManager.PROTOCOL + "://" + ApiManager.DOMAIN + "/" + ApiManager.SERVICES + "/pkuhole/";
        if (type.equals(TYPE_IMAGE)) {
            return baseUrl + URL_IMAGES + "/" + url;
        } else if (type.equals(TYPE_AUDIO)) {
            return baseUrl + URL_AUDIOS + "/" + url;
        }
        return null;
    }
}
