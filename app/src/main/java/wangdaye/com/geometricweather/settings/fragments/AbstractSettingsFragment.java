package wangdaye.com.geometricweather.settings.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.fragment.app.DialogFragment;
import androidx.preference.EditTextPreference;
import androidx.preference.EditTextPreferenceDialogFragmentCompat;
import androidx.preference.ListPreference;
import androidx.preference.ListPreferenceDialogFragmentCompat;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.MultiSelectListPreferenceDialogFragmentCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroupAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerViewAccessibilityDelegate;

import wangdaye.com.geometricweather.common.basic.GeoDialog;
import wangdaye.com.geometricweather.common.basic.insets.FitBothSideBarView;
import wangdaye.com.geometricweather.common.ui.widgets.insets.FitSystemBarRecyclerView;
import wangdaye.com.geometricweather.settings.SettingsManager;
import wangdaye.com.geometricweather.settings.activities.SelectProviderActivity;
import wangdaye.com.geometricweather.settings.activities.SettingsActivity;

public abstract class AbstractSettingsFragment extends PreferenceFragmentCompat {

    private static final String DIALOG_FRAGMENT_TAG
            = "androidx.preference.PreferenceFragment.DIALOG";

    @SuppressLint("RestrictedApi")
    private static class PreferenceRecyclerViewAccessibilityDelegate
            extends RecyclerViewAccessibilityDelegate {
        @SuppressWarnings("WeakerAccess") /* synthetic access */
        final RecyclerView mRecyclerView;
        @SuppressWarnings("WeakerAccess") /* synthetic access */
        final AccessibilityDelegateCompat mDefaultItemDelegate = super.getItemDelegate();

        public PreferenceRecyclerViewAccessibilityDelegate(RecyclerView recyclerView) {
            super(recyclerView);
            mRecyclerView = recyclerView;
        }

        @NonNull
        @Override
        public AccessibilityDelegateCompat getItemDelegate() {
            return mItemDelegate;
        }
        @SuppressWarnings("WeakerAccess") /* synthetic access */
        final AccessibilityDelegateCompat mItemDelegate = new AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
                mDefaultItemDelegate.onInitializeAccessibilityNodeInfo(host, info);
                int position = mRecyclerView.getChildAdapterPosition(host);

                RecyclerView.Adapter<?> adapter = mRecyclerView.getAdapter();
                if (!(adapter instanceof PreferenceGroupAdapter)) {
                    return;
                }

                PreferenceGroupAdapter preferenceGroupAdapter = (PreferenceGroupAdapter) adapter;
                Preference preference = preferenceGroupAdapter.getItem(position);
                if (preference == null) {
                    return;
                }

                preference.onInitializeAccessibilityNodeInfo(info);
            }

            @Override
            public boolean performAccessibilityAction(View host, int action, Bundle args) {
                // Must forward actions since the default delegate will handle actions.
                return mDefaultItemDelegate.performAccessibilityAction(host, action, args);
            }
        };
    }

    @Override
    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        FitSystemBarRecyclerView rv = new FitSystemBarRecyclerView(inflater.getContext());
        parent.addView(rv);

        rv.setClipToPadding(false);
        rv.removeFitSide(FitBothSideBarView.SIDE_TOP);
        rv.addFitSide(FitBothSideBarView.SIDE_BOTTOM);

        rv.setLayoutManager(onCreateLayoutManager());
        rv.setAccessibilityDelegateCompat(
                new PreferenceRecyclerViewAccessibilityDelegate(rv));

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

    protected SettingsManager getSettingsOptionManager() {
        return SettingsManager.getInstance(requireActivity());
    }

    protected void pushFragment(PreferenceFragmentCompat f, String key) {
        if (requireActivity() instanceof SettingsActivity) {
            ((SettingsActivity) requireActivity()).pushFragment(f, key);
        } else if (requireActivity() instanceof SelectProviderActivity) {
            ((SelectProviderActivity) requireActivity()).pushFragment(f, key);
        }
    }
}
