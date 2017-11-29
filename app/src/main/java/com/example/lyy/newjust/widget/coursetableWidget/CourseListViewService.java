package com.example.lyy.newjust.widget.coursetableWidget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class CourseListViewService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new CourseListViewFactory(this.getApplicationContext(), intent);
    }
}
