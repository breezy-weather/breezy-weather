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

package org.breezyweather.sources.bmd

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import org.breezyweather.R
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.sources.bmd.json.BmdData
import org.breezyweather.sources.bmd.json.BmdForecastResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal fun convert(
    context: Context,
    location: Location,
    data: BmdData,
): Location {
    return location.copy(
        latitude = location.latitude,
        longitude = location.longitude,
        timeZone = "Asia/Dhaka",
        country = Locale(context.currentLocale.code, "BD").displayCountry,
        countryCode = "BD",
        admin1 = data.divisionName,
        admin2 = data.districtName,
        city = data.upazilaName ?: ""
    )
}

internal fun getDailyForecast(
    context: Context,
    upazila: String,
    dailyResult: BmdForecastResult,
): List<DailyWrapper> {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Asia/Dhaka")
    val dailyList = mutableListOf<DailyWrapper>()
    val rfMap = mutableMapOf<String, Double?>()
    val maxTMap = mutableMapOf<String, Double?>()
    val minTMap = mutableMapOf<String, Double?>()
    val wsMap = mutableMapOf<String, Double?>()
    val wdMap = mutableMapOf<String, Double?>()
    val wgMap = mutableMapOf<String, Double?>()
    val ccDayMap = mutableMapOf<String, Int?>()
    val ccNightMap = mutableMapOf<String, Int?>()
    dailyResult.data?.getOrElse(upazila) { null }?.forecastData?.let { forecast ->
        forecast.rf?.forEach {
            rfMap[it.stepStart] = it.valMax
        }
        forecast.temp?.forEach {
            maxTMap[it.stepStart] = it.valMax
            minTMap[it.stepStart] = it.valMin
        }
        forecast.windspd?.forEach {
            wsMap[it.stepStart] = it.valAvg?.div(3.6)
        }
        forecast.winddir?.forEach {
            wdMap[it.stepStart] = getCorrectWindDirection(
                min = it.valMin,
                avg = it.valAvg,
                max = it.valMax
            )
        }
        forecast.windgust?.forEach {
            wgMap[it.stepStart] = it.valAvg?.div(3.6)
        }
        forecast.cldcvr?.forEach {
            ccDayMap[it.stepStart] = it.valAvgDay?.times(12.5)?.toInt()
            ccNightMap[it.stepStart] = it.valAvgNight?.times(12.5)?.toInt()
        }
    }

    var date: Date
    rfMap.keys.sorted().forEach { key ->
        date = formatter.parse(key)!!
        dailyList.add(
            DailyWrapper(
                date = date,
                day = HalfDayWrapper(
                    weatherText = getWeatherText(
                        context = context,
                        cloudCover = ccDayMap.getOrElse(key) { null },
                        rainfall = rfMap.getOrElse(key) { null }
                    ),
                    weatherCode = getWeatherCode(
                        cloudCover = ccDayMap.getOrElse(key) { null },
                        rainfall = rfMap.getOrElse(key) { null }
                    ),
                    temperature = TemperatureWrapper(
                        temperature = maxTMap.getOrElse(key) { null }
                    ),
                    wind = Wind(
                        degree = wdMap.getOrElse(key) { null },
                        speed = wsMap.getOrElse(key) { null },
                        gusts = wgMap.getOrElse(key) { null }
                    ),
                    precipitation = Precipitation(
                        total = rfMap.getOrElse(key) { null }
                    )
                ),
                night = HalfDayWrapper(
                    weatherText = getWeatherText(
                        context = context,
                        cloudCover = ccNightMap.getOrElse(key) { null },
                        rainfall = rfMap.getOrElse(key) { null }
                    ),
                    weatherCode = getWeatherCode(
                        cloudCover = ccNightMap.getOrElse(key) { null },
                        rainfall = rfMap.getOrElse(key) { null }
                    ),
                    temperature = TemperatureWrapper(
                        temperature = minTMap.getOrElse(key) { null }
                    ),
                    wind = Wind(
                        degree = wdMap.getOrElse(key) { null },
                        speed = wsMap.getOrElse(key) { null },
                        gusts = wgMap.getOrElse(key) { null }
                    )
                )
            )
        )
    }
    return dailyList
}

internal fun getHourlyForecast(
    context: Context,
    upazila: String,
    hourlyResult: BmdForecastResult,
): List<HourlyWrapper> {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T00:'HH:mm", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Asia/Dhaka")
    val hourlyList = mutableListOf<HourlyWrapper>()
    val rfMap = mutableMapOf<String, Double?>()
    val tMap = mutableMapOf<String, Double?>()
    val rhMap = mutableMapOf<String, Double?>()
    val wsMap = mutableMapOf<String, Double?>()
    val wdMap = mutableMapOf<String, Double?>()
    val wgMap = mutableMapOf<String, Double?>()
    val ccMap = mutableMapOf<String, Int?>()

    hourlyResult.data?.getOrElse(upazila) { null }?.forecastData?.let { forecast ->
        forecast.rf?.forEach {
            rfMap[it.stepStart] = it.valMax
        }
        forecast.temp?.forEach {
            tMap[it.stepStart] = it.valAvg
        }
        forecast.rh?.forEach {
            rhMap[it.stepStart] = it.valAvg
        }
        forecast.windspd?.forEach {
            wsMap[it.stepStart] = it.valAvg?.div(3.6)
        }
        forecast.winddir?.forEach {
            wdMap[it.stepStart] = getCorrectWindDirection(
                min = it.valMin,
                avg = it.valAvg,
                max = it.valMax
            )
        }
        forecast.windgust?.forEach {
            wgMap[it.stepStart] = it.valAvg?.div(3.6)
        }
        forecast.cldcvr?.forEach {
            ccMap[it.stepStart] = it.valAvg?.times(12.5)?.toInt()
        }
    }

    var date: Date
    rfMap.keys.sorted().forEach { key ->
        date = formatter.parse(key)!!
        hourlyList.add(
            HourlyWrapper(
                date = date,
                weatherText = getWeatherText(
                    context = context,
                    cloudCover = ccMap.getOrElse(key) { null },
                    rainfall = rfMap.getOrElse(key) { null }
                ),
                weatherCode = getWeatherCode(
                    cloudCover = ccMap.getOrElse(key) { null },
                    rainfall = rfMap.getOrElse(key) { null }
                ),
                temperature = TemperatureWrapper(
                    temperature = tMap.getOrElse(key) { null }
                ),
                precipitation = Precipitation(
                    total = rfMap.getOrElse(key) { null }
                ),
                wind = Wind(
                    degree = wdMap.getOrElse(key) { null },
                    speed = wsMap.getOrElse(key) { null },
                    gusts = wgMap.getOrElse(key) { null }
                ),
                relativeHumidity = rhMap.getOrElse(key) { null },
                cloudCover = ccMap.getOrElse(key) { null }
            )
        )
    }
    return hourlyList
}

// The "average" wind direction may incorrectly point southward,
// if the "minimum" direction is in the NE and the "maximum" direction is in the NW.
// This function flips the wind direction back.
private fun getCorrectWindDirection(
    min: Double?,
    avg: Double?,
    max: Double?,
): Double? {
    max?.let {
        min?.let {
            if ((max - min) > 180.0) {
                return avg?.plus(180.0)?.mod(360.0)
            }
        }
    }
    return avg
}

// Using the same algorithm as https://bmd.bdservers.site/src/js/dashboard.js
// These functions are needed because when Hourly only has 4 days, yet Daily has 10 days,
// weather conditions are not automatically populated for the last 6 days in the Daily chart.
private fun getWeatherText(
    context: Context,
    cloudCover: Int?,
    rainfall: Double?,
): String? {
    return cloudCover?.let {
        when {
            cloudCover <= 12 -> context.getString(R.string.common_weather_text_clear_sky)
            cloudCover <= 37 -> context.getString(R.string.common_weather_text_mostly_clear)
            (rainfall == null || rainfall < 1.0) -> when {
                cloudCover <= 62 -> context.getString(R.string.common_weather_text_partly_cloudy)
                cloudCover < 100 -> context.getString(R.string.common_weather_text_cloudy)
                cloudCover == 100 -> context.getString(R.string.common_weather_text_overcast)
                else -> null
            }
            rainfall <= 22.0 -> context.getString(R.string.common_weather_text_rain_light)
            rainfall <= 43.0 -> context.getString(R.string.common_weather_text_rain_moderate)
            else -> context.getString(R.string.common_weather_text_rain_heavy)
        }
    }
}

private fun getWeatherCode(
    cloudCover: Int?,
    rainfall: Double?,
): WeatherCode? {
    return cloudCover?.let {
        when {
            cloudCover <= 37 -> WeatherCode.CLEAR
            (rainfall == null || rainfall < 1.0) -> when {
                cloudCover <= 62 -> WeatherCode.PARTLY_CLOUDY
                cloudCover <= 100 -> WeatherCode.CLOUDY
                else -> null
            }
            else -> WeatherCode.RAIN
        }
    }
}
