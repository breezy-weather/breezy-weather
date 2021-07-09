package wangdaye.com.geometricweather.settings.fragments;

import android.os.Bundle;

import androidx.preference.EditTextPreference;

import wangdaye.com.geometricweather.R;

/**
 * Service provider settings fragment.
 */

public class ServiceProviderAdvancedSettingsFragment extends AbstractSettingsFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.perference_service_provider_advanced);
        initPreferences();
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // do nothing.
    }

    private void initPreferences() {
        initPreference(R.string.key_provider_accu_weather_key, getSettingsOptionManager().getProviderAccuWeatherKey(false));
        initPreference(R.string.key_provider_accu_current_key, getSettingsOptionManager().getProviderAccuCurrentKey(false));
        initPreference(R.string.key_provider_accu_aqi_key, getSettingsOptionManager().getProviderAccuAqiKey(false));
        initPreference(R.string.key_provider_owm_key, getSettingsOptionManager().getProviderOwmKey(false));
        initPreference(R.string.key_provider_baidu_ip_location_ak, getSettingsOptionManager().getProviderBaiduIpLocationAk(false));
        initPreference(R.string.key_provider_mf_wsft_key, getSettingsOptionManager().getProviderMfWsftKey(false));
        initPreference(R.string.key_provider_iqa_air_parif_key, getSettingsOptionManager().getProviderIqaAirParifKey(false));
        initPreference(R.string.key_provider_iqa_atmo_aura_key, getSettingsOptionManager().getProviderIqaAtmoAuraKey(false));
    }

    private void initPreference(int stringKey, String currentValue) {
        EditTextPreference pref = findPreference(getString(stringKey));
        pref.setSummary(currentValue != null ? currentValue : getString(R.string.settings_provider_default_value));
        pref.setText(currentValue);
        pref.setOnPreferenceChangeListener((preference, newValue) -> {
            // Allow to reset to default value if left empty
            if (newValue.toString().isEmpty()) {
                preference.setSummary(getString(R.string.settings_provider_default_value));
            } else {
                preference.setSummary(newValue.toString());
            }
            return true;
        });
    }
}