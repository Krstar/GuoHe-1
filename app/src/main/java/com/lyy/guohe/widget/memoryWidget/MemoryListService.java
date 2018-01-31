package com.lyy.guohe.widget.memoryWidget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class MemoryListService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new MemoryListViewFactory(this.getApplicationContext(), intent);
    }

}
