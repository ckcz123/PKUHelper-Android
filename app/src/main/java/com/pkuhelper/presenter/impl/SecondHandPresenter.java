package com.pkuhelper.presenter.impl;

import android.content.Context;
import android.util.Log;

import com.pkuhelper.AppContext;
import com.pkuhelper.entity.SecondHandCategoryEntity;
import com.pkuhelper.entity.SecondHandItemEntity;
import com.pkuhelper.model.Callback;
import com.pkuhelper.model.ISecondHandMod;
import com.pkuhelper.model.impl.SecondHandMod;
import com.pkuhelper.presenter.ISecondHandPresenter;
import com.pkuhelper.ui.secondHand.ISecondHandList;
import com.pkuhelper.ui.secondHand.ISecondHandUI;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zyxu on 4/4/16.
 */
public class SecondHandPresenter implements ISecondHandPresenter {
    public static String TAG = "SecondHandPresenter";

    private final String strSale = "sale";
    private final String strRequire = "require";
    ISecondHandMod secondHandMod;
    ISecondHandUI secondHandUI;
    List<ISecondHandList> secondHandList = new ArrayList<>(20);
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
    public void setListUI(ISecondHandList listUI, int showOrder) {
        secondHandList.add(listUI);
    }

    @Override
    public void load(int showOrder,String category1) {
        getList(showOrder, strSale, 0, category1, "", "");
    }

    @Override
    public void refreshCategory() {
        Callback<ArrayList<SecondHandCategoryEntity>> callback = new Callback<ArrayList<SecondHandCategoryEntity>>() {
            @Override
            public void onFinished(int code, ArrayList<SecondHandCategoryEntity> data) {
                if (code != 0) return;
                int size = data.size();

                secondHandUI.setupViewPager(data);
            }

            @Override
            public void onError(String msg) {
                Log.d(TAG,msg);
            }
        };


        secondHandMod.getCategoryList(callback);
    }

    public void getList(final int showOrder, String type, int page, String category1, String category2, String keywords){
        Callback<ArrayList<SecondHandItemEntity>> callback = new Callback<ArrayList<SecondHandItemEntity>>() {
            @Override
            public void onFinished(int code, ArrayList<SecondHandItemEntity> data) {
                int size = data.size();
                if (size>0) {
                    secondHandList.get(showOrder).showList(data);
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
