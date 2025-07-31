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

import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import org.breezyweather.common.extensions.getIsoFormattedDate
import org.breezyweather.common.extensions.minus
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.sources.geosphereat.json.GeoSphereAtTimeseriesResult
import org.breezyweather.sources.geosphereat.json.GeoSphereAtWarningsResult
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.seconds

/**
 * Returns daily forecast from hourly forecast
 */
internal fun getDailyForecast(
    hourlyResult: GeoSphereAtTimeseriesResult,
    location: Location,
): List<DailyWrapper> {
    if (hourlyResult.timestamps.isNullOrEmpty()) return emptyList()
    val dayList = hourlyResult.timestamps.map {
        it.getIsoFormattedDate(location)
    }.distinct()

    val dailyList = mutableListOf<DailyWrapper>()
    for (i in 0 until dayList.size - 1) {
        val dayDate = dayList[i].toDateNoHour(location.javaTimeZone)
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
    hourlyResult: GeoSphereAtTimeseriesResult,
    airQualityResult: GeoSphereAtTimeseriesResult,
): List<HourlyWrapper> {
    if (hourlyResult.timestamps.isNullOrEmpty() ||
        hourlyResult.features?.getOrNull(0)?.properties?.parameters == null
    ) {
        return emptyList()
    }
    return hourlyResult.timestamps.mapIndexed { i, date ->
        // Wind
        val windU = hourlyResult.features[0].properties!!.parameters!!.u10m?.data?.getOrNull(i)
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
        } else {
            null
        }
        val windSpeed = if (windU != null && windV != null) {
            sqrt(windU.pow(2) + windV.pow(2))
        } else {
            null
        }
        val windGustSpeed = if (windGustU != null && windGustV != null) {
            sqrt(windGustU.pow(2) + windGustV.pow(2))
        } else {
            null
        }

        HourlyWrapper(
            date = date,
            weatherCode = getWeatherCode(hourlyResult.features[0].properties!!.parameters!!.sy?.data?.getOrNull(i)),
            temperature = TemperatureWrapper(
                temperature = hourlyResult.features[0].properties!!.parameters!!.t2m?.data?.getOrNull(i)
            ),
            precipitation = Precipitation(
                total = hourlyResult.features[0].properties!!.parameters!!.rrAcc?.data?.getOrNull(i)
                    .minus(hourlyResult.features[0].properties!!.parameters!!.rrAcc?.data?.getOrNull(i - 1)),
                rain = hourlyResult.features[0].properties!!.parameters!!.rainAcc?.data?.getOrNull(i)
                    .minus(hourlyResult.features[0].properties!!.parameters!!.rainAcc?.data?.getOrNull(i - 1)),
                snow = hourlyResult.features[0].properties!!.parameters!!.snowAcc?.data?.getOrNull(i)
                    .minus(hourlyResult.features[0].properties!!.parameters!!.snowAcc?.data?.getOrNull(i - 1))
            ),
            wind = if (windSpeed != null) {
                Wind(
                    speed = windSpeed,
                    degree = windDegree,
                    gusts = windGustSpeed
                )
            } else {
                null
            },
            relativeHumidity = hourlyResult.features[0].properties!!.parameters!!.rh2m?.data?.getOrNull(i),
            pressure = hourlyResult.features[0].properties!!.parameters!!.sp?.data?.getOrNull(i)?.div(100),
            cloudCover = hourlyResult.features[0].properties!!.parameters!!.tcc?.data?.getOrNull(i)?.times(100)
                ?.roundToInt()
        )
    }
}

internal fun getMinutelyForecast(
    nowcastResult: GeoSphereAtTimeseriesResult,
): List<Minutely>? {
    if (nowcastResult.timestamps.isNullOrEmpty() ||
        nowcastResult.features?.getOrNull(0)?.properties?.parameters?.rr?.data == null
    ) {
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
            precipitationIntensity = nowcastResult.features[0].properties!!.parameters!!.rr!!.data!!.getOrNull(i)
                ?.times(4)
        )
    }
}

internal fun getAlerts(alertsResult: GeoSphereAtWarningsResult): List<Alert>? {
    if (alertsResult.properties?.warnings == null) return null
    return alertsResult.properties.warnings.map { result ->
        Alert(
            alertId = result.properties.warnid.toString(),
            startDate = result.properties.rawinfo?.start?.toLongOrNull()?.seconds?.inWholeMilliseconds?.toDate(),
            endDate = result.properties.rawinfo?.end?.toLongOrNull()?.seconds?.inWholeMilliseconds?.toDate(),
            headline = result.properties.text,
            description = if (!result.properties.meteotext.isNullOrEmpty() ||
                !result.properties.consequences.isNullOrEmpty()
            ) {
                "${result.properties.meteotext.let {
                    if (!it.isNullOrEmpty()) it + "\n\n" else ""
                }}${result.properties.consequences ?: ""}"
            } else {
                null
            },
            instruction = result.properties.instructions,
            source = "GeoSphere Austria",
            severity = when (result.properties.rawinfo?.wlevel) {
                3 -> AlertSeverity.EXTREME
                2 -> AlertSeverity.SEVERE
                1 -> AlertSeverity.MODERATE
                else -> AlertSeverity.UNKNOWN
            },
            color = when (result.properties.rawinfo?.wlevel) {
                3 -> Color.rgb(255, 0, 0)
                2 -> Color.rgb(255, 196, 0)
                1 -> Color.rgb(255, 255, 0)
                else -> Alert.colorFromSeverity(AlertSeverity.UNKNOWN)
            }
        )
    }
}

/**
 * From https://github.com/breezy-weather/breezy-weather/issues/763#issuecomment-2044440950
 */
private fun getWeatherCode(icon: Double?): WeatherCode? {
    if (icon == null) return null
    return when (icon.roundToInt()) {
        1, 2 -> WeatherCode.CLEAR
        3 -> WeatherCode.PARTLY_CLOUDY
        4, 5 -> WeatherCode.CLOUDY
        6, 7 -> WeatherCode.FOG
        8, 9, 10, 17, 18, 19 -> WeatherCode.RAIN
        11, 12, 13, 20, 21, 22 -> WeatherCode.SLEET
        14, 15, 16, 23, 24, 25 -> WeatherCode.SNOW
        26, 27, 28, 29, 30, 31, 32 -> WeatherCode.THUNDERSTORM
        else -> null
    }
}
