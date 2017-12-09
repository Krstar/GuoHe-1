package com.example.lyy.newjust.activity.Tools;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lyy.newjust.R;
import com.example.lyy.newjust.activity.One.PopupActivity;
import com.example.lyy.newjust.util.Audio;
import com.githang.statusbar.StatusBarCompat;
import com.shinelw.library.ColorArcProgressBar;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.PermissionListener;

import java.util.List;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class AudioActivity extends SwipeBackActivity implements View.OnClickListener {

    //    private TextView volume;//显示音量的文本框
    Audio MyAudio;//自己写的用于获取音量的类
    public MyHandler myHandler;//用于传递数据给主线程更新UI

    private ColorArcProgressBar progressbar;

    private LinearLayout mLayout;
    //private NewCreditSesameView newCreditSesameView;

    private final int[] mColors = new int[]{
            Color.rgb(245, 222, 179),
            Color.rgb(255, 228, 181),
            Color.rgb(255, 165, 0)
    };
    private TextView mTvAudioDegree;
    /**
     * 分贝说明
     */
    private Button mBtnAudioInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        initView();

        obtain_permission();

        StatusBarCompat.setStatusBarColor(this, Color.rgb(0, 172, 193));

        setSwipeBackEnable(true);   // 可以调用该方法，设置是否允许滑动退出
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        // 设置滑动方向，可设置EDGE_LEFT, EDGE_RIGHT, EDGE_ALL, EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        // 滑动退出的效果只能从边界滑动才有效果，如果要扩大touch的范围，可以调用这个方法
        mSwipeBackLayout.setEdgeSize(200);

        //设置和toolbar相关的
        Toolbar toolbar = (Toolbar) findViewById(R.id.audio_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        mLayout = (LinearLayout) findViewById(R.id.layout);
//        newCreditSesameView = (NewCreditSesameView) findViewById(R.id.sesame_view);
        /*
         * 进行一些，初始化
         */
        //volume = (TextView) findViewById(R.id.volume);
        myHandler = new MyHandler();
        MyAudio = new Audio(myHandler);
        MyAudio.getNoiseLevel();//获取音量

        progressbar = (ColorArcProgressBar) findViewById(R.id.bar1);

    }

    //权限获取
    private void obtain_permission() {
        AndPermission.with(this)
                .requestCode(200)
                .permission(
                        Permission.STORAGE,
                        Permission.CAMERA,
                        Permission.MICROPHONE
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
                Toast.makeText(getApplicationContext(), "您还未获取权限", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void initView() {
        mTvAudioDegree = (TextView) findViewById(R.id.tv_audio_degree);
        mBtnAudioInfo = (Button) findViewById(R.id.btn_audio_info);
        mBtnAudioInfo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.btn_audio_info:
                String audioUrl = "https://wapbaike.baidu.com/item/%E5%88%86%E8%B4%9D/553473?fr=aladdin";
                Intent intent = new Intent(AudioActivity.this, PopupActivity.class);
                intent.putExtra("URL", audioUrl);
                startActivity(intent);
                break;
        }
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            /*
             * 当收到message的时候，这个函数被执行
             * 读取message的数据，并显示到文本框中
             */
            super.handleMessage(msg);
            Bundle b = msg.getData();
            Double sound = b.getDouble("sound");
            //AudioActivity.this.volume.setText(sound + "" + " 分贝");
            progressbar.setCurrentValues(new Double(sound).intValue());
            if (sound < 20) {
                mTvAudioDegree.setText("安静");
            } else if (sound >= 20 && sound <= 60) {
                mTvAudioDegree.setText("一般");
            } else if (sound > 60 && sound < 80) {
                mTvAudioDegree.setText("较吵");
            } else if (sound >= 80) {
                mTvAudioDegree.setText("非常吵闹");
            }
//            newCreditSesameView.setSesameValues(new Double(sound*10).intValue());
//            startColorChangeAnim();
        }
    }

    public void startColorChangeAnim() {
        ObjectAnimator animator = ObjectAnimator.ofInt(mLayout, "backgroundColor", mColors);
        animator.setDuration(3000);
        animator.setEvaluator(new ArgbEvaluator());
        animator.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                MyAudio.cancel();
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyAudio.cancel();
    }

    @Override
    public void onBackPressed() {
        MyAudio.cancel();
        finish();
        super.onBackPressed();
    }
}
