package org.breezyweather.sources.metno

import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.AirQuality
import org.breezyweather.common.basic.models.weather.Astro
import org.breezyweather.common.basic.models.weather.Base
import org.breezyweather.common.basic.models.weather.Current
import org.breezyweather.common.basic.models.weather.Daily
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
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.sources.metno.json.MetNoAirQualityResult
import org.breezyweather.sources.metno.json.MetNoForecastResult
import org.breezyweather.sources.metno.json.MetNoForecastTimeseries
import org.breezyweather.sources.metno.json.MetNoMoonProperties
import org.breezyweather.sources.metno.json.MetNoMoonResult
import org.breezyweather.sources.metno.json.MetNoNowcastResult
import org.breezyweather.sources.metno.json.MetNoSunProperties
import org.breezyweather.sources.metno.json.MetNoSunResult
import java.util.Date
import java.util.TimeZone
import kotlin.math.roundToInt

fun convert(
    location: Location,
    forecastResult: MetNoForecastResult,
    sunResult: MetNoSunResult,
    moonResult: MetNoMoonResult,
    nowcastResult: MetNoNowcastResult,
    airQualityResult: MetNoAirQualityResult
): WeatherResultWrapper {
    // If the API doesn’t return hourly, consider data as garbage and keep cached data
    if (forecastResult.properties == null
        || forecastResult.properties.timeseries.isNullOrEmpty()) {
        throw WeatherException()
    }

    val currentTimeseries = nowcastResult.properties?.timeseries?.getOrNull(0)?.data
    return WeatherResultWrapper(
        base = Base(
            // TODO: Use nowcast updatedAt if available
            publishDate = forecastResult.properties.meta?.updatedAt ?: Date()
        ),
        current = if (currentTimeseries != null) Current(
            weatherText = null, // TODO: From symbolCode
            weatherCode = getWeatherCode(currentTimeseries.symbolCode),
            temperature = Temperature(
                temperature = currentTimeseries.instant?.details?.airTemperature,
            ),
            wind = if (currentTimeseries.instant?.details != null) Wind(
                degree = currentTimeseries.instant.details.windFromDirection,
                speed = currentTimeseries.instant.details.windSpeed?.times(3.6f)
            ) else null,
            relativeHumidity = currentTimeseries.instant?.details?.relativeHumidity,
            dewPoint = currentTimeseries.instant?.details?.dewPointTemperature,
            pressure = currentTimeseries.instant?.details?.airPressureAtSeaLevel
        ) else null,
        dailyForecast = getDailyList(
            location.timeZone,
            sunResult.properties,
            moonResult.properties,
            forecastResult.properties.timeseries
        ),
        hourlyForecast = getHourlyList(
            forecastResult.properties.timeseries,
            airQualityResult
        ),
        minutelyForecast = getMinutelyList(nowcastResult.properties?.timeseries)
    )
}

private fun getHourlyList(
    forecastTimeseries: List<MetNoForecastTimeseries>,
    airQualityResult: MetNoAirQualityResult
): List<HourlyWrapper> {
    return forecastTimeseries.map { hourlyForecast ->
        val airQualityDataResult = airQualityResult.data?.time?.firstOrNull { it.from.time == hourlyForecast.time.time }

        HourlyWrapper(
            date = hourlyForecast.time,
            weatherText = null, // TODO: From symbolCode
            weatherCode = getWeatherCode(hourlyForecast.data?.symbolCode),
            temperature = Temperature(
                temperature = hourlyForecast.data?.instant?.details?.airTemperature,
            ),
            precipitation = Precipitation(
                total = hourlyForecast.data?.next1Hours?.details?.precipitationAmount
                    ?: hourlyForecast.data?.next6Hours?.details?.precipitationAmount
                    ?: hourlyForecast.data?.next12Hours?.details?.precipitationAmount
            ),
            precipitationProbability = PrecipitationProbability(
                total = hourlyForecast.data?.next1Hours?.details?.probabilityOfPrecipitation
                    ?: hourlyForecast.data?.next6Hours?.details?.probabilityOfPrecipitation
                    ?: hourlyForecast.data?.next12Hours?.details?.probabilityOfPrecipitation,
                thunderstorm = hourlyForecast.data?.next1Hours?.details?.probabilityOfThunder
                    ?: hourlyForecast.data?.next6Hours?.details?.probabilityOfThunder
                    ?: hourlyForecast.data?.next12Hours?.details?.probabilityOfThunder
            ),
            wind = if (hourlyForecast.data?.instant?.details != null) Wind(
                degree = hourlyForecast.data.instant.details.windFromDirection,
                speed = hourlyForecast.data.instant.details.windSpeed?.times(3.6f)
            ) else null,
            airQuality = if (airQualityDataResult != null) AirQuality(
                pM25 = airQualityDataResult.variables?.pm25Concentration?.value,
                pM10 = airQualityDataResult.variables?.pm10Concentration?.value,
                sO2 = airQualityDataResult.variables?.so2Concentration?.value,
                nO2 = airQualityDataResult.variables?.no2Concentration?.value,
                o3 = airQualityDataResult.variables?.o3Concentration?.value
            ) else null,
            uV = UV(index = hourlyForecast.data?.instant?.details?.ultravioletIndexClearSky),
            relativeHumidity = hourlyForecast.data?.instant?.details?.relativeHumidity,
            dewPoint = hourlyForecast.data?.instant?.details?.dewPointTemperature,
            pressure = hourlyForecast.data?.instant?.details?.airPressureAtSeaLevel
        )
    }
}

private fun getDailyList(
    timeZone: TimeZone,
    sunResult: MetNoSunProperties?,
    moonResult: MetNoMoonProperties?,
    forecastTimeseries: List<MetNoForecastTimeseries>
): List<Daily> {
    val dailyList: MutableList<Daily> = ArrayList()
    val hourlyListByDay = forecastTimeseries.groupBy { it.time.getFormattedDate(timeZone, "yyyy-MM-dd") }
    for (i in 0 until hourlyListByDay.entries.size - 1) {
        val dayDate = hourlyListByDay.keys.toTypedArray()[i].toDateNoHour(timeZone)
        if (dayDate != null) {
            dailyList.add(
                Daily(
                    date = dayDate,
                    sun = if (i == 0) Astro(
                        riseDate = sunResult?.sunrise?.time,
                        setDate = sunResult?.sunset?.time,
                    ) else null,
                    moon = if (i == 0) Astro(
                        riseDate = moonResult?.moonrise?.time,
                        setDate = moonResult?.moonset?.time,
                    ) else null,
                    moonPhase = if (i == 0) MoonPhase(
                        angle = moonResult?.moonphase?.roundToInt()
                    ) else null
                )
            )
        }
    }
    return dailyList
}

private fun getMinutelyList(nowcastTimeseries: List<MetNoForecastTimeseries>?): List<Minutely> {
    val minutelyList: MutableList<Minutely> = arrayListOf()
    if (nowcastTimeseries.isNullOrEmpty()) return minutelyList

    nowcastTimeseries.forEachIndexed { i, nowcastForecast ->
        minutelyList.add(
            Minutely(
                date = nowcastForecast.time,
                minuteInterval = if (i < nowcastTimeseries.size - 1) {
                    ((nowcastTimeseries[i + 1].time.time - nowcastForecast.time.time) / (60 * 1000)).toDouble()
                        .roundToInt()
                } else ((nowcastForecast.time.time - nowcastTimeseries[i - 1].time.time) / (60 * 1000)).toDouble()
                    .roundToInt(),
                precipitationIntensity = nowcastForecast.data?.instant?.details?.precipitationRate?.toDouble()
            )
        )
    }

    return minutelyList
}

private fun getWeatherCode(icon: String?): WeatherCode? {
    return if (icon == null) {
        null
    } else when(icon.replace("_night", "").replace("_day", "")) {
        "clearsky", "fair" -> WeatherCode.CLEAR
        "partlycloudy" -> WeatherCode.PARTLY_CLOUDY
        "cloudy" -> WeatherCode.CLOUDY
        "fog" -> WeatherCode.FOG
        "heavyrain", "heavyrainshowers", "lightrain", "lightrainshowers", "rain", "rainshowers" -> WeatherCode.RAIN
        "heavyrainandthunder", "heavyrainshowersandthunder", "heavysleetandthunder", "heavysleetshowersandthunder",
        "heavysnowandthunder", "heavysnowshowersandthunder", "lightrainandthunder", "lightrainshowersandthunder",
        "lightsleetandthunder", "lightsleetshowersandthunder", "lightsnowandthunder", "lightsnowshowersandthunder",
        "rainandthunder", "rainshowersandthunder", "sleetandthunder", "sleetshowersandthunder", "snowandthunder",
        "snowshowersandthunder" -> WeatherCode.THUNDERSTORM
        "heavysnow", "heavysnowshowers", "lightsnow", "lightsnowshowers", "snow", "snowshowers" -> WeatherCode.SNOW
        "heavysleet", "heavysleetshowers", "lightsleet", "lightsleetshowers", "sleet",
        "sleetshowers" -> WeatherCode.SLEET
        else -> null
    }
}
