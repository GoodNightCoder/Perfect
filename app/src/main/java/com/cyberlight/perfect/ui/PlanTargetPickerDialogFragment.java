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

    public static final String PT_NUM_KEY = "pt_num_key";

    private int mSelectedNum;

    public PlanTargetPickerDialogFragment() {
    }

    public static PlanTargetPickerDialogFragment newInstance(int num) {
        PlanTargetPickerDialogFragment fragment = new PlanTargetPickerDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PT_NUM_KEY, num);
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mSelectedNum = bundle.getInt(PT_NUM_KEY);
        }
        if (savedInstanceState != null) {
            mSelectedNum = savedInstanceState.getInt(PT_NUM_KEY);
        }
        // 设置布局
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_pt_picker, null);
        // 初始化两个选择器
        IntegerWheelPicker mTargetPicker = view.findViewById(R.id.pt_target_picker);
        mTargetPicker.setSelectedValue(mSelectedNum, false);
        mTargetPicker.setOnValueSelectedListener(value -> mSelectedNum = value);
        // 设置取消和确认按钮
        TextView mCancelBtnTv = view.findViewById(R.id.dialog_pt_picker_cancel_tv);
        TextView mConfirmBtnTv = view.findViewById(R.id.dialog_pt_picker_confirm_tv);
        mCancelBtnTv.setOnClickListener(v -> dismiss());
        mConfirmBtnTv.setOnClickListener(v -> {
            //将对话框选择的时间返回给Activity
            Bundle result = new Bundle();
            result.putInt(PT_NUM_KEY, mSelectedNum);
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
        outState.putInt(PT_NUM_KEY, mSelectedNum);
        super.onSaveInstanceState(outState);
    }

}