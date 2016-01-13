package com.pkuhelper.ui;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pkuhelper.R;
import com.pkuhelper.entity.HoleListItemEntity;
import com.pkuhelper.lib.MyCalendar;

import java.util.ArrayList;

/**
 * Created by zyxu on 16/1/13.
 */
public class HoleListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<HoleListItemEntity> allItems;

    public HoleListAdapter(Context context, ArrayList<HoleListItemEntity> items){
        this.context = context;
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
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            HoleListItemEntity item = allItems.get(position);
            if (convertView == null) {
                holder = new ViewHolder();

                convertView = LayoutInflater.from(context).inflate(R.layout.mhole_list_item, parent, false);
                holder.findWidgets(convertView);

                //set tag
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            switch (item.getType()) {
                case "image":
                    holder.setImage(item);
                    break;
                case "audio":
                    holder.setAudio(item);
                    break;
                case "text":
                    holder.setText(item);
                    break;
                default:
                    break;
            }

            return convertView;
        }

    public final class ViewHolder{
        public CardView cardView;
        public TextView pidTextView, contentTextView, timeTextView, likeNumTextView, cmtNumTextView;
        public ImageView contentImageView;
        public Button button;

        public void findWidgets(View view) {
            cardView = (CardView) view.findViewById(R.id.hole_card_view);
            pidTextView = (TextView) view.findViewById(R.id.hole_pid);
            contentTextView = (TextView) view.findViewById(R.id.hole_content_text);
            contentImageView = (ImageView) view.findViewById(R.id.hole_content_image);
            button = (Button) view.findViewById(R.id.hole_content_button);
            timeTextView = (TextView) view.findViewById(R.id.hole_time);
            likeNumTextView = (TextView) view.findViewById(R.id.hole_star);
            cmtNumTextView = (TextView) view.findViewById(R.id.hole_comment);
        }

        public void setImage(HoleListItemEntity item) {
            contentImageView.setVisibility(View.VISIBLE);
            button.setVisibility(View.GONE);
            setOther(item);

//            String url = item.getUrl();
//            Bitmap bitmap=null;
//            try {
//                String hash = Util.getHash(url);
//                bitmap = MyBitmapFactory.getCompressedBitmap(MyFile.getCache(context, hash).getAbsolutePath(), 2);
//                contentImageView.setImageBitmap(bitmap);
//            } catch (Exception e) {
//            }

        }
        public void setAudio(HoleListItemEntity item){
            contentImageView.setVisibility(View.VISIBLE);
            button.setVisibility(View.VISIBLE);
            setOther(item);
            //TO-DO add audio
        }

        public void setText(HoleListItemEntity item){
            //SET image, button as gone
            contentImageView.setVisibility(View.GONE);
            button.setVisibility(View.GONE);
            setOther(item);
        }

        public void updateAudio(){

        }

        public void setOther(HoleListItemEntity item){
            if (item.getText().equals(""))
                contentTextView.setVisibility(View.GONE);
            else {
                contentTextView.setVisibility(View.VISIBLE);
                contentTextView.setText(item.getText());
            }
            pidTextView.setText("#" + item.getPid());
            likeNumTextView.setText("" + item.getLikenum());
            cmtNumTextView.setText("" + item.getReply());
            timeTextView.setText(MyCalendar.format(item.getTimestamp()));
            long timestamp = item.getTimestamp();
        }
    }
}
