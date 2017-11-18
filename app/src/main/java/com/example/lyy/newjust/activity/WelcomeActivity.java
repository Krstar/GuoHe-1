package com.example.lyy.newjust.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;

import com.example.lyy.newjust.R;
import com.example.lyy.newjust.util.AppConstants;
import com.example.lyy.newjust.util.SpUtils;
import com.github.paolorotolo.appintro.AppIntro;
import com.umeng.analytics.MobclickAgent;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.PermissionListener;

import java.util.List;

/**
 * Created by lyy on 2017/10/24.
 */

public class WelcomeActivity extends AppIntro {

    @Override
    public void init(@Nullable Bundle savedInstanceState) {

        changeStatusBar();

        obtain_permission();//获取权限

        addSlide(SlideFragment.newInstance(R.layout.welcome_layout_1));
        addSlide(SlideFragment.newInstance(R.layout.welcome_layout_2));
        addSlide(SlideFragment.newInstance(R.layout.welcome_layout_3));
        addSlide(SlideFragment.newInstance(R.layout.welcome_layout_4));
        setSeparatorColor(getResources().getColor(R.color.colorBlue));
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

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    //权限获取
    private void obtain_permission() {
        AndPermission.with(this)
                .requestCode(200)
                .permission(
                        Permission.STORAGE,
                        Permission.CAMERA,
                        Permission.MICROPHONE,
                        Permission.PHONE
                )
                .callback(listener)
                .start();
    }

    //权限获取的监听器
    private PermissionListener listener = new PermissionListener() {
        @Override
        public void onSucceed(int requestCode, List<String> grantedPermissions) {
            // 权限申请成功回调。

            // 这里的requestCode就是申请时设置的requestCode。
            // 和onActivityResult()的requestCode一样，用来区分多个不同的请求。
            if (requestCode == 200) {
                // TODO ...
            }
        }

        @Override
        public void onFailed(int requestCode, List<String> deniedPermissions) {
            // 权限申请失败回调。
            if (requestCode == 200) {
                // TODO ...
            }
        }
    };

    //将背景图和状态栏融合到一起
    private void changeStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
}
