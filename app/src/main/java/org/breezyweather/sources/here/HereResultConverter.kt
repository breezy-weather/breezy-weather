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

package org.breezyweather.sources.here

import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.Alert
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
import org.breezyweather.common.basic.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.WeatherException
import org.breezyweather.common.extensions.plus
import org.breezyweather.sources.here.json.HereGeocodingData
import org.breezyweather.sources.here.json.HereWeatherAstronomy
import org.breezyweather.sources.here.json.HereWeatherData
import org.breezyweather.sources.here.json.HereWeatherForecastResult
import org.breezyweather.sources.here.json.HereWeatherNWSAlerts
import java.util.Objects
import java.util.TimeZone
import kotlin.math.roundToInt

/**
 * Converts here.com geocoding result into a list of locations
 */
fun convert(
    location: Location?,
    results: List<HereGeocodingData>
): List<Location> {
    return results.map { item ->
        Location(
            cityId = item.id,
            latitude = location?.latitude ?: item.position.lat,
            longitude = location?.longitude ?: item.position.lng,
            timeZone = TimeZone.getTimeZone(item.timeZone.name),
            country = item.address.countryName,
            countryCode = item.address.countryCode,
            province = item.address.state,
            provinceCode = item.address.stateCode,
            district = item.address.county,
            city = item.address.city,
            weatherSource = "here",
            airQualitySource = location?.airQualitySource,
            pollenSource = location?.pollenSource,
            minutelySource = location?.minutelySource,
            alertSource = location?.alertSource,
            normalsSource = location?.normalsSource
        )
    }
}

/**
 * Converts here.com weather result into a forecast
 */
fun convert(
    hereWeatherForecastResult: HereWeatherForecastResult
): WeatherWrapper {
    if (hereWeatherForecastResult.places.isNullOrEmpty()) {
        throw WeatherException()
    }

    val dailySimpleForecasts = hereWeatherForecastResult.places.firstNotNullOfOrNull {
        it.dailyForecasts?.getOrNull(0)?.forecasts
    }
    val hourlyForecasts = hereWeatherForecastResult.places.firstNotNullOfOrNull {
        it.hourlyForecasts?.getOrNull(0)?.forecasts
    }

    if (dailySimpleForecasts.isNullOrEmpty() || hourlyForecasts.isNullOrEmpty()) {
        throw WeatherException()
    }

    val currentForecast = hereWeatherForecastResult.places.firstNotNullOfOrNull {
        it.observations?.getOrNull(0)
    }
    val astronomyForecasts = hereWeatherForecastResult.places.firstNotNullOfOrNull {
        it.astronomyForecasts?.getOrNull(0)?.forecasts
    }
    val nwsAlerts = hereWeatherForecastResult.places.firstNotNullOfOrNull { it.nwsAlerts }

    return WeatherWrapper(
        /*base = Base(
            publishDate = currentForecast?.time ?: Date()
        ),*/
        current = getCurrentForecast(currentForecast),
        dailyForecast = getDailyForecast(dailySimpleForecasts, astronomyForecasts),
        hourlyForecast = getHourlyForecast(hourlyForecasts),
        alertList = getAlertList(nwsAlerts)
    )
}

/**
 * Returns current forecast
 */
private fun getCurrentForecast(result: HereWeatherData?): Current? {
    if (result == null) return null
    return Current(
        weatherText = result.description,
        weatherCode = getWeatherCode(result.iconId),
        temperature = Temperature(
            temperature = result.temperature,
            apparentTemperature = result.comfort?.toFloat()
        ),
        wind = Wind(
            degree = result.windDirection,
            speed = result.windSpeed?.div(3.6f)
        ),
        uV = UV(index = result.uvIndex?.toFloat()),
        relativeHumidity = result.humidity?.toFloat(),
        dewPoint = result.dewPoint,
        pressure = result.barometerPressure,
        visibility = result.visibility?.times(1000)
    )
}

/**
 * Returns daily forecast
 */
private fun getDailyForecast(
    dailySimpleForecasts: List<HereWeatherData>,
    astroForecasts: List<HereWeatherAstronomy>?
): List<Daily> {
    val dailyList: MutableList<Daily> = ArrayList(dailySimpleForecasts.size)
    for (i in 0 until dailySimpleForecasts.size - 1) { // Skip last day
        val dailyForecast = dailySimpleForecasts[i]
        val astro = astroForecasts?.firstOrNull { astro -> astro.time == dailyForecast.time }

        dailyList.add(
            Daily(
                date = dailyForecast.time,
                day = HalfDay(
                    temperature = Temperature(
                        temperature = if (!dailyForecast.highTemperature.isNullOrEmpty()) {
                            dailyForecast.highTemperature.toFloat()
                        } else null
                    )
                ),
                night = HalfDay(
                    // low temperature is actually from previous night,
                    // so we try to get low temp from next day if available
                    temperature = Temperature(
                        temperature = if (!dailySimpleForecasts.getOrNull(i + 1)?.lowTemperature.isNullOrEmpty()) {
                            dailySimpleForecasts[i + 1].lowTemperature!!.toFloat()
                        } else null
                    )
                ),
                moonPhase = MoonPhase(angle = astro?.moonPhase?.times(360f)?.roundToInt()),
                uV = UV(index = dailyForecast.uvIndex?.toFloat())
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
            date = result.time,
            weatherText = result.description,
            weatherCode = getWeatherCode(result.iconId),
            temperature = Temperature(
                temperature = result.temperature,
                apparentTemperature = result.comfort?.toFloat()
            ),
            precipitation = Precipitation(
                total = result.precipitation1H ?: (result.rainFall + result.snowFall),
                rain = result.rainFall,
                snow = result.snowFall,
            ),
            precipitationProbability = PrecipitationProbability(
                total = result.precipitationProbability?.toFloat()
            ),
            wind = Wind(
                degree = result.windDirection,
                speed = result.windSpeed?.div(3.6f)
            ),
            uV = UV(index = result.uvIndex?.toFloat()),
            relativeHumidity = result.humidity?.toFloat(),
            dewPoint = result.dewPoint,
            pressure = result.barometerPressure,
            visibility = result.visibility?.times(1000)
        )
    }
}

private fun getAlertList(
    nwsAlerts: HereWeatherNWSAlerts?
): List<Alert> {
    val converted = arrayListOf<Alert>()

    val nwsAlertsCombined = (nwsAlerts?.warnings ?: listOf()) + (nwsAlerts?.watches ?: listOf())
    for (alert in nwsAlertsCombined) {
        val description = alert.description ?: ""

        // try to deduplicate
        if (converted.find {
                it.description == description &&
                        it.startDate == alert.validFromTimeLocal &&
                        it.endDate == alert.validUntilTimeLocal
            } != null) {
            continue
        }

        converted.add(
            Alert(
                // Create unique ID from: description, severity, start time
                alertId = Objects.hash(description, alert.severity, alert.validFromTimeLocal?.time).toString(),
                startDate = alert.validFromTimeLocal,
                endDate = alert.validUntilTimeLocal,
                description = description,
                content = alert.message,
                priority = alert.severity ?: 1
            )
        )
    }

    return converted
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