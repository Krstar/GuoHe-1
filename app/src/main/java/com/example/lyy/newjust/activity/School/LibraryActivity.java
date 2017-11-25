package com.example.lyy.newjust.activity.School;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.lyy.newjust.R;
import com.example.lyy.newjust.util.HttpUtil;
import com.example.lyy.newjust.util.UrlUtil;
import com.githang.statusbar.StatusBarCompat;
import com.lyy.searchlibrary.searchbox.SearchFragment;
import com.lyy.searchlibrary.searchbox.custom.IOnSearchClickListener;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LibraryActivity extends SwipeBackActivity implements Toolbar.OnMenuItemClickListener, IOnSearchClickListener {

    private static final String TAG = "LibraryActivity";

    private SearchFragment searchFragment;

    private String[] mVals = new String[10];

    //热搜图书
    private void searchHotBook() {
        String url = UrlUtil.HOT_BOOK;
        HttpUtil.sendHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    try {
                        JSONArray array = new JSONArray(data);
                        JSONArray innerArray = array.getJSONArray(0);
                        for (int i = 0; i < innerArray.length(); i++) {
                            mVals[i] = innerArray.get(i).toString().split(" ")[1];
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setStatusBarColor(this, Color.rgb(0, 172, 193));
        setContentView(R.layout.activity_library);

        setSwipeBackEnable(true);   // 可以调用该方法，设置是否允许滑动退出
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        // 设置滑动方向，可设置EDGE_LEFT, EDGE_RIGHT, EDGE_ALL, EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        // 滑动退出的效果只能从边界滑动才有效果，如果要扩大touch的范围，可以调用这个方法
        mSwipeBackLayout.setEdgeSize(100);

        //设置和toolbar相关的
        Toolbar toolbar = (Toolbar) findViewById(R.id.library_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        toolbar.setOnMenuItemClickListener(this);

        searchHotBook();

        searchFragment = SearchFragment.newInstance();
        searchFragment.setOnSearchClickListener(this);

        searchFragment.setmVals(mVals);

    }

    @Override
    public void OnSearchClick(String keyword) {
        Intent bookListIntent = new Intent(LibraryActivity.this, BookListActivity.class);
        bookListIntent.putExtra("keyword", keyword);
        startActivity(bookListIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //加载菜单文件
        getMenuInflater().inflate(R.menu.menu_library, menu);
        return true;
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

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search://点击搜索
                searchFragment.show(getSupportFragmentManager(), SearchFragment.TAG);
                break;
        }
        return true;
    }
}
