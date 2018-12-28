package wangdaye.com.geometricweather.ui.fragment;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.utils.SnackbarUtils;

/**
 * Unit settings fragment.
 * */

public class UnitSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.perference_unit);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // do nothing.
    }

    // interface.

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getKey().equals(getString(R.string.key_fahrenheit))) {
            // ℉
            GeometricWeather.getInstance().setFahrenheit(!GeometricWeather.getInstance().isFahrenheit());
            SnackbarUtils.showSnackbar(getString(R.string.feedback_restart));
        } else if (preference.getKey().equals(getString(R.string.key_imperial))) {
            // imperial units.
            GeometricWeather.getInstance().setImperial(!GeometricWeather.getInstance().isImperial());
            SnackbarUtils.showSnackbar(getString(R.string.feedback_restart));
        }
        return super.onPreferenceTreeClick(preference);
    }
}