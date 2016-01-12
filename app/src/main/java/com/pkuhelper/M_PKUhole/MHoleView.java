package com.pkuhelper.M_PKUhole;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pkuhelper.R;
import com.pkuhelper.lib.MyCalendar;
import com.pkuhelper.entity.HoleListItemEntity;

import java.util.ArrayList;

/**
 * Created by zyxu on 16/1/12.
 */
public class MHoleView implements IMHoleView {

    private Context context;

    private ListView listView;

    public MHoleView(Context context) {

        this.context = context;
    }

    @Override
    public void firstLoad(final ArrayList<HoleListItemEntity> list){
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

                HoleListItemEntity item = list.get(position);
                if (convertView == null){
                    holder = new ViewHolder();

                    convertView = LayoutInflater.from(context).inflate(R.layout.mhole_list_item, parent, false);
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
    public void moreLoad(final ArrayList<HoleListItemEntity> list){

    }

    @Override
    public void refreshLoad(final ArrayList<HoleListItemEntity> list) {

    }

    @Override
    public void error(){
        Toast.makeText(context, "加载失败", Toast.LENGTH_SHORT).show();
        Log.e("ERROR:","树洞加载失败");
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

        public void setImage(HoleListItemEntity item) {
            contentImageView.setVisibility(View.VISIBLE);

            //SHOULD get Bitmap from the entity
            //contentImageView.setImageBitmap(item.getBitmap(context));
        }
        public void setAudio(HoleListItemEntity item){
            contentImageView.setVisibility(View.VISIBLE);
            button.setVisibility(View.VISIBLE);

            //TO-DO add audio
        }

        public void updateAudio(){

        }

        public void setOther(HoleListItemEntity item){
            if (item.getText().equals(""))
                contentTextView.setVisibility(View.GONE);
            pidTextView.setText("#"+item.getPid());
            likeNumTextView.setText(""+item.getLikenum());
            cmtNumTextView.setText(""+item.getReply());
            timeTextView.setText(MyCalendar.format(item.getTimestamp()));
        }
    }
}
