package org.breezyweather.weather.china

import android.content.Context
import androidx.annotation.ColorInt
import org.breezyweather.BreezyWeather.Companion.instance
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.*
import org.breezyweather.common.utils.DisplayUtils
import org.breezyweather.weather.getHoursOfDay
import org.breezyweather.weather.getWindDirection
import org.breezyweather.weather.getWindLevel
import org.breezyweather.weather.isDaylight
import org.breezyweather.weather.china.json.ChinaForecastResult
import org.breezyweather.weather.china.json.ChinaMinutelyResult
import org.breezyweather.weather.WeatherService.WeatherResultWrapper
import java.util.*
import kotlin.collections.ArrayList

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
                    temperature = forecastResult.current.temperature?.value?.toInt(),
                    apparentTemperature = forecastResult.current.feelsLike?.value?.toInt()
                ),
                wind = if (forecastResult.current.wind != null) Wind(
                    direction = getWindDirection(context, forecastResult.current.wind.direction?.value?.toFloat()),
                    degree = WindDegree(
                        degree = forecastResult.current.wind.direction?.value?.toFloat(),
                        isNoDirection = false
                    ),
                    speed = forecastResult.current.wind.speed?.value?.toFloat(),
                    level = getWindLevel(context, forecastResult.current.wind.speed?.value?.toFloat())
                ) else null,
                uV = if (forecastResult.current.uvIndex != null) UV(
                    index = forecastResult.current.uvIndex.toInt(),
                    description = getUVDescription(forecastResult.current.uvIndex.toInt())
                ) else null,
                airQuality = getAirQuality(forecastResult),
                relativeHumidity = if (!forecastResult.current.humidity?.value.isNullOrEmpty()) forecastResult.current.humidity!!.value!!.toFloat() else null,
                pressure = if (!forecastResult.current.pressure?.value.isNullOrEmpty()) forecastResult.current.pressure!!.value!!.toFloat() else null,
                visibility = if (!forecastResult.current.visibility?.value.isNullOrEmpty()) forecastResult.current.visibility!!.value!!.toFloat() else null,
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
                getWeatherText(forecastResult.current.weather),
                getWeatherCode(forecastResult.current.weather),
                minutelyResult
            ),
            alertList = getAlertList(forecastResult)
        )
        WeatherResultWrapper(weather)
    } catch (e: Exception) {
        if (instance.debugMode) {
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
            daytimeTemperature = if (!result.yesterday.tempMax.isNullOrEmpty()) result.yesterday.tempMax.toInt() else null,
            nighttimeTemperature = if (!result.yesterday.tempMin.isNullOrEmpty()) result.yesterday.tempMin.toInt() else null,
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
        val calendar = DisplayUtils.toCalendarWithTimeZone(publishDate, timeZone)
        calendar.add(Calendar.DATE, index) // FIXME: Wrong TimeZone for the first item
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        dailyList.add(
            Daily(
                date = calendar.time,
                day = HalfDay(
                    weatherText = getWeatherText(weather.from),
                    weatherPhase = getWeatherText(weather.from),
                    weatherCode = getWeatherCode(weather.from),
                    temperature = Temperature(
                        temperature = dailyForecast.temperature?.value?.getOrNull(index)?.from?.toInt()
                    ),
                    precipitationProbability = PrecipitationProbability(
                        total = getPrecipitationProbability(dailyForecast, index)
                    ),
                    wind = if (dailyForecast.wind != null) Wind(
                        direction = getWindDirection(context, dailyForecast.wind.direction?.value?.getOrNull(index)?.from?.toFloat()),
                        degree = WindDegree(
                            degree = dailyForecast.wind.direction?.value?.getOrNull(index)?.from?.toFloat(),
                            isNoDirection = false
                        ),
                        speed = dailyForecast.wind.speed?.value?.getOrNull(index)?.from?.toFloat(),
                        level = getWindLevel(context, dailyForecast.wind.speed?.value?.getOrNull(index)?.from?.toFloat())
                    ) else null
                ),
                night = HalfDay(
                    weatherText = getWeatherText(weather.to),
                    weatherPhase = getWeatherText(weather.to),
                    weatherCode = getWeatherCode(weather.to),
                    temperature = Temperature(
                        temperature = dailyForecast.temperature?.value?.getOrNull(index)?.to?.toInt()
                    ),
                    precipitationProbability = PrecipitationProbability(
                        total = getPrecipitationProbability(dailyForecast, index)
                    ),
                    wind = if (dailyForecast.wind != null) Wind(
                        direction = getWindDirection(context, dailyForecast.wind.direction?.value?.getOrNull(index)?.to?.toFloat()),
                        degree = WindDegree(
                            degree = dailyForecast.wind.direction?.value?.getOrNull(index)?.to?.toFloat(),
                            isNoDirection = false
                        ),
                        speed = dailyForecast.wind.speed?.value?.getOrNull(index)?.to?.toFloat(),
                        level = getWindLevel(context, dailyForecast.wind.speed?.value?.getOrNull(index)?.to?.toFloat())
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

    return try {
        forecast.precipitationProbability.value.getOrNull(index)?.toFloat()
    } catch (ignore: Exception) {
        null
    }
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
        val calendar = DisplayUtils.toCalendarWithTimeZone(publishDate, timeZone)
        calendar.add(Calendar.HOUR_OF_DAY, index) // FIXME: Wrong TimeZone for the first item
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        val date = calendar.time
        hourlyList.add(
            Hourly(
                date = date,
                isDaylight = isDaylight(sunrise, sunset, date, timeZone),
                weatherText = getWeatherText(weather.toString()),
                weatherCode = getWeatherCode(weather.toString()),
                temperature = Temperature(
                    temperature = hourlyForecast.temperature?.value?.getOrNull(index)
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
    currentWeatherText: String,
    currentWeatherCode: WeatherCode?,
    minutelyResult: ChinaMinutelyResult
): List<Minutely> {
    if (minutelyResult.precipitation == null || minutelyResult.precipitation.value.isNullOrEmpty()) return emptyList()

    val current = minutelyResult.precipitation.pubTime
    val minutelyList: MutableList<Minutely> = ArrayList(minutelyResult.precipitation.value.size)

    minutelyResult.precipitation.value.forEach { precipitation ->
        val calendar = DisplayUtils.toCalendarWithTimeZone(current, timeZone)
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        minutelyList.add(
            Minutely(
                calendar.time,
                getMinuteWeatherText(
                    precipitation,
                    currentWeatherText,
                    currentWeatherCode
                ),
                getMinuteWeatherCode(
                    precipitation,
                    currentWeatherCode
                ),
                1,
                null as Int?,
                null
            )
        )
    }
    return minutelyList
}

private fun getMinuteWeatherText(
    precipitation: Double,
    currentWeatherText: String,
    currentWeatherCode: WeatherCode?
): String {
    return if (precipitation > 0) {
        if (isPrecipitation(currentWeatherCode)) currentWeatherText else "阴"
    } else {
        if (isPrecipitation(currentWeatherCode)) "阴" else currentWeatherText
    }
}

private fun getMinuteWeatherCode(
    precipitation: Double,
    currentWeatherCode: WeatherCode?
): WeatherCode? {
    return if (precipitation > 0) {
        if (isPrecipitation(currentWeatherCode)) currentWeatherCode else WeatherCode.CLOUDY
    } else {
        if (isPrecipitation(currentWeatherCode)) WeatherCode.CLOUDY else currentWeatherCode
    }
}

private fun isPrecipitation(code: WeatherCode?): Boolean {
    return code !== null && (code === WeatherCode.RAIN || code === WeatherCode.SNOW || code === WeatherCode.HAIL
            || code === WeatherCode.SLEET || code === WeatherCode.THUNDERSTORM)
}

private fun getAlertList(result: ChinaForecastResult): List<Alert> {
    if (result.alerts.isNullOrEmpty()) return emptyList()

    val alertList: MutableList<Alert> = ArrayList(result.alerts.size)
    result.alerts.forEach { alert ->
        alertList.add(
            Alert(
                alert.pubTime?.time
                    ?: System.currentTimeMillis(), // TODO: Avoid having the same ID for two different alerts happening at the same time
                alert.pubTime,
                null,
                alert.title,
                alert.detail,
                alert.type,
                getAlertPriority(alert.level)
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

private fun getUVDescription(index: Int): String {
    return when {
        index <= 2 -> "最弱"
        index <= 4 -> "弱"
        index <= 6 -> "中等"
        index <= 9 -> "强"
        else -> "很强"
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