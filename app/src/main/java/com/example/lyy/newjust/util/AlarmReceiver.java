package com.example.lyy.newjust.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;

import com.example.lyy.newjust.R;
import com.example.lyy.newjust.activity.School.ToDoActivity;
import com.example.lyy.newjust.service.AlarmService;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by lyy on 2017/10/26.
 */

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        //当系统到我们设定的时间点的时候会发送广播，执行这里

        sendNotification(context, intent);

        //再次开启LongRunningService这个服务，从而可以
        Intent i = new Intent(context, AlarmService.class);
        context.startService(i);
    }

    //发送通知
    private void sendNotification(Context context, Intent intent) {
        intent = new Intent(context, ToDoActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle("果核")
                .setContentText("快去查看一下要做的事情吧")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_guohe)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_guohe))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS | NotificationCompat.DEFAULT_SOUND)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)  //通知的类型
                .setFullScreenIntent(pi, true)  //不设置此项不会悬挂,false 不会出现悬挂
                .build();
        manager.notify(1, notification);
    }
}
