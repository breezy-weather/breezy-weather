package org.breezyweather.weather

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.basic.models.weather.AirQuality
import org.breezyweather.common.basic.models.weather.Daily
import org.breezyweather.common.basic.models.weather.HalfDay
import org.breezyweather.common.basic.models.weather.Hourly
import org.breezyweather.common.basic.models.weather.Precipitation
import org.breezyweather.common.basic.models.weather.PrecipitationProbability
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.basic.models.weather.UV
import org.breezyweather.common.basic.models.weather.Wind
import org.breezyweather.common.extensions.getFormattedDate
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.sin

/**
 * Helps complete a half day with information from hourly list.
 * Mainly used by providers which don’t provide half days but only full days.
 * Currently helps completing:
 * - Weather code (at 12:00 for day, at 00:00 for night)
 * - Weather text/phase (at 12:00 for day, at 00:00 for night)
 * - Temperature (temperature and windChill, can be expanded if required)
 * - Precipitation (if Precipitation or Precipitation.total is null)
 * - PrecipitationProbability (if PrecipitationProbability or PrecipitationProbability.total is null)
 * - Wind (if Wind or Wind.speed is null)
 * You can expand it to other fields if you need it.
 *
 * @param dailyDate a Date initialized at 00:00 the day of interest
 * @param initialHalfDay the half day to be completed or null
 * @param halfDayHourlyList a List<Hourly> containing only hourlys from 06:00 to 17:59 for day, and 18:00 to 05:59 for night
 * @param isDay true if you want day, false if you want night
 * @return a new List<Daily>, the initial dailyList passed as 1st parameter can be freed after
 */
fun completeHalfDayFromHourlyList(
    dailyDate: Date,
    initialHalfDay: HalfDay? = null,
    halfDayHourlyList: List<Hourly>? = null,
    isDay: Boolean
): HalfDay? {
    if (halfDayHourlyList.isNullOrEmpty()) return initialHalfDay

    var halfDayWeatherText = initialHalfDay?.weatherText
    var halfDayWeatherPhase = initialHalfDay?.weatherPhase
    var halfDayWeatherCode = initialHalfDay?.weatherCode
    var halfDayTemperature = initialHalfDay?.temperature
    var halfDayPrecipitation = initialHalfDay?.precipitation
    var halfDayPrecipitationProbability = initialHalfDay?.precipitationProbability
    val halfDayPrecipitationDuration = initialHalfDay?.precipitationDuration
    var halfDayWind = initialHalfDay?.wind
    val halfDayCloudCover = initialHalfDay?.cloudCover

    // Weather code + Weather text
    if (halfDayWeatherCode == null || halfDayWeatherText == null) {
        // Update at 12:00 on daytime and 00:00 on nighttime
        val halfDayHourlyListWeather = halfDayHourlyList
            .firstOrNull { it.date.time == dailyDate.time + (if (isDay) 12 else 24) * 3600 * 1000 }
            ?: halfDayHourlyList.first() // Use first element in the list to avoid null

        if (halfDayWeatherCode == null) {
            halfDayWeatherCode = halfDayHourlyListWeather.weatherCode
        }
        if (halfDayWeatherPhase == null) {
            halfDayWeatherPhase = halfDayHourlyListWeather.weatherText
        }
        if (halfDayWeatherText == null) {
            halfDayWeatherText = halfDayHourlyListWeather.weatherText
        }
    }

    // Temperature
    if (halfDayTemperature?.temperature == null || halfDayTemperature.windChillTemperature == null) {
        var temperatureTemperature = halfDayTemperature?.temperature
        val temperatureRealFeelTemperature = halfDayTemperature?.realFeelTemperature
        val temperatureRealFeelShaderTemperature = halfDayTemperature?.realFeelShaderTemperature
        val temperatureApparentTemperature = halfDayTemperature?.apparentTemperature
        var temperatureWindChillTemperature = halfDayTemperature?.windChillTemperature
        val temperatureWetBulbTemperature = halfDayTemperature?.wetBulbTemperature
        val temperatureDegreeDayTemperature = halfDayTemperature?.degreeDayTemperature

        if (temperatureTemperature == null) {
            val halfDayHourlyListTemperature = halfDayHourlyList.filter { it.temperature?.temperature != null }
            temperatureTemperature = if (halfDayHourlyListTemperature.isNotEmpty()) {
                if (isDay) {
                    halfDayHourlyListTemperature.maxOf { it.temperature!!.temperature!! }
                } else {
                    halfDayHourlyListTemperature.minOf { it.temperature!!.temperature!! }
                }
            } else null
        }
        if (temperatureWindChillTemperature == null) {
            val halfDayHourlyListWindChillTemperature = halfDayHourlyList.filter { it.temperature?.windChillTemperature != null }
            temperatureWindChillTemperature = if (halfDayHourlyListWindChillTemperature.isNotEmpty()) {
                if (isDay) {
                    halfDayHourlyListWindChillTemperature.maxOf { it.temperature!!.windChillTemperature!! }
                } else {
                    halfDayHourlyListWindChillTemperature.minOf { it.temperature!!.windChillTemperature!! }
                }
            } else null
        }
        halfDayTemperature = Temperature(
            temperature = temperatureTemperature,
            realFeelTemperature = temperatureRealFeelTemperature,
            realFeelShaderTemperature = temperatureRealFeelShaderTemperature,
            apparentTemperature = temperatureApparentTemperature,
            windChillTemperature = temperatureWindChillTemperature,
            wetBulbTemperature = temperatureWetBulbTemperature,
            degreeDayTemperature = temperatureDegreeDayTemperature
        )
    }

    // Precipitation
    if (halfDayPrecipitation?.total == null) {
        val halfDayHourlyListPrecipitationTotal = halfDayHourlyList
            .filter { it.precipitation?.total != null }
        val precipitationTotal = if (halfDayHourlyListPrecipitationTotal.isNotEmpty()) {
            halfDayHourlyListPrecipitationTotal
                .map { it.precipitation!!.total!!.toDouble() }
                .sumOf { it }
        } else null

        val halfDayHourlyListPrecipitationThunderstorm = halfDayHourlyList
            .filter { it.precipitation?.thunderstorm != null }
        val precipitationThunderstorm =
            if (halfDayHourlyListPrecipitationThunderstorm.isNotEmpty()) {
                halfDayHourlyListPrecipitationThunderstorm
                    .map { it.precipitation!!.thunderstorm!!.toDouble() }
                    .sumOf { it }
            } else null

        val halfDayHourlyListPrecipitationRain = halfDayHourlyList
            .filter { it.precipitation?.rain != null }
        val precipitationRain = if (halfDayHourlyListPrecipitationRain.isNotEmpty()) {
            halfDayHourlyListPrecipitationRain
                .map { it.precipitation!!.rain!!.toDouble() }
                .sumOf { it }
        } else null

        val halfDayHourlyListPrecipitationSnow = halfDayHourlyList
            .filter { it.precipitation?.snow != null }
        val precipitationSnow = if (halfDayHourlyListPrecipitationSnow.isNotEmpty()) {
            halfDayHourlyListPrecipitationSnow
                .map { it.precipitation!!.snow!!.toDouble() }
                .sumOf { it }
        } else null

        val halfDayHourlyListPrecipitationIce = halfDayHourlyList
            .filter { it.precipitation?.ice != null }
        val precipitationIce = if (halfDayHourlyListPrecipitationIce.isNotEmpty()) {
            halfDayHourlyListPrecipitationIce
                .map { it.precipitation!!.ice!!.toDouble() }
                .sumOf { it }
        } else null

        halfDayPrecipitation = Precipitation(
            total = precipitationTotal?.toFloat(),
            thunderstorm = precipitationThunderstorm?.toFloat(),
            rain = precipitationRain?.toFloat(),
            snow = precipitationSnow?.toFloat(),
            ice = precipitationIce?.toFloat(),
        )
    }

    // Precipitation probability
    if (halfDayPrecipitationProbability?.total == null) {
        val halfDayHourlyListPrecipitationProbabilityTotal = halfDayHourlyList
            .filter { it.precipitationProbability?.total != null }
        val precipitationProbabilityTotal =
            if (halfDayHourlyListPrecipitationProbabilityTotal.isNotEmpty()) {
                halfDayHourlyListPrecipitationProbabilityTotal
                    .map { it.precipitationProbability!!.total!!.toDouble() }
                    .maxOf { it }
            } else null

        val halfDayHourlyListPrecipitationProbabilityThunderstorm = halfDayHourlyList
            .filter { it.precipitationProbability?.thunderstorm != null }
        val precipitationProbabilityThunderstorm =
            if (halfDayHourlyListPrecipitationProbabilityThunderstorm.isNotEmpty()) {
                halfDayHourlyListPrecipitationProbabilityThunderstorm
                    .map { it.precipitationProbability!!.thunderstorm!!.toDouble() }
                    .maxOf { it }
            } else null

        val halfDayHourlyListPrecipitationProbabilityRain = halfDayHourlyList
            .filter { it.precipitationProbability?.rain != null }
        val precipitationProbabilityRain =
            if (halfDayHourlyListPrecipitationProbabilityRain.isNotEmpty()) {
                halfDayHourlyListPrecipitationProbabilityRain
                    .map { it.precipitationProbability!!.rain!!.toDouble() }
                    .maxOf { it }
            } else null

        val halfDayHourlyListPrecipitationProbabilitySnow = halfDayHourlyList
            .filter { it.precipitationProbability?.snow != null }
        val precipitationProbabilitySnow =
            if (halfDayHourlyListPrecipitationProbabilitySnow.isNotEmpty()) {
                halfDayHourlyListPrecipitationProbabilitySnow
                    .map { it.precipitationProbability!!.snow!!.toDouble() }
                    .maxOf { it }
            } else null

        val halfDayHourlyListPrecipitationProbabilityIce = halfDayHourlyList
            .filter { it.precipitationProbability?.ice != null }
        val precipitationProbabilityIce =
            if (halfDayHourlyListPrecipitationProbabilityIce.isNotEmpty()) {
                halfDayHourlyListPrecipitationProbabilityIce
                    .map { it.precipitationProbability!!.ice!!.toDouble() }
                    .maxOf { it }
            } else null

        halfDayPrecipitationProbability = PrecipitationProbability(
            total = precipitationProbabilityTotal?.toFloat(),
            thunderstorm = precipitationProbabilityThunderstorm?.toFloat(),
            rain = precipitationProbabilityRain?.toFloat(),
            snow = precipitationProbabilitySnow?.toFloat(),
            ice = precipitationProbabilityIce?.toFloat(),
        )
    }

    // Wind
    if (halfDayWind?.speed == null) {
        halfDayHourlyList
            .filter { it.wind?.speed != null }
            .maxByOrNull { it.wind!!.speed!! }
            ?.let {
                halfDayWind = it.wind
            }
    }

    return HalfDay(
        weatherText = halfDayWeatherText,
        weatherPhase = halfDayWeatherPhase,
        weatherCode = halfDayWeatherCode,
        temperature = halfDayTemperature,
        precipitation = halfDayPrecipitation,
        precipitationProbability = halfDayPrecipitationProbability,
        precipitationDuration = halfDayPrecipitationDuration,
        wind = halfDayWind,
        cloudCover = halfDayCloudCover
    )
}

/**
 * Returns an AirQuality object calculated from a List of Hourly for the day
 * (at least 18 non-null Hourly.AirQuality required)
 */
fun getDailyAirQualityFromHourlyList(hourlyList: List<Hourly>? = null): AirQuality? {
    // We need at least 18 hours for a signification estimation
    if (hourlyList.isNullOrEmpty() || hourlyList.size < 18) return null
    val hourlyListWithAirQuality = hourlyList.filter { it.airQuality != null }
    if (hourlyListWithAirQuality.size < 18) return null

    return AirQuality(
        pM25 = hourlyListWithAirQuality.filter { it.airQuality!!.pM25 != null }.map { it.airQuality!!.pM25!! }.average().toFloat(),
        pM10 = hourlyListWithAirQuality.filter { it.airQuality!!.pM10 != null }.map { it.airQuality!!.pM10!! }.average().toFloat(),
        sO2 = hourlyListWithAirQuality.filter { it.airQuality!!.sO2 != null }.map { it.airQuality!!.sO2!! }.average().toFloat(),
        nO2 = hourlyListWithAirQuality.filter { it.airQuality!!.nO2 != null }.map { it.airQuality!!.nO2!! }.average().toFloat(),
        o3 = hourlyListWithAirQuality.filter { it.airQuality!!.o3 != null }.map { it.airQuality!!.o3!! }.average().toFloat(),
        cO = hourlyListWithAirQuality.filter { it.airQuality!!.cO != null }.map { it.airQuality!!.cO!! }.average().toFloat()
    )
}

/**
 * Returns an AirQuality object calculated from a List of Hourly for the day
 * (at least 18 non-null Hourly.AirQuality required)
 */
fun getDailyUVFromHourlyList(context: Context, hourlyList: List<Hourly>? = null): UV? {
    if (hourlyList.isNullOrEmpty()) return null
    val hourlyListWithUV = hourlyList.filter { it.uV?.index != null }
    if (hourlyListWithUV.isEmpty()) return null

    val maxUV = hourlyListWithUV.maxOf { it.uV!!.index!! }
    return UV(
        index = maxUV,
        level = getUVLevel(context, maxUV)
    )
}

/**
 * Completes the isDaylight and/or UV field from a List<Hourly> with the List<Daily>
 * Possible improvement: handle cloud covers as well but doesn't exist in Hourly at the moment
 */
fun completeHourlyListFromDailyList(
    context: Context,
    hourlyList: List<Hourly>,
    dailyList: List<Daily>,
    timeZone: TimeZone,
    completeDaylight: Boolean = false
): List<Hourly> {
    if (hourlyList.isEmpty() || dailyList.isEmpty()) return hourlyList

    val dailyListByDate = dailyList.groupBy { it.date.getFormattedDate(timeZone, "yyyyMMdd") }
    val newHourlyList: MutableList<Hourly> = ArrayList(hourlyList.size)
    hourlyList.forEach { hourly ->
        if (completeDaylight || hourly.isDaylight) {
            val dateForHourFormatted = hourly.date.getFormattedDate(timeZone, "yyyyMMdd")
            dailyListByDate.getOrDefault(dateForHourFormatted, null)
                ?.first()?.let { daily ->
                    if (daily.sun?.riseDate != null && daily.sun.setDate != null) {
                        if (hourly.uV?.index == null && daily.uV?.index != null) {
                            newHourlyList.add(if (completeDaylight) hourly.copy(
                                isDaylight = isDaylight(daily.sun.riseDate, daily.sun.setDate, hourly.date, timeZone),
                                uV = getCurrentUV(context, daily.uV.index, hourly.date, daily.sun.riseDate, daily.sun.setDate, timeZone)
                            ) else hourly.copy(
                                uV = getCurrentUV(context, daily.uV.index, hourly.date, daily.sun.riseDate, daily.sun.setDate, timeZone)
                            ))
                            return@forEach // continue to next item
                        } else if (completeDaylight) {
                            val isDaylight = isDaylight(daily.sun.riseDate, daily.sun.setDate, hourly.date, timeZone)
                            if (hourly.isDaylight != isDaylight) {
                                newHourlyList.add(hourly.copy(
                                    isDaylight = isDaylight
                                ))
                                return@forEach // continue to next item
                            }
                        }
                    }
                }
        }
        newHourlyList.add(hourly)
    }

    return newHourlyList
}

fun getWindLevel(context: Context, speed: Float?): String? {
    return if (speed == null) {
        null
    } else when (speed) {
        in 0f..Wind.WIND_SPEED_0 -> context.getString(R.string.wind_strength_0)
        in Wind.WIND_SPEED_0..Wind.WIND_SPEED_1 -> context.getString(R.string.wind_strength_1)
        in Wind.WIND_SPEED_1..Wind.WIND_SPEED_2 -> context.getString(R.string.wind_strength_2)
        in Wind.WIND_SPEED_2..Wind.WIND_SPEED_3 -> context.getString(R.string.wind_strength_3)
        in Wind.WIND_SPEED_3..Wind.WIND_SPEED_4 -> context.getString(R.string.wind_strength_4)
        in Wind.WIND_SPEED_4..Wind.WIND_SPEED_5 -> context.getString(R.string.wind_strength_5)
        in Wind.WIND_SPEED_5..Wind.WIND_SPEED_6 -> context.getString(R.string.wind_strength_6)
        in Wind.WIND_SPEED_6..Wind.WIND_SPEED_7 -> context.getString(R.string.wind_strength_7)
        in Wind.WIND_SPEED_7..Wind.WIND_SPEED_8 -> context.getString(R.string.wind_strength_8)
        in Wind.WIND_SPEED_8..Wind.WIND_SPEED_9 -> context.getString(R.string.wind_strength_9)
        in Wind.WIND_SPEED_9..Wind.WIND_SPEED_10 -> context.getString(R.string.wind_strength_10)
        in Wind.WIND_SPEED_10..Wind.WIND_SPEED_11 -> context.getString(R.string.wind_strength_11)
        in Wind.WIND_SPEED_11..Float.MAX_VALUE -> context.getString(R.string.wind_strength_12)
        else -> null
    }
}

fun getWindDirection(context: Context, degree: Float?): String? {
    return if (degree == null) {
        null
    } else when(degree) {
        in 0f..22.5f -> context.getString(R.string.wind_direction_short_N)
        in 22.5f..67.5f -> context.getString(R.string.wind_direction_short_NE)
        in 67.5f..112.5f -> context.getString(R.string.wind_direction_short_E)
        in 112.5f..157.5f -> context.getString(R.string.wind_direction_short_SE)
        in 157.5f..202.5f -> context.getString(R.string.wind_direction_short_S)
        in 202.5f..247.5f -> context.getString(R.string.wind_direction_short_SW)
        in 247.5f..292.5f -> context.getString(R.string.wind_direction_short_W)
        in 292.5f..337.5f -> context.getString(R.string.wind_direction_short_NW)
        in 337.5f..360f -> context.getString(R.string.wind_direction_short_N)
        else -> context.getString(R.string.wind_direction_short_variable)
    }
}

fun getMoonPhaseAngle(phase: String?): Int? {
    return if (phase.isNullOrEmpty()) {
        null
    } else when (phase.lowercase(Locale.getDefault())) {
        "waxingcrescent", "waxing crescent" -> 45
        "first", "firstquarter", "first quarter" -> 90
        "waxinggibbous", "waxing gibbous" -> 135
        "full", "fullmoon", "full moon" -> 180
        "waninggibbous", "waning gibbous" -> 225
        "third", "thirdquarter", "third quarter", "last", "lastquarter", "last quarter" -> 270
        "waningcrescent", "waning crescent" -> 315
        else -> 360
    }
}

fun getUVLevel(context: Context, uvIndex: Float?): String? {
    return if (uvIndex == null) {
        null
    } else when (uvIndex) {
        in 0f..UV.UV_INDEX_LOW -> context.getString(R.string.uv_index_0_2)
        in UV.UV_INDEX_LOW..UV.UV_INDEX_MIDDLE -> context.getString(R.string.uv_index_3_5)
        in UV.UV_INDEX_MIDDLE..UV.UV_INDEX_HIGH -> context.getString(R.string.uv_index_6_7)
        in UV.UV_INDEX_HIGH..UV.UV_INDEX_EXCESSIVE -> context.getString(R.string.uv_index_8_10)
        in UV.UV_INDEX_EXCESSIVE..Float.MAX_VALUE -> context.getString(R.string.uv_index_11)
        else -> null
    }
}

fun isDaylight(sunrise: Date?, sunset: Date?, current: Date?, timeZone: TimeZone): Boolean {
    if (sunrise == null || sunset == null || current == null || sunrise.after(sunset)) return true

    val calendar = Calendar.getInstance(timeZone)

    calendar.time = sunrise
    val sunriseTime = calendar[Calendar.HOUR_OF_DAY] * 60 + calendar[Calendar.MINUTE]

    calendar.time = sunset
    val sunsetTime = calendar[Calendar.HOUR_OF_DAY] * 60 + calendar[Calendar.MINUTE]

    calendar.time = current
    val currentTime = calendar[Calendar.HOUR_OF_DAY] * 60 + calendar[Calendar.MINUTE]

    return currentTime in sunriseTime until sunsetTime
}

fun getCurrentUV(
    context: Context,
    dayMaxUV: Float?,
    currentDate: Date?,
    sunriseDate: Date?,
    sunsetDate: Date?,
    timeZone: TimeZone
): UV? {
    if (dayMaxUV == null || currentDate == null || sunriseDate == null || sunsetDate == null ||
        sunriseDate.after(sunsetDate) || currentDate !in sunriseDate..sunsetDate) {
        return null
    }

    // You can visualize formula here: https://www.desmos.com/calculator/lna7dco4zi
    val calendar = Calendar.getInstance(timeZone)

    calendar.time = currentDate
    val currentTime = calendar[Calendar.HOUR_OF_DAY] + calendar[Calendar.MINUTE] / 60f // Approximating to the minute is enough

    calendar.time = sunriseDate
    val sunRiseTime = calendar[Calendar.HOUR_OF_DAY] + calendar[Calendar.MINUTE] / 60f // b in desmos graph

    calendar.time = sunsetDate
    val sunSetTime =  calendar[Calendar.HOUR_OF_DAY] + calendar[Calendar.MINUTE] / 60f // c in desmos graph

    val sunlightDuration = sunSetTime - sunRiseTime // d in desmos graph
    val sunRiseOffset = -Math.PI.toFloat() * sunRiseTime / sunlightDuration // o in desmos graph
    val currentUV =
        dayMaxUV * sin(Math.PI.toFloat() / sunlightDuration * currentTime + sunRiseOffset) // dayMaxUV = a in desmos graph

    val indexUV = if (currentUV < 0) 0f else currentUV

    return UV(
        index = indexUV,
        level = getUVLevel(context, indexUV)
    )
}

fun getHoursOfDay(sunrise: Date?, sunset: Date?): Float? {
    return if (sunrise == null || sunset == null || sunrise.after(sunset)) {
        null
    } else {
        ((sunset.time - sunrise.time) // get delta millisecond.
                / 1000 // second.
                / 60 // minutes.
                / 60.0 // hours.
                ).toFloat()
    }
}
