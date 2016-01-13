package com.pkuhelper.view;

import com.pkuhelper.entity.HoleListItemEntity;

import java.util.ArrayList;

/**
 * Created by zyxu on 16/1/11.
 */

public interface IHoleView {
    void loading();
    void firstLoad(final ArrayList<HoleListItemEntity> list);
    void moreLoad(final ArrayList<HoleListItemEntity> list);
    void refreshLoad(final ArrayList<HoleListItemEntity> list);
    void error();
}
