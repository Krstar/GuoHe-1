package com.lyy.guohe.db;

import org.litepal.crud.DataSupport;

/**
 * Created by lyy on 2018/1/16.
 */

public class DBDate extends DataSupport {

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    private int zhouci;     //记录周次

    public int getZhouci() {
        return zhouci;
    }

    public void setZhouci(int zhouci) {
        this.zhouci = zhouci;
    }

    private String month;  //记录月份

    private String date;  //记录日期
}
