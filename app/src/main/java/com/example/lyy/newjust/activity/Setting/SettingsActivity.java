package com.example.lyy.newjust.activity.Setting;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.lyy.newjust.R;
import com.example.lyy.newjust.activity.Memory.MemoryDayActivity;
import com.example.lyy.newjust.model.Res;
import com.example.lyy.newjust.util.AppConstants;
import com.example.lyy.newjust.util.HttpUtil;
import com.example.lyy.newjust.util.ResponseUtil;
import com.example.lyy.newjust.util.SpUtils;
import com.example.lyy.newjust.util.UrlUtil;
import com.example.lyy.newjust.util.VersionUtils;
import com.githang.statusbar.StatusBarCompat;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import util.UpdateAppUtils;

public class SettingsActivity extends SwipeBackActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private Context mContext;

    private NotificationManager notificationManager;

    public static SwipeBackActivity SettingActivity;

    private ProgressDialog mProgressDialog;

    private TextView mTvCheckInfo;

    private String serverVersion = "";
    private String updateInfo = "";
    private String updateUrl = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SettingActivity = this;
        mContext = SettingsActivity.this;

//        //去掉Activity上面的状态栏
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        StatusBarCompat.setStatusBarColor(this, Color.rgb(255, 255, 255));
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

        /*
      默认当前版本0.0.1
     */
        mTvCheckInfo = (TextView) findViewById(R.id.tv_checkInfo);
        String app_version = VersionUtils.getVerName(mContext);
        if (app_version != null) {
            mTvCheckInfo.setText("当前版本:" + app_version);
        }

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage("正在检查询版本信息，请稍后...");
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(true);

        SwitchCompat switch_notification = (SwitchCompat) findViewById(R.id.switch_notification);
        switch_notification.setOnCheckedChangeListener(this);

        LinearLayout ll_profile = (LinearLayout) findViewById(R.id.ll_profile);
        LinearLayout ll_feedback = (LinearLayout) findViewById(R.id.ll_feedback);
        LinearLayout ll_about_us = (LinearLayout) findViewById(R.id.ll_about_us);
        LinearLayout ll_share = (LinearLayout) findViewById(R.id.ll_share);
        LinearLayout ll_show_grade = (LinearLayout) findViewById(R.id.ll_show_grade);
        LinearLayout ll_upgrade = (LinearLayout) findViewById(R.id.ll_upgrade);

        ll_profile.setOnClickListener(this);
        ll_feedback.setOnClickListener(this);
        ll_about_us.setOnClickListener(this);
        ll_share.setOnClickListener(this);
        ll_show_grade.setOnClickListener(this);
        ll_upgrade.setOnClickListener(this);

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
        Notification notification = new NotificationCompat.Builder(mContext)
                .setContentTitle(today)
                .setContentText("记住今天重要的事情")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_guohe)
                .setContentIntent(contextIntent)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_guohe))
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
                Intent profile_Intent = new Intent(mContext, ProfileActivity.class);
                startActivity(profile_Intent);
                break;
            case R.id.ll_feedback:
                Intent feedbackIntent = new Intent(mContext, FeedBackActivity.class);
                startActivity(feedbackIntent);
                break;
            case R.id.ll_about_us:
                Intent aboutIntent = new Intent(mContext, AboutUSActivity.class);
                startActivity(aboutIntent);
                break;
            case R.id.ll_share:
                shareApp();
                break;
            case R.id.ll_show_grade:
                showSingleChoiceDialog();
                break;
            case R.id.ll_upgrade:
                checkUpdateInfo();
                break;
        }
    }

    int yourChoice;

    private void showSingleChoiceDialog() {
        final String[] items = {"按学年查询", "按学科类型查询"};
        yourChoice = 0;
        AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(mContext);
        singleChoiceDialog.setTitle("请选择成绩查看方式");
        // 第二个参数是默认选项，此处设置为0
        singleChoiceDialog.setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        yourChoice = which;
                    }
                });
        singleChoiceDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (yourChoice) {
                            case 0:
                                SpUtils.putString(getApplicationContext(), AppConstants.SHOW_GRADE, "学年");
                                break;
                            case 1:
                                SpUtils.putString(getApplicationContext(), AppConstants.SHOW_GRADE, "学科");
                                break;
                            default:
                                break;
                        }
                    }
                });
        singleChoiceDialog.show();
    }

    //检查是否可以被更新
    private void checkUpdateInfo() {
        mProgressDialog.show();
        String apkPath = UrlUtil.APK_PATH;
        HttpUtil.sendHttpRequest(apkPath, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Looper.prepare();
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
                Toast.makeText(mContext, "服务器异常，请稍后", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    Res res = ResponseUtil.handleResponse(data);
                    assert res != null;
                    if (res.getCode() == 200) {
                        try {
                            JSONObject object = new JSONObject(res.getInfo());
                            serverVersion = object.getString("serverVersion");
                            updateInfo = object.getString("updateinfo");
                            updateUrl = object.getString("updateurl");

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String local_version = VersionUtils.getVerName(mContext);
                                    if (!local_version.equals(serverVersion)) {
                                        updateApp();
                                    } else {
                                        if (mProgressDialog.isShowing())
                                            mProgressDialog.dismiss();
                                        Toast.makeText(mContext, "当前已是最新版本", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Looper.prepare();
                        if (mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        Toast.makeText(mContext, "错误" + res.getMsg(), Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                } else {
                    Looper.prepare();
                    if (mProgressDialog.isShowing())
                        mProgressDialog.dismiss();
                    Toast.makeText(mContext, "错误" + response.code(), Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        });
    }

    private void updateApp() {
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();
        UpdateAppUtils.from(SettingsActivity.this)
                .checkBy(UpdateAppUtils.CHECK_BY_VERSION_NAME) //更新检测方式，默认为VersionCode
//                .serverVersionCode(2)  //服务器versionCode
                .serverVersionName(serverVersion + "\n") //服务器versionName
                .updateInfo(updateInfo)
                .apkPath(updateUrl) //最新apk下载地址
                .update();

    }

    private void shareApp() {
        Intent intent1 = new Intent(Intent.ACTION_SEND);
        intent1.putExtra(Intent.EXTRA_TEXT, "我发现了一个不错的应用哦：" + UrlUtil.APP);
        intent1.setType("text/plain");
        startActivity(Intent.createChooser(intent1, "果核"));
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

    @Override
    protected void onResume() {
        super.onResume();

        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();

        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
