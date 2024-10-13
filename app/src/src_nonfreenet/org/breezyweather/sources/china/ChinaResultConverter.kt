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

package org.breezyweather.sources.china

import android.graphics.Color
import androidx.annotation.ColorInt
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.AlertSeverity
import breezyweather.domain.weather.model.Astro
import breezyweather.domain.weather.model.Current
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.HalfDay
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.sources.china.json.ChinaForecastDaily
import org.breezyweather.sources.china.json.ChinaForecastHourly
import org.breezyweather.sources.china.json.ChinaForecastResult
import org.breezyweather.sources.china.json.ChinaLocationResult
import org.breezyweather.sources.china.json.ChinaMinutelyResult
import java.util.Calendar
import java.util.Date
import java.util.Objects

fun convert(
    location: Location?, // Null if location search, current location if reverse geocoding
    result: ChinaLocationResult
): Location {
    return (location ?: Location())
        .copy(
            cityId = result.locationKey!!.replace("weathercn:", ""),
            latitude = location?.latitude ?: result.latitude!!.toDouble(),
            longitude = location?.longitude ?: result.longitude!!.toDouble(),
            timeZone = "Asia/Shanghai",
            country = "",
            countryCode = "CN",
            admin2 = result.affiliation, // TODO: Double check if admin1 or admin2
            city = result.name ?: ""
        )
}

fun convert(
    location: Location,
    forecastResult: ChinaForecastResult,
    minutelyResult: ChinaMinutelyResult
): WeatherWrapper {
    // If the API doesn’t return current, hourly or daily, consider data as garbage and keep cached data
    if (forecastResult.current == null || forecastResult.forecastDaily == null || forecastResult.forecastHourly == null) {
        throw InvalidOrIncompleteDataException()
    }

    return WeatherWrapper(
        current = Current(
            weatherText = getWeatherText(forecastResult.current.weather),
            weatherCode = getWeatherCode(forecastResult.current.weather),
            temperature = Temperature(
                temperature = forecastResult.current.temperature?.value?.toDoubleOrNull(),
                apparentTemperature = forecastResult.current.feelsLike?.value?.toDoubleOrNull()
            ),
            wind = if (forecastResult.current.wind != null) Wind(
                degree = forecastResult.current.wind.direction?.value?.toDoubleOrNull(),
                speed = forecastResult.current.wind.speed?.value?.toDoubleOrNull()?.div(3.6)
            ) else null,
            uV = if (forecastResult.current.uvIndex != null) {
                UV(index = forecastResult.current.uvIndex.toDoubleOrNull())
            } else null,
            airQuality = forecastResult.aqi?.let {
                AirQuality(
                    pM25 = it.pm25?.toDoubleOrNull(),
                    pM10 = it.pm10?.toDoubleOrNull(),
                    sO2 = it.so2?.toDoubleOrNull(),
                    nO2 = it.no2?.toDoubleOrNull(),
                    o3 = it.o3?.toDoubleOrNull(),
                    cO = it.co?.toDoubleOrNull()
                )
            },
            relativeHumidity = if (!forecastResult.current.humidity?.value.isNullOrEmpty()) {
                forecastResult.current.humidity!!.value!!.toDoubleOrNull()
            } else null,
            pressure = if (!forecastResult.current.pressure?.value.isNullOrEmpty()) {
                forecastResult.current.pressure!!.value!!.toDoubleOrNull()
            } else null,
            visibility = if (!forecastResult.current.visibility?.value.isNullOrEmpty()) {
                forecastResult.current.visibility!!.value!!.toDoubleOrNull()?.times(1000)
            } else null,
            hourlyForecast = if (minutelyResult.precipitation != null) {
                minutelyResult.precipitation.description
            } else null
        ),
        dailyForecast = getDailyList(
            forecastResult.current.pubTime,
            location,
            forecastResult.forecastDaily
        ),
        hourlyForecast = getHourlyList(
            forecastResult.current.pubTime,
            location,
            forecastResult.forecastHourly
        ),
        minutelyForecast = getMinutelyList(
            location,
            minutelyResult
        ),
        alertList = getAlertList(forecastResult)
    )
}

private fun getDailyList(
    publishDate: Date,
    location: Location,
    dailyForecast: ChinaForecastDaily
): List<Daily> {
    if (dailyForecast.weather == null || dailyForecast.weather.value.isNullOrEmpty()) return emptyList()

    val dailyList: MutableList<Daily> = ArrayList(dailyForecast.weather.value.size)
    dailyForecast.weather.value.forEachIndexed { index, weather ->
        val calendar = publishDate.toCalendarWithTimeZone(location.javaTimeZone).apply {
            add(Calendar.DAY_OF_YEAR, index)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        dailyList.add(
            Daily(
                date = calendar.time,
                day = HalfDay(
                    weatherText = getWeatherText(weather.from),
                    weatherPhase = getWeatherText(weather.from),
                    weatherCode = getWeatherCode(weather.from),
                    temperature = Temperature(
                        temperature = dailyForecast.temperature?.value?.getOrNull(index)?.from?.toDoubleOrNull()
                    ),
                    precipitationProbability = PrecipitationProbability(
                        total = getPrecipitationProbability(dailyForecast, index)
                    ),
                    wind = if (dailyForecast.wind != null) Wind(
                        degree = dailyForecast.wind.direction?.value?.getOrNull(index)?.from?.toDoubleOrNull(),
                        speed = dailyForecast.wind.speed?.value?.getOrNull(index)?.from?.toDoubleOrNull()?.div(3.6)
                    ) else null
                ),
                night = HalfDay(
                    weatherText = getWeatherText(weather.to),
                    weatherPhase = getWeatherText(weather.to),
                    weatherCode = getWeatherCode(weather.to),
                    temperature = Temperature(
                        temperature = dailyForecast.temperature?.value?.getOrNull(index)?.to?.toDoubleOrNull()
                    ),
                    precipitationProbability = PrecipitationProbability(
                        total = getPrecipitationProbability(dailyForecast, index)
                    ),
                    wind = if (dailyForecast.wind != null) Wind(
                        degree = dailyForecast.wind.direction?.value?.getOrNull(index)?.to?.toDoubleOrNull(),
                        speed = dailyForecast.wind.speed?.value?.getOrNull(index)?.to?.toDoubleOrNull()?.div(3.6)
                    ) else null
                ),
                sun = Astro(
                    riseDate = dailyForecast.sunRiseSet?.value?.getOrNull(index)?.from,
                    setDate = dailyForecast.sunRiseSet?.value?.getOrNull(index)?.to
                )
            )
        )
    }
    return dailyList
}

private fun getPrecipitationProbability(forecast: ChinaForecastDaily, index: Int): Double? {
    if (forecast.precipitationProbability == null ||
        forecast.precipitationProbability.value.isNullOrEmpty()) {
        return null
    }

    return forecast.precipitationProbability.value.getOrNull(index)?.toDoubleOrNull()
}

private fun getHourlyList(
    publishDate: Date,
    location: Location,
    hourlyForecast: ChinaForecastHourly
): List<HourlyWrapper> {
    if (hourlyForecast.weather == null || hourlyForecast.weather.value.isNullOrEmpty()) return emptyList()

    val hourlyListPubTime = hourlyForecast.temperature?.pubTime ?: publishDate

    val hourlyList: MutableList<HourlyWrapper> = ArrayList(hourlyForecast.weather.value.size)
    hourlyForecast.weather.value.forEachIndexed { index, weather ->
        val calendar = hourlyListPubTime.toCalendarWithTimeZone(location.javaTimeZone).apply {
            add(Calendar.HOUR_OF_DAY, index) // FIXME: Wrong TimeZone for the first item
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val date = calendar.time
        hourlyList.add(
            HourlyWrapper(
                date = date,
                weatherText = getWeatherText(weather.toString()),
                weatherCode = getWeatherCode(weather.toString()),
                temperature = Temperature(
                    temperature = hourlyForecast.temperature?.value?.getOrNull(index)?.toDouble()
                ),
                wind = if (hourlyForecast.wind != null) Wind(
                    degree = hourlyForecast.wind.value?.getOrNull(index)?.direction?.toDoubleOrNull(),
                    speed = hourlyForecast.wind.value?.getOrNull(index)?.speed?.toDoubleOrNull()?.div(3.6)
                ) else null
            )
        )
    }
    return hourlyList
}

private fun getMinutelyList(
    location: Location,
    minutelyResult: ChinaMinutelyResult
): List<Minutely> {
    if (minutelyResult.precipitation == null || minutelyResult.precipitation.value.isNullOrEmpty()) return emptyList()

    val current = minutelyResult.precipitation.pubTime ?: return emptyList()
    val minutelyList: MutableList<Minutely> = ArrayList(minutelyResult.precipitation.value.size)

    minutelyResult.precipitation.value.forEachIndexed { minute, precipitation ->
        val calendar = current.toCalendarWithTimeZone(location.javaTimeZone).apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, minute)
        }
        minutelyList.add(
            Minutely(
                date = calendar.time,
                minuteInterval = 1,
                precipitationIntensity = precipitation.times(60) // mm/min -> mm/h
            )
        )
    }
    return minutelyList
}

private fun getAlertList(result: ChinaForecastResult): List<Alert> {
    if (result.alerts.isNullOrEmpty()) return emptyList()

    return result.alerts.map { alert ->
        Alert(
            // Create unique ID from: title, level, start time
            alertId = Objects.hash(alert.title, alert.level, alert.pubTime?.time ?: System.currentTimeMillis()).toString(),
            startDate = alert.pubTime,
            headline = alert.title,
            description = alert.detail,
            severity = getAlertPriority(alert.level),
            color = getAlertColor(alert.level) ?: Alert.colorFromSeverity(AlertSeverity.UNKNOWN)
        )
    }
}

fun convertSecondary(
    location: Location,
    forecastResult: ChinaForecastResult,
    minutelyResult: ChinaMinutelyResult
): SecondaryWeatherWrapper {

    return SecondaryWeatherWrapper(
        airQuality = forecastResult.aqi?.let {
            AirQualityWrapper(
                current = AirQuality(
                    pM25 = it.pm25?.toDoubleOrNull(),
                    pM10 = it.pm10?.toDoubleOrNull(),
                    sO2 = it.so2?.toDoubleOrNull(),
                    nO2 = it.no2?.toDoubleOrNull(),
                    o3 = it.o3?.toDoubleOrNull(),
                    cO = it.co?.toDoubleOrNull()
                )
            )
        },
        minutelyForecast = getMinutelyList(location, minutelyResult),
        alertList = getAlertList(forecastResult),
    )
}

private fun getWeatherText(icon: String?): String {
    return if (icon.isNullOrEmpty()) {
        "未知"
    } else when (icon) {
        "0", "00" -> "晴"
        "1", "01" -> "多云"
        "2", "02" -> "阴"
        "3", "03" -> "阵雨"
        "4", "04" -> "雷阵雨"
        "5", "05" -> "雷阵雨伴有冰雹"
        "6", "06" -> "雨夹雪"
        "7", "07" -> "小雨"
        "8", "08" -> "中雨"
        "9", "09" -> "大雨"
        "10" -> "暴雨"
        "11" -> "大暴雨"
        "12" -> "特大暴雨"
        "13" -> "阵雪"
        "14" -> "小雪"
        "15" -> "中雪"
        "16" -> "大雪"
        "17" -> "暴雪"
        "18" -> "雾"
        "19" -> "冻雨"
        "20" -> "沙尘暴"
        "21" -> "小到中雨"
        "22" -> "中到大雨"
        "23" -> "大到暴雨"
        "24" -> "暴雨到大暴雨"
        "25" -> "大暴雨到特大暴雨"
        "26" -> "小到中雪"
        "27" -> "中到大雪"
        "28" -> "大到暴雪"
        "29" -> "浮尘"
        "30" -> "扬沙"
        "31" -> "强沙尘暴"
        "53", "54", "55", "56" -> "霾"
        else -> "未知"
    }
}

private fun getWeatherCode(icon: String?): WeatherCode? {
    return if (icon.isNullOrEmpty()) {
        null
    } else when (icon) {
        "0", "00" -> WeatherCode.CLEAR
        "1", "01" -> WeatherCode.PARTLY_CLOUDY
        "3", "7", "8", "9", "03", "07", "08", "09", "10", "11", "12", "21", "22", "23", "24", "25" -> WeatherCode.RAIN
        "4", "04" -> WeatherCode.THUNDERSTORM
        "5", "05" -> WeatherCode.HAIL
        "6", "06", "19" -> WeatherCode.SLEET
        "13", "14", "15", "16", "17", "26", "27", "28" -> WeatherCode.SNOW
        "18", "32", "49", "57" -> WeatherCode.FOG
        "20", "29", "30" -> WeatherCode.WIND
        "53", "54", "55", "56" -> WeatherCode.HAZE
        else -> WeatherCode.CLOUDY
    }
}

private fun getAlertPriority(color: String?): AlertSeverity {
    if (color.isNullOrEmpty()) return AlertSeverity.UNKNOWN
    return when (color) {
        "蓝", "蓝色" -> AlertSeverity.EXTREME
        "黄", "黄色" -> AlertSeverity.SEVERE
        "橙", "橙色", "橘", "橘色", "橘黄", "橘黄色" -> AlertSeverity.MODERATE
        "红", "红色" -> AlertSeverity.MINOR
        else -> AlertSeverity.UNKNOWN
    }
}

@ColorInt
private fun getAlertColor(color: String?): Int? {
    if (color.isNullOrEmpty()) return null
    return when (color) {
        "蓝", "蓝色" -> Color.rgb(51, 100, 255)
        "黄", "黄色" -> Color.rgb(250, 237, 36)
        "橙", "橙色", "橘", "橘色", "橘黄", "橘黄色" -> Color.rgb(249, 138, 30)
        "红", "红色" -> Color.rgb(215, 48, 42)
        else -> null
    }
}
