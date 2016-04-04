package com.pkuhelper.presenter.impl;

import android.content.Context;
import android.util.Log;

import com.pkuhelper.AppContext;
import com.pkuhelper.entity.SecondHandItemEntity;
import com.pkuhelper.model.Callback;
import com.pkuhelper.model.ISecondHandMod;
import com.pkuhelper.model.impl.SecondHandMod;
import com.pkuhelper.presenter.ISecondHandPresenter;
import com.pkuhelper.ui.secondHand.ISecondHandList;
import com.pkuhelper.ui.secondHand.ISecondHandUI;

import java.util.ArrayList;

/**
 * Created by zyxu on 4/4/16.
 */
public class SecondHandPresenter implements ISecondHandPresenter {
    public static String TAG = "SecondHandPresenter";

    private final String strSale = "sale";
    private final String strRequire = "require";
    ISecondHandMod secondHandMod;
    ISecondHandUI secondHandUI;
    ISecondHandList secondHandList;
    AppContext mAppContext;
    Context mContext;

    public SecondHandPresenter(Context context) {
        mContext = context;
        mAppContext = (AppContext) context.getApplicationContext();
        secondHandMod = new SecondHandMod(context);
    }

    @Override
    public void setUI(ISecondHandUI ui){
        secondHandUI = ui;
    }

    @Override
    public void setListUI(ISecondHandList listUI) {
        secondHandList = listUI;
    }

    @Override
    public void load() {
        getList(strSale,0,"","","");
    }

    public void getList(String type, int page, String category1, String category2, String keywords){
        Callback<ArrayList<SecondHandItemEntity>> callback = new Callback<ArrayList<SecondHandItemEntity>>() {
            @Override
            public void onFinished(int code, ArrayList<SecondHandItemEntity> data) {
                int size = data.size();
                if (size>0) {
                    secondHandList.showList(data);
                    secondHandUI.hideProgressBar();
                }
            }

            @Override
            public void onError(String msg) {
                Log.d(TAG, msg);
            }
        };
        secondHandMod.getItemList(type,page,category1,category2,keywords,callback);
    }
}
