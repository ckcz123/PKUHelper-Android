package com.pkuhelper.M_PKUhole;

import com.pkuhelper.model.HoleListItemMod;

import java.util.ArrayList;

/**
 * Created by zyxu on 16/1/11.
 */

public interface MHoleView_I {
    void firstLoad(final ArrayList<HoleListItemMod> list);
    void moreLoad(final ArrayList<HoleListItemMod> list);
    void refreshLoad(final ArrayList<HoleListItemMod> list);
}
