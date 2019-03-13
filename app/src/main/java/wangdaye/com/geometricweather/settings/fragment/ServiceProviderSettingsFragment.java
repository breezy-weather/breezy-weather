package wangdaye.com.geometricweather.settings.fragment;

import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.utils.ValueUtils;

/**
 * Service provider settings fragment.
 * */

public class ServiceProviderSettingsFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.perference_service_provider);

        Preference chineseSource = findPreference(getString(R.string.key_chinese_source));
        chineseSource.setSummary(
                ValueUtils.getWeatherSource(
                        getActivity(),
                        GeometricWeather.getInstance().getChineseSource()));
        chineseSource.setOnPreferenceChangeListener(this);

        Preference locationService = findPreference(getString(R.string.key_location_service));
        locationService.setSummary(
                ValueUtils.getLocationService(
                        getActivity(),
                        GeometricWeather.getInstance().getLocationService()));
        locationService.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // do nothing.
    }

    // interface.

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        if (preference.getKey().equals(getString(R.string.key_chinese_source))) {
            // Chinese source.
            GeometricWeather.getInstance().setChineseSource((String) o);
            preference.setSummary(ValueUtils.getWeatherSource(getActivity(), (String) o));
        } else if (preference.getKey().equals(getString(R.string.key_location_service))) {
            // Location service.
            GeometricWeather.getInstance().setLocationService((String) o);
            preference.setSummary(ValueUtils.getLocationService(getActivity(), (String) o));
            SnackbarUtils.showSnackbar(getString(R.string.feedback_restart));
        }
        return true;
    }
}