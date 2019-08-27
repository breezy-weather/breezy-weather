package wangdaye.com.geometricweather.settings.fragment;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import androidx.preference.PreferenceManager;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.location.service.AndroidLocationService;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.dialog.LearnMoreAboutGeocoderDialog;
import wangdaye.com.geometricweather.utils.SnackbarUtils;

/**
 * Service provider settings fragment.
 * */

public class ServiceProviderSettingsFragment extends AbstractSettingsFragment {

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
        // chinese source.
        Preference chineseSource = findPreference(getString(R.string.key_chinese_source));
        chineseSource.setSummary(
                getNameByValue(
                        getSettingsOptionManager().getChineseSource(),
                        R.array.chinese_sources,
                        R.array.chinese_source_values
                )
        );
        chineseSource.setOnPreferenceChangeListener((preference, newValue) -> {
            getSettingsOptionManager().setChineseSource((String) newValue);
            preference.setSummary(
                    getNameByValue(
                            (String) newValue,
                            R.array.chinese_sources,
                            R.array.chinese_source_values
                    )
            );

            if (!getSettingsOptionManager().getChineseSource().equals(SettingsOptionManager.WEATHER_SOURCE_ACCU)
                    && getSettingsOptionManager().getLocationService().equals(SettingsOptionManager.LOCATION_SERIVCE_NATIVE)
                    && !AndroidLocationService.geocoderEnabled()) {
                getSettingsOptionManager().setLocationService(SettingsOptionManager.LOCATION_SERIVCE_BAIDU);
                PreferenceManager.getDefaultSharedPreferences(requireActivity())
                        .edit()
                        .putString(
                                getString(R.string.key_location_service),
                                SettingsOptionManager.LOCATION_SERIVCE_BAIDU
                        ).apply();

                initPreferences();

                SnackbarUtils.showSnackbar(
                        (GeoActivity) requireActivity(),
                        getString(R.string.feedback_unusable_geocoder),
                        getString(R.string.learn_more),
                        v -> new LearnMoreAboutGeocoderDialog().show(requireFragmentManager(), null)
                );
            }

            List<Location> locationList = DatabaseHelper.getInstance(requireActivity()).readLocationList();
            for (int i = 0; i < locationList.size(); i ++) {
                if (locationList.get(i).isCurrentPosition()) {
                    locationList.get(i).source = (String) newValue;
                    break;
                }
            }
            DatabaseHelper.getInstance(requireActivity()).writeLocationList(locationList);
            return true;
        });

        // location source.
        Preference locationService = findPreference(getString(R.string.key_location_service));
        ((ListPreference) locationService).setValue(getSettingsOptionManager().getLocationService());
        locationService.setSummary(
                getNameByValue(
                        getSettingsOptionManager().getLocationService(),
                        R.array.location_services,
                        R.array.location_service_values
                )
        );
        locationService.setOnPreferenceChangeListener((preference, newValue) -> {
            getSettingsOptionManager().setLocationService((String) newValue);
            preference.setSummary(
                    getNameByValue(
                            (String) newValue,
                            R.array.location_services,
                            R.array.location_service_values
                    )
            );
            SnackbarUtils.showSnackbar(
                    (GeoActivity) requireActivity(), getString(R.string.feedback_restart));
            return true;
        });
    }
}