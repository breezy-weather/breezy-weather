package com.mbestavros.geometricweather.settings.fragment;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mbestavros.geometricweather.settings.SettingsOptionManager;
import com.mbestavros.geometricweather.settings.activity.SettingsActivity;

public abstract class AbstractSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        RecyclerView rv = super.onCreateRecyclerView(inflater, parent, savedInstanceState);
        rv.setClipToPadding(false);
        rv.setFitsSystemWindows(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            rv.setOnApplyWindowInsetsListener((v, insets) -> {
                v.setPadding(
                        insets.getSystemWindowInsetLeft(),
                        0,
                        insets.getSystemWindowInsetRight(),
                        insets.getSystemWindowInsetBottom()
                );
                return insets;
            });
        }
        return rv;
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
