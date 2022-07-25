package com.cyberlight.perfect.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.cyberlight.perfect.R;
import com.cyberlight.perfect.data.SpecificEvent;

/**
 * 携带一个具体事件的按钮，摆放在时刻表布局中
 */
@SuppressLint("ViewConstructor")
public class SpecEventButton extends androidx.appcompat.widget.AppCompatButton {

    private final SpecificEvent mSpecificEvent;

    public SpecEventButton(@NonNull Context context, SpecificEvent specificEvent) {
        super(context);
        mSpecificEvent = specificEvent;
        setText(mSpecificEvent.event.title);
        TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.textColorPrimary});
        try {
            setTextColor(a.getColor(0, Color.RED));
        } finally {
            a.recycle();
        }
        setAllCaps(false);
        setPadding(10, 4, 0, 0);
        setGravity(Gravity.NO_GRAVITY);//文字左上角显示
        setBackground(AppCompatResources.getDrawable(context,
                R.drawable.bg_unfinished_event_btn));//设置按钮自定义样式
    }

    public SpecificEvent getSpecEvent() {
        return mSpecificEvent;
    }
}
