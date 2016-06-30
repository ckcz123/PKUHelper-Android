package com.pkuhelper.ui.secondHand;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.pkuhelper.R;
import com.pkuhelper.entity.SecondHandCategoryEntity;
import com.pkuhelper.entity.SecondHandItemEntity;
import com.pkuhelper.presenter.ISecondHandPresenter;
import com.pkuhelper.ui.CompatListView;
import com.pkuhelper.ui.hole.IHoleListUI;
import com.pkuhelper.util.SizeUtil;

import java.util.ArrayList;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.MaterialHeader;

/**
 * Created by zyxu on 4/4/16.
 */
public class SecondHandListFragment extends Fragment implements ISecondHandList {
    private static final String ARG_POSITION = "Position_SecondHandListFragment";

    private static final String TAG="SecondHandListFragment";

    private ISecondHandPresenter mPresenter;
    public int showOrder;
    private SecondHandCategoryEntity mEntity;

    private Context mContext;
    private SecondHandListAdapter mAdapter;
    private CompatListView listView;
    private PtrClassicFrameLayout ptrLayout;
    private boolean isVisible;
    private boolean isInitialized = false;
    private ArrayList<SecondHandItemEntity> mData;

    public static SecondHandListFragment newInstance(int position) {
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        SecondHandListFragment fragment = new SecondHandListFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public void setArgs(int position, SecondHandCategoryEntity entity,  ISecondHandPresenter presenter){
        showOrder = position;
        mEntity = entity;
        mPresenter = presenter;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser){
        super.setUserVisibleHint(isVisibleToUser);

        Log.d(TAG, "showOrder:" + showOrder + "");
        if (getUserVisibleHint()){
            isVisible = true;
            onVisible();
        }
        else{
            isVisible = false;
        }
    }

    private void onVisible(){

        if (!isInitialized) {
            if (mEntity != null) {
                Log.d(TAG, "request:" + mEntity.getId());
                String category = mEntity.getId();
                mPresenter.load(this, category);
            } else {
                mPresenter.load(this, "");
            }
        }

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_secondhand_list, container, false);

            setupListView(view);
            setupPtrLayout(view);

        if (isInitialized && mData!=null)
            setupAdapter(mData);
        return view;
    }

    private void setupListView(View view) {
        listView = (CompatListView) view.findViewById(R.id.lv_secondhand);
    }

    /**
     * 配置PullToRefreshLayout
     * @param view rootView
     */
    private void setupPtrLayout(View view) {
        ptrLayout = (PtrClassicFrameLayout) view.findViewById(R.id.ptr_secondhand_list);

        // TODO: 4/4/16 目前都不可刷新
        ptrLayout.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return false;
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {}
        });
        
        return;
    }

    public void setupAdapter(ArrayList<SecondHandItemEntity> entities){
        mAdapter = new SecondHandListAdapter(mContext,entities);
        try {
            listView.setAdapter(mAdapter);
        }catch (Exception e){
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public void showList(ArrayList<SecondHandItemEntity> data) {
        mData = data;
        setupAdapter(mData);
        isInitialized = true;
    }
}
