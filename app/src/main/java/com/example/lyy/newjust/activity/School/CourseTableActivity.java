package com.example.lyy.newjust.activity.School;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.lyy.newjust.AndroidApplication;
import com.example.lyy.newjust.R;
import com.example.lyy.newjust.activity.Setting.CropViewActivity;
import com.example.lyy.newjust.db.DBCourse;
import com.example.lyy.newjust.db.DBCurrentCourse;
import com.example.lyy.newjust.model.Course;
import com.example.lyy.newjust.util.AppConstants;
import com.example.lyy.newjust.util.HttpUtil;
import com.example.lyy.newjust.util.SpUtils;
import com.example.lyy.newjust.util.UrlUtil;
import com.example.lyy.newjust.views.CourseTableView;
import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.ActionSheetDialog;
import com.githang.statusbar.StatusBarCompat;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import shortbread.Shortcut;

@Shortcut(id = "course", icon = R.drawable.ic_menu_coursetable, shortLabel = "查课表")
public class CourseTableActivity extends SwipeBackActivity {
    private static CourseTableActivity courseTableActivity;

    private ProgressDialog dialog;

    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;

    private static final String TAG = "CourseTableActivity";

    private CourseTableView courseTableView;

    private Uri imageUri;

    private ImageView iv_course_table;

    private String bg_course_64;

    private String choose_year;
    private int week;

    private int color[] = {
            R.drawable.course_info_blue,
            R.drawable.course_info_brown,
            R.drawable.course_info_cyan,
            R.drawable.course_info_deep_orange,
            R.drawable.course_info_deep_purple,
            R.drawable.course_info_green,
            R.drawable.course_info_indigo,
            R.drawable.course_info_light_blue,
            R.drawable.course_info_light_green,
            R.drawable.course_info_lime,
            R.drawable.course_info_orange,
            R.drawable.course_info_pink,
            R.drawable.course_info_purple,
            R.drawable.course_info_red,
            R.drawable.course_info_teal,
            R.drawable.course_info_yellow
    };

    private TextView tv_course_table_toolbar;

    private List<String> all_year_list;

    Handler current_course_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    dialog.dismiss();
                    String weekNum = SpUtils.getString(getApplicationContext(), AppConstants.SERVER_THIS_WEEK);
                    if (weekNum != null) {
                        tv_course_table_toolbar.setText("第" + weekNum + "周");
                    }
                    showCurrentCourseTable();
                    break;
            }
        }
    };

    Handler all_course_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    dialog.dismiss();
                    String weekNum = SpUtils.getString(getApplicationContext(), AppConstants.SERVER_THIS_WEEK);
                    if (weekNum != null) {
                        tv_course_table_toolbar.setText("第" + weekNum + "周");
                    }
                    showCourseTable();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_table);

        courseTableActivity = this;
        all_year_list = new ArrayList<>();

        StatusBarCompat.setStatusBarColor(this, Color.rgb(0, 172, 193));
        setSwipeBackEnable(true);   // 可以调用该方法，设置是否允许滑动退出
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        // 设置滑动方向，可设置EDGE_LEFT, EDGE_RIGHT, EDGE_ALL, EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        // 滑动退出的效果只能从边界滑动才有效果，如果要扩大touch的范围，可以调用这个方法
        mSwipeBackLayout.setEdgeSize(200);

        //设置和toolbar相关的
        Toolbar toolbar = (Toolbar) findViewById(R.id.course_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        tv_course_table_toolbar = (TextView) findViewById(R.id.tv_course_table_toolbar);

        requestXiaoLi();    //发送查询校历的请求

        String weekNum = SpUtils.getString(getApplicationContext(), AppConstants.SERVER_THIS_WEEK);
        if (weekNum != null) {
            tv_course_table_toolbar.setText("第" + weekNum + "周");
            //判断是否存在本地周数，如果没有，就把服务器上的当前周保存
        }

        //初始化课表的背景
        iv_course_table = (ImageView) findViewById(R.id.iv_course_table);
        bg_course_64 = SpUtils.getString(getApplicationContext(), AppConstants.BG_COURSE_64);
        if (bg_course_64 != null) {
            byte[] byte64 = Base64.decode(bg_course_64, 0);
            ByteArrayInputStream bais = new ByteArrayInputStream(byte64);
            Bitmap bitmap = BitmapFactory.decodeStream(bais);
            iv_course_table.setImageBitmap(bitmap);
        } else {
            Glide.with(getApplicationContext()).load(R.drawable.bg_course_default).into(iv_course_table);
        }

        //构造课表界面
        courseTableView = (CourseTableView) findViewById(R.id.ctv);

        courseTableView.setOnCourseItemClickListener(new CourseTableView.OnCourseItemClickListener() {
            @Override
            public void onCourseItemClick(TextView tv, int jieci, int day, String des) {
                String string = tv.getText().toString();
                Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
            }
        });

        showCurrentCourseTable();
    }

    //发送查询校历的请求
    private void requestXiaoLi() {
        String url = UrlUtil.XIAO_LI;

        String stu_id = SpUtils.getString(getApplicationContext(), AppConstants.STU_ID);
        String stu_pass = SpUtils.getString(getApplicationContext(), AppConstants.STU_PASS);
        final RequestBody requestBody = new FormBody.Builder()
                .add("username", stu_id)
                .add("password", stu_pass)
                .build();

        HttpUtil.sendPostHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                SpUtils.putBoolean(getApplicationContext(), AppConstants.LOGIN, true);
                String data = response.body().string();
                if (response.code() == 200) {
                    try {
                        JSONObject object = new JSONObject(data);
                        //获取当前周数
                        String weekNum = object.getString("weekNum");
                        //获取今天是周几
                        String week = object.getString("week");
                        //获取这个学生所有的学年
                        JSONArray jsonArray = object.getJSONArray("all_year");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            all_year_list.add(jsonArray.get(i).toString());
                        }

                        if (weekNum != null && week != null) {
                            SpUtils.putString(getApplicationContext(), AppConstants.SERVER_THIS_WEEK, weekNum);
                            SpUtils.putString(getApplicationContext(), AppConstants.TODAY, week);

                            if (SpUtils.getString(getApplicationContext(), AppConstants.LOCAL_CURRENT_WEEK) == null) {
                                SpUtils.putString(getApplicationContext(), AppConstants.LOCAL_CURRENT_WEEK, weekNum);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                }
            }
        });
    }

    //显示当前周课表
    private void showCurrentCourseTable() {
        List<Course> list = new ArrayList<>();

        List<DBCurrentCourse> courseList = DataSupport.findAll(DBCurrentCourse.class);

        List<String> stringList = new ArrayList<>();

        String local_week = SpUtils.getString(getApplicationContext(), AppConstants.LOCAL_CURRENT_WEEK);
        String server_week = SpUtils.getString(getApplicationContext(), AppConstants.SERVER_THIS_WEEK);

        if (courseList.size() != 0) {
            Log.d(TAG, "showCurrentCourseTable: " + local_week);
            Log.d(TAG, "showCurrentCourseTable: " + server_week);
            if (local_week.equals(server_week)) {
                for (DBCurrentCourse dbCurrentCourse : courseList) {
                    stringList.add(dbCurrentCourse.getDes());
                }

                List<String> listWithoutDup = new ArrayList<String>(new HashSet<String>(stringList));

                for (DBCurrentCourse dbCurrentCourse : courseList) {
                    Course course = new Course();
                    if (dbCurrentCourse.getDes().length() > 3) {
                        Log.d(TAG, "onCreate: " + dbCurrentCourse.getDes());
                        course.setDay(dbCurrentCourse.getDay());
                        course.setJieci(dbCurrentCourse.getJieci());
                        course.setDes(dbCurrentCourse.getDes());
                        course.setBg_Color(color[listWithoutDup.indexOf(dbCurrentCourse.getDes())]);
                        list.add(course);
                    }
                }
                courseTableView.updateCourseViews(list);
            } else {
                dialog = new ProgressDialog(CourseTableActivity.this);
                dialog.setMessage("正在导入课表...");
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();

                SpUtils.putString(getApplicationContext(), AppConstants.LOCAL_CURRENT_WEEK, SpUtils.getString(getApplicationContext(), AppConstants.SERVER_THIS_WEEK));

                new RequestCurrentCourseThread().start();
            }
        } else {
            dialog = new ProgressDialog(CourseTableActivity.this);
            dialog.setMessage("正在导入课表...");
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();

            new RequestCurrentCourseThread().start();
        }
    }

    //显示课表
    private void showCourseTable() {
        List<Course> list = new ArrayList<>();

        List<DBCourse> courseList = DataSupport.findAll(DBCourse.class);

        List<String> stringList = new ArrayList<>();

        String local_week = SpUtils.getString(getApplicationContext(), AppConstants.LOCAL_CURRENT_WEEK);
        String server_week = SpUtils.getString(getApplicationContext(), AppConstants.SERVER_THIS_WEEK);

        if (courseList.size() != 0) {
            if (local_week.equals(server_week)) {
                for (DBCourse dbCourse : courseList) {
                    stringList.add(dbCourse.getDes());
                }

                List<String> listWithoutDup = new ArrayList<String>(new HashSet<String>(stringList));

                for (DBCourse dbCourse : courseList) {
                    Course course = new Course();
                    if (dbCourse.getDes().length() > 3) {
                        Log.d(TAG, "onCreate: " + dbCourse.getDes());
                        course.setDay(dbCourse.getDay());
                        course.setJieci(dbCourse.getJieci());
                        course.setDes(dbCourse.getDes());
                        course.setBg_Color(color[listWithoutDup.indexOf(dbCourse.getDes())]);
                        list.add(course);
                    }
                }
                courseTableView.updateCourseViews(list);
            } else {
                dialog = new ProgressDialog(CourseTableActivity.this);
                dialog.setMessage("正在导入课表...");
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();

                new RequestCurrentCourseThread().start();
            }
        } else {
            dialog = new ProgressDialog(CourseTableActivity.this);
            dialog.setMessage("正在导入课表...");
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();

            new RequestCurrentCourseThread().start();
        }
    }

    class RequestCurrentCourseThread extends Thread {
        @Override
        public void run() {
            try {
                requestCurrentCourseInfo();
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                current_course_handler.sendEmptyMessage(1);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class RequestAllCourseThread extends Thread {

        @Override
        public void run() {
            try {
                requestCourseInfo(choose_year, week);
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                all_course_handler.sendEmptyMessage(2);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //显示单选dialog
    int yourChoice;

    private void showSingleChoiceDialog() {
        if (all_year_list.size() != 0) {
            final String[] items = new String[all_year_list.size()];
            for (int i = 0; i < all_year_list.size(); i++) {
                items[i] = all_year_list.get(i);
            }
            yourChoice = 0;
            AlertDialog.Builder singleChoiceDialog =
                    new AlertDialog.Builder(CourseTableActivity.this);
            singleChoiceDialog.setTitle("选择学年");
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
                        public void onClick(DialogInterface radio_dialog, int which) {
                            if (yourChoice != -1) {
                                DataSupport.deleteAll(DBCurrentCourse.class);
                                DataSupport.deleteAll(DBCourse.class);
                                dialog = new ProgressDialog(CourseTableActivity.this);
                                dialog.setMessage("正在导入课表...");
                                dialog.setCancelable(true);
                                dialog.setCanceledOnTouchOutside(true);
                                dialog.show();
                                String this_week = SpUtils.getString(getApplicationContext(), AppConstants.SERVER_THIS_WEEK);
                                choose_year = items[yourChoice];
                                week = Integer.parseInt(this_week);
                                new RequestAllCourseThread().start();
                            }
                        }
                    });
            singleChoiceDialog.show();
        } else {
            Toast.makeText(getApplicationContext(), "暂未获取到你的所有学年，请退出后重试", Toast.LENGTH_SHORT).show();
        }
    }

    private void take_photos() {
        // 创建File对象，用于存储拍照后的图片
        File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT < 24) {
            imageUri = Uri.fromFile(outputImage);
        } else {
            imageUri = FileProvider.getUriForFile(getApplicationContext(), "com.example.lyy.newjust.fileprovider", outputImage);
        }
        // 启动相机程序
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    private void choosePhotoFromGallery() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO); // 打开相册
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_course_table, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_update_course:

                break;
            case R.id.action_change_year:
                showSingleChoiceDialog();
                break;
            case R.id.action_change_background:
                final String[] stringItems = {"拍照", "从相册中选择", "重置为默认"};
                final ActionSheetDialog dialog = new ActionSheetDialog(CourseTableActivity.this, stringItems, null);
                dialog.isTitleShow(false).show();

                dialog.setOnOperItemClickL(new OnOperItemClickL() {
                    @Override
                    public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                        switch (position) {
                            case 0:
                                take_photos();
                                break;
                            case 1:
                                choosePhotoFromGallery();
                                break;
                            case 2:
                                Glide.with(getApplicationContext()).load(R.drawable.bg_course_default).into(iv_course_table);
                                SpUtils.remove(getApplicationContext(), AppConstants.BG_COURSE_64);
                                break;
                        }
                        dialog.dismiss();
                    }
                });
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        // 将拍摄的照片显示出来
                        Intent cropIntent = new Intent(CourseTableActivity.this, CropViewActivity.class);
                        cropIntent.putExtra("uri", imageUri.toString());
                        cropIntent.putExtra("flag", "course");
                        startActivity(cropIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    // 判断手机系统版本号
                    if (Build.VERSION.SDK_INT >= 19) {
                        // 4.4及以上系统使用这个方法处理图片
                        Uri uri = data.getData();
                        Intent cropIntent = new Intent(CourseTableActivity.this, CropViewActivity.class);
                        cropIntent.putExtra("uri", uri.toString());
                        cropIntent.putExtra("flag", "course");
                        startActivity(cropIntent);
                    } else {
                        Toast.makeText(CourseTableActivity.this, "安卓版本过低", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                break;
        }
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        bg_course_64 = SpUtils.getString(this, AppConstants.BG_COURSE_64);
        if (bg_course_64 != null) {
            byte[] byte64 = Base64.decode(bg_course_64, 0);
            ByteArrayInputStream bais = new ByteArrayInputStream(byte64);
            Bitmap bitmap = BitmapFactory.decodeStream(bais);
            iv_course_table.setImageBitmap(bitmap);
        }
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    //获取当前周的课表信息
    private void requestCurrentCourseInfo() {

        requestXiaoLi();

        String url = UrlUtil.CURRENT_COURSE;

        String stu_id = SpUtils.getString(getApplicationContext(), AppConstants.STU_ID);
        String stu_pass = SpUtils.getString(getApplicationContext(), AppConstants.STU_PASS);
        final RequestBody requestBody = new FormBody.Builder()
                .add("username", stu_id)
                .add("password", stu_pass)
                .build();

        HttpUtil.sendPostHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Looper.prepare();
                Toast.makeText(AndroidApplication.getContext(), "服务器异常，请稍后重试", Toast.LENGTH_LONG).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    try {
                        JSONArray innerArray = new JSONArray(data);

                        for (int i = 0; i < 5; i++) {
                            JSONObject monday = innerArray.getJSONObject(i);
                            if (!monday.getString("monday").equals("")) {
                                DBCurrentCourse course = new DBCurrentCourse();
                                course.setDay(1);
                                course.setJieci((i * 2) + 1);
                                course.setDes(monday.getString("monday"));
                                course.save();
                            }
                        }
                        for (int i = 0; i < 5; i++) {
                            JSONObject tuesday = innerArray.getJSONObject(i);
                            if (!tuesday.getString("tuesday").equals("")) {
                                DBCurrentCourse course = new DBCurrentCourse();
                                course.setDay(2);
                                course.setJieci((i * 2) + 1);
                                course.setDes(tuesday.getString("tuesday"));
                                course.save();
                            }
                        }
                        for (int i = 0; i < 5; i++) {
                            JSONObject wednesday = innerArray.getJSONObject(i);
                            if (!wednesday.getString("wednesday").equals("")) {
                                DBCurrentCourse course = new DBCurrentCourse();
                                course.setDay(3);
                                course.setJieci((i * 2) + 1);
                                course.setDes(wednesday.getString("wednesday"));
                                course.save();
                            }
                        }
                        for (int i = 0; i < 5; i++) {
                            JSONObject thursday = innerArray.getJSONObject(i);
                            if (!thursday.getString("thursday").equals("")) {
                                DBCurrentCourse course = new DBCurrentCourse();
                                course.setDay(4);
                                course.setJieci((i * 2) + 1);
                                course.setDes(thursday.getString("thursday"));
                                course.save();
                            }
                        }
                        for (int i = 0; i < 5; i++) {
                            JSONObject friday = innerArray.getJSONObject(i);
                            if (!friday.getString("friday").equals("")) {
                                DBCurrentCourse course = new DBCurrentCourse();
                                course.setDay(5);
                                course.setJieci((i * 2) + 1);
                                course.setDes(friday.getString("friday"));
                                course.save();
                            }
                        }
                        for (int i = 0; i < 5; i++) {
                            JSONObject saturday = innerArray.getJSONObject(i);
                            if (!saturday.getString("saturday").equals("")) {
                                DBCurrentCourse course = new DBCurrentCourse();
                                course.setDay(6);
                                course.setJieci((i * 2) + 1);
                                course.setDes(saturday.getString("saturday"));
                                course.save();
                            }
                        }
                        for (int i = 0; i < 5; i++) {
                            JSONObject sunday = innerArray.getJSONObject(i);
                            if (!sunday.getString("sunday").equals("")) {
                                DBCurrentCourse course = new DBCurrentCourse();
                                course.setDay(7);
                                course.setJieci((i * 2) + 1);
                                course.setDes(sunday.getString("sunday"));
                                course.save();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //获取所有周的课表信息
    private void requestCourseInfo(final String year, final int this_week) {
        String url = UrlUtil.ALL_COURSE;
        String stu_id = SpUtils.getString(getApplicationContext(), AppConstants.STU_ID);
        String stu_pass = SpUtils.getString(getApplicationContext(), AppConstants.STU_PASS);
        RequestBody requestBody = new FormBody.Builder()
                .add("username", stu_id)
                .add("password", stu_pass)
                .add("semester", year)
                .build();

        HttpUtil.sendPostHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    Log.d(TAG, "onResponse: " + data);
                    try {
                        JSONArray jsonArray = new JSONArray(data);
                        JSONObject object = jsonArray.getJSONObject(this_week - 1);
                        JSONArray innerArray = object.getJSONArray(year + "_" + this_week);

                        for (int i = 0; i < 5; i++) {
                            JSONObject monday = innerArray.getJSONObject(i);
                            if (!monday.getString("monday").equals("")) {
                                DBCourse course = new DBCourse();
                                course.setDay(1);
                                course.setJieci((i * 2) + 1);
                                course.setDes(monday.getString("monday"));
                                course.save();
                            }

                        }
                        for (int i = 0; i < 5; i++) {
                            JSONObject tuesday = innerArray.getJSONObject(i);
                            if (!tuesday.getString("tuesday").equals("")) {
                                DBCourse course = new DBCourse();
                                course.setDay(2);
                                course.setJieci((i * 2) + 1);
                                course.setDes(tuesday.getString("tuesday"));
                                course.save();
                            }
                        }
                        for (int i = 0; i < 5; i++) {
                            JSONObject wednesday = innerArray.getJSONObject(i);
                            if (!wednesday.getString("wednesday").equals("")) {
                                DBCourse course = new DBCourse();
                                course.setDay(3);
                                course.setJieci((i * 2) + 1);
                                course.setDes(wednesday.getString("wednesday"));
                                course.save();
                            }
                        }
                        for (int i = 0; i < 5; i++) {
                            JSONObject thursday = innerArray.getJSONObject(i);
                            if (!thursday.getString("thursday").equals("")) {
                                DBCourse course = new DBCourse();
                                course.setDay(4);
                                course.setJieci((i * 2) + 1);
                                course.setDes(thursday.getString("thursday"));
                                course.save();
                            }
                        }
                        for (int i = 0; i < 5; i++) {
                            JSONObject friday = innerArray.getJSONObject(i);
                            if (!friday.getString("friday").equals("")) {
                                DBCourse course = new DBCourse();
                                course.setDay(5);
                                course.setJieci((i * 2) + 1);
                                course.setDes(friday.getString("friday"));
                                course.save();
                            }

                        }
                        for (int i = 0; i < 5; i++) {
                            JSONObject saturday = innerArray.getJSONObject(i);
                            if (!saturday.getString("saturday").equals("")) {
                                DBCourse course = new DBCourse();
                                course.setDay(6);
                                course.setJieci((i * 2) + 1);
                                course.setDes(saturday.getString("saturday"));
                                course.save();
                            }

                        }
                        for (int i = 0; i < 5; i++) {
                            JSONObject sunday = innerArray.getJSONObject(i);
                            if (!sunday.getString("sunday").equals("")) {
                                DBCourse course = new DBCourse();
                                course.setDay(7);
                                course.setJieci((i * 2) + 1);
                                course.setDes(sunday.getString("sunday"));
                                course.save();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}
