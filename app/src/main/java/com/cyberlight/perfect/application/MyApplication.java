package com.cyberlight.perfect.application;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import com.cyberlight.perfect.receiver.EventReminderReceiver;
import com.cyberlight.perfect.service.BedtimeAlarmService;
import com.cyberlight.perfect.service.FocusService;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext();
        createNotificationChannel(context, FocusService.FOCUS_CHANNEL_ID, FocusService.FOCUS_CHANNEL_NAME, FocusService.FOCUS_CHANNEL_IMPORTANCE);
        createNotificationChannel(context, BedtimeAlarmService.BEDTIME_CHANNEL_ID, BedtimeAlarmService.BEDTIME_CHANNEL_NAME, BedtimeAlarmService.BEDTIME_CHANNEL_IMPORTANCE);
        createNotificationChannel(context, EventReminderReceiver.EVENT_CHANNEL_ID, EventReminderReceiver.EVENT_CHANNEL_NAME, EventReminderReceiver.EVENT_CHANNEL_IMPORTANCE);
    }

    private static void createNotificationChannel(Context context, String channelId, CharSequence channelName, int channelImportance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, channelImportance);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}
