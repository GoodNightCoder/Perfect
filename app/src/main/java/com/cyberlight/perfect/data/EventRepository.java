package com.cyberlight.perfect.data;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

public class EventRepository {

    private static final String TAG = "EventRepository";
    private final EventDao eventDao;

    public EventRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        eventDao = database.eventDao();
    }

    public void insertEvents(Event... events) {
        eventDao.insertEvents(events);
    }

    public void deleteEvents(Event... events) {
        eventDao.deleteEvents(events);
    }

    public void deleteAllEvents() {
        eventDao.deleteAllEvents();
    }

    public LiveData<List<Event>> loadAllEventsLiveData() {
        return eventDao.loadAllEventsLiveData();
    }

    public void loadAllEvents(FutureCallback<? super List<Event>> callback, Context context) {
        ListenableFuture<List<Event>> future = eventDao.loadAllEvents();
        Futures.addCallback(
                future,
                callback,
                // causes the callbacks to be executed on the main (UI) thread
                context.getMainExecutor()
        );
    }
}