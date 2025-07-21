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

package org.breezyweather.sources.brightsky

import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.AlertSeverity
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import org.breezyweather.common.extensions.getIsoFormattedDate
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.sources.brightsky.json.BrightSkyAlert
import org.breezyweather.sources.brightsky.json.BrightSkyCurrentWeather
import org.breezyweather.sources.brightsky.json.BrightSkyWeather

/**
 * Returns current weather
 */
internal fun getCurrent(result: BrightSkyCurrentWeather?): CurrentWrapper? {
    if (result == null) return null
    return CurrentWrapper(
        weatherCode = getWeatherCode(result.icon),
        temperature = TemperatureWrapper(
            temperature = result.temperature
        ),
        wind = Wind(
            degree = result.windDirection?.toDouble(),
            speed = result.windSpeed?.div(3.6),
            gusts = result.windGustSpeed?.div(3.6)
        ),
        relativeHumidity = result.relativeHumidity?.toDouble(),
        dewPoint = result.dewPoint,
        pressure = result.pressure,
        cloudCover = result.cloudCover,
        visibility = result.visibility?.toDouble()
    )
}

/**
 * Generate empty daily days from hourly weather since daily doesn't exist in API
 */
internal fun getDailyForecast(
    location: Location,
    weatherResult: List<BrightSkyWeather>?,
): List<DailyWrapper>? {
    if (weatherResult == null) return null

    val dailyList = mutableListOf<DailyWrapper>()
    val hourlyListByDay = weatherResult.groupBy {
        it.timestamp.getIsoFormattedDate(location)
    }
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
    weatherResult: List<BrightSkyWeather>?,
): List<HourlyWrapper>? {
    if (weatherResult == null) return null

    return weatherResult.map { result ->
        HourlyWrapper(
            date = result.timestamp,
            weatherCode = getWeatherCode(result.icon),
            temperature = TemperatureWrapper(
                temperature = result.temperature
            ),
            precipitation = Precipitation(
                total = result.precipitation
            ),
            precipitationProbability = PrecipitationProbability(
                total = result.precipitationProbability?.toDouble()
            ),
            wind = Wind(
                degree = result.windDirection?.toDouble(),
                speed = result.windSpeed?.div(3.6),
                gusts = result.windGustSpeed?.div(3.6)
            ),
            relativeHumidity = result.relativeHumidity?.toDouble(),
            dewPoint = result.dewPoint,
            pressure = result.pressure,
            cloudCover = result.cloudCover,
            visibility = result.visibility?.toDouble(),
            sunshineDuration = result.sunshine?.div(60)
        )
    }
}

/**
 * Returns alerts
 */
internal fun getAlertList(alertList: List<BrightSkyAlert>?, languageCode: String): List<Alert>? {
    if (alertList.isNullOrEmpty()) return null
    return alertList.map { alert ->
        Alert(
            alertId = alert.id.toString(),
            startDate = alert.onset,
            endDate = alert.expires,
            headline = if (languageCode == "de") alert.headlineDe else alert.headlineEn,
            description = if (languageCode == "de") alert.descriptionDe else alert.descriptionEn,
            instruction = if (languageCode == "de") alert.instructionDe else alert.instructionEn,
            source = "Deutscher Wetterdienst",
            severity = when (alert.severity?.lowercase()) {
                "extreme" -> AlertSeverity.EXTREME
                "severe" -> AlertSeverity.SEVERE
                "moderate" -> AlertSeverity.MODERATE
                "minor" -> AlertSeverity.MINOR
                else -> AlertSeverity.UNKNOWN
            },
            color = when (alert.severity?.lowercase()) {
                "extreme" -> Color.rgb(241, 48, 255)
                "severe" -> Color.rgb(255, 48, 48)
                "moderate" -> Color.rgb(255, 179, 48)
                "minor" -> Color.rgb(255, 238, 48)
                else -> Alert.colorFromSeverity(AlertSeverity.UNKNOWN)
            }
        )
    }
}

private fun getWeatherCode(icon: String?): WeatherCode? {
    return when (icon) {
        "clear-day", "clear-night" -> WeatherCode.CLEAR
        "partly-cloudy-day", "partly-cloudy-night" -> WeatherCode.PARTLY_CLOUDY
        "cloudy" -> WeatherCode.CLOUDY
        "fog" -> WeatherCode.FOG
        "wind" -> WeatherCode.WIND
        "rain" -> WeatherCode.RAIN
        "sleet" -> WeatherCode.SLEET
        "snow" -> WeatherCode.SNOW
        "hail" -> WeatherCode.HAIL
        "thunderstorm" -> WeatherCode.THUNDERSTORM
        else -> null
    }
}
