package wangdaye.com.geometricweather.settings.fragment;

import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import java.util.HashSet;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.resource.provider.ResourcesProviderFactory;
import wangdaye.com.geometricweather.ui.dialog.ProvidersPreviewerDialog;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.utils.ValueUtils;

/**
 * Appearance settings fragment.
 * */

public class AppearanceSettingsFragment extends AbstractSettingsFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.perference_appearance);

        // ui style.
        Preference uiStyle = findPreference(getString(R.string.key_ui_style));
        uiStyle.setSummary(
                getNameByValue(
                        getSettingsOptionManager().getUiStyle(),
                        R.array.ui_styles,
                        R.array.ui_style_values
                )
        );
        uiStyle.setOnPreferenceChangeListener((preference, newValue) -> {
            getSettingsOptionManager().setUiStyle((String) newValue);
            preference.setSummary(
                    getNameByValue(
                            (String) newValue,
                            R.array.ui_styles,
                            R.array.ui_style_values
                    )
            );
            SnackbarUtils.showSnackbar(
                    (GeoActivity) requireActivity(), getString(R.string.feedback_restart));
            return true;
        });

        // icon provider.
        initIconProviderPreference();

        // card display.
        Preference cardDisplay = findPreference(getString(R.string.key_card_display));
        cardDisplay.setSummary(
                ValueUtils.getCardDisplay(
                        requireActivity(),
                        getSettingsOptionManager().getCardDisplayValues()
                )
        );
        cardDisplay.setOnPreferenceChangeListener((preference, newValue) -> {
            try {
                String[] values = ((HashSet<String>) newValue).toArray(new String[] {});
                getSettingsOptionManager().setCardDisplayValues(values);
                preference.setSummary(ValueUtils.getCardDisplay(requireActivity(), values));
            } catch (Exception ignore) {
                // do nothing.
            }
            return true;
        });

        // card order.
        Preference cardOrder = findPreference(getString(R.string.key_card_order));
        cardOrder.setSummary(
                getNameByValue(
                        getSettingsOptionManager().getCardOrder(),
                        R.array.card_orders,
                        R.array.card_order_values
                )
        );
        cardOrder.setOnPreferenceChangeListener((preference, newValue) -> {
            getSettingsOptionManager().setCardOrder((String) newValue);
            preference.setSummary(
                    getNameByValue(
                            (String) newValue,
                            R.array.card_orders,
                            R.array.card_order_values
                    )
            );
            return true;
        });

        // sensor.
        findPreference(getString(R.string.key_gravity_sensor_switch)).setOnPreferenceChangeListener((preference, newValue) -> {
            getSettingsOptionManager().setGravitySensorEnabled((Boolean) newValue);
            return true;
        });

        // language.
        Preference language = findPreference(getString(R.string.key_language));
        language.setSummary(
                getNameByValue(
                        getSettingsOptionManager().getLanguage(),
                        R.array.languages,
                        R.array.language_values
                )
        );
        language.setOnPreferenceChangeListener((preference, newValue) -> {
            getSettingsOptionManager().setLanguage((String) newValue);
            preference.setSummary(
                    getNameByValue(
                            (String) newValue,
                            R.array.languages,
                            R.array.language_values
                    )
            );
            SnackbarUtils.showSnackbar(
                    (GeoActivity) requireActivity(), getString(R.string.feedback_restart));
            return true;
        });
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // do nothing.
    }

    private void initIconProviderPreference() {
        Preference iconProvider = findPreference(getString(R.string.key_icon_provider));
        iconProvider.setSummary(ResourcesProviderFactory.getNewInstance().getProviderName());
        
        iconProvider.setOnPreferenceClickListener(preference -> {
            ProvidersPreviewerDialog dialog = new ProvidersPreviewerDialog();
            dialog.setOnIconProviderChangedListener(iconProvider1 -> {
                getSettingsOptionManager().setIconProvider(iconProvider1);
                PreferenceManager.getDefaultSharedPreferences(requireActivity())
                        .edit()
                        .putString(getString(R.string.key_icon_provider), iconProvider1)
                        .apply();
                initIconProviderPreference();
                SnackbarUtils.showSnackbar(
                        (GeoActivity) requireActivity(), 
                        getString(R.string.feedback_refresh_ui_after_refresh)
                );
            });
            dialog.show(requireFragmentManager(), null);
            return true;
        });
    }
}