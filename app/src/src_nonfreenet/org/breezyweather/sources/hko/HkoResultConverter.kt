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

package org.breezyweather.sources.hko

import android.content.Context
import android.graphics.Color
import android.util.Log
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.AlertSeverity
import breezyweather.domain.weather.model.Astro
import breezyweather.domain.weather.model.Current
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.HalfDay
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import org.breezyweather.R
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.sources.hko.json.HkoAstroResult
import org.breezyweather.sources.hko.json.HkoCurrentRegionalWeather
import org.breezyweather.sources.hko.json.HkoCurrentResult
import org.breezyweather.sources.hko.json.HkoDailyForecast
import org.breezyweather.sources.hko.json.HkoForecastResult
import org.breezyweather.sources.hko.json.HkoHourlyWeatherForecast
import org.breezyweather.sources.hko.json.HkoNormalsResult
import org.breezyweather.sources.hko.json.HkoOneJsonResult
import org.breezyweather.sources.hko.json.HkoWarningResult
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun convert(
    context: Context,
    currentResult: HkoCurrentResult,
    forecastResult: HkoForecastResult,
    normalsResult: HkoNormalsResult,
    oneJsonResult: HkoOneJsonResult,
    warningDetailsResult: MutableMap<String, HkoWarningResult>,
    sun1Result: HkoAstroResult,
    sun2Result: HkoAstroResult,
    moon1Result: HkoAstroResult,
    moon2Result: HkoAstroResult
): WeatherWrapper {
    return WeatherWrapper(
        current = getCurrent(context, currentResult.RegionalWeather, oneJsonResult),
        normals = getNormals(normalsResult),
        dailyForecast = getDailyForecast(context, forecastResult.DailyForecast, sun1Result, sun2Result, moon1Result, moon2Result, oneJsonResult),
        hourlyForecast = getHourlyForecast(context, forecastResult.HourlyWeatherForecast, oneJsonResult),
        alertList = getAlertList(context, warningDetailsResult)
    )
}

fun convertSecondary(
    context: Context,
    currentResult: HkoCurrentResult?,
    normalsResult: HkoNormalsResult?,
    oneJsonResult: HkoOneJsonResult?,
    warningDetailsResult: MutableMap<String, HkoWarningResult>?
): SecondaryWeatherWrapper {
    return SecondaryWeatherWrapper(
        current = if (currentResult != null && oneJsonResult !== null) {
            getCurrent(context, currentResult.RegionalWeather, oneJsonResult)
        } else null,
        normals = if (normalsResult !== null) {
            getNormals(normalsResult)
        } else null,
        alertList = if (warningDetailsResult !== null) {
            getAlertList(context, warningDetailsResult)
        } else null
    )
}

private fun getCurrent(
    context: Context,
    regionalWeather: HkoCurrentRegionalWeather?,
    oneJson: HkoOneJsonResult
): Current {
    return Current(
        weatherText = getWeatherText(context, oneJson.FLW?.Icon1?.toIntOrNull()),
        weatherCode = getWeatherCode(oneJson.FLW?.Icon1?.toIntOrNull()),
        temperature = Temperature(
            temperature = regionalWeather?.Temp?.Value?.toDoubleOrNull()
        ),
        wind = Wind(
            degree = when (regionalWeather?.Wind?.WindDirectionCode) {
                "N" -> 0.0
                "NNE" -> 22.5
                "NE" -> 45.0
                "ENE" -> 67.5
                "E" -> 90.0
                "ESE" -> 112.5
                "SE" -> 135.0
                "SSE" -> 157.5
                "S" -> 180.0
                "SSW" -> 202.5
                "SW" -> 225.0
                "WSW" -> 247.5
                "W" -> 270.0
                "WNW" -> 292.5
                "NW" -> 315.0
                "NNW" -> 337.5
                else -> null
            },
            speed = regionalWeather?.Wind?.WindSpeed?.toDoubleOrNull()?.div(3.6), // convert km/h to m/s
            gusts = regionalWeather?.Wind?.Gust?.toDoubleOrNull()?.div(3.6) // convert km/h to m/s
        ),
        uV = UV(
            index = oneJson.RHRREAD?.UVIndex?.toDoubleOrNull()
        ),
        relativeHumidity = regionalWeather?.RH?.Value?.toDoubleOrNull(),
        pressure = regionalWeather?.Pressure?.Value?.toDoubleOrNull(),
        dailyForecast = oneJson.F9D?.WeatherForecast?.getOrElse(0) {null}?.ForecastWeather,
    )
}

private fun getNormals(
    normalsResult: HkoNormalsResult
): Normals {
    val now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Hong_Kong"), Locale.ENGLISH)
    val maxTemps = mutableListOf<Double>()
    val minTemps = mutableListOf<Double>()
    val month = now.get(Calendar.MONTH) + 1
    var value: Double?
    normalsResult.stn?.data?.forEach {
        if (it.code == "MEAN_MAX") {
            it.monData?.forEach { mon ->
                value = mon?.getOrElse(month) {null}?.toDoubleOrNull()
                if (value != null) {
                    maxTemps.add(value!!)
                }
            }
        }
        if (it.code == "MEAN_MIN") {
            it.monData?.forEach { mon ->
                value = mon?.getOrElse(month) {null}?.toDoubleOrNull()
                if (value != null) {
                    minTemps.add(value!!)
                }
            }
        }
    }
    // TODO: Limit the list to only the most recent 30 years?
    // TODO: Include values like " 25.4#" (incomplete months)? - need to trim space and hash sign

    return Normals(
        month = month,
        daytimeTemperature = if (maxTemps.isNotEmpty()) {
            maxTemps.average()
        } else null,
        nighttimeTemperature = if (minTemps.isNotEmpty()) {
            minTemps.average()
        } else null
    )
}

private fun getDailyForecast(
    context: Context,
    dailyForecast: List<HkoDailyForecast>?,
    sun1: HkoAstroResult,
    sun2: HkoAstroResult,
    moon1: HkoAstroResult,
    moon2: HkoAstroResult,
    oneJson: HkoOneJsonResult
): List<Daily> {
    val formatter = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Asia/Hong_Kong")

    val dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
    dateTimeFormatter.timeZone = TimeZone.getTimeZone("Asia/Hong_Kong")
    val dateRegex = Regex("""^\d{4}-\d{2}-\d{2}$""")
    val timeRegex = Regex("""^\d{2}:\d{2}$""")
    val iconRegex = Regex("""^pic\d{2}\.png$""")
    val oneJsonDailyWeather = mutableMapOf<String, Int>()

    // City-wide forecast in case the grid forecast fails to return forecast weather conditions
    oneJson.F9D?.WeatherForecast?.forEach {
        if (it.ForecastDate !== null && it.ForecastIcon !== null && iconRegex.matches(it.ForecastIcon))
            oneJsonDailyWeather[it.ForecastDate] = it.ForecastIcon.substring(3, 5).toInt()
    }

    val dailyList = mutableListOf<Daily>()
    var daytimeWeather: Int? = null
    var nightTimeWeather: Int? = null

    val sunMap = mutableMapOf<String, Astro>()
    val moonMap = mutableMapOf<String, Astro>()
    var key: String
    var value: Astro

    listOf(sun1, sun2).forEach { sun ->
        sun.data?.forEach {
            if (it.size == 4) {
                if (dateRegex.matches(it[0])) {
                    key = it[0].replace("-", "")
                    value = Astro(
                        riseDate = if (timeRegex.matches(it[1])) {
                            dateTimeFormatter.parse(it[0] + " " + it[1])
                        } else null,
                        setDate = if (timeRegex.matches(it[3])) {
                            dateTimeFormatter.parse(it[0] + " " + it[3])
                        } else null
                    )
                    sunMap[key] = value
                }
            }
        }
    }

    // TODO: Create a function to remove redundancy with above
    listOf(moon1, moon2).forEach { moon ->
        moon.data?.forEach {
            if (it.size == 4) {
                if (dateRegex.matches(it[0])) {
                    key = it[0].replace("-", "")
                    value = Astro(
                        riseDate = if (timeRegex.matches(it[1])) {
                            dateTimeFormatter.parse(it[0] + " " + it[1])
                        } else null,
                        setDate = if (timeRegex.matches(it[3])) {
                            dateTimeFormatter.parse(it[0] + " " + it[3])
                        } else null
                    )
                    moonMap[key] = value
                }
            }
        }
    }

    dailyForecast?.forEach {
        daytimeWeather = it.ForecastDailyWeather

        // If the grid forecast does not return forecast daily weather,
        // fall back to citywide daily forecast weather conditions,
        // which is preferable to 9 days of "partly cloudy"
        if (daytimeWeather == null) {
            daytimeWeather = oneJsonDailyWeather.getOrElse(it.ForecastDate) { null }
        }

        // Replace a few full day weather codes with night time equivalents for better fitting descriptions
        nightTimeWeather = when (daytimeWeather) {
            50 -> 70 // Replace "Sunny" with "Fine"
            51 -> 77 // Replace "Sunny Periods" with "Mainly Fine"
            52 -> 76 // Replace "Sunny Intervals" with "Mainly Cloudy"
            else -> daytimeWeather
        }
        dailyList.add(
            Daily(
                date = formatter.parse(it.ForecastDate)!!,
                day = HalfDay(
                    weatherText = getWeatherText(context, daytimeWeather),
                    weatherCode = getWeatherCode(daytimeWeather),
                    precipitationProbability = PrecipitationProbability(
                        total = getPrecipitationProbability(it.ForecastChanceOfRain)
                    )
                ),
                night = HalfDay(
                    weatherText = getWeatherText(context, nightTimeWeather),
                    weatherCode = getWeatherCode(nightTimeWeather),
                    precipitationProbability = PrecipitationProbability(
                        total = getPrecipitationProbability(it.ForecastChanceOfRain)
                    )
                ),
                sun = sunMap.getOrElse(it.ForecastDate) {null},
                moon = moonMap.getOrElse(it.ForecastDate) {null}
            )
        )
    }
    return dailyList
}

private fun getHourlyForecast(
    context: Context,
    hourlyWeatherForecast: List<HkoHourlyWeatherForecast>?,
    oneJson: HkoOneJsonResult
): List<HourlyWrapper> {
    val formatter = SimpleDateFormat("yyyyMMddHH", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Asia/Hong_Kong")

    val hourlyList = mutableListOf<HourlyWrapper>()
    var currentHourWeather: Int?
    var index: Int?
    val iconRegex = Regex("""^pic\d{2}\.png$""")
    val oneJsonDailyWeather = mutableMapOf<String, Int>()

    // City-wide forecast in case the grid forecast fails to return forecast weather conditions
    oneJson.F9D?.WeatherForecast?.forEach {
        if (it.ForecastDate !== null && it.ForecastIcon !== null && iconRegex.matches(it.ForecastIcon))
            oneJsonDailyWeather[it.ForecastDate] = it.ForecastIcon.substring(3, 5).toInt()
    }

    if (hourlyWeatherForecast != null) {
        for ((i, value) in hourlyWeatherForecast.withIndex()) {
            // For some reason, ForecastWeather in the output refers to the condition in the previous 3 hours.
            // Therefore we must back fill 3 hours of weather with the next known weather condition.
            currentHourWeather = null
            index = i+1
            while (currentHourWeather == null && index < hourlyWeatherForecast.size) {
                if (hourlyWeatherForecast[index].ForecastWeather != null) {
                    currentHourWeather = hourlyWeatherForecast[index].ForecastWeather
                }
                index++
            }

            // If the grid forecast does not return forecast hourly weather,
            // fall back to citywide daily forecast weather conditions,
            // which is preferable to 216 hours of "partly cloudy"
            if (currentHourWeather == null) {
                currentHourWeather = oneJsonDailyWeather.getOrElse(value.ForecastHour.substring(0, 8)) { null }
            }

            // The last hour in the output is just the condition for the previous 3 hours. Don't add to the list.
            if (i < hourlyWeatherForecast.size-1) {
                hourlyList.add(
                    HourlyWrapper(
                        date = formatter.parse(value.ForecastHour)!!,
                        weatherText = getWeatherText(context, currentHourWeather),
                        weatherCode = getWeatherCode(currentHourWeather),
                        temperature = Temperature(
                            temperature = value.ForecastTemperature
                        ),
                        wind = Wind(
                            degree = value.ForecastWindDirection,
                            speed = value.ForecastWindSpeed?.div(3.6) // convert km/h to m/s
                        ),
                        relativeHumidity = value.ForecastRelativeHumidity
                    )
                )
            }
        }
    }

    return hourlyList
}

private fun getAlertList(
    context: Context,
    warningMap: MutableMap<String, HkoWarningResult>
): List<Alert> {
    val languageKey: String
    val source: String
    val formatter = SimpleDateFormat("yyyyMMddHHmm", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("Asia/Hong_Kong")

    context.currentLocale.code.let {
        languageKey = when {
            it.startsWith("zh") -> "Val_Chi"
            else -> "Val_Eng"
        }
        source = when {
            it.startsWith("zh") -> "香港天文台"
            else -> "Hong Kong Observatory"
        }
    }
    val alertList = mutableListOf<Alert>()
    var alertDate: String
    var alertId: String
    var startDate: Date?
    var warningCode: String?
    var severity: AlertSeverity
    var headline: String?
    var color: Int?

    // Each warning type has a combination of strings.
    // The combination for each warning type are defined here:
    // https://www.hko.gov.hk/en//files/detail.js
    // We assign the combination in descriptionKeys for each type,
    // and then format accordingly.
    var warning: Map<String, Map<String, String>>?
    var descriptionKeys: List<String>
    var instructionKeys: List<String>
    var descriptionText: String?
    var instructionText: String?

    for ((key, value) in warningMap) {
        warning = when (key) {
            "WTCPRE8" -> value.DYN_DAT_MINDS_WTCPRE8
            "WTCB" -> value.DYN_DAT_MINDS_WTCB
            "WRAINSA" -> value.DYN_DAT_MINDS_WRAINSA
            "WTMW" -> value.DYN_DAT_MINDS_WTMW
            "WFNTSA" -> value.DYN_DAT_MINDS_WFNTSA
            "WMNB" -> value.DYN_DAT_MINDS_WMNB
            "WLSA" -> value.DYN_DAT_MINDS_WLSA
            "WTS" -> value.DYN_DAT_MINDS_WTS
            "WFIRE" -> value.DYN_DAT_MINDS_WFIRE
            "WHOT" -> value.DYN_DAT_MINDS_WHOT
            "WCOLD" -> value.DYN_DAT_MINDS_WCOLD
            "WFROST" -> value.DYN_DAT_MINDS_WFROST
            else -> null
        }

        alertDate = warning?.getOrElse("BulletinDate") {null}?.getOrElse(languageKey) {null} + warning?.getOrElse("BulletinTime") {null}?.getOrElse(languageKey) {null}
        alertId = "$key $alertDate"
        startDate = formatter.parse(alertDate)
        headline = null
        descriptionText = null
        instructionText = null
        severity = AlertSeverity.UNKNOWN
        color = Alert.colorFromSeverity(severity)

        if (key == "WTCPRE8") { // Pre-8 Tropical Cyclone Special Announcement
            severity = AlertSeverity.SEVERE
            color = Alert.colorFromSeverity(severity)
            headline = context.getString(R.string.hko_warning_text_tropical_cyclone_pre_8)
            descriptionKeys = listOf(
                "WTCPRE8_WxAnnouncementSpecialAnnouncementContent1",
                "WTCPRE8_WxAnnouncementSpecialAnnouncementContent2",
                "WTCPRE8_WxAnnouncementSpecialAnnouncementContent3",
                "WTCPRE8_WxAnnouncementSpecialAnnouncementContent4"
            )
            descriptionText = formatWarningText(context, warning, descriptionKeys)
        }
        if (key == "WTCB") { // Tropical Cyclone Warning Signal
            warningCode = warning?.getOrElse("WTCSGNL_WxWarningCode") {null}?.getOrElse(languageKey) {null}
            severity = when (warningCode) {
                "TC1" -> AlertSeverity.MINOR
                "TC3" -> AlertSeverity.MODERATE
                "TC8NW", "TC8SW", "TC8NE", "TC8SE" -> AlertSeverity.SEVERE
                "TC9", "TC10" -> AlertSeverity.EXTREME
                else -> AlertSeverity.UNKNOWN
            }
            color = Alert.colorFromSeverity(severity)
            headline = when (warningCode) {
                "TC1" -> context.getString(R.string.hko_warning_text_tropical_cyclone_1)
                "TC3" -> context.getString(R.string.hko_warning_text_tropical_cyclone_3)
                "TC8NW" -> context.getString(R.string.hko_warning_text_tropical_cyclone_8_northwest)
                "TC8SW" -> context.getString(R.string.hko_warning_text_tropical_cyclone_8_southwest)
                "TC8NE" -> context.getString(R.string.hko_warning_text_tropical_cyclone_8_northeast)
                "TC8SE" -> context.getString(R.string.hko_warning_text_tropical_cyclone_8_southeast)
                "TC9" -> context.getString(R.string.hko_warning_text_tropical_cyclone_9)
                "TC10" -> context.getString(R.string.hko_warning_text_tropical_cyclone_10)
                else -> null
            }
            descriptionKeys = listOf(
                "WTCB1_WxAnnouncementContent",
                "WTCB1_WxAnnouncementContent1",
                "WTCB1_WxAnnouncementContent2",
                "WTCB1_WxAnnouncementContent3",
                "WTCB1_WxAnnouncementContent4",
                "WTCB1_WxAnnouncementContent5",
                "WTCB1_WxAnnouncementContent6",
                "WTCB2_WxAnnouncementContent",
                "WTCB2_WxAnnouncementContent1",
                "WTCB2_WxAnnouncementContent2",
                "WTCB2_WxAnnouncementContent3",
                "WTCB2_WxAnnouncementContent4",
                "WTCB2_WxAnnouncementContent5",
                "WTCB2_WxAnnouncementContent6",
                "WTCB2_WxAnnouncementContent7",
                "WTCB2_WxAnnouncementContent8",
                "WTCB2_WxAnnouncementContent9",
                "WTCB2_WxAnnouncementContent10",
                "WTCB2_WxAnnouncementContent11",
                "WTCB2_WxAnnouncementContent12",
                "WTCB2_WxAnnouncementContent13",
                "WTCB2_WxAnnouncementContent14",
                "WTCB2_WxAnnouncementContent15",
                "WTCB2_WxAnnouncementContent16",
                "WTCB2_WxAnnouncementContent17",
                "WTCB2_WxAnnouncementContent18",
                "WTCB2_WxAnnouncementContent19",
                "WTCB2_WxAnnouncementContent20",
                "WTCB2_WxAnnouncementContent21",
                "WTCB2_WxAnnouncementContent22",
                "WTCB2_WxAnnouncementContent23",
                "WTCB2_WxAnnouncementContent24"
            )
            descriptionText = formatWarningText(context, warning, descriptionKeys)
            instructionKeys = listOf(
                "WTCB3_WxAnnouncementContent",
                "WTCB3_WxAnnouncementContent1",
                "WTCB3_WxAnnouncementContent2",
                "WTCB3_WxAnnouncementContent3",
                "WTCB3_WxAnnouncementContent4",
                "WTCB3_WxAnnouncementContent5",
                "WTCB3_WxAnnouncementContent6",
                "WTCB3_WxAnnouncementContent7",
                "WTCB3_WxAnnouncementContent8",
                "WTCB3_WxAnnouncementContent9",
                "WTCB3_WxAnnouncementContent10",
                "WTCB3_WxAnnouncementContent11",
                "WTCB3_WxAnnouncementContent12",
                "WTCB3_WxAnnouncementContent13",
                "WTCB3_WxAnnouncementContent14"
            )
            instructionText = formatWarningText(context, warning, instructionKeys)
        }
        if (key == "WRAINSA") { // Rainstorm Warning Signal
            warningCode = warning?.getOrElse("WRAIN_WxWarningCode") {null}?.getOrElse(languageKey) {null}
            severity = when (warningCode) {
                "WRAINA" -> AlertSeverity.MODERATE
                "WRAINR" -> AlertSeverity.SEVERE
                "WRAINB" -> AlertSeverity.EXTREME
                else -> AlertSeverity.UNKNOWN
            }
            color = getAlertColor(warningCode)
            headline = when (warningCode) {
                "WRAINA" -> context.getString(R.string.hko_warning_text_rainstorm_amber)
                "WRAINR" -> context.getString(R.string.hko_warning_text_rainstorm_red)
                "WRAINB" -> context.getString(R.string.hko_warning_text_rainstorm_black)
                else -> null
            }
            descriptionKeys = listOf(
                "WRAINSA_WxAnnouncementSpecialAnnouncementContent1",
                "WRAINSA_WxAnnouncementSpecialAnnouncementContent2",
                "WRAINSA_WxAnnouncementSpecialAnnouncementContent3",
                "WRAINSA_WxAnnouncementSpecialAnnouncementContent4",
                "WRAINSA_WxAnnouncementSpecialAnnouncementContent5",
                "WRAINSA_WxAnnouncementSpecialAnnouncementContent6",
                "WRAINSA_WxAnnouncementSpecialAnnouncementContent7",
                "WRAINSA_WxAnnouncementSpecialAnnouncementContent8",
                "WRAINSA_WxAnnouncementSpecialAnnouncementContent9",
                "WRAINSA_WxAnnouncementSpecialAnnouncementContent10"
            )
            descriptionText = formatWarningText(context, warning, descriptionKeys)
        }
        if (key == "WTMW") { // Tsunami Warning
            severity = AlertSeverity.SEVERE
            color = getAlertColor(key)
            headline = context.getString(R.string.hko_warning_text_tsunami)
            descriptionKeys = listOf(
                "WTMW_WxAnnouncementSpecialAnnouncementContent",
                "WTMW_WxAnnouncementSpecialAnnouncementContent1",
                "WTMW_WxAnnouncementSpecialAnnouncementContent2",
                "WTMW_WxAnnouncementSpecialAnnouncementContent3",
                "WTMW_WxAnnouncementSpecialAnnouncementContent4",
                "WTMW_WxAnnouncementSpecialAnnouncementContent5",
                "WTMW_WxAnnouncementSpecialAnnouncementContent6",
                "WTMW_WxAnnouncementSpecialAnnouncementContent7",
                "WTMW_WxAnnouncementSpecialAnnouncementContent8",
                "WTMW_WxAnnouncementSpecialAnnouncementContent9",
                "WTMW_WxAnnouncementSpecialAnnouncementContent10",
                "WTMW_WxAnnouncementSpecialAnnouncementContent11",
                "WTMW_WxAnnouncementSpecialAnnouncementContent12",
                "WTMW_WxAnnouncementSpecialAnnouncementContent13",
                "WTMW_WxAnnouncementSpecialAnnouncementContent14"
            )
            descriptionText = formatWarningText(context, warning, descriptionKeys)
        }
        if (key == "WFNTSA") { // Special Announcement on Flooding in Northern New Territories
            severity = AlertSeverity.SEVERE
            color = getAlertColor(key)
            headline = context.getString(R.string.hko_warning_text_flooding_northern_nt)
            descriptionKeys = listOf(
                "WFNTSA_WxWarningActionDesc",
                "WFNTSA_WxAnnouncementSpecialAnnouncementContent1",
                "WFNTSA_WxAnnouncementSpecialAnnouncementContent2",
                "WFNTSA_WxAnnouncementSpecialAnnouncementContent3",
                "WFNTSA_WxAnnouncementSpecialAnnouncementContent4",
                "WFNTSA_WxAnnouncementSpecialAnnouncementContent5"
            )
            descriptionText = formatWarningText(context, warning, descriptionKeys)
        }
        if (key == "WMNB") { // Strong Monsoon Signal
            severity = AlertSeverity.MODERATE
            color = getAlertColor(key)
            headline = context.getString(R.string.hko_warning_text_strong_monsoon)
            descriptionKeys = listOf(
                "SpecialAnnouncementContent",
                "SpecialAnnouncementContent1",
                "SpecialAnnouncementContent2",
                "SpecialAnnouncementContent3",
                "SpecialAnnouncementContent4",
                "SpecialAnnouncementContent5",
                "SpecialAnnouncementContent6"
            )
            descriptionText = formatWarningText(context, warning, descriptionKeys)
        }
        if (key == "WLSA") { // Landslip Warning
            severity = AlertSeverity.MODERATE
            color = getAlertColor(key)
            headline = context.getString(R.string.hko_warning_text_landslip)
            descriptionKeys = listOf(
                "WLSA_WxAnnouncementSpecialAnnouncementContent1",
                "WLSA_WxAnnouncementSpecialAnnouncementContent2",
                "WLSA_WxAnnouncementSpecialAnnouncementContent3",
                "WLSA_WxAnnouncementSpecialAnnouncementContent4",
                "WLSA_WxAnnouncementSpecialAnnouncementContent5",
                "WLSA_WxAnnouncementSpecialAnnouncementContent6",
                "WLSA_WxAnnouncementSpecialAnnouncementContent7"
            )
            descriptionText = formatWarningText(context, warning, descriptionKeys)
        }
        if (key == "WTS") { // Thunderstorm Warning
            severity = AlertSeverity.MODERATE
            color = getAlertColor(key)
            headline = context.getString(R.string.hko_warning_text_thunderstorm)
            descriptionKeys = listOf(
                "WTS_WxWarningActionDesc",
                "WTS_WxAnnouncementSpecialAnnouncementContent",
                "WTS_WxAnnouncementSpecialAnnouncementContent1",
                "WTS_WxAnnouncementSpecialAnnouncementContent2",
                "WTS_WxAnnouncementSpecialAnnouncementContent3",
                "WTS_WxAnnouncementSpecialAnnouncementContent4",
                "WTS_WxAnnouncementSpecialAnnouncementContent5",
                "WTS_WxAnnouncementSpecialAnnouncementContent6",
                "WTS_WxAnnouncementSpecialAnnouncementContent7",
                "WTS_WxAnnouncementSpecialAnnouncementContent8",
                "WTS_WxAnnouncementSpecialAnnouncementContent9",
                "WTS_WxAnnouncementSpecialAnnouncementContent10",
                "WTS_WxAnnouncementSpecialAnnouncementContent11"
            )
            descriptionText = formatWarningText(context, warning, descriptionKeys)
        }
        if (key == "WFIRE") { // Fire Danger Warning
            warningCode = value.DYN_DAT_MINDS_WFIRE?.getOrElse("WFIRE_WxWarningCode") {null}?.getOrElse(languageKey) {null}
            headline = when (warningCode) {
                "WFIREY" -> context.getString(R.string.hko_warning_text_fire_yellow)
                "WFIRER" -> context.getString(R.string.hko_warning_text_fire_red)
                else -> null
            }
            severity = when (warningCode) {
                "WFIREY" -> AlertSeverity.MODERATE
                "WFIRER" -> AlertSeverity.SEVERE
                else -> AlertSeverity.UNKNOWN
            }
            color = getAlertColor(warningCode)
            descriptionText = when (warningCode) {
                "WFIREY" -> context.getString(R.string.hko_warning_text_fire_yellow_description)
                "WFIRER" -> context.getString(R.string.hko_warning_text_fire_red_description)
                else -> null
            }
        }
        if (key == "WHOT") { // Very Hot Weather Warning
            severity = AlertSeverity.MODERATE
            color = getAlertColor(key)
            headline = context.getString(R.string.hko_warning_text_very_hot)
            descriptionKeys = listOf(
                "WHOT_WxWarningActionDesc",
                "WHOT_WxAnnouncementSpecialAnnouncementContent",
                "WHOT_WxAnnouncementSpecialAnnouncementContent1",
                "WHOT_WxAnnouncementSpecialAnnouncementContent2",
                "WHOT_WxAnnouncementSpecialAnnouncementContent3",
                "WHOT_WxAnnouncementSpecialAnnouncementContent4",
                "WHOT_WxAnnouncementSpecialAnnouncementContent5",
                "WHOT_WxAnnouncementSpecialAnnouncementContent6",
                "WHOT_WxAnnouncementSpecialAnnouncementContent7",
                "WHOT_WxAnnouncementSpecialAnnouncementContent8",
                "WHOT_WxAnnouncementSpecialAnnouncementContent9",
                "WHOT_WxAnnouncementSpecialAnnouncementContent10",
                "WHOT_WxAnnouncementSpecialAnnouncementContent11",
                "WHOT_WxAnnouncementSpecialAnnouncementContent12"
            )
            descriptionText = formatWarningText(context, warning, descriptionKeys)
        }
        if (key == "WCOLD") { // Cold Weather Warning
            severity = AlertSeverity.MODERATE
            color = getAlertColor(key)
            headline = context.getString(R.string.hko_warning_text_cold)
            descriptionKeys = listOf(
                "WCOLD_WxWarningActionDesc",
                "WCOLD_WxAnnouncementContent",
                "WCOLD_WxAnnouncementContent1",
                "WCOLD_WxAnnouncementContent2",
                "WCOLD_WxAnnouncementContent3",
                "WCOLD_WxAnnouncementContent4",
                "WCOLD_WxAnnouncementContent5",
                "WCOLD_WxAnnouncementContent6",
                "WCOLD_WxAnnouncementContent7"
            )
            descriptionText = formatWarningText(context, warning, descriptionKeys)
        }
        if (key == "WFROST") { // Frost Warning
            severity = AlertSeverity.SEVERE
            color = getAlertColor(key)
            headline = context.getString(R.string.hko_warning_text_frost)
            descriptionKeys = listOf("WFROST_WxAnnouncementContent")
            descriptionText = formatWarningText(context, warning, descriptionKeys)
        }

        alertList.add(
            Alert(
                alertId = alertId,
                startDate = startDate,
                headline = headline,
                description = descriptionText,
                instruction = instructionText,
                source = source,
                severity = severity,
                color = color
            )
        )
    }
    return alertList
}

private fun formatWarningText(
    context: Context,
    warning: Map<String, Map<String, String>>?,
    stringKeys: List<String>
): String {
    val languageKey = if (context.currentLocale.code.startsWith("zh")) {
        "Val_Chi"
    } else "Val_Eng"
    val multipleLineFeeds = Regex("\n{3,}")
    val strings = mutableListOf<String>()
    stringKeys.forEach {
        strings.add(warning?.getOrElse(it) {null}?.getOrElse(languageKey) {null} ?: "")
    }
    return multipleLineFeeds.replace(strings.joinToString("\n"), "\n\n").trim()
}

// Source: https://www.hko.gov.hk/textonly/v2/explain/wxicon_e.htm
private fun getWeatherText(
    context: Context,
    icon: Int?
): String? {
    return when (icon) {
        50 -> context.getString(R.string.hko_weather_text_sunny) // Sunny
        51 -> context.getString(R.string.hko_weather_text_sunny_periods) // Sunny Periods
        52 -> context.getString(R.string.hko_weather_text_sunny_intervals) // Sunny Intervals
        53 -> context.getString(R.string.hko_weather_text_sunny_periods_with_a_few_showers) // Sunny Periods with a Few Showers
        54 -> context.getString(R.string.hko_weather_text_sunny_intervals_with_showers) // Sunny Intervals with Showers
        60 -> context.getString(R.string.hko_weather_text_cloudy) // Cloudy
        61 -> context.getString(R.string.hko_weather_text_overcast) // Overcast
        62 -> context.getString(R.string.hko_weather_text_light_rain) // Light Rain
        63 -> context.getString(R.string.hko_weather_text_rain) // Rain
        64 -> context.getString(R.string.hko_weather_text_heavy_rain) // Heavy Rain
        65 -> context.getString(R.string.hko_weather_text_thunderstorms) // Thunderstorms
        70, 71, 72, 73, 74, 75 -> context.getString(R.string.hko_weather_text_fine) // Fine
        76, 701, 711, 721, 741, 751 -> context.getString(R.string.hko_weather_text_mainly_cloudy) // Mainly Cloudy
        77, 702, 712, 722, 742, 752 -> context.getString(R.string.hko_weather_text_mainly_fine) // Mainly Fine
        80 -> context.getString(R.string.hko_weather_text_windy) // Windy
        81 -> context.getString(R.string.hko_weather_text_dry) // Dry
        82 -> context.getString(R.string.hko_weather_text_humid) // Humid
        83 -> context.getString(R.string.hko_weather_text_fog) // Fog
        84 -> context.getString(R.string.hko_weather_text_mist) // Mist
        85 -> context.getString(R.string.hko_weather_text_haze) // Haze
        90 -> context.getString(R.string.hko_weather_text_hot) // Hot
        91 -> context.getString(R.string.hko_weather_text_warm) // Warm
        92 -> context.getString(R.string.hko_weather_text_cool) // Cool
        93 -> context.getString(R.string.hko_weather_text_cold) // Cold
        else -> null
    }
}

// Source: https://www.hko.gov.hk/textonly/v2/explain/wxicon_e.htm
private fun getWeatherCode(
    icon: Int?
): WeatherCode? {
    return when (icon) {
        50, 51, 70, 71, 72, 73, 74, 75,
        77, 702, 712, 722, 742, 752 -> WeatherCode.CLEAR
        52, 76, 701, 711, 721, 741, 751 -> WeatherCode.PARTLY_CLOUDY
        53, 54, 62, 63, 64 -> WeatherCode.RAIN
        60, 61 -> WeatherCode.CLOUDY
        65 -> WeatherCode.THUNDERSTORM
        // The codes below are only used in Current Observation
        // and never in hourly/daily forecasts.
        80 -> WeatherCode.WIND
        83, 84 -> WeatherCode.FOG
        85 -> WeatherCode.HAZE
        // TODO: In getCurrent, defer to hourly forecast data for the following codes?
        // 81 -> "Dry"
        // 82 -> "Humid"
        // 90 -> "Hot"
        // 91 -> "Warm"
        // 92 -> "Cool"
        // 93 -> "Cold"
        else -> null
    }
}

private fun getPrecipitationProbability(
    probability: String?
): Double? {
    return when (probability) {
        "<10%" -> 10.0
        "20%" -> 20.0
        "40%" -> 40.0
        "60%" -> 60.0
        "80%" -> 80.0
        ">90%" -> 90.0
        else -> null
    }
}

private fun getAlertColor(
    warningCode: String?,
    severity: AlertSeverity = AlertSeverity.UNKNOWN
): Int {
    return when (warningCode) {
        "WRAINY" -> Color.rgb(255, 204, 0)
        "WRAINR", "WMNB", "WFROST", "WFIRER", "WHOT" -> Color.rgb(255, 0, 0)
        "WRAINB" -> Color.rgb(0, 0, 0)
        "WTS", "WFIREY" -> Color.rgb(255, 186, 0)
        "WFNTSA" -> Color.rgb(0, 216, 89)
        "WLSA" -> Color.rgb(127, 102, 51)
        "WCOLD" -> Color.rgb(0, 0, 255)
        "WTMW" -> Color.rgb(0, 124, 188)
        else -> Alert.colorFromSeverity(severity)
    }
}
