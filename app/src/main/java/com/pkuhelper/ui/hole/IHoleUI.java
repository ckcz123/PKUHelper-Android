package com.pkuhelper.ui.hole;

import android.view.View;

import com.pkuhelper.entity.HoleListItemEntity;

import java.util.ArrayList;

/**
 * Created by zyxu on 16/1/11.
 */

public interface IHoleUI {
    /**
     * 显示FAB
     */
    void showFloatingActionButton();

    /**
     * 隐藏FAB
     */
    void hideFloatingActionButton();

    /**
     * 显示居中ProgressBar
     */
    void showProgressBarMiddle();

    /**
     * 隐藏居中ProgressBar
     */
    void hideProgressBarMiddle();

    /**
     * 弹出底部错误信息
     * @param msg 错误内容
     */
    void showErrorToast(String msg);

    /**
     * 弹出底部错误信息，并附加操作按钮
     * @param msg 错误内容
     * @param action 操作按钮文本
     * @param onClickListener 操作按钮的点击监听器
     */
    void showErrorToast(String msg, String action, View.OnClickListener onClickListener);

    // 以下两个方法应该改为带有动画的刷新，有比较好的开源库以兼容4.2+版本
//    void loading();
//    void loadingMore();

    // adapter部分移入p层
//    void firstLoad(final ArrayList<HoleListItemEntity> list);

    // adapter部分移入p层
//    void moreLoad(final ArrayList<HoleListItemEntity> list);


//    void refreshLoad(final ArrayList<HoleListItemEntity> list);
//    void loadAttention(final ArrayList<HoleListItemEntity> list);
//    void error();
}
