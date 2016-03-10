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


    public IPGWPresenter(Context context, IIPGWUI ui){
        context = (AppContext) context.getApplicationContext();
        mIPGWMod = new IPGWMod(context);
        mIPGWUI = ui;
    }


    public void doConnectFree(){
        Callback<String> callback = new Callback<String>() {
            @Override
            public void onFinished(int code, String data) {
                mIPGWUI.popSnack("免费连接成功");
            }

            @Override
            public void onError(String msg) {
                mIPGWUI.popSnack("error");
            }
        };

        mIPGWMod.doConnect(true, callback);
    }

    public void doConnectPaid(){
        Callback<String> callback = new Callback<String>() {
            @Override
            public void onFinished(int code, String data) {
                mIPGWUI.popSnack("收费连接成功");
            }

            @Override
            public void onError(String msg) {
                mIPGWUI.popSnack("error");
            }
        };

        mIPGWMod.doConnect(false,callback);
    }

    public void doDisconnect(){
        Callback<String> callback = new Callback<String>() {
            @Override
            public void onFinished(int code, String data) {
                mIPGWUI.popSnack("断开成功");
            }

            @Override
            public void onError(String msg) {
                mIPGWUI.popSnack("error");
            }
        };

        mIPGWMod.disconnect(callback);
    }

    public void doDisconnectAll(){

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
                mIPGWUI.popSnack("error");
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
}
