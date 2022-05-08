package com.cyberlight.perfect.ui;

import android.annotation.SuppressLint;
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
import com.cyberlight.perfect.model.Event;
import com.cyberlight.perfect.receiver.EventReminderReceiver;
import com.cyberlight.perfect.util.DateTimeFormatUtil;
import com.cyberlight.perfect.util.DbUtil;
import com.cyberlight.perfect.util.OnDataAddedListener;
import com.cyberlight.perfect.util.ToastUtil;
import com.cyberlight.perfect.widget.UnitWheelPicker;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class EventDialogFragment extends DialogFragment {
    public static final String TAG = "EventDialogFragment";

    // 用于状态恢复
    private static final String START_YEAR_KEY = "start_year_key";
    private static final String START_MONTH_KEY = "start_month_key";
    private static final String START_DAY_OF_MONTH_KEY = "start_day_of_month_key";
    private static final String START_HOUR_KEY = "start_hour_key";
    private static final String START_MINUTE_KEY = "start_minute_key";
    private static final String DURATION_KEY = "duration_key";
    private static final String INTERVAL_UNIT_KEY = "interval_unit_key";
    private static final String INTERVAL_VALUE_KEY = "interval_value_key";

    // 用于监听对话框返回结果
    private static final String PICK_DHM_REQUEST_KEY = "pick_dhm_request_key";
    private static final String PICK_HM_REQUEST_KEY = "pick_hm_request_key";
    private static final String PICK_TU_REQUEST_KEY = "pick_tu_request_key";

    private LocalDateTime mStartDateTime;
    private long mDuration;
    private int mIntervalValue;
    private int mIntervalUnit;

    private EditText mTitleContentEt;
    private TextView mStartContentTv;
    private TextView mDurationContentTv;
    private TextView mRepeatContentTv;

    public EventDialogFragment() {
    }

    // 事件添加成功监听器，通知主页更新事件集
    private OnDataAddedListener mOnDataAddedListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mOnDataAddedListener = (OnDataAddedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(requireActivity()
                    + " must implement OnDataAddedListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // 设置对话框布局
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.dialog_event, null);
        Context context = requireContext();
        FragmentManager fragmentManager = getChildFragmentManager();
        // 初始化数据
        if (savedInstanceState != null) {
            int startYear = savedInstanceState.getInt(START_YEAR_KEY);
            int startMonth = savedInstanceState.getInt(START_MONTH_KEY);
            int startDayOfMonth = savedInstanceState.getInt(START_DAY_OF_MONTH_KEY);
            int startHour = savedInstanceState.getInt(START_HOUR_KEY);
            int startMinute = savedInstanceState.getInt(START_MINUTE_KEY);
            mStartDateTime = LocalDateTime.of(
                    startYear, startMonth, startDayOfMonth, startHour, startMinute);
            mDuration = savedInstanceState.getLong(DURATION_KEY);
            mIntervalUnit = savedInstanceState.getInt(INTERVAL_UNIT_KEY);
            mIntervalValue = savedInstanceState.getInt(INTERVAL_VALUE_KEY);
        } else {
            // 首次start设置为当前时间，注意要把秒和纳秒置为0，保证只有年月日时分有意义
            mStartDateTime = LocalDateTime.now().withSecond(0).withNano(0);
            mDuration = 0;
            mIntervalValue = 1;
            mIntervalUnit = UnitWheelPicker.UNIT_DAY;
        }
        mTitleContentEt = view.findViewById(R.id.dialog_event_title_content_et);
        mStartContentTv = view.findViewById(R.id.dialog_event_start_content_tv);
        mStartContentTv.setText(DateTimeFormatUtil.getReadableDateHourMinute(context, mStartDateTime));
        mStartContentTv.setOnClickListener(v -> {
            int initYear = mStartDateTime.getYear();
            int initMonth = mStartDateTime.getMonthValue();
            int initDayOfMonth = mStartDateTime.getDayOfMonth();
            int initHour = mStartDateTime.getHour();
            int initMinute = mStartDateTime.getMinute();
            if (fragmentManager.findFragmentByTag(DateHourMinutePickerDialogFragment.TAG) == null) {
                DialogFragment dateHourMinutePickerDialogFragment =
                        DateHourMinutePickerDialogFragment.newInstance(
                                PICK_DHM_REQUEST_KEY, initYear, initMonth,
                                initDayOfMonth, initHour, initMinute);
                dateHourMinutePickerDialogFragment.show(fragmentManager,
                        DateHourMinutePickerDialogFragment.TAG);
            }
        });
        mDurationContentTv = view.findViewById(R.id.dialog_event_duration_content_tv);
        mDurationContentTv.setText(getDurationStr());
        mDurationContentTv.setOnClickListener(v -> {
            int min = (int) (mDuration / 60000);// 计算mDuration共有多少分钟
            int initHour = min / 60;
            int initMinute = min % 60;
            if (fragmentManager.findFragmentByTag(HourMinutePickerDialogFragment.TAG) == null) {
                DialogFragment hourMinutePickerDialogFragment =
                        HourMinutePickerDialogFragment.newInstance(
                                PICK_HM_REQUEST_KEY, initHour, initMinute);
                hourMinutePickerDialogFragment.show(fragmentManager,
                        HourMinutePickerDialogFragment.TAG);
            }
        });
        mRepeatContentTv = view.findViewById(R.id.dialog_event_repeat_content_tv);
        mRepeatContentTv.setText(getIntervalStr());
        mRepeatContentTv.setOnClickListener(v -> {
            if (fragmentManager.findFragmentByTag(TimeOfUnitPickerDialogFragment.TAG) == null) {
                DialogFragment timeOfUnitPickerDialogFragment =
                        TimeOfUnitPickerDialogFragment.newInstance(
                                PICK_TU_REQUEST_KEY, mIntervalValue, mIntervalUnit);
                timeOfUnitPickerDialogFragment.show(fragmentManager,
                        TimeOfUnitPickerDialogFragment.TAG);
            }
        });
        // 设置按钮栏
        TextView dialogTitleTv = view.findViewById(R.id.dialog_action_bar_title_tv);
        dialogTitleTv.setText(R.string.event_dialog_title);
        ImageView confirmIv = view.findViewById(R.id.dialog_action_bar_confirm_iv);
        confirmIv.setOnClickListener(v -> {
            String title = mTitleContentEt != null ? mTitleContentEt.getText().toString() : "";
            long start = mStartDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long interval = getInterval();
            // title不能为空
            if (title.equals("")) {
                ToastUtil.showToast(context,
                        R.string.event_incomplete_toast,
                        Toast.LENGTH_SHORT);
                return;
            }
            // start不允许小于0
            if (start < 0) {
                ToastUtil.showToast(context,
                        R.string.event_start_time_invalid_toast,
                        Toast.LENGTH_SHORT);
                return;
            }
            // duration必须大于等于5分钟
            if (mDuration < 300000) {
                ToastUtil.showToast(context,
                        R.string.event_duration_invalid_toast,
                        Toast.LENGTH_SHORT);
                return;
            }
            // interval必须大于等于duration
            if (interval < mDuration) {
                ToastUtil.showToast(context,
                        R.string.event_interval_less_than_duration_toast,
                        Toast.LENGTH_SHORT);
                return;
            }
            // 不能与已有事件冲突
            Event event = new Event(-1, "", start, mDuration, interval);
            List<Event> events = DbUtil.getEvents(context);
            for (int i = 0; i < events.size(); i++) {
                Event ev = events.get(i);
                if (ev.isTimeConflictWith(event)) {
                    ToastUtil.showToast(context,
                            R.string.event_time_conflict_toast,
                            Toast.LENGTH_SHORT);
                    return;
                }
            }
            // 添加事件，显示是否成功的消息，退出对话框
            if (DbUtil.addEvent(context, title, start, mDuration, interval)) {
                // 更新事件提醒任务
                EventReminderReceiver.activateReminder(context, true);
                ToastUtil.showToast(context,
                        R.string.event_success_toast,
                        Toast.LENGTH_SHORT);
                if (mOnDataAddedListener != null)
                    mOnDataAddedListener.onDataAdded(TAG);
            } else {
                ToastUtil.showToast(context,
                        R.string.event_fail_toast,
                        Toast.LENGTH_SHORT);
            }
            dismiss();
        });
        ImageView cancelIv = view.findViewById(R.id.dialog_action_bar_cancel_iv);
        cancelIv.setOnClickListener(v -> dismiss());
        // 设置FragmentResultListener获取选择结果
        fragmentManager.setFragmentResultListener(PICK_DHM_REQUEST_KEY, this,
                (requestKey, result) -> {
                    int year = result.getInt(
                            DateHourMinutePickerDialogFragment.DHM_YEAR_KEY);
                    int month = result.getInt(
                            DateHourMinutePickerDialogFragment.DHM_MONTH_KEY);
                    int dayOfMonth = result.getInt(
                            DateHourMinutePickerDialogFragment.DHM_DAY_OF_MONTH_KEY);
                    int hour = result.getInt(
                            DateHourMinutePickerDialogFragment.DHM_HOUR_KEY);
                    int minute = result.getInt(
                            DateHourMinutePickerDialogFragment.DHM_MINUTE_KEY);
                    mStartDateTime = LocalDateTime.of(year, month, dayOfMonth, hour, minute);
                    mStartContentTv.setText(DateTimeFormatUtil.getReadableDateHourMinute(
                            context, mStartDateTime));
                }
        );
        fragmentManager.setFragmentResultListener(
                PICK_HM_REQUEST_KEY, this, (requestKey, result) -> {
                    int hour = result.getInt(HourMinutePickerDialogFragment.HM_HOUR_KEY);
                    int minute = result.getInt(HourMinutePickerDialogFragment.HM_MINUTE_KEY);
                    mDuration = (hour * 60L + minute) * 60000;
                    mDurationContentTv.setText(getDurationStr());
                });
        fragmentManager.setFragmentResultListener(
                PICK_TU_REQUEST_KEY, this, (requestKey, result) -> {
                    mIntervalValue = result.getInt(TimeOfUnitPickerDialogFragment.TU_VALUE_KEY);
                    mIntervalUnit = result.getInt(TimeOfUnitPickerDialogFragment.TU_UNIT_KEY);
                    mRepeatContentTv.setText(getIntervalStr());
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
        outState.putInt(START_YEAR_KEY, mStartDateTime.getYear());
        outState.putInt(START_MONTH_KEY, mStartDateTime.getMonthValue());
        outState.putInt(START_DAY_OF_MONTH_KEY, mStartDateTime.getDayOfMonth());
        outState.putInt(START_HOUR_KEY, mStartDateTime.getHour());
        outState.putInt(START_MINUTE_KEY, mStartDateTime.getMinute());
        outState.putLong(DURATION_KEY, mDuration);
        outState.putInt(INTERVAL_UNIT_KEY, mIntervalUnit);
        outState.putInt(INTERVAL_VALUE_KEY, mIntervalValue);
        super.onSaveInstanceState(outState);
    }

    private String getDurationStr() {
        int min = (int) (mDuration / 60000);
        int hour = min / 60;
        int minute = min % 60;
        return getString(R.string.event_duration_pattern, hour, minute);
    }

    private String getIntervalStr() {
        if (mIntervalUnit == UnitWheelPicker.UNIT_WEEK)
            return getResources().getQuantityString(R.plurals.event_interval_week_format,
                    mIntervalValue, mIntervalValue);
        else
            return getResources().getQuantityString(R.plurals.event_interval_day_format,
                    mIntervalValue, mIntervalValue);
    }

    private long getInterval() {
        if (mIntervalUnit == UnitWheelPicker.UNIT_WEEK) {
            // 604800000L是一周的总毫秒数
            return 604800000L * mIntervalValue;
        } else {
            // 86400000L是一天的总毫秒数
            return 86400000L * mIntervalValue;
        }
    }
}
