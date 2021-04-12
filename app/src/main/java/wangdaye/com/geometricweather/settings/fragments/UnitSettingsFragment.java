package wangdaye.com.geometricweather.settings.fragments;

import android.os.Bundle;

import androidx.preference.ListPreference;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.utils.helpers.SnackbarHelper;

/**
 * Unit settings fragment.
 * */

public class UnitSettingsFragment extends AbstractSettingsFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.perference_unit);

        // temperature.
        ListPreference temperature = findPreference(getString(R.string.key_temperature_unit));
        temperature.setSummary(
                getSettingsOptionManager().getTemperatureUnit().getAbbreviation(requireActivity())
        );
        temperature.setOnPreferenceChangeListener((p, newValue) -> {
            temperature.setSummary(
                    getSettingsOptionManager().getTemperatureUnit().getAbbreviation(requireActivity())
            );
            SnackbarHelper.showSnackbar(getString(R.string.feedback_refresh_ui_after_refresh));
            return true;
        });

        // distance.
        ListPreference distance = findPreference(getString(R.string.key_distance_unit));
        distance.setSummary(getSettingsOptionManager().getDistanceUnit().getAbbreviation(requireActivity()));
        distance.setOnPreferenceChangeListener((p, newValue) -> {
            distance.setSummary(getSettingsOptionManager().getDistanceUnit().getAbbreviation(requireActivity()));
            SnackbarHelper.showSnackbar(getString(R.string.feedback_refresh_ui_after_refresh));
            return true;
        });

        // precipitation.
        ListPreference precipitation = findPreference(getString(R.string.key_precipitation_unit));
        precipitation.setSummary(getSettingsOptionManager().getPrecipitationUnit().getAbbreviation(requireActivity()));
        precipitation.setOnPreferenceChangeListener((p, newValue) -> {
            precipitation.setSummary(getSettingsOptionManager().getPrecipitationUnit().getAbbreviation(requireActivity()));
            SnackbarHelper.showSnackbar(getString(R.string.feedback_refresh_ui_after_refresh));
            return true;
        });

        // pressure.
        ListPreference pressure = findPreference(getString(R.string.key_pressure_unit));
        pressure.setSummary(getSettingsOptionManager().getPressureUnit().getAbbreviation(requireActivity()));
        pressure.setOnPreferenceChangeListener((p, newValue) -> {
            pressure.setSummary(getSettingsOptionManager().getPressureUnit().getAbbreviation(requireActivity()));
            SnackbarHelper.showSnackbar(getString(R.string.feedback_refresh_ui_after_refresh));
            return true;
        });

        // speed.
        ListPreference speed = findPreference(getString(R.string.key_speed_unit));
        speed.setSummary(getSettingsOptionManager().getSpeedUnit().getAbbreviation(requireActivity()));
        speed.setOnPreferenceChangeListener((p, newValue) -> {
            speed.setSummary(getSettingsOptionManager().getSpeedUnit().getAbbreviation(requireActivity()));
            SnackbarHelper.showSnackbar(getString(R.string.feedback_refresh_ui_after_refresh));
            return true;
        });
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // do nothing.
    }
}