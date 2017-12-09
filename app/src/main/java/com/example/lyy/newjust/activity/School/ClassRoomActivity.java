package com.example.lyy.newjust.activity.School;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.lyy.newjust.R;
import com.example.lyy.newjust.adapter.ClassRoom;
import com.example.lyy.newjust.adapter.ClassRoomAdapter;
import com.example.lyy.newjust.model.Res;
import com.example.lyy.newjust.util.AppConstants;
import com.example.lyy.newjust.util.HttpUtil;
import com.example.lyy.newjust.util.ResponseUtil;
import com.example.lyy.newjust.util.SpUtils;
import com.example.lyy.newjust.util.UrlUtil;
import com.githang.statusbar.StatusBarCompat;

import org.angmarch.views.NiceSpinner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ClassRoomActivity extends SwipeBackActivity implements View.OnClickListener {

    private static final String TAG = "ClassRoomActivity";

    private Context mContext;

    private Button btn_classroom_search;

    private NiceSpinner niceSpinner1, niceSpinner2, niceSpinner3, niceSpinner4;

    private ListView lv_classroom;

    private List<ClassRoom> classRoomList = new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;

    private SwipeRefreshLayout.OnRefreshListener listener;

    //校区集合
    List<String> dataset1 = new LinkedList<>(Arrays.asList("东校区", "南校区", "西校区", "张家港", "苏州理工"));

    //东校区
    List<String> dataset2 = new LinkedList<>(Arrays.asList("综合楼B", "综合楼C", "综合楼D", "教三", "教四", "实验楼11", "计算中心"));
    //南校区
    List<String> dataset3 = new LinkedList<>(Arrays.asList("第一综合楼", "A楼", "实验楼", "第二综合楼"));
    //西校区
    List<String> dataset4 = new LinkedList<>(Arrays.asList("西综", "图书馆"));
    //张家港校区
    List<String> dataset5 = new LinkedList<>(Arrays.asList("教学楼E", "教学楼F"));
    //苏州理工
    List<String> dataset6 = new LinkedList<>(Arrays.asList("教学楼A", "教学楼B", "教学楼C", "教学楼D", "外语楼", "经管数理信息楼", "船海土木楼"));

    //周数集合
    List<String> dataset7 = new LinkedList<>(Arrays.asList("第一周", "第二周", "第三周", "第四周", "第五周", "第六周", "第七周", "第八周", "第九周", "第十周", "第十一周", "第十二周", "第十三周", "第十四周", "第十五周", "第十六周", "第十七周", "第十八周", "第十九周", "第二十周"));

    //星期集合
    List<String> dataset8 = new LinkedList<>(Arrays.asList("周一", "周二", "周三", "周四", "周五", "周六", "周日"));

    private String area_id = "01", building_id = "2", zc1 = "0", Weekday = "Mon";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setStatusBarColor(this, Color.rgb(0, 172, 193));
        setContentView(R.layout.activity_class_room);

        mContext = this;

        setSwipeBackEnable(true);   // 可以调用该方法，设置是否允许滑动退出
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        // 设置滑动方向，可设置EDGE_LEFT, EDGE_RIGHT, EDGE_ALL, EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        // 滑动退出的效果只能从边界滑动才有效果，如果要扩大touch的范围，可以调用这个方法
        mSwipeBackLayout.setEdgeSize(100);

        //设置和toolbar相关的
        Toolbar toolbar = (Toolbar) findViewById(R.id.classroom_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        lv_classroom = (ListView) findViewById(R.id.lv_classroom);

        btn_classroom_search = (Button) findViewById(R.id.btn_classroom_search);
        btn_classroom_search.setOnClickListener(this);
        initSpinner();
        initSwipeRefresh();
    }

    private void initSpinner() {
        niceSpinner1 = (NiceSpinner) findViewById(R.id.niceSpinner1);
        niceSpinner2 = (NiceSpinner) findViewById(R.id.niceSpinner2);
        niceSpinner3 = (NiceSpinner) findViewById(R.id.niceSpinner3);
        niceSpinner4 = (NiceSpinner) findViewById(R.id.niceSpinner4);


        niceSpinner1.attachDataSource(dataset1);
        niceSpinner2.attachDataSource(dataset2);
        niceSpinner3.attachDataSource(dataset7);
        niceSpinner4.attachDataSource(dataset8);

        niceSpinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        area_id = "01";
                        niceSpinner2.attachDataSource(dataset2);
                        building_id = "2";
                        break;
                    case 1:
                        area_id = "02";
                        niceSpinner2.attachDataSource(dataset3);
                        building_id = "12";
                        break;
                    case 2:
                        area_id = "03";
                        niceSpinner2.attachDataSource(dataset4);
                        building_id = "10";
                        break;
                    case 3:
                        area_id = "04";
                        niceSpinner2.attachDataSource(dataset5);
                        building_id = "26";
                        break;
                    case 4:
                        area_id = "05";
                        niceSpinner2.attachDataSource(dataset6);
                        building_id = "18";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                area_id = "01";
                niceSpinner2.attachDataSource(dataset2);
            }
        });

        niceSpinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (area_id) {
                    case "01":
                        //东校区
                        switch (position) {
                            case 0:
                                building_id = "2";
                                break;
                            case 1:
                                building_id = "3";
                                break;
                            case 2:
                                building_id = "4";
                                break;
                            case 3:
                                building_id = "5";
                                break;
                            case 4:
                                building_id = "6";
                                break;
                            case 5:
                                building_id = "7";
                                break;
                            case 6:
                                building_id = "15";
                                break;
                        }
                        break;
                    case "02":
                        //南校区
                        switch (position) {
                            case 0:
                                building_id = "12";
                                break;
                            case 1:
                                building_id = "13";
                                break;
                            case 2:
                                building_id = "16";
                                break;
                            case 3:
                                building_id = "23";
                                break;
                        }
                        break;
                    case "03":
                        //西校区
                        switch (position) {
                            case 0:
                                building_id = "10";
                                break;
                            case 1:
                                building_id = "11";
                                break;
                        }
                        break;
                    case "04":
                        //张家港
                        switch (position) {
                            case 0:
                                building_id = "26";
                                break;
                            case 1:
                                building_id = "27";
                                break;
                        }
                        break;
                    case "05":
                        //苏州理工
                        switch (position) {
                            case 0:
                                building_id = "18";
                                break;
                            case 1:
                                building_id = "19";
                                break;
                            case 2:
                                building_id = "20";
                                break;
                            case 3:
                                building_id = "21";
                                break;
                            case 4:
                                building_id = "22";
                                break;
                            case 5:
                                building_id = "24";
                                break;
                            case 6:
                                building_id = "25";
                                break;
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                switch (area_id) {
                    case "01":
                        //东校区
                        building_id = "2";
                        break;
                    case "02":
                        //南校区
                        building_id = "12";
                        break;
                    case "03":
                        //西校区
                        building_id = "10";
                        break;
                    case "04":
                        //张家港
                        building_id = "26";
                        break;
                    case "05":
                        //苏州理工
                        building_id = "18";
                        break;
                }
            }
        });

        niceSpinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                zc1 = position + "";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                zc1 = "0";
            }
        });

        niceSpinner4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Weekday = "Mon";
                        break;
                    case 1:
                        Weekday = "Tue";
                        break;
                    case 2:
                        Weekday = "Wedn";
                        break;
                    case 3:
                        Weekday = "Thur";
                        break;
                    case 4:
                        Weekday = "Fri";
                        break;
                    case 5:
                        Weekday = "Sat";
                        break;
                    case 6:
                        Weekday = "Sun";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void requestClassroom() {
        classRoomList.clear();
        String user = SpUtils.getString(mContext, AppConstants.STU_ID);
        String pass = SpUtils.getString(mContext, AppConstants.STU_PASS);
        String url = UrlUtil.CLASSROOM;
        final RequestBody requestBody = new FormBody.Builder()
                .add("username", user)
                .add("password", pass)
                .add("school_year", AppConstants.THIS_YEAR)
                .add("area_id", area_id)
                .add("building_id", building_id)
                .add("zc1", zc1)
                .build();

        HttpUtil.sendPostHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.post(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
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
                        try {
                            JSONArray array = new JSONArray(res.getInfo());
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object = array.getJSONObject(i);
                                String weekday = object.getString("weekday");
                                String time = object.getString("time");
                                String place = object.getString("place");

                                ClassRoom classRoom = new ClassRoom(weekday, time, place);
                                if (Weekday.equals(weekday)) {
                                    classRoomList.add(classRoom);
                                }
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ClassRoomAdapter classRoomAdapter = new ClassRoomAdapter(ClassRoomActivity.this, R.layout.item_classroom, classRoomList);
                                    lv_classroom.setAdapter(classRoomAdapter);
                                }
                            });
                            swipeRefreshLayout.post(new Runnable() {
                                @Override
                                public void run() {
                                    swipeRefreshLayout.setRefreshing(false);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Looper.prepare();
                        swipeRefreshLayout.post(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                        Toast.makeText(mContext, res.getMsg(), Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.post(new Runnable() {
                                @Override
                                public void run() {
                                    swipeRefreshLayout.setRefreshing(false);
                                }
                            });
                            Toast.makeText(mContext, "错误" + response.code() + "，请稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void initSwipeRefresh() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.classroom_refresh);

        // 设置颜色属性的时候一定要注意是引用了资源文件还是直接设置16进制的颜色，因为都是int值容易搞混
        // 设置下拉进度的背景颜色，默认就是白色的
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        // 设置下拉进度的主题颜色
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);

        listener = new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                //TODO
                requestClassroom();
            }
        };

        swipeRefreshLayout.setOnRefreshListener(listener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_classroom_search:
                classRoomList.clear();

                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(true);
                    }
                });
                listener.onRefresh();
                break;
        }
    }
}
