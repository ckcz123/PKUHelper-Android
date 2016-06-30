package com.pkuhelper.presenter.impl;

import android.content.Context;

import com.pkuhelper.AppContext;
import com.pkuhelper.entity.AQIEntity;
import com.pkuhelper.model.Callback;
import com.pkuhelper.model.IIPGWMod;
import com.pkuhelper.model.impl.IPGWMod;
import com.pkuhelper.presenter.IIPGWPresenter;
import com.pkuhelper.ui.ipgw.IIPGWUI;

import java.util.Map;

/**
 * Created by zyxu on 3/9/16.
 */
public class IPGWPresenter implements IIPGWPresenter {

    final String strErrorNoMap = "网络请求失败";
    final String strErrorNoConnection = "无网络连接";
    final String strErrorConnectionFull = "连接数已满";
    final String strErrorNotInSchool = "不在校园网范围内";
    final String strErrorDeficit = "欠费";
    final String strErrorUnknown = "未知错误";
    final String strErrorConnect = " 连接失败";
    final String strSuccessConnect = "连接成功";
    final String strErrorDisconnect = "断开失败";
    final String strSuccessDisconnect = "断开成功";

    AppContext mContext;
    IIPGWMod mIPGWMod;
    IIPGWUI mIPGWUI;
    AQIEntity aqiEntity;
    boolean isFree = true;
    Map<String, String> myIPGWEntity = null;


    public IPGWPresenter(Context context, IIPGWUI ui) {
        context = (AppContext) context.getApplicationContext();
        mIPGWMod = new IPGWMod(context);
        mIPGWUI = ui;
    }


    public void doConnect() {
        Callback<Map<String, String>> callback = new Callback<Map<String, String>>() {
            @Override
            public void onFinished(int code, Map<String, String> data) {

                //parse feedback

                //请求失败
                if (data == null || !data.containsKey("SUCCESS")) {
                    mIPGWUI.clearUpCanvas();
                    mIPGWUI.popSnack(strErrorNoMap + strErrorConnect);
                    myIPGWEntity = null;
                } else {
                    //不成功
                    if (!data.get("SUCCESS").equals("YES")) {
                        String strReason = data.get("REASON");
                        mIPGWUI.clearUpCanvas();
                        mIPGWUI.popSnack(strReason + strErrorConnect);
                        myIPGWEntity = null;
                    }
                    //成功
                    else {
                        myIPGWEntity = data;
                        String strExtra = new String();
                        if (data.get("SCOPE").equals("international")) {
                            double timeAll = 0;
                            double timeUsed = 0;
                            timeAll = Double.parseDouble(data.get("FR_DESC_EN").trim().split("[^\\d]")[0]);
                            timeUsed = Double.parseDouble(data.get("FR_TIME"));

                            strExtra = " 收费时长:" + timeUsed + "/" + timeAll + "小时";
                        }

                        strExtra += " 连接数:" + data.get("CONNECTIONS") + "个";
                        mIPGWUI.lockCanvas();
                        mIPGWUI.popSnack(strSuccessConnect + strExtra);
                    }

                }


            }

            @Override
            public void onError(String msg) {
                mIPGWUI.clearUpCanvas();
                mIPGWUI.popSnack(strErrorNoConnection);
            }
        };

        mIPGWMod.doConnect(isFree, callback);
    }


    public void doDisconnect() {
        Callback<Map<String, String>> callback = new Callback<Map<String, String>>() {
            @Override
            public void onFinished(int code, Map<String, String> data) {

                if (data == null || !data.containsKey("SUCCESS") || !data.get("SUCCESS").equals("YES"))
                    mIPGWUI.popSnack(strErrorDisconnect);
                else {
                    mIPGWUI.unlockCanvas();
                    mIPGWUI.clearUpCanvas();
                    mIPGWUI.popSnack(strSuccessDisconnect);
                }
            }

            @Override
            public void onError(String msg) {
                mIPGWUI.popSnack("error");
            }
        };

        mIPGWMod.disconnect(callback, false);
    }

    public void doDisconnectAll() {
        Callback<Map<String, String>> callback = new Callback<Map<String, String>>() {
            @Override
            public void onFinished(int code, Map<String, String> data) {
                if (data == null || !data.containsKey("SUCCESS") || !data.get("SUCCESS").equals("YES"))
                    mIPGWUI.popSnack(strErrorDisconnect);
                else {
                    mIPGWUI.unlockCanvas();
                    mIPGWUI.clearUpCanvas();
                    mIPGWUI.popSnack(strSuccessDisconnect);
                }
            }

            @Override
            public void onError(String msg) {
                mIPGWUI.popSnack("error");
            }
        };

        mIPGWMod.disconnect(callback, true);
    }

    public void updateAQI() {

        Callback<AQIEntity> callback = new Callback<AQIEntity>() {
            @Override
            public void onFinished(int code, AQIEntity data) {

                aqiEntity = data;
                if (code == 0) {
                    mIPGWUI.updateEarthUI(data.getStage());
                } else {
                    mIPGWUI.popSnack("空气质量获取失败");
                }
            }

            @Override
            public void onError(String msg) {
                mIPGWUI.popSnack("空气质量获取失败");
            }
        };

        mIPGWMod.getAQI(callback);
    }

    public int getAQI() {
        if (aqiEntity == null)
            return 0;
        else
            return aqiEntity.getAQI();
    }

    @Override
    public void changeFreeStatus() {
        isFree = !isFree;
        mIPGWUI.changeFreeUI(isFree);
        if (mIPGWUI.isLocked())
            doConnect();
    }

    @Override
    public Map<String, String> getIPGWEntity() {
        return myIPGWEntity;
    }
}
