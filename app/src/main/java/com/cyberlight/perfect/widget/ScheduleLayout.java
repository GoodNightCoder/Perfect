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
import com.cyberlight.perfect.data.Event;
import com.cyberlight.perfect.data.SpecificEvent;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 显示指定日期发生的事件的视图
 */
public class ScheduleLayout extends ViewGroup {
    // 整个layout的默认宽度
    private static final int DEFAULT_WIDTH = 1000;
    // 第一条线到最后一条线之间的像素，用于决定默认高度
    private static final int DEFAULT_TABLE_HEIGHT = 24 * 60 * 2;

    private boolean mAntiAlias;
    private int mTableColor;
    private int mCurTimeLineColor;
    private int mCurTimeLineWidth;
    private int mTimeLineWidth;
    private int mTimeTextSize;

    private final Paint mTimeLinePaint;
    private final Paint mTimeTextPaint;
    private final Paint mCurTimeLinePaint;

    private final Rect mDrawRect;
    private final float mTimeTextWidth;
    private float mFirstTimeTextY;
    private float mFirstTimeLineY;
    private int mTableHeight;
    private float mTimeLineStartX;

    private final Context mContext;
    private final int mTouchSlop;// 用于事件按钮手势判断
    private boolean mIsToday = false;// 日期是否是今天
    private LocalDate mDate;// 当前日期，由Activity通过setDate()赋值
    private List<Event> mEvents;// 当前layout所使用的事件列表，通过setEvents()修改
    private final Handler mHandler = new Handler();
    private final Runnable mCurTimeLineRunnable = new Runnable() {
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
        mContext = context;
        mDrawRect = new Rect();
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mTimeLinePaint = new Paint();
        mTimeTextPaint = new Paint();
        mCurTimeLinePaint = new Paint();
        initAttrs(context, attrs, defStyleAttr, defStyleRes);
        initPaint();
        // 测量时间文字宽度
        mTimeTextWidth = mTimeTextPaint.measureText("00:00");
    }

    public void setEvents(List<Event> events) {
        this.mEvents = events;
    }

    public void setDate(LocalDate date) {
        this.mDate = LocalDate.ofEpochDay(date.toEpochDay());
    }

    /**
     * 当日期或事件集发生改变时，外部需调用该方法来刷新数据
     */
    @SuppressLint("ClickableViewAccessibility")
    public void refresh() {
        if (mDate != null && mEvents != null) {
            LocalDate today = LocalDate.now();
            mIsToday = today.equals(mDate);
            if (mIsToday) {
                // 启动定时刷新curTimeLine任务
                mHandler.post(mCurTimeLineRunnable);
            } else {
                mHandler.removeCallbacks(mCurTimeLineRunnable);
            }
            removeAllViews();
            // 查找发生在当前日期的事件
            long startTime = mDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long endTime = startTime + 86399999;// 86399999 = 24*60*60*1000-1
            for (int i = 0; i < mEvents.size(); i++) {
                Event event = mEvents.get(i);
                List<SpecificEvent> specificEvents = event.getSpecEventsDuring(startTime, endTime);
                // 为每个事件创建一个按钮，并为各个按钮设置长按监听
                for (SpecificEvent specificEvent : specificEvents) {
                    SpecEventButton specEventButton = new SpecEventButton(mContext, specificEvent);
                    Runnable r = () -> {
                        // TODO:打开事件详情窗口，允许用户进行事件修改
                    };
                    specEventButton.setOnTouchListener(new OnTouchListener() {
                        int downX;
                        int downY;

                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    mHandler.removeCallbacks(r);
                                    downX = (int) event.getX();
                                    downY = (int) event.getY();
                                    mHandler.postDelayed(r, 800);
                                    break;
                                case MotionEvent.ACTION_MOVE:
                                    if (Math.abs(downX - event.getX()) > mTouchSlop
                                            || Math.abs(downY - event.getY()) > mTouchSlop) {
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
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ScheduleLayout, defStyleAttr, defStyleRes);
        try {
            mTableColor = a.getColor(
                    R.styleable.ScheduleLayout_fgColor, Color.BLACK);
            mCurTimeLineColor = a.getColor(
                    R.styleable.ScheduleLayout_curTimeLineColor, Color.BLACK);
            mCurTimeLineWidth = a.getDimensionPixelSize(
                    R.styleable.ScheduleLayout_curTimeLineWidth, 1);
            mTimeTextSize = a.getDimensionPixelSize(
                    R.styleable.ScheduleLayout_timeTextSize,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, metrics));
            mTimeLineWidth = a.getDimensionPixelSize(
                    R.styleable.ScheduleLayout_lineWidth, 1);
            mAntiAlias = a.getBoolean(R.styleable.ScheduleLayout_antiAlias, true);
        } finally {
            a.recycle();
        }
    }

    private void initPaint() {
        mTimeLinePaint.setAntiAlias(mAntiAlias);
        mTimeLinePaint.setStrokeWidth(mTimeLineWidth);
        mTimeLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mTimeLinePaint.setStyle(Paint.Style.STROKE);
        mTimeLinePaint.setColor(mTableColor);

        mTimeTextPaint.setAntiAlias(mAntiAlias);
        mTimeTextPaint.setTextSize(mTimeTextSize);
        mTimeTextPaint.setColor(mTableColor);

        mCurTimeLinePaint.setAntiAlias(mAntiAlias);
        mCurTimeLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mCurTimeLinePaint.setStrokeWidth(mCurTimeLineWidth);
        mCurTimeLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mCurTimeLinePaint.setColor(mCurTimeLineColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Paint.FontMetrics fontMetrics = mTimeTextPaint.getFontMetrics();
        int measuredWidth = resolveSize(DEFAULT_WIDTH, widthMeasureSpec);
        int measuredHeight = resolveSize(Math.round(DEFAULT_TABLE_HEIGHT
                        + getPaddingTop() + getPaddingBottom()
                        - fontMetrics.top + fontMetrics.bottom),
                heightMeasureSpec);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 计算整个控件绘制的区域
        mDrawRect.set(getPaddingLeft(),
                getPaddingTop(),
                w - getPaddingRight(),
                h - getPaddingBottom());
        Paint.FontMetrics fontMetrics = mTimeTextPaint.getFontMetrics();
        // 计算第一条时间线到最后一条时间线之间的距离
        mTableHeight = Math.round(mDrawRect.height() + fontMetrics.top - fontMetrics.bottom);
        // 计算时间线绘制起点x坐标
        mTimeLineStartX = mDrawRect.left + mTimeTextWidth + 20;
        // 计算第一个时间文字(00:00)绘制的y坐标
        mFirstTimeTextY = mDrawRect.top - fontMetrics.top;
        // 计算第一条时间线绘制的y坐标（位于00:00时间文字的y方向中点）
        mFirstTimeLineY = mFirstTimeTextY + (fontMetrics.ascent + fontMetrics.descent) / 2.0f;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final long dayStart = mDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        final long dayEnd = dayStart + 86399999;
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            if (childView instanceof SpecEventButton) {// 根据eventButton的event摆放按钮
                SpecEventButton specEventButton = (SpecEventButton) childView;
                final SpecificEvent specificEvent = specEventButton.getSpecEvent();
                final long eventStart = specificEvent.specStart;
                final long eventEnd = specificEvent.specStart + specificEvent.event.duration - 1;
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
                childView.layout((int) mTimeLineStartX,
                        (int) (mFirstTimeLineY + startMinOfDay * mTableHeight / 1440),
                        mDrawRect.right,
                        (int) (mFirstTimeLineY + endMinOfDay * mTableHeight / 1440));
            }
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        float lineDistance = mTableHeight / 24.0f;
        for (int i = 0; i < 25; i++) {
            // 绘制时间表左端时间
            String timeText = i < 10 ? "0" + i + ":00" : i + ":00";
            canvas.drawText(timeText,
                    mDrawRect.left,
                    mFirstTimeTextY + i * lineDistance,
                    mTimeTextPaint);
            // 绘制时间表的线
            canvas.drawLine(mTimeLineStartX,
                    mFirstTimeLineY + i * lineDistance,
                    mDrawRect.right,
                    mFirstTimeLineY + i * lineDistance,
                    mTimeLinePaint);
        }
        if (mDate != null) {
            if (mIsToday) {// 如果日期与当前日期一致，就画当前时间线
                LocalTime curTime = LocalTime.now();
                int curHour = curTime.getHour();
                int curMinute = curTime.getMinute();
                float curTimeLineY = mFirstTimeLineY + (curHour * 60 + curMinute) * mTableHeight / 1440f;
                // 绘制当前时间线
                canvas.drawLine(mTimeLineStartX,
                        curTimeLineY,
                        mDrawRect.right,
                        curTimeLineY,
                        mCurTimeLinePaint);
                // 绘制当前时间线左端的小圆点，让当前时间线的视觉效果更明显，小圆点的半径取：当前时间线宽*2
                canvas.drawCircle(mTimeLineStartX,
                        curTimeLineY,
                        mCurTimeLineWidth * 2,
                        mCurTimeLinePaint);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        mHandler.removeCallbacks(mCurTimeLineRunnable);
        super.onDetachedFromWindow();
    }
}
