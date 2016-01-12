package com.pkuhelper.Mpkuhole;

import android.os.Bundle;
import android.app.Activity;

import com.pkuhelper.R;

public class MHoleActivity extends Activity {

    private HolePresenter holePresenter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mhole);

        holePresenter = new HolePresenter(this);

        holePresenter.firstLoad();
    }

}
