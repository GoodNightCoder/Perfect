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
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.cyberlight.perfect.R;

public class CircularProgressView extends View {
    private static final String TAG = "CircularProgressView";

    //View的默认尺寸，当指定wrap_content的时候就会使用这个尺寸
    public static final int DEFAULT_SIZE = 280;

    //xml中定义属性的默认值
    public static final boolean DEFAULT_ANTI_ALIAS = true;
    public static final int DEFAULT_START_ANGLE = 270;
    public static final int DEFAULT_SWEEP_ANGLE = 360;
    public static final int DEFAULT_TEXT_COLOR = Color.WHITE;
    public static final int DEFAULT_TEXT_SIZE = 100;
    public static final int DEFAULT_ARC_COLOR = Color.WHITE;
    public static final int DEFAULT_ARC_WIDTH = 15;
    public static final int DEFAULT_BG_ARC_COLOR = Color.GRAY;
    public static final int DEFAULT_BG_ARC_WIDTH = 15;
    public static final boolean DEFAULT_SHOW_PROGRESS = true;
    public static final boolean DEFAULT_SHOW_TEXT = true;


    //xml中定义的属性
    private boolean mShowProgress;
    private boolean mShowText;
    private boolean mAntiAlias;
    private float mStartAngle;
    private float mSweepAngle;
    private int mTextColor;
    private float mTextSize;
    private int mArcColor;
    private float mArcWidth;
    private int mBgArcColor;
    private float mBgArcWidth;

    //根据属性来计算的各个数据
    private Point mCenterPoint;//圆心坐标
    private float mRadius;//圆弧半径
    private float mTextY;//文字绘制时的y坐标
    private RectF mRectF;//圆弧矩形边界
    private TextPaint mTextPaint;
    private Paint mArcPaint;
    private Paint mBgArcPaint;

    private Context mContext;
    private float mProgress = 0.0f;//当前进度条的百分比进度，范围在[0.0f,1.0f]之间
    private String mText = "";//绘制的文字
    private ValueAnimator mAnimator;//动画


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
        //初始化
        mContext = context;
        mAnimator = new ValueAnimator();
        mRectF = new RectF();
        mCenterPoint = new Point();
        initAttrs(context, attrs, defStyleAttr, defStyleRes);
        initPaint();
    }

    private void initAttrs(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularProgressView, defStyleAttr, defStyleRes);
        try {
            mShowProgress = a.getBoolean(R.styleable.CircularProgressView_showProgress, DEFAULT_SHOW_PROGRESS);
            mShowText = a.getBoolean(R.styleable.CircularProgressView_showText, DEFAULT_SHOW_TEXT);
            mAntiAlias = a.getBoolean(R.styleable.CircularProgressView_antiAlias, DEFAULT_ANTI_ALIAS);
            mStartAngle = a.getFloat(R.styleable.CircularProgressView_startAngle, DEFAULT_START_ANGLE);
            mSweepAngle = a.getFloat(R.styleable.CircularProgressView_sweepAngle, DEFAULT_SWEEP_ANGLE);
            mTextColor = a.getColor(R.styleable.CircularProgressView_textColor, DEFAULT_TEXT_COLOR);
            mTextSize = a.getDimension(R.styleable.CircularProgressView_textSize, DEFAULT_TEXT_SIZE);
            mArcColor = a.getColor(R.styleable.CircularProgressView_fgArcColor, DEFAULT_ARC_COLOR);
            mArcWidth = a.getDimension(R.styleable.CircularProgressView_fgArcWidth, DEFAULT_ARC_WIDTH);
            mBgArcColor = a.getColor(R.styleable.CircularProgressView_bgArcColor, DEFAULT_BG_ARC_COLOR);
            mBgArcWidth = a.getDimension(R.styleable.CircularProgressView_bgArcWidth, DEFAULT_BG_ARC_WIDTH);
        } finally {
            a.recycle();
        }
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

    private void initPaint() {
        // 文字Paint
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(mAntiAlias);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
        // 设置Typeface对象，即字体风格，包括粗体，斜体以及衬线体，非衬线体等
        //mValuePaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        //  圆环Paint
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(mAntiAlias);
        mArcPaint.setColor(mArcColor);
        mArcPaint.setStrokeWidth(mArcWidth);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeCap(Paint.Cap.ROUND);

        // 背景圆环Paint
        mBgArcPaint = new Paint();
        mBgArcPaint.setAntiAlias(mAntiAlias);
        mBgArcPaint.setColor(mBgArcColor);
        mBgArcPaint.setStrokeWidth(mBgArcWidth);
        mBgArcPaint.setStyle(Paint.Style.STROKE);
        mBgArcPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(
                measureSize(widthMeasureSpec, dipToPx(mContext, DEFAULT_SIZE)),
                measureSize(heightMeasureSpec, dipToPx(mContext, DEFAULT_SIZE))
        );
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //求圆弧和背景圆弧的最大宽度
        float maxArcWidth = Math.max(mArcWidth, mBgArcWidth);
        //求最小值作为实际值（因为圆弧应当贴合最窄的边界）
        int minSize = Math.min(
                w - getPaddingLeft() - getPaddingRight() - 2 * (int) maxArcWidth,
                h - getPaddingTop() - getPaddingBottom() - 2 * (int) maxArcWidth
        );
        //计算圆弧的圆心、半径信息
        mRadius = minSize / 2;
        mCenterPoint.x = w / 2;
        mCenterPoint.y = h / 2;
        //计算圆弧边界矩形
        mRectF.left = mCenterPoint.x - mRadius - maxArcWidth / 2;
        mRectF.top = mCenterPoint.y - mRadius - maxArcWidth / 2;
        mRectF.right = mCenterPoint.x + mRadius + maxArcWidth / 2;
        mRectF.bottom = mCenterPoint.y + mRadius + maxArcWidth / 2;
        //计算文字绘制时的y坐标
        mTextY = mCenterPoint.y + getBaselineOffsetFromY(mTextPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mShowText)
            drawText(canvas);
        if (mShowProgress)
            drawArc(canvas);
    }


    /**
     * 根据paint获取text字体高度的y方向的中点
     */
    private float getBaselineOffsetFromY(Paint paint) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        return (Math.abs(fontMetrics.ascent) - fontMetrics.descent) / 2;
    }

    /**
     * 绘制内容文字
     */
    private void drawText(Canvas canvas) {
        // 由于Paint已设置为居中绘制，故不需要把文字的x绘制坐标设为mCenterPoint.x - textWidth / 2
        canvas.drawText(mText, mCenterPoint.x, mTextY, mTextPaint);
    }

    private void drawArc(Canvas canvas) {
        // 从进度圆弧结束的地方开始重新绘制，优化性能
        canvas.save();
        //画背景圆弧
        canvas.drawArc(mRectF, mStartAngle, mSweepAngle, false, mBgArcPaint);
        //画圆弧
        canvas.drawArc(mRectF, mStartAngle, mSweepAngle * mProgress, false, mArcPaint);
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
        mAnimator.pause();
        mAnimator = ValueAnimator.ofFloat(start, end);
        mAnimator.setDuration(animTime);
        mAnimator.setInterpolator(new LinearInterpolator());//动画匀速
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mProgress = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mAnimator.start();
    }

    private int measureSize(int measureSpec, int defaultSize) {
        int result = defaultSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(result, specSize);
        }
        return result;
    }

    private int dipToPx(Context context, float dip) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5f * (dip >= 0 ? 1 : -1));
    }
}
