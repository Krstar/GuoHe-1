package com.example.lyy.newjust.activity.Setting;

import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.example.lyy.newjust.R;
import com.example.lyy.newjust.base.ScrollSwipeRefreshLayout;
import com.example.lyy.newjust.util.UrlUtil;
import com.githang.statusbar.StatusBarCompat;
import com.umeng.analytics.MobclickAgent;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

public class AboutUSActivity extends AppCompatActivity {

    private WebView webView;

    private ScrollSwipeRefreshLayout mWaveSwipeRefreshLayout;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        StatusBarCompat.setStatusBarColor(this, Color.rgb(0, 172, 193));

        //设置和toolbar相关的
        Toolbar toolbar = (Toolbar) findViewById(R.id.about_us_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        String url = UrlUtil.ABOUT_US;

        webView = (WebView) findViewById(R.id.about_us_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(url);

        progressBar = (ProgressBar) findViewById(R.id.about_us_progress);
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

        mWaveSwipeRefreshLayout = (ScrollSwipeRefreshLayout) findViewById(R.id.about_us_swipe);
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
                        webView.loadUrl(webView.getUrl());
                        mWaveSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 2000);

            }
        });
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

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
