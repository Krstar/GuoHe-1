package com.lyy.guohe.adapter;

/**
 * Created by lyy on 2017/10/13.
 */

public class Subject {

    private String subject_name;

    private String subject_credit;

    private String subject_score;

    public Subject(String name, String credit, String score) {

        this.subject_name = name;

        this.subject_credit = credit;

        this.subject_score = score;
    }

    public String getSubject_name() {
        return subject_name;
    }

    public String getSubject_credit() {
        return subject_credit;
    }

    public String getSubject_score() {
        return subject_score;
    }
}
