package com.example.lyy.newjust.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lyy.newjust.R;

import java.util.List;

/**
 * Created by lyy on 2017/10/27.
 */

public class MemoryAdapter extends ArrayAdapter<Memory> {

    private int resourceId;

    public MemoryAdapter(Context context, int textViewResourceId, List<Memory> objects) {
        super(context, textViewResourceId, objects);

        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Memory memory = getItem(position);

        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);//实例化一个对象

        TextView memory_day = view.findViewById(R.id.memory_day);
        TextView memory_content = view.findViewById(R.id.memory_content);
        ImageView iv_bg_item_memory = view.findViewById(R.id.iv_bg_item_memory);

        memory_day.setText(memory.getMemory_day());
        memory_content.setText(memory.getMemory_content());
        iv_bg_item_memory.setBackgroundColor(memory.getImageId());

        return view;
    }
}
