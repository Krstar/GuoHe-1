package com.lyy.guohe.activity.School;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
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
import com.lyy.guohe.R;
import com.lyy.guohe.adapter.Club;
import com.lyy.guohe.adapter.ClubAdapter;
import com.lyy.guohe.model.Res;
import com.lyy.guohe.util.AppConstants;
import com.lyy.guohe.util.HttpUtil;
import com.lyy.guohe.util.ResponseUtil;
import com.lyy.guohe.util.SpUtils;
import com.lyy.guohe.util.UrlUtil;
import com.flyco.animation.BounceEnter.BounceBottomEnter;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import shortbread.Shortcut;

@Shortcut(id = "club", icon = R.drawable.ic_menu_club, shortLabel = "俱乐部")
public class ClubActivity extends SwipeBackActivity implements View.OnClickListener {

    private static final String TAG = "ClubActivity";

    private Context mContext;

    //定义listView
    private ListView listView;

    //定义list
    List<Club> clubList = new ArrayList<>();

    private ProgressDialog mProgressDialog;

    private TextView tv_club_info;
    private TextView tv_club_year;

    private SwipeRefreshLayout swipeRefreshLayout;

    private SwipeRefreshLayout.OnRefreshListener listener;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeStatusBar();
        setContentView(R.layout.activity_club);

        mContext = this;

        setSwipeBackEnable(true);   // 可以调用该方法，设置是否允许滑动退出
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        // 设置滑动方向，可设置EDGE_LEFT, EDGE_RIGHT, EDGE_ALL, EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        // 滑动退出的效果只能从边界滑动才有效果，如果要扩大touch的范围，可以调用这个方法
        mSwipeBackLayout.setEdgeSize(100);

        //设置和toolbar相关的
        Toolbar toolbar = (Toolbar) findViewById(R.id.club_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        ImageView club_iv = (ImageView) findViewById(R.id.club_iv);
        Glide.with(ClubActivity.this).load(R.drawable.bg_sport).into(club_iv);
        FloatingActionButton club_floating_btn = (FloatingActionButton) findViewById(R.id.club_floating_btn);
        club_floating_btn.setOnClickListener(this);

        listView = (ListView) findViewById(R.id.club_list_view);
        tv_club_info = (TextView) findViewById(R.id.tv_club_info);
        tv_club_year = (TextView) findViewById(R.id.tv_club_year);

        requestClubInfo();

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
                String username = SpUtils.getString(mContext, AppConstants.STU_ID);
                String pePass = SpUtils.getString(mContext, AppConstants.STU_PE_PASS);
                requestClubScore(username, pePass);
            }
        };

        swipeRefreshLayout.setOnRefreshListener(listener);
    }

    private void requestClubInfo() {
        String username = SpUtils.getString(mContext, AppConstants.STU_ID);
        String pePass = SpUtils.getString(mContext, AppConstants.STU_PE_PASS);
        mProgressDialog = ProgressDialog.show(ClubActivity.this, null, "俱乐部数据导入中,请稍后……", true, false);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setCanceledOnTouchOutside(true);

        if (pePass != null) {
            requestClubScore(username, pePass);
        } else {
            mProgressDialog.dismiss();
            final EditText editText = new EditText(ClubActivity.this);
            AlertDialog.Builder inputDialog =
                    new AlertDialog.Builder(ClubActivity.this);
            inputDialog.setTitle("请输入你的体育学院密码(默认姓名首字母大写)").setView(editText);
            inputDialog.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mProgressDialog = ProgressDialog.show(ClubActivity.this, null, "密码验证中,请稍后……", true, false);
                            mProgressDialog.setCancelable(true);
                            mProgressDialog.setCanceledOnTouchOutside(true);
                            final String username = SpUtils.getString(mContext, AppConstants.STU_ID);
                            String url = UrlUtil.CLUB_SCORE;
                            RequestBody requestBody = new FormBody.Builder()
                                    .add("username", username)
                                    .add("password", editText.getText().toString())
                                    .build();
                            HttpUtil.sendPostHttpRequest(url, requestBody, new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    runOnUiThread(() -> {
                                        if (mProgressDialog.isShowing())
                                            mProgressDialog.dismiss();
                                        Toasty.error(mContext, "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
                                    });
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                                    if (response.isSuccessful()) {
                                        String data = response.body().string();
                                        Res res = ResponseUtil.handleResponse(data);
                                        assert res != null;
                                        if (res.getCode() == 200) {
                                            runOnUiThread(() -> {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = ProgressDialog.show(ClubActivity.this, null, "验证成功,请稍后……", true, false);
                                                mProgressDialog.setCancelable(true);
                                                mProgressDialog.setCanceledOnTouchOutside(true);
                                                requestClubScore(username, editText.getText().toString());
                                                SpUtils.putString(mContext, AppConstants.STU_PE_PASS, editText.getText().toString());
                                            });
                                        } else {
                                            Looper.prepare();
                                            if (mProgressDialog.isShowing())
                                                mProgressDialog.dismiss();
                                            Toasty.error(mContext, res.getMsg(), Toast.LENGTH_SHORT).show();
                                            Looper.loop();
                                        }
                                    } else {
                                        runOnUiThread(() -> {
                                            if (mProgressDialog.isShowing())
                                                mProgressDialog.dismiss();
                                            Toasty.error(mContext, "服务器发生异常，请稍后重试", Toast.LENGTH_SHORT).show();
                                        });
                                    }
                                }
                            });
                        }
                    }).show();
        }
    }

    private void requestClubScore(String username, String pePass) {
        clubList.clear();
        listView.setVisibility(View.GONE);
        String url = UrlUtil.CLUB_SCORE;
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
                        Toasty.error(mContext, "服务器异常，请稍后重试", Toast.LENGTH_SHORT).show();
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
                        try {
                            JSONArray array = new JSONArray(res.getInfo());
                            JSONObject object = array.getJSONObject(0);
                            final String year = object.getString("year");
                            String name = object.getString("name");
                            String total = object.getString("total");
                            final String sum = object.getString("sum");

                            SpUtils.putString(mContext, "club_info", total);

                            JSONArray innerArray = array.getJSONArray(1);
                            for (int i = 0; i < innerArray.length(); i++) {
                                JSONObject innerObject = innerArray.getJSONObject(i);
                                String number = innerObject.getString("number");
                                String date = innerObject.getString("date");
                                String time = innerObject.getString("time");

                                Club club = new Club(time, number, date);
                                clubList.add(club);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String temp = sum.substring(sum.indexOf(" ") + 1, sum.lastIndexOf(" "));
                                    tv_club_info.setText(temp.substring(0, temp.indexOf("(")));
                                    String[] s = year.split(" ");
                                    tv_club_year.setText(s[1]);

                                    ClubAdapter clubAdapter = new ClubAdapter(ClubActivity.this, R.layout.item_club, clubList);
                                    if (clubList.size() == 0) {
                                        Toasty.warning(mContext, "列表数据为空", Toast.LENGTH_SHORT).show();
                                    }
                                    listView.setAdapter(clubAdapter);
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
                        Toasty.error(mContext, res.getMsg(), Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                            Toasty.error(mContext, "错误" + response.code() + "，请稍后重试", Toast.LENGTH_SHORT).show();
                        }
                    });
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
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
            case R.id.club_floating_btn:
                String msg = SpUtils.getString(mContext, "club_info");
                showMaterialDialogDefault(msg);
                break;
        }
    }
}
