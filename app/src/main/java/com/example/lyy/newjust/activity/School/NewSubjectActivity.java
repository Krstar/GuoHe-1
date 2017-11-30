package com.example.lyy.newjust.activity.School;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.lyy.newjust.R;
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

import org.angmarch.views.NiceSpinner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NewSubjectActivity extends AppCompatActivity {

    private static final String TAG = "NewSubjectActivity";

    private Context mContext;

    private ListView listView;
    private NiceSpinner spinner_year;
    private ProgressDialog mProgressDialog;

    private SubjectAdapter subjectAdapter;

    private List<Subject> subjectList;  //成绩集合
    private List<String> all_year_list; //所有学年的集合
    private List<g_Subject> gSubjectList;

    private String stu_id;
    private String stu_pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setStatusBarColor(this, Color.rgb(255, 255, 255));
        setContentView(R.layout.activity_new_subject);

        mContext = this;

        //设置和toolbar相关的
        Toolbar toolbar = (Toolbar) findViewById(R.id.new_subject_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        listView = (ListView) findViewById(R.id.lv_subject_list);
        spinner_year = (NiceSpinner) findViewById(R.id.spinner_year);
        mProgressDialog = new ProgressDialog(NewSubjectActivity.this);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(true);

        subjectList = new ArrayList<>();
        all_year_list = new ArrayList<>();

        stu_id = SpUtils.getString(mContext, AppConstants.STU_ID);
        stu_pass = SpUtils.getString(mContext, AppConstants.STU_PASS);

        requestXiaoLi();
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
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        Toast.makeText(mContext, "服务器异常，请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    if (HttpUtil.isGoodJson(data)) {
                        SpUtils.putString(mContext, AppConstants.XIAO_LI, data);
                        try {
                            JSONObject object = new JSONObject(data);
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
                            searchScoreRequest();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                            Toast.makeText(mContext, "错误" + response.code() + "，请稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
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
        Log.d(TAG, "showChooseResult: " + text);
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

    //发出分数查询的请求
    private void searchScoreRequest() {
        String url = UrlUtil.STU_SCORE;

        RequestBody requestBody = new FormBody.Builder()
                .add("username", stu_id)
                .add("password", stu_pass)
                .build();
        HttpUtil.sendPostHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        Toast.makeText(mContext, "服务器异常，请稍后重试", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseText = response.body().string();
                    Log.d(TAG, "onResponse: " + responseText);
                    switch (responseText) {
                        case "\"\\u672a\\u8bc4\\u4ef7\"":
                            if (mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                            Looper.prepare();
                            Toast.makeText(mContext, "你还没有评教", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                            break;
                        case "\"\\u6ca1\\u6709\\u6210\\u7ee9\"":
                            if (mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                            Looper.prepare();
                            Toast.makeText(mContext, "你还没有成绩", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                            break;
                        default:
                            if (HttpUtil.isGoodJson(responseText))
                                handleScoreResponse(responseText);
                            break;
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                            Toast.makeText(mContext, "错误" + response.code() + "，请稍后重试", Toast.LENGTH_SHORT).show();
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
            gSubjectList = gson.fromJson(response, new TypeToken<List<g_Subject>>() {
            }.getType());
            showAllResult(gSubjectList);
        } else {
            Toast.makeText(mContext, "服务器没有响应", Toast.LENGTH_SHORT).show();
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
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();
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
}
