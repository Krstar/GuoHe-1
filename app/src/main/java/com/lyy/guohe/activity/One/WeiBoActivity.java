package com.lyy.guohe.activity.One;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.lyy.guohe.R;
import com.lyy.guohe.base.ScrollSwipeRefreshLayout;
import com.lyy.guohe.util.UrlUtil;
import com.githang.statusbar.StatusBarCompat;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class WeiBoActivity extends SwipeBackActivity {

    private WebView webView;

    private ScrollSwipeRefreshLayout mWaveSwipeRefreshLayout;

    private ProgressBar progressBar;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wei_bo);

        StatusBarCompat.setStatusBarColor(this, Color.rgb(0, 172, 193));

        String url = UrlUtil.DI_QI;

        setSwipeBackEnable(true);   // 可以调用该方法，设置是否允许滑动退出
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        // 设置滑动方向，可设置EDGE_LEFT, EDGE_RIGHT, EDGE_ALL, EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        // 滑动退出的效果只能从边界滑动才有效果，如果要扩大touch的范围，可以调用这个方法
        mSwipeBackLayout.setEdgeSize(200);

        //设置和toolbar相关的
        Toolbar toolbar = (Toolbar) findViewById(R.id.weibo_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        progressBar = (ProgressBar) findViewById(R.id.weibo_progress);

        webView = (WebView) findViewById(R.id.weibo_web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            //复写shouldOverrideUrlLoading()方法，使得打开网页时不调用系统浏览器， 而是在本WebView中显示
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // TODO 自动生成的方法存根

                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);//加载完网页进度条消失
                } else {
                    progressBar.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
                    progressBar.setProgress(newProgress);//设置进度值
                }
            }
        });

        mWaveSwipeRefreshLayout = (ScrollSwipeRefreshLayout) findViewById(R.id.weibo_swipe);
        mWaveSwipeRefreshLayout.setViewGroup(webView);
        //设置转的圈的颜色
        mWaveSwipeRefreshLayout.setColorSchemeColors(Color.WHITE, Color.WHITE);
        //设置水波纹的颜色
        mWaveSwipeRefreshLayout.setWaveColor(Color.rgb(0, 172, 193));
        mWaveSwipeRefreshLayout.setOnRefreshListener(new WaveSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Do work to refresh the list here.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        webView.clearCache(true);
                        webView.loadUrl(webView.getUrl());
                        mWaveSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 3000);

            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();// 返回前一个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                webView.clearCache(true);
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
