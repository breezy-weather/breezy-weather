/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.sources.openweather

import org.breezyweather.common.basic.models.Location
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
import org.breezyweather.common.basic.wrappers.AirQualityWrapper
import org.breezyweather.common.basic.wrappers.HourlyWrapper
import org.breezyweather.common.basic.wrappers.SecondaryWeatherWrapper
import org.breezyweather.common.basic.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.WeatherException
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.extensions.toTimezoneNoHour
import org.breezyweather.sources.getDailyAirQualityFromHourly
import org.breezyweather.sources.openweather.json.OpenWeatherAirPollution
import org.breezyweather.sources.openweather.json.OpenWeatherAirPollutionResult
import org.breezyweather.sources.openweather.json.OpenWeatherOneCallAlert
import org.breezyweather.sources.openweather.json.OpenWeatherOneCallDaily
import org.breezyweather.sources.openweather.json.OpenWeatherOneCallHourly
import org.breezyweather.sources.openweather.json.OpenWeatherOneCallMinutely
import org.breezyweather.sources.openweather.json.OpenWeatherOneCallResult
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

fun convert(
    location: Location,
    oneCallResult: OpenWeatherOneCallResult,
    airPollutionResult: OpenWeatherAirPollutionResult?
): WeatherWrapper {
    // If the API doesn’t return hourly or daily, consider data as garbage and keep cached data
    if (oneCallResult.hourly.isNullOrEmpty() || oneCallResult.daily.isNullOrEmpty()) {
        throw WeatherException()
    }

    val hourlyAirQuality = getHourlyAirQuality(airPollutionResult?.list)

    return WeatherWrapper(
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
                speed = oneCallResult.current.windSpeed,
                gusts = oneCallResult.current.windGust
            ),
            uV = UV(index = oneCallResult.current.uvi),
            relativeHumidity = oneCallResult.current.humidity?.toFloat(),
            dewPoint = oneCallResult.current.dewPoint,
            pressure = oneCallResult.current.pressure?.toFloat(),
            cloudCover = oneCallResult.current.clouds,
            visibility = oneCallResult.current.visibility?.toFloat()
        ) else null,
        dailyForecast = getDailyList(oneCallResult.daily, hourlyAirQuality, location.timeZone),
        hourlyForecast = getHourlyList(oneCallResult.hourly, hourlyAirQuality),
        minutelyForecast = getMinutelyList(oneCallResult.minutely),
        alertList = getAlertList(oneCallResult.alerts)
    )
}

private fun getDailyList(
    dailyResult: List<OpenWeatherOneCallDaily>,
    hourlyAirQuality: MutableMap<Date, AirQuality>,
    timeZone: TimeZone
): List<Daily> {
    val dailyAirQuality = getDailyAirQualityFromHourly(hourlyAirQuality, timeZone)
    val dailyList: MutableList<Daily> = ArrayList(dailyResult.size)
    for (i in 0 until dailyResult.size - 1) {
        val dailyForecast = dailyResult[i]
        val theDay = Date(dailyForecast.dt.times(1000)).toTimezoneNoHour(timeZone)!!
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
                airQuality = dailyAirQuality.getOrElse(theDay) { null },
                uV = UV(index = dailyForecast.uvi)
            )
        )
    }
    return dailyList
}


private fun getHourlyList(
    hourlyResult: List<OpenWeatherOneCallHourly>,
    hourlyAirQuality: MutableMap<Date, AirQuality>
): List<HourlyWrapper> {
    return hourlyResult.map { result ->
        val theDate = Date(result.dt.times(1000))
        HourlyWrapper(
            date = theDate,
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
                speed = result.windSpeed,
                gusts = result.windGust
            ),
            airQuality = hourlyAirQuality.getOrElse(theDate) { null },
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

private fun getHourlyAirQuality(
    airPollutionResultList: List<OpenWeatherAirPollution>?
): MutableMap<Date, AirQuality> {
    val airQualityHourly = mutableMapOf<Date, AirQuality>()
    airPollutionResultList?.forEach {
        airQualityHourly[it.dt.times(1000).toDate()] = AirQuality(
            pM25 = it.components?.pm2_5,
            pM10 = it.components?.pm10,
            sO2 = it.components?.so2,
            nO2 = it.components?.no2,
            o3 = it.components?.o3,
            cO = it.components?.co?.div(1000.0)?.toFloat()
        )
    }
    return airQualityHourly
}

fun convertSecondary(
    oneCallResult: OpenWeatherOneCallResult,
    airPollutionResult: OpenWeatherAirPollutionResult
): SecondaryWeatherWrapper {
    return SecondaryWeatherWrapper(
        airQuality = AirQualityWrapper(hourlyForecast = getHourlyAirQuality(airPollutionResult.list)),
        minutelyForecast = getMinutelyList(oneCallResult.minutely),
        alertList = getAlertList(oneCallResult.alerts)
    )
}