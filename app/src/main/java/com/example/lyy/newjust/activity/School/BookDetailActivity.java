package com.example.lyy.newjust.activity.School;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lyy.newjust.R;
import com.example.lyy.newjust.adapter.BookDetail;
import com.example.lyy.newjust.adapter.BookDetailAdapter;
import com.example.lyy.newjust.model.Res;
import com.example.lyy.newjust.util.HttpUtil;
import com.example.lyy.newjust.util.ResponseUtil;
import com.example.lyy.newjust.util.UrlUtil;
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

public class BookDetailActivity extends SwipeBackActivity {

    private Context mContext;

    private ListView lv_book_detail;

    private List<BookDetail> bookDetailList = new ArrayList<>();

    private TextView tv_book_name, tv_book_author, tv_book_type, tv_book_press, tv_book_isbn, tv_book_outline;

    private SwipeRefreshLayout swipeRefreshLayout;

    private SwipeRefreshLayout.OnRefreshListener listener;

    private ScrollView scrollView;

    private void initSwipeRefresh(final String keyword) {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.book_detail_refresh);

        // 设置颜色属性的时候一定要注意是引用了资源文件还是直接设置16进制的颜色，因为都是int值容易搞混
        // 设置下拉进度的背景颜色，默认就是白色的
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        // 设置下拉进度的主题颜色
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);

        listener = new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                //TODO
                requestBookDetail(keyword);
            }
        };

        swipeRefreshLayout.setOnRefreshListener(listener);
    }

    private void requestBookDetail(String book_url) {
        bookDetailList.clear();
        String url = UrlUtil.BOOK_DETAIL;
        RequestBody requestBody = new FormBody.Builder()
                .add("bookUrl", book_url)
                .build();
        HttpUtil.sendPostHttpRequest(url, requestBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
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

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    Res res = ResponseUtil.handleResponse(data);
                    if (res.getCode() == 200) {
                        try {
                            JSONArray array = new JSONArray(res.getInfo());
                            for (int i = 0; i < array.length() - 1; i++) {
                                JSONObject object = array.getJSONObject(i);
                                String place = object.getString("place");
                                String call_number = object.getString("call_number");
                                String barcode = object.getString("barcode");

                                String finalPlace = place.split(" ")[place.split(" ").length - 1];

                                BookDetail bookDetail = new BookDetail(call_number, barcode, finalPlace);
                                bookDetailList.add(bookDetail);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        BookDetailAdapter bookDetailAdapter = new BookDetailAdapter(BookDetailActivity.this, R.layout.item_book_detail, bookDetailList);
                                        lv_book_detail.setAdapter(bookDetailAdapter);
                                    }
                                });
                            }
                            JSONObject object = array.getJSONObject(array.length() - 1);
                            final String book_isbn = object.getString("book_isbn");
                            final String book_press = object.getString("book_press");
                            final String book_outline = object.getString("book_outline");
                            final String book_name = object.getString("book_name");
                            final String book_type = object.getString("book_type");
                            final String book_author = object.getString("book_author");

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv_book_name.setText(book_name);
                                    tv_book_author.setText(book_author);
                                    tv_book_type.setText(book_type);
                                    tv_book_press.setText(book_press);
                                    tv_book_isbn.setText(book_isbn);
                                    tv_book_outline.setText(book_outline);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setStatusBarColor(this, Color.rgb(0, 172, 193));
        setContentView(R.layout.activity_book_detail);

        mContext = this;

        setSwipeBackEnable(true);   // 可以调用该方法，设置是否允许滑动退出
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        // 设置滑动方向，可设置EDGE_LEFT, EDGE_RIGHT, EDGE_ALL, EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        // 滑动退出的效果只能从边界滑动才有效果，如果要扩大touch的范围，可以调用这个方法
        mSwipeBackLayout.setEdgeSize(100);

        //设置和toolbar相关的
        Toolbar toolbar = (Toolbar) findViewById(R.id.book_detail_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        lv_book_detail = (ListView) findViewById(R.id.lv_book_detail);

        Intent intent = getIntent();
        String book_url = intent.getStringExtra("book_url");

        tv_book_name = (TextView) findViewById(R.id.tv_book_name);
        tv_book_author = (TextView) findViewById(R.id.tv_book_author);
        tv_book_type = (TextView) findViewById(R.id.tv_book_type);
        tv_book_press = (TextView) findViewById(R.id.tv_book_press);
        tv_book_isbn = (TextView) findViewById(R.id.tv_book_isbn);
        tv_book_outline = (TextView) findViewById(R.id.tv_book_outline);

        scrollView = (ScrollView) findViewById(R.id.book_detail_scrollView);

        if (scrollView != null) {
            scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setEnabled(scrollView.getScrollY() == 0);
                    }
                }
            });
        }

        initSwipeRefresh(book_url);

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
