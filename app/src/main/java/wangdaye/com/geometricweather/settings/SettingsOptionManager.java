package wangdaye.com.geometricweather.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

import wangdaye.com.geometricweather.R;

public class SettingsOptionManager {

    private static SettingsOptionManager instance;

    public static SettingsOptionManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SettingsOptionManager.class) {
                if (instance == null) {
                    instance = new SettingsOptionManager(context);
                }
            }
        }
        return instance;
    }

    private String chineseSource;
    private String locationService;
    private String darkMode;
    private String iconProvider;
    private String[] cardDisplayValues;
    private String cardOrder;
    private boolean fahrenheit;
    private boolean imperial;
    private String language;
    private String updateInterval;

    public static final String DEFAULT_TODAY_FORECAST_TIME = "07:00";
    public static final String DEFAULT_TOMORROW_FORECAST_TIME = "21:00";

    private SettingsOptionManager(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        chineseSource = sharedPreferences.getString(
                context.getString(R.string.key_chinese_source),
                "accu"
        );
        locationService = sharedPreferences.getString(
                context.getString(R.string.key_location_service),
                "native"
        );
        darkMode = sharedPreferences.getString(
                context.getString(R.string.key_dark_mode),
                "auto"
        );
        iconProvider = sharedPreferences.getString(
                context.getString(R.string.key_icon_provider),
                context.getPackageName()
        );
        cardDisplayValues = Objects.requireNonNull(
                sharedPreferences.getStringSet(
                        context.getString(R.string.key_card_display),
                        new HashSet<>(Arrays.asList(
                                context.getResources().getStringArray(R.array.card_display_values)
                        ))
                )
        ).toArray(new String[] {});
        cardOrder = sharedPreferences.getString(
                context.getString(R.string.key_card_order),
                "daily_first"
        );
        fahrenheit = sharedPreferences.getBoolean(
                context.getString(R.string.key_fahrenheit),
                false
        );
        imperial = sharedPreferences.getBoolean(
                context.getString(R.string.key_imperial),
                false
        );
        language = sharedPreferences.getString(
                context.getString(R.string.key_language),
                "follow_system"
        );
        updateInterval = sharedPreferences.getString(
                context.getString(R.string.key_refresh_rate),
                "1:30"
        );
    }

    public String getChineseSource() {
        return chineseSource;
    }

    public void setChineseSource(String chineseSource) {
        this.chineseSource = chineseSource;
    }

    public String getLocationService() {
        return locationService;
    }

    public void setLocationService(String locationService) {
        this.locationService = locationService;
    }

    public String getDarkMode() {
        return darkMode;
    }

    public void setDarkMode(String darkMode) {
        this.darkMode = darkMode;
    }

    public String getIconProvider() {
        return iconProvider;
    }

    public void setIconProvider(String iconProvider) {
        this.iconProvider = iconProvider;
    }

    public String[] getCardDisplayValues() {
        return cardDisplayValues;
    }

    public void setCardDisplayValues(String[] cardDisplayValues) {
        this.cardDisplayValues = cardDisplayValues;
    }

    public String getCardOrder() {
        return cardOrder;
    }

    public void setCardOrder(String cardOrder) {
        this.cardOrder = cardOrder;
    }

    public boolean isFahrenheit() {
        return fahrenheit;
    }

    public void setFahrenheit(boolean fahrenheit) {
        this.fahrenheit = fahrenheit;
    }

    public boolean isImperial() {
        return imperial;
    }

    public void setImperial(boolean imperial) {
        this.imperial = imperial;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(String updateInterval) {
        this.updateInterval = updateInterval;
    }
}
