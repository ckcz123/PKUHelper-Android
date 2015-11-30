package com.pkuhelper.manager;

/**
 * Created by LuoLiangchen on 15/11/30.
 */
public interface Callback<T> {

    void onSuccess(T data);

    void onError(String msg);
}
