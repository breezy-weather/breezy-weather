package wangdaye.com.geometricweather.settings.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.jaredrummler.android.colorpicker.ColorPreferenceCompat;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.BackgroundManager;
import wangdaye.com.geometricweather.utils.ValueUtils;

/**
 * Notification color settings fragment.
 * */

public class NotificationColorSettingsFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.perference_notification_color);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        initNotificationPart(sharedPreferences);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // do nothing.
    }

    private void initNotificationPart(SharedPreferences sharedPreferences) {
        // notification background.
        ColorPreferenceCompat notificationBackgroundColor = findPreference(getString(R.string.key_notification_background_color));
        notificationBackgroundColor.setOnPreferenceChangeListener(this);

        // notification text color.
        ListPreference notificationTextColor = findPreference(getString(R.string.key_notification_text_color));
        notificationTextColor.setSummary(
                ValueUtils.getNotificationTextColor(
                        getActivity(),
                        sharedPreferences.getString(getString(R.string.key_notification_text_color), "dark")
                )
        );
        notificationTextColor.setOnPreferenceChangeListener(this);

        if(sharedPreferences.getBoolean(getString(R.string.key_notification_custom_color), false)) {
            // custom color.
            notificationBackgroundColor.setEnabled(true);
        } else {
            // follow system.
            notificationBackgroundColor.setEnabled(false);
        }
    }

    // interface.

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (preference.getKey().equals(getString(R.string.key_notification_custom_color))) {
            // custom color.
            initNotificationPart(sharedPreferences);
            BackgroundManager.resetNormalBackgroundTask(getActivity(), true);
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        if (preference.getKey().equals(getString(R.string.key_notification_background_color))) {
            // notification background.
            BackgroundManager.resetNormalBackgroundTask(getActivity(), true);
        } else if (preference.getKey().equals(getString(R.string.key_notification_text_color))) {
            // notification text color.
            BackgroundManager.resetNormalBackgroundTask(getActivity(), true);
            preference.setSummary(ValueUtils.getNotificationTextColor(getActivity(), (String) o));
        }
        return true;
    }
}