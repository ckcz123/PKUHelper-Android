package com.pkuhelper.presenter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.pkuhelper.entity.HoleCommentListItemEntity;
import com.pkuhelper.model.Callback;
import com.pkuhelper.model.IPkuHoleMod;
import com.pkuhelper.model.impl.PkuHoleMod;
import com.pkuhelper.entity.HoleListItemEntity;
import com.pkuhelper.ui.BaseListAdapter;
import com.pkuhelper.ui.hole.IHoleCommentUI;
import com.pkuhelper.ui.hole.adapter.HoleListAdapter;
import com.pkuhelper.ui.hole.IHoleListUI;
import com.pkuhelper.ui.hole.IHolePostUI;
import com.pkuhelper.ui.hole.IHoleUI;
import com.pkuhelper.ui.hole.impl.HoleListFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zyxu on 16/1/11.
 * @author Ziyang Xu
 */
public class HolePresenter implements IHolePresenter {
    private static final String TAG = "HolePresenter";

    private IHoleUI mHoleUI;
    private IHolePostUI mHolePostUI;
    private IHoleListUI mHoleListMainUI;
    private IHoleListUI mHoleListAttentionUI;
    private IHoleCommentUI mHoleCommentUI;
    private IPkuHoleMod mPkuHoleMod;
    private Context mContext;
    private int mCurrentPage;
    private boolean mIsOnloadingMore;

    public HolePresenter(Context context) {
        mContext = context;
        mPkuHoleMod = new PkuHoleMod(context);
    }

    @Override
    public void setHoleUI(IHoleUI ui) {
        mHoleUI = ui;
    }

    @Override
    public void setListUI(IHoleListUI mainUI, IHoleListUI attentionUI) {
        mHoleListMainUI = mainUI;
        mHoleListAttentionUI = attentionUI;
    }

    @Override
    public void setCommentUI(IHoleCommentUI ui) {
        mHoleCommentUI = ui;
    }

    @Override
    public void init() {
        mHoleUI.showProgressBarMiddle();
        mCurrentPage = 1;
        mIsOnloadingMore = false;
        mPkuHoleMod.getHoleList(mCurrentPage, new Callback<ArrayList<HoleListItemEntity>>() {
            @Override
            public void onFinished(int code, ArrayList<HoleListItemEntity> data) {
                mHoleUI.hideProgressBarMiddle();
                if (code == 0) {
                    Log.v(TAG, "init successful");
                    mHoleUI.showFloatingActionButton();
                    mHoleListMainUI.setupAdapter(data);
                } else {
                    Log.v(TAG, "init failed with code=" + code);
                    mHoleUI.showErrorToast("加载失败", "重试", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            init();
                        }
                    });
                }
            }

            @Override
            public void onError(String msg) {
                Log.v(TAG, "init failed with Volley error msg=" + msg);
                mHoleUI.showErrorToast("加载失败", "重试", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        init();
                    }
                });
            }
        });
        mPkuHoleMod.getAttentionList(new Callback<ArrayList<HoleListItemEntity>>() {
            @Override
            public void onFinished(int code, ArrayList<HoleListItemEntity> data) {
                if (code == 0) {
                    Log.v(TAG, "init attention list successful");
                    mPkuHoleMod.setupAttentionSet(data);
                    mHoleListAttentionUI.setupAdapter(data);
                } else {
                    Log.v(TAG, "init attention list failed with code=" + code);
                }
            }

            @Override
            public void onError(String msg) {
                Log.v(TAG, "init attention failed with Volley error msg=" + msg);
            }
        });
    }

    @Override
    public void pullToRefresh() {
        mCurrentPage = 1;
        mPkuHoleMod.refreshHoleList(IPkuHoleMod.TIMESTAMP_NOW, new Callback<ArrayList<HoleListItemEntity>>() {
            @Override
            public void onFinished(int code, ArrayList<HoleListItemEntity> data) {
                if (code == 0) {
                    Log.v(TAG, "pullToRefresh successful");
                    addEntitiesToAdapterAtStart(data, mHoleListMainUI.getAdapter());
                    mHoleListMainUI.completePullToRefresh();
                } else {
                    Log.v(TAG, "pullToRefresh failed with code=" + code);
//                    mHoleUI.showErrorToast("");
                }
            }

            @Override
            public void onError(String msg) {
                Log.v(TAG, "pullToRefresh failed with Volley error msg=" + msg);
//                mHoleUI.showErrorToast("");
            }
        });
    }

    @Override
    public void loadMore() {
        if (mIsOnloadingMore) return;
        mIsOnloadingMore = true;
        ++mCurrentPage;
        mPkuHoleMod.getHoleList(mCurrentPage, new Callback<ArrayList<HoleListItemEntity>>() {
            @Override
            public void onFinished(int code, ArrayList<HoleListItemEntity> data) {
                mIsOnloadingMore = false;
                if (code == 0) {
                    Log.v(TAG, "loadMore successful");
                    addEntitiesToAdapterAtEnd(data, mHoleListMainUI.getAdapter());
                } else {
                    Log.v(TAG, "loadMore failed with code=" + code);
//                    mHoleUI.showErrorToast("");
                }
            }

            @Override
            public void onError(String msg) {
                Log.v(TAG, "loadMore failed with Volley error msg=" + msg);
//                mHoleUI.showErrorToast("");
                mIsOnloadingMore = false;
            }
        });
    }

    /**
     * 在Entities列表前方加入数据，并更新Adapter
     * @param entities 加入List前方的Item Entities
     */
    private <Entity> void addEntitiesToAdapterAtStart(ArrayList<Entity> entities, BaseListAdapter<Entity> adapter) {
        adapter.addItemsAtStart(entities);
        adapter.notifyDataSetChanged();
    }

    /**
     * 在Entities列表后方加入数据，并更新Adapter
     * @param entities 加入List后方的Item Entities
     */
    private <Entity> void addEntitiesToAdapterAtEnd(ArrayList<Entity> entities, BaseListAdapter<Entity> adapter) {
        adapter.addItems(entities);
        adapter.notifyDataSetChanged();
    }


    public void post(Bundle bundle, Callback callback) throws IOException {
        String type = bundle.getString("type");
        String text="";
        String uri;
        String data = null;
        byte[] bts;
        if (type==null){
            return;
        }

        if (type.equals("text")){
            text = bundle.getString("text");
            mPkuHoleMod.post(type, text, "", 0, callback);
        }
        else if(type.equals("image")){

            text = bundle.getString("text");
            data = bundle.getString("data");
            mPkuHoleMod.post(type, text, data, 0, callback);
        }
        else if(type.equals("audio")){
            uri = bundle.getString("uri");
        }
    }

    public void reply(int pid, String text){
        mPkuHoleMod.reply(pid, text, new Callback<Void>() {
            @Override
            public void onFinished(int code, Void data) {
                if (code == 0) {
                    Log.v(TAG, "reply successful");
                    // TODO: 16/2/2 更新UI
                } else {
                    Log.v(TAG, "reply failed with code=" + code);
//                    mHoleCommentUI.showErrorToast("");
                }
            }

            @Override
            public void onError(String msg) {
                Log.v(TAG, "reply failed with Volley error msg=" + msg);
//                mHoleCommentUI.showErrorToast("");
            }
        });
    }

    public void search(String keyword){
        /*
        * @todo 找到keyword并显示
        *
        * */
    }

}