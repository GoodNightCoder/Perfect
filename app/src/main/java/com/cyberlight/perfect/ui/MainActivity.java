package com.cyberlight.perfect.ui;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.cyberlight.perfect.R;
import com.cyberlight.perfect.adapter.FocusRecordRecyclerAdapter;
import com.cyberlight.perfect.adapter.PlanRecyclerAdapter;
import com.cyberlight.perfect.model.Event;
import com.cyberlight.perfect.model.FocusRecord;
import com.cyberlight.perfect.model.Plan;
import com.cyberlight.perfect.model.SpecPlan;
import com.cyberlight.perfect.model.Summary;
import com.cyberlight.perfect.receiver.EventReminderReceiver;
import com.cyberlight.perfect.service.BedtimeAlarmService;
import com.cyberlight.perfect.util.DateTimeFormatUtil;
import com.cyberlight.perfect.util.DbUtil;
import com.cyberlight.perfect.util.OnDataAddedListener;
import com.cyberlight.perfect.util.SettingManager;
import com.cyberlight.perfect.util.SharedPrefSettingManager;
import com.cyberlight.perfect.util.ToastUtil;
import com.cyberlight.perfect.widget.ScheduleLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@SuppressLint({"NotifyDataSetChanged", "UnspecifiedImmutableFlag"})
public class MainActivity extends AppCompatActivity implements OnDataAddedListener {

    // 用于监听日期选择对话框结果
    private static final String PICK_D_REQUEST_KEY = "pick_d_request_key";

    // 状态恢复用
    private static final String CUR_EPOCH_DAY_KEY = "cur_epoch_day_key";
    private static final String CUR_POSITION_KEY = "cur_position_key";
    private static final String CUR_SCROLL_Y_KEY = "cur_scroll_y_key";

    // ViewPager总页数
    public static final int PAGES_COUNT = 50;

    // 当前页
    private int mCurPosition;
    // 当前页日期
    private LocalDate mCurDate;
    // 当前滚动到的Y位置
    private int mCurScrollY;
    // 各页的日期
    private final LocalDate[] mPageDates = new LocalDate[PAGES_COUNT];
    // 事件集
    private List<Event> mEvents;
    // 计划集
    private List<Plan> mPlans;

    private TextView mDateTv;
    private ViewPager2 mVp;
    private FloatingActionButton mTodayFab;
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            // 恢复数据
            long epochDay = savedInstanceState.getLong(CUR_EPOCH_DAY_KEY);
            mCurDate = LocalDate.ofEpochDay(epochDay);
            mCurPosition = savedInstanceState.getInt(CUR_POSITION_KEY);
            mCurScrollY = savedInstanceState.getInt(CUR_SCROLL_Y_KEY);
        } else {
            // 初始日期为今天
            mCurDate = LocalDate.now();
            // 初始页为中间页
            mCurPosition = PAGES_COUNT / 2;
            mCurScrollY = 0;
        }
        // 初始化各页日期
        updatePageDates(mCurPosition, mCurDate);

        // 初始化控件
        mDateTv = findViewById(R.id.main_date_tv);
        mVp = findViewById(R.id.main_vp);
        mTodayFab = findViewById(R.id.main_today_fab);
        ImageView focusIv = findViewById(R.id.main_focus_iv);
        ImageView moreIv = findViewById(R.id.main_more_iv);
        FragmentManager fragmentManager = getSupportFragmentManager();
        // 设置定位到今天的fab点击监听
        mTodayFab.setOnClickListener(v -> {
            mTodayFab.setVisibility(View.INVISIBLE);
            mCurDate = LocalDate.now();
            onDateChanged();
            updatePageDates(mCurPosition, mCurDate);
            mPagerAdapter.notifyDataSetChanged();
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
        // 设置ViewPager
        mPagerAdapter = new PagerAdapter();
        mVp.setAdapter(mPagerAdapter);
        mVp.setCurrentItem(mCurPosition, false);
        mVp.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mCurPosition = position;
                // 复制一份pageDates[position]对象给curDate
                mCurDate = LocalDate.ofEpochDay(mPageDates[position].toEpochDay());
                onDateChanged();
                if (position == 0) {
                    mVp.setUserInputEnabled(false);
                    updatePageDates(PAGES_COUNT - 2, mCurDate);
                    // 此处无需pagerAdapter.notifyDataSetChanged();
                    // 因为从第0页跳转到倒数第二页时，倒数第二页是还没有加载的
                    // 所以跳转后倒数第二页加载的就已经是新数据了，不必通知数据有更新
                } else if (position == PAGES_COUNT - 1) {
                    mVp.setUserInputEnabled(false);
                    updatePageDates(1, mCurDate);
                    // 理由同上，无需pagerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    if (mCurPosition == 0) {
                        mVp.setCurrentItem(PAGES_COUNT - 2, false);
                        mVp.setUserInputEnabled(true);
                    } else if (mCurPosition == PAGES_COUNT - 1) {
                        mVp.setCurrentItem(1, false);
                        mVp.setUserInputEnabled(true);
                    }
                }
            }
        });
        // 监听日期选择对话框结果
        fragmentManager.setFragmentResultListener(PICK_D_REQUEST_KEY,
                MainActivity.this, (requestKey, result) -> {
                    int year = result.getInt(DatePickerFragment.D_YEAR_KEY);
                    int month = result.getInt(DatePickerFragment.D_MONTH_KEY);
                    int dayOfMonth = result.getInt(DatePickerFragment.D_DAY_OF_MONTH_KEY);
                    mCurDate = LocalDate.of(year, month, dayOfMonth);
                    onDateChanged();
                    updatePageDates(mCurPosition, mCurDate);
                    mPagerAdapter.notifyDataSetChanged();
                });
        onDateChanged();
        // 检查事件提醒是否启动
        EventReminderReceiver.activateReminder(this, false);
        // 检查闹钟是否启动
        SettingManager settingManager = SharedPrefSettingManager.getInstance(this);
        boolean manageBedtime = settingManager.getManageBedtime();
        if (manageBedtime) {
            BedtimeAlarmService.activateAlarm(this, false);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        // 保证数据能及时更新
        mEvents = DbUtil.getEvents(this);
        mPlans = DbUtil.getPlans(this);
        mPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(CUR_EPOCH_DAY_KEY, mCurDate.toEpochDay());
        savedInstanceState.putInt(CUR_POSITION_KEY, mCurPosition);
        savedInstanceState.putInt(CUR_SCROLL_Y_KEY, mCurScrollY);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * 当当前页日期改变时调用此方法
     */
    private void onDateChanged() {
        mDateTv.setText(DateTimeFormatUtil.getReadableDate(this, mCurDate));
        LocalDate today = LocalDate.now();
        boolean isToday = today.equals(mCurDate);
        if (mTodayFab.getVisibility() == View.VISIBLE && isToday) {
            mTodayFab.setVisibility(View.INVISIBLE);
        }
        if (mTodayFab.getVisibility() == View.INVISIBLE && !isToday) {
            mTodayFab.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 以baseDate更新basePosition对应页的日期，并以该页日期为基准更新整个dates列表
     *
     * @param basePosition 基准页序号
     * @param baseDate     基准页日期
     */
    private void updatePageDates(int basePosition, LocalDate baseDate) {
        mPageDates[basePosition] = LocalDate.ofEpochDay(baseDate.toEpochDay());
        for (int i = basePosition - 1; i >= 0; i--) {
            mPageDates[i] = baseDate.minusDays(basePosition - i);
        }
        for (int i = basePosition + 1; i < mPageDates.length; i++) {
            mPageDates[i] = baseDate.plusDays(i - basePosition);
        }
    }

    @Override
    public void onDataAdded(String tag) {
        // 保证数据在添加事件、计划、总结对话框关闭后能及时更新，
        // 并且对事件、计划添加之后还需更新事件集、计划集
        if (tag.equals(EventDialogFragment.TAG)) {
            mEvents = DbUtil.getEvents(this);
        } else if (tag.equals(PlanDialogFragment.TAG)) {
            mPlans = DbUtil.getPlans(this);
        }
        mPagerAdapter.notifyDataSetChanged();
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

    private class PagerAdapter extends RecyclerView.Adapter<PagerAdapter.PageViewHolder> {

        protected class PageViewHolder extends RecyclerView.ViewHolder {

            private boolean hasSummarized = false;
            private LocalDate date;
            private final List<FocusRecord> focusRecords = new ArrayList<>();
            private final List<SpecPlan> specPlans = new ArrayList<>();
            private final FocusRecordRecyclerAdapter focusRecordRecyclerAdapter;
            private final PlanRecyclerAdapter planRecyclerAdapter;

            private final ScheduleLayout scheduleLayout;
            private final RelativeLayout summaryContentLayout;
            private final ImageView thumbUpIv1;
            private final ImageView thumbUpIv2;
            private final ImageView thumbUpIv3;
            private final ImageView thumbUpIv4;
            private final ImageView thumbUpIv5;
            private final TextView reviewTv;
            private final TextView memoTv;
            private final TextView noSummaryTv;
            private final ScrollView sv;

            public PageViewHolder(View v) {
                super(v);
                scheduleLayout = v.findViewById(R.id.main_schedule);
                summaryContentLayout = v.findViewById(R.id.main_summary_content_layout);
                thumbUpIv1 = v.findViewById(R.id.main_thumb_up_iv1);
                thumbUpIv2 = v.findViewById(R.id.main_thumb_up_iv2);
                thumbUpIv3 = v.findViewById(R.id.main_thumb_up_iv3);
                thumbUpIv4 = v.findViewById(R.id.main_thumb_up_iv4);
                thumbUpIv5 = v.findViewById(R.id.main_thumb_up_iv5);
                reviewTv = v.findViewById(R.id.main_review_tv);
                memoTv = v.findViewById(R.id.main_memo_tv);
                noSummaryTv = v.findViewById(R.id.main_no_summary_tv);
                sv = v.findViewById(R.id.main_sv);
                ImageView addEventIv = v.findViewById(R.id.main_add_event_iv);
                ImageView addFocusRecordIv = v.findViewById(R.id.main_add_focus_record_iv);
                ImageView addPlanIv = v.findViewById(R.id.main_add_plan_iv);
                ImageView addSummaryIv = v.findViewById(R.id.main_add_summary_iv);
                RecyclerView focusRecordsRv = v.findViewById(R.id.main_focus_records_rv);
                RecyclerView plansRv = v.findViewById(R.id.main_plans_rv);
                FragmentManager fragmentManager = getSupportFragmentManager();
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
                    } else if (!date.equals(LocalDate.now())) {
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
                focusRecordRecyclerAdapter = new FocusRecordRecyclerAdapter(
                        MainActivity.this, focusRecords);
                focusRecordsRv.setAdapter(focusRecordRecyclerAdapter);
                // 初始化PlansRv，通过LayoutManager禁止其滚动
                RecyclerView.LayoutManager plansRvLayoutManager = new LinearLayoutManager(
                        MainActivity.this, LinearLayoutManager.VERTICAL, false) {
                    @Override
                    public boolean canScrollVertically() {
                        return false;
                    }
                };
                plansRv.setLayoutManager(plansRvLayoutManager);
                planRecyclerAdapter = new PlanRecyclerAdapter(MainActivity.this, specPlans);
                plansRv.setAdapter(planRecyclerAdapter);
                // scrollView监听
                sv.setOnScrollChangeListener((v15, scrollX, scrollY, oldScrollX, oldScrollY) ->
                        mCurScrollY = scrollY);
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

            private void refreshData() {
                refreshSchedule();
                refreshFocusRecords();
                refreshPlans();
                refreshSummary();
            }

            private void refreshSchedule() {
                scheduleLayout.setEvents(mEvents);
                scheduleLayout.setDate(date);
                scheduleLayout.refresh();
            }

            private void refreshFocusRecords() {
                focusRecords.clear();
                long start = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                long end = start + 86399999;
                List<FocusRecord> frs = DbUtil.getFocusRecordsDuring(
                        MainActivity.this, start, end);
                focusRecords.addAll(frs);
                focusRecordRecyclerAdapter.notifyDataSetChanged();
            }

            private void refreshPlans() {
                String dateStr = DateTimeFormatUtil.getNeatDate(date);
                specPlans.clear();
                for (Plan plan : mPlans) {
                    int completionCount = DbUtil.getPlanRecordCompletionCountByDate(
                            MainActivity.this, dateStr, plan.planId);
                    if (completionCount > -1) {
                        specPlans.add(new SpecPlan(plan, dateStr, completionCount));
                    } else {
                        // 无记录
                        specPlans.add(new SpecPlan(plan, dateStr, 0));
                    }
                }
                planRecyclerAdapter.notifyDataSetChanged();
            }

            private void refreshSummary() {
                Summary summary = DbUtil.getSummary(
                        MainActivity.this, DateTimeFormatUtil.getNeatDate(date));
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
            }
        }

        @NonNull
        @Override
        public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.vp_main, parent, false);
            return new PageViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
            holder.date = LocalDate.ofEpochDay(mPageDates[position].toEpochDay());
        }

        @Override
        public int getItemCount() {
            return PAGES_COUNT;
        }

        @Override
        public void onViewAttachedToWindow(@NonNull PageViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            // 页面可见时才刷新数据，是为了在跨天事件完成状态改变时，相邻页该事件的完成状态也能及时改变
            holder.refreshData();
            // scrollView绘制完后滚动到之前已滚到的位置
            holder.sv.post(() -> {
                holder.sv.scrollTo(0, mCurScrollY);
                mCurScrollY = holder.sv.getScrollY();
            });
        }

    }
}