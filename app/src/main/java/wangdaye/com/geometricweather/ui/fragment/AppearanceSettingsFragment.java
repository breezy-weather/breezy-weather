package wangdaye.com.geometricweather.ui.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.utils.ValueUtils;

/**
 * Appearance settings fragment.
 * */

public class AppearanceSettingsFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.perference_appearance);

        Preference uiStyle = findPreference(getString(R.string.key_ui_style));
        uiStyle.setSummary(
                ValueUtils.getUIStyle(
                        getActivity(),
                        PreferenceManager.getDefaultSharedPreferences(getActivity())
                                .getString(
                                        getString(R.string.key_ui_style),
                                        "material")));
        uiStyle.setOnPreferenceChangeListener(this);

        Preference cardOrder = findPreference(getString(R.string.key_card_order));
        cardOrder.setSummary(
                ValueUtils.getCardOrder(
                        getActivity(),
                        PreferenceManager.getDefaultSharedPreferences(getActivity())
                                .getString(
                                        getString(R.string.key_card_order),
                                        "daily_first")));
        cardOrder.setOnPreferenceChangeListener(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            findPreference(getString(R.string.key_navigationBar_color)).setEnabled(false);
        } else {
            findPreference(getString(R.string.key_navigationBar_color)).setEnabled(true);
        }

        Preference language = findPreference(getString(R.string.key_language));
        language.setSummary(
                ValueUtils.getLanguage(
                        getActivity(),
                        PreferenceManager.getDefaultSharedPreferences(getActivity())
                                .getString(getString(R.string.key_language), "follow_system")));
        language.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // do nothing.
    }

    // interface.

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getKey().equals(getString(R.string.key_navigationBar_color))) {
            // navigation bar color.
            GeometricWeather.getInstance().setColorNavigationBar();
            DisplayUtils.setNavigationBarColor(getActivity(), 0);
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        if (preference.getKey().equals(getString(R.string.key_ui_style))) {
            // UI style.
            preference.setSummary(ValueUtils.getUIStyle(getActivity(), (String) o));
            SnackbarUtils.showSnackbar(getString(R.string.feedback_restart));
        } else if (preference.getKey().equals(getString(R.string.key_card_order))) {
            // Card order.
            GeometricWeather.getInstance().setCardOrder((String) o);
            preference.setSummary(ValueUtils.getCardOrder(getActivity(), (String) o));
            SnackbarUtils.showSnackbar(getString(R.string.feedback_refresh_ui_after_refresh));
        } else if (preference.getKey().equals(getString(R.string.key_language))) {
            // language.
            preference.setSummary(ValueUtils.getLanguage(getActivity(), (String) o));
            GeometricWeather.getInstance().setLanguage((String) o);
            SnackbarUtils.showSnackbar(getString(R.string.feedback_restart));
        } else if (preference.getKey().equals(getString(R.string.key_gravity_sensor_switch))) {
            // sensor.
            SnackbarUtils.showSnackbar(getString(R.string.feedback_restart));
        }
        return true;
    }
}