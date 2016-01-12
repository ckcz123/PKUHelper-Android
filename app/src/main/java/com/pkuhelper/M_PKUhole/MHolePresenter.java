package com.pkuhelper.M_PKUhole;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.pkuhelper.manager.Callback;
import com.pkuhelper.manager.PkuHoleManager;
import com.pkuhelper.model.HoleListItemMod;

import java.util.ArrayList;

/**
 * Created by zyxu on 16/1/11.
 */
public class MHolePresenter {
    private MHoleView mHoleView;
    private PkuHoleManager pkuHoleManager;
    private Activity activity;
    private View view;
    private Context context;
    private int requestPage;
    private ArrayList<HoleListItemMod> mods;

    private Callback callback = new Callback() {
        @Override
        public void onFinished(int code, Object data) {
            if (code == 0) {
                 mods = (ArrayList<HoleListItemMod>) data;
                //TO-DO load data
            } else {
                //TO-DO error solver
            }
        }

        @Override
        public void onError(String msg) {
            //TO-DO error solver
        }
    };

    public MHolePresenter(Activity activity1, View view1) {
        this.activity = activity1;
        this.view = view1;

        mHoleView = new MHoleView(activity, view, context);
    }

    public void firstLoad() {
        requestPage = 1;

        //request from manager
        pkuHoleManager.getHoleList(requestPage, callback);

        //update view
        //mHoleView.

    }

    public void moreLoad() {
        requestPage++;

        pkuHoleManager.getHoleList(requestPage, callback);
    }

    public void refreshLoad() {
        //TO-DO refresh list
    }
}