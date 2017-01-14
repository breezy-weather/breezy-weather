package wangdaye.com.geometricweather.view.fragment;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.helpter.ServiceHelper;
import wangdaye.com.geometricweather.view.dialog.TimeSetterDialog;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Settings fragment.
 * */

public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener, TimeSetterDialog.OnTimeChangedListener {

    /** <br> life cycle. */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.perference);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        initUIPart(sharedPreferences);
        initForecastPart(sharedPreferences);
        initNotificationPart(sharedPreferences);
    }

    /** <br> UI. */

    private void initUIPart(SharedPreferences sharedPreferences) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            findPreference(getString(R.string.key_navigationBar_color)).setEnabled(false);
        } else {
            findPreference(getString(R.string.key_navigationBar_color)).setEnabled(true);
        }

        Preference language = findPreference(getString(R.string.key_language));
        language.setSummary(
                ValueUtils.getLanguage(
                        getActivity(),
                        sharedPreferences.getString(getString(R.string.key_language), "follow_system")));
        language.setOnPreferenceChangeListener(this);

        Preference fahrenheit = findPreference(getString(R.string.key_fahrenheit));
        fahrenheit.setOnPreferenceChangeListener(this);
    }

    private void initForecastPart(SharedPreferences sharedPreferences) {
        // set today forecast time & todayForecastType.
        Preference todayForecastTime = findPreference(getString(R.string.key_forecast_today_time));
        todayForecastTime.setSummary(
                sharedPreferences.getString(getString(R.string.key_forecast_today_time), "07:00"));

        // set tomorrow forecast time & tomorrowForecastType.
        Preference tomorrowForecastTime = findPreference(getString(R.string.key_forecast_tomorrow_time));
        tomorrowForecastTime.setSummary(
                sharedPreferences.getString(getString(R.string.key_forecast_tomorrow_time), "21:00"));

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
        // notification text color.
        ListPreference notificationTextColor = (ListPreference) findPreference(getString(R.string.key_notification_text_color));
        notificationTextColor.setSummary(
                ValueUtils.getNotificationTextColor(
                        getActivity(),
                        sharedPreferences.getString(getString(R.string.key_notification_text_color), "grey")));
        notificationTextColor.setOnPreferenceChangeListener(this);

        // notification background.
        CheckBoxPreference notificationBackground = (CheckBoxPreference) findPreference(getString(R.string.key_notification_background));

        // notification can be cleared.
        CheckBoxPreference notificationClearFlag = (CheckBoxPreference) findPreference(getString(R.string.key_notification_can_be_cleared));

        // notification hide icon.
        CheckBoxPreference notificationIconBehavior = (CheckBoxPreference) findPreference(getString(R.string.key_notification_hide_icon));

        // notification hide in lock screen.
        CheckBoxPreference notificationHideBehavior = (CheckBoxPreference) findPreference(getString(R.string.key_notification_hide_in_lockScreen));

        if(sharedPreferences.getBoolean(getString(R.string.key_notification), false)) {
            // open notification.
            notificationTextColor.setEnabled(true);
            notificationBackground.setEnabled(true);
            notificationClearFlag.setEnabled(true);
            notificationIconBehavior.setEnabled(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notificationHideBehavior.setEnabled(true);
            } else {
                notificationHideBehavior.setEnabled(false);
            }
        } else {
            // close notification.
            notificationTextColor.setEnabled(false);
            notificationBackground.setEnabled(false);
            notificationClearFlag.setEnabled(false);
            notificationIconBehavior.setEnabled(false);
            notificationHideBehavior.setEnabled(false);
        }
    }

    /** interface. */

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (preference.getKey().equals(getString(R.string.key_navigationBar_color))) {
            // navigation bar color.
            GeometricWeather.getInstance().setColorNavigationBar();
            DisplayUtils.setNavigationBarColor(getActivity(), true);
            return true;
        } else if (preference.getKey().equals(getString(R.string.key_fahrenheit))) {
            // ℉
            SnackbarUtils.showSnackbar(getString(R.string.feedback_restart));
        } else if (preference.getKey().equals(getString(R.string.key_forecast_today))) {
            // forecast today.
            initForecastPart(sharedPreferences);
            if (sharedPreferences.getBoolean(getString(R.string.key_forecast_today), false)) {
                ServiceHelper.startForecastService(getActivity(), true);
            } else {
                ServiceHelper.stopForecastService(getActivity(), true);
            }
            return true;
        } else if (preference.getKey().equals(getString(R.string.key_forecast_today_time))) {
            // set today forecast time.
            TimeSetterDialog dialog = new TimeSetterDialog();
            dialog.setModel(true);
            dialog.setOnTimeChangedListener(this);
            dialog.show(getFragmentManager(), null);
            return true;
        } else if (preference.getKey().equals(getString(R.string.key_forecast_tomorrow))) {
            // timing forecast tomorrow.
            initForecastPart(sharedPreferences);
            if (sharedPreferences.getBoolean(getString(R.string.key_forecast_tomorrow), false)) {
                ServiceHelper.startForecastService(getActivity(), false);
            } else {
                ServiceHelper.stopForecastService(getActivity(), false);
            }
            return true;
        } else if (preference.getKey().equals(getString(R.string.key_forecast_tomorrow_time))) {
            // set tomorrow forecast time.
            TimeSetterDialog dialog = new TimeSetterDialog();
            dialog.setModel(false);
            dialog.setOnTimeChangedListener(this);
            dialog.show(getFragmentManager(), null);
            return true;
        } else if (preference.getKey().equals(getString(R.string.key_notification))) {
            // notification switch.
            initNotificationPart(sharedPreferences);
            if (sharedPreferences.getBoolean(getString(R.string.key_notification), false)) {
                // open notification.
                ServiceHelper.startPollingService(getActivity());
                SnackbarUtils.showSnackbar(getString(R.string.feedback_refresh_notification_now));
            } else {
                // close notification.
                ServiceHelper.stopPollingService(getActivity());
            }
            return true;
        } else if (preference.getKey().equals(getString(R.string.key_notification_text_color))) {
            // notification text color.
            SnackbarUtils.showSnackbar(getString(R.string.feedback_refresh_notification_after_back));
            return true;
        } else if (preference.getKey().equals(getString(R.string.key_notification_background))) {
            // notification background.
            SnackbarUtils.showSnackbar(getString(R.string.feedback_refresh_notification_after_back));
            return true;
        } else if (preference.getKey().equals(getString(R.string.key_notification_can_be_cleared))) {
            // notification clear flag.
            SnackbarUtils.showSnackbar(getString(R.string.feedback_refresh_notification_after_back));
            return true;
        } else if (preference.getKey().equals(getString(R.string.key_notification_hide_icon))) {
            SnackbarUtils.showSnackbar(getString(R.string.feedback_refresh_notification_after_back));
            return true;
        } else if (preference.getKey().equals(getString(R.string.key_notification_hide_in_lockScreen))) {
            SnackbarUtils.showSnackbar(getString(R.string.feedback_refresh_notification_after_back));
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        if (preference.getKey().equals(getString(R.string.key_language))) {
            preference.setSummary(ValueUtils.getLanguage(getActivity(), (String) o));
            SnackbarUtils.showSnackbar(getString(R.string.feedback_restart));
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        initForecastPart(sharedPreferences);
        initNotificationPart(sharedPreferences);
        return true;
    }

    @Override
    public void timeChanged() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        this.initForecastPart(sharedPreferences);
        if (sharedPreferences.getBoolean(getString(R.string.key_forecast_today), false)) {
            ServiceHelper.startForecastService(getActivity(), true);
        }
        if (sharedPreferences.getBoolean(getString(R.string.key_forecast_tomorrow), false)) {
            ServiceHelper.startForecastService(getActivity(), false);
        }
    }
}