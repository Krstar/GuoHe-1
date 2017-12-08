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
 * Created by lyy on 2017/11/22.
 */

public class ClubAdapter extends ArrayAdapter<Club> {

    private int resourceId;

    public ClubAdapter(Context context, int resource, List<Club> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Club club = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        TextView tv_number = (TextView) view.findViewById(R.id.tv_number);
        TextView tv_time = (TextView) view.findViewById(R.id.tv_time);
        TextView tv_date = (TextView) view.findViewById(R.id.tv_date);

        tv_time.setText(club.getTime());
        tv_number.setText(club.getNumber());
        tv_date.setText(club.getDate());

        return view;
    }
}
