package com.example.lyy.newjust.activity.School;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.example.lyy.newjust.R;
import com.example.lyy.newjust.util.UrlUtil;
import com.flyco.animation.BounceEnter.BounceBottomEnter;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.MaterialDialog;
import com.githang.statusbar.StatusBarCompat;
import com.umeng.analytics.MobclickAgent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import shortbread.Shortcut;

@Shortcut(id = "school_bus", icon = R.drawable.ic_menu_school_bus, shortLabel = "查校车")
public class SchoolBusActivity extends SwipeBackActivity {

    private Context mContext;

    private ProgressBar progressBar;

    private String checi;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setStatusBarColor(this, Color.rgb(0, 172, 193));
        setContentView(R.layout.activity_school_bus);

        mContext = this;

        setSwipeBackEnable(true);   // 可以调用该方法，设置是否允许滑动退出
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        // 设置滑动方向，可设置EDGE_LEFT, EDGE_RIGHT, EDGE_ALL, EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        // 滑动退出的效果只能从边界滑动才有效果，如果要扩大touch的范围，可以调用这个方法
        mSwipeBackLayout.setEdgeSize(100);

        //设置和toolbar相关的
        Toolbar toolbar = (Toolbar) findViewById(R.id.school_bus_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        progressBar = (ProgressBar) findViewById(R.id.school_bus_progress);

        WebView webView = (WebView) findViewById(R.id.school_bus_webview);
        //支持javascript
        webView.getSettings().setJavaScriptEnabled(true);
        // 设置可以支持缩放
        webView.getSettings().setSupportZoom(true);
        // 设置出现缩放工具
        webView.getSettings().setBuiltInZoomControls(true);
        //扩大比例的缩放
        webView.getSettings().setUseWideViewPort(true);
        //自适应屏幕
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.loadUrl(UrlUtil.SCHOOL_BUS);
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

        hasSchoolBus();
    }

    private void MaterialDialogOneBtn(String mess) {
        final MaterialDialog dialog = new MaterialDialog(mContext);
        dialog.isTitleShow(false)//
                .btnNum(1)
                .content("即将到来的车次是：\n" + mess)//
                .btnText("确定")//
                .showAnim(new BounceBottomEnter())//
                .show();

        dialog.setOnBtnClickL(
                new OnBtnClickL() {//left btn click listener
                    @Override
                    public void onBtnClick() {
                        dialog.dismiss();
                    }
                }
        );
    }

    private void hasSchoolBus() {
        Calendar calendar = Calendar.getInstance();
        //星期天从0开始
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        SimpleDateFormat f = new SimpleDateFormat("HH:mm");
        Date c = new Date(System.currentTimeMillis());
        try {
            String str1 = f.format(c);
            //当前时间,格式为HH：mm
            Date d1 = f.parse(str1);
            if (f.parse("21:55").compareTo(d1) == -1) {
                //没有车了
                checi = "目前没有车了";
            } else if (f.parse("21:05").compareTo(d1) == -1) {
                //16车次
                checi = "车次16\n";
                checi += "21:45 西->21：55 南->东";
            } else if (f.parse("19:40").compareTo(d1) == -1) {
                if (week == 6 || week == 0) {
                    //14
                    //有些车周末周六不开，这种情况跳到下一班车
                    checi = "车次14\n";
                    checi += "19:30 西->19:40 南->东";
                } else {
                    //15
                    checi = "车次15\n";
                    checi += "20:55 西->21：05 南->东";
                }
            } else if (f.parse("18:40").compareTo(d1) == -1) {
                if (week == 0) {
                    //13
                    checi = "车次13\n";
                    checi += "18:30 东->18:40 南->西";
                } else {
                    //14
                    checi = "车次14\n";
                    checi += "19:30 西->19:40 南->东";
                }
            } else if (f.parse("17:50").compareTo(d1) == -1) {
                //13
                checi = "车次13\n";
                checi += "18:30 东->18:40 南->西";
            } else if (f.parse("16:05").compareTo(d1) == -1) {
                //12,11
                checi = "车次11和12\n";
                checi += "17：40 东->17:50 南->西\n";
                checi += "17:40 西->17：50 南->东";
            } else if (f.parse("15:25").compareTo(d1) == -1) {
                if (week == 0) {
                    //9
                    checi = "车次9\n";
                    checi += "15：15 东->15：25 南->西";
                } else {
                    //10
                    checi = "车次10\n";
                    checi += "15：55 西->16：05 南->东";
                }
            } else if (f.parse("13:45").compareTo(d1) == -1) {
                if (week == 0) {
                    //7,8
                    checi = "车次7和8\n";
                    checi += "13:35 东->13：45 南->西\n";
                    checi += "13:35 西->13:45 南->东";
                } else {
                    //9
                    checi = "车次9\n";
                    checi += "15：15 东->15：25 南->西";
                }
            } else if (f.parse("12:00").compareTo(d1) == -1) {
                //7,8
                checi = "车次7和8\n";
                checi += "13:35 东->13：45 南->西\n";
                checi += "13:35 西->13:45 南->东";
            } else if (f.parse("10:05").compareTo(d1) == -1) {
                //5,6
                checi = "车次5和6\n";
                checi += "11:50 东->12:00 南->西\n";
                checi += "11:50 西->12:00 南->东";
            } else if (f.parse("9:30").compareTo(d1) == -1) {
                if (week == 0) {
                    //3
                    checi = "车次3\n";
                    checi += "9:20 东->9:30 南->西\n";
                } else {
                    //4
                    checi = "车次4\n";
                    checi += "9:55 东->10:05 南->西\n";
                }
            } else if (f.parse("7:45").compareTo(d1) == -1) {
                //3
                checi = "车次3\n";
                checi += "9:20 东->9:30 南->西\n";

            } else {
                //2,1
                checi = "车次1和2\n";
                checi += "7:35 东->7:45 南->西\n";
                checi += "7:35 西->7:45 南->东";
            }
            MaterialDialogOneBtn(checi);
        } catch (Exception e) {

        }
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
