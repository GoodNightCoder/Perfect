package com.cyberlight.perfect.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DbHelper extends SQLiteOpenHelper {

    private static final String TAG = "DbHelper";

    // Events Table
    private static final String SQL_CREATE_EVENTS_TABLE =
            "CREATE TABLE " + DbContract.EventsTable.TABLE_NAME + " (" +
                    DbContract.EventsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DbContract.EventsTable.COLUMN_NAME_TITLE + " VARCHAR(100)," +
                    DbContract.EventsTable.COLUMN_NAME_START + " INTEGER," +
                    DbContract.EventsTable.COLUMN_NAME_DURATION + " INTEGER," +
                    DbContract.EventsTable.COLUMN_NAME_INTERVAL + " INTEGER)";
    private static final String SQL_DELETE_EVENTS_TABLE =
            "DROP TABLE IF EXISTS " + DbContract.EventsTable.TABLE_NAME;


    // Event Records Table
    private static final String SQL_CREATE_EVENT_RECORDS_TABLE =
            "CREATE TABLE " + DbContract.EventRecordsTable.TABLE_NAME + " (" +
                    DbContract.EventRecordsTable.COLUMN_NAME_EVENT_ID + " INTEGER," +
                    DbContract.EventRecordsTable.COLUMN_NAME_COMPLETION_INDEX + " INTEGER," +
                    "PRIMARY KEY (" + DbContract.EventRecordsTable.COLUMN_NAME_EVENT_ID + "," +
                    DbContract.EventRecordsTable.COLUMN_NAME_COMPLETION_INDEX + "))";
    private static final String SQL_DELETE_EVENT_RECORDS_TABLE =
            "DROP TABLE IF EXISTS " + DbContract.EventRecordsTable.TABLE_NAME;

    // Focus Records Table
    private static final String SQL_CREATE_FOCUS_RECORDS_TABLE =
            "CREATE TABLE " + DbContract.FocusRecordsTable.TABLE_NAME + " (" +
                    DbContract.FocusRecordsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DbContract.FocusRecordsTable.COLUMN_NAME_COMPLETION_TIME + " INTEGER," +
                    DbContract.FocusRecordsTable.COLUMN_NAME_FOCUS_DURATION + " INTEGER)";
    private static final String SQL_DELETE_FOCUS_RECORDS_TABLE =
            "DROP TABLE IF EXISTS " + DbContract.FocusRecordsTable.TABLE_NAME;


    // Plans Table
    private static final String SQL_CREATE_PLANS_TABLE =
            "CREATE TABLE " + DbContract.PlansTable.TABLE_NAME + " (" +
                    DbContract.PlansTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DbContract.PlansTable.COLUMN_NAME_CONTENT + " VARCHAR(200)," +
                    DbContract.PlansTable.COLUMN_NAME_TARGET_NUM + " INTEGER)";
    private static final String SQL_DELETE_PLANS_TABLE =
            "DROP TABLE IF EXISTS " + DbContract.PlansTable.TABLE_NAME;

    // Plan Records Table
    private static final String SQL_CREATE_PLAN_RECORDS_TABLE =
            "CREATE TABLE " + DbContract.PlanRecordsTable.TABLE_NAME + " (" +
                    DbContract.PlanRecordsTable.COLUMN_NAME_PLAN_RECORD_DATE + " VARCHAR(50)," +
                    DbContract.PlanRecordsTable.COLUMN_NAME_PLAN_ID + " INTEGER," +
                    DbContract.PlanRecordsTable.COLUMN_NAME_COMPLETION_COUNT + " INTEGER," +
                    "PRIMARY KEY (" + DbContract.PlanRecordsTable.COLUMN_NAME_PLAN_RECORD_DATE + "," +
                    DbContract.PlanRecordsTable.COLUMN_NAME_PLAN_ID + "))";
    private static final String SQL_DELETE_PLAN_RECORDS_TABLE =
            "DROP TABLE IF EXISTS " + DbContract.PlanRecordsTable.TABLE_NAME;

    // Summary Table
    private static final String SQL_CREATE_SUMMARY_TABLE =
            "CREATE TABLE " + DbContract.SummaryTable.TABLE_NAME + " (" +
                    DbContract.SummaryTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DbContract.SummaryTable.COLUMN_NAME_RATING + " INTEGER," +
                    DbContract.SummaryTable.COLUMN_NAME_SUMMARY_TEXT + " TEXT," +
                    DbContract.SummaryTable.COLUMN_NAME_SUMMARY_MEMO + " TEXT," +
                    DbContract.SummaryTable.COLUMN_NAME_SUMMARY_DATE + " VARCHAR(50) UNIQUE)";

    private static final String SQL_DELETE_SUMMARY_TABLE =
            "DROP TABLE IF EXISTS " + DbContract.SummaryTable.TABLE_NAME;

    public DbHelper(@Nullable Context context) {
        super(context, DbContract.DATABASE_NAME, null, DbContract.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_EVENTS_TABLE);
        db.execSQL(SQL_CREATE_EVENT_RECORDS_TABLE);
        db.execSQL(SQL_CREATE_FOCUS_RECORDS_TABLE);
        db.execSQL(SQL_CREATE_PLANS_TABLE);
        db.execSQL(SQL_CREATE_PLAN_RECORDS_TABLE);
        db.execSQL(SQL_CREATE_SUMMARY_TABLE);
        Log.d(TAG, "Create succeeded");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_EVENTS_TABLE);
        db.execSQL(SQL_DELETE_EVENT_RECORDS_TABLE);
        db.execSQL(SQL_DELETE_FOCUS_RECORDS_TABLE);
        db.execSQL(SQL_DELETE_PLANS_TABLE);
        db.execSQL(SQL_DELETE_PLAN_RECORDS_TABLE);
        db.execSQL(SQL_DELETE_SUMMARY_TABLE);
        onCreate(db);
        Log.d(TAG, "Update succeeded");
    }

}
