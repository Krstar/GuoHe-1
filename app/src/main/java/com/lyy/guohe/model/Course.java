package com.lyy.guohe.model;


import java.io.Serializable;

public class Course implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -9121734039844677432L;
    private int jieci;

    private int day;
    private String des;
    private int spanNum = 2;

    private int bg_Color;

    private String ClassRoomName;       //教室
    private String ClassTypeName;       //课程号
    private String ClassName;           //课程名
    private String ClassTeacher;        //教师名

    public String getClassName() {
        return ClassName;
    }

    public void setClassName(String className) {
        ClassName = className;
    }

    public String getClassTeacher() {
        return ClassTeacher;
    }

    public void setClassTeacher(String classTeacher) {
        ClassTeacher = classTeacher;
    }

    public Course() {
    }

    public int getBg_Color() {
        return bg_Color;
    }

    public void setBg_Color(int bg_Color) {
        this.bg_Color = bg_Color;
    }

    public Course(int jieci, int day, String des) {
        this.jieci = jieci;
        this.day = day;
        this.des = des;
    }

    public int getJieci() {
        return jieci;
    }

    public void setJieci(int jieci) {
        this.jieci = jieci;
    }

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

    public int getSpanNum() {
        return spanNum;
    }

    public void setSpanNum(int spanNum) {
        this.spanNum = spanNum;
    }

    @Override
    public String toString() {
        return "DBCourse [jieci=" + jieci + ", day=" + day + ", des=" + des
                + ", spanNun=" + spanNum + "]";
    }

    public String getClassRoomName() {
        return ClassRoomName;
    }

    public void setClassRoomName(String classRoomName) {
        ClassRoomName = classRoomName;
    }

    public String getClassTypeName() {
        return ClassTypeName;
    }

    public void setClassTypeName(String classTypeName) {
        ClassTypeName = classTypeName;
    }


}
