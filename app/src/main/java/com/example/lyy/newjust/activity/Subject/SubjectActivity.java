package com.example.lyy.newjust.activity.Subject;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lyy.newjust.R;
import com.example.lyy.newjust.adapter.Point;
import com.example.lyy.newjust.adapter.PointAdapter;
import com.example.lyy.newjust.adapter.Subject;
import com.example.lyy.newjust.adapter.SubjectAdapter;
import com.example.lyy.newjust.db.DBSubject;
import com.example.lyy.newjust.gson.g_Subject;
import com.example.lyy.newjust.util.HttpUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.neokree.materialtabs.MaterialTab;
import it.neokree.materialtabs.MaterialTabHost;
import it.neokree.materialtabs.MaterialTabListener;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SubjectActivity extends SwipeBackActivity implements MaterialTabListener {

    private static final String TAG = "SubjectActivity";


    MaterialTabHost tabHost;
    ViewPager pager;
    ViewPagerAdapter adapter;

    private List<Subject> adapter_list_kaoshi;
    private List<Subject> adapter_list_kaocha;

    private List<DBSubject> dbSubjectList;

    private TextView tv_all_point;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉Activity上面的状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_subject);

        //设置状态栏和toolbar颜色一致
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }

        setSwipeBackEnable(true);   // 可以调用该方法，设置是否允许滑动退出
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        // 设置滑动方向，可设置EDGE_LEFT, EDGE_RIGHT, EDGE_ALL, EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        // 滑动退出的效果只能从边界滑动才有效果，如果要扩大touch的范围，可以调用这个方法
        mSwipeBackLayout.setEdgeSize(200);

        searchScoreRequest();
        searchPointRequest();
        init();
    }

    private void init() {

        adapter_list_kaoshi = new ArrayList<>();
        adapter_list_kaocha = new ArrayList<>();
        dbSubjectList = new ArrayList<>();

        tv_all_point = (TextView) findViewById(R.id.tv_all_point);

        Toolbar subject_toolbar = (Toolbar) findViewById(R.id.subject_toolbar);
        setSupportActionBar(subject_toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_back_blue);
        }

        tabHost = (MaterialTabHost) findViewById(R.id.tabHost);
        pager = (ViewPager) findViewById(R.id.pager);

        // init view pager
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // when user do a swipe the selected tab change
                tabHost.setSelectedNavigationItem(position);
            }
        });

        // insert all tabs from pagerAdapter data
        for (int i = 0; i < adapter.getCount(); i++) {
            tabHost.addTab(
                    tabHost.newTab()
                            .setText(adapter.getPageTitle(i))
                            .setTabListener(this)
            );
        }
    }

    private void searchPointRequest() {
        String pointUrl = "http://120.25.88.41/point";
        HttpUtil.sendHttpRequest(pointUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                showPointResult(responseText);
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
                    PointAdapter adapter = new PointAdapter(SubjectActivity.this, R.layout.item_point, pointList);
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
        String url = "http://120.25.88.41";

        HttpUtil.sendHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                parseJSONData(responseText);
            }
        });
    }

    //对服务器响应的数据进行接收同时保存到数据库中
    private void parseJSONData(String response) {
        if (!TextUtils.isEmpty(response)) {
            Gson gson = new Gson();
            List<g_Subject> gSubjectList = gson.fromJson(response, new TypeToken<List<g_Subject>>() {
            }.getType());
            for (int i = 0; i < gSubjectList.size(); i++) {
                DBSubject dbSubject = new DBSubject();
                dbSubject.setCourse_name(gSubjectList.get(i).getCourse_name());
                dbSubject.setCredit(gSubjectList.get(i).getCredit());
                dbSubject.setExamination_method(gSubjectList.get(i).getExamination_method());
                dbSubject.setScore(gSubjectList.get(i).getScore());
                dbSubject.setStart_semester(gSubjectList.get(i).getStart_semester());
                dbSubject.save();
                dbSubjectList.add(dbSubject);
            }
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
            }
        }
    }

    //显示考试课结果
    private void showScoreResult(List<Subject> subjects_List) {
        if (subjects_List.size() != 0) {
            SubjectAdapter subjectAdapter = new SubjectAdapter(SubjectActivity.this, R.layout.item_subjects, subjects_List);
            FragmentLayout fragment = adapter.getCurrentFragment();
            View view = fragment.getView();
            ListView listView = view.findViewById(R.id.subject_list_item);
            listView.setAdapter(subjectAdapter);
        }

    }

    @Override
    public void onTabSelected(MaterialTab tab) {
        pager.setCurrentItem(tab.getPosition());
        if (tab.getPosition() == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showScoreResult(adapter_list_kaoshi);
                }
            });
        } else if (tab.getPosition() == 1) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showScoreResult(adapter_list_kaocha);
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "出现了一些问题", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTabReselected(MaterialTab tab) {

    }

    @Override
    public void onTabUnselected(MaterialTab tab) {

    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        private FragmentLayout mCurrentFragment;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fragment getItem(int num) {
            return new FragmentLayout();
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "考试课";
            } else {
                return "考查课";
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //方法体中什么也不用写
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return super.instantiateItem(container, position);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            mCurrentFragment = (FragmentLayout) object;
            if (position == 0) {
                showScoreResult(adapter_list_kaoshi);
            } else if (position == 1) {
                showScoreResult(adapter_list_kaocha);
            }
            super.setPrimaryItem(container, position, object);
        }

        public FragmentLayout getCurrentFragment() {
            return mCurrentFragment;
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
    protected void onDestroy() {
        super.onDestroy();
        DataSupport.deleteAll(DBSubject.class);
    }

    @Override
    protected void onRestart() {
        searchScoreRequest();
        super.onRestart();
    }
}
