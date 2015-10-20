package com.pkuhelper.lib.view.colorpicker;

import android.content.DialogInterface;

public interface ColorPickerClickListener {
	void onClick(DialogInterface d, int lastSelectedColor, Integer[] allColors);
}
