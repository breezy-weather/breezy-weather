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

package org.breezyweather.sources.smhi

import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.sources.smhi.json.SmhiTimeSeries
import kotlin.math.roundToInt

internal fun getDailyForecast(
    location: Location,
    forecastResult: List<SmhiTimeSeries>,
): List<DailyWrapper> {
    val dailyList = mutableListOf<DailyWrapper>()
    val hourlyListByDay = forecastResult.groupBy {
        it.validTime.getFormattedDate("yyyy-MM-dd", location)
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
    forecastResult: List<SmhiTimeSeries>,
): List<HourlyWrapper> {
    return forecastResult.map { result ->
        HourlyWrapper(
            date = result.validTime,
            weatherCode = getWeatherCode(result.parameters.firstOrNull { it.name == "Wsymb2" }?.values?.getOrNull(0)),
            temperature = Temperature(
                temperature = result.parameters.firstOrNull { it.name == "t" }?.values?.getOrNull(0)
            ),
            precipitation = Precipitation(
                total = result.parameters.firstOrNull { it.name == "pmean" }?.values?.getOrNull(0)
            ),
            precipitationProbability = PrecipitationProbability(
                thunderstorm = result.parameters.firstOrNull { it.name == "tstm" }?.values?.getOrNull(0)
            ),
            wind = Wind(
                degree = result.parameters.firstOrNull { it.name == "wd" }?.values?.getOrNull(0),
                speed = result.parameters.firstOrNull { it.name == "ws" }?.values?.getOrNull(0),
                gusts = result.parameters.firstOrNull { it.name == "gust" }?.values?.getOrNull(0)
            ),
            relativeHumidity = result.parameters.firstOrNull { it.name == "r" }?.values?.getOrNull(0),
            pressure = result.parameters.firstOrNull { it.name == "msl" }?.values?.getOrNull(0),
            visibility = result.parameters.firstOrNull { it.name == "vis" }?.values?.getOrNull(0)?.times(1000)
        )
    }
}

private fun getWeatherCode(icon: Double?): WeatherCode? {
    if (icon == null) return null
    return when (icon.roundToInt()) {
        1, 2 -> WeatherCode.CLEAR
        3, 4 -> WeatherCode.PARTLY_CLOUDY
        5, 6 -> WeatherCode.CLOUDY
        7 -> WeatherCode.FOG
        8, 9, 10, 18, 19, 20 -> WeatherCode.RAIN
        12, 13, 14, 22, 23, 24 -> WeatherCode.SLEET
        15, 16, 17, 25, 26, 27 -> WeatherCode.SNOW
        11 -> WeatherCode.THUNDERSTORM
        21 -> WeatherCode.THUNDER
        else -> null
    }
}
