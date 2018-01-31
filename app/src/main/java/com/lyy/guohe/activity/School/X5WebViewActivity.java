package com.lyy.guohe.activity.School;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.githang.statusbar.StatusBarCompat;
import com.lyy.guohe.R;
import com.tencent.smtt.export.external.interfaces.JsPromptResult;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.CookieSyncManager;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import es.dmoral.toasty.Toasty;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class X5WebViewActivity extends SwipeBackActivity {

    private static final String TAG = "X5WebViewActivity";

    private static int index = 0;

    private WebView mWebview;

    private Context mContext;

    private ProgressBar progressBar;

    private OkHttpClient mOkHttpClient;
    private Headers requestHeaders;
    private String cookie;

    private String[] vpn_ACC = {"172210710135", "172210708219", "172210702133", "172210703201", "172210703202", "172210710108"};  //VPN账号数组
    private String[] vpn_Pass = {"220455", "242410", "211375", "033880", "012943", "080021 "};//VPN密码数组

    private String vpn_user;
    private String vpn_pwd;

    private String X5Url = "";
    private String title = "";
    /**
     * 奥兰系统
     */
    private TextView mTitle;


    //0表示正常，-1表示被占用，1表示密码错误,-3表示网络异常
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Log.d(TAG, "handleMessage: " + "vpn正常");
                    break;
                case -1:
                    Log.d(TAG, "handleMessage: " + "vpn被占用");
                    VpnSource(vpn_ACC[index], vpn_Pass[index]);
                    if (index < vpn_ACC.length)
                        index++;
                    else
                        index = 0;
                    CheckThread thread1 = new CheckThread();
                    thread1.start();
                    break;
                case 1:
                    Log.d(TAG, "handleMessage: " + "vpn密码错误");
                    VpnSource(vpn_ACC[index], vpn_Pass[index]);
                    if (index < vpn_ACC.length)
                        index++;
                    else
                        index = 0;
                    CheckThread thread = new CheckThread();
                    thread.start();
                    break;
            }
        }
    };

    class CheckThread extends Thread {

        Message message = new Message();

        @Override
        public void run() {
            super.run();
            String url = "https://vpn.just.edu.cn/dana-na/auth/url_default/login.cgi";
            FormBody formBody = new FormBody.Builder()
                    .add("tz_offset", "480")
                    .add("username", vpn_user)
                    .add("password", vpn_pwd)
                    .add("realm", "LDAP-REALM")
                    .add("btnSubmit", "登录")
                    .build();
            Request request = new Request.Builder()
                    .post(formBody)
                    .url(url)
                    .headers(requestHeaders)
                    .build();

        /*
        0: 正常
        1: 密码错误
        -1: 在其他地方登录
        -3: network error
        */

            mOkHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            message.what = -3;
                            handler.sendMessage(message);
                            Toasty.error(mContext, "网络异常", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String location = response.header("location");
                    response.close();
                    if (location != null && location.contains("welcome.cgi?p=failed")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                message.what = 1;
                                handler.sendMessage(message);
                            }
                        });
                    } else if (location != null && location.contains("welcome.cgi?p=user-confirm")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                message.what = -1;
                                handler.sendMessage(message);
                            }
                        });
                    } else {
                        cookie = response.headers("Set-Cookie").get(0)
                                + ";" + response.headers("Set-Cookie").get(1)
                                + ";" + response.headers("Set-Cookie").get(2);
                        assert location != null;
                        Request request = new Request.Builder().url(location)
                                .headers(requestHeaders).header("Cookie", cookie).build();
                        try {
                            response = mOkHttpClient.newCall(request).execute();
                            cookie = cookie + ";" + response.header("Set-Cookie");
                            response.close();
                            if (cookie != null) {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                                    CookieSyncManager.createInstance(mContext);
                                }
                                CookieManager cookieManager = CookieManager.getInstance();
                                for (String t : cookie.split(";")) {
                                    cookieManager.setCookie(X5Url, t);
                                }
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    initView();
                                    message.what = 0;
                                    handler.sendMessage(message);
                                    mWebview.setInitialScale(100);
                                    mWebview.loadUrl(X5Url);
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setStatusBarColor(this, Color.rgb(0, 172, 193));
        setContentView(R.layout.activity_x5_webview);

        mContext = this;

        setSwipeBackEnable(true);   // 可以调用该方法，设置是否允许滑动退出
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        // 设置滑动方向，可设置EDGE_LEFT, EDGE_RIGHT, EDGE_ALL, EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        // 滑动退出的效果只能从边界滑动才有效果，如果要扩大touch的范围，可以调用这个方法
        mSwipeBackLayout.setEdgeSize(100);

        Intent intent = getIntent();
        X5Url = intent.getStringExtra("url");
        title = intent.getStringExtra("title");

        initView();

        //设置和toolbar相关的
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mTitle.setText(title);

        vpn_user = "1441904121";
        vpn_pwd = "173010";

        VpnSource(vpn_user, vpn_pwd);

        CheckThread thread = new CheckThread();
        thread.start();
    }

    public void VpnSource(String vpn_user, String vpn_pwd) {
        this.vpn_user = vpn_user;
        this.vpn_pwd = vpn_pwd;

        requestHeaders = new Headers.Builder()
                .add("User-Agent",
                        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
                .build();
        X509TrustManager xtm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{xtm}, new SecureRandom());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        assert sslContext != null;
        mOkHttpClient = new OkHttpClient.Builder().sslSocketFactory(sslContext.getSocketFactory())
                .hostnameVerifier(DO_NOT_VERIFY)
                .followRedirects(false)
                .followSslRedirects(false)
                .build();
    }

    /*
    0: 正常
    -1: 密码错误
    -2: 在其他地方登录
    -3: network error
     */
    public void checkVpnUser() {
        String url = "https://vpn.just.edu.cn/dana-na/auth/url_default/login.cgi";
        FormBody formBody = new FormBody.Builder()
                .add("tz_offset", "480")
                .add("username", vpn_user)
                .add("password", vpn_pwd)
                .add("realm", "LDAP-REALM")
                .add("btnSubmit", "登录")
                .build();
        Request request = new Request.Builder()
                .post(formBody)
                .url(url)
                .headers(requestHeaders)
                .build();

        /*
        0: 正常
        1: 密码错误
        -1: 在其他地方登录
        -3: network error
        */

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toasty.error(mContext, "网络异常", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String location = response.header("location");
                response.close();
                if (location != null && location.contains("welcome.cgi?p=failed")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toasty.error(mContext, "vpn密码错误", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (location != null && location.contains("welcome.cgi?p=user-confirm")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toasty.error(mContext, "VPN被占用", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    cookie = response.headers("Set-Cookie").get(0)
                            + ";" + response.headers("Set-Cookie").get(1)
                            + ";" + response.headers("Set-Cookie").get(2);
                    assert location != null;
                    Request request = new Request.Builder().url(location)
                            .headers(requestHeaders).header("Cookie", cookie).build();
                    try {
                        response = mOkHttpClient.newCall(request).execute();
                        cookie = cookie + ";" + response.header("Set-Cookie");
                        response.close();
                        if (cookie != null) {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                                CookieSyncManager.createInstance(mContext);
                            }
                            CookieManager cookieManager = CookieManager.getInstance();
                            for (String t : cookie.split(";")) {
                                cookieManager.setCookie(X5Url, t);
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initView();
                                mWebview.setInitialScale(100);
                                mWebview.loadUrl(X5Url);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private void initView() {
        mWebview = (WebView) findViewById(R.id.webview);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        mTitle = (TextView) findViewById(R.id.title);

        WebSettings webSettings = mWebview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setAppCacheMaxSize(1024 * 1024 * 5);
        webSettings.setUserAgentString("lyy Mozilla/5.0 (Linux; Android; guohe) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.125 Mobile Safari/537.36");
        mWebview.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView webView, int newProgress) {
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);//加载完网页进度条消失
                    if (webView != null && webView.getUrl().contains("202.195.195.198")) {
                        webView.loadUrl("javascript:{" +
                                "var id=document.getElementById('iframe2');" +
                                "id.style.height='388px';" +
                                "id.style.height=(id.contentWindow.document.body.scrollHeight+12).toString()+'px';" +
                                "console.log('已优化');" +
                                "}");
                    }
                } else {
                    progressBar.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
                    progressBar.setProgress(newProgress);//设置进度值
                }
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(X5WebViewActivity.this
                        , R.style.AlertDialogCustom);
                b.setTitle("提示");
                b.setMessage(message);
                b.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                b.setCancelable(false);
                b.create().show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(X5WebViewActivity.this
                        , R.style.AlertDialogCustom);
                b.setTitle("确定");
                b.setMessage(message);
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                });
                b.create().show();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue,
                                      final JsPromptResult result) {
                final View v = View.inflate(view.getContext(), R.layout.browser_prompt_dialog, null);
                AlertDialog.Builder b = new AlertDialog.Builder(X5WebViewActivity.this
                        , R.style.AlertDialogCustom);
                b.setTitle("提示");
                b.setView(v);
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String value = ((EditText) v.findViewById(R.id.browser_prompt_dialog_et))
                                .getText().toString();
                        result.confirm(value);
                    }
                });
                b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                });
                b.create().show();
                return true;
            }
        });
        mWebview.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
                // handler.cancel();// Android默认的处理方式
                sslErrorHandler.proceed();// 接受所有网站的证书
                // handleMessage(Message msg);// 进行其他处理
            }
        });
    }

    public void exitVpn() {
        if (cookie != null) {
            Headers requestHeaders = new Headers.Builder()
                    .add("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
                    .build();
            X509TrustManager xtm = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
            SSLContext sslContext = null;
            try {
                sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new TrustManager[]{xtm}, new SecureRandom());

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
            HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            assert sslContext != null;
            OkHttpClient mOkHttpClient = new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory())
                    .hostnameVerifier(DO_NOT_VERIFY)
                    .followRedirects(false)
                    .followSslRedirects(false)
                    .build();
            Request request = new Request.Builder()
                    .url("https://vpn.just.edu.cn/dana-na/auth/logout.cgi")
                    .headers(requestHeaders)
                    .header("Cookie", cookie)
                    .build();

            mOkHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toasty.error(mContext, "退出VPN失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    response.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toasty.success(mContext, "成功退出VPN", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exitVpn();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebview.canGoBack()) {
            mWebview.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
