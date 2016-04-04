package com.pkuhelper.ui.secondHand;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pkuhelper.R;
import com.pkuhelper.entity.SecondHandItemEntity;
import com.pkuhelper.model.ISecondHandMod;
import com.pkuhelper.model.impl.SecondHandMod;
import com.pkuhelper.ui.BaseListAdapter;
import com.pkuhelper.ui.CompatListView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by zyxu on 4/4/16.
 */
public class SecondHandListAdapter extends BaseListAdapter<SecondHandItemEntity> {

    private static final String TAG = "SecondHandListAdapter";
    private ISecondHandMod secondHandMod;


    public SecondHandListAdapter(Context context, ArrayList<SecondHandItemEntity> items) {
        super(context, items);
        secondHandMod = new SecondHandMod(mContext);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        SecondHandItemEntity item = allItems.get(position);

        convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_secondhand, parent, false);

        String name = item.getName();
        String description = item.getDescription();
        String price ="¥ "+item.getPrice();


        // TODO: 4/4/16 应该获取showOrder为0的
        String imgFileName = item.getImages().get(0);

        String url = "http://www.xiongdianpku.com/static/secondhand/image/"+imgFileName;
        Log.d(TAG,"URL:"+url);

        TextView tvTitle = (TextView) convertView.findViewById(R.id.tv_secondhand_item_title);
        TextView tvDesp = (TextView) convertView.findViewById(R.id.tv_secondhand_item_content);
        TextView tvPrice = (TextView) convertView.findViewById(R.id.tv_secondhand_item_price);
        ImageView imgContent = (ImageView) convertView.findViewById(R.id.img_secondhand_item_content);

        tvTitle.setText(name);
        tvDesp.setText(description);
        tvPrice.setText(price);

        Picasso.with(mContext).load(url).resize(100,120).centerCrop().error(R.drawable.error).into(imgContent);

        return convertView;
    }
}
