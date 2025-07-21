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
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.sources.metie.json.MetIeHourly
import org.breezyweather.sources.metie.json.MetIeLocationResult
import org.breezyweather.sources.metie.json.MetIeWarning
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

internal fun convert(
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

internal fun getDailyForecast(
    location: Location,
    hourlyResult: List<MetIeHourly>,
): List<DailyWrapper> {
    val dailyList = mutableListOf<DailyWrapper>()
    val hourlyListByDay = hourlyResult.groupBy { it.date }
    for (i in 0 until hourlyListByDay.entries.size - 1) {
        val dayDate = hourlyListByDay.keys.toTypedArray()[i].toDateNoHour(location.javaTimeZone)
        if (dayDate != null) {
            dailyList.add(
                DailyWrapper(
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
internal fun getHourlyForecast(
    hourlyResult: List<MetIeHourly>,
): List<HourlyWrapper> {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Europe/Dublin")

    return hourlyResult.map { result ->
        HourlyWrapper(
            date = formatter.parse("${result.date} ${result.time}")!!,
            weatherCode = getWeatherCode(result.weatherNumber),
            weatherText = result.weatherDescription,
            temperature = TemperatureWrapper(
                temperature = result.temperature?.toDouble()
            ),
            precipitation = Precipitation(
                total = result.rainfall?.toDoubleOrNull()
            ),
            wind = Wind(
                degree = result.windDirection?.toDoubleOrNull(),
                speed = result.windSpeed?.div(3.6)
            ),
            relativeHumidity = result.humidity?.toDoubleOrNull(),
            pressure = result.pressure?.toDoubleOrNull()
        )
    }
}

internal fun getAlertList(location: Location, warnings: List<MetIeWarning>?): List<Alert>? {
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

private fun getWeatherCode(icon: String?): WeatherCode? {
    if (icon == null) return null
    return with(icon) {
        when {
            startsWith("01") ||
                startsWith("02") -> WeatherCode.CLEAR
            startsWith("03") -> WeatherCode.PARTLY_CLOUDY
            startsWith("04") -> WeatherCode.CLOUDY
            startsWith("05") ||
                startsWith("09") ||
                startsWith("10") ||
                startsWith("40") ||
                startsWith("41") ||
                startsWith("46") -> WeatherCode.RAIN
            startsWith("06") ||
                startsWith("11") ||
                startsWith("14") ||
                startsWith("2") ||
                startsWith("30") ||
                startsWith("31") ||
                startsWith("32") ||
                startsWith("33") ||
                startsWith("34") -> WeatherCode.THUNDERSTORM
            startsWith("07") ||
                startsWith("12") ||
                startsWith("42") ||
                startsWith("43") ||
                startsWith("47") ||
                startsWith("48") -> WeatherCode.SLEET
            startsWith("08") ||
                startsWith("13") ||
                startsWith("44") ||
                startsWith("45") ||
                startsWith("49") ||
                startsWith("50") -> WeatherCode.SNOW
            startsWith("15") -> WeatherCode.FOG
            startsWith("51") ||
                startsWith("52") -> WeatherCode.HAIL
            else -> null
        }
    }
}
