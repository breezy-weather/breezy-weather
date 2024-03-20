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
import breezyweather.domain.weather.model.Astro
import breezyweather.domain.weather.model.Current
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.HalfDay
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.MoonPhase
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.WeatherException
import org.breezyweather.common.extensions.toDate
import org.breezyweather.sources.pirateweather.json.PirateWeatherAlert
import org.breezyweather.sources.pirateweather.json.PirateWeatherCurrently
import org.breezyweather.sources.pirateweather.json.PirateWeatherDaily
import org.breezyweather.sources.pirateweather.json.PirateWeatherForecastResult
import org.breezyweather.sources.pirateweather.json.PirateWeatherHourly
import org.breezyweather.sources.pirateweather.json.PirateWeatherMinutely
import java.util.Date
import java.util.Objects
import kotlin.math.roundToInt


/**
 * Converts PirateWeather result into a forecast
 */
fun convert(
    forecastResult: PirateWeatherForecastResult
): WeatherWrapper {
    // If the API doesn’t return hourly or daily, consider data as garbage and keep cached data
    if (forecastResult.daily?.data.isNullOrEmpty() || forecastResult.hourly?.data.isNullOrEmpty()) {
        throw WeatherException()
    }

    return WeatherWrapper(
        /*base = Base(
            publishDate = forecastResult.currently?.time?.times(1000)?.toDate() ?: Date()
        ),*/
        current = getCurrent(forecastResult.currently),
        dailyForecast = getDailyForecast(forecastResult.daily!!.data!!),
        hourlyForecast = getHourlyForecast(forecastResult.hourly!!.data!!),
        minutelyForecast = getMinutelyForecast(forecastResult.minutely?.data),
        alertList = getAlertList(forecastResult.alerts)
    )
}

/**
 * Returns current weather
 */
private fun getCurrent(result: PirateWeatherCurrently?): Current? {
    if (result == null) return null
    return Current(
        weatherText = result.summary,
        weatherCode = getWeatherCode(result.icon),
        temperature = Temperature(
            temperature = result.temperature,
            apparentTemperature = result.apparentTemperature,
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
        visibility = result.visibility?.times(1000)
    )
}

private fun getDailyForecast(
    dailyResult: List<PirateWeatherDaily>
): List<Daily> {
    return dailyResult.map { result ->
        Daily(
            date = Date(result.time.times(1000)),
            day = HalfDay(
                weatherText = result.summary,
                weatherCode = getWeatherCode(result.icon),
                temperature = Temperature(
                    temperature = result.temperatureHigh,
                    apparentTemperature = result.apparentTemperatureHigh,
                )
            ),
            night = HalfDay(
                weatherText = result.summary,
                weatherCode = getWeatherCode(result.icon),
                // temperatureLow/High are always forward-looking
                // See https://docs.pirateweather.net/en/latest/API/#temperaturelow
                temperature = Temperature(
                    temperature = result.temperatureLow,
                    apparentTemperature = result.apparentTemperatureLow,
                ),
            ),
            sun = Astro(
                riseDate = result.sunrise?.times(1000)?.toDate(),
                setDate = result.sunset?.times(1000)?.toDate(),
            ),
            moon = Astro(),
            moonPhase = MoonPhase(
                angle = result.moonPhase?.times(360)?.roundToInt(), // Seems correct
            ),
            uV = UV(index = result.uvIndex)
        )
    }
}

/**
 * Returns hourly forecast
 */
private fun getHourlyForecast(
    hourlyResult: List<PirateWeatherHourly>
): List<HourlyWrapper> {
    return hourlyResult.map { result ->
        HourlyWrapper(
            date = Date(result.time.times(1000)),
            weatherText = result.summary,
            weatherCode = getWeatherCode(result.icon),
            temperature = Temperature(
                temperature = result.temperature,
                apparentTemperature = result.apparentTemperature,
            ),
            // see https://docs.pirateweather.net/en/latest/API/#preciptype
            precipitation = Precipitation(
                total = result.precipAccumulation,
                rain = if (result.precipType.equals("rain")) result.precipIntensity else null,
                snow = if (result.precipType.equals("snow")) result.precipIntensity else null,
            ),
            precipitationProbability = PrecipitationProbability(
                total = result.precipProbability?.times(100),
            ),
            wind = Wind(
                degree = result.windBearing,
                speed = result.windSpeed,
                gusts = result.windGust
            ),
            uV = UV(
                index = result.uvIndex,
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
private fun getMinutelyForecast(minutelyResult: List<PirateWeatherMinutely>?): List<Minutely>? {
    if (minutelyResult.isNullOrEmpty()) return null
    val minutelyList: MutableList<Minutely> = arrayListOf()
    minutelyResult.forEachIndexed { i, minutelyForecast ->
        minutelyList.add(
            Minutely(
                date = Date(minutelyForecast.time * 1000),
                minuteInterval = if (i < minutelyResult.size - 1) {
                    ((minutelyResult[i + 1].time - minutelyForecast.time) / 60).toDouble()
                        .roundToInt()
                } else ((minutelyForecast.time - minutelyResult[i - 1].time) / 60).toDouble()
                    .roundToInt(),
                precipitationIntensity = minutelyForecast.precipIntensity
            )
        )
    }
    return minutelyList
}

/**
 * Returns alerts
 */
private fun getAlertList(alertList: List<PirateWeatherAlert>?): List<Alert>? {
    if (alertList.isNullOrEmpty()) return null
    return alertList.map { alert ->
        Alert(
            // Create unique ID from: title, severity, start time
            alertId = Objects.hash(alert.title, alert.severity, alert.start).toString(),
            startDate = Date(alert.start.times(1000)),
            endDate = Date(alert.end.times(1000)),
            headline = alert.title,
            description = alert.description,
            severity = when (alert.severity?.lowercase()) {
                "extreme" -> AlertSeverity.EXTREME
                "severe" -> AlertSeverity.SEVERE
                "moderate" -> AlertSeverity.MODERATE
                "minor" -> AlertSeverity.MINOR
                else -> AlertSeverity.UNKNOWN
            }
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

fun convertSecondary(
    forecastResult: PirateWeatherForecastResult
): SecondaryWeatherWrapper {

    return SecondaryWeatherWrapper(
        minutelyForecast = getMinutelyForecast(forecastResult.minutely?.data),
        alertList = getAlertList(forecastResult.alerts)
    )
}