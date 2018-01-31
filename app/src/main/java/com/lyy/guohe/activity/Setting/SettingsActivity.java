package com.lyy.guohe.activity.Setting;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lyy.guohe.R;
import com.lyy.guohe.base.PopupActivity;
import com.lyy.guohe.util.AppConstants;
import com.lyy.guohe.util.SpUtils;
import com.lyy.guohe.util.UrlUtil;
import com.lyy.guohe.util.VersionUtils;
import com.githang.statusbar.StatusBarCompat;
import com.tencent.bugly.beta.Beta;

import java.io.File;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class SettingsActivity extends SwipeBackActivity implements View.OnClickListener {

    private Context mContext;

    public static SwipeBackActivity SettingActivity;

    private TextView mTvCheckInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SettingActivity = this;
        mContext = SettingsActivity.this;

        StatusBarCompat.setStatusBarColor(this, Color.rgb(255, 255, 255));
        setContentView(R.layout.activity_settings);

        setSwipeBackEnable(true);   // 可以调用该方法，设置是否允许滑动退出
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        // 设置滑动方向，可设置EDGE_LEFT, EDGE_RIGHT, EDGE_ALL, EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        // 滑动退出的效果只能从边界滑动才有效果，如果要扩大touch的范围，可以调用这个方法
        mSwipeBackLayout.setEdgeSize(200);
        init();

    }

    private void init() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_back_blue);
        }

        /*
      默认当前版本0.0.1
     */
        mTvCheckInfo = (TextView) findViewById(R.id.tv_checkInfo);
        String app_version = VersionUtils.getVerName(mContext);
        if (app_version != null) {
            mTvCheckInfo.setText("当前版本:" + app_version);
        }

        LinearLayout ll_profile = (LinearLayout) findViewById(R.id.ll_profile);
        LinearLayout ll_feedback = (LinearLayout) findViewById(R.id.ll_feedback);
        LinearLayout ll_about_us = (LinearLayout) findViewById(R.id.ll_about_us);
        LinearLayout ll_share = (LinearLayout) findViewById(R.id.ll_share);
        LinearLayout ll_show_grade = (LinearLayout) findViewById(R.id.ll_show_grade);
        LinearLayout ll_upgrade = (LinearLayout) findViewById(R.id.ll_upgrade);
        LinearLayout ll_join_us = (LinearLayout) findViewById(R.id.ll_join_us);
        LinearLayout ll_contact_me = (LinearLayout) findViewById(R.id.ll_contact_me);
        LinearLayout ll_updateInfo = (LinearLayout) findViewById(R.id.ll_updateInfo);

        ll_profile.setOnClickListener(this);
        ll_feedback.setOnClickListener(this);
        ll_about_us.setOnClickListener(this);
        ll_share.setOnClickListener(this);
        ll_show_grade.setOnClickListener(this);
        ll_upgrade.setOnClickListener(this);
        ll_join_us.setOnClickListener(this);
        ll_contact_me.setOnClickListener(this);
        ll_updateInfo.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_profile:
                Intent profile_Intent = new Intent(mContext, ProfileActivity.class);
                startActivity(profile_Intent);
                break;
            case R.id.ll_feedback:
                Intent feedbackIntent = new Intent(mContext, FeedBackActivity.class);
                startActivity(feedbackIntent);
                break;
            case R.id.ll_about_us:
                Intent aboutIntent = new Intent(mContext, PopupActivity.class);
                aboutIntent.putExtra("URL", UrlUtil.ABOUT_US);
                startActivity(aboutIntent);
                break;
            case R.id.ll_share:
                shareApp();
                break;
            case R.id.ll_show_grade:
                showSingleChoiceDialog();
                break;
            case R.id.ll_upgrade:
                Beta.checkUpgrade();
                break;
            case R.id.ll_join_us:
                if (checkApkExist(this, "com.tencent.mobileqq")) {
                    joinQQGroup("DqWWi3II6MaKcmTVy2mH_SVwgzR_bGs8");
                } else {
                    Toast.makeText(this, "本机未安装QQ应用", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.ll_contact_me:
                if (checkApkExist(this, "com.tencent.mobileqq")) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=" + "420326369" + "&version=1")));
                } else {
                    Toast.makeText(this, "本机未安装QQ应用", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.ll_updateInfo:
                Intent updateInfoIntent = new Intent(getApplicationContext(), PopupActivity.class);
                updateInfoIntent.putExtra("title","版本更新说明");
                updateInfoIntent.putExtra("URL",UrlUtil.UPDATE_INFO);
                startActivity(updateInfoIntent);
                break;
        }
    }

    public boolean checkApkExist(Context context, String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /****************
     *
     * 发起添加群流程。群号：果核 内测(673515498) 的 key 为： DqWWi3II6MaKcmTVy2mH_SVwgzR_bGs8
     * 调用 joinQQGroup(DqWWi3II6MaKcmTVy2mH_SVwgzR_bGs8) 即可发起手Q客户端申请加群 果核 内测(673515498)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回fals表示呼起失败
     ******************/
    public boolean joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }


    int yourChoice;

    private void showSingleChoiceDialog() {
        final String[] items = {"按学年查询", "按学科类型查询"};
        yourChoice = 0;
        AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(mContext);
        singleChoiceDialog.setTitle("请选择成绩查看方式");
        // 第二个参数是默认选项，此处设置为0
        singleChoiceDialog.setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        yourChoice = which;
                    }
                });
        singleChoiceDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (yourChoice) {
                            case 0:
                                SpUtils.putString(getApplicationContext(), AppConstants.SHOW_GRADE, "学年");
                                break;
                            case 1:
                                SpUtils.putString(getApplicationContext(), AppConstants.SHOW_GRADE, "学科");
                                break;
                            default:
                                break;
                        }
                    }
                });
        singleChoiceDialog.show();
    }

    private void shareApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, "我发现了一个不错的应用哦：" + UrlUtil.APP);
            intent.setType("text/plain");
            startActivity(Intent.createChooser(intent, "果核"));
        } else {
            final AlertDialog.Builder normalDialog =
                    new AlertDialog.Builder(SettingsActivity.this);
            normalDialog.setTitle("果核");
            normalDialog.setMessage("请选择分享方式");
            normalDialog.setPositiveButton("分享应用",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //...To-do
                            try {
                                File apkFile = new File(getPackageManager().getPackageInfo("com.example.lyy.newjust", 0).applicationInfo.sourceDir);
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_SEND);
                                intent.setType("*/*");
                                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(apkFile));
                                startActivity(intent);
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            normalDialog.setNegativeButton("分享链接",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //...To-do
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_TEXT, "我发现了一个不错的应用哦：" + UrlUtil.APP);
                            intent.setType("text/plain");
                            startActivity(Intent.createChooser(intent, "果核"));
                        }
                    });
            // 显示
            normalDialog.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
