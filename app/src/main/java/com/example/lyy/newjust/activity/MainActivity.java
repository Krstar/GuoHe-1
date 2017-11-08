package com.example.lyy.newjust.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.lyy.newjust.R;
import com.example.lyy.newjust.activity.Memory.MemoryDayActivity;
import com.example.lyy.newjust.activity.One.ConstellationActivity;
import com.example.lyy.newjust.activity.One.HistoryActivity;
import com.example.lyy.newjust.activity.One.OneActivity;
import com.example.lyy.newjust.activity.School.SchoolBusActivity;
import com.example.lyy.newjust.activity.School.SubjectsActivity;
import com.example.lyy.newjust.activity.Setting.FeedBackActivity;
import com.example.lyy.newjust.activity.Tools.TranslateActivity;
import com.example.lyy.newjust.activity.One.WeiBoActivity;
import com.example.lyy.newjust.activity.Setting.ProfileActivity;
import com.example.lyy.newjust.activity.Setting.SettingsActivity;
import com.example.lyy.newjust.activity.Tools.AudioActivity;
import com.example.lyy.newjust.activity.Tools.EMSActivity;
import com.example.lyy.newjust.activity.Tools.EipActivity;
import com.example.lyy.newjust.activity.Tools.OCRActivity;
import com.example.lyy.newjust.gson.Weather;
import com.example.lyy.newjust.service.LongRunningService;
import com.example.lyy.newjust.util.AppConstants;
import com.example.lyy.newjust.util.DonateDialog;
import com.example.lyy.newjust.util.HttpUtil;
import com.example.lyy.newjust.util.SpUtils;
import com.example.lyy.newjust.util.Util;
import com.githang.statusbar.StatusBarCompat;
import com.mxn.soul.flowingdrawer_core.ElasticDrawer;
import com.mxn.soul.flowingdrawer_core.FlowingDrawer;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.TextOutsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.yalantis.taurus.PullToRefreshView;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.PermissionListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private static final String TAG = "MainActivity";

    private static int index = 0;

    //记录用户首次点击返回键的时间
    private long firstTime = 0;

    private String headPicUrl;
    private String imageBase64;
    private String constellation_en;

    private CollapsingToolbarLayout collapsingToolbarLayout;

    private ImageView head_image_view;

    private CircleImageView civ_header;

    private FlowingDrawer mDrawer;

    private PullToRefreshView mPullToRefreshView;

    private NotificationManager notificationManager;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 判断是否是第一次开启应用
        boolean isFirstOpen = SpUtils.getBoolean(this, AppConstants.FIRST_OPEN);
        // 如果是第一次启动，则先进入功能引导页
        if (!isFirstOpen) {
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        StatusBarCompat.setStatusBarColor(this, Color.rgb(0, 172, 193));
        setContentView(R.layout.activity_main);


        Intent service = new Intent(this, LongRunningService.class);
        startService(service);

        StatusBarCompat.setStatusBarColor(this, Color.rgb(0, 127, 193));

        //设置状态栏和toolbar颜色一致
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }

        changeStatusBar();  //将背景图和状态栏融合到一起的方法

        obtain_permission();//获取权限

        init();             //初始化相关控件

        loadHeadPic();      //添加每日一图

        boolean isNotification = SpUtils.getBoolean(this, AppConstants.IS_NOTIFICATION);
        if (isNotification) {
            setNotification();
        } else {
            cancelNotification();
        }
    }

    //将背景图和状态栏融合到一起
    private void changeStatusBar() {
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    //权限获取
    private void obtain_permission() {
        AndPermission.with(this)
                .requestCode(200)
                .permission(
                        Permission.STORAGE,
                        Permission.CAMERA,
                        Permission.MICROPHONE
                )
                .callback(listener)
                .start();
    }

    //权限获取的监听器
    private PermissionListener listener = new PermissionListener() {
        @Override
        public void onSucceed(int requestCode, List<String> grantedPermissions) {
            // 权限申请成功回调。

            // 这里的requestCode就是申请时设置的requestCode。
            // 和onActivityResult()的requestCode一样，用来区分多个不同的请求。
            if (requestCode == 200) {
                // TODO ...
            }
        }

        @Override
        public void onFailed(int requestCode, List<String> deniedPermissions) {
            // 权限申请失败回调。
            if (requestCode == 200) {
                // TODO ...
                Toast.makeText(getApplicationContext(), "您还未获取权限", Toast.LENGTH_SHORT).show();
            }
        }
    };

    //初始化相关控件
    private void init() {

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //设置和toolbar相关的
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_menu);
        }
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle("New JUST");

        //设置顶部照片
        head_image_view = (ImageView) findViewById(R.id.head_image_view);

        //设置有关存储信息的
        headPicUrl = SpUtils.getString(this, AppConstants.HEAD_PIC_URL);
        if (headPicUrl != null) {
            Glide.with(this).load(headPicUrl).crossFade().into(head_image_view);
        } else {
            loadHeadPic();
        }

        head_image_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HeadImageActivity.class);
                intent.putExtra("headPicUrl", headPicUrl);
                startActivity(intent);
            }
        });

        mDrawer = (FlowingDrawer) findViewById(R.id.drawerlayout);
        mDrawer.setTouchMode(ElasticDrawer.TOUCH_MODE_BEZEL);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        requestWeather();

        //设置菜单栏头像
        imageBase64 = SpUtils.getString(this, AppConstants.IMAGE_BASE_64);
        civ_header = (CircleImageView) findViewById(R.id.civ_header);
        civ_header.setOnClickListener(this);
        if (imageBase64 != null) {
            byte[] byte64 = Base64.decode(imageBase64, 0);
            ByteArrayInputStream bais = new ByteArrayInputStream(byte64);
            Bitmap bitmap = BitmapFactory.decodeStream(bais);
            civ_header.setImageBitmap(bitmap);
        }

        ImageView iv_constellation = (ImageView) findViewById(R.id.iv_constellation);
        ImageView iv_health = (ImageView) findViewById(R.id.iv_health);
        ImageView iv_weibo = (ImageView) findViewById(R.id.iv_weibo);
        ImageView iv_schedule = (ImageView) findViewById(R.id.iv_schedule);
        ImageView iv_one = (ImageView) findViewById(R.id.iv_one);
        ImageView iv_memory = (ImageView) findViewById(R.id.iv_memory);
        ImageView iv_history = (ImageView) findViewById(R.id.iv_history);

        Glide.with(this).load(R.drawable.bg_constellation).into(iv_constellation);
        Glide.with(this).load(R.drawable.bg_health).into(iv_health);
        Glide.with(this).load(R.drawable.bg_weibo).into(iv_weibo);
        Glide.with(this).load(R.drawable.bg_every_day).into(iv_one);
        Glide.with(this).load(R.drawable.bg_memory).into(iv_memory);
        Glide.with(this).load(R.drawable.bg_schedule).into(iv_schedule);
        Glide.with(this).load(R.drawable.bg_history).into(iv_history);

        iv_constellation.setOnClickListener(this);
        iv_health.setOnClickListener(this);
        iv_one.setOnClickListener(this);
        iv_weibo.setOnClickListener(this);
        iv_memory.setOnClickListener(this);
        iv_schedule.setOnClickListener(this);
        iv_history.setOnClickListener(this);

        LinearLayout nav_todo = (LinearLayout) findViewById(R.id.nav_todo);
        LinearLayout nav_eat = (LinearLayout) findViewById(R.id.nav_eat);
        nav_todo.setOnClickListener(this);
        nav_eat.setOnClickListener(this);

        //设置底部弹窗
        showBoomMenu();

        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appBar);

        mPullToRefreshView = (PullToRefreshView) findViewById(R.id.pull_to_refresh);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset >= 0) {
                    mPullToRefreshView.setEnabled(true);
                } else {
                    mPullToRefreshView.setEnabled(false);
                }
            }
        });

        mPullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullToRefreshView.setRefreshing(false);
                        Intent intent = new Intent(MainActivity.this, HeadImageActivity.class);
                        intent.putExtra("headPicUrl", headPicUrl);
                        startActivity(intent);
                    }
                }, 500);
            }
        });

        boolean isNotification = SpUtils.getBoolean(this, AppConstants.IS_NOTIFICATION);
        if (isNotification) {
            setNotification();
        } else {
            cancelNotification();
        }

    }

    //设置底部弹窗按钮--------------------------开始----------------------------
    private void showBoomMenu() {
        BoomMenuButton boomMenuButton = (BoomMenuButton) findViewById(R.id.bmb);
        for (int i = 0; i < boomMenuButton.getPiecePlaceEnum().pieceNumber(); i++) {
            TextOutsideCircleButton.Builder builder = new TextOutsideCircleButton.Builder()
                    .listener(new OnBMClickListener() {
                        @Override
                        public void onBoomButtonClick(int index) {
                            switch (index) {
                                case 0:
                                    Intent emsIntent = new Intent(MainActivity.this, EMSActivity.class);
                                    startActivity(emsIntent);
                                    break;
                                case 1:
                                    Intent ocrIntent = new Intent(MainActivity.this, OCRActivity.class);
                                    startActivity(ocrIntent);
                                    break;
                                case 2:
                                    Intent audioIntent = new Intent(MainActivity.this, AudioActivity.class);
                                    startActivity(audioIntent);
                                    break;
                                case 3:
                                    Intent translateIntent = new Intent(MainActivity.this, TranslateActivity.class);
                                    startActivity(translateIntent);
                                    break;
                                case 4:
                                    Intent eipIntent = new Intent(MainActivity.this, EipActivity.class);
                                    startActivity(eipIntent);
                                    break;
                            }
                        }
                    })
                    .imagePadding(new Rect(25, 25, 25, 25))
                    .normalImageRes(getImageResource())
                    .normalText(getext())
                    .textTopMargin(10)
                    .textSize(12);
            boomMenuButton.addBuilder(builder);
        }
    }

    static String getext() {
        if (index >= text.length) index = 0;
        return text[index++];

    }

    private static String[] text = new String[]{"快递查询", "文字识别", "分贝计", "在线翻译", "表情包制作"};
    private static int imageResourceIndex = 0;

    static int getImageResource() {
        if (imageResourceIndex >= imageResources.length) imageResourceIndex = 0;
        return imageResources[imageResourceIndex++];
    }

    private static int[] imageResources = new int[]{
            R.drawable.ic_menu_ems,
            R.drawable.ic_menu_ocr,
            R.drawable.ic_menu_library,
            R.drawable.ic_menu_translate,
            R.drawable.ic_menu_eip
    };
    //设置底部弹窗按钮--------------------------结束----------------------------

    //发送查询天气的请求
    private void requestWeather() {
        String weatherUrl = "https://free-api.heweather.com/v5/weather?city=CN101190301&key=38c845e8310644ee83a8a7bba9b9be64";
        HttpUtil.sendHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (response.isSuccessful()) {
                    Weather weather = Util.handleWeatherResponse(responseText);
                    parseWeatherData(weather);
                } else {
                    Toast.makeText(getApplicationContext(), "服务器错误", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    //解析天气请求的数据
    private void parseWeatherData(Weather weather) {
        final String degree = weather.now.temperature + "℃";
        final String weatherInfo = weather.now.more.info;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                collapsingToolbarLayout.setTitle(degree + " " + weatherInfo);
            }
        });
    }

    //发送加载首页图片的请求
    private void loadHeadPic() {
        String requestHeadPic = "http://120.25.88.41/just/img";
        HttpUtil.sendHttpRequest(requestHeadPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String headPic = response.body().string();
                SpUtils.putString(MainActivity.this, AppConstants.HEAD_PIC_URL, headPic);
                headPicUrl = headPic;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(MainActivity.this).load(headPicUrl).crossFade().into(head_image_view);
                    }
                });
            }
        });
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
        Notification notification = new NotificationCompat.Builder(MainActivity.this)
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

    //弹出捐赠对话框
    private void showDonateDialog() {
        final DonateDialog donateDialog = new DonateDialog(this);
        donateDialog.onCreateView();
        donateDialog.setUiBeforShow();
        //点击空白区域能不能退出
        donateDialog.setCanceledOnTouchOutside(true);
        //按返回键能不能退出
        donateDialog.setCancelable(true);
        donateDialog.show();
    }

    //取消顶部常驻通知栏
    private void cancelNotification() {
        notificationManager.cancel(2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                Intent helpIntent = new Intent(MainActivity.this, FeedBackActivity.class);
                startActivity(helpIntent);
                break;
            case R.id.action_donate:
                showDonateDialog();
                break;
            case R.id.action_exit:
                this.finish();
                break;
            case android.R.id.home:
                mDrawer.openMenu();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        mDrawer.closeMenu();

        switch (item.getItemId()) {
            case R.id.nav_grade:
                Intent search_subject_intent = new Intent(MainActivity.this, SubjectsActivity.class);
                startActivity(search_subject_intent);
                break;
            case R.id.nav_library:
                Toast.makeText(MainActivity.this, "你点击了馆藏查询按钮", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_pe:
                Toast.makeText(MainActivity.this, "你点击了体育课查询按钮", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_school_bus:
                Intent school_busIntent = new Intent(MainActivity.this, SchoolBusActivity.class);
                startActivity(school_busIntent);
                break;
            case R.id.nav_setting:
                Intent settings_intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settings_intent);
                break;
            case R.id.nav_classroom:
                Toast.makeText(MainActivity.this, "你点击了查询空教室按钮", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_theme:
                Toast.makeText(MainActivity.this, "你点击了更换皮肤按钮", Toast.LENGTH_SHORT).show();
                break;
        }

        return true;
    }

    //点击两次返回键退出
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                long secondTime = System.currentTimeMillis();
                if (secondTime - firstTime > 2000) {
                    Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    firstTime = secondTime;
                    return true;
                } else {
                    System.exit(0);
                }
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.civ_header:
                Intent avatorIntent = new Intent(MainActivity.this, AvatarActivity.class);
                startActivity(avatorIntent);
                break;
            case R.id.iv_constellation:
                constellation_en = SpUtils.getString(this, AppConstants.EN_CONSTELLATION);
                if (constellation_en != null) {
                    Intent constellationIntent = new Intent(MainActivity.this, ConstellationActivity.class);
                    constellationIntent.putExtra("constellation_en", constellation_en);
                    startActivity(constellationIntent);
                } else {
                    new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("未检测到你的星座！")
                            .setContentText("需要前往设置你的星座吗？")
                            .setCancelText("关闭")
                            .setConfirmText("设置")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                                    startActivity(intent);
                                    sDialog.dismiss();
                                }
                            })
                            .show();
                }
                break;
            case R.id.iv_memory:
                Intent memoryIntent = new Intent(MainActivity.this, MemoryDayActivity.class);
                startActivity(memoryIntent);
                break;
            case R.id.iv_schedule:
                Toast.makeText(MainActivity.this, "课程表", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_history:
                Intent historyIntent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(historyIntent);
                break;
            case R.id.iv_weibo:
                Intent weiboIntent = new Intent(MainActivity.this, WeiBoActivity.class);
                startActivity(weiboIntent);
                break;
            case R.id.iv_one:
                Intent oneIntent = new Intent(MainActivity.this, OneActivity.class);
                startActivity(oneIntent);
                break;
            case R.id.nav_todo:
                Intent todoIntent = new Intent(MainActivity.this, ToDoActivity.class);
                startActivity(todoIntent);
                break;
            case R.id.nav_eat:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        imageBase64 = SpUtils.getString(this, AppConstants.IMAGE_BASE_64);
        if (imageBase64 != null) {
            byte[] byte64 = Base64.decode(imageBase64, 0);
            ByteArrayInputStream bais = new ByteArrayInputStream(byte64);
            Bitmap bitmap = BitmapFactory.decodeStream(bais);
            civ_header.setImageBitmap(bitmap);
        }
    }

}
