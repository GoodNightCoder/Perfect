package com.cyberlight.perfect;

import android.annotation.SuppressLint;
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

import com.cyberlight.perfect.widget.IntegerWheelPicker;
import com.cyberlight.perfect.widget.UnitWheelPicker;

import java.util.Locale;

public class TimeOfUnitPickerDialogFragment extends DialogFragment {
    public static final String TAG = "TimeOfUnitPickerDialogFragment";

    private static final String TU_REQUEST_KEY = "tu_request_key";
    public static final String TU_VALUE_KEY = "tu_value_key";
    public static final String TU_UNIT_KEY = "tu_unit_key";

    private String mRequestKey;
    private int mSelectedValue;
    private int mSelectedUnit;

    // 用于单位选择器判断语言是否需要切换单复数
    private final String mLanguage;
    private final String mZhLanguage;

    public TimeOfUnitPickerDialogFragment() {
        mLanguage = Locale.getDefault().getLanguage();
        mZhLanguage = new Locale("zh").getLanguage();
    }

    public static TimeOfUnitPickerDialogFragment newInstance(String requestKey,
                                                             int initValue,
                                                             int initUnit) {
        TimeOfUnitPickerDialogFragment fragment = new TimeOfUnitPickerDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TU_REQUEST_KEY, requestKey);
        bundle.putInt(TU_VALUE_KEY, initValue);
        bundle.putInt(TU_UNIT_KEY, initUnit);
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mRequestKey = bundle.getString(TU_REQUEST_KEY);
            mSelectedValue = bundle.getInt(TU_VALUE_KEY);
            mSelectedUnit = bundle.getInt(TU_UNIT_KEY);
        }
        if (savedInstanceState != null) {
            mRequestKey = savedInstanceState.getString(TU_REQUEST_KEY);
            mSelectedValue = savedInstanceState.getInt(TU_VALUE_KEY);
            mSelectedUnit = savedInstanceState.getInt(TU_UNIT_KEY);
        }
        // 设置布局
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.dialog_tu_picker, null);
        IntegerWheelPicker mValueWp = view.findViewById(R.id.dialog_tu_value_wp);
        UnitWheelPicker mUnitWp = view.findViewById(R.id.dialog_tu_unit_wp);
        mValueWp.setSelectedValue(mSelectedValue, false);
        mUnitWp.setSelectedUnit(mSelectedUnit, false);
        mValueWp.setOnValueSelectedListener(value -> {
            // 只有在系统为英语并且原来选中1或即将选中1，才更新UnitPicker数据集
            if (!mLanguage.equals(mZhLanguage) && (value == 1 || mSelectedValue == 1)) {
                mUnitWp.updateDataList(value);
            }
            mSelectedValue = value;
        });
        mUnitWp.setOnUnitSelectedListener(unit -> mSelectedUnit = unit);
        // 把初始值告知UnitPicker，以保证复数等语法规则一开始就正确生效
        if (!mLanguage.equals(mZhLanguage)) {
            mUnitWp.updateDataList(mSelectedValue);
        }
        // 设置确认、取消按钮
        TextView confirmTv = view.findViewById(R.id.dialog_btn_bar_positive_tv);
        TextView cancelTv = view.findViewById(R.id.dialog_btn_bar_negative_tv);
        confirmTv.setText(R.string.dialog_btn_confirm);
        cancelTv.setText(R.string.dialog_btn_cancel);
        confirmTv.setOnClickListener(v -> {
            // 返回结果
            Bundle result = new Bundle();
            result.putInt(TU_VALUE_KEY, mSelectedValue);
            result.putInt(TU_UNIT_KEY, mSelectedUnit);
            getParentFragmentManager().setFragmentResult(mRequestKey, result);
            dismiss();
        });
        cancelTv.setOnClickListener(v -> dismiss());
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
        outState.putString(TU_REQUEST_KEY, mRequestKey);
        outState.putInt(TU_VALUE_KEY, mSelectedValue);
        outState.putInt(TU_UNIT_KEY, mSelectedUnit);
        super.onSaveInstanceState(outState);
    }

}