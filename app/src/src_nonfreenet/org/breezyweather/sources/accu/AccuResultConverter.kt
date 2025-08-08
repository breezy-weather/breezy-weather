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

package org.breezyweather.sources.accu

import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.location.model.LocationAddressInfo
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.DegreeDay
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.Pollen
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationDuration
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.WeatherCode
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HalfDayWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.PollenWrapper
import breezyweather.domain.weather.wrappers.TemperatureWrapper
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.extensions.toTimezoneNoHour
import org.breezyweather.sources.accu.json.AccuAirQualityData
import org.breezyweather.sources.accu.json.AccuAlertResult
import org.breezyweather.sources.accu.json.AccuCurrentResult
import org.breezyweather.sources.accu.json.AccuForecastAirAndPollen
import org.breezyweather.sources.accu.json.AccuForecastDailyForecast
import org.breezyweather.sources.accu.json.AccuForecastDailyResult
import org.breezyweather.sources.accu.json.AccuForecastHourlyResult
import org.breezyweather.sources.accu.json.AccuLocationResult
import org.breezyweather.sources.accu.json.AccuMinutelyResult
import org.breezyweather.sources.accu.json.AccuValue
import java.util.Date
import java.util.TimeZone
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal fun getCurrent(
    currentResult: AccuCurrentResult?,
    dailyResult: AccuForecastDailyResult? = null,
    minuteResult: AccuMinutelyResult? = null,
): CurrentWrapper? {
    if (currentResult == null) return null

    return CurrentWrapper(
        weatherText = currentResult.WeatherText,
        weatherCode = getWeatherCode(currentResult.WeatherIcon),
        temperature = TemperatureWrapper(
            temperature = currentResult.Temperature?.Metric?.Value,
            feelsLike = currentResult.RealFeelTemperature?.Metric?.Value
        ),
        wind = Wind(
            degree = currentResult.Wind?.Direction?.Degrees?.toDouble(),
            speed = currentResult.Wind?.Speed?.Metric?.Value?.div(3.6),
            gusts = currentResult.WindGust?.Speed?.Metric?.Value?.div(3.6)
        ),
        uV = UV(index = currentResult.UVIndex?.toDouble()),
        relativeHumidity = currentResult.RelativeHumidity?.toDouble(),
        dewPoint = currentResult.DewPoint?.Metric?.Value,
        pressure = currentResult.Pressure?.Metric?.Value,
        cloudCover = currentResult.CloudCover,
        visibility = currentResult.Visibility?.Metric?.Value?.times(1000),
        ceiling = currentResult.Ceiling?.Metric?.Value,
        dailyForecast = dailyResult?.Headline?.Text,
        hourlyForecast = minuteResult?.Summary?.LongPhrase
    )
}

internal fun getDailyList(
    dailyForecasts: List<AccuForecastDailyForecast>?,
    location: Location,
): List<DailyWrapper>? {
    return dailyForecasts?.map { forecasts ->
        DailyWrapper(
            date = forecasts.EpochDate.seconds.inWholeMilliseconds.toDate().toTimezoneNoHour(location.timeZone),
            day = HalfDayWrapper(
                weatherText = forecasts.Day?.ShortPhrase,
                weatherSummary = forecasts.Day?.LongPhrase,
                weatherCode = getWeatherCode(forecasts.Day?.Icon),
                temperature = TemperatureWrapper(
                    temperature = getTemperatureInCelsius(forecasts.Temperature?.Maximum),
                    feelsLike = getTemperatureInCelsius(forecasts.RealFeelTemperature?.Maximum)
                ),
                precipitation = Precipitation(
                    total = getQuantityInMillimeters(forecasts.Day?.TotalLiquid),
                    rain = getQuantityInMillimeters(forecasts.Day?.Rain),
                    snow = getQuantityInMillimeters(forecasts.Day?.Snow),
                    ice = getQuantityInMillimeters(forecasts.Day?.Ice)
                ),
                precipitationProbability = PrecipitationProbability(
                    total = forecasts.Day?.PrecipitationProbability?.toDouble(),
                    thunderstorm = forecasts.Day?.ThunderstormProbability?.toDouble(),
                    rain = forecasts.Day?.RainProbability?.toDouble(),
                    snow = forecasts.Day?.SnowProbability?.toDouble(),
                    ice = forecasts.Day?.IceProbability?.toDouble()
                ),
                precipitationDuration = PrecipitationDuration(
                    total = forecasts.Day?.HoursOfPrecipitation,
                    rain = forecasts.Day?.HoursOfRain,
                    snow = forecasts.Day?.HoursOfSnow,
                    ice = forecasts.Day?.HoursOfIce
                ),
                wind = Wind(
                    degree = forecasts.Day?.Wind?.Direction?.Degrees?.toDouble(),
                    speed = getSpeedInMetersPerSecond(forecasts.Day?.Wind?.Speed),
                    gusts = getSpeedInMetersPerSecond(forecasts.Day?.WindGust?.Speed)
                )
            ),
            night = HalfDayWrapper(
                weatherText = forecasts.Night?.ShortPhrase,
                weatherSummary = forecasts.Night?.LongPhrase,
                weatherCode = getWeatherCode(forecasts.Night?.Icon),
                temperature = TemperatureWrapper(
                    temperature = getTemperatureInCelsius(forecasts.Temperature?.Minimum),
                    feelsLike = getTemperatureInCelsius(forecasts.RealFeelTemperature?.Minimum)
                ),
                precipitation = Precipitation(
                    total = getQuantityInMillimeters(forecasts.Night?.TotalLiquid),
                    rain = getQuantityInMillimeters(forecasts.Night?.Rain),
                    snow = getQuantityInMillimeters(forecasts.Night?.Snow),
                    ice = getQuantityInMillimeters(forecasts.Night?.Ice)
                ),
                precipitationProbability = PrecipitationProbability(
                    total = forecasts.Night?.PrecipitationProbability?.toDouble(),
                    thunderstorm = forecasts.Night?.ThunderstormProbability?.toDouble(),
                    rain = forecasts.Night?.RainProbability?.toDouble(),
                    snow = forecasts.Night?.SnowProbability?.toDouble(),
                    ice = forecasts.Night?.IceProbability?.toDouble()
                ),
                precipitationDuration = PrecipitationDuration(
                    total = forecasts.Night?.HoursOfPrecipitation,
                    rain = forecasts.Night?.HoursOfRain,
                    snow = forecasts.Night?.HoursOfSnow,
                    ice = forecasts.Night?.HoursOfIce
                ),
                wind = Wind(
                    degree = forecasts.Night?.Wind?.Direction?.Degrees?.toDouble(),
                    speed = getSpeedInMetersPerSecond(forecasts.Night?.Wind?.Speed),
                    gusts = getSpeedInMetersPerSecond(forecasts.Night?.WindGust?.Speed)
                )
            ),
            degreeDay = DegreeDay(
                heating = getDegreeDayInCelsius(forecasts.DegreeDaySummary?.Heating),
                cooling = getDegreeDayInCelsius(forecasts.DegreeDaySummary?.Cooling)
            ),
            uV = getDailyUV(forecasts.AirAndPollen),
            sunshineDuration = forecasts.HoursOfSun
        )
    }
}

/**
 * Accu returns 0 / m³ for all days if they don’t measure it, instead of null values
 * This function will tell us if they measured at least one pollen or mold during the 15-day period
 * to make the difference between a 0 and a null
 */
private fun supportsPollen(dailyForecasts: List<AccuForecastDailyForecast>): Boolean {
    dailyForecasts.forEach { daily ->
        val pollens = listOf(
            daily.AirAndPollen?.firstOrNull { it.Name == "Tree" },
            daily.AirAndPollen?.firstOrNull { it.Name == "Grass" },
            daily.AirAndPollen?.firstOrNull { it.Name == "Ragweed" },
            daily.AirAndPollen?.firstOrNull { it.Name == "Mold" }
        ).filter { it?.Value != null && it.Value > 0 }
        if (pollens.isNotEmpty()) return true
    }
    return false
}

private fun getDailyPollen(list: List<AccuForecastAirAndPollen>?): Pollen? {
    if (list == null) return null

    val grass = list.firstOrNull { it.Name == "Grass" }
    val mold = list.firstOrNull { it.Name == "Mold" }
    val ragweed = list.firstOrNull { it.Name == "Ragweed" }
    val tree = list.firstOrNull { it.Name == "Tree" }
    return Pollen(
        grass = grass?.Value,
        mold = mold?.Value,
        ragweed = ragweed?.Value,
        tree = tree?.Value
    )
}

private fun getDailyUV(
    list: List<AccuForecastAirAndPollen>?,
): UV? {
    if (list == null) return null

    val uv = list.firstOrNull { it.Name == "UVIndex" }
    return UV(index = uv?.Value?.toDouble())
}

internal fun getHourlyList(
    resultList: List<AccuForecastHourlyResult>,
): List<HourlyWrapper> {
    return resultList.map { result ->
        HourlyWrapper(
            date = result.EpochDateTime.seconds.inWholeMilliseconds.toDate(),
            isDaylight = result.IsDaylight,
            weatherText = result.IconPhrase,
            weatherCode = getWeatherCode(result.WeatherIcon),
            temperature = TemperatureWrapper(
                temperature = getTemperatureInCelsius(result.Temperature),
                feelsLike = getTemperatureInCelsius(result.RealFeelTemperature)
            ),
            precipitation = Precipitation(
                total = getQuantityInMillimeters(result.TotalLiquid),
                rain = getQuantityInMillimeters(result.Rain),
                snow = getQuantityInMillimeters(result.Snow),
                ice = getQuantityInMillimeters(result.Ice)
            ),
            precipitationProbability = PrecipitationProbability(
                total = result.PrecipitationProbability?.toDouble(),
                thunderstorm = result.ThunderstormProbability?.toDouble(),
                rain = result.RainProbability?.toDouble(),
                snow = result.SnowProbability?.toDouble(),
                ice = result.IceProbability?.toDouble()
            ),
            wind = Wind(
                degree = result.Wind?.Direction?.Degrees?.toDouble(),
                speed = getSpeedInMetersPerSecond(result.Wind?.Speed),
                gusts = getSpeedInMetersPerSecond(result.WindGust?.Speed)
            ),
            uV = UV(index = result.UVIndex?.toDouble()),
            relativeHumidity = result.RelativeHumidity?.toDouble(),
            dewPoint = getTemperatureInCelsius(result.DewPoint),
            cloudCover = result.CloudCover,
            visibility = getDistanceInMeters(result.Visibility)
        )
    }
}

internal fun getAirQualityWrapper(airQualityHourlyResult: List<AccuAirQualityData>?): AirQualityWrapper? {
    if (airQualityHourlyResult.isNullOrEmpty()) return null

    val airQualityHourly = mutableMapOf<Date, AirQuality>()
    airQualityHourlyResult
        .forEach {
            var pm25: Double? = null
            var pm10: Double? = null
            var so2: Double? = null
            var no2: Double? = null
            var o3: Double? = null
            var co: Double? = null
            it.pollutants?.forEach { p ->
                when (p.type) {
                    "O3" -> o3 = p.concentration.value
                    "NO2" -> no2 = p.concentration.value
                    "PM2_5" -> pm25 = p.concentration.value
                    "PM10" -> pm10 = p.concentration.value
                    "SO2" -> so2 = p.concentration.value
                    "CO" -> co = p.concentration.value?.div(1000.0)
                }
            }
            val airQuality = if (pm25 != null ||
                pm10 != null ||
                so2 != null ||
                no2 != null ||
                o3 != null ||
                co != null
            ) {
                AirQuality(
                    pM25 = pm25,
                    pM10 = pm10,
                    sO2 = so2,
                    nO2 = no2,
                    o3 = o3,
                    cO = co
                )
            } else {
                null
            }
            if (airQuality != null) {
                airQualityHourly[it.epochDate.seconds.inWholeMilliseconds.toDate()] = airQuality
            }
        }

    return AirQualityWrapper(
        hourlyForecast = airQualityHourly
    )
}

/**
 * Used from secondary
 */
internal fun getPollenWrapper(
    dailyPollenResult: List<AccuForecastDailyForecast>?,
    location: Location,
): PollenWrapper? {
    if (dailyPollenResult.isNullOrEmpty()) return null
    if (!supportsPollen(dailyPollenResult)) return null

    val pollenDaily = mutableMapOf<Date, Pollen>()
    dailyPollenResult
        .forEach {
            val dailyPollen = getDailyPollen(it.AirAndPollen)
            if (dailyPollen != null) {
                pollenDaily[
                    it.EpochDate.seconds.inWholeMilliseconds.toDate().toTimezoneNoHour(location.timeZone)
                ] = dailyPollen
            }
        }

    return PollenWrapper(
        dailyForecast = pollenDaily
    )
}

internal fun getMinutelyList(
    minuteResult: AccuMinutelyResult?,
): List<Minutely>? {
    if (minuteResult == null) return null
    if (minuteResult.Intervals.isNullOrEmpty()) return emptyList()
    return minuteResult.Intervals.mapIndexed { i, interval ->
        Minutely(
            date = Date(interval.StartEpochDateTime),
            minuteInterval = if (i < minuteResult.Intervals.size - 1) {
                (
                    (minuteResult.Intervals[i + 1].StartEpochDateTime - interval.StartEpochDateTime) /
                        1.minutes.inWholeMilliseconds
                    ).toDouble().roundToInt()
            } else {
                (
                    (interval.StartEpochDateTime - minuteResult.Intervals[i - 1].StartEpochDateTime) /
                        1.minutes.inWholeMilliseconds
                    ).toDouble().roundToInt()
            },
            precipitationIntensity = Minutely.dbzToPrecipitationIntensity(interval.Dbz)
        )
    }
}

internal fun getAlertList(
    resultList: List<AccuAlertResult>?,
): List<Alert>? {
    if (resultList == null) return null
    return resultList.map { result ->
        val severity = when (result.Priority) {
            1 -> AlertSeverity.EXTREME
            2 -> AlertSeverity.SEVERE
            3 -> AlertSeverity.MODERATE
            4, 5 -> AlertSeverity.MINOR
            else -> AlertSeverity.UNKNOWN
        }
        Alert(
            alertId = result.AlertID.toString(),
            startDate = result.Area?.getOrNull(0)?.let { area ->
                area.EpochStartTime?.seconds?.inWholeMilliseconds?.toDate()
            },
            endDate = result.Area?.getOrNull(0)?.let { area ->
                area.EpochEndTime?.seconds?.inWholeMilliseconds?.toDate()
            },
            headline = result.Description?.Localized,
            description = result.Area?.getOrNull(0)?.Text,
            source = result.Source,
            severity = severity,
            color = result.Color?.let {
                Color.rgb(it.Red, it.Green, it.Blue)
            } ?: Alert.colorFromSeverity(severity)
        )
    }
}

private fun getWeatherCode(icon: Int?): WeatherCode? {
    return when (icon) {
        null -> null
        1, 2, 30, 33, 34 -> WeatherCode.CLEAR
        3, 4, 6, 35, 36, 38 -> WeatherCode.PARTLY_CLOUDY
        5, 37 -> WeatherCode.HAZE
        7, 8 -> WeatherCode.CLOUDY
        11 -> WeatherCode.FOG
        12, 13, 14, 18, 39, 40 -> WeatherCode.RAIN
        15, 16, 17, 41, 42 -> WeatherCode.THUNDERSTORM
        19, 20, 21, 22, 23, 24, 31, 43, 44 -> WeatherCode.SNOW
        25 -> WeatherCode.HAIL
        26, 29 -> WeatherCode.SLEET
        32 -> WeatherCode.WIND
        else -> null
    }
}

private fun getTemperatureInCelsius(value: AccuValue?): Double? {
    return if (value?.UnitType == 18) { // F
        value.Value?.minus(32)?.div(1.8)
    } else {
        value?.Value
    }
}

private fun getDegreeDayInCelsius(value: AccuValue?): Double? {
    return if (value?.UnitType == 18) { // F
        value.Value?.div(1.8)
    } else {
        value?.Value
    }
}

private fun getSpeedInMetersPerSecond(value: AccuValue?): Double? {
    return if (value?.UnitType == 9) { // mi/h
        value.Value?.div(2.23694)
    } else {
        value?.Value?.div(3.6)
    }
}

private fun getDistanceInMeters(value: AccuValue?): Double? {
    return when (value?.UnitType) {
        2 -> value.Value?.times(1609.344) // mi
        0 -> value.Value?.div(3.28084) // ft
        6 -> value.Value?.times(1000) // km
        else -> value?.Value // m
    }
}

private fun getQuantityInMillimeters(value: AccuValue?): Double? {
    return when (value?.UnitType) {
        1 -> value.Value?.times(25.4) // in
        4 -> value.Value?.times(10) // cm
        else -> value?.Value // mm
    }
}
