package com.example.a12136.calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Created by 12136 on 2016/12/15.
 */

public class CalendarView extends View {

    private static final String TAG = "CalendarView";
    /**
     * 两种模式 （月份和星期）
     */
    public static final int MONTH_STYLE = 0;
    public static final int WEEK_STYLE = 1;

    private static final int TOTAL_COL = 7;
    private static final int TOTAL_ROW = 6;

    private Paint mCirclePaint;
    private Paint mTextPaint;
    private int mViewWidth;
    private int mViewHight;
    private int mCellSpace;
    private Row rows[] = new Row[TOTAL_ROW];
    private static CustomDate mShowDate;//自定义的日期  包括year month day
    public static int style = MONTH_STYLE;
    private static final int WEEK = 7;
    private CallBack mCallBack;//回调
    private int touchSlop;
    private boolean callBackCellSpace;

    public interface CallBack {

        void clickDate(CustomDate date);//回调点击的日期

        void onMeasureCellHeight(int cellSpace);// 回调cell的高度确定slidingDrawer高度

        void changeDate(CustomDate date);//回调滑动viewPager改变的日期
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CalendarView(Context context) {
        super(context);
        init(context);
    }

    public CalendarView(Context context, int style, CallBack mCallBack) {
        super(context);
        CalendarView.style = style;
        this.mCallBack = mCallBack;
        init(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < TOTAL_ROW; i++) {
            if (rows[i] != null)
                rows[i].drawCells(canvas);
        }
    }

    private void init(Context context) {
        // Paint.ANTI_ALIAS_FLAG用于绘制时抗锯齿
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setColor(Color.parseColor("#F24949"));
        //touchSlop获得的是触发移动事件的最短距离，如果小于这个距离就不触发移动控件
        // 用这个距离来判断用户是否翻页
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        initDate();
    }

    private void initDate() {
        if (style == MONTH_STYLE) {
            mShowDate = new CustomDate();
        } else if(style == WEEK_STYLE ) {
            mShowDate = DateUtil.getNextSunday();
        }
        fillDate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHight = h;
        mCellSpace = Math.min(mViewHight / TOTAL_ROW, mViewWidth / TOTAL_COL);
        if (!callBackCellSpace) {
            mCallBack.onMeasureCellHeight(mCellSpace);
            callBackCellSpace = true;
        }
        mTextPaint.setTextSize(mCellSpace / 3);
    }

    private Cell mClickCell;
    private float mDownX;
    private float mDownY;

    /*
     *
     * 触摸事件为了确定点击的位置日期
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:// 手指按下
                mDownX = event.getX();
                mDownY = event.getY();
                break;
            case MotionEvent.ACTION_UP:// 手指抬起
                // 根据滑动时手指按下和抬起的距离来和翻页触发距离比较
                // 小于的时候就是点击
                float disX = event.getX() - mDownX;
                float disY = event.getY() - mDownY;
                if (Math.abs(disX) < touchSlop && Math.abs(disY) < touchSlop) {
                    int col = (int) (mDownX / mCellSpace);
                    int row = (int) (mDownY / mCellSpace);
                    measureClickCell(col, row);
                }
                break;
        }
        return true;
    }

    // 对点击事件处理
    private void measureClickCell(int col, int row) {
        System.out.println("col = " + col + "   row = " + row);
        if (col >= TOTAL_COL || row >= TOTAL_ROW)
            return;
        if (mClickCell != null) {
            System.out.println("mClickCell.i = " + mClickCell.i + "   mClickCell.j = " + mClickCell.j);
            rows[mClickCell.j].cells[mClickCell.i] = mClickCell;
        }
        if (rows[row] != null) {
            //rows[mClickCell.j].cells[mClickCell.i] = mClickCell;
            mClickCell = new Cell(rows[row].cells[col].date,
                    rows[row].cells[col].state, rows[row].cells[col].i,
                    rows[row].cells[col].j);
            rows[row].cells[col].state = State.CLICK_DAY;
            CustomDate date = rows[row].cells[col].date;
            date.week = col;
            mCallBack.clickDate(date);
            // 请求重绘视图，刷新view
            invalidate();
        }
    }

    // 组
    class Row {
        public int j;

        Row(int j) {
            this.j = j;
        }

        public Cell[] cells = new Cell[TOTAL_COL];

        public void drawCells(Canvas canvas) {
            for (int i = 0; i < cells.length; i++) {
                if (cells[i] != null)
                    cells[i].drawSelf(canvas);
            }

        }
    }

    // 单元格
    class Cell {
        public CustomDate date;
        public State state;
        public int i;
        public int j;

        public Cell(CustomDate date, State state, int i, int j) {
            super();
            this.date = date;
            this.state = state;
            this.i = i;
            this.j = j;
        }

        // 绘制一个单元格 颜色自定义
        public void drawSelf(Canvas canvas) {
            switch (state) {
                case CURRENT_MONTH_DAY:
                    mTextPaint.setColor(Color.parseColor("#80000000"));
                    break;
                // 如果 不是当前月，那么字体颜色设置的浅一些，和当前月区别出来
                case NEXT_MONTH_DAY:
                case PAST_MONTH_DAY:
                    mTextPaint.setColor(Color.parseColor("#40000000"));
                    break;
                // 今天的字体颜色设为红色，这样不管点到哪里都知道今天在哪里
                case TODAY:
                    mTextPaint.setColor(Color.parseColor("#F24949"));
                    break;
                // 点击选中的日期时，字体颜色变成白色，同时加上一个红色的圆圈
                case CLICK_DAY:
                    mTextPaint.setColor(Color.parseColor("#fffffe"));
                    canvas.drawCircle((float) (mCellSpace * (i + 0.5)),
                            (float) ((j + 0.5) * mCellSpace), mCellSpace / 2,
                            mCirclePaint);
                    break;
            }
            // 绘制文字
            String content = date.day+"";
            //System.out.println("date.day = " + content);
            canvas.drawText(content,
                    (float) ((i+0.5) * mCellSpace - mTextPaint.measureText(content)/2),
                    (float) ((j + 0.7) * mCellSpace - mTextPaint.measureText(
                            content, 0, 1) / 2), mTextPaint);
        }
    }
    /**
     *
     * cell的state
     *当前月日期，过去的月的日期，下个月的日期，今天，点击的日期
     *
     */
    enum State {
        CURRENT_MONTH_DAY, PAST_MONTH_DAY, NEXT_MONTH_DAY, TODAY, CLICK_DAY
    }

    /**
     * 填充日期的数据
     */
    private void fillDate() {
        if (style == MONTH_STYLE) {
            fillMonthDate();
        } else if(style == WEEK_STYLE) {
            fillWeekDate();
        }
        mCallBack.changeDate(mShowDate);
    }

    // 填充周模式下的数据
    private void fillWeekDate() {
        // 上一个月的天数
        int lastMonthDays = DateUtil.getMonthDays(mShowDate.year, mShowDate.month-1);
        rows[0] = new Row(0);
        // 这里的day是下一个星期天
        int day = mShowDate.day;
        // 从后往前填充7天
        for (int i = TOTAL_COL -1; i >= 0 ; i--) {
            day -= 1;
            // 当回到一个月的第一天后还需要继续填充的话啊就需要变成上一个月的最后一天的日期
            if (day < 1) {
                day = lastMonthDays;
            }
            // 获取修正后的日期，也就是改变了日
            CustomDate date = CustomDate.modifyDayForObject(mShowDate, day);
            // 如果当前待填充的日期就是今天，则重绘一下今天的图形
            if (DateUtil.isToday(date)) {
                mClickCell = new Cell(date, State.TODAY, i, 0);
                //System.out.println("i = " + i);
                date.week = i;
                mCallBack.clickDate(date);
                rows[0].cells[i] = new Cell(date, State.CLICK_DAY, i, 0);
                continue;
            } else {
                mClickCell = null;
            }
            // 填充，在周视图下默认都是当前月，不做区分
            rows[0].cells[i] = new Cell(date, State.CURRENT_MONTH_DAY,i, 0);
        }
    }

    /**
     * 填充月份模式下数据 通过getWeekDayFromDate得到一个月第一天是星期几就可以算出所有的日期的位置 然后依次填充
     */
    private void fillMonthDate() {
        int monthDay = DateUtil.getCurrentMonthDay();
        int lastMonthDays = DateUtil.getMonthDays(mShowDate.year, mShowDate.month - 1);
        int currentMonthDays = DateUtil.getMonthDays(mShowDate.year, mShowDate.month);
        int firstDayWeek = DateUtil.getWeekDayFromDate(mShowDate.year, mShowDate.month);
        boolean isCurrentMonth = false;
        if (DateUtil.isCurrentMonth(mShowDate)) {
            isCurrentMonth = true;
        }
        int day = 0;
        for (int j = 0; j < TOTAL_ROW; j++) {
            rows[j] = new Row(j);
            for (int i = 0; i < TOTAL_COL; i++) {
                int postion = i + j * TOTAL_COL;
                if (postion >= firstDayWeek
                        && postion < firstDayWeek + currentMonthDays) {
                    day++;
                    if (isCurrentMonth && day == monthDay) {
                        CustomDate date = CustomDate.modifyDayForObject(mShowDate, day);
                        mClickCell = new Cell(date,State.TODAY, i,j);
                        date.week = i;
                        mCallBack.clickDate(date);
                        rows[j].cells[i] = new Cell(date,State.CLICK_DAY, i,j);
                        continue;
                    } else {
                        mClickCell = null;
                    }
                    rows[j].cells[i] = new Cell(CustomDate.modifyDayForObject(mShowDate, day),
                            State.CURRENT_MONTH_DAY, i, j);
                } else if (postion < firstDayWeek) {
                    rows[j].cells[i] = new Cell(new CustomDate(mShowDate.year, mShowDate.month-1, lastMonthDays - (firstDayWeek- postion - 1)), State.PAST_MONTH_DAY, i, j);
                } else if (postion >= firstDayWeek + currentMonthDays) {
                    rows[j].cells[i] = new Cell((new CustomDate(mShowDate.year, mShowDate.month+1, postion - firstDayWeek - currentMonthDays + 1)), State.NEXT_MONTH_DAY, i, j);
                }
            }
        }
    }

    public void update() {
        fillDate();
        // 请求重绘视图，刷新view
        invalidate();
    }

    public void backToday(){
        initDate();
        // 请求重绘视图，刷新view
        invalidate();
    }
    //切换style
    public void switchStyle(int style) {
        CalendarView.style = style;
        if (style == MONTH_STYLE) {
            update();
        } else if (style == WEEK_STYLE) {
            int firstDayWeek = DateUtil.getWeekDayFromDate(mShowDate.year,
                    mShowDate.month);
            int day =  1 + WEEK - firstDayWeek;
            mShowDate.day = day;
            update();
        }

    }
    //向右滑动, 重新计算标题栏的日期显示
    public void rightSilde() {
        if (style == MONTH_STYLE) {
            if (mShowDate.month == 12) {
                mShowDate.month = 1;
                mShowDate.year += 1;
            } else {
                mShowDate.month += 1;
            }
        } else if (style == WEEK_STYLE) {
            int currentMonthDays = DateUtil.getMonthDays(mShowDate.year, mShowDate.month);
            if (mShowDate.day + WEEK > currentMonthDays) {
                if (mShowDate.month == 12) {
                    mShowDate.month = 1;
                    mShowDate.year += 1;
                } else {
                    mShowDate.month += 1;
                }
                mShowDate.day = WEEK - currentMonthDays + mShowDate.day;
            }else{
                mShowDate.day += WEEK;
            }
        }
        update();
    }
    //向左滑动, 重新计算标题栏的日期显示
    public void leftSilde() {

        if (style == MONTH_STYLE) {
            if (mShowDate.month == 1) {
                mShowDate.month = 12;
                mShowDate.year -= 1;
            } else {
                mShowDate.month -= 1;
            }

        } else if (style == WEEK_STYLE) {
            int lastMonthDays = DateUtil.getMonthDays(mShowDate.year, mShowDate.month);
            if (mShowDate.day - WEEK < 1) {
                if (mShowDate.month == 1) {
                    mShowDate.month = 12;
                    mShowDate.year -= 1;
                } else {
                    mShowDate.month -= 1;
                }
                mShowDate.day = lastMonthDays - WEEK + mShowDate.day;

            }else{
                mShowDate.day -= WEEK;
            }
            Log.i(TAG, "leftSilde"+mShowDate.toString());
        }
        update();
    }
}

