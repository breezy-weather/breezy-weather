package wangdaye.com.geometricweather.settings.fragments;

import android.os.Bundle;

import androidx.preference.ListPreference;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.models.options.unit.DistanceUnit;
import wangdaye.com.geometricweather.basic.models.options.unit.PrecipitationUnit;
import wangdaye.com.geometricweather.basic.models.options.unit.PressureUnit;
import wangdaye.com.geometricweather.basic.models.options.unit.SpeedUnit;
import wangdaye.com.geometricweather.basic.models.options.unit.TemperatureUnit;
import wangdaye.com.geometricweather.utils.helpters.SnackbarHelper;

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
            getSettingsOptionManager().setTemperatureUnit(TemperatureUnit.getInstance((String) newValue));
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
            getSettingsOptionManager().setDistanceUnit(DistanceUnit.getInstance((String) newValue));
            distance.setSummary(getSettingsOptionManager().getDistanceUnit().getAbbreviation(requireActivity()));
            SnackbarHelper.showSnackbar(getString(R.string.feedback_refresh_ui_after_refresh));
            return true;
        });

        // precipitation.
        ListPreference precipitation = findPreference(getString(R.string.key_precipitation_unit));
        precipitation.setSummary(getSettingsOptionManager().getPrecipitationUnit().getAbbreviation(requireActivity()));
        precipitation.setOnPreferenceChangeListener((p, newValue) -> {
            getSettingsOptionManager().setPrecipitationUnit(PrecipitationUnit.getInstance((String) newValue));
            precipitation.setSummary(getSettingsOptionManager().getPrecipitationUnit().getAbbreviation(requireActivity()));
            SnackbarHelper.showSnackbar(getString(R.string.feedback_refresh_ui_after_refresh));
            return true;
        });

        // pressure.
        ListPreference pressure = findPreference(getString(R.string.key_pressure_unit));
        pressure.setSummary(getSettingsOptionManager().getPressureUnit().getAbbreviation(requireActivity()));
        pressure.setOnPreferenceChangeListener((p, newValue) -> {
            getSettingsOptionManager().setPressureUnit(PressureUnit.getInstance((String) newValue));
            pressure.setSummary(getSettingsOptionManager().getPressureUnit().getAbbreviation(requireActivity()));
            SnackbarHelper.showSnackbar(getString(R.string.feedback_refresh_ui_after_refresh));
            return true;
        });

        // speed.
        ListPreference speed = findPreference(getString(R.string.key_speed_unit));
        speed.setSummary(getSettingsOptionManager().getSpeedUnit().getAbbreviation(requireActivity()));
        speed.setOnPreferenceChangeListener((p, newValue) -> {
            getSettingsOptionManager().setSpeedUnit(SpeedUnit.getInstance((String) newValue));
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