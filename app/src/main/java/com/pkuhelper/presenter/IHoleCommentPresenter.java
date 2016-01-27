package com.pkuhelper.presenter;

import android.os.Bundle;

import com.pkuhelper.entity.HoleListItemEntity;
import com.pkuhelper.model.Callback;

/**
 * Created by zyxu on 1/19/16.
 */
public interface IHoleCommentPresenter {
    void load(HoleListItemEntity item);
    void reply();
    void setAttention();
    void report(int pid, String reason, Callback callback);
}
