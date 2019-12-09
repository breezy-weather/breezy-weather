package wangdaye.com.geometricweather.settings.fragment;

import android.os.Build;
import android.os.Bundle;

import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.option.NotificationStyle;
import wangdaye.com.geometricweather.settings.OptionMapper;
import wangdaye.com.geometricweather.settings.dialog.RunningInBackgroundDialog;
import wangdaye.com.geometricweather.settings.dialog.RunningInBackgroundODialog;
import wangdaye.com.geometricweather.settings.dialog.TimeSetterDialog;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.background.polling.PollingManager;
import wangdaye.com.geometricweather.remoteviews.presenter.notification.NormalNotificationIMP;

/**
 * Settings fragment.
 * */

public class SettingsFragment extends AbstractSettingsFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.perference);

        initBasicPart();
        initForecastPart();
        initWidgetPart();
        initNotificationPart();
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // do nothing.
    }

    private void initBasicPart() {
        // background free.
        findPreference(getString(R.string.key_background_free)).setOnPreferenceChangeListener((preference, newValue) -> {
            boolean backgroundFree = (boolean) newValue;
            getSettingsOptionManager().setBackgroundFree(backgroundFree);

            PollingManager.resetNormalBackgroundTask(requireActivity(), false);
            if (!backgroundFree) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    new RunningInBackgroundODialog().show(requireFragmentManager(), null);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    new RunningInBackgroundDialog().show(requireFragmentManager(), null);
                }
            }
            return true;
        });

        // alert notification.
        findPreference(getString(R.string.key_alert_notification_switch)).setOnPreferenceChangeListener((p, newValue) -> {
            getSettingsOptionManager().setAlertPushEnabled((Boolean) newValue);
            return true;
        });

        // precipitation notification.
        findPreference(getString(R.string.key_precipitation_notification_switch)).setOnPreferenceChangeListener((p, v) -> {
            getSettingsOptionManager().setPrecipitationPushEnabled((Boolean) v);
            return true;
        });

        // update interval.
        Preference refreshRate = findPreference(getString(R.string.key_refresh_rate));
        refreshRate.setSummary(getSettingsOptionManager().getUpdateInterval().getUpdateIntervalName(getActivity()));
        refreshRate.setOnPreferenceChangeListener((preference, newValue) -> {
            getSettingsOptionManager().setUpdateInterval(OptionMapper.getUpdateInterval((String) newValue));
            preference.setSummary(getSettingsOptionManager().getUpdateInterval().getUpdateIntervalName(getActivity()));
            PollingManager.resetNormalBackgroundTask(requireActivity(), false);
            return true;
        });

        // dark mode.
        Preference darkMode = findPreference(getString(R.string.key_dark_mode));
        darkMode.setSummary(getSettingsOptionManager().getDarkMode().getDarkModeName(getActivity()));
        darkMode.setOnPreferenceChangeListener((preference, newValue) -> {
            getSettingsOptionManager().setDarkMode(OptionMapper.getDarkMode((String) newValue));
            preference.setSummary(getSettingsOptionManager().getDarkMode().getDarkModeName(getActivity()));
            GeometricWeather.getInstance().resetDayNightMode();
            GeometricWeather.getInstance().recreateAllActivities();
            return true;
        });

        // live wallpaper.
        findPreference(getString(R.string.key_live_wallpaper)).setOnPreferenceClickListener(preference -> {
            IntentHelper.startLiveWallpaperActivity((GeoActivity) requireActivity());
            return true;
        });

        // service provider.
        findPreference(getString(R.string.key_service_provider)).setOnPreferenceClickListener(preference -> {
            pushFragment(new ServiceProviderSettingsFragment(), preference.getKey());
            return true;
        });

        // unit.
        findPreference(getString(R.string.key_unit)).setOnPreferenceClickListener(preference -> {
            pushFragment(new UnitSettingsFragment(), preference.getKey());
            return true;
        });

        // appearance.
        findPreference(getString(R.string.key_appearance)).setOnPreferenceClickListener(preference -> {
            pushFragment(new AppearanceSettingsFragment(), preference.getKey());
            return true;
        });
    }

    private void initForecastPart() {
        // today forecast.
        findPreference(getString(R.string.key_forecast_today)).setOnPreferenceChangeListener((preference, newValue) -> {
            getSettingsOptionManager().setTodayForecastEnabled((Boolean) newValue);
            initForecastPart();
            PollingManager.resetNormalBackgroundTask(requireActivity(), false);
            return true;
        });

        // today forecast time.
        Preference todayForecastTime = findPreference(getString(R.string.key_forecast_today_time));
        todayForecastTime.setSummary(getSettingsOptionManager().getTodayForecastTime());
        todayForecastTime.setOnPreferenceClickListener(preference -> {
            TimeSetterDialog dialog = new TimeSetterDialog();
            dialog.setIsToday(true);
            dialog.setOnTimeChangedListener(() -> {
                initForecastPart();
                if (getSettingsOptionManager().isTodayForecastEnabled()) {
                    PollingManager.resetTodayForecastBackgroundTask(
                            requireActivity(), false, false);
                }
            });
            dialog.show(requireFragmentManager(), null);
            return true;
        });
        todayForecastTime.setEnabled(getSettingsOptionManager().isTodayForecastEnabled());

        // tomorrow forecast.
        findPreference(getString(R.string.key_forecast_tomorrow)).setOnPreferenceChangeListener((preference, newValue) -> {
            getSettingsOptionManager().setTomorrowForecastEnabled((Boolean) newValue);
            initForecastPart();
            PollingManager.resetNormalBackgroundTask(requireActivity(), false);
            return true;
        });

        // tomorrow forecast time.
        Preference tomorrowForecastTime = findPreference(getString(R.string.key_forecast_tomorrow_time));
        tomorrowForecastTime.setSummary(getSettingsOptionManager().getTomorrowForecastTime());
        tomorrowForecastTime.setOnPreferenceClickListener(preference -> {
            TimeSetterDialog dialog = new TimeSetterDialog();
            dialog.setIsToday(false);
            dialog.setOnTimeChangedListener(() -> {
                initForecastPart();
                if (getSettingsOptionManager().isTomorrowForecastEnabled()) {
                    PollingManager.resetTomorrowForecastBackgroundTask(
                            requireActivity(), false, false);
                }
            });
            dialog.show(requireFragmentManager(), null);
            return true;
        });
        tomorrowForecastTime.setEnabled(getSettingsOptionManager().isTomorrowForecastEnabled());
    }

    private void initWidgetPart() {
        // widget minimal icon.
        findPreference(getString(R.string.key_widget_minimal_icon)).setOnPreferenceChangeListener((preference, newValue) -> {
            getSettingsOptionManager().setWidgetMinimalIconEnabled((Boolean) newValue);
            PollingManager.resetNormalBackgroundTask(requireActivity(), true);
            return true;
        });

        // click widget to update.
        findPreference(getString(R.string.key_click_widget_to_refresh)).setOnPreferenceChangeListener((preference, newValue) -> {
            getSettingsOptionManager().setWidgetClickToRefreshEnabled((Boolean) newValue);
            PollingManager.resetNormalBackgroundTask(requireActivity(), true);
            return true;
        });
    }


    private void initNotificationPart() {
        // notification enabled.
        findPreference(getString(R.string.key_notification)).setOnPreferenceChangeListener((preference, newValue) -> {
            boolean enabled = (boolean) newValue;
            getSettingsOptionManager().setNotificationEnabled(enabled);
            initNotificationPart();
            if (enabled) { // open notification.
                PollingManager.resetNormalBackgroundTask(requireActivity(), true);
            } else { // close notification.
                NormalNotificationIMP.cancelNotification(requireActivity());
                PollingManager.resetNormalBackgroundTask(requireActivity(), false);
            }
            return true;
        });

        // notification style.
        ListPreference notificationStyle = findPreference(getString(R.string.key_notification_style));
        notificationStyle.setSummary(
                getSettingsOptionManager().getNotificationStyle().getNotificationStyleName(getActivity())
        );
        notificationStyle.setOnPreferenceChangeListener((preference, newValue) -> {
            getSettingsOptionManager().setNotificationStyle(OptionMapper.getNotificationStyle((String) newValue));
            initNotificationPart();
            preference.setSummary(
                    getSettingsOptionManager().getNotificationStyle().getNotificationStyleName(getActivity())
            );
            PollingManager.resetNormalBackgroundTask(requireActivity(), true);
            return true;
        });

        // notification minimal icon.
        CheckBoxPreference notificationMinimalIcon = findPreference(getString(R.string.key_notification_minimal_icon));
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            notificationMinimalIcon.setVisible(false);
        }
        notificationMinimalIcon.setOnPreferenceChangeListener((preference, newValue) -> {
            getSettingsOptionManager().setNotificationMinimalIconEnabled((Boolean) newValue);
            PollingManager.resetNormalBackgroundTask(requireActivity(), true);
            return true;
        });

        // notification temp icon.
        CheckBoxPreference notificationTempIcon = findPreference(getString(R.string.key_notification_temp_icon));
        notificationTempIcon.setOnPreferenceChangeListener((preference, newValue) -> {
            getSettingsOptionManager().setNotificationTemperatureIconEnabled((Boolean) newValue);
            PollingManager.resetNormalBackgroundTask(requireActivity(), true);
            return true;
        });

        // notification color.
        Preference notificationColor = findPreference(getString(R.string.key_notification_color));
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            notificationColor.setVisible(false);
        }
        notificationColor.setOnPreferenceClickListener(preference -> {
            pushFragment(new NotificationColorSettingsFragment(), preference.getKey());
            return true;
        });

        // notification can be cleared.
        CheckBoxPreference notificationClearFlag = findPreference(getString(R.string.key_notification_can_be_cleared));
        notificationClearFlag.setOnPreferenceChangeListener((preference, newValue) -> {
            getSettingsOptionManager().setNotificationCanBeClearedEnabled((Boolean) newValue);
            PollingManager.resetNormalBackgroundTask(requireActivity(), true);
            return true;
        });

        // notification hide icon.
        CheckBoxPreference hideNotificationIcon = findPreference(getString(R.string.key_notification_hide_icon));
        hideNotificationIcon.setOnPreferenceChangeListener((preference, newValue) -> {
            getSettingsOptionManager().setNotificationHideIconEnabled((Boolean) newValue);
            PollingManager.resetNormalBackgroundTask(requireActivity(), true);
            return true;
        });

        // notification hide in lock screen.
        CheckBoxPreference hideNotificationInLockScreen = findPreference(getString(R.string.key_notification_hide_in_lockScreen));
        hideNotificationInLockScreen.setOnPreferenceChangeListener((preference, newValue) -> {
            getSettingsOptionManager().setNotificationHideInLockScreenEnabled((Boolean) newValue);
            PollingManager.resetNormalBackgroundTask(requireActivity(), true);
            return true;
        });

        // notification hide big view.
        CheckBoxPreference notificationHideBigView = findPreference(getString(R.string.key_notification_hide_big_view));
        notificationHideBigView.setOnPreferenceChangeListener((preference, newValue) -> {
            getSettingsOptionManager().setNotificationHideBigViewEnabled((Boolean) newValue);
            PollingManager.resetNormalBackgroundTask(requireActivity(), true);
            return true;
        });

        boolean sendNotification = getSettingsOptionManager().isNotificationEnabled();
        boolean nativeNotification = getSettingsOptionManager().getNotificationStyle() == NotificationStyle.NATIVE;
        boolean androidL = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
        notificationStyle.setEnabled(sendNotification);
        notificationMinimalIcon.setEnabled(sendNotification && !nativeNotification);
        notificationTempIcon.setEnabled(sendNotification);
        notificationColor.setEnabled(sendNotification && !nativeNotification);
        notificationClearFlag.setEnabled(sendNotification);
        hideNotificationIcon.setEnabled(sendNotification);
        hideNotificationInLockScreen.setEnabled(sendNotification && androidL);
        notificationHideBigView.setEnabled(sendNotification && !nativeNotification);
    }
}