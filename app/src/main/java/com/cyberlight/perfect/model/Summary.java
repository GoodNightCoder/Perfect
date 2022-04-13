package com.cyberlight.perfect.model;

import androidx.annotation.NonNull;

public class Summary {
    public final int summaryId;
    public final int rating;
    public final String comment;
    public final String memo;
    public final String dateStr;

    public Summary(int summaryId, int rating, String comment, String memo, String dateStr) {
        this.summaryId = summaryId;
        this.rating = rating;
        this.comment = comment;
        this.memo = memo;
        this.dateStr = dateStr;
    }

    @NonNull
    @Override
    public String toString() {
        return "Summary{" +
                "summaryId=" + summaryId +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", memo='" + memo + '\'' +
                ", dateStr='" + dateStr + '\'' +
                '}';
    }
}
