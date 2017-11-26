package com.example.lyy.newjust.adapter;

/**
 * Created by lyy on 2017/11/26.
 */

public class Exercise {

    /**
     * date : 2017年10月18日(第 7周 周3)
     * number : 1
     * time : 无记录
     */

    private String date;
    private String number;
    private String time;

    public Exercise(String time, String number, String date) {
        this.time = time;
        this.number = number;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
