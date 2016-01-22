package com.pkuhelper.ui.hole.impl;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pkuhelper.R;
import com.pkuhelper.entity.HoleCommentListItemEntity;
import com.pkuhelper.entity.HoleListItemEntity;
import com.pkuhelper.lib.MyCalendar;
import com.pkuhelper.manager.CalendarManager;
import com.pkuhelper.manager.ImageManager;
import com.pkuhelper.model.IPkuHoleMod;
import com.pkuhelper.model.impl.PkuHoleMod;
import com.pkuhelper.presenter.HoleCommentPresenter;
import com.pkuhelper.ui.BaseActivity;
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
    private ListView lvComment;
    private CardView card;
    private ContentLoadingProgressBar pbLoading;
    private int pid;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hole_comment);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        pid = intent.getIntExtra("pid",0);

        pbLoading = (ContentLoadingProgressBar) findViewById(R.id.pb_hole_comment_load);

        fab = (FloatingActionButton) findViewById(R.id.fab_hole_comment);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("start-type","comment");
                bundle.putBoolean("isReply", false);
                bundle.putInt("pid",pid);
                HolePostFragment holePostFragment=new HolePostFragment();
                holePostFragment.setArguments(bundle);
                holePostFragment.show(getSupportFragmentManager(), holePostFragment.getTag());
            }
        });

        holeCommentPresenter = new HoleCommentPresenter(this);


        if (pid>0) {

            lvComment = (ListView) findViewById(R.id.lv_hole_comment);
            card = (CardView) findViewById(R.id.cv_hole_comment_card);
            holeCommentPresenter.load(pid);
        }

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
    public void error() {
        pbLoading.setVisibility(View.GONE);
        Snackbar.make(lvComment,"评论加载失败",Snackbar.LENGTH_LONG).setAction("重试", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holeCommentPresenter.load(pid);
            }
        }).show();
        Log.e("ERROR:","树洞评论加载失败");
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

        public void setImage(HoleListItemEntity item) {
            contentImageView.setVisibility(View.VISIBLE);
            button.setVisibility(View.GONE);
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
