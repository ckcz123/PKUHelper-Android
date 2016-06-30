package com.pkuhelper.ui.secondHand;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.List;

/**
 * Created by zyxu on 4/4/16.
 */
public class SecondHandPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = "HolePagerAdapter";
//    private static final int PAGER_SIZE = 2;
    private List<String> titles;
    private List<SecondHandListFragment> mFragments;

    public SecondHandPagerAdapter(FragmentManager fm, List<SecondHandListFragment> fragments) {
        super(fm);
        Log.d(TAG, "Fragment Size:"+fragments.size());
        for (int i=0;i<fragments.size();i++)
            Log.d(TAG, fragments.get(i).showOrder+"");
        mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        Log.d(TAG,"position:"+position);
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return titles.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }

    public void setTitles(List<String> titles){
        this.titles = titles;
    }
}
