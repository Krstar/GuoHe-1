package com.example.lyy.newjust.base;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;

/**
 * Created by lyy on 2017/12/9.
 */

public class ScrollSwipeRefreshLayout extends WaveSwipeRefreshLayout {
    private ViewGroup viewGroup;

    public ScrollSwipeRefreshLayout(Context context) {
        super(context);
    }

    public ScrollSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewGroup getViewGroup() {
        return viewGroup;
    }

    public void setViewGroup(ViewGroup viewGroup) {
        this.viewGroup = viewGroup;
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent arg0) {
//        if (null != viewGroup) {
//            if (viewGroup.getScrollY() > 1) {
//                //直接截断时间传播
//                return false;
//            } else {
//                return super.onTouchEvent(arg0);
//            }
//        }
//        return super.onTouchEvent(arg0);
//    }

    @Override
    public boolean canChildScrollUp() {
        if(viewGroup != null){
            if(viewGroup.getScrollY() > 0){
                return true;
            }
        }
        return false;
    }

}
