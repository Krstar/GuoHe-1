package com.example.lyy.newjust.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.lyy.newjust.R;

import java.util.List;

/**
 * Created by lyy on 2017/10/13.
 */

public class SubjectAdapter extends ArrayAdapter<Subject> {

    private final int resourceId;

    public SubjectAdapter(Context context, int textViewResourceId, List<Subject> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Subject subject = getItem(position);      //获取当前页的Subject实例

        View view= LayoutInflater.from(getContext()).inflate(resourceId, parent,false);//实例化一个对象

        TextView subject_name = (TextView) view.findViewById(R.id.subject_name);       //获取该布局内的学科名
        TextView subject_credit = (TextView) view.findViewById(R.id.subject_credit);   //获取该布局内的学科学分
        TextView subject_score = (TextView) view.findViewById(R.id.subject_score);       //获取当前布局内的学科成绩

        subject_name.setText(subject.getSubject_name());                    //设置学科名
        subject_credit.setText(subject.getSubject_credit());                //设置学科学分
        subject_score.setText(subject.getSubject_score());                  //设置学科分数
        return view;
    }

}
