package wangdaye.com.geometricweather.settings.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.preference.EditTextPreference;
import androidx.preference.EditTextPreferenceDialogFragmentCompat;
import androidx.preference.ListPreference;
import androidx.preference.ListPreferenceDialogFragmentCompat;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.MultiSelectListPreferenceDialogFragmentCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import wangdaye.com.geometricweather.basic.GeoDialog;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.settings.activities.SettingsActivity;

public abstract class AbstractSettingsFragment extends PreferenceFragmentCompat {

    private static final String DIALOG_FRAGMENT_TAG
            = "androidx.preference.PreferenceFragment.DIALOG";

    @Override
    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        RecyclerView rv = super.onCreateRecyclerView(inflater, parent, savedInstanceState);
        rv.setClipToPadding(false);
        rv.setFitsSystemWindows(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            rv.setOnApplyWindowInsetsListener((v, insets) -> {
                v.setPadding(0, 0, 0, insets.getSystemWindowInsetBottom());
                return insets;
            });
        }
        return rv;
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        // check if dialog is already showing.
        if (getParentFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
            return;
        }

        final DialogFragment f;
        if (preference instanceof EditTextPreference) {
            f = EditTextPreferenceDialogFragmentCompat.newInstance(preference.getKey());
        } else if (preference instanceof ListPreference) {
            f = ListPreferenceDialogFragmentCompat.newInstance(preference.getKey());
        } else if (preference instanceof MultiSelectListPreference) {
            f = MultiSelectListPreferenceDialogFragmentCompat.newInstance(preference.getKey());
        } else {
            throw new IllegalArgumentException(
                    "Cannot display dialog for an unknown Preference type: "
                            + preference.getClass().getSimpleName()
                            + ". Make sure to implement onPreferenceDisplayDialog() to handle "
                            + "displaying a custom dialog for this Preference.");
        }

        GeoDialog.injectStyle(f);
        f.setTargetFragment(this, 0);
        f.show(getParentFragmentManager(), DIALOG_FRAGMENT_TAG);
    }

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
