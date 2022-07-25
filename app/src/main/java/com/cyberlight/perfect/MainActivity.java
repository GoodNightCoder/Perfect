package com.cyberlight.perfect;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberlight.perfect.adapter.FocusRecordAdapter;
import com.cyberlight.perfect.adapter.PlanAdapter;
import com.cyberlight.perfect.adapter.PlanClickCallback;
import com.cyberlight.perfect.data.FocusRecord;
import com.cyberlight.perfect.data.PlanWithRecord;
import com.cyberlight.perfect.receiver.EventReminderReceiver;
import com.cyberlight.perfect.service.BedtimeAlarmService;
import com.cyberlight.perfect.util.DateTimeFormatUtil;
import com.cyberlight.perfect.util.SettingManager;
import com.cyberlight.perfect.util.SharedPrefSettingManager;
import com.cyberlight.perfect.util.ToastUtil;
import com.cyberlight.perfect.viewmodels.ActivityMainViewModel;
import com.cyberlight.perfect.widget.ScheduleLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@SuppressLint({"NotifyDataSetChanged", "UnspecifiedImmutableFlag"})
public class MainActivity extends AppCompatActivity {
    // 用于监听日期选择对话框结果
    private static final String PICK_D_REQUEST_KEY = "pick_d_request_key";
    // 状态恢复用
    private static final String CUR_EPOCH_DAY_KEY = "cur_epoch_day_key";
    private static final String TAG = "MainActivity";
    // 当前页日期
    private LocalDate mCurDate;

    private final List<FocusRecord> mFocusRecords = new ArrayList<>();
    private final List<PlanWithRecord> mPlanWithRecords = new ArrayList<>();

    private boolean hasSummarized = false;

    private FocusRecordAdapter focusRecordAdapter;
    private PlanAdapter planAdapter;

    private ScheduleLayout scheduleLayout;
    private RelativeLayout summaryContentLayout;
    private ImageView thumbUpIv1;
    private ImageView thumbUpIv2;
    private ImageView thumbUpIv3;
    private ImageView thumbUpIv4;
    private ImageView thumbUpIv5;
    private TextView reviewTv;
    private TextView memoTv;
    private TextView noSummaryTv;

    private TextView mDateTv;
    private FloatingActionButton mTodayFab;
    private ActivityMainViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewModel = new ViewModelProvider(this).get(ActivityMainViewModel.class);

        scheduleLayout = findViewById(R.id.main_schedule);
        summaryContentLayout = findViewById(R.id.main_summary_content_layout);
        thumbUpIv1 = findViewById(R.id.main_thumb_up_iv1);
        thumbUpIv2 = findViewById(R.id.main_thumb_up_iv2);
        thumbUpIv3 = findViewById(R.id.main_thumb_up_iv3);
        thumbUpIv4 = findViewById(R.id.main_thumb_up_iv4);
        thumbUpIv5 = findViewById(R.id.main_thumb_up_iv5);
        reviewTv = findViewById(R.id.main_review_tv);
        memoTv = findViewById(R.id.main_memo_tv);
        noSummaryTv = findViewById(R.id.main_no_summary_tv);
        ImageView addEventIv = findViewById(R.id.main_add_event_iv);
        ImageView addFocusRecordIv = findViewById(R.id.main_add_focus_record_iv);
        ImageView addPlanIv = findViewById(R.id.main_add_plan_iv);
        ImageView addSummaryIv = findViewById(R.id.main_add_summary_iv);
        RecyclerView focusRecordsRv = findViewById(R.id.main_focus_records_rv);
        RecyclerView plansRv = findViewById(R.id.main_plans_rv);

        mDateTv = findViewById(R.id.main_date_tv);
        mTodayFab = findViewById(R.id.main_today_fab);
        ImageView focusIv = findViewById(R.id.main_focus_iv);
        ImageView moreIv = findViewById(R.id.main_more_iv);
        if (savedInstanceState != null) {
            // 恢复数据
            long epochDay = savedInstanceState.getLong(CUR_EPOCH_DAY_KEY);
            setDate(LocalDate.ofEpochDay(epochDay));
        } else {
            // 初始日期为今天
            setDate(LocalDate.now());
        }
        mViewModel.setDate(mCurDate);
        scheduleLayout.setDate(mCurDate);
        scheduleLayout.refresh();
        mViewModel.getEvents().observe(this, events -> {
            Log.d(TAG, "事件更新");
            scheduleLayout.setEvents(events);
            scheduleLayout.refresh();
        });
        mViewModel.getFocusRecords().observe(this, focusRecords -> {
            Log.d(TAG, "专注记录更新");
            mFocusRecords.clear();
            mFocusRecords.addAll(focusRecords);
            focusRecordAdapter.notifyDataSetChanged();
        });
        mViewModel.getPlanWithRecords().observe(this, planWithRecords -> {
            Log.d(TAG, "计划更新");
            mPlanWithRecords.clear();
            mPlanWithRecords.addAll(planWithRecords);
            planAdapter.notifyDataSetChanged();
        });
        mViewModel.getSummary().observe(this, summary -> {
            Log.d(TAG, "总结更新");
            if (summary != null) {
                hasSummarized = true;
                summaryContentLayout.setVisibility(View.VISIBLE);
                noSummaryTv.setVisibility(View.GONE);
                // 将Summary的内容应用到界面上
                setRating(summary.rating);
                reviewTv.setText(summary.review);
                memoTv.setText(summary.memo);
            } else {
                hasSummarized = false;
                summaryContentLayout.setVisibility(View.GONE);
                noSummaryTv.setVisibility(View.VISIBLE);
            }
        });


        addEventIv.setOnClickListener(v13 -> {
            if (fragmentManager.findFragmentByTag(EventDialogFragment.TAG) == null) {
                DialogFragment dialogFragment = new EventDialogFragment();
                dialogFragment.show(fragmentManager, EventDialogFragment.TAG);
            }
        });
        addFocusRecordIv.setOnClickListener(v14 -> {
            Intent intent = new Intent(MainActivity.this, FocusActivity.class);
            startActivity(intent);
        });
        addPlanIv.setOnClickListener(v12 -> {
            if (fragmentManager.findFragmentByTag(PlanDialogFragment.TAG) == null) {
                DialogFragment dialogFragment = new PlanDialogFragment();
                dialogFragment.show(fragmentManager, PlanDialogFragment.TAG);
            }
        });
        addSummaryIv.setOnClickListener(v1 -> {
            if (hasSummarized) {
                ToastUtil.showToast(MainActivity.this,
                        R.string.main_have_sum_toast,
                        Toast.LENGTH_SHORT);
            } else if (!mCurDate.equals(LocalDate.now())) {
                ToastUtil.showToast(MainActivity.this,
                        R.string.main_sum_not_today_toast,
                        Toast.LENGTH_SHORT);
            } else {
                if (fragmentManager.findFragmentByTag(SummaryDialogFragment.TAG) == null) {
                    DialogFragment dialogFragment = new SummaryDialogFragment();
                    dialogFragment.show(fragmentManager, SummaryDialogFragment.TAG);
                }
            }
        });
        // 初始化FocusRecordsRv，通过LayoutManager禁止其滚动
        RecyclerView.LayoutManager focusRecordsRvLayoutManager = new LinearLayoutManager(
                MainActivity.this, LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        focusRecordsRv.setLayoutManager(focusRecordsRvLayoutManager);
        focusRecordAdapter = new FocusRecordAdapter(MainActivity.this, mFocusRecords);
        focusRecordsRv.setAdapter(focusRecordAdapter);
        // 初始化PlansRv，通过LayoutManager禁止其滚动
        RecyclerView.LayoutManager plansRvLayoutManager = new LinearLayoutManager(
                MainActivity.this, LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        plansRv.setLayoutManager(plansRvLayoutManager);
        planAdapter = new PlanAdapter(mPlanWithRecords, planWithRecord -> mViewModel.doOnClickPlan(planWithRecord));
        plansRv.setAdapter(planAdapter);

        // 设置定位到今天的fab点击监听
        mTodayFab.setOnClickListener(v -> {
            mTodayFab.setVisibility(View.INVISIBLE);
            setDate(LocalDate.now());
        });
        focusIv.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FocusActivity.class);
            startActivity(intent);
        });
        moreIv.setOnClickListener(v -> {
            // TODO:实现菜单图标显示
            PopupMenu popup = new PopupMenu(MainActivity.this, v);
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.main_more_layout:
                        // TODO:修改主页布局
                        return true;
                    case R.id.main_more_settings:
                        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(intent);
                        return true;
                    default:
                        return false;
                }
            });
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.main_more_menu, popup.getMenu());
            popup.show();
        });
        mDateTv.setOnClickListener(v -> {
            int initYear = mCurDate.getYear();
            int initMonth = mCurDate.getMonthValue();
            int initDayOfMonth = mCurDate.getDayOfMonth();
            if (fragmentManager.findFragmentByTag(DatePickerFragment.TAG) == null) {
                DialogFragment dialogFragment = DatePickerFragment.newInstance(PICK_D_REQUEST_KEY,
                        initYear, initMonth, initDayOfMonth);
                dialogFragment.show(fragmentManager, DatePickerFragment.TAG);
            }
        });
        // 监听日期选择对话框结果
        fragmentManager.setFragmentResultListener(PICK_D_REQUEST_KEY,
                MainActivity.this, (requestKey, result) -> {
                    int year = result.getInt(DatePickerFragment.D_YEAR_KEY);
                    int month = result.getInt(DatePickerFragment.D_MONTH_KEY);
                    int dayOfMonth = result.getInt(DatePickerFragment.D_DAY_OF_MONTH_KEY);
                    setDate(LocalDate.of(year, month, dayOfMonth));
                });
        // 检查事件提醒是否启动
        EventReminderReceiver.activateReminder(this, false);
        // 检查闹钟是否启动
        SettingManager settingManager = SharedPrefSettingManager.getInstance(this);
        boolean manageBedtime = settingManager.getManageBedtime();
        if (manageBedtime) {
            BedtimeAlarmService.activateAlarm(this, false);
        }
    }

    private void setDate(LocalDate date) {
        mCurDate = date;
        mDateTv.setText(DateTimeFormatUtil.getReadableDate(this, mCurDate));
        LocalDate today = LocalDate.now();
        boolean isToday = today.equals(mCurDate);
        if (mTodayFab.getVisibility() == View.VISIBLE && isToday) {
            mTodayFab.setVisibility(View.INVISIBLE);
        }
        if (mTodayFab.getVisibility() == View.INVISIBLE && !isToday) {
            mTodayFab.setVisibility(View.VISIBLE);
        }
//        long start = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
//        long end = start + 86399999;
//        mViewModel.setFocusRecordsDuring(start, end);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(CUR_EPOCH_DAY_KEY, mCurDate.toEpochDay());
        super.onSaveInstanceState(savedInstanceState);
    }

    // 注意这个DatePickerFragment只能是public static，否则报错，原因待挖掘
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        public static final String TAG = "DatePickerFragment";

        private static final String D_REQUEST_KEY = "d_request_key";
        public static final String D_YEAR_KEY = "d_year_key";
        public static final String D_MONTH_KEY = "d_month_key";
        public static final String D_DAY_OF_MONTH_KEY = "d_day_of_month_key";

        private String mRequestKey;

        public DatePickerFragment() {
        }

        public static DatePickerFragment newInstance(String requestKey,
                                                     int year,
                                                     int month,
                                                     int dayOfMonth) {
            DatePickerFragment fragment = new DatePickerFragment();
            Bundle bundle = new Bundle();
            bundle.putString(D_REQUEST_KEY, requestKey);
            bundle.putInt(D_YEAR_KEY, year);
            bundle.putInt(D_MONTH_KEY, month - 1);
            bundle.putInt(D_DAY_OF_MONTH_KEY, dayOfMonth);
            fragment.setArguments(bundle);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle bundle = getArguments();
            int initYear = 2000;
            int initMonth = 0;
            int initDayOfMonth = 1;
            if (bundle != null) {
                mRequestKey = bundle.getString(D_REQUEST_KEY);
                initYear = bundle.getInt(D_YEAR_KEY);
                initMonth = bundle.getInt(D_MONTH_KEY);
                initDayOfMonth = bundle.getInt(D_DAY_OF_MONTH_KEY);
            }
            if (savedInstanceState != null) {
                mRequestKey = savedInstanceState.getString(D_REQUEST_KEY);
            }
            return new DatePickerDialog(getActivity(), this,
                    initYear, initMonth, initDayOfMonth);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            Bundle result = new Bundle();
            result.putInt(D_YEAR_KEY, year);
            result.putInt(D_MONTH_KEY, month + 1);
            result.putInt(D_DAY_OF_MONTH_KEY, dayOfMonth);
            getParentFragmentManager().setFragmentResult(mRequestKey, result);
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            outState.putString(D_REQUEST_KEY, mRequestKey);
            super.onSaveInstanceState(outState);
        }
    }

    private void setRating(int rating) {
        int purpleA50 = ContextCompat.getColor(MainActivity.this, R.color.purple_a50);
        int grayA50 = ContextCompat.getColor(MainActivity.this, R.color.gray_a50);
        final ImageView[] imageViews =
                {thumbUpIv1, thumbUpIv2, thumbUpIv3, thumbUpIv4, thumbUpIv5};
        for (int i = 0; i < imageViews.length; i++) {
            imageViews[i].setColorFilter(i < rating ? purpleA50 : grayA50);
        }
    }

}