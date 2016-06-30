package com.pkuhelper.ui.secondHand;

import com.pkuhelper.entity.SecondHandItemEntity;

/**
 * Created by zyxu on 4/12/16.
 */
public interface ISecondHandItemUI {

    void hideProgressBar();

    void showProgressBar();

    void setupContent(SecondHandItemEntity<SecondHandItemEntity.ItemImage> mod);
    void showMessage(String msg);
    void startMessageSession(String chatTo);
}
