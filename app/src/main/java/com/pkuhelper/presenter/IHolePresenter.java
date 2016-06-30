package com.pkuhelper.presenter;

import android.os.Bundle;

import com.pkuhelper.entity.HoleListItemEntity;
import com.pkuhelper.model.Callback;
import com.pkuhelper.ui.hole.IHoleCommentUI;
import com.pkuhelper.ui.hole.IHoleListUI;
import com.pkuhelper.ui.hole.IHoleUI;
import com.pkuhelper.ui.hole.impl.HoleListFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zyxu on 16/1/12.
 */
public interface IHolePresenter {

    /**
     * 配置树洞主Activity的UI接口
     * @param ui 树洞主页UI
     */
    void setHoleUI(IHoleUI ui);

    /**
     * 配置两个ListView的UI接口
     * @param mainUI 树洞主页List UI
     * @param attentionUI 树洞关注页List UI
     */
    void setListUI(IHoleListUI mainUI, IHoleListUI attentionUI);

    /**
     * 配置树洞详情页的UI接口
     * @param ui 树洞详情页UI
     */
    void setCommentUI(IHoleCommentUI ui);

    /**
     * 进入树洞时初始化
     * 包括HoleList的加载，AttentionSet的初始化
     */
    void init();

    /**
     * 下拉刷新
     */
    void pullToRefresh();

    /**
     * 上拉加载
     */
    void loadMore();

//    void firstLoad();
//    void moreLoad();
//    void refreshLoad();
//    void attentionLoad();
    void post(Bundle bundle, Callback callback) throws IOException;
    void reply(int pid, String text);
    void search(String text);
}
