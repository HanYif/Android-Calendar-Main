package com.example.a12136.calendar;

/**
 * Created by Otherwise蔡岳 on 2016/11/30.
 */
public class Weather {
    private String Date, Weather_Description, Temperature;

    public Weather(String d, String w, String t) {
        this.Date = d;
        this.Weather_Description = w;
        this.Temperature = t;
    }

    public String getDate() {
        return Date;
    }

    public String getWeather_description() {
        return Weather_Description;
    }

    public String getTemperature() {
        return Temperature;
    }
}
