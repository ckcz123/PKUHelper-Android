package com.pkuhelper.ui.secondHand;

import com.pkuhelper.entity.SecondHandCategoryEntity;
import com.pkuhelper.entity.SecondHandItemEntity;

import java.util.ArrayList;

/**
 * Created by zyxu on 4/4/16.
 */
public interface ISecondHandUI {
    void setupViewPager(ArrayList<SecondHandCategoryEntity> entities);
    void hideProgressBar();
}
