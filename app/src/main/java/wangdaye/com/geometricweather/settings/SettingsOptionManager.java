package wangdaye.com.geometricweather.settings;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.options.DarkMode;
import wangdaye.com.geometricweather.common.basic.models.options.NotificationStyle;
import wangdaye.com.geometricweather.common.basic.models.options.NotificationTextColor;
import wangdaye.com.geometricweather.common.basic.models.options.UpdateInterval;
import wangdaye.com.geometricweather.common.basic.models.options.WidgetWeekIconMode;
import wangdaye.com.geometricweather.common.basic.models.options.appearance.CardDisplay;
import wangdaye.com.geometricweather.common.basic.models.options.appearance.DailyTrendDisplay;
import wangdaye.com.geometricweather.common.basic.models.options.appearance.Language;
import wangdaye.com.geometricweather.common.basic.models.options.appearance.UIStyle;
import wangdaye.com.geometricweather.common.basic.models.options.provider.LocationProvider;
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.common.basic.models.options.unit.DistanceUnit;
import wangdaye.com.geometricweather.common.basic.models.options.unit.PrecipitationUnit;
import wangdaye.com.geometricweather.common.basic.models.options.unit.PressureUnit;
import wangdaye.com.geometricweather.common.basic.models.options.unit.SpeedUnit;
import wangdaye.com.geometricweather.common.basic.models.options.unit.TemperatureUnit;

public class SettingsOptionManager {

    private static volatile SettingsOptionManager sInstance;

    public static SettingsOptionManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (SettingsOptionManager.class) {
                if (sInstance == null) {
                    sInstance = new SettingsOptionManager(context);
                }
            }
        }
        return sInstance;
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
    private List<DailyTrendDisplay> dailyTrendDisplayList;
    private static final String DEFAULT_DAILY_TREND_DISPLAY = "temperature"
            + "&air_quality"
            + "&wind"
            + "&uv_index"
            + "&precipitation";

    private boolean trendHorizontalLinesEnabled;
    private boolean exchangeDayNightTempEnabled;
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
    private WidgetWeekIconMode widgetWeekIconMode;
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
        ConfigStore config = ConfigStore.getInstance(context);

        // basic.

        // force set background free on android 12+.
        backgroundFree = config.getBoolean(
                context.getString(R.string.key_background_free),
                true
        ); // || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;

        alertPushEnabled = config.getBoolean(
                context.getString(R.string.key_alert_notification_switch), true);

        precipitationPushEnabled = config.getBoolean(
                context.getString(R.string.key_precipitation_notification_switch), false);

        updateInterval = UpdateInterval.getInstance(
                config.getString(
                        context.getString(R.string.key_refresh_rate), "1:30")
        );

        darkMode = DarkMode.getInstance(
                config.getString(
                        context.getString(R.string.key_dark_mode), "auto")
        );

        // service provider.

        weatherSource = WeatherSource.getInstance(
                config.getString(
                        context.getString(R.string.key_weather_source), "accu")
        );

        locationProvider = LocationProvider.getInstance(
                config.getString(
                        context.getString(R.string.key_location_service), "native")
        );

        // unit.

        temperatureUnit = TemperatureUnit.getInstance(
                config.getString(
                        context.getString(R.string.key_temperature_unit), "c")
        );
        distanceUnit = DistanceUnit.getInstance(
                config.getString(
                        context.getString(R.string.key_distance_unit), "km")
        );
        precipitationUnit = PrecipitationUnit.getInstance(
                config.getString(
                        context.getString(R.string.key_precipitation_unit), "mm")
        );
        pressureUnit = PressureUnit.getInstance(
                config.getString(
                        context.getString(R.string.key_pressure_unit), "mb")
        );
        speedUnit = SpeedUnit.getInstance(
                config.getString(
                        context.getString(R.string.key_speed_unit), "mps")
        );

        // appearance.

        uiStyle = UIStyle.getInstance(
                config.getString(
                        context.getString(R.string.key_ui_style), "material")
        );

        iconProvider = config.getString(
                context.getString(R.string.key_icon_provider),
                context.getPackageName()
        );

        cardDisplayList = CardDisplay.toCardDisplayList(
                config.getString(context.getString(R.string.key_card_display), DEFAULT_CARD_DISPLAY)
        );

        dailyTrendDisplayList = DailyTrendDisplay.toDailyTrendDisplayList(
                config.getString(
                        context.getString(R.string.key_daily_trend_display),
                        DEFAULT_DAILY_TREND_DISPLAY
                )
        );

        trendHorizontalLinesEnabled = config.getBoolean(
                context.getString(R.string.key_trend_horizontal_line_switch), true);

        exchangeDayNightTempEnabled = config.getBoolean(
                context.getString(R.string.key_exchange_day_night_temp_switch), false);

        gravitySensorEnabled = config.getBoolean(
                context.getString(R.string.key_gravity_sensor_switch), true);

        listAnimationEnabled = config.getBoolean(
                context.getString(R.string.key_list_animation_switch), true);

        itemAnimationEnabled = config.getBoolean(
                context.getString(R.string.key_item_animation_switch), true);

        language = Language.getInstance(
                config.getString(
                        context.getString(R.string.key_language), "follow_system")
        );

        // forecast.

        todayForecastEnabled = config.getBoolean(
                context.getString(R.string.key_forecast_today), false);

        todayForecastTime = config.getString(
                context.getString(R.string.key_forecast_today_time), DEFAULT_TODAY_FORECAST_TIME);

        tomorrowForecastEnabled = config.getBoolean(
                context.getString(R.string.key_forecast_tomorrow), false);

        tomorrowForecastTime = config.getString(
                context.getString(R.string.key_forecast_tomorrow_time), DEFAULT_TOMORROW_FORECAST_TIME);

        // widget.

        widgetWeekIconMode = WidgetWeekIconMode.getInstance(
                config.getString(
                        context.getString(R.string.key_week_icon_mode), "auto")
        );

        widgetMinimalIconEnabled = config.getBoolean(
                context.getString(R.string.key_widget_minimal_icon), false);

        widgetClickToRefreshEnabled = config.getBoolean(
                context.getString(R.string.key_click_widget_to_refresh), false);

        // notification.

        notificationEnabled = config.getBoolean(
                context.getString(R.string.key_notification), false);

        notificationStyle = NotificationStyle.getInstance(
                config.getString(
                        context.getString(R.string.key_notification_style), "daily")
        );

        notificationMinimalIconEnabled = config.getBoolean(
                context.getString(R.string.key_notification_minimal_icon), false);

        notificationTemperatureIconEnabled = config.getBoolean(
                context.getString(R.string.key_notification_temp_icon), false);

        notificationCustomColorEnabled = config.getBoolean(
                context.getString(R.string.key_notification_custom_color), false);

        notificationBackgroundColor = config.getInt(
                context.getString(R.string.key_notification_background_color),
                ContextCompat.getColor(context, R.color.notification_background_l));

        notificationTextColor = NotificationTextColor.getInstance(
                config.getString(
                        context.getString(R.string.key_notification_text_color), "dark")
        );

        notificationCanBeClearedEnabled = config.getBoolean(
                context.getString(R.string.key_notification_can_be_cleared), false);

        notificationHideIconEnabled = config.getBoolean(
                context.getString(R.string.key_notification_hide_icon), false);

        notificationHideInLockScreenEnabled = config.getBoolean(
                context.getString(R.string.key_notification_hide_in_lockScreen), false);

        notificationHideBigViewEnabled = config.getBoolean(
                context.getString(R.string.key_notification_hide_big_view), false);
    }

    public synchronized boolean isBackgroundFree() {
        return backgroundFree;
    }

    public synchronized void setBackgroundFree(boolean backgroundFree) {
        this.backgroundFree = backgroundFree;
    }

    public synchronized boolean isAlertPushEnabled() {
        return alertPushEnabled;
    }

    public synchronized void setAlertPushEnabled(boolean alertPushEnabled) {
        this.alertPushEnabled = alertPushEnabled;
    }

    public synchronized boolean isPrecipitationPushEnabled() {
        return precipitationPushEnabled;
    }

    public synchronized void setPrecipitationPushEnabled(boolean precipitationPushEnabled) {
        this.precipitationPushEnabled = precipitationPushEnabled;
    }

    public synchronized UpdateInterval getUpdateInterval() {
        return updateInterval;
    }

    public synchronized void setUpdateInterval(UpdateInterval updateInterval) {
        this.updateInterval = updateInterval;
    }

    public synchronized DarkMode getDarkMode() {
        return darkMode;
    }

    public synchronized void setDarkMode(DarkMode darkMode) {
        this.darkMode = darkMode;
    }

    public synchronized WeatherSource getWeatherSource() {
        return weatherSource;
    }

    public synchronized void setWeatherSource(WeatherSource weatherSource) {
        this.weatherSource = weatherSource;
    }

    public synchronized LocationProvider getLocationProvider() {
        return locationProvider;
    }

    public synchronized void setLocationProvider(LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    public synchronized TemperatureUnit getTemperatureUnit() {
        return temperatureUnit;
    }

    public synchronized void setTemperatureUnit(TemperatureUnit temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
    }

    public synchronized DistanceUnit getDistanceUnit() {
        return distanceUnit;
    }

    public synchronized void setDistanceUnit(DistanceUnit distanceUnit) {
        this.distanceUnit = distanceUnit;
    }

    public synchronized PrecipitationUnit getPrecipitationUnit() {
        return precipitationUnit;
    }

    public synchronized void setPrecipitationUnit(PrecipitationUnit precipitationUnit) {
        this.precipitationUnit = precipitationUnit;
    }

    public synchronized PressureUnit getPressureUnit() {
        return pressureUnit;
    }

    public synchronized void setPressureUnit(PressureUnit pressureUnit) {
        this.pressureUnit = pressureUnit;
    }

    public synchronized SpeedUnit getSpeedUnit() {
        return speedUnit;
    }

    public synchronized void setSpeedUnit(SpeedUnit speedUnit) {
        this.speedUnit = speedUnit;
    }

    public synchronized UIStyle getUiStyle() {
        return uiStyle;
    }

    public synchronized void setUiStyle(UIStyle uiStyle) {
        this.uiStyle = uiStyle;
    }

    public synchronized String getIconProvider() {
        return iconProvider;
    }

    public synchronized void setIconProvider(String iconProvider) {
        this.iconProvider = iconProvider;
    }

    public synchronized List<CardDisplay> getCardDisplayList() {
        return new ArrayList<>(cardDisplayList);
    }

    public synchronized void setCardDisplayList(List<CardDisplay> cardDisplayList) {
        this.cardDisplayList = cardDisplayList;
    }

    public synchronized List<DailyTrendDisplay> getDailyTrendDisplayList() {
        return new ArrayList<>(dailyTrendDisplayList);
    }

    public synchronized void setDailyTrendDisplayList(List<DailyTrendDisplay> dailyTrendDisplayList) {
        this.dailyTrendDisplayList = dailyTrendDisplayList;
    }

    public synchronized boolean isTrendHorizontalLinesEnabled() {
        return trendHorizontalLinesEnabled;
    }

    public synchronized void setTrendHorizontalLinesEnabled(boolean trendHorizontalLinesEnabled) {
        this.trendHorizontalLinesEnabled = trendHorizontalLinesEnabled;
    }

    public synchronized boolean isExchangeDayNightTempEnabled() {
        return exchangeDayNightTempEnabled;
    }

    public synchronized void setExchangeDayNightTempEnabled(boolean exchangeDayNightTempEnabled) {
        this.exchangeDayNightTempEnabled = exchangeDayNightTempEnabled;
    }

    public synchronized boolean isGravitySensorEnabled() {
        return gravitySensorEnabled;
    }

    public synchronized void setGravitySensorEnabled(boolean gravitySensorEnabled) {
        this.gravitySensorEnabled = gravitySensorEnabled;
    }

    public synchronized boolean isListAnimationEnabled() {
        return listAnimationEnabled;
    }

    public synchronized void setListAnimationEnabled(boolean listAnimationEnabled) {
        this.listAnimationEnabled = listAnimationEnabled;
    }

    public synchronized boolean isItemAnimationEnabled() {
        return itemAnimationEnabled;
    }

    public synchronized void setItemAnimationEnabled(boolean itemAnimationEnabled) {
        this.itemAnimationEnabled = itemAnimationEnabled;
    }

    public synchronized Language getLanguage() {
        return language;
    }

    public synchronized void setLanguage(Language language) {
        this.language = language;
    }

    public synchronized boolean isTodayForecastEnabled() {
        return todayForecastEnabled;
    }

    public synchronized void setTodayForecastEnabled(boolean todayForecastEnabled) {
        this.todayForecastEnabled = todayForecastEnabled;
    }

    public synchronized String getTodayForecastTime() {
        return todayForecastTime;
    }

    public synchronized void setTodayForecastTime(String todayForecastTime) {
        this.todayForecastTime = todayForecastTime;
    }

    public synchronized boolean isTomorrowForecastEnabled() {
        return tomorrowForecastEnabled;
    }

    public synchronized void setTomorrowForecastEnabled(boolean tomorrowForecastEnabled) {
        this.tomorrowForecastEnabled = tomorrowForecastEnabled;
    }

    public synchronized String getTomorrowForecastTime() {
        return tomorrowForecastTime;
    }

    public synchronized void setTomorrowForecastTime(String tomorrowForecastTime) {
        this.tomorrowForecastTime = tomorrowForecastTime;
    }

    public synchronized WidgetWeekIconMode getWidgetWeekIconMode() {
        return widgetWeekIconMode;
    }

    public synchronized void setWidgetWeekIconMode(WidgetWeekIconMode widgetWeekIconMode) {
        this.widgetWeekIconMode = widgetWeekIconMode;
    }

    public synchronized boolean isWidgetMinimalIconEnabled() {
        return widgetMinimalIconEnabled;
    }

    public synchronized void setWidgetMinimalIconEnabled(boolean widgetMinimalIconEnabled) {
        this.widgetMinimalIconEnabled = widgetMinimalIconEnabled;
    }

    public synchronized boolean isWidgetClickToRefreshEnabled() {
        return widgetClickToRefreshEnabled;
    }

    public synchronized void setWidgetClickToRefreshEnabled(boolean widgetClickToRefreshEnabled) {
        this.widgetClickToRefreshEnabled = widgetClickToRefreshEnabled;
    }

    public synchronized boolean isNotificationEnabled() {
        return notificationEnabled;
    }

    public synchronized void setNotificationEnabled(boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    public synchronized NotificationStyle getNotificationStyle() {
        return notificationStyle;
    }

    public synchronized void setNotificationStyle(NotificationStyle notificationStyle) {
        this.notificationStyle = notificationStyle;
    }

    public synchronized boolean isNotificationMinimalIconEnabled() {
        return notificationMinimalIconEnabled;
    }

    public synchronized void setNotificationMinimalIconEnabled(boolean notificationMinimalIconEnabled) {
        this.notificationMinimalIconEnabled = notificationMinimalIconEnabled;
    }

    public synchronized boolean isNotificationTemperatureIconEnabled() {
        return notificationTemperatureIconEnabled;
    }

    public synchronized void setNotificationTemperatureIconEnabled(boolean notificationTemperatureIconEnabled) {
        this.notificationTemperatureIconEnabled = notificationTemperatureIconEnabled;
    }

    public synchronized boolean isNotificationCustomColorEnabled() {
        return notificationCustomColorEnabled;
    }

    public synchronized void setNotificationCustomColorEnabled(boolean notificationCustomColorEnabled) {
        this.notificationCustomColorEnabled = notificationCustomColorEnabled;
    }

    public synchronized int getNotificationBackgroundColor() {
        return notificationBackgroundColor;
    }

    public synchronized void setNotificationBackgroundColor(int notificationBackgroundColor) {
        this.notificationBackgroundColor = notificationBackgroundColor;
    }

    public synchronized NotificationTextColor getNotificationTextColor() {
        return notificationTextColor;
    }

    public synchronized void setNotificationTextColor(NotificationTextColor notificationTextColor) {
        this.notificationTextColor = notificationTextColor;
    }

    public synchronized boolean isNotificationCanBeClearedEnabled() {
        return notificationCanBeClearedEnabled;
    }

    public synchronized void setNotificationCanBeClearedEnabled(boolean notificationCanBeClearedEnabled) {
        this.notificationCanBeClearedEnabled = notificationCanBeClearedEnabled;
    }

    public synchronized boolean isNotificationHideIconEnabled() {
        return notificationHideIconEnabled;
    }

    public synchronized void setNotificationHideIconEnabled(boolean notificationHideIconEnabled) {
        this.notificationHideIconEnabled = notificationHideIconEnabled;
    }

    public synchronized boolean isNotificationHideInLockScreenEnabled() {
        return notificationHideInLockScreenEnabled;
    }

    public synchronized void setNotificationHideInLockScreenEnabled(boolean notificationHideInLockScreenEnabled) {
        this.notificationHideInLockScreenEnabled = notificationHideInLockScreenEnabled;
    }

    public synchronized boolean isNotificationHideBigViewEnabled() {
        return notificationHideBigViewEnabled;
    }

    public synchronized void setNotificationHideBigViewEnabled(boolean notificationHideBigViewEnabled) {
        this.notificationHideBigViewEnabled = notificationHideBigViewEnabled;
    }
}
