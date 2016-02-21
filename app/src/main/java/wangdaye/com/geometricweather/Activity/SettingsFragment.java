package wangdaye.com.geometricweather.Activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.Service.NotificationService;
import wangdaye.com.geometricweather.Service.WidgetService;

/**
 * Created by WangDaYe on 2016/2/8.
 */

public class SettingsFragment extends PreferenceFragment {

// life cycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.perference);
        this.initNotificationPart();
    }

// touch interface

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference.getKey().equals(getString(R.string.key_more_animator_switch))) {
            Toast.makeText(getActivity(),
                    getString(R.string.please_restart),
                    Toast.LENGTH_SHORT).show();
        } else if (preference.getKey().equals(getString(R.string.key_hide_star))) {
            Toast.makeText(getActivity(),
                    getString(R.string.please_restart),
                    Toast.LENGTH_SHORT).show();
        } else if (preference.getKey().equals(getString(R.string.key_navigation_bar_color_switch))) {
            MainActivity.initNavigationBar(getActivity(), getActivity().getWindow());
        } else if (preference.getKey().equals(getString(R.string.refresh_widget))) {
            refreshWidget();
        } else if (preference.getKey().equals(getString(R.string.key_notification_switch))) {
            initNotificationPart();
            SharedPreferences sharedPreferences
                    = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if (sharedPreferences.getBoolean(getString(R.string.key_notification_switch), false)) {
                // set the notification switch on
                if (sharedPreferences.getBoolean(getString(R.string.key_notification_auto_refresh_switch), false)) {
                    // set the auto refresh switch on
                    startNotificationService();
                    Toast.makeText(
                            getActivity(),
                            getString(R.string.refresh_notification_now),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // do not auto refresh
                    Toast.makeText(
                            getActivity(),
                            getString(R.string.refresh_notification_after_back),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                // set the notification switch off
                stopNotificationService();
                NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(MainActivity.NOTIFICATION_ID);
            }
        } else if (preference.getKey().equals(getString(R.string.key_notification_text_color))) {
            Toast.makeText(
                    getActivity(),
                    getString(R.string.refresh_notification_after_back),
                    Toast.LENGTH_SHORT).show();
        } else if (preference.getKey().equals(getString(R.string.key_notification_can_clear_switch))) {
            SharedPreferences sharedPreferences
                    = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if(sharedPreferences.getBoolean(getString(R.string.key_notification_switch), false)) {
                Toast.makeText(
                        getActivity(),
                        getString(R.string.refresh_notification_after_back),
                        Toast.LENGTH_SHORT).show();
            }
        } else if (preference.getKey().equals(getString(R.string.key_notification_auto_refresh_switch))) {
            SharedPreferences sharedPreferences
                    = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if(sharedPreferences.getBoolean(getString(R.string.key_notification_auto_refresh_switch), false)) {
                startNotificationService();
                Toast.makeText(
                        getActivity(),
                        getString(R.string.refresh_notification_now),
                        Toast.LENGTH_SHORT).show();
            } else {
                stopNotificationService();
            }
            this.initNotificationPart();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

// initialize

    private void initNotificationPart() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        ListPreference listPreferenceNotificationTextColor = (ListPreference) findPreference(getString(R.string.key_notification_text_color));
        CheckBoxPreference checkBoxPreferenceClear = (CheckBoxPreference) findPreference(getString(R.string.key_notification_can_clear_switch));
        CheckBoxPreference checkBoxPreferenceHide = (CheckBoxPreference) findPreference(getString(R.string.key_hide_notification_in_lockScreen));
        CheckBoxPreference checkBoxPreferenceAuto = (CheckBoxPreference) findPreference(getString(R.string.key_notification_auto_refresh_switch));
        ListPreference listPreferenceNotificationTime = (ListPreference) findPreference(getString(R.string.key_notification_time));
        CheckBoxPreference checkBoxPreferenceNotificationSound = (CheckBoxPreference) findPreference(getString(R.string.key_notification_sound_switch));
        CheckBoxPreference checkBoxPreferenceNotificationShock = (CheckBoxPreference) findPreference(getString(R.string.key_notification_shock_switch));

        if(sharedPreferences.getBoolean(getString(R.string.key_notification_switch), false)) {
            // set the notification switch on
            listPreferenceNotificationTextColor.setEnabled(true);
            checkBoxPreferenceClear.setEnabled(true);
            checkBoxPreferenceHide.setEnabled(true);
            checkBoxPreferenceAuto.setEnabled(true);
            if(sharedPreferences.getBoolean(getString(R.string.key_notification_auto_refresh_switch), false)) {
                // set the auto refresh switch on
                listPreferenceNotificationTime.setEnabled(true);
                checkBoxPreferenceNotificationSound.setEnabled(true);
                checkBoxPreferenceNotificationShock.setEnabled(true);
            } else {
                listPreferenceNotificationTime.setEnabled(false);
                checkBoxPreferenceNotificationSound.setEnabled(false);
                checkBoxPreferenceNotificationShock.setEnabled(false);
            }
        } else {
            // set the notification switch off
            listPreferenceNotificationTextColor.setEnabled(false);
            checkBoxPreferenceClear.setEnabled(false);
            checkBoxPreferenceHide.setEnabled(false);
            checkBoxPreferenceAuto.setEnabled(false);
            listPreferenceNotificationTime.setEnabled(false);
            checkBoxPreferenceNotificationSound.setEnabled(false);
            checkBoxPreferenceNotificationShock.setEnabled(false);
        }
    }

// option feedback

    private void refreshWidget() {
        Intent intent = new Intent(getActivity(), WidgetService.class);
        getActivity().startService(intent);
        Toast.makeText(
                getActivity(),
                getString(R.string.refresh_widget),
                Toast.LENGTH_SHORT).show();
    }

    private void startNotificationService() {
        Intent intent = new Intent(getActivity(), NotificationService.class);
        getActivity().startService(intent);
    }

    private void stopNotificationService() {
        Intent intent = new Intent(getActivity(), NotificationService.class);
        getActivity().stopService(intent);
    }
}