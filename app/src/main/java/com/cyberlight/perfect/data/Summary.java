package com.cyberlight.perfect.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

@Entity(tableName = "tb_summary",
        indices = {@Index(value = {"date_str"}, unique = true)})
public class Summary {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "summary_id", typeAffinity = ColumnInfo.INTEGER)
    public int summaryId;

    @ColumnInfo(name = "rating", typeAffinity = ColumnInfo.INTEGER)
    public int rating;

    @ColumnInfo(name = "review", typeAffinity = ColumnInfo.TEXT)
    public String review;

    @ColumnInfo(name = "memo", typeAffinity = ColumnInfo.TEXT)
    public String memo;

    @ColumnInfo(name = "date_str", typeAffinity = ColumnInfo.TEXT)
    public LocalDate date;

    public Summary(int summaryId, int rating, String review, String memo, LocalDate date) {
        this.summaryId = summaryId;
        this.rating = rating;
        this.review = review;
        this.memo = memo;
        this.date = date;
    }

    @Ignore
    public Summary(int rating, String review, String memo, LocalDate date) {
        this.rating = rating;
        this.review = review;
        this.memo = memo;
        this.date = date;
    }

    @NonNull
    @Override
    public String toString() {
        return "Summary{" +
                "summaryId=" + summaryId +
                ", rating=" + rating +
                ", review='" + review + '\'' +
                ", memo='" + memo + '\'' +
                ", date=" + date +
                '}';
    }
}
