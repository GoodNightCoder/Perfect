package com.cyberlight.perfect.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.IntDef;

import com.cyberlight.perfect.R;
import com.cyberlight.perfect.util.DateTimeFormatUtil;
import com.cyberlight.perfect.util.SettingManager;
import com.cyberlight.perfect.util.SharedPrefSettingManager;
import com.cyberlight.perfect.util.ToastUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

@SuppressLint("UnspecifiedImmutableFlag")
public class BedtimeAlarmService extends Service {

    private static final CharSequence CHANNEL_NAME = "Bedtime notifications";
    private static final String CHANNEL_ID = "bedtime_channel";
    private static final int CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_HIGH;
    private static final int NOTIFICATION_ID = 12;

    private static final String EXTRA_STOP = "stop_alarm";
    private static final String EXTRA_ALARM_TYPE = "alarm_type";

    @IntDef({TYPE_WAKE_UP, TYPE_FALL_ASLEEP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AlarmType {
    }

    private static final int TYPE_WAKE_UP = 1;
    private static final int TYPE_FALL_ASLEEP = 2;

    private MediaPlayer mMediaPlayer;
    private AudioFocusRequest audioFocusRequest;
    private boolean mRunning = false;
    private int volumeToReset;

    public BedtimeAlarmService() {
    }

    @Override
    public void onCreate() {
        // 创建通知渠道
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME, CHANNEL_IMPORTANCE);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mRunning) {
            boolean stop = intent.getBooleanExtra(EXTRA_STOP, false);
            if (stop) stopSelf();
        } else {
            mRunning = true;
            int alarmType = intent.getIntExtra(EXTRA_ALARM_TYPE, 0);
            startAlarm(alarmType);
            // 设置下次闹钟
            activateBedtimeAlarm(this, true);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.isVolumeFixed()) {
            ToastUtil.showToast(this, R.string.bedtime_cannot_adjust_volume_toast, Toast.LENGTH_SHORT);
        }
        // 恢复原媒体音量
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeToReset, AudioManager.FLAG_SHOW_UI);
        // 释放AudioFocus，让其他音乐继续播放
        if (audioFocusRequest != null) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest);
            audioFocusRequest = null;
        }
        // MediaPlayer使用完必须释放
        if (mMediaPlayer != null)
            mMediaPlayer.release();
    }

    /**
     * 检查闹钟是否启动，如果没有则启动，
     * 如果已启动则根据需要更新闹钟
     *
     * @param context 可用Context对象
     * @param update  若已启动，是否需要更新闹钟
     */
    public static void activateBedtimeAlarm(Context context, boolean update) {
        Intent intent = new Intent(context, BedtimeAlarmService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 77,
                intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null && !update) {
            //TEST
            Log.d("perfect_debug", "闹钟已设且不需更新");
            return;
        }
        SettingManager settingManager = SharedPrefSettingManager.getInstance(context);
        // 计算下一次闹钟的触发时间
        long curSecs = LocalTime.now().toSecondOfDay();
        long wakeUpSecs = settingManager.getWakeUp();
        long fallAsleepSecs = settingManager.getFallAsleep();
        long small, big;
        boolean isWakeUpSmaller;
        if (wakeUpSecs < fallAsleepSecs) {
            isWakeUpSmaller = true;
            small = wakeUpSecs;
            big = fallAsleepSecs;
        } else {
            isWakeUpSmaller = false;
            small = fallAsleepSecs;
            big = wakeUpSecs;
        }
        int alarmType;
        long alarmTimeMillis;
        // 一天被两个闹钟分成三个时段：
        if (curSecs < small) {
            // 处于第一个时段，下次闹钟为今天更早的闹钟
            alarmTimeMillis = LocalTime.ofSecondOfDay(small).atDate(LocalDate.now())
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            alarmType = isWakeUpSmaller ? TYPE_WAKE_UP : TYPE_FALL_ASLEEP;
        } else if (curSecs < big) {
            // 处于第二个时段，下次闹钟为今天更晚的闹钟
            alarmTimeMillis = LocalTime.ofSecondOfDay(big).atDate(LocalDate.now())
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            alarmType = isWakeUpSmaller ? TYPE_FALL_ASLEEP : TYPE_WAKE_UP;
        } else {
            // 处于第三个时段，下次闹钟为明天更早的闹钟
            alarmTimeMillis = LocalTime.ofSecondOfDay(small).atDate(LocalDate.now().plusDays(1))
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            alarmType = isWakeUpSmaller ? TYPE_WAKE_UP : TYPE_FALL_ASLEEP;
        }
        // 设置闹钟
        //TEST
        Log.d("perfect_debug", "设置闹钟:" + DateTimeFormatUtil.getDateTimeForDebugging(alarmTimeMillis));
        intent.putExtra(EXTRA_ALARM_TYPE, alarmType);
        pendingIntent = PendingIntent.getService(context, 77,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntent);
    }

    /**
     * 取消闹钟
     *
     * @param context 可用Context对象
     */
    public static void cancelBedtimeAlarm(Context context) {
        Intent intent = new Intent(context, BedtimeAlarmService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 77,
                intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            // TEST
            Log.d("perfect_debug", "闹钟被取消");
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    private void startAlarm(@AlarmType int alarmType) {
        int resId;
        String title;
        switch (alarmType) {
            case TYPE_WAKE_UP:
                resId = R.raw.wake_up_alarm_music;
                title = getString(R.string.bedtime_notification_wake_up_title);
                break;
            case TYPE_FALL_ASLEEP:
                resId = R.raw.fall_asleep_alarm_music;
                title = getString(R.string.bedtime_notification_fall_asleep_title);
                break;
            default:
                return;
        }
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // 获取AudioFocus，暂停其他音乐播放
        audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).build();
        audioManager.requestAudioFocus(audioFocusRequest);
        // 创建MediaPlayer播放音乐
        mMediaPlayer = MediaPlayer.create(this, resId);
        mMediaPlayer.setOnCompletionListener(mp -> stopSelf());
        mMediaPlayer.start();
        if (audioManager.isVolumeFixed()) {
            // 无法调节设备音量，提醒用户
            ToastUtil.showToast(this, R.string.bedtime_cannot_adjust_volume_toast, Toast.LENGTH_SHORT);
        } else {
            // 记录当前的媒体音量大小，用于闹铃播放结束后恢复
            volumeToReset = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            // 调节媒体音量至一半大小
            int volumeToSet = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2;
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeToSet, AudioManager.FLAG_SHOW_UI);
        }
        // 创建停止闹钟的intent，使用户在轻触通知后停止闹钟
        Intent stopIntent = new Intent(this, BedtimeAlarmService.class);
        stopIntent.putExtra(EXTRA_STOP, true);
        PendingIntent stopPendingIntent =
                PendingIntent.getService(this, 7, stopIntent, 0);
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentTitle(title)
                .setContentText(getString(R.string.bedtime_notification_stop_alarm_text))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(stopPendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }
}