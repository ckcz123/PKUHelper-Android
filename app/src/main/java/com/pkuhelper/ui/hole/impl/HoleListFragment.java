package com.pkuhelper.ui.hole.impl;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.pkuhelper.R;
import com.pkuhelper.entity.HoleListItemEntity;
import com.pkuhelper.presenter.IHolePresenter;
import com.pkuhelper.ui.CompatListView;
import com.pkuhelper.ui.hole.adapter.HoleListAdapter;
import com.pkuhelper.ui.hole.IHoleListUI;
import com.pkuhelper.util.SizeUtil;

import java.util.ArrayList;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.MaterialHeader;

/**
 * Created by LuoLiangchen on 16/1/30.
 */
public class HoleListFragment extends Fragment implements IHoleListUI {
    private static final String TAG = "HoleListFragment";

    private IHolePresenter mHolePresenter;
    private HoleListAdapter mAdapter;
    private Context mContext;

    private PtrClassicFrameLayout ptrLayout;
    private CompatListView listView;
    private int mPosition;

    /**
     * 新建HoleListFragment实例
     * @param position 在ViewPager中的位置
     * @return 实例
     */
    public static HoleListFragment newInstance(int position) {
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        HoleListFragment fragment = new HoleListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPosition = getArguments() != null ? getArguments().getInt(ARG_POSITION) : POSITION_MAIN;
        mContext = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hole_list, container, false);
        setupListView(view);
        setupPtrLayout(view);
        return view;
    }

    private void setupListView(View view) {
        listView = (CompatListView) view.findViewById(R.id.lv_hole);

        // TODO: 16/2/2 上拉加载
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount != 0) {
                    int lastItem = firstVisibleItem + visibleItemCount;
                    int itemLeft = 0;
                    if (lastItem >= totalItemCount - itemLeft) {
//                        mHolePresenter.loadMore();
                    }
                }
            }
        });
    }

    /**
     * 配置PullToRefreshLayout
     * @param view rootView
     */
    private void setupPtrLayout(View view) {
        ptrLayout = (PtrClassicFrameLayout) view.findViewById(R.id.ptr_hole_list);

        // 仅当Fragment是主页面时再允许下拉刷新
        if (mPosition == IHoleListUI.POSITION_MAIN) {
            ptrLayout.setPtrHandler(new PtrDefaultHandler() {
                @Override
                public void onRefreshBegin(PtrFrameLayout ptrFrameLayout) {
                    mHolePresenter.pullToRefresh();
                }
            });
        } else {
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

        // 设置下拉刷新时ListView保持不动（仿Chrome刷新效果）
        ptrLayout.setPinContent(true);

        // set material pull-to-refresh progressBar
        final MaterialHeader header = new MaterialHeader(getActivity());
        header.setPtrFrameLayout(ptrLayout);
        int[] colors = {
                ContextCompat.getColor(mContext, R.color.colorPrimaryDark),
                ContextCompat.getColor(mContext, R.color.colorPrimary) };
        header.setColorSchemeColors(colors);
        header.setLayoutParams(new PtrFrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        header.setPadding(0, SizeUtil.dip2px(mContext, 16), 0, SizeUtil.dip2px(mContext, 16));
        ptrLayout.setHeaderView(header);
        ptrLayout.addPtrUIHandler(header);
    }

    public HoleListFragment setPresenter(IHolePresenter presenter) {
        mHolePresenter = presenter;
        return this;
    }

    @Override
    public void completePullToRefresh() {
        ptrLayout.refreshComplete();
    }

    @Override
    public HoleListAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void setupAdapter(ArrayList<HoleListItemEntity> entities) {
        mAdapter = new HoleListAdapter(mContext, entities);
        listView.setAdapter(mAdapter);
    }
}
