package com.pkuhelper.presenter;

import com.pkuhelper.ui.main.IPkuHelperUI;

/**
 * Created by LuoLiangchen on 16/1/24.
 */
public interface IPkuHelperPresenter {

    /**
     * 配置PKUHelper主Activity的UI接口
     * @param ui PKUHelper主页UI
     */
    void setPkuHelperUI(IPkuHelperUI ui);

    /**
     * 配置侧边栏的用户姓名和院系
     */
    void setupUserInfoInDrawer();

    /**
     * 启动P大树洞Activity
     */
    void startHoleUI();
}
