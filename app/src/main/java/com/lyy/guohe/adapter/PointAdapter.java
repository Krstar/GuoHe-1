package com.lyy.guohe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lyy.guohe.R;

import java.util.List;

/**
 * Created by lyy on 2017/10/14.
 */

public class PointAdapter extends ArrayAdapter<Point> {

    private final int resourceId;

    public PointAdapter(Context context, int textViewResourceId, List<Point> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Point point = getItem(position);      //获取当前页的point实例

        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);//实例化一个对象

        TextView every_year = (TextView) view.findViewById(R.id.year);       //获取该布局内的学年
        TextView year_point = (TextView) view.findViewById(R.id.point);   //获取该布局内的学年绩点

        every_year.setText(point.getYear());                    //设置学年
        year_point.setText(point.getPoint());                //设置学年绩点

        return view;
    }
}
