package org.breezyweather.sources

import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.AirQuality
import org.breezyweather.common.basic.models.weather.Allergen
import org.breezyweather.common.basic.models.weather.Astro
import org.breezyweather.common.basic.models.weather.Current
import org.breezyweather.common.basic.models.weather.Daily
import org.breezyweather.common.basic.models.weather.DegreeDay
import org.breezyweather.common.basic.models.weather.HalfDay
import org.breezyweather.common.basic.models.weather.Hourly
import org.breezyweather.common.basic.models.weather.MoonPhase
import org.breezyweather.common.basic.models.weather.Precipitation
import org.breezyweather.common.basic.models.weather.PrecipitationProbability
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.basic.models.weather.UV
import org.breezyweather.common.basic.models.weather.Wind
import org.breezyweather.common.basic.wrappers.HourlyWrapper
import org.breezyweather.common.basic.wrappers.SecondaryWeatherWrapper
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toDateNoHour
import org.shredzone.commons.suncalc.MoonIllumination
import org.shredzone.commons.suncalc.MoonTimes
import org.shredzone.commons.suncalc.SunTimes
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin


/**
 * MERGE MAIN WEATHER DATA WITH SECONDARY WEATHER DATA
 */

/**
 * Complete daily data missing on SecondaryWeatherWrapper early
 * to avoid being unable to do it later
 */
fun completeMissingSecondaryWeatherDailyData(
    initialSecondaryWeatherWrapper: SecondaryWeatherWrapper?,
    timeZone: TimeZone
): SecondaryWeatherWrapper? {
    if (initialSecondaryWeatherWrapper == null) return null

    return if ((!initialSecondaryWeatherWrapper.airQuality?.hourlyForecast.isNullOrEmpty()
        && initialSecondaryWeatherWrapper.airQuality?.dailyForecast.isNullOrEmpty())
        || (!initialSecondaryWeatherWrapper.allergen?.hourlyForecast.isNullOrEmpty()
                && initialSecondaryWeatherWrapper.allergen?.dailyForecast.isNullOrEmpty())) {
        val dailyAirQuality: Map<Date, AirQuality>? = if (initialSecondaryWeatherWrapper.airQuality?.dailyForecast.isNullOrEmpty()) {
            val dailyAirQualityMap: MutableMap<Date, AirQuality> = mutableMapOf()
            initialSecondaryWeatherWrapper.airQuality?.hourlyForecast?.entries?.groupBy {
                it.key.getFormattedDate(timeZone, "yyyy-MM-dd")
            }?.forEach { entry ->
                val airQuality = getDailyAirQualityFromSecondaryHourlyList(entry.value)
                if (airQuality != null) {
                    dailyAirQualityMap[entry.key.toDateNoHour(timeZone)!!] = airQuality
                }
            }
            dailyAirQualityMap
        } else initialSecondaryWeatherWrapper.airQuality!!.dailyForecast

        val dailyAllergen: Map<Date, Allergen>? = if (initialSecondaryWeatherWrapper.allergen?.dailyForecast.isNullOrEmpty()) {
            val dailyAllergenMap: MutableMap<Date, Allergen> = mutableMapOf()
            initialSecondaryWeatherWrapper.allergen?.hourlyForecast?.entries?.groupBy {
                it.key.getFormattedDate(timeZone, "yyyy-MM-dd")
            }?.forEach { entry ->
                val allergen = getDailyAllergenFromSecondaryHourlyList(entry.value)
                if (allergen != null) {
                    dailyAllergenMap[entry.key.toDateNoHour(timeZone)!!] = allergen
                }
            }
            dailyAllergenMap
        } else initialSecondaryWeatherWrapper.allergen!!.dailyForecast

        initialSecondaryWeatherWrapper.copy(
            airQuality = initialSecondaryWeatherWrapper.airQuality?.copy(
                dailyForecast = dailyAirQuality
            ),
            allergen = initialSecondaryWeatherWrapper.allergen?.copy(
                dailyForecast = dailyAllergen
            )
        )
    } else {
        initialSecondaryWeatherWrapper
    }
}

private fun getDailyAirQualityFromSecondaryHourlyList(
    hourlyList: List<Map.Entry<Date, AirQuality>>
): AirQuality? {
    // We need at least 18 hours for a signification estimation
    if (hourlyList.isEmpty() || hourlyList.size < 18) return null

    return AirQuality(
        pM25 = hourlyList.filter { it.value.pM25 != null }.map { it.value.pM25!! }.average().toFloat(),
        pM10 = hourlyList.filter { it.value.pM10 != null }.map { it.value.pM10!! }.average().toFloat(),
        sO2 = hourlyList.filter { it.value.sO2 != null }.map { it.value.sO2!! }.average().toFloat(),
        nO2 = hourlyList.filter { it.value.nO2 != null }.map { it.value.nO2!! }.average().toFloat(),
        o3 = hourlyList.filter { it.value.o3 != null }.map { it.value.o3!! }.average().toFloat(),
        cO = hourlyList.filter { it.value.cO != null }.map { it.value.cO!! }.average().toFloat()
    )
}

/**
 * Returns an Allergen object calculated from a List of Hourly for the day
 * (at least 18 non-null Hourly.Allergen required)
 */
private fun getDailyAllergenFromSecondaryHourlyList(
    hourlyList: List<Map.Entry<Date, Allergen>>
): Allergen? {
    // We need at least 18 hours for a signification estimation
    if (hourlyList.isEmpty() || hourlyList.size < 18) return null

    return Allergen(
        tree = hourlyList.filter { it.value.tree != null }.let { hl -> if (hl.isNotEmpty()) hl.maxOf { it.value.tree!! } else null },
        alder = hourlyList.filter { it.value.alder != null }.let { hl -> if (hl.isNotEmpty()) hl.maxOf { it.value.alder!! } else null },
        birch = hourlyList.filter { it.value.birch != null }.let { hl -> if (hl.isNotEmpty()) hl.maxOf { it.value.birch!! } else null },
        grass = hourlyList.filter { it.value.grass != null }.let { hl -> if (hl.isNotEmpty()) hl.maxOf { it.value.grass!! } else null },
        olive = hourlyList.filter { it.value.olive != null }.let { hl -> if (hl.isNotEmpty()) hl.maxOf { it.value.olive!! } else null },
        ragweed = hourlyList.filter { it.value.ragweed != null }.let { hl -> if (hl.isNotEmpty()) hl.maxOf { it.value.ragweed!! } else null },
        mugwort = hourlyList.filter { it.value.mugwort != null }.let { hl -> if (hl.isNotEmpty()) hl.maxOf { it.value.mugwort!! } else null },
        mold = hourlyList.filter { it.value.mold != null }.let { hl -> if (hl.isNotEmpty()) hl.maxOf { it.value.mold!! } else null },
    )
}

/**
 * Completes a List<HourlyWrapper> with data from a SecondaryWeatherWrapper
 * If a data is present in SecondaryWeatherWrapper, it will be used in priority, so don’t send
 * data you don’t want to use in it!
 * Process:
 * - Hourly air quality
 * - Hourly allergen
 */
fun mergeSecondaryWeatherDataIntoHourlyWrapperList(
    mainHourlyList: List<HourlyWrapper>?,
    secondaryWeatherWrapper: SecondaryWeatherWrapper?
): List<HourlyWrapper> {
    if (mainHourlyList.isNullOrEmpty() || secondaryWeatherWrapper == null
        || (secondaryWeatherWrapper.airQuality?.hourlyForecast.isNullOrEmpty()
        && secondaryWeatherWrapper.allergen?.hourlyForecast.isNullOrEmpty())) {
        return mainHourlyList ?: emptyList()
    }

    return mainHourlyList.map { mainHourly ->
        val airQuality = secondaryWeatherWrapper.airQuality?.hourlyForecast?.getOrElse(mainHourly.date) { null }
        val allergen = secondaryWeatherWrapper.allergen?.hourlyForecast?.getOrElse(mainHourly.date) { null }

        if (airQuality != null || allergen != null) {
            mainHourly.copy(
                airQuality = airQuality,
                allergen = allergen
            )
        } else mainHourly
    }
}

/**
 * Completes a List<HourlyWrapper> with data from a SecondaryWeatherWrapper
 * If a data is present in SecondaryWeatherWrapper, it will be used in priority, so don’t send
 * data you don’t want to use in it!
 * Process:
 * - Hourly air quality
 * - Hourly allergen
 */
fun mergeSecondaryWeatherDataIntoDailyList(
    mainDailyList: List<Daily>?,
    secondaryWeatherWrapper: SecondaryWeatherWrapper?
): List<Daily> {
    if (mainDailyList.isNullOrEmpty() || secondaryWeatherWrapper == null
        || (secondaryWeatherWrapper.airQuality?.dailyForecast.isNullOrEmpty()
                && secondaryWeatherWrapper.allergen?.dailyForecast.isNullOrEmpty())) {
        return mainDailyList ?: emptyList()
    }

    return mainDailyList.map { mainDaily ->
        val airQuality = secondaryWeatherWrapper.airQuality?.dailyForecast?.getOrElse(mainDaily.date) { null }
        val allergen = secondaryWeatherWrapper.allergen?.dailyForecast?.getOrElse(mainDaily.date) { null }

        if (airQuality != null || allergen != null) {
            mainDaily.copy(
                airQuality = airQuality,
                allergen = allergen
            )
        } else mainDaily
    }
}

/**
 * COMPUTE MISSING HOURLY DATA
 */

/**
 * Completes a List<HourlyWrapper> with the following data than can be computed:
 * - Dew point
 * - Wind chill temperature
 */
fun computeMissingHourlyData(
    hourlyList: List<HourlyWrapper>
): List<HourlyWrapper> {
    return hourlyList.map { hourly ->
        if (hourly.dewPoint == null || hourly.temperature?.windChillTemperature == null) {
            hourly.copy(
                dewPoint = hourly.dewPoint ?: computeDewPoint(hourly.temperature?.temperature?.toDouble(), hourly.relativeHumidity?.toDouble()),
                temperature = completeTemperatureWithComputedData(hourly.temperature, hourly.wind?.speed)
            )
        } else hourly
    }
}

/**
 * Compute dew point from temperature and relative humidity
 * Uses Magnus approximation with Arden Buck best variable set
 * TODO: Unit test
 *
 * @param temperature in °C
 * @param relativeHumidity in %
 */
private fun computeDewPoint(temperature: Double?, relativeHumidity: Double?): Float? {
    if (temperature == null || relativeHumidity == null) return null

    val b = if (temperature < 0) 17.966 else 17.368
    val c = if (temperature < 0) 227.15 else 238.88 //°C

    val magnus = ln(relativeHumidity / 100) + (b * temperature) / (c + temperature)
    return ((c * magnus) / (b - magnus)).toFloat()
}

fun completeTemperatureWithComputedData(
    temperature: Temperature?,
    windSpeed: Float?
): Temperature? {
    if (temperature?.temperature == null || temperature.windChillTemperature != null || windSpeed == null) return temperature

    return temperature.copy(
        windChillTemperature = computeWindChillTemperature(temperature.temperature.toDouble(), windSpeed.toDouble()),
    )
}

/**
 * Compute wind chill from temperature and wind speed
 * Uses Environment Canada methodology
 * Only valid for temperatures at or below 10 °C and wind speeds above 4.8 km/h
 * TODO: Unit test
 *
 * @param temperature in °C
 * @param windSpeed in km/h
 */
private fun computeWindChillTemperature(temperature: Double?, windSpeed: Double?): Float? {
    if (temperature == null || windSpeed == null || temperature > 10 || windSpeed <= 4.8) return null

    return (13.12 + (0.6215 * temperature) - (11.37 * windSpeed.pow(0.16)) + (0.3965 * temperature * windSpeed.pow(0.16))).toFloat()
}

/**
 * DAILY FROM HOURLY
 */

/**
 * Completes daily data from hourly data:
 * - HalfDay (day and night)
 * - Degree day
 * - Sunrise/set
 * - Air quality
 * - Allergen
 * - UV
 * - Hours of sun
 *
 * @param dailyList daily data
 * @param hourlyList hourly data
 * @param location for timeZone and calculation of sunrise/set according to lon/lat purposes
 */
fun completeDailyListFromHourlyList(
    dailyList: List<Daily>,
    hourlyList: List<HourlyWrapper>,
    location: Location
): List<Daily> {
    if (dailyList.isEmpty() || hourlyList.isEmpty()) return dailyList

    val hourlyListByHalfDay = getHourlyListByHalfDay(hourlyList, location.timeZone)
    val hourlyListByDay = hourlyList.groupBy { it.date.getFormattedDate(location.timeZone, "yyyy-MM-dd") }
    return dailyList.map { daily ->
        val theDayFormatted = daily.date.getFormattedDate(location.timeZone, "yyyy-MM-dd")
        val newDay = completeHalfDayFromHourlyList(
            dailyDate = daily.date,
            initialHalfDay = daily.day,
            halfDayHourlyList = hourlyListByHalfDay.getOrElse(theDayFormatted) { null }?.get("day"),
            isDay = true
        )
        val newNight = completeHalfDayFromHourlyList(
            dailyDate = daily.date,
            initialHalfDay = daily.night,
            halfDayHourlyList = hourlyListByHalfDay.getOrElse(theDayFormatted) { null }?.get("night"),
            isDay = false
        )
        /**
         * Most sources will return null data both on midnight sun and polar night
         * because the sun never rises in both cases
         * So we recalculate even in that case, and if it’s always up, we set up fake dates for
         * the whole 24-hour period to avoid having nighttime all the time
         */
        val newSun = if (daily.sun != null && daily.sun.isValid) daily.sun else {
            getCalculatedAstroSun(daily.date, location.longitude.toDouble(), location.latitude.toDouble())
        }

        daily.copy(
            day = newDay,
            night = newNight,
            degreeDay = if (daily.degreeDay?.cooling == null || daily.degreeDay.heating == null) {
                getDegreeDay(
                    minTemp = newDay?.temperature?.temperature?.toDouble(),
                    maxTemp = newNight?.temperature?.temperature?.toDouble()
                )
            } else daily.degreeDay,
            sun = newSun,
            moon = if (daily.moon != null && daily.moon.isValid) daily.moon else {
                getCalculatedAstroMoon(daily.date, location.longitude.toDouble(), location.latitude.toDouble())
            },
            moonPhase = if (daily.moonPhase?.angle != null) daily.moonPhase else {
                getCalculatedMoonPhase(daily.date)
            },
            airQuality = daily.airQuality ?: getDailyAirQualityFromHourlyList(
                hourlyListByDay.getOrElse(theDayFormatted) { null }
            ),
            allergen = daily.allergen ?: getDailyAllergenFromHourlyList(
                hourlyListByDay.getOrElse(theDayFormatted) { null }
            ),
            uV = if (daily.uV?.index != null) daily.uV else getDailyUVFromHourlyList(
                hourlyListByDay.getOrElse(theDayFormatted) { null }
            ),
            hoursOfSun = daily.hoursOfSun ?: getHoursOfDay(newSun.riseDate, newSun.setDate)
        )
    }
}

const val HEATING_DEGREE_DAY_BASE_TEMPERATURE = 18.0
const val COOLING_DEGREE_DAY_BASE_TEMPERATURE = 21.0
const val DEGREE_DAY_TEMPERATURE_MARGIN = 3.0

private fun getDegreeDay(minTemp: Double?, maxTemp: Double?): DegreeDay? {
    if (minTemp == null || maxTemp == null) return null

    val meanTemp = (minTemp + maxTemp) / 2
    if (meanTemp in (HEATING_DEGREE_DAY_BASE_TEMPERATURE - DEGREE_DAY_TEMPERATURE_MARGIN)..(COOLING_DEGREE_DAY_BASE_TEMPERATURE + DEGREE_DAY_TEMPERATURE_MARGIN)) {
        return DegreeDay(heating = 0f, cooling = 0f)
    }

    return if (meanTemp < HEATING_DEGREE_DAY_BASE_TEMPERATURE) {
        DegreeDay(heating = (HEATING_DEGREE_DAY_BASE_TEMPERATURE - meanTemp).toFloat(), cooling = 0f)
    } else {
        DegreeDay(heating = 0f, cooling = (meanTemp - COOLING_DEGREE_DAY_BASE_TEMPERATURE).toFloat())
    }
}

/**
 * Return 00:00:00.00 to 23:59:59.999 if sun is always up (assuming date parameter is at 00:00)
 * Takes 5 to 40 ms to execute on my device
 * Means that for a 15-day forecast, take between 0.1 and 0.6 sec
 * Given it is only called on missing data, it’s efficiently-safe
 */
private fun getCalculatedAstroSun(date: Date, longitude: Double, latitude: Double): Astro {
    val times = SunTimes.compute().on(date).at(latitude, longitude).execute()

    if (times.isAlwaysUp) {
        return Astro(
            riseDate = date,
            setDate = Date(date.time + (24 * 3600 * 1000) - 1)
        )
    }

    // If we miss the rise time, it means we are leaving midnight sun season
    if (times.rise == null && times.set != null) {
        return Astro(
            riseDate = date, // Setting 00:00 as rise date
            setDate = times.set
        )
    }

    // If we miss the set time, redo a calculation that takes more computing power
    // Should not happen very often so avoid doing full cycle everytime
    if (times.rise != null && times.set == null) {
        val times2 = SunTimes.compute().fullCycle().on(date).at(latitude, longitude).execute()
        return Astro(
            riseDate = times2.rise,
            setDate = times2.set
        )
    }

    return Astro(
        riseDate = times.rise,
        setDate = times.set
    )
}

private fun getCalculatedAstroMoon(date: Date, longitude: Double, latitude: Double): Astro {
    val times = MoonTimes.compute().on(date).at(latitude, longitude).execute()

    if (times.isAlwaysUp) {
        return Astro(
            riseDate = date,
            setDate = Date(date.time + (24 * 3600 * 1000) - 1)
        )
    }

    // If we miss the rise time, it means moon was already up before 00:00
    if (times.rise == null && times.set != null) {
        return Astro(
            riseDate = date, // Setting 00:00 as rise date
            setDate = times.set
        )
    }

    // If we miss the set time, redo a calculation that takes more computing power
    // Should not happen very often so avoid doing full cycle everytime
    if (times.rise != null && times.set == null) {
        val times2 = MoonTimes.compute().fullCycle().on(date).at(latitude, longitude).execute()
        return Astro(
            riseDate = times2.rise,
            setDate = times2.set
        )
    }

    return Astro(
        riseDate = times.rise,
        setDate = times.set
    )
}

/**
 * From a date initialized at 00:00, returns the moon phase angle at 12:00
 */
private fun getCalculatedMoonPhase(date: Date): MoonPhase {
    val illumination = MoonIllumination.compute()
        // Let’s take the middle of the day
        .on(Date(date.time + (12 * 3600 * 1000)))
        .execute()

    return MoonPhase(
        angle = (illumination.phase + 180).roundToInt()
    )
}

private fun getHourlyListByHalfDay(
    hourlyList: List<HourlyWrapper>,
    timeZone: TimeZone
): MutableMap<String, Map<String, MutableList<HourlyWrapper>>> {
    val hourlyByHalfDay: MutableMap<String, Map<String, MutableList<HourlyWrapper>>> = HashMap()

    hourlyList.forEach { hourly ->
        // We shift by 6 hours the hourly date, otherwise nighttime (00:00 to 05:59) would be on the wrong day
        val theDayShifted = Date(hourly.date.time - (6 * 3600 * 1000))
        val theDayFormatted = theDayShifted.getFormattedDate(timeZone, "yyyy-MM-dd")

        if (!hourlyByHalfDay.containsKey(theDayFormatted)) {
            hourlyByHalfDay[theDayFormatted] = hashMapOf(
                "day" to mutableListOf(),
                "night" to mutableListOf()
            )
        }
        if (theDayShifted.toCalendarWithTimeZone(timeZone).get(Calendar.HOUR_OF_DAY) < 12) {
            // 06:00 to 17:59 is the day (12 because we shifted by 6 hours)
            hourlyByHalfDay[theDayFormatted]!!["day"]!!.add(hourly)
        } else {
            // 18:00 to 05:59 is the night
            hourlyByHalfDay[theDayFormatted]!!["night"]!!.add(hourly)
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
 * Returns an Allergen object calculated from a List of Hourly for the day
 * (at least 18 non-null Hourly.Allergen required)
 */
private fun getDailyAllergenFromHourlyList(hourlyList: List<HourlyWrapper>? = null): Allergen? {
    // We need at least 18 hours for a signification estimation
    if (hourlyList.isNullOrEmpty() || hourlyList.size < 18) return null
    val hourlyListWithAllergen = hourlyList.filter { it.allergen != null }
    if (hourlyListWithAllergen.size < 18) return null

    return Allergen(
        tree = hourlyListWithAllergen.filter { it.allergen!!.tree != null }.let { hl -> if (hl.isNotEmpty()) hl.maxOf { it.allergen!!.tree!! } else null },
        alder = hourlyListWithAllergen.filter { it.allergen!!.alder != null }.let { hl -> if (hl.isNotEmpty()) hl.maxOf { it.allergen!!.alder!! } else null },
        birch = hourlyListWithAllergen.filter { it.allergen!!.birch != null }.let { hl -> if (hl.isNotEmpty()) hl.maxOf { it.allergen!!.birch!! } else null },
        grass = hourlyListWithAllergen.filter { it.allergen!!.grass != null }.let { hl -> if (hl.isNotEmpty()) hl.maxOf { it.allergen!!.grass!! } else null },
        olive = hourlyListWithAllergen.filter { it.allergen!!.olive != null }.let { hl -> if (hl.isNotEmpty()) hl.maxOf { it.allergen!!.olive!! } else null },
        ragweed = hourlyListWithAllergen.filter { it.allergen!!.ragweed != null }.let { hl -> if (hl.isNotEmpty()) hl.maxOf { it.allergen!!.ragweed!! } else null },
        mugwort = hourlyListWithAllergen.filter { it.allergen!!.mugwort != null }.let { hl -> if (hl.isNotEmpty()) hl.maxOf { it.allergen!!.mugwort!! } else null },
        mold = hourlyListWithAllergen.filter { it.allergen!!.mold != null }.let { hl -> if (hl.isNotEmpty()) hl.maxOf { it.allergen!!.mold!! } else null },
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
    return if (sunrise == null || sunset == null) {
        // Polar night
        0f
    } else if (sunrise.after(sunset)) {
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
        // Only keep hours in the future except for the forecast of the current hour
        // Example: 15:01 -> starts at 15:00, 15:59 -> starts at 15:00
        if (hourly.date.time >= System.currentTimeMillis() - (3600 * 1000)) {
            val dateForHourFormatted = hourly.date.getFormattedDate(timeZone, "yyyyMMdd")
            dailyListByDate.getOrElse(dateForHourFormatted) { null }
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
 * Returns false if no sunrise or sunset (polar night)
 * If sunrise after sunset (should not happen), returns true
 */
private fun isDaylight(sunrise: Date?, sunset: Date?, current: Date): Boolean {
    if (sunrise == null || sunset == null) return false
    if (sunrise.after(sunset)) return true

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

    val newWind = if (newCurrent.wind?.speed != null || hourly.wind?.speed == null) {
        newCurrent.wind
    } else hourly.wind
    val newRelativeHumidity = newCurrent.relativeHumidity ?: hourly.relativeHumidity
    val newTemperature = completeCurrentTemperatureFromHourly(
        newCurrent.temperature,
        hourly.temperature,
        newWind?.speed
    )
    val newDewPoint = newCurrent.dewPoint ?: if (newCurrent.relativeHumidity != null
        || newCurrent.temperature?.temperature != null) {
        // If current data is available, we compute this over hourly dewpoint
        computeDewPoint(newTemperature?.temperature?.toDouble(), newRelativeHumidity?.toDouble())
    } else hourly.dewPoint // Already calculated earlier
    return newCurrent.copy(
        weatherText = newCurrent.weatherText ?: hourly.weatherText,
        weatherCode = newCurrent.weatherCode ?: hourly.weatherCode,
        temperature = newTemperature,
        wind = newWind,
        uV = if (newCurrent.uV?.index == null && todayDaily != null) getCurrentUVFromDayMax(
            todayDaily.uV?.index,
            Date(),
            todayDaily.sun?.riseDate,
            todayDaily.sun?.setDate,
            timeZone
        ) else newCurrent.uV,
        airQuality = newCurrent.airQuality ?: hourly.airQuality,
        relativeHumidity = newRelativeHumidity,
        dewPoint = newDewPoint,
        pressure = newCurrent.pressure ?: hourly.pressure,
        cloudCover = newCurrent.cloudCover ?: hourly.cloudCover,
        visibility = newCurrent.visibility ?: hourly.visibility
    )
}

private fun completeCurrentTemperatureFromHourly(
    initialTemperature: Temperature?,
    hourlyTemperature: Temperature?,
    windSpeed: Float?
): Temperature? {
    if (hourlyTemperature == null) return initialTemperature
    val newTemperature = initialTemperature ?: Temperature()

    val newWindChill = newTemperature.windChillTemperature ?: if (newTemperature.temperature != null) {
        // If current data is available, we compute this over hourly windChill
        computeWindChillTemperature(newTemperature.temperature.toDouble(), windSpeed?.toDouble())
    } else hourlyTemperature.windChillTemperature
    return newTemperature.copy(
        temperature = newTemperature.temperature ?: hourlyTemperature.temperature,
        realFeelTemperature = newTemperature.realFeelTemperature ?: hourlyTemperature.realFeelTemperature,
        realFeelShaderTemperature = newTemperature.realFeelShaderTemperature ?: hourlyTemperature.realFeelShaderTemperature,
        apparentTemperature = newTemperature.apparentTemperature ?: hourlyTemperature.apparentTemperature,
        windChillTemperature = newWindChill,
        wetBulbTemperature = newTemperature.wetBulbTemperature ?: hourlyTemperature.wetBulbTemperature,
    )
}