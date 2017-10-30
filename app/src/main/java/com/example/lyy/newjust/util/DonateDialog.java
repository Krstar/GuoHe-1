package com.example.lyy.newjust.util;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.lyy.newjust.R;
import com.flyco.animation.Attention.ShakeHorizontal;
import com.flyco.animation.Attention.Swing;
import com.flyco.dialog.widget.MaterialDialog;

/**
 * Created by lyy on 2017/10/29.
 */

public class DonateDialog extends MaterialDialog {

    private Context context;

    private TextView tv_content;

    private TextView tv_cancel;

    private Button btn_alipay;

    public DonateDialog(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public View onCreateView() {

        widthScale(0.85f);

        //设置弹出动画
        showAnim(new Swing());

        //填充弹窗布局
        View inflate = View.inflate(context, R.layout.donate_dialog, null);

        tv_content = inflate.findViewById(R.id.tv_content);
        tv_cancel = inflate.findViewById(R.id.tv_cancel);
        btn_alipay = inflate.findViewById(R.id.btn_alipay);

        tv_content.setText("如果你觉得本应用好用并且想让本应用一直活下去的话,欢迎捐赠!你们的支持与捐赠将是我一直更新下去的动力和维护软件必需的来源!\n");

        return inflate;
    }

    //该方法用来处理逻辑代码
    @Override
    public void setUiBeforShow() {
        btn_alipay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                donate();
                dismiss();
            }
        });

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    private void donate() {
        String intentFullUrl = "intent://platformapi/startapp?saId=10000007&" +
                "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2FFKX07846GRVQI6HABMOJ72%3F_s" +
                "%3Dweb-other&_t=1472443966571#Intent;" +
                "scheme=alipayqr;package=com.eg.android.AlipayGphone;end";
        try {
            Intent intent = Intent.parseUri(intentFullUrl, Intent.URI_INTENT_SCHEME);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
