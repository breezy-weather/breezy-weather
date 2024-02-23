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

package org.breezyweather.sources.dmi

import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.WeatherException
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.sources.dmi.json.DmiResult
import org.breezyweather.sources.dmi.json.DmiTimeserie
import org.breezyweather.sources.dmi.json.DmiWarning
import org.breezyweather.sources.dmi.json.DmiWarningResult
import java.util.Locale
import java.util.Objects
import java.util.TimeZone

fun convert(
    location: Location,
    result: DmiResult
): Location {
    return Location(
        cityId = result.id,
        latitude = location.latitude,
        longitude = location.longitude,
        timeZone = TimeZone.getTimeZone(result.timezone),
        country = result.country ?: location.country,
        countryCode = result.country,
        province = "",
        provinceCode = "",
        city = result.city ?: "",
        weatherSource = "dmi",
        airQualitySource = location.airQualitySource,
        pollenSource = location.pollenSource,
        minutelySource = location.minutelySource,
        alertSource = location.alertSource,
        normalsSource = location.normalsSource
    )
}

/**
 * Converts DMI result into a forecast
 */
fun convert(
    dmiResult: DmiResult,
    dmiWarningResult: DmiWarningResult,
    timeZone: TimeZone
): WeatherWrapper {
    // If the API doesn’t return timeseries, consider data as garbage and keep cached data
    if (dmiResult.timeserie.isNullOrEmpty()) {
        throw WeatherException()
    }

    return WeatherWrapper(
        dailyForecast = getDailyForecast(timeZone, dmiResult.timeserie),
        hourlyForecast = getHourlyForecast(dmiResult.timeserie),
        alertList = getAlertList(dmiWarningResult.locationWarnings)
    )
}

/**
 * Returns empty daily forecast
 * Will be completed with hourly forecast data later
 */
private fun getDailyForecast(
    timeZone: TimeZone,
    dailyResult: List<DmiTimeserie>
): List<Daily> {
    val dailyList: MutableList<Daily> = ArrayList()
    val hourlyListByDay = dailyResult.groupBy { it.localTimeIso.getFormattedDate(timeZone, "yyyy-MM-dd", Locale.ENGLISH) }
    for (i in 0 until hourlyListByDay.entries.size - 1) {
        val dayDate = hourlyListByDay.keys.toTypedArray()[i].toDateNoHour(timeZone)
        if (dayDate != null) {
            dailyList.add(
                Daily(
                    date = dayDate
                )
            )
        }
    }
    return dailyList
}

/**
 * Returns hourly forecast
 */
private fun getHourlyForecast(
    hourlyResult: List<DmiTimeserie>
): List<HourlyWrapper> {
    return hourlyResult.map { result ->
        HourlyWrapper(
            // TODO: Check units
            date = result.localTimeIso,
            weatherCode = getWeatherCode(result.symbol),
            temperature = Temperature(
                temperature = result.temp
            ),
            precipitation = if (result.precip != null) {
                Precipitation(total = result.precip)
            } else null,
            wind = Wind(
                degree = result.windDegree,
                speed = result.windSpeed,
                gusts = result.windGust
            ),
            relativeHumidity = result.humidity,
            pressure = result.pressure,
            visibility = result.visibility
        )
    }
}

private fun getAlertList(
    resultList: List<DmiWarning>?
): List<Alert>? {
    if (resultList.isNullOrEmpty()) return null
    return resultList.map {
        Alert(
            alertId = Objects.hash(it.warningTitle, it.validFrom).toString(),
            startDate = it.validFrom,
            endDate = it.validTo,
            description = it.warningTitle ?: "Varsel",
            content = it.warningText + "\n\n" + it.additionalText,
            priority = 5, // TODO
        )
    }
}

fun convertSecondary(
    dmiWarningResult: DmiWarningResult,
): SecondaryWeatherWrapper {

    return SecondaryWeatherWrapper(
        alertList = getAlertList(dmiWarningResult.locationWarnings)
    )
}

private fun getWeatherCode(icon: Int?): WeatherCode? {
    return when (icon) {
        1, 101 -> WeatherCode.CLEAR
        2, 102 -> WeatherCode.PARTLY_CLOUDY
        3, 103 -> WeatherCode.CLOUDY
        60, 63, 80, 160, 163, 180, 181 -> WeatherCode.RAIN
        168, 169, 183, 184 -> WeatherCode.SLEET
        70, 138, 170, 173, 185, 186 -> WeatherCode.SNOW
        195 -> WeatherCode.THUNDERSTORM
        145 -> WeatherCode.FOG
        else -> null
    }
}
