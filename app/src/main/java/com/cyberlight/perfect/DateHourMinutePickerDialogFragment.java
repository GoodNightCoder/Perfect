package com.cyberlight.perfect;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.cyberlight.perfect.util.DateTimeFormatUtil;
import com.cyberlight.perfect.widget.DateWheelPicker;
import com.cyberlight.perfect.widget.IntegerWheelPicker;

public class DateHourMinutePickerDialogFragment extends DialogFragment {
    // 用于findFragmentByTag
    public static final String TAG = "DateHourMinutePickerDialogFragment";

    // 用于FragmentResultListener传递结果、bundle初始化、
    // savedInstanceState状态恢复等
    private static final String DHM_REQUEST_KEY = "dhm_request_key";
    public static final String DHM_YEAR_KEY = "dhm_year_key";
    public static final String DHM_MONTH_KEY = "dhm_month_key";
    public static final String DHM_DAY_OF_MONTH_KEY = "dhm_day_of_month_key";
    public static final String DHM_HOUR_KEY = "dhm_hour_key";
    public static final String DHM_MINUTE_KEY = "dhm_minute_key";

    private String mRequestKey;
    // 当前选中的年月日时分
    private int mSelectedYear;
    private int mSelectedMonth;
    private int mSelectedDayOfMonth;
    private int mSelectedHour;
    private int mSelectedMinute;

    public DateHourMinutePickerDialogFragment() {
    }

    // 创建DateHourMinutePickerDialogFragment对象，并设置初始值
    public static DateHourMinutePickerDialogFragment newInstance(String requestKey,
                                                                 int year,
                                                                 int month,
                                                                 int dayOfMonth,
                                                                 int hour,
                                                                 int minute) {
        DateHourMinutePickerDialogFragment fragment = new DateHourMinutePickerDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DHM_REQUEST_KEY, requestKey);
        bundle.putInt(DHM_YEAR_KEY, year);
        bundle.putInt(DHM_MONTH_KEY, month);
        bundle.putInt(DHM_DAY_OF_MONTH_KEY, dayOfMonth);
        bundle.putInt(DHM_HOUR_KEY, hour);
        bundle.putInt(DHM_MINUTE_KEY, minute);
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = requireContext();
        // 获取并设置初始值
        Bundle bundle = getArguments();
        if (bundle != null) {
            mRequestKey = bundle.getString(DHM_REQUEST_KEY);
            mSelectedYear = bundle.getInt(DHM_YEAR_KEY);
            mSelectedMonth = bundle.getInt(DHM_MONTH_KEY);
            mSelectedDayOfMonth = bundle.getInt(DHM_DAY_OF_MONTH_KEY);
            mSelectedHour = bundle.getInt(DHM_HOUR_KEY);
            mSelectedMinute = bundle.getInt(DHM_MINUTE_KEY);
        }
        // 恢复对话框状态
        if (savedInstanceState != null) {
            mRequestKey = savedInstanceState.getString(DHM_REQUEST_KEY);
            mSelectedYear = savedInstanceState.getInt(DHM_YEAR_KEY);
            mSelectedMonth = savedInstanceState.getInt(DHM_MONTH_KEY);
            mSelectedDayOfMonth = savedInstanceState.getInt(DHM_DAY_OF_MONTH_KEY);
            mSelectedHour = savedInstanceState.getInt(DHM_HOUR_KEY);
            mSelectedMinute = savedInstanceState.getInt(DHM_MINUTE_KEY);
        }
        // 设置对话框布局
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.dialog_dhm_picker, null);
        // 将初始日期赋给日期指示textView
        TextView indicatorTv = view.findViewById(R.id.dialog_dhm_indicator_tv);
        indicatorTv.setText(DateTimeFormatUtil.getReadableDateAndDayOfWeek(
                context, mSelectedYear, mSelectedMonth, mSelectedDayOfMonth));
        // 获取三个选择器
        DateWheelPicker dateWp = view.findViewById(R.id.dialog_dhm_date_wp);
        IntegerWheelPicker hourWp = view.findViewById(R.id.dialog_dhm_hour_wp);
        IntegerWheelPicker minuteWp = view.findViewById(R.id.dialog_dhm_minute_wp);
        // 初始化三个选择器的选中项
        dateWp.setSelectedDate(mSelectedYear, mSelectedMonth, mSelectedDayOfMonth, false);
        hourWp.setSelectedValue(mSelectedHour, false);
        minuteWp.setSelectedValue(mSelectedMinute, false);
        // 对三个选择器设置选中监听
        dateWp.setOnDateSelectedListener((year, month, dayOfMonth) -> {
            mSelectedYear = year;
            mSelectedMonth = month;
            mSelectedDayOfMonth = dayOfMonth;
            indicatorTv.setText(DateTimeFormatUtil.getReadableDateAndDayOfWeek(
                    context, mSelectedYear, mSelectedMonth, mSelectedDayOfMonth));
        });
        hourWp.setOnValueSelectedListener(value -> mSelectedHour = value);
        minuteWp.setOnValueSelectedListener(value -> mSelectedMinute = value);
        // 设置对话框的取消、确认按钮
        TextView confirmTv = view.findViewById(R.id.dialog_btn_bar_positive_tv);
        TextView cancelTv = view.findViewById(R.id.dialog_btn_bar_negative_tv);
        confirmTv.setText(R.string.dialog_btn_confirm);
        cancelTv.setText(R.string.dialog_btn_cancel);
        confirmTv.setOnClickListener(v -> {
            // 将对话框选择结果通过setFragmentResult返回给Activity
            Bundle result = new Bundle();
            result.putInt(DHM_YEAR_KEY, mSelectedYear);
            result.putInt(DHM_MONTH_KEY, mSelectedMonth);
            result.putInt(DHM_DAY_OF_MONTH_KEY, mSelectedDayOfMonth);
            result.putInt(DHM_HOUR_KEY, mSelectedHour);
            result.putInt(DHM_MINUTE_KEY, mSelectedMinute);
            // 注意此处要使用getParentFragmentManager()
            getParentFragmentManager().setFragmentResult(mRequestKey, result);
            dismiss();
        });
        cancelTv.setOnClickListener(v -> dismiss());
        // 设置对话框
        // R.style.SlideBottomAnimDialog是对话框进出动画
        Dialog dialog = new Dialog(context, R.style.SlideBottomAnimDialog);
        // 将设置好的布局应用到对话框
        dialog.setContentView(view);
        // 设置对话框样式
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.BOTTOM; // 贴近底部
            lp.y = 80;// 与底部的距离
            lp.width = getResources().getDisplayMetrics().widthPixels / 8 * 7; // 宽度
            window.setAttributes(lp);
        }
        return dialog;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // 保存对话框状态
        outState.putString(DHM_REQUEST_KEY, mRequestKey);
        outState.putInt(DHM_YEAR_KEY, mSelectedYear);
        outState.putInt(DHM_MONTH_KEY, mSelectedMonth);
        outState.putInt(DHM_DAY_OF_MONTH_KEY, mSelectedDayOfMonth);
        outState.putInt(DHM_HOUR_KEY, mSelectedHour);
        outState.putInt(DHM_MINUTE_KEY, mSelectedMinute);
        super.onSaveInstanceState(outState);
    }

}