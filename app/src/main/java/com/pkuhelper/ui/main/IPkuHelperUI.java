package com.pkuhelper.ui.main;

/**
 * Created by LuoLiangchen on 16/1/23.
 */
public interface IPkuHelperUI {

    /**
     * 设定侧边栏中的用户姓名
     * @param name 姓名
     */
    void setUserNameInDrawer(String name);

    /**
     * 设定侧边栏中的用户院系
     * @param department 院系
     */
    void setUserDepartmentInDrawer(String department);
}
