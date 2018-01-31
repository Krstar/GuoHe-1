package com.lyy.guohe.adapter;

/**
 * Created by lyy on 2017/11/28.
 */

public class Library {

    /**
     * bookcode : F015/213
     * borrow_times : 3
     * collection : 2
     * press : 中国人民大学出版社 2011.09
     * number : 95
     * borrow_rate : 1.5
     * name : 高鸿业版《西方经济学》(宏观部分·第五版) 学习手册
     * author : 主编陈新
     */

    private String bookcode;
    private String borrow_times;
    private String collection;
    private String press;
    private String number;
    private String borrow_rate;
    private String name;
    private String author;
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private int color;

    //需要用到的字段 bookcode name press author
    public Library(String bookcode, String name, String press, String url, String author, int color) {
        super();

        this.bookcode = bookcode;
        this.url = url;
        this.name = name;
        this.press = press;
        this.author = author;
        this.color = color;

    }

    public int getColor() {
        return color;
    }

    public String getBookcode() {
        return bookcode;
    }

    public void setBookcode(String bookcode) {
        this.bookcode = bookcode;
    }

    public String getBorrow_times() {
        return borrow_times;
    }

    public void setBorrow_times(String borrow_times) {
        this.borrow_times = borrow_times;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getPress() {
        return press;
    }

    public void setPress(String press) {
        this.press = press;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getBorrow_rate() {
        return borrow_rate;
    }

    public void setBorrow_rate(String borrow_rate) {
        this.borrow_rate = borrow_rate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
