package com.pkuhelper.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class WidgetCourse2Service extends RemoteViewsService {

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new WidgetCourse2Factory(this.getApplicationContext(), intent);
	}

}

