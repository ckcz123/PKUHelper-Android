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
import com.pkuhelper.entity.SecondHandItemEntity;
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
    private Context mContext;
    private SecondHandListAdapter mAdapter;
    private CompatListView listView;
    private PtrClassicFrameLayout ptrLayout;

    public static SecondHandListFragment newInstance(int position) {
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        SecondHandListFragment fragment = new SecondHandListFragment();
        fragment.setArguments(args);
        return fragment;
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
        setupAdapter(data);
    }
}
