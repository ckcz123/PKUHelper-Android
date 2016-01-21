package com.pkuhelper.ui.hole;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pkuhelper.AppContext;
import com.pkuhelper.R;
import com.pkuhelper.entity.HoleListItemEntity;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.MyCalendar;
import com.pkuhelper.manager.ImageManager;
import com.pkuhelper.model.IPkuHoleMod;
import com.pkuhelper.model.impl.PkuHoleMod;
import com.pkuhelper.ui.hole.impl.HoleCommentActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by zyxu on 16/1/13.
 */
public class HoleListAdapter extends BaseAdapter {
    private static final String TAG = "HoleListAdapter";

    private AppContext mContext;
    private IPkuHoleMod mPkuHoleMod;
    private ImageManager mImageManager;
    public static ArrayList<HoleListItemEntity> allItems;//这里这种做法有问题！不应该设置static

    public HoleListAdapter(Context context, ArrayList<HoleListItemEntity> items){
        mContext = (AppContext) context.getApplicationContext();
        mPkuHoleMod = new PkuHoleMod(mContext);
        mImageManager = new ImageManager(mContext);
        allItems = new ArrayList<>();
        allItems.addAll(items);
    }

    public void addItems(ArrayList<HoleListItemEntity> items){
        allItems.addAll(items);
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
        ViewHolder holder;

        HoleListItemEntity item = allItems.get(position);
        if (convertView == null) {
            holder = new ViewHolder(position);

            convertView = LayoutInflater.from(mContext).inflate(R.layout.mhole_list_item, parent, false);
            holder.findWidgets(convertView);

            //set tag
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        switch (item.getType()) {
            case IPkuHoleMod.TYPE_IMAGE:
                holder.setImage(item);
                break;
            case IPkuHoleMod.TYPE_AUDIO:
                holder.setAudio(item);
                break;
            case IPkuHoleMod.TYPE_TEXT:
                holder.setText(item);
                break;
            default:
                break;
        }

        return convertView;
    }

    public final class ViewHolder{
        CardView cardView;
        TextView tvPid;
        TextView tvTextContent;
        TextView tvTime;
        TextView tvLikeCount;
        TextView tvCommentCount;
        ImageView imgImageContent;
        Button button;
        int position;

        public ViewHolder(int position){
            this.position = position;
        }

        public void findWidgets(View view) {
            cardView = (CardView) view.findViewById(R.id.card_hole_item);
            tvPid = (TextView) view.findViewById(R.id.tv_pid);
            tvTextContent = (TextView) view.findViewById(R.id.tv_text_content);
            imgImageContent = (ImageView) view.findViewById(R.id.img_image_content);
            button = (Button) view.findViewById(R.id.hole_content_button);
            tvTime = (TextView) view.findViewById(R.id.tv_time);
            tvLikeCount = (TextView) view.findViewById(R.id.tv_like_count);
            tvCommentCount = (TextView) view.findViewById(R.id.tv_comment_count);
        }

        public void setImage(HoleListItemEntity item) {
            imgImageContent.setVisibility(View.VISIBLE);
            button.setVisibility(View.GONE);
            setOther(item);

            String url = mPkuHoleMod.getResourceUrl(IPkuHoleMod.TYPE_IMAGE, item.getUrl());
            mImageManager.displayBigImage(url, imgImageContent);
        }
        public void setAudio(HoleListItemEntity item){
            imgImageContent.setVisibility(View.VISIBLE);
            button.setVisibility(View.VISIBLE);
            setOther(item);
            //TO-DO add audio
        }

        public void setText(HoleListItemEntity item){
            //SET image, button as gone
            imgImageContent.setVisibility(View.GONE);
            button.setVisibility(View.GONE);
            setOther(item);
        }

        public void updateAudio(){

        }

        public void setOther(final HoleListItemEntity item){
            if (item.getText().equals("")) {
                tvTextContent.setVisibility(View.GONE);
            } else {
                tvTextContent.setVisibility(View.VISIBLE);
                tvTextContent.setText(item.getText());
            }
            tvPid.setText("#" + item.getPid());
            tvLikeCount.setText("" + item.getLikenum());
            tvCommentCount.setText("" + item.getReply());
            tvTime.setText(MyCalendar.format(item.getTimestamp()*1000));
            Log.v(TAG, "Timestamp: " + item.getTimestamp());

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView tv = (TextView) v.findViewById(R.id.tv_pid);
                    int pid = Integer.parseInt(tv.getText().toString().substring(1));
                    Intent intent;
                    intent = new Intent(mContext, HoleCommentActivity.class);

                    intent.putExtra("pid", pid);

                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            });
        }
    }
}
