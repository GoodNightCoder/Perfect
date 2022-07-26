package com.cyberlight.perfect;

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

import com.cyberlight.perfect.data.EventRepository;
import com.cyberlight.perfect.data.FocusRecordRepository;
import com.cyberlight.perfect.data.PlanAndRecordRepository;
import com.cyberlight.perfect.data.SummaryRepository;
import com.cyberlight.perfect.receiver.EventReminderReceiver;

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
        CheckBox eventsCheckBox = view.findViewById(R.id.dialog_clear_events_cb);
        CheckBox focusRecordsCheckBox = view.findViewById(R.id.dialog_clear_focus_records_cb);
        CheckBox plansCheckBox = view.findViewById(R.id.dialog_clear_plans_cb);
        CheckBox summaryCheckBox = view.findViewById(R.id.dialog_clear_summary_cb);
        TextView confirmTv = view.findViewById(R.id.dialog_btn_bar_positive_tv);
        TextView cancelTv = view.findViewById(R.id.dialog_btn_bar_negative_tv);
        confirmTv.setText(R.string.dialog_btn_confirm);
        cancelTv.setText(R.string.dialog_btn_cancel);
        confirmTv.setOnClickListener(v -> {
            boolean eventsChecked = eventsCheckBox.isChecked();
            boolean focusRecordsChecked = focusRecordsCheckBox.isChecked();
            boolean plansChecked = plansCheckBox.isChecked();
            boolean summaryChecked = summaryCheckBox.isChecked();
            if (!eventsChecked && !focusRecordsChecked
                    && !plansChecked && !summaryChecked) {
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
                        getString(R.string.dialog_btn_cancel));
                confirmDialogFragment.show(fragmentManager, ConfirmDialogFragment.TAG);
            }
        });
        cancelTv.setOnClickListener(v -> dismiss());
        // 监听确认对话框的返回结果
        fragmentManager.setFragmentResultListener(CLEAR_DATA_REQUEST_KEY,
                this, (requestKey, result) -> {
                    if (result.getInt(ConfirmDialogFragment.CONFIRM_WHICH_KEY) ==
                            ConfirmDialogFragment.CONFIRM_POSITIVE) {
                        if (eventsCheckBox.isChecked()) {
                            // 删除所有事件
                            EventRepository eventRepository = new EventRepository(context);
                            eventRepository.deleteAllEvents();
                            // 取消已有事件提醒
                            EventReminderReceiver.cancelReminder(context);
                        }
                        if (focusRecordsCheckBox.isChecked()) {
                            FocusRecordRepository focusRecordRepository = new FocusRecordRepository(context);
                            focusRecordRepository.deleteAllFocusRecords();
                        }
                        if (plansCheckBox.isChecked()) {
                            PlanAndRecordRepository planAndRecordRepository = new PlanAndRecordRepository(context);
                            planAndRecordRepository.deleteAllPlans();
                            planAndRecordRepository.deleteAllPlanRecords();
                        }
                        if (summaryCheckBox.isChecked()) {
                            SummaryRepository summaryRepository = new SummaryRepository(context);
                            summaryRepository.deleteAllSummaries();
                        }
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
