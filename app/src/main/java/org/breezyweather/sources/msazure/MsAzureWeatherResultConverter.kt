package org.breezyweather.sources.msazure

import org.breezyweather.common.basic.models.weather.AirQuality
import org.breezyweather.common.basic.models.weather.Alert
import org.breezyweather.common.basic.models.weather.Allergen
import org.breezyweather.common.basic.models.weather.Base
import org.breezyweather.common.basic.models.weather.Current
import org.breezyweather.common.basic.models.weather.Daily
import org.breezyweather.common.basic.models.weather.DegreeDay
import org.breezyweather.common.basic.models.weather.HalfDay
import org.breezyweather.common.basic.models.weather.History
import org.breezyweather.common.basic.models.weather.Minutely
import org.breezyweather.common.basic.models.weather.Precipitation
import org.breezyweather.common.basic.models.weather.PrecipitationDuration
import org.breezyweather.common.basic.models.weather.PrecipitationProbability
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.basic.models.weather.UV
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.common.basic.models.weather.Wind
import org.breezyweather.common.basic.wrappers.AirQualityWrapper
import org.breezyweather.common.basic.wrappers.AllergenWrapper
import org.breezyweather.common.basic.wrappers.HourlyWrapper
import org.breezyweather.common.basic.wrappers.SecondaryWeatherWrapper
import org.breezyweather.common.basic.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.WeatherException
import org.breezyweather.sources.msazure.json.airquality.MsAzureAirPollutant
import org.breezyweather.sources.msazure.json.airquality.MsAzureAirQualityForecast
import org.breezyweather.sources.msazure.json.airquality.MsAzureAirQualityForecastResponse
import org.breezyweather.sources.msazure.json.alerts.MsAzureWeatherAlert
import org.breezyweather.sources.msazure.json.alerts.MsAzureWeatherAlertsResponse
import org.breezyweather.sources.msazure.json.current.MsAzureCurrentConditions
import org.breezyweather.sources.msazure.json.current.MsAzureCurrentConditionsResponse
import org.breezyweather.sources.msazure.json.daily.MsAzureDailyForecast
import org.breezyweather.sources.msazure.json.daily.MsAzureDailyForecastResponse
import org.breezyweather.sources.msazure.json.daily.MsAzureWeatherAirTraits
import org.breezyweather.sources.msazure.json.hourly.MsAzureHourlyForecast
import org.breezyweather.sources.msazure.json.hourly.MsAzureHourlyForecastResponse
import org.breezyweather.sources.msazure.json.minutely.MsAzureMinutelyForecast
import org.breezyweather.sources.msazure.json.minutely.MsAzureMinutelyForecastResponse
import java.util.Date
import kotlin.time.Duration.Companion.hours


/**
 * Converts Microsoft Azure Maps responses into a forecast
 */
fun convertPrimary(
    currentConditionsResponse: MsAzureCurrentConditionsResponse,
    dailyForecastResponse: MsAzureDailyForecastResponse,
    hourlyForecastResponse: MsAzureHourlyForecastResponse,
    minutelyForecastResponse: MsAzureMinutelyForecastResponse,
    currentAirQualityResponse: MsAzureAirQualityForecastResponse,
    hourlyAirQualityForecastResponse: MsAzureAirQualityForecastResponse,
    alertsResponse: MsAzureWeatherAlertsResponse
): WeatherWrapper {
    if (currentConditionsResponse.results.isNullOrEmpty() ||
        hourlyForecastResponse.forecasts.isNullOrEmpty() ||
        dailyForecastResponse.forecasts.isNullOrEmpty()
    ) {
        throw WeatherException()
    }

    return WeatherWrapper(
        base = Base(
            publishDate = currentConditionsResponse.results.getOrNull(0)?.time ?: Date()
        ),
        yesterday = getYesterdayConditions(
            currentConditionsResponse.results.getOrNull(0)
        ),
        current = getCurrentForecast(
            currentConditionsResponse.results.getOrNull(0),
            currentAirQualityResponse.results?.getOrNull(0)
        ),
        dailyForecast = getDailyForecast(
            dailyForecastResponse.forecasts
        ),
        hourlyForecast = getHourlyForecast(
            hourlyForecastResponse.forecasts,
            hourlyAirQualityForecastResponse.results
        ),
        minutelyForecast = getMinutelyForecast(
            minutelyForecastResponse.intervals
        ),
        alertList = getAlertList(alertsResponse.results)
    )
}

fun convertSecondary(
    dailyForecastResponse: MsAzureDailyForecastResponse,
    minutelyForecastResponse: MsAzureMinutelyForecastResponse,
    currentAirQualityResponse: MsAzureAirQualityForecastResponse,
    hourlyAirQualityForecastResponse: MsAzureAirQualityForecastResponse,
    alertsResponse: MsAzureWeatherAlertsResponse
): SecondaryWeatherWrapper {
    return SecondaryWeatherWrapper(
        airQuality = AirQualityWrapper(
            current = getAirQuality(
                currentAirQualityResponse.results?.getOrNull(0)?.pollutants
            ),
            hourlyForecast = hourlyAirQualityForecastResponse.results?.associate {
                Pair(it.time, getAirQuality(it.pollutants))
            }
        ),
        allergen = AllergenWrapper(
            dailyForecast = dailyForecastResponse.forecasts?.associate {
                Pair(it.time, getAllergen(it.airTraits))
            }
        ),
        minutelyForecast = getMinutelyForecast(
            minutelyForecastResponse.intervals
        ),
        alertList = getAlertList(alertsResponse.results)
    )
}

/**
 * Returns yesterday's weather conditions
 */
private fun getYesterdayConditions(conditions: MsAzureCurrentConditions?): History? {
    if (conditions == null) return null

    return History(
        date = Date(conditions.time.time.hours.minus(24.hours).inWholeMilliseconds),
        daytimeTemperature = conditions.pastTemperature?.pastTwentyFourHours?.maximum?.value?.toFloat(),
        nighttimeTemperature = conditions.pastTemperature?.pastTwentyFourHours?.minimum?.value?.toFloat()
    )
}

/**
 * Returns current weather conditions
 */
private fun getCurrentForecast(
    conditions: MsAzureCurrentConditions?,
    aqResult: MsAzureAirQualityForecast?
): Current? {
    if (conditions == null) return null

    return Current(
        weatherText = conditions.description,
        weatherCode = getWeatherCode(conditions.iconCode),
        temperature = Temperature(
            temperature = conditions.temperature?.value?.toFloat(),
            realFeelTemperature = conditions.realFeelTemperature?.value?.toFloat(),
            realFeelShaderTemperature = conditions.realFeelTemperatureShade?.value?.toFloat(),
            apparentTemperature = conditions.apparentTemperature?.value?.toFloat(),
            windChillTemperature = conditions.windChillTemperature?.value?.toFloat(),
            wetBulbTemperature = conditions.wetBulbTemperature?.value?.toFloat()
        ),
        wind = Wind(
            degree = conditions.wind?.direction?.degrees?.toFloat(),
            speed = conditions.wind?.speed?.value?.toFloat(),
            gusts = conditions.windGust?.speed?.value?.toFloat()
        ),
        uV = UV(
            index = conditions.uvIndex?.toFloat()
        ),
        airQuality = getAirQuality(aqResult?.pollutants),
        relativeHumidity = conditions.relativeHumidity?.toFloat(),
        dewPoint = conditions.dewPoint?.value?.toFloat(),
        pressure = conditions.pressure?.value?.toFloat(),
        cloudCover = conditions.cloudCover,
        visibility = conditions.visibility?.value?.times(1000)?.toFloat(),
        ceiling = conditions.ceiling?.value?.toFloat()
    )
}

private fun getDailyForecast(
    days: List<MsAzureDailyForecast>?
): List<Daily>? {
    if (days.isNullOrEmpty()) return null

    return days.mapIndexed { i, day ->
        Daily(
            date = day.time,
            day = HalfDay(
                weatherText = day.day?.shortPhrase,
                weatherCode = getWeatherCode(day.day?.iconCode),
                temperature = Temperature(
                    temperature = day.temperature?.maximum?.value?.toFloat(),
                    realFeelTemperature = day.realFeelTemperature?.maximum?.value?.toFloat(),
                    realFeelShaderTemperature = day.realFeelTemperatureShade?.maximum?.value?.toFloat()
                ),
                precipitation = Precipitation(
                    total = day.day?.totalLiquid?.value?.toFloat(),
                    rain = day.day?.rain?.value?.toFloat(),
                    snow = day.day?.snow?.value?.toFloat(),
                    ice = day.day?.ice?.value?.toFloat()
                ),
                precipitationProbability = PrecipitationProbability(
                    total = day.day?.precipitationProbability?.toFloat(),
                    thunderstorm = day.day?.thunderstormProbability?.toFloat(),
                    rain = day.day?.rainProbability?.toFloat(),
                    snow = day.day?.snowProbability?.toFloat(),
                    ice = day.day?.iceProbability?.toFloat()
                ),
                precipitationDuration = PrecipitationDuration(
                    total = day.day?.hoursOfPrecipitation?.toFloat(),
                    rain = day.day?.hoursOfRain?.toFloat(),
                    snow = day.day?.hoursOfSnow?.toFloat(),
                    ice = day.day?.hoursOfIce?.toFloat()
                ),
                wind = Wind(
                    degree = day.day?.wind?.direction?.degrees?.toFloat(),
                    speed = day.day?.wind?.speed?.value?.toFloat(),
                    gusts = day.day?.windGust?.speed?.value?.toFloat()
                ),
                cloudCover = day.day?.cloudCover
            ),
            night = HalfDay(
                weatherText = day.night?.shortPhrase,
                weatherCode = getWeatherCode(day.night?.iconCode),
                temperature = Temperature(
                    temperature = day.temperature?.minimum?.value?.toFloat(),
                    realFeelTemperature = day.realFeelTemperature?.minimum?.value?.toFloat(),
                    realFeelShaderTemperature = day.realFeelTemperatureShade?.minimum?.value?.toFloat()
                ),
                precipitation = Precipitation(
                    total = day.night?.totalLiquid?.value?.toFloat(),
                    rain = day.night?.rain?.value?.toFloat(),
                    snow = day.night?.snow?.value?.toFloat(),
                    ice = day.night?.ice?.value?.toFloat()
                ),
                precipitationProbability = PrecipitationProbability(
                    total = day.night?.precipitationProbability?.toFloat(),
                    thunderstorm = day.night?.thunderstormProbability?.toFloat(),
                    rain = day.night?.rainProbability?.toFloat(),
                    snow = day.night?.snowProbability?.toFloat(),
                    ice = day.night?.iceProbability?.toFloat()
                ),
                precipitationDuration = PrecipitationDuration(
                    total = day.night?.hoursOfPrecipitation?.toFloat(),
                    rain = day.night?.hoursOfRain?.toFloat(),
                    snow = day.night?.hoursOfSnow?.toFloat(),
                    ice = day.night?.hoursOfIce?.toFloat()
                ),
                wind = Wind(
                    degree = day.night?.wind?.direction?.degrees?.toFloat(),
                    speed = day.night?.wind?.speed?.value?.toFloat(),
                    gusts = day.night?.windGust?.speed?.value?.toFloat()
                ),
                cloudCover = day.night?.cloudCover
            ),
            degreeDay = DegreeDay(
                heating = day.degreeDay?.heating?.value?.toFloat(),
                cooling = day.degreeDay?.cooling?.value?.toFloat()
            ),
            allergen = getAllergen(day.airTraits),
            uV = UV(
                index = day.airTraits?.find { it.name == "UVIndex" }?.value?.toFloat()
            ),
            hoursOfSun = day.hoursOfSun?.toFloat()
        )
    }
}

/**
 * Returns hourly forecast
 */
private fun getHourlyForecast(
    hours: List<MsAzureHourlyForecast>?,
    aqResults: List<MsAzureAirQualityForecast>?
): List<HourlyWrapper>? {
    if (hours.isNullOrEmpty()) return null

    return hours.mapIndexed { i, hour ->
        HourlyWrapper(
            date = hour.time,
            isDaylight = hour.isDaylight,
            weatherText = hour.description,
            weatherCode = getWeatherCode(hour.iconCode),
            temperature = Temperature(
                temperature = hour.temperature?.value?.toFloat(),
                realFeelTemperature = hour.realFeelTemperature?.value?.toFloat(),
                wetBulbTemperature = hour.wetBulbTemperature?.value?.toFloat()
            ),
            precipitation = Precipitation(
                total = hour.totalLiquid?.value?.toFloat(),
                rain = hour.rain?.value?.toFloat(),
                snow = hour.snow?.value?.toFloat(),
                ice = hour.ice?.value?.toFloat()
            ),
            precipitationProbability = PrecipitationProbability(
                total = hour.precipitationProbability?.toFloat(),
                rain = hour.rainProbability?.toFloat(),
                snow = hour.snowProbability?.toFloat(),
                ice = hour.iceProbability?.toFloat()
            ),
            wind = Wind(
                degree = hour.wind?.direction?.degrees?.toFloat(),
                speed = hour.wind?.speed?.value?.toFloat(),
                gusts = hour.windGust?.speed?.value?.toFloat()
            ),
            airQuality = getAirQuality(aqResults?.getOrNull(i)?.pollutants),
            uV = UV(
                index = hour.uvIndex?.toFloat(),
            ),
            relativeHumidity = hour.relativeHumidity?.toFloat(),
            dewPoint = hour.dewPoint?.value?.toFloat(),
            cloudCover = hour.cloudCover,
            visibility = hour.visibility?.value?.times(1000)?.toFloat()
        )
    }
}


/**
 * Returns minutely forecast
 */
private fun getMinutelyForecast(
    minutes: List<MsAzureMinutelyForecast>?
): List<Minutely>? {
    if (minutes.isNullOrEmpty()) return null

    val minutelyList: MutableList<Minutely> = arrayListOf()
    var previousMinute = minutes[1].minute?.let { minutes[0].minute?.minus(it) } ?: 0
    minutes.forEach { minute ->
        val interval = minute.minute?.minus(previousMinute) ?: 1
        minutelyList.add(
            Minutely(
                date = minute.time,
                minuteInterval = interval,
                precipitationIntensity = minute.precipitationIntensity
            )
        )
        previousMinute += interval
    }
    return minutelyList
}

/**
 * Returns alerts
 */
private fun getAlertList(
    alerts: List<MsAzureWeatherAlert>?
): List<Alert>? {
    if (alerts.isNullOrEmpty()) return null

    return alerts.map { alert ->
        Alert(
            alertId = alert.alertId,
            startDate = alert.alertAreas?.getOrNull(0)?.start,
            endDate = alert.alertAreas?.getOrNull(0)?.end,
            description = alert.description?.localized ?: "",
            content = alert.alertAreas?.getOrNull(0)?.alertDetails,
            priority = alert.priority ?: 1
        )
    }
}

/**
 * Returns allergen info from MsnWeatherAirTraits
 */
private fun getAllergen(airTraits: List<MsAzureWeatherAirTraits>?): Allergen {
    return Allergen(
        tree = airTraits?.find { it.name == "Tree" }?.value,
        alder = airTraits?.find { it.name == "Alder" }?.value,
        birch = airTraits?.find { it.name == "Birch" }?.value,
        grass = airTraits?.find { it.name == "Grass" }?.value,
        olive = airTraits?.find { it.name == "Olive" }?.value,
        ragweed = airTraits?.find { it.name == "Ragweed" }?.value,
        mugwort = airTraits?.find { it.name == "Mugwort" }?.value,
        mold = airTraits?.find { it.name == "Mold" }?.value
    )
}

/**
 * Returns air quality from list of pollutants
 */
private fun getAirQuality(pollutants: List<MsAzureAirPollutant>?): AirQuality {
    return AirQuality(
        pM25 = pollutants?.find { it.type == "PM2.5" }?.concentration?.value?.toFloat(),
        pM10 = pollutants?.find { it.type == "PM10" }?.concentration?.value?.toFloat(),
        sO2 = pollutants?.find { it.type == "SO2" }?.concentration?.value?.toFloat(),
        nO2 = pollutants?.find { it.type == "NO2" }?.concentration?.value?.toFloat(),
        o3 = pollutants?.find { it.type == "O3" }?.concentration?.value?.toFloat(),
        cO = pollutants?.find { it.type == "CO" }?.concentration?.value?.div(1000)?.toFloat()
    )
}

/**
 * Gets weather code
 *
 * See https://docs.pirateweather.net/en/latest/API/#icon
 */
private fun getWeatherCode(icon: Int?): WeatherCode? {
    return when (icon) {
        1, 2, 30, 31, 33, 34 -> WeatherCode.CLEAR
        3, 4, 35, 36 -> WeatherCode.PARTLY_CLOUDY
        5, 37 -> WeatherCode.HAZE
        6, 7, 8, 38 -> WeatherCode.CLOUDY
        11 -> WeatherCode.FOG
        12, 13, 14, 18, 39, 40 -> WeatherCode.RAIN
        15, 16, 17, 41, 42 -> WeatherCode.THUNDERSTORM
        19, 20, 21, 22, 23, 43, 44 -> WeatherCode.SNOW
        24, 26 -> WeatherCode.HAIL
        25, 29 -> WeatherCode.SLEET
        32 -> WeatherCode.WIND
        else -> null
    }
}