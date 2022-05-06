package com.cyberlight.perfect.application;

import android.app.Application;
import android.content.Context;

import com.cyberlight.perfect.receiver.EventReminderReceiver;
import com.cyberlight.perfect.service.BedtimeAlarmService;
import com.cyberlight.perfect.service.FocusService;
import com.cyberlight.perfect.util.NotificationUtil;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext();
        // 创建应用所需的所有Notification Channel
        NotificationUtil.createNotificationChannel(context,
                FocusService.FOCUS_CHANNEL_ID,
                FocusService.FOCUS_CHANNEL_NAME,
                FocusService.FOCUS_CHANNEL_IMPORTANCE);
        NotificationUtil.createNotificationChannel(context,
                BedtimeAlarmService.BEDTIME_CHANNEL_ID,
                BedtimeAlarmService.BEDTIME_CHANNEL_NAME,
                BedtimeAlarmService.BEDTIME_CHANNEL_IMPORTANCE);
        NotificationUtil.createNotificationChannel(context,
                EventReminderReceiver.EVENT_CHANNEL_ID,
                EventReminderReceiver.EVENT_CHANNEL_NAME,
                EventReminderReceiver.EVENT_CHANNEL_IMPORTANCE);
    }

}
