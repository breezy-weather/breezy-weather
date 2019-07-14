package wangdaye.com.geometricweather.settings.fragment;

import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.HashSet;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.dialog.ProvidersPreviewerDialog;
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
                                .getString(getString(R.string.key_ui_style), "material")
                )
        );
        uiStyle.setOnPreferenceChangeListener(this);

        initIconProviderPreference();

        Preference cardDisplay = findPreference(getString(R.string.key_card_display));
        cardDisplay.setSummary(
                ValueUtils.getCardDislay(
                        getActivity(),
                        SettingsOptionManager.getInstance(getActivity()).getCardDisplayValues()
                )
        );
        cardDisplay.setOnPreferenceChangeListener(this);

        Preference cardOrder = findPreference(getString(R.string.key_card_order));
        cardOrder.setSummary(
                ValueUtils.getCardOrder(
                        getActivity(),
                        SettingsOptionManager.getInstance(getActivity()).getCardOrder()
                )
        );
        cardOrder.setOnPreferenceChangeListener(this);

        findPreference(getString(R.string.key_gravity_sensor_switch))
                .setOnPreferenceChangeListener(this);

        Preference language = findPreference(getString(R.string.key_language));
        language.setSummary(
                ValueUtils.getLanguage(
                        getActivity(),
                        SettingsOptionManager.getInstance(getActivity()).getLanguage()
                )
        );
        language.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // do nothing.
    }

    private void initIconProviderPreference() {
        Preference iconProvider = findPreference(getString(R.string.key_icon_provider));
        iconProvider.setSummary(ResourcesProviderFactory.getNewInstance().getProviderName());
    }

    // interface.

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getKey().equals(getString(R.string.key_icon_provider))) {
            // icon provider.
            ProvidersPreviewerDialog dialog = new ProvidersPreviewerDialog();
            dialog.setOnIconProviderChangedListener(iconProvider -> {
                SettingsOptionManager.getInstance(getActivity()).setIconProvider(iconProvider);
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putString(getString(R.string.key_icon_provider), iconProvider)
                        .apply();
                initIconProviderPreference();
                SnackbarUtils.showSnackbar((GeoActivity) getActivity(), getString(R.string.feedback_refresh_ui_after_refresh));
            });
            dialog.show(getFragmentManager(), null);
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        if (preference.getKey().equals(getString(R.string.key_ui_style))) {
            // UI style.
            preference.setSummary(ValueUtils.getUIStyle(getActivity(), (String) o));
            SnackbarUtils.showSnackbar((GeoActivity) getActivity(), getString(R.string.feedback_restart));
        } else if (preference.getKey().equals(getString(R.string.key_card_display))) {
            // Card display.
            try {
                String[] values = ((HashSet<String>) o).toArray(new String[] {});
                SettingsOptionManager.getInstance(getActivity()).setCardDisplayValues(values);
                preference.setSummary(ValueUtils.getCardDislay(getActivity(), values));
            } catch (Exception ignore) {
                // do nothing.
            }
        } else if (preference.getKey().equals(getString(R.string.key_card_order))) {
            // Card order.
            SettingsOptionManager.getInstance(getActivity()).setCardOrder((String) o);
            preference.setSummary(ValueUtils.getCardOrder(getActivity(), (String) o));
        } else if (preference.getKey().equals(getString(R.string.key_language))) {
            // language.
            preference.setSummary(ValueUtils.getLanguage(getActivity(), (String) o));
            SettingsOptionManager.getInstance(getActivity()).setLanguage((String) o);
            SnackbarUtils.showSnackbar((GeoActivity) getActivity(), getString(R.string.feedback_restart));
        } else if (preference.getKey().equals(getString(R.string.key_gravity_sensor_switch))) {
            // sensor.
            SettingsOptionManager.getInstance(getActivity()).setGravitySensorEnabled((Boolean) o);
        }
        return true;
    }
}