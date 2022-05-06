package com.cyberlight.perfect.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.cyberlight.perfect.constant.SettingConstants;

/**
 * 以SharedPreferences实现的设置访问类
 */
public class SharedPrefSettingManager implements SettingManager {

    private static final SharedPrefSettingManager INSTANCE = new SharedPrefSettingManager();

    private SharedPreferences mSharedPreferences;

    private SharedPrefSettingManager() {

    }

    public static SharedPrefSettingManager getInstance(Context context) {
        if (INSTANCE.mSharedPreferences == null) {
            INSTANCE.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                    context.getApplicationContext());
        }
        return INSTANCE;
    }

    @Override
    public long getFocusDuration() {
        String str = mSharedPreferences.getString(
                SettingConstants.KEY_FOCUS_DURATION, SettingConstants.DEFAULT_FOCUS_DURATION);
        return Long.parseLong(str);
    }

    @Override
    public void setFocusDuration(long focusDuration) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(SettingConstants.KEY_FOCUS_DURATION, String.valueOf(focusDuration));
        editor.apply();
    }

    @Override
    public boolean getSound() {
        return mSharedPreferences.getBoolean(
                SettingConstants.KEY_SOUND, SettingConstants.DEFAULT_SOUND);
    }

    @Override
    public void setSound(boolean sound) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(SettingConstants.KEY_SOUND, sound);
        editor.apply();
    }

    @Override
    public boolean getVibration() {
        return mSharedPreferences.getBoolean(
                SettingConstants.KEY_VIBRATION, SettingConstants.DEFAULT_VIBRATION);
    }

    @Override
    public void setVibration(boolean vibration) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(SettingConstants.KEY_VIBRATION, vibration);
        editor.apply();
    }

    @Override
    public boolean getFlashlight() {
        return mSharedPreferences.getBoolean(
                SettingConstants.KEY_FLASHLIGHT, SettingConstants.DEFAULT_FLASHLIGHT);
    }

    @Override
    public void setFlashlight(boolean flashlight) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(SettingConstants.KEY_FLASHLIGHT, flashlight);
        editor.apply();
    }

    @Override
    public boolean getStrictTime() {
        return mSharedPreferences.getBoolean(
                SettingConstants.KEY_STRICT_TIME, SettingConstants.DEFAULT_STRICT_TIME);
    }

    @Override
    public void setStrictTime(boolean strictTime) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(SettingConstants.KEY_STRICT_TIME, strictTime);
        editor.apply();
    }

    @Override
    public boolean getKeepScreenOn() {
        return mSharedPreferences.getBoolean(
                SettingConstants.KEY_KEEP_SCREEN_ON, SettingConstants.DEFAULT_KEEP_SCREEN_ON);
    }

    @Override
    public void setKeepScreenOn(boolean keepScreenOn) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(SettingConstants.KEY_KEEP_SCREEN_ON, keepScreenOn);
        editor.apply();
    }

    @Override
    public boolean getManageBedtime() {
        return mSharedPreferences.getBoolean(
                SettingConstants.KEY_MANAGE_BEDTIME, SettingConstants.DEFAULT_MANAGE_BEDTIME);
    }

    @Override
    public void setManageBedtime(boolean manageBedtime) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(SettingConstants.KEY_MANAGE_BEDTIME, manageBedtime);
        editor.apply();
    }

    @Override
    public long getWakeUp() {
        return mSharedPreferences.getLong(
                SettingConstants.KEY_WAKE_UP, SettingConstants.DEFAULT_WAKE_UP);
    }

    @Override
    public void setWakeUp(long wakeUp) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putLong(SettingConstants.KEY_WAKE_UP, wakeUp);
        editor.apply();
    }

    @Override
    public long getFallAsleep() {
        return mSharedPreferences.getLong(
                SettingConstants.KEY_FALL_ASLEEP, SettingConstants.DEFAULT_FALL_ASLEEP);
    }

    @Override
    public void setFallAsleep(long fallAsleep) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putLong(SettingConstants.KEY_FALL_ASLEEP, fallAsleep);
        editor.apply();
    }
}
