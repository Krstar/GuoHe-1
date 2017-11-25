package com.example.lyy.newjust.adapter;

/**
 * Created by lyy on 2017/11/24.
 */

public class ClassRoom {

    private String weekday;

    private String time;

    private String place;

    public ClassRoom(String weekday, String time, String place) {
        this.weekday = weekday;
        this.time = time;
        this.place = place;
    }

    public String getWeekday() {
        return weekday;
    }

    public String getTime() {
        return time;
    }

    public String getPlace() {
        return place;
    }
}
