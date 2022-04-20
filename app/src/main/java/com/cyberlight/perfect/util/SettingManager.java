package com.cyberlight.perfect.util;

/**
 * 设置访问接口
 */
public interface SettingManager {

    long getFocusDuration();

    void setFocusDuration(long focusDuration);

    boolean getSound();

    void setSound(boolean sound);

    boolean getVibration();

    void setVibration(boolean vibration);

    boolean getFlashlight();

    void setFlashlight(boolean flashlight);

    boolean getStrictTime();

    void setStrictTime(boolean strictTime);

    boolean getKeepScreenOn();

    void setKeepScreenOn(boolean keepScreenOn);

    boolean getManageBedtime();

    void setManageBedtime(boolean manageBedtime);

    long getWakeUp();

    void setWakeUp(long wakeUp);

    long getFallAsleep();

    void setFallAsleep(long fallAsleep);
}
