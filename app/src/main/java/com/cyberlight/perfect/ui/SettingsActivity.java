package com.cyberlight.perfect.ui;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.cyberlight.perfect.R;
import com.cyberlight.perfect.constant.SettingConstants;
import com.cyberlight.perfect.util.DbUtil;

@SuppressLint("BatteryLife")
public class SettingsActivity extends AppCompatActivity {

    private static final String RESET_ALL_SETTINGS_REQUEST_KEY = "reset_all_settings_request_key";
    private static final String CLEAR_ALL_DATA_REQUEST_KEY = "clear_all_data_request_key";

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

            // 获取各个Preference
            ListPreference focusDurationPref = findPreference(SettingConstants.KEY_FOCUS_DURATION);
            SwitchPreferenceCompat soundPref = findPreference(SettingConstants.KEY_SOUND);
            SwitchPreferenceCompat vibrationPref = findPreference(SettingConstants.KEY_VIBRATION);
            SwitchPreferenceCompat flashlightPref = findPreference(SettingConstants.KEY_FLASHLIGHT);
            SwitchPreferenceCompat strictTimePref = findPreference(SettingConstants.KEY_STRICT_TIME);
            SwitchPreferenceCompat keepScreenOnPref = findPreference(SettingConstants.KEY_KEEP_SCREEN_ON);
            Preference clearAllDataPref = findPreference("clear_all_data");
            Preference resetPref = findPreference("reset_all_settings");
            Preference ignoreBatteryOptimizationPref = findPreference("ignore_battery_optimization");
            Preference manageStartupAppsPref = findPreference("manage_startup_apps");

            // 检查各个Preference是否存在
            if (focusDurationPref == null || soundPref == null || vibrationPref == null ||
                    flashlightPref == null || strictTimePref == null || keepScreenOnPref == null ||
                    clearAllDataPref == null || resetPref == null
                    || ignoreBatteryOptimizationPref == null || manageStartupAppsPref == null)
                return;

            // 设置几个自定义Preference的点击监听
            FragmentManager fragmentManager = getChildFragmentManager();
            clearAllDataPref.setOnPreferenceClickListener(preference -> {
                if (fragmentManager.findFragmentByTag(ConfirmDialogFragment.TAG) == null) {
                    DialogFragment confirmDialogFragment = ConfirmDialogFragment.newInstance(
                            getString(R.string.settings_clear_all_data_confirm_dialog_title),
                            getString(R.string.settings_clear_all_data_confirm_dialog_content),
                            getString(R.string.dialog_btn_yes),
                            getString(R.string.dialog_btn_no),
                            CLEAR_ALL_DATA_REQUEST_KEY
                    );
                    confirmDialogFragment.show(fragmentManager, ConfirmDialogFragment.TAG);
                }
                return false;
            });
            resetPref.setOnPreferenceClickListener(preference -> {
                if (fragmentManager.findFragmentByTag(ConfirmDialogFragment.TAG) == null) {
                    DialogFragment confirmDialogFragment = ConfirmDialogFragment.newInstance(
                            getString(R.string.settings_reset_confirm_dialog_title),
                            getString(R.string.settings_reset_confirm_dialog_content),
                            getString(R.string.dialog_btn_yes),
                            getString(R.string.dialog_btn_no),
                            RESET_ALL_SETTINGS_REQUEST_KEY
                    );
                    confirmDialogFragment.show(fragmentManager, ConfirmDialogFragment.TAG);
                }
                return false;
            });
            ignoreBatteryOptimizationPref.setOnPreferenceClickListener(preference -> {
                PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
                if (!powerManager.isIgnoringBatteryOptimizations(context.getPackageName())) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                    startActivity(intent);
                }
                return false;
            });
            manageStartupAppsPref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent();
                ComponentName componentName = new ComponentName(
                        "com.android.settings",
                        "com.android.settings.Settings$AppAndNotificationDashboardActivity"
                );
                intent.setComponent(componentName);
                if (intent.resolveActivity(context.getPackageManager()) != null)
                    startActivity(intent);
                return false;
            });
            // 监听清空数据对话框和重置设置对话框的返回结果
            fragmentManager.setFragmentResultListener(CLEAR_ALL_DATA_REQUEST_KEY,
                    this, (requestKey, result) -> {
                        if (result.getInt(ConfirmDialogFragment.CONFIRM_WHICH_KEY) ==
                                ConfirmDialogFragment.CONFIRM_POSITIVE)
                            // 删除所有数据
                            DbUtil.truncateAllTables(context);
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
                        }
                    });
        }
    }
}