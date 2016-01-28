package com.pkuhelper.ui.hole;

import com.pkuhelper.entity.HoleCommentListItemEntity;
import com.pkuhelper.entity.HoleListItemEntity;

import java.util.ArrayList;

/**
 * Created by zyxu on 1/20/16.
 */
public interface IHoleCommentUI {
    void loading();
    void loadCard(HoleListItemEntity data);
    void loadList(ArrayList<HoleCommentListItemEntity> data);
    void error(String text);
}
