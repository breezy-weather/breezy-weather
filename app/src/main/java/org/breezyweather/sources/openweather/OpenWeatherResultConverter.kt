package org.breezyweather.sources.openweather

import org.breezyweather.common.basic.models.weather.AirQuality
import org.breezyweather.common.basic.models.weather.Alert
import org.breezyweather.common.basic.models.weather.Astro
import org.breezyweather.common.basic.models.weather.Base
import org.breezyweather.common.basic.models.weather.Current
import org.breezyweather.common.basic.models.weather.Daily
import org.breezyweather.common.basic.models.weather.HalfDay
import org.breezyweather.common.basic.models.weather.Minutely
import org.breezyweather.common.basic.models.weather.Precipitation
import org.breezyweather.common.basic.models.weather.PrecipitationProbability
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.basic.models.weather.UV
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.common.basic.models.weather.Wind
import org.breezyweather.common.basic.wrappers.HourlyWrapper
import org.breezyweather.common.basic.wrappers.WeatherResultWrapper
import org.breezyweather.common.exceptions.WeatherException
import org.breezyweather.common.extensions.toDate
import org.breezyweather.sources.openweather.json.OpenWeatherAirPollutionResult
import org.breezyweather.sources.openweather.json.OpenWeatherOneCallAlert
import org.breezyweather.sources.openweather.json.OpenWeatherOneCallDaily
import org.breezyweather.sources.openweather.json.OpenWeatherOneCallHourly
import org.breezyweather.sources.openweather.json.OpenWeatherOneCallMinutely
import org.breezyweather.sources.openweather.json.OpenWeatherOneCallResult
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

fun convert(
    oneCallResult: OpenWeatherOneCallResult,
    airPollutionResult: OpenWeatherAirPollutionResult?
): WeatherResultWrapper {
    // If the API doesn’t return hourly or daily, consider data as garbage and keep cached data
    if (oneCallResult.hourly.isNullOrEmpty() || oneCallResult.daily.isNullOrEmpty()) {
        throw WeatherException()
    }

    return WeatherResultWrapper(
        base = Base(
            publishDate = oneCallResult.current?.dt?.times(1000)?.toDate() ?: Date()
        ),
        current = if (oneCallResult.current != null) Current(
            weatherText = oneCallResult.current.weather?.getOrNull(0)?.description?.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            },
            weatherCode = getWeatherCode(oneCallResult.current.weather?.getOrNull(0)?.id),
            temperature = Temperature(
                temperature = oneCallResult.current.temp,
                apparentTemperature = oneCallResult.current.feelsLike
            ),
            wind = Wind(
                degree = oneCallResult.current.windDeg?.toFloat(),
                speed = oneCallResult.current.windSpeed?.times(3.6f)
            ),
            uV = UV(index = oneCallResult.current.uvi),
            relativeHumidity = oneCallResult.current.humidity?.toFloat(),
            dewPoint = oneCallResult.current.dewPoint,
            pressure = oneCallResult.current.pressure?.toFloat(),
            cloudCover = oneCallResult.current.clouds,
            visibility = (oneCallResult.current.visibility?.div(1000.0))?.toFloat()
        ) else null,
        dailyForecast = getDailyList(oneCallResult.daily),
        hourlyForecast = getHourlyList(oneCallResult.hourly, airPollutionResult),
        minutelyForecast = getMinutelyList(oneCallResult.minutely),
        alertList = getAlertList(oneCallResult.alerts)
    )
}

private fun getDailyList(
    dailyResult: List<OpenWeatherOneCallDaily>
): List<Daily> {
    val dailyList: MutableList<Daily> = ArrayList(dailyResult.size)
    for (i in 0 until dailyResult.size - 1) {
        val dailyForecast = dailyResult[i]
        val theDay = Date(dailyForecast.dt.times(1000))
        dailyList.add(
            Daily(
                date = theDay,
                day = HalfDay(
                    weatherText = dailyForecast.weather?.getOrNull(0)?.description?.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    },
                    weatherPhase = dailyForecast.weather?.getOrNull(0)?.description,
                    weatherCode = getWeatherCode(dailyForecast.weather?.getOrNull(0)?.id),
                    temperature = Temperature(
                        temperature = dailyForecast.temp?.max,
                        apparentTemperature = dailyForecast.feelsLike?.eve
                    )
                ),
                night = HalfDay(
                    weatherText = dailyForecast.weather?.getOrNull(0)?.description?.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    },
                    weatherPhase = dailyForecast.weather?.getOrNull(0)?.description,
                    weatherCode = getWeatherCode(dailyForecast.weather?.getOrNull(0)?.id),
                    // night temperature is actually from previous night,
                    // so we try to get night from next day if available
                    temperature = Temperature(
                        temperature = dailyResult[i + 1].temp?.min,
                        apparentTemperature = dailyResult[i + 1].feelsLike?.morn
                    )
                ),
                sun = Astro(
                    riseDate = dailyForecast.sunrise?.times(1000)?.toDate(),
                    setDate = dailyForecast.sunset?.times(1000)?.toDate()
                ),
                moon = Astro(
                    riseDate = dailyForecast.moonrise?.times(1000)?.toDate(),
                    setDate = dailyForecast.moonset?.times(1000)?.toDate()
                ),
                uV = UV(index = dailyForecast.uvi)
            )
        )
    }
    return dailyList
}


private fun getHourlyList(
    hourlyResult: List<OpenWeatherOneCallHourly>,
    airPollutionResult: OpenWeatherAirPollutionResult?
): List<HourlyWrapper> {
    return hourlyResult.map { result ->
        HourlyWrapper(
            date = Date(result.dt.times(1000)),
            weatherText = result.weather?.getOrNull(0)?.main?.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            },
            weatherCode = getWeatherCode(result.weather?.getOrNull(0)?.id),
            temperature = Temperature(
                temperature = result.temp,
                apparentTemperature = result.feelsLike
            ),
            precipitation = Precipitation(
                total = getTotalPrecipitation(result.rain?.cumul1h, result.snow?.cumul1h),
                rain = result.rain?.cumul1h,
                snow = result.snow?.cumul1h
            ),
            precipitationProbability = PrecipitationProbability(total = result.pop?.times(100f)),
            wind = Wind(
                degree = result.windDeg?.toFloat(),
                speed = result.windSpeed?.times(3.6f)
            ),
            airQuality = getAirQuality(result.dt, airPollutionResult),
            uV = UV(index = result.uvi),
            relativeHumidity = result.humidity?.toFloat(),
            dewPoint = result.dewPoint,
            pressure = result.pressure?.toFloat(),
            cloudCover = result.clouds,
            visibility = result.visibility?.toFloat()
        )
    }
}

// Function that checks for null before sum up
private fun getTotalPrecipitation(rain: Float?, snow: Float?): Float? {
    if (rain == null) {
        return snow
    }
    return if (snow == null) {
        rain
    } else rain + snow
}

private fun getMinutelyList(minutelyResult: List<OpenWeatherOneCallMinutely>?): List<Minutely> {
    val minutelyList: MutableList<Minutely> = arrayListOf()
    minutelyResult?.forEachIndexed { i, minutelyForecast ->
        minutelyList.add(
            Minutely(
                date = Date(minutelyForecast.dt * 1000),
                minuteInterval = if (i < minutelyResult.size - 1) {
                    ((minutelyResult[i + 1].dt - minutelyForecast.dt) / 60).toDouble().roundToInt()
                } else ((minutelyForecast.dt - minutelyResult[i - 1].dt) / 60).toDouble()
                    .roundToInt(),
                precipitationIntensity = minutelyForecast.precipitation?.toDouble()
            )
        )
    }
    return minutelyList
}

private fun getAirQuality(
    requestedTime: Long,
    ownAirPollutionResult: OpenWeatherAirPollutionResult?
): AirQuality? {
    if (ownAirPollutionResult == null) return null

    val matchingAirQualityForecast =
        ownAirPollutionResult.list?.firstOrNull { it.dt == requestedTime } ?: return null

    val pm25: Float? = matchingAirQualityForecast.components?.pm2_5
    val pm10: Float? = matchingAirQualityForecast.components?.pm10
    val so2: Float? = matchingAirQualityForecast.components?.so2
    val no2: Float? = matchingAirQualityForecast.components?.no2
    val o3: Float? = matchingAirQualityForecast.components?.o3
    val co: Float? = matchingAirQualityForecast.components?.co?.div(1000.0)?.toFloat()

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

private fun getAlertList(resultList: List<OpenWeatherOneCallAlert>?): List<Alert> {
    return if (resultList != null) {
        return resultList.map { result ->
            Alert(
                // TODO: Avoid having the same ID for two different alerts starting at the same time
                alertId = result.start,
                startDate = Date(result.start.times(1000)),
                endDate = Date(result.end.times(1000)),
                description = result.event ?: "",
                content = result.description,
                priority = 1 // Does not exist
            )
        }
    } else {
        emptyList()
    }
}

private fun getWeatherCode(icon: Int?): WeatherCode? {
    return when (icon) {
        null -> null
        200, 201, 202 -> WeatherCode.THUNDERSTORM
        210, 211, 212 -> WeatherCode.THUNDER
        221, 230, 231, 232 -> WeatherCode.THUNDERSTORM
        300, 301, 302, 310, 311, 312, 313, 314, 321 -> WeatherCode.RAIN
        500, 501, 502, 503, 504 -> WeatherCode.RAIN
        511 -> WeatherCode.SLEET
        600, 601, 602 -> WeatherCode.SNOW
        611, 612, 613, 614, 615, 616 -> WeatherCode.SLEET
        620, 621, 622 -> WeatherCode.SNOW
        701, 711, 721, 731 -> WeatherCode.HAZE
        741 -> WeatherCode.FOG
        751, 761, 762 -> WeatherCode.HAZE
        771, 781 -> WeatherCode.WIND
        800 -> WeatherCode.CLEAR
        801, 802 -> WeatherCode.PARTLY_CLOUDY
        803, 804 -> WeatherCode.CLOUDY
        else -> null
    }
}