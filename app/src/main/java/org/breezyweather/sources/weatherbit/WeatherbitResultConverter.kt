package org.breezyweather.sources.weatherbit

import org.breezyweather.common.basic.models.weather.AirQuality
import org.breezyweather.common.basic.models.weather.Alert
import org.breezyweather.common.basic.models.weather.Astro
import org.breezyweather.common.basic.models.weather.Base
import org.breezyweather.common.basic.models.weather.Current
import org.breezyweather.common.basic.models.weather.Daily
import org.breezyweather.common.basic.models.weather.HalfDay
import org.breezyweather.common.basic.models.weather.Minutely
import org.breezyweather.common.basic.models.weather.MoonPhase
import org.breezyweather.common.basic.models.weather.Precipitation
import org.breezyweather.common.basic.models.weather.PrecipitationProbability
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.basic.models.weather.UV
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.common.basic.models.weather.Wind
import org.breezyweather.common.basic.wrappers.HourlyWrapper
import org.breezyweather.common.basic.wrappers.WeatherResultWrapper
import org.breezyweather.common.exceptions.WeatherException
import org.breezyweather.common.extensions.plus
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.extensions.toTimezone
import org.breezyweather.sources.weatherbit.json.WeatherbitAirQuality
import org.breezyweather.sources.weatherbit.json.WeatherbitAlert
import org.breezyweather.sources.weatherbit.json.WeatherbitCurrent
import org.breezyweather.sources.weatherbit.json.WeatherbitCurrentResponse
import org.breezyweather.sources.weatherbit.json.WeatherbitDaily
import org.breezyweather.sources.weatherbit.json.WeatherbitHourly
import org.breezyweather.sources.weatherbit.json.WeatherbitMinutely
import org.breezyweather.sources.weatherbit.json.WeatherbitResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt


/**
 * Converts weatherbit.io result into a forecast
 */
fun convert(
    currentResult: WeatherbitCurrentResponse,
    minutelyResult: WeatherbitResponse<WeatherbitMinutely>?,
    hourlyResult: WeatherbitResponse<WeatherbitHourly>?,
    dailyResult: WeatherbitResponse<WeatherbitDaily>,
    aqCurrentResult: WeatherbitResponse<WeatherbitAirQuality>?,
    aqHourlyResult: WeatherbitResponse<WeatherbitAirQuality>?
): WeatherResultWrapper {
    // If the API does not return current or daily, consider data as garbage and keep cached data
    if (currentResult.current.isNullOrEmpty() || dailyResult.data.isNullOrEmpty()) {
        throw WeatherException()
    }

    val timezone = TimeZone.getTimeZone(currentResult.current[0].timezone)

    return WeatherResultWrapper(
        base = Base(
            publishDate = currentResult.current[0].time.times(1000).toDate()
        ),
        current = getCurrentForecast(currentResult.current[0], aqCurrentResult?.data?.get(0)),
        dailyForecast = getDailyForecast(dailyResult.data),
        hourlyForecast = getHourlyForecast(hourlyResult?.data, aqHourlyResult?.data),
        minutelyForecast = getMinutelyForecast(minutelyResult?.data),
        alertList = getAlertList(currentResult.alerts, timezone)
    )
}

/**
 * Returns current weather forecast
 */
private fun getCurrentForecast(
    result: WeatherbitCurrent, aq: WeatherbitAirQuality?
): Current {
    return Current(
        weatherText = result.weather?.description ?: "",
        weatherCode = getWeatherCode(result.weather?.code),
        temperature = Temperature(
            temperature = result.temperature, apparentTemperature = result.apparentTemperature
        ),
        wind = Wind(
            degree = result.windDir?.toFloat(),
            speed = result.windSpeed?.times(3.6f),
        ),
        uV = UV(
            index = result.uvIndex
        ),
        airQuality = AirQuality(
            pM25 = aq?.pm25?.toFloat(),
            pM10 = aq?.pm10?.toFloat(),
            sO2 = aq?.so2?.toFloat(),
            nO2 = aq?.no2?.toFloat(),
            o3 = aq?.o3?.toFloat()
        ),
        relativeHumidity = result.humidity?.toFloat(),
        dewPoint = result.dewPoint,
        pressure = result.pressure,
        cloudCover = result.cloudCover,
        visibility = result.visibility
    )
}

private fun getDailyForecast(
    dailyResults: List<WeatherbitDaily>
): List<Daily> {
    return dailyResults.map { result ->
        Daily(
            date = result.time?.times(1000)?.toDate() ?: Date(),
            day = HalfDay(
                weatherText = result.weather?.description,
                weatherCode = getWeatherCode(result.weather?.code),
                temperature = Temperature(
                    temperature = result.highTemperature,
                    apparentTemperature = result.apparentMaxTemperature
                )
            ),
            night = HalfDay(
                temperature = Temperature(
                    temperature = result.lowTemperature,
                    apparentTemperature = result.apparentMinTemperature
                ),
            ),
            sun = Astro(
                riseDate = result.sunrise?.times(1000)?.toDate(),
                setDate = result.sunset?.times(1000)?.toDate()
            ),
            moon = Astro(
                riseDate = result.moonrise?.times(1000)?.toDate(),
                setDate = result.moonset?.times(1000)?.toDate()
            ),
            moonPhase = MoonPhase(
                angle = result.moonPhase?.times(360)?.roundToInt()
            ),
            uV = UV(
                index = result.uvIndex
            )
        )
    }
}

/**
 * Returns hourly forecast
 */
private fun getHourlyForecast(
    hourlyResult: List<WeatherbitHourly>?, hourlyAq: List<WeatherbitAirQuality>?
): List<HourlyWrapper>? {
    return hourlyResult?.mapIndexed { index, result ->
        HourlyWrapper(
            date = result.time.times(1000).toDate(),
            weatherText = result.weather?.description,
            weatherCode = getWeatherCode(result.weather?.code),
            temperature = Temperature(
                temperature = result.temperature, apparentTemperature = result.apparentTemperature
            ),
            precipitation = Precipitation(
                total = result.precipitation?.plus(result.snow),
                rain = result.precipitation,
                snow = result.snow,
            ),
            precipitationProbability = PrecipitationProbability(
                total = result.precipitationProbability?.toFloat()
            ),
            wind = Wind(
                degree = result.windDir?.toFloat(),
                speed = result.windSpeed?.times(3.6f),
            ),
            airQuality = hourlyAq?.getOrNull(index)?.let {
                AirQuality(
                    pM25 = it.pm25?.toFloat(),
                    pM10 = it.pm10?.toFloat(),
                    sO2 = it.so2?.toFloat(),
                    nO2 = it.no2?.toFloat(),
                    o3 = it.o3?.toFloat()
                )
            },
            uV = UV(
                index = result.uvIndex,
            ),
            relativeHumidity = result.humidity?.times(100)?.toFloat(),
            dewPoint = result.dewPoint?.toFloat(),
            pressure = result.pressure,
            cloudCover = result.cloudCover,
            visibility = result.visibility?.toFloat(),
        )
    }
}


/**
 * Returns minutely forecast
 * Copied from openweather implementation
 */
private fun getMinutelyForecast(
    minutelyResult: List<WeatherbitMinutely>?
): List<Minutely>? {
    if (minutelyResult.isNullOrEmpty()) return null
    val minutelyList: MutableList<Minutely> = arrayListOf()
    minutelyResult.forEachIndexed { i, minutelyForecast ->
        minutelyList.add(
            Minutely(
                date = Date(minutelyForecast.time * 1000),
                minuteInterval = if (i < minutelyResult.size - 1) {
                    ((minutelyResult[i + 1].time - minutelyForecast.time) / 60).toDouble()
                        .roundToInt()
                } else ((minutelyForecast.time - minutelyResult[i - 1].time) / 60).toDouble()
                    .roundToInt(),
                precipitationIntensity = minutelyForecast.precipitation?.toDouble()
            )
        )
    }
    return minutelyList
}

/**
 * Returns alerts
 */
private fun getAlertList(
    alertList: List<WeatherbitAlert>?,
    timezone: TimeZone
): List<Alert>? {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    return alertList?.map { alert ->
        Alert(
            // TODO: Avoid having the same ID for two different alerts starting at the same time
            alertId = (alert.title ?: "").length.plus((alert.description ?: "").length).toLong(),
            startDate = formatter.parse(alert.onsetUtc ?: alert.effectiveUtc)?.toTimezone(timezone),
            endDate = formatter.parse(alert.endsUtc ?: alert.expiresUtc)?.toTimezone(timezone),
            description = alert.title ?: "",
            content = alert.description,
            priority = when (alert.severity) {
                "Advisory" -> 4
                "Watch" -> 3
                "Warning" -> 2
                else -> 1
            }
        )
    }
}

/**
 * Gets weather code
 * See https://www.weatherbit.io/api/codes
 */
private fun getWeatherCode(code: Int?): WeatherCode? {
    return when (code) {
        200, 201, 202, 230, 231, 232, 233 -> WeatherCode.THUNDERSTORM
        300, 301, 302, 500, 501, 502, 511, 520, 521, 522 -> WeatherCode.RAIN
        600, 601, 602 -> WeatherCode.SNOW
        610, 611, 612, 621, 622, 623 -> WeatherCode.SLEET
        700, 741, 751 -> WeatherCode.FOG
        711, 721, 731 -> WeatherCode.HAZE
        800 -> WeatherCode.CLEAR
        801, 802 -> WeatherCode.PARTLY_CLOUDY
        803, 804 -> WeatherCode.CLOUDY
        900 -> null
        else -> null
    }
}