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
import breezyweather.domain.weather.model.Current
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
import org.breezyweather.sources.brightsky.json.BrightSkyAlert
import org.breezyweather.sources.brightsky.json.BrightSkyAlertsResult
import org.breezyweather.sources.brightsky.json.BrightSkyCurrentWeather
import org.breezyweather.sources.brightsky.json.BrightSkyCurrentWeatherResult
import org.breezyweather.sources.brightsky.json.BrightSkyWeather
import org.breezyweather.sources.brightsky.json.BrightSkyWeatherResult

/**
 * Converts Bright Sky result into a forecast
 */
fun convert(
    weatherResult: BrightSkyWeatherResult,
    currentWeatherResult: BrightSkyCurrentWeatherResult,
    alertsResult: BrightSkyAlertsResult,
    location: Location,
    languageCode: String,
): WeatherWrapper {
    // If the API doesn’t return weather, consider data as garbage and keep cached data
    if (weatherResult.weather.isNullOrEmpty()) {
        throw InvalidOrIncompleteDataException()
    }

    return WeatherWrapper(
        current = getCurrent(currentWeatherResult.weather),
        dailyForecast = getDailyForecast(location, weatherResult.weather),
        hourlyForecast = getHourlyForecast(weatherResult.weather),
        alertList = getAlertList(alertsResult.alerts, languageCode)
    )
}

/**
 * Returns current weather
 */
private fun getCurrent(result: BrightSkyCurrentWeather?): Current? {
    if (result == null) return null
    return Current(
        weatherCode = getWeatherCode(result.icon),
        temperature = Temperature(
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
private fun getDailyForecast(
    location: Location,
    weatherResult: List<BrightSkyWeather>,
): List<Daily> {
    val dailyList = mutableListOf<Daily>()
    val hourlyListByDay = weatherResult.groupBy {
        it.timestamp.getFormattedDate("yyyy-MM-dd", location)
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
    weatherResult: List<BrightSkyWeather>,
): List<HourlyWrapper> {
    return weatherResult.map { result ->
        HourlyWrapper(
            date = result.timestamp,
            weatherCode = getWeatherCode(result.icon),
            temperature = Temperature(
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
private fun getAlertList(alertList: List<BrightSkyAlert>?, languageCode: String): List<Alert>? {
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

fun convertSecondary(
    currentWeather: BrightSkyCurrentWeatherResult,
    alertsResult: BrightSkyAlertsResult,
    languageCode: String,
): SecondaryWeatherWrapper {
    return SecondaryWeatherWrapper(
        current = getCurrent(currentWeather.weather),
        alertList = getAlertList(alertsResult.alerts, languageCode)
    )
}
