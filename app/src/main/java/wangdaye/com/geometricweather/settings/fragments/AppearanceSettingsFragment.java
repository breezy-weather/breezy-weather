package wangdaye.com.geometricweather.settings.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.options.appearance.CardDisplay;
import wangdaye.com.geometricweather.common.basic.models.options.appearance.DailyTrendDisplay;
import wangdaye.com.geometricweather.common.basic.models.options.appearance.Language;
import wangdaye.com.geometricweather.common.basic.models.options.appearance.UIStyle;
import wangdaye.com.geometricweather.common.basic.models.weather.Temperature;
import wangdaye.com.geometricweather.resource.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.settings.dialogs.ProvidersPreviewerDialog;
import wangdaye.com.geometricweather.common.utils.helpers.SnackbarHelper;
import wangdaye.com.geometricweather.common.utils.helpers.IntentHelper;

/**
 * Appearance settings fragment.
 * */

public class AppearanceSettingsFragment extends AbstractSettingsFragment {

    private final BroadcastReceiver selectResourceProviderCallback = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String packageName = intent.getStringExtra(ProvidersPreviewerDialog.KEY_PACKAGE_NAME);
            if (packageName == null) {
                return;
            }

            getSettingsOptionManager().setIconProvider(packageName);
            PreferenceManager.getDefaultSharedPreferences(requireActivity())
                    .edit()
                    .putString(getString(R.string.key_icon_provider), packageName)
                    .apply();
            initIconProviderPreference();
            SnackbarHelper.showSnackbar(
                    getString(R.string.feedback_refresh_ui_after_refresh));
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.perference_appearance);

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                selectResourceProviderCallback,
                new IntentFilter(ProvidersPreviewerDialog.ACTION_RESOURCE_PROVIDER_CHANGED)
        );

        // ui style.
        Preference uiStyle = findPreference(getString(R.string.key_ui_style));
        uiStyle.setSummary(getSettingsOptionManager().getUiStyle().getUIStyleName(requireActivity()));
        uiStyle.setOnPreferenceChangeListener((preference, newValue) -> {
            getSettingsOptionManager().setUiStyle(UIStyle.getInstance((String) newValue));
            preference.setSummary(getSettingsOptionManager().getUiStyle().getUIStyleName(requireActivity()));
            SnackbarHelper.showSnackbar(
                    getString(R.string.feedback_restart),
                    getString(R.string.restart),
                    v -> GeometricWeather.getInstance().recreateAllActivities()
            );
            return true;
        });

        // icon provider.
        initIconProviderPreference();

        // set card display preference in onStart().
        // set daily trend display preference in onStart().

        // horizontal lines in trend.
        findPreference(getString(R.string.key_trend_horizontal_line_switch)).setOnPreferenceChangeListener((preference, newValue) -> {
            getSettingsOptionManager().setTrendHorizontalLinesEnabled((Boolean) newValue);
            return true;
        });

        // exchange day night temperature.
        Preference exchangeDayNightTemperature = findPreference(getString(R.string.key_exchange_day_night_temp_switch));
        exchangeDayNightTemperature.setSummary(
                Temperature.getTrendTemperature(
                        requireActivity(),
                        3,
                        7,
                        SettingsOptionManager.getInstance(requireActivity()).getTemperatureUnit()
                )
        );
        exchangeDayNightTemperature.setOnPreferenceChangeListener((preference, newValue) -> {
            getSettingsOptionManager().setExchangeDayNightTempEnabled((Boolean) newValue);
            preference.setSummary(
                    Temperature.getTrendTemperature(
                            requireActivity(),
                            3,
                            7,
                            SettingsOptionManager.getInstance(requireActivity()).getTemperatureUnit()
                    )
            );
            return true;
        });

        // sensor.
        findPreference(getString(R.string.key_gravity_sensor_switch)).setOnPreferenceChangeListener((preference, newValue) -> {
            getSettingsOptionManager().setGravitySensorEnabled((Boolean) newValue);
            return true;
        });

        // list animation.
        findPreference(getString(R.string.key_list_animation_switch)).setOnPreferenceChangeListener((preference, newValue) -> {
            getSettingsOptionManager().setListAnimationEnabled((Boolean) newValue);
            return true;
        });

        // item animation.
        findPreference(getString(R.string.key_item_animation_switch)).setOnPreferenceChangeListener((preference, newValue) -> {
            getSettingsOptionManager().setItemAnimationEnabled((Boolean) newValue);
            return true;
        });

        // language.
        Preference language = findPreference(getString(R.string.key_language));
        language.setSummary(getSettingsOptionManager().getLanguage().getLanguageName(requireActivity()));
        language.setOnPreferenceChangeListener((preference, newValue) -> {
            getSettingsOptionManager().setLanguage(Language.getInstance((String) newValue));
            preference.setSummary(getSettingsOptionManager().getLanguage().getLanguageName(requireActivity()));
            SnackbarHelper.showSnackbar(
                    getString(R.string.feedback_restart),
                    getString(R.string.restart),
                    v -> GeometricWeather.getInstance().recreateAllActivities()
            );
            return true;
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // card display.
        Preference cardDisplay = findPreference(getString(R.string.key_card_display));
        cardDisplay.setSummary(CardDisplay.getSummary(
                requireActivity(),
                SettingsOptionManager.getInstance(requireActivity()).getCardDisplayList()
        ));
        cardDisplay.setOnPreferenceClickListener(preference -> {
            IntentHelper.startCardDisplayManageActivityForResult(requireActivity(), 0);
            return true;
        });

        // daily trend display.
        Preference dailyTrendDisplay = findPreference(getString(R.string.key_daily_trend_display));
        dailyTrendDisplay.setSummary(DailyTrendDisplay.getSummary(
                requireActivity(),
                SettingsOptionManager.getInstance(requireActivity()).getDailyTrendDisplayList()
        ));
        dailyTrendDisplay.setOnPreferenceClickListener(preference -> {
            IntentHelper.startDailyTrendDisplayManageActivityForResult(requireActivity(), 1);
            return true;
        });
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // do nothing.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(
                selectResourceProviderCallback);
    }

    private void initIconProviderPreference() {
        Preference iconProvider = findPreference(getString(R.string.key_icon_provider));
        iconProvider.setSummary(ResourcesProviderFactory.getNewInstance().getProviderName());
        
        iconProvider.setOnPreferenceClickListener(preference -> {
            new ProvidersPreviewerDialog().show(getParentFragmentManager(), null);
            return true;
        });
    }
}