package wangdaye.com.geometricweather.settings.fragment;

import android.os.Bundle;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.utils.SnackbarUtils;

/**
 * Unit settings fragment.
 * */

public class UnitSettingsFragment extends AbstractSettingsFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.perference_unit);

        // ℉.
        findPreference(getString(R.string.key_fahrenheit)).setOnPreferenceChangeListener((p, newValue) -> {
            getSettingsOptionManager().setFahrenheit((Boolean) newValue);
            SnackbarUtils.showSnackbar(
                    (GeoActivity) requireActivity(), getString(R.string.feedback_refresh_ui_after_refresh));
            return true;
        });

        // imperial.
        findPreference(getString(R.string.key_imperial)).setOnPreferenceChangeListener((p, newValue) -> {
            getSettingsOptionManager().setImperial((Boolean) newValue);
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