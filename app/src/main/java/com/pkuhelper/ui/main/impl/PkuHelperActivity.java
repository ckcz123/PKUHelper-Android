package com.pkuhelper.ui.main.impl;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.pkuhelper.R;
import com.pkuhelper.presenter.IPkuHelperPresenter;
import com.pkuhelper.presenter.impl.PkuHelperPresenter;
import com.pkuhelper.ui.BaseActivity;
import com.pkuhelper.ui.ipgw.impl.IPGWFragment;
import com.pkuhelper.ui.main.IPkuHelperUI;

import org.w3c.dom.Text;

/**
 * Created by Liangchen Luo on 16/1/14, who is enjoying the delicious
 * dessert made of fruit and yogurt.
 * @author Liangchen Luo
 */
public class PkuHelperActivity extends BaseActivity implements IPkuHelperUI, NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "PkuHelperActivity";

    private IPkuHelperPresenter mPkuHelperPresenter;

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private TextView tvUserName;
    private TextView tvUserDepartment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pkuhelper);

        setupToolbar();
        setupDrawer();
        mPkuHelperPresenter = new PkuHelperPresenter(this);
        mPkuHelperPresenter.setPkuHelperUI(this);
        mPkuHelperPresenter.setupUserInfoInDrawer();

    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupDrawer() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        // 禁用默认的ColorTint，使得icon可以显示出原本的颜色而非被强制转换成灰色
        // 在XML中给app:itemIconTint设定为@null并没有效果，疑似NavigationView的坑
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);

        View headerLayout = navigationView.getHeaderView(0);
        tvUserName = (TextView) headerLayout.findViewById(R.id.tv_user_name);
        tvUserDepartment = (TextView) headerLayout.findViewById(R.id.tv_user_department);
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

            //DEV
            //ZY MAR 9
            Log.d("fragment","ipgw start");
            Fragment ipgwFragment = new IPGWFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_main,ipgwFragment).commit();

            //END DEV

        } else if (id == R.id.nav_syllabus) {

        } else if (id == R.id.nav_school_life) {

        } else if (id == R.id.nav_my_pku) {

        } else if (id == R.id.nav_hole) {
            mPkuHelperPresenter.startHoleUI();
        } else if (id == R.id.nav_bbs) {

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void setUserNameInDrawer(String name) {
        tvUserName.setText(name);
    }

    @Override
    public void setUserDepartmentInDrawer(String department) {
        tvUserDepartment.setText(department);
    }
}
