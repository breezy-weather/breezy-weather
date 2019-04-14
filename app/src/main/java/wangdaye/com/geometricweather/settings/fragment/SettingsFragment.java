package wangdaye.com.geometricweather.settings.fragment;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.settings.activity.SettingsActivity;
import wangdaye.com.geometricweather.ui.dialog.RunningInBackgroundDialog;
import wangdaye.com.geometricweather.ui.dialog.RunningInBackgroundODialog;
import wangdaye.com.geometricweather.ui.dialog.TimeSetterDialog;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;
import wangdaye.com.geometricweather.background.BackgroundManager;
import wangdaye.com.geometricweather.remoteviews.presenter.NormalNotificationIMP;

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
                        GeometricWeather.getInstance().getDarkMode()));
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
                        GeometricWeather.DEFAULT_TODAY_FORECAST_TIME));

        // set tomorrow forecast time & tomorrowForecastType.
        Preference tomorrowForecastTime = findPreference(getString(R.string.key_forecast_tomorrow_time));
        tomorrowForecastTime.setSummary(
                sharedPreferences.getString(
                        getString(R.string.key_forecast_tomorrow_time),
                        GeometricWeather.DEFAULT_TOMORROW_FORECAST_TIME));

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
        CheckBoxPreference notificationMinimalIcon = (CheckBoxPreference) findPreference(getString(R.string.key_notification_minimal_icon));

        // notification temp icon.
        CheckBoxPreference notificationTempIcon = (CheckBoxPreference) findPreference(getString(R.string.key_notification_temp_icon));

        // notification text color.
        ListPreference notificationTextColor = (ListPreference) findPreference(getString(R.string.key_notification_text_color));
        notificationTextColor.setSummary(
                ValueUtils.getNotificationTextColor(
                        getActivity(),
                        sharedPreferences.getString(getString(R.string.key_notification_text_color), "dark")));
        notificationTextColor.setOnPreferenceChangeListener(this);

        // notification background.
        CheckBoxPreference notificationBackground = (CheckBoxPreference) findPreference(getString(R.string.key_notification_background));

        // notification can be cleared.
        CheckBoxPreference notificationClearFlag = (CheckBoxPreference) findPreference(getString(R.string.key_notification_can_be_cleared));

        // notification hide icon.
        CheckBoxPreference notificationIconBehavior = (CheckBoxPreference) findPreference(getString(R.string.key_notification_hide_icon));

        // notification hide in lock screen.
        CheckBoxPreference notificationHideBehavior = (CheckBoxPreference) findPreference(getString(R.string.key_notification_hide_in_lockScreen));

        // notification hide big view.
        CheckBoxPreference notificationHideBigView = (CheckBoxPreference) findPreference(getString(R.string.key_notification_hide_big_view));

        if(sharedPreferences.getBoolean(getString(R.string.key_notification), false)) {
            // open notification.
            notificationMinimalIcon.setEnabled(true);
            notificationTempIcon.setEnabled(true);
            notificationTextColor.setEnabled(true);
            notificationBackground.setEnabled(true);
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
            notificationTextColor.setEnabled(false);
            notificationBackground.setEnabled(false);
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
                    preference.getKey());
        } else if (preference.getKey().equals(getString(R.string.key_unit))) {
            ((SettingsActivity) getActivity()).pushFragment(
                    new UnitSettingsFragment(),
                    preference.getKey());
        } else if (preference.getKey().equals(getString(R.string.key_appearance))) {
            ((SettingsActivity) getActivity()).pushFragment(
                    new AppearanceSettingsFragment(),
                    preference.getKey());
        } else if (preference.getKey().equals(getString(R.string.key_background_free))) {
            // background free.
            BackgroundManager.resetNormalBackgroundTask(getActivity(), false);
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
            BackgroundManager.resetNormalBackgroundTask(getActivity(), false);
        } else if (preference.getKey().equals(getString(R.string.key_forecast_today_time))) {
            // set today forecast time.
            TimeSetterDialog dialog = new TimeSetterDialog();
            dialog.setModel(true);
            dialog.setOnTimeChangedListener(this);
            dialog.show(getFragmentManager(), null);
        } else if (preference.getKey().equals(getString(R.string.key_forecast_tomorrow))) {
            // forecast tomorrow.
            initForecastPart(sharedPreferences);
            BackgroundManager.resetNormalBackgroundTask(getActivity(), false);
        } else if (preference.getKey().equals(getString(R.string.key_forecast_tomorrow_time))) {
            // set tomorrow forecast time.
            TimeSetterDialog dialog = new TimeSetterDialog();
            dialog.setModel(false);
            dialog.setOnTimeChangedListener(this);
            dialog.show(getFragmentManager(), null);
        } else if (preference.getKey().equals(getString(R.string.key_widget_minimal_icon))
                || preference.getKey().equals(getString(R.string.key_notification_minimal_icon))) {
            BackgroundManager.resetNormalBackgroundTask(getActivity(), true);
        } else if (preference.getKey().equals(getString(R.string.key_click_widget_to_refresh))) {
            // click widget to refresh.
            BackgroundManager.resetNormalBackgroundTask(getActivity(), true);
        } else if (preference.getKey().equals(getString(R.string.key_notification))) {
            // notification switch.
            initNotificationPart(sharedPreferences);
            if (sharedPreferences.getBoolean(getString(R.string.key_notification), false)) {
                // open notification.
                BackgroundManager.resetNormalBackgroundTask(getActivity(), true);
            } else {
                // close notification.
                NormalNotificationIMP.cancelNotification(getActivity());
                BackgroundManager.resetNormalBackgroundTask(getActivity(), false);
            }
        } else if (preference.getKey().equals(getString(R.string.key_notification_temp_icon))) {
            // notification temp icon.
            BackgroundManager.resetNormalBackgroundTask(getActivity(), true);
        } else if (preference.getKey().equals(getString(R.string.key_notification_background))) {
            // notification background.
            BackgroundManager.resetNormalBackgroundTask(getActivity(), true);
        } else if (preference.getKey().equals(getString(R.string.key_notification_can_be_cleared))) {
            // notification clear flag.
            BackgroundManager.resetNormalBackgroundTask(getActivity(), true);
        } else if (preference.getKey().equals(getString(R.string.key_notification_hide_icon))) {
            BackgroundManager.resetNormalBackgroundTask(getActivity(), true);
        } else if (preference.getKey().equals(getString(R.string.key_notification_hide_in_lockScreen))) {
            BackgroundManager.resetNormalBackgroundTask(getActivity(), true);
        } else if (preference.getKey().equals(getString(R.string.key_notification_hide_big_view))) {
            BackgroundManager.resetNormalBackgroundTask(getActivity(), true);
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        if (preference.getKey().equals(getString(R.string.key_dark_mode))) {
            // Dark mode.
            preference.setSummary(ValueUtils.getDarkMode(getActivity(), (String) o));
            GeometricWeather.getInstance().setDarkMode((String) o);
            GeometricWeather.getInstance().resetDayNightMode();
            GeometricWeather.getInstance().recreateAllActivities();
        } else if (preference.getKey().equals(getString(R.string.key_refresh_rate))) {
            GeometricWeather.getInstance().setUpdateInterval((String) o);
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
            editor.putString(getString(R.string.key_refresh_rate), (String) o);
            editor.apply();
            preference.setSummary((String) o);
            BackgroundManager.resetNormalBackgroundTask(getActivity(), false);
        } else if (preference.getKey().equals(getString(R.string.key_notification_text_color))) {
            // notification text color.
            BackgroundManager.resetNormalBackgroundTask(getActivity(), true);
            preference.setSummary(ValueUtils.getNotificationTextColor(getActivity(), (String) o));
        }
        return true;
    }

    @Override
    public void timeChanged() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        this.initForecastPart(sharedPreferences);
        if (sharedPreferences.getBoolean(getString(R.string.key_forecast_today), false)
                || sharedPreferences.getBoolean(getString(R.string.key_forecast_tomorrow), false)) {
            BackgroundManager.resetTodayForecastBackgroundTask(getActivity(), false);
            BackgroundManager.resetTomorrowForecastBackgroundTask(getActivity(), false);
        }
    }
}