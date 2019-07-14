package wangdaye.com.geometricweather.settings.fragment;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.settings.activity.SettingsActivity;
import wangdaye.com.geometricweather.ui.dialog.RunningInBackgroundDialog;
import wangdaye.com.geometricweather.ui.dialog.RunningInBackgroundODialog;
import wangdaye.com.geometricweather.ui.dialog.TimeSetterDialog;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.background.polling.PollingManager;
import wangdaye.com.geometricweather.remoteviews.presenter.notification.NormalNotificationIMP;

/**
 * Settings fragment.
 * */

public class SettingsFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceChangeListener, TimeSetterDialog.OnTimeChangedListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.perference);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        initBasicPart();
        initForecastPart(sharedPreferences);
        initNotificationPart(sharedPreferences);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // do nothing.
    }

    private void initBasicPart() {
        Preference darkMode = findPreference(getString(R.string.key_dark_mode));
        darkMode.setSummary(
                ValueUtils.getDarkMode(
                        getActivity(),
                        SettingsOptionManager.getInstance(getActivity()).getDarkMode()
                )
        );
        darkMode.setOnPreferenceChangeListener(this);

        Preference refreshRate = findPreference(getString(R.string.key_refresh_rate));
        refreshRate.setSummary(
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getString(
                                getString(R.string.key_refresh_rate),
                                "1:30"));
        refreshRate.setOnPreferenceChangeListener(this);
    }

    private void initForecastPart(SharedPreferences sharedPreferences) {
        // set today forecast time & todayForecastType.
        Preference todayForecastTime = findPreference(getString(R.string.key_forecast_today_time));
        todayForecastTime.setSummary(
                sharedPreferences.getString(
                        getString(R.string.key_forecast_today_time),
                        SettingsOptionManager.DEFAULT_TODAY_FORECAST_TIME));

        // set tomorrow forecast time & tomorrowForecastType.
        Preference tomorrowForecastTime = findPreference(getString(R.string.key_forecast_tomorrow_time));
        tomorrowForecastTime.setSummary(
                sharedPreferences.getString(
                        getString(R.string.key_forecast_tomorrow_time),
                        SettingsOptionManager.DEFAULT_TOMORROW_FORECAST_TIME));

        if (sharedPreferences.getBoolean(getString(R.string.key_forecast_today), false)) {
            // open today forecast.
            // set item enable.
            todayForecastTime.setEnabled(true);
        } else {
            // set item enable.
            todayForecastTime.setEnabled(false);
        }

        if (sharedPreferences.getBoolean(getString(R.string.key_forecast_tomorrow), false)) {
            // open tomorrow forecast.
            tomorrowForecastTime.setEnabled(true);
        } else {
            tomorrowForecastTime.setEnabled(false);
        }
    }

    private void initNotificationPart(SharedPreferences sharedPreferences) {
        // notification minimal icon.
        CheckBoxPreference notificationMinimalIcon = findPreference(getString(R.string.key_notification_minimal_icon));
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            notificationMinimalIcon.setVisible(false);
        }

        // notification temp icon.
        CheckBoxPreference notificationTempIcon = findPreference(getString(R.string.key_notification_temp_icon));

        // notification color.
        Preference notificationColor = findPreference(getString(R.string.key_notification_color));
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            notificationColor.setVisible(false);
        }

        // notification can be cleared.
        CheckBoxPreference notificationClearFlag = findPreference(getString(R.string.key_notification_can_be_cleared));

        // notification hide icon.
        CheckBoxPreference notificationIconBehavior = findPreference(getString(R.string.key_notification_hide_icon));

        // notification hide in lock screen.
        CheckBoxPreference notificationHideBehavior = findPreference(getString(R.string.key_notification_hide_in_lockScreen));

        // notification hide big view.
        CheckBoxPreference notificationHideBigView = findPreference(getString(R.string.key_notification_hide_big_view));

        if(sharedPreferences.getBoolean(getString(R.string.key_notification), false)) {
            // open notification.
            notificationMinimalIcon.setEnabled(true);
            notificationTempIcon.setEnabled(true);
            notificationColor.setEnabled(true);
            notificationClearFlag.setEnabled(true);
            notificationIconBehavior.setEnabled(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notificationHideBehavior.setEnabled(true);
            } else {
                notificationHideBehavior.setEnabled(false);
            }
            notificationHideBigView.setEnabled(true);
        } else {
            // close notification.
            notificationMinimalIcon.setEnabled(false);
            notificationTempIcon.setEnabled(false);
            notificationColor.setEnabled(false);
            notificationClearFlag.setEnabled(false);
            notificationIconBehavior.setEnabled(false);
            notificationHideBehavior.setEnabled(false);
            notificationHideBigView.setEnabled(false);
        }
    }

    // interface.

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (preference.getKey().equals(getString(R.string.key_live_wallpaper))) {
            IntentHelper.startLiveWallpaperActivity(getActivity());
        } else if (preference.getKey().equals(getString(R.string.key_service_provider))) {
            ((SettingsActivity) getActivity()).pushFragment(
                    new ServiceProviderSettingsFragment(),
                    preference.getKey()
            );
        } else if (preference.getKey().equals(getString(R.string.key_unit))) {
            ((SettingsActivity) getActivity()).pushFragment(
                    new UnitSettingsFragment(),
                    preference.getKey()
            );
        } else if (preference.getKey().equals(getString(R.string.key_appearance))) {
            ((SettingsActivity) getActivity()).pushFragment(
                    new AppearanceSettingsFragment(),
                    preference.getKey()
            );
        } else if (preference.getKey().equals(getString(R.string.key_background_free))) {
            // background free.
            PollingManager.resetNormalBackgroundTask(getActivity(), false);
            boolean backgroundFree = sharedPreferences.getBoolean(getString(R.string.key_background_free), true);
            if (!backgroundFree) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    new RunningInBackgroundODialog().show(getFragmentManager(), null);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    new RunningInBackgroundDialog().show(getFragmentManager(), null);
                }
            }
        } else if (preference.getKey().equals(getString(R.string.key_forecast_today))) {
            // forecast today.
            initForecastPart(sharedPreferences);
            PollingManager.resetNormalBackgroundTask(getActivity(), false);
        } else if (preference.getKey().equals(getString(R.string.key_forecast_today_time))) {
            // set today forecast time.
            TimeSetterDialog dialog = new TimeSetterDialog();
            dialog.setModel(true);
            dialog.setOnTimeChangedListener(this);
            dialog.show(getFragmentManager(), null);
        } else if (preference.getKey().equals(getString(R.string.key_forecast_tomorrow))) {
            // forecast tomorrow.
            initForecastPart(sharedPreferences);
            PollingManager.resetNormalBackgroundTask(getActivity(), false);
        } else if (preference.getKey().equals(getString(R.string.key_forecast_tomorrow_time))) {
            // set tomorrow forecast time.
            TimeSetterDialog dialog = new TimeSetterDialog();
            dialog.setModel(false);
            dialog.setOnTimeChangedListener(this);
            dialog.show(getFragmentManager(), null);
        } else if (preference.getKey().equals(getString(R.string.key_widget_minimal_icon))
                || preference.getKey().equals(getString(R.string.key_notification_minimal_icon))) {
            PollingManager.resetNormalBackgroundTask(getActivity(), true);
        } else if (preference.getKey().equals(getString(R.string.key_click_widget_to_refresh))) {
            // click widget to refresh.
            PollingManager.resetNormalBackgroundTask(getActivity(), true);
        } else if (preference.getKey().equals(getString(R.string.key_notification))) {
            // notification switch.
            initNotificationPart(sharedPreferences);
            if (sharedPreferences.getBoolean(getString(R.string.key_notification), false)) {
                // open notification.
                PollingManager.resetNormalBackgroundTask(getActivity(), true);
            } else {
                // close notification.
                NormalNotificationIMP.cancelNotification(getActivity());
                PollingManager.resetNormalBackgroundTask(getActivity(), false);
            }
        } else if (preference.getKey().equals(getString(R.string.key_notification_temp_icon))) {
            // notification temp icon.
            PollingManager.resetNormalBackgroundTask(getActivity(), true);
        } else if (preference.getKey().equals(getString(R.string.key_notification_color))) {
            // notification color.
            ((SettingsActivity) getActivity()).pushFragment(
                    new NotificationColorSettingsFragment(),
                    preference.getKey()
            );
        } else if (preference.getKey().equals(getString(R.string.key_notification_can_be_cleared))) {
            // notification clear flag.
            PollingManager.resetNormalBackgroundTask(getActivity(), true);
        } else if (preference.getKey().equals(getString(R.string.key_notification_hide_icon))) {
            PollingManager.resetNormalBackgroundTask(getActivity(), true);
        } else if (preference.getKey().equals(getString(R.string.key_notification_hide_in_lockScreen))) {
            PollingManager.resetNormalBackgroundTask(getActivity(), true);
        } else if (preference.getKey().equals(getString(R.string.key_notification_hide_big_view))) {
            PollingManager.resetNormalBackgroundTask(getActivity(), true);
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        if (preference.getKey().equals(getString(R.string.key_dark_mode))) {
            // Dark mode.
            preference.setSummary(ValueUtils.getDarkMode(getActivity(), (String) o));
            SettingsOptionManager.getInstance(getActivity()).setDarkMode((String) o);
            GeometricWeather.getInstance().resetDayNightMode();
            GeometricWeather.getInstance().recreateAllActivities();
        } else if (preference.getKey().equals(getString(R.string.key_refresh_rate))) {
            SettingsOptionManager.getInstance(getActivity()).setUpdateInterval((String) o);
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
            editor.putString(getString(R.string.key_refresh_rate), (String) o);
            editor.apply();
            preference.setSummary((String) o);
            PollingManager.resetNormalBackgroundTask(getActivity(), false);
        }
        return true;
    }

    @Override
    public void timeChanged() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        this.initForecastPart(sharedPreferences);
        if (sharedPreferences.getBoolean(getString(R.string.key_forecast_today), false)
                || sharedPreferences.getBoolean(getString(R.string.key_forecast_tomorrow), false)) {
            PollingManager.resetTodayForecastBackgroundTask(getActivity(), false);
            PollingManager.resetTomorrowForecastBackgroundTask(getActivity(), false);
        }
    }
}