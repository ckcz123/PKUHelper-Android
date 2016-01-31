package com.pkuhelper.ui.hole.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.pkuhelper.ui.hole.IHoleListUI;
import com.pkuhelper.ui.hole.impl.HoleListFragment;

import java.util.List;

/**
 * Created by LuoLiangchen on 16/1/31.
 */
public class HolePagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = "HolePagerAdapter";
    private static final int PAGER_SIZE = 2;

    private List<HoleListFragment> mFragments;

    public HolePagerAdapter(FragmentManager fm, List<HoleListFragment> fragments) {
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
            case IHoleListUI.POSITION_MAIN:
                return "树洞";
            case IHoleListUI.POSITION_ATTENTION:
                return "关注";
        }
        return null;
    }
}
