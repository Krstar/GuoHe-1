package com.example.lyy.newjust.activity.Subject;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.lyy.newjust.R;
import com.example.lyy.newjust.adapter.Subject;
import com.example.lyy.newjust.adapter.SubjectAdapter;
import com.example.lyy.newjust.db.DBSubject;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class FragmentLayout extends Fragment {

    private static final String TAG = "FragmentLayout";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_layout, null);

        List<DBSubject> dbSubjectList = DataSupport.where("examination_method like?", "考试").find(DBSubject.class);

        List<Subject> adapter_list_kaoshi = new ArrayList<>();

        for (int i = 0; i < dbSubjectList.size(); i++) {
            Subject subject = new Subject(dbSubjectList.get(i).getCourse_name(), dbSubjectList.get(i).getCredit(), dbSubjectList.get(i).getScore());
            adapter_list_kaoshi.add(subject);
        }

        SubjectAdapter subjectAdapter = new SubjectAdapter(getActivity(), R.layout.item_subjects, adapter_list_kaoshi);
        ListView listView = view.findViewById(R.id.subject_list_item);
        listView.setAdapter(subjectAdapter);

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            //相当于Fragment的onResume

        } else {
            //相当于Fragment的onPause
        }
    }
}
