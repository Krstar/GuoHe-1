package com.example.lyy.newjust.widget.memoryWidget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.lyy.newjust.R;
import com.example.lyy.newjust.activity.Memory.MemoryDayActivity;
import com.example.lyy.newjust.db.DBMemory;

import org.litepal.crud.DataSupport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Implementation of App Widget functionality.
 */

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class MemoryWidget extends AppWidgetProvider {

    private static final String TAG = "MemoryWidget";

    String clickAction = "com.tamic.WidgetProvider.onclick";
    int i = 0;

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        // 获取Widget的组件名
        ComponentName thisWidget = new ComponentName(context,
                MemoryWidget.class);

        // 创建一个RemoteView
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.widget_memory);

        // 把这个Widget绑定到RemoteViewsService
        Intent intent = new Intent(context, MemoryListService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[0]);

        // 设置适配器
        remoteViews.setRemoteAdapter(R.id.widget_list_memory, intent);

        // 设置当显示的widget_list为空显示的View
        remoteViews.setEmptyView(R.id.widget_list_memory, R.layout.none_data);

        // 点击列表触发事件
        Intent clickIntent = new Intent(context, MemoryWidget.class);
        // 设置Action，方便在onReceive中区别点击事件
        clickIntent.setAction(clickAction);
        clickIntent.setData(Uri.parse(clickIntent.toUri(Intent.URI_INTENT_SCHEME)));

        //点击头部跳转到页面内
        Intent skipIntent = new Intent(context, MemoryDayActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 200, skipIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.tv_widget_memory, pi);

        PendingIntent pendingIntentTemplate = PendingIntent.getBroadcast(
                context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setPendingIntentTemplate(R.id.widget_list_memory,
                pendingIntentTemplate);

        // 刷新按钮
        final Intent refreshIntent = new Intent(context,
                MemoryWidget.class);
        refreshIntent.setAction("refresh");
        final PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(
                context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.button_refresh_memory,
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

        int year, month, date;

        Calendar calendar = Calendar.getInstance();

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        date = calendar.get(Calendar.DATE);

        String today = year + "年" + (month + 1) + "月" + date + "日";

        String action = intent.getAction();

        if (action.equals("refresh")) {
            // 刷新Widget
            final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            final ComponentName cn = new ComponentName(context, MemoryWidget.class);

            List<DBMemory> memories = DataSupport.findAll(DBMemory.class);

            MemoryListViewFactory.mList.clear();
            for (int i = 0; i < memories.size(); i++) {
                String daysBetween = daysOfTwo_2(today, memories.get(i).getMemory_day());
                Log.d(TAG, "onReceive: " + today);
                Log.d(TAG, "onReceive: " + memories.get(i).getMemory_day());
                Log.d(TAG, "onReceive: " + daysBetween);
                MemoryListViewFactory.mList.add(memories.get(i).getMemory_content() + daysBetween + "天");
            }
            // 这句话会调用RemoteViewSerivce中RemoteViewsFactory的onDataSetChanged()方法。
            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn),
                    R.id.widget_list_memory);
        } else if (action.equals(clickAction)) {
            // 单击Wdiget中ListView的某一项会显示一个Toast提示。
            Toast.makeText(context, intent.getStringExtra("content"),
                    Toast.LENGTH_SHORT).show();
        }
        i++;
    }


    //判断两个时间段内的天数差
    private String daysOfTwo_2(String day1, String day2) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
            //跨年不会出现问题
            //如果时间为：2016-03-18 11:59:59 和 2016-03-19 00:00:01的话差值为 0
            Date fDate = sdf.parse(day1);
            Date oDate = sdf.parse(day2);
            long days = (oDate.getTime() - fDate.getTime()) / (1000 * 3600 * 24);
            if (days < 0) {
                days = days * (-1);
                return ("已经" + days);
            }
            return ("还有" + days);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}

