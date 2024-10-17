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

import android.content.Context
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
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.sources.meteoam.json.MeteoAmForecastResult
import org.breezyweather.sources.meteoam.json.MeteoAmForecastStats
import org.breezyweather.sources.meteoam.json.MeteoAmObservationResult
import org.breezyweather.sources.meteoam.json.MeteoAmReverseLocation
import java.util.Date

fun convert(
    location: Location,
    reverseLocation: MeteoAmReverseLocation,
    timezone: String
): Location {
    return location.copy(
        timeZone = timezone,
        country = reverseLocation.country,
        countryCode = reverseLocation.country_code,
        admin2 = reverseLocation.county,
        city = reverseLocation.city
    )
}

fun convert(
    context: Context,
    forecastResult: MeteoAmForecastResult,
    observationResult: MeteoAmObservationResult
): WeatherWrapper {
    val timeseries = forecastResult.timeseries
    val params = forecastResult.paramlist
    val stats = forecastResult.extrainfo?.stats
    val data = forecastResult.datasets?.data
    val oParams = observationResult.paramlist
    val observation = observationResult.datasets?.getOrElse("0") { null }

    if (timeseries == null || params == null || stats == null || data == null || oParams == null) {
        throw InvalidOrIncompleteDataException()
    }

    return WeatherWrapper(
        current = if (observation != null) {
            getCurrent(context, oParams, observation)
        } else null,
        dailyForecast = getDailyForecast(context, stats),
        hourlyForecast = getHourlyForecast(context, timeseries, params, data)
    )
}

private fun getCurrent(
    context: Context,
    params: List<String>,
    currentResult: Map<String, Map<String, Any?>>
): Current {
    val keys = mutableMapOf<String, String>()
    for (i in params.indices) {
        keys[params[i]] = i.toString()
    }
    val icon = currentResult.getOrElse(keys["icon"].toString()) { null }?.getOrElse("0") { null }.toString()
    val temp = when (currentResult.getOrElse(keys["2t"].toString()) { null }?.getOrElse("0") { null }) {
        is Double -> currentResult.getOrElse(keys["2t"].toString()) { null }?.get("0") as Double
        else -> null // sometimes returned as "-"
    }
    val wdir = when (currentResult.getOrElse(keys["wdir"].toString()) { null }?.getOrElse("0") { null }) {
        is Double -> currentResult.getOrElse(keys["wdir"].toString()) { null }?.get("0") as Double
        "VRB" -> -1.0
        else -> null // sometimes returned as "-"
    }
    val wspd = when (currentResult.getOrElse(keys["wkmh"].toString()) { null }?.getOrElse("0") { null }) {
        is Double -> currentResult.getOrElse(keys["wkmh"].toString()) { null }?.get("0") as Double / 3.6
        else -> null // sometimes returned as "-"
    }
    val rhum = when (currentResult.getOrElse(keys["r"].toString()) { null }?.getOrElse("0") { null }) {
        is Double -> currentResult.getOrElse(keys["r"].toString()) { null }?.get("0") as Double
        else -> null // sometimes returned as "-"
    }
    val pres = when (currentResult.getOrElse(keys["pmsl"].toString()) { null }?.getOrElse("0") { null }) {
        is Double -> currentResult.getOrElse(keys["pmsl"].toString()) { null }?.get("0") as Double
        else -> null // sometimes returned as "-"
    }

    return Current(
        weatherText = getWeatherText(context, icon),
        weatherCode = getWeatherCode(icon),
        temperature = Temperature(
            temperature = temp
        ),
        wind = Wind(
            degree = wdir,
            speed = wspd
        ),
        relativeHumidity = rhum,
        pressure = pres
    )
}

private fun getDailyForecast(
    context: Context,
    dailyResult: List<MeteoAmForecastStats>
): List<Daily> {
    val dailyForecast = mutableListOf<Daily>()
    dailyResult.forEach {
        if (it.icon != "-") {
            dailyForecast.add(
                Daily(
                    date = it.localDate,
                    day = HalfDay(
                        weatherText = getWeatherText(context, it.icon),
                        weatherCode = getWeatherCode(it.icon)
                    ),
                    night = HalfDay(
                        weatherText = getWeatherText(context, it.icon),
                        weatherCode = getWeatherCode(it.icon)
                    )
                )
            )
        }
    }
    return dailyForecast
}

private fun getHourlyForecast(
    context: Context,
    timeseries: List<Date>,
    params: List<String>,
    data: Map<String, Map<String, Any?>>
): List<HourlyWrapper> {
    val hourlyForecast = mutableListOf<HourlyWrapper>()
    val keys = mutableMapOf<String, String>()
    var icon: String
    var temp: Double?
    var tpop: Double?
    var wdir: Double?
    var wspd: Double?
    var rhum: Double?
    var pres: Double?
    for (i in params.indices) {
        keys[params[i]] = i.toString()
    }
    for (i in timeseries.indices) {
        icon = data.getOrElse(keys["icon"].toString()) { null }?.getOrElse(i.toString()) { null }.toString()
        temp = when (data.getOrElse(keys["2t"].toString()) { null }?.getOrElse(i.toString()) { null }) {
            is Double -> data.getOrElse(keys["2t"].toString()) { null }?.get(i.toString()) as Double
            else -> null // in case a non-numerical value is returned
        }
        tpop = when (data.getOrElse(keys["tpp"].toString()) { null }?.getOrElse(i.toString()) { null }) {
            is Double -> data.getOrElse(keys["tpp"].toString()) { null }?.get(i.toString()) as Double
            else -> null // in case a non-numerical value is returned
        }
        wdir = when (data.getOrElse(keys["wdir"].toString()) { null }?.getOrElse(i.toString()) { null }) {
            is Double -> data.getOrElse(keys["wdir"].toString()) { null }?.get(i.toString()) as Double
            "VRB" -> -1.0
            else -> null // in case a non-numerical value is returned
        }
        wspd = when (data.getOrElse(keys["wkmh"].toString()) { null }?.getOrElse(i.toString()) { null }) {
            is Double -> data.getOrElse(keys["wkmh"].toString()) { null }?.get(i.toString()) as Double / 3.6
            else -> null // in case a non-numerical value is returned
        }
        rhum = when (data.getOrElse(keys["r"].toString()) { null }?.getOrElse(i.toString()) { null }) {
            is Double -> data.getOrElse(keys["r"].toString()) { null }?.get(i.toString()) as Double
            else -> null // in case a non-numerical value is returned
        }
        pres = when (data.getOrElse(keys["pmsl"].toString()) { null }?.getOrElse(i.toString()) { null }) {
            is Double -> data.getOrElse(keys["pmsl"].toString()) { null }?.get(i.toString()) as Double
            else -> null // in case a non-numerical value is returned
        }

        hourlyForecast.add(
            HourlyWrapper(
                date = timeseries[i],
                weatherText = getWeatherText(context, icon),
                weatherCode = getWeatherCode(icon),
                temperature = Temperature(
                    temperature = temp
                ),
                precipitationProbability = PrecipitationProbability(
                    total = tpop
                ),
                wind = Wind(
                    degree = wdir,
                    speed = wspd
                ),
                relativeHumidity = rhum,
                pressure = pres
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
        "10" -> WeatherCode.THUNDERSTORM
        "11", "12" -> WeatherCode.SLEET
        "13", "18", "36" -> WeatherCode.HAZE
        "14" -> WeatherCode.FOG
        "15" -> WeatherCode.HAIL
        "16" -> WeatherCode.SNOW
        "17", "19" -> WeatherCode.WIND
        else -> null
    }
}

// Icon definitions can be found at https://www.meteoam.it/it/legenda-simboli
// Icon sources can be found at https://www-static.meteoam.it/maps/img/icon_web_v4/{icon}.svg
// Icons 06 and 07 are duplicates
private fun getWeatherText(context: Context, icon: String?): String? {
    return when (icon) {
        "01", "31" -> context.getString(R.string.meteoam_weather_text_clear_sky)     // "Sereno"
        "02", "03", "32", "33" -> context.getString(R.string.meteoam_weather_text_mainly_cloudy) // "Parzialmente velato" o "Velato"
        "04", "34" -> context.getString(R.string.meteoam_weather_text_partly_cloudy) // "Poco nuvoloso"
        "05", "35" -> context.getString(R.string.meteoam_weather_text_cloudy)        // "Molto nuvoloso"
        "06", "07" -> context.getString(R.string.meteoam_weather_text_overcast)      // "Coperto"
        "08" -> context.getString(R.string.meteoam_weather_text_rain_light)          // "Pioggia debole"
        "09" -> context.getString(R.string.meteoam_weather_text_rain_heavy)          // "Pioggia forte"
        "10" -> context.getString(R.string.meteoam_weather_text_thunderstorm)        // "Temporale"
        "11" -> context.getString(R.string.meteoam_weather_text_rain_snow_mixed)     // "Pioggia mista a neve"
        "12" -> context.getString(R.string.meteoam_weather_text_rain_freezing)       // "Pioggia che gela"
        "13", "36" -> context.getString(R.string.meteoam_weather_text_haze)          // "Foschia"
        "14" -> context.getString(R.string.meteoam_weather_text_fog)                 // "Nebbia"
        "15" -> context.getString(R.string.meteoam_weather_text_hail)                // "Grandine"
        "16" -> context.getString(R.string.meteoam_weather_text_snow)                // "Neve"
        "17" -> context.getString(R.string.meteoam_weather_text_tornado_watersprout) // "Tromba d’aria o marina"
        "18" -> context.getString(R.string.meteoam_weather_text_smoke)               // "Fumo"
        "19" -> context.getString(R.string.meteoam_weather_text_sand_storm)          // "Tempesta di sabbia"
        else -> null
    }
}