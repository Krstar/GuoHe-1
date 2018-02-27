package com.lyy.guohe.activity.Main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
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
import com.lyy.guohe.R;
import com.lyy.guohe.activity.Game.GameActivity;
import com.lyy.guohe.activity.Memory.MemoryDayActivity;
import com.lyy.guohe.activity.One.ConstellationActivity;
import com.lyy.guohe.activity.One.HistoryActivity;
import com.lyy.guohe.activity.One.OneActivity;
import com.lyy.guohe.activity.One.WeiBoActivity;
import com.lyy.guohe.activity.School.CETActivity;
import com.lyy.guohe.activity.School.ClassRoomActivity;
import com.lyy.guohe.activity.School.ClubActivity;
import com.lyy.guohe.activity.School.CourseTableActivity;
import com.lyy.guohe.activity.School.ExerciseActivity;
import com.lyy.guohe.activity.School.LibraryActivity;
import com.lyy.guohe.activity.School.NewSubjectActivity;
import com.lyy.guohe.activity.School.SchoolBusActivity;
import com.lyy.guohe.activity.School.SubjectsActivity;
import com.lyy.guohe.activity.School.ToDoActivity;
import com.lyy.guohe.activity.School.X5WebViewActivity;
import com.lyy.guohe.activity.Setting.FeedBackActivity;
import com.lyy.guohe.activity.Setting.ProfileActivity;
import com.lyy.guohe.activity.Setting.SettingsActivity;
import com.lyy.guohe.activity.Tools.AudioActivity;
import com.lyy.guohe.activity.Tools.OCRActivity;
import com.lyy.guohe.activity.Tools.WebToolsActivity;
import com.lyy.guohe.base.DonateDialog;
import com.lyy.guohe.base.PopupActivity;
import com.lyy.guohe.db.DBCourse;
import com.lyy.guohe.db.DBCurrentCourse;
import com.lyy.guohe.gson.Weather;
import com.lyy.guohe.model.Res;
import com.lyy.guohe.util.AppConstants;
import com.lyy.guohe.util.HttpUtil;
import com.lyy.guohe.util.ResponseUtil;
import com.lyy.guohe.util.SpUtils;
import com.lyy.guohe.util.UrlUtil;
import com.mxn.soul.flowingdrawer_core.ElasticDrawer;
import com.mxn.soul.flowingdrawer_core.FlowingDrawer;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.TextOutsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencent.stat.StatService;
import com.yalantis.taurus.PullToRefreshView;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private static final String TAG = "MainActivity";

    private Context mContext;

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

        String server_week = SpUtils.getString(mContext, AppConstants.SERVER_WEEK);
        if (server_week != null) {
            List<DBCourse> courseList = DataSupport.where("zhouci = ? ", server_week).find(DBCourse.class);
            for (int i = 0; i < courseList.size(); i++) {
                if (courseList.get(i).getDes().length() > 5 && courseList.get(i).getDay() == a[weekday]) {
                    if (hour < 12 && hour > 0) {
                        now_course_jieci_1.setText("1-2");
                        now_course_jieci_2.setText("3-4");
                        if (courseList.get(i).getJieci() == 1) {
//                            now_course_jieci_1.setText("1-2");
                            String courseInfo[] = courseList.get(i).getDes().split("@");
                            Log.d(TAG, "show_now_course: " + courseList.get(i).getDes());
                            String courseName = "";
                            String courseClassRoom = "";
                            if (courseInfo.length == 2 || courseInfo.length == 3) {
                                courseName = courseInfo[1];
                            } else if (courseInfo.length == 4) {
                                courseName = courseInfo[1];
                                courseClassRoom = courseInfo[3];
                            }
                            now_course_1_1.setText(courseName);
                            Log.d(TAG, "show_now_course: " + courseName);
                            now_course_1_2.setText(courseClassRoom);
                        } else if (courseList.get(i).getJieci() == 3) {
//                            now_course_jieci_2.setText("3-4");
                            Log.d(TAG, "show_now_course: " + courseList.get(i).getDes());
                            String courseInfo[] = courseList.get(i).getDes().split("@");
                            String courseName = "";
                            String courseClassRoom = "";
                            if (courseInfo.length == 2 || courseInfo.length == 3) {
                                courseName = courseInfo[1];
                            } else if (courseInfo.length == 4) {
                                courseName = courseInfo[1];
                                courseClassRoom = courseInfo[3];
                            }
                            now_course_2_1.setText(courseName);
                            now_course_2_2.setText(courseClassRoom);
                        }
                    } else if (hour > 12 && hour < 18) {
                        now_course_jieci_1.setText("5-6");
                        now_course_jieci_2.setText("7-8");
                        if (courseList.get(i).getJieci() == 5) {
//                            now_course_jieci_1.setText("5-6");
                            String courseInfo[] = courseList.get(i).getDes().split("@");
                            String courseName = "";
                            String courseClassRoom = "";
                            if (courseInfo.length == 2 || courseInfo.length == 3) {
                                courseName = courseInfo[1];
                            } else if (courseInfo.length == 4) {
                                courseName = courseInfo[1];
                                courseClassRoom = courseInfo[3];
                            }
                            now_course_1_1.setText(courseName);
                            now_course_1_2.setText(courseClassRoom);
                        } else if (courseList.get(i).getJieci() == 7) {
//                            now_course_jieci_2.setText("7-8");
                            String courseInfo[] = courseList.get(i).getDes().split("@");
                            String courseName = "";
                            String courseClassRoom = "";
                            if (courseInfo.length == 2 || courseInfo.length == 3) {
                                courseName = courseInfo[1];
                            } else if (courseInfo.length == 4) {
                                courseName = courseInfo[1];
                                courseClassRoom = courseInfo[3];
                            }
                            now_course_2_1.setText(courseName);
                            now_course_2_2.setText(courseClassRoom);
                        }
                    } else if (hour > 19 && hour < 24) {
                        now_course_jieci_1.setText("9-10");
                        now_course_jieci_2.setText("11-12");
                        if (courseList.get(i).getJieci() == 9) {
//                            now_course_jieci_1.setText("9-10");
                            String courseInfo[] = courseList.get(i).getDes().split("@");
                            String courseName = "";
                            String courseClassRoom = "";
                            if (courseInfo.length == 2 || courseInfo.length == 3) {
                                courseName = courseInfo[1];
                            } else if (courseInfo.length == 4) {
                                courseName = courseInfo[1];
                                courseClassRoom = courseInfo[3];
                            }
                            now_course_1_1.setText(courseName);
                            now_course_1_2.setText(courseClassRoom);
                        } else if (courseList.get(i).getJieci() == 11) {
//                            now_course_jieci_2.setText("11-12");
                            String courseInfo[] = courseList.get(i).getDes().split("@");
                            String courseName = "";
                            String courseClassRoom = "";
                            if (courseInfo.length == 2 || courseInfo.length == 3) {
                                courseName = courseInfo[1];
                            } else if (courseInfo.length == 4) {
                                courseName = courseInfo[1];
                                courseClassRoom = courseInfo[3];
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

        String server_week = SpUtils.getString(mContext, AppConstants.SERVER_WEEK);
        if (server_week != null) {
            List<DBCourse> courseList = DataSupport.where("zhouci = ? ", server_week).find(DBCourse.class);
            for (int i = 0; i < courseList.size(); i++) {
                if (courseList.get(i).getDes().length() > 5 && courseList.get(i).getDay() == a[weekday]) {
                    stringList.add(courseList.get(i).getDes());
                }
            }
            listWithoutDup = new ArrayList<>(new HashSet<>(stringList));
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
        //设置和toolbar相关的
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_menu);
        }
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle("GuoHe");

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
//        ImageView iv_health = (ImageView) findViewById(R.id.iv_health);
        ImageView iv_weibo = (ImageView) findViewById(R.id.iv_weibo);
        ImageView iv_schedule = (ImageView) findViewById(R.id.iv_schedule);
        ImageView iv_one = (ImageView) findViewById(R.id.iv_one);
        ImageView iv_memory = (ImageView) findViewById(R.id.iv_memory);
        ImageView iv_history = (ImageView) findViewById(R.id.iv_history);

        Glide.with(this).load(R.drawable.bg_constellation).into(iv_constellation);
//        Glide.with(this).load(R.drawable.bg_health).into(iv_health);
        Glide.with(this).load(R.drawable.bg_weibo).into(iv_weibo);
        Glide.with(this).load(R.drawable.bg_every_day).into(iv_one);
        Glide.with(this).load(R.drawable.bg_memory).into(iv_memory);
        iv_schedule.setBackgroundColor(getResources().getColor(R.color.material_white_1000));
        Glide.with(this).load(R.drawable.bg_history).into(iv_history);

        iv_constellation.setOnClickListener(this);
//        iv_health.setOnClickListener(this);
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
        LinearLayout nav_pe = (LinearLayout) findViewById(R.id.nav_pe);
        LinearLayout nav_system = (LinearLayout) findViewById(R.id.nav_system);
        LinearLayout nav_cet = (LinearLayout) findViewById(R.id.nav_cet);

        ll_schedule.setOnClickListener(this);

        nav_library.setOnClickListener(this);
        nav_grade.setOnClickListener(this);
        nav_school_bus.setOnClickListener(this);
        nav_classroom.setOnClickListener(this);
        nav_pe.setOnClickListener(this);
        nav_system.setOnClickListener(this);
        nav_cet.setOnClickListener(this);

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
                                    Intent emsIntent = new Intent(MainActivity.this, WebToolsActivity.class);
                                    emsIntent.putExtra("URL", UrlUtil.EMS);
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
                                    Intent translateIntent = new Intent(MainActivity.this, WebToolsActivity.class);
                                    translateIntent.putExtra("URL", UrlUtil.TRANSLATE);
                                    startActivity(translateIntent);
                                    break;
                                case 4:
                                    Intent eipIntent = new Intent(MainActivity.this, WebToolsActivity.class);
                                    eipIntent.putExtra("URL", UrlUtil.EIP);
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

    //权限获取
    private void obtain_permission() {
        RxPermissions rxPermissions = new RxPermissions(MainActivity.this);
        rxPermissions
                .request(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.SET_WALLPAPER,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.VIBRATE,
                        Manifest.permission.READ_PHONE_STATE
                )
                .subscribe(granted -> {
                    if (granted) {
//                        Toast.makeText(MainActivity.this, "同意权限", Toast.LENGTH_SHORT).show();
                    } else {
//                        Toast.makeText(MainActivity.this, "拒绝权限", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //发送查询校历的请求
    private void requestXiaoLi() {
        String url = UrlUtil.XIAO_LI;
        String stu_id = SpUtils.getString(mContext, AppConstants.STU_ID);
        String stu_pass = SpUtils.getString(mContext, AppConstants.STU_PASS);
        final String local_week = SpUtils.getString(mContext, AppConstants.SERVER_WEEK, "0");

        if (stu_id != null && stu_pass != null) {
            final RequestBody requestBody = new FormBody.Builder()
                    .add("username", stu_id)
                    .add("password", stu_pass)
                    .build();

            HttpUtil.sendPostHttpRequest(url, requestBody, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.d(TAG, "onFailure: " + "服务器异常");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String data = response.body().string();
                        if (data.length() > 3) {
                            Res res = ResponseUtil.handleResponse(data);
                            assert res != null;
                            if (res.getCode() == 200) {
                                SpUtils.putString(mContext, AppConstants.XIAO_LI, res.getInfo());
                                try {
                                    JSONObject object = new JSONObject(res.getInfo());
                                    //获取当前周数
                                    String server_week = object.getString("weekNum");
                                    if (Integer.parseInt(local_week) < Integer.parseInt(server_week))
                                        SpUtils.putString(mContext, AppConstants.SERVER_WEEK, server_week);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toasty.error(mContext, "服务器异常，请稍后", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toasty.error(mContext, "服务器异常，请稍后", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toasty.error(mContext, "错误" + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
    }

    //发送查询天气的请求
    private void requestWeather() {
        String weatherUrl = UrlUtil.WEATHER;
        HttpUtil.sendHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Looper.prepare();
                Toasty.error(mContext, "天气请求失败", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseText = response.body().string();
                    if (responseText != null) {
                        Weather weather = ResponseUtil.handleWeatherResponse(responseText);
                        parseWeatherData(weather);
                    } else {
                        Looper.prepare();
                        Toasty.error(mContext, "服务器错误", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                } else {
                    Looper.prepare();
                    Toasty.error(mContext, "服务器错误", Toast.LENGTH_SHORT).show();
                    Looper.loop();
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
        String requestHeadPic = UrlUtil.HEAD_PIC;
        HttpUtil.sendHttpRequest(requestHeadPic, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Looper.prepare();
                Toasty.error(mContext, "网络异常，请稍后重试", Toast.LENGTH_LONG).show();
                Looper.loop();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseText = response.body().string();
                SpUtils.putString(MainActivity.this, AppConstants.HEAD_PIC_URL, responseText);
                headPicUrl = responseText;
                if (headPicUrl != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!isFinishing())
                                Glide.with(MainActivity.this).load(headPicUrl).crossFade().into(head_image_view);
                        }
                    });

                }

            }
        });
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

    //分享此应用
    private void shareApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, "我发现了一个不错的应用哦：" + UrlUtil.APP);
            intent.setType("text/plain");
            startActivity(Intent.createChooser(intent, "果核"));
        } else {
            final AlertDialog.Builder normalDialog =
                    new AlertDialog.Builder(MainActivity.this);
            normalDialog.setTitle("果核");
            normalDialog.setMessage("请选择分享方式");
            normalDialog.setPositiveButton("分享应用",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //...To-do
                            try {
                                File apkFile = new File(getPackageManager().getPackageInfo("com.example.lyy.newjust", 0).applicationInfo.sourceDir);
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_SEND);
                                intent.setType("*/*");
                                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(apkFile));
                                startActivity(intent);
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            normalDialog.setNegativeButton("分享链接",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //...To-do
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_TEXT, "我发现了一个不错的应用哦：" + UrlUtil.APP);
                            intent.setType("text/plain");
                            startActivity(Intent.createChooser(intent, "果核"));
                        }
                    });
            // 显示
            normalDialog.show();
        }
    }

    //选择进入哪一个系统
    private void showSystemDialog() {
        final String[] items = {"教务系统", "奥兰系统", "实验系统", "师生服务中心"};
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle("选择要进入的系统");
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始

                Intent intent = new Intent(MainActivity.this, X5WebViewActivity.class);
                switch (which) {
                    case 0:
                        intent.putExtra("url", UrlUtil.JIAOWU_URL);
                        intent.putExtra("title", "强智教务");
                        startActivity(intent);
                        break;
                    case 1:
                        intent.putExtra("url", UrlUtil.AOLAN_URL);
                        intent.putExtra("title", "奥兰系统");
                        startActivity(intent);
                        break;
                    case 2:
                        intent.putExtra("url", UrlUtil.LAB_URL);
                        intent.putExtra("title", "实验系统");
                        startActivity(intent);
                        break;
                    case 3:
                        intent.putExtra("url", UrlUtil.FUWU_URL);
                        intent.putExtra("title", "师生服务中心");
                        startActivity(intent);
                        break;
                }
            }
        });
        listDialog.show();
    }

    //选择查询六级还是四级
    private void showCETDialog() {
        final String[] items = {"四六级成绩查询", "四六级准考证找回"};
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle("请选择");
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                switch (which) {
                    case 0:
                        String cetUrl = "http://cet.neea.edu.cn/cet/";
                        Intent intent = new Intent(MainActivity.this, PopupActivity.class);
                        intent.putExtra("URL", cetUrl);
                        startActivity(intent);
                        break;
                    case 1:
                        Intent cetIntent = new Intent(MainActivity.this, CETActivity.class);
                        startActivity(cetIntent);
                        break;
                }
            }
        });
        listDialog.show();
    }

    //选择进入哪一个体育查询窗口
    private void showPEDialog() {
        final String[] items = {"俱乐部查询", "早操查询"};
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle("请选择");
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                switch (which) {
                    case 0:
                        Intent clubIntent = new Intent(MainActivity.this, ClubActivity.class);
                        startActivity(clubIntent);
                        break;
                    case 1:
                        Intent exerciseIntent = new Intent(MainActivity.this, ExerciseActivity.class);
                        startActivity(exerciseIntent);
                        break;
                }
            }
        });
        listDialog.show();
    }

    //切换账号
    private void changeAccount() {
        final boolean isLogin = SpUtils.getBoolean(getApplicationContext(), AppConstants.LOGIN);
        if (isLogin) {
            SpUtils.remove(getApplicationContext(), AppConstants.LOGIN);
            SpUtils.remove(getApplicationContext(), AppConstants.STU_ACADEMY);
            SpUtils.remove(getApplicationContext(), AppConstants.STU_ID);
            SpUtils.remove(getApplicationContext(), AppConstants.STU_MAJOR);
            SpUtils.remove(getApplicationContext(), AppConstants.STU_NAME);
            SpUtils.remove(getApplicationContext(), AppConstants.STU_PASS);
            SpUtils.remove(getApplicationContext(), AppConstants.STU_PE_PASS);
            SpUtils.remove(getApplicationContext(), AppConstants.HEAD_PIC_URL);
            SpUtils.remove(getApplicationContext(), AppConstants.FIRST_OPEN_COURSE);
            SpUtils.remove(getApplicationContext(), AppConstants.SERVER_WEEK);
            SpUtils.remove(getApplicationContext(), AppConstants.XIAO_LI);
            SpUtils.remove(getApplicationContext(), "year");
            SpUtils.remove(getApplicationContext(), "month");
            SpUtils.remove(getApplicationContext(), "day");
            SpUtils.remove(getApplicationContext(), "birthday");
            SpUtils.remove(getApplicationContext(), "constellation");
            SpUtils.remove(getApplicationContext(), "constellation_en");

            DataSupport.deleteAll(DBCourse.class);
            DataSupport.deleteAll(DBCurrentCourse.class);

            if (SettingsActivity.SettingActivity != null) {
                if (!SettingsActivity.SettingActivity.isFinishing())
                    SettingsActivity.SettingActivity.finish();
            }
            if (SettingsActivity.SettingActivity != null) {
                if (!MainActivity.mainActivity.isFinishing())
                    MainActivity.mainActivity.finish();
            }

            finish();

            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//关掉所要到的界面中间的activity
            startActivity(intent);
            Toasty.success(getApplicationContext(), "退出成功", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }
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
                //在此处退出APP
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.

        mDrawer.closeMenu();

        switch (item.getItemId()) {
            case R.id.nav_todo:
                Intent todo_intent = new Intent(MainActivity.this, ToDoActivity.class);
                startActivity(todo_intent);
                break;
            case R.id.nav_setting:
                Intent settings_intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settings_intent);
                break;
            case R.id.nav_change_account:
                changeAccount();
                break;
            case R.id.nav_share:
                shareApp();
                break;
            case R.id.nav_game:
                Intent gameIntent = new Intent(MainActivity.this, GameActivity.class);
                startActivity(gameIntent);
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
            case R.id.nav_library:
                Intent libraryIntent = new Intent(MainActivity.this, LibraryActivity.class);
                startActivity(libraryIntent);
                break;
            case R.id.nav_grade:
                String show_grade = SpUtils.getString(mContext, AppConstants.SHOW_GRADE, "学年");
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
            case R.id.nav_pe:
                showPEDialog();
                break;
            case R.id.nav_system:
                showSystemDialog();
                break;
            case R.id.nav_cet:
                showCETDialog();
                break;
        }
    }

    /**
     * 通过反射，设置menu显示icon
     *
     * @param view
     * @param menu
     * @return
     */
    @SuppressLint("RestrictedApi")
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (menu != null) {
            if (menu.getClass() == MenuBuilder.class) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                }
            }
        }
        return super.onPrepareOptionsPanel(view, menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;

        changeStatusBar();  //将背景图和状态栏融合到一起的方法

        setContentView(R.layout.activity_main);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        mContext = this;

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
            String stu_name = SpUtils.getString(mContext, AppConstants.STU_NAME);
            String stu_id = SpUtils.getString(mContext, AppConstants.STU_ID);
            tv_stu_id.setText(stu_id);
            tv_stu_name.setText(stu_name);
        }

        obtain_permission();

        init();             //初始化相关控件

        loadHeadPic();      //添加每日一图

        requestWeather();   //发送查询天气的请求

        show_course_number();
        show_now_course();
        requestXiaoLi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        StatService.onResume(this);
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
}
