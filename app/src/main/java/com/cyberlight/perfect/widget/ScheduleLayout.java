package com.cyberlight.perfect.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.cyberlight.perfect.R;
import com.cyberlight.perfect.model.Event;
import com.cyberlight.perfect.model.SpecEvent;
import com.cyberlight.perfect.util.DbUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

public class ScheduleLayout extends ViewGroup {
    private static final int DEFAULT_WIDTH = 1080;// 整个layout的默认宽度
    private static final int TABLE_HEIGHT = 24 * 60 * 2;// 第一条线到最后一条线之间的像素

    // 定义属性
    private boolean antiAlias;
    private int tableColor;
    private int curTimeLineColor;
    private int curTimeLineWidth;
    private int lineWidth;
    private int timeTextSize;
    private int remainTop;
    private int remainBottom;
    private int remainLeft;
    private int remainRight;

    // 根据属性计算得到的数据、paint
    private int timeTextWidth;
    private int timeTextHeight;
    private final Paint mLinePaint;
    private final Paint mTimePaint;
    private final Paint mCurTimePaint;

    //布局所使用的各种数据
    private final Context mContext;
    private int mWidth;
    private final int mTouchSlop;// 用于事件按钮手势判断
    private LocalDate date;// 当前日期，由Activity通过setDate()赋值
    private boolean isToday = false;// 日期是否是今天
    private List<Event> events;// 当前layout所使用的事件列表，通过setEvents()修改

    private final Handler mHandler = new Handler();
    private final Runnable mCurTimeRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            invalidate();
            // 控制在每分钟第1000ms刷新
            long delayMillis = 60000 + (1000 - (System.currentTimeMillis() % 60000));
            mHandler.postDelayed(this, delayMillis);
        }
    };

    public ScheduleLayout(Context context) {
        this(context, null);
    }

    public ScheduleLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScheduleLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.DefaultScheduleLayout);
    }

    public ScheduleLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        //初始化
        mContext = context;
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mLinePaint = new Paint();
        mTimePaint = new Paint();
        mCurTimePaint = new Paint();
        initAttrs(context, attrs, defStyleAttr, defStyleRes);
        initPaint();
        initTimeTextBounds();
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public void setDate(LocalDate date) {
        this.date = LocalDate.ofEpochDay(date.toEpochDay());
    }

    /**
     * 当日期或事件集发生改变时，外部需调用该方法来刷新数据
     */
    @SuppressLint("ClickableViewAccessibility")
    public void refresh() {
        if (date != null && events != null) {
            LocalDate today = LocalDate.now();
            isToday = today.equals(date);
            if (isToday) {//启动定时刷新curTimeLine任务
                mHandler.post(mCurTimeRefreshRunnable);
            } else {
                mHandler.removeCallbacks(mCurTimeRefreshRunnable);
            }
            removeAllViews();
            // 查找发生在当前日期的事件
            long startTime = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long endTime = startTime + 86399999;// 86399999 = 24*60*60*1000-1
            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                List<SpecEvent> specEvents = event.getSpecEventsDuring(startTime, endTime);
                // 为每个事件创建一个按钮，并为各个按钮设置长按监听
                for (SpecEvent specEvent : specEvents) {
                    SpecEventButton specEventButton = new SpecEventButton(mContext, specEvent);
                    Runnable r = () -> {
                        // 切换事件的完成状态
                        if (DbUtil.specEventIsFinished(mContext, specEvent)) {
                            if (DbUtil.unfinishSpecEvent(mContext, specEvent))
                                specEventButton.toggleFinishState();
                        } else {
                            if (DbUtil.finishSpecEvent(mContext, specEvent))
                                specEventButton.toggleFinishState();
                        }
                    };
                    specEventButton.setOnTouchListener(new OnTouchListener() {
                        int mDownX;
                        int mDownY;

                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    mHandler.removeCallbacks(r);
                                    mDownX = (int) event.getX();
                                    mDownY = (int) event.getY();
                                    mHandler.postDelayed(r, 800);
                                    break;
                                case MotionEvent.ACTION_MOVE:
                                    if (Math.abs(mDownX - event.getX()) > mTouchSlop
                                            || Math.abs(mDownY - event.getY()) > mTouchSlop) {
                                        // 移动过远则不是长按
                                        mHandler.removeCallbacks(r);
                                    }
                                    break;
                                case MotionEvent.ACTION_UP:
                                case MotionEvent.ACTION_CANCEL:
                                    // 当用户触摸到事件按钮，但其实是为了在ScrollView中上下滑动或者
                                    // 在ViewPager中左右滑动时，父view会传ACTION_CANCEL通知事件
                                    // 按钮停止处理手势，所以收到ACTION_CANCEL时必须结束长按
                                    mHandler.removeCallbacks(r);
                            }
                            // 对于View，如果onTouch()return true，onTouchEvent()就不会触发，
                            // 而内置的click事件都依赖onTouchEvent()，所以这里return true会
                            // 导致按钮被点击时没有点击效果
                            return false;
                        }
                    });
                    addView(specEventButton);
                }
            }
            invalidate();
        }
    }

    private void initAttrs(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {

        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScheduleLayout, defStyleAttr, defStyleRes);
        try {
            tableColor = a.getColor(R.styleable.ScheduleLayout_fgColor, Color.BLACK);
            curTimeLineColor = a.getColor(R.styleable.ScheduleLayout_curTimeLineColor, Color.BLACK);
            curTimeLineWidth = a.getDimensionPixelSize(R.styleable.ScheduleLayout_curTimeLineWidth, 1);
            timeTextSize = a.getDimensionPixelSize(R.styleable.ScheduleLayout_timeTextSize,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, metrics));
            lineWidth = a.getDimensionPixelSize(R.styleable.ScheduleLayout_lineWidth, 1);
            remainTop = a.getDimensionPixelSize(R.styleable.ScheduleLayout_remainTop, 0);
            remainBottom = a.getDimensionPixelSize(R.styleable.ScheduleLayout_remainBottom, 0);
            remainLeft = a.getDimensionPixelSize(R.styleable.ScheduleLayout_remainLeft, 0);
            remainRight = a.getDimensionPixelSize(R.styleable.ScheduleLayout_remainRight, 0);
            antiAlias = a.getBoolean(R.styleable.ScheduleLayout_antiAlias, true);
        } finally {
            a.recycle();
        }
    }

    private void initPaint() {
        //画线Paint设置
        mLinePaint.setAntiAlias(antiAlias);
        mLinePaint.setStrokeWidth(lineWidth);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setColor(tableColor);
        //画时间Paint设置
        mTimePaint.setAntiAlias(antiAlias);
        mTimePaint.setTextSize(timeTextSize);
        mTimePaint.setColor(tableColor);
        //画当前时间线的Paint设置
        mCurTimePaint.setAntiAlias(antiAlias);
        mCurTimePaint.setStrokeWidth(curTimeLineWidth);
        mCurTimePaint.setStrokeCap(Paint.Cap.ROUND);
        mCurTimePaint.setStyle(Paint.Style.FILL);
        mCurTimePaint.setColor(curTimeLineColor);
    }

    public void initTimeTextBounds() {
        String baseText = "00:00";
        Rect rect = new Rect();
        mTimePaint.getTextBounds(baseText, 0, baseText.length(), rect);
        timeTextWidth = rect.right - rect.left;
        timeTextHeight = rect.bottom - rect.top;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {//绘制Layout背景
        super.dispatchDraw(canvas);
        for (int i = 0; i < 25; i++) {
            //绘制时间表左端时间
            String timeText = i < 10 ? "0" + i + ":00" : i + ":00";
            canvas.drawText(
                    timeText,
                    remainLeft,
                    remainTop + timeTextHeight + i * TABLE_HEIGHT / 24.0f,
                    mTimePaint);
            //绘制时间表的线
            canvas.drawLine(
                    remainLeft + timeTextWidth + 20,
                    remainTop + timeTextHeight / 2.0f + i * TABLE_HEIGHT / 24.0f,
                    mWidth - remainRight,
                    remainTop + timeTextHeight / 2.0f + i * TABLE_HEIGHT / 24.0f,
                    mLinePaint);
        }
        if (date != null) {
            if (isToday) {// 如果日期与当前日期一致，就画当前时间线
                LocalTime curTime = LocalTime.now();
                int curHour = curTime.getHour();
                int curMinute = curTime.getMinute();
                // 绘制当前时间线
                canvas.drawLine(
                        remainLeft + timeTextWidth + 20,
                        remainTop + timeTextHeight / 2.0f + (curHour * 60 + curMinute) * TABLE_HEIGHT / 24.0f / 60.0f,
                        mWidth - remainRight,
                        remainTop + timeTextHeight / 2.0f + (curHour * 60 + curMinute) * TABLE_HEIGHT / 24.0f / 60.0f,
                        mCurTimePaint
                );
                //绘制当前时间线左端的小圆点，让当前时间线的视觉效果更明显，小圆点的半径取：当前时间线宽*2
                canvas.drawCircle(
                        remainLeft + timeTextWidth + 20,
                        remainTop + timeTextHeight / 2.0f + (curHour * 60 + curMinute) * TABLE_HEIGHT / 24.0f / 60.0f,
                        curTimeLineWidth * 2,
                        mCurTimePaint
                );
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 测量width（高度不用测，根据常量指定）
        int width;
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int size = MeasureSpec.getSize(widthMeasureSpec);
        switch (mode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                width = size;
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                width = DEFAULT_WIDTH;
        }
        setMeasuredDimension(width, TABLE_HEIGHT + timeTextHeight + remainTop + remainBottom);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final long dayStart = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        final long dayEnd = dayStart + 86399999;
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            if (childView instanceof SpecEventButton) {// 根据eventButton的event摆放按钮
                SpecEventButton specEventButton = (SpecEventButton) childView;
                final SpecEvent specEvent = specEventButton.getSpecEvent();
                final long eventStart = specEvent.specStart;
                final long eventEnd = specEvent.specStart + specEvent.duration - 1;
                int startMinOfDay;// 事件开始于一天的第几分钟
                int endMinOfDay;// 事件结束于一天的第几分钟
                if (eventStart >= dayStart) {
                    startMinOfDay = (int) ((eventStart - dayStart) / 60000);
                } else {
                    startMinOfDay = 0;
                }
                if (eventEnd <= dayEnd) {
                    endMinOfDay = (int) ((eventEnd - dayStart) / 60000);
                } else {
                    endMinOfDay = 1440;// 24 * 60
                }
                childView.layout(
                        remainLeft + timeTextWidth + 20,
                        remainTop + timeTextHeight / 2 + startMinOfDay * TABLE_HEIGHT / 1440,
                        mWidth - remainRight,
                        remainTop + timeTextHeight / 2 + endMinOfDay * TABLE_HEIGHT / 1440
                );
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacks(mCurTimeRefreshRunnable);
    }
}
