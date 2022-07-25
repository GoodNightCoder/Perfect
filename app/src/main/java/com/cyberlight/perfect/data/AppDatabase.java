package com.cyberlight.perfect.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Event.class, FocusRecord.class, Plan.class, PlanRecord.class, Summary.class},
        version = 1, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "perfectroom.db";
    private static AppDatabase mInstance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (mInstance == null) {
            mInstance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, DATABASE_NAME).build();
        }
        return mInstance;
    }

    public abstract EventDao eventDao();

    public abstract FocusRecordDao focusRecordDao();

    public abstract PlanDao planDao();

    public abstract PlanRecordDao planRecordDao();

    public abstract PlanWithRecordDao planWithRecordDao();

    public abstract SummaryDao summaryDao();


}
