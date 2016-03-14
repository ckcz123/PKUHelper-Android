package com.pkuhelper.presenter.impl;

import android.content.Context;

import com.pkuhelper.AppContext;
import com.pkuhelper.entity.AQIEntity;
import com.pkuhelper.model.Callback;
import com.pkuhelper.model.IIPGWMod;
import com.pkuhelper.model.impl.IPGWMod;
import com.pkuhelper.presenter.IIPGWPresenter;
import com.pkuhelper.ui.ipgw.IIPGWUI;

/**
 * Created by zyxu on 3/9/16.
 */
public class IPGWPresenter implements IIPGWPresenter {

    AppContext mContext;
    IIPGWMod mIPGWMod;
    IIPGWUI mIPGWUI;
    AQIEntity aqiEntity;
    boolean isFree=true;

    public IPGWPresenter(Context context, IIPGWUI ui){
        context = (AppContext) context.getApplicationContext();
        mIPGWMod = new IPGWMod(context);
        mIPGWUI = ui;
    }


    public void doConnect(){
        Callback<String> callback = new Callback<String>() {
            @Override
            public void onFinished(int code, String data) {
                if (code == 0) {
                    mIPGWUI.lockCanvas();
                    mIPGWUI.popSnack(data);
                }
                else{
                    mIPGWUI.clearUpCanvas();
                    mIPGWUI.popSnack("连接失败");
                }

            }

            @Override
            public void onError(String msg) {
                mIPGWUI.clearUpCanvas();
                mIPGWUI.popSnack("error");
            }
        };

        mIPGWMod.doConnect(isFree, callback);
    }


    public void doDisconnect(){
        Callback<String> callback = new Callback<String>() {
            @Override
            public void onFinished(int code, String data) {
                if (code == 0){
                    mIPGWUI.unlockCanvas();
                    mIPGWUI.clearUpCanvas();
                    mIPGWUI.popSnack(data);
                }
                else
                    mIPGWUI.popSnack("断开失败");
            }

            @Override
            public void onError(String msg) {
                mIPGWUI.popSnack("error");
            }
        };

        mIPGWMod.disconnect(callback,false);
    }

    public void doDisconnectAll(){
        Callback<String> callback = new Callback<String>() {
            @Override
            public void onFinished(int code, String data) {
                if (code == 0) {
                    mIPGWUI.unlockCanvas();
                    mIPGWUI.clearUpCanvas();
                    mIPGWUI.popSnack(data);
                }
                else
                    mIPGWUI.popSnack("断开失败");
            }

            @Override
            public void onError(String msg) {
                mIPGWUI.popSnack("error");
            }
        };

        mIPGWMod.disconnect(callback,true);
    }

    public void updateAQI(){

        Callback<AQIEntity> callback = new Callback<AQIEntity>() {
            @Override
            public void onFinished(int code, AQIEntity data) {

                aqiEntity = data;
                if (code == 0){
                    mIPGWUI.updateEarthUI(data.getStage());
                }
                else {
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

    public int getAQI(){
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
}
