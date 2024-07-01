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

package org.breezyweather.sources.meteoam

import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Current
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.HalfDay
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.sources.meteoam.json.MeteoAmForecastData
import org.breezyweather.sources.meteoam.json.MeteoAmForecastResult
import org.breezyweather.sources.meteoam.json.MeteoAmForecastStats
import org.breezyweather.sources.meteoam.json.MeteoAmObservationDatasets
import org.breezyweather.sources.meteoam.json.MeteoAmObservationResult
import org.breezyweather.sources.meteoam.json.MeteoAmReverseLocation
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun convert(
    location: Location,
    reverseLocation: MeteoAmReverseLocation,
    timezone: String
): Location {
    return location.copy(
        timeZone = timezone,
        country = reverseLocation.country!!,
        countryCode = reverseLocation.country_code,
        province = reverseLocation.county,
        city = reverseLocation.city!!
    )
}

fun convert(
    forecastResult: MeteoAmForecastResult,
    observationResult: MeteoAmObservationResult
): WeatherWrapper {
    val timeseries = forecastResult.timeseries
    val params = forecastResult.paramlist
    val stats = forecastResult.extrainfo?.stats
    val data = forecastResult.datasets?.data
    val oParams = observationResult.paramlist
    val observation = observationResult.datasets?.get("0")

    if (timeseries == null || params == null || stats == null || data == null || oParams == null) {
        throw InvalidOrIncompleteDataException()
    }

    if (params.size < 10 || params[0] != "2t" || params[1] != "r" || params[2] != "pmsl" || params[3] != "tpp"
        || params[4] != "wdir" || params[7] != "wkmh" || params[9] != "icon") {
        throw InvalidOrIncompleteDataException()
    }

    if (oParams.size < 9 || oParams[0] != "2t" || oParams[1] != "r" || oParams[2] != "pmsl"
        || oParams[3] != "wdir" || oParams[6] != "wkmh" || oParams[8] != "icon") {
        throw InvalidOrIncompleteDataException()
    }

    return WeatherWrapper(
        current = if (observation != null) getCurrent(observation) else null,
        dailyForecast = getDailyForecast(stats),
        hourlyForecast = getHourlyForecast(timeseries, data)
    )
}

private fun getCurrent(
    currentResult: MeteoAmObservationDatasets
): Current {
    return Current(
        weatherText = getWeatherText(currentResult.icon?.get("0")),
        weatherCode = getWeatherCode(currentResult.icon?.get("0")),
        temperature = Temperature(
            currentResult.temp?.get("0")?.toDouble()
        ),
        wind = Wind(
            degree = when (currentResult.wdir?.get("0")) {
                is Double -> currentResult.wdir["0"] as Double
                "VRB" -> -1.0
                else -> null
            },
            speed = when (currentResult.wkph?.get("0")) {
                is Double -> currentResult.wkph["0"] as Double / 3.6
                else -> null
            }
        ),
        relativeHumidity = currentResult.rhum?.get("0")?.toDouble(),
        pressure = currentResult.pres?.get("0")?.toDouble()
    )
}

private fun getDailyForecast(
    dailyResult: List<MeteoAmForecastStats>
): List<Daily> {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.ITALIAN)
    formatter.timeZone = TimeZone.getTimeZone("Europe/Rome")
    val dailyForecast = mutableListOf<Daily>()
    dailyResult.forEach {
        if (it.icon != "-") {
            dailyForecast.add(
                Daily(
                    date = formatter.parse(it.localDate)!!,
                    day = HalfDay(
                        weatherText = getWeatherText(it.icon),
                        weatherCode = getWeatherCode(it.icon)
                    ),
                    night = HalfDay(
                        weatherText = getWeatherText(it.icon),
                        weatherCode = getWeatherCode(it.icon)
                    )
                )
            )
        }
    }
    return dailyForecast
}

private fun getHourlyForecast(
    timeseries: List<String>,
    data: MeteoAmForecastData
): List<HourlyWrapper> {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.ITALIAN)
    formatter.timeZone = TimeZone.getTimeZone("Europe/Rome")
    val hourlyForecast = mutableListOf<HourlyWrapper>()
    for (i in timeseries.indices) {
        hourlyForecast.add(
            HourlyWrapper(
                date = formatter.parse(timeseries[i])!!,
                weatherText = getWeatherText(data.icon?.get(i.toString())),
                weatherCode = getWeatherCode(data.icon?.get(i.toString())),
                temperature = Temperature(
                    temperature = data.temp?.get(i.toString())?.toDouble()
                ),
                precipitationProbability = PrecipitationProbability(
                    total = data.tpop?.get(i.toString())?.toDouble()
                ),
                wind = Wind(
                    degree = data.wdir?.get(i.toString())?.toDouble(),
                    speed = data.wkph?.get(i.toString())?.toDouble()?.div(3.6)
                ),
                relativeHumidity = data.rhum?.get(i.toString())?.toDouble(),
                pressure = data.pres?.get(i.toString())?.toDouble()
            )
        )
    }
    return hourlyForecast
}

// Icon definitions can be found at https://www.meteoam.it/it/legenda-simboli
// Icon sources can be found at https://www-static.meteoam.it/maps/img/icon_web_v4/{icon}.svg
// Icons 06 and 07 are duplicates
private fun getWeatherCode(icon: String?): WeatherCode? {
    return when (icon) {
        "01", "31" -> WeatherCode.CLEAR
        "02", "03", "04", "32", "33", "34" -> WeatherCode.PARTLY_CLOUDY
        "05", "06", "07", "35" -> WeatherCode.CLOUDY
        "08", "09" -> WeatherCode.RAIN
        "10" -> WeatherCode.THUNDER
        "11", "12" -> WeatherCode.SLEET
        "13", "14", "36" -> WeatherCode.FOG
        "15" -> WeatherCode.HAIL
        "16" -> WeatherCode.SNOW
        "17", "19" -> WeatherCode.WIND
        "18" -> WeatherCode.HAZE
        else -> null
    }
}

// Icon definitions can be found at https://www.meteoam.it/it/legenda-simboli
// Icon sources can be found at https://www-static.meteoam.it/maps/img/icon_web_v4/{icon}.svg
// Icons 06 and 07 are duplicates
private fun getWeatherText(icon: String?): String? {
    return when (icon) {
        "01", "31" -> "Sereno"
        "02", "32" -> "Parzialmente velato"
        "03", "33" -> "Velato"
        "04", "34" -> "Poco nuvoloso"
        "05", "35" -> "Molto nuvoloso"
        "06", "07" -> "Coperto"
        "08" -> "Pioggia debole"
        "09" -> "Piogga forte"
        "10" -> "Temporale"
        "11" -> "Pioggia mista a neve"
        "12" -> "Pioggia che gela"
        "13", "36" -> "Foschia"
        "14" -> "Nebbia"
        "15" -> "Grandine"
        "16" -> "Neve"
        "17" -> "Tromba d’aria o marina"
        "18" -> "Fumo"
        "19" -> "Tempesta di sabbia"
        else -> null
    }
}