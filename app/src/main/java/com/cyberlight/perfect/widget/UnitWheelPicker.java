package com.cyberlight.perfect.widget;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;

import com.cyberlight.perfect.R;

import java.util.ArrayList;
import java.util.List;

public class UnitWheelPicker extends WheelPicker<String> {

    //单位，要求从0开始以1递增，否则监听器无法监听到正确对应的单位
    public static final int UNIT_DAY = 0;
    public static final int UNIT_WEEK = 1;

    private List<String> mDataList;
    private OnUnitSelectedListener mOnUnitSelectedListener;

    public UnitWheelPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMaxWidthText("WWWWW");
        updateDataList(0);
        setOnItemSelectedListener((item, position) -> {
            if (mOnUnitSelectedListener != null) {
                mOnUnitSelectedListener.onUnitSelected(position);
            }
        });
    }

    /**
     * 根据语言、选中数值更新数据集，以支持复数等语法规则
     *
     * @param value 与之绑定的数值选择器的选中值
     */
    public void updateDataList(int value) {
        Resources res = getResources();
        mDataList = new ArrayList<>();
        mDataList.add(res.getQuantityString(R.plurals.unit_day, value));
        mDataList.add(res.getQuantityString(R.plurals.unit_week, value));
        setDataList(mDataList);
    }

    public void setSelectedUnit(int unit, boolean scroll) {
        if (unit >= 0 && unit <= mDataList.size()) {
            setPosition(unit, scroll);
        }
    }

    public void setOnUnitSelectedListener(OnUnitSelectedListener onUnitSelectedListener) {
        mOnUnitSelectedListener = onUnitSelectedListener;
    }

    public interface OnUnitSelectedListener {
        void onUnitSelected(int unit);
    }
}
