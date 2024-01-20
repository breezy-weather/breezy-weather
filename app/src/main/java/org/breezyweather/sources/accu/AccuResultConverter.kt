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
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.AirQuality
import org.breezyweather.common.basic.models.weather.Alert
import org.breezyweather.common.basic.models.weather.Pollen
import org.breezyweather.common.basic.models.weather.Astro
import org.breezyweather.common.basic.models.weather.Current
import org.breezyweather.common.basic.models.weather.Daily
import org.breezyweather.common.basic.models.weather.DegreeDay
import org.breezyweather.common.basic.models.weather.HalfDay
import org.breezyweather.common.basic.models.weather.Normals
import org.breezyweather.common.basic.models.weather.Minutely
import org.breezyweather.common.basic.models.weather.MoonPhase
import org.breezyweather.common.basic.models.weather.Precipitation
import org.breezyweather.common.basic.models.weather.PrecipitationDuration
import org.breezyweather.common.basic.models.weather.PrecipitationProbability
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.basic.models.weather.UV
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.common.basic.models.weather.Wind
import org.breezyweather.common.basic.wrappers.HourlyWrapper
import org.breezyweather.common.basic.wrappers.SecondaryWeatherWrapper
import org.breezyweather.common.basic.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.WeatherException
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.extensions.toTimezoneNoHour
import org.breezyweather.sources.accu.json.AccuAirQualityData
import org.breezyweather.sources.accu.json.AccuAirQualityResult
import org.breezyweather.sources.accu.json.AccuAlertResult
import org.breezyweather.sources.accu.json.AccuClimoSummaryResult
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

fun convert(
    location: Location?,
    result: AccuLocationResult
): Location {
    return Location(
        cityId = result.Key,
        latitude = location?.latitude ?: result.GeoPosition.Latitude.toFloat(),
        longitude = location?.longitude ?: result.GeoPosition.Longitude.toFloat(),
        timeZone = TimeZone.getTimeZone(result.TimeZone.Name),
        country = result.Country.LocalizedName.ifEmpty { result.Country.EnglishName },
        countryCode = result.Country.ID,
        province = result.AdministrativeArea?.LocalizedName?.ifEmpty { result.AdministrativeArea.EnglishName },
        provinceCode = result.AdministrativeArea?.ID,
        city = result.LocalizedName?.ifEmpty { result.EnglishName } ?: "",
        weatherSource = "accu",
        airQualitySource = location?.airQualitySource,
        pollenSource = location?.pollenSource,
        minutelySource = location?.minutelySource,
        alertSource = location?.alertSource,
        normalsSource = location?.normalsSource
    )
}

fun convert(
    location: Location,
    currentResult: AccuCurrentResult,
    dailyResult: AccuForecastDailyResult,
    hourlyResultList: List<AccuForecastHourlyResult>,
    minuteResult: AccuMinutelyResult?,
    alertResultList: List<AccuAlertResult>,
    airQualityHourlyResult: AccuAirQualityResult,
    climoSummaryResult: AccuClimoSummaryResult,
    currentMonth: Int
): WeatherWrapper {
    // If the API doesn’t return hourly or daily, consider data as garbage and keep cached data
    if (dailyResult.DailyForecasts == null || dailyResult.DailyForecasts.isEmpty() || hourlyResultList.isEmpty()) {
        throw WeatherException()
    }

    return WeatherWrapper(
        /*base = Base(
            publishDate = Date(currentResult.EpochTime.times(1000)),
        ),*/
        current = Current(
            weatherText = currentResult.WeatherText,
            weatherCode = getWeatherCode(currentResult.WeatherIcon),
            temperature = Temperature(
                temperature = currentResult.Temperature?.Metric?.Value?.toFloat(),
                realFeelTemperature = currentResult.RealFeelTemperature?.Metric?.Value?.toFloat(),
                realFeelShaderTemperature = currentResult.RealFeelTemperatureShade?.Metric?.Value?.toFloat(),
                apparentTemperature = currentResult.ApparentTemperature?.Metric?.Value?.toFloat(),
                windChillTemperature = currentResult.WindChillTemperature?.Metric?.Value?.toFloat(),
                wetBulbTemperature = currentResult.WetBulbTemperature?.Metric?.Value?.toFloat()
            ),
            wind = Wind(
                degree = currentResult.Wind?.Direction?.Degrees?.toFloat(),
                speed = currentResult.Wind?.Speed?.Metric?.Value?.div(3.6)?.toFloat(),
                gusts = currentResult.WindGust?.Speed?.Metric?.Value?.div(3.6)?.toFloat()
            ),
            uV = UV(index = currentResult.UVIndex?.toFloat()),
            relativeHumidity = currentResult.RelativeHumidity?.toFloat(),
            dewPoint = currentResult.DewPoint?.Metric?.Value?.toFloat(),
            pressure = currentResult.Pressure?.Metric?.Value?.toFloat(),
            cloudCover = currentResult.CloudCover,
            visibility = currentResult.Visibility?.Metric?.Value?.times(1000)?.toFloat(),
            ceiling = currentResult.Ceiling?.Metric?.Value?.toFloat(),
            dailyForecast = dailyResult.Headline?.Text,
            hourlyForecast = minuteResult?.Summary?.LongPhrase
        ),
        normals = if (climoSummaryResult.Normals?.Temperatures != null) {
            Normals(
                month = currentMonth,
                daytimeTemperature = climoSummaryResult.Normals.Temperatures.Maximum.Metric?.Value?.toFloat(),
                nighttimeTemperature = climoSummaryResult.Normals.Temperatures.Minimum.Metric?.Value?.toFloat()
            )
        } else null,
        dailyForecast = getDailyList(dailyResult.DailyForecasts, location.timeZone),
        hourlyForecast = getHourlyList(hourlyResultList, airQualityHourlyResult.data),
        minutelyForecast = getMinutelyList(minuteResult),
        alertList = getAlertList(alertResultList)
    )
}

private fun getDailyList(
    dailyForecasts: List<AccuForecastDailyForecast>,
    timeZone: TimeZone
): List<Daily> {
    val supportsPollen = supportsPollen(dailyForecasts)

    return dailyForecasts.map { forecasts ->
        Daily(
            date = Date(forecasts.EpochDate.times(1000)).toTimezoneNoHour(timeZone)!!,
            day = HalfDay(
                weatherText = forecasts.Day?.LongPhrase,
                weatherPhase = forecasts.Day?.ShortPhrase,
                weatherCode = getWeatherCode(forecasts.Day?.Icon),
                temperature = Temperature(
                    temperature = getTemperatureInCelsius(forecasts.Temperature?.Maximum),
                    realFeelTemperature = getTemperatureInCelsius(forecasts.RealFeelTemperature?.Maximum),
                    realFeelShaderTemperature = getTemperatureInCelsius(forecasts.RealFeelTemperatureShade?.Maximum)
                ),
                precipitation = Precipitation(
                    total = getQuantityInMillimeters(forecasts.Day?.TotalLiquid),
                    rain = getQuantityInMillimeters(forecasts.Day?.Rain),
                    snow = getQuantityInMillimeters(forecasts.Day?.Snow),
                    ice = getQuantityInMillimeters(forecasts.Day?.Ice)
                ),
                precipitationProbability = PrecipitationProbability(
                    total = forecasts.Day?.PrecipitationProbability?.toFloat(),
                    thunderstorm = forecasts.Day?.ThunderstormProbability?.toFloat(),
                    rain = forecasts.Day?.RainProbability?.toFloat(),
                    snow = forecasts.Day?.SnowProbability?.toFloat(),
                    ice = forecasts.Day?.IceProbability?.toFloat()
                ),
                precipitationDuration = PrecipitationDuration(
                    total = forecasts.Day?.HoursOfPrecipitation?.toFloat(),
                    rain = forecasts.Day?.HoursOfRain?.toFloat(),
                    snow = forecasts.Day?.HoursOfSnow?.toFloat(),
                    ice = forecasts.Day?.HoursOfIce?.toFloat()
                ),
                wind = Wind(
                    degree = forecasts.Day?.Wind?.Direction?.Degrees?.toFloat(),
                    speed = getSpeedInMetersPerSecond(forecasts.Day?.Wind?.Speed),
                    gusts = getSpeedInMetersPerSecond(forecasts.Day?.WindGust?.Speed)
                ),
                cloudCover = forecasts.Day?.CloudCover
            ),
            night = HalfDay(
                weatherText = forecasts.Night?.LongPhrase,
                weatherPhase = forecasts.Night?.ShortPhrase,
                weatherCode = getWeatherCode(forecasts.Night?.Icon),
                temperature = Temperature(
                    temperature = getTemperatureInCelsius(forecasts.Temperature?.Minimum),
                    realFeelTemperature = getTemperatureInCelsius(forecasts.RealFeelTemperature?.Minimum),
                    realFeelShaderTemperature = getTemperatureInCelsius(forecasts.RealFeelTemperatureShade?.Minimum)
                ),
                precipitation = Precipitation(
                    total = getQuantityInMillimeters(forecasts.Night?.TotalLiquid),
                    rain = getQuantityInMillimeters(forecasts.Night?.Rain),
                    snow = getQuantityInMillimeters(forecasts.Night?.Snow),
                    ice = getQuantityInMillimeters(forecasts.Night?.Ice)
                ),
                precipitationProbability = PrecipitationProbability(
                    total = forecasts.Night?.PrecipitationProbability?.toFloat(),
                    thunderstorm = forecasts.Night?.ThunderstormProbability?.toFloat(),
                    rain = forecasts.Night?.RainProbability?.toFloat(),
                    snow = forecasts.Night?.SnowProbability?.toFloat(),
                    ice = forecasts.Night?.IceProbability?.toFloat()
                ),
                precipitationDuration = PrecipitationDuration(
                    total = forecasts.Night?.HoursOfPrecipitation?.toFloat(),
                    rain = forecasts.Night?.HoursOfRain?.toFloat(),
                    snow = forecasts.Night?.HoursOfSnow?.toFloat(),
                    ice = forecasts.Night?.HoursOfIce?.toFloat()
                ),
                wind = Wind(
                    degree = forecasts.Night?.Wind?.Direction?.Degrees?.toFloat(),
                    speed = getSpeedInMetersPerSecond(forecasts.Night?.Wind?.Speed),
                    gusts = getSpeedInMetersPerSecond(forecasts.Night?.WindGust?.Speed)
                ),
                cloudCover = forecasts.Night?.CloudCover
            ),
            degreeDay = DegreeDay(
                heating = getDegreeDayInCelsius(forecasts.DegreeDaySummary?.Heating),
                cooling = getDegreeDayInCelsius(forecasts.DegreeDaySummary?.Cooling)
            ),
            sun = Astro(
                riseDate = forecasts.Sun?.EpochRise?.times(1000)?.toDate(),
                setDate = forecasts.Sun?.EpochSet?.times(1000)?.toDate()
            ),
            moon = Astro(
                riseDate = forecasts.Moon?.EpochRise?.times(1000)?.toDate(),
                setDate = forecasts.Moon?.EpochSet?.times(1000)?.toDate()
            ),
            moonPhase = MoonPhase(
                angle = MoonPhase.getAngleFromEnglishDescription(forecasts.Moon?.Phase)
            ),
            pollen = if (supportsPollen) getDailyPollen(forecasts.AirAndPollen) else null,
            uV = getDailyUV(forecasts.AirAndPollen),
            hoursOfSun = forecasts.HoursOfSun?.toFloat()
        )
    }
}

/**
 * Accu returns 0 / m³ for all days if they don’t measure it, instead of null values
 * This function will tell us if they measured at least one pollen or mold during the 15-day period
 * to make the difference between a 0 and a null
 */
fun supportsPollen(dailyForecasts: List<AccuForecastDailyForecast>): Boolean {
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

    val tree = list.firstOrNull { it.Name == "Tree" }
    val grass = list.firstOrNull { it.Name == "Grass" }
    val ragweed = list.firstOrNull { it.Name == "Ragweed" }
    val mold = list.firstOrNull { it.Name == "Mold" }
    return Pollen(
        tree = tree?.Value,
        grass = grass?.Value,
        ragweed = ragweed?.Value,
        mold = mold?.Value,
    )
}

private fun getDailyUV(
    list: List<AccuForecastAirAndPollen>?
): UV? {
    if (list == null) return null

    val uv = list.firstOrNull { it.Name == "UVIndex" }
    return UV(index = uv?.Value?.toFloat())
}

private fun getHourlyList(
    resultList: List<AccuForecastHourlyResult>,
    airQualityData: List<AccuAirQualityData>?
): List<HourlyWrapper> {
    return resultList.map { result ->
        HourlyWrapper(
            date = Date(result.EpochDateTime.times(1000)),
            isDaylight = result.IsDaylight,
            weatherText = result.IconPhrase,
            weatherCode = getWeatherCode(result.WeatherIcon),
            temperature = Temperature(
                temperature = getTemperatureInCelsius(result.Temperature),
                realFeelTemperature = getTemperatureInCelsius(result.RealFeelTemperature),
                realFeelShaderTemperature = getTemperatureInCelsius(result.RealFeelTemperatureShade),
                wetBulbTemperature = getTemperatureInCelsius(result.WetBulbTemperature)
            ),
            precipitation = Precipitation(
                total = getQuantityInMillimeters(result.TotalLiquid),
                rain = getQuantityInMillimeters(result.Rain),
                snow = getQuantityInMillimeters(result.Snow),
                ice = getQuantityInMillimeters(result.Ice)
            ),
            precipitationProbability = PrecipitationProbability(
                total = result.PrecipitationProbability?.toFloat(),
                thunderstorm = result.ThunderstormProbability?.toFloat(),
                rain = result.RainProbability?.toFloat(),
                snow = result.SnowProbability?.toFloat(),
                ice = result.IceProbability?.toFloat()
            ),
            wind = Wind(
                degree = result.Wind?.Direction?.Degrees?.toFloat(),
                speed = getSpeedInMetersPerSecond(result.Wind?.Speed),
                gusts = getSpeedInMetersPerSecond(result.WindGust?.Speed)
            ),
            airQuality = getAirQualityForHour(result.EpochDateTime, airQualityData),
            uV = UV(index = result.UVIndex?.toFloat()),
            relativeHumidity = result.RelativeHumidity?.toFloat(),
            dewPoint = getTemperatureInCelsius(result.DewPoint),
            cloudCover = result.CloudCover,
            visibility = getDistanceInMeters(result.Visibility)
        )
    }
}

fun getAirQualityForHour(
    requestedTime: Long,
    accuAirQualityDataList: List<AccuAirQualityData>?
): AirQuality? {
    if (accuAirQualityDataList == null) return null

    var pm25: Float? = null
    var pm10: Float? = null
    var so2: Float? = null
    var no2: Float? = null
    var o3: Float? = null
    var co: Float? = null
    accuAirQualityDataList
        .firstOrNull { it.epochDate == requestedTime }
        ?.pollutants?.forEach { p ->
            when (p.type) {
                "O3" -> o3 = p.concentration.value?.toFloat()
                "NO2" -> no2 = p.concentration.value?.toFloat()
                "PM2_5" -> pm25 = p.concentration.value?.toFloat()
                "PM10" -> pm10 = p.concentration.value?.toFloat()
                "SO2" -> so2 = p.concentration.value?.toFloat()
                "CO" -> co = p.concentration.value?.div(1000.0)?.toFloat()
            }
        }

    // Return null instead of an object initialized with null values to ease the filtering later when aggregating for daily
    return if (pm25 != null || pm10 != null || so2 != null || no2 != null || o3 != null || co != null) AirQuality(
        pM25 = pm25,
        pM10 = pm10,
        sO2 = so2,
        nO2 = no2,
        o3 = o3,
        cO = co
    ) else null
}

private fun getMinutelyList(
    minuteResult: AccuMinutelyResult?
): List<Minutely>? {
    if (minuteResult == null) return null
    if (minuteResult.Intervals.isNullOrEmpty()) return emptyList()
    return minuteResult.Intervals.map { interval ->
        Minutely(
            date = Date(interval.StartEpochDateTime),
            minuteInterval = interval.Minute,
            dbz = interval.Dbz.roundToInt()
        )
    }
}

private fun getAlertList(
    resultList: List<AccuAlertResult>?
): List<Alert>? {
    if (resultList == null) return null
    return resultList.map { result ->
        Alert(
            alertId = result.AlertID.toString(),
            startDate = result.Area?.getOrNull(0)?.let { area ->
                area.EpochStartTime?.times(1000)?.let { Date(it) }
            },
            endDate = result.Area?.getOrNull(0)?.let { area ->
                area.EpochEndTime?.times(1000)?.let { Date(it) }
            },
            description = result.Description?.Localized ?: "",
            content = result.Area?.getOrNull(0)?.Text,
            priority = result.Priority,
            color = result.Color?.let { Color.rgb(it.Red, it.Green, it.Blue) }
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

/**
 * Secondary convert
 */
fun convertSecondary(
    minuteResult: AccuMinutelyResult?,
    alertResultList: List<AccuAlertResult>?
): SecondaryWeatherWrapper {
    return SecondaryWeatherWrapper(
        minutelyForecast = getMinutelyList(minuteResult),
        alertList = getAlertList(alertResultList)
    )
}

fun getTemperatureInCelsius(value: AccuValue?): Float? {
    return if (value?.UnitType == 18) { // F
        value.Value?.minus(32)?.div(1.8)?.toFloat()
    } else value?.Value?.toFloat()
}

fun getDegreeDayInCelsius(value: AccuValue?): Float? {
    return if (value?.UnitType == 18) { // F
        value.Value?.div(1.8)?.toFloat()
    } else value?.Value?.toFloat()
}

fun getSpeedInMetersPerSecond(value: AccuValue?): Float? {
    return if (value?.UnitType == 9) { // mi/h
        value.Value?.div(2.23694)?.toFloat()
    } else value?.Value?.div(3.6)?.toFloat()
}

fun getDistanceInMeters(value: AccuValue?): Float? {
    return when (value?.UnitType) {
        2 -> value.Value?.times(1609.344)?.toFloat() // mi
        0 -> value.Value?.div(3.28084)?.toFloat() // ft
        6 -> value.Value?.times(1000)?.toFloat() // km
        else -> value?.Value?.toFloat() // m
    }
}

fun getQuantityInMillimeters(value: AccuValue?): Float? {
    return when (value?.UnitType) {
        1 -> value.Value?.times(25.4)?.toFloat() // in
        4 -> value.Value?.times(10)?.toFloat() // cm
        else -> value?.Value?.toFloat() // mm
    }
}