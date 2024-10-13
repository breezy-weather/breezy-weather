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

package org.breezyweather.sources.metie

import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.AlertSeverity
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.sources.metie.json.MetIeLocationResult
import org.breezyweather.sources.metie.json.MetIeWarning
import org.breezyweather.sources.metie.json.MetIeWarningResult
import org.breezyweather.sources.metie.xml.MetIeTime
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

fun convert(
    location: Location,
    result: MetIeLocationResult,
): Location {
    return location.copy(
        timeZone = "Europe/Dublin",
        country = "Ireland",
        countryCode = "IE",
        admin2 = result.county,
        city = result.city ?: ""
    )
}
fun convert(
    hourlyResult: List<MetIeTime>?,
    warningsResult: MetIeWarningResult?,
    location: Location,
): WeatherWrapper {
    // If the API doesn’t return data, consider data as garbage and keep cached data
    if (hourlyResult.isNullOrEmpty()) {
        throw InvalidOrIncompleteDataException()
    }

    return WeatherWrapper(
        dailyForecast = getDailyForecast(location, hourlyResult),
        hourlyForecast = getHourlyForecast(hourlyResult),
        alertList = getAlertList(location, warningsResult?.warnings?.national)
    )
}

private fun getDailyForecast(
    location: Location,
    hourlyResult: List<MetIeTime>,
): List<Daily> {
    val dailyList = mutableListOf<Daily>()
    val hourlyListByDay = hourlyResult.groupBy {
        it.from.getFormattedDate("yyyy-MM-dd", location)
    }
    for (i in 0 until hourlyListByDay.entries.size - 1) {
        val dayDate = hourlyListByDay.keys.toTypedArray()[i].toDateNoHour(location.javaTimeZone)
        if (dayDate != null) {
            dailyList.add(
                Daily(
                    date = dayDate
                )
            )
        }
    }
    return dailyList
}

/**
 * Returns hourly forecast
 */
private fun getHourlyForecast(
    hourlyResult: List<MetIeTime>,
): List<HourlyWrapper> {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Europe/Dublin")

    // TODO: Deduplicate
    // There are two blocks, not from the same period
    return hourlyResult.mapNotNull { time ->
        time.location?.let { result ->
            HourlyWrapper(
                date = time.from,
                weatherCode = getWeatherCode(result.symbol?.number),
                temperature = Temperature(
                    temperature = result.temperature?.value
                ),
                precipitation = Precipitation(
                    total = result.precipitation?.value
                ),
                precipitationProbability = PrecipitationProbability(
                    total = result.precipitation?.probability
                ),
                wind = Wind(
                    degree = result.windDirection?.deg,
                    speed = result.windSpeed?.mps,
                    gusts = result.windGust?.mps
                ),
                relativeHumidity = result.humidity?.percent,
                dewPoint = result.dewpointTemperature?.value,
                pressure = result.pressure?.value,
                cloudCover = result.cloudiness?.percent?.roundToInt()
            )
        }
    }
}

fun getAlertList(location: Location, warnings: List<MetIeWarning>?): List<Alert>? {
    if (warnings == null) return null
    if (warnings.isEmpty()) return emptyList()

    val region = if (MetIeService.regionsMapping.containsKey(location.admin2)) {
        location.admin2
    } else {
        location.parameters.getOrElse("metie") { null }?.getOrElse("region") { null }
    }
    val eiRegion = region?.let { MetIeService.regionsMapping.getOrElse(region) { null } }

    return warnings
        .filter {
            // National
            it.regions.contains("EI0") ||
                it.regions.contains(eiRegion)
        }
        .map { alert ->
            val severity = when (alert.severity?.lowercase()) {
                "extreme" -> AlertSeverity.EXTREME
                "severe" -> AlertSeverity.SEVERE
                "moderate" -> AlertSeverity.MODERATE
                "minor" -> AlertSeverity.MINOR
                else -> AlertSeverity.UNKNOWN
            }
            Alert(
                alertId = alert.id,
                startDate = alert.onset,
                endDate = alert.expiry,
                headline = alert.headline,
                description = alert.description,
                source = "MET Éireann",
                severity = severity,
                color = when (alert.level?.lowercase()) {
                    "red" -> Color.rgb(224, 0, 0)
                    "orange" -> Color.rgb(255, 140, 0)
                    "yellow" -> Color.rgb(255, 255, 0)
                    else -> Alert.colorFromSeverity(severity)
                }
            )
        }
}

fun convertSecondary(
    warningsResult: MetIeWarningResult?,
    location: Location,
): SecondaryWeatherWrapper {
    return SecondaryWeatherWrapper(
        alertList = getAlertList(location, warningsResult?.warnings?.national)
    )
}

private fun getWeatherCode(icon: Int?): WeatherCode? {
    if (icon == null) return null
    return when (icon) {
        1, 2 -> WeatherCode.CLEAR
        3 -> WeatherCode.PARTLY_CLOUDY
        4 -> WeatherCode.CLOUDY
        5, 9, 10, 40, 41, 46 -> WeatherCode.RAIN
        6, 11, 14, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34 -> WeatherCode.THUNDERSTORM
        7, 12, 42, 43, 47, 48 -> WeatherCode.SLEET
        8, 13, 44, 45, 49, 50 -> WeatherCode.SNOW
        15 -> WeatherCode.FOG
        51, 52 -> WeatherCode.HAIL
        else -> null
    }
}
