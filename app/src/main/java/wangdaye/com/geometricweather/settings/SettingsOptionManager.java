package wangdaye.com.geometricweather.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.ColorInt;
import androidx.annotation.StringDef;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.Location;

public class SettingsOptionManager {

    private static volatile SettingsOptionManager instance;

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

    // basic.

    private boolean backgroundFree;

    private boolean alertPushEnabled;

    private boolean precipitationPushEnabled;

    private String updateInterval;
    public static final String UPDATE_INTERVAL_0_30 = "0:30";
    public static final String UPDATE_INTERVAL_1_00 = "1:00";
    public static final String UPDATE_INTERVAL_1_30 = "1:30";
    public static final String UPDATE_INTERVAL_2_00 = "2:00";
    public static final String UPDATE_INTERVAL_2_30 = "2:30";
    public static final String UPDATE_INTERVAL_3_00 = "3:00";
    public static final String UPDATE_INTERVAL_3_30 = "3:30";
    public static final String UPDATE_INTERVAL_4_00 = "4:00";
    @StringDef({
            UPDATE_INTERVAL_0_30, UPDATE_INTERVAL_1_00, UPDATE_INTERVAL_1_30, UPDATE_INTERVAL_2_00,
            UPDATE_INTERVAL_2_30, UPDATE_INTERVAL_3_00, UPDATE_INTERVAL_3_30, UPDATE_INTERVAL_4_00
    }) public @interface UpdateIntervalRule {}

    private String darkMode;
    public static final String DARK_MODE_AUTO = "auto";
    public static final String DARK_MODE_LIGHT = "light";
    public static final String DARK_MODE_DARK = "dark";
    @StringDef({DARK_MODE_AUTO, DARK_MODE_LIGHT, DARK_MODE_DARK})
    public @interface DarkModeRule {}

    // service provider.

    private String chineseSource;
    public static final String WEATHER_SOURCE_CN = Location.WEATHER_SOURCE_CN;
    public static final String WEATHER_SOURCE_CAIYUN = Location.WEATHER_SOURCE_CAIYUN;
    public static final String WEATHER_SOURCE_ACCU = Location.WEATHER_SOURCE_ACCU;
    @StringDef({WEATHER_SOURCE_CN, WEATHER_SOURCE_CAIYUN, WEATHER_SOURCE_ACCU})
    public @interface WeatherSourceRule {}

    private String locationService;
    public static final String LOCATION_SERIVCE_BAIDU = "baidu";
    public static final String LOCATION_SERIVCE_BAIDU_IP = "baidu_ip";
    public static final String LOCATION_SERIVCE_AMAP = "amap";
    public static final String LOCATION_SERIVCE_NATIVE = "native";
    @StringDef({
            LOCATION_SERIVCE_BAIDU, LOCATION_SERIVCE_BAIDU_IP,
            LOCATION_SERIVCE_AMAP, LOCATION_SERIVCE_NATIVE
    }) public @interface LocationSourceRule {}

    // unit.

    private boolean fahrenheit;
    private boolean imperial;

    // appearance.

    private String uiStyle;
    public static final String UI_STYLE_CIRCULAR = "circular";
    public static final String UI_STYLE_MATERIAL = "material";
    @StringDef({UI_STYLE_CIRCULAR, UI_STYLE_MATERIAL})
    public @interface UiStyleRule {}

    private String iconProvider;

    private String[] cardDisplayValues;
    public static final String CARD_DAILY_OVERVIEW = "daily_overview";
    public static final String CARD_HOURLY_OVERVIEW = "hourly_overview";
    public static final String CARD_AIR_QUALITY = "air_quality";
    public static final String CARD_LIFE_DETAILS = "life_details";
    public static final String CARD_SUNRISE_SUNSET = "sunrise_sunset";
    @StringDef({
            CARD_DAILY_OVERVIEW, CARD_HOURLY_OVERVIEW,
            CARD_AIR_QUALITY, CARD_LIFE_DETAILS, CARD_SUNRISE_SUNSET
    }) public @interface CardDisplayValueRule {}

    private String cardOrder;
    public static final String CARD_ORDER_DAILY_FIRST = "daily_first";
    public static final String CARD_ORDER_HOURLY_FIRST = "hourly_first";
    @StringDef({CARD_ORDER_DAILY_FIRST, CARD_ORDER_HOURLY_FIRST})
    public @interface CardOrderRule {}

    private boolean gravitySensorEnabled;

    private String language;
    public static final String LANGUAGE_SYSTEM = "follow_system";
    public static final String LANGUAGE_CHINESE = "chinese";
    public static final String LANGUAGE_UNSIMPLIFIED_CHINESE = "unsimplified_chinese";
    public static final String LANGUAGE_ENGLISH_US = "english_america";
    public static final String LANGUAGE_ENGLISH_Uk = "english_britain";
    public static final String LANGUAGE_ENGLISH_AU = "english_australia";
    public static final String LANGUAGE_TURKISH = "turkish";
    public static final String LANGUAGE_FRENCH = "french";
    public static final String LANGUAGE_RUSSIAN = "russian";
    public static final String LANGUAGE_GERMAN = "german";
    public static final String LANGUAGE_SERBIAN = "serbian";
    public static final String LANGUAGE_SPANISH = "spanish";
    public static final String LANGUAGE_ITALIAN = "italian";
    public static final String LANGUAGE_DUTCH = "dutch";
    public static final String LANGUAGE_HUNGARIAN = "hungarian";
    public static final String LANGUAGE_PORTUGUESE = "portuguese";
    public static final String LANGUAGE_PORTUGUESE_BR = "portuguese_brazilian";
    public static final String LANGUAGE_SLOVENIAN = "slovenian";
    @StringDef({
            LANGUAGE_SYSTEM, LANGUAGE_CHINESE, LANGUAGE_UNSIMPLIFIED_CHINESE,
            LANGUAGE_ENGLISH_US, LANGUAGE_ENGLISH_Uk, LANGUAGE_ENGLISH_AU,
            LANGUAGE_TURKISH, LANGUAGE_FRENCH, LANGUAGE_RUSSIAN, LANGUAGE_GERMAN,
            LANGUAGE_SERBIAN, LANGUAGE_SPANISH, LANGUAGE_ITALIAN, LANGUAGE_DUTCH,
            LANGUAGE_HUNGARIAN, LANGUAGE_PORTUGUESE, LANGUAGE_PORTUGUESE_BR, LANGUAGE_SLOVENIAN
    }) public @interface LanguageRule {}

    // forecast.

    private boolean todayForecastEnabled;
    private String todayForecastTime;
    public static final String DEFAULT_TODAY_FORECAST_TIME = "07:00";

    private boolean tomorrowForecastEnabled;
    private String tomorrowForecastTime;
    public static final String DEFAULT_TOMORROW_FORECAST_TIME = "21:00";

    // widget.

    private boolean widgetMinimalIconEnabled;
    private boolean widgetClickToRefreshEnabled;

    // notification.

    private boolean notificationEnabled;

    private String notificationStyle;
    public static final String NOTIFICATION_STYLE_NATIVE = "native";
    public static final String NOTIFICATION_STYLE_CUSTOM = "geometric";
    @StringDef({NOTIFICATION_STYLE_NATIVE, NOTIFICATION_STYLE_CUSTOM})
    public @interface NotificationStyleRule {}

    private boolean notificationMinimalIconEnabled;

    private boolean notificationTemperatureIconEnabled;

    private boolean notificationCustomColorEnabled;

    @ColorInt
    private int notificationBackgroundColor;

    private String notificationTextColor;
    public static final String NOTIFICATION_TEXT_COLOR_DARK = "dark";
    public static final String NOTIFICATION_TEXT_COLOR_GREY = "grey";
    public static final String NOTIFICATION_TEXT_COLOR_LIGHT = "light";
    @StringDef({
            NOTIFICATION_TEXT_COLOR_DARK,
            NOTIFICATION_TEXT_COLOR_GREY,
            NOTIFICATION_TEXT_COLOR_LIGHT
    }) public @interface NotificationTextColorRule {}

    private boolean notificationCanBeClearedEnabled;

    private boolean notificationHideIconEnabled;

    private boolean notificationHideInLockScreenEnabled;

    private boolean notificationHideBigViewEnabled;

    private SettingsOptionManager(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // basic.
        backgroundFree = sharedPreferences.getBoolean(
                context.getString(R.string.key_background_free), true);

        alertPushEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.key_alert_notification_switch), true);

        precipitationPushEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.key_precipitation_notification_switch), true);

        updateInterval = sharedPreferences.getString(
                context.getString(R.string.key_refresh_rate), UPDATE_INTERVAL_1_30);

        darkMode = sharedPreferences.getString(
                context.getString(R.string.key_dark_mode), DARK_MODE_AUTO);

        // service provider.

        chineseSource = sharedPreferences.getString(
                context.getString(R.string.key_chinese_source), WEATHER_SOURCE_ACCU);

        locationService = sharedPreferences.getString(
                context.getString(R.string.key_location_service), LOCATION_SERIVCE_NATIVE);

        // unit.

        fahrenheit = sharedPreferences.getBoolean(
                context.getString(R.string.key_fahrenheit), false);

        imperial = sharedPreferences.getBoolean(
                context.getString(R.string.key_imperial), false);

        // appearance.

        uiStyle = sharedPreferences.getString(
                context.getString(R.string.key_ui_style), UI_STYLE_MATERIAL);

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
                context.getString(R.string.key_card_order), CARD_ORDER_DAILY_FIRST);

        gravitySensorEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.key_gravity_sensor_switch), true);

        language = sharedPreferences.getString(
                context.getString(R.string.key_language), LANGUAGE_SYSTEM);

        // forecast.

        todayForecastEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.key_forecast_today), false);

        todayForecastTime = sharedPreferences.getString(
                context.getString(R.string.key_forecast_today_time), DEFAULT_TODAY_FORECAST_TIME);

        tomorrowForecastEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.key_forecast_tomorrow), false);

        tomorrowForecastTime = sharedPreferences.getString(
                context.getString(R.string.key_forecast_tomorrow_time), DEFAULT_TOMORROW_FORECAST_TIME);

        // widget.

        widgetMinimalIconEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.key_widget_minimal_icon), false);

        widgetClickToRefreshEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.key_click_widget_to_refresh), false);

        // notification.

        notificationEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification), false);

        notificationStyle = sharedPreferences.getString(
                context.getString(R.string.key_notification_style), NOTIFICATION_STYLE_CUSTOM);

        notificationMinimalIconEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_minimal_icon), false);

        notificationTemperatureIconEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_temp_icon), false);

        notificationCustomColorEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_custom_color), false);

        notificationBackgroundColor = sharedPreferences.getInt(
                context.getString(R.string.key_notification_background_color),
                ContextCompat.getColor(context, R.color.notification_background_l)
        );

        notificationTextColor = sharedPreferences.getString(
                context.getString(R.string.key_notification_text_color),
                NOTIFICATION_TEXT_COLOR_DARK
        );

        notificationCanBeClearedEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_can_be_cleared), false);

        notificationHideIconEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_hide_icon), false);

        notificationHideInLockScreenEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_hide_in_lockScreen), false);

        notificationHideBigViewEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_hide_big_view), false);
    }

    public boolean isBackgroundFree() {
        return backgroundFree;
    }

    public void setBackgroundFree(boolean backgroundFree) {
        this.backgroundFree = backgroundFree;
    }

    public boolean isAlertPushEnabled() {
        return alertPushEnabled;
    }

    public void setAlertPushEnabled(boolean alertPushEnabled) {
        this.alertPushEnabled = alertPushEnabled;
    }

    public boolean isPrecipitationPushEnabled() {
        return precipitationPushEnabled;
    }

    public void setPrecipitationPushEnabled(boolean precipitationPushEnabled) {
        this.precipitationPushEnabled = precipitationPushEnabled;
    }

    @UpdateIntervalRule
    public String getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(@UpdateIntervalRule String updateInterval) {
        this.updateInterval = updateInterval;
    }

    @DarkModeRule
    public String getDarkMode() {
        return darkMode;
    }

    public void setDarkMode(@DarkModeRule String darkMode) {
        this.darkMode = darkMode;
    }

    @WeatherSourceRule
    public String getChineseSource() {
        return chineseSource;
    }

    public void setChineseSource(@WeatherSourceRule String chineseSource) {
        this.chineseSource = chineseSource;
    }

    @LocationSourceRule
    public String getLocationService() {
        return locationService;
    }

    public void setLocationService(@LocationSourceRule String locationService) {
        this.locationService = locationService;
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

    @UiStyleRule
    public String getUiStyle() {
        return uiStyle;
    }

    public void setUiStyle(@UiStyleRule String uiStyle) {
        this.uiStyle = uiStyle;
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

    @CardOrderRule
    public String getCardOrder() {
        return cardOrder;
    }

    public void setCardOrder(@CardOrderRule String cardOrder) {
        this.cardOrder = cardOrder;
    }

    public boolean isGravitySensorEnabled() {
        return gravitySensorEnabled;
    }

    public void setGravitySensorEnabled(boolean gravitySensorEnabled) {
        this.gravitySensorEnabled = gravitySensorEnabled;
    }

    @LanguageRule
    public String getLanguage() {
        return language;
    }

    public void setLanguage(@LanguageRule String language) {
        this.language = language;
    }

    public boolean isTodayForecastEnabled() {
        return todayForecastEnabled;
    }

    public void setTodayForecastEnabled(boolean todayForecastEnabled) {
        this.todayForecastEnabled = todayForecastEnabled;
    }

    public String getTodayForecastTime() {
        return todayForecastTime;
    }

    public void setTodayForecastTime(String todayForecastTime) {
        this.todayForecastTime = todayForecastTime;
    }

    public boolean isTomorrowForecastEnabled() {
        return tomorrowForecastEnabled;
    }

    public void setTomorrowForecastEnabled(boolean tomorrowForecastEnabled) {
        this.tomorrowForecastEnabled = tomorrowForecastEnabled;
    }

    public String getTomorrowForecastTime() {
        return tomorrowForecastTime;
    }

    public void setTomorrowForecastTime(String tomorrowForecastTime) {
        this.tomorrowForecastTime = tomorrowForecastTime;
    }

    public boolean isWidgetMinimalIconEnabled() {
        return widgetMinimalIconEnabled;
    }

    public void setWidgetMinimalIconEnabled(boolean widgetMinimalIconEnabled) {
        this.widgetMinimalIconEnabled = widgetMinimalIconEnabled;
    }

    public boolean isWidgetClickToRefreshEnabled() {
        return widgetClickToRefreshEnabled;
    }

    public void setWidgetClickToRefreshEnabled(boolean widgetClickToRefreshEnabled) {
        this.widgetClickToRefreshEnabled = widgetClickToRefreshEnabled;
    }

    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }

    public void setNotificationEnabled(boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    @NotificationStyleRule
    public String getNotificationStyle() {
        return notificationStyle;
    }

    public void setNotificationStyle(@NotificationStyleRule String notificationStyle) {
        this.notificationStyle = notificationStyle;
    }

    public boolean isNotificationMinimalIconEnabled() {
        return notificationMinimalIconEnabled;
    }

    public void setNotificationMinimalIconEnabled(boolean notificationMinimalIconEnabled) {
        this.notificationMinimalIconEnabled = notificationMinimalIconEnabled;
    }

    public boolean isNotificationTemperatureIconEnabled() {
        return notificationTemperatureIconEnabled;
    }

    public void setNotificationTemperatureIconEnabled(boolean notificationTemperatureIconEnabled) {
        this.notificationTemperatureIconEnabled = notificationTemperatureIconEnabled;
    }

    public boolean isNotificationCustomColorEnabled() {
        return notificationCustomColorEnabled;
    }

    public void setNotificationCustomColorEnabled(boolean notificationCustomColorEnabled) {
        this.notificationCustomColorEnabled = notificationCustomColorEnabled;
    }

    @ColorInt
    public int getNotificationBackgroundColor() {
        return notificationBackgroundColor;
    }

    public void setNotificationBackgroundColor(@ColorInt int notificationBackgroundColor) {
        this.notificationBackgroundColor = notificationBackgroundColor;
    }

    @NotificationTextColorRule
    public String getNotificationTextColor() {
        return notificationTextColor;
    }

    public void setNotificationTextColor(@NotificationTextColorRule String notificationTextColor) {
        this.notificationTextColor = notificationTextColor;
    }

    public boolean isNotificationCanBeClearedEnabled() {
        return notificationCanBeClearedEnabled;
    }

    public void setNotificationCanBeClearedEnabled(boolean notificationCanBeClearedEnabled) {
        this.notificationCanBeClearedEnabled = notificationCanBeClearedEnabled;
    }

    public boolean isNotificationHideIconEnabled() {
        return notificationHideIconEnabled;
    }

    public void setNotificationHideIconEnabled(boolean notificationHideIconEnabled) {
        this.notificationHideIconEnabled = notificationHideIconEnabled;
    }

    public boolean isNotificationHideInLockScreenEnabled() {
        return notificationHideInLockScreenEnabled;
    }

    public void setNotificationHideInLockScreenEnabled(boolean notificationHideInLockScreenEnabled) {
        this.notificationHideInLockScreenEnabled = notificationHideInLockScreenEnabled;
    }

    public boolean isNotificationHideBigViewEnabled() {
        return notificationHideBigViewEnabled;
    }

    public void setNotificationHideBigViewEnabled(boolean notificationHideBigViewEnabled) {
        this.notificationHideBigViewEnabled = notificationHideBigViewEnabled;
    }
}
