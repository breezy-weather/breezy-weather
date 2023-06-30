package org.breezyweather.settings

import android.content.Context
import org.breezyweather.BuildConfig
import org.breezyweather.BreezyWeather
import org.breezyweather.common.basic.models.options.*
import org.breezyweather.common.basic.models.options.appearance.*
import org.breezyweather.common.basic.models.options.provider.LocationProvider
import org.breezyweather.weather.openweather.preferences.OpenWeatherOneCallVersion
import org.breezyweather.common.basic.models.options.provider.WeatherSource
import org.breezyweather.common.basic.models.options.unit.DistanceUnit
import org.breezyweather.common.basic.models.options.unit.PrecipitationIntensityUnit
import org.breezyweather.common.basic.models.options.unit.PrecipitationUnit
import org.breezyweather.common.basic.models.options.unit.PressureUnit
import org.breezyweather.common.basic.models.options.unit.SpeedUnit
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.bus.EventBus
import org.breezyweather.weather.accu.preferences.AccuDaysPreference
import org.breezyweather.weather.accu.preferences.AccuHoursPreference
import org.breezyweather.weather.accu.preferences.AccuPortalPreference

class SettingsChangedMessage

class SettingsManager private constructor(context: Context) {

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
                + "&live")
        private const val DEFAULT_DAILY_TREND_DISPLAY = ("temperature"
                + "&air_quality"
                + "&wind"
                + "&uv_index"
                + "&precipitation")
        private const val DEFAULT_HOURLY_TREND_DISPLAY = ("temperature"
                + "&air_quality"
                + "&wind"
                + "&uv_index"
                + "&precipitation")
        private const val DEFAULT_DETAILS_DISPLAY = ("feels_like"
                + "&wind"
                + "&uv_index"
                + "&humidity")

        const val DEFAULT_TODAY_FORECAST_TIME = "07:00"
        const val DEFAULT_TOMORROW_FORECAST_TIME = "21:00"
    }

    private val config = ConfigStore(context)

    // basic.
    var backgroundUpdateMethod: BackgroundUpdateMethod
        set(value) {
            config.edit().putString("background_update_method", value.id).apply()
            notifySettingsChanged()
        }
        get() = BackgroundUpdateMethod.getInstance(
            config.getString("background_update_method", "worker") ?: ""
        )

    var isAlertPushEnabled: Boolean
        set(value) {
            config.edit().putBoolean("alert_notification_switch", value).apply()
            notifySettingsChanged()
        }
        get() = config.getBoolean("alert_notification_switch", true)

    var isPrecipitationPushEnabled: Boolean
        set(value) {
            config.edit().putBoolean("precipitation_notification_switch", value).apply()
            notifySettingsChanged()
        }
        get() = config.getBoolean("precipitation_notification_switch", false)

    var updateInterval: UpdateInterval
        set(value) {
            config.edit().putString("refresh_rate", value.id).apply()
            notifySettingsChanged()
        }
        get() = UpdateInterval.getInstance(
            config.getString("refresh_rate", "1:30") ?: ""
        )

    var darkMode: DarkMode
        set(value) {
            config.edit().putString("dark_mode", value.id).apply()
            notifySettingsChanged()
        }
        get() = DarkMode.getInstance(
            config.getString("dark_mode", "auto") ?: ""
        )

    // service providers.

    var weatherSource: WeatherSource
        set(value) {
            config.edit().putString("weather_source", value.id).apply()
            notifySettingsChanged()
        }
        get() = WeatherSource.getInstance(
            config.getString("weather_source", "accu") ?: ""
        )

    var locationProvider: LocationProvider
        set(value) {
            config.edit().putString("location_service", value.id).apply()
            notifySettingsChanged()
        }
        get() = LocationProvider.getInstance(
            config.getString("location_service", "native") ?: ""
        )

    // unit.

    var temperatureUnit: TemperatureUnit
        set(value) {
            config.edit().putString("temperature_unit", value.id).apply()
            notifySettingsChanged()
        }
        get() = TemperatureUnit.getInstance(
            config.getString("temperature_unit", "c") ?: ""
        )

    var distanceUnit: DistanceUnit
        set(value) {
            config.edit().putString("distance_unit", value.id).apply()
            notifySettingsChanged()
        }
        get() = DistanceUnit.getInstance(
            config.getString("distance_unit", "km") ?: ""
        )

    var precipitationUnit: PrecipitationUnit
        set(value) {
            config.edit().putString("precipitation_unit", value.id).apply()
            notifySettingsChanged()
        }
        get() = PrecipitationUnit.getInstance(
            config.getString("precipitation_unit", "mm") ?: ""
        )

    val precipitationIntensityUnit: PrecipitationIntensityUnit
        get() = PrecipitationIntensityUnit.getInstance(
            (config.getString("precipitation_unit", "mm") ?: "") + "ph"
        )

    var pressureUnit: PressureUnit
        set(value) {
            config.edit().putString("pressure_unit", value.id).apply()
            notifySettingsChanged()
        }
        get() = PressureUnit.getInstance(
            config.getString("pressure_unit", "mb") ?: ""
        )

    var speedUnit: SpeedUnit
        set(value) {
            config.edit().putString("speed_unit", value.id).apply()
            notifySettingsChanged()
        }
        get() = SpeedUnit.getInstance(
            config.getString("speed_unit", "mps") ?: ""
        )

    // appearance.

    var iconProvider: String
        set(value) {
            config
                .edit()
                .putString("iconProvider", value)
                .apply()
            notifySettingsChanged()
        }
        get() = config.getString("iconProvider", BreezyWeather.instance.packageName) ?: ""

    var cardDisplayList: List<CardDisplay>
        set(value) {
            config
                .edit()
                .putString("card_display_2", CardDisplay.toValue(value))
                .apply()
            notifySettingsChanged()
        }
        get() = CardDisplay
            .toCardDisplayList(
                config.getString("card_display_2", DEFAULT_CARD_DISPLAY)
            )
            .toMutableList()

    var dailyTrendDisplayList: List<DailyTrendDisplay>
        set(value) {
            config
                .edit()
                .putString("daily_trend_display", DailyTrendDisplay.toValue(value))
                .apply()
            notifySettingsChanged()
        }
        get() = DailyTrendDisplay
            .toDailyTrendDisplayList(
                config.getString("daily_trend_display", DEFAULT_DAILY_TREND_DISPLAY)
            )
            .toMutableList()

    var hourlyTrendDisplayList: List<HourlyTrendDisplay>
        set(value) {
            config
                .edit()
                .putString("hourly_trend_display", HourlyTrendDisplay.toValue(value))
                .apply()
            notifySettingsChanged()
        }
        get() = HourlyTrendDisplay
            .toHourlyTrendDisplayList(
                config.getString("hourly_trend_display", DEFAULT_HOURLY_TREND_DISPLAY)
            )
            .toMutableList()

    var detailDisplayList: List<DetailDisplay>
        set(value) {
            config
                .edit()
                .putString("details_display", DetailDisplay.toValue(value))
                .apply()
            notifySettingsChanged()
        }
        get() = DetailDisplay
            .toDetailDisplayList(
                config.getString("details_display", DEFAULT_DETAILS_DISPLAY)
            )
            .toMutableList()

    val detailDisplayUnlisted: List<DetailDisplay>
        get() = DetailDisplay
            .toDetailDisplayUnlisted(
                config.getString("details_display", DEFAULT_DETAILS_DISPLAY)
            )
            .toMutableList()

    var isTrendHorizontalLinesEnabled: Boolean
        set(value) {
            config.edit().putBoolean("trend_horizontal_line_switch", value).apply()
            notifySettingsChanged()
        }
        get() = config.getBoolean("trend_horizontal_line_switch", true)

    var isDayNightTempOrderReversed: Boolean
        set(value) {
            config.edit().putBoolean("exchange_day_night_temp_switch", value).apply()
            notifySettingsChanged()
        }
        get() = config.getBoolean("exchange_day_night_temp_switch", false)

    var backgroundAnimationMode: BackgroundAnimationMode
        set(value) {
            config.edit().putString("background_animation_mode", value.id).apply()
            notifySettingsChanged()
        }
        get() = BackgroundAnimationMode.getInstance(
            config.getString("background_animation_mode", "system") ?: ""
        )

    var isGravitySensorEnabled: Boolean
        set(value) {
            config.edit().putBoolean("gravity_sensor_switch", value).apply()
            notifySettingsChanged()
        }
        get() = config.getBoolean("gravity_sensor_switch", true)

    var isCardsFadeInEnabled: Boolean
        set(value) {
            config.edit().putBoolean("list_animation_switch", value).apply()
            notifySettingsChanged()
        }
        get() = config.getBoolean("list_animation_switch", true)

    var isElementsAnimationEnabled: Boolean
        set(value) {
            config.edit().putBoolean("item_animation_switch", value).apply()
            notifySettingsChanged()
        }
        get() = config.getBoolean("item_animation_switch", true)

    var language: Language
        set(value) {
            config.edit().putString("language", value.id).apply()
            notifySettingsChanged()
        }
        get() = Language.getInstance(
            config.getString("language", "follow_system") ?: ""
        )

    // forecast.

    var isTodayForecastEnabled: Boolean
        set(value) {
            config.edit().putBoolean("timing_forecast_switch_today", value).apply()
            notifySettingsChanged()
        }
        get() = config.getBoolean("timing_forecast_switch_today", false)

    var todayForecastTime: String
        set(value) {
            config.edit().putString("forecast_time_today", value).apply()
            notifySettingsChanged()
        }
        get() = config
            .getString("forecast_time_today", DEFAULT_TODAY_FORECAST_TIME)
            ?: DEFAULT_TODAY_FORECAST_TIME

    var isTomorrowForecastEnabled: Boolean
        set(value) {
            config.edit().putBoolean("timing_forecast_switch_tomorrow", value).apply()
            notifySettingsChanged()
        }
        get() = config.getBoolean("timing_forecast_switch_tomorrow", false)

    var tomorrowForecastTime: String
        set(value) {
            config.edit().putString("forecast_time_tomorrow", value).apply()
            notifySettingsChanged()
        }
        get() = config
            .getString("forecast_time_tomorrow", DEFAULT_TOMORROW_FORECAST_TIME)
            ?: DEFAULT_TOMORROW_FORECAST_TIME

    // widget.

    var widgetWeekIconMode: WidgetWeekIconMode
        set(value) {
            config.edit().putString("widget_week_icon_mode", value.id).apply()
            notifySettingsChanged()
        }
        get() = WidgetWeekIconMode.getInstance(
            config.getString("widget_week_icon_mode", "auto") ?: ""
        )

    var isWidgetUsingMonochromeIcons: Boolean
        set(value) {
            config.edit().putBoolean("widget_monochrome_icons", value).apply()
            notifySettingsChanged()
        }
        get() = config.getBoolean("widget_monochrome_icons", false)

    // notification widget
    var isWidgetNotificationEnabled: Boolean
        set(value) {
            config.edit().putBoolean("notification_widget_switch", value).apply()
            notifySettingsChanged()
        }
        get() = config.getBoolean("notification_widget_switch", false)

    var isWidgetNotificationPersistent: Boolean
        set(value) {
            config.edit().putBoolean("notification_widget_persistent_switch", value).apply()
            notifySettingsChanged()
        }
        get() = config.getBoolean("notification_widget_persistent_switch", true)

    var widgetNotificationStyle: NotificationStyle
        set(value) {
            config.edit().putString("notification_widget_style", value.id).apply()
            notifySettingsChanged()
        }
        get() = NotificationStyle.getInstance(
            config.getString("notification_widget_style", "daily") ?: ""
        )

    var isWidgetNotificationTemperatureIconEnabled: Boolean
        set(value) {
            config.edit().putBoolean("notification_widget_temp_icon_switch", value).apply()
            notifySettingsChanged()
        }
        get() = config.getBoolean("notification_widget_temp_icon_switch", false)

    var isWidgetNotificationUsingFeelsLike: Boolean
        set(value) {
            config.edit().putBoolean("notification_widget_feelslike", value).apply()
            notifySettingsChanged()
        }
        get() = config.getBoolean("notification_widget_feelslike", false)

    // service provider advanced
    var customAccuPortal: AccuPortalPreference
        set(value) {
            config.edit().putString("provider_accu_portal", value.id).apply()
            notifySettingsChanged()
        }
        get() = AccuPortalPreference.getInstance(
            config.getString("provider_accu_portal", "enterprise") ?: ""
        )

    var customAccuWeatherKey: String
        set(value) {
            config.edit().putString("provider_accu_weather_key", value).apply()
            notifySettingsChanged()
        }
        get() = config.getString("provider_accu_weather_key", "") ?: ""

    var customAccuDays: AccuDaysPreference
        set(value) {
            config.edit().putString("provider_accu_days", value.id).apply()
            notifySettingsChanged()
        }
        get() = AccuDaysPreference.getInstance(
            config.getString("provider_accu_days", "15") ?: ""
        )

    var customAccuHours: AccuHoursPreference
        set(value) {
            config.edit().putString("provider_accu_hours", value.id).apply()
            notifySettingsChanged()
        }
        get() = AccuHoursPreference.getInstance(
            config.getString("provider_accu_hours", "120") ?: ""
        )

    var customOpenWeatherKey: String
        set(value) {
            config.edit().putString("provider_open_weather_key", value).apply()
            notifySettingsChanged()
        }
        get() = config.getString("provider_open_weather_key", "") ?: ""

    var customOpenWeatherOneCallVersion: OpenWeatherOneCallVersion
        set(value) {
            config.edit().putString("provider_open_weather_one_call_version", value.id).apply()
            notifySettingsChanged()
        }
        get() = OpenWeatherOneCallVersion.getInstance(
            config.getString("provider_open_weather_one_call_version", "2.5") ?: ""
        )

    var customBaiduIpLocationAk: String
        set(value) {
            config.edit().putString("provider_baidu_ip_location_ak", value).apply()
            notifySettingsChanged()
        }
        get() = config.getString("provider_baidu_ip_location_ak", "") ?: ""

    var customMfWsftKey: String
        set(value) {
            config.edit().putString("provider_mf_wsft_key", value).apply()
            notifySettingsChanged()
        }
        get() = config.getString("provider_mf_wsft_key", "") ?: ""

    var customIqaAtmoAuraKey: String
        set(value) {
            config.edit().putString("provider_iqa_atmo_aura_key", value).apply()
            notifySettingsChanged()
        }
        get() = config.getString("provider_iqa_atmo_aura_key", "") ?: ""

    val providerAccuWeatherKey: String
        get() = getProviderSettingValue(
            customValue = customAccuWeatherKey,
            defaultValue = BuildConfig.ACCU_WEATHER_KEY,
        )

    val providerOpenWeatherKey: String
        get() = getProviderSettingValue(
            customValue = customOpenWeatherKey,
            defaultValue = BuildConfig.OPEN_WEATHER_KEY,
        )

    val providerBaiduIpLocationAk: String
        get() = getProviderSettingValue(
            customValue = customBaiduIpLocationAk,
            defaultValue = BuildConfig.BAIDU_IP_LOCATION_AK,
        )

    val providerMfWsftKey: String
        get() = getProviderSettingValue(
            customValue = customMfWsftKey,
            defaultValue = BuildConfig.MF_WSFT_KEY,
        )

    val providerIqaAtmoAuraKey: String
        get() = getProviderSettingValue(
            customValue = customIqaAtmoAuraKey,
            defaultValue = BuildConfig.IQA_ATMO_AURA_KEY,
        )

    private fun getProviderSettingValue(
        customValue: String,
        defaultValue: String,
    ) = customValue.ifEmpty { defaultValue }

    private fun notifySettingsChanged() {
        EventBus
            .instance
            .with(SettingsChangedMessage::class.java)
            .postValue(SettingsChangedMessage())
    }
}
