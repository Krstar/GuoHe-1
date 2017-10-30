package com.example.lyy.newjust.activity.Memory;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.lyy.newjust.R;
import com.example.lyy.newjust.db.DBMemory;
import com.example.lyy.newjust.util.HttpUtil;
import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.ActionSheetDialog;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MemoryDetailActivity extends SwipeBackActivity {

    private static final String TAG = "MemoryDetailActivity";

    private ImageView bingPicImg;

    private int position;

    private TextView tv_memory_date;
    private TextView tv_memory_content;
    private TextView tv_between;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉Activity上面的状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
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

        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);

        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        String bingPic = sharedPreferences.getString("head_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).crossFade().into(bingPicImg);
        } else {
            loadBingPic();
        }

        bingPicImg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final String[] stringItems = {"分享", "编辑", "删除"};
                final ActionSheetDialog dialog = new ActionSheetDialog(MemoryDetailActivity.this, stringItems, null);
                dialog.isTitleShow(false).show();
                dialog.setOnOperItemClickL(new OnOperItemClickL() {
                    @Override
                    public void onOperItemClick(AdapterView<?> parent, View view, int pid, long id) {
                        switch (pid) {
                            case 0:
                                Toast.makeText(getApplicationContext(), "分享", Toast.LENGTH_SHORT).show();
                                break;
                            case 1:
                                modifyItem(position);
                                break;
                            case 2:
                                Toast.makeText(getApplicationContext(), "删除", Toast.LENGTH_SHORT).show();
                                deleteItem(position);
                                break;
                        }
                        dialog.dismiss();
                    }
                });

                return true;
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

    //加载每日一图
    private void loadBingPic() {
        String requestBingPic = "http://120.25.88.41/just/img";
        HttpUtil.sendHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String head_pic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MemoryDetailActivity.this).edit();
                editor.putString("head_pic", head_pic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: ");
                        Glide.with(MemoryDetailActivity.this).load(head_pic).into(bingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
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
                tv_days.setText("DAY");
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
