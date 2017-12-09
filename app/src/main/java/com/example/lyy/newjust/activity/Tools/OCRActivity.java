package com.example.lyy.newjust.activity.Tools;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.GeneralParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.WordSimple;
import com.baidu.ocr.ui.camera.CameraActivity;
import com.example.lyy.newjust.R;
import com.example.lyy.newjust.util.FileUtil;
import com.githang.statusbar.StatusBarCompat;
import com.roger.catloadinglibrary.CatLoadingView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class OCRActivity extends SwipeBackActivity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_CAMERA = 1000;
    private static final int REQUEST_CODE_PICK_IMAGE = 2000;

    private String path = "";
    private List<String> resultList = new ArrayList<>();

    CatLoadingView mView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setStatusBarColor(this, Color.rgb(255, 255, 255));
        setContentView(R.layout.activity_ocr);

        setSwipeBackEnable(true);   // 可以调用该方法，设置是否允许滑动退出
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        // 设置滑动方向，可设置EDGE_LEFT, EDGE_RIGHT, EDGE_ALL, EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        // 滑动退出的效果只能从边界滑动才有效果，如果要扩大touch的范围，可以调用这个方法
        mSwipeBackLayout.setEdgeSize(200);

        initView();
    }

    private void initView() {

        mView = new CatLoadingView();

        CircleImageView photo_btn = (CircleImageView) findViewById(R.id.photo_btn);
        photo_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(OCRActivity.this, CameraActivity.class);
                intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                        FileUtil.getSaveFile(getApplication()).getAbsolutePath());
                intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                        CameraActivity.CONTENT_TYPE_GENERAL);
                mView.show(getSupportFragmentManager(), "");
                startActivityForResult(intent, REQUEST_CODE_CAMERA);
            }
        });
    }

    private void showNormalDialog(final String result) {
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(OCRActivity.this);
        normalDialog.setTitle("识别结果");
        normalDialog.setMessage(result);
        normalDialog.setPositiveButton("复制到剪贴板",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        // 将文本内容放到系统剪贴板里。
                        cm.setText(result);
                        Toast.makeText(OCRActivity.this, "文本已复制，快去粘贴吧", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
        normalDialog.setNegativeButton("关闭弹窗",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        dialog.dismiss();
                    }
                });
        // 显示
        normalDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == 0) {
            mView.dismiss();
            Toast.makeText(getApplicationContext(), "你取消了本次识别", Toast.LENGTH_SHORT).show();
        }
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String filePath = getRealPathFromURI(uri);
            path = filePath;
            initData();
        }

        if (requestCode == REQUEST_CODE_CAMERA && resultCode == RESULT_OK) {
            path = FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath();
            initData();
        } else {
            mView.dismiss();
            Toast.makeText(getApplicationContext(), "出错了", Toast.LENGTH_SHORT).show();
        }
    }

    private void initData() {
        OCR.getInstance()
                .initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
                    @Override
                    public void onResult(AccessToken result) {
                        //String token = result.getAccessToken();
                        OCR.getInstance().initWithToken(getApplicationContext(), result);
                        getData();
                    }

                    @Override
                    public void onError(OCRError error) {
                        Log.d(TAG, "onError: " + error);
                    }
                }, getApplicationContext(), "C5vpONIBbMh5El673vDOo5P0", "B0xGzqPIXSpoAny1nr05wvm8uEbacAlf");
    }

    private void getData() {
        GeneralParams param = new GeneralParams();
        param.setImageFile(new File(path));
        OCR.getInstance().recognizeGeneral(param, new OnResultListener<GeneralResult>() {
            @Override
            public void onResult(GeneralResult result) {
                StringBuilder sb = new StringBuilder();
                for (WordSimple word : result.getWordList()) {
                    sb.append(word.getWords());
                    resultList.add(word.getWords());
                    sb.append("\n");
                }
                mView.dismiss();
                showNormalDialog(sb.toString());
            }

            @Override
            public void onError(OCRError ocrError) {
                Toast.makeText(OCRActivity.this, "错误：" + ocrError.getErrorCode() + "  " + ocrError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}
