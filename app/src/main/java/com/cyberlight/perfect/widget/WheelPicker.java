package com.cyberlight.perfect.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import androidx.annotation.Nullable;

import com.cyberlight.perfect.R;

import java.text.Format;
import java.util.List;

public class WheelPicker<T> extends View {

    /**
     * Fling最大初始速度
     */
    private static final int MAX_FLING_VELOCITY = 10000;

    /**
     * Fling最小初始速度
     */
    private static final int MIN_FLING_VELOCITY = 50;

    /**
     * 选择器的数据集
     */
    private List<T> mDataList;

    /**
     * 选择器数据的格式
     */
    private Format mDataFormat;

    /**
     * 当前选中item在dataList中的下标值
     */
    private int mSelectedItemPosition;
    private OnItemSelectedListener<T> mOnItemSelectedListener;

    // 未选中项的文字样式
    private int mItemTextSize;
    private int mItemTextColor;
    private final TextPaint mItemTextPaint;

    // 选中项的文字样式
    private int mSelectedItemTextSize;
    private int mSelectedItemTextColor;
    private final TextPaint mSelectedItemTextPaint;
    private float mSelectedItemTextStrokeWidth;
    private float mSelectedItemLetterSpacing;

    // 指示文字样式
    private int mIndicatorTextSize;
    private int mIndicatorTextColor;
    private final TextPaint mIndicatorTextPaint;

    /**
     * 指示文字
     */
    private String mIndicatorText;

    /**
     * 用于测量文字最大宽度的文字
     */
    private String mMaxWidthText;

    /**
     * 用于测量文字尺寸的paint
     */
    private final Paint mMeasureTextPaint;

    /**
     * 文字最大宽度，用来保证文字都能显示完全
     */
    private int mTextMaxWidth;

    /**
     * 文字最大高度，用来保证文字都能显示完全
     */
    private int mTextMaxHeight;

    /**
     * Item高度方向留余的空间
     */
    private int mItemHeightSpace;

    /**
     * Item宽度方向留余的空间
     */
    private int mItemWidthSpace;

    /**
     * 一个item所占高度
     */
    private int mItemHeight;

    /**
     * 在选中item上下方的可见item个数(可见item总共有 mHalfVisibleItemCount*2+1 个)
     */
    private int mHalfVisibleItemCount;

    /**
     * 是否开启文字大小渐变
     */
    private boolean mIsTextSizeGradual;

    /**
     * 是否开启文字颜色渐变
     */
    private boolean mIsTextColorGradual;

    /**
     * 是否开启文字透明度渐变
     */
    private boolean mIsTextAlphaGradual;

    /**
     * 是否绘制幕布
     */
    private boolean mIsShowCurtain;

    /**
     * 幕布颜色
     */
    private int mCurtainColor;

    /**
     * 用于绘制幕布
     */
    private final Paint mCurtainPaint;

    /**
     * 是否绘制选中项的分隔线
     */
    private boolean mIsShowSelectedItemDivider;

    /**
     * 选中项的分隔线颜色
     */
    private int mSelectedItemDividerColor;

    /**
     * 用于绘制选中项的分隔线
     */
    private final Paint mSelectedItemDividerPaint;

    /**
     * 整个选择器的绘制区域
     */
    private final Rect mDrawRect;

    /**
     * 选中item的区域
     */
    private final Rect mSelectedItemRect;

    /**
     * item文字绘制时的X坐标，使文字水平居中
     */
    private int mTextDrawX;

    /**
     * 第一个item文字绘制时的Y坐标，使文字垂直居中
     */
    private int mFirstTextDrawY;

    /**
     * 中间item(也就是选中item)文字绘制时的Y坐标，使文字垂直居中
     */
    private int mCenterTextDrawY;

    /**
     * 用于在手势移动超过TouchSlop之后使TouchSlop失效
     */
    private boolean mEnableTouchSlop;

    /**
     * TouchSlop，决定将手势解释为移动手势时需要移动的距离
     */
    private final int mTouchSlop;

    /**
     * 是否循环读取
     */
    private boolean mIsCyclic;

    /**
     * 处理触摸事件时用到的辅助变量，记录上个触摸事件的Y坐标
     */
    private int mLastTouchY;

    /**
     * 处理触摸事件时用到的辅助变量，记录手指按下时的Y坐标
     */
    private int mTouchDownY;

    /**
     * 处理触摸事件时用到的辅助变量，用于指示用户手势是否为取消滚动的手势
     */
    private boolean mIsAbortScroller;

    /**
     * Y方向偏移量，用来实现界面滚动
     */
    private int mOffsetY;

    /**
     * Fling的最大Y值，限制滚动范围(Cyclic模式下置为Integer.MAX_VALUE)
     */
    private int mMaxFlingY;

    /**
     * Fling的最小Y值，限制滚动范围(Cyclic模式下置为Integer.MIN_VALUE)
     */
    private int mMinFlingY;

    /**
     * 用于跟踪和计算手势速度
     */
    private VelocityTracker mVelocityTracker;

    /**
     * 用于实现滚动
     */
    private final Scroller mScroller;

    private final Handler mHandler = new Handler();

    /**
     * 将scroller的值应用到界面
     */
    private final Runnable mScrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (mScroller.computeScrollOffset()) {
                mOffsetY = mScroller.getCurrY();
                selectByOffsetY();
                postInvalidate();
                mHandler.postDelayed(this, 15);
            }
        }
    };

    public WheelPicker(Context context) {
        this(context, null);
    }

    public WheelPicker(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WheelPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.DefaultWheelPicker);
    }

    public WheelPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mItemTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
        mSelectedItemTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
        mIndicatorTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
        mCurtainPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mSelectedItemDividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mMeasureTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.LINEAR_TEXT_FLAG);
        mDrawRect = new Rect();
        mSelectedItemRect = new Rect();
        mScroller = new Scroller(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        initAttrs(context, attrs, defStyleAttr, defStyleRes);
        initPaint();
    }

    private void initAttrs(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.WheelPicker, defStyleAttr, defStyleRes);
        try {
            mItemTextSize = a.getDimensionPixelSize(
                    R.styleable.WheelPicker_itemTextSize, -1);
            mItemTextColor = a.getColor(
                    R.styleable.WheelPicker_itemTextColor, Color.BLACK);
            mSelectedItemTextSize = a.getDimensionPixelSize(
                    R.styleable.WheelPicker_selectedItemTextSize, -1);
            mSelectedItemTextColor = a.getColor(
                    R.styleable.WheelPicker_selectedItemTextColor, Color.BLACK);
            mSelectedItemTextStrokeWidth = a.getFloat(
                    R.styleable.WheelPicker_selectedItemTextStrokeWidth, 0);
            mSelectedItemLetterSpacing = a.getFloat(
                    R.styleable.WheelPicker_selectedItemLetterSpacing, 0);
            mIndicatorTextSize = a.getDimensionPixelSize(
                    R.styleable.WheelPicker_indicatorTextSize, -1);
            mIndicatorTextColor = a.getColor(
                    R.styleable.WheelPicker_indicatorTextColor, Color.BLACK);
            mIndicatorText = a.getString(
                    R.styleable.WheelPicker_indicatorText);
            mMaxWidthText = a.getString(
                    R.styleable.WheelPicker_maxWidthText);
            mItemWidthSpace = a.getDimensionPixelSize(
                    R.styleable.WheelPicker_itemWidthSpace, 0);
            mItemHeightSpace = a.getDimensionPixelSize(
                    R.styleable.WheelPicker_itemHeightSpace, 0);
            mHalfVisibleItemCount = a.getInteger(
                    R.styleable.WheelPicker_halfVisibleItemCount, 2);
            mIsTextSizeGradual = a.getBoolean(
                    R.styleable.WheelPicker_textSizeGradual, true);
            mIsTextColorGradual = a.getBoolean(
                    R.styleable.WheelPicker_textColorGradual, true);
            mIsTextAlphaGradual = a.getBoolean(
                    R.styleable.WheelPicker_textAlphaGradual, true);
            mIsShowCurtain = a.getBoolean(
                    R.styleable.WheelPicker_showCurtain, false);
            mCurtainColor = a.getColor(
                    R.styleable.WheelPicker_curtainColor, Color.GRAY);
            mIsShowSelectedItemDivider = a.getBoolean(
                    R.styleable.WheelPicker_showSelectedItemDivider, true);
            mSelectedItemDividerColor = a.getColor(
                    R.styleable.WheelPicker_selectedItemDividerColor, Color.BLACK);
            mIsCyclic = a.getBoolean(
                    R.styleable.WheelPicker_cyclic, false);
        } finally {
            a.recycle();
        }
    }

    private void initPaint() {
        mCurtainPaint.setStyle(Paint.Style.FILL);
        mCurtainPaint.setColor(mCurtainColor);

        mSelectedItemDividerPaint.setStyle(Paint.Style.STROKE);
        mSelectedItemDividerPaint.setColor(mSelectedItemDividerColor);

        mItemTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mItemTextPaint.setTextAlign(Paint.Align.CENTER);
        mItemTextPaint.setColor(mItemTextColor);
        mItemTextPaint.setTextSize(mItemTextSize);

        mSelectedItemTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mSelectedItemTextPaint.setTextAlign(Paint.Align.CENTER);
        mSelectedItemTextPaint.setColor(mSelectedItemTextColor);
        mSelectedItemTextPaint.setTextSize(mSelectedItemTextSize);
        mSelectedItemTextPaint.setStrokeWidth(mSelectedItemTextStrokeWidth);
        mSelectedItemTextPaint.setLetterSpacing(mSelectedItemLetterSpacing);

        mIndicatorTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mIndicatorTextPaint.setTextAlign(Paint.Align.LEFT);
        mIndicatorTextPaint.setColor(mIndicatorTextColor);
        mIndicatorTextPaint.setTextSize(mIndicatorTextSize);

        mMeasureTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mMeasureTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    /**
     * 计算实际的大小，用于onMeasure里测量
     */
    private int measureSize(int specMode, int specSize, int size) {
        if (specMode == MeasureSpec.EXACTLY) {
            return specSize;
        } else {
            return Math.min(specSize, size);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int specWidthSize = MeasureSpec.getSize(widthMeasureSpec);
        int specWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int specHeightSize = MeasureSpec.getSize(heightMeasureSpec);
        int specHeightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = mTextMaxWidth + mItemWidthSpace + getPaddingLeft() + getPaddingRight();
        int height = (mTextMaxHeight + mItemHeightSpace) * getVisibleItemCount() + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(measureSize(specWidthMode, specWidthSize, width),
                measureSize(specHeightMode, specHeightSize, height));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 计算整个控件绘制的区域
        mDrawRect.set(getPaddingLeft(),
                getPaddingTop(),
                w - getPaddingRight(),
                h - getPaddingBottom());
        // 计算item的高度
        mItemHeight = mDrawRect.height() / getVisibleItemCount();
        // 计算文字绘制的X坐标，使文字在item里水平居中
        mTextDrawX = mDrawRect.centerX();
        // 计算文字绘制的Y坐标，使文字在item里垂直居中
        mFirstTextDrawY = mDrawRect.top + (int) ((mItemHeight - (mSelectedItemTextPaint.ascent() +
                mSelectedItemTextPaint.descent())) / 2);
        mCenterTextDrawY = mFirstTextDrawY + mItemHeight * mHalfVisibleItemCount;
        // 计算selectedItem的边框位置
        mSelectedItemRect.set(mDrawRect.left,
                mItemHeight * mHalfVisibleItemCount + mDrawRect.top,
                mDrawRect.right,
                mItemHeight * mHalfVisibleItemCount + mDrawRect.top + mItemHeight);
        // 计算Fling极限
        computeFlingLimitY();
        // 计算offsetY
        mOffsetY = -mItemHeight * mSelectedItemPosition;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mIsShowCurtain) {
            canvas.drawRect(mSelectedItemRect, mCurtainPaint);
        }
        if (mIsShowSelectedItemDivider) {
            canvas.drawLine(mSelectedItemRect.left, mSelectedItemRect.top, mSelectedItemRect.right, mSelectedItemRect.top, mSelectedItemDividerPaint);
            canvas.drawLine(mSelectedItemRect.left, mSelectedItemRect.bottom, mSelectedItemRect.right, mSelectedItemRect.bottom, mSelectedItemDividerPaint);
        }
        int selectedPos = computePosition(mOffsetY);
        // 首尾各多绘制一个用于缓冲
        for (int drawPos = selectedPos - mHalfVisibleItemCount - 1;
             drawPos <= selectedPos + mHalfVisibleItemCount + 1; drawPos++) {
            int dataPos = drawPos;
            if (mIsCyclic) {
                dataPos = fixPosition(dataPos);
            } else if (dataPos < 0 || dataPos > mDataList.size() - 1) {
                continue;
            }
            T data = mDataList.get(dataPos);
            int itemDrawY = mFirstTextDrawY + (drawPos + mHalfVisibleItemCount) * mItemHeight + mOffsetY;
            int distanceY = Math.abs(mCenterTextDrawY - itemDrawY);
            // 文字颜色渐变(文字颜色渐变要在设置透明度上边，否则会被覆盖)
            if (mIsTextColorGradual && distanceY < mItemHeight) {
                float colorRatio = 1 - (distanceY / (float) mItemHeight);
                mSelectedItemTextPaint.setColor(getGradientColor(mItemTextColor, mSelectedItemTextColor, colorRatio));
                mItemTextPaint.setColor(getGradientColor(mItemTextColor, mSelectedItemTextColor, colorRatio));
            } else {
                mSelectedItemTextPaint.setColor(mSelectedItemTextColor);
                mItemTextPaint.setColor(mItemTextColor);
            }
            // 文字透明度渐变
            if (mIsTextAlphaGradual) {
                float alphaRatio;
                if (itemDrawY > mCenterTextDrawY) {// item在绘制区域下半部分
                    alphaRatio = (float) (mDrawRect.bottom - itemDrawY) / (mDrawRect.bottom - mCenterTextDrawY);
                } else {// item在绘制区域上半部分
                    alphaRatio = (float) (itemDrawY - mDrawRect.top) / (mCenterTextDrawY - mDrawRect.top);
                }
                alphaRatio = alphaRatio < 0 ? 0 : alphaRatio;
                mSelectedItemTextPaint.setAlpha((int) (alphaRatio * 255));
                mItemTextPaint.setAlpha((int) (alphaRatio * 255));
            }
            // 文字大小渐变
            if (mIsTextSizeGradual && distanceY < mItemHeight) {
                float addedSize = (mItemHeight - distanceY) / (float) mItemHeight * (mSelectedItemTextSize - mItemTextSize);
                mSelectedItemTextPaint.setTextSize(mItemTextSize + addedSize);
                mItemTextPaint.setTextSize(mItemTextSize + addedSize);
            } else {
                mSelectedItemTextPaint.setTextSize(mSelectedItemTextSize);
                mItemTextPaint.setTextSize(mItemTextSize);
            }
            // 绘制文字
            String drawText = mDataFormat == null ? data.toString() : mDataFormat.format(data);
            if (drawPos == selectedPos) {
                // 绘制选中项
                canvas.drawText(drawText, mTextDrawX, itemDrawY, mSelectedItemTextPaint);
            } else {
                // 绘制未选中项
                canvas.drawText(drawText, mTextDrawX, itemDrawY, mItemTextPaint);
            }
        }
        if (!TextUtils.isEmpty(mIndicatorText)) {
            canvas.drawText(mIndicatorText, mTextDrawX + mTextMaxWidth / 2.0f, mCenterTextDrawY, mIndicatorTextPaint);
        }

        //TEST
        Rect tempRect = new Rect();
        tempRect.left = mDrawRect.left + 1;
        tempRect.top = mDrawRect.top + 1;
        tempRect.right = mDrawRect.right;
        tempRect.bottom = mDrawRect.bottom;
        Paint p = new Paint();
        p.setColor(Color.BLUE);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1);
        canvas.drawRect(tempRect, p);
    }

    @Override
    protected void onDetachedFromWindow() {
        //停止滚动释放资源
        mHandler.removeCallbacks(mScrollRunnable);
        super.onDetachedFromWindow();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                    mIsAbortScroller = true;
                } else {
                    mIsAbortScroller = false;
                }
                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                } else {
                    mVelocityTracker.clear();
                }
                mVelocityTracker.addMovement(event);
                mTouchDownY = mLastTouchY = (int) event.getY();
                mEnableTouchSlop = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mEnableTouchSlop && Math.abs(mTouchDownY - event.getY()) < mTouchSlop) {
                    break;
                }
                mEnableTouchSlop = false;
                mVelocityTracker.addMovement(event);
                float move = event.getY() - mLastTouchY;
                mOffsetY += move;
                selectByOffsetY();
                mLastTouchY = (int) event.getY();
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                performClick();
                if (!mIsAbortScroller && mTouchDownY == mLastTouchY) {
                    //用户点击后滚动到所点击item的位置
                    if (event.getY() > mSelectedItemRect.bottom) {
                        //用户点击selectedItemRect下方
                        int scrollItem = (int) (event.getY() - mSelectedItemRect.bottom) / mItemHeight + 1;
                        mScroller.startScroll(
                                0,
                                mOffsetY,
                                0,
                                -scrollItem * mItemHeight
                        );
                    } else if (event.getY() < mSelectedItemRect.top) {
                        //用户点击selectedItemRect上方
                        int scrollItem = (int) (mSelectedItemRect.top - event.getY()) / mItemHeight + 1;
                        mScroller.startScroll(
                                0,
                                mOffsetY,
                                0,
                                scrollItem * mItemHeight
                        );
                    }
                } else {
                    //用户滑动
                    mVelocityTracker.computeCurrentVelocity(1000, MAX_FLING_VELOCITY);
                    int velocityY = (int) mVelocityTracker.getYVelocity();
                    if (Math.abs(velocityY) > MIN_FLING_VELOCITY &&
                            !(mOffsetY < mMinFlingY && velocityY < 0) &&
                            !(mOffsetY > mMaxFlingY && velocityY > 0)
                    ) {
                        //用户滑动较快，开始fling
                        mScroller.fling(
                                0,
                                mOffsetY,
                                0,
                                velocityY,
                                0,
                                0,
                                mMinFlingY,
                                mMaxFlingY
                        );
                        //使fling结束在item对齐的位置
                        mScroller.setFinalY(
                                mScroller.getFinalY() + computeDistanceToEndY(mScroller.getFinalY())
                        );
                    } else {
                        //用户滑动较慢，此时的情形为用户手指定在屏幕上不动然后松手，所以将item滚动到最近对齐位置即可
                        mScroller.startScroll(
                                0,
                                mOffsetY,
                                0,
                                computeDistanceToEndY(mOffsetY)
                        );
                    }
                }
                if (!mIsCyclic) {
                    if (mScroller.getFinalY() > mMaxFlingY) {
                        mScroller.setFinalY(mMaxFlingY);
                    } else if (mScroller.getFinalY() < mMinFlingY) {
                        mScroller.setFinalY(mMinFlingY);
                    }
                }
                mHandler.post(mScrollRunnable);//开始让界面跟随scroller的数据滚动
                mVelocityTracker.recycle();
                mVelocityTracker = null;//velocityTracker回收后不能再用
                break;
        }
        return true;
    }

    /**
     * 选择器选择当前mOffsetY对应的item
     * 每当用户通过手势改变mOffsetY后，都应该调用该方法
     */
    private void selectByOffsetY() {
        int position = computePosition(mOffsetY);
        if (mIsCyclic) {
            position = fixPosition(position);
        } else if (position < 0 || position > mDataList.size() - 1) {
            return;
        }
        if (mSelectedItemPosition != position) {
            mSelectedItemPosition = position;
            if (mOnItemSelectedListener != null) {
                //回调监听器接口的onItemSelected
                mOnItemSelectedListener.onItemSelected(mDataList.get(position), position);
            }
        }
    }

    /**
     * 计算Fling极限
     * 如果为Cyclic模式则为Integer的极限值，如果正常模式，则为一整个数据集的上下限
     */
    private void computeFlingLimitY() {
        mMinFlingY = mIsCyclic ? Integer.MIN_VALUE :
                -mItemHeight * (mDataList.size() - 1);
        mMaxFlingY = mIsCyclic ? Integer.MAX_VALUE : 0;
    }

    /**
     * 计算文字最大尺寸
     */
    private void computeTextMaxSize() {
        //选更大字体来测量Text
        mMeasureTextPaint.setTextSize(Math.max(mSelectedItemTextSize, mItemTextSize));
        if (!TextUtils.isEmpty(mMaxWidthText)) {
            mTextMaxWidth = (int) mMeasureTextPaint.measureText(mMaxWidthText);
        } else if (mDataList != null && mDataList.size() > 0) {
            mTextMaxWidth = (int) mMeasureTextPaint.measureText(mDataList.get(0).toString());
        }
        Paint.FontMetrics fontMetrics = mMeasureTextPaint.getFontMetrics();
        mTextMaxHeight = (int) (fontMetrics.bottom - fontMetrics.top);
    }

    /**
     * 计算指定offsetY与最近对齐位置的距离
     */
    private int computeDistanceToEndY(int offsetY) {
        int remainder = offsetY % mItemHeight;
        if (remainder <= -mItemHeight / 2) {
            return -mItemHeight - remainder;
        } else if (remainder > mItemHeight / 2) {
            return mItemHeight - remainder;
        } else {
            return -remainder;
        }
    }

    /**
     * 根据offsetY计算position(在cyclic模式下计算结果还应该用fixPosition()修正)
     */
    private int computePosition(int offsetY) {
        int base = -offsetY + mItemHeight / 2;
        return base < 0 ? base / mItemHeight - 1 : base / mItemHeight;
    }

    /**
     * cyclic模式下将position修正到0~mDataList.size()中
     */
    private int fixPosition(int position) {
        int r = position % mDataList.size();
        return r < 0 ? r + mDataList.size() : r;
    }

    /**
     * 获取可见item数
     */
    private int getVisibleItemCount() {
        return mHalfVisibleItemCount * 2 + 1;
    }

    /**
     * 计算并返回线性渐变色
     */
    private int getGradientColor(int startColor, int endColor, float ratio) {
        int sR = Color.red(startColor);
        int sG = Color.green(startColor);
        int sB = Color.blue(startColor);
        int eR = Color.red(endColor);
        int eG = Color.green(endColor);
        int eB = Color.blue(endColor);
        int r = (int) (sR + ((eR - sR) * ratio + 0.5));
        int g = (int) (sG + ((eG - sG) * ratio + 0.5));
        int b = (int) (sB + ((eB - sB) * ratio + 0.5));
        return Color.rgb(r, g, b);
    }

    /**
     * 设置数据集
     */
    public void setDataList(List<T> dataList) {
        mDataList = dataList;
        if (dataList != null && dataList.size() > 0) {
            computeTextMaxSize();
            computeFlingLimitY();
            requestLayout();
            postInvalidate();
        }
    }

    /**
     * 设置数据格式
     */
    public void setDataFormat(Format dataFormat) {
        mDataFormat = dataFormat;
        postInvalidate();
    }

    /**
     * 设置未选中item的文字大小
     */
    public void setItemTextSize(int itemTextSize) {
        if (mItemTextSize != itemTextSize) {
            mItemTextSize = itemTextSize;
            mItemTextPaint.setTextSize(itemTextSize);
            computeTextMaxSize();
            requestLayout();
            postInvalidate();
        }
    }

    /**
     * 设置未选中item的文字颜色
     */
    public void setItemTextColor(int itemTextColor) {
        if (mItemTextColor != itemTextColor) {
            mItemTextColor = itemTextColor;
            mItemTextPaint.setColor(itemTextColor);
            postInvalidate();
        }
    }

    /**
     * 设置选中item的文字大小
     */
    public void setSelectedItemTextSize(int selectedItemTextSize) {
        if (mSelectedItemTextSize != selectedItemTextSize) {
            mSelectedItemTextSize = selectedItemTextSize;
            mSelectedItemTextPaint.setTextSize(selectedItemTextSize);
            computeTextMaxSize();
            requestLayout();
            postInvalidate();
        }
    }

    /**
     * 设置选中item的文字颜色
     */
    public void setSelectedItemTextColor(int selectedItemTextColor) {
        if (mSelectedItemTextColor != selectedItemTextColor) {
            mSelectedItemTextColor = selectedItemTextColor;
            mSelectedItemTextPaint.setColor(selectedItemTextColor);
            postInvalidate();
        }
    }

    /**
     * 设置指示文字大小
     */
    public void setIndicatorTextSize(int indicatorTextSize) {
        if (mIndicatorTextSize != indicatorTextSize) {
            mIndicatorTextSize = indicatorTextSize;
            mIndicatorTextPaint.setTextSize(indicatorTextSize);
            postInvalidate();
        }
    }

    /**
     * 设置指示文字颜色
     */
    public void setIndicatorTextColor(int indicatorTextColor) {
        if (mIndicatorTextColor != indicatorTextColor) {
            mIndicatorTextColor = indicatorTextColor;
            mIndicatorTextPaint.setColor(indicatorTextColor);
            postInvalidate();
        }
    }

    /**
     * 设置指示文字
     */
    public void setIndicatorText(String indicatorText) {
        if (mIndicatorText == null || !mIndicatorText.equals(indicatorText)) {
            mIndicatorText = indicatorText;
            postInvalidate();
        }
    }

    /**
     * 设置用于测量文字最大宽度的文本
     */
    public void setMaxWidthText(String maxWidthText) {
        mMaxWidthText = maxWidthText;
        computeTextMaxSize();
        requestLayout();
        postInvalidate();
    }

    /**
     * 设置Item高度方向留余的空间
     */
    public void setItemHeightSpace(int itemHeightSpace) {
        if (mItemHeightSpace != itemHeightSpace) {
            mItemHeightSpace = itemHeightSpace;
            requestLayout();
        }
    }

    /**
     * 设置Item宽度方向留余的空间
     */
    public void setItemWidthSpace(int itemWidthSpace) {
        if (mItemWidthSpace != itemWidthSpace) {
            mItemWidthSpace = itemWidthSpace;
            requestLayout();
        }
    }

    /**
     * 设置选中item上下可见item数目
     */
    public void setHalfVisibleItemCount(int halfVisibleItemCount) {
        if (mHalfVisibleItemCount != halfVisibleItemCount) {
            mHalfVisibleItemCount = halfVisibleItemCount;
            requestLayout();
            postInvalidate();
        }
    }

    /**
     * 设置position
     *
     * @param position 指定位置
     * @param scroll   是否采用滚动
     */
    public synchronized void setPosition(int position, boolean scroll) {
        position = fixPosition(position);
        if (mSelectedItemPosition != position) {
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
            //如果mItemHeight=0代表还没有绘制完成，无需滚动
            if (scroll && mItemHeight != 0) {
                mScroller.startScroll(0, mOffsetY, 0, (mSelectedItemPosition - position) * mItemHeight);
                //保证滚动到对齐位置，因为不一定是从对齐位置开始滚动的
                int finalY = -position * mItemHeight;
                mScroller.setFinalY(finalY);
                mHandler.post(mScrollRunnable);
            } else {
                mSelectedItemPosition = position;
                mOffsetY = -mItemHeight * mSelectedItemPosition;
                postInvalidate();
                if (mOnItemSelectedListener != null) {
                    mOnItemSelectedListener.onItemSelected(mDataList.get(position), position);
                }
            }
        }
    }

    /**
     * 设置文字大小是否渐变
     */
    public void setTextSizeGradual(boolean textSizeGradual) {
        if (mIsTextSizeGradual != textSizeGradual) {
            mIsTextSizeGradual = textSizeGradual;
            postInvalidate();
        }
    }

    /**
     * 设置文字颜色是否渐变
     */
    public void setTextColorGradual(boolean textColorGradual) {
        if (mIsTextColorGradual != textColorGradual) {
            mIsTextColorGradual = textColorGradual;
            postInvalidate();
        }
    }

    /**
     * 设置文字透明度是否渐变
     */
    public void setTextAlphaGradual(boolean textAlphaGradual) {
        if (mIsTextAlphaGradual != textAlphaGradual) {
            mIsTextAlphaGradual = textAlphaGradual;
            postInvalidate();
        }
    }

    /**
     * 设置是否显示幕布
     */
    public void setShowCurtain(boolean showCurtain) {
        if (mIsShowCurtain != showCurtain) {
            mIsShowCurtain = showCurtain;
            postInvalidate();
        }
    }

    /**
     * 设置幕布颜色
     */
    public void setCurtainColor(int curtainColor) {
        if (mCurtainColor != curtainColor) {
            mCurtainColor = curtainColor;
            mCurtainPaint.setColor(curtainColor);
            postInvalidate();
        }
    }

    /**
     * 设置是否显示选中项分隔线
     */
    public void setShowSelectedItemDivider(boolean showSelectedItemDivider) {
        if (mIsShowSelectedItemDivider != showSelectedItemDivider) {
            mIsShowSelectedItemDivider = showSelectedItemDivider;
            postInvalidate();
        }
    }

    /**
     * 设置幕布边框颜色
     */
    public void setSelectedItemDividerColor(int selectedItemDividerColor) {
        if (mSelectedItemDividerColor != selectedItemDividerColor) {
            mSelectedItemDividerColor = selectedItemDividerColor;
            mSelectedItemDividerPaint.setColor(selectedItemDividerColor);
            postInvalidate();
        }
    }

    /**
     * 设置是否开启循环读取模式
     */
    public void setCyclic(boolean cyclic) {
        if (mIsCyclic != cyclic) {
            mIsCyclic = cyclic;
            computeFlingLimitY();
            requestLayout();
            postInvalidate();
        }
    }

    /**
     * 设置选中item监听器
     */
    public void setOnItemSelectedListener(OnItemSelectedListener<T> onItemSelectedListener) {
        mOnItemSelectedListener = onItemSelectedListener;
    }

    /**
     * 获取当前选中项的下标
     */
    public int getSelectedItemPosition() {
        return mSelectedItemPosition;
    }

    /**
     * 获取当前选中项
     */
    public T getSelectedItem() {
        return mDataList.get(mSelectedItemPosition);
    }

    /**
     * item被选中监听器接口
     */
    public interface OnItemSelectedListener<T> {
        void onItemSelected(T item, int position);
    }
}