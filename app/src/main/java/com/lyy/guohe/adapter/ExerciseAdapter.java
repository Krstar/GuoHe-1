package com.lyy.guohe.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.lyy.guohe.R;

import java.util.List;

/**
 * Created by lyy on 2017/11/26.
 */

public class ExerciseAdapter extends ArrayAdapter<Exercise> {
    private int resourceId;

    public ExerciseAdapter(Context context, int resource, List<Exercise> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Exercise exercise = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        TextView tv_number = (TextView) view.findViewById(R.id.tv_number);
        TextView tv_time = (TextView) view.findViewById(R.id.tv_time);
        TextView tv_date = (TextView) view.findViewById(R.id.tv_date);

        tv_time.setText(exercise.getTime());
        tv_number.setText(exercise.getNumber());
        tv_date.setText(exercise.getDate());

        return view;
    }
}
