package com.pkuhelper.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class WidgetExamService extends RemoteViewsService {

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new WidgetExamFactory(this.getApplicationContext(), intent);
	}

}

