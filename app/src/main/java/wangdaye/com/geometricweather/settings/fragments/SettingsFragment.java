package wangdaye.com.geometricweather.settings.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.polling.PollingManager;
import wangdaye.com.geometricweather.common.basic.models.options.NotificationStyle;
import wangdaye.com.geometricweather.common.utils.helpers.IntentHelper;
import wangdaye.com.geometricweather.remoteviews.config.ClockDayDetailsWidgetConfigActivity;
import wangdaye.com.geometricweather.remoteviews.config.ClockDayHorizontalWidgetConfigActivity;
import wangdaye.com.geometricweather.remoteviews.config.ClockDayVerticalWidgetConfigActivity;
import wangdaye.com.geometricweather.remoteviews.config.ClockDayWeekWidgetConfigActivity;
import wangdaye.com.geometricweather.remoteviews.config.DailyTrendWidgetConfigActivity;
import wangdaye.com.geometricweather.remoteviews.config.DayWeekWidgetConfigActivity;
import wangdaye.com.geometricweather.remoteviews.config.DayWidgetConfigActivity;
import wangdaye.com.geometricweather.remoteviews.config.HourlyTrendWidgetConfigActivity;
import wangdaye.com.geometricweather.remoteviews.config.MultiCityWidgetConfigActivity;
import wangdaye.com.geometricweather.remoteviews.config.TextWidgetConfigActivity;
import wangdaye.com.geometricweather.remoteviews.config.WeekWidgetConfigActivity;
import wangdaye.com.geometricweather.remoteviews.presenters.ClockDayDetailsWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenters.ClockDayHorizontalWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenters.ClockDayVerticalWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenters.ClockDayWeekWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenters.DailyTrendWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenters.DayWeekWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenters.DayWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenters.HourlyTrendWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenters.MultiCityWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenters.TextWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenters.WeekWidgetIMP;
import wangdaye.com.geometricweather.remoteviews.presenters.notification.NormalNotificationIMP;
import wangdaye.com.geometricweather.settings.dialogs.RunningInBackgroundDialog;
import wangdaye.com.geometricweather.settings.dialogs.RunningInBackgroundODialog;
import wangdaye.com.geometricweather.settings.dialogs.TimeSetterDialog;

/**
 * Settings fragment.
 * */

public class SettingsFragment extends AbstractSettingsFragment {

    private final BroadcastReceiver setTimeCallback = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean today = intent.getBooleanExtra(TimeSetterDialog.KEY_TODAY, true);

            initForecastPart();
            if (today) {
                if (getSettingsOptionManager().isTodayForecastEnabled()) {
                    PollingManager.resetTodayForecastBackgroundTask(
                            requireActivity(), false, false);
                }
            } else {
                if (getSettingsOptionManager().isTomorrowForecastEnabled()) {
                    PollingManager.resetTomorrowForecastBackgroundTask(
                            requireActivity(), false, false);
                }
            }
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.perference);

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                setTimeCallback,
                new IntentFilter(TimeSetterDialog.ACTION_SET_TIME)
        );

        initBasicPart();
        initForecastPart();
        initWidgetPart();
        initNotificationPart();
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // do nothing.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(setTimeCallback);
    }

    private void initBasicPart() {
        // background free.
        // force set background free on android 12+.
        // findPreference(getString(R.string.key_background_free)).setVisible(
        //         Build.VERSION.SDK_INT < Build.VERSION_CODES.S);
        findPreference(getString(R.string.key_background_free)).setOnPreferenceChangeListener((preference, newValue) -> {
            boolean backgroundFree = (boolean) newValue;

            PollingManager.resetNormalBackgroundTask(requireActivity(), false);
            if (!backgroundFree) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    new RunningInBackgroundODialog().show(getParentFragmentManager(), null);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    new RunningInBackgroundDialog().show(getParentFragmentManager(), null);
                }
            }
            return true;
        });

        // update interval.
        Preference refreshRate = findPreference(getString(R.string.key_refresh_rate));
        refreshRate.setSummary(getSettingsOptionManager().getUpdateInterval().getUpdateIntervalName(requireActivity()));
        refreshRate.setOnPreferenceChangeListener((preference, newValue) -> {
            preference.setSummary(getSettingsOptionManager().getUpdateInterval().getUpdateIntervalName(requireActivity()));
            PollingManager.resetNormalBackgroundTask(requireActivity(), false);
            return true;
        });

        // dark mode.
        Preference darkMode = findPreference(getString(R.string.key_dark_mode));
        darkMode.setSummary(getSettingsOptionManager().getDarkMode().getDarkModeName(requireActivity()));
        darkMode.setOnPreferenceChangeListener((preference, newValue) -> {
            preference.setSummary(getSettingsOptionManager().getDarkMode().getDarkModeName(requireActivity()));
            GeometricWeather.getInstance().resetDayNightMode();
            GeometricWeather.getInstance().recreateAllActivities();
            return true;
        });

        // live wallpaper.
        findPreference(getString(R.string.key_live_wallpaper)).setOnPreferenceClickListener(preference -> {
            IntentHelper.startLiveWallpaperActivity(requireContext());
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
            requireView().post(this::initForecastPart);
            PollingManager.resetNormalBackgroundTask(requireActivity(), false);
            return true;
        });

        // today forecast time.
        Preference todayForecastTime = findPreference(getString(R.string.key_forecast_today_time));
        todayForecastTime.setSummary(getSettingsOptionManager().getTodayForecastTime());
        todayForecastTime.setOnPreferenceClickListener(preference -> {
            TimeSetterDialog.getInstance(true).show(getParentFragmentManager(), null);
            return true;
        });
        todayForecastTime.setEnabled(getSettingsOptionManager().isTodayForecastEnabled());

        // tomorrow forecast.
        findPreference(getString(R.string.key_forecast_tomorrow)).setOnPreferenceChangeListener((preference, newValue) -> {
            requireView().post(this::initForecastPart);
            PollingManager.resetNormalBackgroundTask(requireActivity(), false);
            return true;
        });

        // tomorrow forecast time.
        Preference tomorrowForecastTime = findPreference(getString(R.string.key_forecast_tomorrow_time));
        tomorrowForecastTime.setSummary(getSettingsOptionManager().getTomorrowForecastTime());
        tomorrowForecastTime.setOnPreferenceClickListener(preference -> {
            TimeSetterDialog.getInstance(false).show(getParentFragmentManager(), null);
            return true;
        });
        tomorrowForecastTime.setEnabled(getSettingsOptionManager().isTomorrowForecastEnabled());
    }

    private void initWidgetPart() {
        // widget week icon mode.
        ListPreference widgetWeekIconMode = findPreference(getString(R.string.key_week_icon_mode));
        widgetWeekIconMode.setSummary(
                getSettingsOptionManager().getWidgetWeekIconMode().getWidgetWeekIconModeName(requireActivity())
        );
        widgetWeekIconMode.setOnPreferenceChangeListener((preference, newValue) -> {
            requireView().post(this::initWidgetPart);
            preference.setSummary(
                    getSettingsOptionManager().getWidgetWeekIconMode().getWidgetWeekIconModeName(requireActivity())
            );
            PollingManager.resetNormalBackgroundTask(requireActivity(), true);
            return true;
        });

        // widget minimal icon.
        findPreference(getString(R.string.key_widget_minimal_icon)).setOnPreferenceChangeListener((preference, newValue) -> {
            PollingManager.resetNormalBackgroundTask(requireActivity(), true);
            return true;
        });

        // day.
        Preference day = findPreference(getString(R.string.key_widget_day));
        day.setVisible(DayWidgetIMP.isEnable(requireActivity()));
        day.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(requireActivity(), DayWidgetConfigActivity.class));
            return true;
        });

        // week.
        Preference week = findPreference(getString(R.string.key_widget_week));
        week.setVisible(WeekWidgetIMP.isEnable(requireActivity()));
        week.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(requireActivity(), WeekWidgetConfigActivity.class));
            return true;
        });

        // day + week.
        Preference dayWeek = findPreference(getString(R.string.key_widget_day_week));
        dayWeek.setVisible(DayWeekWidgetIMP.isEnable(requireActivity()));
        dayWeek.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(requireActivity(), DayWeekWidgetConfigActivity.class));
            return true;
        });

        // clock + day (horizontal).
        Preference clockDayHorizontal = findPreference(getString(R.string.key_widget_clock_day_horizontal));
        clockDayHorizontal.setVisible(ClockDayHorizontalWidgetIMP.isEnable(requireActivity()));
        clockDayHorizontal.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(requireActivity(), ClockDayHorizontalWidgetConfigActivity.class));
            return true;
        });

        // clock + day (details).
        Preference clockDayDetails = findPreference(getString(R.string.key_widget_clock_day_details));
        clockDayDetails.setVisible(ClockDayDetailsWidgetIMP.isEnable(requireActivity()));
        clockDayDetails.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(requireActivity(), ClockDayDetailsWidgetConfigActivity.class));
            return true;
        });

        // clock + day (vertical).
        Preference clockDayVertical = findPreference(getString(R.string.key_widget_clock_day_vertical));
        clockDayVertical.setVisible(ClockDayVerticalWidgetIMP.isEnable(requireActivity()));
        clockDayVertical.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(requireActivity(), ClockDayVerticalWidgetConfigActivity.class));
            return true;
        });

        // clock + day + week.
        Preference clockDayWeek = findPreference(getString(R.string.key_widget_clock_day_week));
        clockDayWeek.setVisible(ClockDayWeekWidgetIMP.isEnable(requireActivity()));
        clockDayWeek.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(requireActivity(), ClockDayWeekWidgetConfigActivity.class));
            return true;
        });

        // text.
        Preference text = findPreference(getString(R.string.key_widget_text));
        text.setVisible(TextWidgetIMP.isEnable(requireActivity()));
        text.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(requireActivity(), TextWidgetConfigActivity.class));
            return true;
        });

        // daily trend.
        Preference dailyTrend = findPreference(getString(R.string.key_widget_trend_daily));
        dailyTrend.setVisible(DailyTrendWidgetIMP.isEnable(requireActivity()));
        dailyTrend.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(requireActivity(), DailyTrendWidgetConfigActivity.class));
            return true;
        });

        // hourly trend.
        Preference hourlyTrend = findPreference(getString(R.string.key_widget_trend_hourly));
        hourlyTrend.setVisible(HourlyTrendWidgetIMP.isEnable(requireActivity()));
        hourlyTrend.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(requireActivity(), HourlyTrendWidgetConfigActivity.class));
            return true;
        });

        // multi city.
        Preference multiCity = findPreference(getString(R.string.key_widget_multi_city));
        multiCity.setVisible(MultiCityWidgetIMP.isEnable(requireActivity()));
        multiCity.setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(requireActivity(), MultiCityWidgetConfigActivity.class));
            return true;
        });
    }


    private void initNotificationPart() {
        // notification enabled.
        findPreference(getString(R.string.key_notification)).setOnPreferenceChangeListener((preference, newValue) -> {
            boolean enabled = (boolean) newValue;

            requireView().post(this::initNotificationPart);
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
                getSettingsOptionManager().getNotificationStyle().getNotificationStyleName(requireActivity())
        );
        notificationStyle.setOnPreferenceChangeListener((preference, newValue) -> {
            requireView().post(this::initNotificationPart);
            preference.setSummary(
                    getSettingsOptionManager().getNotificationStyle().getNotificationStyleName(requireActivity())
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
            PollingManager.resetNormalBackgroundTask(requireActivity(), true);
            return true;
        });

        // notification temp icon.
        CheckBoxPreference notificationTempIcon = findPreference(getString(R.string.key_notification_temp_icon));
        notificationTempIcon.setOnPreferenceChangeListener((preference, newValue) -> {
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
            PollingManager.resetNormalBackgroundTask(requireActivity(), true);
            return true;
        });

        // notification hide icon.
        CheckBoxPreference hideNotificationIcon = findPreference(getString(R.string.key_notification_hide_icon));
        hideNotificationIcon.setOnPreferenceChangeListener((preference, newValue) -> {
            PollingManager.resetNormalBackgroundTask(requireActivity(), true);
            return true;
        });

        // notification hide in lock screen.
        CheckBoxPreference hideNotificationInLockScreen = findPreference(getString(R.string.key_notification_hide_in_lockScreen));
        hideNotificationInLockScreen.setOnPreferenceChangeListener((preference, newValue) -> {
            PollingManager.resetNormalBackgroundTask(requireActivity(), true);
            return true;
        });

        // notification hide big view.
        CheckBoxPreference notificationHideBigView = findPreference(getString(R.string.key_notification_hide_big_view));
        notificationHideBigView.setOnPreferenceChangeListener((preference, newValue) -> {
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