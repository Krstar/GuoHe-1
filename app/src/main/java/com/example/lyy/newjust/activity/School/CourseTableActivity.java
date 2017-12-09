package com.example.lyy.newjust.activity.School;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
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
import com.example.lyy.newjust.R;
import com.example.lyy.newjust.activity.Setting.CropViewActivity;
import com.example.lyy.newjust.db.DBCourse;
import com.example.lyy.newjust.model.Course;
import com.example.lyy.newjust.model.Res;
import com.example.lyy.newjust.util.AppConstants;
import com.example.lyy.newjust.util.HttpUtil;
import com.example.lyy.newjust.util.ResponseUtil;
import com.example.lyy.newjust.util.SpUtils;
import com.example.lyy.newjust.util.UrlUtil;
import com.example.lyy.newjust.views.CourseTableView;
import com.flyco.animation.BounceEnter.BounceBottomEnter;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.ActionSheetDialog;
import com.flyco.dialog.widget.MaterialDialog;
import com.githang.statusbar.StatusBarCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
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

    private Context mContext;

    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;

    private static final String TAG = "CourseTableActivity";

    private CourseTableView courseTableView;

    private ProgressDialog mProgressDialog;

    private Uri imageUri;

    private ImageView iv_course_table;

    private String bg_course_64;

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

    private String stu_id;
    private String stu_pass;
    private String current_year;

    private String server_week;     //服务器当前周

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_table);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        mContext = this;

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

        //初始化课表的背景
        iv_course_table = (ImageView) findViewById(R.id.iv_course_table);
        bg_course_64 = SpUtils.getString(mContext, AppConstants.BG_COURSE_64);
        if (bg_course_64 != null) {
            byte[] byte64 = Base64.decode(bg_course_64, 0);
            ByteArrayInputStream bais = new ByteArrayInputStream(byte64);
            Bitmap bitmap = BitmapFactory.decodeStream(bais);
            iv_course_table.setImageBitmap(bitmap);
        } else {
            Glide.with(mContext).load(R.drawable.bg_course_default).into(iv_course_table);
        }

        //构造课表界面
        courseTableView = (CourseTableView) findViewById(R.id.ctv);

        courseTableView.setOnCourseItemClickListener(new CourseTableView.OnCourseItemClickListener() {
            @Override
            public void onCourseItemClick(TextView tv, int jieci, int day, String des) {
                String string = tv.getText().toString();
                showCourseDialog(string);
            }
        });

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.fab_course);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWeekChoiceDialog();
            }
        });

        mProgressDialog = new ProgressDialog(CourseTableActivity.this);
        mProgressDialog.setMessage("课表导入中,请稍后……");
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(true);

        stu_id = SpUtils.getString(mContext, AppConstants.STU_ID);
        stu_pass = SpUtils.getString(mContext, AppConstants.STU_PASS);

        server_week = SpUtils.getString(mContext, AppConstants.SERVER_WEEK);
        if (server_week != null) {
            tv_course_table_toolbar.setText("第" + server_week + "周");
            Calendar calendar = Calendar.getInstance();
            int weekday = calendar.get(Calendar.DAY_OF_WEEK);
            //判断今天是不是周一
            if (weekday == 2) {
                //判断是否第一次导入课表，默认false，没有导入课表
                boolean first_open_course = SpUtils.getBoolean(mContext, AppConstants.FIRST_OPEN_COURSE);
                if (first_open_course) {
                    Log.d(TAG, "onCreate: " + first_open_course);
                    requestXiaoLi();
                    SpUtils.putBoolean(mContext, AppConstants.FIRST_OPEN_COURSE, false);
                } else {
                    Log.d(TAG, "onCreate: " + first_open_course);
                    showCourseTable(server_week);
                }
            } else {
                //判断是否第一次导入课表，默认false，没有导入课表
                boolean first_open_course = SpUtils.getBoolean(mContext, AppConstants.FIRST_OPEN_COURSE);
                if (first_open_course) {
                    Log.d(TAG, "onCreate: " + first_open_course);
                    requestXiaoLi();
                    SpUtils.putBoolean(mContext, AppConstants.FIRST_OPEN_COURSE, false);
                } else {
                    Log.d(TAG, "onCreate: " + first_open_course);
                    showCourseTable(server_week);
                }
            }

            List<DBCourse> dbCourses = DataSupport.findAll(DBCourse.class);
            if (dbCourses.size() == 0) {
                requestXiaoLi();
                SpUtils.putBoolean(mContext, AppConstants.FIRST_OPEN_COURSE, false);
            }

        } else {
            requestXiaoLi();
            SpUtils.putBoolean(mContext, AppConstants.FIRST_OPEN_COURSE, false);
        }
    }

    private void showCourseDialog(String courseMesg) {
        String[] courseInfo = courseMesg.split("@");
        String courseClassroom = "";
        String courseName = "";
        String courseTeacher = "";
        if (courseInfo.length == 1) {
            courseName = courseInfo[0];
        }
        if (courseInfo.length == 2) {
            courseName = courseInfo[0];
            courseTeacher = courseInfo[1];
        }
        if (courseInfo.length == 3) {
            courseName = courseInfo[0];
            courseTeacher = courseInfo[1];
            courseClassroom = courseInfo[2];
        }

        final MaterialDialog dialog = new MaterialDialog(mContext);

        dialog.isTitleShow(false)//
                .btnNum(1)
                .content("课程信息为：\n" + "课程名：\t" + courseName + "\n课程教师：\t" + courseTeacher + "\n教室：\t" + courseClassroom)
                .btnText("确定")//
                .showAnim(new BounceBottomEnter())
                .show();

        dialog.setOnBtnClickL(
                new OnBtnClickL() {//left btn click listener
                    @Override
                    public void onBtnClick() {
                        dialog.dismiss();
                    }
                }
        );

    }

    //发送查询校历的请求
    private void requestXiaoLi() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.show();
            }
        });
        String url = UrlUtil.XIAO_LI;
        final RequestBody requestBody = new FormBody.Builder()
                .add("username", stu_id)
                .add("password", stu_pass)
                .build();

        HttpUtil.sendPostHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        Toast.makeText(mContext, "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    Res res = ResponseUtil.handleResponse(data);
                    if (res.getCode() == 200) {
                        SpUtils.putString(mContext, AppConstants.XIAO_LI, res.getInfo());
                        try {
                            JSONObject object = new JSONObject(res.getInfo());
                            //获取当前周数
                            server_week = object.getString("weekNum");
                            //获取这个学生所有的学年
                            JSONArray jsonArray = object.getJSONArray("all_year");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                all_year_list.add(jsonArray.get(i).toString());
                            }
                            current_year = all_year_list.get(0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_course_table_toolbar.setText("第" + server_week + "周");
                            }
                        });
                        SpUtils.putString(mContext, AppConstants.SERVER_WEEK, server_week);
                        if (current_year != null) {
                            requestCourseInfo(current_year);
                        }
                    } else {
                        Looper.prepare();
                        if (mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        Toast.makeText(mContext, res.getMsg(), Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                            Toast.makeText(mContext, "错误" + response.code() + "，请稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
    }

    //获取所有周的课表信息
    private void requestCourseInfo(final String year) {
        String url = UrlUtil.ALL_COURSE;
        RequestBody requestBody = new FormBody.Builder()
                .add("username", stu_id)
                .add("password", stu_pass)
                .add("semester", year)
                .build();

        HttpUtil.sendPostHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        Toast.makeText(mContext, "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    Res res = ResponseUtil.handleResponse(data);
                    assert res != null;
                    if (res.getCode() == 200) {
                        try {
                            JSONArray jsonArray = new JSONArray(res.getInfo());
                            for (int k = 1; k <= jsonArray.length(); k++) {
                                JSONObject object = jsonArray.getJSONObject(k - 1);
                                Log.d(TAG, "onResponse: " + object);
                                JSONArray innerArray = object.getJSONArray(year + "_" + k);
                                for (int i = 0; i < 5; i++) {
                                    JSONObject monday = innerArray.getJSONObject(i);
                                    if (!monday.getString("monday").equals("")) {
                                        DBCourse course = new DBCourse();
                                        course.setDay(1);
                                        course.setJieci((i * 2) + 1);
                                        course.setZhouci(k);
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
                                        course.setZhouci(k);
                                        course.setDes(tuesday.getString("tuesday"));
                                        course.save();
                                    }
                                }
                                for (int i = 0; i < 5; i++) {
                                    JSONObject wednesday = innerArray.getJSONObject(i);
                                    if (!wednesday.getString("wednesday").equals("")) {
                                        DBCourse course = new DBCourse();
                                        course.setDay(3);
                                        course.setZhouci(k);
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
                                        course.setZhouci(k);
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
                                        course.setZhouci(k);
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
                                        course.setZhouci(k);
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
                                        course.setZhouci(k);
                                        course.setDes(sunday.getString("sunday"));
                                        course.save();
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mProgressDialog.isShowing()) {
                                    mProgressDialog.dismiss();
                                }
                                showCourseTable(server_week);
                            }
                        });
                    } else {
                        Looper.prepare();
                        if (mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        Toast.makeText(mContext, res.getMsg(), Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                            Toast.makeText(mContext, "错误" + response.code() + "，请稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    //显示课表
    private void showCourseTable(final String week) {
        List<Course> list = new ArrayList<>();

        List<DBCourse> courseList = DataSupport.where("zhouci = ? ", week).find(DBCourse.class);
        List<String> stringList = new ArrayList<>();

        for (DBCourse dbCourse : courseList) {
            stringList.add(dbCourse.getDes());
        }

        List<String> listWithoutDup = new ArrayList<>(new HashSet<>(stringList));

        for (DBCourse dbCourse : courseList) {
            Course course = new Course();
            if (dbCourse.getDes().length() > 3) {
                course.setDay(dbCourse.getDay());
                course.setJieci(dbCourse.getJieci());
                course.setDes(dbCourse.getDes());
                course.setBg_Color(color[listWithoutDup.indexOf(dbCourse.getDes())]);
                list.add(course);
            }
        }
        courseTableView.updateCourseViews(list);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
            }
        });


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
            imageUri = FileProvider.getUriForFile(mContext, "com.example.lyy.newjust.fileprovider", outputImage);
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
            case R.id.action_change_week:
                showWeekChoiceDialog();
                break;
            case R.id.action_update_course:
                DataSupport.deleteAll(DBCourse.class);
                SpUtils.remove(mContext, AppConstants.SERVER_WEEK);
                SpUtils.remove(mContext, AppConstants.XIAO_LI);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        requestXiaoLi();
                    }
                });
                break;
            case R.id.action_change_year:
                showSchoolYearChoiceDialog();
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
                                Glide.with(mContext).load(R.drawable.bg_course_default).into(iv_course_table);
                                SpUtils.remove(mContext, AppConstants.BG_COURSE_64);
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
        bg_course_64 = SpUtils.getString(this, AppConstants.BG_COURSE_64);
        if (bg_course_64 != null) {
            byte[] byte64 = Base64.decode(bg_course_64, 0);
            ByteArrayInputStream bais = new ByteArrayInputStream(byte64);
            Bitmap bitmap = BitmapFactory.decodeStream(bais);
            iv_course_table.setImageBitmap(bitmap);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }

    //显示单选dialog
    int yearChoice;

    private void showSchoolYearChoiceDialog() {
        String xiaoli = SpUtils.getString(mContext, AppConstants.XIAO_LI);
        if (xiaoli != null) {
            try {
                JSONObject object = new JSONObject(xiaoli);
                JSONArray jsonArray = object.getJSONArray("all_year");
                //获取当前周数
                server_week = object.getString("weekNum");
                for (int i = 0; i < jsonArray.length(); i++) {
                    all_year_list.add(jsonArray.get(i).toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            tv_course_table_toolbar.setText("第" + server_week + "周");
            List<String> listWithoutDup = new ArrayList<>(new HashSet<>(all_year_list));

            if (listWithoutDup.size() != 0) {
                final String[] items = new String[listWithoutDup.size()];
                for (int i = 0; i < listWithoutDup.size(); i++) {
                    items[i] = listWithoutDup.get(i);
                }
                yearChoice = 0;
                AlertDialog.Builder singleChoiceDialog =
                        new AlertDialog.Builder(CourseTableActivity.this);
                singleChoiceDialog.setTitle("选择学年");
                // 第二个参数是默认选项，此处设置为0
                singleChoiceDialog.setSingleChoiceItems(items, 0,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                yearChoice = which;
                            }
                        });
                singleChoiceDialog.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface radio_dialog, int which) {
                                if (yearChoice != -1) {
                                    mProgressDialog.setMessage("正在切换中,请稍后...");
                                    mProgressDialog.show();
                                    Log.d(TAG, "onClick: " + items[yearChoice]);
                                    DataSupport.deleteAll(DBCourse.class);
                                    requestCourseInfo(items[yearChoice]);
                                }
                            }
                        });
                singleChoiceDialog.show();
            } else {
                Toast.makeText(mContext, "暂未获取到你的所有学年，请退出后重试", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mContext, "暂未获取你的学年，请点击更新课表后重试", Toast.LENGTH_SHORT).show();
        }

    }

    int weekChoice;

    private void showWeekChoiceDialog() {
        final String[] items = {"第一周", "第二周", "第三周", "第四周", "第五周", "第六周", "第七周", "第八周", "第九周", "第十周", "第十一周", "第十二周", "第十三周", "第十四周", "第十五周", "第十六周", "第十七周", "第十八周", "第十九周", "第二十周"};
        weekChoice = 0;
        AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(CourseTableActivity.this);
        singleChoiceDialog.setTitle("请选择周次");
        // 第二个参数是默认选项，此处设置为0
        singleChoiceDialog.setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        weekChoice = which;
                    }
                });
        singleChoiceDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (weekChoice != -1) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv_course_table_toolbar.setText("第" + (weekChoice + 1) + "周");
                                    showCourseTable((weekChoice + 1) + "");
                                }
                            });
                        }
                    }
                });
        singleChoiceDialog.show();
    }
}
