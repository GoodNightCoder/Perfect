package com.cyberlight.perfect.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.cyberlight.perfect.R;
import com.cyberlight.perfect.util.DbUtil;
import com.cyberlight.perfect.util.ToastUtil;

public class PlanDialogFragment extends DialogFragment {

    public static final String TAG = "PlanDialogFragment";

    private static final String TARGET_KEY = "target_key";

    private int mTarget;

    private TextView mStepperValueTv;

    public PlanDialogFragment() {
    }

    public static PlanDialogFragment newInstance() {
        return new PlanDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // 设置对话框布局
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_plan, null);

        Context context = getContext();
        // 获取fragmentManager准备用于对话框操作
        FragmentManager fragmentManager = getChildFragmentManager();
        // 初始化数据
        if (savedInstanceState != null) {
            mTarget = savedInstanceState.getInt(TARGET_KEY);
        } else {
            mTarget = 1;
        }
        mStepperValueTv = view.findViewById(R.id.dialog_plan_stepper_value_tv);
        mStepperValueTv.setText(String.valueOf(mTarget));
        mStepperValueTv.setOnClickListener(v -> {
            if (fragmentManager.findFragmentByTag(PlanTargetPickerDialogFragment.TAG) == null) {
                DialogFragment planTargetPickerDialogFragment =
                        PlanTargetPickerDialogFragment.newInstance(mTarget);
                planTargetPickerDialogFragment.show(fragmentManager,
                        PlanTargetPickerDialogFragment.TAG);
            }
        });
        ImageView mStepperPlusIv = view.findViewById(R.id.dialog_plan_stepper_plus_iv);
        mStepperPlusIv.setOnClickListener(v -> {
            if (mTarget < 99) {
                mTarget++;
                mStepperValueTv.setText(String.valueOf(mTarget));
            }
        });
        ImageView mStepperMinusIv = view.findViewById(R.id.dialog_plan_stepper_minus_iv);
        mStepperMinusIv.setOnClickListener(v -> {
            if (mTarget > 1) {
                mTarget--;
                mStepperValueTv.setText(String.valueOf(mTarget));
            }
        });
        fragmentManager.setFragmentResultListener(PlanTargetPickerDialogFragment.PT_REQUEST_KEY,
                this, (requestKey, result) -> {
                    mTarget = result.getInt(PlanTargetPickerDialogFragment.PT_TARGET_KEY);
                    mStepperValueTv.setText(String.valueOf(mTarget));
                });
        ImageView mCancelIv = view.findViewById(R.id.dialog_plan_cancel_iv);
        mCancelIv.setOnClickListener(v -> dismiss());
        ImageView mConfirmIv = view.findViewById(R.id.dialog_plan_confirm_iv);
        mConfirmIv.setOnClickListener(v -> {
            EditText mContentEt = view.findViewById(R.id.dialog_plan_content_et);
            String planContent = mContentEt.getText().toString();
            if (planContent.equals("")) {
                ToastUtil.showToast(
                        context,
                        getString(R.string.plan_incomplete_toast),
                        Toast.LENGTH_SHORT);
                return;
            }
            if (DbUtil.addPlan(context, planContent, mTarget)) {
                ToastUtil.showToast(
                        context,
                        getString(R.string.plan_success_toast),
                        Toast.LENGTH_SHORT);
            } else {
                ToastUtil.showToast(
                        context,
                        getString(R.string.plan_fail_toast),
                        Toast.LENGTH_SHORT);
            }
            dismiss();
        });
        // 设置对话框
        Dialog dialog = new Dialog(context, R.style.SlideBottomAnimDialog);
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.BOTTOM; // 靠近底部
            lp.width = WindowManager.LayoutParams.MATCH_PARENT; // 宽度
            window.setAttributes(lp);
        }
        return dialog;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        //保存对话框状态
        outState.putInt(TARGET_KEY, mTarget);
        super.onSaveInstanceState(outState);
    }
}
