package com.example.lyy.newjust.activity.School;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.lyy.newjust.R;
import com.example.lyy.newjust.adapter.Exercise;
import com.example.lyy.newjust.adapter.ExerciseAdapter;
import com.example.lyy.newjust.model.Res;
import com.example.lyy.newjust.util.AppConstants;
import com.example.lyy.newjust.util.HttpUtil;
import com.example.lyy.newjust.util.ResponseUtil;
import com.example.lyy.newjust.util.SpUtils;
import com.example.lyy.newjust.util.UrlUtil;
import com.flyco.animation.BounceEnter.BounceBottomEnter;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.MaterialDialog;

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

public class ExerciseActivity extends SwipeBackActivity implements View.OnClickListener {

    private Context mContext;

    //定义listView
    private ListView listView;

    //定义list
    List<Exercise> exerciseList = new ArrayList<>();

    private ProgressDialog mProgressDialog;

    private TextView tv_exercise_info;
    private TextView tv_exercise_year;

    private SwipeRefreshLayout swipeRefreshLayout;

    private SwipeRefreshLayout.OnRefreshListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeStatusBar();
        setContentView(R.layout.activity_exercise);

        mContext = this;

        setSwipeBackEnable(true);   // 可以调用该方法，设置是否允许滑动退出
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        // 设置滑动方向，可设置EDGE_LEFT, EDGE_RIGHT, EDGE_ALL, EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        // 滑动退出的效果只能从边界滑动才有效果，如果要扩大touch的范围，可以调用这个方法
        mSwipeBackLayout.setEdgeSize(100);

        //设置和toolbar相关的
        Toolbar toolbar = (Toolbar) findViewById(R.id.exercise_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        ImageView exercise_iv = (ImageView) findViewById(R.id.exercise_iv);
        Glide.with(ExerciseActivity.this).load(R.drawable.bg_sport).into(exercise_iv);
        FloatingActionButton exercise_floating_btn = (FloatingActionButton) findViewById(R.id.exercise_floating_btn);
        exercise_floating_btn.setOnClickListener(this);

        listView = (ListView) findViewById(R.id.exercise_list_view);
        tv_exercise_info = (TextView) findViewById(R.id.tv_exercise_info);
        tv_exercise_year = (TextView) findViewById(R.id.tv_exercise_year);

        initSwipeRefresh();

        requestExerciseInfo();
    }

    private void initSwipeRefresh() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.exercise_refresh);

        // 设置颜色属性的时候一定要注意是引用了资源文件还是直接设置16进制的颜色，因为都是int值容易搞混
        // 设置下拉进度的背景颜色，默认就是白色的
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        // 设置下拉进度的主题颜色
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);

        listener = new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                //TODO
                String username = SpUtils.getString(mContext, AppConstants.STU_ID);
                String pePass = SpUtils.getString(mContext, AppConstants.STU_PE_PASS);
                requestExerciseScore(username, pePass);
            }
        };

        swipeRefreshLayout.setOnRefreshListener(listener);
    }

    private void requestExerciseInfo() {
        String username = SpUtils.getString(mContext, AppConstants.STU_ID);
        String pePass = SpUtils.getString(mContext, AppConstants.STU_PE_PASS);
        mProgressDialog = ProgressDialog.show(ExerciseActivity.this, null, "早操数据导入中,请稍后……", true, false);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(true);
        if (pePass != null) {
            requestExerciseScore(username, pePass);
        } else {
            mProgressDialog.dismiss();
            final EditText editText = new EditText(ExerciseActivity.this);
            AlertDialog.Builder inputDialog =
                    new AlertDialog.Builder(ExerciseActivity.this);
            inputDialog.setTitle("请输入你的体育学院密码").setView(editText);
            inputDialog.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mProgressDialog = ProgressDialog.show(ExerciseActivity.this, null, "密码验证中,请稍后……", true, false);
                            mProgressDialog.setCancelable(true);
                            mProgressDialog.setCanceledOnTouchOutside(true);
                            final String username = SpUtils.getString(mContext, AppConstants.STU_ID);
                            String url = UrlUtil.EXERCISE_SCORE;
                            RequestBody requestBody = new FormBody.Builder()
                                    .add("username", username)
                                    .add("password", editText.getText().toString())
                                    .build();
                            HttpUtil.sendPostHttpRequest(url, requestBody, new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (mProgressDialog.isShowing())
                                                mProgressDialog.dismiss();
                                            Toast.makeText(mContext, "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                                    if (response.isSuccessful()) {
                                        String data = response.body().string();
                                        Res res = ResponseUtil.handleResponse(data);
                                        assert res != null;
                                        if (res.getCode() == 200) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mProgressDialog.dismiss();
                                                    mProgressDialog = ProgressDialog.show(ExerciseActivity.this, null, "验证成功,请稍后……", true, false);
                                                    mProgressDialog.setCancelable(true);
                                                    mProgressDialog.setCanceledOnTouchOutside(true);
                                                    requestExerciseScore(username, editText.getText().toString());
                                                    SpUtils.putString(mContext, AppConstants.STU_PE_PASS, editText.getText().toString());
                                                }
                                            });
                                        } else {
                                            Looper.prepare();
                                            if (mProgressDialog.isShowing())
                                                mProgressDialog.dismiss();
                                            Toast.makeText(mContext, res.getMsg(), Toast.LENGTH_SHORT).show();
                                            Looper.loop();
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
                    }).show();
        }
    }

    private void requestExerciseScore(String username, String pePass) {
        exerciseList.clear();
        listView.setVisibility(View.GONE);
        String url = UrlUtil.EXERCISE_SCORE;
        RequestBody requestBody = new FormBody.Builder()
                .add("username", username)
                .add("password", pePass)
                .build();

        HttpUtil.sendPostHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
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
                            JSONObject object = array.getJSONObject(0);
                            final String year = object.getString("year");
                            String name = object.getString("name");
                            final String total = object.getString("total");

                            SpUtils.putString(mContext, "exercise_info", name + "\n" + total);

                            JSONArray innerArray = array.getJSONArray(1);
                            for (int i = 0; i < innerArray.length(); i++) {
                                JSONObject innerObject = innerArray.getJSONObject(i);
                                String number = innerObject.getString("number");
                                String date = innerObject.getString("date");
                                String time = innerObject.getString("time");

                                Exercise exercise = new Exercise(time, number, date);
                                exerciseList.add(exercise);
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String[] s = year.split(" ");
                                    tv_exercise_year.setText(s[1]);

                                    String[] s1 = total.split(" ");
                                    tv_exercise_info.setText(s1[1] + s1[2]);

                                    ExerciseAdapter exerciseAdapter = new ExerciseAdapter(ExerciseActivity.this, R.layout.item_exercise, exerciseList);
                                    listView.setAdapter(exerciseAdapter);
                                    listView.setVisibility(View.VISIBLE);
                                    mProgressDialog.dismiss();
                                    swipeRefreshLayout.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            swipeRefreshLayout.setRefreshing(false);
                                        }
                                    });
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Looper.prepare();
                        if (mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
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
                            if (mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                            Toast.makeText(mContext, "错误" + response.code() + "，请稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void showMaterialDialogDefault(String msg) {
        final MaterialDialog dialog = new MaterialDialog(ExerciseActivity.this);
        dialog.content(msg)//
                .btnText("取消", "确定")//
                .showAnim(new BounceBottomEnter())//
                .show();

        dialog.setOnBtnClickL(
                new OnBtnClickL() {//left btn click listener
                    @Override
                    public void onBtnClick() {
                        dialog.dismiss();
                    }
                },
                new OnBtnClickL() {//right btn click listener
                    @Override
                    public void onBtnClick() {
                        dialog.dismiss();
                    }
                }
        );
    }

    //将背景图和状态栏融合到一起
    private void changeStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));
            }
        }
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
    protected void onStop() {
        super.onStop();

        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.exercise_floating_btn:
                String msg = SpUtils.getString(mContext, "exercise_info");
                showMaterialDialogDefault(msg);
                break;
        }
    }
}
