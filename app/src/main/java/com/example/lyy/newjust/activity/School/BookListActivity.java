package com.example.lyy.newjust.activity.School;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.lyy.newjust.R;
import com.example.lyy.newjust.adapter.Book;
import com.example.lyy.newjust.adapter.BookAdapter;
import com.example.lyy.newjust.util.HttpUtil;
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

public class BookListActivity extends SwipeBackActivity {

    private static final String TAG = "BookListActivity";

    private Context mContext;

    private ListView lv_book_list;

    private List<Book> bookList = new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;

    private SwipeRefreshLayout.OnRefreshListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setStatusBarColor(this, Color.rgb(0, 172, 193));
        setContentView(R.layout.activity_book_list);

        mContext = this;

        setSwipeBackEnable(true);   // 可以调用该方法，设置是否允许滑动退出
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        // 设置滑动方向，可设置EDGE_LEFT, EDGE_RIGHT, EDGE_ALL, EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        // 滑动退出的效果只能从边界滑动才有效果，如果要扩大touch的范围，可以调用这个方法
        mSwipeBackLayout.setEdgeSize(100);

        //设置和toolbar相关的
        Toolbar toolbar = (Toolbar) findViewById(R.id.book_list_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        lv_book_list = (ListView) findViewById(R.id.lv_book_list);

        Intent intent = getIntent();
        String keyword = intent.getStringExtra("keyword");

        lv_book_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Book book = bookList.get(position);
                Intent bookDetailIntent = new Intent(BookListActivity.this, BookDetailActivity.class);
                bookDetailIntent.putExtra("book_url", book.getBook_url());
                startActivity(bookDetailIntent);
            }
        });

        initSwipeRefresh(keyword);

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        listener.onRefresh();

    }


    private void initSwipeRefresh(final String keyword) {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);

        // 设置颜色属性的时候一定要注意是引用了资源文件还是直接设置16进制的颜色，因为都是int值容易搞混
        // 设置下拉进度的背景颜色，默认就是白色的
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        // 设置下拉进度的主题颜色
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);

        listener = new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                //TODO
                searchBookList(keyword);
            }
        };

        swipeRefreshLayout.setOnRefreshListener(listener);
    }


    //查询图书列表
    private void searchBookList(String bookName) {
        bookList.clear();
        String url = UrlUtil.BOOK_LIST;

        RequestBody requestBody = new FormBody.Builder()
                .add("bookName", bookName)
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
                    try {
                        JSONArray array = new JSONArray(data);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            String book_author_press = object.getString("book_author_press");
                            String book_can_borrow = object.getString("book_can_borrow");
                            String book_url = object.getString("book_url");
                            String book_title = object.getString("book_title");
                            Log.d(TAG, "onResponse: " + book_author_press);
                            Log.d(TAG, "onResponse: " + book_can_borrow);
                            Log.d(TAG, "onResponse: " + book_url);
                            Log.d(TAG, "onResponse: " + book_title);

                            Book book = new Book(book_title, book_author_press, book_can_borrow, book_url);
                            bookList.add(book);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                BookAdapter bookAdapter = new BookAdapter(BookListActivity.this, R.layout.item_book_list, bookList);
                                lv_book_list.setAdapter(bookAdapter);
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
