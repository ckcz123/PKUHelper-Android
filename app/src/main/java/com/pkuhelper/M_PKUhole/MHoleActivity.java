package com.pkuhelper.M_PKUhole;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.widget.CardView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pkuhelper.R;

public class MHoleActivity extends Activity {

    private MHolePresenter mHolePresenter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mhole);

        mHolePresenter = new MHolePresenter();
    }

}
