package com.cyberlight.perfect.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.cyberlight.perfect.R;
import com.cyberlight.perfect.model.SpecEvent;
import com.cyberlight.perfect.util.DbUtil;

/**
 * SpecEventButton保存一个事件
 */
@SuppressLint("ViewConstructor")
public class SpecEventButton extends androidx.appcompat.widget.AppCompatButton {

    private final Context mContext;
    public SpecEvent mSpecEvent;
    private boolean isFinished;

    public void toggleFinishState() {
        if (isFinished) {
            setBackground(AppCompatResources.getDrawable(mContext,
                    R.drawable.bg_unfinished_event_btn));
        } else {
            setBackground(AppCompatResources.getDrawable(mContext,
                    R.drawable.bg_finished_event_btn));
        }
        isFinished = !isFinished;
    }

    public SpecEventButton(@NonNull Context context, SpecEvent specEvent) {
        super(context);
        mContext = context;
        mSpecEvent = specEvent;
        setText(mSpecEvent.title);
        TypedArray a = mContext.obtainStyledAttributes(new int[]{android.R.attr.textColorPrimary});
        try {
            setTextColor(a.getColor(0, Color.RED));
        } finally {
            a.recycle();
        }
        setAllCaps(false);
        setPadding(10, 4, 0, 0);
        setGravity(Gravity.NO_GRAVITY);//文字左上角显示
        if (DbUtil.specEventIsFinished(mContext, mSpecEvent)) {
            isFinished = true;
            setBackground(AppCompatResources.getDrawable(mContext,
                    R.drawable.bg_finished_event_btn));//设置按钮自定义样式
        } else {
            isFinished = false;
            setBackground(AppCompatResources.getDrawable(mContext,
                    R.drawable.bg_unfinished_event_btn));//设置按钮自定义样式
        }
    }

}
