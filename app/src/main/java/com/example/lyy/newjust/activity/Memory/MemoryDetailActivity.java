package com.example.lyy.newjust.activity.Memory;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.lyy.newjust.R;
import com.example.lyy.newjust.db.DBMemory;
import com.githang.statusbar.StatusBarCompat;

import org.litepal.crud.DataSupport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class MemoryDetailActivity extends SwipeBackActivity {

    private int position;

    private TextView tv_memory_date;
    private TextView tv_memory_content;
    private TextView tv_between;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setStatusBarColor(this, Color.rgb(0, 172, 193));

        setContentView(R.layout.activity_memory_detail);

        init();
    }

    private void init() {
        setSwipeBackEnable(true);   // 可以调用该方法，设置是否允许滑动退出
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        // 设置滑动方向，可设置EDGE_LEFT, EDGE_RIGHT, EDGE_ALL, EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        // 滑动退出的效果只能从边界滑动才有效果，如果要扩大touch的范围，可以调用这个方法
        mSwipeBackLayout.setEdgeSize(200);

        //设置和toolbar相关的
        Toolbar toolbar = (Toolbar) findViewById(R.id.memory_detail_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        Time time = new Time("GMT+8");
        time.setToNow();
        int year = time.year;
        int month = time.month;
        int date = time.monthDay;

        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0);
        String memory_date = intent.getStringExtra("memory_date");
        String memory_content = intent.getStringExtra("memory_content");

        String today = year + "年" + (month + 1) + "月" + date + "日";
        String daysBetween = daysOfTwo_2(today, memory_date);

        tv_memory_date = (TextView) findViewById(R.id.tv_memory_date);
        tv_memory_content = (TextView) findViewById(R.id.tv_memory_content);
        tv_between = (TextView) findViewById(R.id.tv_between);

        tv_memory_date.setText(memory_date);
        tv_memory_content.setText(memory_content);
        tv_between.setText(daysBetween);

        CardView card_modify = (CardView) findViewById(R.id.card_modify);
        CardView card_delete = (CardView) findViewById(R.id.card_delete);

        card_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyItem(position);
            }
        });
        card_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItem(position);
            }
        });
    }

    private void deleteItem(int position) {
        DBMemory memory = DBMemory.findAll(DBMemory.class).get(position);
        if (memory.isSaved()) {
            memory.delete();
        }
        this.finish();
    }

    private void modifyItem(int position) {
        List<DBMemory> dbMemoryList = DataSupport.findAll(DBMemory.class);
        String memory_date = dbMemoryList.get(position).getMemory_day();
        String memory_content = dbMemoryList.get(position).getMemory_content();
        Intent intent = new Intent(this, ModifyMemoryActivity.class);
        intent.putExtra("position", position);
        intent.putExtra("flag", "modify");
        intent.putExtra("memory_date", memory_date);
        intent.putExtra("memory_content", memory_content);
        startActivity(intent);
    }

    //判断两个时间段内的天数差
    private String daysOfTwo_2(String day1, String day2) {
        try {
            TextView tv_days = (TextView) findViewById(R.id.tv_days);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
            //跨年不会出现问题
            //如果时间为：2016-03-18 11:59:59 和 2016-03-19 00:00:01的话差值为 0
            Date fDate = sdf.parse(day1);
            Date oDate = sdf.parse(day2);
            long days = (oDate.getTime() - fDate.getTime()) / (1000 * 3600 * 24);
            if (days < 0) {
                days = days * (-1);
            } else if (days == 0) {
                tv_days.setText("天");
            }
            return (days + "");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Time time = new Time("GMT+8");
        time.setToNow();
        int year = time.year;
        int month = time.month;
        int date = time.monthDay;

        DBMemory memory = DBMemory.findAll(DBMemory.class).get(position);
        String today = year + "年" + (month + 1) + "月" + date + "日";
        String daysBetween = daysOfTwo_2(today, memory.getMemory_day());
        tv_memory_date.setText(memory.getMemory_day());
        tv_memory_content.setText(memory.getMemory_content());
        tv_between.setText(daysBetween);
    }
}
