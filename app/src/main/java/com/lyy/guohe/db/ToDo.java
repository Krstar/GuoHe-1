package com.lyy.guohe.db;

import org.litepal.crud.DataSupport;

/**
 * Created by lyy on 2017/10/14.
 */

public class ToDo extends DataSupport{

    private String content;

    private boolean isFinished;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }
}
