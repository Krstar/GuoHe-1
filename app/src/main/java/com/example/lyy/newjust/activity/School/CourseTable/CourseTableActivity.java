package com.example.lyy.newjust.activity.School.CourseTable;

import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lyy.newjust.R;
import com.githang.statusbar.StatusBarCompat;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class CourseTableActivity extends SwipeBackActivity {

    private CourseTableView courseTableView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_table);

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

        courseTableView = (CourseTableView) findViewById(R.id.ctv);
        List<Course> list = new ArrayList<>();
        Course c1 = new Course();
        c1.setDay(1);
        c1.setDes("第一节课");
        c1.setJieci(1);
        list.add(c1);

        Course c2 = new Course();
        c2.setDay(2);
        c2.setDes("第二节课");
        c2.setJieci(6);
        list.add(c2);

        Course c3 = new Course();
        c3.setDay(3);
        c3.setDes("第三节课");
        c3.setJieci(6);
        list.add(c3);

        Course c4 = new Course();
        c4.setDay(5);
        c4.setDes("第四节课");
        c4.setJieci(3);
        list.add(c4);

        Course c5 = new Course();
        c5.setDay(5);
        c5.setDes("第四节课");
        c5.setJieci(1);
        list.add(c5);

        courseTableView.updateCourseViews(list);
        courseTableView.setOnCourseItemClickListener(new CourseTableView.OnCourseItemClickListener() {
            @Override
            public void onCourseItemClick(TextView tv, int jieci, int day, String des) {
                String string = tv.getText().toString();
                Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
            }
        });
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

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
