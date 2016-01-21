package com.pkuhelper.ui.hole.impl;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mhole);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


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
        pd.dismiss();
        Log.d("List Num:", "" + list.size());
        holeListAdapter = new HoleListAdapter(this,list);
        listView.setAdapter(holeListAdapter);
    }

    @Override
    public void moreLoad(final ArrayList<HoleListItemEntity> list){
        pd.dismiss();
        //listView = (ListView)findViewById(R.id.MHole_listview);

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
        pd.dismiss();
        Snackbar.make(listView, "加载失败", Snackbar.LENGTH_LONG).show();
        Log.e("ERROR:","树洞加载失败");
    }

    @Override
    public void loading(){
        if (pd==null || !pd.isShowing())
            pd = ProgressDialog.show(this, "正在加载", "正在加载数据");
    }

}
