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

package org.breezyweather.sources.pirateweather

import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.AlertSeverity
import breezyweather.domain.weather.model.HalfDay
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import org.breezyweather.common.extensions.toDate
import org.breezyweather.sources.pirateweather.json.PirateWeatherAlert
import org.breezyweather.sources.pirateweather.json.PirateWeatherCurrently
import org.breezyweather.sources.pirateweather.json.PirateWeatherDaily
import org.breezyweather.sources.pirateweather.json.PirateWeatherHourly
import org.breezyweather.sources.pirateweather.json.PirateWeatherMinutely
import java.util.Objects
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

/**
 * Returns current weather
 */
internal fun getCurrent(
    result: PirateWeatherCurrently?,
    dailySummary: String?,
    hourlySummary: String?,
): CurrentWrapper? {
    if (result == null) return null
    return CurrentWrapper(
        weatherText = result.summary,
        weatherCode = getWeatherCode(result.icon),
        temperature = TemperatureWrapper(
            temperature = result.temperature,
            feelsLike = result.apparentTemperature
        ),
        wind = Wind(
            degree = result.windBearing,
            speed = result.windSpeed,
            gusts = result.windGust
        ),
        uV = UV(index = result.uvIndex),
        relativeHumidity = result.humidity?.times(100),
        dewPoint = result.dewPoint,
        pressure = result.pressure,
        cloudCover = result.cloudCover?.times(100)?.roundToInt(),
        visibility = result.visibility?.times(1000),
        dailyForecast = dailySummary,
        hourlyForecast = hourlySummary
    )
}

internal fun getDailyForecast(
    dailyResult: List<PirateWeatherDaily>?,
): List<DailyWrapper>? {
    return dailyResult?.map { result ->
        DailyWrapper(
            date = result.time.seconds.inWholeMilliseconds.toDate(),
            day = HalfDayWrapper(
                weatherPhase = result.summary,
                weatherCode = getWeatherCode(result.icon),
                temperature = TemperatureWrapper(
                    temperature = result.temperatureHigh,
                    feelsLike = result.apparentTemperatureHigh
                )
            ),
            night = HalfDayWrapper(
                weatherPhase = result.summary,
                weatherCode = getWeatherCode(result.icon),
                // temperatureLow/High are always forward-looking
                // See https://docs.pirateweather.net/en/latest/API/#temperaturelow
                temperature = TemperatureWrapper(
                    temperature = result.temperatureLow,
                    feelsLike = result.apparentTemperatureLow
                )
            ),
            uV = UV(index = result.uvIndex)
        )
    }
}

/**
 * Returns hourly forecast
 */
internal fun getHourlyForecast(
    hourlyResult: List<PirateWeatherHourly>?,
): List<HourlyWrapper>? {
    return hourlyResult?.map { result ->
        HourlyWrapper(
            date = result.time.seconds.inWholeMilliseconds.toDate(),
            weatherText = result.summary,
            weatherCode = getWeatherCode(result.icon),
            temperature = TemperatureWrapper(
                temperature = result.temperature,
                feelsLike = result.apparentTemperature
            ),
            // see https://docs.pirateweather.net/en/latest/API/#precipaccumulation
            precipitation = Precipitation(
                total = result.precipAccumulation?.times(10),
                rain = result.liquidAccumulation?.times(10),
                snow = result.snowAccumulation?.times(10),
                ice = result.iceAccumulation?.times(10)
            ),
            precipitationProbability = PrecipitationProbability(
                total = result.precipProbability?.times(100)
            ),
            wind = Wind(
                degree = result.windBearing,
                speed = result.windSpeed,
                gusts = result.windGust
            ),
            uV = UV(
                index = result.uvIndex
            ),
            relativeHumidity = result.humidity?.times(100),
            dewPoint = result.dewPoint,
            pressure = result.pressure,
            cloudCover = result.cloudCover?.times(100)?.roundToInt(),
            visibility = result.visibility?.times(1000)
        )
    }
}

/**
 * Returns minutely forecast
 * Copied from openweather implementation
 */
internal fun getMinutelyForecast(minutelyResult: List<PirateWeatherMinutely>?): List<Minutely>? {
    if (minutelyResult.isNullOrEmpty()) return null
    val minutelyList = mutableListOf<Minutely>()
    minutelyResult.forEachIndexed { i, minutelyForecast ->
        minutelyList.add(
            Minutely(
                date = minutelyForecast.time.seconds.inWholeMilliseconds.toDate(),
                minuteInterval = if (i < minutelyResult.size - 1) {
                    ((minutelyResult[i + 1].time - minutelyForecast.time) / 60).toDouble().roundToInt()
                } else {
                    ((minutelyForecast.time - minutelyResult[i - 1].time) / 60).toDouble().roundToInt()
                },
                precipitationIntensity = minutelyForecast.precipIntensity
            )
        )
    }
    return minutelyList
}

/**
 * Returns alerts
 */
internal fun getAlertList(alertList: List<PirateWeatherAlert>?): List<Alert>? {
    if (alertList.isNullOrEmpty()) return null
    return alertList.map { alert ->
        val severity = when (alert.severity?.lowercase()) {
            "extreme" -> AlertSeverity.EXTREME
            "severe" -> AlertSeverity.SEVERE
            "moderate" -> AlertSeverity.MODERATE
            "minor" -> AlertSeverity.MINOR
            else -> AlertSeverity.UNKNOWN
        }
        Alert(
            // Create unique ID from: title, severity, start time
            alertId = Objects.hash(alert.title, alert.severity, alert.start).toString(),
            startDate = alert.start.seconds.inWholeMilliseconds.toDate(),
            endDate = alert.end.seconds.inWholeMilliseconds.toDate(),
            headline = alert.title,
            description = alert.description,
            source = alert.uri,
            severity = severity,
            color = Alert.colorFromSeverity(severity)
        )
    }
}

private fun getWeatherCode(icon: String?): WeatherCode? {
    return when (icon) {
        "rain" -> WeatherCode.RAIN
        "sleet" -> WeatherCode.SLEET
        "snow" -> WeatherCode.SNOW
        "fog" -> WeatherCode.FOG
        "wind" -> WeatherCode.WIND
        "clear-day", "clear-night" -> WeatherCode.CLEAR
        "partly-cloudy-day", "partly-cloudy-night" -> WeatherCode.PARTLY_CLOUDY
        "cloudy" -> WeatherCode.CLOUDY
        else -> null
    }
}
