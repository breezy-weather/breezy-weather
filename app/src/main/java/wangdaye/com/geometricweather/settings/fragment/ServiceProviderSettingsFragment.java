package wangdaye.com.geometricweather.settings.fragment;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import androidx.preference.PreferenceManager;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.location.service.AndroidLocationService;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.dialog.LearnMoreAboutGeocoderDialog;
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
        initPreferences();
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // do nothing.
    }

    private void initPreferences() {
        Preference chineseSource = findPreference(getString(R.string.key_chinese_source));
        chineseSource.setSummary(
                ValueUtils.getWeatherSource(
                        getActivity(),
                        SettingsOptionManager.getInstance(getActivity()).getChineseSource()
                )
        );
        chineseSource.setOnPreferenceChangeListener(this);

        Preference locationService = findPreference(getString(R.string.key_location_service));
        if (locationService != null) {
            ((ListPreference) locationService).setValue(
                    SettingsOptionManager.getInstance(getActivity()).getLocationService()
            );
            locationService.setSummary(
                    ValueUtils.getLocationService(
                            getActivity(),
                            SettingsOptionManager.getInstance(getActivity()).getLocationService()
                    )
            );
            locationService.setOnPreferenceChangeListener(this);
        }
    }

    // interface.

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        if (preference.getKey().equals(getString(R.string.key_chinese_source))) {
            // Chinese source.
            SettingsOptionManager.getInstance(getActivity()).setChineseSource((String) o);
            preference.setSummary(ValueUtils.getWeatherSource(getActivity(), (String) o));

            if (!SettingsOptionManager.getInstance(getActivity()).getChineseSource().equals("accu")
                    && SettingsOptionManager.getInstance(getActivity()).getLocationService().equals("native")
                    && !AndroidLocationService.hasValidGeocoder()) {
                SettingsOptionManager.getInstance(getActivity()).setLocationService("baidu");
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putString(getString(R.string.key_location_service), "baidu")
                        .apply();

                initPreferences();

                SnackbarUtils.showSnackbar(
                        getString(R.string.feedback_unusable_geocoder),
                        getString(R.string.learn_more),
                        v -> new LearnMoreAboutGeocoderDialog().show(getFragmentManager(), null)
                );
            }
        } else if (preference.getKey().equals(getString(R.string.key_location_service))) {
            // Location service.
            SettingsOptionManager.getInstance(getActivity()).setLocationService((String) o);
            preference.setSummary(ValueUtils.getLocationService(getActivity(), (String) o));
            SnackbarUtils.showSnackbar(getString(R.string.feedback_restart));
        }
        return true;
    }
}