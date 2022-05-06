package com.cyberlight.perfect.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.cyberlight.perfect.R;

public class CircularProgressView extends View {
    private static final int DEFAULT_SIZE_IN_DIP = 280;

    private boolean mShowProgress;
    private boolean mShowText;
    private boolean mAntiAlias;
    private float mStartAngle;
    private float mSweepAngle;
    private int mTextColor;
    private int mTextSize;
    private int mArcColor;
    private int mArcWidth;
    private int mBgArcColor;
    private int mBgArcWidth;

    private final Point mCenterPoint;// 圆心坐标
    private float mTextY;// 文字绘制时的y坐标
    private final RectF mArcRectF;// 圆弧矩形边界

    private final TextPaint mTextPaint;
    private final Paint mArcPaint;
    private final Paint mBgArcPaint;

    private float mProgress = 0.0f;// 进度条进度，范围在[0.0f,1.0f]之间
    private String mText = "";// 绘制的文字
    private ValueAnimator mAnimator;

    public CircularProgressView(Context context) {
        this(context, null);
    }

    public CircularProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.DefaultCircularProgressView);
    }

    public CircularProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mArcRectF = new RectF();
        mCenterPoint = new Point();
        mTextPaint = new TextPaint();
        mArcPaint = new Paint();
        mBgArcPaint = new Paint();
        initAttrs(context, attrs, defStyleAttr, defStyleRes);
        initPaint();
    }

    private void initAttrs(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularProgressView,
                defStyleAttr, defStyleRes);
        try {
            mShowProgress = a.getBoolean(
                    R.styleable.CircularProgressView_showProgress, true);
            mShowText = a.getBoolean(
                    R.styleable.CircularProgressView_showText, false);
            mAntiAlias = a.getBoolean(
                    R.styleable.CircularProgressView_antiAlias, true);
            mStartAngle = a.getFloat(
                    R.styleable.CircularProgressView_startAngle, 270);
            mSweepAngle = a.getFloat(
                    R.styleable.CircularProgressView_sweepAngle, 360);
            mTextColor = a.getColor(
                    R.styleable.CircularProgressView_textColor, Color.BLACK);
            mTextSize = a.getDimensionPixelSize(
                    R.styleable.CircularProgressView_textSize, -1);
            mArcColor = a.getColor(
                    R.styleable.CircularProgressView_fgArcColor, Color.BLACK);
            mArcWidth = a.getDimensionPixelSize(
                    R.styleable.CircularProgressView_fgArcWidth, 1);
            mBgArcColor = a.getColor(
                    R.styleable.CircularProgressView_bgArcColor, Color.BLACK);
            mBgArcWidth = a.getDimensionPixelSize(
                    R.styleable.CircularProgressView_bgArcWidth, 1);
        } finally {
            a.recycle();
        }
    }

    private void initPaint() {
        mTextPaint.setAntiAlias(mAntiAlias);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mArcPaint.setAntiAlias(mAntiAlias);
        mArcPaint.setColor(mArcColor);
        mArcPaint.setStrokeWidth(mArcWidth);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeCap(Paint.Cap.ROUND);

        mBgArcPaint.setAntiAlias(mAntiAlias);
        mBgArcPaint.setColor(mBgArcColor);
        mBgArcPaint.setStrokeWidth(mBgArcWidth);
        mBgArcPaint.setStyle(Paint.Style.STROKE);
        mBgArcPaint.setStrokeCap(Paint.Cap.ROUND);
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
        // 求圆弧和背景圆弧的最大宽度
        int maxArcWidth = Math.max(mArcWidth, mBgArcWidth);
        // 求更小值作为直径（因为圆弧应当贴合最窄的边界）
        int d = Math.min(w - getPaddingLeft() - getPaddingRight() - maxArcWidth,
                h - getPaddingTop() - getPaddingBottom() - maxArcWidth);
        // 计算圆弧半径
        float r = d / 2.0f;
        // 计算圆弧边界矩形
        mArcRectF.left = mCenterPoint.x - r;
        mArcRectF.top = mCenterPoint.y - r;
        mArcRectF.right = mCenterPoint.x + r;
        mArcRectF.bottom = mCenterPoint.y + r;
        // 计算文字绘制时的y坐标
        mTextY = mCenterPoint.y - (mTextPaint.ascent() + mTextPaint.descent()) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mShowText) {
            // 由于Paint已设置为居中绘制，故不需要把文字的x绘制坐标设为mCenterPoint.x - textWidth / 2
            canvas.drawText(mText, mCenterPoint.x, mTextY, mTextPaint);
        }
        if (mShowProgress) {
            // 从进度圆弧结束的地方开始重新绘制，优化性能
            canvas.save();
            // 画背景圆弧
            canvas.drawArc(mArcRectF, mStartAngle, mSweepAngle, false, mBgArcPaint);
            // 画圆弧
            canvas.drawArc(mArcRectF, mStartAngle, mSweepAngle * mProgress,
                    false, mArcPaint);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mAnimator != null)
            mAnimator.cancel();
        super.onDetachedFromWindow();
    }

    public void setShowProgress(boolean showProgress) {
        if (mShowProgress != showProgress) {
            mShowProgress = showProgress;
            invalidate();
        }
    }

    public void setShowText(boolean showText) {
        if (mShowText != showText) {
            mShowText = showText;
            invalidate();
        }
    }

    /**
     * 设置进度,从当前进度通过动画过渡到指定进度
     */
    public void setProgress(float progress, long animTime) {
        startAnimator(mProgress, progress, animTime);
    }

    public void setText(String text) {
        mText = text;
        invalidate();
    }

    private void startAnimator(float start, float end, long animTime) {
        if (mAnimator != null)
            mAnimator.pause();
        mAnimator = ValueAnimator.ofFloat(start, end);
        mAnimator.setDuration(animTime);
        mAnimator.setInterpolator(new LinearInterpolator());// 动画匀速
        mAnimator.addUpdateListener(animation -> {
            mProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        mAnimator.start();
    }
}