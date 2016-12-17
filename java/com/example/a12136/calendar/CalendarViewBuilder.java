package com.example.a12136.calendar;

import android.content.Context;

import com.example.a12136.calendar.CalendarView.CallBack;

/**
 * Created by 12136 on 2016/12/15.
 */

public class CalendarViewBuilder {
    private CalendarView[] calendarViews;
    /**
     * 生产多个CalendarView
     * @param context
     * @param count
     * @param style
     * @param callBack
     * @return
     */
    public  CalendarView[] createMassCalendarViews(Context context, int count, int style, CallBack callBack){
        calendarViews = new CalendarView[count];
        for(int i = 0; i < count; i++){
            calendarViews[i] = new CalendarView(context, style, callBack);
        }
        return calendarViews;
    }

    public  CalendarView[] createMassCalendarViews(Context context,int count, CallBack callBack){
        return createMassCalendarViews(context, count, CalendarView.MONTH_STYLE, callBack);
    }

    /**
     * 切换CandlendarView的样式
     * @param style
     */
    public void swtichCalendarViewsStyle(int style){
        if(calendarViews != null)
            for(int i = 0 ;i < calendarViews.length;i++){
                calendarViews[i].switchStyle(style);
            }
    }

    /**
     * CalendarView回到当前日期
     */
    public void backTodayCalendarViews(){
        if(calendarViews != null)
            for (int i = 0 ;i < calendarViews.length;i++){
                calendarViews[i].backToday();
            }
    }
}
