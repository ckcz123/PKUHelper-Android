package com.pkuhelper.M_PKUhole;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pkuhelper.R;
import com.pkuhelper.lib.MyCalendar;
import com.pkuhelper.lib.ViewSetting;
import com.pkuhelper.model.HoleListItemMod;
import com.pkuhelper.pkuhole.HoleInfo;

import java.net.URI;
import java.util.ArrayList;

/**
 * Created by zyxu on 16/1/12.
 */
public class MHoleView implements MHoleView_I {

    private View view;
    private Activity activity;
    private Context context;

    private ListView listView;

    public MHoleView(Activity activity, View view, Context context) {

        this.activity = activity;
        this.view = view;
        this.context = context;
    }

    @Override
    public void firstLoad(final ArrayList<HoleListItemMod> list){
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return (list.size());
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

                HoleListItemMod item = list.get(position);
                if (convertView == null){
                    holder = new ViewHolder();

                    convertView = activity.getLayoutInflater().inflate(R.layout.mhole_list_item, parent, false);
                    holder.findWidgets(convertView);

                    //set tag
                    convertView.setTag(holder);
                }
                else{
                    holder = (ViewHolder) convertView.getTag();
                }

                switch (item.getType()) {
                    case "image":
                        holder.setImage(item);
                        break;
                    case "audio":
                        holder.setAudio(item);
                        break;
                    default:
                        break;
                }
                holder.setOther(item);

                return convertView;
            }
        });
    };

    @Override
    public void moreLoad(final ArrayList<HoleListItemMod> list){

    }

    @Override
    public void refreshLoad(final ArrayList<HoleListItemMod> list) {

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
            listView = (ListView) view.findViewById(R.id.MHole_listview);

            //SET image, button as gone
            contentImageView.setVisibility(View.GONE);
            button.setVisibility(View.GONE);
        }

        public void setImage(HoleListItemMod item) {
            contentImageView.setVisibility(View.VISIBLE);
            contentImageView.setImageBitmap(item.getBitmap(context));
        }
        public void setAudio(HoleListItemMod item){
            contentImageView.setVisibility(View.VISIBLE);
            button.setVisibility(View.VISIBLE);

            //TO-DO add audio
        }

        public void updateAudio(){

        }

        public void setOther(HoleListItemMod item){
            if (item.getText().equals(""))
                contentTextView.setVisibility(View.GONE);
            pidTextView.setText("#"+item.getPid());
            likeNumTextView.setText(""+item.getLikenum());
            cmtNumTextView.setText(""+item.getReply());
            timeTextView.setText(MyCalendar.format(item.getTimestamp()));
        }
    }
}
