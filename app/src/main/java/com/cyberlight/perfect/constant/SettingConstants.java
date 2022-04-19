package com.cyberlight.perfect.constant;

public final class SettingConstants {
    public static final long[] FOCUS_DURATION_VALUES = {1200000, 1500000, 2700000, 3000000};
    //各设置对应的键
    public static final String KEY_FOCUS_DURATION = "focus_duration";
    public static final String KEY_SOUND = "sound";
    public static final String KEY_VIBRATION = "vibration";
    public static final String KEY_FLASHLIGHT = "flashlight";
    public static final String KEY_STRICT_TIME = "strict_time";
    public static final String KEY_KEEP_SCREEN_ON = "keep_screen_on";
    public static final String KEY_CLEAR_DATA = "clear_data";
    public static final String KEY_RESET_ALL_SETTINGS = "reset_all_settings";
    public static final String KEY_MANAGE_BEDTIME = "manage_bedtime";
    public static final String KEY_WAKE_UP = "wake_up";
    public static final String KEY_FALL_ASLEEP = "fall_asleep";
    public static final String KEY_IGNORE_BATTERY_OPTIMIZATION = "ignore_battery_optimization";
    public static final String KEY_MANAGE_STARTUP_APPS = "manage_startup_apps";
    //设置的默认值
    public static final String DEFAULT_FOCUS_DURATION_VALUE = "25min";
    public static final boolean DEFAULT_SOUND_VALUE = true;
    public static final boolean DEFAULT_VIBRATION_VALUE = true;
    public static final boolean DEFAULT_FLASHLIGHT_VALUE = true;
    public static final boolean DEFAULT_STRICT_TIME_VALUE = true;
    public static final boolean DEFAULT_KEEP_SCREEN_ON_VALUE = true;
    public static final boolean DEFAULT_MANAGE_BEDTIME_VALUE = false;
    public static final int DEFAULT_WAKE_UP_VALUE = 25200;
    public static final int DEFAULT_FALL_ASLEEP_VALUE = 82800;
}
