package com.pkuhelper.ui.ipgw.impl;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.telecom.Call;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pkuhelper.R;
import com.pkuhelper.entity.AQIEntity;
import com.pkuhelper.model.Callback;
import com.pkuhelper.model.IIPGWMod;
import com.pkuhelper.model.impl.IPGWMod;
import com.pkuhelper.presenter.IIPGWPresenter;
import com.pkuhelper.presenter.impl.IPGWPresenter;
import com.pkuhelper.ui.DrawView;
import com.pkuhelper.ui.ipgw.IIPGWUI;

/**
 * Created by zyxu on 3/1/16.
 */
public class IPGWFragment extends Fragment implements IIPGWUI {

    Button btnFree;
    Button btnPaid;
    Button btnDisconnect;
    ImageButton btnDisconnectAll;
    TextView tvDebug;

    DrawView drawView;
    ImageButton btnPhone;
    ImageButton btnEarth;
    Resources r;

    int earthLocation[] = new int[2];
    int earthHeight;
    int earthWidth;

    private Bitmap mBitmap = null;
    private Canvas mBitmapCanvas = null;

    IIPGWPresenter mIPGWPresenter;

    @Override
    public void onStart() {
        super.onStart();
        r = getResources();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        Log.d("fragment", "oncreateView");
        final View view = inflater.inflate(R.layout.fragment_ipgw, container, false);

        mIPGWPresenter = new IPGWPresenter(getContext(), this);

        findWidgets(view);

        mIPGWPresenter.updateAQI();

        return view;
    }

    private void findWidgets(View view) {

        //BEGIN-DEV
//        btnFree = (Button) view.findViewById(R.id.btn_connect_free_dev);
//        btnPaid = (Button) view.findViewById(R.id.btn_connect_paid_dev);
//        btnDisconnect = (Button) view.findViewById(R.id.btn_disconnect_dev);
//        btnDisconnectAll = (Button) view.findViewById(R.id.btn_disconnect_all_dev);
//        tvDebug = (TextView) view.findViewById(R.id.tv_ipgw_debug_dev);
//
//        btnFree.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                connectFree();
//            }
//        });
//        btnPaid.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                connectPaid();
//            }
//        });
//        btnDisconnect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                disconnect();
//            }
//        });
//        btnDisconnectAll.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                disconnectAll();
//            }
//        });
        //END-DEV

        drawView = (DrawView) view.findViewById(R.id.drawview_ipgw);
        btnPhone = (ImageButton) view.findViewById(R.id.btn_ipgw_start);
        btnEarth = (ImageButton) view.findViewById(R.id.btn_ipgw_end);
        btnDisconnectAll = (ImageButton) view.findViewById(R.id.btn_disconnect_all);

        btnPhone.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("drawing", "On Phone");
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d("drawing", "On Phone action down");
                        drawView.setCanDraw(true);
                        drawView.onTouchEvent(event);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.d("drawing", "On Phone action move");
                        drawView.onTouchEvent(event);
                        break;
                    case MotionEvent.ACTION_UP:
                        drawView.setCanDraw(false);

                        float x = event.getX();
                        float y = event.getY();


                        btnEarth.getLocationOnScreen(earthLocation);

                        earthHeight = btnEarth.getHeight();
                        earthWidth = btnEarth.getWidth();

                        Log.d("earth location", earthLocation[0] + " " + earthLocation[1] + " " + earthHeight + " " + earthWidth);
                        if (earthLocation[0] <= x
                                && x < earthLocation[0] + earthHeight
                                && earthLocation[1] <= y
                                && y < earthLocation[1] + earthWidth)
                            mIPGWPresenter.doConnectFree();
                        else if (!drawView.isLockded())
                            clearUpCanvas();
                        break;
                }

                return true;
            }
        });

        btnEarth.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                Log.d("drawing", "On Earth");
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        drawView.setCanDraw(false);
                        Log.d("drawing", "on earth action up");
                        break;
                    case MotionEvent.ACTION_MOVE:
                        drawView.setCanDraw(false);
                        Log.d("drawing", "on earth action move");
                        break;
                }

                return false;
            }
        });

        btnEarth.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d("click", "On Earth");
                int aqi = mIPGWPresenter.getAQI();
                AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setTitle("空气质量")
                        .setMessage("P大附近的空气质量指数(AQI)为" + aqi + "\n数据来自PM25.in")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                dialog.show();

                return true;
            }
        });

        btnDisconnectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIPGWPresenter.doDisconnectAll();
            }
        });
    }


    private void connectFree() {
        mIPGWPresenter.doConnectFree();

    }

    private void connectPaid() {
        mIPGWPresenter.doConnectPaid();
    }

    private void disconnect() {
        mIPGWPresenter.doDisconnect();
    }

    private void disconnectAll() {
        mIPGWPresenter.doDisconnectAll();
    }

    @Override
    public void popSnack(String str) {
        Snackbar.make(btnPhone, str, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void updateEarthUI(int stage) {

        Drawable[] layers = new Drawable[2];

        switch (stage) {
            case AQIEntity.AQI_0_100:
                layers[0] = r.getDrawable(R.drawable.earth_healthy);
                layers[1] = r.getDrawable(R.drawable.aqi_100);
                break;
            case AQIEntity.AQI_100_200:
                layers[0] = r.getDrawable(R.drawable.earth_healthy);
                layers[1] = r.getDrawable(R.drawable.aqi_200);
                break;
            case AQIEntity.AQI_200_300:
                layers[0] = r.getDrawable(R.drawable.earth_polluted);
                layers[1] = r.getDrawable(R.drawable.aqi_300);
                break;
            case AQIEntity.AQI_300_400:
                layers[0] = r.getDrawable(R.drawable.earth_polluted);
                layers[1] = r.getDrawable(R.drawable.aqi_400);
                break;
            case AQIEntity.AQI_400_500:
                layers[0] = r.getDrawable(R.drawable.earth_polluted);
                layers[1] = r.getDrawable(R.drawable.aqi_500);
                break;
            case AQIEntity.AQI_500_INF:
                layers[0] = r.getDrawable(R.drawable.earth_dead);
                layers[1] = r.getDrawable(R.drawable.earth_dead);
                break;
        }


        LayerDrawable layerDrawable = new LayerDrawable(layers);
        btnEarth.setImageDrawable(layerDrawable);
    }

    @Override
    public void clearUpCanvas() {
        drawView.refreshBitmap();
    }

    @Override
    public void lockCanvas() {
        btnPhone.setImageResource(R.drawable.android_sketch_colored);
        drawView.lock();
        mIPGWPresenter.updateAQI();
    }

    @Override
    public void unlockCanvas() {
        btnPhone.setImageResource(R.drawable.android_sketch);
        drawView.unlock();
    }
}
