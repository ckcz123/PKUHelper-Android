package com.pkuhelper.ui.hole.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.pkuhelper.ui.CompatListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zyxu on 1/26/16.
 */
public class HoleViewPagerAdapter extends PagerAdapter {
    private List<View> compatListViews = new ArrayList<View>();

    public HoleViewPagerAdapter(List<View> views){
        this.compatListViews = views;
    }

    @Override
    public int getCount() {
        return compatListViews.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        try {
            if(compatListViews.get(position).getParent()==null)
                container.addView(compatListViews.get(position), 0);
            else{
                ((ViewGroup)compatListViews.get(position).getParent()).removeView(compatListViews.get(position));
                container.addView(compatListViews.get(position), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return compatListViews.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((View)object);
    }
}
