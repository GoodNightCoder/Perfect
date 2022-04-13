package com.cyberlight.perfect.widget;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cyberlight.perfect.R;

public class CountableRadioButton extends View {
    private static final String TAG = "CountableRadioButton";

    //View的默认尺寸，当指定wrap_content的时候就会使用这个尺寸
    public static final int DEFAULT_SIZE = 24;

    //xml中定义属性的默认值
    public static final boolean DEFAULT_ANTI_ALIAS = true;
    public static final int DEFAULT_TEXT_COLOR = Color.GRAY;
    public static final int DEFAULT_TEXT_SIZE = 40;
    public static final int DEFAULT_CIRCLE_COLOR = Color.GRAY;
    public static final int DEFAULT_ARC_WIDTH = 3;
    public static final int DEFAULT_CIRCLE_BG_COLOR = Color.GREEN;

    //xml中定义的属性
    private boolean mAntiAlias;
    private int mTextColor;
    private float mTextSize;
    private int mHollowCircleColor;
    private float mHollowCircleArcWidth;
    private int mSolidCircleColor;

    //根据属性来计算的各个数据
    private Point mCenterPoint;//圆心坐标
    private float mRadius;//圆弧半径
    private float mTextY;//文字绘制时的y坐标
    private RectF mRectF;//圆弧矩形边界
    private TextPaint mTextPaint;
    private Paint mPaint;
    private Paint mHollowCirclePaint;
    private Paint mSolidCirclePaint;

    private OnCountChangedListener mOnCountChangedListener;
    private Context mContext;
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
        //初始化
        mContext = context;
        mRectF = new RectF();
        mCenterPoint = new Point();
        initAttrs(context, attrs, defStyleAttr, defStyleRes);
        initPaint();
        setOnClickListener(v -> increaseCount());
    }

    public void initCountAndMaxCount(int count, int maxCount) {
        mCount = count;
        mMaxCount = maxCount;
        invalidate();
    }

    private void initAttrs(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CountableRadioButton, defStyleAttr, defStyleRes);
        try {
            mAntiAlias = typedArray.getBoolean(R.styleable.CountableRadioButton_antiAlias, DEFAULT_ANTI_ALIAS);
            mTextColor = typedArray.getColor(R.styleable.CountableRadioButton_textColor, DEFAULT_TEXT_COLOR);
            mTextSize = typedArray.getDimension(R.styleable.CountableRadioButton_textSize, DEFAULT_TEXT_SIZE);
            mHollowCircleColor = typedArray.getColor(R.styleable.CountableRadioButton_hollowCircleColor, DEFAULT_CIRCLE_COLOR);
            mHollowCircleArcWidth = typedArray.getDimension(R.styleable.CountableRadioButton_hollowCircleArcWidth, DEFAULT_ARC_WIDTH);
            mSolidCircleColor = typedArray.getColor(R.styleable.CountableRadioButton_solidCircleColor, DEFAULT_CIRCLE_BG_COLOR);
        } finally {
            typedArray.recycle();
        }
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(mAntiAlias);
        // 文字Paint
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(mAntiAlias);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        //  圆环Paint
        mHollowCirclePaint = new Paint();
        mHollowCirclePaint.setAntiAlias(mAntiAlias);
        mHollowCirclePaint.setColor(mHollowCircleColor);
        mHollowCirclePaint.setStrokeWidth(mHollowCircleArcWidth);
        mHollowCirclePaint.setStyle(Paint.Style.STROKE);
        // 背景圆Paint
        mSolidCirclePaint = new Paint();
        mSolidCirclePaint.setAntiAlias(mAntiAlias);
        mSolidCirclePaint.setColor(mSolidCircleColor);
        mSolidCirclePaint.setStyle(Paint.Style.FILL);
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
        float maxArcWidth = mHollowCircleArcWidth;
        //求最小值作为实际值（因为圆弧应当贴合最窄的边界）
        int minSize = Math.min(
                w - getPaddingLeft() - getPaddingRight() - 2 * (int) maxArcWidth,
                h - getPaddingTop() - getPaddingBottom() - 2 * (int) maxArcWidth
        );
        //计算圆弧的圆心、半径信息
        mRadius = minSize / 2f;
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

    private void increaseCount() {
        mCount = (mCount + 1) % (mMaxCount + 1);
        invalidate();
        if (mOnCountChangedListener != null)
            mOnCountChangedListener.onCountChanged(mCount);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mCount == mMaxCount) {
            //画圆
            canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mRadius - 8, mSolidCirclePaint);
        } else if (mCount != 0) {
            // 由于Paint已设置为居中绘制，故不需要把文字的x绘制坐标设为mCenterPoint.x - textWidth / 2
            canvas.drawText(String.valueOf(mCount), mCenterPoint.x, mTextY, mTextPaint);
        }
        //画圆环
        canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mRadius, mHollowCirclePaint);
    }

    /**
     * 根据paint获取text字体高度的y方向的中点
     */
    private float getBaselineOffsetFromY(Paint paint) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        return (Math.abs(fontMetrics.ascent) - fontMetrics.descent) / 2;
    }


    private int measureSize(int measureSpec, int defaultSize) {
        int result = defaultSize;
        int specMode = View.MeasureSpec.getMode(measureSpec);
        int specSize = View.MeasureSpec.getSize(measureSpec);
        if (specMode == View.MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == View.MeasureSpec.AT_MOST) {
            result = Math.min(result, specSize);
        }
        return result;
    }

    private int dipToPx(@NonNull Context context, float dip) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5f * (dip >= 0 ? 1 : -1));
    }

    public void setOnCountListener(OnCountChangedListener onCountChangedListener) {
        mOnCountChangedListener = onCountChangedListener;
    }

    public interface OnCountChangedListener {
        void onCountChanged(int count);
    }
}
