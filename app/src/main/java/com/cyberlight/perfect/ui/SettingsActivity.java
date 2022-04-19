package com.cyberlight.perfect.ui;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.cyberlight.perfect.R;
import com.cyberlight.perfect.constant.SettingConstants;
import com.cyberlight.perfect.util.DateTimeFormatUtil;
import com.cyberlight.perfect.util.DbContract;
import com.cyberlight.perfect.util.DbUtil;
import com.cyberlight.perfect.util.ToastUtil;

import java.time.LocalTime;

@SuppressLint("BatteryLife")
public class SettingsActivity extends AppCompatActivity {

    private static final String RESET_ALL_SETTINGS_REQUEST_KEY = "reset_all_settings_request_key";
    private static final String PICK_WAKE_UP_REQUEST_KEY = "pick_wake_up_request_key";
    private static final String PICK_FALL_ASLEEP_REQUEST_KEY = "pick_fall_asleep_request_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        //对自定义返回键设置监听
        ImageView mBackIv = findViewById(R.id.settings_back_iv);
        mBackIv.setOnClickListener(v -> finish());
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            // 获取并检查context
            Context context = getContext();
            if (context == null)
                return;
            // 获取SharedPreferences
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(context);
            // 获取各个Preference
            ListPreference focusDurationPref = findPreference(SettingConstants.KEY_FOCUS_DURATION);
            SwitchPreferenceCompat soundPref = findPreference(SettingConstants.KEY_SOUND);
            SwitchPreferenceCompat vibrationPref = findPreference(SettingConstants.KEY_VIBRATION);
            SwitchPreferenceCompat flashlightPref = findPreference(SettingConstants.KEY_FLASHLIGHT);
            SwitchPreferenceCompat strictTimePref = findPreference(SettingConstants.KEY_STRICT_TIME);
            SwitchPreferenceCompat keepScreenOnPref = findPreference(SettingConstants.KEY_KEEP_SCREEN_ON);
            SwitchPreferenceCompat manageBedtimePref = findPreference(SettingConstants.KEY_MANAGE_BEDTIME);
            Preference wakeUpPref = findPreference(SettingConstants.KEY_WAKE_UP);
            Preference fallAsleepPref = findPreference(SettingConstants.KEY_FALL_ASLEEP);
            Preference clearDataPref = findPreference(SettingConstants.KEY_CLEAR_DATA);
            Preference resetPref = findPreference(SettingConstants.KEY_RESET_ALL_SETTINGS);
            Preference ignoreBatteryOptimizationPref = findPreference(SettingConstants.KEY_IGNORE_BATTERY_OPTIMIZATION);
            Preference manageStartupAppsPref = findPreference(SettingConstants.KEY_MANAGE_STARTUP_APPS);
            // 检查各个Preference是否存在
            if (focusDurationPref == null || soundPref == null || vibrationPref == null ||
                    flashlightPref == null || strictTimePref == null || keepScreenOnPref == null ||
                    manageBedtimePref == null || wakeUpPref == null || fallAsleepPref == null ||
                    clearDataPref == null || resetPref == null ||
                    ignoreBatteryOptimizationPref == null || manageStartupAppsPref == null)
                return;
            // 设置wakeUpPref和fallAsleepPref的Summary
            long wakeUpSecs = sharedPreferences.getInt(SettingConstants.KEY_WAKE_UP,
                    SettingConstants.DEFAULT_WAKE_UP_VALUE);
            wakeUpPref.setSummary(DateTimeFormatUtil.getNeatHourMinute(wakeUpSecs));
            long fallAsleepSecs = sharedPreferences.getInt(SettingConstants.KEY_FALL_ASLEEP,
                    SettingConstants.DEFAULT_FALL_ASLEEP_VALUE);
            fallAsleepPref.setSummary(DateTimeFormatUtil.getNeatHourMinute(fallAsleepSecs));
            // 设置几个自定义Preference的点击监听
            FragmentManager fragmentManager = getChildFragmentManager();
            wakeUpPref.setOnPreferenceClickListener(preference -> {
                if (fragmentManager.findFragmentByTag(HourMinutePickerDialogFragment.TAG) == null) {
                    int secs = sharedPreferences.getInt(
                            SettingConstants.KEY_WAKE_UP,
                            SettingConstants.DEFAULT_WAKE_UP_VALUE);
                    DialogFragment dialogFragment =
                            HourMinutePickerDialogFragment.newInstance(PICK_WAKE_UP_REQUEST_KEY,
                                    secs / 3600, secs % 3600 / 60);
                    dialogFragment.show(fragmentManager, HourMinutePickerDialogFragment.TAG);
                }
                return true;
            });
            fallAsleepPref.setOnPreferenceClickListener(preference -> {
                if (fragmentManager.findFragmentByTag(HourMinutePickerDialogFragment.TAG) == null) {
                    int secs = sharedPreferences.getInt(
                            SettingConstants.KEY_FALL_ASLEEP,
                            SettingConstants.DEFAULT_FALL_ASLEEP_VALUE);
                    DialogFragment dialogFragment =
                            HourMinutePickerDialogFragment.newInstance(PICK_FALL_ASLEEP_REQUEST_KEY,
                                    secs / 3600, secs % 3600 / 60);
                    dialogFragment.show(fragmentManager, HourMinutePickerDialogFragment.TAG);
                }
                return true;
            });
            clearDataPref.setOnPreferenceClickListener(preference -> {
                if (fragmentManager.findFragmentByTag(ClearDialogFragment.TAG) == null) {
                    DialogFragment dialogFragment = new ClearDialogFragment();
                    dialogFragment.show(fragmentManager, ClearDialogFragment.TAG);
                }
                return true;
            });
            resetPref.setOnPreferenceClickListener(preference -> {
                if (fragmentManager.findFragmentByTag(ConfirmDialogFragment.TAG) == null) {
                    DialogFragment dialogFragment = ConfirmDialogFragment.newInstance(
                            RESET_ALL_SETTINGS_REQUEST_KEY,
                            getString(R.string.settings_reset_confirm_dialog_title),
                            getString(R.string.settings_reset_confirm_dialog_content),
                            getString(R.string.dialog_btn_yes),
                            getString(R.string.dialog_btn_no)
                    );
                    dialogFragment.show(fragmentManager, ConfirmDialogFragment.TAG);
                }
                return true;
            });
            ignoreBatteryOptimizationPref.setOnPreferenceClickListener(preference -> {
                PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
                if (!powerManager.isIgnoringBatteryOptimizations(context.getPackageName())) {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        ToastUtil.showToast(context,
                                R.string.no_matching_activity_toast,
                                Toast.LENGTH_SHORT);
                    }
                }
                return true;
            });
            manageStartupAppsPref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    ToastUtil.showToast(context,
                            R.string.no_matching_activity_toast,
                            Toast.LENGTH_SHORT);
                }
                return true;
            });
            fragmentManager.setFragmentResultListener(RESET_ALL_SETTINGS_REQUEST_KEY,
                    this, (requestKey, result) -> {
                        if (result.getInt(ConfirmDialogFragment.CONFIRM_WHICH_KEY) ==
                                ConfirmDialogFragment.CONFIRM_POSITIVE) {
                            // 恢复默认设置
                            focusDurationPref.setValue(SettingConstants.DEFAULT_FOCUS_DURATION_VALUE);
                            soundPref.setChecked(SettingConstants.DEFAULT_SOUND_VALUE);
                            vibrationPref.setChecked(SettingConstants.DEFAULT_VIBRATION_VALUE);
                            flashlightPref.setChecked(SettingConstants.DEFAULT_FLASHLIGHT_VALUE);
                            strictTimePref.setChecked(SettingConstants.DEFAULT_STRICT_TIME_VALUE);
                            keepScreenOnPref.setChecked(SettingConstants.DEFAULT_KEEP_SCREEN_ON_VALUE);
                            manageBedtimePref.setChecked(SettingConstants.DEFAULT_MANAGE_BEDTIME_VALUE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            // 恢复默认起床、睡觉时间
                            editor.putInt(SettingConstants.KEY_WAKE_UP,
                                    SettingConstants.DEFAULT_WAKE_UP_VALUE);
                            editor.putInt(SettingConstants.KEY_FALL_ASLEEP,
                                    SettingConstants.DEFAULT_FALL_ASLEEP_VALUE);
                            editor.apply();
                            wakeUpPref.setSummary(DateTimeFormatUtil.getNeatHourMinute(
                                    SettingConstants.DEFAULT_WAKE_UP_VALUE));
                            fallAsleepPref.setSummary(DateTimeFormatUtil.getNeatHourMinute(
                                    SettingConstants.DEFAULT_FALL_ASLEEP_VALUE));
                        }
                    });
            fragmentManager.setFragmentResultListener(PICK_WAKE_UP_REQUEST_KEY,
                    this, (requestKey, result) -> {
                        int hour = result.getInt(HourMinutePickerDialogFragment.HM_HOUR_KEY);
                        int minute = result.getInt(HourMinutePickerDialogFragment.HM_MINUTE_KEY);
                        int newSecs = hour * 3600 + minute * 60;
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(SettingConstants.KEY_WAKE_UP, newSecs);
                        editor.apply();
                        wakeUpPref.setSummary(DateTimeFormatUtil.getNeatHourMinute(newSecs));
                    });
            fragmentManager.setFragmentResultListener(PICK_FALL_ASLEEP_REQUEST_KEY,
                    this, (requestKey, result) -> {
                        int hour = result.getInt(HourMinutePickerDialogFragment.HM_HOUR_KEY);
                        int minute = result.getInt(HourMinutePickerDialogFragment.HM_MINUTE_KEY);
                        int newSecs = hour * 3600 + minute * 60;
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(SettingConstants.KEY_FALL_ASLEEP, newSecs);
                        editor.apply();
                        fallAsleepPref.setSummary(DateTimeFormatUtil.getNeatHourMinute(newSecs));
                    });
        }
    }
}