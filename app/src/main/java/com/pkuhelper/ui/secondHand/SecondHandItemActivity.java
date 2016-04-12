package com.pkuhelper.ui.secondHand;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pkuhelper.R;
import com.pkuhelper.chat.ChatActivity;
import com.pkuhelper.entity.SecondHandItemEntity;
import com.pkuhelper.presenter.ISecondHandPresenter;
import com.pkuhelper.presenter.impl.SecondHandPresenter;
import com.pkuhelper.ui.BaseActivity;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zyxu on 4/12/16.
 */
public class SecondHandItemActivity extends BaseActivity implements ISecondHandItemUI {

    private static final String TAG = "SecondHandItemActivity";
    private ViewPager viewPager;
    private ContentLoadingProgressBar pbInit;
    private Toolbar toolbar;
    private String itemID;
    private FloatingActionButton fab;
    private ISecondHandPresenter mPresent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondhand_item);
        pbInit = (ContentLoadingProgressBar) findViewById(R.id.pb_init);

        setupToolbar();
        setupFab();

        mPresent = new SecondHandPresenter(this);
        itemID = getIntent().getStringExtra("itemID");
        Log.d(TAG, "itemID:"+itemID);
        if (itemID!=null && !itemID.isEmpty()) {
            mPresent.getItem(this, itemID);
        }

    }

    @Override
    public void hideProgressBar() {
        pbInit.hide();
    }

    @Override
    public void showProgressBar() {
        Log.d(TAG,"show progress bar");
        pbInit.setVisibility(View.VISIBLE);
    }

    @Override
    public void setupContent(SecondHandItemEntity<SecondHandItemEntity.ItemImage> mod) {
        TextView tvTitle = (TextView) findViewById(R.id.tv_title);
        TextView tvPrice = (TextView) findViewById(R.id.tv_price);
        TextView tvDaoable = (TextView) findViewById(R.id.tv_daoable);
        TextView tvDescription = (TextView) findViewById(R.id.tv_description);
        ViewPager vpImages = (ViewPager) findViewById(R.id.vp_secondhand_item_images);

        final List<ImageView> imageViews = new ArrayList<>();

        try {
            tvTitle.setText(mod.getName());
            tvPrice.setText("¥ "+mod.getPrice());
            tvDaoable.setText(mod.isDaoable()?"是":"否");
            tvDescription.setText(mod.getDescription());
        }catch (Exception e){
            e.printStackTrace();
        }

        final List<SecondHandItemEntity.ItemImage> images = mod.getImages();

        for (int i=0;i<images.size();i++){
            imageViews.add(new ImageView(this));
        }

        vpImages.setAdapter(new PagerAdapter() {

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(imageViews.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ImageView imageView = imageViews.get(position);
                SecondHandItemEntity.ItemImage image = images.get(position);
                showProgressBar();
                Picasso.with(getParent()).load(image.getUrl()).into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        hideProgressBar();
                    }

                    @Override
                    public void onError() {
                        showMessage("图片加载错误");
                    }
                });
                container.addView(imageView);
                return imageView;
            }

            @Override
            public int getCount() {
                return imageViews.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }
        });

    }

    @Override
    public void showMessage(String msg) {
        Snackbar.make(toolbar,msg,Snackbar.LENGTH_LONG);
    }


    private void setupToolbar(){
        setTitle("二手详情");
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
        fab = (FloatingActionButton) findViewById(R.id.fab_secondhand_contact);
        final ISecondHandItemUI ui = this;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresent.createSessionByItemID(ui,itemID);
            }
        });
    }

    public void startMessageSession(String chatTo){
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("uid",chatTo);
        startActivity(intent);
    }
}
