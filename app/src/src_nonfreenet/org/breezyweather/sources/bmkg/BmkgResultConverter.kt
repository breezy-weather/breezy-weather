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

package org.breezyweather.sources.bmkg

import android.content.Context
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.AlertSeverity
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import com.google.maps.android.SphericalUtil
import com.google.maps.android.model.LatLng
import org.breezyweather.R
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getIsoFormattedDate
import org.breezyweather.sources.bmkg.json.BmkgCurrentResult
import org.breezyweather.sources.bmkg.json.BmkgForecastResult
import org.breezyweather.sources.bmkg.json.BmkgIbfMessage
import org.breezyweather.sources.bmkg.json.BmkgIbfResult
import org.breezyweather.sources.bmkg.json.BmkgLocationResult
import org.breezyweather.sources.bmkg.json.BmkgPm25Result
import org.breezyweather.sources.bmkg.json.BmkgWarningResult
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

internal fun convert(
    location: Location,
    result: BmkgLocationResult,
): Location {
    // Make sure location is within 10km of a known location in Indonesia
    if (result.distance !== null && result.distance > 10000.0) {
        throw InvalidLocationException()
    }

    return location.copy(
        latitude = location.latitude,
        longitude = location.longitude,
        timeZone = getTimeZone(result.adm1),
        country = "Indonesia",
        countryCode = "ID",
        admin1 = result.provinsi,
        admin1Code = result.adm1,
        admin2 = result.kotkab,
        admin2Code = result.adm2,
        admin3 = result.kecamatan,
        admin3Code = result.adm3,
        admin4 = result.desa,
        admin4Code = result.adm4,
        city = result.kotkab ?: "",
        district = result.kecamatan
    )
}

internal fun getCurrent(
    context: Context,
    currentResult: BmkgCurrentResult,
): CurrentWrapper {
    return CurrentWrapper(
        weatherText = getWeatherText(context, currentResult.data?.cuaca?.weather),
        weatherCode = getWeatherCode(currentResult.data?.cuaca?.weather),
        temperature = Temperature(
            temperature = currentResult.data?.cuaca?.t
        ),
        wind = Wind(
            degree = currentResult.data?.cuaca?.wdDeg,
            speed = currentResult.data?.cuaca?.ws?.div(3.6) // convert km/h to m/s
        ),
        relativeHumidity = currentResult.data?.cuaca?.hu,
        visibility = currentResult.data?.cuaca?.vs
    )
}

internal fun getDailyForecast(
    context: Context,
    location: Location,
    forecastResult: BmkgForecastResult,
): List<DailyWrapper> {
    // CommonConverter.kt does not compute daily for this source
    // without providing at least a empty list filled with dates.
    val hourlyList = getHourlyForecast(context, forecastResult)
    val hourlyListDates = hourlyList.groupBy { it.date.getIsoFormattedDate(location) }.keys
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone(location.timeZone)
    val dailyList = mutableListOf<DailyWrapper>()
    hourlyListDates.forEachIndexed { i, date ->
        if (i < hourlyListDates.size - 1) { // Don't store last index to avoid incomplete day
            dailyList.add(
                DailyWrapper(
                    date = formatter.parse(date)!!
                )
            )
        }
    }
    return dailyList
}

internal fun getHourlyForecast(
    context: Context,
    forecastResult: BmkgForecastResult,
): List<HourlyWrapper> {
    val hourlyList = mutableListOf<HourlyWrapper>()
    forecastResult.data?.forEach { data ->
        data.cuaca?.forEach { cuaca ->
            cuaca.forEach {
                if (it.datetime != null) {
                    hourlyList.add(
                        HourlyWrapper(
                            date = it.datetime,
                            weatherText = getWeatherText(context, it.weather),
                            weatherCode = getWeatherCode(it.weather),
                            temperature = Temperature(
                                temperature = it.t
                            ),
                            precipitation = Precipitation(
                                total = it.tp
                            ),
                            wind = Wind(
                                degree = it.wdDeg,
                                speed = it.ws?.div(3.6) // convert km/h to m/s
                            ),
                            relativeHumidity = it.hu,
                            cloudCover = it.tcc?.toInt(),
                            visibility = it.vs
                        )
                    )
                }
            }
        }
    }
    return hourlyList
}

internal fun getAlertList(
    context: Context,
    warningResult: BmkgWarningResult,
    ibf1Result: BmkgIbfResult,
    ibf2Result: BmkgIbfResult,
    ibf3Result: BmkgIbfResult,
): List<Alert> {
    val source = "Badan Meteorologi, Klimatologi, dan Geofisika"
    var severity: AlertSeverity
    val alertList = mutableListOf<Alert>()
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    // Nowcast warning times are always given in Asia/Jakarta
    // regardless of which time zone the location belongs to.
    formatter.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
    warningResult.data?.today?.let {
        if (it.description != null) {
            alertList.add(
                Alert(
                    alertId = it.description.idKode,
                    startDate = if (it.description.dateStart !== null) {
                        formatter.parse(it.description.dateStart)
                    } else {
                        null
                    },
                    endDate = if (it.description.expired !== null) {
                        formatter.parse(it.description.expired)
                    } else {
                        null
                    },
                    headline = it.description.headline?.trim(),
                    description = it.description.description?.trim(),
                    source = source,
                    color = if (it.tipearea?.contains("Terjadi", ignoreCase = true) == true) {
                        // TODO: Seems to never be "Terjadi" when coming from our app (works on website)
                        // Are lon/lat hardcoded server side??
                        Color.rgb(255, 153, 0) // #ff9900 - Orange
                    } else if (it.tipearea?.contains("Meluas", ignoreCase = true) == true) {
                        Color.rgb(255, 255, 0) // #ffff00 - Yellow
                    } else {
                        Alert.colorFromSeverity(AlertSeverity.UNKNOWN)
                    }
                )
            )
        }
    }

    // Impact Based Forecast
    listOf(ibf1Result, ibf2Result, ibf3Result).forEach { item ->
        item.data?.forEach {
            // IBFs are issued in 10 categories:
            // • 1: minor impact, medium likelihood
            // • 2: minor impact, high likelihood
            // • 3: significant impact, very low likelihood
            // • 4: significant impact, low likelihood
            // • 5: severe impact, very low likelihood
            // • 6: significant impact, medium likelihood
            // • 7: significant impact, high likelihood
            // • 8: severe impact, low likelihood
            // • 9: severe impact, medium likelihood
            // • 10: severe impact, high likelihood
            // Severity is based on this matrix:
            // https://www.bmkg.go.id/cuaca/cuaca-berbasis-dampak.bmkg
            severity = when (it.category) {
                "1", "2", "3", "4", "5" -> AlertSeverity.MODERATE
                "6", "7", "8", "9" -> AlertSeverity.SEVERE
                "10" -> AlertSeverity.EXTREME
                else -> AlertSeverity.UNKNOWN
            }

            // BMKG currently only issues IBFs for "heavy rain" which is hardcoded in its JavaScript.
            // When BMKG starts issuing IBFs for events other than "heavy rain", we'll have to adjust.
            // Currently a proprietary code "in" is used for Indonesian in Breezy Weather.
            // We are future-proofing this with "id" which is the actual ISO 639-1 code.
            val headline = if (context.currentLocale.code.startsWith("in") ||
                context.currentLocale.code.startsWith("id")
            ) {
                "Hujan Lebat (kategory ${it.category})"
            } else {
                "Heavy Rain (category ${it.category})"
            }

            alertList.add(
                Alert(
                    alertId = it.id,
                    startDate = it.validFor,
                    headline = headline,
                    description = getWarningText(context, it.effect),
                    instruction = getWarningText(context, it.response?.public),
                    source = source,
                    severity = severity,
                    color = Alert.colorFromSeverity(severity)
                )
            )
        }
    }
    return alertList
}

internal fun getAirQuality(
    location: Location,
    pm25Result: List<BmkgPm25Result>?,
): AirQuality {
    var pm25: Double? = null
    var distance: Double
    var nearestDistance: Double = Double.POSITIVE_INFINITY
    pm25Result?.forEach {
        if (it.LAT !== null && it.LON !== null && it.PM25 !== null) {
            distance = SphericalUtil.computeDistanceBetween(
                LatLng(location.latitude, location.longitude),
                LatLng(it.LAT, it.LON)
            )
            if (distance < nearestDistance) {
                pm25 = it.PM25
                nearestDistance = distance
            }
        }
    }
    return AirQuality(
        pM25 = pm25
    )
}

private fun getWarningText(
    context: Context,
    messages: List<BmkgIbfMessage>?,
): String {
    var text = ""
    messages?.forEach {
        // Currently a proprietary code "in" is used for Indonesian in Breezy Weather.
        // We are future-proofing this with "id" which is the actual ISO 639-1 code.
        if (context.currentLocale.code.startsWith("in") ||
            context.currentLocale.code.startsWith("id")
        ) {
            if (!it.id.isNullOrEmpty()) {
                text += "• ${it.id.trim()}\n"
            }
        } else {
            if (!it.en.isNullOrEmpty()) {
                text += "• ${it.en.trim()}\n"
            }
        }
    }
    return text.trim()
}

// Source: https://cuaca.bmkg.go.id/_nuxt/B5IL8-NA.js
private fun getWeatherText(
    context: Context,
    weather: Int?,
): String? {
    return when (weather) {
        0, 1 -> context.getString(R.string.common_weather_text_clear_sky)
        2 -> context.getString(R.string.common_weather_text_partly_cloudy)
        3 -> context.getString(R.string.common_weather_text_cloudy)
        4 -> context.getString(R.string.common_weather_text_overcast)
        10 -> context.getString(R.string.weather_kind_haze)
        17 -> context.getString(R.string.weather_kind_thunder)
        45 -> context.getString(R.string.common_weather_text_fog)
        60, 61 -> context.getString(R.string.common_weather_text_rain_light)
        63 -> context.getString(R.string.common_weather_text_rain_moderate)
        65 -> context.getString(R.string.common_weather_text_rain_heavy)
        95, 96, 99 -> context.getString(R.string.weather_kind_thunderstorm)
        else -> null
    }
}

private fun getWeatherCode(
    weather: Int?,
): WeatherCode? {
    return when (weather) {
        0, 1 -> WeatherCode.CLEAR
        2 -> WeatherCode.PARTLY_CLOUDY
        3, 4 -> WeatherCode.CLOUDY
        10 -> WeatherCode.HAZE
        17 -> WeatherCode.THUNDER
        45 -> WeatherCode.FOG
        60, 61, 63, 65 -> WeatherCode.RAIN
        95, 96, 99 -> WeatherCode.THUNDERSTORM
        else -> null
    }
}

// Time zones in Indonesia: https://en.wikipedia.org/wiki/Time_in_Indonesia
// Province codes of Indonesia: https://en.wikipedia.org/wiki/Provinces_of_Indonesia
private fun getTimeZone(
    province: String?,
): String {
    return when (province) {
        "11", // Aceh
        "12", // North Sumatra
        "13", // West Sumatra
        "14", // Riau
        "15", // Jambi
        "16", // South Sumatra
        "17", // Bengkulu
        "18", // Lampung
        "19", // Bangka Belitung Islands
        "21", // Riau Islands
        "31", // Jakarta
        "32", // West Java
        "33", // Central Java
        "34", // Special Region of Yogyakarta
        "35", // East Java
        "36", // Banten
        -> "Asia/Jakarta"

        "61", // West Kalimantan
        "62", // Central Kalimantan
        -> "Asia/Pontianak"

        "51", // Bali
        "52", // West Nusa Tenggara
        "53", // East Nusa Tenggara
        "63", // South Kalimantan
        "64", // East Kalimantan
        "65", // North Kalimantan
        "71", // North Sulawesi
        "72", // Central Sulawesi
        "73", // South Sulawesi
        "74", // Southeast Sulawesi
        "75", // Gorontalo
        "76", // West Sulawesi
        -> "Asia/Makassar"

        "81", // Maluku
        "82", // North Maluku
        "91", // Papua
        "92", // West Papua
        "93", // South Papua
        "94", // Central Papua
        "95", // Highland Papua
        "96", // Southwest Papua
        -> "Asia/Jayapura"

        else -> "Asia/Jakarta"
    }
}
