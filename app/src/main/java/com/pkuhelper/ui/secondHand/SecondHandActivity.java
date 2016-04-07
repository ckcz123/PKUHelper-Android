package com.pkuhelper.ui.secondHand;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.pkuhelper.R;
import com.pkuhelper.entity.SecondHandCategoryEntity;
import com.pkuhelper.entity.SecondHandItemEntity;
import com.pkuhelper.presenter.ISecondHandPresenter;
import com.pkuhelper.presenter.impl.SecondHandPresenter;
import com.pkuhelper.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zyxu on 4/4/16.
 */
public class SecondHandActivity extends BaseActivity implements ISecondHandUI {

    private ISecondHandList secondHandList;
    private ISecondHandPresenter mPresenter;
    private SecondHandPagerAdapter mPagerAdapter;
    private ViewPager viewPager;
    private ContentLoadingProgressBar pbInit;
    private Toolbar toolbar;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondhand_main_dev);
        pbInit = (ContentLoadingProgressBar) findViewById(R.id.pb_init);

        setTitle("二手平台");

        mPresenter = new SecondHandPresenter(this);
        mPresenter.setUI(this);
        setupToolbar();
        viewPager = (ViewPager) findViewById(R.id.vp_secondhand_content);;
        setupTabLayout();

        mPresenter.refreshCategory();
    }

    @Override
    public void hideProgressBar() {
        pbInit.hide();
    }

    @Override
    public void setupViewPager(ArrayList<SecondHandCategoryEntity> entities) {
        int size = entities.size();
        List<SecondHandListFragment> fragments = new ArrayList<SecondHandListFragment>(size+1){};
        List<String> titles = new ArrayList<String>(size+1){};

        fragments.add(0,SecondHandListFragment.newInstance(0));
        titles.add(0,"全部");
        mPresenter.setListUI(fragments.get(0),0);
        mPresenter.load(0,"");

        for (int i=1;i<size;i++){
            SecondHandCategoryEntity entity = entities.get(i);
            int showOrder = entity.getShowOrder();
            fragments.add(i,SecondHandListFragment.newInstance(showOrder));
            titles.add(i,entity.getName());

            mPresenter.setListUI(fragments.get(i), showOrder);
            mPresenter.load(showOrder,entity.getId());
        }
        mPagerAdapter = new SecondHandPagerAdapter(getSupportFragmentManager(), fragments);
        mPagerAdapter.setTitles(titles);

        viewPager.setAdapter(mPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }


    private void setupToolbar(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupTabLayout(){
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        //tabLayout.setupWithViewPager(viewPager);
    }
}
