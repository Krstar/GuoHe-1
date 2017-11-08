package com.example.lyy.newjust.activity.Setting;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.example.lyy.newjust.R;
import com.example.lyy.newjust.activity.Memory.MemoryDayActivity;
import com.example.lyy.newjust.util.AppConstants;
import com.example.lyy.newjust.util.SpUtils;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class SettingsActivity extends SwipeBackActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //去掉Activity上面的状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_settings);

        setSwipeBackEnable(true);   // 可以调用该方法，设置是否允许滑动退出
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        // 设置滑动方向，可设置EDGE_LEFT, EDGE_RIGHT, EDGE_ALL, EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        // 滑动退出的效果只能从边界滑动才有效果，如果要扩大touch的范围，可以调用这个方法
        mSwipeBackLayout.setEdgeSize(200);
        init();

    }

    private void init() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_back_blue);
        }

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        SwitchCompat switch_notification = (SwitchCompat) findViewById(R.id.switch_notification);
        switch_notification.setOnCheckedChangeListener(this);

        LinearLayout ll_profile = (LinearLayout) findViewById(R.id.ll_profile);
        LinearLayout ll_feedback = (LinearLayout) findViewById(R.id.ll_feedback);
        LinearLayout ll_about_us = (LinearLayout) findViewById(R.id.ll_about_us);
        ll_profile.setOnClickListener(this);
        ll_feedback.setOnClickListener(this);
        ll_about_us.setOnClickListener(this);

        boolean isNotification = SpUtils.getBoolean(this, AppConstants.IS_NOTIFICATION);
        switch_notification.setChecked(isNotification);
        if (isNotification) {
            setNotification();
        } else {
            cancelNotification();
        }

    }

    //添加顶部通知栏
    private void setNotification() {
        Time time = new Time("GMT+8");
        time.setToNow();
        int year = time.year;
        int month = time.month;
        int date = time.monthDay;
        String today = year + "-" + month + "-" + date;
        Intent intent = new Intent(this, MemoryDayActivity.class);
        PendingIntent contextIntent = PendingIntent.getActivity(this, 0,
                intent, 0);
        Notification notification = new NotificationCompat.Builder(SettingsActivity.this)
                .setContentTitle(today)
                .setContentText("记住今天重要的事情")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(contextIntent)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .build();
        notification.flags = Notification.FLAG_ONGOING_EVENT; // 设置常驻 Flag
        notificationManager.notify(2, notification);
    }

    //取消顶部常驻通知栏
    private void cancelNotification() {
        notificationManager.cancel(2);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_profile:
                Intent profile_Intent = new Intent(SettingsActivity.this, ProfileActivity.class);
                startActivity(profile_Intent);
                break;
            case R.id.ll_feedback:
                Intent feedbackIntent = new Intent(SettingsActivity.this, FeedBackActivity.class);
                startActivity(feedbackIntent);
                break;
            case R.id.ll_about_us:
                Intent aboutIntent = new Intent(SettingsActivity.this, AboutUSActivity.class);
                startActivity(aboutIntent);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        if (checked) {
            setNotification();
            SpUtils.putBoolean(this, AppConstants.IS_NOTIFICATION, true);
        } else {
            cancelNotification();
            SpUtils.putBoolean(this, AppConstants.IS_NOTIFICATION, false);

        }
    }
}
