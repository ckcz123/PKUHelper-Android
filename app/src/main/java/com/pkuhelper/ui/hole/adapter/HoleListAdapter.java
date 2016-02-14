package com.pkuhelper.ui.hole.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pkuhelper.R;
import com.pkuhelper.entity.HoleListItemEntity;
import com.pkuhelper.manager.CalendarManager;
import com.pkuhelper.manager.ImageManager;
import com.pkuhelper.model.IPkuHoleMod;
import com.pkuhelper.model.impl.PkuHoleMod;
import com.pkuhelper.ui.BaseListAdapter;
import com.pkuhelper.ui.hole.impl.HoleCommentActivity;

import java.util.ArrayList;

/**
 * Created by zyxu on 16/1/13.
 */
public class HoleListAdapter extends BaseListAdapter<HoleListItemEntity> {
    private static final String TAG = "HoleListAdapter";

    private IPkuHoleMod mPkuHoleMod;
    private ImageManager mImageManager;

    public HoleListAdapter(Context context, ArrayList<HoleListItemEntity> items){
        super(context, items);
        mPkuHoleMod = new PkuHoleMod(mContext);
        mImageManager = new ImageManager(mContext);
    }

    public void addItems(ArrayList<HoleListItemEntity> items){
        allItems.addAll(items);
    }

    public void addItemsAtStart(ArrayList<HoleListItemEntity> items) {
        allItems.addAll(0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        HoleListItemEntity item = allItems.get(position);
        if (convertView == null) {
            holder = new ViewHolder(position);

            convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_hole, parent, false);
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
            tvTime.setText(CalendarManager.getDeltaTime(item.getTimestamp() * 1000));

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView tv = (TextView) v.findViewById(R.id.tv_pid);
                    Intent intent;
                    intent = new Intent(mContext, HoleCommentActivity.class);
                    String json = mPkuHoleMod.getJson(item);
                    intent.putExtra("json", json);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            });
        }
    }
}
