package com.cyberlight.perfect.data;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

import com.cyberlight.perfect.data.Plan;

import java.time.LocalDate;

public class PlanWithRecord {
    @Embedded
    public Plan plan;

    @ColumnInfo(name = "date_str", typeAffinity = ColumnInfo.TEXT)
    public LocalDate date;

    @ColumnInfo(name = "completion_count", typeAffinity = ColumnInfo.INTEGER)
    public int completionCount;

    public PlanWithRecord(Plan plan, LocalDate date, int completionCount) {
        this.plan = plan;
        this.date = date;
        this.completionCount = completionCount;
    }
}
