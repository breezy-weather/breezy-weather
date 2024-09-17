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

package org.breezyweather.sources.eccc

import android.graphics.Color
import android.os.Build
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.AlertSeverity
import breezyweather.domain.weather.model.Current
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.HalfDay
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toDate
import org.breezyweather.common.extensions.toTimezoneNoHour
import org.breezyweather.sources.eccc.json.EcccAlert
import org.breezyweather.sources.eccc.json.EcccDailyFcst
import org.breezyweather.sources.eccc.json.EcccHourly
import org.breezyweather.sources.eccc.json.EcccObservation
import org.breezyweather.sources.eccc.json.EcccRegionalNormalsMetric
import org.breezyweather.sources.eccc.json.EcccResult
import org.breezyweather.sources.eccc.json.EcccUnit
import java.util.Calendar
import java.util.Date
import java.util.Objects
import kotlin.time.Duration.Companion.seconds

fun convert(
    location: Location,
    result: EcccResult
): Location {
    return location.copy(
        country = "Canada",
        countryCode = "CA",
        admin1 = "",
        admin1Code = "",
        admin2 = "",
        admin2Code = "",
        admin3 = "",
        admin3Code = "",
        admin4 = "",
        admin4Code = "",
        city = result.displayName ?: "",
        // Make sure to update TimeZone, especially useful on current location
        timeZone = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            android.icu.util.TimeZone.getDefault().id
        } else {
            java.util.TimeZone.getDefault().id
        }
    )
}

/**
 * Converts ECCC result into a forecast
 */
fun convert(
    ecccResult: EcccResult,
    location: Location
): WeatherWrapper {
    // If the API doesn’t return hourly or daily, consider data as garbage and keep cached data
    if (ecccResult.dailyFcst?.daily.isNullOrEmpty() ||
        ecccResult.dailyFcst?.dailyIssuedTimeEpoch.isNullOrEmpty() ||
        ecccResult.hourlyFcst?.hourly.isNullOrEmpty()) {
        throw InvalidOrIncompleteDataException()
    }

    return WeatherWrapper(
        current = getCurrent(ecccResult.observation),
        dailyForecast = getDailyForecast(location, ecccResult.dailyFcst!!),
        hourlyForecast = getHourlyForecast(ecccResult.hourlyFcst!!.hourly!!),
        alertList = getAlertList(ecccResult.alert?.alerts),
        normals = getNormals(location, ecccResult.dailyFcst.regionalNormals?.metric)
    )
}

/**
 * Returns current weather
 */
private fun getCurrent(result: EcccObservation?): Current? {
    if (result == null) return null
    return Current(
        weatherCode = getWeatherCode(result.iconCode),
        weatherText = result.condition,
        temperature = Temperature(
            temperature = getNonEmptyMetric(result.temperature),
            apparentTemperature = getNonEmptyMetric(result.feelsLike)
        ),
        wind = Wind(
            degree = result.windBearing?.toDoubleOrNull(),
            speed = getNonEmptyMetric(result.windSpeed)?.div(3.6),
            gusts = getNonEmptyMetric(result.windGust)?.div(3.6)
        ),
        relativeHumidity = result.humidity?.toDoubleOrNull(),
        dewPoint = getNonEmptyMetric(result.dewpoint),
        pressure = getNonEmptyMetric(result.pressure)?.times(10),
        visibility = getNonEmptyMetric(result.visibility)?.times(1000)
    )
}

/**
 * Returns daily forecast
 * The daily list is actually a list of daytime/nighttime periods starting from dailyIssuedTime,
 * making the parsing a bit more complex than other sources
 */
private fun getDailyForecast(
    location: Location,
    dailyResult: EcccDailyFcst
): List<Daily> {
    val dailyFirstDay = dailyResult.dailyIssuedTimeEpoch!!.toLong().seconds.inWholeMilliseconds.toDate().toTimezoneNoHour(location.javaTimeZone)
    val dailyList = mutableListOf<Daily>()
    if (dailyFirstDay != null) {
        val firstDayIsNight = dailyResult.daily!![0].temperature?.periodLow != null
        for (i in 0 until 6) {
            val daytime = if (!firstDayIsNight) {
                dailyResult.daily.getOrNull(i * 2)
            } else {
                if (i != 0) {
                    dailyResult.daily.getOrNull((i * 2) - 1)
                } else null
            }
            val nighttime = if (!firstDayIsNight) {
                dailyResult.daily.getOrNull((i * 2) + 1)
            } else dailyResult.daily.getOrNull(i * 2)

            if ((daytime != null && nighttime != null) ||
                (firstDayIsNight && i == 0 && nighttime != null)) {
                val currentDay = if (i != 0) {
                    val cal = Calendar.getInstance()
                    cal.setTime(dailyFirstDay)
                    cal.add(Calendar.DAY_OF_YEAR, i)
                    cal.time
                } else dailyFirstDay

                dailyList.add(
                    Daily(
                        date = currentDay,
                        day = if (daytime != null) {
                            HalfDay(
                                weatherCode = getWeatherCode(daytime.iconCode),
                                weatherText = daytime.summary,
                                weatherPhase = daytime.text,
                                temperature = Temperature(
                                    temperature = daytime.temperature?.periodHigh?.toDouble()
                                ),
                                precipitationProbability = PrecipitationProbability(
                                    total = daytime.precip?.toDoubleOrNull()
                                )
                            )
                        } else null,
                        night = HalfDay(
                            weatherCode = getWeatherCode(nighttime.iconCode),
                            weatherText = nighttime.summary,
                            weatherPhase = nighttime.text,
                            temperature = Temperature(
                                temperature = nighttime.temperature?.periodLow?.toDouble()
                            ),
                            precipitationProbability = PrecipitationProbability(
                                total = nighttime.precip?.toDoubleOrNull()
                            )
                        ),
                        sunshineDuration = daytime?.sun?.value?.toDoubleOrNull()
                    )
                )
            }
        }
    }
    return dailyList
}

/**
 * Returns hourly forecast
 */
private fun getHourlyForecast(
    hourlyResult: List<EcccHourly>
): List<HourlyWrapper> {
    return hourlyResult.map { result ->
        HourlyWrapper(
            date = result.epochTime.times(1000L).toDate(),
            weatherText = result.condition,
            weatherCode = getWeatherCode(result.iconCode),
            temperature = Temperature(
                temperature = getNonEmptyMetric(result.temperature),
                apparentTemperature = getNonEmptyMetric(result.feelsLike)
            ),
            precipitationProbability = if (!result.precip.isNullOrEmpty()) {
                PrecipitationProbability(total = result.precip.toDoubleOrNull())
            } else null,
            wind = Wind(
                degree = getWindDegree(result.windDir),
                speed = getNonEmptyMetric(result.windSpeed)?.div(3.6),
                gusts = getNonEmptyMetric(result.windGust)?.div(3.6)
            )
        )
    }
}

/**
 * Returns alerts
 */
private fun getAlertList(alertList: List<EcccAlert>?): List<Alert>? {
    if (alertList.isNullOrEmpty()) return null
    return alertList.map { alert ->
        Alert(
            alertId = alert.alertId ?: Objects.hash(alert.alertBannerText, alert.issueTime).toString(),
            startDate = alert.issueTime,
            endDate = alert.expiryTime,
            headline = alert.alertBannerText,
            description = alert.text,
            source = alert.specialText?.firstOrNull { it.type == "email" }?.link,
            severity = when (alert.type) {
                "warning" -> AlertSeverity.SEVERE
                "watch" -> AlertSeverity.MODERATE
                "statement" -> AlertSeverity.MINOR
                else -> AlertSeverity.UNKNOWN
            },
            color = if (!alert.bannerColour.isNullOrEmpty() &&
                alert.bannerColour.startsWith("#")) {
                Color.parseColor(alert.bannerColour)
            } else null
        )
    }
}

/**
 * Returns normals
 */
private fun getNormals(location: Location, normals: EcccRegionalNormalsMetric?): Normals? {
    if (normals?.highTemp == null || normals.lowTemp == null) return null
    val currentMonth = Date().toCalendarWithTimeZone(location.javaTimeZone)[Calendar.MONTH]
    return Normals(
        month = currentMonth,
        daytimeTemperature = normals.highTemp.toDouble(),
        nighttimeTemperature = normals.lowTemp.toDouble()
    )
}

private fun getNonEmptyMetric(ecccUnit: EcccUnit?): Double? {
    if (ecccUnit == null ||
        (ecccUnit.metric.isNullOrEmpty() && ecccUnit.metricUnrounded.isNullOrEmpty())) {
        return null
    }
    return if (!ecccUnit.metricUnrounded.isNullOrEmpty()) {
        ecccUnit.metricUnrounded.toDoubleOrNull()
    } else ecccUnit.metric!!.toDoubleOrNull()
}

private fun getWeatherCode(icon: String?): WeatherCode? {
    return when (icon) {
        "00", "01", "30", "31" -> WeatherCode.CLEAR
        "02", "03", "04", "05", "22", "32", "33", "34", "35" -> WeatherCode.PARTLY_CLOUDY
        "10", "20", "21" -> WeatherCode.CLOUDY
        "06", "11", "12", "13", "28", "36" -> WeatherCode.RAIN
        "14", "27" -> WeatherCode.HAIL
        "07", "15", "37" -> WeatherCode.SLEET
        "08", "16", "17", "18", "26", "38" -> WeatherCode.SNOW
        "09", "19", "39", "46", "47" -> WeatherCode.THUNDERSTORM
        "23", "44", "45" -> WeatherCode.HAZE
        "24" -> WeatherCode.FOG
        "25", "40", "41", "42", "43", "48" -> WeatherCode.WIND
        else -> null
    }
}

private fun getWindDegree(direction: String?): Double? {
    return when (direction) {
        "N" -> 0.0
        "NE" -> 45.0
        "E" -> 90.0
        "SE" -> 135.0
        "S" -> 180.0
        "SW", "SO" -> 225.0
        "W", "O" -> 270.0
        "NW", "NO" -> 315.0
        "VR" -> -1.0
        else -> null
    }
}

fun convertSecondary(
    ecccResult: EcccResult,
    location: Location
): SecondaryWeatherWrapper {

    return SecondaryWeatherWrapper(
        alertList = getAlertList(ecccResult.alert?.alerts),
        normals = getNormals(location, ecccResult.dailyFcst?.regionalNormals?.metric)
    )
}
