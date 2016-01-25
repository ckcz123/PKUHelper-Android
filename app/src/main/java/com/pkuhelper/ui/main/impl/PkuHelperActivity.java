package com.pkuhelper.ui.main.impl;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.pkuhelper.R;
import com.pkuhelper.presenter.IPkuHelperPresenter;
import com.pkuhelper.presenter.impl.PkuHelperPresenter;
import com.pkuhelper.ui.BaseActivity;
import com.pkuhelper.ui.main.IPkuHelperUI;

/**
 * Created by LuoLiangchen on 16/1/14.
 */
public class PkuHelperActivity extends BaseActivity implements IPkuHelperUI, NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "PkuHelperActivity";

    private IPkuHelperPresenter mPkuHelperPresenter;
    private Toolbar toolbar;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pkuhelper);

        mPkuHelperPresenter = new PkuHelperPresenter(this);
        setupToolbar();
        setupView();
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_ip_gateway) {

        } else if (id == R.id.nav_course_table) {

        } else if (id == R.id.nav_school_life) {

        } else if (id == R.id.nav_my_pku) {

        } else if (id == R.id.nav_hole) {
            mPkuHelperPresenter.startHoleUI();
        } else if (id == R.id.nav_bbs) {

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
