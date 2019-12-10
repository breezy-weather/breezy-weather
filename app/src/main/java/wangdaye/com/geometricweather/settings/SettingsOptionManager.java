package wangdaye.com.geometricweather.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.appearance.CardDisplay;
import wangdaye.com.geometricweather.basic.model.option.appearance.Language;
import wangdaye.com.geometricweather.basic.model.option.NotificationStyle;
import wangdaye.com.geometricweather.basic.model.option.NotificationTextColor;
import wangdaye.com.geometricweather.basic.model.option.appearance.UIStyle;
import wangdaye.com.geometricweather.basic.model.option.provider.LocationProvider;
import wangdaye.com.geometricweather.basic.model.option.provider.WeatherSource;
import wangdaye.com.geometricweather.basic.model.option.DarkMode;
import wangdaye.com.geometricweather.basic.model.option.UpdateInterval;
import wangdaye.com.geometricweather.basic.model.option.unit.DistanceUnit;
import wangdaye.com.geometricweather.basic.model.option.unit.PrecipitationUnit;
import wangdaye.com.geometricweather.basic.model.option.unit.PressureUnit;
import wangdaye.com.geometricweather.basic.model.option.unit.SpeedUnit;
import wangdaye.com.geometricweather.basic.model.option.unit.TemperatureUnit;

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
    private UpdateInterval updateInterval;
    private DarkMode darkMode;

    // service provider.
    private WeatherSource weatherSource;
    private LocationProvider locationProvider;

    // unit.
    private TemperatureUnit temperatureUnit;
    private DistanceUnit distanceUnit;
    private PrecipitationUnit precipitationUnit;
    private PressureUnit pressureUnit;
    private SpeedUnit speedUnit;

    // appearance.
    private UIStyle uiStyle;
    private String iconProvider;
    private List<CardDisplay> cardDisplayList;
    private static final String DEFAULT_CARD_DISPLAY = "daily_overview"
            + "&hourly_overview"
            + "&air_quality"
            + "&allergen"
            + "&sunrise_sunset"
            + "&life_details";

    private boolean gravitySensorEnabled;
    private boolean listAnimationEnabled;
    private boolean itemAnimationEnabled;
    private Language language;

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
    private NotificationStyle notificationStyle;
    private boolean notificationMinimalIconEnabled;
    private boolean notificationTemperatureIconEnabled;
    private boolean notificationCustomColorEnabled;
    @ColorInt private int notificationBackgroundColor;
    private NotificationTextColor notificationTextColor;
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
                context.getString(R.string.key_precipitation_notification_switch), false);

        updateInterval = OptionMapper.getUpdateInterval(
                sharedPreferences.getString(
                        context.getString(R.string.key_refresh_rate), "1:30")
        );

        darkMode = OptionMapper.getDarkMode(
                sharedPreferences.getString(
                        context.getString(R.string.key_dark_mode), "auto")
        );

        // service provider.

        weatherSource = OptionMapper.getWeatherSource(
                sharedPreferences.getString(
                        context.getString(R.string.key_weather_source), "accu")
        );

        locationProvider = OptionMapper.getLocationProvider(
                sharedPreferences.getString(
                        context.getString(R.string.key_location_service), "native")
        );

        // unit.

        temperatureUnit = OptionMapper.getTemperatureUnit(
                sharedPreferences.getString(
                        context.getString(R.string.key_temperature_unit), "c")
        );
        distanceUnit = OptionMapper.getDistanceUnit(
                sharedPreferences.getString(
                        context.getString(R.string.key_distance_unit), "km")
        );
        precipitationUnit = OptionMapper.getPrecipitationUnit(
                sharedPreferences.getString(
                        context.getString(R.string.key_precipitation_unit), "mm")
        );
        pressureUnit = OptionMapper.getPressureUnit(
                sharedPreferences.getString(
                        context.getString(R.string.key_pressure_unit), "mb")
        );
        speedUnit = OptionMapper.getSpeedUnit(
                sharedPreferences.getString(
                        context.getString(R.string.key_speed_unit), "mps")
        );

        // appearance.

        uiStyle = OptionMapper.getUIStyle(
                sharedPreferences.getString(
                        context.getString(R.string.key_ui_style), "material")
        );

        iconProvider = sharedPreferences.getString(
                context.getString(R.string.key_icon_provider),
                context.getPackageName()
        );

        cardDisplayList = OptionMapper.getCardDisplayList(
                sharedPreferences.getString(context.getString(R.string.key_card_display), DEFAULT_CARD_DISPLAY)
        );

        gravitySensorEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.key_gravity_sensor_switch), true);

        listAnimationEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.key_list_animation_switch), true);

        itemAnimationEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.key_item_animation_switch), true);

        language = OptionMapper.getLanguage(
                sharedPreferences.getString(
                        context.getString(R.string.key_language), "follow_system")
        );

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

        notificationStyle = OptionMapper.getNotificationStyle(
                sharedPreferences.getString(
                        context.getString(R.string.key_notification_style), "geometric")
        );

        notificationMinimalIconEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_minimal_icon), false);

        notificationTemperatureIconEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_temp_icon), false);

        notificationCustomColorEnabled = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_custom_color), false);

        notificationBackgroundColor = sharedPreferences.getInt(
                context.getString(R.string.key_notification_background_color),
                ContextCompat.getColor(context, R.color.notification_background_l));

        notificationTextColor = OptionMapper.getNotificationTextColor(
                sharedPreferences.getString(
                        context.getString(R.string.key_notification_text_color), "dark")
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

    public UpdateInterval getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(UpdateInterval updateInterval) {
        this.updateInterval = updateInterval;
    }

    public DarkMode getDarkMode() {
        return darkMode;
    }

    public void setDarkMode(DarkMode darkMode) {
        this.darkMode = darkMode;
    }

    public WeatherSource getWeatherSource() {
        return weatherSource;
    }

    public void setWeatherSource(WeatherSource weatherSource) {
        this.weatherSource = weatherSource;
    }

    public LocationProvider getLocationProvider() {
        return locationProvider;
    }

    public void setLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    public TemperatureUnit getTemperatureUnit() {
        return temperatureUnit;
    }

    public void setTemperatureUnit(TemperatureUnit temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
    }

    public DistanceUnit getDistanceUnit() {
        return distanceUnit;
    }

    public void setDistanceUnit(DistanceUnit distanceUnit) {
        this.distanceUnit = distanceUnit;
    }

    public PrecipitationUnit getPrecipitationUnit() {
        return precipitationUnit;
    }

    public void setPrecipitationUnit(PrecipitationUnit precipitationUnit) {
        this.precipitationUnit = precipitationUnit;
    }

    public PressureUnit getPressureUnit() {
        return pressureUnit;
    }

    public void setPressureUnit(PressureUnit pressureUnit) {
        this.pressureUnit = pressureUnit;
    }

    public SpeedUnit getSpeedUnit() {
        return speedUnit;
    }

    public void setSpeedUnit(SpeedUnit speedUnit) {
        this.speedUnit = speedUnit;
    }

    public UIStyle getUiStyle() {
        return uiStyle;
    }

    public void setUiStyle(UIStyle uiStyle) {
        this.uiStyle = uiStyle;
    }

    public String getIconProvider() {
        return iconProvider;
    }

    public void setIconProvider(String iconProvider) {
        this.iconProvider = iconProvider;
    }

    public List<CardDisplay> getCardDisplayList() {
        return cardDisplayList;
    }

    public void setCardDisplayList(List<CardDisplay> cardDisplayList) {
        this.cardDisplayList = cardDisplayList;
    }

    public boolean isGravitySensorEnabled() {
        return gravitySensorEnabled;
    }

    public void setGravitySensorEnabled(boolean gravitySensorEnabled) {
        this.gravitySensorEnabled = gravitySensorEnabled;
    }

    public boolean isListAnimationEnabled() {
        return listAnimationEnabled;
    }

    public void setListAnimationEnabled(boolean listAnimationEnabled) {
        this.listAnimationEnabled = listAnimationEnabled;
    }

    public boolean isItemAnimationEnabled() {
        return itemAnimationEnabled;
    }

    public void setItemAnimationEnabled(boolean itemAnimationEnabled) {
        this.itemAnimationEnabled = itemAnimationEnabled;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
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

    public NotificationStyle getNotificationStyle() {
        return notificationStyle;
    }

    public void setNotificationStyle(NotificationStyle notificationStyle) {
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

    public int getNotificationBackgroundColor() {
        return notificationBackgroundColor;
    }

    public void setNotificationBackgroundColor(int notificationBackgroundColor) {
        this.notificationBackgroundColor = notificationBackgroundColor;
    }

    public NotificationTextColor getNotificationTextColor() {
        return notificationTextColor;
    }

    public void setNotificationTextColor(NotificationTextColor notificationTextColor) {
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
