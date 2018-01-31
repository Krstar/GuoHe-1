package com.lyy.guohe.widget.todoWidget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.lyy.guohe.R;
import com.lyy.guohe.activity.School.ToDoActivity;
import com.lyy.guohe.db.ToDo;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by LIUYONGKUI726 on 2017-07-10.
 */

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class ToDoListWidget extends AppWidgetProvider {

    private static final String TAG = "ToDoListWidget";

    String clickAction = "com.tamic.WidgetProvider.onclick";
    int i = 0;

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        // 获取Widget的组件名
        ComponentName thisWidget = new ComponentName(context,
                ToDoListWidget.class);

        // 创建一个RemoteView
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.widget_todo);

        // 把这个Widget绑定到RemoteViewsService
        Intent intent = new Intent(context, ToDoListViewService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[0]);

        // 设置适配器
        remoteViews.setRemoteAdapter(R.id.widget_list, intent);

        // 设置当显示的widget_list为空显示的View
        remoteViews.setEmptyView(R.id.widget_list, R.layout.none_data);

        // 点击列表触发事件
        Intent clickIntent = new Intent(context, ToDoListWidget.class);
        // 设置Action，方便在onReceive中区别点击事件
        clickIntent.setAction(clickAction);
        clickIntent.setData(Uri.parse(clickIntent.toUri(Intent.URI_INTENT_SCHEME)));

        //点击头部跳转到页面内
        Intent skipIntent = new Intent(context, ToDoActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 200, skipIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.tv_widget, pi);

        PendingIntent pendingIntentTemplate = PendingIntent.getBroadcast(
                context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setPendingIntentTemplate(R.id.widget_list,
                pendingIntentTemplate);

        // 刷新按钮
        final Intent refreshIntent = new Intent(context,
                ToDoListWidget.class);
        refreshIntent.setAction("refresh");
        final PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(
                context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.button_refresh,
                refreshPendingIntent);

        // 更新Wdiget
        appWidgetManager.updateAppWidget(thisWidget, remoteViews);

    }

    /**
     * 接收Intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        Log.d(TAG, "onReceive: " + i);

        String action = intent.getAction();

        if (action.equals("refresh")) {
            // 刷新Widget
            final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            final ComponentName cn = new ComponentName(context, ToDoListWidget.class);

            List<ToDo> toDos = DataSupport.findAll(ToDo.class);

            ToDoListViewFactory.mList.clear();
            for (int i = 0; i < toDos.size(); i++) {
                ToDoListViewFactory.mList.add(toDos.get(i).getContent());
            }
            // 这句话会调用RemoteViewSerivce中RemoteViewsFactory的onDataSetChanged()方法。
            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn),
                    R.id.widget_list);
        } else if (action.equals(clickAction)) {
            // 单击Wdiget中ListView的某一项会显示一个Toast提示。
            Toast.makeText(context, intent.getStringExtra("content"),
                    Toast.LENGTH_SHORT).show();
        }
        i++;
        Log.d(TAG, "onReceive: " + i);
    }

}