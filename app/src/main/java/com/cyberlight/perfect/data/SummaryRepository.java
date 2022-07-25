package com.cyberlight.perfect.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.time.LocalDate;
import java.util.List;

public class SummaryRepository {
    private final SummaryDao summaryDao;

    public SummaryRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        summaryDao = database.summaryDao();
    }

    public void insertSummaries(Summary... summaries) {
        summaryDao.insertSummaries(summaries);
    }

    public void deleteSummaries(Summary... summaries) {
        summaryDao.deleteSummaries(summaries);
    }

    public void deleteAllSummaries() {
        summaryDao.deleteAllSummaries();
    }

    public LiveData<Summary> loadSummaryLiveDataByDate(LocalDate date) {
        return summaryDao.loadSummaryLiveDataByDate(date);
    }

    public void loadSummaryByDate(LocalDate date, FutureCallback<? super Summary> callback, Context context) {
        ListenableFuture<Summary> future = summaryDao.loadSummaryByDate(date);
        Futures.addCallback(
                future,
                callback,
                // causes the callbacks to be executed on the main (UI) thread
                context.getMainExecutor()
        );
    }
}
