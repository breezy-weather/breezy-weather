/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.settings

import android.content.Context
import androidx.core.text.util.LocalePreferences
import org.breezyweather.BreezyWeather
import org.breezyweather.BuildConfig
import org.breezyweather.common.basic.models.options.DarkMode
import org.breezyweather.common.basic.models.options.NotificationStyle
import org.breezyweather.common.basic.models.options.UpdateInterval
import org.breezyweather.common.basic.models.options.WidgetWeekIconMode
import org.breezyweather.common.basic.models.options.appearance.BackgroundAnimationMode
import org.breezyweather.common.basic.models.options.appearance.CardDisplay
import org.breezyweather.common.basic.models.options.appearance.DailyTrendDisplay
import org.breezyweather.common.basic.models.options.appearance.DetailDisplay
import org.breezyweather.common.basic.models.options.appearance.HourlyTrendDisplay
import org.breezyweather.common.basic.models.options.unit.DistanceUnit
import org.breezyweather.common.basic.models.options.unit.PrecipitationIntensityUnit
import org.breezyweather.common.basic.models.options.unit.PrecipitationUnit
import org.breezyweather.common.basic.models.options.unit.PressureUnit
import org.breezyweather.common.basic.models.options.unit.SpeedUnit
import org.breezyweather.common.basic.models.options.unit.TemperatureUnit
import org.breezyweather.common.bus.EventBus

class SettingsChangedMessage

class SettingsManager private constructor(context: Context) {

    companion object {

        @Volatile
        private var instance: SettingsManager? = null

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

       const val DEFAULT_CARD_DISPLAY = ("precipitation_nowcast"
            + "&daily_overview"
            + "&hourly_overview"
            + "&air_quality"
            + "&pollen"
            + "&sunrise_sunset"
            + "&live")
        const val DEFAULT_DAILY_TREND_DISPLAY = ("temperature"
            + "&air_quality"
            + "&wind"
            + "&uv_index"
            + "&precipitation"
            + "&sunshine"
            + "&feels_like")
        const val DEFAULT_HOURLY_TREND_DISPLAY = ("temperature"
            + "&air_quality"
            + "&wind"
            + "&uv_index"
            + "&precipitation"
            + "&feels_like"
            + "&humidity"
            + "&pressure"
            + "&cloud_cover"
            + "&visibility")
        private const val DEFAULT_DETAILS_DISPLAY = ("feels_like"
            + "&wind"
            + "&uv_index"
            + "&humidity")

        const val DEFAULT_TODAY_FORECAST_TIME = "07:00"
        const val DEFAULT_TOMORROW_FORECAST_TIME = "21:00"
    }

    private val config = ConfigStore(context)

    // App updates
    var lastVersionCode: Int
        set(value) {
            config.edit().putInt("last_version_code", value).apply()
        }
        get() = config.getInt("last_version_code", 0)

    var isAppUpdateCheckEnabled: Boolean
        set(value) {
            config.edit().putBoolean("app_update_check_switch", value).apply()
        }
        get() = config.getBoolean("app_update_check_switch", false)

    var isAppUpdateCheckPromptAlreadyAsked: Boolean
        set(value) {
            config.edit().putBoolean("app_update_check_prompt", value).apply()
        }
        get() = config.getBoolean("app_update_check_prompt", false)

    var appUpdateCheckLastTimestamp: Long
        set(value) {
            config.edit().putLong("app_update_check_last_timestamp", value).apply()
        }
        get() = config.getLong("app_update_check_last_timestamp", 0)

    // Weather updates
    var weatherUpdateLastTimestamp: Long
        set(value) {
            config.edit().putLong("weather_update_last_timestamp", value).apply()
        }
        get() = config.getLong("weather_update_last_timestamp", 0)

    var weatherManualUpdateLastTimestamp: Long
        set(value) {
            config.edit().putLong("weather_manual_update_last_timestamp", value).apply()
        }
        get() = config.getLong("weather_manual_update_last_timestamp", 0)

    var weatherManualUpdateLastLocationId: String
        set(value) {
            config.edit().putString("weather_manual_update_last_location_id", value).apply()
        }
        get() = config.getString("weather_manual_update_last_location_id", null) ?: ""

    // basic.
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
            config.getString("refresh_rate", null)
                ?: (if (BreezyWeather.instance.debugMode) "never" else "1:30")
        )

    var ignoreUpdatesWhenBatteryLow: Boolean
        set(value) {
            config.edit().putBoolean("refresh_ignore_battery_low", value).apply()
            notifySettingsChanged()
        }
        get() = config.getBoolean("refresh_ignore_battery_low", true)

    var darkMode: DarkMode
        set(value) {
            config.edit().putString("dark_mode", value.id).apply()
            notifySettingsChanged()
        }
        get() = DarkMode.getInstance(
            config.getString("dark_mode", null) ?: "system"
        )

    var dayNightModeForLocations: Boolean
        set(value) {
            config.edit().putBoolean("day_night_mode_locations", value).apply()
            notifySettingsChanged()
        }
        get() = config.getBoolean("day_night_mode_locations", true)

    // service providers.
    var locationSource: String
        set(value) {
            config.edit().putString("location_service", value).apply()
            notifySettingsChanged()
        }
        get() = if (BuildConfig.FLAVOR != "freenet") {
            config.getString("location_service", null) ?: BuildConfig.DEFAULT_LOCATION_SOURCE
        } else "native"

    var defaultWeatherSource: String
        set(value) {
            config.edit().putString("default_weather_source", value).apply()
            notifySettingsChanged()
        }
        get() = config.getString("default_weather_source", null) ?: BuildConfig.DEFAULT_WEATHER_SOURCE

    // unit.
    var temperatureUnit: TemperatureUnit
        set(value) {
            config.edit().putString("temperature_unit", value.id).apply()
            notifySettingsChanged()
        }
        get() = TemperatureUnit.getInstance(
            config.getString("temperature_unit", null) ?: defaultTemperatureUnit
        )

    private val defaultTemperatureUnit: String
        get() {
            return when (LocalePreferences.getTemperatureUnit()) {
                LocalePreferences.TemperatureUnit.FAHRENHEIT -> "f"
                LocalePreferences.TemperatureUnit.KELVIN -> "k"
                else -> "c"
            }
        }

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

    var languageUpdateLastTimestamp: Long
        set(value) {
            config.edit().putLong("language_update_last_timestamp", value).apply()
        }
        get() = config.getLong("language_update_last_timestamp", 0)

    var alternateCalendar: String
        set(value) {
            config.edit().putString("calendar_alternate", value).apply()
        }
        get() = config.getString("calendar_alternate", null) ?: ""

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

    private fun notifySettingsChanged() {
        EventBus
            .instance
            .with(SettingsChangedMessage::class.java)
            .postValue(SettingsChangedMessage())
    }
}
