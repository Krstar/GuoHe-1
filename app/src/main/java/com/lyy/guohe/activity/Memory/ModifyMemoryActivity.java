package com.lyy.guohe.activity.Memory;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lyy.guohe.R;
import com.lyy.guohe.activity.Setting.CropViewActivity;
import com.lyy.guohe.db.DBMemory;
import com.githang.statusbar.StatusBarCompat;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.IOException;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class ModifyMemoryActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ModifyMemoryActivity";

    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;

    private TextView tv_datePicker;

    private EditText et_memory_content;

    private Uri imageUri;

    private int year, month, date;

    private String choose_date;

    private String flag;

    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_memory);

        StatusBarCompat.setStatusBarColor(this, Color.rgb(0, 172, 193));

        //设置和toolbar相关的
        Toolbar toolbar = (Toolbar) findViewById(R.id.add_memory_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        Time time = new Time("GMT+8");
        time.setToNow();
        year = time.year;
        month = time.month;
        date = time.monthDay;

        et_memory_content = (EditText) findViewById(R.id.et_memory_content);

        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0);
        String memory_date = intent.getStringExtra("memory_date");
        String memory_content = intent.getStringExtra("memory_content");
        flag = intent.getStringExtra("flag");

        if (flag.equals("add")) {
            choose_date = year + "年" + (month + 1) + "月" + date + "日";
        } else if (flag.equals("modify")) {
            choose_date = memory_date;
            et_memory_content.setText(memory_content);
        }


        tv_datePicker = (TextView) findViewById(R.id.tv_datePicker);
        tv_datePicker.setText(choose_date);
        tv_datePicker.setOnClickListener(this);

    }

    //显示日期选择弹窗
    private void showDatePicker() {
        DatePickerDialog dd = new DatePickerDialog(ModifyMemoryActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                choose_date = year + "年" + (month + 1) + "月" + day + "日";
                tv_datePicker.setText(choose_date);
            }
        }, year, month, date);
        dd.show();
    }

    //保存最后的结果
    private void confirmMemory() {
        if (flag.equals("add")) {
            DBMemory dbMemory = new DBMemory();
            if (!(et_memory_content.getText().toString().equals("")) && choose_date != null) {
                dbMemory.setMemory_content(et_memory_content.getText().toString());
                dbMemory.setMemory_day(choose_date);
                dbMemory.save();
                Toasty.success(ModifyMemoryActivity.this, "已添加", Toast.LENGTH_SHORT).show();
            } else {
                ModifyMemoryActivity.this.finish();
            }
        } else if (flag.equals("modify")) {
            List<DBMemory> dbMemoryList = DataSupport.findAll(DBMemory.class);
            dbMemoryList.get(position).setMemory_day(choose_date);
            dbMemoryList.get(position).setMemory_content(et_memory_content.getText().toString());
            dbMemoryList.get(position).save();
            Toasty.success(ModifyMemoryActivity.this, "已修改", Toast.LENGTH_SHORT).show();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
            case R.id.action_sure:
                confirmMemory();
                ModifyMemoryActivity.this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_memory, menu);
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_datePicker:
                showDatePicker();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        // 将拍摄的照片显示出来
                        Intent cropIntent = new Intent(ModifyMemoryActivity.this, CropViewActivity.class);
                        cropIntent.putExtra("uri", imageUri.toString());
                        cropIntent.putExtra("flag", "memory");
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
                        Intent cropIntent = new Intent(ModifyMemoryActivity.this, CropViewActivity.class);
                        cropIntent.putExtra("uri", uri.toString());
                        cropIntent.putExtra("flag", "memory");
                        startActivity(cropIntent);
                    } else {
                        Toasty.error(ModifyMemoryActivity.this, "安卓版本过低", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                break;
        }
    }

}
