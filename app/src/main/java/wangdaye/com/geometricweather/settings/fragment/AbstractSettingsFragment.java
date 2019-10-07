package wangdaye.com.geometricweather.settings.fragment;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.settings.activity.SettingsActivity;

public abstract class AbstractSettingsFragment extends PreferenceFragmentCompat {

    @NonNull
    @Override
    public <T extends Preference> T findPreference(@NonNull CharSequence key) {
        T result = super.findPreference(key);
        if (result == null) {
            throw new NullPointerException("Cannot find preference");
        } else {
            return result;
        }
    }

    protected SettingsOptionManager getSettingsOptionManager() {
        return SettingsOptionManager.getInstance(requireActivity());
    }

    protected void pushFragment(PreferenceFragmentCompat f, String key) {
        ((SettingsActivity) requireActivity()).pushFragment(f, key);
    }
}
