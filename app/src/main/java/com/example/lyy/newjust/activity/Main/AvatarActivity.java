package com.example.lyy.newjust.activity.Main;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.lyy.newjust.R;
import com.example.lyy.newjust.util.AppConstants;
import com.example.lyy.newjust.util.SpUtils;
import com.umeng.analytics.MobclickAgent;

import java.io.ByteArrayInputStream;

public class AvatarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉Activity上面的状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_avatar);

        String imageBase64=SpUtils.getString(this, AppConstants.IMAGE_BASE_64);

        ImageView iv_avator = (ImageView) findViewById(R.id.iv_avator);
        if (imageBase64 != null) {
            byte[] byte64 = Base64.decode(imageBase64, 0);
            ByteArrayInputStream bais = new ByteArrayInputStream(byte64);
            Bitmap bitmap = BitmapFactory.decodeStream(bais);
            iv_avator.setImageBitmap(bitmap);
        } else {
            iv_avator.setImageResource(R.drawable.header);
            //Toasty.warning(getApplicationContext(), "你还没有设置头像", Toast.LENGTH_SHORT).show();
        }

        LinearLayout ll_avator = (LinearLayout) findViewById(R.id.ll_avator);
        ll_avator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AvatarActivity.this.finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
