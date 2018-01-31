package com.lyy.guohe.activity.Main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.ActionSheetDialog;
import com.github.ppamorim.dragger.DraggerActivity;
import com.github.ppamorim.dragger.DraggerPosition;
import com.github.ppamorim.dragger.DraggerView;
import com.lyy.guohe.R;

import java.io.IOException;

import es.dmoral.toasty.Toasty;

public class HeadImageActivity extends DraggerActivity {


    private DraggerView draggerView;

    private ImageView bg_imageview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉Activity上面的状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_head_image);

        draggerView = (DraggerView) findViewById(R.id.dragger_view);
        draggerView.setDraggerLimit(0.9f);
        draggerView.setDraggerPosition(DraggerPosition.BOTTOM);

        Intent intent = getIntent();
        String headPicUrl = intent.getStringExtra("headPicUrl");

        bg_imageview = (ImageView) findViewById(R.id.bg_imageView);
        bg_imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                draggerView.closeActivity();
            }
        });
        bg_imageview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final String[] stringItems = {"分享", "下载到本地", "设为壁纸"};
                final ActionSheetDialog dialog = new ActionSheetDialog(HeadImageActivity.this, stringItems, null);
                dialog.isTitleShow(false).show();

                dialog.setOnOperItemClickL(new OnOperItemClickL() {
                    @Override
                    public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                        switch (position) {
                            case 0:
                                bg_imageview.setDrawingCacheEnabled(true);
                                Bitmap bitmap = bg_imageview.getDrawingCache();//获取imageview中的图像
                                Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "这是title", "这是description"));
                                shareImg("果核 - 每日一图", "我的主题", "我的分享内容", uri);
                                break;
                            case 1:
                                saveImage(bg_imageview);
                                Toasty.success(HeadImageActivity.this, "图片保存成功", Toast.LENGTH_SHORT).show();
                                break;
                            case 2:
                                setWallpaper1(bg_imageview);
                                Toasty.success(HeadImageActivity.this, "壁纸设置成功", Toast.LENGTH_SHORT).show();
                                break;
                        }
                        dialog.dismiss();
                    }
                });

                return true;
            }
        });

        Glide.with(this).load(headPicUrl).into(bg_imageview);
    }

    private void shareImg(String dlgTitle, String subject, String content,
                          Uri uri) {
        if (uri == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        if (subject != null && !"".equals(subject)) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        if (content != null && !"".equals(content)) {
            intent.putExtra(Intent.EXTRA_TEXT, content);
        }

        // 设置弹出框标题
        if (dlgTitle != null && !"".equals(dlgTitle)) { // 自定义标题
            startActivity(Intent.createChooser(intent, dlgTitle));
        } else { // 系统默认标题
            startActivity(intent);
        }
    }

    private void saveImage(ImageView imageView) {
        imageView.setDrawingCacheEnabled(true);//开启catch，开启之后才能获取ImageView中的bitmap
        Bitmap bitmap = imageView.getDrawingCache();//获取imageview中的图像
        MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "这是title", "这是description");
        imageView.setDrawingCacheEnabled(false);//关闭catch
    }

    private void setWallpaper1(ImageView imageView) {
        imageView.setDrawingCacheEnabled(true);//开启catch，开启之后才能获取ImageView中的bitmap
        Bitmap bmp = imageView.getDrawingCache();//获取imageview中的图像
        try {
            setWallpaper(bmp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        draggerView.closeActivity();
    }
}
