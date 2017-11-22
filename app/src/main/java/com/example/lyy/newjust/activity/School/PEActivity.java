package com.example.lyy.newjust.activity.School;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Looper;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import com.flyco.animation.Attention.Swing;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.MaterialDialog;
import com.githang.statusbar.StatusBarCompat;
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

public class PEActivity extends SwipeBackActivity implements View.OnClickListener {

    private static final String TAG = "PEActivity";

    //定义listView
    private ListView listView;

    //定义list
    List<Club> clubList = new ArrayList<>();

    private ProgressDialog mProgressDialog;

    private TextView tv_pe_info;
    private TextView tv_pe_year;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeStatusBar();
        setContentView(R.layout.activity_pe);

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
        Glide.with(PEActivity.this).load(R.drawable.bg_sport).into(pe_iv);
        FloatingActionButton pe_floating_btn = (FloatingActionButton) findViewById(R.id.pe_floating_btn);
        pe_floating_btn.setOnClickListener(this);

        requestPEInfo();

        listView = (ListView) findViewById(R.id.pe_list_view);
        tv_pe_info = (TextView) findViewById(R.id.tv_pe_info);
        tv_pe_year = (TextView) findViewById(R.id.tv_pe_year);
    }

    private void requestPEInfo() {
        String username = SpUtils.getString(getApplicationContext(), AppConstants.STU_ID);
        String pePass = SpUtils.getString(getApplicationContext(), AppConstants.STU_PE_PASS);
        mProgressDialog = ProgressDialog.show(PEActivity.this, null, "俱乐部数据导入中,请稍后……", true, false);
        if (pePass != null) {
            request(username, pePass);
        } else {
            mProgressDialog.dismiss();
            final EditText editText = new EditText(PEActivity.this);
            AlertDialog.Builder inputDialog =
                    new AlertDialog.Builder(PEActivity.this);
            inputDialog.setTitle("请输入你的体育学院密码").setView(editText);
            inputDialog.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final String username = SpUtils.getString(getApplicationContext(), AppConstants.STU_ID);
                            String url = "http://120.25.88.41/vpnSport";
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
                                        SpUtils.putString(getApplicationContext(), AppConstants.STU_PE_PASS, editText.getText().toString());
                                        Looper.prepare();
                                        Toast.makeText(getApplicationContext(), "体育课数据导入中，请稍后...", Toast.LENGTH_LONG).show();
                                        request(username, editText.getText().toString());
                                        Looper.loop();
                                    }
                                }
                            });
                        }
                    }).show();
        }
    }

    private void request(String username, String pePass) {
        String url = "http://120.25.88.41/vpnSport";
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

                            Log.d(TAG, "onResponse: " + number);
                            Log.d(TAG, "onResponse: " + date);
                            Log.d(TAG, "onResponse: " + time);
                        }

                        Log.d(TAG, "onCreate: " + clubList.size());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String temp = sum.substring(sum.indexOf(" ") + 1, sum.lastIndexOf(" "));
                                tv_pe_info.setText(temp.substring(0, temp.indexOf("(")));
                                String[] s = year.split(" ");
                                tv_pe_year.setText(s[1]);

                                ClubAdapter clubAdapter = new ClubAdapter(PEActivity.this, R.layout.item_club, clubList);
                                listView.setAdapter(clubAdapter);
                                mProgressDialog.dismiss();
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
        final MaterialDialog dialog = new MaterialDialog(PEActivity.this);
        dialog.content(msg)//
                .btnText("取消", "确定")//
                .showAnim(new Swing())//
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
