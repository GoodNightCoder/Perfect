package com.cyberlight.perfect.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cyberlight.perfect.service.BedtimeAlarmService;
import com.cyberlight.perfect.util.SettingManager;
import com.cyberlight.perfect.util.SharedPrefSettingManager;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // 检查事件提醒是否启动
            EventReminderReceiver.activateReminder(context, true);
            // 检查闹钟是否启动
            SettingManager settingManager = SharedPrefSettingManager.getInstance(context);
            boolean manageBedtime = settingManager.getManageBedtime();
            if (manageBedtime) {
                BedtimeAlarmService.activateAlarm(context, true);
            }
        }
    }
}
