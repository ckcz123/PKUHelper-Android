package com.pkuhelper.Mpkuhole;

import android.os.Bundle;
import android.app.Activity;
import android.widget.AbsListView;
import android.widget.ListView;

import com.pkuhelper.R;

public class MHoleActivity extends Activity {

    private HolePresenter holePresenter;
    private ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mhole);

        holePresenter = new HolePresenter(this);

        holePresenter.firstLoad();

        listView = (ListView) findViewById(R.id.MHole_listview);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount != 0) {
                    int lastItem = firstVisibleItem + visibleItemCount;
                    int itemLeft = 0;
                    if (lastItem >= totalItemCount - itemLeft)
                        holePresenter.moreLoad();
                }
            }
        });
    }

}
