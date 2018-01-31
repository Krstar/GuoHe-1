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
 * Created by lyy on 2017/11/25.
 */

public class BookAdapter extends ArrayAdapter<Book> {

    private int resourceId;

    public BookAdapter(Context context, int resource, List<Book> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Book book = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);

        TextView tv_book_name = (TextView) view.findViewById(R.id.tv_book_name);
        TextView tv_book_author = (TextView) view.findViewById(R.id.tv_book_author);
        TextView tv_book_borrow = (TextView) view.findViewById(R.id.tv_book_borrow);

        tv_book_name.setText(book.getBook_title());
        tv_book_author.setText(book.getBook_author_press());
        tv_book_borrow.setText(book.getBook_can_borrow());

        return view;
    }
}
