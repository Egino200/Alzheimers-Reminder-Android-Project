package com.example.yearproject12;

import android.os.Build;

import java.time.LocalTime;

public class TestReminder {
    String title;
    String time;



    public TestReminder() {

    }
    public TestReminder(String title, String time) {
        this.title = title;
        this.time = time;

    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "arraylist{" +
                "title='" + title + '\'' +
                ", time=" + time +
                '}';
    }
}


