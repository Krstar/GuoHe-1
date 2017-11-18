package com.example.lyy.newjust.activity.Memory;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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

import com.example.lyy.newjust.R;
import com.example.lyy.newjust.db.DBMemory;
import com.githang.statusbar.StatusBarCompat;
import com.umeng.analytics.MobclickAgent;

import org.litepal.crud.DataSupport;

import java.util.List;

public class ModifyMemoryActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ModifyMemoryActivity";

    private TextView tv_datePicker;

    private EditText et_memory_content;

    private int year, month, date;

    private String choose_date;

    private String flag;

    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_memory);

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
                Toast.makeText(ModifyMemoryActivity.this, "已添加", Toast.LENGTH_SHORT).show();
            } else {
                ModifyMemoryActivity.this.finish();
            }
        } else if (flag.equals("modify")) {
            List<DBMemory> dbMemoryList = DataSupport.findAll(DBMemory.class);
            dbMemoryList.get(position).setMemory_day(choose_date);
            dbMemoryList.get(position).setMemory_content(et_memory_content.getText().toString());
            dbMemoryList.get(position).save();
            Toast.makeText(ModifyMemoryActivity.this, "已修改", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
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

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
