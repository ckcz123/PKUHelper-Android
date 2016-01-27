package com.pkuhelper.ui.hole.impl;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;

import com.pkuhelper.presenter.HolePresenter;
import com.pkuhelper.R;
import com.pkuhelper.entity.HoleListItemEntity;
import com.pkuhelper.ui.BaseActivity;
import com.pkuhelper.ui.CompatListView;
import com.pkuhelper.ui.hole.HoleListAdapter;
import com.pkuhelper.ui.hole.HoleViewPagerAdapter;
import com.pkuhelper.ui.hole.IHoleUI;

import java.util.ArrayList;

public class MHoleActivity extends BaseActivity implements IHoleUI, NavigationView.OnNavigationItemSelectedListener {

    private HolePresenter holePresenter;
    private HoleListAdapter holeListAdapter;
    private HoleListAdapter attentionListAdapter;
    private CompatListView listViewMain;
    private CompatListView listViewAttention;
    private ViewPager viewPager;
    private ContentLoadingProgressBar pbMore, pbRefresh;
    private FloatingActionButton fab;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hole);

        LayoutInflater inflater = getLayoutInflater();
        View view1 = inflater.inflate(R.layout.listview_hole_content, null);
        View view2 = inflater.inflate(R.layout.listview_hole_content, null);
        listViewMain = (CompatListView) view1.findViewById(R.id.MHole_listview);
        listViewAttention = (CompatListView) view2.findViewById(R.id.MHole_listview);

        ArrayList<View> viewList = new ArrayList<>();
        viewList.add(listViewMain);
        viewList.add(listViewAttention);
        viewPager = (ViewPager) findViewById(R.id.vp_hole_content);
        viewPager.setAdapter(new HoleViewPagerAdapter(viewList));

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_hole_search:
                        break;
                    case R.id.action_hole_settings:
                        break;
                }

                return false;
            }
        });

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tabLayout.getTabAt(0).isSelected())
                    listViewMain.smoothScrollToPosition(0);
                else
                    listViewAttention.smoothScrollToPosition(0);
            }
        });
        setSupportActionBar(toolbar);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setText("树洞主页").select();
        tabLayout.getTabAt(1).setText("我的收藏");

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
        holePresenter.attentionLoad();

        listViewMain.setOnScrollListener(new AbsListView.OnScrollListener() {
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

        setupView();
    }


    @Override
    public void firstLoad(final ArrayList<HoleListItemEntity> list){
        pbRefresh.setVisibility(View.GONE);

        fab.setVisibility(View.VISIBLE);

        Log.d("List Num:", "" + list.size());
        holeListAdapter = new HoleListAdapter(this,list);
        listViewMain.setAdapter(holeListAdapter);
    }

    @Override
    public void moreLoad(final ArrayList<HoleListItemEntity> list){
        pbMore.setVisibility(View.GONE);

        if (listViewMain != null) {
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
    public void loadAttention(ArrayList<HoleListItemEntity> list) {
        attentionListAdapter = new HoleListAdapter(this,list);
        listViewAttention.setAdapter(attentionListAdapter);
    }

    @Override
    public void error(){
        pbMore.setVisibility(View.GONE);
        pbRefresh.setVisibility(View.GONE);

        Snackbar.make(listViewMain, "加载失败", Snackbar.LENGTH_LONG).setAction("重试", new View.OnClickListener() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_hole, menu);
        menu.findItem(R.id.action_hole_search).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
               /*
               * @todo 实现搜索
               * */
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void setupView() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_ip_gateway) {

        } else if (id == R.id.nav_course_table) {

        } else if (id == R.id.nav_school_life) {

        } else if (id == R.id.nav_my_pku) {

        } else if (id == R.id.nav_hole) {
            //mPkuHelperPresenter.startHoleUI();
        } else if (id == R.id.nav_bbs) {

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}
