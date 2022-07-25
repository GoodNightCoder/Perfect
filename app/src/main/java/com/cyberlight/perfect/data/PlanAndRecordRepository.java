package com.cyberlight.perfect.data;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.time.LocalDate;
import java.util.List;

public class PlanAndRecordRepository {
    private static final String TAG = "PlanAndRecordRepository";
    private final PlanDao planDao;
    private final PlanRecordDao planRecordDao;
    private final PlanWithRecordDao planWithRecordDao;

    public PlanAndRecordRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        planDao = database.planDao();
        planRecordDao = database.planRecordDao();
        planWithRecordDao = database.planWithRecordDao();
    }

    public void insertPlans(Plan... plans) {
        planDao.insertPlans(plans);
    }

    public void deletePlans(Plan... plans) {
        planDao.deletePlans(plans);
    }

    public void deleteAllPlans() {
        planDao.deleteAllPlans();
    }

    public void deleteAllPlanRecords() {
        planRecordDao.deleteAllPlanRecords();
    }

    public void insertPlanRecords(PlanRecord... planRecords) {
        planRecordDao.insertPlanRecords(planRecords);
        Log.d(TAG, String.valueOf(planRecords[0].planId));
        Log.d(TAG, String.valueOf(planRecords[0].completionCount));
        Log.d(TAG, String.valueOf(planRecords[0].date));
    }

    public LiveData<List<PlanWithRecord>> loadPlanWithRecordsLiveDataByDate(LocalDate date) {
        return planWithRecordDao.loadPlanWithRecordsLiveDataByDate(date);
    }

    // 同步方法，用于AppWidget，不可在主线程调用
    public List<PlanWithRecord> loadPlanWithRecordsByDateSync(LocalDate date) {
        return planWithRecordDao.loadPlanWithRecordsByDateSync(date);
    }

    public void loadPlanWithRecordByDateAndPlanId(LocalDate date, int planId, FutureCallback<? super PlanWithRecord> callback, Context context) {
        ListenableFuture<PlanWithRecord> future = planWithRecordDao.loadPlanWithRecordByDateAndPlanId(date, planId);
        Futures.addCallback(
                future,
                callback,
                // causes the callbacks to be executed on the main (UI) thread
                context.getMainExecutor()
        );
    }
}
