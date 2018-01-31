package com.lyy.guohe.activity.School;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.githang.statusbar.StatusBarCompat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lyy.guohe.R;
import com.lyy.guohe.adapter.Subject;
import com.lyy.guohe.adapter.SubjectAdapter;
import com.lyy.guohe.base.MyListView;
import com.lyy.guohe.gson.g_Subject;
import com.lyy.guohe.model.Res;
import com.lyy.guohe.util.AppConstants;
import com.lyy.guohe.util.HttpUtil;
import com.lyy.guohe.util.ResponseUtil;
import com.lyy.guohe.util.SpUtils;
import com.lyy.guohe.util.UrlUtil;

import org.angmarch.views.NiceSpinner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NewSubjectActivity extends SwipeBackActivity {

    private Context mContext;

    private MyListView listView;
    private NiceSpinner spinner_year;
    private ProgressDialog mProgressDialog;

    private SubjectAdapter subjectAdapter;

    private List<Subject> subjectList;  //成绩集合
    private List<String> all_year_list; //所有学年的集合
    private List<g_Subject> gSubjectList;

    private String stu_id;
    private String stu_pass;

    private LineChartView lineChart;

    private TextView tv_GPA_ALL;


    List<String> dateList = new ArrayList<>();
    List<Float> gpaList = new ArrayList<>();

    private List<PointValue> mPointValues = new ArrayList<PointValue>();
    private List<AxisValue> mAxisXValues = new ArrayList<AxisValue>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setStatusBarColor(this, Color.rgb(255, 255, 255));
        setContentView(R.layout.activity_new_subject);

        mContext = this;

        setSwipeBackEnable(true);   // 可以调用该方法，设置是否允许滑动退出
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        // 设置滑动方向，可设置EDGE_LEFT, EDGE_RIGHT, EDGE_ALL, EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        // 滑动退出的效果只能从边界滑动才有效果，如果要扩大touch的范围，可以调用这个方法
        mSwipeBackLayout.setEdgeSize(100);

        //设置和toolbar相关的
        Toolbar toolbar = (Toolbar) findViewById(R.id.new_subject_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        listView = (MyListView) findViewById(R.id.lv_subject_list);
        spinner_year = (NiceSpinner) findViewById(R.id.spinner_year);
        lineChart = (LineChartView) findViewById(R.id.chart);
        tv_GPA_ALL = (TextView) findViewById(R.id.tv_GPA_ALL);

        mProgressDialog = new ProgressDialog(NewSubjectActivity.this);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(true);

        subjectList = new ArrayList<>();
        all_year_list = new ArrayList<>();

        stu_id = SpUtils.getString(mContext, AppConstants.STU_ID);
        stu_pass = SpUtils.getString(mContext, AppConstants.STU_PASS);

        requestXiaoLi();
    }

    /**
     * 初始化LineChart的一些设置
     */
    private void initLineChart() {
        Line line = new Line(mPointValues).setColor(Color.parseColor("#87CEFA"));  //折线的颜色
        List<Line> lines = new ArrayList<Line>();
        line.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.SQUARE）
        line.setCubic(true);//曲线是否平滑
        line.setStrokeWidth(1);//线条的粗细，默认是3
        line.setFilled(false);//是否填充曲线的面积
        line.setHasLabels(true);//曲线的数据坐标是否加上备注
        line.setPointRadius(5);
//        line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        line.setHasLines(true);//是否用直线显示。如果为false 则没有曲线只有点显示
        line.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);

        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(false);  //X轴下面坐标轴字体是斜的显示还是直的，true是斜的显示
//	    axisX.setTextColor(Color.WHITE);  //设置字体颜色
        axisX.setTextColor(Color.parseColor("#000000"));//灰色

//        axisX.setName("GPA走向图");  //表格名称
        axisX.setTextSize(13);//设置字体大小
//        axisX.setMaxLabelChars(7); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisValues.length
        axisX.setValues(mAxisXValues);  //填充X轴的坐标名称
        data.setAxisXBottom(axisX); //x 轴在底部
//	    data.setAxisXTop(axisX);  //x 轴在顶部
        axisX.setHasLines(true); //x 轴分割线


        Axis axisY = new Axis();  //Y轴
        axisY.setName("");//y轴标注
        axisY.setTextSize(13);//设置字体大小
        axisY.setTextColor(Color.parseColor("#000000"));//灰色
        data.setAxisYLeft(axisY);  //Y轴设置在左边
        //data.setAxisYRight(axisY);  //y轴设置在右边
        //设置行为属性，支持缩放、滑动以及平移
        lineChart.setInteractive(true);
        lineChart.setScrollEnabled(true);
        lineChart.setZoomType(ZoomType.HORIZONTAL);  //缩放类型，水平
        lineChart.setMaxZoom((float) 3);//缩放比例
        lineChart.setLineChartData(data);
        lineChart.setVisibility(View.VISIBLE);
        /**注：下面的7，10只是代表一个数字去类比而已
         * 尼玛搞的老子好辛苦！！！见（http://forum.xda-developers.com/tools/programming/library-hellocharts-charting-library-t2904456/page2）;
         * 下面几句可以设置X轴数据的显示个数（x轴0-7个数据），当数据点个数小于（29）的时候，缩小到极致hellochart默认的是所有显示。当数据点个数大于（29）的时候，
         * 若不设置axisX.setMaxLabelChars(int count)这句话,则会自动适配X轴所能显示的尽量合适的数据个数。
         * 若设置axisX.setMaxLabelChars(int count)这句话,
         * 33个数据点测试，若 axisX.setMaxLabelChars(10);里面的10大于v.right= 7; 里面的7，则
         刚开始X轴显示7条数据，然后缩放的时候X轴的个数会保证大于7小于10
         若小于v.right= 7;中的7,反正我感觉是这两句都好像失效了的样子 - -!
         * 并且Y轴是根据数据的大小自动设置Y轴上限
         * 若这儿不设置 v.right= 7; 这句话，则图表刚开始就会尽可能的显示所有数据，交互性太差
         */
        Viewport v = new Viewport(lineChart.getMaximumViewport());
        v.left = 0;
        v.right = 7;
        lineChart.setCurrentViewport(v);
    }

    /**
     * X 轴的显示
     */
    private void getAxisXLables() {
        for (int i = 0; i < dateList.size(); i++) {
            mAxisXValues.add(new AxisValue(i).setLabel(dateList.get(i)));
        }
    }

    /**
     * 图表的每个点的显示
     */
    private void getAxisPoints() {
        for (int i = 0; i < gpaList.size(); i++) {
            mPointValues.add(new PointValue(i, gpaList.get(i)).setLabel(gpaList.get(i) + ""));
        }
    }

    //发送查询校历的请求
    private void requestXiaoLi() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog.setMessage("查询成绩中...");
                mProgressDialog.show();
            }
        });
        String url = UrlUtil.XIAO_LI;
        final RequestBody requestBody = new FormBody.Builder()
                .add("username", stu_id)
                .add("password", stu_pass)
                .build();

        HttpUtil.sendPostHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        Toasty.error(mContext, "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    Res res = ResponseUtil.handleResponse(data);
                    assert res != null;
                    if (res.getCode() == 200 && res.getInfo() != null) {
                        SpUtils.putString(mContext, AppConstants.XIAO_LI, res.getInfo());
                        try {
                            JSONObject object = new JSONObject(res.getInfo());
                            //获取当前周数
                            //获取这个学生所有的学年
                            JSONArray jsonArray = object.getJSONArray("all_year");
                            all_year_list.add("请选择学年");
                            for (int i = 1; i < jsonArray.length(); i++) {
                                all_year_list.add(jsonArray.get(i).toString());
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    spinner_year.attachDataSource(all_year_list);
                                }
                            });
                            requestScore();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mProgressDialog.isShowing() && !isFinishing())
                                mProgressDialog.dismiss();
                            Toasty.error(mContext, "错误" + response.code() + "，请稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    //发出分数查询的请求
    private void requestScore() {
        String url = UrlUtil.STU_SCORE;

        RequestBody requestBody = new FormBody.Builder()
                .add("username", stu_id)
                .add("password", stu_pass)
                .build();
        HttpUtil.sendPostHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mProgressDialog.isShowing() && !isFinishing())
                            mProgressDialog.dismiss();
                        Toasty.error(mContext, "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    Res res = ResponseUtil.handleResponse(data);
                    assert res != null;
                    if (res.getCode() == 200 && res.getInfo() != null) {
                        handleScoreResponse(res.getInfo());
                        requestPoint();
                    } else {
                        Looper.prepare();
                        if (mProgressDialog.isShowing() && !isFinishing())
                            mProgressDialog.dismiss();
                        Toasty.error(mContext, res.getMsg(), Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mProgressDialog.isShowing() && !isFinishing())
                                mProgressDialog.dismiss();
                            Toasty.error(mContext, "错误" + response.code() + "，请稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void requestPoint() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    mProgressDialog.setMessage("绩点导入中,请稍后……");
                    mProgressDialog.show();
                }
            }
        });

        String pointUrl = UrlUtil.STU_GPA;
        String username = SpUtils.getString(mContext, AppConstants.STU_ID);
        String password = SpUtils.getString(mContext, AppConstants.STU_PASS);
        RequestBody requestBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();
        HttpUtil.sendPostHttpRequest(pointUrl, requestBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mProgressDialog.isShowing() && !isFinishing())
                            mProgressDialog.dismiss();
                        Toasty.error(mContext, "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    Res res = ResponseUtil.handleResponse(data);
                    assert res != null;
                    if (res.getCode() == 200 && res.getInfo() != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mProgressDialog.isShowing() && !isFinishing())
                                    mProgressDialog.dismiss();
                            }
                        });
                        showPointResult(res.getInfo());
                    } else {
                        Looper.prepare();
                        if (mProgressDialog.isShowing() && !isFinishing())
                            mProgressDialog.dismiss();
                        Toasty.error(mContext, res.getMsg(), Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mProgressDialog.isShowing() && !isFinishing())
                                mProgressDialog.dismiss();
                            Toasty.error(mContext, "错误" + response.code() + "，请稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void showPointResult(String responseText) {
        try {
            JSONArray jsonArray = new JSONArray(responseText);
            final JSONObject object = jsonArray.getJSONObject(0); //总学年绩点信息

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        tv_GPA_ALL.setText("总学年绩点：" + object.getString("point"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            for (int i = 1; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String every_year = jsonObject.getString("year");
                String every_point = jsonObject.getString("point");
                dateList.add(every_year);
                gpaList.add(Float.parseFloat(every_point));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        getAxisXLables();//获取x轴的标注
        getAxisPoints();//获取坐标点
        initLineChart();//初始化
    }

    private void chooseScore() {
        final int count = all_year_list.size();
        spinner_year.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 1; i < count; i++) {
                    if (position == i) {
                        String text = all_year_list.get(i);
                        showChooseResult(text);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void showChooseResult(String text) {
        subjectList.clear();
        for (int i = 0; i < gSubjectList.size(); i++) {
            if (gSubjectList.get(i).getStart_semester().equals(text)) {
                Subject subject = new Subject(gSubjectList.get(i).getCourse_name(), gSubjectList.get(i).getCredit(), gSubjectList.get(i).getScore());
                subjectList.add(subject);
            }
        }
        subjectAdapter = new SubjectAdapter(NewSubjectActivity.this, R.layout.item_subjects, subjectList);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listView.setAdapter(subjectAdapter);
            }
        });
    }

    //对服务器响应的数据进行接收
    private void handleScoreResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            Gson gson = new Gson();
            gSubjectList = gson.fromJson(response, new TypeToken<List<g_Subject>>() {
            }.getType());
            showAllResult(gSubjectList);
        } else {
            Toasty.error(mContext, "服务器没有响应", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAllResult(List<g_Subject> gSubjectList) {
        for (int i = 0; i < gSubjectList.size(); i++) {
            Subject subject = new Subject(gSubjectList.get(i).getCourse_name(), gSubjectList.get(i).getCredit(), gSubjectList.get(i).getScore());
            subjectList.add(subject);
        }
        subjectAdapter = new SubjectAdapter(NewSubjectActivity.this, R.layout.item_subjects, subjectList);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listView.setAdapter(subjectAdapter);
            }
        });
        mProgressDialog.dismiss();
        chooseScore();
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
                Intent in = new Intent(NewSubjectActivity.this, SubjectsActivity.class);
                startActivity(in);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mProgressDialog.isShowing() && !isFinishing())
            mProgressDialog.dismiss();
    }
}
