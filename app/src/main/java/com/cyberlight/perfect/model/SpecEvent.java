package com.cyberlight.perfect.model;

import android.content.Context;

import androidx.annotation.NonNull;

import com.cyberlight.perfect.util.DateTimeFormatUtil;

public class SpecEvent extends Event {
    public final long specStart;
    public final int occurNum;

    public SpecEvent(Event event, long specStart, int occurNum) {
        super(event.eventId, event.title, event.start, event.duration, event.interval);
        this.specStart = specStart;
        this.occurNum = occurNum;
    }

    @NonNull
    @Override
    public String toString() {
        return "SpecEvent{" +
                "eventId=" + eventId +
                ", title='" + title + '\'' +
                ", start=" + start +
                ", duration=" + duration +
                ", interval=" + interval +
                ", specStart=" + specStart +
                ", occurNum=" + occurNum +
                '}';
    }

    public String toTimeString(Context context) {
        return DateTimeFormatUtil.getReadableDateHourMinute(context, specStart) + " ~ " +
                DateTimeFormatUtil.getReadableDateHourMinute(context, specStart + duration);
    }
}
