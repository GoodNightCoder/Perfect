package com.cyberlight.perfect.receiver;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.cyberlight.perfect.R;
import com.cyberlight.perfect.model.Event;
import com.cyberlight.perfect.model.SpecEvent;
import com.cyberlight.perfect.ui.MainActivity;
import com.cyberlight.perfect.util.DbUtil;

import java.util.List;

@SuppressLint("UnspecifiedImmutableFlag")
public class EventReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "EventReminderReceiver";

    private static final CharSequence CHANNEL_NAME = "Event notifications";
    private static final String CHANNEL_ID = "event_channel";
    private static final int CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_HIGH;
    private static final int NOTIFICATION_ID = 10;

    public static final String IS_FIRST_REMIND_EXTRA_KEY = "first_remind";
    public static final String EVENT_REMINDER_ACTION = "event_reminder_action";
    public static final int EVENT_REMINDER_REQUEST_CODE = 6;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"收到事件提醒广播");
        List<Event> events = DbUtil.getDbEvents(context);
        if (events.size() > 0) {
            // create notification channel
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME, CHANNEL_IMPORTANCE);
            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            // create notification manager
            NotificationManagerCompat notificationManagerCompat =
                    NotificationManagerCompat.from(context);
            // 如果是第一次启动事件提醒，发送通知告知用户
            boolean isFirstRemind = intent.getBooleanExtra(IS_FIRST_REMIND_EXTRA_KEY, true);
            if (isFirstRemind) {
                // build notification
                Intent notificationIntent = new Intent(context, MainActivity.class);
                PendingIntent pendingIntent =
                        PendingIntent.getActivity(context, 0, notificationIntent, 0);
                Notification notification = new Notification.Builder(context,
                        EventReminderReceiver.CHANNEL_ID)
                        .setContentTitle(context.getString(R.string.event_reminder_activated_title))
                        .setContentText(context.getString(R.string.event_reminder_activated_text))
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setOnlyAlertOnce(false)
                        .build();
                // show notification
                notificationManagerCompat.notify(NOTIFICATION_ID, notification);
            }
            long curTimeMillis = System.currentTimeMillis();
            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                SpecEvent specEvent = event.getOnGoingSpecEvent(curTimeMillis);
                if (specEvent != null) {//有事件正在进行，发通知提醒
                    // build notification
                    Intent notificationIntent = new Intent(context, MainActivity.class);
                    PendingIntent pendingIntent =
                            PendingIntent.getActivity(context, 0, notificationIntent, 0);
                    Notification notification = new Notification.Builder(context,
                            EventReminderReceiver.CHANNEL_ID)
                            .setContentTitle(specEvent.title)
                            .setContentText(specEvent.toTimeString())
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true)
                            .setOnlyAlertOnce(false)
                            .build();
                    // show notification
                    notificationManagerCompat.notify(NOTIFICATION_ID, notification);
                    break;
                }
            }
            // 判断即将发生的事件并添加定时提醒
            Event nextEvent = events.get(0);
            long nextStart = nextEvent.start +
                    nextEvent.getEventOccurNum(curTimeMillis) * nextEvent.interval;
            for (int i = 1; i < events.size(); i++) {
                Event event = events.get(i);
                long nextStart_ = event.start +
                        event.getEventOccurNum(curTimeMillis) * event.interval;
                if (nextStart_ < nextStart) {
                    nextEvent = event;
                    nextStart = nextStart_;
                }
            }
            AlarmManager alarmManager =
                    (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent nextAlarmIntent = new Intent(context, EventReminderReceiver.class);
            nextAlarmIntent.putExtra(IS_FIRST_REMIND_EXTRA_KEY, false);
            nextAlarmIntent.setAction(EVENT_REMINDER_ACTION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    EVENT_REMINDER_REQUEST_CODE,
                    nextAlarmIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT
            );
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, nextStart, pendingIntent);
        }

    }
}
