package com.lyy.guohe.activity.School;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.githang.statusbar.StatusBarCompat;
import com.lyy.guohe.R;
import com.lyy.guohe.model.Res;
import com.lyy.guohe.util.HttpUtil;
import com.lyy.guohe.util.ResponseUtil;
import com.lyy.guohe.util.UrlUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import es.dmoral.toasty.Toasty;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CETActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CETActivity";
    /**
     * 请输入你的姓名
     */
    private EditText mEtName;
    /**
     * 请输入你的身份证号
     */
    private EditText mEtSfzh;
    /**
     * 点击查询
     */
    private Button mBtnSearch;

    private int type = 1;

    private Context mContext;
    private ImageView mIvBgCET;
    /**
     * 四级
     */
    private RadioButton mRdCet4;
    /**
     * 六级
     */
    private RadioButton mRdCet6;
    private RadioGroup mRdCet;

    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StatusBarCompat.setStatusBarColor(this, Color.rgb(251, 252, 240));

        setContentView(R.layout.activity_cet);

        mContext = this;

        initView();
    }


    private void initView() {
        mEtName = (EditText) findViewById(R.id.et_name);
        mEtSfzh = (EditText) findViewById(R.id.et_sfzh);
        mBtnSearch = (Button) findViewById(R.id.btn_search);
        mBtnSearch.setOnClickListener(this);
        ImageView mIvBgCET = (ImageView) findViewById(R.id.iv_bgCET);
        Glide.with(mContext).load(R.drawable.bg_cet).into(mIvBgCET);
        mRdCet4 = (RadioButton) findViewById(R.id.rd_cet4);
        mRdCet6 = (RadioButton) findViewById(R.id.rd_cet6);
        mRdCet = (RadioGroup) findViewById(R.id.rd_cet);
        mRdCet4.setChecked(true);
        mRdCet.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rd_cet4:
                        type = 1;
                        break;
                    case R.id.rd_cet6:
                        type = 2;
                        break;
                }
            }
        });

        mProgressDialog = new ProgressDialog(CETActivity.this);
        mProgressDialog.setMessage("查询中,请稍后……");
    }

    private void requestCET() {
        String xm = mEtName.getText().toString();
        String sfzh = mEtSfzh.getText().toString();

        if (xm != null && sfzh != null && !xm.equals("") && !sfzh.equals("")) {
            runOnUiThread(() -> {
                if (!mProgressDialog.isShowing())
                    mProgressDialog.show();
            });
            RequestBody requestBody = new FormBody.Builder()
                    .add("ks_xm", xm)
                    .add("ks_sfz", sfzh)
                    .add("type", String.valueOf(type))
                    .build();

            HttpUtil.sendPostHttpRequest(UrlUtil.CET_BACK_URL, requestBody, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        if (mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        Toasty.error(mContext, "服务器异常，请稍后重试", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String data = response.body().string();
                        if (data.length() > 3) {
                            final Res res = ResponseUtil.handleResponse(data);
                            assert res != null;
                            if (res.getCode() == 200) {
                                try {
                                    JSONObject object = new JSONObject(res.getInfo());
                                    final String zkzh = object.getString("zkzh");
                                    Log.d(TAG, "onResponse: " + zkzh);
                                    runOnUiThread(() -> {
                                        if (mProgressDialog.isShowing())
                                            mProgressDialog.dismiss();
                                        showResultDialog(zkzh);
                                    });
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                runOnUiThread(() -> {
                                    if (mProgressDialog.isShowing())
                                        mProgressDialog.dismiss();
                                    Toasty.error(mContext, "服务器异常,请稍后重试", Toast.LENGTH_SHORT).show();
                                });
                            }
                        } else {
                            runOnUiThread(() -> {
                                if (mProgressDialog.isShowing())
                                    mProgressDialog.dismiss();
                                Toasty.error(mContext, "服务器异常，请稍后", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        if (response.code() == 502) {
                            runOnUiThread(() -> {
                                if (mProgressDialog.isShowing())
                                    mProgressDialog.dismiss();
                                Toasty.error(mContext, "当前查询人数过多，请稍后重试", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            runOnUiThread(() -> {
                                if (mProgressDialog.isShowing())
                                    mProgressDialog.dismiss();
                                Toasty.error(mContext, "服务器异常，请稍后", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                }
            });
        } else {
            Toasty.warning(mContext, "请输入有效字符", Toast.LENGTH_SHORT).show();
        }

    }

    private void showResultDialog(final String result) {
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog = new AlertDialog.Builder(CETActivity.this);
        normalDialog.setTitle("查询结果");
        normalDialog.setMessage("你的准考证号：\n" + result);
        normalDialog.setPositiveButton("复制到剪贴板",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        //获取剪贴板管理器：
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData mClipData = ClipData.newPlainText("Label", result);
                        cm.setPrimaryClip(mClipData);
                        Toasty.success(mContext, "已复制到剪贴板，快去官网粘贴你的准考证吧", Toast.LENGTH_SHORT).show();
                    }
                });
        // 显示
        normalDialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.btn_search:
                requestCET();
                break;
        }
    }
}
