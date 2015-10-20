package com.pkuhelper.lib;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.ViewConfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class BaseActivity extends Activity {

    private SystemBarTintManager tintManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getOverflowMenu();
        tintManager=new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
    }

    protected abstract void finishRequest(int type, String string);

    protected void wantToExit() {
        setResult(RESULT_CANCELED);
        finish();
    }

    protected void setStatusBarColor(int color) {
        tintManager.setStatusBarTintColor(color);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        setIconEnable(menu, true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN) {
            wantToExit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void getOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class
                    .getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setIconEnable(Menu menu, boolean enable) {
        try {
            Class<?> clazz = Class.forName("com.android.internal.view.menu.MenuBuilder");
            Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            m.setAccessible(true);
            m.invoke(menu, enable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
