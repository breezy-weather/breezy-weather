package wangdaye.com.geometricweather.settings.fragment;

import android.os.Bundle;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.settings.OptionMapper;
import wangdaye.com.geometricweather.utils.SnackbarUtils;

/**
 * Unit settings fragment.
 * */

public class UnitSettingsFragment extends AbstractSettingsFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.perference_unit);

        // temperature.
        findPreference(getString(R.string.key_temperature_unit)).setOnPreferenceChangeListener((p, newValue) -> {
            getSettingsOptionManager().setTemperatureUnit(OptionMapper.getTemperatureUnit((String) newValue));
            SnackbarUtils.showSnackbar(
                    (GeoActivity) requireActivity(), getString(R.string.feedback_refresh_ui_after_refresh));
            return true;
        });

        // distance.
        findPreference(getString(R.string.key_distance_unit)).setOnPreferenceChangeListener((p, newValue) -> {
            getSettingsOptionManager().setDistanceUnit(OptionMapper.getDistanceUnit((String) newValue));
            SnackbarUtils.showSnackbar(
                    (GeoActivity) requireActivity(), getString(R.string.feedback_refresh_ui_after_refresh));
            return true;
        });

        // precipitation.
        findPreference(getString(R.string.key_precipitation_unit)).setOnPreferenceChangeListener((p, newValue) -> {
            getSettingsOptionManager().setPrecipitationUnit(OptionMapper.getPrecipitationUnit((String) newValue));
            SnackbarUtils.showSnackbar(
                    (GeoActivity) requireActivity(), getString(R.string.feedback_refresh_ui_after_refresh));
            return true;
        });

        // pressure.
        findPreference(getString(R.string.key_pressure_unit)).setOnPreferenceChangeListener((p, newValue) -> {
            getSettingsOptionManager().setPressureUnit(OptionMapper.getPressureUnit((String) newValue));
            SnackbarUtils.showSnackbar(
                    (GeoActivity) requireActivity(), getString(R.string.feedback_refresh_ui_after_refresh));
            return true;
        });

        // speed.
        findPreference(getString(R.string.key_speed_unit)).setOnPreferenceChangeListener((p, newValue) -> {
            getSettingsOptionManager().setSpeedUnit(OptionMapper.getSpeedUnit((String) newValue));
            SnackbarUtils.showSnackbar(
                    (GeoActivity) requireActivity(), getString(R.string.feedback_refresh_ui_after_refresh));
            return true;
        });
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // do nothing.
    }
}