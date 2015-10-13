package com.pkuhelper.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class WidgetCourseService extends RemoteViewsService{

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new WidgetCourseFactory(this.getApplicationContext(), intent);
	}

}

