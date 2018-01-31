package com.lyy.guohe.model;

/**
 * Created by lyy on 2017/12/7.
 */

public class Res {

    /**
     * code : 602
     * msg : 教务系统账号错误
     * info :
     */

    private int code;
    private String msg;
    private String info;

    public Res(int code, String msg, String info) {
        super();
        this.code = code;
        this.msg = msg;
        this.info = info;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
