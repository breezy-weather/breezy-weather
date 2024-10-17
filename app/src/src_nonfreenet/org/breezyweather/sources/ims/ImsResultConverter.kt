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

package org.breezyweather.sources.ims

import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.AlertSeverity
import breezyweather.domain.weather.model.Current
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.sources.ims.json.ImsLocation
import org.breezyweather.sources.ims.json.ImsWeatherData
import org.breezyweather.sources.ims.json.ImsWeatherResult
import java.util.Calendar
import java.util.Date

fun convert(
    location: Location,
    result: ImsLocation
): Location {
    return location.copy(
        timeZone = "Asia/Jerusalem",
        country = "", // We don’t put any country name to avoid political issues
        countryCode = "IL", // but we need to identify the location as being part of the coverage of IMS
        city = result.name
    )
}
fun convert(
    weatherResult: ImsWeatherResult?,
    location: Location
): WeatherWrapper {
    // If the API doesn’t return data, consider data as garbage and keep cached data
    if (weatherResult?.data?.forecastData.isNullOrEmpty()) {
        throw InvalidOrIncompleteDataException()
    }

    return WeatherWrapper(
        dailyForecast = getDailyForecast(location, weatherResult!!.data!!),
        hourlyForecast = getHourlyForecast(location, weatherResult.data!!),
        current = getCurrent(weatherResult.data),
        alertList = getAlerts(weatherResult.data)
    )
}

private fun getDailyForecast(
    location: Location,
    data: ImsWeatherData
): List<Daily> {
    return data.forecastData!!.keys.mapNotNull {
        it.toDateNoHour(location.javaTimeZone)?.let { dayDate ->
            Daily(
                date = dayDate,
                uV = data.forecastData[it]!!.daily?.maximumUVI?.toDoubleOrNull()?.let { uvi ->
                    UV(uvi)
                }
            )
        }
    }
}

private fun getHourlyForecast(
    location: Location,
    data: ImsWeatherData
): List<HourlyWrapper> {
    val hourlyList = mutableListOf<HourlyWrapper>()
    data.forecastData!!.keys.forEach {
        it.toDateNoHour(location.javaTimeZone)?.let { dayDate ->
            data.forecastData[it]!!.hourly?.forEach { hourlyResult ->
                val hourlyDate = dayDate.toCalendarWithTimeZone(location.javaTimeZone).apply {
                    set(Calendar.HOUR_OF_DAY, hourlyResult.value.hour.toInt())
                }.time

                hourlyList.add(
                    HourlyWrapper(
                        date = hourlyDate,
                        // TODO: Weather code + text, map for the next 6 hours where it's null otherwise
                        temperature = Temperature(
                            temperature = hourlyResult.value.preciseTemperature?.toDoubleOrNull(),
                            windChillTemperature = hourlyResult.value.windChill?.toDoubleOrNull()
                        ),
                        precipitationProbability = PrecipitationProbability(
                            total = hourlyResult.value.rainChance?.toDoubleOrNull()
                        ),
                        wind = hourlyResult.value.windSpeed?.let { windSpeed ->
                            Wind(
                                speed = windSpeed.div(3.6),
                                degree = hourlyResult.value.windDirectionId?.let { windDirId ->
                                    data.windDirections?.getOrElse(windDirId) { null }?.direction?.toDoubleOrNull()
                                }
                            )
                        },
                        uV = hourlyResult.value.uvIndex?.toDoubleOrNull()?.let { uvi ->
                            UV(uvi)
                        },
                        relativeHumidity = hourlyResult.value.relativeHumidity?.toDoubleOrNull()
                    )
                )
            }
        }
    }
    return hourlyList
}

fun getCurrent(data: ImsWeatherData?): Current? {
    if (data?.analysis == null) return null

    return Current(
        temperature = data.analysis.temperature?.toDoubleOrNull()?.let {
            Temperature(
                temperature = it,
                apparentTemperature = data.analysis.feelsLike?.toDoubleOrNull(),
                windChillTemperature = data.analysis.windChill?.toDoubleOrNull()
            )
        },
        wind = data.analysis.windSpeed?.let { windSpeed ->
            Wind(
                speed = windSpeed.div(3.6),
                degree = data.analysis.windDirectionId?.let {
                    data.windDirections?.getOrElse(it) { null }?.direction?.toDoubleOrNull()
                }
            )
        },
        uV = data.analysis.uvIndex?.toDoubleOrNull()?.let { UV(it) },
        relativeHumidity = data.analysis.relativeHumidity?.toDoubleOrNull(),
        dewPoint = data.analysis.dewPointTemp?.toDoubleOrNull()
    )
}

fun getAlerts(data: ImsWeatherData): List<Alert>? {
    return data.allWarnings?.mapNotNull { warningEntry ->
        val severity = when (warningEntry.value.severityId) {
            "6" -> AlertSeverity.EXTREME
            "4", "5" -> AlertSeverity.SEVERE
            "2", "3" -> AlertSeverity.MODERATE
            "0", "1" -> AlertSeverity.MINOR
            else -> AlertSeverity.UNKNOWN
        }
        Alert(
            alertId = warningEntry.value.alertId,
            startDate = warningEntry.value.validFromUnix?.let { Date(it.times(1000L)) },
            // TODO: endDate = warningEntry.value.validTo, // TimeZone-dependant
            headline = warningEntry.value.warningTypeId?.let {
                data.warningsMetadata?.imsWarningType?.getOrElse(it) { null }?.name
            },
            description = warningEntry.value.text,
            source = "Israel Meteorological Service",
            severity = severity,
            color = warningEntry.value.severityId?.let { severityId ->
                data.warningsMetadata?.warningSeverity?.getOrElse(severityId) { null }?.color?.let {
                    Color.parseColor(it)
                }
            } ?: Alert.colorFromSeverity(severity)
        )
    }
}

fun convertSecondary(
    weatherResult: ImsWeatherResult?
): SecondaryWeatherWrapper {

    return SecondaryWeatherWrapper(
        current = getCurrent(weatherResult?.data),
        alertList = weatherResult?.data?.let { getAlerts(it) }
    )
}
