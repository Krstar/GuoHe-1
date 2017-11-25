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
 * Created by lyy on 2017/11/25.
 */

public class BookDetailAdapter extends ArrayAdapter<BookDetail> {

    private int resourceId;

    public BookDetailAdapter(Context context, int resource, List<BookDetail> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        BookDetail bookDetail = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);

        TextView tv_book_call_number = view.findViewById(R.id.tv_book_call_number);
        TextView tv_book_barcode = view.findViewById(R.id.tv_book_barcode);
        TextView tv_book_place = view.findViewById(R.id.tv_book_place);

        tv_book_call_number.setText(bookDetail.getCall_number());
        tv_book_barcode.setText(bookDetail.getBarcode());
        tv_book_place.setText(bookDetail.getPlace());

        return view;
    }
}
