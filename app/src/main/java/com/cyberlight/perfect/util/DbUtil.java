package com.cyberlight.perfect.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.cyberlight.perfect.model.Event;
import com.cyberlight.perfect.model.FocusRecord;
import com.cyberlight.perfect.model.Plan;
import com.cyberlight.perfect.model.SpecEvent;
import com.cyberlight.perfect.model.Summary;

import java.util.ArrayList;
import java.util.List;

public class DbUtil {
    private static final String TAG = "DbUtil";

    /**
     * 清空指定数据库表的数据
     * tableName必须是如下数据库表名：
     * <ul>
     *  <li>{@link DbContract.EventsTable#TABLE_NAME}</li>
     *  <li>{@link DbContract.EventRecordsTable#TABLE_NAME}</li>
     *  <li>{@link DbContract.FocusRecordsTable#TABLE_NAME}</li>
     *  <li>{@link DbContract.PlansTable#TABLE_NAME}</li>
     *  <li>{@link DbContract.PlanRecordsTable#TABLE_NAME}</li>
     *  <li>{@link DbContract.SummaryTable#TABLE_NAME}</li>
     * </ul>
     *
     * @param context   用于创建DbHelper的context对象
     * @param tableName 要清空数据的数据库表名
     */
    public static void truncateTable(Context context, @DbContract.DbTableName String tableName) {
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + tableName + ";");
        db.execSQL("DELETE FROM sqlite_sequence WHERE name = '" + tableName + "';");
        db.execSQL("VACUUM");
    }

    /**
     * 添加事件到事件表
     *
     * @param context  用于创建DbHelper的context对象
     * @param title    事件标题
     * @param start    事件起始时间(EpochMillis)
     * @param duration 事件持续时长(ms)
     * @param interval 事件重复间隔(ms)
     * @return 事件是否添加成功
     */
    public static boolean addEvent(Context context,
                                   String title,
                                   long start,
                                   long duration,
                                   long interval) {
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.EventsTable.COLUMN_NAME_TITLE, title);
        values.put(DbContract.EventsTable.COLUMN_NAME_START, start);
        values.put(DbContract.EventsTable.COLUMN_NAME_DURATION, duration);
        values.put(DbContract.EventsTable.COLUMN_NAME_INTERVAL, interval);
        long newRowId = db.insert(DbContract.EventsTable.TABLE_NAME, null, values);
        return newRowId != -1;
    }

    /**
     * 获取事件表中所有事件
     *
     * @param context 用于创建DbHelper的context对象
     * @return 包含事件表中所有事件的List
     */
    public static List<Event> getDbEvents(Context context) {
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Event> events = new ArrayList<>();
        //从数据库中读取所有事件
        Cursor cursor = db.query(DbContract.EventsTable.TABLE_NAME, null, null
                , null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                int eventIdColIndex = cursor.getColumnIndex(DbContract.EventsTable._ID);
                int eventTitleColIndex = cursor.getColumnIndex(DbContract.EventsTable.COLUMN_NAME_TITLE);
                int eventStartColIndex = cursor.getColumnIndex(DbContract.EventsTable.COLUMN_NAME_START);
                int eventDurationColIndex = cursor.getColumnIndex(DbContract.EventsTable.COLUMN_NAME_DURATION);
                int eventIntervalColIndex = cursor.getColumnIndex(DbContract.EventsTable.COLUMN_NAME_INTERVAL);
                if (eventIdColIndex >= 0 && eventTitleColIndex >= 0 && eventStartColIndex >= 0
                        && eventDurationColIndex >= 0 && eventIntervalColIndex >= 0) {
                    int eventId = cursor.getInt(eventIdColIndex);
                    String title = cursor.getString(eventTitleColIndex);
                    long start = cursor.getLong(eventStartColIndex);
                    long duration = cursor.getLong(eventDurationColIndex);
                    long interval = cursor.getLong(eventIntervalColIndex);
                    events.add(new Event(eventId, title, start, duration, interval));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return events;
    }

    public static boolean addPlanRecord(Context context,
                                        String dateStr,
                                        int planId,
                                        int completionCount) {
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //添加Event record到数据库
        ContentValues values = new ContentValues();
        values.put(DbContract.PlanRecordsTable.COLUMN_NAME_PLAN_RECORD_DATE, dateStr);
        values.put(DbContract.PlanRecordsTable.COLUMN_NAME_PLAN_ID, planId);
        values.put(DbContract.PlanRecordsTable.COLUMN_NAME_COMPLETION_COUNT, completionCount);
        long newRowId = db.insert(DbContract.PlanRecordsTable.TABLE_NAME, null, values);
        return newRowId != -1;
    }

    public static int getPlanCompletionCountByDate(Context context,
                                                   String dateStr,
                                                   int planId) {
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int completionCount = -1;
        //从数据库中读取所有事件
        String selection = DbContract.PlanRecordsTable.COLUMN_NAME_PLAN_RECORD_DATE + " = ? AND " +
                DbContract.PlanRecordsTable.COLUMN_NAME_PLAN_ID + " = ?";
        String[] selectionArgs = {dateStr, String.valueOf(planId)};
        Cursor cursor = db.query(DbContract.PlanRecordsTable.TABLE_NAME, null, selection
                , selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            int completionCountCol =
                    cursor.getColumnIndex(DbContract.PlanRecordsTable.COLUMN_NAME_COMPLETION_COUNT);
            if (completionCountCol >= 0) {
                completionCount = cursor.getInt(completionCountCol);
            }
        }
        cursor.close();
        return completionCount;
    }

    public static boolean updatePlanRecord(Context context,
                                           String dateStr,
                                           int planId,
                                           int newCompletionCount) {
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.PlanRecordsTable.COLUMN_NAME_COMPLETION_COUNT, newCompletionCount);
        String selection = DbContract.PlanRecordsTable.COLUMN_NAME_PLAN_RECORD_DATE + " = ? AND "
                + DbContract.PlanRecordsTable.COLUMN_NAME_PLAN_ID + " = ?";
        String[] selectionArgs = {dateStr, String.valueOf(planId)};
        int count = db.update(DbContract.PlanRecordsTable.TABLE_NAME, values, selection, selectionArgs);
        return count > 0;
    }

    public static boolean addPlan(Context context,
                                  String planContent,
                                  int targetNum) {
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //添加Event到数据库
        ContentValues values = new ContentValues();
        values.put(DbContract.PlansTable.COLUMN_NAME_CONTENT, planContent);
        values.put(DbContract.PlansTable.COLUMN_NAME_TARGET_NUM, targetNum);
        long newRowId = db.insert(DbContract.PlansTable.TABLE_NAME, null, values);
        return newRowId != -1;
    }

    public static List<Plan> getPlans(Context context) {
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Plan> plans = new ArrayList<>();
        //从数据库中读取所有事件
        Cursor cursor = db.query(DbContract.PlansTable.TABLE_NAME, null, null
                , null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                int planIdColIndex = cursor.getColumnIndex(DbContract.PlansTable._ID);
                int planContentColIndex = cursor.getColumnIndex(DbContract.PlansTable.COLUMN_NAME_CONTENT);
                int targetNumColIndex = cursor.getColumnIndex(DbContract.PlansTable.COLUMN_NAME_TARGET_NUM);
                if (planIdColIndex >= 0 && planContentColIndex >= 0 && targetNumColIndex >= 0) {
                    int planId = cursor.getInt(planIdColIndex);
                    String planContent = cursor.getString(planContentColIndex);
                    int targetNum = cursor.getInt(targetNumColIndex);
                    plans.add(new Plan(planId, planContent, targetNum));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return plans;
    }

    public static boolean addSummary(Context context,
                                     int rating,
                                     String summaryText,
                                     String summaryMemo,
                                     String dateStr) {
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.SummaryTable.COLUMN_NAME_RATING, rating);
        values.put(DbContract.SummaryTable.COLUMN_NAME_SUMMARY_TEXT, summaryText);
        values.put(DbContract.SummaryTable.COLUMN_NAME_SUMMARY_MEMO, summaryMemo);
        values.put(DbContract.SummaryTable.COLUMN_NAME_SUMMARY_DATE, dateStr);
        long newRowId = db.insert(DbContract.SummaryTable.TABLE_NAME, null, values);
        return newRowId != -1;
    }

    public static Summary getSummary(Context context,
                                     String dateStr) {
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Summary summary = null;
        //从数据库中读取所有事件
        String selection = DbContract.SummaryTable.COLUMN_NAME_SUMMARY_DATE + " = ?";
        String[] selectionArgs = {dateStr};
        Cursor cursor = db.query(DbContract.SummaryTable.TABLE_NAME, null, selection
                , selectionArgs, null, null, null);
        if (cursor.moveToFirst()) {
            int summaryIdCol = cursor.getColumnIndex(DbContract.SummaryTable._ID);
            int ratingCol = cursor.getColumnIndex(DbContract.SummaryTable.COLUMN_NAME_RATING);
            int summaryTextCol = cursor.getColumnIndex(DbContract.SummaryTable.COLUMN_NAME_SUMMARY_TEXT);
            int summaryMemoCol = cursor.getColumnIndex(DbContract.SummaryTable.COLUMN_NAME_SUMMARY_MEMO);
            int summaryDateCol = cursor.getColumnIndex(DbContract.SummaryTable.COLUMN_NAME_SUMMARY_DATE);
            if (summaryIdCol >= 0 && ratingCol >= 0 && summaryTextCol >= 0
                    && summaryMemoCol >= 0 && summaryDateCol >= 0) {
                int summaryId = cursor.getInt(summaryIdCol);
                int rating = cursor.getInt(ratingCol);
                String summaryText = cursor.getString(summaryTextCol);
                String summaryMemo = cursor.getString(summaryMemoCol);
                String summaryDate = cursor.getString(summaryDateCol);
                summary = new Summary(summaryId, rating, summaryText, summaryMemo, summaryDate);
            }
        }
        cursor.close();
        return summary;
    }

    public static boolean addFocusRecord(Context context,
                                         long completionTime,
                                         long focusDuration) {
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbContract.FocusRecordsTable.COLUMN_NAME_COMPLETION_TIME, completionTime);
        values.put(DbContract.FocusRecordsTable.COLUMN_NAME_FOCUS_DURATION, focusDuration);
        long newRowId = db.insert(DbContract.FocusRecordsTable.TABLE_NAME, null, values);
        return newRowId != -1;
    }

    public static List<FocusRecord> getFocusRecordsDuring(Context context,
                                                          long start,
                                                          long end) {
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<FocusRecord> focusRecords = new ArrayList<>();
        String[] columns = {
                DbContract.FocusRecordsTable.COLUMN_NAME_COMPLETION_TIME,
                DbContract.FocusRecordsTable.COLUMN_NAME_FOCUS_DURATION
        };
        String selection = DbContract.FocusRecordsTable.COLUMN_NAME_COMPLETION_TIME
                + " BETWEEN " + start + " AND " + end;
        Cursor cursor = db.query(DbContract.FocusRecordsTable.TABLE_NAME, columns, selection
                , null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                int completionTimeCol =
                        cursor.getColumnIndex(DbContract.FocusRecordsTable.COLUMN_NAME_COMPLETION_TIME);
                int focusDurationCol =
                        cursor.getColumnIndex(DbContract.FocusRecordsTable.COLUMN_NAME_FOCUS_DURATION);
                if (completionTimeCol >= 0 && focusDurationCol >= 0) {
                    long completionTime = cursor.getLong(completionTimeCol);
                    long focusDuration = cursor.getLong(focusDurationCol);
                    FocusRecord focusRecord = new FocusRecord(completionTime, focusDuration);
                    focusRecords.add(focusRecord);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return focusRecords;
    }

    public static boolean specEventIsFinished(Context context,
                                              SpecEvent specEvent) {
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectSql = "SELECT * FROM " + DbContract.EventsTable.TABLE_NAME + " INNER JOIN "
                + DbContract.EventRecordsTable.TABLE_NAME + " ON " + DbContract.EventsTable.TABLE_NAME
                + "." + DbContract.EventsTable._ID + "=" + DbContract.EventRecordsTable.TABLE_NAME
                + "." + DbContract.EventRecordsTable.COLUMN_NAME_EVENT_ID + " WHERE "
                + DbContract.EventRecordsTable.COLUMN_NAME_EVENT_ID + "=" + specEvent.eventId
                + " AND " + DbContract.EventRecordsTable.COLUMN_NAME_COMPLETION_INDEX + "="
                + specEvent.occurNum + ";";
        Cursor cursor = db.rawQuery(selectSql, null);
        if (cursor.moveToFirst()) {
            Log.d(TAG, "specEvent finished");
            cursor.close();
            return true;
        } else {
            Log.d(TAG, "specEvent unfinished");
            cursor.close();
            return false;
        }
    }

    public static boolean finishSpecEvent(Context context,
                                          SpecEvent specEvent) {
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //添加Event record到数据库
        ContentValues values = new ContentValues();
        values.put(DbContract.EventRecordsTable.COLUMN_NAME_EVENT_ID, specEvent.eventId);
        values.put(DbContract.EventRecordsTable.COLUMN_NAME_COMPLETION_INDEX, specEvent.occurNum);
        long newRowId = db.insert(DbContract.EventRecordsTable.TABLE_NAME, null, values);
        return newRowId != -1;
    }

    public static boolean unfinishSpecEvent(Context context,
                                            SpecEvent specEvent) {
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = DbContract.EventRecordsTable.COLUMN_NAME_EVENT_ID + "="
                + specEvent.eventId + " AND "
                + DbContract.EventRecordsTable.COLUMN_NAME_COMPLETION_INDEX
                + "=" + specEvent.occurNum;
        int deletedRows = db.delete(DbContract.EventRecordsTable.TABLE_NAME, selection, null);
        return deletedRows > 0;
    }
}
