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

package org.breezyweather.sources.qweather

import android.graphics.Color
import androidx.annotation.ColorInt
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.AlertSeverity
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import org.breezyweather.sources.qweather.json.QWeatherAirResult
import org.breezyweather.sources.qweather.json.QWeatherLocation
import org.breezyweather.sources.qweather.json.QWeatherMinuteResult
import org.breezyweather.sources.qweather.json.QWeatherWarningResult

fun convertLocation(
    location: Location?, // Null if location search, current location if reverse geocoding
    result: QWeatherLocation
): Location {
    return (location ?: Location())
        .copy(
            cityId = result.id,
            latitude = location?.latitude ?: result.lat!!.toDouble(),
            longitude = location?.longitude ?: result.lon!!.toDouble(),
            timeZone = result.tz!!,
            country = result.country!!,
            countryCode = "CN",
            admin1 = result.adm1,
            admin2 = result.adm2,
            city = result.name!!
        )
}

fun convertSecondary(
    airResult: QWeatherAirResult,
    warningResult: QWeatherWarningResult,
    minutelyResult: QWeatherMinuteResult
): SecondaryWeatherWrapper {
    return SecondaryWeatherWrapper(
        airQuality = airResult.now?.let {
            AirQualityWrapper(
                current = AirQuality(
                    pM25 = it.pm2p5?.toDoubleOrNull(),
                    pM10 = it.pm10?.toDoubleOrNull(),
                    sO2 = it.so2?.toDoubleOrNull(),
                    nO2 = it.no2?.toDoubleOrNull(),
                    o3 = it.o3?.toDoubleOrNull(),
                    cO = it.co?.toDoubleOrNull()
                )
            )
        },
        alertList = getWarningList(warningResult),
        minutelyForecast = getMinutelyList(minutelyResult),
    )
}

private fun getMinutelyList(
    minutelyResult: QWeatherMinuteResult
): List<Minutely> {
    if (minutelyResult.minutely.isNullOrEmpty()) return emptyList()
    val minutelyList: MutableList<Minutely> = ArrayList(minutelyResult.minutely.size)

    minutelyResult.minutely.forEachIndexed { _, precipitation ->
        minutelyList.add(
            Minutely(
                date = precipitation.fxTime,
                minuteInterval = 5,
                precipitationIntensity = precipitation.precip?.toDouble()?.times(12) // mm/min -> mm/h
            )
        )
    }
    return minutelyList
}

private fun getWarningList(result: QWeatherWarningResult): List<Alert> {
    if (result.warning.isNullOrEmpty()) return emptyList()
    return result.warning.map { warning ->
        Alert(
            alertId = warning.id ?: System.currentTimeMillis().toString(),
            startDate = warning.pubTime,
            headline = warning.title,
            description = warning.text,
            severity = getWarningSeverity(warning.severity),
            color = getWarningColor(warning.severityColor)
        )
    }.sortedWith(compareByDescending<Alert> { it.severity.id }.thenByDescending(Alert::startDate))
}

private fun getWarningSeverity(severity: String?): AlertSeverity {
    if (severity.isNullOrEmpty()) return AlertSeverity.UNKNOWN
    return when (severity) {
        "Cancel", "None", "Unknown", "Standard", "Minor" -> AlertSeverity.MINOR
        "Moderate" -> AlertSeverity.MODERATE
        "Major", "Severe" -> AlertSeverity.SEVERE
        "Extreme" -> AlertSeverity.EXTREME
        else -> AlertSeverity.UNKNOWN
    }
}

@ColorInt
private fun getWarningColor(severityColor: String?): Int? {
    if (severityColor.isNullOrEmpty()) return null
    return when (severityColor) {
        "White" -> Color.rgb(200, 200, 200)
        "Blue" -> Color.rgb(66, 151, 231)
        "Yellow" -> Color.rgb(255, 242, 184)
        "Orange" -> Color.rgb(255, 145, 0)
        "Red" -> Color.rgb(255, 86, 86)
        "Black" -> Color.rgb(0, 0, 0)
        else -> null
    }
}

fun getWeatherText(icon: String?): String? {
    return if (icon.isNullOrEmpty()) {
        null
    } else when (icon) {
        "100" -> "Sunny"
        "101" -> "Cloudy"
        "102" -> "Few Clouds"
        "103" -> "Partly Cloudy"
        "104" -> "Overcast"
        "150" -> "Clear"
        "151" -> "Cloudy"
        "152" -> "Few Clouds"
        "153" -> "Partly Cloudy"
        "300" -> "Shower"
        "301" -> "Heavy Shower"
        "302" -> "Thundershower"
        "303" -> "Heavy Thunderstorm"
        "304" -> "Hail"
        "305" -> "Light Rain"
        "306" -> "Moderate Rain"
        "307" -> "Heavy Rain"
        "308" -> "Extreme Rain"
        "309" -> "Drizzle Rain"
        "310" -> "Rainstorm"
        "311" -> "Heavy Rainstorm"
        "312" -> "Severe Rainstorm"
        "313" -> "Freezing Rain"
        "314" -> "Light to Moderate Rain"
        "315" -> "Moderate to Heavy Rain"
        "316" -> "Heavy Rain to Rainstorm"
        "317" -> "Rainstorm to Heavy Rainstorm"
        "318" -> "Heavy to Severe Rainstorm"
        "350" -> "Shower"
        "351" -> "Heavy Shower"
        "399" -> "Rain"
        "400" -> "Light Snow"
        "401" -> "Moderate Snow"
        "402" -> "Heavy Snow"
        "403" -> "Snowstorm"
        "404" -> "Sleet"
        "405" -> "Rain and Snow"
        "406" -> "Shower Rain and Snow"
        "407" -> "Snow Flurry"
        "408" -> "Light to Moderate Snow"
        "409" -> "Moderate to Heavy Snow"
        "410" -> "Heavy Snow to Snowstorm"
        "456" -> "Shower Rain and Snow"
        "457" -> "Snow Flurry"
        "499" -> "Snow"
        "500" -> "Mist"
        "501" -> "Fog"
        "502" -> "Haze"
        "503" -> "Sand"
        "504" -> "Dust"
        "507" -> "Sandstorm"
        "508" -> "Severe Sandstorm"
        "509" -> "Dense Fog"
        "510" -> "Strong Fog"
        "511" -> "Moderate Haze"
        "512" -> "Heavy Haze"
        "513" -> "Severe Haze"
        "514" -> "Heavy Fog"
        "515" -> "Extra Heavy Fog"
        "900" -> "Hot"
        "901" -> "Cold"
        "999" -> "Unknown"
        else -> "Unknown"
    }
}

fun getWeatherCode(icon: String?): WeatherCode? {
    return if (icon.isNullOrEmpty()) {
        null
    } else when (icon) {
        "100" -> WeatherCode.CLEAR
        "101" -> WeatherCode.PARTLY_CLOUDY
        "102" -> WeatherCode.PARTLY_CLOUDY
        "103" -> WeatherCode.PARTLY_CLOUDY
        "104" -> WeatherCode.CLOUDY
        "150" -> WeatherCode.CLEAR
        "151" -> WeatherCode.PARTLY_CLOUDY
        "152" -> WeatherCode.PARTLY_CLOUDY
        "153" -> WeatherCode.PARTLY_CLOUDY
        "300" -> WeatherCode.RAIN
        "301" -> WeatherCode.RAIN
        "302" -> WeatherCode.THUNDERSTORM
        "303" -> WeatherCode.THUNDERSTORM
        "304" -> WeatherCode.HAIL
        "305" -> WeatherCode.RAIN
        "306" -> WeatherCode.RAIN
        "307" -> WeatherCode.RAIN
        "308" -> WeatherCode.RAIN
        "309" -> WeatherCode.RAIN
        "310" -> WeatherCode.RAIN
        "311" -> WeatherCode.RAIN
        "312" -> WeatherCode.RAIN
        "313" -> WeatherCode.SLEET
        "314" -> WeatherCode.RAIN
        "315" -> WeatherCode.RAIN
        "316" -> WeatherCode.RAIN
        "317" -> WeatherCode.RAIN
        "318" -> WeatherCode.RAIN
        "350" -> WeatherCode.RAIN
        "351" -> WeatherCode.RAIN
        "399" -> WeatherCode.RAIN
        "400" -> WeatherCode.SNOW
        "401" -> WeatherCode.SNOW
        "402" -> WeatherCode.SNOW
        "403" -> WeatherCode.SNOW
        "404" -> WeatherCode.SLEET
        "405" -> WeatherCode.SLEET
        "406" -> WeatherCode.SLEET
        "407" -> WeatherCode.SNOW
        "408" -> WeatherCode.SNOW
        "409" -> WeatherCode.SNOW
        "410" -> WeatherCode.SNOW
        "456" -> WeatherCode.SLEET
        "457" -> WeatherCode.SNOW
        "499" -> WeatherCode.SNOW
        "500" -> WeatherCode.FOG
        "501" -> WeatherCode.FOG
        "502" -> WeatherCode.HAZE
        "503" -> WeatherCode.WIND
        "504" -> WeatherCode.WIND
        "507" -> WeatherCode.WIND
        "508" -> WeatherCode.WIND
        "509" -> WeatherCode.FOG
        "510" -> WeatherCode.FOG
        "511" -> WeatherCode.HAZE
        "512" -> WeatherCode.HAZE
        "513" -> WeatherCode.HAZE
        "514" -> WeatherCode.FOG
        "515" -> WeatherCode.FOG
        "900" -> WeatherCode.CLEAR
        "901" -> WeatherCode.CLOUDY
        "999" -> WeatherCode.CLOUDY
        else -> WeatherCode.CLOUDY
    }
}