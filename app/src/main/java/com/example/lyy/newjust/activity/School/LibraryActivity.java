package com.example.lyy.newjust.activity.School;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.lyy.newjust.R;
import com.example.lyy.newjust.adapter.Library;
import com.example.lyy.newjust.adapter.LibraryAdapter;
import com.example.lyy.newjust.model.Res;
import com.example.lyy.newjust.util.HttpUtil;
import com.example.lyy.newjust.util.ResponseUtil;
import com.example.lyy.newjust.util.UrlUtil;
import com.githang.statusbar.StatusBarCompat;
import com.lyy.searchlibrary.searchbox.SearchFragment;
import com.lyy.searchlibrary.searchbox.custom.IOnSearchClickListener;
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
import okhttp3.Response;

public class LibraryActivity extends SwipeBackActivity implements Toolbar.OnMenuItemClickListener, IOnSearchClickListener {

    private static final String TAG = "LibraryActivity";

    private Context mContext;

    private SearchFragment searchFragment;

    private String[] mVals = new String[15];

    private List<Library> libraryList = new ArrayList<>();

    private LibraryAdapter adapter;

    private RecyclerView recyclerView;

    private GridLayoutManager layoutManager;

    private SwipeRefreshLayout swipeRefreshLayout;

    private SwipeRefreshLayout.OnRefreshListener listener;

    private int[] color = {
            R.color.material_amber_200,
            R.color.material_red_200,
            R.color.material_light_green_300,
            R.color.material_green_200,
            R.color.material_teal_500,
            R.color.material_light_blue_500,
            R.color.material_blue_400,
            R.color.material_pink_200,
            R.color.material_orange_500,
            R.color.material_deep_orange_A200,
            R.color.material_orange_A200,
            R.color.material_lime_400,
            R.color.material_cyan_A200,
            R.color.material_red_300
    };

    private void initSwipeRefresh() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.library_refresh);

        // 设置颜色属性的时候一定要注意是引用了资源文件还是直接设置16进制的颜色，因为都是int值容易搞混
        // 设置下拉进度的背景颜色，默认就是白色的
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        // 设置下拉进度的主题颜色
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);

        listener = new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                //TODO
                libraryList.clear();
                requestBookTop();
            }
        };

        swipeRefreshLayout.setOnRefreshListener(listener);
    }

    //发送查询前本热门书籍的请求
    private void requestBookTop() {
        recyclerView.setVisibility(View.GONE);
        String url = UrlUtil.BOOK_TOP;
        HttpUtil.sendHttpRequest(url, new Callback() {
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
                    requestHotBook();
                    assert res != null;
                    if (res.getCode() == 200)
                        handleResponse(res.getInfo());
                    else {
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

    //热搜图书
    private void requestHotBook() {
        String url = UrlUtil.HOT_BOOK;
        HttpUtil.sendHttpRequest(url, new Callback() {
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
                            JSONArray innerArray = array.getJSONArray(0);
                            for (int i = 0; i < 10; i++) {
                                if (innerArray.get(i) != null) {
                                    Log.d(TAG, "onResponse: " + innerArray.get(i).toString());
                                    mVals[i] = innerArray.get(i).toString().split(" ")[1];
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        swipeRefreshLayout.post(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
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
                                Toast.makeText(mContext, "服务器异常，请稍后重试", Toast.LENGTH_SHORT).show();
                            }
                        });
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

    private void handleResponse(String data) {
        try {
            JSONArray jsonArray = new JSONArray(data);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                String bookcode = object.getString("bookcode");
                String press = object.getString("press");
                String name = object.getString("name");
                String author = object.getString("author");
                String url = object.getString("url");

                int x = (int) (Math.random() * 13);
                Library library = new Library(bookcode, name, press, url, author, color[x]);
                libraryList.add(library);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recyclerView.setLayoutManager(layoutManager);
                    adapter = new LibraryAdapter(libraryList);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search://点击搜索
                searchFragment.show(getSupportFragmentManager(), SearchFragment.TAG);
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setStatusBarColor(this, Color.rgb(0, 172, 193));
        setContentView(R.layout.activity_library);

        mContext = this;

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

        searchFragment = SearchFragment.newInstance();
        searchFragment.setOnSearchClickListener(this);

        searchFragment.setmVals(mVals);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        layoutManager = new GridLayoutManager(LibraryActivity.this, 2);

        initSwipeRefresh();
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        listener.onRefresh();
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
}
