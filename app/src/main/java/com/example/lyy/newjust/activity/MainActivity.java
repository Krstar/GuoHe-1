package com.example.lyy.newjust.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
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
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.lyy.newjust.R;
import com.example.lyy.newjust.activity.Main.AvatarActivity;
import com.example.lyy.newjust.activity.Main.HeadImageActivity;
import com.example.lyy.newjust.activity.Main.LoginActivity;
import com.example.lyy.newjust.activity.Main.WelcomeActivity;
import com.example.lyy.newjust.activity.Memory.MemoryDayActivity;
import com.example.lyy.newjust.activity.One.ConstellationActivity;
import com.example.lyy.newjust.activity.One.HistoryActivity;
import com.example.lyy.newjust.activity.One.OneActivity;
import com.example.lyy.newjust.activity.One.WeiBoActivity;
import com.example.lyy.newjust.activity.School.AoLanActivity;
import com.example.lyy.newjust.activity.School.ClassRoomActivity;
import com.example.lyy.newjust.activity.School.ClubActivity;
import com.example.lyy.newjust.activity.School.CourseTableActivity;
import com.example.lyy.newjust.activity.School.ExerciseActivity;
import com.example.lyy.newjust.activity.School.LibraryActivity;
import com.example.lyy.newjust.activity.School.NewSubjectActivity;
import com.example.lyy.newjust.activity.School.SchoolBusActivity;
import com.example.lyy.newjust.activity.School.SubjectsActivity;
import com.example.lyy.newjust.activity.School.ToDoActivity;
import com.example.lyy.newjust.activity.Setting.FeedBackActivity;
import com.example.lyy.newjust.activity.Setting.ProfileActivity;
import com.example.lyy.newjust.activity.Setting.SettingsActivity;
import com.example.lyy.newjust.activity.Tools.AudioActivity;
import com.example.lyy.newjust.activity.Tools.EMSActivity;
import com.example.lyy.newjust.activity.Tools.EipActivity;
import com.example.lyy.newjust.activity.Tools.OCRActivity;
import com.example.lyy.newjust.activity.Tools.TranslateActivity;
import com.example.lyy.newjust.db.DBCourse;
import com.example.lyy.newjust.gson.Weather;
import com.example.lyy.newjust.service.AlarmService;
import com.example.lyy.newjust.util.AppConstants;
import com.example.lyy.newjust.base.DonateDialog;
import com.example.lyy.newjust.util.HttpUtil;
import com.example.lyy.newjust.util.SpUtils;
import com.example.lyy.newjust.util.ResponseUtil;
import com.mxn.soul.flowingdrawer_core.ElasticDrawer;
import com.mxn.soul.flowingdrawer_core.FlowingDrawer;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.TextOutsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.umeng.analytics.MobclickAgent;
import com.yalantis.taurus.PullToRefreshView;

import org.litepal.crud.DataSupport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.glide.transformations.BlurTransformation;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private static final String TAG = "MainActivity";

    public static AppCompatActivity mainActivity;

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
        mainActivity = this;

        // 判断是否是第一次开启应用
        boolean isFirstOpen = SpUtils.getBoolean(this, AppConstants.FIRST_OPEN);
        // 如果是第一次启动，则先进入功能引导页
        if (!isFirstOpen) {
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        changeStatusBar();  //将背景图和状态栏融合到一起的方法

        setContentView(R.layout.activity_main);

        //判断是否已经登录
        boolean isLogIn = SpUtils.getBoolean(this, AppConstants.LOGIN);
        // 如果没有登陆，则先进入登陆页
        if (!isLogIn) {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        } else {
            TextView tv_stu_name = (TextView) findViewById(R.id.tv_stu_name);
            TextView tv_stu_id = (TextView) findViewById(R.id.tv_stu_id);
            String stu_name = SpUtils.getString(getApplicationContext(), AppConstants.STU_NAME);
            String stu_id = SpUtils.getString(getApplicationContext(), AppConstants.STU_ID);
            tv_stu_id.setText(stu_id);
            tv_stu_name.setText(stu_name);
        }

        Intent alarmService = new Intent(this, AlarmService.class);
        startService(alarmService);

        init();             //初始化相关控件

        loadHeadPic();      //添加每日一图

        requestWeather();   //发送查询天气的请求

        boolean isNotification = SpUtils.getBoolean(this, AppConstants.IS_NOTIFICATION);
        if (isNotification) {
            setNotification();
        } else {
            cancelNotification();
        }

        show_course_number();
        show_now_course();
    }

    private void show_now_course() {
        TextView now_course_1_1 = (TextView) findViewById(R.id.now_course_1_1);
        TextView now_course_1_2 = (TextView) findViewById(R.id.now_course_1_2);
        TextView now_course_2_1 = (TextView) findViewById(R.id.now_course_2_1);
        TextView now_course_2_2 = (TextView) findViewById(R.id.now_course_2_2);
        TextView now_course_jieci_1 = (TextView) findViewById(R.id.now_course_jieci_1);
        TextView now_course_jieci_2 = (TextView) findViewById(R.id.now_course_jieci_2);

        Calendar calendar = Calendar.getInstance();
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        int[] a = new int[]{0, 7, 1, 2, 3, 4, 5, 6};

        String server_week = SpUtils.getString(getApplicationContext(), AppConstants.SERVER_WEEK);
        Log.d(TAG, "show_now_course: " + server_week);
        if (server_week != null) {
            List<DBCourse> courseList = DataSupport.where("zhouci = ? ", server_week).find(DBCourse.class);
            for (int i = 0; i < courseList.size(); i++) {
                if (courseList.get(i).getDes().length() > 5 && courseList.get(i).getDay() == a[weekday]) {
                    if (hour < 12 && hour > 0) {
                        if (courseList.get(i).getJieci() == 1) {
                            now_course_jieci_1.setText("1-2");
                            Log.d(TAG, "show_now_course: " + courseList.get(i).getDes());
                            String courseInfo[] = courseList.get(i).getDes().split("@");
                            String courseName = "";
                            String courseClassRoom = "";
                            if (courseInfo.length == 1) {
                                courseName = courseInfo[0];
                            } else if (courseInfo.length == 2) {
                                courseName = courseInfo[0];
                            } else if (courseInfo.length == 3) {
                                courseName = courseInfo[0];
                                courseClassRoom = courseInfo[2];
                            }
                            now_course_1_1.setText(courseName);
                            now_course_1_2.setText(courseClassRoom);
                        } else if (courseList.get(i).getJieci() == 3) {
                            now_course_jieci_2.setText("3-4");
                            Log.d(TAG, "show_now_course: " + courseList.get(i).getDes());
                            String courseInfo[] = courseList.get(i).getDes().split("@");
                            String courseName = "";
                            String courseClassRoom = "";
                            if (courseInfo.length == 1) {
                                courseName = courseInfo[0];
                            } else if (courseInfo.length == 2) {
                                courseName = courseInfo[0];
                            } else if (courseInfo.length == 3) {
                                courseName = courseInfo[0];
                                courseClassRoom = courseInfo[2];
                            }
                            now_course_2_1.setText(courseName);
                            now_course_2_2.setText(courseClassRoom);
                        }
                    } else if (hour > 12 && hour < 18) {
                        if (courseList.get(i).getJieci() == 5) {
                            now_course_jieci_1.setText("5-6");
                            Log.d(TAG, "show_now_course: " + courseList.get(i).getDes());
                            String courseInfo[] = courseList.get(i).getDes().split("@");
                            String courseName = "";
                            String courseClassRoom = "";
                            if (courseInfo.length == 1) {
                                courseName = courseInfo[0];
                            } else if (courseInfo.length == 2) {
                                courseName = courseInfo[0];
                            } else if (courseInfo.length == 3) {
                                courseName = courseInfo[0];
                                courseClassRoom = courseInfo[2];
                            }
                            now_course_1_1.setText(courseName);
                            now_course_1_2.setText(courseClassRoom);
                        } else if (courseList.get(i).getJieci() == 7) {
                            now_course_jieci_2.setText("7-8");
                            Log.d(TAG, "show_now_course: " + courseList.get(i).getDes());
                            String courseInfo[] = courseList.get(i).getDes().split("@");
                            String courseName = "";
                            String courseClassRoom = "";
                            if (courseInfo.length == 1) {
                                courseName = courseInfo[0];
                            } else if (courseInfo.length == 2) {
                                courseName = courseInfo[0];
                            } else if (courseInfo.length == 3) {
                                courseName = courseInfo[0];
                                courseClassRoom = courseInfo[2];
                            }
                            now_course_2_1.setText(courseName);
                            now_course_2_2.setText(courseClassRoom);
                        }
                    } else if (hour > 19 && hour < 24) {
                        if (courseList.get(i).getJieci() == 9) {
                            now_course_jieci_1.setText("9-10");
                            Log.d(TAG, "show_now_course: " + courseList.get(i).getDes());
                            String courseInfo[] = courseList.get(i).getDes().split("@");
                            String courseName = "";
                            String courseClassRoom = "";
                            if (courseInfo.length == 1) {
                                courseName = courseInfo[0];
                            } else if (courseInfo.length == 2) {
                                courseName = courseInfo[0];
                            } else if (courseInfo.length == 3) {
                                courseName = courseInfo[0];
                                courseClassRoom = courseInfo[2];
                            }
                            now_course_1_1.setText(courseName);
                            now_course_1_2.setText(courseClassRoom);
                        } else if (courseList.get(i).getJieci() == 11) {
                            now_course_jieci_2.setText("11-12");
                            Log.d(TAG, "show_now_course: " + courseList.get(i).getDes());
                            String courseInfo[] = courseList.get(i).getDes().split("@");
                            String courseName = "";
                            String courseClassRoom = "";
                            if (courseInfo.length == 1) {
                                courseName = courseInfo[0];
                            } else if (courseInfo.length == 2) {
                                courseName = courseInfo[0];
                            } else if (courseInfo.length == 3) {
                                courseName = courseInfo[0];
                                courseClassRoom = courseInfo[2];
                            }
                            now_course_2_1.setText(courseName);
                            now_course_2_2.setText(courseClassRoom);
                        }
                    }
                }
            }
        }
    }

    //显示今天有多少节课
    private void show_course_number() {
        TextView tv_courses = (TextView) findViewById(R.id.tv_courses);

        Calendar calendar = Calendar.getInstance();
        int weekday = calendar.get(Calendar.DAY_OF_WEEK);

        int[] a = new int[]{0, 7, 1, 2, 3, 4, 5, 6};

        List<String> stringList = new ArrayList<>();
        List<String> listWithoutDup = new ArrayList<>();

        String server_week = SpUtils.getString(getApplicationContext(), AppConstants.SERVER_WEEK);
        if (server_week != null) {
            List<DBCourse> courseList = DataSupport.where("zhouci = ? ", server_week).find(DBCourse.class);
            for (int i = 0; i < courseList.size(); i++) {
                if (courseList.get(i).getDes().length() > 5 && courseList.get(i).getDay() == a[weekday]) {
                    stringList.add(courseList.get(i).getDes());
                }
            }
            listWithoutDup = new ArrayList<String>(new HashSet<String>(stringList));
            Log.d(TAG, "show_course_number: " + listWithoutDup.size());
            Log.d(TAG, "show_course_number: " + listWithoutDup);
        }

        if (listWithoutDup.size() == 0) {
            tv_courses.setText("今天没有课哦");
        } else {
            tv_courses.setText("今天有" + listWithoutDup.size() + "节课");
        }
    }

    //将背景图和状态栏融合到一起
    private void changeStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));
            }
        }
    }

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
//        Glide.with(this).load(R.drawable.bg_schedule).into(iv_schedule);
        iv_schedule.setBackgroundColor(getResources().getColor(R.color.material_white_1000));
//        Glide.with(this).load(R.drawable.bg_schedule).bitmapTransform(new BlurTransformation(this, 10)).into(iv_schedule);
        Glide.with(this).load(R.drawable.bg_history).into(iv_history);

        iv_constellation.setOnClickListener(this);
        iv_health.setOnClickListener(this);
        iv_one.setOnClickListener(this);
        iv_weibo.setOnClickListener(this);
        iv_memory.setOnClickListener(this);
        iv_schedule.setOnClickListener(this);
        iv_history.setOnClickListener(this);

        LinearLayout ll_schedule = (LinearLayout) findViewById(R.id.ll_schedule);

        LinearLayout nav_library = (LinearLayout) findViewById(R.id.nav_library);
        LinearLayout nav_grade = (LinearLayout) findViewById(R.id.nav_grade);
        LinearLayout nav_school_bus = (LinearLayout) findViewById(R.id.nav_school_bus);
        LinearLayout nav_classroom = (LinearLayout) findViewById(R.id.nav_classroom);

        ll_schedule.setOnClickListener(this);

        nav_library.setOnClickListener(this);
        nav_grade.setOnClickListener(this);
        nav_school_bus.setOnClickListener(this);
        nav_classroom.setOnClickListener(this);

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
            R.drawable.ic_menu_audio,
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
                Looper.prepare();
                Toast.makeText(getApplicationContext(), "天气请求失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseText = response.body().string();
                    Weather weather = ResponseUtil.handleWeatherResponse(responseText);
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
                Looper.prepare();
                Toast.makeText(getApplicationContext(), "服务器异常，请稍后重试", Toast.LENGTH_LONG).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String headPic = response.body().string();
                SpUtils.putString(MainActivity.this, AppConstants.HEAD_PIC_URL, headPic);
                headPicUrl = headPic;
                if (headPic!=null){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(MainActivity.this).load(headPicUrl).crossFade().into(head_image_view);
                        }
                    });
                }
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

    //分享此应用
    private void shareApp() {
        Intent intent1 = new Intent(Intent.ACTION_SEND);
        intent1.putExtra(Intent.EXTRA_TEXT, "我发现了一个不错的应用哦：" + "http://u5413978.viewer.maka.im/k/L3OW3S5E");
        intent1.setType("text/plain");
        startActivity(Intent.createChooser(intent1, "果核"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            case R.id.nav_todo:
                Intent todo_intent = new Intent(MainActivity.this, ToDoActivity.class);
                startActivity(todo_intent);
                break;
            case R.id.nav_exercise:
                Intent exerciseIntent = new Intent(MainActivity.this, ExerciseActivity.class);
                startActivity(exerciseIntent);
                break;
            case R.id.nav_club:
                Intent clubIntent = new Intent(MainActivity.this, ClubActivity.class);
                startActivity(clubIntent);
                break;

            case R.id.nav_setting:
                Intent settings_intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settings_intent);
                break;

            case R.id.nav_theme:
                Toast.makeText(MainActivity.this, "换肤功能正在集成中，敬请期待！", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_share:
                shareApp();
                break;

            case R.id.nav_ao_lan:
                Intent aolanIntent = new Intent(MainActivity.this, AoLanActivity.class);
                startActivity(aolanIntent);
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
            case R.id.ll_schedule:
                Intent scheduleIntent = new Intent(MainActivity.this, CourseTableActivity.class);
                startActivity(scheduleIntent);
                break;
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
                Intent courseIntent = new Intent(MainActivity.this, CourseTableActivity.class);
                startActivity(courseIntent);
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
            case R.id.iv_health:
                Toast.makeText(MainActivity.this, "该功能暂未上线，敬请期待！", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_library:
                Intent libraryIntent = new Intent(MainActivity.this, LibraryActivity.class);
                startActivity(libraryIntent);
                break;
            case R.id.nav_grade:
                String show_grade = SpUtils.getString(getApplicationContext(), AppConstants.SHOW_GRADE, "学年");
                if (show_grade.equals("学科")) {
                    Intent gradeIntent = new Intent(MainActivity.this, SubjectsActivity.class);
                    startActivity(gradeIntent);
                } else if (show_grade.equals("学年")) {
                    Intent gradeIntent = new Intent(MainActivity.this, NewSubjectActivity.class);
                    startActivity(gradeIntent);
                }
                break;
            case R.id.nav_school_bus:
                Intent busIntent = new Intent(MainActivity.this, SchoolBusActivity.class);
                startActivity(busIntent);
                break;
            case R.id.nav_classroom:
                Intent classroomIntent = new Intent(MainActivity.this, ClassRoomActivity.class);
                startActivity(classroomIntent);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        imageBase64 = SpUtils.getString(this, AppConstants.IMAGE_BASE_64);
        if (imageBase64 != null) {
            byte[] byte64 = Base64.decode(imageBase64, 0);
            ByteArrayInputStream bais = new ByteArrayInputStream(byte64);
            Bitmap bitmap = BitmapFactory.decodeStream(bais);
            civ_header.setImageBitmap(bitmap);
        }

        show_course_number();
        show_now_course();
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

}
