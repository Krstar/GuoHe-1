package com.example.lyy.newjust;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;

import org.litepal.LitePalApplication;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import shortbread.Shortbread;

/**
 * Created by lyy on 2017/11/15.
 */

public class AndroidApplication extends Application {
    private static Context context;

    private PushAgent pushAgent;

    @Override
    public void onCreate() {
        super.onCreate();
        Shortbread.create(this);
        context = getApplicationContext();

        LitePalApplication.initialize(context);

//        CrashReport.initCrashReport(getApplicationContext(), "3ec47ff7b7", false);

        Context context = getApplicationContext();
        // 获取当前包名
        String packageName = context.getPackageName();
        // 获取当前进程名
        String processName = getProcessName(android.os.Process.myPid());
        // 设置是否为上报进程
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
        // 初始化Bugly
        CrashReport.initCrashReport(context, "3ec47ff7b7", false, strategy);

        pushAgent = PushAgent.getInstance(this);
        //开启推送
        pushAgent.register(new IUmengRegisterCallback() {

            @Override
            public void onSuccess(String deviceToken) {
                //注册成功会返回device token
            }

            @Override
            public void onFailure(String s, String s1) {

            }
        });
        pushAgent.onAppStart();//友盟统计app启动次数，如不开启可能会因为，尝长时间不登录而推送失败
    }

    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

    public static Context getContext() {
        return context;
    }

}
