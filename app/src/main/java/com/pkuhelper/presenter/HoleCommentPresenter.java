package com.pkuhelper.presenter;

import android.content.Context;

import com.pkuhelper.AppContext;
import com.pkuhelper.entity.HoleCommentListItemEntity;
import com.pkuhelper.entity.HoleListItemEntity;
import com.pkuhelper.model.Callback;
import com.pkuhelper.model.IPkuHoleMod;
import com.pkuhelper.model.impl.PkuHoleMod;
import com.pkuhelper.ui.hole.HoleListAdapter;
import com.pkuhelper.ui.hole.IHoleCommentUI;
import com.pkuhelper.ui.hole.IHoleUI;

import java.util.ArrayList;

/**
 * Created by zyxu on 1/20/16.
 */
public class HoleCommentPresenter implements IHoleCommentPresenter {

    private IPkuHoleMod pkuHoleMod;
    private IHoleCommentUI iHoleCommentUI;
    private Callback callback = null;
    private ArrayList<HoleCommentListItemEntity> commentEntities;
    private HoleListItemEntity cardEntity;
    private AppContext mContext;

    public HoleCommentPresenter(Context context){
        mContext = (AppContext) context.getApplicationContext();
        iHoleCommentUI = (IHoleCommentUI) context;
        pkuHoleMod = new PkuHoleMod(context);
        callback = new Callback() {
            @Override
            public void onFinished(int code, Object data) {

                if (code == 0) {
                    commentEntities = (ArrayList<HoleCommentListItemEntity>) data;
                    //TO-DO load data
                    iHoleCommentUI.loadList(commentEntities);
                } else {
                    iHoleCommentUI.error();
                }
            }

            @Override
            public void onError(String msg) {
                iHoleCommentUI.error();
            }
        };
    }

    @Override
    public void load(int pid) {
        iHoleCommentUI.loading();

        int num=HoleListAdapter.allItems.size();
        for (int i=0;i<num;i++)
            if (HoleListAdapter.allItems.get(i).getPid()==pid) {
                cardEntity = HoleListAdapter.allItems.get(i);
                break;
            }

        iHoleCommentUI.loadCard(cardEntity);
        pkuHoleMod.getCommentList(pid,callback);

    }

    @Override
    public void reply() {

    }

    @Override
    public void setAttention() {

    }
}