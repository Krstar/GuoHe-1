package com.example.lyy.newjust;

import android.app.Application;
import android.content.Context;

import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;

import org.litepal.LitePalApplication;

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

    public static Context getContext() {
        return context;
    }

}
