package com.pkuhelper.presenter;

import android.os.Bundle;

import com.pkuhelper.model.Callback;

/**
 * Created by zyxu on 1/19/16.
 */
public interface IHoleCommentPresenter {
    void load(int pid, Bundle bundle);
    void reply();
    void setAttention();
    void report(int pid, String reason, Callback callback);
}
