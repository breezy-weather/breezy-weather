package wangdaye.com.geometricweather.settings.fragment;

import android.os.Build;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.HashSet;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.settings.dialog.ProvidersPreviewerDialog;
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
                                .getString(getString(R.string.key_ui_style), "material")
                )
        );
        uiStyle.setOnPreferenceChangeListener(this);

        initIconProviderPreference();

        Preference cardDisplay = findPreference(getString(R.string.key_card_display));
        cardDisplay.setSummary(
                ValueUtils.getCardDislay(
                        getActivity(),
                        GeometricWeather.getInstance().getCardDisplayValues()
                )
        );
        cardDisplay.setOnPreferenceChangeListener(this);

        Preference cardOrder = findPreference(getString(R.string.key_card_order));
        cardOrder.setSummary(
                ValueUtils.getCardOrder(
                        getActivity(),
                        GeometricWeather.getInstance().getCardOrder()
                )
        );
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
                        GeometricWeather.getInstance().getLanguage()
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
                GeometricWeather.getInstance().setIconProvider(iconProvider);
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putString(getString(R.string.key_icon_provider), iconProvider)
                        .apply();
                initIconProviderPreference();
            });
            dialog.show(getFragmentManager(), null);
        } else if (preference.getKey().equals(getString(R.string.key_navigationBar_color))) {
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
        } else if (preference.getKey().equals(getString(R.string.key_card_display))) {
            // Card display.
            try {
                String[] values = ((HashSet<String>) o).toArray(new String[] {});
                GeometricWeather.getInstance().setCardDisplayValues(values);
                preference.setSummary(ValueUtils.getCardDislay(getActivity(), values));
            } catch (Exception ignore) {
                // do nothing.
            }
        } else if (preference.getKey().equals(getString(R.string.key_card_order))) {
            // Card order.
            GeometricWeather.getInstance().setCardOrder((String) o);
            preference.setSummary(ValueUtils.getCardOrder(getActivity(), (String) o));
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