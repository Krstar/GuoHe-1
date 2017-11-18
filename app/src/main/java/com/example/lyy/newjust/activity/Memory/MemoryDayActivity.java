package com.example.lyy.newjust.activity.Memory;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.text.format.Time;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.example.lyy.newjust.R;
import com.example.lyy.newjust.adapter.Memory;
import com.example.lyy.newjust.adapter.MemoryAdapter;
import com.example.lyy.newjust.db.DBMemory;
import com.githang.statusbar.StatusBarCompat;
import com.umeng.analytics.MobclickAgent;

import org.litepal.crud.DataSupport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lib.homhomlib.design.SlidingLayout;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class MemoryDayActivity extends SwipeBackActivity {

    private Context context;

    private List<Memory> memoryList = new ArrayList<>();

    private static final String TAG = "MemoryDayActivity";

    private Time time = new Time("GMT+8");

    private int year, month, date;

    private MemoryAdapter adapter;

    private SwipeMenuListView listView;

    private List<DBMemory> dbMemoryList;

    private SlidingLayout s;

    private String[] colors = null;

    private List<Integer> imageResources = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_day);

        StatusBarCompat.setStatusBarColor(this, Color.rgb(255,255,255));

        colors = this.getResources().getStringArray(R.array.color_arr);

        for (int i = 0; i < colors.length; i++) {
            imageResources.add(Color.parseColor(colors[i]));
        }

        context = this;
        time.setToNow();

        setSwipeBackEnable(true);   // 可以调用该方法，设置是否允许滑动退出
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        // 设置滑动方向，可设置EDGE_LEFT, EDGE_RIGHT, EDGE_ALL, EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        // 滑动退出的效果只能从边界滑动才有效果，如果要扩大touch的范围，可以调用这个方法
        mSwipeBackLayout.setEdgeSize(200);

        //设置和toolbar相关的
        Toolbar toolbar = (Toolbar) findViewById(R.id.memory_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        s = (SlidingLayout) findViewById(R.id.slideLayout);
        s.setSlidingOffset(0.6f);

        initData();
        initSwipeMenuList();

    }

    private void initSwipeMenuList() {
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem openItem = new SwipeMenuItem(context);
                openItem.setBackground(new ColorDrawable(Color.rgb(189, 189, 189)));
                openItem.setWidth(180);
                openItem.setTitle("编辑");
                openItem.setTitleSize(20);
                openItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(openItem);

                SwipeMenuItem deleteItem = new SwipeMenuItem(context);
                deleteItem.setBackground(new ColorDrawable(Color.rgb(255, 82, 82)));
                deleteItem.setWidth(180);
                deleteItem.setTitle("删除");
                deleteItem.setTitleSize(20);
                deleteItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(deleteItem);
            }
        };

        listView = (SwipeMenuListView) findViewById(R.id.swipeMenuListView);
        adapter = new MemoryAdapter(MemoryDayActivity.this, R.layout.item_memory, memoryList);
        listView.setAdapter(adapter);
        listView.setMenuCreator(creator);

        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                //index的值就是在SwipeMenu依次添加SwipeMenuItem顺序值，类似数组的下标。
                //从0开始，依次是：0、1、2、3...
                switch (index) {
                    case 0:
                        modifyItem(position);
                        break;

                    case 1:
                        Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show();
                        deleteItem(position);
                        memoryList.clear();
                        initData();
                        initSwipeMenuList();
                        break;
                }
                // false : 当用户触发其他地方的屏幕时候，自动收起菜单。
                // true : 不改变已经打开菜单的样式，保持原样不收起。
                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String memory_date = dbMemoryList.get(position).getMemory_day();
                String memory_content = dbMemoryList.get(position).getMemory_content();
                Intent intent = new Intent(MemoryDayActivity.this, MemoryDetailActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("memory_date", memory_date);
                intent.putExtra("memory_content", memory_content);
                startActivity(intent);
            }
        });

        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int localWigth = 0;
                int localHeigth = 0;
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        localWigth = (int) motionEvent.getX();
                        localHeigth = (int) motionEvent.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int sx = (int) motionEvent.getX();
                        if (Math.abs(localWigth - sx) > 30) {
                            s.requestDisallowInterceptTouchEvent(true);
                        } else {
                            s.requestDisallowInterceptTouchEvent(false);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        s.requestDisallowInterceptTouchEvent(false);
                        localWigth = 0;
                        localHeigth = 0;
                        break;
                }
                return false;
            }
        });

        // 监测用户在ListView的SwipeMenu侧滑事件。
        listView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int pos) {
                Log.d("位置:" + pos, "开始侧滑...");
            }

            @Override
            public void onSwipeEnd(int pos) {
                Log.d("位置:" + pos, "侧滑结束.");
            }
        });
    }

    private void modifyItem(int position) {
        String memory_date = dbMemoryList.get(position).getMemory_day();
        String memory_content = dbMemoryList.get(position).getMemory_content();
        Intent intent = new Intent(this, ModifyMemoryActivity.class);
        intent.putExtra("position", position);
        intent.putExtra("flag", "modify");
        intent.putExtra("memory_date", memory_date);
        intent.putExtra("memory_content", memory_content);
        startActivity(intent);
    }

    private void deleteItem(int position) {
        dbMemoryList = DataSupport.findAll(DBMemory.class);
        for (int i = 0; i < dbMemoryList.size(); i++) {
            dbMemoryList.get(position).delete();
        }
        String today = year + "年" + (month + 1) + "月" + date + "日";
        memoryList.clear();
        for (DBMemory dbMemory : dbMemoryList) {
            int i = (int) (Math.random() * 150);
            String daysBetween = daysOfTwo_2(today, dbMemory.getMemory_day());
            Memory memory = new Memory(dbMemory.getMemory_content(), daysBetween, imageResources.get(i));
            memoryList.add(memory);
        }
    }

    private void initData() {
        year = time.year;
        month = time.month;
        date = time.monthDay;
        memoryList.clear();
        String today = year + "年" + (month + 1) + "月" + date + "日";
        dbMemoryList = DataSupport.findAll(DBMemory.class);
        for (DBMemory dbMemory : dbMemoryList) {
            int i = (int) (Math.random() * 150);
            String daysBetween = daysOfTwo_2(today, dbMemory.getMemory_day());
            Memory memory = new Memory(dbMemory.getMemory_content(), daysBetween, imageResources.get(i));
            memoryList.add(memory);
        }
    }

    //判断两个时间段内的天数差
    private String daysOfTwo_2(String day1, String day2) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
            //跨年不会出现问题
            //如果时间为：2016-03-18 11:59:59 和 2016-03-19 00:00:01的话差值为 0
            Date fDate = sdf.parse(day1);
            Date oDate = sdf.parse(day2);
            long days = (oDate.getTime() - fDate.getTime()) / (1000 * 3600 * 24);
            if (days < 0) {
                days = days * (-1);
                return (days + "+");
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
            case R.id.action_add:
                Intent addIntent = new Intent(MemoryDayActivity.this, ModifyMemoryActivity.class);
                addIntent.putExtra("flag", "add");
                startActivity(addIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_memory, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        memoryList.clear();
        initData();
        initSwipeMenuList();
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
