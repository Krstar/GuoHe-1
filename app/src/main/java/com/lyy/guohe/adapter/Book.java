package com.lyy.guohe.adapter;

/**
 * Created by lyy on 2017/11/25.
 */

public class Book {

    private String book_title;

    private String book_author_press;

    private String book_can_borrow;

    private String book_url;

    public Book(String book_title, String book_author_press, String book_can_borrow, String book_url) {
        this.book_title = book_title;
        this.book_author_press = book_author_press;
        this.book_can_borrow = book_can_borrow;
        this.book_url = book_url;
    }

    public String getBook_title() {
        return book_title;
    }

    public String getBook_author_press() {
        return book_author_press;
    }

    public String getBook_can_borrow() {
        return book_can_borrow;
    }

    public String getBook_url() {
        return book_url;
    }

}
