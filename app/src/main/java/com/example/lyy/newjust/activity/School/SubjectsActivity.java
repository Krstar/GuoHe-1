package com.example.lyy.newjust.activity.School;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lyy.newjust.R;
import com.example.lyy.newjust.adapter.Point;
import com.example.lyy.newjust.adapter.PointAdapter;
import com.example.lyy.newjust.adapter.Subject;
import com.example.lyy.newjust.adapter.SubjectAdapter;
import com.example.lyy.newjust.gson.g_Subject;
import com.example.lyy.newjust.util.AppConstants;
import com.example.lyy.newjust.util.HttpUtil;
import com.example.lyy.newjust.util.SpUtils;
import com.example.lyy.newjust.util.UrlUtil;
import com.githang.statusbar.StatusBarCompat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import shortbread.Shortcut;

@Shortcut(id = "grade", icon = R.drawable.ic_menu_grade, shortLabel = "查成绩")
public class SubjectsActivity extends SwipeBackActivity {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    private LayoutInflater mInflater;
    private List<String> mTitleList = new ArrayList<>();//页卡标题集合
    private View view1, view2, view3;//页卡视图
    private List<View> mViewList = new ArrayList<>();//页卡视图集合

    private List<Subject> adapter_list_kaoshi;
    private List<Subject> adapter_list_kaocha;
    private List<Subject> adapter_list_others;

    private TextView tv_all_point;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setStatusBarColor(this, Color.rgb(255, 255, 255));
        setContentView(R.layout.activity_subjects);

        setSwipeBackEnable(true);   // 可以调用该方法，设置是否允许滑动退出
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        // 设置滑动方向，可设置EDGE_LEFT, EDGE_RIGHT, EDGE_ALL, EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        // 滑动退出的效果只能从边界滑动才有效果，如果要扩大touch的范围，可以调用这个方法
        mSwipeBackLayout.setEdgeSize(100);

        searchPointRequest();
        searchScoreRequest();

        init();
    }

    private void init() {
        adapter_list_kaoshi = new ArrayList<>();
        adapter_list_kaocha = new ArrayList<>();
        adapter_list_others = new ArrayList<>();

        tv_all_point = (TextView) findViewById(R.id.tv_all_point);

        Toolbar subject_toolbar = (Toolbar) findViewById(R.id.subject_toolbar);
        setSupportActionBar(subject_toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        mViewPager = (ViewPager) findViewById(R.id.vp_view);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.setOffscreenPageLimit(2);//参数为预加载数量，系统最小值为1。慎用！预加载数量过多低端机子受不了

        mInflater = LayoutInflater.from(this);
        view1 = mInflater.inflate(R.layout.fragment_layout_left, null);
        view2 = mInflater.inflate(R.layout.fragment_layout_middle, null);
        view3 = mInflater.inflate(R.layout.fragment_layout_right, null);

        //添加页卡视图
        mViewList.add(view1);
        mViewList.add(view2);
        mViewList.add(view3);

        //添加页卡标题
        mTitleList.add("考试课");
        mTitleList.add("考查课");
        mTitleList.add("其他");

        mTabLayout.setTabMode(TabLayout.MODE_FIXED);//设置tab模式，当前为系统默认模式
        mTabLayout.addTab(mTabLayout.newTab().setText(mTitleList.get(0)));//添加tab选项卡
        mTabLayout.addTab(mTabLayout.newTab().setText(mTitleList.get(1)));
        mTabLayout.addTab(mTabLayout.newTab().setText(mTitleList.get(2)));


        MyPagerAdapter mAdapter = new MyPagerAdapter(mViewList);
        mViewPager.setAdapter(mAdapter);//给ViewPager设置适配器
        mTabLayout.setupWithViewPager(mViewPager);//将TabLayout和ViewPager关联起来。

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        showLeftScoreResult();
                        break;
                    case 1:
                        showMiddleScoreResult();
                        break;
                    case 2:
                        showRightScoreResult();
                        break;
                    default:
                        break;
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void searchPointRequest() {
        final ProgressDialog dialog = ProgressDialog.show(SubjectsActivity.this, null, "绩点导入中,请稍后……", true, false);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        String pointUrl = UrlUtil.STU_GPA;
        String username = SpUtils.getString(getApplicationContext(), AppConstants.STU_ID);
        String password = SpUtils.getString(getApplicationContext(), AppConstants.STU_PASS);
        RequestBody requestBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();
        HttpUtil.sendPostHttpRequest(pointUrl, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                dialog.dismiss();

                Looper.prepare();
                Toast.makeText(getApplicationContext(), "服务器异常，请稍后重试", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseText = response.body().string();
                    showPointResult(responseText);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                        }
                    });
                }
            }
        });
    }

    private void showPointResult(String responseText) {
        try {
            final List<Point> pointList = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(responseText);
            JSONObject object = jsonArray.getJSONObject(0);
            final String all_point = object.getString("point");
            for (int i = 1; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String every_year = jsonObject.getString("year");
                String every_point = jsonObject.getString("point");
                pointList.add(new Point(every_year, every_point));
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ListView list_every_year_point = (ListView) findViewById(R.id.list_every_year_point);
                    PointAdapter adapter = new PointAdapter(SubjectsActivity.this, R.layout.item_point, pointList);
                    list_every_year_point.setAdapter(adapter);
                    tv_all_point.setText(all_point);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //发出分数查询的请求
    private void searchScoreRequest() {
        mProgressDialog = ProgressDialog.show(SubjectsActivity.this, null, "成绩导入中,请稍后……", true, false);
        mProgressDialog.setCanceledOnTouchOutside(true);
        mProgressDialog.setCancelable(true);
        String url = UrlUtil.STU_SCORE;

        String username = SpUtils.getString(getApplicationContext(), AppConstants.STU_ID);
        String password = SpUtils.getString(getApplicationContext(), AppConstants.STU_PASS);

        RequestBody requestBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();
        HttpUtil.sendPostHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mProgressDialog.dismiss();

                Looper.prepare();
                Toast.makeText(getApplicationContext(), "服务器异常，请稍后重试", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseText = response.body().string();
                    handleScoreResponse(responseText);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.dismiss();
                        }
                    });
                }
            }
        });
    }

    //对服务器响应的数据进行接收
    private void handleScoreResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            Gson gson = new Gson();
            List<g_Subject> gSubjectList = gson.fromJson(response, new TypeToken<List<g_Subject>>() {
            }.getType());
            chooseResult(gSubjectList);
        } else {
            Toast.makeText(getApplicationContext(), "服务器没有响应", Toast.LENGTH_SHORT).show();
        }
    }

    private void chooseResult(List<g_Subject> gSubjectList) {
        for (int i = 0; i < gSubjectList.size(); i++) {
            if (gSubjectList.get(i).getExamination_method().equals("考试")) {
                Subject subject = new Subject(gSubjectList.get(i).getCourse_name(), gSubjectList.get(i).getCredit(), gSubjectList.get(i).getScore());
                adapter_list_kaoshi.add(subject);
            } else if (gSubjectList.get(i).getExamination_method().equals("考查")) {
                Subject subject = new Subject(gSubjectList.get(i).getCourse_name(), gSubjectList.get(i).getCredit(), gSubjectList.get(i).getScore());
                adapter_list_kaocha.add(subject);
            } else {
                Subject subject = new Subject(gSubjectList.get(i).getCourse_name(), gSubjectList.get(i).getCredit(), gSubjectList.get(i).getScore());
                adapter_list_others.add(subject);
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showLeftScoreResult();
            }
        });

    }

    //显示考试课结果
    private void showLeftScoreResult() {
        if (adapter_list_kaoshi.size() != 0) {
            SubjectAdapter subjectAdapter = new SubjectAdapter(SubjectsActivity.this, R.layout.item_subjects, adapter_list_kaoshi);
            ListView listView = (ListView) findViewById(R.id.left_subject_list_item);
            listView.setAdapter(subjectAdapter);
        }
    }

    //显示考查课结果
    private void showMiddleScoreResult() {
        if (adapter_list_kaocha.size() != 0) {
            SubjectAdapter subjectAdapter = new SubjectAdapter(SubjectsActivity.this, R.layout.item_subjects, adapter_list_kaocha);
            ListView listView = (ListView) findViewById(R.id.middle_subject_list_item);
            listView.setAdapter(subjectAdapter);
        }
    }

    //显示其他类型的考试的结果
    private void showRightScoreResult() {
        if (adapter_list_others.size() != 0) {
            SubjectAdapter subjectAdapter = new SubjectAdapter(SubjectsActivity.this, R.layout.item_subjects, adapter_list_others);
            ListView listView = (ListView) findViewById(R.id.right_subject_list_item);
            listView.setAdapter(subjectAdapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_switch_subjects, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_switch:
                Intent in = new Intent(SubjectsActivity.this, NewSubjectActivity.class);
                startActivity(in);
                break;
        }
        return true;
    }

    //ViewPager适配器
    class MyPagerAdapter extends PagerAdapter {
        private List<View> mViewList;

        public MyPagerAdapter(List<View> mViewList) {
            this.mViewList = mViewList;
        }

        @Override
        public int getCount() {
            return mViewList.size();//页卡数
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;//官方推荐写法
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mViewList.get(position));//添加页卡
            return mViewList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mViewList.get(position));//删除页卡
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitleList.get(position);//页卡标题
        }
    }

}
