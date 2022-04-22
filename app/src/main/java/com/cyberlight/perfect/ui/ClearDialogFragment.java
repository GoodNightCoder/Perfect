package com.cyberlight.perfect.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.cyberlight.perfect.R;
import com.cyberlight.perfect.receiver.EventReminderReceiver;
import com.cyberlight.perfect.util.DbContract;
import com.cyberlight.perfect.util.DbUtil;

public class ClearDialogFragment extends DialogFragment {
    public static final String TAG = "ClearDialogFragment";

    // 用于请求ConfirmDialog返回确认结果
    private static final String CLEAR_DATA_REQUEST_KEY = "clear_data_request_key";

    public ClearDialogFragment() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getContext();
        FragmentManager fragmentManager = getChildFragmentManager();
        // 设置布局
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.dialog_clear, null);
        CheckBox mEventsCheckBox = view.findViewById(R.id.dialog_clear_events_check_box);
        CheckBox mFocusRecordsCheckBox = view.findViewById(R.id.dialog_clear_focus_records_check_box);
        CheckBox mPlansCheckBox = view.findViewById(R.id.dialog_clear_plans_check_box);
        CheckBox mSummaryCheckBox = view.findViewById(R.id.dialog_clear_summary_check_box);
        TextView mConfirmTv = view.findViewById(R.id.dialog_clear_confirm_tv);
        TextView mCancelTv = view.findViewById(R.id.dialog_clear_cancel_tv);
        mConfirmTv.setOnClickListener(v -> {
            boolean eventsChecked = mEventsCheckBox.isChecked();
            boolean focusRecordsChecked = mFocusRecordsCheckBox.isChecked();
            boolean plansChecked = mPlansCheckBox.isChecked();
            boolean summaryChecked = mSummaryCheckBox.isChecked();
            if (!eventsChecked && !focusRecordsChecked &&
                    !plansChecked && !summaryChecked) {
                // 没有一项选中
                dismiss();
                return;
            }
            if (fragmentManager.findFragmentByTag(ConfirmDialogFragment.TAG) == null) {
                StringBuilder builder = new StringBuilder();
                if (eventsChecked)
                    builder.append("•").append(getString(R.string.clear_events_option)).append("\n");
                if (focusRecordsChecked)
                    builder.append("•").append(getString(R.string.clear_focus_records_option)).append("\n");
                if (plansChecked)
                    builder.append("•").append(getString(R.string.clear_plans_option)).append("\n");
                if (summaryChecked)
                    builder.append("•").append(getString(R.string.clear_summary_option));
                DialogFragment confirmDialogFragment = ConfirmDialogFragment.newInstance(
                        CLEAR_DATA_REQUEST_KEY,
                        getString(R.string.settings_clear_confirm_dialog_title),
                        builder.toString().trim(),
                        getString(R.string.dialog_btn_confirm),
                        getString(R.string.dialog_btn_cancel)
                );
                confirmDialogFragment.show(fragmentManager, ConfirmDialogFragment.TAG);
            }
        });
        mCancelTv.setOnClickListener(v -> dismiss());
        // 监听确认对话框的返回结果
        fragmentManager.setFragmentResultListener(CLEAR_DATA_REQUEST_KEY,
                this, (requestKey, result) -> {
                    if (result.getInt(ConfirmDialogFragment.CONFIRM_WHICH_KEY) ==
                            ConfirmDialogFragment.CONFIRM_POSITIVE) {
                        if (mEventsCheckBox.isChecked()) {
                            DbUtil.truncateTable(context, DbContract.EventsTable.TABLE_NAME);
                            DbUtil.truncateTable(context, DbContract.EventRecordsTable.TABLE_NAME);
                            // 取消已有事件提醒
                            EventReminderReceiver.cancelReminder(context);
                        }
                        if (mFocusRecordsCheckBox.isChecked())
                            DbUtil.truncateTable(context, DbContract.FocusRecordsTable.TABLE_NAME);
                        if (mPlansCheckBox.isChecked()) {
                            DbUtil.truncateTable(context, DbContract.PlansTable.TABLE_NAME);
                            DbUtil.truncateTable(context, DbContract.PlanRecordsTable.TABLE_NAME);
                        }
                        if (mSummaryCheckBox.isChecked())
                            DbUtil.truncateTable(context, DbContract.SummaryTable.TABLE_NAME);
                        dismiss();
                    }
                });
        // 设置对话框
        Dialog dialog = new Dialog(context, R.style.FadeAnimDialog);
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = getResources().getDisplayMetrics().widthPixels / 8 * 7; // 宽度
            window.setAttributes(lp);
        }
        return dialog;
    }

}
