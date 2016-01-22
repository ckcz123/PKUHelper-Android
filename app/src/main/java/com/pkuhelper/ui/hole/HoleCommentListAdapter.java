package com.pkuhelper.ui.hole;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pkuhelper.AppContext;
import com.pkuhelper.R;
import com.pkuhelper.entity.HoleCommentListItemEntity;
import com.pkuhelper.entity.HoleListItemEntity;
import com.pkuhelper.lib.MyCalendar;
import com.pkuhelper.manager.CalendarManager;
import com.pkuhelper.model.IPkuHoleMod;
import com.pkuhelper.model.impl.PkuHoleMod;
import com.pkuhelper.ui.hole.impl.HoleCommentActivity;
import com.pkuhelper.ui.hole.impl.HolePostFragment;

import java.util.ArrayList;

/**
 * Created by zyxu on 1/20/16.
 */
public class HoleCommentListAdapter extends BaseAdapter {

    private AppContext mContext;
    private IPkuHoleMod mPkuHoleMod;
    private ArrayList<HoleCommentListItemEntity> allItems;
    private HoleCommentActivity activity;

    public HoleCommentListAdapter(Context context, ArrayList<HoleCommentListItemEntity> entities){
        mContext = (AppContext) context.getApplicationContext();
        activity = (HoleCommentActivity) context;
        mPkuHoleMod = new PkuHoleMod(mContext);
        allItems = new ArrayList<>();
        allItems.addAll(entities);
    }

    @Override
    public int getCount() {
        return allItems.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        HoleCommentListItemEntity item = allItems.get(position);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_hole_comment, parent, false);
            holder.findWidgets(convertView);
            //set tag
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.setContent(item);
        return convertView;
    }

    public final class ViewHolder{
        public CardView cardView;
        public TextView tvContent;
        public TextView tvPid;
        public TextView tvTime;
        public ImageView imgLz;

        public void findWidgets(View view){
            cardView = (CardView) view.findViewById(R.id.card_hole_comment);
            tvContent = (TextView) view.findViewById(R.id.tv_hole_comment_content);
            tvPid = (TextView) view.findViewById(R.id.tv_hole_comment_pid);
            tvTime = (TextView) view.findViewById(R.id.tv_hole_comment_time);
            imgLz = (ImageView) view.findViewById(R.id.img_hole_comment_card_lz);
        }

        public void setContent(final HoleCommentListItemEntity item){
            tvContent.setText(item.getText());
            tvPid.setText("#"+item.getCid());
            tvTime.setText(CalendarManager.getDeltaTime(item.getTimestamp() * 1000));
            if (item.getIslz()==1)
                imgLz.setVisibility(View.VISIBLE);
            else
                imgLz.setVisibility(View.GONE);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString("start-type","comment");
                    bundle.putBoolean("isReply", true);
                    bundle.putInt("pid",item.getPid());
                    bundle.putInt("cid",item.getCid());
                    HolePostFragment holePostFragment=new HolePostFragment();
                    holePostFragment.setArguments(bundle);
                    holePostFragment.show(activity.getSupportFragmentManager(), holePostFragment.getTag());
                }
            });
        }
    }
}
