package com.example.lyy.newjust.activity.Setting;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lyy.newjust.R;
import com.example.lyy.newjust.activity.Main.LoginActivity;
import com.example.lyy.newjust.activity.MainActivity;
import com.example.lyy.newjust.db.DBCourse;
import com.example.lyy.newjust.db.DBCurrentCourse;
import com.example.lyy.newjust.util.AppConstants;
import com.example.lyy.newjust.util.SpUtils;
import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.ActionSheetDialog;
import com.githang.statusbar.StatusBarCompat;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.PermissionListener;

import org.litepal.crud.DataSupport;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class ProfileActivity extends SwipeBackActivity implements View.OnClickListener {

    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;


    private Uri imageUri;

    private TextView tv_constellation;
    private TextView tv_birthday;

    private String birthday;
    private String constellation;
    private String constellation_en;
    private int year, month, day;

    private String imageBase64;

    private CircleImageView civ_header;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //去掉Activity上面的状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_profile);

        StatusBarCompat.setStatusBarColor(this, Color.rgb(0, 127, 193));
        setSwipeBackEnable(true);   // 可以调用该方法，设置是否允许滑动退出
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        // 设置滑动方向，可设置EDGE_LEFT, EDGE_RIGHT, EDGE_ALL, EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        // 滑动退出的效果只能从边界滑动才有效果，如果要扩大touch的范围，可以调用这个方法
        mSwipeBackLayout.setEdgeSize(200);

        obtain_permission();

        init();
    }

    @SuppressLint("CommitPrefEdits")
    private void init() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_back_blue);
        }

        constellation = SpUtils.getString(this, "constellation", null);
        constellation_en = SpUtils.getString(this, "constellation_en", null);
        birthday = SpUtils.getString(this, "birthday", null);
        year = SpUtils.getInt(this, "year", 1994);
        month = SpUtils.getInt(this, "month", 12);
        day = SpUtils.getInt(this, "day", 1);


        imageBase64 = SpUtils.getString(this, AppConstants.IMAGE_BASE_64);

        LinearLayout ll_birthday = (LinearLayout) findViewById(R.id.ll_birthday);
        LinearLayout ll_constellation = (LinearLayout) findViewById(R.id.ll_constellation);
        ll_birthday.setOnClickListener(this);
        ll_constellation.setOnClickListener(this);

        tv_constellation = (TextView) findViewById(R.id.tv_constellation);
        tv_birthday = (TextView) findViewById(R.id.tv_birthday);

        if (constellation != null)
            tv_birthday.setText(birthday);
        if (constellation != null)
            tv_constellation.setText(constellation);

        civ_header = (CircleImageView) findViewById(R.id.civ_header);
        civ_header.setOnClickListener(this);
        if (imageBase64 != null) {
            byte[] byte64 = Base64.decode(imageBase64, 0);
            ByteArrayInputStream bais = new ByteArrayInputStream(byte64);
            Bitmap bitmap = BitmapFactory.decodeStream(bais);
            civ_header.setImageBitmap(bitmap);
        }

        TextView tv_stu_name = (TextView) findViewById(R.id.tv_stu_name);
        TextView tv_stu_id = (TextView) findViewById(R.id.tv_stu_id);
        TextView tv_stu_academy = (TextView) findViewById(R.id.tv_stu_academy);
        TextView tv_stu_major = (TextView) findViewById(R.id.tv_stu_major);

        String stu_name = SpUtils.getString(getApplicationContext(), AppConstants.STU_NAME, "");
        String stu_id = SpUtils.getString(getApplicationContext(), AppConstants.STU_ID, "");
        String stu_academy = SpUtils.getString(getApplicationContext(), AppConstants.STU_ACADEMY, "");
        String stu_major = SpUtils.getString(getApplicationContext(), AppConstants.STU_MAJOR, "");

        tv_stu_name.setText(stu_name);
        tv_stu_id.setText(stu_id);
        tv_stu_major.setText(stu_major);
        tv_stu_academy.setText(stu_academy);

        final Button btn_login = (Button) findViewById(R.id.btn_login);
        final boolean isLogin = SpUtils.getBoolean(getApplicationContext(), AppConstants.LOGIN);
        if (isLogin) {
            btn_login.setText("退出登录");
        }
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    Toast.makeText(getApplicationContext(), "退出成功", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        });
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
            imageUri = FileProvider.getUriForFile(ProfileActivity.this, "com.example.lyy.newjust.fileprovider", outputImage);
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

    //显示日期选择弹窗
    private void showDatePicker() {
        DatePickerDialog dd = new DatePickerDialog(ProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                String temp_constellation = getConstellation(month + 1, day);
                String temp_constellation_en = getENConstellation(temp_constellation);
                String temp_birthday = year + "年" + (month + 1) + "月" + day + "日";

                tv_birthday.setText(temp_birthday);
                tv_constellation.setText(temp_constellation);

                SpUtils.putInt(ProfileActivity.this, "year", year);
                SpUtils.putInt(ProfileActivity.this, "month", month);
                SpUtils.putInt(ProfileActivity.this, "day", day);

                SpUtils.putString(ProfileActivity.this, "birthday", temp_birthday);
                SpUtils.putString(ProfileActivity.this, "constellation", temp_constellation);
                SpUtils.putString(ProfileActivity.this, "constellation_en", temp_constellation_en);
            }
        }, year, month, day);
        dd.show();
    }

    //将日期转换成星座
    private String getConstellation(int month, int day) {
        String[] starArr = {"魔羯座", "水瓶座", "双鱼座", "白羊座",
                "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座"};
        int[] DayArr = {22, 20, 19, 21, 21, 21, 22, 23, 23, 23, 23, 22};  // 两个星座分割日
        int index = month;
        // 所查询日期在分割日之前，索引-1，否则不变
        if (day < DayArr[month - 1]) {
            index = index - 1;
        }
        // 返回索引指向的星座string
        return starArr[index];
    }

    //将中文的星座转换成英文的星座
    public static String getENConstellation(String select) {
        String horoscope_name[] = {"白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "魔羯座", "水瓶座", "双鱼座"};
        String horoscope_english[] = {"aries", "taurus", "gemini", "cancer", "cancer", "leo", "virgo", "libra", "scorpio", "sagittarius", "aquarius", "pisces"};
        for (int i = 0; i < horoscope_name.length; i++) {
            if (horoscope_name[i].contains(select)) {
                return horoscope_english[i];
            }
        }
        return select;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        // 将拍摄的照片显示出来
                        Intent cropIntent = new Intent(ProfileActivity.this, CropViewActivity.class);
                        cropIntent.putExtra("uri", imageUri.toString());
                        cropIntent.putExtra("flag", "header");
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
                        Intent cropIntent = new Intent(ProfileActivity.this, CropViewActivity.class);
                        cropIntent.putExtra("flag", "header");
                        cropIntent.putExtra("uri", uri.toString());
                        startActivity(cropIntent);
                    } else {
                        Toast.makeText(ProfileActivity.this, "安卓版本过低", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                break;
        }
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
            case R.id.ll_birthday:
                showDatePicker();
                break;
            case R.id.ll_constellation:
                if (tv_constellation.getText().equals(""))
                    Toasty.warning(ProfileActivity.this, "你还未设置星座，点击生日设置", Toast.LENGTH_SHORT).show();
                break;
            case R.id.civ_header:
                final String[] stringItems = {"拍照", "从相册中选择"};
                final ActionSheetDialog dialog = new ActionSheetDialog(ProfileActivity.this, stringItems, null);
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
                        }
                        dialog.dismiss();
                    }
                });
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
