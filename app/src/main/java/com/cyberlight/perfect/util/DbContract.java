package com.cyberlight.perfect.util;

import android.provider.BaseColumns;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 数据库协定类，协定数据库名、版本，
 * 并为每个表创建一个静态内部类，在类
 * 中枚举列的名称
 */
public final class DbContract {
    private DbContract() {
    }

    //DB info
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "perfect.db";


    // @interface 创建自定义注解
    // @Retention 指定注解保留时间
    // @IntDef 限制取值
    @StringDef({EventsTable.TABLE_NAME, EventRecordsTable.TABLE_NAME, FocusRecordsTable.TABLE_NAME,
            PlansTable.TABLE_NAME, PlanRecordsTable.TABLE_NAME, SummaryTable.TABLE_NAME})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DbTableName {
    }

    public static class EventsTable implements BaseColumns {

        public static final String TABLE_NAME = "events";

        /**
         * 事件标题
         */
        public static final String COLUMN_NAME_TITLE = "title";

        /**
         * 事件开始时间
         */
        public static final String COLUMN_NAME_START = "start";

        /**
         * 事件持续时长
         */
        public static final String COLUMN_NAME_DURATION = "duration";

        /**
         * 事件重复间隔时间
         */
        public static final String COLUMN_NAME_INTERVAL = "interval";
    }


    public static class EventRecordsTable {
        public static final String TABLE_NAME = "event_records";

        /**
         * 事件ID(对应Events表里的一个事件)
         */
        public static final String COLUMN_NAME_EVENT_ID = "event_id";

        /**
         * 完成下标(对应事件发生次数occurNum)
         */
        public static final String COLUMN_NAME_COMPLETION_INDEX = "completion_index";
    }


    public static class FocusRecordsTable implements BaseColumns {
        public static final String TABLE_NAME = "focus_records";

        /**
         * 完成时间
         */
        public static final String COLUMN_NAME_COMPLETION_TIME = "completion_time";

        /**
         * 专注时长，有20、25、40、45
         */
        public static final String COLUMN_NAME_FOCUS_DURATION = "focus_duration";
    }


    public static class PlansTable implements BaseColumns {
        public static final String TABLE_NAME = "plans";

        /**
         * 计划内容
         */
        public static final String COLUMN_NAME_CONTENT = "content";

        /**
         * 目标完成个数
         */
        public static final String COLUMN_NAME_TARGET_NUM = "target_num";
    }

    public static class PlanRecordsTable {
        public static final String TABLE_NAME = "plan_records";

        /**
         * 计划ID(对应Plans表里的一个计划)
         */
        public static final String COLUMN_NAME_PLAN_ID = "plan_id";

        /**
         * 记录日期
         */
        public static final String COLUMN_NAME_PLAN_RECORD_DATE = "plan_record_date";

        /**
         * 完成个数
         */
        public static final String COLUMN_NAME_COMPLETION_COUNT = "completion_count";
    }

    public static class SummaryTable implements BaseColumns {
        public static final String TABLE_NAME = "summary";

        /**
         * 打分,0到5
         */
        public static final String COLUMN_NAME_RATING = "rating";

        /**
         * 总结文字
         */
        public static final String COLUMN_NAME_SUMMARY_TEXT = "summary_text";

        /**
         * 总结对应的日期
         */
        public static final String COLUMN_NAME_SUMMARY_DATE = "summary_date";

        /**
         * 总结的明日备忘
         */
        public static final String COLUMN_NAME_SUMMARY_MEMO = "summary_memo";
    }
}
