package com.example.lyy.newjust.activity.School;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.example.lyy.newjust.adapter.Club;
import com.example.lyy.newjust.adapter.ClubAdapter;
import com.example.lyy.newjust.util.AppConstants;
import com.example.lyy.newjust.util.HttpUtil;
import com.example.lyy.newjust.util.SpUtils;
import com.example.lyy.newjust.util.UrlUtil;
import com.flyco.animation.Attention.Flash;
import com.flyco.animation.Attention.Swing;
import com.flyco.animation.BounceEnter.BounceBottomEnter;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.MaterialDialog;
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

public class ClubActivity extends SwipeBackActivity implements View.OnClickListener {

    private static final String TAG = "ClubActivity";

    //定义listView
    private ListView listView;

    //定义list
    List<Club> clubList = new ArrayList<>();

    private ProgressDialog mProgressDialog;

    private TextView tv_pe_info;
    private TextView tv_pe_year;

    private SwipeRefreshLayout swipeRefreshLayout;

    private SwipeRefreshLayout.OnRefreshListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeStatusBar();
        setContentView(R.layout.activity_club);

        setSwipeBackEnable(true);   // 可以调用该方法，设置是否允许滑动退出
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        // 设置滑动方向，可设置EDGE_LEFT, EDGE_RIGHT, EDGE_ALL, EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        // 滑动退出的效果只能从边界滑动才有效果，如果要扩大touch的范围，可以调用这个方法
        mSwipeBackLayout.setEdgeSize(100);

        //设置和toolbar相关的
        Toolbar toolbar = (Toolbar) findViewById(R.id.pe_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        ImageView pe_iv = (ImageView) findViewById(R.id.pe_iv);
        Glide.with(ClubActivity.this).load(R.drawable.bg_sport).into(pe_iv);
        FloatingActionButton pe_floating_btn = (FloatingActionButton) findViewById(R.id.pe_floating_btn);
        pe_floating_btn.setOnClickListener(this);

        requestPEInfo();

        listView = (ListView) findViewById(R.id.pe_list_view);
        tv_pe_info = (TextView) findViewById(R.id.tv_pe_info);
        tv_pe_year = (TextView) findViewById(R.id.tv_pe_year);

        initSwipeRefresh();
    }

    private void initSwipeRefresh() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.club_refresh);

        // 设置颜色属性的时候一定要注意是引用了资源文件还是直接设置16进制的颜色，因为都是int值容易搞混
        // 设置下拉进度的背景颜色，默认就是白色的
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        // 设置下拉进度的主题颜色
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);

        listener = new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                //TODO
                String username = SpUtils.getString(getApplicationContext(), AppConstants.STU_ID);
                String pePass = SpUtils.getString(getApplicationContext(), AppConstants.STU_PE_PASS);
                request(username, pePass);
            }
        };

        swipeRefreshLayout.setOnRefreshListener(listener);
    }

    private void requestPEInfo() {
        String username = SpUtils.getString(getApplicationContext(), AppConstants.STU_ID);
        String pePass = SpUtils.getString(getApplicationContext(), AppConstants.STU_PE_PASS);
        mProgressDialog = ProgressDialog.show(ClubActivity.this, null, "俱乐部数据导入中,请稍后……", true, false);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(true);
        if (pePass != null) {
            request(username, pePass);
        } else {
            mProgressDialog.dismiss();
            final EditText editText = new EditText(ClubActivity.this);
            AlertDialog.Builder inputDialog =
                    new AlertDialog.Builder(ClubActivity.this);
            inputDialog.setTitle("请输入你的体育学院密码").setView(editText);
            inputDialog.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mProgressDialog = ProgressDialog.show(ClubActivity.this, null, "密码验证中,请稍后……", true, false);
                            mProgressDialog.setCancelable(true);
                            mProgressDialog.setCanceledOnTouchOutside(true);
                            final String username = SpUtils.getString(getApplicationContext(), AppConstants.STU_ID);
                            String url = UrlUtil.CLUB_SCORE;
                            RequestBody requestBody = new FormBody.Builder()
                                    .add("username", username)
                                    .add("password", editText.getText().toString())
                                    .build();
                            HttpUtil.sendPostHttpRequest(url, requestBody, new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {

                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    if (response.isSuccessful()) {
                                        Looper.prepare();
                                        mProgressDialog.dismiss();
                                        mProgressDialog = ProgressDialog.show(ClubActivity.this, null, "验证成功,请稍后……", true, false);
                                        mProgressDialog.setCancelable(true);
                                        mProgressDialog.setCanceledOnTouchOutside(true);
                                        request(username, editText.getText().toString());
                                        SpUtils.putString(getApplicationContext(), AppConstants.STU_PE_PASS, editText.getText().toString());
                                        Looper.loop();
                                    }
                                }
                            });
                        }
                    }).show();
        }
    }

    private void request(String username, String pePass) {
        clubList.clear();
        String url = UrlUtil.CLUB_SCORE;
        RequestBody requestBody = new FormBody.Builder()
                .add("username", username)
                .add("password", pePass)
                .build();

        HttpUtil.sendPostHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    try {
                        JSONArray array = new JSONArray(data);
                        JSONObject object = array.getJSONObject(0);
                        final String year = object.getString("year");
                        String name = object.getString("name");
                        String total = object.getString("total");
                        final String sum = object.getString("sum");

                        SpUtils.putString(getApplicationContext(), "pe_info", total);

                        Log.d(TAG, "onResponse: " + year);
                        Log.d(TAG, "onResponse: " + name);
                        Log.d(TAG, "onResponse: " + total);
                        Log.d(TAG, "onResponse: " + sum);

                        JSONArray innerArray = array.getJSONArray(1);
                        for (int i = 0; i < innerArray.length(); i++) {
                            JSONObject innerObject = innerArray.getJSONObject(i);
                            String number = innerObject.getString("number");
                            String date = innerObject.getString("date");
                            String time = innerObject.getString("time");

                            Club club = new Club(time, number, date);
                            clubList.add(club);
                        }

                        Log.d(TAG, "onCreate: " + clubList.size());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String temp = sum.substring(sum.indexOf(" ") + 1, sum.lastIndexOf(" "));
                                tv_pe_info.setText(temp.substring(0, temp.indexOf("(")));
                                String[] s = year.split(" ");
                                tv_pe_year.setText(s[1]);

                                ClubAdapter clubAdapter = new ClubAdapter(ClubActivity.this, R.layout.item_club, clubList);
                                listView.setAdapter(clubAdapter);
                                mProgressDialog.dismiss();
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
                }
            }
        });
    }

    private void showMaterialDialogDefault(String msg) {
        final MaterialDialog dialog = new MaterialDialog(ClubActivity.this);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pe_floating_btn:
                String msg = SpUtils.getString(getApplicationContext(), "pe_info");
                showMaterialDialogDefault(msg);
                break;
        }
    }
}
