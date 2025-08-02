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

import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Astro
import breezyweather.domain.weather.model.Current
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.DegreeDay
import breezyweather.domain.weather.model.HalfDay
import breezyweather.domain.weather.model.Hourly
import breezyweather.domain.weather.model.MoonPhase
import breezyweather.domain.weather.model.Pollen
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Weather
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import org.breezyweather.common.extensions.getIsoFormattedDate
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.domain.weather.index.PollutantIndex
import org.breezyweather.ui.theme.weatherView.WeatherViewController
import org.shredzone.commons.suncalc.MoonIllumination
import org.shredzone.commons.suncalc.MoonTimes
import org.shredzone.commons.suncalc.SunTimes
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

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

/**
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

/**
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
 */
internal fun computeMissingHourlyData(
    hourlyList: List<HourlyWrapper>?,
): List<Hourly>? {
    return hourlyList?.map { hourly ->
        val weatherCode = hourly.weatherCode ?: getHalfDayWeatherCodeFromHourlyList(
            listOf(hourly.toHourly()),
            hourly.precipitation,
            hourly.precipitationProbability,
            hourly.wind,
            hourly.cloudCover,
            hourly.visibility
        )
        val relativeHumidity = hourly.relativeHumidity
            ?: computeRelativeHumidity(hourly.temperature?.temperature, hourly.dewPoint)
        val dewPoint = hourly.dewPoint
            ?: computeDewPoint(hourly.temperature?.temperature, hourly.relativeHumidity)

        hourly.toHourly().copy(
            weatherCode = weatherCode,
            weatherText = if (hourly.weatherText.isNullOrEmpty()) {
                weatherCode?.let { WeatherViewController.getWeatherText(it) }
            } else {
                hourly.weatherText
            },
            relativeHumidity = relativeHumidity,
            dewPoint = dewPoint,
            temperature = hourly.temperature?.toTemperature(
                computedApparent = computeApparentTemperature(
                    hourly.temperature!!.temperature,
                    relativeHumidity,
                    hourly.wind?.speed
                ),
                computedWindChill = computeWindChillTemperature(hourly.temperature!!.temperature, hourly.wind?.speed),
                computedHumidex = computeHumidex(hourly.temperature!!.temperature, dewPoint)
            )
        )
    }
}

/**
 * Compute relative humidity from temperature and dew point
 * Uses Magnus approximation with Arden Buck best variable set
 * TODO: Unit test
 *
 * @param temperature in °C
 * @param dewPoint in °C
 */
private fun computeRelativeHumidity(
    temperature: Double?,
    dewPoint: Double?,
): Double? {
    if (temperature == null || dewPoint == null) return null

    val b = if (temperature < 0) 17.966 else 17.368
    val c = if (temperature < 0) 227.15 else 238.88 // °C

    return 100 * (exp((b * dewPoint).div(c + dewPoint)) / exp((b * temperature).div(c + temperature)))
}

/**
 * Compute dew point from temperature and relative humidity
 * Uses Magnus approximation with Arden Buck best variable set
 * TODO: Unit test
 *
 * @param temperature in °C
 * @param relativeHumidity in %
 */
private fun computeDewPoint(temperature: Double?, relativeHumidity: Double?): Double? {
    if (temperature == null || relativeHumidity == null) return null

    val b = if (temperature < 0) 17.966 else 17.368
    val c = if (temperature < 0) 227.15 else 238.88 // °C

    val magnus = ln(relativeHumidity / 100) + (b * temperature) / (c + temperature)
    return ((c * magnus) / (b - magnus))
}

/**
 * Compute apparent temperature from temperature, relative humidity, and wind speed
 * Uses Bureau of Meteorology Australia methodology
 * Source: http://www.bom.gov.au/info/thermal_stress/#atapproximation
 * TODO: Unit test
 *
 * @param temperature in °C
 * @param relativeHumidity in %
 * @param windSpeed in m/s
 */
internal fun computeApparentTemperature(
    temperature: Double?,
    relativeHumidity: Double?,
    windSpeed: Double?,
): Double? {
    if (temperature == null || relativeHumidity == null || windSpeed == null) return null

    val e = relativeHumidity / 100 * 6.105 * exp(17.27 * temperature / (237.7 + temperature))
    return temperature + 0.33 * e - 0.7 * windSpeed - 4.0
}

/**
 * Compute wind chill from temperature and wind speed
 * Uses Environment Canada methodology
 * Source: https://climate.weather.gc.ca/glossary_e.html#w
 * Only valid for (T ≤ 0°C) or (T ≤ 10°C and WS ≥ 5km/h)
 * TODO: Unit test
 *
 * @param temperature in °C
 * @param windSpeed in m/s
 */
internal fun computeWindChillTemperature(
    temperature: Double?,
    windSpeed: Double?,
): Double? {
    if (temperature == null || windSpeed == null || temperature > 10) return null
    val windSpeedKph = windSpeed * 3.6

    return if (windSpeedKph >= 5.0) {
        13.12 +
            (0.6215 * temperature) -
            (11.37 * windSpeedKph.pow(0.16)) +
            (0.3965 * temperature * windSpeedKph.pow(0.16))
    } else if (temperature <= 0.0) {
        temperature + ((-1.59 + 0.1345 * temperature) / 5.0) * windSpeedKph
    } else {
        null
    }
}

/**
 * Compute humidex from temperature and humidity
 * Based on formula from ECCC
 *
 * @param temperature in °C
 * @param dewPoint in °C
 */
internal fun computeHumidex(
    temperature: Double?,
    dewPoint: Double?,
): Double? {
    if (temperature == null || dewPoint == null || temperature < 15) return null

    return temperature +
        0.5555.times(
            6.11.times(
                exp(5417.7530.times(1.div(273.15) - 1.div(273.15 + dewPoint)))
            ).minus(10)
        )
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

/**
 * Compute mean sea level pressure (MSLP) from barometric pressure and altitude.
 * Optional elements can be provided for minor adjustments.
 * Source: https://integritext.net/DrKFS/correctiontosealevel.htm
 *
 * To compute barometric pressure from MSLP,
 * simply enter negative altitude.
 *
 * @param barometricPressure in hPa
 * @param altitude in meters
 * @param temperature in °C (optional)
 * @param humidity in % (optional)
 * @param latitude in ° (optional)
 */
internal fun computeMeanSeaLevelPressure(
    barometricPressure: Double?,
    altitude: Double?,
    temperature: Double? = null,
    humidity: Double? = null,
    latitude: Double? = null,
): Double? {
    // There is nothing to calculate if barometric pressure or altitude is null.
    if (barometricPressure == null || altitude == null) {
        return null
    }

    // Source: http://www.bom.gov.au/info/thermal_stress/#atapproximation
    val waterVaporPressure = if (humidity != null && temperature != null) {
        humidity / 100 * 6.105 * exp(17.27 * temperature / (237.7 + temperature))
    } else {
        0.0
    }

    // adjustment for temperature
    val term1 = 1.0 + 0.0037 * (temperature ?: 0.0)

    // adjustment for humidity
    val term2 = 1.0 / (1.0 - 0.378 * waterVaporPressure / barometricPressure)

    // adjustment for asphericity of the Earth
    val term3 = 1.0 / (1.0 - 0.0026 * cos(2 * (latitude ?: 45.0) * Math.PI / 180))

    // adjustment for variation of gravitational acceleration with height
    val term4 = 1.0 + (altitude / 6367324)

    return (10.0).pow(log10(barometricPressure) + altitude / (18400.0 * term1 * term2 * term3 * term4))
}

/**
 * Compute pollutant concentration in µg/m³ when given in ppb.
 * Can also be used for converting to mg/m³ from ppm.
 * Source: https://en.wikipedia.org/wiki/Useful_conversions_and_formulas_for_air_dispersion_modeling
 *
 * Basis for temperature and pressure assumptions:
 * https://www.ecfr.gov/current/title-40/chapter-I/subchapter-C/part-50/section-50.3
 *
 * @param pollutant one of NO2, O3, SO2 or CO
 * @param concentrationInPpb in ppb
 * @param temperature in °C (assumed 25 °C if omitted)
 * @param barometricPressure in hPa (assumed 1 atm = 1013.25 hPa if omitted)
 */
internal fun computePollutantInUgm3FromPpb(
    pollutant: PollutantIndex,
    concentrationInPpb: Double?,
    temperature: Double? = null,
    barometricPressure: Double? = null,
): Double? {
    if (concentrationInPpb == null) return null
    if (pollutant.molecularMass == null) return null
    return concentrationInPpb *
        pollutant.molecularMass /
        (8.31446261815324 / (barometricPressure ?: 1013.25) * 10) /
        (273.15 + (temperature ?: 25.0))
}

/**
 * Compute pollutant concentration in ppb from µg/m³
 * Can also be used for converting to ppm from mg/m³
 * Source: https://en.wikipedia.org/wiki/Useful_conversions_and_formulas_for_air_dispersion_modeling
 *
 * Basis for temperature and pressure assumptions:
 * https://www.ecfr.gov/current/title-40/chapter-I/subchapter-C/part-50/section-50.3
 *
 * @param pollutant one of NO2, O3, SO2 or CO
 * @param concentrationInUgm3 in µg/m³
 * @param temperature in °C (assumed 25 °C if omitted)
 * @param barometricPressure in hPa (assumed 1 atm = 1013.25 hPa if omitted)
 */
internal fun computePollutantInPpbFromUgm3(
    pollutant: PollutantIndex,
    concentrationInUgm3: Double?,
    temperature: Double? = null,
    barometricPressure: Double? = null,
): Double? {
    if (concentrationInUgm3 == null) return null
    if (pollutant.molecularMass == null) return null
    return concentrationInUgm3 /
        pollutant.molecularMass *
        (8.31446261815324 / (barometricPressure ?: 1013.25) * 10) *
        (273.15 + (temperature ?: 25.0))
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
    hourlySunshine: Map<Date, Double?>,
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
            uV = if (daily.uV?.index != null) {
                daily.uV
            } else {
                getDailyUVFromHourlyList(hourlyListByDay.getOrElse(theDayFormatted) { null })
            },
            sunshineDuration = daily.sunshineDuration
                ?: getSunshineDuration(
                    hourlySunshine.filter { it.key.getIsoFormattedDate(location) == theDayFormatted }.values
                )
        )
    }
}

private const val HEATING_DEGREE_DAY_BASE_TEMP = 18.0
private const val COOLING_DEGREE_DAY_BASE_TEMP = 21.0
private const val DEGREE_DAY_TEMP_MARGIN = 3.0

private fun getDegreeDay(
    minTemp: Double?,
    maxTemp: Double?,
): DegreeDay? {
    if (minTemp == null || maxTemp == null) return null

    val meanTemp = (minTemp + maxTemp) / 2
    if (meanTemp in
        (HEATING_DEGREE_DAY_BASE_TEMP - DEGREE_DAY_TEMP_MARGIN)..(COOLING_DEGREE_DAY_BASE_TEMP + DEGREE_DAY_TEMP_MARGIN)
    ) {
        return DegreeDay(heating = 0.0, cooling = 0.0)
    }

    return if (meanTemp < HEATING_DEGREE_DAY_BASE_TEMP) {
        DegreeDay(heating = (HEATING_DEGREE_DAY_BASE_TEMP - meanTemp), cooling = 0.0)
    } else {
        DegreeDay(heating = 0.0, cooling = (meanTemp - COOLING_DEGREE_DAY_BASE_TEMP))
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
        if (theDayShifted.toCalendarWithTimeZone(location.javaTimeZone).get(Calendar.HOUR_OF_DAY) < 12) {
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

    val extremeTemperature = if (newHalfDay.temperature?.temperature == null ||
        newHalfDay.temperature!!.computedApparent == null ||
        newHalfDay.temperature!!.computedWindChill == null ||
        newHalfDay.temperature!!.computedHumidex == null
    ) {
        getHalfDayTemperatureFromHourlyList(newHalfDay.temperature, halfDayHourlyList, isDay)
    } else {
        newHalfDay.temperature
    }

    val totalPrecipitation = if (newHalfDay.precipitation?.total == null) {
        getHalfDayPrecipitationFromHourlyList(halfDayHourlyList)
    } else {
        newHalfDay.precipitation
    }

    val maxPrecipitationProbability = if (newHalfDay.precipitationProbability?.total == null) {
        getHalfDayPrecipitationProbabilityFromHourlyList(halfDayHourlyList)
    } else {
        newHalfDay.precipitationProbability
    }

    val maxWind = if (newHalfDay.wind?.speed == null) {
        getHalfDayWindFromHourlyList(halfDayHourlyList)
    } else {
        newHalfDay.wind
    }

    val avgCloudCover = if (newHalfDay.precipitationProbability?.total == null) {
        getHalfDayCloudCoverFromHourlyList(halfDayHourlyList)
    } else {
        newHalfDay.cloudCover
    }

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

    val halfDayWeatherTextFromCode = halfDayWeatherCode?.let { WeatherViewController.getWeatherText(it) }
    val halfDayWeatherText = if (newHalfDay.weatherText.isNullOrEmpty()) {
        halfDayWeatherTextFromCode
    } else {
        newHalfDay.weatherText
    }
    val halfDayWeatherPhase = if (newHalfDay.weatherPhase.isNullOrEmpty()) {
        if (newHalfDay.weatherText.isNullOrEmpty()) {
            halfDayWeatherTextFromCode
        } else {
            newHalfDay.weatherText
        }
    } else {
        newHalfDay.weatherPhase
    }

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
    halfDayHourlyList: List<Hourly>,
    totPrecipitation: Precipitation?,
    maxPrecipitationProbability: PrecipitationProbability?,
    maxWind: Wind?,
    avgCloudCover: Int?,
    avgVisibility: Double?,
): WeatherCode? {
    val minPrecipIntensity = 1.0 // in mm
    val minPrecipProbability = 30.0 // in %
    val maxVisibilityHaze = 5000 // in m
    val maxVisibilityFog = 1000 // in m
    val maxWindSpeedWindy = 10.0 // in m/s
    val minCloudCoverPartlyCloudy = 37.5 // in %
    val minCloudCoverCloudy = 75.0 // in %

    // If total precipitation is greater than 1 mm
    // and max probability is greater than 30 % (assume 100 % if not reported)
    if ((totPrecipitation?.total ?: 0.0) > minPrecipIntensity &&
        (maxPrecipitationProbability?.total ?: 100.0) > minPrecipProbability
    ) {
        val isRain =
            maxPrecipitationProbability?.rain?.let { it > minPrecipProbability }
                ?: totPrecipitation!!.rain?.let { it > 0 }
                ?: false
        val isSnow =
            maxPrecipitationProbability?.snow?.let { it > minPrecipProbability }
                ?: totPrecipitation!!.snow?.let { it > 0 }
                ?: false
        val isIce =
            maxPrecipitationProbability?.ice?.let { it > minPrecipProbability }
                ?: totPrecipitation!!.ice?.let { it > 0 }
                ?: false
        val isThunder =
            maxPrecipitationProbability?.thunderstorm?.let { it > minPrecipProbability }
                ?: totPrecipitation!!.thunderstorm?.let { it > 0 }
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
    if ((maxWind?.speed ?: 0.0) > maxWindSpeedWindy) {
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
    initialTemperature: Temperature?,
    halfDayHourlyList: List<Hourly>,
    isDay: Boolean,
): Temperature {
    val newTemperature = initialTemperature ?: Temperature()

    var temperatureTemperature = newTemperature.temperature
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
            halfDayHourlyListPrecipitationTotal.sum()
        } else {
            null
        },
        thunderstorm = if (halfDayHourlyListPrecipitationThunderstorm.isNotEmpty()) {
            halfDayHourlyListPrecipitationThunderstorm.sum()
        } else {
            null
        },
        rain = if (halfDayHourlyListPrecipitationRain.isNotEmpty()) {
            halfDayHourlyListPrecipitationRain.sum()
        } else {
            null
        },
        snow = if (halfDayHourlyListPrecipitationSnow.isNotEmpty()) {
            halfDayHourlyListPrecipitationSnow.sum()
        } else {
            null
        },
        ice = if (halfDayHourlyListPrecipitationIce.isNotEmpty()) {
            halfDayHourlyListPrecipitationIce.sum()
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
): Int? {
    // average() would return NaN when called for an empty list
    return halfDayHourlyList.mapNotNull { it.cloudCover }
        .takeIf { it.isNotEmpty() }?.average()?.roundToInt()
}

private fun getHalfDayAvgVisibilityFromHourlyList(
    halfDayHourlyList: List<Hourly>,
): Double? {
    // average() would return NaN when called for an empty list
    return halfDayHourlyList.mapNotNull { it.visibility }.takeIf { it.isNotEmpty() }?.average()
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
        pM25 = hourlyList.mapNotNull { it.pM25 }.takeIf { it.isNotEmpty() }?.average(),
        pM10 = hourlyList.mapNotNull { it.pM10 }.takeIf { it.isNotEmpty() }?.average(),
        sO2 = hourlyList.mapNotNull { it.sO2 }.takeIf { it.isNotEmpty() }?.average(),
        nO2 = hourlyList.mapNotNull { it.nO2 }.takeIf { it.isNotEmpty() }?.average(),
        o3 = hourlyList.mapNotNull { it.o3 }.takeIf { it.isNotEmpty() }?.average(),
        cO = hourlyList.mapNotNull { it.cO }.takeIf { it.isNotEmpty() }?.average()
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
    hourlyList: Collection<Double?>?,
): Double? {
    return if (hourlyList != null) {
        val hourlyWithSunshine = hourlyList.filterNotNull()
        if (hourlyWithSunshine.isNotEmpty()) {
            hourlyWithSunshine.sum()
        } else {
            null
        }
    } else {
        null
    }
}

/**
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
                uV = if (hourly.uV?.index != null) {
                    hourly.uV
                } else {
                    getCurrentUVFromDayMax(
                        daily.first?.index,
                        hourly.date,
                        daily.second?.riseDate,
                        daily.second?.setDate,
                        location.javaTimeZone
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
            uV = if (newCurrent.uV?.index == null && todayDaily != null) {
                getCurrentUVFromDayMax(
                    todayDaily.uV?.index,
                    Date(),
                    todayDaily.sun?.riseDate,
                    todayDaily.sun?.setDate,
                    location.javaTimeZone
                )
            } else {
                newCurrent.uV
            }
        )
    }

    val newWind = if (newCurrent.wind?.speed != null || hourly.wind?.speed == null) {
        newCurrent.wind
    } else {
        hourly.wind
    }
    val newRelativeHumidity = newCurrent.relativeHumidity ?: hourly.relativeHumidity
    val newDewPoint = newCurrent.dewPoint ?: if (newCurrent.relativeHumidity != null ||
        newCurrent.temperature?.temperature != null
    ) {
        // If current data is available, we compute this over hourly dewpoint
        computeDewPoint(newCurrent.temperature?.temperature ?: hourly.temperature?.temperature, newRelativeHumidity)
    } else {
        // Already calculated earlier
        hourly.dewPoint
    }
    val newTemperature = completeCurrentTemperatureFromHourly(
        newCurrent.temperature,
        hourly.temperature,
        newWind?.speed,
        newRelativeHumidity,
        newDewPoint
    )
    return newCurrent.toCurrent(
        uV = if (newCurrent.uV?.index == null && todayDaily != null) {
            getCurrentUVFromDayMax(
                todayDaily.uV?.index,
                Date(),
                todayDaily.sun?.riseDate,
                todayDaily.sun?.setDate,
                location.javaTimeZone
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
        pressure = newCurrent.pressure ?: hourly.pressure,
        cloudCover = newCurrent.cloudCover ?: hourly.cloudCover,
        visibility = newCurrent.visibility ?: hourly.visibility
    )
}

private fun completeCurrentTemperatureFromHourly(
    initialTemperature: TemperatureWrapper?,
    hourlyTemperature: Temperature?,
    windSpeed: Double?,
    relativeHumidity: Double?,
    dewPoint: Double?,
): Temperature? {
    if (initialTemperature == null) return hourlyTemperature

    return Temperature(
        temperature = initialTemperature.temperature ?: hourlyTemperature?.temperature,
        sourceFeelsLike = initialTemperature.feelsLike ?: hourlyTemperature?.sourceFeelsLike,
        computedApparent = initialTemperature.temperature?.let {
            computeApparentTemperature(it, relativeHumidity, windSpeed)
        } ?: hourlyTemperature?.computedApparent,
        computedWindChill = initialTemperature.temperature?.let {
            computeWindChillTemperature(it, windSpeed)
        } ?: hourlyTemperature?.computedWindChill,
        computedHumidex = initialTemperature.temperature?.let {
            computeHumidex(it, dewPoint)
        } ?: hourlyTemperature?.computedHumidex
    )
}
