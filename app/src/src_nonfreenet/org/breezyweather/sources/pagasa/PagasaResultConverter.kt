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

package org.breezyweather.sources.pagasa

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import com.google.maps.android.SphericalUtil
import com.google.maps.android.model.LatLng
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.getIsoFormattedDate
import org.breezyweather.sources.getWindDegree
import org.breezyweather.sources.pagasa.json.PagasaCurrentResult
import org.breezyweather.sources.pagasa.json.PagasaHourlyResult
import org.breezyweather.sources.pagasa.json.PagasaLocationResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.time.Duration.Companion.hours

internal fun convert(
    location: Location,
    locations: Map<String, PagasaLocationResult>,
): Map<String, String> {
    var nearestDistance = Double.POSITIVE_INFINITY
    var nearestStation = ""
    var nearestKey = ""
    var distance = Double.POSITIVE_INFINITY
    locations.keys.forEach { key ->
        locations[key]!!.let {
            distance = SphericalUtil.computeDistanceBetween(
                LatLng(it.latitude.toDouble(), it.longitude.toDouble()),
                LatLng(location.latitude, location.longitude)
            )
            if (distance < nearestDistance) {
                nearestDistance = distance
                nearestStation = it.siteId
                nearestKey = key
            }
        }
    }

    // Forecast locations are sparse across the archipelago of the Philippines.
    // Therefore, only reject distances more than 200 km,
    // otherwise several major cities will trigger invalid location exception.
    if (nearestDistance > 200000) {
        throw InvalidLocationException()
    }
    return mapOf(
        "station" to nearestStation,
        "key" to nearestKey
    )
}

internal fun getCurrent(
    location: Location,
    currentResult: List<PagasaCurrentResult>?,
): CurrentWrapper? {
    val formatter = SimpleDateFormat("MMMM d, yyyy, h:mm a", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Asia/Manila")
    val now = Date().time
    var nearestDistance = Double.POSITIVE_INFINITY
    var nearestStation = Int.MAX_VALUE
    var distance = Double.POSITIVE_INFINITY
    var update: Long
    currentResult?.forEachIndexed { i, station ->
        update = formatter.parse(station.datetime)!!.time
        if ((now - update) <= 2.hours.inWholeMilliseconds) {
            distance = SphericalUtil.computeDistanceBetween(
                LatLng(station.latitude.toDouble(), station.longitude.toDouble()),
                LatLng(location.latitude, location.longitude)
            )
            if (distance < nearestDistance) {
                nearestDistance = distance
                nearestStation = i
            }
        }
    }
    return currentResult?.getOrNull(nearestStation)?.let {
        CurrentWrapper(
            temperature = TemperatureWrapper(
                temperature = it.temperature?.substringBefore(" ")?.toDoubleOrNull()
            ),
            wind = Wind(
                degree = getWindDegree(it.windDirection),
                speed = it.windSpeed?.substringBefore(" ")?.toDoubleOrNull()?.div(3.6)
            ),
            relativeHumidity = it.humidity?.substringBefore(" ")?.toDoubleOrNull(),
            pressure = it.pressure?.toDoubleOrNull()
        )
    }
}

internal fun getDailyForecast(
    location: Location,
    hourlyForecast: List<HourlyWrapper>,
): List<DailyWrapper> {
    // Need to provide an empty daily list so that
    // CommonConverter.kt will compute the daily forecast items.
    val dates = hourlyForecast.groupBy { it.date.getIsoFormattedDate(location) }.keys
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    formatter.timeZone = location.timeZone
    val dailyList = mutableListOf<DailyWrapper>()
    dates.forEachIndexed { i, day ->
        if (i < dates.size - 1) { // skip the last day
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
    context: Context,
    hourlyResult: List<PagasaHourlyResult>?,
): List<HourlyWrapper> {
    val hourlyList = mutableListOf<HourlyWrapper>()
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    // Hourly forecasts are likely reported in UTC rather than Asia/Manila,
    // judging from the temperature and humidity trends.
    formatter.timeZone = TimeZone.getTimeZone("Etc/UTC")
    var lastTime: String? = null
    hourlyResult?.forEach { day ->
        day.forecast?.tabular?.time?.forEach {
            // Check for duplication: for day 0, every hour in the output is duplicated
            if ((it.attributes?.from != null) && (it.attributes.from != lastTime)) {
                lastTime = it.attributes.from
                hourlyList.add(
                    HourlyWrapper(
                        date = formatter.parse(it.attributes.from)!!,
                        weatherText = getHourlyWeatherText(context, it.symbol?.attributes?.symbol),
                        weatherCode = getHourlyWeatherCode(it.symbol?.attributes?.symbol),
                        temperature = TemperatureWrapper(
                            temperature = it.temperature?.attributes?.value?.toDoubleOrNull()
                        ),
                        precipitation = Precipitation(
                            total = it.precipitation?.attributes?.value?.toDoubleOrNull()
                        ),
                        wind = Wind(
                            degree = it.windDirection?.attributes?.deg?.toDoubleOrNull(),
                            speed = it.windSpeed?.attributes?.mps?.toDoubleOrNull()
                        ),
                        relativeHumidity = it.relativeHumidity?.attributes?.value?.toDoubleOrNull()
                    )
                )
            }
        }
    }
    return hourlyList
}

private fun getHourlyWeatherText(
    context: Context,
    symbol: String?,
): String? {
    return symbol?.let {
        with(symbol) {
            when {
                startsWith("01") -> context.getString(R.string.common_weather_text_clear_sky)
                startsWith("02") -> context.getString(R.string.common_weather_text_mostly_clear)
                startsWith("03") -> context.getString(R.string.common_weather_text_partly_cloudy)
                startsWith("04") -> context.getString(R.string.common_weather_text_cloudy)
                startsWith("05") -> context.getString(R.string.common_weather_text_rain_showers)
                startsWith("06") -> "Thunder showers" // thundershower
                startsWith("07") -> context.getString(R.string.common_weather_text_rain_snow_mixed_showers)
                startsWith("08") -> context.getString(R.string.common_weather_text_snow_showers)
                startsWith("09") -> context.getString(R.string.common_weather_text_rain)
                startsWith("10") -> context.getString(R.string.common_weather_text_rain_heavy)
                startsWith("11") -> context.getString(R.string.weather_kind_thunderstorm) // heavy thunderstorm
                startsWith("12") -> context.getString(R.string.common_weather_text_rain_snow_mixed)
                startsWith("13") -> context.getString(R.string.common_weather_text_snow)
                startsWith("14") -> "Thunder snowstorm" // thunder snow
                startsWith("15") -> context.getString(R.string.common_weather_text_fog)
                startsWith("16") -> "Thunder sleet shower" // thunder sleet shower
                startsWith("17") -> "Thunder snow shower" // thunder snow shower
                startsWith("18") -> context.getString(R.string.weather_kind_thunderstorm) // thunderstorm
                startsWith("19") -> "Thunder rain and snow mixed" // thunder sleet
                else -> null
            }
        }
    }
}

// Source: https://pubfiles.pagasa.dost.gov.ph/pagasaweb/images/meteogram-symbols-30px.png
// There are too many snow and sleet icons for a tropical country like the Philippines
private fun getHourlyWeatherCode(
    symbol: String?,
): WeatherCode? {
    return symbol?.let {
        with(symbol) {
            when {
                startsWith("01") -> WeatherCode.CLEAR
                startsWith("02") -> WeatherCode.CLEAR
                startsWith("03") -> WeatherCode.PARTLY_CLOUDY
                startsWith("04") -> WeatherCode.CLOUDY
                startsWith("05") -> WeatherCode.RAIN
                startsWith("06") -> WeatherCode.THUNDERSTORM // thundershower
                startsWith("07") -> WeatherCode.SLEET
                startsWith("08") -> WeatherCode.SNOW
                startsWith("09") -> WeatherCode.RAIN
                startsWith("10") -> WeatherCode.RAIN
                startsWith("11") -> WeatherCode.THUNDERSTORM // heavy thunderstorm
                startsWith("12") -> WeatherCode.SLEET
                startsWith("13") -> WeatherCode.SNOW
                startsWith("14") -> WeatherCode.THUNDERSTORM // thunder snow
                startsWith("15") -> WeatherCode.FOG
                startsWith("16") -> WeatherCode.THUNDERSTORM // thunder sleet shower
                startsWith("17") -> WeatherCode.THUNDERSTORM // thunder snow shower
                startsWith("18") -> WeatherCode.THUNDERSTORM // thunderstorm
                startsWith("19") -> WeatherCode.THUNDERSTORM // thunder sleet
                else -> null
            }
        }
    }
}
