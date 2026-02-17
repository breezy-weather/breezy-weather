/*
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

import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Astro
import breezyweather.domain.weather.model.Current
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.DailyCloudCover
import breezyweather.domain.weather.model.DailyDewPoint
import breezyweather.domain.weather.model.DailyPressure
import breezyweather.domain.weather.model.DailyRelativeHumidity
import breezyweather.domain.weather.model.DailyVisibility
import breezyweather.domain.weather.model.DegreeDay
import breezyweather.domain.weather.model.HalfDay
import breezyweather.domain.weather.model.Hourly
import breezyweather.domain.weather.model.MoonPhase
import breezyweather.domain.weather.model.Pollen
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Weather
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import org.breezyweather.common.extensions.CLOUD_COVER_BKN
import org.breezyweather.common.extensions.CLOUD_COVER_FEW
import org.breezyweather.common.extensions.ensurePositive
import org.breezyweather.common.extensions.getIsoFormattedDate
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.domain.weather.model.validate
import org.breezyweather.ui.theme.weatherView.WeatherViewController
import org.breezyweather.unit.computing.computeApparentTemperature
import org.breezyweather.unit.computing.computeDewPoint
import org.breezyweather.unit.computing.computeHumidex
import org.breezyweather.unit.computing.computeRelativeHumidity
import org.breezyweather.unit.computing.computeTotalPrecipitation
import org.breezyweather.unit.computing.computeTotalProbabilityOfPrecipitation
import org.breezyweather.unit.computing.computeWindChillTemperature
import org.breezyweather.unit.distance.Distance
import org.breezyweather.unit.distance.Distance.Companion.meters
import org.breezyweather.unit.duration.toValidDailyOrNull
import org.breezyweather.unit.duration.toValidHalfDayOrNull
import org.breezyweather.unit.pollutant.PollutantConcentration.Companion.microgramsPerCubicMeter
import org.breezyweather.unit.precipitation.Precipitation.Companion.micrometers
import org.breezyweather.unit.precipitation.Precipitation.Companion.millimeters
import org.breezyweather.unit.pressure.Pressure
import org.breezyweather.unit.pressure.Pressure.Companion.pascals
import org.breezyweather.unit.ratio.Ratio
import org.breezyweather.unit.ratio.Ratio.Companion.percent
import org.breezyweather.unit.ratio.Ratio.Companion.permille
import org.breezyweather.unit.speed.Speed
import org.breezyweather.unit.speed.Speed.Companion.metersPerSecond
import org.breezyweather.unit.temperature.Temperature
import org.breezyweather.unit.temperature.Temperature.Companion.deciCelsius
import org.shredzone.commons.suncalc.MoonIllumination
import org.shredzone.commons.suncalc.MoonTimes
import org.shredzone.commons.suncalc.SunTimes
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.nanoseconds

/*
 * /!\ WARNING /!\
 * It took us a lot of time to write these functions.
 * I would appreciate you don’t steal our work into a proprietary software.
 * LPGLv3 allows you to reuse our code but you have to respect its terms, basically
 * crediting the work of Breezy Weather along with giving a copy of its license.
 * If you already use a GPL license, you’re done. However, if you are using a
 * proprietary-friendly open source license such as MIT or Apache, you will have to
 * make an exception for the file you’re using our functions in.
 * For example, you will distribute your app under the MIT license with LPGLv3 exception
 * for file X. Basically, your app can become proprietary, however this file will have to
 * always stay free and open source if you add any modification to it.
 */

/*
 * MERGE DATABASE DATA WITH REFRESHED WEATHER DATA
 * Useful to keep forecast history
 */

/**
 * Complete previous hours/days with weather data from database
 */
internal fun completeNewWeatherWithPreviousData(
    newWeather: WeatherWrapper,
    oldWeather: Weather?,
    startDate: Date,
    airQualitySource: String?,
    pollenSource: String?,
): WeatherWrapper {
    if (oldWeather == null ||
        (oldWeather.dailyForecast.isEmpty() && oldWeather.hourlyForecast.isEmpty())
    ) {
        return newWeather
    }

    val missingDailyList = oldWeather.dailyForecast.filter {
        it.date >= startDate &&
            (newWeather.dailyForecast?.getOrNull(0) == null || it.date < newWeather.dailyForecast!![0].date)
    }.map { it.toDailyWrapper() }
    val missingHourlyList = oldWeather.hourlyForecast.filter {
        it.date >= startDate &&
            (newWeather.hourlyForecast?.getOrNull(0) == null || it.date < newWeather.hourlyForecast!![0].date)
    }.map { it.toHourlyWrapper() }
    val missingDailyAirQualityList = oldWeather.dailyForecast.filter {
        it.date >= startDate &&
            it.airQuality?.isValid == true &&
            (newWeather.dailyForecast?.getOrNull(0) == null || it.date < newWeather.dailyForecast!![0].date)
    }.associate { it.date to it.airQuality!! }
    val missingHourlyAirQualityList = oldWeather.hourlyForecast.filter {
        it.date >= startDate &&
            it.airQuality?.isValid == true &&
            (newWeather.hourlyForecast?.getOrNull(0) == null || it.date < newWeather.hourlyForecast!![0].date)
    }.associate { it.date to it.airQuality!! }
    val missingDailyPollenList = oldWeather.dailyForecast.filter {
        it.date >= startDate &&
            it.pollen?.isValid == true &&
            (newWeather.dailyForecast?.getOrNull(0) == null || it.date < newWeather.dailyForecast!![0].date)
    }.associate { it.date to it.pollen!! }

    return if (missingDailyList.isEmpty() && missingHourlyList.isEmpty()) {
        newWeather
    } else {
        newWeather.copy(
            dailyForecast = missingDailyList + (newWeather.dailyForecast ?: emptyList()),
            hourlyForecast = missingHourlyList + (newWeather.hourlyForecast ?: emptyList()),
            airQuality = if (!airQualitySource.isNullOrEmpty()) {
                newWeather.airQuality?.copy(
                    dailyForecast = missingDailyAirQualityList +
                        (newWeather.airQuality!!.dailyForecast ?: emptyMap()),
                    hourlyForecast = missingHourlyAirQualityList +
                        (newWeather.airQuality!!.hourlyForecast ?: emptyMap())
                )
            } else {
                null
            },
            pollen = if (!pollenSource.isNullOrEmpty()) {
                newWeather.pollen?.copy(
                    dailyForecast = missingDailyPollenList + (newWeather.pollen!!.dailyForecast ?: emptyMap())
                )
            } else {
                null
            }
        )
    }
}

/*
 * MERGE MAIN WEATHER DATA WITH SECONDARY WEATHER DATA
 */

/**
 * Convert List<DailyWrapper> to List<Daily> completing with air quality/pollen data if available
 */
internal fun convertDailyWrapperToDailyList(
    weatherWrapper: WeatherWrapper,
): List<Daily> {
    if (weatherWrapper.airQuality?.dailyForecast.isNullOrEmpty() &&
        weatherWrapper.pollen?.dailyForecast.isNullOrEmpty()
    ) {
        return weatherWrapper.dailyForecast?.map { it.toDaily() } ?: emptyList()
    }

    return weatherWrapper.dailyForecast!!.map { mainDaily ->
        val airQuality = weatherWrapper.airQuality?.dailyForecast?.getOrElse(mainDaily.date) { null }
        val pollen = weatherWrapper.pollen?.dailyForecast?.getOrElse(mainDaily.date) { null }

        mainDaily.toDaily(
            airQuality = airQuality,
            pollen = pollen
        )
    }
}

/*
 * COMPUTE MISSING HOURLY DATA
 */

/**
 * Completes a List<HourlyWrapper> with the following data than can be computed:
 * - Weather code
 * - Weather text
 * - Dew point
 * - Apparent temperature
 * - Wind chill temperature
 * - Wet bulb temperature
 *
 * Also ensures all values are valid
 */
internal fun computeMissingHourlyData(
    hourlyList: List<HourlyWrapper>?,
): List<Hourly>? {
    return hourlyList?.map { hourly ->
        val temp = hourly.temperature?.temperature?.toValidOrNull()
        val feelsLike = hourly.temperature?.feelsLike?.toValidOrNull()
        val wind = hourly.wind?.validate()
        val precipitation = hourly.precipitation?.copy(
            total = hourly.precipitation!!.total?.toValidHourlyOrNull(),
            thunderstorm = hourly.precipitation!!.thunderstorm?.toValidHourlyOrNull(),
            rain = hourly.precipitation!!.rain?.toValidHourlyOrNull(),
            snow = hourly.precipitation!!.snow?.toValidHourlyOrNull(),
            ice = hourly.precipitation!!.ice?.toValidHourlyOrNull()
        )
        val precipitationProbability = hourly.precipitationProbability?.copy(
            total = hourly.precipitationProbability!!.total?.toValidRangeOrNull(),
            thunderstorm = hourly.precipitationProbability!!.thunderstorm?.toValidRangeOrNull(),
            rain = hourly.precipitationProbability!!.rain?.toValidRangeOrNull(),
            snow = hourly.precipitationProbability!!.snow?.toValidRangeOrNull(),
            ice = hourly.precipitationProbability!!.ice?.toValidRangeOrNull()
        )
        val cloudCover = hourly.cloudCover?.toValidRangeOrNull()
        val visibility = hourly.visibility?.toValidOrNull()
        val weatherCode = hourly.weatherCode ?: getHalfDayWeatherCodeFromHourlyList(
            listOf(hourly.toHourly()),
            precipitation,
            precipitationProbability,
            wind,
            cloudCover,
            visibility
        )
        val relativeHumidity = hourly.relativeHumidity?.toValidRangeOrNull()
            ?: computeRelativeHumidity(temp, hourly.dewPoint?.toValidOrNull())
        val dewPoint = hourly.dewPoint?.toValidOrNull()
            ?: computeDewPoint(temp, relativeHumidity)

        hourly.toHourly().copy(
            weatherText = if (hourly.weatherText.isNullOrEmpty()) {
                weatherCode?.let { WeatherViewController.getWeatherText(it) }
            } else {
                hourly.weatherText
            },
            weatherCode = weatherCode,
            temperature = breezyweather.domain.weather.model.Temperature(
                temperature = temp,
                sourceFeelsLike = feelsLike,
                computedApparent = computeApparentTemperature(temp, relativeHumidity, wind?.speed),
                computedWindChill = computeWindChillTemperature(temp, wind?.speed),
                computedHumidex = computeHumidex(temp, dewPoint)
            ),
            precipitation = Precipitation(
                total = precipitation?.total
                    ?: computeTotalPrecipitation(
                        temp,
                        precipitation?.rain,
                        precipitation?.snow,
                        precipitation?.ice
                    ),
                thunderstorm = precipitation?.thunderstorm,
                rain = precipitation?.rain,
                snow = precipitation?.snow,
                ice = precipitation?.ice
            ),
            precipitationProbability = PrecipitationProbability(
                total = precipitationProbability?.total ?: computeTotalProbabilityOfPrecipitation(
                    precipitationProbability?.thunderstorm,
                    precipitationProbability?.rain,
                    precipitationProbability?.ice,
                    precipitationProbability?.snow
                ),
                thunderstorm = precipitationProbability?.thunderstorm,
                rain = precipitationProbability?.rain,
                snow = precipitationProbability?.snow,
                ice = precipitationProbability?.ice
            ),
            wind = wind,
            relativeHumidity = relativeHumidity,
            dewPoint = dewPoint,
            pressure = hourly.pressure?.toValidOrNull(),
            cloudCover = cloudCover,
            visibility = visibility
        )
    }
}

/**
 * Convert cardinal points direction to a degree
 * Supports up to 3 characters cardinal points
 *
 * Supported languages:
 * - N, W, E, S cardinal points (English)
 * - N, O, E, S cardinal points (French, Spanish)
 * - VR, VAR for variable direction
 */
internal fun getWindDegree(
    direction: String?,
): Double? = when (direction) {
    "N" -> 0.0
    "NNE" -> 22.5
    "NE" -> 45.0
    "ENE" -> 67.5
    "E" -> 90.0
    "ESE" -> 112.5
    "SE" -> 135.0
    "SSE" -> 157.5
    "S" -> 180.0
    "SSW", "SSO" -> 202.5
    "SW", "SO" -> 225.0
    "WSW", "OSO" -> 247.5
    "W", "O" -> 270.0
    "WNW", "ONO" -> 292.5
    "NW", "NO" -> 315.0
    "NNW", "NNO" -> 337.5
    "VR", "VAR" -> -1.0
    else -> null
}

/*
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
 * - Sunshine duration
 *
 * @param dailyList daily data
 * @param hourlyList hourly data
 * @param hourlyAirQuality hourly air quality data from WeatherWrapper
 * @param hourlyPollen hourly pollen data from WeatherWrapper
 * @param hourlySunshine hourly sunshine duration from HourlyWrapper
 * @param currentPollen current pollen data from WeatherWrapper, will be used as "Today" if hourlyPollen is empty
 * @param location for timeZone and calculation of sunrise/set according to lon/lat purposes
 */
internal fun completeDailyListFromHourlyList(
    dailyList: List<Daily>,
    hourlyList: List<Hourly>,
    hourlyAirQuality: Map<Date, AirQuality>,
    hourlyPollen: Map<Date, Pollen>,
    hourlySunshine: Map<Date, Duration?>,
    currentPollen: Pollen?,
    location: Location,
): List<Daily> {
    if (dailyList.isEmpty()) return dailyList

    val hourlyListByHalfDay = getHourlyListByHalfDay(hourlyList, location)
    val hourlyListByDay = hourlyList.groupBy { it.date.getIsoFormattedDate(location) }
    val todayFormatted = Date().getIsoFormattedDate(location)
    return dailyList.map { daily ->
        val theDayFormatted = daily.date.getIsoFormattedDate(location)
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

        val newSun = getCalculatedAstroSun(daily.date, location.longitude, location.latitude)

        daily.copy(
            day = newDay,
            night = newNight,
            degreeDay = if (daily.degreeDay?.cooling == null || daily.degreeDay!!.heating == null) {
                getDegreeDay(
                    minTemp = newDay?.temperature?.temperature,
                    maxTemp = newNight?.temperature?.temperature
                )
            } else {
                daily.degreeDay
            },
            sun = newSun,
            twilight = getCalculatedAstroSun(
                daily.date,
                location.longitude,
                location.latitude,
                SunTimes.Twilight.CIVIL
            ),
            moon = getCalculatedAstroMoon(daily.date, location.longitude, location.latitude),
            moonPhase = getCalculatedMoonPhase(daily.date),
            airQuality = daily.airQuality ?: getDailyAirQualityFromHourlyList(
                hourlyAirQuality.filter { it.key.getIsoFormattedDate(location) == theDayFormatted }.values
            ),
            pollen = daily.pollen ?: if (hourlyPollen.isEmpty() &&
                currentPollen != null &&
                todayFormatted == theDayFormatted
            ) {
                getDailyPollenFromHourlyList(listOf(currentPollen), byPassSizeCheck = true)
            } else if (hourlyPollen.isNotEmpty()) {
                getDailyPollenFromHourlyList(
                    hourlyPollen.filter { it.key.getIsoFormattedDate(location) == theDayFormatted }.values
                )
            } else {
                null
            },
            uV = if (daily.uV?.index?.ensurePositive() != null) {
                daily.uV
            } else {
                getDailyUVFromHourlyList(hourlyListByDay.getOrElse(theDayFormatted) { null })
            },
            sunshineDuration = daily.sunshineDuration?.toValidDailyOrNull()
                ?: getSunshineDuration(
                    hourlySunshine.filter { it.key.getIsoFormattedDate(location) == theDayFormatted }.values
                ),
            relativeHumidity = getDailyRelativeHumidity(
                daily.relativeHumidity,
                hourlyListByDay.getOrElse(theDayFormatted) { null }?.mapNotNull { it.relativeHumidity }
            ),
            dewPoint = getDailyDewPoint(
                daily.dewPoint,
                hourlyListByDay.getOrElse(theDayFormatted) { null }?.mapNotNull { it.dewPoint }
            ),
            pressure = getDailyPressure(
                daily.pressure,
                hourlyListByDay.getOrElse(theDayFormatted) { null }?.mapNotNull { it.pressure }
            ),
            cloudCover = getDailyCloudCover(
                daily.cloudCover,
                hourlyListByDay.getOrElse(theDayFormatted) { null }?.mapNotNull { it.cloudCover }
            ),
            visibility = getDailyVisibility(
                daily.visibility,
                hourlyListByDay.getOrElse(theDayFormatted) { null }?.mapNotNull { it.visibility }
            )
        )
    }
}

private const val HEATING_DEGREE_DAY_BASE_TEMP = 180.0
private const val COOLING_DEGREE_DAY_BASE_TEMP = 210.0
private const val DEGREE_DAY_TEMP_MARGIN = 30.0

private fun getDegreeDay(
    minTemp: Temperature?,
    maxTemp: Temperature?,
): DegreeDay? {
    if (minTemp == null || maxTemp == null) return null

    val meanTemp = (minTemp.value + maxTemp.value) / 2.0
    if (meanTemp in
        (HEATING_DEGREE_DAY_BASE_TEMP - DEGREE_DAY_TEMP_MARGIN)..(COOLING_DEGREE_DAY_BASE_TEMP + DEGREE_DAY_TEMP_MARGIN)
    ) {
        return DegreeDay(heating = 0.deciCelsius, cooling = 0.deciCelsius)
    }

    return if (meanTemp < HEATING_DEGREE_DAY_BASE_TEMP) {
        DegreeDay(heating = (HEATING_DEGREE_DAY_BASE_TEMP - meanTemp).deciCelsius, cooling = 0.deciCelsius)
    } else {
        DegreeDay(heating = 0.deciCelsius, cooling = (meanTemp - COOLING_DEGREE_DAY_BASE_TEMP).deciCelsius)
    }
}

/**
 * Return 00:00:00.00 to 23:59:59.999 if sun is always up (assuming date parameter is at 00:00)
 * Takes 5 to 40 ms to execute on my device
 * Means that for a 15-day forecast, take between 0.1 and 0.6 sec
 */
private fun getCalculatedAstroSun(
    date: Date,
    longitude: Double,
    latitude: Double,
    twilight: SunTimes.Twilight = SunTimes.Twilight.VISUAL,
): Astro {
    val riseTimes = SunTimes.compute().twilight(twilight).on(date).at(latitude, longitude).execute()

    if (riseTimes.isAlwaysUp) {
        return Astro(
            riseDate = date,
            setDate = Date(date.time + 1.days.inWholeMilliseconds - 1)
        )
    }

    // If we miss the rise time, it means we are leaving midnight sun season
    if (riseTimes.rise == null && riseTimes.set != null) {
        return Astro(
            riseDate = date, // Setting 00:00 as rise date
            setDate = riseTimes.set
        )
    }

    if (riseTimes.rise != null && riseTimes.set != null && riseTimes.set!! < riseTimes.rise) {
        val setTimes = SunTimes.compute().twilight(twilight).on(riseTimes.rise).at(latitude, longitude).execute()
        if (setTimes.set != null) {
            return Astro(
                riseDate = riseTimes.rise,
                setDate = setTimes.set
            )
        }

        // If we miss the set time, redo a calculation that takes more computing power
        // Should not happen very often so avoid doing full cycle everytime
        val setTimes2 =
            SunTimes.compute().twilight(twilight).fullCycle().on(riseTimes.rise).at(latitude, longitude).execute()
        return Astro(
            riseDate = riseTimes.rise,
            setDate = setTimes2.set
        )
    }

    // If we miss the set time, redo a calculation that takes more computing power
    // Should not happen very often so avoid doing full cycle everytime
    if (riseTimes.rise != null && riseTimes.set == null) {
        val times2 =
            SunTimes.compute().twilight(twilight).fullCycle().on(riseTimes.rise).at(latitude, longitude).execute()
        return Astro(
            riseDate = times2.rise,
            setDate = times2.set
        )
    }

    return Astro(
        riseDate = riseTimes.rise,
        setDate = riseTimes.set
    )
}

private fun getCalculatedAstroMoon(
    date: Date,
    longitude: Double,
    latitude: Double,
): Astro {
    val riseTimes = MoonTimes.compute().on(date).at(latitude, longitude).execute()

    if (riseTimes.isAlwaysUp) {
        return Astro(
            riseDate = date,
            setDate = Date(date.time + 1.days.inWholeMilliseconds - 1)
        )
    }

    // If we miss the rise time, it means moon was already up before 00:00
    if (riseTimes.rise == null && riseTimes.set != null) {
        return Astro(
            riseDate = date, // Setting 00:00 as rise date
            setDate = riseTimes.set
        )
    }

    if (riseTimes.rise != null && riseTimes.set != null && riseTimes.set!! < riseTimes.rise) {
        val setTimes = MoonTimes.compute().on(riseTimes.rise).at(latitude, longitude).execute()
        if (setTimes.set != null) {
            return Astro(
                riseDate = riseTimes.rise,
                setDate = setTimes.set
            )
        }

        // If we miss the set time, redo a calculation that takes more computing power
        // Should not happen very often so avoid doing full cycle everytime
        val setTimes2 = MoonTimes.compute().fullCycle().on(riseTimes.rise).at(latitude, longitude).execute()
        return Astro(
            riseDate = riseTimes.rise,
            setDate = setTimes2.set
        )
    }

    // If we miss the set time, redo a calculation that takes more computing power
    // Should not happen very often so avoid doing full cycle everytime
    if (riseTimes.rise != null && riseTimes.set == null) {
        val times2 = MoonTimes.compute().fullCycle().on(riseTimes.rise).at(latitude, longitude).execute()
        return Astro(
            riseDate = times2.rise,
            setDate = times2.set
        )
    }

    return Astro(
        riseDate = riseTimes.rise,
        setDate = riseTimes.set
    )
}

/**
 * From a date initialized at 00:00, returns the moon phase angle at 12:00
 */
private fun getCalculatedMoonPhase(
    date: Date,
): MoonPhase {
    val illumination = MoonIllumination.compute()
        // Let’s take the middle of the day
        .on(Date(date.time + 12.hours.inWholeMilliseconds))
        .execute()

    return MoonPhase(
        angle = (illumination.phase + 180).roundToInt()
    )
}

private fun getHourlyListByHalfDay(
    hourlyList: List<Hourly>,
    location: Location,
): MutableMap<String, Map<String, MutableList<Hourly>>> {
    val hourlyByHalfDay: MutableMap<String, Map<String, MutableList<Hourly>>> = HashMap()

    hourlyList.forEach { hourly ->
        // We shift by 6 hours the hourly date, otherwise nighttime (00:00 to 05:59) would be on the wrong day
        val theDayShifted = Date(hourly.date.time - 6.hours.inWholeMilliseconds)
        val theDayFormatted = theDayShifted.getIsoFormattedDate(location)

        if (!hourlyByHalfDay.containsKey(theDayFormatted)) {
            hourlyByHalfDay[theDayFormatted] = hashMapOf(
                "day" to mutableListOf(),
                "night" to mutableListOf()
            )
        }
        if (theDayShifted.toCalendarWithTimeZone(location.timeZone).get(Calendar.HOUR_OF_DAY) < 12) {
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
 * @param halfDayHourlyList a List<Hourly> with only hourlys from 06:00 to 17:59 for day, and 18:00 to 05:59 for night
 * @param isDay true if you want day, false if you want night
 * @return a new List<Daily>, the initial dailyList passed as 1st parameter can be freed after
 */
private fun completeHalfDayFromHourlyList(
    initialHalfDay: HalfDay? = null,
    halfDayHourlyList: List<Hourly>? = null,
    isDay: Boolean,
): HalfDay? {
    if (halfDayHourlyList.isNullOrEmpty()) return initialHalfDay

    val newHalfDay = initialHalfDay ?: HalfDay()

    val initialTemperature = breezyweather.domain.weather.model.Temperature(
        temperature = newHalfDay.temperature?.temperature?.toValidOrNull(),
        sourceFeelsLike = newHalfDay.temperature?.sourceFeelsLike?.toValidOrNull()
    )
    val extremeTemperature = if (initialTemperature.temperature == null || initialTemperature.sourceFeelsLike == null) {
        getHalfDayTemperatureFromHourlyList(initialTemperature, halfDayHourlyList, isDay)
    } else {
        // No need to compute alternative feels like if real temp. and source feels like are there
        initialTemperature
    }

    val initialPrecipitation = newHalfDay.precipitation?.copy(
        total = newHalfDay.precipitation!!.total?.toValidHalfDayOrNull(),
        thunderstorm = newHalfDay.precipitation!!.thunderstorm?.toValidHalfDayOrNull(),
        rain = newHalfDay.precipitation!!.rain?.toValidHalfDayOrNull(),
        snow = newHalfDay.precipitation!!.snow?.toValidHalfDayOrNull(),
        ice = newHalfDay.precipitation!!.ice?.toValidHalfDayOrNull()
    )
    val totalPrecipitation = if (initialPrecipitation?.total == null) {
        getHalfDayPrecipitationFromHourlyList(halfDayHourlyList)
    } else {
        initialPrecipitation
    }

    val initialPrecipitationProbability = newHalfDay.precipitationProbability?.copy(
        total = newHalfDay.precipitationProbability!!.total?.toValidRangeOrNull(),
        thunderstorm = newHalfDay.precipitationProbability!!.thunderstorm?.toValidRangeOrNull(),
        rain = newHalfDay.precipitationProbability!!.rain?.toValidRangeOrNull(),
        snow = newHalfDay.precipitationProbability!!.snow?.toValidRangeOrNull(),
        ice = newHalfDay.precipitationProbability!!.ice?.toValidRangeOrNull()
    )
    val maxPrecipitationProbability = if (initialPrecipitationProbability?.total == null) {
        getHalfDayPrecipitationProbabilityFromHourlyList(halfDayHourlyList)
    } else {
        initialPrecipitationProbability
    }

    val precipitationDuration = newHalfDay.precipitationDuration?.copy(
        total = newHalfDay.precipitationDuration!!.total?.toValidHalfDayOrNull(),
        thunderstorm = newHalfDay.precipitationDuration!!.thunderstorm?.toValidHalfDayOrNull(),
        rain = newHalfDay.precipitationDuration!!.rain?.toValidHalfDayOrNull(),
        snow = newHalfDay.precipitationDuration!!.snow?.toValidHalfDayOrNull(),
        ice = newHalfDay.precipitationDuration!!.ice?.toValidHalfDayOrNull()
    )

    val initialWind = newHalfDay.wind?.validate()
    val maxWind = if (initialWind?.speed == null) {
        getHalfDayWindFromHourlyList(halfDayHourlyList)
    } else {
        initialWind
    }

    val halfDayWeatherCode = newHalfDay.weatherCode ?: getHalfDayWeatherCodeFromHourlyList(
        halfDayHourlyList,
        totalPrecipitation,
        maxPrecipitationProbability,
        maxWind,
        getHalfDayCloudCoverFromHourlyList(halfDayHourlyList),
        getHalfDayAvgVisibilityFromHourlyList(halfDayHourlyList)
    )
    /*if (BreezyWeather.instance.debugMode) {
        LogHelper.log(msg = "code $halfDayWeatherCode when\n" +
                "p: ${totalPrecipitation.total}, pop: ${maxPrecipitationProbability.total}, " +
                "wind: ${maxWind?.speed}, vis: $avgVisibility, clouds: $avgCloudCover")
    }*/

    val halfDayWeatherTextFromCode = halfDayWeatherCode?.let { WeatherViewController.getWeatherText(it) }
    val halfDayWeatherText = if (newHalfDay.weatherText.isNullOrEmpty()) {
        halfDayWeatherTextFromCode
    } else {
        newHalfDay.weatherText
    }
    val halfDayweatherSummary = if (newHalfDay.weatherSummary.isNullOrEmpty()) {
        if (newHalfDay.weatherText.isNullOrEmpty()) {
            halfDayWeatherTextFromCode
        } else {
            newHalfDay.weatherText
        }
    } else {
        newHalfDay.weatherSummary
    }

    return newHalfDay.copy(
        weatherText = halfDayWeatherText,
        weatherSummary = halfDayweatherSummary,
        weatherCode = halfDayWeatherCode,
        temperature = extremeTemperature,
        precipitation = totalPrecipitation,
        precipitationProbability = maxPrecipitationProbability,
        precipitationDuration = precipitationDuration,
        wind = maxWind
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
    halfDayHourlyList: List<Hourly>,
    totPrecipitation: Precipitation?,
    maxPrecipitationProbability: PrecipitationProbability?,
    maxWind: Wind?,
    avgCloudCover: Ratio?,
    avgVisibility: Distance?,
): WeatherCode? {
    val minPrecipIntensity = 1.0.millimeters
    val minPrecipProbability = 30.percent
    val maxVisibilityHaze = 5000.meters
    val maxVisibilityFog = 1000.meters
    val maxWindSpeedWindy = 10.metersPerSecond

    // If total precipitation is greater or equal than 1 mm
    // and max probability is greater than 30 % (assume 100 % if not reported)
    if ((totPrecipitation?.total ?: 1.0.millimeters) >= minPrecipIntensity &&
        (maxPrecipitationProbability?.total ?: 100.percent) > minPrecipProbability
    ) {
        val isRain = maxPrecipitationProbability?.rain?.let { it > minPrecipProbability }
            ?: totPrecipitation!!.rain?.let { it.value > 0 }
            ?: false
        val isSnow = maxPrecipitationProbability?.snow?.let { it > minPrecipProbability }
            ?: totPrecipitation!!.snow?.let { it.value > 0 }
            ?: false
        val isIce = maxPrecipitationProbability?.ice?.let { it > minPrecipProbability }
            ?: totPrecipitation!!.ice?.let { it.value > 0 }
            ?: false
        val isThunder = maxPrecipitationProbability?.thunderstorm?.let { it > minPrecipProbability }
            ?: totPrecipitation!!.thunderstorm?.let { it.value > 0 }
            ?: false

        if (isRain || isSnow || isIce || isThunder) {
            return if (isThunder) {
                if (isRain || isSnow || isIce) WeatherCode.THUNDERSTORM else WeatherCode.THUNDER
            } else if (isIce) {
                WeatherCode.HAIL
            } else if (isSnow) {
                if (isRain) WeatherCode.SLEET else WeatherCode.SNOW
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
                if ((counts[0] > 1 && counts[2] > 1) || counts[1] > 1) {
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
    if (maxWind?.speed != null && maxWind.speed!! > maxWindSpeedWindy) {
        return WeatherCode.WIND
    }

    // It’s not raining, it’s not windy, and it’s not mysterious. Just cloudy
    if (avgCloudCover != null) {
        if (avgCloudCover > CLOUD_COVER_BKN.percent) return WeatherCode.CLOUDY
        if (avgCloudCover > CLOUD_COVER_FEW.percent) return WeatherCode.PARTLY_CLOUDY
        return WeatherCode.CLEAR
    }

    // No precipitation, visibility, wind, or cloud cover data
    // let’s fallback to the median
    val hourlyListWithWeatherCode = halfDayHourlyList.filter { it.weatherCode != null }
    return if (hourlyListWithWeatherCode.isNotEmpty()) {
        hourlyListWithWeatherCode[
            if (hourlyListWithWeatherCode.size % 2 == 0) {
                hourlyListWithWeatherCode.size / 2
            } else {
                (hourlyListWithWeatherCode.size - 1) / 2
            }
        ].weatherCode
    } else {
        WeatherCode.PARTLY_CLOUDY
    }
}

/**
 * We complete everything except RealFeel and RealFeelShade which are AccuWeather-specific
 * and that we know is already completed
 */
private fun getHalfDayTemperatureFromHourlyList(
    initialTemperature: breezyweather.domain.weather.model.Temperature?,
    halfDayHourlyList: List<Hourly>,
    isDay: Boolean,
): breezyweather.domain.weather.model.Temperature {
    val newTemperature = initialTemperature ?: breezyweather.domain.weather.model.Temperature()

    var temperatureTemperature = newTemperature.temperature?.toValidOrNull()
    var temperatureSourceFeelsLike = newTemperature.sourceFeelsLike?.toValidOrNull()
    var temperatureApparent = newTemperature.computedApparent
    var temperatureWindChill = newTemperature.computedWindChill
    var temperatureComputedHumidex = newTemperature.computedHumidex

    if (temperatureTemperature == null) {
        val halfDayHourlyListTemperature = halfDayHourlyList.mapNotNull { it.temperature?.temperature }
        temperatureTemperature = if (isDay) {
            halfDayHourlyListTemperature.maxOrNull()
        } else {
            halfDayHourlyListTemperature.minOrNull()
        }
    }
    if (temperatureSourceFeelsLike == null) {
        val halfDayHourlyListSourceFeelsLike = halfDayHourlyList.mapNotNull {
            it.temperature?.sourceFeelsLike
        }
        temperatureSourceFeelsLike = if (halfDayHourlyListSourceFeelsLike.size == halfDayHourlyList.size) {
            if (isDay) {
                halfDayHourlyListSourceFeelsLike.maxOrNull()
            } else {
                halfDayHourlyListSourceFeelsLike.minOrNull()
            }
        } else {
            null
        }
    }
    if (temperatureApparent == null) {
        val halfDayHourlyListApparent = halfDayHourlyList.mapNotNull { it.temperature?.computedApparent }
        temperatureApparent = if (isDay) {
            halfDayHourlyListApparent.maxOrNull()
        } else {
            halfDayHourlyListApparent.minOrNull()
        }
    }
    if (temperatureWindChill == null) {
        val halfDayHourlyListWindChill = halfDayHourlyList.mapNotNull {
            it.temperature?.computedWindChill
        }
        temperatureWindChill = if (isDay) {
            halfDayHourlyListWindChill.maxOrNull()
        } else {
            halfDayHourlyListWindChill.minOrNull()
        }
    }
    if (temperatureComputedHumidex == null) {
        val halfDayHourlyListComputedHumidex = halfDayHourlyList.mapNotNull { it.temperature?.computedHumidex }
        temperatureComputedHumidex = if (isDay) {
            halfDayHourlyListComputedHumidex.maxOrNull()
        } else {
            halfDayHourlyListComputedHumidex.minOrNull()
        }
    }
    return newTemperature.copy(
        temperature = temperatureTemperature,
        sourceFeelsLike = temperatureSourceFeelsLike,
        computedApparent = temperatureApparent,
        computedWindChill = temperatureWindChill,
        computedHumidex = temperatureComputedHumidex
    )
}

private fun getHalfDayPrecipitationFromHourlyList(
    halfDayHourlyList: List<Hourly>,
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
            halfDayHourlyListPrecipitationTotal.sumOf { it.value }.micrometers
        } else {
            null
        },
        thunderstorm = if (halfDayHourlyListPrecipitationThunderstorm.isNotEmpty()) {
            halfDayHourlyListPrecipitationThunderstorm.sumOf { it.value }.micrometers
        } else {
            null
        },
        rain = if (halfDayHourlyListPrecipitationRain.isNotEmpty()) {
            halfDayHourlyListPrecipitationRain.sumOf { it.value }.micrometers
        } else {
            null
        },
        snow = if (halfDayHourlyListPrecipitationSnow.isNotEmpty()) {
            halfDayHourlyListPrecipitationSnow.sumOf { it.value }.micrometers
        } else {
            null
        },
        ice = if (halfDayHourlyListPrecipitationIce.isNotEmpty()) {
            halfDayHourlyListPrecipitationIce.sumOf { it.value }.micrometers
        } else {
            null
        }
    )
}

private fun getHalfDayPrecipitationProbabilityFromHourlyList(
    halfDayHourlyList: List<Hourly>,
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
    halfDayHourlyList: List<Hourly>,
): Wind? {
    val maxWind = halfDayHourlyList
        .filter { it.wind?.speed != null }
        .maxByOrNull { it.wind!!.speed!! }
        ?.wind
    val maxWindGusts = halfDayHourlyList
        .filter { it.wind?.gusts != null }
        .maxByOrNull { it.wind!!.gusts!! }
        ?.wind?.gusts
    return if (maxWindGusts == null || maxWindGusts == maxWind?.gusts) {
        maxWind
    } else {
        maxWind?.copy(
            gusts = maxWindGusts
        )
    }
}

private fun getHalfDayCloudCoverFromHourlyList(
    halfDayHourlyList: List<Hourly>,
): Ratio? {
    // average() would return NaN when called for an empty list
    return halfDayHourlyList.mapNotNull { it.cloudCover?.value }.takeIf { it.isNotEmpty() }?.average()?.permille
}

private fun getHalfDayAvgVisibilityFromHourlyList(
    halfDayHourlyList: List<Hourly>,
): Distance? {
    // average() would return NaN when called for an empty list
    return halfDayHourlyList.mapNotNull { it.visibility?.inMeters }.takeIf { it.isNotEmpty() }?.average()?.meters
}

/**
 * Returns an AirQuality object calculated from a List of Hourly for the day
 * (at least 18 non-null Hourly.AirQuality required)
 */
private fun getDailyAirQualityFromHourlyList(
    hourlyList: Collection<AirQuality>? = null,
): AirQuality? {
    // We need at least 18 hours for a signification estimation
    if (hourlyList.isNullOrEmpty() || hourlyList.size < 18) return null

    // average() would return NaN when called for an empty list
    return AirQuality(
        pM25 = hourlyList.mapNotNull { it.pM25?.inMicrogramsPerCubicMeter }.takeIf { it.isNotEmpty() }?.average()
            ?.microgramsPerCubicMeter,
        pM10 = hourlyList.mapNotNull { it.pM10?.inMicrogramsPerCubicMeter }.takeIf { it.isNotEmpty() }?.average()
            ?.microgramsPerCubicMeter,
        sO2 = hourlyList.mapNotNull { it.sO2?.inMicrogramsPerCubicMeter }.takeIf { it.isNotEmpty() }?.average()
            ?.microgramsPerCubicMeter,
        nO2 = hourlyList.mapNotNull { it.nO2?.inMicrogramsPerCubicMeter }.takeIf { it.isNotEmpty() }?.average()
            ?.microgramsPerCubicMeter,
        o3 = hourlyList.mapNotNull { it.o3?.inMicrogramsPerCubicMeter }.takeIf { it.isNotEmpty() }?.average()
            ?.microgramsPerCubicMeter,
        cO = hourlyList.mapNotNull { it.cO?.inMicrogramsPerCubicMeter }.takeIf { it.isNotEmpty() }?.average()
            ?.microgramsPerCubicMeter
    )
}

/**
 * Returns a Pollen object calculated from a List of Hourly for the day
 * (at least 18 non-null Hourly.Pollen required)
 */
private fun getDailyPollenFromHourlyList(
    hourlyList: Collection<Pollen>? = null,
    byPassSizeCheck: Boolean = false,
): Pollen? {
    // We need at least 18 hours for a signification estimation
    if (hourlyList.isNullOrEmpty() || (hourlyList.size < 18 && !byPassSizeCheck)) return null

    return Pollen(
        alder = hourlyList.mapNotNull { it.alder }.maxOrNull(),
        ash = hourlyList.mapNotNull { it.ash }.maxOrNull(),
        birch = hourlyList.mapNotNull { it.birch }.maxOrNull(),
        chestnut = hourlyList.mapNotNull { it.chestnut }.maxOrNull(),
        cypress = hourlyList.mapNotNull { it.cypress }.maxOrNull(),
        grass = hourlyList.mapNotNull { it.grass }.maxOrNull(),
        hazel = hourlyList.mapNotNull { it.hazel }.maxOrNull(),
        hornbeam = hourlyList.mapNotNull { it.hornbeam }.maxOrNull(),
        linden = hourlyList.mapNotNull { it.linden }.maxOrNull(),
        mold = hourlyList.mapNotNull { it.mold }.maxOrNull(),
        mugwort = hourlyList.mapNotNull { it.mugwort }.maxOrNull(),
        oak = hourlyList.mapNotNull { it.oak }.maxOrNull(),
        olive = hourlyList.mapNotNull { it.olive }.maxOrNull(),
        plane = hourlyList.mapNotNull { it.plane }.maxOrNull(),
        plantain = hourlyList.mapNotNull { it.plantain }.maxOrNull(),
        poplar = hourlyList.mapNotNull { it.poplar }.maxOrNull(),
        ragweed = hourlyList.mapNotNull { it.ragweed }.maxOrNull(),
        sorrel = hourlyList.mapNotNull { it.sorrel }.maxOrNull(),
        tree = hourlyList.mapNotNull { it.tree }.maxOrNull(),
        urticaceae = hourlyList.mapNotNull { it.urticaceae }.maxOrNull()
    )
}

/**
 * Returns the max UV for the day from a list of hourly
 */
private fun getDailyUVFromHourlyList(
    hourlyList: List<Hourly>? = null,
): UV? {
    if (hourlyList.isNullOrEmpty()) return null
    val hourlyListWithUV = hourlyList.mapNotNull { it.uV?.index }
    if (hourlyListWithUV.isEmpty()) return null

    return UV(index = hourlyListWithUV.max())
}

/**
 * Returns the sunshine duration if present in hourlyList
 *
 * @param hourlyList hourly list for that day
 */
private fun getSunshineDuration(
    hourlyList: Collection<Duration?>?,
): Duration? {
    return if (hourlyList != null) {
        val hourlyWithSunshine = hourlyList.filterNotNull()
        if (hourlyWithSunshine.isNotEmpty()) {
            hourlyWithSunshine.sumOf { it.inWholeNanoseconds }.nanoseconds
        } else {
            null
        }
    } else {
        null
    }
}

fun getDailyRelativeHumidity(
    initialDailyRelativeHumidity: DailyRelativeHumidity?,
    values: List<Ratio>?,
): DailyRelativeHumidity? {
    if (values.isNullOrEmpty()) return initialDailyRelativeHumidity

    return DailyRelativeHumidity(
        average = initialDailyRelativeHumidity?.average?.toValidRangeOrNull()
            ?: values.map { it.value }.average().permille,
        min = initialDailyRelativeHumidity?.min?.toValidRangeOrNull()
            ?: values.min(),
        max = initialDailyRelativeHumidity?.max?.toValidRangeOrNull()
            ?: values.max()
    )
}

fun getDailyDewPoint(
    initialDailyDewPoint: DailyDewPoint?,
    values: List<Temperature>?,
): DailyDewPoint? {
    if (values.isNullOrEmpty()) return initialDailyDewPoint

    return DailyDewPoint(
        average = initialDailyDewPoint?.average?.toValidOrNull()
            ?: values.map { it.value }.average().deciCelsius,
        min = initialDailyDewPoint?.min?.toValidOrNull()
            ?: values.min(),
        max = initialDailyDewPoint?.max?.toValidOrNull()
            ?: values.max()
    )
}

fun getDailyPressure(
    initialDailyPressure: DailyPressure?,
    values: List<Pressure>?,
): DailyPressure? {
    if (values.isNullOrEmpty()) return initialDailyPressure

    return DailyPressure(
        average = initialDailyPressure?.average?.toValidOrNull()
            ?: values.map { it.value }.average().pascals,
        min = initialDailyPressure?.min?.toValidOrNull()
            ?: values.minOfOrNull { it.value }?.pascals,
        max = initialDailyPressure?.max?.toValidOrNull()
            ?: values.maxOfOrNull { it.value }?.pascals
    )
}

fun getDailyCloudCover(
    initialDailyCloudCover: DailyCloudCover?,
    values: List<Ratio>?,
): DailyCloudCover? {
    if (values.isNullOrEmpty()) return initialDailyCloudCover

    return DailyCloudCover(
        average = initialDailyCloudCover?.average?.toValidRangeOrNull()
            ?: values.map { it.value }.average().permille,
        min = initialDailyCloudCover?.min?.toValidRangeOrNull()
            ?: values.min(),
        max = initialDailyCloudCover?.max?.toValidRangeOrNull()
            ?: values.max()
    )
}

fun getDailyVisibility(
    initialDailyVisibility: DailyVisibility?,
    values: List<Distance>?,
): DailyVisibility? {
    if (values.isNullOrEmpty()) return initialDailyVisibility

    return DailyVisibility(
        average = initialDailyVisibility?.average?.toValidOrNull()
            ?: values.map { it.value }.average().meters,
        min = initialDailyVisibility?.min?.toValidOrNull()
            ?: values.minOfOrNull { it.value }?.meters,
        max = initialDailyVisibility?.max?.toValidOrNull()
            ?: values.maxOfOrNull { it.value }?.meters
    )
}

/*
 * HOURLY FROM DAILY
 */

/**
 * Completes hourly data from daily data:
 * - isDaylight
 * - UV field
 * Completes hourly data with air quality from WeatherWrapper
 *
 * Also removes hourlys in the past (30 min margin)
 *
 * @param hourlyList hourly data
 * @param dailyList daily data
 * @param hourlyAirQuality hourly air quality data from WeatherWrapper
 * @param location timeZone of the location
 */
internal fun completeHourlyListFromDailyList(
    hourlyList: List<Hourly>,
    dailyList: List<Daily>,
    hourlyAirQuality: Map<Date, AirQuality>,
    location: Location,
): List<Hourly> {
    val dailyListByDate = dailyList.groupBy { it.date.getIsoFormattedDate(location) }
        .mapValues { it.value.first().let { day -> Pair(day.uV, day.sun) } }
    return hourlyList.map { hourly ->
        val dateForHourFormatted = hourly.date.getIsoFormattedDate(location)
        dailyListByDate.getOrElse(dateForHourFormatted) { null }?.let { daily ->
            hourly.copy(
                airQuality = hourlyAirQuality.getOrElse(hourly.date) { null },
                isDaylight = isDaylight(daily.second?.riseDate, daily.second?.setDate, hourly.date),
                uV = if (hourly.uV?.index?.ensurePositive() != null) {
                    hourly.uV
                } else {
                    getCurrentUVFromDayMax(
                        daily.first?.index,
                        hourly.date,
                        daily.second?.riseDate,
                        daily.second?.setDate,
                        location.timeZone
                    )
                }
            )
        } ?: hourly
    }
}

/**
 * From sunrise and sunset times, returns true if current time is daytime
 * Returns false if no sunrise or sunset (polar night)
 * If sunrise after sunset (should not happen), returns true
 */
private fun isDaylight(
    sunrise: Date?,
    sunset: Date?,
    current: Date,
): Boolean {
    if (sunrise == null || sunset == null) return false
    if (sunrise.after(sunset)) return true

    return current.time in sunrise.time until sunset.time
}

/**
 * Returns an estimated UV index for current time from max UV of the day
 */
private fun getCurrentUVFromDayMax(
    dayMaxUV: Double?,
    currentDate: Date?,
    sunriseDate: Date?,
    sunsetDate: Date?,
    timeZone: TimeZone,
): UV? {
    if (dayMaxUV == null ||
        currentDate == null ||
        sunriseDate == null ||
        sunsetDate == null ||
        sunriseDate.after(sunsetDate) ||
        currentDate !in sunriseDate..sunsetDate
    ) {
        return null
    }

    val calendar = Calendar.getInstance(timeZone)

    calendar.time = currentDate
    val currentTime = calendar[Calendar.HOUR_OF_DAY] + calendar[Calendar.MINUTE] / 60f // 1 minute approx. is enough

    calendar.time = sunriseDate
    val sunRiseTime = calendar[Calendar.HOUR_OF_DAY] + calendar[Calendar.MINUTE] / 60f

    calendar.time = sunsetDate
    val sunSetTime = calendar[Calendar.HOUR_OF_DAY] + calendar[Calendar.MINUTE] / 60f

    val sunlightDuration = sunSetTime - sunRiseTime
    val currentOffset = currentTime - sunRiseTime

    // Make θ = −π at sunrise, θ = 0 at local noon, and θ = +π at sunset
    // such that cos(θ) is at minimum at sunrise and sunset, and maximum at noon
    val theta = (currentOffset / sunlightDuration) * 2.0 * Math.PI - Math.PI

    // cos(θ) output is between −1 and 1
    // We normalize so that dayMaxUV is multiplied by a value between 0 and 1
    val currentUV = dayMaxUV * (cos(theta) + 1) / 2

    return UV(index = currentUV)
}

/*
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
 * @param location the location
 */
internal fun completeCurrentFromHourlyData(
    initialCurrent: CurrentWrapper?,
    hourly: Hourly?,
    todayDaily: Daily?,
    currentAirQuality: AirQuality?,
    location: Location,
): Current {
    val newCurrent = initialCurrent ?: CurrentWrapper()
    if (hourly == null) {
        return newCurrent.toCurrent(
            uV = if (newCurrent.uV?.index?.ensurePositive() == null && todayDaily != null) {
                getCurrentUVFromDayMax(
                    todayDaily.uV?.index,
                    Date(),
                    todayDaily.sun?.riseDate,
                    todayDaily.sun?.setDate,
                    location.timeZone
                )
            } else {
                newCurrent.uV
            }
        )
    }

    val initialTemp = newCurrent.temperature?.temperature?.toValidOrNull()
    val initialFeelsLike = newCurrent.temperature?.feelsLike?.toValidOrNull()
    val initialWind = newCurrent.wind?.validate()
    val newWind = if (initialWind?.speed != null || hourly.wind?.speed == null) initialWind else hourly.wind
    val newRelativeHumidity = newCurrent.relativeHumidity?.toValidRangeOrNull()
        ?: hourly.relativeHumidity
    val newDewPoint = newCurrent.dewPoint?.toValidOrNull()
        ?: if (newRelativeHumidity != null || initialTemp != null) {
            // If current data is available, we compute this over hourly dewpoint
            computeDewPoint(initialTemp ?: hourly.temperature?.temperature, newRelativeHumidity)
        } else {
            // Already calculated earlier
            hourly.dewPoint
        }
    val newTemperature = completeCurrentTemperatureFromHourly(
        initialTemp,
        initialFeelsLike,
        hourly.temperature,
        newWind?.speed,
        newRelativeHumidity,
        newDewPoint
    )
    return newCurrent.toCurrent(
        uV = if (newCurrent.uV?.index?.ensurePositive() == null && todayDaily != null) {
            getCurrentUVFromDayMax(
                todayDaily.uV?.index,
                Date(),
                todayDaily.sun?.riseDate,
                todayDaily.sun?.setDate,
                location.timeZone
            )
        } else {
            newCurrent.uV
        }
    ).copy(
        weatherText = if (newCurrent.weatherText.isNullOrEmpty()) {
            newCurrent.weatherCode?.let {
                WeatherViewController.getWeatherText(it)
            } ?: hourly.weatherCode?.let {
                WeatherViewController.getWeatherText(it)
            } ?: hourly.weatherText
        } else {
            newCurrent.weatherText
        },
        weatherCode = newCurrent.weatherCode ?: hourly.weatherCode,
        temperature = newTemperature,
        wind = newWind,
        airQuality = currentAirQuality ?: hourly.airQuality,
        relativeHumidity = newRelativeHumidity,
        dewPoint = newDewPoint,
        pressure = newCurrent.pressure?.toValidOrNull() ?: hourly.pressure,
        cloudCover = newCurrent.cloudCover?.toValidRangeOrNull() ?: hourly.cloudCover,
        visibility = newCurrent.visibility?.toValidOrNull() ?: hourly.visibility,
        ceiling = newCurrent.ceiling?.toValidOrNull()
    )
}

private fun completeCurrentTemperatureFromHourly(
    initialTemp: Temperature?,
    initialFeelsLike: Temperature?,
    hourlyTemperature: breezyweather.domain.weather.model.Temperature?,
    windSpeed: Speed?,
    relativeHumidity: Ratio?,
    dewPoint: Temperature?,
): breezyweather.domain.weather.model.Temperature? {
    if (initialTemp == null) return hourlyTemperature

    return breezyweather.domain.weather.model.Temperature(
        temperature = initialTemp,
        sourceFeelsLike = initialFeelsLike ?: hourlyTemperature?.sourceFeelsLike,
        computedApparent = computeApparentTemperature(initialTemp, relativeHumidity, windSpeed),
        computedWindChill = computeWindChillTemperature(initialTemp, windSpeed),
        computedHumidex = computeHumidex(initialTemp, dewPoint)
    )
}
