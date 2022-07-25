package com.cyberlight.perfect.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

public class FocusRecordRepository {
    private final FocusRecordDao focusRecordDao;

    public FocusRecordRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        focusRecordDao = database.focusRecordDao();
    }

    public void insertFocusRecords(FocusRecord... focusRecords) {
        focusRecordDao.insertFocusRecords(focusRecords);
    }

    public void deleteFocusRecords(FocusRecord... focusRecords) {
        focusRecordDao.deleteFocusRecords(focusRecords);
    }

    public void deleteAllFocusRecords() {
        focusRecordDao.deleteAllFocusRecords();
    }

    public LiveData<List<FocusRecord>> loadAllFocusRecordsLiveDataDuring(long start, long end) {
        return focusRecordDao.loadAllFocusRecordsLiveDataDuring(start, end);
    }

    public void loadAllFocusRecordsDuring(long start,
                                          long end,
                                          FutureCallback<? super List<FocusRecord>> callback,
                                          Context context) {
        ListenableFuture<List<FocusRecord>> future = focusRecordDao.loadAllFocusRecordsDuring(start, end);
        Futures.addCallback(
                future,
                callback,
                context.getMainExecutor()
        );
    }
}
