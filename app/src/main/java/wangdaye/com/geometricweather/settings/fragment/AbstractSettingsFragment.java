package wangdaye.com.geometricweather.settings.fragment;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.settings.activity.SettingsActivity;
import wangdaye.com.geometricweather.utils.ValueUtils;

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

    protected String getNameByValue(String value,
                                    @ArrayRes int nameArrayId, @ArrayRes int valueArrayId) {
        return ValueUtils.getNameByValue(requireActivity(), value, nameArrayId, valueArrayId);
    }

    protected SettingsOptionManager getSettingsOptionManager() {
        return SettingsOptionManager.getInstance(requireActivity());
    }

    protected void pushFragment(PreferenceFragmentCompat f, String key) {
        ((SettingsActivity) requireActivity()).pushFragment(f, key);
    }
}
