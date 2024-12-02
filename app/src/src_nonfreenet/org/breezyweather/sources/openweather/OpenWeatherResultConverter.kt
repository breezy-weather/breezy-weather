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

package org.breezyweather.sources.openweather

import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Current
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.extensions.capitalize
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.sources.getDailyAirQualityFromHourly
import org.breezyweather.sources.openweather.json.OpenWeatherAirPollution
import org.breezyweather.sources.openweather.json.OpenWeatherAirPollutionResult
import org.breezyweather.sources.openweather.json.OpenWeatherForecast
import org.breezyweather.sources.openweather.json.OpenWeatherForecastResult
import java.util.Date
import kotlin.time.Duration.Companion.seconds

fun convert(
    location: Location,
    forecastResult: OpenWeatherForecastResult,
    currentResult: OpenWeatherForecast,
    airPollutionResult: OpenWeatherAirPollutionResult?,
    failedFeatures: List<SourceFeature>,
): WeatherWrapper {
    // If the API doesn’t return hourly, consider data as garbage and keep cached data
    if (forecastResult.list.isNullOrEmpty()) {
        throw InvalidOrIncompleteDataException()
    }

    val hourlyAirQuality = getHourlyAirQuality(airPollutionResult?.list)

    return WeatherWrapper(
        current = getCurrent(currentResult),
        dailyForecast = getDailyList(forecastResult.list, hourlyAirQuality, location),
        hourlyForecast = getHourlyList(forecastResult.list, hourlyAirQuality),
        failedFeatures = failedFeatures
    )
}

fun getCurrent(currentResult: OpenWeatherForecast): Current? {
    if (currentResult.dt == null) return null

    return Current(
        weatherText = currentResult.weather?.getOrNull(0)?.main?.capitalize(),
        weatherCode = getWeatherCode(currentResult.weather?.getOrNull(0)?.id),
        temperature = Temperature(
            temperature = currentResult.main?.temp,
            apparentTemperature = currentResult.main?.feelsLike
        ),
        wind = Wind(
            degree = currentResult.wind?.deg?.toDouble(),
            speed = currentResult.wind?.speed,
            gusts = currentResult.wind?.gust
        ),
        relativeHumidity = currentResult.main?.humidity?.toDouble(),
        pressure = currentResult.main?.pressure?.toDouble(),
        cloudCover = currentResult.clouds?.all,
        visibility = currentResult.visibility?.toDouble()
    )
}

private fun getDailyList(
    hourlyResult: List<OpenWeatherForecast>,
    hourlyAirQuality: MutableMap<Date, AirQuality>,
    location: Location,
): List<Daily> {
    val dailyAirQuality = getDailyAirQualityFromHourly(hourlyAirQuality, location)
    val dailyList = mutableListOf<Daily>()
    val hourlyListByDay = hourlyResult.groupBy {
        it.dt!!.seconds.inWholeMilliseconds.toDate().getFormattedDate("yyyy-MM-dd", location)
    }
    for (i in 0 until hourlyListByDay.entries.size - 1) {
        val dayDate = hourlyListByDay.keys.toTypedArray()[i].toDateNoHour(location.javaTimeZone)
        if (dayDate != null) {
            dailyList.add(
                Daily(
                    date = dayDate,
                    airQuality = dailyAirQuality.getOrElse(dayDate) { null }
                )
            )
        }
    }
    return dailyList
}

private fun getHourlyList(
    hourlyResult: List<OpenWeatherForecast>,
    hourlyAirQuality: MutableMap<Date, AirQuality>,
): List<HourlyWrapper> {
    return hourlyResult.map { result ->
        val theDate = result.dt!!.seconds.inWholeMilliseconds.toDate()
        HourlyWrapper(
            date = theDate,
            weatherText = result.weather?.getOrNull(0)?.main?.capitalize(),
            weatherCode = getWeatherCode(result.weather?.getOrNull(0)?.id),
            temperature = Temperature(
                temperature = result.main?.temp,
                apparentTemperature = result.main?.feelsLike
            ),
            precipitation = Precipitation(
                total = getTotalPrecipitation(result.rain?.cumul3h, result.snow?.cumul3h),
                rain = result.rain?.cumul3h,
                snow = result.snow?.cumul3h
            ),
            precipitationProbability = PrecipitationProbability(total = result.pop?.times(100.0)),
            wind = Wind(
                degree = result.wind?.deg?.toDouble(),
                speed = result.wind?.speed,
                gusts = result.wind?.gust
            ),
            airQuality = hourlyAirQuality.getOrElse(theDate) { null },
            relativeHumidity = result.main?.humidity?.toDouble(),
            pressure = result.main?.pressure?.toDouble(),
            cloudCover = result.clouds?.all,
            visibility = result.visibility?.toDouble()
        )
    }
}

// Function that checks for null before sum up
private fun getTotalPrecipitation(rain: Double?, snow: Double?): Double? {
    if (rain == null) {
        return snow
    }
    return if (snow == null) {
        rain
    } else {
        rain + snow
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
    airPollutionResultList: List<OpenWeatherAirPollution>?,
): MutableMap<Date, AirQuality> {
    val airQualityHourly = mutableMapOf<Date, AirQuality>()
    airPollutionResultList?.forEach {
        airQualityHourly[it.dt.seconds.inWholeMilliseconds.toDate()] = AirQuality(
            pM25 = it.components?.pm25,
            pM10 = it.components?.pm10,
            sO2 = it.components?.so2,
            nO2 = it.components?.no2,
            o3 = it.components?.o3,
            cO = it.components?.co?.div(1000.0)
        )
    }
    return airQualityHourly
}

fun convertSecondary(
    currentResult: OpenWeatherForecast,
    airPollutionResult: OpenWeatherAirPollutionResult?,
    failedFeatures: List<SourceFeature>,
): SecondaryWeatherWrapper {
    return SecondaryWeatherWrapper(
        current = getCurrent(currentResult),
        airQuality = if (airPollutionResult != null) {
            AirQualityWrapper(hourlyForecast = getHourlyAirQuality(airPollutionResult.list))
        } else {
            null
        },
        failedFeatures = failedFeatures
    )
}
