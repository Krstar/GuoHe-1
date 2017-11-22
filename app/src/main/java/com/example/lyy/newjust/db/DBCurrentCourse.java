package com.example.lyy.newjust.db;

import org.litepal.crud.DataSupport;

/**
 * Created by lyy on 2017/11/20.
 */

public class DBCurrentCourse extends DataSupport {

    private int day;  //星期几的课

    private String des;      //课程的描述

    private int jieci;   //课程的节次

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public int getJieci() {
        return jieci;
    }

    public void setJieci(int jieci) {
        this.jieci = jieci;
    }

}
