package com.cyberlight.perfect.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.cyberlight.perfect.data.Event;
import com.cyberlight.perfect.data.EventRepository;
import com.cyberlight.perfect.data.FocusRecord;
import com.cyberlight.perfect.data.FocusRecordRepository;
import com.cyberlight.perfect.data.PlanAndRecordRepository;
import com.cyberlight.perfect.data.PlanRecord;
import com.cyberlight.perfect.data.Summary;
import com.cyberlight.perfect.data.SummaryRepository;
import com.cyberlight.perfect.data.PlanWithRecord;
import com.cyberlight.perfect.util.DateTimeFormatUtil;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class ActivityMainViewModel extends AndroidViewModel {

    private static final String TAG = "ActivityMainViewModel";
    private final MutableLiveData<LocalDate> date = new MutableLiveData<>();
    private LiveData<List<Event>> events;
    private final LiveData<List<FocusRecord>> focusRecords =
            Transformations.switchMap(date, new Function<LocalDate, LiveData<List<FocusRecord>>>() {
                @Override
                public LiveData<List<FocusRecord>> apply(LocalDate input) {
                    Log.d(TAG, "加载" + DateTimeFormatUtil.getNeatDate(input) + "的focusRecords");
                    long start = input.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                    long end = start + 86399999;
                    return focusRecordRepository.loadAllFocusRecordsLiveDataDuring(start, end);
                }
            });
    private final LiveData<List<PlanWithRecord>> planWithRecords =
            Transformations.switchMap(date, new Function<LocalDate, LiveData<List<PlanWithRecord>>>() {
                @Override
                public LiveData<List<PlanWithRecord>> apply(LocalDate input) {
                    Log.d(TAG, "加载" + DateTimeFormatUtil.getNeatDate(input) + "的planWithRecords");
                    return planAndRecordRepository.loadPlanWithRecordsLiveDataByDate(input);
                }
            });
    private final LiveData<Summary> summary =
            Transformations.switchMap(date, new Function<LocalDate, LiveData<Summary>>() {
                @Override
                public LiveData<Summary> apply(LocalDate input) {
                    Log.d(TAG, "加载" + DateTimeFormatUtil.getNeatDate(input) + "的summmary");
                    return summaryRepository.loadSummaryLiveDataByDate(input);
                }
            });

    private final EventRepository eventRepository;
    private final FocusRecordRepository focusRecordRepository;
    private final PlanAndRecordRepository planAndRecordRepository;
    private final SummaryRepository summaryRepository;

    public ActivityMainViewModel(@NonNull Application application) {
        super(application);
        eventRepository = new EventRepository(application);
        focusRecordRepository = new FocusRecordRepository(application);
        planAndRecordRepository = new PlanAndRecordRepository(application);
        summaryRepository = new SummaryRepository(application);
    }

    public LiveData<List<Event>> getEvents() {
        if (events == null) {
            events = eventRepository.loadAllEventsLiveData();
        }
        return events;
    }

    public LiveData<List<FocusRecord>> getFocusRecords() {
        return focusRecords;
    }

    public LiveData<List<PlanWithRecord>> getPlanWithRecords() {
        return planWithRecords;
    }

    public LiveData<Summary> getSummary() {
        return summary;
    }

    public void setDate(LocalDate date) {
        this.date.setValue(date);
    }

    public void doOnClickPlan(PlanWithRecord planWithRecord) {
        int newCompletionCount = (planWithRecord.completionCount + 1) % (planWithRecord.plan.targetNum + 1);
        PlanRecord planRecordToUpdate = new PlanRecord(planWithRecord.date, newCompletionCount, planWithRecord.plan.planId);

        planAndRecordRepository.insertPlanRecords(planRecordToUpdate);
    }

}