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

package org.breezyweather.sources.meteolux

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import com.google.maps.android.SphericalUtil
import com.google.maps.android.model.LatLng
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.sources.getWindDegree
import org.breezyweather.sources.meteolux.json.MeteoLuxWeatherResult
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

internal fun getCurrent(
    context: Context,
    weatherResult: MeteoLuxWeatherResult,
): CurrentWrapper? {
    return weatherResult.forecast?.current?.let {
        CurrentWrapper(
            weatherText = getWeatherText(context, it.icon?.id),
            weatherCode = getWeatherCode(it.icon?.id),
            temperature = TemperatureWrapper(
                temperature = it.temperature?.temperature,
                feelsLike = it.temperature?.felt
            ),
            wind = Wind(
                degree = getWindDegree(it.wind?.direction),
                speed = getRangeMax(it.wind?.speed)?.div(3.6), // convert km/h to m/s
                gusts = getRangeMax(it.wind?.gusts)?.div(3.6) // convert km/h to m/s
            )
        )
    }
}

internal fun getDailyForecast(
    context: Context,
    weatherResult: MeteoLuxWeatherResult,
): List<DailyWrapper> {
    val dailyList = mutableListOf<DailyWrapper>()
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Europe/Luxembourg")
    weatherResult.forecast?.daily?.forEach {
        dailyList.add(
            DailyWrapper(
                date = formatter.parse(it.date)!!,
                day = HalfDayWrapper(
                    weatherText = getWeatherText(context, it.icon?.id),
                    weatherCode = getWeatherCode(it.icon?.id),
                    temperature = TemperatureWrapper(
                        temperature = it.temperatureMax?.temperature,
                        feelsLike = it.temperatureMax?.felt
                    ),
                    precipitation = Precipitation(
                        rain = getRangeMax(it.rain),
                        snow = getRangeMax(it.snow)?.times(10.0) // convert cm to mm
                    ),
                    wind = Wind(
                        degree = getWindDegree(it.wind?.direction),
                        speed = getRangeMax(it.wind?.speed)?.div(3.6), // convert km/h to m/s
                        gusts = getRangeMax(it.wind?.gusts)?.div(3.6) // convert km/h to m/s
                    )
                ),
                night = HalfDayWrapper(
                    weatherText = getWeatherText(context, it.icon?.id),
                    weatherCode = getWeatherCode(it.icon?.id),
                    temperature = TemperatureWrapper(
                        temperature = it.temperatureMin?.temperature,
                        feelsLike = it.temperatureMin?.felt
                    ),
                    wind = Wind(
                        degree = getWindDegree(it.wind?.direction),
                        speed = getRangeMax(it.wind?.speed)?.div(3.6), // convert km/h to m/s
                        gusts = getRangeMax(it.wind?.gusts)?.div(3.6) // convert km/h to m/s
                    )
                ),
                uV = UV(
                    index = it.uvIndex
                ),
                sunshineDuration = it.sunshine
            )
        )
    }
    return dailyList
}

internal fun getHourlyForecast(
    context: Context,
    weatherResult: MeteoLuxWeatherResult,
): List<HourlyWrapper> {
    val hourlyList = mutableListOf<HourlyWrapper>()
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Europe/Luxembourg")
    weatherResult.forecast?.hourly?.forEach {
        hourlyList.add(
            HourlyWrapper(
                date = formatter.parse(it.date)!!,
                weatherText = getWeatherText(context, it.icon?.id),
                weatherCode = getWeatherCode(it.icon?.id),
                temperature = TemperatureWrapper(
                    temperature = it.temperature?.temperature?.average(),
                    feelsLike = it.temperature?.felt
                ),
                precipitation = Precipitation(
                    rain = getRangeMax(it.rain),
                    snow = getRangeMax(it.snow)?.times(10) // convert cm to mm
                ),
                wind = Wind(
                    degree = getWindDegree(it.wind?.direction),
                    speed = getRangeMax(it.wind?.speed)?.div(3.6), // convert km/h to m/s
                    gusts = getRangeMax(it.wind?.gusts)?.div(3.6) // convert km/h to m/s
                )
            )
        )
    }
    return hourlyList
}

internal fun getAlertList(
    context: Context,
    weatherResult: MeteoLuxWeatherResult,
): List<Alert> {
    val alertList = mutableListOf<Alert>()
    var severity: AlertSeverity

    weatherResult.vigilances?.forEach {
        severity = when (it.level) {
            2 -> AlertSeverity.MODERATE
            3 -> AlertSeverity.SEVERE
            4 -> AlertSeverity.EXTREME
            else -> AlertSeverity.UNKNOWN
        }
        if (it.region == "all" ||
            (it.region ?: "").uppercase().startsWith((weatherResult.city?.region ?: "").uppercase())
        ) {
            alertList.add(
                Alert(
                    alertId = "${it.type} ${it.level} ${it.datetimeStart}",
                    startDate = it.datetimeStart,
                    endDate = it.datetimeEnd,
                    headline = getAlertHeadline(
                        context = context,
                        type = it.type,
                        level = it.level
                    ),
                    description = it.description,
                    source = "MeteoLux",
                    severity = severity,
                    color = Alert.colorFromSeverity(severity)
                )
            )
        }
    }
    return alertList
}

private fun getWeatherText(
    context: Context,
    icon: Int?,
): String? {
    // These icon codes are not the same as those from
    // https://metapi.ana.lu/api/v1/metapp/text?lang=en
    //
    // TODO: Check the correct text for the following icon codes:
    // 15: may be "freezing fog"
    // 20: may be "heavy rain and drizzle"
    // 50: may be "thunderstorm with rain and snow mixed"
    // 51: may be "light thunder snowstorm"
    // 52: may be "thunder snowstorm"
    // 48: migrate string to common_weather_text?
    // 53: migrate string to common_weather_text?
    //
    // The meaning of the following icon codes are unclear:
    // 28, 29 -> sun/moon, cloud, and solid circles??
    // 47 -> two lightning bolts with solid circles??
    // 57 -> sun, cloud, thunder, no precipitation?
    // 58 -> sun, cloud, thunder rain shower?
    // 59 -> sun, cloud, thunder snow shower?
    return when (icon) {
        1, 7 -> context.getString(R.string.common_weather_text_clear_sky)
        2, 8 -> context.getString(R.string.common_weather_text_mostly_clear)
        3, 9 -> context.getString(R.string.common_weather_text_partly_cloudy)
        4, 10 -> context.getString(R.string.common_weather_text_cloudy)
        5 -> context.getString(R.string.common_weather_text_overcast)
        12, 13 -> context.getString(R.string.common_weather_text_mist)
        14, 15 -> context.getString(R.string.common_weather_text_fog)
        17 -> context.getString(R.string.common_weather_text_drizzle_light)
        18 -> context.getString(R.string.common_weather_text_drizzle)
        19, 20 -> context.getString(R.string.common_weather_text_drizzle_heavy)
        21 -> context.getString(R.string.common_weather_text_rain_light)
        22 -> context.getString(R.string.common_weather_text_rain_moderate)
        23 -> context.getString(R.string.common_weather_text_rain_heavy)
        24 -> context.getString(R.string.common_weather_text_rain_snow_mixed)
        25 -> context.getString(R.string.common_weather_text_snow_light)
        26 -> context.getString(R.string.common_weather_text_snow)
        27 -> context.getString(R.string.common_weather_text_rain_freezing)
        30, 36 -> context.getString(R.string.common_weather_text_rain_showers_light)
        31, 37 -> context.getString(R.string.common_weather_text_rain_showers_heavy)
        32, 38 -> context.getString(R.string.common_weather_text_rain_snow_mixed_showers)
        33, 39 -> context.getString(R.string.common_weather_text_snow_showers_light)
        34, 40 -> context.getString(R.string.common_weather_text_snow_showers_heavy)
        35, 41 -> context.getString(R.string.weather_kind_hail)
        42, 57 -> context.getString(R.string.weather_kind_thunder)
        43 -> context.getString(R.string.common_weather_text_rain_snow_mixed_light)
        44, 45 -> context.getString(R.string.common_weather_text_rain_snow_mixed_showers_light)
        48 -> context.getString(R.string.openmeteo_weather_text_thunderstorm_slight_or_moderate)
        49, 51, 52, 58, 59 -> context.getString(R.string.weather_kind_thunderstorm)
        53 -> context.getString(R.string.openmeteo_weather_text_thunderstorm_with_heavy_hail)
        54 -> context.getString(R.string.weather_kind_wind)
        55 -> context.getString(R.string.common_weather_text_hot)
        56 -> context.getString(R.string.common_weather_text_cold)
        else -> null
    }
}

private fun getWeatherCode(
    icon: Int?,
): WeatherCode? {
    return when (icon) {
        1, 2, 7, 8 -> WeatherCode.CLEAR
        3, 9 -> WeatherCode.PARTLY_CLOUDY
        4, 5, 10 -> WeatherCode.CLOUDY
        12, 13, 14, 15 -> WeatherCode.FOG
        17, 18, 19, 20, 21, 22, 23, 30, 31, 36, 37 -> WeatherCode.RAIN
        24, 27, 32, 38, 43, 44, 45 -> WeatherCode.SLEET
        25, 26, 33, 34, 39, 40 -> WeatherCode.SNOW
        35, 41 -> WeatherCode.HAIL
        54 -> WeatherCode.WIND
        42 -> WeatherCode.THUNDER
        48, 49, 50, 51, 52, 53 -> WeatherCode.THUNDERSTORM
        // 28, 29 -> sun/moon, cloud, and round drops??
        // 47 -> two lightnings with solid round drops?
        // 55 -> hot
        // 56 -> cold
        // 57 -> sun cloud thunder?
        // 58 -> sun cloud thunder rain?
        // 59 -> sun cloud thunder snow?
        else -> null
    }
}

private fun getRangeMax(
    speed: String?,
): Double? {
    if (speed == null) {
        return null
    }
    val speedRangeRegex = Regex("""(\d+)$""")
    val matchResult = speedRangeRegex.find(speed)
    return matchResult?.groups?.get(1)?.value?.toDoubleOrNull()
}

// Source: https://metapi.ana.lu/api/v1/metapp/text?lang=en
private fun getAlertHeadline(
    context: Context,
    type: Int?,
    level: Int?,
): String? {
    var headline = when (type) {
        2 -> context.getString(R.string.meteolux_warning_text_wind)
        3 -> context.getString(R.string.meteolux_warning_text_rain)
        4 -> context.getString(R.string.meteolux_warning_text_snow)
        5 -> context.getString(R.string.meteolux_warning_text_black_ice)
        6 -> context.getString(R.string.meteolux_warning_text_thunderstorm)
        9 -> context.getString(R.string.meteolux_warning_text_heat)
        10 -> context.getString(R.string.meteolux_warning_text_cold)
        11 -> context.getString(R.string.meteolux_warning_text_flood)
        13 -> context.getString(R.string.meteolux_warning_text_ozone)
        14 -> context.getString(R.string.meteolux_warning_text_pm)
        else -> null
    }

    // warning types 11, 13, 14 are not issued by MeteoLux
    // and therefore not assigned text to go with warning levels
    if (listOf(2, 3, 4, 5, 6, 9, 10).contains(type)) {
        headline += when (level) {
            2 -> " – " + context.getString(R.string.meteolux_warning_text_level_2)
            3 -> " – " + context.getString(R.string.meteolux_warning_text_level_3)
            4 -> " – " + context.getString(R.string.meteolux_warning_text_level_4)
            else -> ""
        }
    }
    return headline
}
