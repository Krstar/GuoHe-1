package com.example.lyy.newjust.activity.Tools;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
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
import com.roger.catloadinglibrary.CatLoadingView;
import com.umeng.analytics.MobclickAgent;

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

    private String path = new String();
    private TextView txtResult;//返回的数据
    private List<String> resultList = new ArrayList<>();

    CatLoadingView mView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        txtResult = (TextView) findViewById(R.id.tv_ocr_result);
        txtResult.setTextIsSelectable(true);

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
                txtResult.setText(sb);
            }

            @Override
            public void onError(OCRError ocrError) {
                txtResult.setText(ocrError.getErrorCode() + "  " + ocrError.getMessage());
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

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
