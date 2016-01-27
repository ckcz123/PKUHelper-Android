package com.pkuhelper.presenter;

import android.os.Bundle;

/**
 * Created by zyxu on 16/1/12.
 */
public interface IHolePresenter {
    void firstLoad();
    void moreLoad();
    void refreshLoad();
    void attentionLoad();
    void post(Bundle bundle);
    void reply(int pid, String text);
    void search(String text);
}
