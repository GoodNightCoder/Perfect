package com.cyberlight.perfect.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.cyberlight.perfect.R;
import com.cyberlight.perfect.widget.IntegerWheelPicker;

public class HourMinutePickerDialogFragment extends DialogFragment {
    public static final String TAG = "HourMinutePickerDialogFragment";
    public static final String HM_REQUEST_KEY = "hm_request_key";

    public static final String HM_HOUR_KEY = "hm_hour_key";
    public static final String HM_MINUTE_KEY = "hm_minute_key";

    private int mSelectedHour;
    private int mSelectedMinute;

    public HourMinutePickerDialogFragment() {
    }

    public static HourMinutePickerDialogFragment newInstance(int hour, int minute) {
        HourMinutePickerDialogFragment fragment = new HourMinutePickerDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(HM_HOUR_KEY, hour);
        bundle.putInt(HM_MINUTE_KEY, minute);
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mSelectedHour = bundle.getInt(HM_HOUR_KEY);
            mSelectedMinute = bundle.getInt(HM_MINUTE_KEY);
        }
        if (savedInstanceState != null) {
            mSelectedHour = savedInstanceState.getInt(HM_HOUR_KEY);
            mSelectedMinute = savedInstanceState.getInt(HM_MINUTE_KEY);
        }
        // 设置布局
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_hm_picker, null);
        // 初始化两个选择器
        IntegerWheelPicker mHourPicker = view.findViewById(R.id.hm_hour_picker);
        IntegerWheelPicker mMinutePicker = view.findViewById(R.id.hm_minute_picker);
        mHourPicker.setSelectedValue(mSelectedHour, false);
        mMinutePicker.setSelectedValue(mSelectedMinute, false);
        mHourPicker.setOnValueSelectedListener(value -> mSelectedHour = value);
        mMinutePicker.setOnValueSelectedListener(value -> mSelectedMinute = value);
        // 设置取消和确认按钮
        TextView mCancelBtnTv = view.findViewById(R.id.dialog_hm_picker_cancel_tv);
        TextView mConfirmBtnTv = view.findViewById(R.id.dialog_hm_picker_confirm_tv);
        mCancelBtnTv.setOnClickListener(v -> dismiss());
        mConfirmBtnTv.setOnClickListener(v -> {
            //将对话框选择的时间返回给Activity
            Bundle result = new Bundle();
            result.putInt(HM_HOUR_KEY, mSelectedHour);
            result.putInt(HM_MINUTE_KEY, mSelectedMinute);
            getParentFragmentManager().setFragmentResult(HM_REQUEST_KEY, result);
            dismiss();
        });
        // 设置对话框
        Dialog dialog = new Dialog(getContext(), R.style.SlideBottomAnimDialog);
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.BOTTOM; // 紧贴底部
            lp.y = 80;// 与底部距离
            lp.width = getResources().getDisplayMetrics().widthPixels / 8 * 7; // 宽度
            window.setAttributes(lp);
        }
        return dialog;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(HM_HOUR_KEY, mSelectedHour);
        outState.putInt(HM_MINUTE_KEY, mSelectedMinute);
        super.onSaveInstanceState(outState);
    }

}