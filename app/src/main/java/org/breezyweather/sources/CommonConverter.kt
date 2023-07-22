package org.breezyweather.sources

import org.breezyweather.common.basic.models.weather.AirQuality
import org.breezyweather.common.basic.models.weather.Current
import org.breezyweather.common.basic.models.weather.Daily
import org.breezyweather.common.basic.models.weather.HalfDay
import org.breezyweather.common.basic.models.weather.Hourly
import org.breezyweather.common.basic.models.weather.Precipitation
import org.breezyweather.common.basic.models.weather.PrecipitationProbability
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.basic.models.weather.UV
import org.breezyweather.common.basic.models.weather.Wind
import org.breezyweather.common.basic.wrappers.HourlyWrapper
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.toTimezoneNoHour
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Functions used directly in weather sources
 */
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

/**
 * DAILY FROM HOURLY
 */

/**
 * Completes daily data from hourly data:
 * - HalfDay (day and night)
 * - Air quality
 * - UV
 * - Hours of sun
 * TODO: Calculate sun & moon & moon phase https://github.com/shred/commons-suncalc
 * TODO: Calculate degree day
 * TODO: Calculate dewpoint from humidity
 *
 * @param dailyList daily data
 * @param hourlyList hourly data
 * @param timeZone timeZone of the location
 */
fun completeDailyListFromHourlyList(
    dailyList: List<Daily>,
    hourlyList: List<HourlyWrapper>,
    timeZone: TimeZone
): List<Daily> {
    if (dailyList.isEmpty() || hourlyList.isEmpty()) return dailyList

    val newDailyList = mutableListOf<Daily>()
    val hourlyListByHalfDay = getHourlyListByHalfDay(hourlyList, timeZone)
    val hourlyListByDay = hourlyList.groupBy { it.date.getFormattedDate(timeZone, "yyyy-MM-dd") }
    dailyList.forEach { daily ->
        val theDayFormatted = daily.date.getFormattedDate(timeZone, "yyyy-MM-dd")
        newDailyList.add(
            daily.copy(
                day = completeHalfDayFromHourlyList(
                    dailyDate = daily.date,
                    initialHalfDay = daily.day,
                    halfDayHourlyList = hourlyListByHalfDay.getOrDefault(theDayFormatted, null)?.get("day"),
                    isDay = true
                ),
                night = completeHalfDayFromHourlyList(
                    dailyDate = daily.date,
                    initialHalfDay = daily.night,
                    halfDayHourlyList = hourlyListByHalfDay.getOrDefault(theDayFormatted, null)?.get("night"),
                    isDay = false
                ),
                airQuality = daily.airQuality ?: getDailyAirQualityFromHourlyList(
                    hourlyListByDay.getOrDefault(theDayFormatted, null)
                ),
                uV = if (daily.uV?.index != null) daily.uV else getDailyUVFromHourlyList(
                    hourlyListByDay.getOrDefault(theDayFormatted, null)
                ),
                hoursOfSun = daily.hoursOfSun ?: getHoursOfDay(
                    daily.sun?.riseDate,
                    daily.sun?.setDate
                )
            )
        )
    }

    return newDailyList
}

private fun getHourlyListByHalfDay(
    hourlyList: List<HourlyWrapper>,
    timeZone: TimeZone
): MutableMap<String, Map<String, MutableList<HourlyWrapper>>> {
    val hourlyByHalfDay: MutableMap<String, Map<String, MutableList<HourlyWrapper>>> = HashMap()

    hourlyList.forEach { hourly ->
        // We shift by 6 hours the hourly date, otherwise nighttime (00:00 to 05:59) would be on the wrong day
        val theDayAtMidnight = Date(hourly.date.time - (6 * 3600 * 1000))
            .toTimezoneNoHour(timeZone)
        val theDayFormatted = theDayAtMidnight?.getFormattedDate(timeZone, "yyyy-MM-dd")
        if (theDayFormatted != null) {
            if (!hourlyByHalfDay.containsKey(theDayFormatted)) {
                hourlyByHalfDay[theDayFormatted] = hashMapOf(
                    "day" to ArrayList(),
                    "night" to ArrayList()
                )
            }
            if (hourly.date.time < theDayAtMidnight.time + 18 * 3600 * 1000) {
                // 06:00 to 17:59 is the day
                hourlyByHalfDay[theDayFormatted]!!["day"]!!.add(hourly)
            } else {
                // 18:00 to 05:59 is the night
                hourlyByHalfDay[theDayFormatted]!!["night"]!!.add(hourly)
            }
        }
    }

    return hourlyByHalfDay
}

/**
 * Helps complete a half day with information from hourly list.
 * Mainly used by providers which don’t provide half days but only full days.
 * Currently helps completing:
 * - Weather code (at 12:00 for day, at 00:00 for night)
 * - Weather text/phase (at 12:00 for day, at 00:00 for night)
 * - Temperature (temperature, apparent, windChill and wetBulb)
 * - Precipitation (if Precipitation or Precipitation.total is null)
 * - PrecipitationProbability (if PrecipitationProbability or PrecipitationProbability.total is null)
 * - Wind (if Wind or Wind.speed is null)
 * - CloudCover (average)
 *
 * @param dailyDate a Date initialized at 00:00 the day of interest
 * @param initialHalfDay the half day to be completed or null
 * @param halfDayHourlyList a List<Hourly> containing only hourlys from 06:00 to 17:59 for day, and 18:00 to 05:59 for night
 * @param isDay true if you want day, false if you want night
 * @return a new List<Daily>, the initial dailyList passed as 1st parameter can be freed after
 */
private fun completeHalfDayFromHourlyList(
    dailyDate: Date,
    initialHalfDay: HalfDay? = null,
    halfDayHourlyList: List<HourlyWrapper>? = null,
    isDay: Boolean
): HalfDay? {
    if (halfDayHourlyList.isNullOrEmpty()) return initialHalfDay

    val newHalfDay = initialHalfDay ?: HalfDay()

    var halfDayWeatherText = newHalfDay.weatherText
    var halfDayWeatherPhase = newHalfDay.weatherPhase
    var halfDayWeatherCode = newHalfDay.weatherCode

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

    return newHalfDay.copy(
        weatherText = halfDayWeatherText,
        weatherPhase = halfDayWeatherPhase,
        weatherCode = halfDayWeatherCode,
        temperature = if (newHalfDay.temperature?.temperature == null
            || newHalfDay.temperature.apparentTemperature == null
            || newHalfDay.temperature.windChillTemperature == null
            || newHalfDay.temperature.wetBulbTemperature == null) {
            getHalfDayTemperatureFromHourlyList(newHalfDay.temperature, halfDayHourlyList, isDay)
        } else newHalfDay.temperature,
        precipitation = if (newHalfDay.precipitation?.total == null) {
            getHalfDayPrecipitationFromHourlyList(halfDayHourlyList)
        } else newHalfDay.precipitation,
        precipitationProbability = if (newHalfDay.precipitationProbability?.total == null) {
           getHalfDayPrecipitationProbabilityFromHourlyList(halfDayHourlyList)
        } else newHalfDay.precipitationProbability,
        wind = if (newHalfDay.wind?.speed == null) {
           getHalfDayWindFromHourlyList(halfDayHourlyList)
        } else newHalfDay.wind,
        cloudCover = newHalfDay.cloudCover ?: getHalfDayCloudCoverFromHourlyList(halfDayHourlyList)
    )
}

/**
 * We complete everything except RealFeel and RealFeelShade which are AccuWeather-specific
 * and that we know is already completed
 */
private fun getHalfDayTemperatureFromHourlyList(
    initialTemperature: Temperature?,
    halfDayHourlyList: List<HourlyWrapper>,
    isDay: Boolean
): Temperature {
    val newTemperature = initialTemperature ?: Temperature()

    var temperatureTemperature = newTemperature.temperature
    var temperatureApparentTemperature = newTemperature.apparentTemperature
    var temperatureWindChillTemperature = newTemperature.windChillTemperature
    var temperatureWetBulbTemperature = newTemperature.wetBulbTemperature

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
    if (temperatureApparentTemperature == null) {
        val halfDayHourlyListApparentTemperature = halfDayHourlyList.filter { it.temperature?.apparentTemperature != null }
        temperatureApparentTemperature = if (halfDayHourlyListApparentTemperature.isNotEmpty()) {
            if (isDay) {
                halfDayHourlyListApparentTemperature.maxOf { it.temperature!!.apparentTemperature!! }
            } else {
                halfDayHourlyListApparentTemperature.minOf { it.temperature!!.apparentTemperature!! }
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
    if (temperatureWetBulbTemperature == null) {
        val halfDayHourlyListWetBulbTemperature = halfDayHourlyList.filter { it.temperature?.wetBulbTemperature != null }
        temperatureWetBulbTemperature = if (halfDayHourlyListWetBulbTemperature.isNotEmpty()) {
            if (isDay) {
                halfDayHourlyListWetBulbTemperature.maxOf { it.temperature!!.wetBulbTemperature!! }
            } else {
                halfDayHourlyListWetBulbTemperature.minOf { it.temperature!!.wetBulbTemperature!! }
            }
        } else null
    }
    return newTemperature.copy(
        temperature = temperatureTemperature,
        apparentTemperature = temperatureApparentTemperature,
        windChillTemperature = temperatureWindChillTemperature,
        wetBulbTemperature = temperatureWetBulbTemperature
    )
}

private fun getHalfDayPrecipitationFromHourlyList(
    halfDayHourlyList: List<HourlyWrapper>
): Precipitation {
    val halfDayHourlyListPrecipitationTotal = halfDayHourlyList
        .filter { it.precipitation?.total != null }
        .map { it.precipitation!!.total!!.toDouble() }
    val halfDayHourlyListPrecipitationThunderstorm = halfDayHourlyList
        .filter { it.precipitation?.thunderstorm != null }
        .map { it.precipitation!!.thunderstorm!!.toDouble() }
    val halfDayHourlyListPrecipitationRain = halfDayHourlyList
        .filter { it.precipitation?.rain != null }
        .map { it.precipitation!!.rain!!.toDouble() }
    val halfDayHourlyListPrecipitationSnow = halfDayHourlyList
        .filter { it.precipitation?.snow != null }
        .map { it.precipitation!!.snow!!.toDouble() }
    val halfDayHourlyListPrecipitationIce = halfDayHourlyList
        .filter { it.precipitation?.ice != null }
        .map { it.precipitation!!.ice!!.toDouble() }

    return Precipitation(
        total = if (halfDayHourlyListPrecipitationTotal.isNotEmpty()) {
            halfDayHourlyListPrecipitationTotal.sumOf { it }.toFloat()
        } else null,
        thunderstorm = if (halfDayHourlyListPrecipitationThunderstorm.isNotEmpty()) {
            halfDayHourlyListPrecipitationThunderstorm.sumOf { it }.toFloat()
        } else null,
        rain = if (halfDayHourlyListPrecipitationRain.isNotEmpty()) {
            halfDayHourlyListPrecipitationRain.sumOf { it }.toFloat()
        } else null,
        snow = if (halfDayHourlyListPrecipitationSnow.isNotEmpty()) {
            halfDayHourlyListPrecipitationSnow.sumOf { it }.toFloat()
        } else null,
        ice = if (halfDayHourlyListPrecipitationIce.isNotEmpty()) {
            halfDayHourlyListPrecipitationIce.sumOf { it }.toFloat()
        } else null
    )
}

private fun getHalfDayPrecipitationProbabilityFromHourlyList(
    halfDayHourlyList: List<HourlyWrapper>
): PrecipitationProbability {
    val halfDayHourlyListPrecipitationProbabilityTotal = halfDayHourlyList
        .filter { it.precipitationProbability?.total != null }
        .map { it.precipitationProbability!!.total!!.toDouble() }
    val halfDayHourlyListPrecipitationProbabilityThunderstorm = halfDayHourlyList
        .filter { it.precipitationProbability?.thunderstorm != null }
        .map { it.precipitationProbability!!.thunderstorm!!.toDouble() }
    val halfDayHourlyListPrecipitationProbabilityRain = halfDayHourlyList
        .filter { it.precipitationProbability?.rain != null }
        .map { it.precipitationProbability!!.rain!!.toDouble() }
    val halfDayHourlyListPrecipitationProbabilitySnow = halfDayHourlyList
        .filter { it.precipitationProbability?.snow != null }
        .map { it.precipitationProbability!!.snow!!.toDouble() }
    val halfDayHourlyListPrecipitationProbabilityIce = halfDayHourlyList
        .filter { it.precipitationProbability?.ice != null }
        .map { it.precipitationProbability!!.ice!!.toDouble() }

    return PrecipitationProbability(
        total = if (halfDayHourlyListPrecipitationProbabilityTotal.isNotEmpty()) {
            halfDayHourlyListPrecipitationProbabilityTotal.maxOf { it }.toFloat()
        } else null,
        thunderstorm = if (halfDayHourlyListPrecipitationProbabilityThunderstorm.isNotEmpty()) {
            halfDayHourlyListPrecipitationProbabilityThunderstorm.maxOf { it }.toFloat()
        } else null,
        rain = if (halfDayHourlyListPrecipitationProbabilityRain.isNotEmpty()) {
            halfDayHourlyListPrecipitationProbabilityRain.maxOf { it }.toFloat()
        } else null,
        snow = if (halfDayHourlyListPrecipitationProbabilitySnow.isNotEmpty()) {
            halfDayHourlyListPrecipitationProbabilitySnow.maxOf { it }.toFloat()
        } else null,
        ice = if (halfDayHourlyListPrecipitationProbabilityIce.isNotEmpty()) {
            halfDayHourlyListPrecipitationProbabilityIce.maxOf { it }.toFloat()
        } else null,
    )
}

private fun getHalfDayWindFromHourlyList(
    halfDayHourlyList: List<HourlyWrapper>
): Wind? {
    return halfDayHourlyList
        .filter { it.wind?.speed != null }
        .maxByOrNull { it.wind!!.speed!! }
        ?.wind
}

private fun getHalfDayCloudCoverFromHourlyList(
    halfDayHourlyList: List<HourlyWrapper>
): Int? {
    val halfDayHourlyListCloudCover = halfDayHourlyList
        .filter { it.cloudCover != null }
        .map { it.cloudCover!! }

    return if (halfDayHourlyListCloudCover.isNotEmpty()) {
        halfDayHourlyListCloudCover.average().roundToInt()
    } else null
}

/**
 * Returns an AirQuality object calculated from a List of Hourly for the day
 * (at least 18 non-null Hourly.AirQuality required)
 */
private fun getDailyAirQualityFromHourlyList(hourlyList: List<HourlyWrapper>? = null): AirQuality? {
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
 * Returns the max UV for the day from a list of hourly
 */
private fun getDailyUVFromHourlyList(hourlyList: List<HourlyWrapper>? = null): UV? {
    if (hourlyList.isNullOrEmpty()) return null
    val hourlyListWithUV = hourlyList.filter { it.uV?.index != null }
    if (hourlyListWithUV.isEmpty()) return null

    return UV(index = hourlyListWithUV.maxOf { it.uV!!.index!! })
}

private fun getHoursOfDay(sunrise: Date?, sunset: Date?): Float? {
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

/**
 * HOURLY FROM DAILY
 */

/**
 * Completes hourly data from daily data:
 * - isDaylight
 * - UV field
 * TODO: Calculate dewpoint from humidity
 *
 * Also removes hourlys in the past (30 min margin)
 *
 * @param hourlyList hourly data
 * @param dailyList daily data
 * @param timeZone timeZone of the location
 */
fun completeHourlyListFromDailyList(
    hourlyList: List<HourlyWrapper>,
    dailyList: List<Daily>,
    timeZone: TimeZone
): List<Hourly> {
    val dailyListByDate = dailyList.groupBy { it.date.getFormattedDate(timeZone, "yyyyMMdd") }
    val newHourlyList: MutableList<Hourly> = ArrayList(hourlyList.size)
    hourlyList.forEach { hourly ->
        // Only keep hours in the future with a 30 min margin
        // Example: 15:29 -> starts at 15:00, 15:31 -> starts at 16:00
        if (hourly.date.time >= System.currentTimeMillis() - (30 * 60 * 1000)) {
            val dateForHourFormatted = hourly.date.getFormattedDate(timeZone, "yyyyMMdd")
            dailyListByDate.getOrDefault(dateForHourFormatted, null)
                ?.first()?.let { daily ->
                    val isDaylight = hourly.isDaylight ?: isDaylight(
                        daily.sun?.riseDate,
                        daily.sun?.setDate,
                        hourly.date
                    )
                    newHourlyList.add(
                        hourly.copyToHourly(
                            isDaylight = isDaylight,
                            uV = if (hourly.uV?.index != null) hourly.uV else getCurrentUVFromDayMax(
                                daily.uV?.index,
                                hourly.date,
                                daily.sun?.riseDate,
                                daily.sun?.setDate,
                                timeZone
                            )
                        )
                    )
                    return@forEach // continue to next item
                }
            newHourlyList.add(hourly.copyToHourly())
        }
    }

    return newHourlyList
}

/**
 * From sunrise and sunset times, returns true if current time is daytime
 * Will return true if any data is incoherent
 */
private fun isDaylight(sunrise: Date?, sunset: Date?, current: Date?): Boolean {
    if (sunrise == null || sunset == null || current == null || sunrise.after(sunset)) return true

    return current.time in sunrise.time until sunset.time
}

/**
 * Returns an estimated UV index for current time from max UV of the day
 */
private fun getCurrentUVFromDayMax(
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

    return UV(index = indexUV)
}

/**
 * CURRENT FROM DAILY AND HOURLY LIST
 */

/**
 * Completes Current object from today daily and hourly data:
 * - Weather text
 * - Weather code
 * - Temperature
 * - Wind
 * - UV
 * - Air quality
 * - Relative humidity
 * - Dew point
 * - Pressure
 * - Cloud cover
 * - Visibility
 *
 * @param hourly hourly data for the first hour
 * @param todayDaily daily for today
 * @param timeZone timeZone of the location
 */
fun completeCurrentFromTodayDailyAndHourly(
    initialCurrent: Current?,
    hourly: Hourly?,
    todayDaily: Daily?,
    timeZone: TimeZone
): Current {
    val newCurrent = initialCurrent ?: Current()
    if (hourly == null) {
        return newCurrent.copy(
            uV = if (newCurrent.uV?.index == null && todayDaily != null) getCurrentUVFromDayMax(
                todayDaily.uV?.index,
                Date(),
                todayDaily.sun?.riseDate,
                todayDaily.sun?.setDate,
                timeZone
            ) else newCurrent.uV
        )
    }

    return newCurrent.copy(
        weatherText = newCurrent.weatherText ?: hourly.weatherText,
        weatherCode = newCurrent.weatherCode ?: hourly.weatherCode,
        temperature = completeCurrentTemperatureFromHourly(newCurrent.temperature, hourly.temperature),
        wind = if (newCurrent.wind?.speed != null || hourly.wind?.speed == null) {
            newCurrent.wind
        } else hourly.wind,
        uV = if (newCurrent.uV?.index == null && todayDaily != null) getCurrentUVFromDayMax(
            todayDaily.uV?.index,
            Date(),
            todayDaily.sun?.riseDate,
            todayDaily.sun?.setDate,
            timeZone
        ) else newCurrent.uV,
        airQuality = newCurrent.airQuality ?: hourly.airQuality,
        relativeHumidity = newCurrent.relativeHumidity ?: hourly.relativeHumidity,
        dewPoint = newCurrent.dewPoint ?: hourly.dewPoint,
        pressure = newCurrent.pressure ?: hourly.pressure,
        cloudCover = newCurrent.cloudCover ?: hourly.cloudCover,
        visibility = newCurrent.visibility ?: hourly.visibility
    )
}

private fun completeCurrentTemperatureFromHourly(
    initialTemperature: Temperature?,
    hourlyTemperature: Temperature?
): Temperature? {
    if (hourlyTemperature == null) return initialTemperature
    val newTemperature = initialTemperature ?: Temperature()

    return newTemperature.copy(
        temperature = newTemperature.temperature ?: hourlyTemperature.temperature,
        realFeelTemperature = newTemperature.realFeelTemperature ?: hourlyTemperature.realFeelTemperature,
        realFeelShaderTemperature = newTemperature.realFeelShaderTemperature ?: hourlyTemperature.realFeelShaderTemperature,
        apparentTemperature = newTemperature.apparentTemperature ?: hourlyTemperature.apparentTemperature,
        windChillTemperature = newTemperature.windChillTemperature ?: hourlyTemperature.windChillTemperature,
        wetBulbTemperature = newTemperature.wetBulbTemperature ?: hourlyTemperature.wetBulbTemperature,
    )
}