package com.lyy.guohe.adapter;

/**
 * Created by lyy on 2017/11/22.
 */

public class Club {

    private String time;

    private String number;

    private String date;

    public Club(String time, String number, String date) {
        this.time = time;
        this.number = number;
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public String getNumber() {
        return number;
    }

    public String getDate() {
        return date;
    }
}
