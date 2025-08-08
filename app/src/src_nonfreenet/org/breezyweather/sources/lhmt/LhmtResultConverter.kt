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

package org.breezyweather.sources.lhmt

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import com.google.maps.android.model.LatLng
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.sources.lhmt.json.LhmtAlertText
import org.breezyweather.sources.lhmt.json.LhmtAlertsResult
import org.breezyweather.sources.lhmt.json.LhmtLocationsResult
import org.breezyweather.sources.lhmt.json.LhmtWeatherResult
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

internal fun getCurrent(
    context: Context,
    currentResult: LhmtWeatherResult,
): CurrentWrapper? {
    return currentResult.observations?.last()?.let {
        CurrentWrapper(
            weatherText = getWeatherText(context, it.conditionCode),
            weatherCode = getWeatherCode(it.conditionCode),
            temperature = TemperatureWrapper(
                temperature = it.airTemperature,
                feelsLike = it.feelsLikeTemperature
            ),
            wind = Wind(
                degree = it.windDirection,
                speed = it.windSpeed,
                gusts = it.windGust
            ),
            relativeHumidity = it.relativeHumidity,
            pressure = it.seaLevelPressure,
            cloudCover = it.cloudCover?.toInt()
        )
    }
}

internal fun getDailyForecast(
    hourlyForecast: List<HourlyWrapper>?,
): List<DailyWrapper> {
    if (hourlyForecast.isNullOrEmpty()) return emptyList()

    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Europe/Vilnius")
    val hourlyListDates = hourlyForecast.groupBy { formatter.format(it.date) }.keys

    return hourlyListDates.map {
        DailyWrapper(
            date = formatter.parse(it)!!
        )
    }.dropLast(1) // Remove last (incomplete) daily item
}

internal fun getHourlyForecast(
    context: Context,
    forecastResult: LhmtWeatherResult,
): List<HourlyWrapper> {
    val hourlyList = mutableListOf<HourlyWrapper>()
    forecastResult.forecastTimestamps?.forEach {
        if (it.forecastTimeUtc != null) {
            hourlyList.add(
                HourlyWrapper(
                    date = it.forecastTimeUtc,
                    weatherText = getWeatherText(context, it.conditionCode),
                    weatherCode = getWeatherCode(it.conditionCode),
                    temperature = TemperatureWrapper(
                        temperature = it.airTemperature,
                        feelsLike = it.feelsLikeTemperature
                    ),
                    precipitation = Precipitation(
                        total = it.totalPrecipitation
                    ),
                    wind = Wind(
                        degree = it.windDirection,
                        speed = it.windSpeed,
                        gusts = it.windGust
                    ),
                    relativeHumidity = it.relativeHumidity,
                    pressure = it.seaLevelPressure,
                    cloudCover = it.cloudCover?.toInt()
                )
            )
        }
    }
    return hourlyList
}

internal fun getAlertList(
    context: Context,
    location: Location,
    alertsResult: LhmtAlertsResult,
): List<Alert> {
    val id = "lhmt"
    val municipality = location.parameters.getOrElse(id) { null }?.getOrElse("municipality") { null }
    val county = location.parameters.getOrElse(id) { null }?.getOrElse("county") { null }
    if (municipality.isNullOrEmpty() || county.isNullOrEmpty()) {
        throw InvalidLocationException()
    }

    val alertList = mutableListOf<Alert>()
    var severity: AlertSeverity
    var active: Boolean
    alertsResult.phenomenonGroups?.forEach { phenomenonGroup ->
        phenomenonGroup.areaGroups?.forEach { areaGroup ->
            active = false
            areaGroup.areas?.forEach { area ->
                active = active || (area.id.endsWith(municipality) || area.id.endsWith(county))
            }
            if (active) {
                areaGroup.singleAlerts?.forEach { singleAlert ->
                    if (singleAlert.responseType?.none != true) {
                        severity = getAlertSeverity(singleAlert.severity)
                        alertList.add(
                            Alert(
                                alertId = singleAlert.phenomenon +
                                    " " +
                                    singleAlert.severity +
                                    " " +
                                    singleAlert.tFrom?.time.toString(),
                                startDate = singleAlert.tFrom,
                                endDate = singleAlert.tTo,
                                headline = getAlertText(context, singleAlert.headline),
                                description = getAlertText(context, singleAlert.description),
                                instruction = getAlertText(context, singleAlert.instruction),
                                source = "Lietuvos hidrometeorologijos tarnyba",
                                severity = severity,
                                color = Alert.colorFromSeverity(severity)
                            )
                        )
                    }
                }
            }
        }
    }
    return alertList
}

private fun getWeatherText(
    context: Context,
    code: String?,
): String? {
    return when (code) {
        "clear" -> context.getString(R.string.common_weather_text_clear_sky)
        "partly-cloudy" -> context.getString(R.string.common_weather_text_partly_cloudy)
        "variable-cloudiness" -> context.getString(R.string.common_weather_text_partly_cloudy)
        "cloudy-with-sunny-intervals" -> context.getString(R.string.common_weather_text_partly_cloudy)
        "cloudy" -> context.getString(R.string.common_weather_text_partly_cloudy)
        "rain-showers" -> context.getString(R.string.common_weather_text_rain_showers)
        "light-rain-at-times" -> context.getString(R.string.common_weather_text_rain_light)
        "rain-at-times" -> context.getString(R.string.common_weather_text_rain)
        "light-rain" -> context.getString(R.string.common_weather_text_rain_light)
        "rain" -> context.getString(R.string.common_weather_text_rain)
        "heavy-rain" -> context.getString(R.string.common_weather_text_rain_heavy)
        "thunder" -> context.getString(R.string.weather_kind_thunder)
        "isolated-thunderstorms" -> context.getString(R.string.weather_kind_thunderstorm)
        "thunderstorms" -> context.getString(R.string.weather_kind_thunderstorm)
        "heavy-rain-with-thunderstorms" -> context.getString(R.string.weather_kind_thunderstorm)
        "sleet-showers" -> context.getString(R.string.common_weather_text_rain_snow_mixed_showers)
        "sleet-at-times" -> context.getString(R.string.common_weather_text_rain_snow_mixed)
        "light-sleet" -> context.getString(R.string.common_weather_text_rain_snow_mixed_light)
        "sleet" -> context.getString(R.string.common_weather_text_rain_snow_mixed)
        "freezing-rain" -> context.getString(R.string.common_weather_text_rain_freezing)
        "hail" -> context.getString(R.string.weather_kind_hail)
        "snow-showers" -> context.getString(R.string.common_weather_text_snow_showers)
        "light-snow-at-times" -> context.getString(R.string.common_weather_text_snow_light)
        "snow-at-times" -> context.getString(R.string.common_weather_text_snow)
        "light-snow" -> context.getString(R.string.common_weather_text_snow_light)
        "snow" -> context.getString(R.string.common_weather_text_snow)
        "heavy-snow" -> context.getString(R.string.common_weather_text_snow_heavy)
        "snowstorm" -> context.getString(R.string.common_weather_text_snow_heavy)
        "fog" -> context.getString(R.string.common_weather_text_fog)
        "squall" -> context.getString(R.string.common_weather_text_squall)
        else -> null
    }
}

private fun getWeatherCode(
    code: String?,
): WeatherCode? {
    return when (code) {
        "clear" -> WeatherCode.CLEAR
        "partly-cloudy" -> WeatherCode.PARTLY_CLOUDY
        "variable-cloudiness" -> WeatherCode.PARTLY_CLOUDY
        "cloudy-with-sunny-intervals" -> WeatherCode.PARTLY_CLOUDY
        "cloudy" -> WeatherCode.CLOUDY
        "rain-showers" -> WeatherCode.RAIN
        "light-rain-at-times" -> WeatherCode.RAIN
        "rain-at-times" -> WeatherCode.RAIN
        "light-rain" -> WeatherCode.RAIN
        "rain" -> WeatherCode.RAIN
        "heavy-rain" -> WeatherCode.RAIN
        "thunder" -> WeatherCode.THUNDER
        "isolated-thunderstorms" -> WeatherCode.THUNDERSTORM
        "thunderstorms" -> WeatherCode.THUNDERSTORM
        "heavy-rain-with-thunderstorms" -> WeatherCode.THUNDERSTORM
        "sleet-showers" -> WeatherCode.SLEET
        "sleet-at-times" -> WeatherCode.SLEET
        "light-sleet" -> WeatherCode.SLEET
        "sleet" -> WeatherCode.SLEET
        "freezing-rain" -> WeatherCode.SLEET
        "hail" -> WeatherCode.HAIL
        "snow-showers" -> WeatherCode.SNOW
        "light-snow-at-times" -> WeatherCode.SNOW
        "snow-at-times" -> WeatherCode.SNOW
        "light-snow" -> WeatherCode.SNOW
        "snow" -> WeatherCode.SNOW
        "heavy-snow" -> WeatherCode.SNOW
        "snowstorm" -> WeatherCode.SNOW
        "fog" -> WeatherCode.FOG
        "squall" -> WeatherCode.WIND
        else -> null
    }
}

private fun getAlertText(
    context: Context,
    text: LhmtAlertText?,
): String? {
    return if (context.currentLocale.code.startsWith("lt")) {
        text?.lt
    } else {
        text?.en
    }
}

private fun getAlertSeverity(
    severity: String?,
): AlertSeverity {
    return with(severity) {
        when {
            equals("Extreme", ignoreCase = true) -> AlertSeverity.EXTREME
            equals("Severe", ignoreCase = true) -> AlertSeverity.SEVERE
            equals("Moderate", ignoreCase = true) -> AlertSeverity.MODERATE
            equals("Minor", ignoreCase = true) -> AlertSeverity.MINOR
            else -> AlertSeverity.UNKNOWN
        }
    }
}
