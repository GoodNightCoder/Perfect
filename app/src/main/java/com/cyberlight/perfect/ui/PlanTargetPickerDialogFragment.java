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

public class PlanTargetPickerDialogFragment extends DialogFragment {
    public static final String TAG = "PlanTargetPickerDialogFragment";
    public static final String PT_REQUEST_KEY = "pt_request_key";

    public static final String PT_TARGET_KEY = "pt_target_key";

    private int mSelectedTarget;

    public PlanTargetPickerDialogFragment() {
    }

    public static PlanTargetPickerDialogFragment newInstance(int target) {
        PlanTargetPickerDialogFragment fragment = new PlanTargetPickerDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PT_TARGET_KEY, target);
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mSelectedTarget = bundle.getInt(PT_TARGET_KEY);
        }
        if (savedInstanceState != null) {
            mSelectedTarget = savedInstanceState.getInt(PT_TARGET_KEY);
        }
        // 设置布局
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_pt_picker, null);
        // 初始化两个选择器
        IntegerWheelPicker mTargetWp = view.findViewById(R.id.dialog_pt_target_wp);
        mTargetWp.setSelectedValue(mSelectedTarget, false);
        mTargetWp.setOnValueSelectedListener(value -> mSelectedTarget = value);
        // 设置取消和确认按钮
        TextView mCancelTv = view.findViewById(R.id.dialog_pt_cancel_tv);
        TextView mConfirmTv = view.findViewById(R.id.dialog_pt_confirm_tv);
        mCancelTv.setOnClickListener(v -> dismiss());
        mConfirmTv.setOnClickListener(v -> {
            //将对话框选择的时间返回给Activity
            Bundle result = new Bundle();
            result.putInt(PT_TARGET_KEY, mSelectedTarget);
            getParentFragmentManager().setFragmentResult(PT_REQUEST_KEY, result);
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
        outState.putInt(PT_TARGET_KEY, mSelectedTarget);
        super.onSaveInstanceState(outState);
    }

}