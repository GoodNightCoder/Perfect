package com.cyberlight.perfect.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.cyberlight.perfect.util.DateTimeFormatUtil;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class DateWheelPicker extends WheelPicker<String> {

    private LocalDate mStartDate;
    private LocalDate mEndDate;
    private OnDateSelectedListener mOnDateSelectedListener;

    public DateWheelPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 默认的日期选择范围
        setStartAndEnd(2020, 1, 1,
                2024, 12, 31);
        setMaxWidthText("WWW 00");
        setOnItemSelectedListener((item, position) -> {
            if (mOnDateSelectedListener != null) {
                LocalDate mSelectedDate = mStartDate.plusDays(position);
                mOnDateSelectedListener.onDateSelected(mSelectedDate.getYear(),
                        mSelectedDate.getMonthValue(),
                        mSelectedDate.getDayOfMonth());
            }
        });
    }

    public void setStartAndEnd(int startYear, int startMonth, int startDayOfMonth,
                               int endYear, int endMonth, int endDayOfMonth) {
        mStartDate = LocalDate.of(startYear, startMonth, startDayOfMonth);
        mEndDate = LocalDate.of(endYear, endMonth, endDayOfMonth);
        updateDataList();
    }

    private void updateDataList() {
        List<String> mDataList = new ArrayList<>();
        if (mStartDate.equals(mEndDate) || mStartDate.isBefore(mEndDate)) {
            int i = 0;
            while (true) {
                final LocalDate date = mStartDate.plusDays(i);
                mDataList.add(DateTimeFormatUtil.getReadableMonthAndDayOfMonth(getContext(), date));
                if (date.equals(mEndDate)) {
                    break;
                }
                i++;
            }
        }
        setDataList(mDataList);
    }

    public void setSelectedDate(int year, int month, int dayOfMonth, boolean scroll) {
        LocalDate date = LocalDate.of(year, month, dayOfMonth);
        if (date.equals(mStartDate) || date.equals(mEndDate)
                || (date.isAfter(mStartDate) && date.isBefore(mEndDate))) {
            int pos = (int) ChronoUnit.DAYS.between(mStartDate, date);
            setPosition(pos, scroll);
        }
    }

    public void setOnDateSelectedListener(OnDateSelectedListener onDateSelectedListener) {
        mOnDateSelectedListener = onDateSelectedListener;
    }

    public interface OnDateSelectedListener {
        void onDateSelected(int year, int month, int dayOfMonth);
    }
}
