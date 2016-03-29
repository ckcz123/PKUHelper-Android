package com.pkuhelper.ui.ipgw.impl;

import android.app.AlertDialog;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.pkuhelper.R;
import com.pkuhelper.entity.AQIEntity;
import com.pkuhelper.presenter.IIPGWPresenter;
import com.pkuhelper.presenter.impl.IPGWPresenter;
import com.pkuhelper.ui.DrawView;
import com.pkuhelper.ui.ipgw.IIPGWUI;

import java.util.Map;

/**
 * Created by zyxu on 3/1/16.
 */
public class IPGWFragment extends Fragment implements IIPGWUI {
    private static final String TAG = "IPGWFragment";
    Button btnFree;
    Button btnPaid;
    Button btnDisconnect_dev;
    ImageButton btnDisconnectAll;
    ImageButton btnDisconnect;
    ImageButton btnChangeFree;
    TextView tvDebug;

    DrawView drawView;
    ImageButton btnPhone;
    ImageButton btnEarth;
    Resources r;

    int earthLocation[] = new int[2];
    int earthHeight;
    int earthWidth;

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
        btnDisconnect = (ImageButton) view.findViewById(R.id.btn_disconnect);
        btnChangeFree = (ImageButton) view.findViewById(R.id.btn_change_free);

        setupDrawView(drawView);


        btnPhone.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("drawing", "On Phone");
                updateDrawViewOffset(drawView);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d("drawing", "On Phone action down");

                        //如果是锁住的 显示账户信息
                        if (drawView.isLocked()){

                            Map<String,String> data = mIPGWPresenter.getIPGWEntity();

                            Log.d("IPGW:","show status");
                            if (data!=null) {
                                String msg;
                                msg = "IP:" + data.get("IP") + "\n";
                                msg += "范围:" + (data.get("SCOPE").equals("international") ? "收费地址" : "免费地址") + "\n";
                                msg += "时长:" + (Double.parseDouble(data.get("FR_TIME"))) + "/"
                                        + (Double.parseDouble(data.get("FR_DESC_EN").trim().split("[^\\d]")[0]))
                                        + "小时\n";
                                msg += "余额:" + data.get("BALANCE");

                                AlertDialog dialog = new AlertDialog.Builder(getContext())
                                        .setTitle("网关账户")
                                        .setMessage(msg)
                                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .create();
                                dialog.show();
                            }
                        }
                        else {
                            drawView.setCanDraw(true);
                            drawView.onTouchEvent(event);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.d("drawing", "On Phone action move");
                        drawView.onTouchEvent(event);
                        break;
                    case MotionEvent.ACTION_UP:
                        drawView.setCanDraw(false);

                        float x = event.getX();
                        float y = event.getY();
                        float offsetX = btnPhone.getX();
                        float offsetY = btnPhone.getY();

                        x+=offsetX;
                        y+=offsetY;
                        //btnEarth.getLocationInWindow(earthLocation);

                        earthLocation[0] = (int) btnEarth.getX();
                        earthLocation[1] = (int) btnEarth.getY();

                        earthHeight = btnEarth.getHeight();
                        earthWidth = btnEarth.getWidth();

                        Log.d("earth location", earthLocation[0] + " " + earthLocation[1] + " " + earthHeight + " " + earthWidth);
                        if (earthLocation[0] <= x
                                && x < earthLocation[0] + earthHeight
                                && earthLocation[1] <= y
                                && y < earthLocation[1] + earthWidth)
                            mIPGWPresenter.doConnect();
                        else if (!drawView.isLocked())
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

        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIPGWPresenter.doDisconnect();
            }
        });

        btnChangeFree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIPGWPresenter.changeFreeStatus();
            }
        });
    }


    @Override
    public void popSnack(String str) {
        Snackbar.make(btnPhone, str, Snackbar.LENGTH_LONG).show();
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
                layers[0] = r.getDrawable(R.drawable.earth_healthy);
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

    @Override
    public boolean isLocked(){
        Log.d("drawer state", "" + drawView.isLocked());
        return drawView.isLocked();
    }

    @Override
    public void changeFreeUI(boolean isFree){
        if (!isFree){
            btnChangeFree.setImageResource(R.drawable.ipgw_button_1_colored);
        }
        else{
            btnChangeFree.setImageResource(R.drawable.button_ipgw_change);
        }
    }

    public void setupDrawView(DrawView drawView) {

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        drawView.setUpBitmap(width, height);
    }

    public  void updateDrawViewOffset(DrawView drawview){
        int phoneX = (int) btnPhone.getX();
        int phoneY = (int) btnPhone.getY();
        drawView.updateOffset(phoneX, phoneY);
    }
}
