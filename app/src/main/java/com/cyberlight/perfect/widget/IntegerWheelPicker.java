package com.cyberlight.perfect.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.cyberlight.perfect.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class IntegerWheelPicker extends WheelPicker<Integer> {

    private int mMaxValue;
    private int mMinValue;
    private int mDigits;
    private boolean mDigitsEnable;

    private OnValueSelectedListener mOnValueSelectedListener;
    private List<Integer> mDataList;

    public IntegerWheelPicker(Context context) {
        this(context, null);
    }

    public IntegerWheelPicker(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IntegerWheelPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.DefaultWheelPicker);
    }

    public IntegerWheelPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttrs(context, attrs, defStyleAttr, defStyleRes);
        if (mDigitsEnable) {
            // 格式化数字的位数
            NumberFormat numberFormat = NumberFormat.getNumberInstance();
            numberFormat.setMinimumIntegerDigits(mDigits);
            numberFormat.setMaximumIntegerDigits(mDigits);
            // 取消格式中的逗号分组
            numberFormat.setGroupingUsed(false);
            setDataFormat(numberFormat);
        }
        updateMaxWidthText();
        updateDataList();
        setOnItemSelectedListener((item, position) -> {
            if (mOnValueSelectedListener != null) {
                mOnValueSelectedListener.onValueSelected(item);
            }
        });
    }

    private void initAttrs(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.IntegerWheelPicker, defStyleAttr, defStyleRes);
        try {
            mMaxValue = a.getInt(R.styleable.IntegerWheelPicker_maxValue, 10);
            mMinValue = a.getInt(R.styleable.IntegerWheelPicker_minValue, -10);
            mDigits = a.getInt(R.styleable.IntegerWheelPicker_digits, 1);
            mDigitsEnable = a.getBoolean(R.styleable.IntegerWheelPicker_digitsEnable, false);
        } finally {
            a.recycle();
        }
    }

    public void setDigits(int digits) {
        if (digits != mDigits && digits > 0) {
            mDigits = digits;
            if (mDigitsEnable) {
                NumberFormat numberFormat = NumberFormat.getNumberInstance();
                numberFormat.setMinimumIntegerDigits(mDigits);
                numberFormat.setMaximumIntegerDigits(mDigits);
                numberFormat.setGroupingUsed(false);
                setDataFormat(numberFormat);
                updateMaxWidthText();
            }
        }
    }

    public void setDigitsEnable(boolean digitsEnable) {
        if (mDigitsEnable != digitsEnable) {
            mDigitsEnable = digitsEnable;
            updateMaxWidthText();
            if (digitsEnable) {
                NumberFormat numberFormat = NumberFormat.getNumberInstance();
                numberFormat.setMinimumIntegerDigits(mDigits);
                numberFormat.setMaximumIntegerDigits(mDigits);
                numberFormat.setGroupingUsed(false);
                setDataFormat(numberFormat);
            }
        }
    }

    public void setMaxValue(int maxValue) {
        if (maxValue != mMaxValue) {
            if (maxValue >= mMinValue) {
                mMaxValue = maxValue;
            } else {
                mMaxValue = maxValue;
                mMinValue = maxValue;
            }
            updateMaxWidthText();
            updateDataList();
        }
    }

    public void setMinValue(int minValue) {
        if (minValue != mMinValue) {
            if (minValue <= mMaxValue) {
                mMinValue = minValue;
            } else {
                mMinValue = minValue;
                mMaxValue = minValue;
            }
            updateMaxWidthText();
            updateDataList();
        }
    }

    private int getIntDigits(int num) {
        if (num == 0)
            return 1;
        num = Math.abs(num);
        int digits = 0;
        while (num != 0) {
            num = num / 10;
            digits++;
        }
        return digits;
    }

    private void updateDataList() {
        mDataList = new ArrayList<>();
        for (int i = mMinValue; i <= mMaxValue; i++) {
            mDataList.add(i);
        }
        setDataList(mDataList);
    }

    private void updateMaxWidthText() {
        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setGroupingUsed(false);
        if (mDigitsEnable) {
            // 依据指定位数计算宽度(考虑负号占位)
            numberFormat.setMinimumIntegerDigits(mMinValue < 0 ? mDigits + 1 : mDigits);
        } else {
            // 求最宽文字宽度(考虑负号占位)
            int maxDigits = Math.max(getIntDigits(mMaxValue),
                    mMinValue < 0 ? getIntDigits(mMinValue) + 1 : getIntDigits(mMinValue));
            numberFormat.setMinimumIntegerDigits(maxDigits);
        }
        setMaxWidthText(numberFormat.format(0));
    }

    public void setSelectedValue(int value, boolean smoothScroll) {
        if (value <= mMinValue)
            setPosition(0, smoothScroll);
        else if (value >= mMaxValue)
            setPosition(mDataList.size() - 1, smoothScroll);
        else
            setPosition(value - mMinValue, smoothScroll);
    }

    public void setOnValueSelectedListener(OnValueSelectedListener onValueSelectedListener) {
        mOnValueSelectedListener = onValueSelectedListener;
    }

    public interface OnValueSelectedListener {
        void onValueSelected(int value);
    }
}
