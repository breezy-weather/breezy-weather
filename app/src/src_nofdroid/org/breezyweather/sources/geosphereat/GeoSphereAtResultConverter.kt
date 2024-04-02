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

package org.breezyweather.sources.geosphereat

import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.sources.geosphereat.json.GeoSphereAtTimeseriesResult
import java.util.Date
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Converts DMI result into a forecast
 */
fun convert(
    hourlyResult: GeoSphereAtTimeseriesResult,
    airQualityResult: GeoSphereAtTimeseriesResult,
    nowcastResult: GeoSphereAtTimeseriesResult,
    location: Location
): WeatherWrapper {
    // If the API doesn’t return timeseries, consider data as garbage and keep cached data
    if (hourlyResult.timestamps.isNullOrEmpty() || hourlyResult.features?.getOrNull(0)?.properties?.parameters == null) {
        throw InvalidOrIncompleteDataException()
    }

    return WeatherWrapper(
        dailyForecast = getDailyForecast(hourlyResult, location),
        hourlyForecast = getHourlyForecast(hourlyResult, airQualityResult),
        minutelyForecast = getMinutelyForecast(nowcastResult)
    )
}

/**
 * Returns daily forecast from hourly forecast
 */
private fun getDailyForecast(
    hourlyResult: GeoSphereAtTimeseriesResult,
    location: Location
): List<Daily> {
    val dayList = hourlyResult.timestamps!!.map {
        it.getFormattedDate("yyyy-MM-dd", location)
    }.distinct()

    val dailyList = mutableListOf<Daily>()
    for (i in 0 until dayList.size - 1) {
        val dayDate = dayList[i].toDateNoHour(location.javaTimeZone)
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
    hourlyResult: GeoSphereAtTimeseriesResult,
    airQualityResult: GeoSphereAtTimeseriesResult
): List<HourlyWrapper> {
    return hourlyResult.timestamps!!.mapIndexed { i, date ->
        // Wind
        val windU = hourlyResult.features!![0].properties!!.parameters!!.u10m?.data?.getOrNull(i)
        val windV = hourlyResult.features[0].properties!!.parameters!!.v10m?.data?.getOrNull(i)
        val windGustU = hourlyResult.features[0].properties!!.parameters!!.ugust?.data?.getOrNull(i)
        val windGustV = hourlyResult.features[0].properties!!.parameters!!.vgust?.data?.getOrNull(i)
        val windDegree = if (windU != null && windV != null) {
            if (windU == 0.0) {
                -1.0
            } else {
                // I have absolutely no idea what I'm doing
                // https://confluence.ecmwf.int/pages/viewpage.action?pageId=133262398
                (180 + (180 / Math.PI) * atan2(windU, windV)).mod(360.0)
            }
        } else null
        val windSpeed = if (windU != null && windV != null) {
            sqrt(windU.pow(2) + windV.pow(2))
        } else null
        val windGustSpeed = if (windGustU != null && windGustV != null) {
            sqrt(windGustU.pow(2) + windGustV.pow(2))
        } else null

        // Air quality
        val airQualityIndex = airQualityResult.timestamps?.indexOfFirst { it == date }

        HourlyWrapper(
            date = date,
            //weatherCode = getWeatherCode(hourlyResult.features!![0].properties!!.parameters!!.sy?.data?.getOrNull(i)), // TODO
            temperature = Temperature(
                temperature = hourlyResult.features[0].properties!!.parameters!!.t2m?.data?.getOrNull(i)
            ),
            precipitation = Precipitation(
                total = hourlyResult.features[0].properties!!.parameters!!.rrAcc?.data?.getOrNull(i),
                rain = hourlyResult.features[0].properties!!.parameters!!.rainAcc?.data?.getOrNull(i),
                snow = hourlyResult.features[0].properties!!.parameters!!.snowAcc?.data?.getOrNull(i)
            ),
            wind = if (windSpeed != null) {
                Wind(
                    speed = windSpeed,
                    degree = windDegree,
                    gusts = windGustSpeed
                )
            } else null,
            airQuality = if (airQualityIndex != null &&
                airQualityResult.features?.getOrNull(0)?.properties?.parameters != null) {
                AirQuality(
                    pM25 = airQualityResult.features[0].properties!!.parameters!!.pm25surf?.data?.getOrNull(airQualityIndex),
                    pM10 = airQualityResult.features[0].properties!!.parameters!!.pm10surf?.data?.getOrNull(airQualityIndex),
                    nO2 = airQualityResult.features[0].properties!!.parameters!!.no2surf?.data?.getOrNull(airQualityIndex),
                    o3 = airQualityResult.features[0].properties!!.parameters!!.o3surf?.data?.getOrNull(airQualityIndex)
                )
            } else null,
            relativeHumidity = hourlyResult.features[0].properties!!.parameters!!.rh2m?.data?.getOrNull(i),
            pressure = hourlyResult.features[0].properties!!.parameters!!.sp?.data?.getOrNull(i)?.div(100),
            cloudCover = hourlyResult.features[0].properties!!.parameters!!.tcc?.data?.getOrNull(i)?.times(100)?.roundToInt()
        )
    }
}

private fun getMinutelyForecast(
    nowcastResult: GeoSphereAtTimeseriesResult
): List<Minutely>? {
    if (nowcastResult.timestamps.isNullOrEmpty()
        || nowcastResult.features?.getOrNull(0)?.properties?.parameters?.rr?.data == null) {
        return null
    }

    return nowcastResult.timestamps.mapIndexed { i, date ->
        Minutely(
            date = date,
            minuteInterval = 15,
            /**
             * If I understand correctly, the unit is kg/m², which is approximately the same as mm
             * However, since it's 15 min by 15 min, and we want mm/h unit, we just have to multiply
             * by 4, right?
             */
            precipitationIntensity = nowcastResult.features[0].properties!!.parameters!!.rr!!.data!!.getOrNull(i)?.times(4)
        )
    }
}

fun convertSecondary(
    airQualityResult: GeoSphereAtTimeseriesResult,
    nowcastResult: GeoSphereAtTimeseriesResult
): SecondaryWeatherWrapper {
    val airQualityHourly = mutableMapOf<Date, AirQuality>()

    if (!airQualityResult.timestamps.isNullOrEmpty()
        && airQualityResult.features?.getOrNull(0)?.properties?.parameters != null) {
        airQualityResult.timestamps.forEachIndexed { i, date ->
            airQualityHourly[date] = AirQuality(
                pM25 = airQualityResult.features[0].properties!!.parameters!!.pm25surf?.data?.getOrNull(i),
                pM10 = airQualityResult.features[0].properties!!.parameters!!.pm10surf?.data?.getOrNull(i),
                nO2 = airQualityResult.features[0].properties!!.parameters!!.no2surf?.data?.getOrNull(i),
                o3 = airQualityResult.features[0].properties!!.parameters!!.o3surf?.data?.getOrNull(i)
            )
        }
    }

    return SecondaryWeatherWrapper(
        airQuality = if (airQualityHourly.isNotEmpty()) {
            AirQualityWrapper(hourlyForecast = airQualityHourly)
        } else null,
        minutelyForecast = getMinutelyForecast(nowcastResult)
    )
}

/**
 * TODO
 */
private fun getWeatherCode(icon: Double?): WeatherCode? {
    if (icon == null) return null
    return when (icon) {
        1.0 -> WeatherCode.CLEAR
        else -> null
    }
}
