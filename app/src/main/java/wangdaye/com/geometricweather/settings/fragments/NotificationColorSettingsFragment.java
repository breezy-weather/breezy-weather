package wangdaye.com.geometricweather.settings.fragments;

import android.os.Bundle;

import androidx.preference.ListPreference;

import com.jaredrummler.android.colorpicker.ColorPreferenceCompat;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.polling.PollingManager;

/**
 * Notification color settings fragment.
 * */

public class NotificationColorSettingsFragment extends AbstractSettingsFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.perference_notification_color);
        initNotificationPart();
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // do nothing.
    }

    private void initNotificationPart() {
        // notification custom color.
        findPreference(getString(R.string.key_notification_custom_color)).setOnPreferenceChangeListener((p, newValue) -> {
            requireView().post(this::initNotificationPart);
            PollingManager.resetNormalBackgroundTask(getActivity(), true);
            return true;
        });

        // notification background.
        ColorPreferenceCompat notificationBackgroundColor = findPreference(getString(R.string.key_notification_background_color));
        notificationBackgroundColor.setEnabled(getSettingsOptionManager().isNotificationCustomColorEnabled());
        notificationBackgroundColor.setOnPreferenceChangeListener((preference, newValue) -> {
            PollingManager.resetNormalBackgroundTask(getActivity(), true);
            return true;
        });

        // notification text color.
        ListPreference notificationTextColor = findPreference(getString(R.string.key_notification_text_color));
        notificationTextColor.setSummary(
                getSettingsOptionManager().getNotificationTextColor().getNotificationTextColorName(
                        requireContext())
        );
        notificationTextColor.setOnPreferenceChangeListener((preference, newValue) -> {
            PollingManager.resetNormalBackgroundTask(getActivity(), true);
            preference.setSummary(
                    getSettingsOptionManager().getNotificationTextColor().getNotificationTextColorName(
                            requireContext())
            );
            return true;
        });
    }
}