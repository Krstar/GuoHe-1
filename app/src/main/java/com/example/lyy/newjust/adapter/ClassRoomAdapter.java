package com.example.lyy.newjust.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.lyy.newjust.R;

import java.util.List;

/**
 * Created by lyy on 2017/11/24.
 */

public class ClassRoomAdapter extends ArrayAdapter<ClassRoom> {
    private int resourceId;

    public ClassRoomAdapter(Context context, int resource, List<ClassRoom> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ClassRoom classRoom = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);

        TextView tv_classroom = view.findViewById(R.id.tv_classroom);
        TextView tv_time = view.findViewById(R.id.tv_time);

        tv_time.setText(classRoom.getTime());
        tv_classroom.setText(classRoom.getPlace());

        return view;
    }
}
