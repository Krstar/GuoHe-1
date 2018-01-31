package com.lyy.guohe.widget.courseListWidget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class CourseListViewService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new CourseListViewFactory(this.getApplicationContext(), intent);
    }
}
