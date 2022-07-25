package com.cyberlight.perfect.data;

import android.content.Context;

import com.cyberlight.perfect.data.Event;
import com.cyberlight.perfect.util.DateTimeFormatUtil;

public class SpecificEvent {
    public final Event event;
    public final long specStart;
    public final int occurNum;

    public SpecificEvent(Event event, long specStart, int occurNum) {
        this.event = event;
        this.specStart = specStart;
        this.occurNum = occurNum;
    }

    public String toTimeString(Context context) {
        return DateTimeFormatUtil.getReadableDateHourMinute(context, specStart) + " ~ " +
                DateTimeFormatUtil.getReadableDateHourMinute(context, specStart + event.duration);
    }
}
