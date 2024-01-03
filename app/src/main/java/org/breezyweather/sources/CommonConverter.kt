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

package org.breezyweather.sources

import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.AirQuality
import org.breezyweather.common.basic.models.weather.Alert
import org.breezyweather.common.basic.models.weather.Pollen
import org.breezyweather.common.basic.models.weather.Astro
import org.breezyweather.common.basic.models.weather.Current
import org.breezyweather.common.basic.models.weather.Daily
import org.breezyweather.common.basic.models.weather.DegreeDay
import org.breezyweather.common.basic.models.weather.HalfDay
import org.breezyweather.common.basic.models.weather.Hourly
import org.breezyweather.common.basic.models.weather.Minutely
import org.breezyweather.common.basic.models.weather.MoonPhase
import org.breezyweather.common.basic.models.weather.Normals
import org.breezyweather.common.basic.models.weather.Precipitation
import org.breezyweather.common.basic.models.weather.PrecipitationProbability
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.basic.models.weather.UV
import org.breezyweather.common.basic.models.weather.Weather
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.common.basic.models.weather.Wind
import org.breezyweather.common.basic.wrappers.AirQualityWrapper
import org.breezyweather.common.basic.wrappers.PollenWrapper
import org.breezyweather.common.basic.wrappers.HourlyWrapper
import org.breezyweather.common.basic.wrappers.SecondaryWeatherWrapper
import org.breezyweather.common.basic.wrappers.WeatherWrapper
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.median
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.theme.weatherView.WeatherViewController
import org.shredzone.commons.suncalc.MoonIllumination
import org.shredzone.commons.suncalc.MoonTimes
import org.shredzone.commons.suncalc.SunTimes
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.atan
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * /!\ WARNING /!\
 * It took me a lot of time to write these functions.
 * I would appreciate you don’t steal my work into a proprietary software.
 * LPGLv3 allows you to reuse my code but you have to respect its terms, basically
 * crediting the work of Breezy Weather along with giving a copy of its license.
 * If you already use a GPL license, you’re done. However, if you are using a
 * proprietary-friendly open source license such as MIT or Apache, you will have to
 * make an exception for the file you’re using my functions in.
 * For example, you will distribute your app under the MIT license with LPGLv3 exception
 * for file X. Basically, your app can become proprietary, however this file will have to
 * always stay free and open source if you add any modification to it.
 */

/**
 * MERGE DATABASE DATA WITH REFRESHED WEATHER DATA
 * Useful to keep forecast history
 */

/**
 * Complete previous hours/days with weather data from database
 */
fun completeMainWeatherWithPreviousData(
    newWeather: WeatherWrapper,
    oldWeather: Weather?,
    startDate: Date
): WeatherWrapper {
    if (oldWeather == null
        || (oldWeather.dailyForecast.isEmpty() && oldWeather.hourlyForecast.isEmpty())) {
        return newWeather
    }

    val missingDailyList = oldWeather.dailyForecast.filter {
        it.date >= startDate && (newWeather.dailyForecast?.getOrNull(0) == null || it.date < newWeather.dailyForecast[0].date)
    }
    val missingHourlyList = oldWeather.hourlyForecast.filter {
        it.date >= startDate && (newWeather.hourlyForecast?.getOrNull(0) == null || it.date < newWeather.hourlyForecast[0].date)
    }.map { it.toHourlyWrapper() }

    return if (missingDailyList.isEmpty() && missingHourlyList.isEmpty()) {
        newWeather
    } else {
        newWeather.copy(
            dailyForecast = missingDailyList + (newWeather.dailyForecast ?: emptyList()),
            hourlyForecast = missingHourlyList + (newWeather.hourlyForecast ?: emptyList())
        )
    }
}

/**
 * Get an air quality wrapper object to use from an old weather object
 * @param weather Old weather data
 * @param startDate a date initialized at yesterday 00:00 in the correct timezone, to ignore everything before that date
 */
fun getAirQualityWrapperFromWeather(
    weather: Weather?,
    startDate: Date
): AirQualityWrapper? {
    if (weather == null) return null
    val hourlyAirQuality = weather.hourlyForecast.filter { it.date >= startDate && it.airQuality?.isValid == true }
    val dailyAirQuality = weather.dailyForecast.filter { it.date >= startDate && it.airQuality?.isValid == true }
    if (hourlyAirQuality.isEmpty() && dailyAirQuality.isEmpty()) return null

    return AirQualityWrapper(
        dailyForecast = dailyAirQuality.associate { it.date to it.airQuality!! },
        hourlyForecast = hourlyAirQuality.associate { it.date to it.airQuality!! }
    )
}

/**
 * Get a pollen wrapper object to use from an old weather object
 * @param weather Old weather data
 * @param startDate a date initialized at yesterday 00:00 in the correct timezone, to ignore everything before that date
 */
fun getPollenWrapperFromWeather(
    weather: Weather?,
    startDate: Date
): PollenWrapper? {
    if (weather == null) return null
    val dailyPollen = weather.dailyForecast.filter { it.date >= startDate && it.pollen?.isValid == true }
    if (dailyPollen.isEmpty()) return null

    return PollenWrapper(
        dailyForecast = dailyPollen.associate { it.date to it.pollen!! }
    )
}

/**
 * Get a minutely list to use from an old weather object.
 * Only forecast data is kept (no minutely from the past)
 * @param weather Old weather data
 */
fun getMinutelyFromWeather(
    weather: Weather?
): List<Minutely>? {
    if (weather == null) return null
    val now = Date()
    return weather.minutelyForecast.filter { it.date >= now }
}

/**
 * Get an alert list to use from an old weather object.
 * Only alerts still valid (= not in the past) is kept
 * @param weather Old weather data
 */
fun getAlertsFromWeather(
    weather: Weather?
): List<Alert>? {
    if (weather == null) return null
    val now = Date()
    return weather.alertList.filter { it.endDate == null || it.endDate >= now }
}

/**
 * Get normals from an old weather object
 * Only normals still valid will be returned
 * @param location Location containing old weather data
 */
fun getNormalsFromWeather(
    location: Location
): Normals? {
    if (location.weather?.normals?.month == null) return null
    val cal = Date().toCalendarWithTimeZone(location.timeZone)
    return if (location.weather.normals.month == cal[Calendar.MONTH]) {
        location.weather.normals
    } else null
}

/**
 * MERGE MAIN WEATHER DATA WITH SECONDARY WEATHER DATA
 */
fun getDailyAirQualityFromHourly(
    hourlyAirQuality: Map<Date, AirQuality>?,
    timeZone: TimeZone
): MutableMap<Date, AirQuality> {
    val dailyAirQualityMap = mutableMapOf<Date, AirQuality>()
    hourlyAirQuality?.entries?.groupBy {
        it.key.getFormattedDate(timeZone, "yyyy-MM-dd", Locale.ENGLISH)
    }?.forEach { entry ->
        val airQuality = getDailyAirQualityFromSecondaryHourlyList(entry.value)
        if (airQuality != null) {
            dailyAirQualityMap[entry.key.toDateNoHour(timeZone)!!] = airQuality
        }
    }
    return dailyAirQualityMap
}

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
        || (!initialSecondaryWeatherWrapper.pollen?.hourlyForecast.isNullOrEmpty()
                && initialSecondaryWeatherWrapper.pollen?.dailyForecast.isNullOrEmpty())) {
        val dailyAirQuality: Map<Date, AirQuality>? = if (initialSecondaryWeatherWrapper.airQuality != null
            && initialSecondaryWeatherWrapper.airQuality.dailyForecast.isNullOrEmpty()) {
            getDailyAirQualityFromHourly(
                initialSecondaryWeatherWrapper.airQuality.hourlyForecast, timeZone
            )
        } else initialSecondaryWeatherWrapper.airQuality?.dailyForecast

        val dailyPollen: Map<Date, Pollen>? = if (initialSecondaryWeatherWrapper.pollen != null
            && initialSecondaryWeatherWrapper.pollen.dailyForecast.isNullOrEmpty()) {
            val dailyPollenMap: MutableMap<Date, Pollen> = mutableMapOf()
            initialSecondaryWeatherWrapper.pollen.hourlyForecast?.entries?.groupBy {
                it.key.getFormattedDate(timeZone, "yyyy-MM-dd", Locale.ENGLISH)
            }?.forEach { entry ->
                val pollen = getDailyPollenFromSecondaryHourlyList(entry.value)
                if (pollen != null) {
                    dailyPollenMap[entry.key.toDateNoHour(timeZone)!!] = pollen
                }
            }
            dailyPollenMap
        } else initialSecondaryWeatherWrapper.pollen?.dailyForecast

        initialSecondaryWeatherWrapper.copy(
            airQuality = initialSecondaryWeatherWrapper.airQuality?.copy(
                dailyForecast = dailyAirQuality
            ),
            pollen = initialSecondaryWeatherWrapper.pollen?.copy(
                dailyForecast = dailyPollen
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
        pM25 = hourlyList.mapNotNull { it.value.pM25 }.average().toFloat(),
        pM10 = hourlyList.mapNotNull { it.value.pM10 }.average().toFloat(),
        sO2 = hourlyList.mapNotNull { it.value.sO2 }.average().toFloat(),
        nO2 = hourlyList.mapNotNull { it.value.nO2 }.average().toFloat(),
        o3 = hourlyList.mapNotNull { it.value.o3 }.average().toFloat(),
        cO = hourlyList.mapNotNull { it.value.cO }.average().toFloat()
    )
}

/**
 * Returns a Pollen object calculated from a List of Hourly for the day
 * (at least 18 non-null Hourly.Pollen required)
 */
private fun getDailyPollenFromSecondaryHourlyList(
    hourlyList: List<Map.Entry<Date, Pollen>>
): Pollen? {
    // We need at least 18 hours for a signification estimation
    if (hourlyList.isEmpty() || hourlyList.size < 18) return null

    return Pollen(
        tree = hourlyList.mapNotNull { it.value.tree }.maxOrNull(),
        alder = hourlyList.mapNotNull { it.value.alder }.maxOrNull(),
        birch = hourlyList.mapNotNull { it.value.birch }.maxOrNull(),
        grass = hourlyList.mapNotNull { it.value.grass }.maxOrNull(),
        olive = hourlyList.mapNotNull { it.value.olive }.maxOrNull(),
        ragweed = hourlyList.mapNotNull { it.value.ragweed }.maxOrNull(),
        mugwort = hourlyList.mapNotNull { it.value.mugwort }.maxOrNull(),
        mold = hourlyList.mapNotNull { it.value.mold }.maxOrNull()
    )
}

/**
 * Completes a List<HourlyWrapper> with data from a SecondaryWeatherWrapper
 * If a data is present in SecondaryWeatherWrapper, it will be used in priority, so don’t send
 * data you don’t want to use in it!
 * Process:
 * - Hourly air quality
 * - Hourly pollen
 */
fun mergeSecondaryWeatherDataIntoHourlyWrapperList(
    mainHourlyList: List<HourlyWrapper>?,
    secondaryWeatherWrapper: SecondaryWeatherWrapper?
): List<HourlyWrapper> {
    if (mainHourlyList.isNullOrEmpty() || secondaryWeatherWrapper == null
        || (secondaryWeatherWrapper.airQuality?.hourlyForecast.isNullOrEmpty()
        && secondaryWeatherWrapper.pollen?.hourlyForecast.isNullOrEmpty())) {
        return mainHourlyList ?: emptyList()
    }

    return mainHourlyList.map { mainHourly ->
        val airQuality = secondaryWeatherWrapper.airQuality?.hourlyForecast?.getOrElse(mainHourly.date) { null }
        val pollen = secondaryWeatherWrapper.pollen?.hourlyForecast?.getOrElse(mainHourly.date) { null }

        if (airQuality != null && pollen != null) {
            mainHourly.copy(
                airQuality = airQuality,
                pollen = pollen
            )
        } else if (airQuality != null) {
            mainHourly.copy(airQuality = airQuality)
        } else if (pollen != null) {
            mainHourly.copy(pollen = pollen)
        } else {
            mainHourly
        }
    }
}

/**
 * Completes a List<HourlyWrapper> with data from a SecondaryWeatherWrapper
 * If a data is present in SecondaryWeatherWrapper, it will be used in priority, so don’t send
 * data you don’t want to use in it!
 * Process:
 * - Hourly air quality
 * - Hourly pollen
 */
fun mergeSecondaryWeatherDataIntoDailyList(
    mainDailyList: List<Daily>?,
    secondaryWeatherWrapper: SecondaryWeatherWrapper?
): List<Daily> {
    if (mainDailyList.isNullOrEmpty() || secondaryWeatherWrapper == null
        || (secondaryWeatherWrapper.airQuality?.dailyForecast.isNullOrEmpty()
                && secondaryWeatherWrapper.pollen?.dailyForecast.isNullOrEmpty())) {
        return mainDailyList ?: emptyList()
    }

    return mainDailyList.map { mainDaily ->
        val airQuality = secondaryWeatherWrapper.airQuality?.dailyForecast?.getOrElse(mainDaily.date) { null }
        val pollen = secondaryWeatherWrapper.pollen?.dailyForecast?.getOrElse(mainDaily.date) { null }

        if (airQuality != null && pollen != null) {
            mainDaily.copy(
                airQuality = airQuality,
                pollen = pollen
            )
        } else if (airQuality != null) {
            mainDaily.copy(airQuality = airQuality)
        } else if (pollen != null) {
            mainDaily.copy(pollen = pollen)
        } else {
            mainDaily
        }
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
                temperature = completeTemperatureWithComputedData(hourly.temperature, hourly.wind?.speed, hourly.relativeHumidity)
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
    windSpeed: Float?,
    relativeHumidity: Float?
): Temperature? {
    if (temperature?.temperature == null || temperature.windChillTemperature != null && temperature.wetBulbTemperature != null) return temperature

    return temperature.copy(
        windChillTemperature = temperature.windChillTemperature ?: computeWindChillTemperature(temperature.temperature.toDouble(), windSpeed?.toDouble()),
        wetBulbTemperature = temperature.wetBulbTemperature ?: computeWetBulbTemperature(temperature.temperature.toDouble(), relativeHumidity?.toDouble()),
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
 * Compute wet bulb from temperature and humidity
 * Based on formula from https://journals.ametsoc.org/view/journals/apme/50/11/jamc-d-11-0143.1.xml
 * TODO: Unit test
 *
 * @param temperature in °C
 * @param relativeHumidity in %
 */
private fun computeWetBulbTemperature(temperature: Double?, relativeHumidity: Double?): Float? {
    if (temperature == null || relativeHumidity == null) return null

    return (temperature * atan(0.151977 * (relativeHumidity + 8.313659).pow(0.5))
        + atan(temperature + relativeHumidity)
        - atan(relativeHumidity - 1.676331)
        + 0.00391838 * relativeHumidity.pow(3 / 2) * atan(0.023101 * relativeHumidity)
        - 4.686035).toFloat()
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
 * - Pollen
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
    val hourlyListByDay = hourlyList.groupBy { it.date.getFormattedDate(location.timeZone, "yyyy-MM-dd", Locale.ENGLISH) }
    return dailyList.map { daily ->
        val theDayFormatted = daily.date.getFormattedDate(location.timeZone, "yyyy-MM-dd", Locale.ENGLISH)
        val newDay = completeHalfDayFromHourlyList(
            initialHalfDay = daily.day,
            halfDayHourlyList = hourlyListByHalfDay.getOrElse(theDayFormatted) { null }?.get("day"),
            isDay = true
        )
        val newNight = completeHalfDayFromHourlyList(
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
            pollen = daily.pollen ?: getDailyPollenFromHourlyList(
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
        val theDayFormatted = theDayShifted.getFormattedDate(timeZone, "yyyy-MM-dd", Locale.ENGLISH)

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
 * - Weather code
 * - Weather text/phase
 * - Temperature (temperature, apparent, windChill and wetBulb)
 * - Precipitation (if Precipitation or Precipitation.total is null)
 * - PrecipitationProbability (if PrecipitationProbability or PrecipitationProbability.total is null)
 * - Wind (if Wind or Wind.speed is null)
 * - CloudCover (average)
 *
 * @param initialHalfDay the half day to be completed or null
 * @param halfDayHourlyList a List<Hourly> containing only hourlys from 06:00 to 17:59 for day, and 18:00 to 05:59 for night
 * @param isDay true if you want day, false if you want night
 * @return a new List<Daily>, the initial dailyList passed as 1st parameter can be freed after
 */
private fun completeHalfDayFromHourlyList(
    initialHalfDay: HalfDay? = null,
    halfDayHourlyList: List<HourlyWrapper>? = null,
    isDay: Boolean
): HalfDay? {
    if (halfDayHourlyList.isNullOrEmpty()) return initialHalfDay

    val newHalfDay = initialHalfDay ?: HalfDay()

    val extremeTemperature = if (newHalfDay.temperature?.temperature == null
        || newHalfDay.temperature.apparentTemperature == null
        || newHalfDay.temperature.windChillTemperature == null
        || newHalfDay.temperature.wetBulbTemperature == null) {
        getHalfDayTemperatureFromHourlyList(newHalfDay.temperature, halfDayHourlyList, isDay)
    } else newHalfDay.temperature

    val totalPrecipitation = if (newHalfDay.precipitation?.total == null) {
        getHalfDayPrecipitationFromHourlyList(halfDayHourlyList)
    } else newHalfDay.precipitation

    val maxPrecipitationProbability = if (newHalfDay.precipitationProbability?.total == null) {
        getHalfDayPrecipitationProbabilityFromHourlyList(halfDayHourlyList)
    } else newHalfDay.precipitationProbability

    val maxWind = if (newHalfDay.wind?.speed == null) {
        getHalfDayWindFromHourlyList(halfDayHourlyList)
    } else newHalfDay.wind

    val avgCloudCover = if (newHalfDay.precipitationProbability?.total == null) {
        getHalfDayCloudCoverFromHourlyList(halfDayHourlyList)
    } else newHalfDay.cloudCover

    val halfDayWeatherCode = newHalfDay.weatherCode ?: getHalfDayWeatherCodeFromHourlyList(
        halfDayHourlyList,
        totalPrecipitation,
        maxPrecipitationProbability,
        maxWind,
        avgCloudCover,
        getHalfDayAvgVisibilityFromHourlyList(halfDayHourlyList)
    )
    /*if (BreezyWeather.instance.debugMode) {
        LogHelper.log(msg = "code $halfDayWeatherCode when\n" +
                "p: ${totalPrecipitation.total}, pop: ${maxPrecipitationProbability.total}, " +
                "wind: ${maxWind?.speed}, vis: $avgVisibility, clouds: $avgCloudCover")
    }*/

    val halfDayWeatherTextFromCode = WeatherViewController.getWeatherText(halfDayWeatherCode)
    val halfDayWeatherText = newHalfDay.weatherText ?: halfDayWeatherTextFromCode
    val halfDayWeatherPhase = newHalfDay.weatherPhase ?: halfDayWeatherTextFromCode

    return newHalfDay.copy(
        weatherText = halfDayWeatherText,
        weatherPhase = halfDayWeatherPhase,
        weatherCode = halfDayWeatherCode,
        temperature = extremeTemperature,
        precipitation = totalPrecipitation,
        precipitationProbability = maxPrecipitationProbability,
        wind = maxWind,
        cloudCover = avgCloudCover
    )
}

/**
 * Tries to identify a weather code based on reported data (based on NOAA)
 *
 * @param halfDayHourlyList a List<Hourly> for the half day
 * @param totPrecipitation COMBINED (TOTAL) precipitation for the time period
 * @param maxPrecipitationProbability MAX precipitation probability for the time period
 * @param maxWind MAX wind speed for the time period
 * @param avgCloudCover AVERAGE cloud cover for the time period
 * @param avgVisibility AVERAGE visibility for the time period
 * @return a WeatherCode?, if guessed
 */
private fun getHalfDayWeatherCodeFromHourlyList(
    halfDayHourlyList: List<HourlyWrapper>,
    totPrecipitation: Precipitation,
    maxPrecipitationProbability: PrecipitationProbability,
    maxWind: Wind?,
    avgCloudCover: Int?,
    avgVisibility: Double?
) : WeatherCode? {
    val minPrecipIntensity = 1.0 // in mm
    val minPrecipProbability = 30.0 // in %
    val maxVisibilityHaze = 5000 // in m
    val maxVisibilityFog = 1000 // in m
    val maxWindSpeedWindy = 10.0 // in m/s
    val minCloudCoverPartlyCloudy = 37.5 // in %
    val minCloudCoverCloudy = 75.0 // in %

    // If total precipitation is greater than 1 mm
    // and max probability is greater than 30 % (assume 100 % if not reported)
    if ((totPrecipitation.total ?: 0f) > minPrecipIntensity &&
        (maxPrecipitationProbability.total ?: 100f) > minPrecipProbability
    ) {
        val isRain =
            maxPrecipitationProbability.rain?.let { it > minPrecipProbability }
                ?: totPrecipitation.rain?.let { it > 0 }
                ?: false
        val isSnow =
            maxPrecipitationProbability.snow?.let { it > minPrecipProbability }
                ?: totPrecipitation.snow?.let { it > 0 }
                ?: false
        val isIce =
            maxPrecipitationProbability.ice?.let { it > minPrecipProbability }
                ?: totPrecipitation.ice?.let { it > 0 }
                ?: false
        val isThunder =
            maxPrecipitationProbability.thunderstorm?.let { it > minPrecipProbability }
                ?: totPrecipitation.thunderstorm?.let { it > 0 }
                ?: false

        if (isRain || isSnow || isIce || isThunder) {
            return if (isThunder) {
                if (isRain || isSnow || isIce) WeatherCode.THUNDERSTORM
                else WeatherCode.THUNDER
            } else if (isIce) {
                WeatherCode.HAIL
            } else if (isSnow) {
                if (isRain) WeatherCode.SLEET
                else WeatherCode.SNOW
            } else {
                WeatherCode.RAIN
            }
        }

        // Fallback to using weather codes
        if (halfDayHourlyList.count { it.weatherCode == WeatherCode.THUNDERSTORM } > 1) {
            return WeatherCode.THUNDERSTORM
        }

        val counts = arrayOf(
            halfDayHourlyList.count { it.weatherCode == WeatherCode.RAIN },
            halfDayHourlyList.count { it.weatherCode == WeatherCode.SLEET },
            halfDayHourlyList.count { it.weatherCode == WeatherCode.SNOW },
            halfDayHourlyList.count { it.weatherCode == WeatherCode.HAIL }
        )
        if (counts.max() > 0) {
            if (halfDayHourlyList.count { it.weatherCode == WeatherCode.THUNDER } > 1) {
                return WeatherCode.THUNDERSTORM
            } else {
                if (counts[3] > 1) {
                    return WeatherCode.HAIL
                }
                if (counts[0] > 1 && counts[2] > 1 || counts[1] > 1) {
                    return WeatherCode.SLEET
                }
                if (counts[2] > 1) {
                    return WeatherCode.SNOW
                }
                return WeatherCode.RAIN
            }
        } else {
            // If the source doesn't provide probability, intensity, or weather codes
            return WeatherCode.RAIN
        }
    }

    // If average visibility is below 5 km, conditions are either FOG or HAZY
    if (avgVisibility != null && avgVisibility < maxVisibilityHaze) {
        if (avgVisibility < maxVisibilityFog) return WeatherCode.FOG
        return WeatherCode.HAZE
    }

    // Max winds > 10 m/s, it’s windy
    if (maxWind?.speed != null && maxWind.speed > maxWindSpeedWindy) {
        return WeatherCode.WIND
    }

    // It’s not raining, it’s not windy, and it’s not mysterious. Just cloudy
    if (avgCloudCover != null) {
        if (avgCloudCover > minCloudCoverCloudy) return WeatherCode.CLOUDY
        if (avgCloudCover > minCloudCoverPartlyCloudy) return WeatherCode.PARTLY_CLOUDY
        return WeatherCode.CLEAR
    }

    // No precipitation, visibility, wind, or cloud cover data
    // let’s fallback to the median
    val hourlyListWithWeatherCode = halfDayHourlyList.filter { it.weatherCode != null }
    return if (hourlyListWithWeatherCode.isNotEmpty()) {
        hourlyListWithWeatherCode[
            if (hourlyListWithWeatherCode.size % 2 == 0) {
                hourlyListWithWeatherCode.size / 2
            } else (hourlyListWithWeatherCode.size - 1) / 2
        ].weatherCode
    } else WeatherCode.PARTLY_CLOUDY
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
        val halfDayHourlyListTemperature = halfDayHourlyList.mapNotNull { it.temperature?.temperature }
        temperatureTemperature = if (isDay) {
            halfDayHourlyListTemperature.maxOrNull()
        } else {
            halfDayHourlyListTemperature.minOrNull()
        }
    }
    if (temperatureApparentTemperature == null) {
        val halfDayHourlyListApparentTemperature = halfDayHourlyList.mapNotNull { it.temperature?.apparentTemperature }
        temperatureApparentTemperature = if (isDay) {
            halfDayHourlyListApparentTemperature.maxOrNull()
        } else {
            halfDayHourlyListApparentTemperature.minOrNull()
        }
    }
    if (temperatureWindChillTemperature == null) {
        val halfDayHourlyListWindChillTemperature = halfDayHourlyList.mapNotNull { it.temperature?.windChillTemperature }
        temperatureWindChillTemperature = if (isDay) {
            halfDayHourlyListWindChillTemperature.maxOrNull()
        } else {
            halfDayHourlyListWindChillTemperature.minOrNull()
        }
    }
    if (temperatureWetBulbTemperature == null) {
        val halfDayHourlyListWetBulbTemperature = halfDayHourlyList.mapNotNull { it.temperature?.wetBulbTemperature }
        temperatureWetBulbTemperature = if (isDay) {
            halfDayHourlyListWetBulbTemperature.maxOrNull()
        } else {
            halfDayHourlyListWetBulbTemperature.minOrNull()
        }
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
        .mapNotNull { it.precipitation?.total }
    val halfDayHourlyListPrecipitationThunderstorm = halfDayHourlyList
        .mapNotNull { it.precipitation?.thunderstorm }
    val halfDayHourlyListPrecipitationRain = halfDayHourlyList
        .mapNotNull { it.precipitation?.rain }
    val halfDayHourlyListPrecipitationSnow = halfDayHourlyList
        .mapNotNull { it.precipitation?.snow }
    val halfDayHourlyListPrecipitationIce = halfDayHourlyList
        .mapNotNull { it.precipitation?.ice }

    return Precipitation(
        total = if (halfDayHourlyListPrecipitationTotal.isNotEmpty()) {
            halfDayHourlyListPrecipitationTotal.sum()
        } else null,
        thunderstorm = if (halfDayHourlyListPrecipitationThunderstorm.isNotEmpty()) {
            halfDayHourlyListPrecipitationThunderstorm.sum()
        } else null,
        rain = if (halfDayHourlyListPrecipitationRain.isNotEmpty()) {
            halfDayHourlyListPrecipitationRain.sum()
        } else null,
        snow = if (halfDayHourlyListPrecipitationSnow.isNotEmpty()) {
            halfDayHourlyListPrecipitationSnow.sum()
        } else null,
        ice = if (halfDayHourlyListPrecipitationIce.isNotEmpty()) {
            halfDayHourlyListPrecipitationIce.sum()
        } else null
    )
}

private fun getHalfDayPrecipitationProbabilityFromHourlyList(
    halfDayHourlyList: List<HourlyWrapper>
): PrecipitationProbability {
    val halfDayHourlyListPrecipitationProbabilityTotal = halfDayHourlyList
        .mapNotNull { it.precipitationProbability?.total }
    val halfDayHourlyListPrecipitationProbabilityThunderstorm = halfDayHourlyList
        .mapNotNull { it.precipitationProbability?.thunderstorm }
    val halfDayHourlyListPrecipitationProbabilityRain = halfDayHourlyList
        .mapNotNull { it.precipitationProbability?.rain }
    val halfDayHourlyListPrecipitationProbabilitySnow = halfDayHourlyList
        .mapNotNull { it.precipitationProbability?.snow }
    val halfDayHourlyListPrecipitationProbabilityIce = halfDayHourlyList
        .mapNotNull { it.precipitationProbability?.ice }

    return PrecipitationProbability(
        total = halfDayHourlyListPrecipitationProbabilityTotal.maxOrNull(),
        thunderstorm = halfDayHourlyListPrecipitationProbabilityThunderstorm.maxOrNull(),
        rain = halfDayHourlyListPrecipitationProbabilityRain.maxOrNull(),
        snow = halfDayHourlyListPrecipitationProbabilitySnow.maxOrNull(),
        ice = halfDayHourlyListPrecipitationProbabilityIce.maxOrNull()
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
    val halfDayHourlyListCloudCover = halfDayHourlyList.mapNotNull { it.cloudCover }

    return if (halfDayHourlyListCloudCover.isNotEmpty()) {
        halfDayHourlyListCloudCover.average().roundToInt()
    } else null
}

private fun getHalfDayAvgVisibilityFromHourlyList(halfDayHourlyList: List<HourlyWrapper>): Double? {
    val withVisibility = halfDayHourlyList.mapNotNull { it.visibility }
    return if (withVisibility.isNotEmpty()) withVisibility.average() else null
}

/**
 * Returns an AirQuality object calculated from a List of Hourly for the day
 * (at least 18 non-null Hourly.AirQuality required)
 */
private fun getDailyAirQualityFromHourlyList(hourlyList: List<HourlyWrapper>? = null): AirQuality? {
    // We need at least 18 hours for a signification estimation
    if (hourlyList.isNullOrEmpty() || hourlyList.size < 18) return null
    val hourlyListWithAirQuality = hourlyList.mapNotNull { it.airQuality }
    if (hourlyListWithAirQuality.size < 18) return null

    return AirQuality(
        pM25 = hourlyListWithAirQuality.mapNotNull { it.pM25 }.average().toFloat(),
        pM10 = hourlyListWithAirQuality.mapNotNull { it.pM10 }.average().toFloat(),
        sO2 = hourlyListWithAirQuality.mapNotNull { it.sO2 }.average().toFloat(),
        nO2 = hourlyListWithAirQuality.mapNotNull { it.nO2 }.average().toFloat(),
        o3 = hourlyListWithAirQuality.mapNotNull { it.o3 }.average().toFloat(),
        cO = hourlyListWithAirQuality.mapNotNull { it.cO }.average().toFloat()
    )
}

/**
 * Returns a Pollen object calculated from a List of Hourly for the day
 * (at least 18 non-null Hourly.Pollen required)
 */
private fun getDailyPollenFromHourlyList(hourlyList: List<HourlyWrapper>? = null): Pollen? {
    // We need at least 18 hours for a signification estimation
    if (hourlyList.isNullOrEmpty() || hourlyList.size < 18) return null
    val hourlyListWithPollen = hourlyList.mapNotNull { it.pollen }
    if (hourlyListWithPollen.size < 18) return null

    return Pollen(
        tree = hourlyListWithPollen.mapNotNull { it.tree }.maxOrNull(),
        alder = hourlyListWithPollen.mapNotNull { it.alder }.maxOrNull(),
        birch = hourlyListWithPollen.mapNotNull { it.birch }.maxOrNull(),
        grass = hourlyListWithPollen.mapNotNull { it.grass }.maxOrNull(),
        olive = hourlyListWithPollen.mapNotNull { it.olive }.maxOrNull(),
        ragweed = hourlyListWithPollen.mapNotNull { it.ragweed }.maxOrNull(),
        mugwort = hourlyListWithPollen.mapNotNull { it.mugwort }.maxOrNull(),
        mold = hourlyListWithPollen.mapNotNull { it.mold }.maxOrNull()
    )
}

/**
 * Returns the max UV for the day from a list of hourly
 */
private fun getDailyUVFromHourlyList(hourlyList: List<HourlyWrapper>? = null): UV? {
    if (hourlyList.isNullOrEmpty()) return null
    val hourlyListWithUV = hourlyList.mapNotNull { it.uV?.index }
    if (hourlyListWithUV.isEmpty()) return null

    return UV(index = hourlyListWithUV.max())
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
    val dailyListByDate = dailyList.groupBy { it.date.getFormattedDate(timeZone, "yyyyMMdd", Locale.ENGLISH) }
    val newHourlyList: MutableList<Hourly> = ArrayList(hourlyList.size)
    hourlyList.forEach { hourly ->
        val dateForHourFormatted = hourly.date.getFormattedDate(timeZone, "yyyyMMdd", Locale.ENGLISH)
        dailyListByDate.getOrElse(dateForHourFormatted) { null }
            ?.first()?.let { daily ->
                val isDaylight = hourly.isDaylight ?: isDaylight(
                    daily.sun?.riseDate,
                    daily.sun?.setDate,
                    hourly.date
                )
                newHourlyList.add(
                    hourly.toHourly(
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
        newHourlyList.add(hourly.toHourly())
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
        newWind?.speed,
        newRelativeHumidity
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
    windSpeed: Float?,
    relativeHumidity: Float?
): Temperature? {
    if (hourlyTemperature == null) return initialTemperature
    val newTemperature = initialTemperature ?: Temperature()

    val newWindChill = newTemperature.windChillTemperature ?: if (newTemperature.temperature != null) {
        // If current data is available, we compute this over hourly windChill
        computeWindChillTemperature(newTemperature.temperature.toDouble(), windSpeed?.toDouble())
    } else hourlyTemperature.windChillTemperature
    val newWetBulb = newTemperature.wetBulbTemperature ?: if (newTemperature.temperature != null) {
        // If current data is available, we compute this over hourly wetBulb
        computeWetBulbTemperature(newTemperature.temperature.toDouble(), relativeHumidity?.toDouble())
    } else hourlyTemperature.wetBulbTemperature
    return newTemperature.copy(
        temperature = newTemperature.temperature ?: hourlyTemperature.temperature,
        realFeelTemperature = newTemperature.realFeelTemperature ?: hourlyTemperature.realFeelTemperature,
        realFeelShaderTemperature = newTemperature.realFeelShaderTemperature ?: hourlyTemperature.realFeelShaderTemperature,
        apparentTemperature = newTemperature.apparentTemperature ?: hourlyTemperature.apparentTemperature,
        windChillTemperature = newWindChill,
        wetBulbTemperature = newWetBulb,
    )
}

fun completeNormalsFromDaily(
    normals: Normals?,
    dailyForecast: List<Daily>
): Normals? {
    if (normals?.month != null && normals.isValid) return normals
    if (dailyForecast.isEmpty()) return null

    return Normals(
        daytimeTemperature = dailyForecast.mapNotNull { it.day?.temperature?.temperature }.toTypedArray().median,
        nighttimeTemperature = dailyForecast.mapNotNull { it.night?.temperature?.temperature }.toTypedArray().median
    )
}