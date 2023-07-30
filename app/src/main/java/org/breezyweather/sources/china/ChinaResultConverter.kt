package org.breezyweather.sources.china

import android.graphics.Color
import androidx.annotation.ColorInt
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.AirQuality
import org.breezyweather.common.basic.models.weather.Alert
import org.breezyweather.common.basic.models.weather.Astro
import org.breezyweather.common.basic.models.weather.Base
import org.breezyweather.common.basic.models.weather.Current
import org.breezyweather.common.basic.models.weather.Daily
import org.breezyweather.common.basic.models.weather.HalfDay
import org.breezyweather.common.basic.models.weather.History
import org.breezyweather.common.basic.models.weather.Minutely
import org.breezyweather.common.basic.models.weather.PrecipitationProbability
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.basic.models.weather.UV
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.common.basic.models.weather.Wind
import org.breezyweather.common.basic.wrappers.HourlyWrapper
import org.breezyweather.common.basic.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.WeatherException
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.sources.china.json.ChinaForecastDaily
import org.breezyweather.sources.china.json.ChinaForecastHourly
import org.breezyweather.sources.china.json.ChinaForecastResult
import org.breezyweather.sources.china.json.ChinaLocationResult
import org.breezyweather.sources.china.json.ChinaMinutelyResult
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

fun convert(location: Location?, result: ChinaLocationResult): Location {
    return Location(
        cityId = result.locationKey!!.replace("weathercn:", ""),
        latitude = location?.latitude ?: result.latitude!!.toFloat(),
        longitude = location?.longitude ?: result.longitude!!.toFloat(),
        timeZone = TimeZone.getTimeZone("Asia/Shanghai"),
        country = "",
        province = result.affiliation,
        city = result.name!!,
        weatherSource = "china",
        airQualitySource = location?.airQualitySource,
        allergenSource = location?.allergenSource,
        minutelySource = location?.minutelySource,
        alertSource = location?.alertSource
    )
}
fun convert(
    location: Location,
    forecastResult: ChinaForecastResult,
    minutelyResult: ChinaMinutelyResult
): WeatherWrapper {
    // If the API doesn’t return current, hourly or daily, consider data as garbage and keep cached data
    if (forecastResult.current == null || forecastResult.forecastDaily == null || forecastResult.forecastHourly == null) {
        throw WeatherException()
    }

    return WeatherWrapper(
        base = Base(
            publishDate = forecastResult.current.pubTime
        ),
        current = Current(
            weatherText = getWeatherText(forecastResult.current.weather),
            weatherCode = getWeatherCode(forecastResult.current.weather),
            temperature = Temperature(
                temperature = forecastResult.current.temperature?.value?.toFloatOrNull(),
                apparentTemperature = forecastResult.current.feelsLike?.value?.toFloatOrNull()
            ),
            wind = if (forecastResult.current.wind != null) Wind(
                degree = forecastResult.current.wind.direction?.value?.toFloatOrNull(),
                speed = forecastResult.current.wind.speed?.value?.toDoubleOrNull()?.div(3.6)?.toFloat()
            ) else null,
            uV = if (forecastResult.current.uvIndex != null) {
                UV(index = forecastResult.current.uvIndex.toFloatOrNull())
            } else null,
            airQuality = forecastResult.aqi?.let {
                AirQuality(
                    pM25 = it.pm25?.toFloat(),
                    pM10 = it.pm10?.toFloat(),
                    sO2 = it.so2?.toFloat(),
                    nO2 = it.no2?.toFloat(),
                    o3 = it.o3?.toFloat(),
                    cO = it.co?.toFloat()
                )
            },
            relativeHumidity = if (!forecastResult.current.humidity?.value.isNullOrEmpty()) {
                forecastResult.current.humidity!!.value!!.toFloatOrNull()
            } else null,
            pressure = if (!forecastResult.current.pressure?.value.isNullOrEmpty()) {
                forecastResult.current.pressure!!.value!!.toFloat()
            } else null,
            visibility = if (!forecastResult.current.visibility?.value.isNullOrEmpty()) {
                forecastResult.current.visibility!!.value!!.toFloatOrNull()?.times(1000)
            } else null,
            hourlyForecast = if (minutelyResult.precipitation != null) {
                minutelyResult.precipitation.description
            } else null
        ),
        yesterday = getYesterday(forecastResult),
        dailyForecast = getDailyList(
            forecastResult.current.pubTime,
            location.timeZone,
            forecastResult.forecastDaily
        ),
        hourlyForecast = getHourlyList(
            forecastResult.current.pubTime,
            location.timeZone,
            forecastResult.forecastHourly
        ),
        minutelyForecast = getMinutelyList(
            location.timeZone,
            minutelyResult
        ),
        alertList = getAlertList(forecastResult)
    )
}

private fun getYesterday(result: ChinaForecastResult): History? {
    if (result.yesterday == null) return null

    return try {
        History(
            date = Date(result.updateTime - 24 * 60 * 60 * 1000),
            daytimeTemperature = result.yesterday.tempMax?.toFloatOrNull(),
            nighttimeTemperature = result.yesterday.tempMin?.toFloatOrNull(),
        )
    } catch (ignore: Exception) {
        null
    }
}

private fun getDailyList(
    publishDate: Date,
    timeZone: TimeZone,
    dailyForecast: ChinaForecastDaily
): List<Daily> {
    if (dailyForecast.weather == null || dailyForecast.weather.value.isNullOrEmpty()) return emptyList()

    val dailyList: MutableList<Daily> = ArrayList(dailyForecast.weather.value.size)
    dailyForecast.weather.value.forEachIndexed { index, weather ->
        val calendar = publishDate.toCalendarWithTimeZone(timeZone).apply {
            add(Calendar.DATE, index) // FIXME: Wrong TimeZone for the first item
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
                        temperature = dailyForecast.temperature?.value?.getOrNull(index)?.from?.toFloatOrNull()
                    ),
                    precipitationProbability = PrecipitationProbability(
                        total = getPrecipitationProbability(dailyForecast, index)
                    ),
                    wind = if (dailyForecast.wind != null) Wind(
                        degree = dailyForecast.wind.direction?.value?.getOrNull(index)?.from?.toFloatOrNull(),
                        speed = dailyForecast.wind.speed?.value?.getOrNull(index)?.from?.toDoubleOrNull()?.div(3.6)?.toFloat()
                    ) else null
                ),
                night = HalfDay(
                    weatherText = getWeatherText(weather.to),
                    weatherPhase = getWeatherText(weather.to),
                    weatherCode = getWeatherCode(weather.to),
                    temperature = Temperature(
                        temperature = dailyForecast.temperature?.value?.getOrNull(index)?.to?.toFloatOrNull()
                    ),
                    precipitationProbability = PrecipitationProbability(
                        total = getPrecipitationProbability(dailyForecast, index)
                    ),
                    wind = if (dailyForecast.wind != null) Wind(
                        degree = dailyForecast.wind.direction?.value?.getOrNull(index)?.to?.toFloatOrNull(),
                        speed = dailyForecast.wind.speed?.value?.getOrNull(index)?.to?.toDoubleOrNull()?.div(3.6)?.toFloat()
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

private fun getPrecipitationProbability(forecast: ChinaForecastDaily, index: Int): Float? {
    if (forecast.precipitationProbability == null
        || forecast.precipitationProbability.value.isNullOrEmpty()) return null

    return forecast.precipitationProbability.value.getOrNull(index)?.toFloatOrNull()
}

private fun getHourlyList(
    publishDate: Date, timeZone: TimeZone,
    hourlyForecast: ChinaForecastHourly
): List<HourlyWrapper> {
    if (hourlyForecast.weather == null || hourlyForecast.weather.value.isNullOrEmpty()) return emptyList()

    val hourlyList: MutableList<HourlyWrapper> = ArrayList(hourlyForecast.weather.value.size)
    hourlyForecast.weather.value.forEachIndexed { index, weather ->
        val calendar = publishDate.toCalendarWithTimeZone(timeZone).apply {
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
                    temperature = hourlyForecast.temperature?.value?.getOrNull(index)?.toFloat()
                ),
                wind = if (hourlyForecast.wind != null) Wind(
                    degree = hourlyForecast.wind.value?.getOrNull(index)?.direction?.toFloatOrNull(),
                    speed = hourlyForecast.wind.value?.getOrNull(index)?.speed?.toDoubleOrNull()?.div(3.6)?.toFloat()
                ) else null
            )
        )
    }
    return hourlyList
}

private fun getMinutelyList(
    timeZone: TimeZone,
    minutelyResult: ChinaMinutelyResult
): List<Minutely> {
    if (minutelyResult.precipitation == null || minutelyResult.precipitation.value.isNullOrEmpty()) return emptyList()

    val current = minutelyResult.precipitation.pubTime ?: return emptyList()
    val minutelyList: MutableList<Minutely> = ArrayList(minutelyResult.precipitation.value.size)

    minutelyResult.precipitation.value.forEachIndexed { minute, precipitation ->
        val calendar = current.toCalendarWithTimeZone(timeZone).apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, minute)
        }
        minutelyList.add(
            Minutely(
                date = calendar.time,
                minuteInterval = 1,
                precipitationIntensity = precipitation
            )
        )
    }
    return minutelyList
}

private fun getAlertList(result: ChinaForecastResult): List<Alert> {
    if (result.alerts.isNullOrEmpty()) return emptyList()

    return result.alerts.map { alert ->
        Alert(
            // TODO: Avoid having the same ID for two different alerts happening at the same time
            alertId = alert.pubTime?.time ?: System.currentTimeMillis(),
            startDate = alert.pubTime,
            description = alert.title ?: "",
            content = alert.detail,
            priority = getAlertPriority(alert.level),
            color = getAlertColor(alert.level)
        )
    }
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

@ColorInt
private fun getAlertPriority(color: String?): Int {
    if (color.isNullOrEmpty()) return 0
    return when (color) {
        "蓝", "蓝色" -> 1
        "黄", "黄色" -> 2
        "橙", "橙色", "橘", "橘色", "橘黄", "橘黄色" -> 3
        "红", "红色" -> 4
        else -> 0
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