package com.example.peng.myapplication;

import android.app.Activity;
import android.os.Bundle;

import com.example.calendarview.CalendarView;


/**
 * Created by peng on 2016/12/11.
 */
public class MainActivity extends Activity{
    CalendarView calendarView;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        calendarView = (CalendarView)findViewById(R.id.calendarView);
    }

}
