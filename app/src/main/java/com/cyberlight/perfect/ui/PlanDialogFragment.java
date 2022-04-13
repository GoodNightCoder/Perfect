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

    private TextView mTargetTv;

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
        mTargetTv = view.findViewById(R.id.plan_target_stepper_value_tv);
        mTargetTv.setText(String.valueOf(mTarget));
        mTargetTv.setOnClickListener(v -> {
            PlanTargetPickerDialogFragment planTargetPickerDialogFragment =
                    PlanTargetPickerDialogFragment.newInstance(mTarget);
            planTargetPickerDialogFragment.show(fragmentManager,
                    PlanTargetPickerDialogFragment.TAG);
        });
        ImageView stepperPlusImg = view.findViewById(R.id.plan_stepper_plus_img);
        stepperPlusImg.setOnClickListener(v -> {
            if (mTarget < 99) {
                mTarget++;
                mTargetTv.setText(String.valueOf(mTarget));
            }
        });
        ImageView stepperMinusImg = view.findViewById(R.id.plan_stepper_minus_img);
        stepperMinusImg.setOnClickListener(v -> {
            if (mTarget > 1) {
                mTarget--;
                mTargetTv.setText(String.valueOf(mTarget));
            }
        });
        fragmentManager.setFragmentResultListener(PlanTargetPickerDialogFragment.PT_REQUEST_KEY,
                this, (requestKey, result) -> {
                    mTarget = result.getInt(PlanTargetPickerDialogFragment.PT_NUM_KEY);
                    mTargetTv.setText(String.valueOf(mTarget));
                });
        ImageView cancelImg = view.findViewById(R.id.plan_cancel_img);
        cancelImg.setOnClickListener(v -> dismiss());
        ImageView confirmImg = view.findViewById(R.id.plan_confirm_img);
        confirmImg.setOnClickListener(v -> {
            EditText contentEt = view.findViewById(R.id.plan_content_et);
            String planContent = contentEt.getText().toString();
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
