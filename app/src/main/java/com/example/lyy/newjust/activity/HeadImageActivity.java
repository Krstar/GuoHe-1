package com.example.lyy.newjust.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.lyy.newjust.R;
import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.ActionSheetDialog;
import com.github.ppamorim.dragger.DraggerActivity;
import com.github.ppamorim.dragger.DraggerPosition;
import com.github.ppamorim.dragger.DraggerView;

import java.io.IOException;

import es.dmoral.toasty.Toasty;

public class HeadImageActivity extends DraggerActivity {

    private static final String TAG = "HeadImageActivity";

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

        Glide.with(this).load(headPicUrl).diskCacheStrategy(DiskCacheStrategy.ALL).into(bg_imageview);
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
    public void onBackPressed() {
        draggerView.closeActivity();
    }
}
