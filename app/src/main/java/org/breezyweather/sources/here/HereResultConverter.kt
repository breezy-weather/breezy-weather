package org.breezyweather.sources.here

import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.Alert
import org.breezyweather.common.basic.models.weather.Astro
import org.breezyweather.common.basic.models.weather.Base
import org.breezyweather.common.basic.models.weather.Current
import org.breezyweather.common.basic.models.weather.Daily
import org.breezyweather.common.basic.models.weather.HalfDay
import org.breezyweather.common.basic.models.weather.MoonPhase
import org.breezyweather.common.basic.models.weather.Precipitation
import org.breezyweather.common.basic.models.weather.PrecipitationProbability
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.basic.models.weather.UV
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.common.basic.models.weather.Wind
import org.breezyweather.common.basic.wrappers.HourlyWrapper
import org.breezyweather.common.basic.wrappers.WeatherResultWrapper
import org.breezyweather.common.exceptions.LocationSearchException
import org.breezyweather.common.exceptions.WeatherException
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.common.extensions.toTimezoneNoHour
import org.breezyweather.sources.here.json.HereGeocodingResult
import org.breezyweather.sources.here.json.HereWeatherAlert
import org.breezyweather.sources.here.json.HereWeatherAlertTimeSegment
import org.breezyweather.sources.here.json.HereWeatherAstronomy
import org.breezyweather.sources.here.json.HereWeatherData
import org.breezyweather.sources.here.json.HereWeatherForecastResult
import org.breezyweather.sources.here.json.HereWeatherNWSAlerts
import org.breezyweather.sources.here.json.HereWeatherStatusResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.hours

private val DATEFORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)
private val ASTRODATEFORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)

/**
 * Converts here.com geocoding result into a list of locations
 */
fun convert(
    result: HereGeocodingResult
): List<Location> {
    if (result.items == null) {
        throw LocationSearchException()
    }

    return result.items.map { item ->
        Location(
            cityId = item.id,
            latitude = item.position.lat,
            longitude = item.position.lng,
            timeZone = TimeZone.getTimeZone(item.timeZone?.name),
            country = item.address.countryName,
            countryCode = item.address.countryCode,
            province = item.address.state,
            provinceCode = item.address.stateCode,
            district = item.address.county,
            city = item.address.city,
            weatherSource = "here"
        )
    }
}

/**
 * Converts here.com weather result into a forecast
 */
fun convert(
    hereWeatherForecastResult: HereWeatherForecastResult,
    hereWeatherStatusResult: HereWeatherStatusResult,
): WeatherResultWrapper {
    if (!hereWeatherStatusResult.status.equals("ok")) {
        throw WeatherException()
    }

    // Indexes changed several times while testing, so had to resort to this
    // There is 100% a way to make it nicer
    val currentForecasts =
        hereWeatherForecastResult.dataList.find { it?.currentForecasts != null }?.currentForecasts
    val hourlyForecasts =
        hereWeatherForecastResult.dataList.find { it?.hourlyForecasts != null }?.hourlyForecasts

    val dailyExtendedForecasts =
        hereWeatherForecastResult.dataList.find { it?.extendedDailyForecasts != null }?.extendedDailyForecasts
    val dailySimpleForecasts =
        hereWeatherForecastResult.dataList.find { it?.dailyForecasts != null }?.dailyForecasts
    val astronomyForecasts =
        hereWeatherForecastResult.dataList.find { it?.astronomyForecasts != null }?.astronomyForecasts

    val alerts = hereWeatherForecastResult.dataList.find { it?.alerts != null }?.alerts
    val nwsAlerts = hereWeatherForecastResult.dataList.find { it?.nwsAlerts != null }?.nwsAlerts

    return WeatherResultWrapper(
        base = Base(
            publishDate = currentForecasts?.get(0)?.time?.toDateNoHour() ?: Date()
        ),
        current = currentForecasts?.get(0)?.let { getCurrentForecast(it) },
        dailyForecast = getDailyForecast(
            dailySimpleForecasts?.get(0)?.forecasts,
            dailyExtendedForecasts?.get(0)?.forecasts,
            astronomyForecasts?.get(0)?.forecasts
        ),
        hourlyForecast = hourlyForecasts?.get(0)?.forecasts?.let { getHourlyForecast(it) },
        alertList = getAlertList(
            alerts, nwsAlerts, getTimeZone(currentForecasts?.get(0)?.time)
        )
    )
}

/**
 * Parses time from String
 */
private fun getDate(time: String?): Date? {
    return time?.let { DATEFORMAT.parse(it) }
}

/**
 * Parses time for astro events
 */
private fun getAstroDate(date: String?, time: String?): Date? {
    if (date == null || time == null) return null
    val withTime = date.replace("00:00:00", time) // bad, API21 forced my hand
    return withTime.let { ASTRODATEFORMAT.parse(it) }
}

/**
 * Retrieves timezone from Date string
 */
private fun getTimeZone(time: String?): TimeZone? {
    if (time == null || getDate(time) == null) return null
    val regex = Regex("([-+][01][0-9]:[0-9][0-9])$")
    val capture = regex.find(time)?.value
    if (capture.isNullOrEmpty()) return TimeZone.getDefault()

    return TimeZone.getTimeZone("GMT${capture}")
}

/**
 * Returns current forecast
 */
private fun getCurrentForecast(result: HereWeatherData): Current {
    return Current(
        weatherText = result.skyDesc,
        weatherCode = getWeatherCode(result.iconId),
        temperature = Temperature(
            temperature = result.temperature,
            apparentTemperature = result.apparentTemperature?.toFloat()
        ),
        wind = Wind(
            degree = result.windDirection, speed = result.windSpeed
        ),
        uV = UV(
            index = result.uvIndex?.toFloat()
        ),
        relativeHumidity = result.humidity?.toFloat(),
        dewPoint = result.dewPoint,
        pressure = result.pressure,
        visibility = result.visibility,
    )
}

/**
 * Returns daily forecast
 */
private fun getDailyForecast(
    dailySimpleForecasts: List<HereWeatherData>?,
    dailyExtendedForecasts: List<HereWeatherData>?,
    astroForecasts: List<HereWeatherAstronomy>?
): List<Daily>? {
    if (dailyExtendedForecasts == null) {
        return null
    }

    val dailyList = arrayListOf<Daily>()

    var currentDay = 0
    for ((index, nightSegment) in dailyExtendedForecasts.withIndex()) {
        if (nightSegment.timeOfDay != "night") {
            continue
        }
        currentDay++

        val daySegment = if (index >= 2) dailyExtendedForecasts[index - 2] else null
        val fullDay = dailySimpleForecasts?.get(currentDay - 1)
        val astro = astroForecasts?.get(currentDay - 1)

        dailyList.add(
            Daily(
                date = getDate(daySegment?.time) ?: Date(),
                // Use day segment if available
                day = daySegment?.let {
                    HalfDay(
                        weatherText = it.skyDesc,
                        weatherCode = getWeatherCode(it.iconId),
                        temperature = Temperature(
                            temperature = it.temperature,
                            apparentTemperature = it.apparentTemperature?.toFloat()
                        ),
                        precipitation = Precipitation(
                            total = it.precipitation12H ?: getTotalPrecipFallback(it),
                            rain = it.rainFall,
                            snow = it.snowFall,
                        ),
                        precipitationProbability = PrecipitationProbability(
                            total = it.precipitationProbability?.toFloat()
                        )
                    )
                }, night = HalfDay(
                    weatherText = nightSegment.skyDesc,
                    weatherCode = getWeatherCode(nightSegment.iconId),
                    temperature = Temperature(
                        temperature = nightSegment.temperature,
                        apparentTemperature = nightSegment.apparentTemperature?.toFloat()
                    )
                ), sun = Astro(
                    riseDate = getAstroDate(astro?.time, astro?.sunRise),
                    setDate = getAstroDate(astro?.time, astro?.sunSet)
                ), moon = Astro(
                    riseDate = getAstroDate(astro?.time, astro?.moonRise),
                    setDate = getAstroDate(astro?.time, astro?.moonSet)
                ), moonPhase = MoonPhase(
                    angle = astro?.moonPhase?.times(360f)?.roundToInt()
                ), uV = UV(
                    index = fullDay?.uvIndex?.toFloat()
                )
            )
        )
    }
    return dailyList
}

/**
 * Returns hourly forecast
 */
private fun getHourlyForecast(
    hourlyResult: List<HereWeatherData>
): List<HourlyWrapper> {
    return hourlyResult.map { result ->
        HourlyWrapper(
            date = getDate(result.time) ?: Date(),
            weatherText = result.skyDesc,
            weatherCode = getWeatherCode(result.iconId),
            temperature = Temperature(
                temperature = result.temperature,
                apparentTemperature = result.apparentTemperature?.toFloat()
            ),
            precipitation = Precipitation(
                total = result.precipitation1H ?: getTotalPrecipFallback(result),
                rain = result.rainFall,
                snow = result.snowFall,
            ),
            precipitationProbability = PrecipitationProbability(
                total = result.precipitationProbability?.toFloat()
            ),
            wind = Wind(
                degree = result.windDirection, speed = result.windSpeed
            ),
            uV = UV(
                index = result.uvIndex?.toFloat()
            ),
            relativeHumidity = result.humidity?.toFloat(),
            dewPoint = result.dewPoint,
            pressure = result.pressure,
            visibility = result.visibility
        )
    }
}

/**
 * Fallback total precipitation calculation
 * Sums up snow and rain values
 */
private fun getTotalPrecipFallback(weatherData: HereWeatherData): Float {
    return (weatherData.rainFall ?: 0f).plus(weatherData.snowFall ?: 0f)
}

/**
 * Returns a list of alerts, combined from
 * general alerts forecasted for next 24 hours and
 * NWS warnings and watches
 *
 * TODO: Smarter deduplication
 */
private fun getAlertList(
    alerts: List<HereWeatherAlert>?, nwsAlerts: HereWeatherNWSAlerts?, tz: TimeZone?
): List<Alert> {
    val converted = arrayListOf<Alert>()

    alerts?.let {
        for (alert in it) {
            if (alert.timeSegments.isNullOrEmpty()) {
                continue
            }

            for (timeSegment in alert.timeSegments) {
                converted.add(
                    Alert(
                        // see https://stackoverflow.com/questions/15184820/how-to-generate-unique-positive-long-using-uuid
                        alertId = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE,
                        startDate = getForecastedAlertStart(timeSegment, tz),
                        endDate = getForecastedAlertEnd(timeSegment, tz),
                        description = alert.description?.substringBefore(" - ")
                            ?: alert.description
                            ?: "",
                        content = alert.description?.substringAfter(" - "),
                        priority = getForecastedAlertPriority(alert)
                    )
                )
            }
        }
    }

    val nwsAlertsCombined = (nwsAlerts?.warnings ?: listOf()) + (nwsAlerts?.watches ?: listOf())
    for (alert in nwsAlertsCombined) {
        val description = alert.description ?: ""
        val startDate = getDate(alert.start) ?: Date()
        val endDate = getDate(alert.end) ?: Date()

        // try to deduplicate
        if (converted.find {
                it.description == description &&
                        it.startDate == startDate &&
                        it.endDate == endDate
            } != null) {
            continue
        }

        converted.add(
            Alert(
                alertId = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE,
                startDate = startDate,
                endDate = endDate,
                description = description,
                content = alert.message,
                priority = alert.severity ?: 1
            )
        )
    }

    return converted
}

/**
 * Returns forecasted alert start time
 * manually inferred from time of day in HereAlertTimeSegment
 *
 * TODO: Alerts are forecasted for next 24 hours
 *  Right now each alert is set to today regardless of dayOfWeek,
 *  but it is possible that the alert is for tomorrow
 */
private fun getForecastedAlertStart(
    timeSegment: HereWeatherAlertTimeSegment, tz: TimeZone?
): Date {
    val dayStart = tz?.let { Date().toTimezoneNoHour(it) } ?: Date()

    val alertStart = when (timeSegment.timeOfDay) {
        "morning" -> 2
        "afternoon" -> 10
        "evening" -> 16
        "night" -> 20
        else -> 0
    }
    return dayStart.time.plus(alertStart.hours.inWholeMilliseconds).toDate()
}

/**
 * Returns forecasted alert end time
 * manually inferred from time of day in HereAlertTimeSegment
 */
private fun getForecastedAlertEnd(
    timeSegment: HereWeatherAlertTimeSegment, tz: TimeZone?
): Date {
    val dayStart = tz?.let { Date().toTimezoneNoHour(it) } ?: Date()
    val alertEnd = when (timeSegment.timeOfDay) {
        "morning" -> 10
        "afternoon" -> 16
        "evening" -> 20
        "night" -> 24
        else -> 0
    }
    return dayStart.time.plus(alertEnd.hours.inWholeMilliseconds).toDate()
}

/**
 * Returns forecasted alert priority
 * manually inferred from alert's description.
 *
 * See https://developer.here.com/documentation/destination-weather/api-reference-v3.html
 * ApiInformation for details
 */
private fun getForecastedAlertPriority(alert: HereWeatherAlert): Int {
    return when (alert.type) {
        1, 4, 7, 8, 10, 11, 13, 14, 15, 17, 18, 20, 22, 30, 32, 35 -> 3
        2, 5, 9, 16, 19, 21, 33, 34 -> 2
        3, 6, 12, 31 -> 1
        else -> 1
    }
}

/**
 * Returns weather code based on icon id
 */
private fun getWeatherCode(icon: Int?): WeatherCode? {
    return when (icon) {
        1, 2, 13, 14 -> WeatherCode.CLEAR
        3 -> WeatherCode.HAZE
        4, 5, 15, 16 -> WeatherCode.PARTLY_CLOUDY
        6 -> WeatherCode.PARTLY_CLOUDY
        7, 17 -> WeatherCode.CLOUDY
        8, 9, 10, 12 -> WeatherCode.FOG
        11 -> WeatherCode.WIND
        18, 19, 20, 32, 33, 34 -> WeatherCode.RAIN
        21, 22, 23, 25, 26, 35 -> WeatherCode.THUNDERSTORM
        24 -> WeatherCode.HAIL
        27, 28 -> WeatherCode.SLEET
        29, 30, 31 -> WeatherCode.SNOW
        else -> null
    }
}