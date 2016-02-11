package com.pkuhelper.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.pkuhelper.AppContext;
import com.pkuhelper.entity.HoleListItemEntity;
import com.pkuhelper.manager.ImageManager;
import com.pkuhelper.model.IPkuHoleMod;
import com.pkuhelper.model.impl.PkuHoleMod;

import java.util.ArrayList;

/**
 * Created by LuoLiangchen on 16/1/23.
 */
public abstract class BaseListAdapter<Entity> extends BaseAdapter {
    private static final String TAG = "BaseListAdapter";

    protected Context mContext;

    protected ArrayList<Entity> allItems;

    public BaseListAdapter(Context context, ArrayList<Entity> items) {
        mContext = context;
        allItems = new ArrayList<>();
        allItems.addAll(items);
    }
    public void addItems(ArrayList<Entity> items){
        allItems.addAll(items);
    }

    public void addItemsAtStart(ArrayList<Entity> items) {
        allItems.addAll(0, items);
    }

    @Override
    public int getCount() {
        return (allItems.size());
    }

    @Override
    public Object getItem(int position) {
        return allItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
