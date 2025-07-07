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

package org.breezyweather.sources.mf

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.toColorInt
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.AlertSeverity
import breezyweather.domain.weather.model.HalfDay
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import breezyweather.domain.weather.wrappers.CurrentWrapper
import breezyweather.domain.weather.wrappers.DailyWrapper
import breezyweather.domain.weather.wrappers.HourlyWrapper
import org.breezyweather.common.extensions.plus
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.sources.mf.json.MfCurrentResult
import org.breezyweather.sources.mf.json.MfForecastDaily
import org.breezyweather.sources.mf.json.MfForecastHourly
import org.breezyweather.sources.mf.json.MfForecastProbability
import org.breezyweather.sources.mf.json.MfForecastResult
import org.breezyweather.sources.mf.json.MfNormalsResult
import org.breezyweather.sources.mf.json.MfRainResult
import org.breezyweather.sources.mf.json.MfWarningDictionaryResult
import org.breezyweather.sources.mf.json.MfWarningsOverseasResult
import org.breezyweather.sources.mf.json.MfWarningsResult
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Objects
import java.util.TimeZone
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

internal fun convert(location: Location, result: MfForecastResult): Location {
    return if (result.properties == null) {
        location
    } else {
        location.copy(
            timeZone = result.properties.timezone,
            country = result.properties.country,
            countryCode = result.properties.country.substring(0, 2),
            admin2 = if (!result.properties.frenchDepartment.isNullOrEmpty()) {
                frenchDepartments.getOrElse(result.properties.frenchDepartment) { null }
            } else {
                null
            }, // Département
            admin2Code = result.properties.frenchDepartment, // Département
            city = result.properties.name
        )
    }
}

internal fun getCurrent(currentResult: MfCurrentResult): CurrentWrapper? {
    if (currentResult.properties?.gridded == null) {
        return null
    }

    return CurrentWrapper(
        weatherText = currentResult.properties.gridded.weatherDescription,
        weatherCode = getWeatherCode(currentResult.properties.gridded.weatherIcon),
        temperature = Temperature(
            temperature = currentResult.properties.gridded.temperature
        ),
        wind = Wind(
            degree = currentResult.properties.gridded.windDirection?.toDouble(),
            speed = currentResult.properties.gridded.windSpeed
        )
    )
}

internal fun getDailyList(
    location: Location,
    dailyForecasts: List<MfForecastDaily>?,
): List<DailyWrapper> {
    if (dailyForecasts.isNullOrEmpty()) return emptyList()
    val dailyList: MutableList<DailyWrapper> = ArrayList(dailyForecasts.size)
    for (i in 0 until dailyForecasts.size - 1) {
        val dailyForecast = dailyForecasts[i]
        // Given as UTC, we need to convert in the correct timezone at 00:00
        val dayInUTCCalendar = dailyForecast.time.toCalendarWithTimeZone(TimeZone.getTimeZone("UTC"))
        val dayInLocalCalendar = Calendar.getInstance(location.javaTimeZone).apply {
            set(Calendar.YEAR, dayInUTCCalendar[Calendar.YEAR])
            set(Calendar.MONTH, dayInUTCCalendar[Calendar.MONTH])
            set(Calendar.DAY_OF_MONTH, dayInUTCCalendar[Calendar.DAY_OF_MONTH])
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val theDayInLocal = dayInLocalCalendar.time
        dailyList.add(
            DailyWrapper(
                date = theDayInLocal,
                day = HalfDay(
                    // Too complicated to get weather from hourly, so let's just use daily info for both day and night
                    weatherText = dailyForecast.dailyWeatherDescription,
                    weatherPhase = dailyForecast.dailyWeatherDescription,
                    weatherCode = getWeatherCode(dailyForecast.dailyWeatherIcon),
                    temperature = Temperature(temperature = dailyForecast.tMax)
                ),
                night = HalfDay(
                    weatherText = dailyForecast.dailyWeatherDescription,
                    weatherPhase = dailyForecast.dailyWeatherDescription,
                    weatherCode = getWeatherCode(dailyForecast.dailyWeatherIcon),
                    // tMin is for current day, so it actually takes the previous night,
                    // so we try to get tMin from next day if available
                    temperature = Temperature(temperature = dailyForecasts.getOrNull(i + 1)?.tMin)
                ),
                uV = UV(index = dailyForecast.uvIndex?.toDouble())
            )
        )
    }
    return dailyList
}

internal fun getHourlyList(
    hourlyForecastList: List<MfForecastHourly>?,
    probabilityForecastResult: List<MfForecastProbability>?,
): List<HourlyWrapper>? {
    return hourlyForecastList?.map { hourlyForecast ->
        HourlyWrapper(
            date = hourlyForecast.time,
            weatherText = hourlyForecast.weatherDescription,
            weatherCode = getWeatherCode(hourlyForecast.weatherIcon),
            temperature = Temperature(
                temperature = hourlyForecast.t,
                windChillTemperature = hourlyForecast.tWindchill
            ),
            precipitation = getHourlyPrecipitation(hourlyForecast),
            precipitationProbability = getHourlyPrecipitationProbability(
                probabilityForecastResult,
                hourlyForecast.time
            ),
            wind = Wind(
                degree = hourlyForecast.windDirection?.toDouble(),
                speed = hourlyForecast.windSpeed?.toDouble(),
                // Seems to be always 0? Or not available in low wind speeds maybe
                gusts = hourlyForecast.windSpeedGust?.toDouble()
            ),
            relativeHumidity = hourlyForecast.relativeHumidity?.toDouble(),
            pressure = hourlyForecast.pSea,
            cloudCover = hourlyForecast.totalCloudCover
        )
    }
}

private fun getHourlyPrecipitation(hourlyForecast: MfForecastHourly): Precipitation {
    val rainCumul = with(hourlyForecast) {
        rain1h ?: rain3h ?: rain6h ?: rain12h ?: rain24h
    }
    val snowCumul = with(hourlyForecast) {
        snow1h ?: snow3h ?: snow6h ?: snow12h ?: snow24h
    }
    return Precipitation(
        total = rainCumul + snowCumul,
        rain = rainCumul,
        snow = snowCumul
    )
}

/**
 * TODO: Needs to be reviewed
 */
private fun getHourlyPrecipitationProbability(
    probabilityForecastResult: List<MfForecastProbability>?,
    dt: Date,
): PrecipitationProbability? {
    if (probabilityForecastResult.isNullOrEmpty()) return null

    var rainProbability: Double? = null
    var snowProbability: Double? = null
    var iceProbability: Double? = null
    for (probabilityForecast in probabilityForecastResult) {
        /*
         * Probablity are given every 3 hours, sometimes every 6 hours.
         * Sometimes every 3 hour-schedule give 3 hours probability AND 6 hours probability,
         * sometimes only one of them
         * It's not very clear, but we take all hours in order.
         */
        if (probabilityForecast.time.time == dt.time ||
            probabilityForecast.time.time + 1.hours.inWholeMilliseconds == dt.time ||
            probabilityForecast.time.time + 2.hours.inWholeMilliseconds == dt.time
        ) {
            if (probabilityForecast.rainHazard3h != null) {
                rainProbability = probabilityForecast.rainHazard3h.toDouble()
            } else if (probabilityForecast.rainHazard6h != null) {
                rainProbability = probabilityForecast.rainHazard6h.toDouble()
            }
            if (probabilityForecast.snowHazard3h != null) {
                snowProbability = probabilityForecast.snowHazard3h.toDouble()
            } else if (probabilityForecast.snowHazard6h != null) {
                snowProbability = probabilityForecast.snowHazard6h.toDouble()
            }
            if (probabilityForecast.freezingHazard != null) {
                iceProbability = probabilityForecast.freezingHazard.toDouble()
            }
        }

        /*
         * If it's found as part of the "6 hour schedule" and we find later a "3 hour schedule"
         * the "3 hour schedule" will overwrite the "6 hour schedule" below with the above
         */
        if (probabilityForecast.time.time + 3.hours.inWholeMilliseconds == dt.time ||
            probabilityForecast.time.time + 4.hours.inWholeMilliseconds == dt.time ||
            probabilityForecast.time.time + 5.hours.inWholeMilliseconds == dt.time
        ) {
            if (probabilityForecast.rainHazard6h != null) {
                rainProbability = probabilityForecast.rainHazard6h.toDouble()
            }
            if (probabilityForecast.snowHazard6h != null) {
                snowProbability = probabilityForecast.snowHazard6h.toDouble()
            }
            if (probabilityForecast.freezingHazard != null) {
                iceProbability = probabilityForecast.freezingHazard.toDouble()
            }
        }
    }
    return PrecipitationProbability(
        maxOf(rainProbability ?: 0.0, snowProbability ?: 0.0, iceProbability ?: 0.0),
        null,
        rainProbability,
        snowProbability,
        iceProbability
    )
}

internal fun getMinutelyList(rainResult: MfRainResult?): List<Minutely> {
    val minutelyList: MutableList<Minutely> = arrayListOf()
    rainResult?.properties?.rainForecasts?.forEachIndexed { i, rainForecast ->
        minutelyList.add(
            Minutely(
                date = rainForecast.time,
                minuteInterval = if (i < rainResult.properties.rainForecasts.size - 1) {
                    (rainResult.properties.rainForecasts[i + 1].time.time - rainForecast.time.time)
                        .div(1.minutes.inWholeMilliseconds)
                        .toDouble().roundToInt()
                } else {
                    (rainForecast.time.time - rainResult.properties.rainForecasts[i - 1].time.time)
                        .div(1.minutes.inWholeMilliseconds)
                        .toDouble().roundToInt()
                },
                precipitationIntensity = if (rainForecast.rainIntensity != null) {
                    getPrecipitationIntensity(rainForecast.rainIntensity)
                } else {
                    null
                }
            )
        )
    }
    return minutelyList
}

internal fun getOverseasWarningsList(
    warningsDictionaryResult: MfWarningDictionaryResult,
    warningsResult: MfWarningsOverseasResult,
): List<Alert> {
    val alertList: MutableList<Alert> = arrayListOf()
    warningsResult.text?.let {
        if (!it.textBlocItems.isNullOrEmpty()) {
            val title = "Bulletin de Vigilance météo"
            val content = StringBuilder()
            it.textBlocItems.forEach { textBlocItem ->
                if (content.toString().isNotEmpty()) {
                    content.append("\n\n")
                }
                textBlocItem.title?.forEach { t ->
                    content
                        .append(t.uppercase(Locale.FRENCH))
                        .append("\n")
                }
                textBlocItem.text?.forEach { txt ->
                    content
                        .append(txt)
                        .append("\n")
                }
            }
            alertList.add(
                Alert(
                    // Create unique ID from: alert type ID, alert level, start time
                    alertId = Objects.hash(title, warningsResult.colorMax, it.beginTime).toString(),
                    startDate = it.beginTime,
                    endDate = it.endTime,
                    headline = title,
                    description = content.toString(),
                    source = "Météo-France",
                    severity = AlertSeverity.EXTREME, // Let’s put it on top
                    color = warningsDictionaryResult.colors?.firstOrNull { c -> c.id == warningsResult.colorMax }
                        ?.hexaCode?.toColorInt()
                        ?: Alert.colorFromSeverity(AlertSeverity.UNKNOWN)
                )
            )
        }
    }
    warningsResult.timelaps?.forEach { timelaps ->
        timelaps.timelapsItems
            ?.filter { it.colorId > 1 }
            ?.forEach { timelapsItem ->
                val consequences = warningsResult.consequences?.firstOrNull { it.phenomenonId == timelaps.phenomenonId }
                    ?.textConsequence?.replace("<br>", "\n")
                val advices = warningsResult.advices?.firstOrNull { it.phenomenonId == timelaps.phenomenonId }
                    ?.textAdvice?.replace("<br>", "\n")

                val content = StringBuilder()
                if (!consequences.isNullOrEmpty()) {
                    if (content.toString().isNotEmpty()) {
                        content.append("\n\n")
                    }
                    // TODO: Move to non-translatable en/fr strings
                    content
                        .append("CONSÉQUENCES POSSIBLES\n")
                        .append(consequences)
                }
                if (!advices.isNullOrEmpty()) {
                    if (content.toString().isNotEmpty()) {
                        content.append("\n\n")
                    }
                    // TODO: Move to non-translatable en/fr strings
                    content
                        .append("CONSEILS DE COMPORTEMENT\n")
                        .append(advices)
                }

                alertList.add(
                    Alert(
                        // Create unique ID from: alert type ID, alert level, start time
                        alertId = Objects.hash(timelaps.phenomenonId, timelapsItem.colorId, timelapsItem.beginTime.time)
                            .toString(),
                        startDate = timelapsItem.beginTime,
                        endDate = timelapsItem.endTime,
                        headline = warningsDictionaryResult.phenomenons
                            ?.firstOrNull { c -> c.id == timelaps.phenomenonId }
                            ?.name
                            ?: getWarningType(timelaps.phenomenonId.toString()),
                        description = content.toString(),
                        source = "Météo-France",
                        severity = warningsDictionaryResult.colors?.firstOrNull { c -> c.id == timelapsItem.colorId }
                            ?.name?.let { h ->
                                with(h) {
                                    when {
                                        contains("rouge") || contains("violet") -> AlertSeverity.EXTREME
                                        contains("orange") -> AlertSeverity.SEVERE
                                        contains("jaune") || contains("blanc") -> AlertSeverity.MODERATE
                                        contains("vert") || contains("bleu") -> AlertSeverity.MINOR
                                        else -> AlertSeverity.UNKNOWN
                                    }
                                }
                            } ?: AlertSeverity.UNKNOWN,
                        color = warningsDictionaryResult.colors?.firstOrNull { c -> c.id == timelapsItem.colorId }
                            ?.hexaCode?.toColorInt()
                            ?: Alert.colorFromSeverity(AlertSeverity.UNKNOWN)
                    )
                )
            }
    }
    return alertList
}

internal fun getWarningsList(warningsJ0Result: MfWarningsResult, warningsJ1Result: MfWarningsResult): List<Alert> {
    return getMergedBulletinWarning(warningsJ0Result, warningsJ1Result) +
        getWarningsList(warningsJ0Result) +
        getWarningsList(warningsJ1Result)
}

internal fun getMergedBulletinWarning(
    warningsJ0Result: MfWarningsResult,
    warningsJ1Result: MfWarningsResult,
): List<Alert> {
    if (warningsJ0Result.text == null && warningsJ1Result.text == null) return emptyList()

    val warningBulletinJ0 = getBulletinWarning(warningsJ0Result)
    val warningBulletinJ1 = getBulletinWarning(warningsJ1Result)

    return if (warningBulletinJ0 != null && warningBulletinJ1 != null) {
        if (warningBulletinJ0.headline == warningBulletinJ1.headline &&
            warningBulletinJ0.startDate == warningBulletinJ1.startDate &&
            warningBulletinJ0.color == warningBulletinJ1.color &&
            warningBulletinJ0.description == warningBulletinJ1.description
        ) {
            // In case bulletins are identical, let's show the one from J1 which has a later validity end date
            listOf(warningBulletinJ1)
        } else {
            listOf(warningBulletinJ0, warningBulletinJ1)
        }
    } else if (warningBulletinJ0 != null) {
        listOf(warningBulletinJ0)
    } else if (warningBulletinJ1 != null) {
        listOf(warningBulletinJ1)
    } else {
        emptyList()
    }
}

fun getBulletinWarning(warningsResult: MfWarningsResult): Alert? {
    return warningsResult.text?.let {
        if (warningsResult.updateTime != null) {
            val textBlocs = it.textBlocItems?.filter { textBlocItem ->
                textBlocItem.textItems?.any { textItem -> textItem.hazardCode == null } == true
            }
            if (!textBlocs.isNullOrEmpty()) {
                val colors = mutableListOf<String>()
                textBlocs.forEach { textBlocItem ->
                    textBlocItem.textItems?.forEach { textItem ->
                        if (textItem.hazardCode == null) {
                            textItem.termItems?.forEach { termItem ->
                                if (!termItem.riskName.isNullOrEmpty()) {
                                    colors.add(termItem.riskName)
                                }
                            }
                        }
                    }
                }
                val color = getWarningColor(colors)
                val title = it.blocTitle ?: "Bulletin de Vigilance météo"
                Alert(
                    // Create unique ID from: alert type ID, alert level, start time
                    alertId = Objects.hash(title, color, warningsResult.updateTime).toString(),
                    startDate = warningsResult.updateTime,
                    endDate = warningsResult.endValidityTime,
                    headline = title,
                    description = getWarningContent(null, warningsResult),
                    source = "Météo-France",
                    severity = AlertSeverity.EXTREME, // Let’s put it on top
                    color = color
                )
            } else {
                null
            }
        } else {
            null
        }
    }
}

private fun getWarningsList(warningsResult: MfWarningsResult): List<Alert> {
    val alertList: MutableList<Alert> = arrayListOf()
    warningsResult.timelaps?.forEach { timelaps ->
        timelaps.timelapsItems
            ?.filter { it.colorId > 1 }
            ?.forEach { timelapsItem ->
                alertList.add(
                    Alert(
                        // Create unique ID from: alert type ID, alert level, start time
                        alertId = Objects.hash(timelaps.phenomenonId, timelapsItem.colorId, timelapsItem.beginTime.time)
                            .toString(),
                        startDate = timelapsItem.beginTime,
                        endDate = timelapsItem.endTime,
                        headline = getWarningType(timelaps.phenomenonId) + " — " + getWarningText(timelapsItem.colorId),
                        description = if (timelapsItem.colorId >= 3) {
                            getWarningContent(timelaps.phenomenonId, warningsResult)
                        } else {
                            null
                        },
                        source = "Météo-France",
                        severity = AlertSeverity.getInstance(timelapsItem.colorId),
                        color = getWarningColor(timelapsItem.colorId)
                    )
                )
            }
    }
    return alertList
}

internal fun getNormals(location: Location, normalsResult: MfNormalsResult): Normals? {
    val currentMonth = Date().toCalendarWithTimeZone(location.javaTimeZone)[Calendar.MONTH]
    val normalsStats = normalsResult.properties?.stats?.getOrNull(currentMonth)
    return if (normalsStats != null) {
        Normals(
            month = currentMonth,
            daytimeTemperature = normalsStats.tMax,
            nighttimeTemperature = normalsStats.tMin
        )
    } else {
        null
    }
}

private fun getPrecipitationIntensity(rain: Int): Double = when (rain) {
    4 -> Precipitation.PRECIPITATION_HOURLY_HEAVY
    3 -> Precipitation.PRECIPITATION_HOURLY_MEDIUM
    2 -> Precipitation.PRECIPITATION_HOURLY_LIGHT
    else -> 0.0
}

// TODO: Move to non-translatable en/fr strings
private fun getWarningType(phemononId: String): String = when (phemononId) {
    "1" -> "Vent"
    "2" -> "Pluie-inondation"
    "3" -> "Orages"
    "4" -> "Crues"
    "5" -> "Neige-verglas"
    "6" -> "Canicule"
    "7" -> "Grand froid"
    "8" -> "Avalanches"
    "9" -> "Vagues-submersion"
    else -> "Divers"
}

// TODO: Move to non-translatable en/fr strings
private fun getWarningText(colorId: Int): String = when (colorId) {
    4 -> "Vigilance absolue"
    3 -> "Soyez très vigilant"
    2 -> "Soyez attentif"
    else -> "Pas de vigilance particulière"
}

@ColorInt
private fun getWarningColor(colors: List<String>): Int = when {
    colors.contains("Rouge") -> Color.rgb(204, 0, 0)
    colors.contains("Orange") -> Color.rgb(255, 184, 43)
    colors.contains("Jaune") -> Color.rgb(255, 246, 0)
    colors.contains("Vert") -> Color.rgb(49, 170, 53)
    else -> Alert.colorFromSeverity(AlertSeverity.UNKNOWN)
}

@ColorInt
private fun getWarningColor(colorId: Int): Int = when (colorId) {
    4 -> Color.rgb(204, 0, 0)
    3 -> Color.rgb(255, 184, 43)
    2 -> Color.rgb(255, 246, 0)
    1 -> Color.rgb(49, 170, 53)
    else -> Alert.colorFromSeverity(AlertSeverity.UNKNOWN)
}

private fun getWarningContent(phenomenonId: String?, warningsResult: MfWarningsResult): String? {
    val textBlocs = warningsResult.text?.textBlocItems?.filter { textBlocItem ->
        textBlocItem.textItems?.any { it.hazardCode == phenomenonId } == true
    }
    val consequences = warningsResult.consequences?.firstOrNull { it.phenomenonId == phenomenonId }
        ?.textConsequence?.replace("<br>", "\n")
    val advices = warningsResult.advices?.firstOrNull { it.phenomenonId == phenomenonId }
        ?.textAdvice?.replace("<br>", "\n")

    val content = StringBuilder()
    if (!textBlocs.isNullOrEmpty()) {
        textBlocs.forEach { textBlocItem ->
            if (content.toString().isNotEmpty()) {
                content.append("\n\n")
            }
            if (!textBlocItem.typeName.isNullOrEmpty()) {
                content
                    .append(textBlocItem.typeName.uppercase(Locale.FRENCH))
                    .append("\n")
            }
            textBlocItem.textItems?.filter { it.hazardCode == phenomenonId }?.forEach { textItem ->
                textItem.termItems?.forEach { termItem ->
                    termItem.subdivisionTexts?.forEach { subdivisionText ->
                        subdivisionText.text?.let {
                            content.append(it.joinToString("\n"))
                        }
                    }
                }
            }
        }
    }
    if (!consequences.isNullOrEmpty()) {
        if (content.toString().isNotEmpty()) {
            content.append("\n\n")
        }
        // TODO: Move to non-translatable en/fr strings
        content
            .append("CONSÉQUENCES POSSIBLES\n")
            .append(consequences)
    }
    if (!advices.isNullOrEmpty()) {
        if (content.toString().isNotEmpty()) {
            content.append("\n\n")
        }
        // TODO: Move to non-translatable en/fr strings
        content
            .append("CONSEILS DE COMPORTEMENT\n")
            .append(advices)
    }

    return content.toString().ifEmpty { null }
}

private fun getWeatherCode(icon: String?): WeatherCode? {
    return if (icon == null) {
        null
    } else {
        with(icon) {
            when {
                // We need to take care of two-digits first
                startsWith("p32") ||
                    startsWith("p33") ||
                    startsWith("p34") -> WeatherCode.WIND
                startsWith("p31") -> null // What is this?
                startsWith("p26") ||
                    startsWith("p27") ||
                    startsWith("p28") ||
                    startsWith("p29") -> WeatherCode.THUNDER
                startsWith("p26") ||
                    startsWith("p27") ||
                    startsWith("p28") ||
                    startsWith("p29") -> WeatherCode.THUNDER
                startsWith("p21") ||
                    startsWith("p22") ||
                    startsWith("p23") -> WeatherCode.SNOW
                startsWith("p19") ||
                    startsWith("p20") -> WeatherCode.HAIL
                startsWith("p17") ||
                    startsWith("p18") -> WeatherCode.SLEET
                startsWith("p16") ||
                    startsWith("p24") ||
                    startsWith("p25") ||
                    startsWith("p30") -> WeatherCode.THUNDERSTORM
                startsWith("p9") ||
                    startsWith("p10") ||
                    startsWith("p11") ||
                    startsWith("p12") ||
                    startsWith("p13") ||
                    startsWith("p14") ||
                    startsWith("p15") -> WeatherCode.RAIN
                startsWith("p6") ||
                    startsWith("p7") ||
                    startsWith("p8") -> WeatherCode.FOG
                startsWith("p4") ||
                    startsWith("p5") -> WeatherCode.HAZE
                startsWith("p3") -> WeatherCode.CLOUDY
                startsWith("p2") -> WeatherCode.PARTLY_CLOUDY
                startsWith("p1") -> WeatherCode.CLEAR
                else -> null
            }
        }
    }
}

internal val frenchDepartments: Map<String, String> = mapOf(
    "01" to "Ain",
    "02" to "Aisne",
    "03" to "Allier",
    "04" to "Alpes de Hautes-Provence",
    "05" to "Hautes-Alpes",
    "06" to "Alpes-Maritimes",
    "07" to "Ardèche",
    "08" to "Ardennes",
    "09" to "Ariège",
    "10" to "Aube",
    "11" to "Aude",
    "12" to "Aveyron",
    "13" to "Bouches-du-Rhône",
    "14" to "Calvados",
    "15" to "Cantal",
    "16" to "Charente",
    "17" to "Charente-Maritime",
    "18" to "Cher",
    "19" to "Corrèze",
    "21" to "Côte-d'Or",
    "22" to "Côtes d'Armor",
    "23" to "Creuse",
    "24" to "Dordogne",
    "25" to "Doubs",
    "26" to "Drôme",
    "27" to "Eure",
    "28" to "Eure-et-Loir",
    "29" to "Finistère",
    "2A" to "Corse-du-Sud",
    "2B" to "Haute-Corse",
    "30" to "Gard",
    "31" to "Haute-Garonne",
    "32" to "Gers",
    "33" to "Gironde",
    "34" to "Hérault",
    "35" to "Ille-et-Vilaine",
    "36" to "Indre",
    "37" to "Indre-et-Loire",
    "38" to "Isère",
    "39" to "Jura",
    "40" to "Landes",
    "41" to "Loir-et-Cher",
    "42" to "Loire",
    "43" to "Haute-Loire",
    "44" to "Loire-Atlantique",
    "45" to "Loiret",
    "46" to "Lot",
    "47" to "Lot-et-Garonne",
    "48" to "Lozère",
    "49" to "Maine-et-Loire",
    "50" to "Manche",
    "51" to "Marne",
    "52" to "Haute-Marne",
    "53" to "Mayenne",
    "54" to "Meurthe-et-Moselle",
    "55" to "Meuse",
    "56" to "Morbihan",
    "57" to "Moselle",
    "58" to "Nièvre",
    "59" to "Nord",
    "60" to "Oise",
    "61" to "Orne",
    "62" to "Pas-de-Calais",
    "63" to "Puy-de-Dôme",
    "64" to "Pyrénées-Atlantiques",
    "65" to "Hautes-Pyrénées",
    "66" to "Pyrénées-Orientales",
    "67" to "Bas-Rhin",
    "68" to "Haut-Rhin",
    "69" to "Rhône",
    "70" to "Haute-Saône",
    "71" to "Saône-et-Loire",
    "72" to "Sarthe",
    "73" to "Savoie",
    "74" to "Haute-Savoie",
    "75" to "Paris",
    "76" to "Seine-Maritime",
    "77" to "Seine-et-Marne",
    "78" to "Yvelines",
    "79" to "Deux-Sèvres",
    "80" to "Somme",
    "81" to "Tarn",
    "82" to "Tarn-et-Garonne",
    "83" to "Var",
    "84" to "Vaucluse",
    "85" to "Vendée",
    "86" to "Vienne",
    "87" to "Haute-Vienne",
    "88" to "Vosges",
    "89" to "Yonne",
    "90" to "Territoire-de-Belfort",
    "91" to "Essonne",
    "92" to "Hauts-de-Seine",
    "93" to "Seine-Saint-Denis",
    "94" to "Val-de-Marne",
    "95" to "Val-d'Oise",
    "99" to "Andorre"
)

internal val overseaTerritories: Map<String, String> = mapOf(
    "971" to "Guadeloupe",
    "972" to "Martinique",
    "973" to "Guyane",
    "974" to "La Réunion",
    "975" to "Saint-Pierre-et-Miquelon",
    "976" to "Mayotte",
    "977" to "Saint-Barthélemy",
    "978" to "Saint-Martin",
    "986" to "Wallis-et-Futuna",
    "987" to "Polynésie française",
    "988" to "Nouvelle-Calédonie"
)
