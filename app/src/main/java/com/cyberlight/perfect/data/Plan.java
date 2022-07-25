package com.cyberlight.perfect.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "tb_plan")
public class Plan {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "plan_id", typeAffinity = ColumnInfo.INTEGER)
    public int planId;

    @ColumnInfo(name = "content", typeAffinity = ColumnInfo.TEXT)
    public String content;

    @ColumnInfo(name = "target_num", typeAffinity = ColumnInfo.INTEGER)
    public int targetNum;

    public Plan(int planId, String content, int targetNum) {
        this.planId = planId;
        this.content = content;
        this.targetNum = targetNum;
    }

    @Ignore
    public Plan(String content, int targetNum) {
        this.content = content;
        this.targetNum = targetNum;
    }

    @NonNull
    @Override
    public String toString() {
        return "Plan{" +
                "planId=" + planId +
                ", content='" + content + '\'' +
                ", targetNum=" + targetNum +
                '}';
    }
}