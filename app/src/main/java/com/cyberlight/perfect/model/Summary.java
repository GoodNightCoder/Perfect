package com.cyberlight.perfect.model;

import androidx.annotation.NonNull;

public class Summary {
    public final int summaryId;
    public final int rating;
    public final String review;
    public final String memo;
    public final String dateStr;

    public Summary(int summaryId, int rating, String review, String memo, String dateStr) {
        this.summaryId = summaryId;
        this.rating = rating;
        this.review = review;
        this.memo = memo;
        this.dateStr = dateStr;
    }

    @NonNull
    @Override
    public String toString() {
        return "Summary{" +
                "summaryId=" + summaryId +
                ", rating=" + rating +
                ", review='" + review + '\'' +
                ", memo='" + memo + '\'' +
                ", dateStr='" + dateStr + '\'' +
                '}';
    }
}
