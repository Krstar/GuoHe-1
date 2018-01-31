package com.lyy.guohe.adapter;

/**
 * Created by lyy on 2017/10/27.
 */

public class Memory {

    private String memory_content;  //提醒的内容
    private String memory_day;      //提醒的日期

    private int imageId;            //背景

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public Memory(String content, String day, int id) {
        this.memory_content = content;
        this.memory_day = day;

        this.imageId = id;
    }

    public String getMemory_content() {
        return memory_content;
    }

    public void setMemory_content(String memory_content) {
        this.memory_content = memory_content;
    }

    public String getMemory_day() {
        return memory_day;
    }

    public void setMemory_day(String memory_day) {
        this.memory_day = memory_day;
    }
}
