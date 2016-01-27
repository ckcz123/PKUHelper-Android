package com.pkuhelper.presenter;

import android.os.Bundle;

/**
 * Created by zyxu on 1/19/16.
 */
public interface IHoleCommentPresenter {
    void load(int pid, Bundle bundle);
    void reply();
    void setAttention();
}
