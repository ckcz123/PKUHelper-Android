package com.pkuhelper.presenter;

/**
 * Created by zyxu on 3/9/16.
 */
public interface IIPGWPresenter {

    void doConnectFree();
    void doConnectPaid();
    void doDisconnect();
    void doDisconnectAll();
    void updateAQI();
    int getAQI();
}
