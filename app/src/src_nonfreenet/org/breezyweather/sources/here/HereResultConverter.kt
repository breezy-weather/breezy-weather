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

package org.breezyweather.sources.here

import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import org.breezyweather.common.extensions.plus
import org.breezyweather.sources.here.json.HereGeocodingData
import org.breezyweather.sources.here.json.HereWeatherData
import java.util.TimeZone

/**
 * Returns current forecast
 */
internal fun getCurrentForecast(result: HereWeatherData?): CurrentWrapper? {
    if (result == null) return null
    return CurrentWrapper(
        weatherText = result.description,
        weatherCode = getWeatherCode(result.iconId),
        temperature = TemperatureWrapper(
            temperature = result.temperature,
            feelsLike = result.comfort?.toDouble()
        ),
        wind = Wind(
            degree = result.windDirection,
            speed = result.windSpeed?.div(3.6)
        ),
        uV = UV(index = result.uvIndex?.toDouble()),
        relativeHumidity = result.humidity?.toDouble(),
        dewPoint = result.dewPoint,
        pressure = result.barometerPressure,
        visibility = result.visibility?.times(1000)
    )
}

/**
 * Returns daily forecast
 */
internal fun getDailyForecast(
    dailySimpleForecasts: List<HereWeatherData>?,
): List<DailyWrapper> {
    if (dailySimpleForecasts.isNullOrEmpty()) return emptyList()
    val dailyList: MutableList<DailyWrapper> = ArrayList(dailySimpleForecasts.size)
    for (i in 0 until dailySimpleForecasts.size - 1) { // Skip last day
        val dailyForecast = dailySimpleForecasts[i]

        dailyList.add(
            DailyWrapper(
                date = dailyForecast.time,
                day = HalfDayWrapper(
                    temperature = TemperatureWrapper(
                        temperature = if (!dailyForecast.highTemperature.isNullOrEmpty()) {
                            dailyForecast.highTemperature.toDouble()
                        } else {
                            null
                        }
                    )
                ),
                night = HalfDayWrapper(
                    // low temperature is actually from previous night,
                    // so we try to get low temp from next day if available
                    temperature = TemperatureWrapper(
                        temperature = if (!dailySimpleForecasts.getOrNull(i + 1)?.lowTemperature.isNullOrEmpty()) {
                            dailySimpleForecasts[i + 1].lowTemperature!!.toDouble()
                        } else {
                            null
                        }
                    )
                ),
                uV = UV(index = dailyForecast.uvIndex?.toDouble())
            )
        )
    }
    return dailyList
}

/**
 * Returns hourly forecast
 */
internal fun getHourlyForecast(
    hourlyResult: List<HereWeatherData>?,
): List<HourlyWrapper> {
    if (hourlyResult.isNullOrEmpty()) return emptyList()
    return hourlyResult.map { result ->
        HourlyWrapper(
            date = result.time,
            weatherText = result.description,
            weatherCode = getWeatherCode(result.iconId),
            temperature = TemperatureWrapper(
                temperature = result.temperature,
                feelsLike = result.comfort?.toDouble()
            ),
            precipitation = Precipitation(
                total = result.precipitation1H ?: (result.rainFall + result.snowFall),
                rain = result.rainFall,
                snow = result.snowFall
            ),
            precipitationProbability = PrecipitationProbability(
                total = result.precipitationProbability?.toDouble()
            ),
            wind = Wind(
                degree = result.windDirection,
                speed = result.windSpeed?.div(3.6)
            ),
            uV = UV(index = result.uvIndex?.toDouble()),
            relativeHumidity = result.humidity?.toDouble(),
            dewPoint = result.dewPoint,
            pressure = result.barometerPressure,
            visibility = result.visibility?.times(1000)
        )
    }
}

/**
 * Returns weather code based on icon id
 */
private fun getWeatherCode(icon: Int?): WeatherCode? {
    return when (icon) {
        1, 2, 13, 14 -> WeatherCode.CLEAR
        3 -> WeatherCode.HAZE
        4, 5, 15, 16 -> WeatherCode.PARTLY_CLOUDY
        6 -> WeatherCode.PARTLY_CLOUDY
        7, 17 -> WeatherCode.CLOUDY
        8, 9, 10, 12 -> WeatherCode.FOG
        11 -> WeatherCode.WIND
        18, 19, 20, 32, 33, 34 -> WeatherCode.RAIN
        21, 22, 23, 25, 26, 35 -> WeatherCode.THUNDERSTORM
        24 -> WeatherCode.HAIL
        27, 28 -> WeatherCode.SLEET
        29, 30, 31 -> WeatherCode.SNOW
        else -> null
    }
}
