package wangdaye.com.geometricweather.ui.fragment;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import java.util.List;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.utils.LanguageUtils;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;
import wangdaye.com.geometricweather.utils.helpter.LocationHelper;
import wangdaye.com.geometricweather.utils.helpter.ServiceHelper;
import wangdaye.com.geometricweather.utils.remoteView.NormalNotificationUtils;
import wangdaye.com.geometricweather.ui.dialog.TimeSetterDialog;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Settings fragment.
 * */

public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener, TimeSetterDialog.OnTimeChangedListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.perference);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        initBasicPart(sharedPreferences);
        initForecastPart(sharedPreferences);
        initWidgetPart(sharedPreferences);
        initNotificationPart(sharedPreferences);
    }

    private void initBasicPart(SharedPreferences sharedPreferences) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CheckBoxPreference backgroundFree = (CheckBoxPreference) findPreference(getString(R.string.key_background_free));
            backgroundFree.setChecked(true);
            backgroundFree.setEnabled(false);
        }

        Preference chineseSource = findPreference(getString(R.string.key_chinese_source));
        chineseSource.setSummary(
                ValueUtils.getChineseSource(
                        getActivity(),
                        PreferenceManager.getDefaultSharedPreferences(getActivity())
                                .getString(
                                        getString(R.string.key_chinese_source),
                                        "cn")));
        chineseSource.setOnPreferenceChangeListener(this);
        if (!hasChineseLocation()) {
            ((PreferenceCategory) findPreference("basic")).removePreference(chineseSource);
        }

        Preference uiStyle = findPreference(getString(R.string.key_ui_style));
        uiStyle.setSummary(
                ValueUtils.getUIStyle(
                        getActivity(),
                        PreferenceManager.getDefaultSharedPreferences(getActivity())
                                .getString(
                                        getString(R.string.key_ui_style),
                                        "material")));
        uiStyle.setOnPreferenceChangeListener(this);

        Preference cardOrder = findPreference(getString(R.string.key_card_order));
        cardOrder.setSummary(
                ValueUtils.getCardOrder(
                        getActivity(),
                        PreferenceManager.getDefaultSharedPreferences(getActivity())
                                .getString(
                                        getString(R.string.key_card_order),
                                        "daily_first")));
        cardOrder.setOnPreferenceChangeListener(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            findPreference(getString(R.string.key_navigationBar_color)).setEnabled(false);
        } else {
            findPreference(getString(R.string.key_navigationBar_color)).setEnabled(true);
        }

        Preference fahrenheit = findPreference(getString(R.string.key_fahrenheit));
        fahrenheit.setOnPreferenceChangeListener(this);

        Preference imperial = findPreference(getString(R.string.key_imperial));
        imperial.setOnPreferenceChangeListener(this);

        Preference refreshRate = findPreference(getString(R.string.key_refresh_rate));
        refreshRate.setSummary(
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getString(
                                getString(R.string.key_refresh_rate),
                                "1:30"));
        refreshRate.setOnPreferenceChangeListener(this);

        Preference language = findPreference(getString(R.string.key_language));
        language.setSummary(
                ValueUtils.getLanguage(
                        getActivity(),
                        sharedPreferences.getString(getString(R.string.key_language), "follow_system")));
        language.setOnPreferenceChangeListener(this);
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

    private void initWidgetPart(SharedPreferences sharedPreferences) {
        // widget icon style.
        ListPreference widgetIconStyle = (ListPreference) findPreference(getString(R.string.key_widget_icon_style));
        widgetIconStyle.setSummary(
                ValueUtils.getIconStyle(
                        getActivity(),
                        sharedPreferences.getString(getString(R.string.key_widget_icon_style), "material")));
        widgetIconStyle.setOnPreferenceChangeListener(this);
    }

    private void initNotificationPart(SharedPreferences sharedPreferences) {
        // widget icon style.
        ListPreference notificationIconStyle = (ListPreference) findPreference(getString(R.string.key_notification_icon_style));
        notificationIconStyle.setSummary(
                ValueUtils.getIconStyle(
                        getActivity(),
                        sharedPreferences.getString(getString(R.string.key_notification_icon_style), "material")));
        notificationIconStyle.setOnPreferenceChangeListener(this);

        // notification temp icon.
        CheckBoxPreference notificationTempIcon = (CheckBoxPreference) findPreference(getString(R.string.key_notification_temp_icon));

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

        // notification hide big view.
        CheckBoxPreference notificationHideBigView = (CheckBoxPreference) findPreference(getString(R.string.key_notification_hide_big_view));

        if(sharedPreferences.getBoolean(getString(R.string.key_notification), false)) {
            // open notification.
            notificationIconStyle.setEnabled(true);
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
            notificationIconStyle.setEnabled(false);
            notificationTempIcon.setEnabled(false);
            notificationTextColor.setEnabled(false);
            notificationBackground.setEnabled(false);
            notificationClearFlag.setEnabled(false);
            notificationIconBehavior.setEnabled(false);
            notificationHideBehavior.setEnabled(false);
            notificationHideBigView.setEnabled(false);
        }
    }

    private boolean hasChineseLocation() {
        if (LanguageUtils.buildLocale(GeometricWeather.getInstance().getLanguage())
                .getLanguage().toLowerCase().equals("zh")) {
            return true;
        }

        List<Location> list = DatabaseHelper.getInstance(getActivity()).readLocationList();
        for (int i = 0; i < list.size(); i ++) {
            if (LanguageUtils.isChinese(list.get(i).city)
                    || LanguageUtils.isChinese(list.get(i).prov)
                    || LanguageUtils.isChinese(list.get(i).cnty)) {
                return true;
            }
        }

        return false;
    }

    // interface.

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (preference.getKey().equals(getString(R.string.key_background_free))) {
            // background free.
            ServiceHelper.resetNormalService(getActivity(), false, true);
        } else if (preference.getKey().equals(getString(R.string.key_chinese_source))) {
            // Chinese source.
            SnackbarUtils.showSnackbar(getString(R.string.feedback_readd_location_after_changing_source));
        } else if (preference.getKey().equals(getString(R.string.key_navigationBar_color))) {
            // navigation bar color.
            GeometricWeather.getInstance().setColorNavigationBar();
            DisplayUtils.setNavigationBarColor(getActivity(), 0);
        } else if (preference.getKey().equals(getString(R.string.key_fahrenheit))) {
            // ℉
            GeometricWeather.getInstance().setFahrenheit(!GeometricWeather.getInstance().isFahrenheit());
            SnackbarUtils.showSnackbar(getString(R.string.feedback_restart));
        } else if (preference.getKey().equals(getString(R.string.key_imperial))) {
            // imperial units.
            GeometricWeather.getInstance().setImperial(!GeometricWeather.getInstance().isImperial());
            SnackbarUtils.showSnackbar(getString(R.string.feedback_restart));
        } else if (preference.getKey().equals(getString(R.string.key_forecast_today))) {
            // forecast today.
            initForecastPart(sharedPreferences);
            ServiceHelper.resetForecastService(getActivity(), true);
        } else if (preference.getKey().equals(getString(R.string.key_forecast_today_time))) {
            // set today forecast time.
            TimeSetterDialog dialog = new TimeSetterDialog();
            dialog.setModel(true);
            dialog.setOnTimeChangedListener(this);
            dialog.show(getFragmentManager(), null);
        } else if (preference.getKey().equals(getString(R.string.key_forecast_tomorrow))) {
            // timing forecast tomorrow.
            initForecastPart(sharedPreferences);
            ServiceHelper.resetForecastService(getActivity(), false);
        } else if (preference.getKey().equals(getString(R.string.key_forecast_tomorrow_time))) {
            // set tomorrow forecast time.
            TimeSetterDialog dialog = new TimeSetterDialog();
            dialog.setModel(false);
            dialog.setOnTimeChangedListener(this);
            dialog.show(getFragmentManager(), null);
        } else if (preference.getKey().equals(getString(R.string.key_click_widget_to_refresh))) {
            // click widget to refresh.
            ServiceHelper.resetNormalService(getActivity(), false, true);
            SnackbarUtils.showSnackbar(getString(R.string.feedback_refresh_notification_now));
        } else if (preference.getKey().equals(getString(R.string.key_notification))) {
            // notification switch.
            initNotificationPart(sharedPreferences);
            if (sharedPreferences.getBoolean(getString(R.string.key_notification), false)) {
                // open notification.
                ServiceHelper.resetNormalService(getActivity(), false, true);
                SnackbarUtils.showSnackbar(getString(R.string.feedback_refresh_notification_now));
            } else {
                // close notification.
                NormalNotificationUtils.cancelNotification(getActivity());
                ServiceHelper.resetNormalService(getActivity(), false, true);
            }
        } else if (preference.getKey().equals(getString(R.string.key_notification_temp_icon))) {
            // notification temp icon.
            ServiceHelper.resetNormalService(getActivity(), false, true);
            SnackbarUtils.showSnackbar(getString(R.string.feedback_refresh_notification_now));
        } else if (preference.getKey().equals(getString(R.string.key_notification_background))) {
            // notification background.
            ServiceHelper.resetNormalService(getActivity(), false, true);
            SnackbarUtils.showSnackbar(getString(R.string.feedback_refresh_notification_now));
        } else if (preference.getKey().equals(getString(R.string.key_notification_can_be_cleared))) {
            // notification clear flag.
            ServiceHelper.resetNormalService(getActivity(), false, true);
            SnackbarUtils.showSnackbar(getString(R.string.feedback_refresh_notification_now));
        } else if (preference.getKey().equals(getString(R.string.key_notification_hide_icon))) {
            ServiceHelper.resetNormalService(getActivity(), false, true);
            SnackbarUtils.showSnackbar(getString(R.string.feedback_refresh_notification_now));
        } else if (preference.getKey().equals(getString(R.string.key_notification_hide_in_lockScreen))) {
            ServiceHelper.resetNormalService(getActivity(), false, true);
            SnackbarUtils.showSnackbar(getString(R.string.feedback_refresh_notification_now));
        } else if (preference.getKey().equals(getString(R.string.key_notification_hide_big_view))) {
            ServiceHelper.resetNormalService(getActivity(), false, true);
            SnackbarUtils.showSnackbar(getString(R.string.feedback_refresh_notification_now));
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        if (preference.getKey().equals(getString(R.string.key_chinese_source))) {
            // Chinese source.
            if (!GeometricWeather.getInstance().getChineseSource().equals(o)) {
                DatabaseHelper.getInstance(getActivity()).clearLocation();
                LocationHelper.clearLocationCache(getActivity());
                SnackbarUtils.showSnackbar(getString(R.string.feedback_readd_location));
            }
            GeometricWeather.getInstance().setChineseSource((String) o);
            preference.setSummary(ValueUtils.getChineseSource(getActivity(), (String) o));
        } if (preference.getKey().equals(getString(R.string.key_ui_style))) {
            // UI style.
            preference.setSummary(ValueUtils.getUIStyle(getActivity(), (String) o));
            SnackbarUtils.showSnackbar(getString(R.string.feedback_restart));
        } else if (preference.getKey().equals(getString(R.string.key_card_order))) {
            // Card order.
            GeometricWeather.getInstance().setCardOrder((String) o);
            preference.setSummary(ValueUtils.getCardOrder(getActivity(), (String) o));
            SnackbarUtils.showSnackbar(getString(R.string.feedback_refresh_ui_after_refresh));
        } else if (preference.getKey().equals(getString(R.string.key_refresh_rate))) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
            editor.putString(getString(R.string.key_refresh_rate), (String) o);
            editor.apply();
            preference.setSummary((String) o);
            ServiceHelper.resetNormalService(getActivity(), false, true);
        } else if (preference.getKey().equals(getString(R.string.key_language))) {
            preference.setSummary(ValueUtils.getLanguage(getActivity(), (String) o));
            GeometricWeather.getInstance().setLanguage((String) o);
            SnackbarUtils.showSnackbar(getString(R.string.feedback_restart));
        } else if (preference.getKey().equals(getString(R.string.key_widget_icon_style))
                || preference.getKey().equals(getString(R.string.key_notification_icon_style))) {
            ServiceHelper.resetNormalService(getActivity(), false, true);
            SnackbarUtils.showSnackbar(getString(R.string.feedback_refresh_notification_now));
            preference.setSummary(ValueUtils.getIconStyle(getActivity(), (String) o));
        } else if (preference.getKey().equals(getString(R.string.key_notification_text_color))) {
            // notification text color.
            ServiceHelper.resetNormalService(getActivity(), false, true);
            SnackbarUtils.showSnackbar(getString(R.string.feedback_refresh_notification_now));
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
            ServiceHelper.resetForecastService(getActivity(), true);
            ServiceHelper.resetForecastService(getActivity(), false);
        }
    }
}