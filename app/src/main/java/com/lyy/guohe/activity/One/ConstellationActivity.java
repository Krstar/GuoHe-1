package com.lyy.guohe.activity.One;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

public class ConstellationActivity extends SwipeBackActivity {

    private WebView webView;

    private ScrollSwipeRefreshLayout mWaveSwipeRefreshLayout;

    private ProgressBar progressBar;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_constellation);

        StatusBarCompat.setStatusBarColor(this, Color.rgb(0, 172, 193));

        Intent intent = getIntent();
        String constellation_en = intent.getStringExtra("constellation_en");

        String url = UrlUtil.CONSTELLATION + constellation_en;

        setSwipeBackEnable(true);   // 可以调用该方法，设置是否允许滑动退出
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        // 设置滑动方向，可设置EDGE_LEFT, EDGE_RIGHT, EDGE_ALL, EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        // 滑动退出的效果只能从边界滑动才有效果，如果要扩大touch的范围，可以调用这个方法
        mSwipeBackLayout.setEdgeSize(200);

        //设置和toolbar相关的
        Toolbar toolbar = (Toolbar) findViewById(R.id.constellation_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        progressBar = (ProgressBar) findViewById(R.id.collapsing_progress);

        webView = (WebView) findViewById(R.id.constellation_web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                String script1 = "for(var i=22;i<44;i++)\n" +
                        "    document.getElementsByTagName('div')[i].remove()\n";
                String script2 = "document.getElementsByTagName('img')[document.getElementsByTagName('img').length-1].remove()\n";
                String script3 = "document.getElementsByClassName('b_tab date_tab')[0].remove()";
                view.loadUrl("javascript:" + script2);
                view.loadUrl("javascript:" + script1);
                view.loadUrl("javascript:"+script3);

                super.onPageFinished(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }
        });
        webView.loadUrl(url);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                String script4 = "document.getElementsByTagName('header')[0].remove()";
                String script5="document.getElementsByClassName('lday clear swiper-container-horizontal swiper-container-free-mode')[0].remove()";

                view.loadUrl("javascript:" + script4);
                view.loadUrl("javascript:" + script5);

                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);//加载完网页进度条消失
                } else {
                    progressBar.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
                    progressBar.setProgress(newProgress);//设置进度值
                }
            }
        });

        mWaveSwipeRefreshLayout = (ScrollSwipeRefreshLayout) findViewById(R.id.constellation_swipe);
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
                }, 2000);

            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack(); //goBack()表示返回WebView的上一页面
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
