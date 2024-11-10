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

package org.breezyweather.sources.metoffice

import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.HalfDay
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.sources.metoffice.json.MetOfficeDaily
import org.breezyweather.sources.metoffice.json.MetOfficeForecast
import org.breezyweather.sources.metoffice.json.MetOfficeHourly

/**
 * Converts Met Office result into a forecast
 */
fun convert(
    hourlyForecastResult: MetOfficeForecast<MetOfficeHourly>,
    dailyForecastResult: MetOfficeForecast<MetOfficeDaily>
): WeatherWrapper {
    if (hourlyForecastResult.features.isEmpty() && dailyForecastResult.features.isEmpty()) {
        throw InvalidOrIncompleteDataException()
    }

    return WeatherWrapper(
        hourlyForecast = getHourlyForecast(hourlyForecastResult),
        dailyForecast = getDailyForecast(dailyForecastResult)
    )
}

private fun getDailyForecast(
    dailyResult: MetOfficeForecast<MetOfficeDaily>
): List<Daily> {
    val feature = dailyResult.features[0] // should only be one feature for this kind of API call
    return feature.properties.timeSeries.map { result ->
        val (dayText, dayCode) = convertWeatherCode(result.daySignificantWeatherCode)
            ?: Pair(null, null)
        val (nightText, nightCode) = convertWeatherCode(result.nightSignificantWeatherCode)
            ?: Pair(null, null)
        Daily(
            date = result.time,
            day = HalfDay(
                weatherText = dayText,
                weatherCode = dayCode,
                temperature = Temperature(
                    temperature = result.dayMaxScreenTemperature,
                    apparentTemperature = result.dayMaxFeelsLikeTemp,
                ),
                precipitationProbability = PrecipitationProbability(
                    total = result.dayProbabilityOfPrecipitation?.toDouble(),
                    rain = result.dayProbabilityOfRain?.toDouble(),
                    snow = result.dayProbabilityOfSnow?.toDouble(),
                    thunderstorm = result.dayProbabilityOfSferics?.toDouble()
                ),
            ),
            night = HalfDay(
                weatherText = nightText,
                weatherCode = nightCode,
                temperature = Temperature(
                    temperature = result.nightMinScreenTemperature,
                    apparentTemperature = result.nightMinFeelsLikeTemp,
                ),
                precipitationProbability = PrecipitationProbability(
                    total = result.nightProbabilityOfPrecipitation?.toDouble(),
                    rain = result.nightProbabilityOfRain?.toDouble(),
                    snow = result.nightProbabilityOfSnow?.toDouble(),
                    thunderstorm = result.nightProbabilityOfSferics?.toDouble()
                )
            ),
            uV = UV(index = result.maxUvIndex?.toDouble())
        )
    }
}

/**
 * Returns hourly forecast
 */
private fun getHourlyForecast(
    hourlyResult: MetOfficeForecast<MetOfficeHourly>
): List<HourlyWrapper> {
    val feature = hourlyResult.features[0] // should only be one feature for this kind of API call
    return feature.properties.timeSeries.map { result ->
        val (weatherText, weatherCode) = convertWeatherCode(result.significantWeatherCode)
            ?: Pair(null, null)
        HourlyWrapper(
            date = result.time,
            weatherText = weatherText,
            weatherCode = weatherCode,
            temperature = Temperature(
                temperature = result.screenTemperature,
                apparentTemperature = result.feelsLikeTemperature,
            ),
            precipitation = Precipitation(
                total = result.totalPrecipAmount,
                snow = result.totalSnowAmount,
            ),
            precipitationProbability = PrecipitationProbability(
                total = result.probOfPrecipitation?.toDouble(),
            ),
            wind = Wind(
                degree = result.windDirectionFrom10m?.toDouble(),
                speed = result.windSpeed10m,
                gusts = result.windGustSpeed10m
            ),
            uV = UV(
                index = result.uvIndex?.toDouble(),
            ),
            relativeHumidity = result.screenRelativeHumidity,
            dewPoint = result.screenDewPointTemperature,
            pressure = result.mslp?.toDouble()?.div(100), // pa -> mb
            visibility = result.visibility?.toDouble()
        )
    }
}

private fun convertWeatherCode(significantWeatherCode: Int?): Pair<String, WeatherCode>? {
    return when (significantWeatherCode) {
        -1 -> Pair("Trace rain", WeatherCode.CLOUDY)
        0 -> Pair("Clear night", WeatherCode.CLEAR)
        1 -> Pair("Sunny day", WeatherCode.CLEAR)
        2, 3 -> Pair("Partly cloudy", WeatherCode.PARTLY_CLOUDY)
        5 -> Pair("Mist", WeatherCode.FOG)
        6 -> Pair("Fog", WeatherCode.FOG)
        7 -> Pair("Cloudy", WeatherCode.CLOUDY)
        8 -> Pair("Overcast", WeatherCode.CLOUDY)
        9, 10 -> Pair("Light rain shower", WeatherCode.RAIN)
        11 -> Pair("Drizzle", WeatherCode.RAIN)
        12 -> Pair("Light rain", WeatherCode.RAIN)
        13, 14 -> Pair("Heavy rain shower", WeatherCode.RAIN)
        15 -> Pair("Heavy rain", WeatherCode.RAIN)
        16, 17 -> Pair("Sleet shower", WeatherCode.SLEET)
        18 -> Pair("Sleet", WeatherCode.SLEET)
        19, 20 -> Pair("Hail shower", WeatherCode.HAIL)
        21 -> Pair("Hail", WeatherCode.HAIL)
        22, 23 -> Pair("Light snow shower", WeatherCode.SNOW)
        24 -> Pair("Light snow", WeatherCode.SNOW)
        25, 26 -> Pair("Heavy snow shower", WeatherCode.SNOW)
        27 -> Pair("Heavy snow", WeatherCode.SNOW)
        28, 29 -> Pair("Thunder shower", WeatherCode.THUNDER)
        30 -> Pair("Thunder", WeatherCode.THUNDER)
        else -> null
    }
}
