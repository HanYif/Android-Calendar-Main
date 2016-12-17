package com.example.a12136.calendar;

import java.io.Serializable;

/**
 * Created by 12136 on 2016/12/15.
 */

public class CustomDate implements Serializable {

    private static final long serialVersionUID = 1L;
    public int year;
    public int month;
    public int day;
    public int week;

    public CustomDate(int year,int month,int day){
        if(month > 12){
            month = 1;
            year++;
        }else if(month <1){
            month = 12;
            year--;
        }
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public CustomDate(){
        this.year = DateUtil.getYear();
        this.month = DateUtil.getMonth();
        this.day = DateUtil.getCurrentMonthDay();
    }

    public static CustomDate modifyDayForObject(CustomDate date, int day){
        return new CustomDate(date.year, date.month, day);
    }

    @Override
    public String toString() {
        return year+"-"+month+"-"+day;
    }
}
