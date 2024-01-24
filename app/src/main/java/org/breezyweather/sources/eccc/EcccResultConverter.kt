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
import org.breezyweather.common.basic.models.Location
import org.breezyweather.common.basic.models.weather.Alert
import org.breezyweather.common.basic.models.weather.Current
import org.breezyweather.common.basic.models.weather.Daily
import org.breezyweather.common.basic.models.weather.HalfDay
import org.breezyweather.common.basic.models.weather.Normals
import org.breezyweather.common.basic.models.weather.PrecipitationProbability
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.common.basic.models.weather.Wind
import org.breezyweather.common.basic.wrappers.HourlyWrapper
import org.breezyweather.common.basic.wrappers.SecondaryWeatherWrapper
import org.breezyweather.common.basic.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.WeatherException
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
import java.util.TimeZone

fun convert(
    location: Location,
    result: EcccResult
): Location {
    return Location(
        latitude = location.latitude,
        longitude = location.longitude,
        timeZone = TimeZone.getDefault(), // Assumed device timezone
        country = "Canada",
        countryCode = "CA",
        province = "",
        provinceCode = "",
        city = result.displayName ?: "",
        weatherSource = "eccc",
        airQualitySource = location.airQualitySource,
        pollenSource = location.pollenSource,
        minutelySource = location.minutelySource,
        alertSource = location.alertSource,
        normalsSource = location.normalsSource
    )
}

/**
 * Converts ECCC result into a forecast
 */
fun convert(
    ecccResult: EcccResult,
    timeZone: TimeZone
): WeatherWrapper {
    // If the API doesn’t return hourly or daily, consider data as garbage and keep cached data
    if (ecccResult.dailyFcst?.daily.isNullOrEmpty()
        || ecccResult.dailyFcst?.dailyIssuedTimeEpoch.isNullOrEmpty()
        || ecccResult.hourlyFcst?.hourly.isNullOrEmpty()) {
        throw WeatherException()
    }

    return WeatherWrapper(
        current = getCurrent(ecccResult.observation),
        dailyForecast = getDailyForecast(timeZone, ecccResult.dailyFcst!!),
        hourlyForecast = getHourlyForecast(ecccResult.hourlyFcst!!.hourly!!),
        alertList = getAlertList(ecccResult.alert?.alerts),
        normals = getNormals(timeZone, ecccResult.dailyFcst.regionalNormals?.metric)
    )
}

/**
 * Returns current weather
 */
private fun getCurrent(result: EcccObservation?): Current? {
    if (result == null) return null
    return Current(
        weatherCode = getWeatherCode(result.iconCode),
        temperature = Temperature(
            temperature = getNonEmptyMetric(result.temperature),
            apparentTemperature = getNonEmptyMetric(result.feelsLike)
        ),
        wind = Wind(
            degree = result.windBearing?.toFloat(),
            speed = getNonEmptyMetric(result.windSpeed)?.div(3.6f),
            gusts = getNonEmptyMetric(result.windGust)?.div(3.6f)
        ),
        relativeHumidity = result.humidity?.toFloatOrNull(),
        dewPoint = getNonEmptyMetric(result.dewpoint),
        pressure = getNonEmptyMetric(result.pressure)?.times(10f),
        visibility = getNonEmptyMetric(result.visibility)?.times(1000f)
    )
}

/**
 * Returns daily forecast
 * The daily list is actually a list of daytime/nighttime periods starting from dailyIssuedTime,
 * making the parsing a bit more complex than other sources
 */
private fun getDailyForecast(
    timeZone: TimeZone,
    dailyResult: EcccDailyFcst
): List<Daily> {
    val dailyFirstDay = dailyResult.dailyIssuedTimeEpoch!!.toLong().times(1000).toDate().toTimezoneNoHour(timeZone)
    val dailyList: MutableList<Daily> = ArrayList()
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

            if ((daytime != null && nighttime != null)
                || (firstDayIsNight && i == 0 && nighttime != null)) {
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
                                    temperature = daytime.temperature?.periodHigh?.toFloat()
                                ),
                                precipitationProbability = PrecipitationProbability(
                                    total = daytime.precip?.toFloatOrNull()
                                )
                            )
                        } else null,
                        night = HalfDay(
                            weatherCode = getWeatherCode(nighttime.iconCode),
                            weatherText = nighttime.summary,
                            weatherPhase = nighttime.text,
                            temperature = Temperature(
                                temperature = nighttime.temperature?.periodLow?.toFloat()
                            ),
                            precipitationProbability = PrecipitationProbability(
                                total = nighttime.precip?.toFloatOrNull()
                            )
                        )
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
                PrecipitationProbability(total = result.precip.toFloatOrNull())
            } else null,
            wind = Wind(
                degree = getWindDegree(result.windDir),
                speed = getNonEmptyMetric(result.windSpeed)?.div(3.6f),
                gusts = getNonEmptyMetric(result.windGust)?.div(3.6f)
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
            description = alert.alertBannerText ?: "",
            content = alert.text,
            priority = when (alert.type) {
                "warning" -> 1
                "watch" -> 2
                "statement" -> 3
                else -> 4
            },
            color = if (!alert.bannerColour.isNullOrEmpty()
                && alert.bannerColour.startsWith("#")) {
                Color.parseColor(alert.bannerColour)
            } else null
        )
    }
}

/**
 * Returns normals
 */
private fun getNormals(timeZone:  TimeZone, normals: EcccRegionalNormalsMetric?): Normals? {
    if (normals?.highTemp == null || normals.lowTemp == null) return null
    val currentMonth = Date().toCalendarWithTimeZone(timeZone)[Calendar.MONTH]
    return Normals(
        month = currentMonth,
        daytimeTemperature = normals.highTemp.toFloat(),
        nighttimeTemperature = normals.lowTemp.toFloat()
    )
}

private fun getNonEmptyMetric(ecccUnit: EcccUnit?): Float? {
    if (ecccUnit == null
        || (ecccUnit.metric.isNullOrEmpty() && ecccUnit.metricUnrounded.isNullOrEmpty())) {
        return null
    }
    return if (!ecccUnit.metricUnrounded.isNullOrEmpty()) {
        ecccUnit.metricUnrounded.toFloatOrNull()
    } else ecccUnit.metric!!.toFloatOrNull()
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

private fun getWindDegree(direction: String?): Float? {
    return when (direction) {
        "N" -> 0f
        "NE" -> 45f
        "E" -> 90f
        "SE" -> 135f
        "S" -> 180f
        "SW", "SO" -> 225f
        "W", "O" -> 270f
        "NW", "NO" -> 315f
        "VR" -> -1f
        else -> null
    }
}

fun convertSecondary(
    ecccResult: EcccResult,
    timeZone: TimeZone
): SecondaryWeatherWrapper {

    return SecondaryWeatherWrapper(
        alertList = getAlertList(ecccResult.alert?.alerts),
        normals = getNormals(timeZone, ecccResult.dailyFcst?.regionalNormals?.metric)
    )
}