package com.pkuhelper.ui.secondHand;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by zyxu on 4/4/16.
 */
public class SecondHandPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = "HolePagerAdapter";
    private static final int PAGER_SIZE = 2;

    private List<SecondHandListFragment> mFragments;

    public SecondHandPagerAdapter(FragmentManager fm, List<SecondHandListFragment> fragments) {
        super(fm);
        mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return PAGER_SIZE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "全部";
            case 1:
                return "其它";
        }
        return null;
    }
}
