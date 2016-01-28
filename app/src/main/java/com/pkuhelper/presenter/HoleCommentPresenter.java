package com.pkuhelper.presenter;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.telecom.Call;
import android.widget.Toast;

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
    private ArrayList<HoleCommentListItemEntity> commentEntities;
    private HoleListItemEntity cardEntity;
    private AppContext mContext;
    private int pid;

    public HoleCommentPresenter(Context context){
        mContext = (AppContext) context.getApplicationContext();
        iHoleCommentUI = (IHoleCommentUI) context;
        pkuHoleMod = new PkuHoleMod(context);
    }

    @Override
    public void load(HoleListItemEntity item) {

        iHoleCommentUI.loading();
        iHoleCommentUI.loadCard(item);

        Callback callback = new Callback() {
            @Override
            public void onFinished(int code, Object data) {

                if (code == 0) {
                    commentEntities = (ArrayList<HoleCommentListItemEntity>) data;
                    iHoleCommentUI.loadList(commentEntities);
                } else {
                    iHoleCommentUI.error("树洞评论加载");
                }
            }

            @Override
            public void onError(String msg) {
                iHoleCommentUI.error("树洞评论加载");
            }
        };

        pkuHoleMod.getCommentList(item.getPid(),callback);
    }

    @Override
    public void reply(int pid, String text,Callback callback) {
        pkuHoleMod.reply(pid, text, callback);
    }

    @Override
    public void setAttention(final int pid) {
        final int ATTENTION_ON = 1;
        final int ATTENTION_OFF = 0;

        int tmp;
        if (pkuHoleMod.isOnAttention(pid))
            tmp = ATTENTION_OFF;
        else
            tmp = ATTENTION_ON;
        final int want = tmp;

        Callback callback = new Callback() {
            @Override
            public void onFinished(int code, Object data) {
                if (code == 0) {
                    pkuHoleMod.setOnAttention(pid, want);
                }
                else
                    iHoleCommentUI.error("设置关注");
            }

            @Override
            public void onError(String msg) {
                iHoleCommentUI.error("设置关注");
            }
        };

        pkuHoleMod.setAttention(pid, want, callback);

    }

    @Override
    public void report(int pid, String reason,Callback callback){

        pkuHoleMod.report(pid,reason,callback);
    }

    @Override
    public boolean isOnAttention(int pid){
        return pkuHoleMod.isOnAttention(pid);
    }
}
