package com.cyberlight.perfect.ui;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.cyberlight.perfect.R;
import com.cyberlight.perfect.adapter.FocusRecordRecyclerAdapter;
import com.cyberlight.perfect.adapter.PlanRecyclerAdapter;
import com.cyberlight.perfect.model.FocusRecord;
import com.cyberlight.perfect.model.Plan;
import com.cyberlight.perfect.model.SpecPlan;
import com.cyberlight.perfect.model.Summary;
import com.cyberlight.perfect.receiver.EventReminderReceiver;
import com.cyberlight.perfect.util.DateTimeFormatUtil;
import com.cyberlight.perfect.util.DbUtil;
import com.cyberlight.perfect.util.ToastUtil;
import com.cyberlight.perfect.widget.ScheduleLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@SuppressLint({"NotifyDataSetChanged", "UnspecifiedImmutableFlag"})
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // 主页日期选择器
    private static final String D_REQUEST_KEY = "pick_date";
    private static final String D_YEAR_KEY = "d_year_key";
    private static final String D_MONTH_KEY = "d_month_key";
    private static final String D_DAY_OF_MONTH_KEY = "d_day_of_month_key";

    // 状态恢复用
    private static final String CUR_EPOCH_DAY_KEY = "cur_epoch_day_key";
    private static final String CUR_POSITION_KEY = "cur_position_key";
    private static final String CUR_SCROLL_Y_KEY = "cur_scroll_y_key";

    /**
     * 总页数
     */
    public static final int PAGES_COUNT = 50;

    /**
     * 当前页序号
     */
    private int curPosition;

    /**
     * 当前页日期
     */
    private LocalDate curDate;

    /**
     * 当前ViewPager滚动到的位置
     */
    private int curScrollY;

    /**
     * 各页的日期
     */
    private final LocalDate[] pageDates = new LocalDate[PAGES_COUNT];

    private TextView timeTv;
    private ViewPager2 viewPager;
    private FloatingActionButton fab;
    private PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            // 恢复数据
            long epochDay = savedInstanceState.getLong(CUR_EPOCH_DAY_KEY);
            curDate = LocalDate.ofEpochDay(epochDay);
            curPosition = savedInstanceState.getInt(CUR_POSITION_KEY);
            curScrollY = savedInstanceState.getInt(CUR_SCROLL_Y_KEY);
        } else {
            // 初始化数据
            curDate = LocalDate.now();
            curPosition = PAGES_COUNT / 2;// 设置初始页为中间页
            curScrollY = 0;
        }
        updatePageDates(curPosition, curDate);

        // 初始化控件
        timeTv = findViewById(R.id.main_date_tv);
        viewPager = findViewById(R.id.main_pager);
        fab = findViewById(R.id.main_fab);
        ImageView focusImg = findViewById(R.id.main_focus_img);
        ImageView settingsImg = findViewById(R.id.main_settings_img);
        // 设置fab
        fab.setOnClickListener(v -> {
            fab.setVisibility(View.INVISIBLE);
            curDate = LocalDate.now();
            onDateChanged();
            updatePageDates(curPosition, curDate);
            pagerAdapter.notifyDataSetChanged();
        });
        // 对专注模式图片添加点击监听
        focusImg.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FocusActivity.class);
            startActivity(intent);
        });
        // 对设置图片添加点击监听
        settingsImg.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        // 设置timeTv
        timeTv.setOnClickListener(v -> {
            int initYear = curDate.getYear();
            int initMonth = curDate.getMonthValue();
            int initDayOfMonth = curDate.getDayOfMonth();
            DialogFragment dialogFragment = DatePickerFragment.newInstance(initYear, initMonth, initDayOfMonth);
            dialogFragment.show(getSupportFragmentManager(), DatePickerFragment.DATE_PICKER_TAG);
        });
        // 获取并设置viewPager
        pagerAdapter = new PagerAdapter();
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(curPosition, false);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                curPosition = position;
                // 复制一份pageDates[position]对象给curDate
                curDate = LocalDate.ofEpochDay(pageDates[position].toEpochDay());
                onDateChanged();
                if (position == 0) {
                    viewPager.setUserInputEnabled(false);
                    updatePageDates(PAGES_COUNT - 2, curDate);
                    // 此处无需pagerAdapter.notifyDataSetChanged();
                    // 因为从第0页跳转到倒数第二页时，倒数第二页是还没有加载的
                    // 所以跳转后倒数第二页加载的就已经是新数据了，不必通知数据有更新
                } else if (position == PAGES_COUNT - 1) {
                    viewPager.setUserInputEnabled(false);
                    updatePageDates(1, curDate);
                    // 理由同上，无需pagerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    if (curPosition == 0) {
                        viewPager.setCurrentItem(PAGES_COUNT - 2, false);
                        viewPager.setUserInputEnabled(true);
                    } else if (curPosition == PAGES_COUNT - 1) {
                        viewPager.setCurrentItem(1, false);
                        viewPager.setUserInputEnabled(true);
                    }
                }
            }
        });
        // 监听日期选择对话框结果
        getSupportFragmentManager().setFragmentResultListener(D_REQUEST_KEY,
                MainActivity.this, (requestKey, result) -> {
                    int year = result.getInt(D_YEAR_KEY);
                    int month = result.getInt(D_MONTH_KEY);
                    int dayOfMonth = result.getInt(D_DAY_OF_MONTH_KEY);
                    curDate = LocalDate.of(year, month, dayOfMonth);
                    onDateChanged();
                    updatePageDates(curPosition, curDate);
                    pagerAdapter.notifyDataSetChanged();
                });
        onDateChanged();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // 保证在其他Activity修改了数据后，返回时数据及时更新，比如去EventActivity添加事件后
        pagerAdapter.notifyDataSetChanged();
        // 检查事件提醒是否启用
        checkEventReminder();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(CUR_EPOCH_DAY_KEY, curDate.toEpochDay());
        savedInstanceState.putInt(CUR_POSITION_KEY, curPosition);
        savedInstanceState.putInt(CUR_SCROLL_Y_KEY, curScrollY);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * 检查事件提醒是否启动，如果没有则启动
     */
    private void checkEventReminder() {
        Intent nextAlarmIntent = new Intent(this, EventReminderReceiver.class);
        nextAlarmIntent.setAction(EventReminderReceiver.EVENT_REMINDER_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                EventReminderReceiver.EVENT_REMINDER_REQUEST_CODE,
                nextAlarmIntent,
                PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent == null) {
            Log.d(TAG, "事件提醒未启动");
            sendBroadcast(nextAlarmIntent);
        } else {
            Log.d(TAG, "事件提醒已启动");
        }
    }

    /**
     * 当当前页日期改变时调用此方法
     */
    private void onDateChanged() {
        timeTv.setText(DateTimeFormatUtil.getReadableDate(curDate));
        LocalDate today = LocalDate.now();
        boolean isToday = today.equals(curDate);
        if (fab.getVisibility() == View.VISIBLE && isToday) {
            fab.setVisibility(View.INVISIBLE);
        }
        if (fab.getVisibility() == View.INVISIBLE && !isToday) {
            fab.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 以baseDate更新basePosition对应页的日期，并以该页日期为基准更新整个dates列表
     *
     * @param basePosition 基准页序号
     * @param baseDate     基准页日期
     */
    private void updatePageDates(int basePosition, LocalDate baseDate) {
        pageDates[basePosition] = LocalDate.ofEpochDay(baseDate.toEpochDay());
        for (int i = basePosition - 1; i >= 0; i--) {
            pageDates[i] = baseDate.minusDays(basePosition - i);
        }
        for (int i = basePosition + 1; i < pageDates.length; i++) {
            pageDates[i] = baseDate.plusDays(i - basePosition);
        }
    }

    // 注意这个DatePickerFragment只能是public static，否则报错，原因待挖掘
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        public static final String DATE_PICKER_TAG = "DatePickerFragment";


        public DatePickerFragment() {
        }

        public static DatePickerFragment newInstance(int year,
                                                     int month,
                                                     int dayOfMonth) {
            DatePickerFragment fragment = new DatePickerFragment();
            Bundle bundle = new Bundle();
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
                initYear = bundle.getInt(D_YEAR_KEY);
                initMonth = bundle.getInt(D_MONTH_KEY);
                initDayOfMonth = bundle.getInt(D_DAY_OF_MONTH_KEY);
            }
            return new DatePickerDialog(getActivity(), this, initYear, initMonth, initDayOfMonth);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            Bundle result = new Bundle();
            result.putInt(D_YEAR_KEY, year);
            result.putInt(D_MONTH_KEY, month + 1);
            result.putInt(D_DAY_OF_MONTH_KEY, dayOfMonth);
            getParentFragmentManager().setFragmentResult(D_REQUEST_KEY, result);
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
            private final RelativeLayout contentLayout;
            private final ImageView thumbUpImg1;
            private final ImageView thumbUpImg2;
            private final ImageView thumbUpImg3;
            private final ImageView thumbUpImg4;
            private final ImageView thumbUpImg5;
            private final TextView commentTv;
            private final TextView memoTv;
            private final TextView noSummaryTv;
            private final ScrollView scrollView;

            public PageViewHolder(View v) {
                super(v);
                // 获取控件
                scheduleLayout = v.findViewById(R.id.main_schedule_layout);
                contentLayout = v.findViewById(R.id.main_summary_content);
                thumbUpImg1 = v.findViewById(R.id.main_thumb_up_img1);
                thumbUpImg2 = v.findViewById(R.id.main_thumb_up_img2);
                thumbUpImg3 = v.findViewById(R.id.main_thumb_up_img3);
                thumbUpImg4 = v.findViewById(R.id.main_thumb_up_img4);
                thumbUpImg5 = v.findViewById(R.id.main_thumb_up_img5);
                commentTv = v.findViewById(R.id.main_comment_tv);
                memoTv = v.findViewById(R.id.main_memo_tv);
                noSummaryTv = v.findViewById(R.id.main_no_summary_tv);
                scrollView = v.findViewById(R.id.main_scroll_view);
                ImageView addEventImg = v.findViewById(R.id.main_add_event_img);
                ImageView addFocusRecordImg = v.findViewById(R.id.main_add_focus_record_img);
                ImageView addPlanImg = v.findViewById(R.id.main_add_plan_img);
                ImageView addSummaryImg = v.findViewById(R.id.main_add_summary_img);
                RecyclerView focusRecordsRv = v.findViewById(R.id.main_focus_records_rv);
                RecyclerView plansRv = v.findViewById(R.id.main_plans_rv);
                // 对添加事件按钮设置监听
                addEventImg.setOnClickListener(v13 -> {
                    EventDialogFragment dialogFragment = EventDialogFragment.newInstance();
                    dialogFragment.show(getSupportFragmentManager(), EventDialogFragment.TAG);
                });
                // 对添加专注按钮设置监听
                addFocusRecordImg.setOnClickListener(v14 -> {
                    Intent intent = new Intent(MainActivity.this, FocusActivity.class);
                    startActivity(intent);
                });
                // 对添加计划按钮设置监听
                addPlanImg.setOnClickListener(v12 -> {
                    PlanDialogFragment dialogFragment = PlanDialogFragment.newInstance();
                    dialogFragment.show(getSupportFragmentManager(), PlanDialogFragment.TAG);
                });
                // 对添加总结按钮设置监听
                addSummaryImg.setOnClickListener(v1 -> {
                    if (hasSummarized) {
                        ToastUtil.showToast(
                                MainActivity.this,
                                getString(R.string.main_have_sum_toast),
                                Toast.LENGTH_SHORT);

                    } else if (!date.equals(LocalDate.now())) {
                        ToastUtil.showToast(
                                MainActivity.this,
                                getString(R.string.main_sum_not_today_toast),
                                Toast.LENGTH_SHORT);
                    } else {
                        SummaryDialogFragment dialogFragment = SummaryDialogFragment.newInstance();
                        dialogFragment.show(getSupportFragmentManager(), SummaryDialogFragment.TAG);
                    }
                });
                // 初始化FocusRecordsRv，通过LayoutManager禁止其滚动
                RecyclerView.LayoutManager focusRecordsRvLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false) {
                    @Override
                    public boolean canScrollVertically() {
                        return false;
                    }
                };
                focusRecordsRv.setLayoutManager(focusRecordsRvLayoutManager);
                focusRecordRecyclerAdapter = new FocusRecordRecyclerAdapter(MainActivity.this, focusRecords);
                focusRecordsRv.setAdapter(focusRecordRecyclerAdapter);
                // 初始化PlansRv，通过LayoutManager禁止其滚动
                RecyclerView.LayoutManager plansRvLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false) {
                    @Override
                    public boolean canScrollVertically() {
                        return false;
                    }
                };
                plansRv.setLayoutManager(plansRvLayoutManager);
                planRecyclerAdapter = new PlanRecyclerAdapter(MainActivity.this, specPlans);
                plansRv.setAdapter(planRecyclerAdapter);
                // scrollView监听
                scrollView.setOnScrollChangeListener((v15, scrollX, scrollY, oldScrollX, oldScrollY) -> curScrollY = scrollY);
            }

            private void setRating(int rating) {
                int purpleA50 = ContextCompat.getColor(MainActivity.this, R.color.purple_a50);
                int grayA50 = ContextCompat.getColor(MainActivity.this, R.color.gray_a50);
                ImageView[] imageViews = {thumbUpImg1, thumbUpImg2, thumbUpImg3, thumbUpImg4, thumbUpImg5};
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
                scheduleLayout.setEvents(DbUtil.getDbEvents(MainActivity.this));
                scheduleLayout.setDate(date);
                scheduleLayout.refresh();
            }

            private void refreshFocusRecords() {
                focusRecords.clear();
                long start = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                long end = start + 86399999;
                List<FocusRecord> frs = DbUtil.getFocusRecordsDuring(MainActivity.this,
                        start, end);
                focusRecords.addAll(frs);
                focusRecordRecyclerAdapter.notifyDataSetChanged();
            }

            private void refreshPlans() {
                String dateStr = DateTimeFormatUtil.getNeatDate(date);
                specPlans.clear();
                List<Plan> plans = DbUtil.getPlans(MainActivity.this);
                for (Plan plan : plans) {
                    int completionCount = DbUtil.getPlanCompletionCountByDate(
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
                Summary summary = DbUtil.getSummary(MainActivity.this,
                        DateTimeFormatUtil.getNeatDate(date));
                if (summary != null) {
                    hasSummarized = true;
                    contentLayout.setVisibility(View.VISIBLE);
                    noSummaryTv.setVisibility(View.GONE);
                    // 将Summary的内容应用到界面上
                    setRating(summary.rating);
                    commentTv.setText(summary.comment);
                    memoTv.setText(summary.memo);
                } else {
                    hasSummarized = false;
                    contentLayout.setVisibility(View.GONE);
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
            holder.date = LocalDate.ofEpochDay(pageDates[position].toEpochDay());
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
            // scrollView绘制完后scrollTo到当前viewPager滚动到的位置
            holder.scrollView.post(() -> {
                holder.scrollView.scrollTo(0, curScrollY);
                curScrollY = holder.scrollView.getScrollY();
            });
        }

    }
}