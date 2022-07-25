package com.cyberlight.perfect.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import com.cyberlight.perfect.R;

/**
 * 可计数按钮（外观类似单选按钮）
 */
public class CountableRadioButton extends View {
    // 默认尺寸，当指定wrap_content的时候就会使用这个尺寸
    private static final int DEFAULT_SIZE_IN_DIP = 21;

    private boolean mAntiAlias;
    private int mTextColor;
    private int mTextSize;
    private int mHollowCircleColor;
    private int mHollowCircleArcWidth;
    private int mSolidCircleColor;

    private final Point mCenterPoint = new Point();// 圆心坐标
    private float mRadius;// 圆弧半径
    private float mTextY;// 文字绘制时的y坐标
    private final TextPaint mTextPaint = new TextPaint();
    private final Paint mHollowCirclePaint = new Paint();
    private final Paint mSolidCirclePaint = new Paint();

    //    private OnCountChangedListener mOnCountChangedListener;
    private int mCount = 0;
    private int mMaxCount = Integer.MAX_VALUE;

    public CountableRadioButton(Context context) {
        this(context, null);
    }

    public CountableRadioButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CountableRadioButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.DefaultCountableRadioButton);
    }

    public CountableRadioButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        // 初始化各attr属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.CountableRadioButton, defStyleAttr, defStyleRes);
        try {
            mAntiAlias = typedArray.getBoolean(
                    R.styleable.CountableRadioButton_antiAlias, true);
            mTextColor = typedArray.getColor(
                    R.styleable.CountableRadioButton_textColor, Color.BLACK);
            mTextSize = typedArray.getDimensionPixelSize(
                    R.styleable.CountableRadioButton_textSize, -1);
            mHollowCircleColor = typedArray.getColor(
                    R.styleable.CountableRadioButton_hollowCircleColor, Color.BLACK);
            mHollowCircleArcWidth = typedArray.getDimensionPixelSize(
                    R.styleable.CountableRadioButton_hollowCircleArcWidth, 1);
            mSolidCircleColor = typedArray.getColor(
                    R.styleable.CountableRadioButton_solidCircleColor, Color.BLACK);
        } finally {
            typedArray.recycle();
        }
        // 初始化各个Paint对象
        mTextPaint.setAntiAlias(mAntiAlias);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mHollowCirclePaint.setAntiAlias(mAntiAlias);
        mHollowCirclePaint.setColor(mHollowCircleColor);
        mHollowCirclePaint.setStrokeWidth(mHollowCircleArcWidth);
        mHollowCirclePaint.setStyle(Paint.Style.STROKE);
        mSolidCirclePaint.setAntiAlias(mAntiAlias);
        mSolidCirclePaint.setColor(mSolidCircleColor);
        mSolidCirclePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int defaultSizeInPx = Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, DEFAULT_SIZE_IN_DIP,
                getResources().getDisplayMetrics()));
        setMeasuredDimension(resolveSize(defaultSizeInPx, widthMeasureSpec),
                resolveSize(defaultSizeInPx, heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 计算圆心
        mCenterPoint.x = w / 2;
        mCenterPoint.y = h / 2;
        // 求更小值作为半径（因为圆应当贴合最窄的边界）
        mRadius = Math.min(w - getPaddingLeft() - getPaddingRight() - mHollowCircleArcWidth,
                h - getPaddingTop() - getPaddingBottom() - mHollowCircleArcWidth) / 2.0f;
        // 计算文字绘制时的y坐标
        mTextY = mCenterPoint.y - (mTextPaint.ascent() + mTextPaint.descent()) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mCount == mMaxCount) {
            // 画圆
            canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mRadius - 8, mSolidCirclePaint);
        } else if (mCount > 0) {
            // 由于Paint已设置为居中绘制，故不需要把文字的x绘制坐标设为mCenterPoint.x - textWidth / 2
            canvas.drawText(String.valueOf(mCount), mCenterPoint.x, mTextY, mTextPaint);
        }
        // 画圆环
        canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mRadius, mHollowCirclePaint);
    }

    /**
     * 设置计数值，限制范围在0~maxCount
     *
     * @param count 计数值
     */
    public void setCount(int count) {
        if (mCount >= 0 && mCount <= mMaxCount && mCount != count) {
            mCount = count;
            invalidate();
        }
    }

    /**
     * 设置最大值，如果新的最大值小于当前计数值，
     * 会将计数值置为新的最大值
     *
     * @param maxCount 最大值
     */
    public void setMaxCount(int maxCount) {
        if (mMaxCount >= 1 && mMaxCount != maxCount) {
            if (maxCount < mCount) mCount = maxCount;
            mMaxCount = maxCount;
            invalidate();
        }
    }
}