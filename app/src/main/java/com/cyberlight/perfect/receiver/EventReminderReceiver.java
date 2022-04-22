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

import androidx.core.app.NotificationManagerCompat;

import com.cyberlight.perfect.R;
import com.cyberlight.perfect.model.Event;
import com.cyberlight.perfect.model.SpecEvent;
import com.cyberlight.perfect.ui.MainActivity;
import com.cyberlight.perfect.util.DbUtil;

import java.util.List;

@SuppressLint("UnspecifiedImmutableFlag")
public class EventReminderReceiver extends BroadcastReceiver {

    public static final CharSequence EVENT_CHANNEL_NAME = "Event notifications";
    public static final String EVENT_CHANNEL_ID = "event_channel";
    public static final int EVENT_CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_HIGH;

    private static final int NOTIFICATION_ID = 10;

    private static final String EXTRA_EVENT_TITLE = "extra_event_title";
    private static final String EXTRA_EVENT_TIME = "extra_event_time";

    private static final String EVENT_REMINDER_ACTION = "event_reminder_action";
    private static final int EVENT_REMINDER_REQUEST_CODE = 6;

    @Override
    public void onReceive(Context context, Intent intent) {
        String eventTitle = intent.getStringExtra(EXTRA_EVENT_TITLE);
        String eventTime = intent.getStringExtra(EXTRA_EVENT_TIME);
        Notification notification = buildNotification(context, eventTitle, eventTime);
        showNotification(context, notification);
        setNextReminder(context);
    }

    /**
     * 检查事件提醒是否启动，如果没有则启动，
     * 如果已启动则根据需要更新定时任务
     *
     * @param context 可用Context对象
     * @param update  若已启动，是否需要更新任务
     */
    public static void activateReminder(Context context, boolean update) {
        Intent intent = new Intent(context, EventReminderReceiver.class);
        intent.setAction(EVENT_REMINDER_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                EVENT_REMINDER_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent == null) {
            // 启动事件提醒
            boolean firstSet = setNextReminder(context);
            if (firstSet) {
                Notification notification = buildNotification(context, context.getString(R.string.event_reminder_activated_title), context.getString(R.string.event_reminder_activated_text));
                showNotification(context, notification);
            }
        } else if (update) {
            // 更新事件提醒
            setNextReminder(context);
        }
    }

    /**
     * 取消事件提醒
     *
     * @param context 可用的Context对象
     */
    public static void cancelReminder(Context context) {
        Intent remindIntent = new Intent(context, EventReminderReceiver.class);
        remindIntent.setAction(EVENT_REMINDER_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, EVENT_REMINDER_REQUEST_CODE, remindIntent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    /**
     * 判断下次事件并为其添加事件提醒定时任务
     *
     * @param context 可用Context对象
     * @return 是否存在下次事件
     */
    private static boolean setNextReminder(Context context) {
        List<Event> events = DbUtil.getDbEvents(context);
        if (events.size() > 0) {
            long curTimeMillis = System.currentTimeMillis();
            SpecEvent next = events.get(0).getNextSpecEvent(curTimeMillis);
            for (int i = 1; i < events.size(); i++) {
                SpecEvent tmp = events.get(i).getNextSpecEvent(curTimeMillis);
                if (tmp.specStart < next.specStart) {
                    next = tmp;
                }
            }
            setReminder(context, next.title, next.toTimeString(context), next.specStart);
            return true;
        }
        return false;
    }

    /**
     * 设置事件提醒定时任务
     *
     * @param context         可用Context对象
     * @param eventTitle      事件标题
     * @param eventTime       事件时间信息
     * @param triggerAtMillis 提醒触发时间
     */
    private static void setReminder(Context context, String eventTitle, String eventTime, long triggerAtMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, EventReminderReceiver.class);
        intent.putExtra(EXTRA_EVENT_TITLE, eventTitle);
        intent.putExtra(EXTRA_EVENT_TIME, eventTime);
        intent.setAction(EVENT_REMINDER_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, EVENT_REMINDER_REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
    }

    private static Notification buildNotification(Context context, String title, String text) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        return new Notification.Builder(context,
                EventReminderReceiver.EVENT_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(false)
                .build();
    }

    private static void showNotification(Context context, Notification notification) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(NOTIFICATION_ID, notification);
    }

}
