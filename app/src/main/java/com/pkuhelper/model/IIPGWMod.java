package com.pkuhelper.model;

import com.pkuhelper.entity.AQIEntity;

/**
 * Created by zyxu on 3/1/16.
 */
public interface IIPGWMod {


    /**
     * @param isFree 是否免费
     * @param callback 返回
     */
    void doConnect(boolean isFree, final Callback<String> callback);

    /**
     * 断开连接
     * @param callback 返回
     * @param isDisconnectAll 是否断开全部连接
     */
    void disconnect(final Callback<String> callback, boolean isDisconnectAll);


    void getAQI(final Callback<AQIEntity> callback);
}
