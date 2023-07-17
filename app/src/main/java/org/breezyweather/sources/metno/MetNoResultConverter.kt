package org.breezyweather.sources.metno

import android.content.Context
import org.breezyweather.BreezyWeather
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.*
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.common.extensions.toTimezoneNoHour
import org.breezyweather.sources.*
import org.breezyweather.sources.metno.json.MetNoForecastResult
import org.breezyweather.sources.metno.json.MetNoSunResult
import org.breezyweather.common.source.WeatherResultWrapper
import org.breezyweather.sources.metno.json.MetNoAirQualityResult
import org.breezyweather.sources.metno.json.MetNoForecastTimeseries
import org.breezyweather.sources.metno.json.MetNoMoonProperties
import org.breezyweather.sources.metno.json.MetNoMoonResult
import org.breezyweather.sources.metno.json.MetNoNowcastResult
import org.breezyweather.sources.metno.json.MetNoSunProperties
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.roundToInt

fun convert(
    context: Context,
    location: Location,
    forecastResult: MetNoForecastResult,
    sunResult: MetNoSunResult,
    moonResult: MetNoMoonResult,
    nowcastResult: MetNoNowcastResult,
    airQualityResult: MetNoAirQualityResult
): WeatherResultWrapper {
    // If the API doesn’t return hourly, consider data as garbage and keep cached data
    if (forecastResult.properties == null
        || forecastResult.properties.timeseries.isNullOrEmpty()) {
        return WeatherResultWrapper(null)
    }

    return try {
        val hourlyByHalfDay: MutableMap<String, Map<String, MutableList<Hourly>>> = HashMap()
        val hourlyList: MutableList<Hourly> = mutableListOf()
        var currentI: Int? = null

        for (i in forecastResult.properties.timeseries.indices) {
            val hourlyForecast = forecastResult.properties.timeseries[i]
            val airQualityIndex = airQualityResult.data?.time?.indexOfFirst { it.from.time == hourlyForecast.time.time }

            val hourly = Hourly(
                date = hourlyForecast.time,
                isDaylight = true, // Will be completed later with daily sunrise/set
                weatherText = null, // TODO: From symbolCode
                weatherCode = getWeatherCode(hourlyForecast.data?.symbolCode),
                temperature = Temperature(
                    temperature = hourlyForecast.data?.instant?.details?.airTemperature,
                ),
                precipitation = Precipitation(
                    total = hourlyForecast.data?.next1Hours?.details?.precipitationAmount
                        ?: hourlyForecast.data?.next6Hours?.details?.precipitationAmount
                        ?: hourlyForecast.data?.next12Hours?.details?.precipitationAmount
                ),
                precipitationProbability = PrecipitationProbability(
                    total = hourlyForecast.data?.next1Hours?.details?.probabilityOfPrecipitation
                        ?: hourlyForecast.data?.next6Hours?.details?.probabilityOfPrecipitation
                        ?: hourlyForecast.data?.next12Hours?.details?.probabilityOfPrecipitation,
                    thunderstorm = hourlyForecast.data?.next1Hours?.details?.probabilityOfThunder
                        ?: hourlyForecast.data?.next6Hours?.details?.probabilityOfThunder
                        ?: hourlyForecast.data?.next12Hours?.details?.probabilityOfThunder
                ),
                wind = if (hourlyForecast.data?.instant?.details != null) Wind(
                    direction = getWindDirection(context, hourlyForecast.data.instant.details.windFromDirection),
                    degree = WindDegree(hourlyForecast.data.instant.details.windFromDirection, false),
                    speed = hourlyForecast.data.instant.details.windSpeed?.times(3.6f),
                    level = getWindLevel(context, hourlyForecast.data.instant.details.windSpeed?.times(3.6f))
                ) else null,
                airQuality = if (airQualityIndex != null && airQualityIndex != -1) AirQuality(
                    pM25 = airQualityResult.data.time.getOrNull(airQualityIndex)?.variables?.pm25Concentration?.value,
                    pM10 = airQualityResult.data.time.getOrNull(airQualityIndex)?.variables?.pm10Concentration?.value,
                    sO2 = airQualityResult.data.time.getOrNull(airQualityIndex)?.variables?.so2Concentration?.value,
                    nO2 = airQualityResult.data.time.getOrNull(airQualityIndex)?.variables?.no2Concentration?.value,
                    o3 = airQualityResult.data.time.getOrNull(airQualityIndex)?.variables?.o3Concentration?.value
                ) else null,
                uV = UV(
                    index = hourlyForecast.data?.instant?.details?.ultravioletIndexClearSky,
                    level = getUVLevel(context, hourlyForecast.data?.instant?.details?.ultravioletIndexClearSky)
                )
            )

            // We shift by 6 hours the hourly date, otherwise nighttime (00:00 to 05:59) would be on the wrong day
            val theDayAtMidnight = Date(hourlyForecast.time.time - (6 * 3600 * 1000))
                .toTimezoneNoHour(location.timeZone)
            val theDayFormatted = theDayAtMidnight?.getFormattedDate(location.timeZone, "yyyy-MM-dd")
            if (theDayFormatted != null) {
                if (!hourlyByHalfDay.containsKey(theDayFormatted)) {
                    hourlyByHalfDay[theDayFormatted] = hashMapOf(
                        "day" to ArrayList(),
                        "night" to ArrayList()
                    )
                }
                if (hourlyForecast.time.time < theDayAtMidnight.time + 18 * 3600 * 1000) {
                    // 06:00 to 17:59 is the day
                    hourlyByHalfDay[theDayFormatted]!!["day"]!!.add(hourly)
                } else {
                    // 18:00 to 05:59 is the night
                    hourlyByHalfDay[theDayFormatted]!!["night"]!!.add(hourly)
                }
            }

            // Add to the app only if starts in the current hour
            if (hourlyForecast.time.time >= System.currentTimeMillis() - 3600 * 1000) {
                if (currentI == null) {
                    currentI = i + 1
                }
                hourlyList.add(hourly)
            }
        }

        val dailyList = getDailyList(context, location.timeZone, sunResult.properties, moonResult.properties, hourlyList, hourlyByHalfDay)

        val currentTimeseries = nowcastResult.properties?.timeseries?.getOrNull(0)?.data
            ?: (if (currentI != null) forecastResult.properties.timeseries.getOrNull(currentI)?.data else null)

        val weather = Weather(
            base = Base(
                cityId = location.cityId,
                // TODO: Use nowcast updatedAt if available
                publishDate = forecastResult.properties.meta?.updatedAt ?: Date()
            ),
            current = Current(
                weatherText = null, // TODO: From symbolCode
                weatherCode = getWeatherCode(currentTimeseries?.symbolCode),
                temperature = Temperature(
                    temperature = currentTimeseries?.instant?.details?.airTemperature,
                ),
                wind = if (currentTimeseries?.instant?.details != null) Wind(
                    direction = getWindDirection(context, currentTimeseries.instant.details.windFromDirection),
                    degree = WindDegree(currentTimeseries.instant.details.windFromDirection, false),
                    speed = currentTimeseries.instant.details.windSpeed?.times(3.6f),
                    level = getWindLevel(context, currentTimeseries.instant.details.windSpeed?.times(3.6f))
                ) else null,
                uV = getCurrentUV(
                    context,
                    dailyList.getOrNull(0)?.uV?.index,
                    Date(),
                    dailyList.getOrNull(0)?.sun?.riseDate,
                    dailyList.getOrNull(0)?.sun?.setDate,
                    location.timeZone
                ),
                airQuality = hourlyList.getOrNull(1)?.airQuality,
                relativeHumidity = currentTimeseries?.instant?.details?.relativeHumidity,
                pressure = if (currentI != null)
                    forecastResult.properties.timeseries.getOrNull(currentI)?.data?.instant?.details?.airPressureAtSeaLevel
                else null,
                dewPoint = if (currentI != null)
                    forecastResult.properties.timeseries.getOrNull(currentI)?.data?.instant?.details?.dewPointTemperature
                else null
            ),
            dailyForecast = dailyList,
            hourlyForecast = completeHourlyListFromDailyList(context, hourlyList, dailyList, location.timeZone, completeDaylight = true),
            minutelyForecast = getMinutelyList(nowcastResult.properties?.timeseries)
        )
        WeatherResultWrapper(weather)
    } catch (e: Exception) {
        if (BreezyWeather.instance.debugMode) {
            e.printStackTrace()
        }
        WeatherResultWrapper(null)
    }
}

private fun getDailyList(
    context: Context,
    timeZone: TimeZone,
    sunResult: MetNoSunProperties?,
    moonResult: MetNoMoonProperties?,
    hourlyList: List<Hourly>,
    hourlyListByHalfDay: Map<String, Map<String, MutableList<Hourly>>>
): List<Daily> {
    val dailyList: MutableList<Daily> = ArrayList()
    val hourlyListByDay = hourlyList.groupBy { it.date.getFormattedDate(timeZone, "yyyy-MM-dd") }
    hourlyListByDay.entries.forEachIndexed { i, day ->
        val dayDate = day.key.toDateNoHour(timeZone)
        if (dayDate != null) {
            dailyList.add(
                Daily(
                    date = dayDate,
                    day = completeHalfDayFromHourlyList(
                        dailyDate = dayDate,
                        initialHalfDay = null,
                        halfDayHourlyList = hourlyListByHalfDay.getOrDefault(day.key, null)?.get("day"),
                        isDay = true
                    ),
                    night = completeHalfDayFromHourlyList(
                        dailyDate = dayDate,
                        initialHalfDay = null,
                        halfDayHourlyList = hourlyListByHalfDay.getOrDefault(day.key, null)?.get("night"),
                        isDay = false
                    ),
                    sun = if (i == 0) Astro(
                        riseDate = sunResult?.sunrise?.time,
                        setDate = sunResult?.sunset?.time,
                    ) else null,
                    moon = if (i == 0) Astro(
                        riseDate = moonResult?.moonrise?.time,
                        setDate = moonResult?.moonset?.time,
                    ) else null,
                    moonPhase = if (i == 0) MoonPhase(
                        angle = moonResult?.moonphase?.roundToInt()
                    ) else null,
                    airQuality = getDailyAirQualityFromHourlyList(hourlyListByDay.getOrDefault(day.key, null)),
                    uV = getDailyUVFromHourlyList(context, day.value),
                    hoursOfSun = if (i == 0) getHoursOfDay(sunResult?.sunrise?.time, sunResult?.sunset?.time) else null
                )
            )
        }
    }
    return dailyList
}

private fun getMinutelyList(nowcastTimeseries: List<MetNoForecastTimeseries>?): List<Minutely> {
    val minutelyList: MutableList<Minutely> = arrayListOf()
    if (nowcastTimeseries.isNullOrEmpty()) return minutelyList

    nowcastTimeseries.forEachIndexed { i, nowcastForecast ->
        minutelyList.add(
            Minutely(
                nowcastForecast.time,
                if (i < nowcastTimeseries.size - 1) {
                    ((nowcastTimeseries[i + 1].time.time - nowcastForecast.time.time) / (60 * 1000)).toDouble()
                        .roundToInt()
                } else ((nowcastForecast.time.time - nowcastTimeseries[i - 1].time.time) / (60 * 1000)).toDouble()
                    .roundToInt(),
                nowcastForecast.data?.instant?.details?.precipitationRate?.toDouble()
            )
        )
    }

    return minutelyList
}

private fun getWeatherCode(icon: String?): WeatherCode? {
    return if (icon == null) {
        null
    } else when(icon.replace("_night", "").replace("_day", "")) {
        "clearsky", "fair" -> WeatherCode.CLEAR
        "partlycloudy" -> WeatherCode.PARTLY_CLOUDY
        "cloudy" -> WeatherCode.CLOUDY
        "fog" -> WeatherCode.FOG
        "heavyrain", "heavyrainshowers", "lightrain", "lightrainshowers", "rain", "rainshowers" -> WeatherCode.RAIN
        "heavyrainandthunder", "heavyrainshowersandthunder", "heavysleetandthunder", "heavysleetshowersandthunder",
        "heavysnowandthunder", "heavysnowshowersandthunder", "lightrainandthunder", "lightrainshowersandthunder",
        "lightsleetandthunder", "lightsleetshowersandthunder", "lightsnowandthunder", "lightsnowshowersandthunder",
        "rainandthunder", "rainshowersandthunder", "sleetandthunder", "sleetshowersandthunder", "snowandthunder",
        "snowshowersandthunder" -> WeatherCode.THUNDERSTORM
        "heavysnow", "heavysnowshowers", "lightsnow", "lightsnowshowers", "snow", "snowshowers" -> WeatherCode.SNOW
        "heavysleet", "heavysleetshowers", "lightsleet", "lightsleetshowers", "sleet",
        "sleetshowers" -> WeatherCode.SLEET
        else -> null
    }
}
