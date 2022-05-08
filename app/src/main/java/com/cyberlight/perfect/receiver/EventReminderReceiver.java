package com.cyberlight.perfect.receiver;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.cyberlight.perfect.R;
import com.cyberlight.perfect.model.Event;
import com.cyberlight.perfect.model.SpecEvent;
import com.cyberlight.perfect.ui.MainActivity;
import com.cyberlight.perfect.util.DbUtil;
import com.cyberlight.perfect.util.NotificationUtil;

import java.util.List;

@SuppressLint("UnspecifiedImmutableFlag")
public class EventReminderReceiver extends BroadcastReceiver {
    public static final CharSequence EVENT_CHANNEL_NAME = "Event notifications";
    public static final String EVENT_CHANNEL_ID = "event_channel";
    public static final int EVENT_CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_HIGH;

    private static final int EVENT_NOTIFICATION_ID = 83;
    private static final int EVENT_NOTIFICATION_REQUEST_CODE = 883;

    private static final int EVENT_ALARM_REQUEST_CODE = 8881;

    private static final String EXTRA_EVENT_TITLE = "extra_event_title";
    private static final String EXTRA_EVENT_TIME = "extra_event_time";

    private static final String EVENT_REMINDER_ACTION = "event_reminder_action";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 发送事件通知
        String eventTitle = intent.getStringExtra(EXTRA_EVENT_TITLE);
        String eventTime = intent.getStringExtra(EXTRA_EVENT_TIME);
        if (!TextUtils.isEmpty(eventTitle) && !TextUtils.isEmpty(eventTime)) {
            Intent ni = new Intent(context, MainActivity.class);
            PendingIntent npi = PendingIntent.getActivity(context,
                    EVENT_NOTIFICATION_REQUEST_CODE,
                    ni,
                    PendingIntent.FLAG_IMMUTABLE);
            Notification notification = NotificationUtil.buildNotification(context,
                    EVENT_CHANNEL_ID,
                    eventTitle,
                    eventTime,
                    npi,
                    true,
                    false);
            NotificationUtil.showNotification(context, EVENT_NOTIFICATION_ID, notification);
        }
        // 安排下一次事件提醒
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
                EVENT_ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent == null) {
            // 启动事件提醒
            boolean firstSet = setNextReminder(context);
            if (firstSet) {
                // 发通知提醒用户事件提醒已启动
                Intent ni = new Intent(context, MainActivity.class);
                PendingIntent npi = PendingIntent.getActivity(context,
                        EVENT_NOTIFICATION_REQUEST_CODE,
                        ni,
                        PendingIntent.FLAG_IMMUTABLE);
                Notification notification = NotificationUtil.buildNotification(context,
                        EVENT_CHANNEL_ID,
                        context.getText(R.string.event_notification_reminder_activated_title),
                        context.getText(R.string.event_notification_reminder_activated_text),
                        npi,
                        true,
                        false);
                NotificationUtil.showNotification(context, EVENT_NOTIFICATION_ID, notification);
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
        Intent intent = new Intent(context, EventReminderReceiver.class);
        intent.setAction(EVENT_REMINDER_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                EVENT_ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE);
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
        List<Event> events = DbUtil.getEvents(context);
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
    private static void setReminder(Context context,
                                    String eventTitle,
                                    String eventTime,
                                    long triggerAtMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, EventReminderReceiver.class);
        intent.putExtra(EXTRA_EVENT_TITLE, eventTitle);
        intent.putExtra(EXTRA_EVENT_TIME, eventTime);
        intent.setAction(EVENT_REMINDER_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                EVENT_ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
    }

}