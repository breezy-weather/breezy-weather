package wangdaye.com.geometricweather.settings

import android.content.Context
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.models.options.*
import wangdaye.com.geometricweather.common.basic.models.options.appearance.CardDisplay
import wangdaye.com.geometricweather.common.basic.models.options.appearance.DailyTrendDisplay
import wangdaye.com.geometricweather.common.basic.models.options.appearance.Language
import wangdaye.com.geometricweather.common.basic.models.options.appearance.UIStyle
import wangdaye.com.geometricweather.common.basic.models.options.provider.LocationProvider
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource
import wangdaye.com.geometricweather.common.basic.models.options.unit.*


class SettingsManager private constructor(context: Context){

    companion object {

        @Volatile
        private var instance: SettingsManager? = null

        @JvmStatic
        fun getInstance(context: Context): SettingsManager {
            if (instance == null) {
                synchronized(SettingsManager::class) {
                    if (instance == null) {
                        instance = SettingsManager(context)
                    }
                }
            }
            return instance!!
        }

        private const val DEFAULT_CARD_DISPLAY = ("daily_overview"
                + "&hourly_overview"
                + "&air_quality"
                + "&allergen"
                + "&sunrise_sunset"
                + "&life_details")
        private const val DEFAULT_DAILY_TREND_DISPLAY = ("temperature"
                + "&air_quality"
                + "&wind"
                + "&uv_index"
                + "&precipitation")

        const val DEFAULT_TODAY_FORECAST_TIME = "07:00"
        const val DEFAULT_TOMORROW_FORECAST_TIME = "21:00"
    }

    // basic.
    private var backgroundFree: Boolean
    private var alertPushEnabled: Boolean
    private var precipitationPushEnabled: Boolean
    private var updateInterval: UpdateInterval
    private var darkMode: DarkMode

    // service provider.
    private var weatherSource: WeatherSource
    private var locationProvider: LocationProvider

    // unit.
    private var temperatureUnit: TemperatureUnit
    private var distanceUnit: DistanceUnit
    private var precipitationUnit: PrecipitationUnit
    private var pressureUnit: PressureUnit
    private var speedUnit: SpeedUnit

    // appearance.
    private var uiStyle: UIStyle
    private var iconProvider: String
    private var cardDisplayList: List<CardDisplay>
    private var dailyTrendDisplayList: List<DailyTrendDisplay>

    private var trendHorizontalLinesEnabled: Boolean
    private var exchangeDayNightTempEnabled: Boolean
    private var gravitySensorEnabled: Boolean
    private var listAnimationEnabled: Boolean
    private var itemAnimationEnabled: Boolean
    private var language: Language

    // forecast.
    private var todayForecastEnabled: Boolean
    private var todayForecastTime: String

    private var tomorrowForecastEnabled: Boolean
    private var tomorrowForecastTime: String

    // widget.
    private var widgetWeekIconMode: WidgetWeekIconMode
    private var widgetMinimalIconEnabled: Boolean
    private var widgetClickToRefreshEnabled: Boolean

    // notification.
    private var notificationEnabled: Boolean
    private var notificationStyle: NotificationStyle
    private var notificationMinimalIconEnabled: Boolean
    private var notificationTemperatureIconEnabled: Boolean
    private var notificationCustomColorEnabled: Boolean

    @ColorInt
    private var notificationBackgroundColor: Int
    private var notificationTextColor: NotificationTextColor
    private var notificationCanBeClearedEnabled: Boolean
    private var notificationHideIconEnabled: Boolean
    private var notificationHideInLockScreenEnabled: Boolean
    private var notificationHideBigViewEnabled: Boolean

    init {
        val config = ConfigStore.getInstance(context)

        // basic.

        // force set background free on android 12+.
        backgroundFree = config.getBoolean(
                context.getString(R.string.key_background_free),
                true
        ) // || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;

        alertPushEnabled = config.getBoolean(
                context.getString(R.string.key_alert_notification_switch), true)

        precipitationPushEnabled = config.getBoolean(
                context.getString(R.string.key_precipitation_notification_switch), false)

        updateInterval = UpdateInterval.getInstance(
                config.getString(
                        context.getString(R.string.key_refresh_rate), "1:30")
        )

        darkMode = DarkMode.getInstance(
                config.getString(
                        context.getString(R.string.key_dark_mode), "auto")
        )

        // service provider.
        weatherSource = WeatherSource.getInstance(
                config.getString(
                        context.getString(R.string.key_weather_source), "accu")
        )

        locationProvider = LocationProvider.getInstance(
                config.getString(
                        context.getString(R.string.key_location_service), "native")
        )

        // unit.
        temperatureUnit = TemperatureUnit.getInstance(
                config.getString(
                        context.getString(R.string.key_temperature_unit), "c")
        )
        distanceUnit = DistanceUnit.getInstance(
                config.getString(
                        context.getString(R.string.key_distance_unit), "km")
        )
        precipitationUnit = PrecipitationUnit.getInstance(
                config.getString(
                        context.getString(R.string.key_precipitation_unit), "mm")
        )
        pressureUnit = PressureUnit.getInstance(
                config.getString(
                        context.getString(R.string.key_pressure_unit), "mb")
        )
        speedUnit = SpeedUnit.getInstance(
                config.getString(
                        context.getString(R.string.key_speed_unit), "mps")
        )

        // appearance.
        uiStyle = UIStyle.getInstance(
                config.getString(
                        context.getString(R.string.key_ui_style), "material")
        )

        iconProvider = config.getString(
                context.getString(R.string.key_icon_provider),
                context.packageName
        )!!

        cardDisplayList = CardDisplay.toCardDisplayList(
                config.getString(context.getString(R.string.key_card_display), DEFAULT_CARD_DISPLAY)
        )

        dailyTrendDisplayList = DailyTrendDisplay.toDailyTrendDisplayList(
                config.getString(
                        context.getString(R.string.key_daily_trend_display),
                        DEFAULT_DAILY_TREND_DISPLAY
                )
        )

        trendHorizontalLinesEnabled = config.getBoolean(
                context.getString(R.string.key_trend_horizontal_line_switch), true)

        exchangeDayNightTempEnabled = config.getBoolean(
                context.getString(R.string.key_exchange_day_night_temp_switch), false)

        gravitySensorEnabled = config.getBoolean(
                context.getString(R.string.key_gravity_sensor_switch), true)

        listAnimationEnabled = config.getBoolean(
                context.getString(R.string.key_list_animation_switch), true)

        itemAnimationEnabled = config.getBoolean(
                context.getString(R.string.key_item_animation_switch), true)

        language = Language.getInstance(
                config.getString(
                        context.getString(R.string.key_language), "follow_system")
        )

        // forecast.
        todayForecastEnabled = config.getBoolean(
                context.getString(R.string.key_forecast_today), false)

        todayForecastTime = config.getString(
                context.getString(R.string.key_forecast_today_time), DEFAULT_TODAY_FORECAST_TIME)!!

        tomorrowForecastEnabled = config.getBoolean(
                context.getString(R.string.key_forecast_tomorrow), false)

        tomorrowForecastTime = config.getString(
                context.getString(R.string.key_forecast_tomorrow_time), DEFAULT_TOMORROW_FORECAST_TIME)!!

        // widget.
        widgetWeekIconMode = WidgetWeekIconMode.getInstance(
                config.getString(
                        context.getString(R.string.key_week_icon_mode), "auto")
        )

        widgetMinimalIconEnabled = config.getBoolean(
                context.getString(R.string.key_widget_minimal_icon), false)

        widgetClickToRefreshEnabled = config.getBoolean(
                context.getString(R.string.key_click_widget_to_refresh), false)

        // notification.
        notificationEnabled = config.getBoolean(
                context.getString(R.string.key_notification), false)

        notificationStyle = NotificationStyle.getInstance(
                config.getString(
                        context.getString(R.string.key_notification_style), "daily")
        )

        notificationMinimalIconEnabled = config.getBoolean(
                context.getString(R.string.key_notification_minimal_icon), false)

        notificationTemperatureIconEnabled = config.getBoolean(
                context.getString(R.string.key_notification_temp_icon), false)

        notificationCustomColorEnabled = config.getBoolean(
                context.getString(R.string.key_notification_custom_color), false)

        notificationBackgroundColor = config.getInt(
                context.getString(R.string.key_notification_background_color),
                ContextCompat.getColor(context, R.color.notification_background_l))

        notificationTextColor = NotificationTextColor.getInstance(
                config.getString(
                        context.getString(R.string.key_notification_text_color), "dark")
        )

        notificationCanBeClearedEnabled = config.getBoolean(
                context.getString(R.string.key_notification_can_be_cleared), false)

        notificationHideIconEnabled = config.getBoolean(
                context.getString(R.string.key_notification_hide_icon), false)

        notificationHideInLockScreenEnabled = config.getBoolean(
                context.getString(R.string.key_notification_hide_in_lockScreen), false)

        notificationHideBigViewEnabled = config.getBoolean(
                context.getString(R.string.key_notification_hide_big_view), false)
    }

    @Synchronized
    fun isBackgroundFree(): Boolean {
        return backgroundFree
    }

    @Synchronized
    fun setBackgroundFree(backgroundFree: Boolean) {
        this.backgroundFree = backgroundFree
    }

    @Synchronized
    fun isAlertPushEnabled(): Boolean {
        return alertPushEnabled
    }

    @Synchronized
    fun setAlertPushEnabled(alertPushEnabled: Boolean) {
        this.alertPushEnabled = alertPushEnabled
    }

    @Synchronized
    fun isPrecipitationPushEnabled(): Boolean {
        return precipitationPushEnabled
    }

    @Synchronized
    fun setPrecipitationPushEnabled(precipitationPushEnabled: Boolean) {
        this.precipitationPushEnabled = precipitationPushEnabled
    }

    @Synchronized
    fun getUpdateInterval(): UpdateInterval {
        return updateInterval
    }

    @Synchronized
    fun setUpdateInterval(updateInterval: UpdateInterval) {
        this.updateInterval = updateInterval
    }

    @Synchronized
    fun getDarkMode(): DarkMode {
        return darkMode
    }

    @Synchronized
    fun setDarkMode(darkMode: DarkMode) {
        this.darkMode = darkMode
    }

    @Synchronized
    fun getWeatherSource(): WeatherSource {
        return weatherSource
    }

    @Synchronized
    fun setWeatherSource(weatherSource: WeatherSource) {
        this.weatherSource = weatherSource
    }

    @Synchronized
    fun getLocationProvider(): LocationProvider {
        return locationProvider
    }

    @Synchronized
    fun setLocationProvider(locationProvider: LocationProvider) {
        this.locationProvider = locationProvider
    }

    @Synchronized
    fun getTemperatureUnit(): TemperatureUnit {
        return temperatureUnit
    }

    @Synchronized
    fun setTemperatureUnit(temperatureUnit: TemperatureUnit) {
        this.temperatureUnit = temperatureUnit
    }

    @Synchronized
    fun getDistanceUnit(): DistanceUnit {
        return distanceUnit
    }

    @Synchronized
    fun setDistanceUnit(distanceUnit: DistanceUnit) {
        this.distanceUnit = distanceUnit
    }

    @Synchronized
    fun getPrecipitationUnit(): PrecipitationUnit {
        return precipitationUnit
    }

    @Synchronized
    fun setPrecipitationUnit(precipitationUnit: PrecipitationUnit) {
        this.precipitationUnit = precipitationUnit
    }

    @Synchronized
    fun getPressureUnit(): PressureUnit {
        return pressureUnit
    }

    @Synchronized
    fun setPressureUnit(pressureUnit: PressureUnit) {
        this.pressureUnit = pressureUnit
    }

    @Synchronized
    fun getSpeedUnit(): SpeedUnit {
        return speedUnit
    }

    @Synchronized
    fun setSpeedUnit(speedUnit: SpeedUnit) {
        this.speedUnit = speedUnit
    }

    @Synchronized
    fun getUiStyle(): UIStyle {
        return uiStyle
    }

    @Synchronized
    fun setUiStyle(uiStyle: UIStyle) {
        this.uiStyle = uiStyle
    }

    @Synchronized
    fun getIconProvider(): String {
        return iconProvider
    }

    @Synchronized
    fun setIconProvider(iconProvider: String) {
        this.iconProvider = iconProvider
    }

    @Synchronized
    fun getCardDisplayList(): List<CardDisplay> {
        return ArrayList(cardDisplayList)
    }

    @Synchronized
    fun setCardDisplayList(cardDisplayList: List<CardDisplay>) {
        this.cardDisplayList = cardDisplayList
    }

    @Synchronized
    fun getDailyTrendDisplayList(): List<DailyTrendDisplay> {
        return ArrayList(dailyTrendDisplayList)
    }

    @Synchronized
    fun setDailyTrendDisplayList(dailyTrendDisplayList: List<DailyTrendDisplay>) {
        this.dailyTrendDisplayList = dailyTrendDisplayList
    }

    @Synchronized
    fun isTrendHorizontalLinesEnabled(): Boolean {
        return trendHorizontalLinesEnabled
    }

    @Synchronized
    fun setTrendHorizontalLinesEnabled(trendHorizontalLinesEnabled: Boolean) {
        this.trendHorizontalLinesEnabled = trendHorizontalLinesEnabled
    }

    @Synchronized
    fun isExchangeDayNightTempEnabled(): Boolean {
        return exchangeDayNightTempEnabled
    }

    @Synchronized
    fun setExchangeDayNightTempEnabled(exchangeDayNightTempEnabled: Boolean) {
        this.exchangeDayNightTempEnabled = exchangeDayNightTempEnabled
    }

    @Synchronized
    fun isGravitySensorEnabled(): Boolean {
        return gravitySensorEnabled
    }

    @Synchronized
    fun setGravitySensorEnabled(gravitySensorEnabled: Boolean) {
        this.gravitySensorEnabled = gravitySensorEnabled
    }

    @Synchronized
    fun isListAnimationEnabled(): Boolean {
        return listAnimationEnabled
    }

    @Synchronized
    fun setListAnimationEnabled(listAnimationEnabled: Boolean) {
        this.listAnimationEnabled = listAnimationEnabled
    }

    @Synchronized
    fun isItemAnimationEnabled(): Boolean {
        return itemAnimationEnabled
    }

    @Synchronized
    fun setItemAnimationEnabled(itemAnimationEnabled: Boolean) {
        this.itemAnimationEnabled = itemAnimationEnabled
    }

    @Synchronized
    fun getLanguage(): Language {
        return language
    }

    @Synchronized
    fun setLanguage(language: Language) {
        this.language = language
    }

    @Synchronized
    fun isTodayForecastEnabled(): Boolean {
        return todayForecastEnabled
    }

    @Synchronized
    fun setTodayForecastEnabled(todayForecastEnabled: Boolean) {
        this.todayForecastEnabled = todayForecastEnabled
    }

    @Synchronized
    fun getTodayForecastTime(): String {
        return todayForecastTime
    }

    @Synchronized
    fun setTodayForecastTime(todayForecastTime: String) {
        this.todayForecastTime = todayForecastTime
    }

    @Synchronized
    fun isTomorrowForecastEnabled(): Boolean {
        return tomorrowForecastEnabled
    }

    @Synchronized
    fun setTomorrowForecastEnabled(tomorrowForecastEnabled: Boolean) {
        this.tomorrowForecastEnabled = tomorrowForecastEnabled
    }

    @Synchronized
    fun getTomorrowForecastTime(): String {
        return tomorrowForecastTime
    }

    @Synchronized
    fun setTomorrowForecastTime(tomorrowForecastTime: String) {
        this.tomorrowForecastTime = tomorrowForecastTime
    }

    @Synchronized
    fun getWidgetWeekIconMode(): WidgetWeekIconMode {
        return widgetWeekIconMode
    }

    @Synchronized
    fun setWidgetWeekIconMode(widgetWeekIconMode: WidgetWeekIconMode) {
        this.widgetWeekIconMode = widgetWeekIconMode
    }

    @Synchronized
    fun isWidgetMinimalIconEnabled(): Boolean {
        return widgetMinimalIconEnabled
    }

    @Synchronized
    fun setWidgetMinimalIconEnabled(widgetMinimalIconEnabled: Boolean) {
        this.widgetMinimalIconEnabled = widgetMinimalIconEnabled
    }

    @Synchronized
    fun isWidgetClickToRefreshEnabled(): Boolean {
        return widgetClickToRefreshEnabled
    }

    @Synchronized
    fun setWidgetClickToRefreshEnabled(widgetClickToRefreshEnabled: Boolean) {
        this.widgetClickToRefreshEnabled = widgetClickToRefreshEnabled
    }

    @Synchronized
    fun isNotificationEnabled(): Boolean {
        return notificationEnabled
    }

    @Synchronized
    fun setNotificationEnabled(notificationEnabled: Boolean) {
        this.notificationEnabled = notificationEnabled
    }

    @Synchronized
    fun getNotificationStyle(): NotificationStyle {
        return notificationStyle
    }

    @Synchronized
    fun setNotificationStyle(notificationStyle: NotificationStyle) {
        this.notificationStyle = notificationStyle
    }

    @Synchronized
    fun isNotificationMinimalIconEnabled(): Boolean {
        return notificationMinimalIconEnabled
    }

    @Synchronized
    fun setNotificationMinimalIconEnabled(notificationMinimalIconEnabled: Boolean) {
        this.notificationMinimalIconEnabled = notificationMinimalIconEnabled
    }

    @Synchronized
    fun isNotificationTemperatureIconEnabled(): Boolean {
        return notificationTemperatureIconEnabled
    }

    @Synchronized
    fun setNotificationTemperatureIconEnabled(notificationTemperatureIconEnabled: Boolean) {
        this.notificationTemperatureIconEnabled = notificationTemperatureIconEnabled
    }

    @Synchronized
    fun isNotificationCustomColorEnabled(): Boolean {
        return notificationCustomColorEnabled
    }

    @Synchronized
    fun setNotificationCustomColorEnabled(notificationCustomColorEnabled: Boolean) {
        this.notificationCustomColorEnabled = notificationCustomColorEnabled
    }

    @Synchronized
    fun getNotificationBackgroundColor(): Int {
        return notificationBackgroundColor
    }

    @Synchronized
    fun setNotificationBackgroundColor(notificationBackgroundColor: Int) {
        this.notificationBackgroundColor = notificationBackgroundColor
    }

    @Synchronized
    fun getNotificationTextColor(): NotificationTextColor {
        return notificationTextColor
    }

    @Synchronized
    fun setNotificationTextColor(notificationTextColor: NotificationTextColor) {
        this.notificationTextColor = notificationTextColor
    }

    @Synchronized
    fun isNotificationCanBeClearedEnabled(): Boolean {
        return notificationCanBeClearedEnabled
    }

    @Synchronized
    fun setNotificationCanBeClearedEnabled(notificationCanBeClearedEnabled: Boolean) {
        this.notificationCanBeClearedEnabled = notificationCanBeClearedEnabled
    }

    @Synchronized
    fun isNotificationHideIconEnabled(): Boolean {
        return notificationHideIconEnabled
    }

    @Synchronized
    fun setNotificationHideIconEnabled(notificationHideIconEnabled: Boolean) {
        this.notificationHideIconEnabled = notificationHideIconEnabled
    }

    @Synchronized
    fun isNotificationHideInLockScreenEnabled(): Boolean {
        return notificationHideInLockScreenEnabled
    }

    @Synchronized
    fun setNotificationHideInLockScreenEnabled(notificationHideInLockScreenEnabled: Boolean) {
        this.notificationHideInLockScreenEnabled = notificationHideInLockScreenEnabled
    }

    @Synchronized
    fun isNotificationHideBigViewEnabled(): Boolean {
        return notificationHideBigViewEnabled
    }

    @Synchronized
    fun setNotificationHideBigViewEnabled(notificationHideBigViewEnabled: Boolean) {
        this.notificationHideBigViewEnabled = notificationHideBigViewEnabled
    }
}