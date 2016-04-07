package com.pkuhelper.presenter;

import com.pkuhelper.ui.secondHand.ISecondHandList;
import com.pkuhelper.ui.secondHand.ISecondHandUI;

/**
 * Created by zyxu on 4/4/16.
 */
public interface ISecondHandPresenter {

    void setUI(ISecondHandUI ui);
    void setListUI(ISecondHandList listUI, int showOrder);
    void load(int showOrder, String category1);
    void refreshCategory();
}
