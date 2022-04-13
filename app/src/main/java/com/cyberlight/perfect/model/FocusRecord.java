package com.cyberlight.perfect.model;

import androidx.annotation.NonNull;

public class FocusRecord {
    public final long completionTime;
    public final long focusDuration;

    public FocusRecord(long completionTime, long focusDuration) {
        this.completionTime = completionTime;
        this.focusDuration = focusDuration;
    }

    @NonNull
    @Override
    public String toString() {
        return "FocusRecords{" +
                "completionTime=" + completionTime +
                ", focusDuration=" + focusDuration +
                '}';
    }
}
