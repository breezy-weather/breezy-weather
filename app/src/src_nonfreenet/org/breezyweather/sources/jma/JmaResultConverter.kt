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

package org.breezyweather.sources.jma

import android.content.Context
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.google.maps.android.data.geojson.GeoJsonFeature
import com.google.maps.android.data.geojson.GeoJsonMultiPolygon
import com.google.maps.android.data.geojson.GeoJsonParser
import com.google.maps.android.data.geojson.GeoJsonPolygon
import com.google.maps.android.model.LatLng
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.sources.jma.json.JmaAlertResult
import org.breezyweather.sources.jma.json.JmaAmedasResult
import org.breezyweather.sources.jma.json.JmaAreasResult
import org.breezyweather.sources.jma.json.JmaBulletinResult
import org.breezyweather.sources.jma.json.JmaCurrentResult
import org.breezyweather.sources.jma.json.JmaDailyResult
import org.breezyweather.sources.jma.json.JmaForecastAreaResult
import org.breezyweather.sources.jma.json.JmaHourlyResult
import org.breezyweather.sources.jma.json.JmaWeekAreaResult
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.floor
import kotlin.math.max
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

internal fun getCurrent(
    context: Context,
    currentResult: Map<String, JmaCurrentResult>,
    bulletinResult: JmaBulletinResult,
): CurrentWrapper? {
    val lastKey = currentResult.keys.sortedDescending()[0]
    val lastHourKey = lastKey.substring(0, 10) + "0000"
    var dailyForecast = bulletinResult.text?.trim()
    if ((dailyForecast ?: "").startsWith("【")) {
        dailyForecast = dailyForecast?.substringAfter("】")?.trim()
    }
    dailyForecast = dailyForecast?.substringBefore("\n")?.trim()
    return currentResult[lastKey]?.let {
        val weather = currentResult.getOrElse(lastHourKey) { null }?.weather?.getOrNull(0)
        CurrentWrapper(
            weatherText = getCurrentWeatherText(context, weather),
            weatherCode = getCurrentWeatherCode(weather),
            temperature = TemperatureWrapper(
                temperature = it.temp?.getOrNull(0)
            ),
            wind = Wind(
                degree = getWindDirection(it.windDirection?.getOrNull(0)),
                speed = it.wind?.getOrNull(0)
            ),
            relativeHumidity = it.humidity?.getOrNull(0),
            pressure = it.normalPressure?.getOrNull(0),
            visibility = it.visibility?.getOrNull(0),
            dailyForecast = dailyForecast
        )
    }
}

internal fun getDailyForecast(
    context: Context,
    dailyResult: List<JmaDailyResult>,
    class10s: String,
    weekArea05: String,
    weekAreaAmedas: String,
    forecastAmedas: String,
): List<DailyWrapper> {
    val formatter = SimpleDateFormat("HH", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Asia/Tokyo")

    val wxMap = mutableMapOf<Long, String?>()
    val maxTMap = mutableMapOf<Long, Double?>()
    val minTMap = mutableMapOf<Long, Double?>()
    val popMap = mutableMapOf<Long, Double>()

    // 7-day weather conditions and daily probabilities of precipitation
    dailyResult.getOrNull(1)?.timeSeries?.getOrNull(0)?.let { series ->
        series.areas
            ?.filter { it.area.code == weekArea05 }
            ?.forEach { area ->
                series.timeDefines?.forEachIndexed { i, timeDefines ->
                    timeDefines.time.let {
                        wxMap[it] = area.weatherCodes?.getOrNull(i)
                        val pop = area.pops?.getOrNull(i)?.toDoubleOrNull()
                        // fill up popMap from 6am local time rather than midnight,
                        // otherwise the midnight-to-6am PoP will be duplicated in daily charts
                        popMap[it + 6.hours.inWholeMilliseconds] = pop ?: 0.0
                        popMap[it + 12.hours.inWholeMilliseconds] = pop ?: 0.0
                        popMap[it + 18.hours.inWholeMilliseconds] = pop ?: 0.0
                        popMap[it + 24.hours.inWholeMilliseconds] = pop ?: 0.0
                    }
                }
            }
    }

    // 7-day max and min temperatures
    dailyResult.getOrNull(1)?.timeSeries?.getOrNull(1)?.let { series ->
        series.areas
            ?.filter { it.area.code == weekAreaAmedas }
            ?.forEach { area ->
                series.timeDefines?.forEachIndexed { i, timeDefines ->
                    timeDefines.time.let {
                        minTMap[it] = area.tempsMin?.getOrNull(i)?.toDoubleOrNull()
                        maxTMap[it] = area.tempsMax?.getOrNull(i)?.toDoubleOrNull()
                    }
                }
            }
    }

    // 3-day weather conditions
    dailyResult.getOrNull(0)?.timeSeries?.getOrNull(0)?.let { series ->
        series.areas
            ?.filter { it.area.code == class10s }
            ?.forEach { area ->
                series.timeDefines?.forEachIndexed { i, timeDefines ->
                    timeDefines.time.let {
                        // normalize timestamp to midnight local time
                        val midnight = (
                            floor((it.toDouble() + 9.hours.inWholeMilliseconds) / 1.days.inWholeMilliseconds) *
                                1.days.inWholeMilliseconds -
                                9.hours.inWholeMilliseconds
                            ).toLong()
                        wxMap[midnight] = area.weatherCodes?.getOrNull(i)
                    }
                }
            }
    }

    // 3-day 6-hourly probabilities of precipitation
    dailyResult.getOrNull(0)?.timeSeries?.getOrNull(1)?.let { series ->
        series.areas
            ?.filter { it.area.code == class10s }
            ?.forEach { area ->
                series.timeDefines?.forEachIndexed { i, timeDefines ->
                    timeDefines.time.let {
                        popMap[it] = area.pops?.getOrNull(i)?.toDoubleOrNull() ?: 0.0
                    }
                }
            }
    }

    // 3-day max and min temperatures
    val forecastAmedasCodes = forecastAmedas.split(",")
    dailyResult.getOrNull(0)?.timeSeries?.getOrNull(2)?.let { series ->
        series.areas?.forEach { area ->
            forecastAmedasCodes
                .filter { area.area.code == it }
                .forEach { code ->
                    series.timeDefines?.forEachIndexed { i, timeDefines ->
                        timeDefines.time.let {
                            val hour = formatter.format(Date(it))
                            if (hour == "00") {
                                minTMap[it] = area.temps?.getOrNull(i)?.toDoubleOrNull()
                            }
                            if (hour == "09") {
                                maxTMap[it - 9.hours.inWholeMilliseconds] = area.temps?.getOrNull(i)?.toDoubleOrNull()
                            }
                        }
                    }
                }
        }
    }

    return minTMap.keys.sorted().map { key ->
        DailyWrapper(
            date = Date(key),
            day = HalfDayWrapper(
                weatherText = getDailyWeatherText(
                    context = context,
                    weather = wxMap.getOrElse(key) { null },
                    night = false
                ),
                weatherCode = getDailyWeatherCode(
                    weather = wxMap.getOrElse(key) { null },
                    night = false
                ),
                temperature = TemperatureWrapper(
                    temperature = maxTMap.getOrElse(key) { null }
                ),
                precipitationProbability = PrecipitationProbability(
                    total = max(
                        popMap.getOrElse(key + 6.hours.inWholeMilliseconds) { 0.0 },
                        popMap.getOrElse(key + 12.hours.inWholeMilliseconds) { 0.0 }
                    )
                )
            ),
            night = HalfDayWrapper(
                weatherText = getDailyWeatherText(
                    context = context,
                    weather = wxMap.getOrElse(key) { null },
                    night = true
                ),
                weatherCode = getDailyWeatherCode(
                    weather = wxMap.getOrElse(key) { null },
                    night = true
                ),
                temperature = TemperatureWrapper(
                    temperature = minTMap.getOrElse(key + 1.days.inWholeMilliseconds) { null }
                ),
                precipitationProbability = PrecipitationProbability(
                    total = max(
                        popMap.getOrElse(key + 18.hours.inWholeMilliseconds) { 0.0 },
                        popMap.getOrElse(key + 24.hours.inWholeMilliseconds) { 0.0 }
                    )
                )
            )
        )
    }
}

internal fun getHourlyForecast(
    context: Context,
    hourlyResult: JmaHourlyResult,
): List<HourlyWrapper> {
    val wxTextMap = mutableMapOf<Long, String?>()
    val wxCodeMap = mutableMapOf<Long, WeatherCode?>()
    val tMap = mutableMapOf<Long, Double?>()
    val wdMap = mutableMapOf<Long, Double?>()
    val wsMap = mutableMapOf<Long, Double?>()
    hourlyResult.areaTimeSeries?.let { timeSeries ->
        timeSeries.timeDefines?.forEachIndexed { i, timeDefines ->
            timeDefines?.dateTime?.time?.let {
                wxTextMap[it] = getHourlyWeatherText(context, timeSeries.weather?.getOrNull(i))
                wxCodeMap[it] = getHourlyWeatherCode(timeSeries.weather?.getOrNull(i))
                wdMap[it] = getWindDirection(timeSeries.wind?.getOrNull(i)?.direction)
                wsMap[it] = timeSeries.wind?.getOrNull(i)?.range?.substringAfterLast(" ")?.toDoubleOrNull()
            }
        }
    }
    hourlyResult.pointTimeSeries?.let { timeSeries ->
        timeSeries.timeDefines?.forEachIndexed { i, timeDefines ->
            timeDefines?.dateTime?.time?.let {
                tMap[it] = timeSeries.temperature?.getOrNull(i)
            }
        }
    }

    return wxTextMap.keys.sorted().map { key ->
        HourlyWrapper(
            date = Date(key),
            weatherText = wxTextMap.getOrElse(key) { null },
            weatherCode = wxCodeMap.getOrElse(key) { null },
            temperature = TemperatureWrapper(
                temperature = tMap.getOrElse(key) { null }
            ),
            wind = Wind(
                degree = wdMap.getOrElse(key) { null },
                speed = wsMap.getOrElse(key) { null }
            )
        )
    }
}

internal fun getNormals(
    dailyResult: List<JmaDailyResult>,
    weekAreaAmedas: String,
): Normals? {
    dailyResult.getOrNull(1)?.tempAverage?.areas?.forEach { area ->
        if (area.area.code == weekAreaAmedas) {
            return Normals(
                daytimeTemperature = area.max?.toDoubleOrNull(),
                nighttimeTemperature = area.min?.toDoubleOrNull()
            )
        }
    }
    return null
}

internal fun getAlertList(
    context: Context,
    alertResult: JmaAlertResult,
    class20s: String,
): List<Alert> {
    val alertList = mutableListOf<Alert>()
    var severity: AlertSeverity
    alertResult.areaTypes?.getOrNull(1)?.areas?.forEach { area ->
        if (area.code == class20s) {
            area.warnings?.forEach { warning ->
                if (warning.status != "発表警報・注意報はなし" && warning.status != "解除") {
                    severity = getAlertSeverity(warning.code)
                    alertList.add(
                        Alert(
                            alertId = "${warning.code} ${alertResult.reportDatetime?.time}",
                            startDate = alertResult.reportDatetime,
                            headline = getAlertHeadline(context, warning.code),
                            description = alertResult.headlineText?.trim(),
                            source = alertResult.publishingOffice?.trim(),
                            severity = severity,
                            color = getAlertColor(severity)
                        )
                    )
                }
            }
        }
    }
    return alertList
}

private fun getWindDirection(
    direction: Any?,
): Double? {
    return when (direction) {
        0 -> -1.0
        1 -> 22.5
        2, "北東" -> 45.0
        3 -> 67.5
        4, "東" -> 90.0
        5 -> 112.5
        6, "南東" -> 135.0
        7 -> 157.5
        8, "南" -> 180.0
        9 -> 202.5
        10, "南西" -> 225.0
        11 -> 247.5
        12, "西" -> 270.0
        13 -> 292.5
        14, "北西" -> 315.0
        15 -> 337.5
        16, "北" -> 0.0
        else -> null
    }
}

private fun getHourlyWeatherText(
    context: Context,
    weather: String?,
): String? {
    return when (weather) {
        "晴れ" -> context.getString(R.string.common_weather_text_clear_sky)
        "くもり" -> context.getString(R.string.common_weather_text_cloudy)
        "雨" -> context.getString(R.string.common_weather_text_rain)
        "雪" -> context.getString(R.string.common_weather_text_snow)
        "雨または雪" -> context.getString(R.string.common_weather_text_rain_snow_mixed)
        else -> null
    }
}

private fun getHourlyWeatherCode(
    weather: String?,
): WeatherCode? {
    return when (weather) {
        "晴れ" -> WeatherCode.CLEAR
        "くもり" -> WeatherCode.CLOUDY
        "雨" -> WeatherCode.RAIN
        "雪" -> WeatherCode.SNOW
        "雨または雪" -> WeatherCode.SLEET
        else -> null
    }
}

// There are 118 daily weather codes used by JMA.
// They have been listed in its own file at JmaConstants.kt.
private fun getDailyWeatherText(
    context: Context,
    weather: String?,
    night: Boolean = false,
): String? {
    val i = if (night) {
        1
    } else {
        0
    }
    val stringId = if (JMA_DAILY_WEATHER_TEXTS.containsKey(weather)) {
        JMA_DAILY_WEATHER_TEXTS[weather]!!.getOrNull(i)
    } else {
        null
    }
    return stringId?.let { context.getString(it) }
}

// There are 118 daily weather codes used by JMA.
// They have been listed in its own file at JmaConstants.kt.
private fun getDailyWeatherCode(
    weather: String?,
    night: Boolean = false,
): WeatherCode? {
    val i = if (night) {
        1
    } else {
        0
    }
    return if (JMA_DAILY_WEATHER_CODES.containsKey(weather)) {
        JMA_DAILY_WEATHER_CODES[weather]!!.getOrNull(i)
    } else {
        null
    }
}

private fun getCurrentWeatherText(
    context: Context,
    weather: Int?,
): String? {
    return when (weather) {
        0 -> context.getString(R.string.common_weather_text_clear_sky) // 晴
        1 -> context.getString(R.string.common_weather_text_cloudy) // 曇
        2 -> context.getString(R.string.weather_kind_haze) // 煙霧
        3 -> context.getString(R.string.common_weather_text_fog) // 霧
        4 -> context.getString(R.string.common_weather_text_rain) // 降水またはしゅう雨性の降水
        5 -> context.getString(R.string.common_weather_text_drizzle) // 霧雨
        6 -> context.getString(R.string.common_weather_text_drizzle_freezing) // 着氷性の霧雨
        7 -> context.getString(R.string.common_weather_text_rain) // 雨
        8 -> context.getString(R.string.common_weather_text_rain_freezing) // 着氷性の雨
        9 -> context.getString(R.string.common_weather_text_rain_snow_mixed) // みぞれ
        10 -> context.getString(R.string.common_weather_text_snow) // 雪
        11 -> context.getString(R.string.common_weather_text_rain_freezing) // 凍雨
        12 -> context.getString(R.string.common_weather_text_snow_grains) // 霧雪
        13 -> context.getString(R.string.common_weather_text_rain_showers) // しゅう雨または止み間のある雨
        14 -> context.getString(R.string.common_weather_text_snow_showers) // しゅう雪または止み間のある雪
        15 -> context.getString(R.string.weather_kind_hail) // ひょう
        16 -> context.getString(R.string.weather_kind_thunderstorm) // 雷
        else -> null
    }
}

private fun getCurrentWeatherCode(
    weather: Int?,
): WeatherCode? {
    return when (weather) {
        0 -> WeatherCode.CLEAR
        1 -> WeatherCode.CLOUDY
        2 -> WeatherCode.HAZE
        3 -> WeatherCode.FOG
        4, 5, 7, 13 -> WeatherCode.RAIN
        6, 8, 9, 11 -> WeatherCode.SLEET
        10, 12, 14 -> WeatherCode.SNOW
        15 -> WeatherCode.HAIL
        16 -> WeatherCode.THUNDERSTORM
        else -> null
    }
}

private fun getAlertHeadline(
    context: Context,
    code: String?,
): String? {
    return when (code) {
        "33" -> context.getString(R.string.jma_warning_text_heavy_rain_emergency) // 大雨特別警報
        "03" -> context.getString(R.string.jma_warning_text_heavy_rain_warning) // 大雨警報
        "10" -> context.getString(R.string.jma_warning_text_heavy_rain_advisory) // 大雨注意報
        "04" -> context.getString(R.string.jma_warning_text_flood_warning) // 洪水警報
        "18" -> context.getString(R.string.jma_warning_text_flood_advisory) // 洪水注意報
        "35" -> context.getString(R.string.jma_warning_text_storm_emergency) // 暴風特別警報
        "05" -> context.getString(R.string.jma_warning_text_storm_warning) // 暴風警報
        "15" -> context.getString(R.string.jma_warning_text_gale_advisory) // 強風注意報
        "32" -> context.getString(R.string.jma_warning_text_snowstorm_emergency) // 暴風雪特別警報
        "02" -> context.getString(R.string.jma_warning_text_snowstorm_warning) // 暴風雪警報
        "13" -> context.getString(R.string.jma_warning_text_gale_and_snow_advisory) // 風雪注意報
        "36" -> context.getString(R.string.jma_warning_text_heavy_snow_emergency) // 大雪特別警報
        "06" -> context.getString(R.string.jma_warning_text_heavy_snow_warning) // 大雪警報
        "12" -> context.getString(R.string.jma_warning_text_heavy_snow_advisory) // 大雪注意報
        "37" -> context.getString(R.string.jma_warning_text_high_wave_emergency) // 波浪特別警報
        "07" -> context.getString(R.string.jma_warning_text_high_wave_warning) // 波浪警報
        "16" -> context.getString(R.string.jma_warning_text_high_wave_advisory) // 波浪注意報
        "38" -> context.getString(R.string.jma_warning_text_storm_surge_emergency) // 高潮特別警報
        "08" -> context.getString(R.string.jma_warning_text_storm_surge_warning) // 高潮警報
        "19+" -> context.getString(R.string.jma_warning_text_storm_surge_advisory) // 高潮注意報
        "19" -> context.getString(R.string.jma_warning_text_storm_surge_advisory) // 高潮注意報
        "14" -> context.getString(R.string.jma_warning_text_thunderstorm_advisory) // 雷注意報
        "17" -> context.getString(R.string.jma_warning_text_snow_melting_advisory) // 融雪注意報
        "20" -> context.getString(R.string.jma_warning_text_dense_fog_advisory) // 濃霧注意報
        "21" -> context.getString(R.string.jma_warning_text_dry_air_advisory) // 乾燥注意報
        "22" -> context.getString(R.string.jma_warning_text_avalanche_advisory) // なだれ注意報
        "23" -> context.getString(R.string.jma_warning_text_low_temperature_advisory) // 低温注意報
        "24" -> context.getString(R.string.jma_warning_text_frost_advisory) // 霜注意報
        "25" -> context.getString(R.string.jma_warning_text_ice_accretion_advisory) // 着氷注意報
        "26" -> context.getString(R.string.jma_warning_text_snow_accretion_advisory) // 着雪注意報
        else -> null
    }
}

private fun getAlertSeverity(
    code: String?,
): AlertSeverity {
    return when (code) {
        "33" -> AlertSeverity.EXTREME
        "35", "32", "36", "37", "38", "08" -> AlertSeverity.SEVERE
        "03", "04", "05", "02", "06", "07", "19+" -> AlertSeverity.MODERATE
        "10", "18", "15", "13", "12", "16", "19", "14", "17",
        "20", "21", "22", "23", "24", "25", "26",
        -> AlertSeverity.MINOR
        else -> AlertSeverity.UNKNOWN
    }
}

private fun getAlertColor(
    severity: AlertSeverity,
): Int {
    return when (severity) {
        AlertSeverity.EXTREME -> Color.rgb(12, 0, 12)
        AlertSeverity.SEVERE -> Color.rgb(160, 0, 160)
        AlertSeverity.MODERATE -> Color.rgb(255, 40, 0)
        AlertSeverity.MINOR -> Color.rgb(242, 231, 0)
        else -> Alert.colorFromSeverity(AlertSeverity.UNKNOWN)
    }
}
