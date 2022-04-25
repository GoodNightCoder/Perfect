package com.cyberlight.perfect.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;

import androidx.core.app.NotificationManagerCompat;

import com.cyberlight.perfect.R;

public class NotificationUtil {

    public static void createNotificationChannel(Context context,
                                                 String channelId,
                                                 CharSequence channelName,
                                                 int channelImportance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, channelImportance);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public static Notification buildNotification(Context context,
                                                 String channelId,
                                                 CharSequence title,
                                                 CharSequence text,
                                                 PendingIntent pendingIntent,
                                                 boolean autoCancel,
                                                 boolean onlyAlertOnce) {
        return new Notification.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setAutoCancel(autoCancel)
                .setOnlyAlertOnce(onlyAlertOnce)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
    }

    public static void showNotification(Context context, int notificationId, Notification notification) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(notificationId, notification);
    }
}
