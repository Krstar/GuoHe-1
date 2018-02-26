package com.lyy.guohe.activity.Main;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lyy.guohe.R;
import com.lyy.guohe.model.Res;
import com.lyy.guohe.util.AppConstants;
import com.lyy.guohe.util.HttpUtil;
import com.lyy.guohe.util.ResponseUtil;
import com.lyy.guohe.util.SpUtils;
import com.lyy.guohe.util.UrlUtil;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.json.JSONObject;

import java.io.IOException;

import es.dmoral.toasty.Toasty;
import jp.wasabeef.glide.transformations.BlurTransformation;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import shem.com.materiallogin.DefaultLoginView;
import shem.com.materiallogin.DefaultRegisterView;
import shem.com.materiallogin.MaterialLoginView;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeStatusBar();
        setContentView(R.layout.activity_login);

        obtain_permission();

        ImageView iv_bg_login = (ImageView) findViewById(R.id.iv_bg_login);
        Glide.with(this).load(R.drawable.bg_login).bitmapTransform(new BlurTransformation(this, 10)).into(iv_bg_login);

        Toasty.warning(getApplicationContext(), "请用教务系统的账号和密码登录", Toast.LENGTH_SHORT).show();

        final MaterialLoginView login = (MaterialLoginView) findViewById(R.id.login);
        ((DefaultLoginView) login.getLoginView()).setListener(new DefaultLoginView.DefaultLoginViewListener() {
            @Override
            public void onLogin(TextInputLayout loginUser, TextInputLayout loginPass) {
                //Handle login
                final String user = loginUser.getEditText().getText().toString();
                if (user.isEmpty()) {
                    loginUser.setError("学号不能为空");
                    return;
                }
                loginUser.setError("");

                final String pass = loginPass.getEditText().getText().toString();
                if (pass.isEmpty()) {
                    loginPass.setError("密码不能为空");
                    return;
                }
                loginPass.setError("");

                final ProgressDialog dialog = ProgressDialog.show(LoginActivity.this, null, "正在登录,请稍后……", true, false);

                String url = UrlUtil.USER_INFO;
                RequestBody requestBody = new FormBody.Builder()
                        .add("username", user)
                        .add("password", pass)
                        .build();
                HttpUtil.sendPostHttpRequest(url, requestBody, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        dialog.dismiss();
                        Looper.prepare();
                        Toasty.warning(getApplicationContext(), "服务器异常，请稍后重试", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        SpUtils.putBoolean(getApplicationContext(), AppConstants.LOGIN, true);
                        String data = response.body().string();
                        if (response.isSuccessful()) {
                            Res res = ResponseUtil.handleResponse(data);
                            assert res != null;
                            if (res.getCode() == 200) {
                                try {
                                    JSONObject object = new JSONObject(res.getInfo());
                                    String academy = object.getString("academy");
                                    String name = object.getString("name");
                                    String major = object.getString("major");
                                    String stu_id = object.getString("class_num");

                                    SpUtils.putString(getApplicationContext(), AppConstants.STU_ID, user);
                                    SpUtils.putString(getApplicationContext(), AppConstants.STU_PASS, pass);
                                    SpUtils.putString(getApplicationContext(), AppConstants.STU_ACADEMY, academy);
                                    SpUtils.putString(getApplicationContext(), AppConstants.STU_NAME, name);
                                    SpUtils.putString(getApplicationContext(), AppConstants.STU_MAJOR, major);

                                    SpUtils.putBoolean(getApplicationContext(), AppConstants.LOGIN, true);

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (dialog.isShowing())
                                                dialog.dismiss();
                                            Toasty.success(getApplicationContext(), "登录成功!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Looper.prepare();
                                if (dialog.isShowing())
                                    dialog.dismiss();
                                Toasty.error(LoginActivity.this, res.getMsg(), Toast.LENGTH_SHORT).show();
                                Looper.loop();
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (dialog.isShowing())
                                        dialog.dismiss();
                                    Toasty.error(getApplicationContext(), "服务器异常，请稍后", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }
        });

        ((DefaultRegisterView) login.getRegisterView()).setListener(new DefaultRegisterView.DefaultRegisterViewListener() {
            @Override
            public void onRegister(TextInputLayout registerUser, TextInputLayout registerPass, TextInputLayout registerPassRep) {
                Toast.makeText(getApplicationContext(), "当前暂未开通注册功能", Toast.LENGTH_SHORT).show();

                String user = registerUser.getEditText().getText().toString();
                if (user.isEmpty()) {
                    registerUser.setError("User name can't be empty");
                    return;
                }
                registerUser.setError("");

                String pass = registerPass.getEditText().getText().toString();
                if (pass.isEmpty()) {
                    registerPass.setError("Password can't be empty");
                    return;
                }
                registerPass.setError("");

                String passRep = registerPassRep.getEditText().getText().toString();
                if (!pass.equals(passRep)) {
                    registerPassRep.setError("Passwords are different");
                    return;
                }
                registerPassRep.setError("");

                Snackbar.make(login, "Register success!", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SpUtils.putBoolean(getApplicationContext(), AppConstants.LOGIN, false);
        finish();
    }

    //将背景图和状态栏融合到一起
    private void changeStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //权限获取
    private void obtain_permission() {
        RxPermissions rxPermissions = new RxPermissions(LoginActivity.this);
        rxPermissions
                .request(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.SET_WALLPAPER,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.VIBRATE,
                        Manifest.permission.READ_PHONE_STATE
                )
                .subscribe(granted -> {
                    if (granted) {
                        Toast.makeText(LoginActivity.this, "同意权限", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "拒绝权限", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
