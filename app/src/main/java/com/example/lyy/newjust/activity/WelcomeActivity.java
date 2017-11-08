package com.example.lyy.newjust.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import com.example.lyy.newjust.R;
import com.example.lyy.newjust.util.AppConstants;
import com.example.lyy.newjust.util.SpUtils;
import com.github.paolorotolo.appintro.AppIntro;

/**
 * Created by lyy on 2017/10/24.
 */

public class WelcomeActivity extends AppIntro {

    @Override
    public void init(@Nullable Bundle savedInstanceState) {

        changeStatusBar();

        addSlide(SlideFragment.newInstance(R.layout.welcome_layout_1));
        addSlide(SlideFragment.newInstance(R.layout.welcome_layout_2));
        addSlide(SlideFragment.newInstance(R.layout.welcome_layout_3));
        addSlide(SlideFragment.newInstance(R.layout.welcome_layout_4));
        setSeparatorColor(getResources().getColor(R.color.colorAccent));
        setFlowAnimation();
        setVibrateIntensity(30);
        setSkipText("跳过");
        setDoneText("完成");
    }

    @Override
    public void onSkipPressed() {
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(intent);
        SpUtils.putBoolean(WelcomeActivity.this, AppConstants.FIRST_OPEN, true);
        finish();
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onDonePressed() {
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(intent);
        SpUtils.putBoolean(WelcomeActivity.this, AppConstants.FIRST_OPEN, true);
        finish();
    }

    @Override
    public void onSlideChanged() {

    }

    //将背景图和状态栏融合到一起
    private void changeStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }
}
