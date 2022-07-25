package com.cyberlight.perfect.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.time.LocalDate;

@Entity(tableName = "tb_plan_record", primaryKeys = {"date_str", "plan_id"})
public class PlanRecord {


    @NonNull
    @ColumnInfo(name = "date_str", typeAffinity = ColumnInfo.TEXT)
    public LocalDate date;

    @ColumnInfo(name = "completion_count", typeAffinity = ColumnInfo.INTEGER)
    public int completionCount;

    @ColumnInfo(name = "plan_id", typeAffinity = ColumnInfo.INTEGER)
    public int planId;

    public PlanRecord(@NonNull LocalDate date, int completionCount, int planId) {
        this.date = date;
        this.completionCount = completionCount;
        this.planId = planId;
    }
}