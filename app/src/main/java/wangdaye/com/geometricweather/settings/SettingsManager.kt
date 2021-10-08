package wangdaye.com.geometricweather.settings

import android.content.Context
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import wangdaye.com.geometricweather.BuildConfig
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

    private val config = ConfigStore.getInstance(context)

    // basic.
    private val backgroundFree = context.getString(R.string.key_background_free)
    private val alertPushEnabled = context.getString(R.string.key_alert_notification_switch)
    private val precipitationPushEnabled = context.getString(R.string.key_precipitation_notification_switch)
    private val updateInterval = context.getString(R.string.key_refresh_rate)
    private val darkMode = context.getString(R.string.key_dark_mode)

    // service provider.
    private val weatherSource = context.getString(R.string.key_weather_source)
    private val locationProvider = context.getString(R.string.key_location_service)

    // unit.
    private val temperatureUnit = context.getString(R.string.key_temperature_unit)
    private val distanceUnit = context.getString(R.string.key_distance_unit)
    private val precipitationUnit = context.getString(R.string.key_precipitation_unit)
    private val pressureUnit = context.getString(R.string.key_pressure_unit)
    private val speedUnit = context.getString(R.string.key_speed_unit)

    // appearance.
    private val uiStyle = context.getString(R.string.key_ui_style)
    private val iconProvider = context.getString(R.string.key_icon_provider)
    private val cardDisplayList = context.getString(R.string.key_card_display)
    private val dailyTrendDisplayList = context.getString(R.string.key_daily_trend_display)

    private val trendHorizontalLinesEnabled = context.getString(R.string.key_trend_horizontal_line_switch)
    private val exchangeDayNightTempEnabled = context.getString(R.string.key_exchange_day_night_temp_switch)
    private val gravitySensorEnabled = context.getString(R.string.key_gravity_sensor_switch)
    private val listAnimationEnabled = context.getString(R.string.key_list_animation_switch)
    private val itemAnimationEnabled = context.getString(R.string.key_item_animation_switch)
    private val language = context.getString(R.string.key_language)

    // forecast.
    private val todayForecastEnabled = context.getString(R.string.key_forecast_today)
    private val todayForecastTime = context.getString(R.string.key_forecast_today_time)

    private val tomorrowForecastEnabled = context.getString(R.string.key_forecast_tomorrow)
    private val tomorrowForecastTime = context.getString(R.string.key_forecast_tomorrow_time)

    // widget.
    private val widgetWeekIconMode = context.getString(R.string.key_week_icon_mode)
    private val widgetMinimalIconEnabled = context.getString(R.string.key_widget_minimal_icon)

    // notification.
    private val notificationEnabled = context.getString(R.string.key_notification)
    private val notificationStyle = context.getString(R.string.key_notification_style)
    private val notificationMinimalIconEnabled = context.getString(R.string.key_notification_minimal_icon)
    private val notificationTemperatureIconEnabled = context.getString(R.string.key_notification_temp_icon)
    private val notificationCustomColorEnabled = context.getString(R.string.key_notification_custom_color)

    private val notificationBackgroundColor = context.getString(R.string.key_notification_background_color)
    private val notificationTextColor = context.getString(R.string.key_notification_text_color)
    private val notificationCanBeClearedEnabled = context.getString(R.string.key_notification_can_be_cleared)
    private val notificationHideIconEnabled = context.getString(R.string.key_notification_hide_icon)
    private val notificationHideInLockScreenEnabled = context.getString(R.string.key_notification_hide_in_lockScreen)
    private val notificationHideBigViewEnabled = context.getString(R.string.key_notification_hide_big_view)

    // service providers
    private val providerAccuWeatherKey = context.getString(R.string.key_provider_accu_weather_key)
    private val providerAccuCurrentKey = context.getString(R.string.key_provider_accu_current_key)
    private val providerAccuAqiKey = context.getString(R.string.key_provider_accu_aqi_key)

    private val providerOwmKey = context.getString(R.string.key_provider_owm_key)

    private val providerBaiduIpLocationAk = context.getString(R.string.key_provider_baidu_ip_location_ak)

    private val providerMfWsftKey = context.getString(R.string.key_provider_mf_wsft_key)
    private val providerIqaAirParifKey = context.getString(R.string.key_provider_iqa_air_parif_key)
    private val providerIqaAtmoAuraKey = context.getString(R.string.key_provider_iqa_atmo_aura_key)

    fun preload() {
        config.preload()
    }

    fun isBackgroundFree(): Boolean {
        return config.getBoolean(backgroundFree, true)
        // || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    }

    fun isAlertPushEnabled(): Boolean {
        return config.getBoolean(alertPushEnabled, true)
    }

    fun isPrecipitationPushEnabled(): Boolean {
        return config.getBoolean(precipitationPushEnabled, false)
    }

    fun getUpdateInterval(): UpdateInterval {
        return UpdateInterval.getInstance(
                config.getString(updateInterval, "1:30"))
    }

    fun getDarkMode(): DarkMode {
        return DarkMode.getInstance(
                config.getString(darkMode, "auto"))
    }

    fun getWeatherSource(): WeatherSource {
        return WeatherSource.getInstance(
                config.getString(weatherSource, "accu"))
    }

    fun getLocationProvider(): LocationProvider {
        return LocationProvider.getInstance(
                config.getString(locationProvider, "native"))
    }

    fun setLocationProvider(provider: LocationProvider) {
        config.edit().putString(locationProvider, provider.providerId).apply()
    }

    fun getTemperatureUnit(): TemperatureUnit {
        return TemperatureUnit.getInstance(
                config.getString(temperatureUnit, "c"))
    }

    fun getDistanceUnit(): DistanceUnit {
        return DistanceUnit.getInstance(
                config.getString(distanceUnit, "km"))
    }

    fun getPrecipitationUnit(): PrecipitationUnit {
        return PrecipitationUnit.getInstance(
                config.getString(precipitationUnit, "mm"))
    }

    fun getPressureUnit(): PressureUnit {
        return PressureUnit.getInstance(
                config.getString(pressureUnit, "mb"))
    }

    fun getSpeedUnit(): SpeedUnit {
        return SpeedUnit.getInstance(
                config.getString(speedUnit, "mps"))
    }

    fun getUiStyle(): UIStyle {
        return UIStyle.getInstance(
                config.getString(uiStyle, "material"))
    }

    fun getIconProvider(context: Context): String {
        return config.getString(iconProvider, context.packageName)!!
    }

    fun setIconProvider(packageName: String) {
        config.edit().putString(iconProvider, packageName).apply()
    }

    fun getCardDisplayList(): List<CardDisplay> {
        return ArrayList(
                CardDisplay.toCardDisplayList(
                        config.getString(cardDisplayList, DEFAULT_CARD_DISPLAY)
                )
        )
    }

    fun setCardDisplayList(list: List<CardDisplay>) {
        config.edit()
                .putString(cardDisplayList, CardDisplay.toValue(list))
                .apply()
    }

    fun getDailyTrendDisplayList(): List<DailyTrendDisplay> {
        return ArrayList(
                DailyTrendDisplay.toDailyTrendDisplayList(
                        config.getString(dailyTrendDisplayList, DEFAULT_DAILY_TREND_DISPLAY)
                )
        )
    }

    fun setDailyTrendDisplayList(list: List<DailyTrendDisplay>) {
        config.edit()
                .putString(dailyTrendDisplayList, DailyTrendDisplay.toValue(list))
                .apply()
    }

    fun isTrendHorizontalLinesEnabled(): Boolean {
        return config.getBoolean(trendHorizontalLinesEnabled, true)
    }

    fun isExchangeDayNightTempEnabled(): Boolean {
        return config.getBoolean(exchangeDayNightTempEnabled, false)
    }

    fun isGravitySensorEnabled(): Boolean {
        return config.getBoolean(gravitySensorEnabled, true)
    }

    fun isListAnimationEnabled(): Boolean {
        return config.getBoolean(listAnimationEnabled, true)
    }

    fun isItemAnimationEnabled(): Boolean {
        return config.getBoolean(itemAnimationEnabled, true)
    }

    fun getLanguage(): Language {
        return Language.getInstance(
                config.getString(language, "follow_system"))
    }

    fun isTodayForecastEnabled(): Boolean {
        return config.getBoolean(todayForecastEnabled, false)
    }

    fun getTodayForecastTime(): String {
        return config.getString(todayForecastTime, DEFAULT_TODAY_FORECAST_TIME)!!
    }

    fun setTodayForecastTime(time: String) {
        return config.edit().putString(todayForecastTime, time).apply()
    }

    fun isTomorrowForecastEnabled(): Boolean {
        return config.getBoolean(tomorrowForecastEnabled, false)
    }

    fun getTomorrowForecastTime(): String {
        return config.getString(tomorrowForecastTime, DEFAULT_TOMORROW_FORECAST_TIME)!!
    }

    fun setTomorrowForecastTime(time: String) {
        return config.edit().putString(tomorrowForecastTime, time).apply()
    }

    fun getWidgetWeekIconMode(): WidgetWeekIconMode {
        return WidgetWeekIconMode.getInstance(
                config.getString(widgetWeekIconMode, "auto"))
    }

    fun isWidgetMinimalIconEnabled(): Boolean {
        return config.getBoolean(widgetMinimalIconEnabled, false)
    }

    fun isNotificationEnabled(): Boolean {
        return config.getBoolean(notificationEnabled, false)
    }

    fun getNotificationStyle(): NotificationStyle {
        return NotificationStyle.getInstance(
                config.getString(notificationStyle, "daily"))
    }

    fun isNotificationMinimalIconEnabled(): Boolean {
        return config.getBoolean(notificationMinimalIconEnabled, false)
    }

    fun isNotificationTemperatureIconEnabled(): Boolean {
        return config.getBoolean(notificationTemperatureIconEnabled, false)
    }

    fun isNotificationCustomColorEnabled(): Boolean {
        return config.getBoolean(notificationCustomColorEnabled, false)
    }

    @ColorInt
    fun getNotificationBackgroundColor(context: Context): Int {
        return config.getInt(
                notificationBackgroundColor,
                ContextCompat.getColor(context, R.color.notification_background_l)
        )
    }

    fun getNotificationTextColor(): NotificationTextColor {
        return NotificationTextColor.getInstance(
                config.getString(notificationTextColor, "dark"))
    }

    fun isNotificationCanBeClearedEnabled(): Boolean {
        return config.getBoolean(notificationCanBeClearedEnabled, false)
    }

    fun isNotificationHideIconEnabled(): Boolean {
        return config.getBoolean(notificationHideIconEnabled, false)
    }

    fun isNotificationHideInLockScreenEnabled(): Boolean {
        return config.getBoolean(notificationHideInLockScreenEnabled, false)
    }

    fun isNotificationHideBigViewEnabled(): Boolean {
        return config.getBoolean(notificationHideBigViewEnabled, false)
    }

    private fun getProviderSettingValue(
            key: String,
            defaultValue: String?,
            useDefaultValue: Boolean
    ): String {
        val prefValue = config.getString(key, "")

        return (if (prefValue == null || prefValue.isEmpty()) {
            if (useDefaultValue) defaultValue else null
        } else {
            prefValue
        }) ?: ""
    }

    fun getProviderAccuWeatherKey(useDefaultValue: Boolean): String {
        return getProviderSettingValue(
                providerAccuWeatherKey,
                BuildConfig.ACCU_WEATHER_KEY,
                useDefaultValue
        )
    }

    fun getProviderAccuCurrentKey(useDefaultValue: Boolean): String {
        return getProviderSettingValue(
                providerAccuCurrentKey,
                BuildConfig.ACCU_CURRENT_KEY,
                useDefaultValue
        )
    }

    fun getProviderAccuAqiKey(useDefaultValue: Boolean): String {
        return getProviderSettingValue(
                providerAccuAqiKey,
                BuildConfig.ACCU_AQI_KEY,
                useDefaultValue
        )
    }

    fun getProviderOwmKey(useDefaultValue: Boolean): String {
        return getProviderSettingValue(
                providerOwmKey,
                BuildConfig.OWM_KEY,
                useDefaultValue
        )
    }

    fun getProviderBaiduIpLocationAk(useDefaultValue: Boolean): String {
        return getProviderSettingValue(
                providerBaiduIpLocationAk,
                BuildConfig.BAIDU_IP_LOCATION_AK,
                useDefaultValue
        )
    }

    fun getProviderMfWsftKey(useDefaultValue: Boolean): String {
        return getProviderSettingValue(
                providerMfWsftKey,
                BuildConfig.MF_WSFT_KEY,
                useDefaultValue
        )
    }

    fun getProviderIqaAirParifKey(useDefaultValue: Boolean): String {
        return getProviderSettingValue(
                providerIqaAirParifKey,
                BuildConfig.IQA_AIR_PARIF_KEY,
                useDefaultValue
        )
    }

    fun getProviderIqaAtmoAuraKey(useDefaultValue: Boolean): String {
        return getProviderSettingValue(
                providerIqaAtmoAuraKey,
                BuildConfig.IQA_ATMO_AURA_KEY,
                useDefaultValue
        )
    }
}