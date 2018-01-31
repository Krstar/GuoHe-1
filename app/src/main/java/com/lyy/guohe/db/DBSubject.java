package com.lyy.guohe.db;

import org.litepal.crud.DataSupport;

/**
 * Created by lyy on 2017/10/18.
 */

public class DBSubject extends DataSupport{
    private String course_name;

    private String credit;

    private String score;

    private String start_semester;

    private String examination_method;

    public String getCourse_name() {
        return course_name;
    }

    public void setCourse_name(String course_name) {
        this.course_name = course_name;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getStart_semester() {
        return start_semester;
    }

    public void setStart_semester(String start_semester) {
        this.start_semester = start_semester;
    }

    public String getExamination_method() {
        return examination_method;
    }

    public void setExamination_method(String examination_method) {
        this.examination_method = examination_method;
    }
}
