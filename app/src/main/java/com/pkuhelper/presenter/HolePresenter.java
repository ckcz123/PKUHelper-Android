package com.pkuhelper.presenter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.pkuhelper.model.Callback;
import com.pkuhelper.model.IPkuHoleMod;
import com.pkuhelper.model.impl.PkuHoleMod;
import com.pkuhelper.entity.HoleListItemEntity;
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
 */
public class HolePresenter implements IHolePresenter {
    private static final String TAG = "HolePresenter";

    private IHoleUI mHoleUI;
    private IHolePostUI mHolePostUI;
    private IHoleListUI mHoleListMainUI;
    private IHoleListUI mHoleListAttentionUI;
    private PkuHoleMod mPkuHoleMod;
    private Context mContext;
    private int requestPage;
    private int mCurrentPage;
    private ArrayList<HoleListItemEntity> mods;
    private ArrayList<HoleListItemEntity> mHoleListItemEntities;
    private boolean isLoading = false;

    private Callback callbackMain = null;
    private Callback callbackAttention =null;

    public HolePresenter(Context context) {
        mContext = context;
        mHoleUI = (IHoleUI) context;
        mPkuHoleMod = new PkuHoleMod(context);
//        callbackMain = new Callback() {
//            @Override
//            public void onFinished(int code, Object data) {
//
//                if (code == 0) {
//                    mods = (ArrayList<HoleListItemEntity>) data;
//                    requestPage++;
//                    //TO-DO load data
//                    if (requestPage == 1)
//                        mHoleUI.firstLoad(mods);
//                    else
//                        mHoleUI.moreLoad(mods);
//                } else {
//                    Log.v(TAG, "Volley error on callbackMain: code=" + code);
//                    mHoleUI.error();
//                }
//                isLoading = false;
//            }
//
//            @Override
//            public void onError(String msg) {
//                isLoading = false;
//                Log.v(TAG, "Volley error on callbackMain: volley error msg=" + msg);
//                mHoleUI.error();
//            }
//        };
    }

    @Override
    public void setListUI(List<HoleListFragment> uis) {
        mHoleListMainUI = uis.get(IHoleListUI.POSITION_MAIN);
        mHoleListAttentionUI = uis.get(IHoleListUI.POSITION_ATTENTION);
    }

    @Override
    public void init() {
        mHoleUI.showProgressBarMiddle();
        mCurrentPage = 1;
        mPkuHoleMod.getHoleList(mCurrentPage, new Callback<ArrayList<HoleListItemEntity>>() {
            @Override
            public void onFinished(int code, ArrayList<HoleListItemEntity> data) {
                if (code == 0) {
                    Log.v(TAG, "init successful");
                    mHoleUI.hideProgressBarMiddle();
                    mHoleUI.showFloatingActionButton();
                    mHoleListItemEntities = data;
                    mHoleListMainUI.setupAdapter(data);
                } else {
                    Log.v(TAG, "init failed with code=" + code);
                    mHoleUI.hideProgressBarMiddle();
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
        ++mCurrentPage;
        mPkuHoleMod.getHoleList(mCurrentPage, new Callback<ArrayList<HoleListItemEntity>>() {
            @Override
            public void onFinished(int code, ArrayList<HoleListItemEntity> data) {
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
            }
        });
    }

    /**
     * 在Entities列表前方加入数据，并更新Adapter
     * @param entities 加入List前方的Item Entities
     */
    private void addEntitiesToAdapterAtStart(ArrayList<HoleListItemEntity> entities, HoleListAdapter adapter) {
        adapter.addItemsAtStart(entities);
        adapter.notifyDataSetChanged();
    }

    /**
     * 在Entities列表后方加入数据，并更新Adapter
     * @param entities 加入List后方的Item Entities
     */
    private void addEntitiesToAdapterAtEnd(ArrayList<HoleListItemEntity> entities, HoleListAdapter adapter) {
        adapter.addItems(entities);
        adapter.notifyDataSetChanged();
    }

//    public void firstLoad() {
//
//        isLoading = true;
//        requestPage = 0;
//
//        mHoleUI.loading();
//        //request from manager
//        mPkuHoleMod.getHoleList(requestPage + 1, callbackMain);
//    }
//
//    public void moreLoad() {
//        Log.d("Presenter Status:",String.valueOf(isLoading));
//        if (isLoading)
//            return;
//        isLoading = true;
//        mHoleUI.loadingMore();
//        mPkuHoleMod.getHoleList(requestPage + 1, callbackMain);
//    }
//
//    public void refreshLoad() {
//        //TO-DO refresh list
//    }
//
//    public void attentionLoad(){
//
//        callbackAttention = new Callback() {
//            @Override
//            public void onFinished(int code, Object data) {
//                if (code == 0) {
//                    mHoleUI.loadAttention((ArrayList<HoleListItemEntity>) data);
//                    mPkuHoleMod.setupAttentionSet((ArrayList<HoleListItemEntity>) data);
//                } else {
//                    Log.v(TAG, "Volley error on callbackAttention: code=" + code);
//                    mHoleUI.error();
//                }
//                isLoading = false;
//            }
//
//            @Override
//            public void onError(String msg) {
//                isLoading = false;
//                Log.v(TAG, "Volley error on callbackAttention: volley error msg=" + msg);
//                mHoleUI.error();
//            }
//        };
//
//        mPkuHoleMod.getAttentionList(callbackAttention);
//    }

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

        Callback simpleCallback = new Callback<Void>() {
            @Override
            public void onFinished(int code, Void data) {
                Log.d("success code:",""+code);
            }

            @Override
            public void onError(String msg) {
                Log.d("error",msg);
            }
        };

        mPkuHoleMod.reply(pid,text,simpleCallback);
    }

    public void search(String keyword){
        /*
        * @todo 找到keyword并显示
        *
        * */
    }

}