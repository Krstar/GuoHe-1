package com.lyy.guohe.db;

import org.litepal.crud.DataSupport;

/**
 * Created by lyy on 2017/10/27.
 */

public class DBMemory extends DataSupport{

    private String memory_content;  //提醒的内容

    private String memory_day;      //提醒的日期

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
