package com.pkuhelper.model;

public interface Callback<T> {

    void onFinished(int code, T data);

    void onError(String msg);
}