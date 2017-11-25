package com.example.lyy.newjust.base;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.lyy.newjust.R;
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

        tv_content.setText("假如此App为您带来了便利与舒适，假如您愿意支持我们。我们希望能得到小小的赞赏，这是一种莫大的肯定与鼓励。\n" +
                "金钱是保持自由的一种工具，我们诚挚祈望未来与你同在。\n");

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
