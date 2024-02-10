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

package org.breezyweather.sources.nws

import android.graphics.Color
import org.breezyweather.common.basic.models.weather.Alert
import org.breezyweather.common.basic.models.weather.Daily
import org.breezyweather.common.basic.models.weather.PrecipitationProbability
import org.breezyweather.common.basic.models.weather.Temperature
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.common.basic.models.weather.Wind
import org.breezyweather.common.basic.wrappers.HourlyWrapper
import org.breezyweather.common.basic.wrappers.SecondaryWeatherWrapper
import org.breezyweather.common.basic.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.WeatherException
import org.breezyweather.common.extensions.getFormattedDate
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.sources.brightsky.json.BrightSkyAlertsResult
import org.breezyweather.sources.nws.json.NwsAlert
import org.breezyweather.sources.nws.json.NwsAlertsResult
import org.breezyweather.sources.nws.json.NwsGridPointPeriod
import org.breezyweather.sources.nws.json.NwsGridPointResult
import java.util.Locale
import java.util.TimeZone

fun convert(
    forecastResult: NwsGridPointResult,
    alertResult: NwsAlertsResult,
    timeZone: TimeZone
): WeatherWrapper {
    // If the API doesn’t return data, consider data as garbage and keep cached data
    if (forecastResult.properties?.periods.isNullOrEmpty()) {
        throw WeatherException()
    }

    return WeatherWrapper(
        dailyForecast = getDailyForecast(timeZone, forecastResult.properties!!.periods!!),
        hourlyForecast = getHourlyForecast(forecastResult.properties.periods!!),
        alertList = getAlerts(alertResult.features)
    )
}

private fun getDailyForecast(
    timeZone: TimeZone,
    forecastResult: List<NwsGridPointPeriod>
): List<Daily> {
    val dailyList: MutableList<Daily> = ArrayList()
    val hourlyListByDay = forecastResult.groupBy { it.startTime.getFormattedDate(timeZone, "yyyy-MM-dd", Locale.ENGLISH) }
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
    forecastResult: List<NwsGridPointPeriod>
): List<HourlyWrapper> {
    return forecastResult.map { result ->
        HourlyWrapper(
            date = result.startTime,
            weatherCode = getWeatherCode(result.shortForecast),
            weatherText = result.shortForecast,
            temperature = Temperature(
                temperature = result.temperature?.toFloat()
            ),
            precipitationProbability = PrecipitationProbability(
                total = result.probabilityOfPrecipitation?.value
            ),
            wind = Wind(
                degree = getWindDegree(result.windDirection),
                speed = if (!result.windSpeed.isNullOrEmpty() && result.windSpeed.contains(" km/h")) {
                    result.windSpeed.replace(" km/h", "").toDoubleOrNull()?.div(3.6)?.toFloat()
                } else null
            ),
            relativeHumidity = result.relativeHumidity?.value,
            dewPoint = result.dewpoint?.value,
        )
    }
}

fun getAlerts(alerts: List<NwsAlert>?): List<Alert>? {
    if (alerts.isNullOrEmpty()) return null
    return alerts.filter { it.properties != null }.map {
        Alert(
            alertId = it.properties!!.id,
            startDate = it.properties.onset,
            endDate = it.properties.expires,
            description = it.properties.event ?: it.properties.headline ?: "Alert",
            content = it.properties.description + "\n\n" + it.properties.instruction,
            priority = when (it.properties.severity) {
                "Extreme" -> 1
                "Severe" -> 2
                "Moderate" -> 3
                "Minor" -> 4
                else -> 5
            },
            color = getAlertColor(it.properties.event)
        )
    }
}

fun convertSecondary(
    alertsResult: NwsAlertsResult
): SecondaryWeatherWrapper {

    return SecondaryWeatherWrapper(
        alertList = getAlerts(alertsResult.features)
    )
}

/**
 * May need to be completed; we don't have a full list of existing status
 */
private fun getWeatherCode(shortForecast: String?): WeatherCode? {
    if (shortForecast == null) return null
    return with (shortForecast) {
        when {
            equals("Sunny") || equals("Mostly clear") -> WeatherCode.CLEAR
            equals("Partly Sunny") || equals("Partly Cloudy") -> WeatherCode.PARTLY_CLOUDY
            equals("Cloudy") || equals("Mostly Cloudy") -> WeatherCode.CLOUDY
            contains("Thunderstorms") -> WeatherCode.THUNDERSTORM
            contains("Rain And Snow") -> WeatherCode.SLEET
            contains("Snow") -> WeatherCode.SNOW
            contains("Rain") -> WeatherCode.RAIN
            contains("Showers") -> WeatherCode.RAIN
            else -> null
        }
    }
}

private fun getWindDegree(direction: String?): Float? {
    return when (direction) {
        "N" -> 0f
        "NNE" -> 22.5f
        "NE" -> 45f
        "ENE" -> 67.5f
        "E" -> 90f
        "ESE" -> 112.5f
        "SE" -> 135f
        "SSE" -> 157.5f
        "S" -> 180f
        "SSW" -> 202.5f
        "SW" -> 225f
        "WSW" -> 247.5f
        "W" -> 270f
        "WNW" -> 292.5f
        "NW" -> 315f
        "NNW" -> 337.5f
        else -> null
    }
}

/**
 * Based on https://www.weather.gov/help-map
 * Last updated March 24th, 2021
 */
private fun getAlertColor(event: String?): Int? {
    return when(event) {
        "Tsunami Warning" -> Color.rgb(253, 99, 71)
        "Tornado Warning" -> Color.rgb(255, 0, 0)
        "Extreme Wind Warning" -> Color.rgb(255, 140, 0)
        "Severe Thunderstorm Warning" -> Color.rgb(255, 165, 0)
        "Flash Flood Warning" -> Color.rgb(139, 0, 0)
        "Flash Flood Statement" -> Color.rgb(139, 0, 0)
        "Severe Weather Statement" -> Color.rgb(0, 255, 255)
        "Shelter In Place Warning" -> Color.rgb(250, 128, 114)
        "Evacuation Immediate" -> Color.rgb(127, 255, 0)
        "Civil Danger Warning" -> Color.rgb(255, 182, 193)
        "Nuclear Power Plant Warning" -> Color.rgb(75, 0, 130)
        "Radiological Hazard Warning" -> Color.rgb(75, 0, 130)
        "Hazardous Materials Warning" -> Color.rgb(75, 0, 130)
        "Fire Warning" -> Color.rgb(160, 82, 45)
        "Civil Emergency Message" -> Color.rgb(255, 182, 193)
        "Law Enforcement Warning" -> Color.rgb(192, 192, 192)
        "Storm Surge Warning" -> Color.rgb(181, 36, 247)
        "Hurricane Force Wind Warning" -> Color.rgb(205, 92, 92)
        "Hurricane Warning" -> Color.rgb(220, 20, 60)
        "Typhoon Warning" -> Color.rgb(220, 20, 60)
        "Special Marine Warning" -> Color.rgb(255, 165, 0)
        "Blizzard Warning" -> Color.rgb(255, 69, 0)
        "Snow Squall Warning" -> Color.rgb(199, 21, 133)
        "Ice Storm Warning" -> Color.rgb(139, 0, 139)
        "Winter Storm Warning" -> Color.rgb(255, 105, 180)
        "High Wind Warning" -> Color.rgb(218, 165, 32)
        "Tropical Storm Warning" -> Color.rgb(178, 34, 34)
        "Storm Warning" -> Color.rgb(148, 0, 211)
        "Tsunami Advisory" -> Color.rgb(210, 105, 30)
        "Tsunami Watch" -> Color.rgb(255, 0, 255)
        "Avalanche Warning" -> Color.rgb(30, 144, 255)
        "Earthquake Warning" -> Color.rgb(139, 69, 19)
        "Volcano Warning" -> Color.rgb(47, 79, 79)
        "Ashfall Warning" -> Color.rgb(169, 169, 169)
        "Coastal Flood Warning" -> Color.rgb(34, 139, 34)
        "Lakeshore Flood Warning" -> Color.rgb(34, 139, 34)
        "Flood Warning" -> Color.rgb(0, 255, 0)
        "High Surf Warning" -> Color.rgb(34, 139, 34)
        "Dust Storm Warning" -> Color.rgb(255, 228, 196)
        "Blowing Dust Warning" -> Color.rgb(255, 228, 196)
        "Lake Effect Snow Warning" -> Color.rgb(0, 139, 139)
        "Excessive Heat Warning" -> Color.rgb(199, 21, 133)
        "Tornado Watch" -> Color.rgb(255, 255, 0)
        "Severe Thunderstorm Watch" -> Color.rgb(219, 112, 147)
        "Flash Flood Watch" -> Color.rgb(46, 139, 87)
        "Gale Warning" -> Color.rgb(221, 160, 221)
        "Flood Statement" -> Color.rgb(0, 255, 0)
        "Wind Chill Warning" -> Color.rgb(176, 196, 222)
        "Extreme Cold Warning" -> Color.rgb(0, 0, 255)
        "Hard Freeze Warning" -> Color.rgb(148, 0, 211)
        "Freeze Warning" -> Color.rgb(72, 61, 139)
        "Red Flag Warning" -> Color.rgb(255, 20, 147)
        "Storm Surge Watch" -> Color.rgb(219, 127, 247)
        "Hurricane Watch" -> Color.rgb(255, 0, 255)
        "Hurricane Force Wind Watch" -> Color.rgb(153, 50, 204)
        "Typhoon Watch" -> Color.rgb(255, 0, 255)
        "Tropical Storm Watch" -> Color.rgb(240, 128, 128)
        "Storm Watch" -> Color.rgb(255, 228, 181)
        "Hurricane Local Statement" -> Color.rgb(255, 228, 181)
        "Typhoon Local Statement" -> Color.rgb(255, 228, 181)
        "Tropical Storm Local Statement" -> Color.rgb(255, 228, 181)
        "Tropical Depression Local Statement" -> Color.rgb(255, 228, 181)
        "Avalanche Advisory" -> Color.rgb(205, 133, 63)
        "Winter Weather Advisory" -> Color.rgb(123, 104, 238)
        "Wind Chill Advisory" -> Color.rgb(175, 238, 238)
        "Heat Advisory" -> Color.rgb(255, 127, 80)
        "Urban and Small Stream Flood Advisory" -> Color.rgb(0, 255, 127)
        "Small Stream Flood Advisory" -> Color.rgb(0, 255, 127)
        "Arroyo and Small Stream Flood Advisory" -> Color.rgb(0, 255, 127)
        "Flood Advisory" -> Color.rgb(0, 255, 127)
        "Hydrologic Advisory" -> Color.rgb(0, 255, 127)
        "Lakeshore Flood Advisory" -> Color.rgb(124, 252, 0)
        "Coastal Flood Advisory" -> Color.rgb(124, 252, 0)
        "High Surf Advisory" -> Color.rgb(186, 85, 211)
        "Heavy Freezing Spray Warning" -> Color.rgb(0, 191, 255)
        "Dense Fog Advisory" -> Color.rgb(112, 128, 144)
        "Dense Smoke Advisory" -> Color.rgb(240, 230, 140)
        "Small Craft Advisory" -> Color.rgb(216, 191, 216)
        "Brisk Wind Advisory" -> Color.rgb(216, 191, 216)
        "Hazardous Seas Warning" -> Color.rgb(216, 191, 216)
        "Dust Advisory" -> Color.rgb(189, 183, 107)
        "Blowing Dust Advisory" -> Color.rgb(189, 183, 107)
        "Lake Wind Advisory" -> Color.rgb(210, 180, 140)
        "Wind Advisory" -> Color.rgb(210, 180, 140)
        "Frost Advisory" -> Color.rgb(100, 149, 237)
        "Ashfall Advisory" -> Color.rgb(105, 105, 105)
        "Freezing Fog Advisory" -> Color.rgb(0, 128, 128)
        "Freezing Spray Advisory" -> Color.rgb(0, 191, 255)
        "Low Water Advisory" -> Color.rgb(165, 42, 42)
        "Local Area Emergency" -> Color.rgb(192, 192, 192)
        "Avalanche Watch" -> Color.rgb(244, 164, 96)
        "Blizzard Watch" -> Color.rgb(173, 255, 47)
        "Rip Current Statement" -> Color.rgb(64, 224, 208)
        "Beach Hazards Statement" -> Color.rgb(64, 224, 208)
        "Gale Watch" -> Color.rgb(255, 192, 203)
        "Winter Storm Watch" -> Color.rgb(70, 130, 180)
        "Hazardous Seas Watch" -> Color.rgb(72, 61, 139)
        "Heavy Freezing Spray Watch" -> Color.rgb(188, 143, 143)
        "Coastal Flood Watch" -> Color.rgb(102, 205, 170)
        "Lakeshore Flood Watch" -> Color.rgb(102, 205, 170)
        "Flood Watch" -> Color.rgb(46, 139, 87)
        "High Wind Watch" -> Color.rgb(184, 134, 11)
        "Excessive Heat Watch" -> Color.rgb(128, 0, 0)
        "Extreme Cold Watch" -> Color.rgb(0, 0, 255)
        "Wind Chill Watch" -> Color.rgb(95, 158, 160)
        "Lake Effect Snow Watch" -> Color.rgb(135, 206, 250)
        "Hard Freeze Watch" -> Color.rgb(65, 105, 225)
        "Freeze Watch" -> Color.rgb(0, 255, 255)
        "Fire Weather Watch" -> Color.rgb(255, 222, 173)
        "Extreme Fire Danger" -> Color.rgb(233, 150, 122)
        "911 Telephone Outage" -> Color.rgb(192, 192, 192)
        "Coastal Flood Statement" -> Color.rgb(107, 142, 35)
        "Lakeshore Flood Statement" -> Color.rgb(107, 142, 35)
        "Special Weather Statement" -> Color.rgb(255, 228, 181)
        "Marine Weather Statement" -> Color.rgb(255, 239, 213)
        "Air Quality Alert" -> Color.rgb(128, 128, 128)
        "Air Stagnation Advisory" -> Color.rgb(128, 128, 128)
        "Hazardous Weather Outlook" -> Color.rgb(238, 232, 170)
        "Hydrologic Outlook" -> Color.rgb(144, 238, 144)
        "Short Term Forecast" -> Color.rgb(152, 251, 152)
        "Administrative Message" -> Color.rgb(192, 192, 192)
        "Test" -> Color.rgb(240, 255, 255)
        else -> null
    }
}