package org.breezyweather.weather.china

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import org.breezyweather.BreezyWeather
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.*
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.weather.WeatherService.WeatherResultWrapper
import org.breezyweather.weather.china.json.ChinaForecastResult
import org.breezyweather.weather.china.json.ChinaMinutelyResult
import org.breezyweather.weather.getHoursOfDay
import org.breezyweather.weather.getWindDirection
import org.breezyweather.weather.getWindLevel
import org.breezyweather.weather.isDaylight
import java.util.*


fun convert(
    context: Context, location: Location,
    forecastResult: ChinaForecastResult,
    minutelyResult: ChinaMinutelyResult
): WeatherResultWrapper {
    // If the API doesn’t return current, hourly or daily, consider data as garbage and keep cached data
    if (forecastResult.current == null || forecastResult.forecastDaily == null || forecastResult.forecastHourly == null) {
        return WeatherResultWrapper(null)
    }

    return try {
        val weather = Weather(
            base = Base(
                cityId = location.cityId,
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
                    direction = getWindDirection(context, forecastResult.current.wind.direction?.value?.toFloatOrNull()),
                    degree = WindDegree(
                        degree = forecastResult.current.wind.direction?.value?.toFloatOrNull(),
                        isNoDirection = false
                    ),
                    speed = forecastResult.current.wind.speed?.value?.toFloatOrNull(),
                    level = getWindLevel(context, forecastResult.current.wind.speed?.value?.toFloatOrNull())
                ) else null,
                uV = if (forecastResult.current.uvIndex != null) UV(
                    index = forecastResult.current.uvIndex.toFloatOrNull(),
                    description = getUVDescription(forecastResult.current.uvIndex.toInt())
                ) else null,
                airQuality = getAirQuality(forecastResult),
                relativeHumidity = if (!forecastResult.current.humidity?.value.isNullOrEmpty()) forecastResult.current.humidity!!.value!!.toFloatOrNull() else null,
                pressure = if (!forecastResult.current.pressure?.value.isNullOrEmpty()) forecastResult.current.pressure!!.value!!.toFloat() else null,
                visibility = if (!forecastResult.current.visibility?.value.isNullOrEmpty()) forecastResult.current.visibility!!.value!!.toFloatOrNull() else null,
                hourlyForecast = if (minutelyResult.precipitation != null) minutelyResult.precipitation.description else null
            ),
            yesterday = getYesterday(forecastResult),
            dailyForecast = getDailyList(context, forecastResult.current.pubTime, location.timeZone, forecastResult.forecastDaily),
            hourlyForecast = getHourlyList(
                context,
                forecastResult.current.pubTime,
                location.timeZone,
                forecastResult.forecastDaily.sunRiseSet?.value?.getOrNull(0)?.from,
                forecastResult.forecastDaily.sunRiseSet?.value?.getOrNull(0)?.to,
                forecastResult.forecastHourly
            ),
            minutelyForecast = getMinutelyList(
                location.timeZone,
                minutelyResult
            ),
            alertList = getAlertList(forecastResult)
        )
        WeatherResultWrapper(weather)
    } catch (e: Exception) {
        if (BreezyWeather.instance.debugMode) {
            e.printStackTrace()
        }
        WeatherResultWrapper(null)
    }
}

private fun getAirQuality(result: ChinaForecastResult): AirQuality? {
    if (result.aqi == null) return null

    return AirQuality(
        pM25 = result.aqi.pm25?.toFloat(),
        pM10 = result.aqi.pm10?.toFloat(),
        sO2 = result.aqi.so2?.toFloat(),
        nO2 = result.aqi.no2?.toFloat(),
        o3 = result.aqi.o3?.toFloat(),
        cO = result.aqi.co?.toFloat()
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
    context: Context,
    publishDate: Date,
    timeZone: TimeZone,
    dailyForecast: org.breezyweather.weather.china.json.ChinaForecastDaily
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
                        direction = getWindDirection(context, dailyForecast.wind.direction?.value?.getOrNull(index)?.from?.toFloatOrNull()),
                        degree = WindDegree(
                            degree = dailyForecast.wind.direction?.value?.getOrNull(index)?.from?.toFloatOrNull(),
                            isNoDirection = false
                        ),
                        speed = dailyForecast.wind.speed?.value?.getOrNull(index)?.from?.toFloatOrNull(),
                        level = getWindLevel(context, dailyForecast.wind.speed?.value?.getOrNull(index)?.from?.toFloatOrNull())
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
                        direction = getWindDirection(context, dailyForecast.wind.direction?.value?.getOrNull(index)?.to?.toFloatOrNull()),
                        degree = WindDegree(
                            degree = dailyForecast.wind.direction?.value?.getOrNull(index)?.to?.toFloatOrNull(),
                            isNoDirection = false
                        ),
                        speed = dailyForecast.wind.speed?.value?.getOrNull(index)?.to?.toFloatOrNull(),
                        level = getWindLevel(context, dailyForecast.wind.speed?.value?.getOrNull(index)?.to?.toFloatOrNull())
                    ) else null
                ),
                sun = Astro(
                    riseDate = dailyForecast.sunRiseSet?.value?.getOrNull(index)?.from,
                    setDate = dailyForecast.sunRiseSet?.value?.getOrNull(index)?.to
                ),
                hoursOfSun = getHoursOfDay(dailyForecast.sunRiseSet?.value?.getOrNull(index)?.from, dailyForecast.sunRiseSet?.value?.getOrNull(index)?.to)
            )
        )
    }
    return dailyList
}

private fun getPrecipitationProbability(forecast: org.breezyweather.weather.china.json.ChinaForecastDaily, index: Int): Float? {
    if (forecast.precipitationProbability == null
        || forecast.precipitationProbability.value.isNullOrEmpty()) return null

    return forecast.precipitationProbability.value.getOrNull(index)?.toFloatOrNull()
}

private fun getHourlyList(
    context: Context, publishDate: Date,
    timeZone: TimeZone,
    sunrise: Date?, sunset: Date?,
    hourlyForecast: org.breezyweather.weather.china.json.ChinaForecastHourly
): List<Hourly> {
    if (hourlyForecast.weather == null || hourlyForecast.weather.value.isNullOrEmpty()) return emptyList()

    val hourlyList: MutableList<Hourly> = ArrayList(hourlyForecast.weather.value.size)
    hourlyForecast.weather.value.forEachIndexed { index, weather ->
        val calendar = publishDate.toCalendarWithTimeZone(timeZone).apply {
            add(Calendar.HOUR_OF_DAY, index) // FIXME: Wrong TimeZone for the first item
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val date = calendar.time
        hourlyList.add(
            Hourly(
                date = date,
                isDaylight = isDaylight(sunrise, sunset, date, timeZone),
                weatherText = getWeatherText(weather.toString()),
                weatherCode = getWeatherCode(weather.toString()),
                temperature = Temperature(
                    temperature = hourlyForecast.temperature?.value?.getOrNull(index)?.toFloat()
                ),
                wind = if (hourlyForecast.wind != null) Wind(
                    direction = getWindDirection(context, hourlyForecast.wind.value?.getOrNull(index)?.direction?.toFloat()),
                    degree = WindDegree(
                        degree = hourlyForecast.wind.value?.getOrNull(index)?.direction?.toFloat(),
                        isNoDirection = false
                    ),
                    speed = hourlyForecast.wind.value?.getOrNull(index)?.speed?.toFloat(),
                    level = getWindLevel(context, hourlyForecast.wind.value?.getOrNull(index)?.speed?.toFloat())
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
                calendar.time,
                1,
                precipitation
            )
        )
    }
    return minutelyList
}

private fun getAlertList(result: ChinaForecastResult): List<Alert> {
    if (result.alerts.isNullOrEmpty()) return emptyList()

    val alertList: MutableList<Alert> = ArrayList(result.alerts.size)
    result.alerts.forEach { alert ->
        alertList.add(
            Alert(
                // TODO: Avoid having the same ID for two different alerts happening at the same time
                alertId = alert.pubTime?.time ?: System.currentTimeMillis(),
                startDate = alert.pubTime,
                description = alert.title ?: "",
                content = alert.detail,
                priority = getAlertPriority(alert.level),
                color = getAlertColor(alert.level)
            )
        )
    }
    return alertList
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

private fun getUVDescription(index: Int): String = when {
    index <= 2 -> "最弱"
    index <= 4 -> "弱"
    index <= 6 -> "中等"
    index <= 9 -> "强"
    else -> "很强"
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