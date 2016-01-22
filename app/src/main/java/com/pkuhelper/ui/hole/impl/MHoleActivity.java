package com.pkuhelper.ui.hole.impl;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pkuhelper.presenter.HolePresenter;
import com.pkuhelper.R;
import com.pkuhelper.entity.HoleListItemEntity;
import com.pkuhelper.ui.BaseActivity;
import com.pkuhelper.ui.hole.HoleListAdapter;
import com.pkuhelper.ui.hole.IHoleUI;

import java.util.ArrayList;

public class MHoleActivity extends BaseActivity implements IHoleUI {

    private HolePresenter holePresenter;
    private HoleListAdapter holeListAdapter;
    private ListView listView;
    private ContentLoadingProgressBar pbMore, pbRefresh;
    private FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mhole);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab_hole_post);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HolePostFragment holePostFragment=new HolePostFragment();
                Bundle bundle = new Bundle();
                bundle.putString("start-type","hole");
                holePostFragment.setArguments(bundle);
                holePostFragment.show(getSupportFragmentManager(), holePostFragment.getTag());
            }
        });

        pbMore = (ContentLoadingProgressBar) findViewById(R.id.pb_hole_more);
        pbRefresh = (ContentLoadingProgressBar) findViewById(R.id.pb_hole_refresh);
        pbMore.setVisibility(View.GONE);
        pbRefresh.setVisibility(View.VISIBLE);

        holePresenter = new HolePresenter(this);
        holePresenter.firstLoad();

        listView = (ListView) findViewById(R.id.MHole_listview);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount != 0) {
                    int lastItem = firstVisibleItem + visibleItemCount;
                    int itemLeft = 0;
                    if (lastItem >= totalItemCount - itemLeft)
                        holePresenter.moreLoad();
                }
            }
        });
    }


    @Override
    public void firstLoad(final ArrayList<HoleListItemEntity> list){
        pbRefresh.setVisibility(View.GONE);

        fab.setVisibility(View.VISIBLE);

        Log.d("List Num:", "" + list.size());
        holeListAdapter = new HoleListAdapter(this,list);
        listView.setAdapter(holeListAdapter);
    }

    @Override
    public void moreLoad(final ArrayList<HoleListItemEntity> list){
        pbMore.setVisibility(View.GONE);

        if (listView != null) {
            Log.d("listview","should in");
            holeListAdapter.addItems(list);
            holeListAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void refreshLoad(final ArrayList<HoleListItemEntity> list) {
        //TO-DO 加入
    }

    @Override
    public void error(){
        pbMore.setVisibility(View.GONE);
        pbRefresh.setVisibility(View.GONE);

        Snackbar.make(listView, "加载失败", Snackbar.LENGTH_LONG).setAction("重试", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holePresenter.firstLoad();
            }
        }).show();
        Log.e("ERROR:", "树洞加载失败");
    }

    @Override
    public void loading(){
        pbRefresh.setVisibility(View.VISIBLE);
    }

    @Override
    public void loadingMore(){
        pbMore.setVisibility(View.VISIBLE);
    }

}
