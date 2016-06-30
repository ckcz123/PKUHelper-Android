package com.pkuhelper.presenter;

import java.util.Map;

/**
 * Created by zyxu on 3/9/16.
 */
public interface IIPGWPresenter {

    void doConnect();
    void doDisconnect();
    void doDisconnectAll();
    void updateAQI();
    int getAQI();
    void changeFreeStatus();
    Map<String, String> getIPGWEntity();
}
