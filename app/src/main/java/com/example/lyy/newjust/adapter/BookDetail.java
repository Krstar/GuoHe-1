package com.example.lyy.newjust.adapter;

/**
 * Created by lyy on 2017/11/25.
 */

public class BookDetail {

    private String place;

    private String barcode;

    private String call_number;

    public BookDetail(String call_number, String barcode, String place) {
        this.place = place;
        this.barcode = barcode;
        this.call_number = call_number;
    }

    public String getPlace() {
        return place;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getCall_number() {
        return call_number;
    }
}
