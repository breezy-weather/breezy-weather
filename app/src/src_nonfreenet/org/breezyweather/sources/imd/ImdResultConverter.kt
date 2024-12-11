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

package org.breezyweather.sources.imd

import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.sources.imd.json.ImdWeatherResult
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.time.Duration.Companion.hours

internal fun getDailyForecast(
    location: Location,
    hourlyForecast: List<HourlyWrapper>,
): List<DailyWrapper> {
    // Need to provide an empty daily list so that
    // CommonConverter.kt will compute the daily forecast items.
    val dates = hourlyForecast.groupBy { it.date.getFormattedDate("yyyy-MM-dd", location) }.keys
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone(location.timeZone)
    val now = Calendar.getInstance(TimeZone.getTimeZone(location.timeZone))
    now.add(Calendar.DATE, -1)
    val yesterday = formatter.format(now.time)
    val dailyList = mutableListOf<DailyWrapper>()
    dates.forEachIndexed { i, day ->
        // Do not add days prior to yesterday.
        // Do not add the last day to avoid incomplete day.
        if (day >= yesterday && i < (dates.size - 1)) {
            dailyList.add(
                DailyWrapper(
                    date = formatter.parse(day)!!
                )
            )
        }
    }
    return dailyList
}

internal fun getHourlyForecast(
    forecast1hr: ImdWeatherResult,
    forecast3hr: ImdWeatherResult,
    forecast6hr: ImdWeatherResult,
    forecast1hrTimestamp: Long?,
    forecast3hrTimestamp: Long?,
    forecast6hrTimestamp: Long?,
): List<HourlyWrapper> {
    val hourlyList = mutableListOf<HourlyWrapper>()
    val apcpMap = mutableMapOf<Long, Double?>()
    val tempMap = mutableMapOf<Long, Double?>()
    val wspdMap = mutableMapOf<Long, Double?>()
    val wdirMap = mutableMapOf<Long, Double?>()
    val rhMap = mutableMapOf<Long, Double?>()
    val tcdcMap = mutableMapOf<Long, Double?>()
    val gustMap = mutableMapOf<Long, Double?>()
    var key: Long

    val forecastSets = listOf(forecast6hr, forecast3hr, forecast1hr)
    val forecastParameters = listOf(
        mapOf(
            "timestamp" to forecast6hrTimestamp,
            "interval" to 6.hours.inWholeMilliseconds,
            "size" to 40 // 10 days
        ),
        mapOf(
            "timestamp" to forecast3hrTimestamp,
            "interval" to 3.hours.inWholeMilliseconds,
            "size" to 40 // 5 days
        ),
        mapOf(
            "timestamp" to forecast1hrTimestamp,
            "interval" to 1.hours.inWholeMilliseconds,
            "size" to 36 // 1.5 days
        )
    )

    // Put data from all three forecasts into respective maps
    // first 6 hr, then 3 hr, then 1 hr
    forecastSets.forEachIndexed { set, forecast ->
        forecastParameters.getOrNull(set)?.let { parameters ->
            if (parameters["timestamp"] != null) {
                forecast.let {
                    // skip 0 as it is always "NaN"
                    for (i in 1..parameters["size"]!!.toInt()) {
                        key = parameters["timestamp"]!! + (parameters["interval"]!! * i)
                        apcpMap[key] = it.apcp?.getOrNull(i) as Double?
                        tempMap[key] = it.temp?.getOrNull(i) as Double?
                        wspdMap[key] = it.wspd?.getOrNull(i) as Double?
                        wdirMap[key] = it.wdir?.getOrNull(i) as Double?
                        rhMap[key] = it.rh?.getOrNull(i) as Double?
                        tcdcMap[key] = it.tcdc?.getOrNull(i) as Double?
                        gustMap[key] = it.gust?.getOrNull(i) as Double?
                    }
                }
            }
        }
    }

    val keys = apcpMap.keys.sorted()
    keys.forEach {
        hourlyList.add(
            HourlyWrapper(
                date = Date(it),
                temperature = Temperature(
                    temperature = tempMap[it]
                ),
                precipitation = Precipitation(
                    total = apcpMap[it]
                ),
                wind = Wind(
                    degree = wdirMap[it],
                    speed = wspdMap[it],
                    gusts = gustMap[it]
                ),
                relativeHumidity = rhMap[it],
                cloudCover = tcdcMap[it]?.toInt()
            )
        )
    }
    return hourlyList
}
