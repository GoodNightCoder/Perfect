package com.cyberlight.perfect.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import com.cyberlight.perfect.R;

public class BedtimeAlarmService extends Service {

    private static final CharSequence CHANNEL_NAME = "Bedtime notifications";
    private static final String CHANNEL_ID = "bedtime_channel";
    private static final int CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_DEFAULT;
    private static final int NOTIFICATION_ID = 12;

    private boolean mRunning = false;

    public BedtimeAlarmService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mRunning) {
            mRunning = true;
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.wake_up_alarm_music);
            mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        }
        return super.onStartCommand(intent, flags, startId);
    }
}