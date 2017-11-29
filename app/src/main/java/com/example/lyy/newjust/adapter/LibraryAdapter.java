package com.example.lyy.newjust.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lyy.newjust.R;
import com.example.lyy.newjust.activity.School.BookListActivity;

import java.util.List;

/**
 * Created by lyy on 2017/11/28.
 */

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder> {

    private static final String TAG = "LibraryAdapter";

    private Context mContext;

    private List<Library> mLibraryList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tv_name;
        TextView tv_bookcode;
        TextView tv_author;
        TextView tv_press;
        ImageView iv_bg_library;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;

            tv_name = (TextView) view.findViewById(R.id.tv_name);
//            tv_bookcode = (TextView) view.findViewById(R.id.tv_bookcode);
            tv_author = (TextView) view.findViewById(R.id.tv_author);
            tv_press = (TextView) view.findViewById(R.id.tv_press);
            iv_bg_library = (ImageView) view.findViewById(R.id.iv_bg_library);
        }
    }

    public LibraryAdapter(List<Library> libraryList) {
        mLibraryList = libraryList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }

        View view = LayoutInflater.from(mContext).inflate(R.layout.item_library, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Library library = mLibraryList.get(position);
                Intent intent = new Intent(mContext, BookListActivity.class);
                intent.putExtra("keyword", library.getName());
                mContext.startActivity(intent);
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Library library = mLibraryList.get(position);
        holder.tv_name.setText(library.getName());
        holder.tv_author.setText(library.getAuthor());
//        holder.tv_bookcode.setText(library.getBookcode());
        holder.tv_press.setText(library.getPress());
        holder.iv_bg_library.setBackgroundColor(library.getColor());
        Log.d(TAG, "onBindViewHolder: " + library.getColor());
    }

    @Override
    public int getItemCount() {
        return mLibraryList.size();
    }

}
