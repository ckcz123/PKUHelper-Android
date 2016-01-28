package com.pkuhelper.ui.hole.impl;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pkuhelper.R;
import com.pkuhelper.entity.HoleCommentListItemEntity;
import com.pkuhelper.entity.HoleListItemEntity;
import com.pkuhelper.lib.Constants;
import com.pkuhelper.lib.MyCalendar;
import com.pkuhelper.manager.CalendarManager;
import com.pkuhelper.manager.ImageManager;
import com.pkuhelper.model.IPkuHoleMod;
import com.pkuhelper.model.impl.PkuHoleMod;
import com.pkuhelper.presenter.HoleCommentPresenter;
import com.pkuhelper.subactivity.SubActivity;
import com.pkuhelper.ui.BaseActivity;
import com.pkuhelper.ui.CompatListView;
import com.pkuhelper.ui.hole.HoleCommentListAdapter;
import com.pkuhelper.ui.hole.HoleListAdapter;
import com.pkuhelper.ui.hole.IHoleCommentUI;

import java.util.ArrayList;

/**
 * Created by zyxu on 1/20/16.
 */
public class HoleCommentActivity extends BaseActivity implements IHoleCommentUI {

    private HoleCommentPresenter holeCommentPresenter;
    private HoleCommentListAdapter holeCommentListAdapter;
    private CompatListView lvComment;
    private CardView card;
    private ContentLoadingProgressBar pbLoading;
    private int pid;
    private FloatingActionButton fab;
    private HoleListItemEntity cardEntity;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hole_comment);

        setTitle("树洞评论");

        /*
        * 从intent中获取原文的entity
        * */
        Intent intent = getIntent();
        String json = intent.getStringExtra("json");
        cardEntity = new Gson().fromJson(json, new TypeToken<HoleListItemEntity>() {
        }.getType());
        pid = cardEntity.getPid();

        setupToolbar();
        setupFab();
        pbLoading = (ContentLoadingProgressBar) findViewById(R.id.pb_hole_comment_load);
        lvComment = (CompatListView) findViewById(R.id.lv_hole_comment);
        card = (CardView) findViewById(R.id.cv_hole_comment_card);

        /*
        * bind presenter加载card
        * */
        holeCommentPresenter = new HoleCommentPresenter(this);
        if (pid>0)
            holeCommentPresenter.load(cardEntity);

    }


    @Override
    public void loading() {
        pbLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void loadCard(HoleListItemEntity item) {
        CardFiller cardFiller = new CardFiller(this);

        cardFiller.init(card);

        switch (item.getType()) {
            case IPkuHoleMod.TYPE_IMAGE:
                cardFiller.setImage(item);
                break;
            case IPkuHoleMod.TYPE_AUDIO:
                cardFiller.setAudio(item);
                break;
            case IPkuHoleMod.TYPE_TEXT:
                cardFiller.setText(item);
                break;
            default:
                break;
        }

    }

    @Override
    public void loadList(ArrayList<HoleCommentListItemEntity> data) {
        holeCommentListAdapter = new HoleCommentListAdapter(this,data);
        lvComment.setAdapter(holeCommentListAdapter);

        TextView tvHoleCommentNum = (TextView) findViewById(R.id.tv_hole_comment_num);
        int commentNum = data.size();
        tvHoleCommentNum.setText(""+commentNum+"条评论");

        pbLoading.setVisibility(View.GONE);
    }


    @Override
    public void error(String text) {
        pbLoading.setVisibility(View.GONE);
        Snackbar.make(lvComment,text+"失败",Snackbar.LENGTH_LONG).show();
        Log.e("ERROR:", "树洞评论加载失败");
    }

    private void setupToolbar(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupFab(){
        fab = (FloatingActionButton) findViewById(R.id.fab_hole_comment);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*
                * 打开评论的dialog
                * */
                Bundle bundle = new Bundle();
                bundle.putString("start-type", "comment");
                bundle.putBoolean("isReply", false);
                bundle.putInt("pid", pid);
                HolePostFragment holePostFragment = new HolePostFragment();
                holePostFragment.setArguments(bundle);
                holePostFragment.show(getSupportFragmentManager(), holePostFragment.getTag());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_hole_comment, menu);

        final MenuItem setAttention;
        MenuItem report;

        /**
         * 发送更新关注事件，并更新关注图标
         * */

        setAttention=menu.findItem(R.id.action_hole_set_attention);
        updateAttentionIcon(setAttention);
        setAttention.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                holeCommentPresenter.setAttention(pid);

                updateAttentionIcon(setAttention);
                return false;
            }
        });

        report =menu.findItem(R.id.action_hole_report);

        report.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Bundle bundle = new Bundle();
                bundle.putString("start-type", "report");
                bundle.putInt("pid", pid);
                HolePostFragment holePostFragment = new HolePostFragment();
                holePostFragment.setArguments(bundle);
                holePostFragment.show(getSupportFragmentManager(), holePostFragment.getTag());
                return false;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 动态改变attention icon
     * @param menuItem
     */
    void updateAttentionIcon(MenuItem menuItem){

        if (holeCommentPresenter.isOnAttention(pid)) {
            menuItem.setIcon(R.drawable.ic_star_white_24dp);
        }
        else{
            menuItem.setIcon(R.drawable.ic_star_border_white_24dp);
        }

        invalidateOptionsMenu();
    }

    //这段代码在adapter里有，但如何复用?
    public final class CardFiller{
        public TextView pidTextView, contentTextView, timeTextView;
        public ImageView contentImageView;
        public Button button;
        public IPkuHoleMod mPkuHoleMod;
        public ImageManager mImageManager;

        public CardFiller(Context context){
            mPkuHoleMod = new PkuHoleMod(context);
            mImageManager = new ImageManager(context);
        }
        public void init(View view) {
            pidTextView = (TextView) view.findViewById(R.id.tv_hole_comment_card_pid);
            contentTextView = (TextView) view.findViewById(R.id.tv_hole_comment_card_content);
            contentImageView = (ImageView) view.findViewById(R.id.img_hole_comment_card_content);
            button = (Button) view.findViewById(R.id.btn_hole_comment_card_content);
            timeTextView = (TextView) view.findViewById(R.id.tv_hole_comment_card_time);
        }

        public void setImage(final HoleListItemEntity item) {
            contentImageView.setVisibility(View.VISIBLE);
            button.setVisibility(View.GONE);

            contentImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater inflater = LayoutInflater.from(HoleCommentActivity.this);
                    final View imgEntryView = inflater.inflate(R.layout.dialog_photo_entry, null); // 加载自定义的布局文件

                    final AlertDialog dialog = new AlertDialog.Builder(HoleCommentActivity.this).create();
                    final ImageView imgLargePhoto = (ImageView) imgEntryView.findViewById(R.id.img_large_photo);

                    String url = mPkuHoleMod.getResourceUrl(IPkuHoleMod.TYPE_IMAGE, item.getUrl());
                    mImageManager.displayBigImage(url, imgLargePhoto);


                    dialog.setView(imgEntryView); // 自定义dialog
                    dialog.show();

                    imgEntryView.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View paramView) {
                            dialog.cancel();
                        }
                    });

                    imgEntryView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            Bitmap bitmap = ((BitmapDrawable)imgLargePhoto.getDrawable()).getBitmap();
                            MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "img", "来自树洞");
                            Toast.makeText(HoleCommentActivity.this,"图片保存至系统图库",Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    });
                }
                });

            setOther(item);

            String url = mPkuHoleMod.getResourceUrl(IPkuHoleMod.TYPE_IMAGE, item.getUrl());
            mImageManager.displayBigImage(url, contentImageView);
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
            timeTextView.setText(CalendarManager.getDeltaTime(item.getTimestamp() * 1000));
        }
    }


}
