package com.pkuhelper.ui.hole;

import com.pkuhelper.entity.HoleListItemEntity;
import com.pkuhelper.presenter.IHolePresenter;
import com.pkuhelper.ui.hole.adapter.HoleListAdapter;

import java.util.ArrayList;

/**
 * Created by LuoLiangchen on 16/1/30.
 */
public interface IHoleListUI {

    String ARG_POSITION = "Position_HoleListFragment";
    int POSITION_MAIN = 0;
    int POSITION_ATTENTION = 1;

    /**
     * 通知PTR Layout上拉加载已经完成
     */
    void completePullToRefresh();

    /**
     * 获取当前List持有的Adapter
     * @return List对应的Adapter
     */
    HoleListAdapter getAdapter();

    /**
     * 配置当前List的Adapter
     * @param entities 初次加入Adapter的Entities
     */
    void setupAdapter(ArrayList<HoleListItemEntity> entities);
}
