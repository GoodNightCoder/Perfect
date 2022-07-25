package com.cyberlight.perfect.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "tb_focus_record")
public class FocusRecord {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "record_id", typeAffinity = ColumnInfo.INTEGER)
    public int recordId;

    @ColumnInfo(name = "completion_time", typeAffinity = ColumnInfo.INTEGER)
    public long completionTime;

    @ColumnInfo(name = "focus_duration", typeAffinity = ColumnInfo.INTEGER)
    public long focusDuration;

    public FocusRecord(int recordId, long completionTime, long focusDuration) {
        this.recordId = recordId;
        this.completionTime = completionTime;
        this.focusDuration = focusDuration;
    }

    @Ignore
    public FocusRecord(long completionTime, long focusDuration) {
        this.completionTime = completionTime;
        this.focusDuration = focusDuration;
    }
}
